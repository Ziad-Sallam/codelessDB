import { WebSocketServer } from 'ws';
import http from 'http';
import * as Y from 'yjs';
import axios from 'axios';

// --- CHANGE: Import from local file ---
import { setupWSConnection } from './ws-handler.js'; 
// --------------------------------------

const port = 1234;
const server = http.createServer((request, response) => {
    response.writeHead(200, { 'Content-Type': 'text/plain' });
    response.end('Y.js Sidecar Service Running');
});

const wss = new WebSocketServer({ server });

const SPRING_API_URL = 'http://localhost:8080/api/diagrams'; 
const SAVE_THRESHOLD = 100; 

const docs = new Map();

const saveToBackend = async (roomId, doc) => {
    try {
        const stateVector = Y.encodeStateAsUpdate(doc);
        const buffer = Buffer.from(stateVector);

        await axios.put(`${SPRING_API_URL}/${roomId}/snapshot`, buffer, {
            headers: { 'Content-Type': 'application/octet-stream' },
            maxContentLength: Infinity,
            maxBodyLength: Infinity
        });
        console.log(`[${roomId}] Saved snapshot to Spring Boot (${buffer.length} bytes).`);
    } catch (error) {
        console.error(`[${roomId}] Failed to save to backend:`, error.message);
    }
};

const getYDoc = async (roomId) => {
    if (docs.has(roomId)) {
        return docs.get(roomId);
    }

    const doc = new Y.Doc();
    
    // --- NEW: Setup Awareness (Cursors) ---
    // This allows the custom handler to sync cursors
    doc.awareness = new (await import('y-protocols/awareness')).Awareness(doc);
    // --------------------------------------

    const entry = { doc, updateCount: 0, activeConns: 0, saveDebounce: null };
    docs.set(roomId, entry);

    try {
        const response = await axios.get(`${SPRING_API_URL}/${roomId}/snapshot`, {
            responseType: 'arraybuffer'
        });
        
        if (response.data && response.data.length > 0) {
            Y.applyUpdate(doc, new Uint8Array(response.data));
            console.log(`[${roomId}] Loaded initial data from backend.`);
        }
    } catch (error) {
        if (error.response && error.response.status === 404) {
            console.log(`[${roomId}] New diagram.`);
        } else {
            console.error(`[${roomId}] Error fetching data:`, error.message);
        }
    }

    doc.on('update', (update, origin) => {
        entry.updateCount++;

        if (entry.updateCount >= SAVE_THRESHOLD) {
            console.log(`[${roomId}] Threshold reached. Saving...`);
            saveToBackend(roomId, doc);
            entry.updateCount = 0; 
            if (entry.saveDebounce) clearTimeout(entry.saveDebounce);
        } else {
            if (entry.saveDebounce) clearTimeout(entry.saveDebounce);
            entry.saveDebounce = setTimeout(() => {
                console.log(`[${roomId}] Auto-save timer.`);
                saveToBackend(roomId, doc);
                entry.updateCount = 0;
            }, 5000);
        }
    });

    return entry;
};

wss.on('connection', async (ws, req) => {
    const roomId = req.url.slice(1).split('?')[0]; 
    if (!roomId) {
        ws.close();
        return;
    }

    const entry = await getYDoc(roomId);
    entry.activeConns++;

    console.log(`[${roomId}] Client connected. Total: ${entry.activeConns}`);

    // --- CHANGE: Call local handler with 'ws' and 'doc' ---
    setupWSConnection(ws, entry.doc);
    // -----------------------------------------------------

    ws.on('close', () => {
        entry.activeConns--;
        console.log(`[${roomId}] Client disconnected. Remaining: ${entry.activeConns}`);

        if (entry.activeConns <= 0) {
            console.log(`[${roomId}] Last user left. Flushing...`);
            saveToBackend(roomId, entry.doc).then(() => {
                if (entry.saveDebounce) clearTimeout(entry.saveDebounce);
                entry.doc.destroy();
                docs.delete(roomId);
                console.log(`[${roomId}] Memory cleared.`);
            });
        }
    });
});

server.listen(port, () => {
    console.log(`Y.js Helper running on port ${port}`);
});