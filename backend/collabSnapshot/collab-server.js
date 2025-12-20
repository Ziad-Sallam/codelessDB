import { WebSocketServer } from 'ws';
import http from 'http';
import * as Y from 'yjs';
import axios from 'axios';
import { setupWSConnection, setupAwarenessBroadcasting } from './ws-handler.js';

const port = 1234;
const SPRING_API_URL = 'http://localhost:8080/api/diagrams'; 
const SAVE_THRESHOLD = 100; 

// --- 1. HTTP Server ---
const server = http.createServer((request, response) => {
    response.writeHead(200, { 'Content-Type': 'text/plain' });
    response.end('Y.js Gatekeeper Running');
});

// --- 2. WebSocket Server (Detached) ---
// We set noServer: true so we can handle the upgrade manually after Auth
const wss = new WebSocketServer({ noServer: true });

// Document Store
const docs = new Map();

// --- Helper: Authenticate ---
const authenticate = async (roomId, request) => {
    try {
        const url = new URL(request.url, `http://${request.headers.host}`);
        const token = url.searchParams.get('token');
        
        if (!token) return null;

        const response = await axios.get(`${SPRING_API_URL}/${roomId}/permission`, {
            headers: { Authorization: `Bearer ${token}` }
        });
        return response.data; // "WRITER" or "READER"
    } catch (error) {
        console.error(`[${roomId}] Auth check failed: ${error.message}`);
        return null;
    }
};

// --- Helper: Save to Spring Boot ---
const saveToBackend = async (roomId, doc) => {
    try {
        const stateVector = Y.encodeStateAsUpdate(doc);
        const buffer = Buffer.from(stateVector);

        await axios.put(`${SPRING_API_URL}/${roomId}/snapshot`, buffer, {
            headers: { 'Content-Type': 'application/octet-stream' },
            maxContentLength: Infinity,
            maxBodyLength: Infinity
        });
        console.log(`[${roomId}] Saved snapshot.`);
    } catch (error) {
        if (error.response) console.error(`[${roomId}] Save Error:`, error.response.status);
        else console.error(`[${roomId}] Save Connection Error:`, error.message);
    }
};

// --- Helper: Get/Create Doc ---
const getYDoc = async (roomId) => {
    if (docs.has(roomId)) return docs.get(roomId);

    const doc = new Y.Doc();
    doc.awareness = new (await import('y-protocols/awareness')).Awareness(doc);
    
    // Track connections for Awareness Broadcasting
    const conns = new Set();
    
    const entry = { doc, conns, updateCount: 0, activeConns: 0, saveDebounce: null };
    docs.set(roomId, entry);

    // Setup Broadcasting (ONCE per document)
    setupAwarenessBroadcasting(doc, conns);

    // Load Data
    try {
        const response = await axios.get(`${SPRING_API_URL}/${roomId}/snapshot`, { responseType: 'arraybuffer' });
        if (response.data && response.data.length > 0) {
            Y.applyUpdate(doc, new Uint8Array(response.data));
            console.log(`[${roomId}] Loaded initial data.`);
        }
    } catch (e) { /* Ignore 404 */ }

    // Auto-Save Logic
    doc.on('update', () => {
        entry.updateCount++;
        if (entry.updateCount >= SAVE_THRESHOLD) {
            saveToBackend(roomId, doc);
            entry.updateCount = 0; 
            if (entry.saveDebounce) clearTimeout(entry.saveDebounce);
        } else {
            if (entry.saveDebounce) clearTimeout(entry.saveDebounce);
            entry.saveDebounce = setTimeout(() => {
                saveToBackend(roomId, doc);
                entry.updateCount = 0;
            }, 5000);
        }
    });

    return entry;
};

// --- 3. HANDLE UPGRADE (The Fix for "Abnormal Behavior") ---
server.on('upgrade', async (request, socket, head) => {
    // A. Extract Room ID
    const urlParts = request.url.split('?');
    const roomId = urlParts[0].slice(1); // Remove leading '/'

    if (!roomId) {
        socket.destroy();
        return;
    }

    // B. AUTHENTICATE HERE (Before accepting connection)
    const role = await authenticate(roomId, request);

    if (!role) {
        console.log(`[${roomId}] Connection rejected (Unauthorized)`);
        socket.write('HTTP/1.1 401 Unauthorized\r\n\r\n');
        socket.destroy();
        return;
    }

    // C. Accept Connection
    wss.handleUpgrade(request, socket, head, (ws) => {
        // Pass the role to the connection event
        ws.role = role;
        ws.roomId = roomId;
        wss.emit('connection', ws, request);
    });
});

// --- 4. HANDLE CONNECTION (Logic is now synchronous/safe) ---
wss.on('connection', async (ws, req) => {
    const { roomId, role } = ws; // Passed from upgrade handler

    console.log(`[${roomId}] User connected as ${role}`);

    const entry = await getYDoc(roomId);
    
    // Add to awareness list
    entry.conns.add(ws);
    entry.activeConns++;

    // Setup Sync Logic
    setupWSConnection(ws, entry.doc);

    // Cleanup
    ws.on('close', () => {
        entry.conns.delete(ws);
        entry.activeConns--;
        
        if (entry.activeConns <= 0) {
            console.log(`[${roomId}] Last user left. Flushing...`);
            saveToBackend(roomId, entry.doc).then(() => {
                if (entry.saveDebounce) clearTimeout(entry.saveDebounce);
                entry.doc.destroy();
                docs.delete(roomId);
            });
        }
    });
});

server.listen(port, () => {
    console.log(`Y.js Gatekeeper running on port ${port}`);
});