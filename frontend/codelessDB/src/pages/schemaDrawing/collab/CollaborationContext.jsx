import throttle from "lodash/throttle";
import {
	createContext,
	useCallback,
	useContext,
	useEffect,
	useRef,
	useState,
} from "react";

import { WebsocketProvider } from "y-websocket";
import * as Y from "yjs";
import { UndoManager } from "yjs";
import { useAuth } from "../../../components/AuthProvider";

const CollaborationContext = createContext(null);

const base64ToBytes = (base64) => {
	if (!base64 || typeof base64 !== "string") return new Uint8Array(0);
	try {
		const clean = base64.replace(/\s/g, "");
		const binary = window.atob(clean);
		const bytes = new Uint8Array(binary.length);
		for (let i = 0; i < binary.length; i++) {
			bytes[i] = binary.charCodeAt(i);
		}
		return bytes;
	} catch (err) {
		console.error("❌ Failed to decode snapshot", err);
		return new Uint8Array(0);
	}
};

// ----------------- Provider -----------------
export const CollaborationProvider = ({ roomId, children }) => {
	const { user } = useAuth();

	const [nodes, setNodes] = useState([]);
	const [edges, setEdges] = useState([]);
	const [schemaName, setSchemaName] = useState("Untitled Schema");
	const [cursors, setCursors] = useState([]);
	const [connectedUsers, setConnectedUsers] = useState([]);

	const ydocRef = useRef(null);
	const providerRef = useRef(null);
	const undoManagerRef = useRef(null);

	// Buffer for batched node positions
	const pendingPositions = useRef(new Map());

	// ----------------- Setup -----------------
	useEffect(() => {
		const doc = new Y.Doc();
		const nodesMap = doc.getMap("nodes");
		const edgesMap = doc.getMap("edges");
		const metaMap = doc.getMap("meta");

		ydocRef.current = doc;
		console.log("Empty doc: ")
		console.log(Y.encodeStateAsUpdate(doc));

		const provider = new WebsocketProvider(
			`ws://localhost:8080/ws/collab`,
			roomId,
			doc,
			{
				params: { token: localStorage.getItem("authToken") },
			}
		);

		providerRef.current = provider;

		// Undo (ignore position-only transactions)
		undoManagerRef.current = new UndoManager(
			[nodesMap, edgesMap],
			{
				captureTimeout: 500,
				trackedOrigins: new Set(["user"]),
			}
		);

		// ----------------- Awareness -----------------
		const awareness = provider.awareness;

		const localUser = {
			name: user.username,
			picture: user.picture,
			color: "#" + Math.floor(Math.random() * 16777215).toString(16),
		};

		awareness.setLocalStateField("user", localUser);

		const handleAwarenessChange = () => {
			const states = awareness.getStates();
			const newCursors = [];
			const newUsers = [];

			states.forEach((state, clientId) => {
				if (!state.user) return;

				newUsers.push({
					id: clientId,
					...state.user,
					isMe: clientId === awareness.clientID,
				});

				if (state.cursor && clientId !== awareness.clientID) {
					newCursors.push({
						id: clientId,
						...state.cursor,
						name: state.user.name,
						color: state.user.color,
					});
				}
			});

			setConnectedUsers(newUsers);
			setCursors(newCursors);
		};

		awareness.on("change", handleAwarenessChange);

		// ----------------- Yjs → React Sync -----------------
		const syncObserver = () => {
			setNodes(Array.from(nodesMap.values()));
			setEdges(Array.from(edgesMap.values()));

			const name = metaMap.get("name");
			if (name) setSchemaName(name);
		};

		nodesMap.observe(syncObserver);
		edgesMap.observe(syncObserver);
		metaMap.observe(syncObserver);

		return () => {
			const arr = Y.encodeStateAsUpdate(doc);
			// console.log("Snap: ", arr);
			console.log("Snap: ", Array.from(new Uint8Array(arr), x => x > 127 ? x - 256 : x));

			awareness.setLocalState(null);
			awareness.off("change", handleAwarenessChange);
			provider.destroy();
			doc.destroy();
		};
	}, []);

	// ----------------- Snapshot Loader -----------------
	// snapshotBytes is Uint8Array
	const applySnapshot = useCallback((snapshotBytes) => {
		const ydoc = ydocRef.current;
		if (!ydoc) return;

		ydoc.transact(() => {

			if (snapshotBytes && snapshotBytes.byteLength > 0) {
				try {
					Y.applyUpdate(ydoc, snapshotBytes);
					console.log(`✅ Snapshot applied (${snapshotBytes.byteLength} bytes)`);
				
				} catch (err) {
					console.error("❌ CRITICAL: Database Snapshot is corrupt!", err);
				}
			}
		});
	}, []);

	// ----------------- Cursor Updates -----------------
	const lastCursor = useRef({ x: 0, y: 0 });

	const updateCursor = useRef(
		throttle((x, y) => {
			const provider = providerRef.current;
			if (!provider) return;

			const nx = Math.round(x);
			const ny = Math.round(y);

			if (
				Math.abs(nx - lastCursor.current.x) < 2 &&
				Math.abs(ny - lastCursor.current.y) < 2
			) {
				return;
			}

			lastCursor.current = { x: nx, y: ny };
			provider.awareness.setLocalStateField("cursor", { x: nx, y: ny });
		}, 100)
	).current;

	// ----------------- Batched Position Flush -----------------
	const flushPositions = useRef(
		throttle(() => {
			const doc = ydocRef.current;
			if (!doc || pendingPositions.current.size === 0) return;

			const nodesMap = doc.getMap("nodes");

			doc.transact(() => {
				pendingPositions.current.forEach((position, id) => {
					const node = nodesMap.get(id);
					if (node) {
						nodesMap.set(id, { ...node, position });
					}
				});
			}, "position");

			pendingPositions.current.clear();
		}, 50)
	).current;

	// ----------------- React Flow Handlers -----------------
	const onNodesChange = useCallback((changes) => {
		const doc = ydocRef.current;
		if (!doc) return;

		const nodesMap = doc.getMap("nodes");

		changes.forEach((change) => {
			if (change.type === "position" && change.position) {
				pendingPositions.current.set(change.id, change.position);
				flushPositions();

			} else if (change.type === "remove") {
				nodesMap.delete(change.id);
				pendingPositions.current.delete(change.id);

			} else if (change.type === "add") {
				nodesMap.set(change.item.id, change.item);
			}
		});
	}, []);

	const onEdgesChange = useCallback((changes) => {
		const doc = ydocRef.current;
		if (!doc) return;

		const edgesMap = doc.getMap("edges");

		changes.forEach((change) => {
			if (change.type === "remove") {
				edgesMap.delete(change.id);
			}
		});
	}, []);

	// ----------------- Data Mutators -----------------
	const updateNodeData = useCallback((id, newData) => {
		const doc = ydocRef.current;
		if (!doc) return;

		const nodesMap = doc.getMap("nodes");
		const node = nodesMap.get(id);
		if (!node) return;

		doc.transact(() => {
			nodesMap.set(id, {
				...node,
				data: { ...node.data, ...newData },
			});
		}, "user");
	}, []);

	const addNodeYjs = (node) => {
		ydocRef.current?.getMap("nodes").set(node.id, node);
	};

	const addEdgeYjs = (edge) => {
		ydocRef.current?.getMap("edges").set(edge.id, edge);
	};

	const updateSchemaName = useCallback((name) => {
		ydocRef.current?.getMap("meta").set("name", name);
	}, []);

	// ----------------- Undo / Redo -----------------
	const undo = () => undoManagerRef.current?.undo();
	const redo = () => undoManagerRef.current?.redo();

	// ----------------- Context -----------------
	return (
		<CollaborationContext.Provider
			value={{
				nodes,
				edges,
				schemaName,
				cursors,
				connectedUsers,
				updateCursor,
				onNodesChange,
				onEdgesChange,
				updateNodeData,
				addNodeYjs,
				addEdgeYjs,
				updateSchemaName,
				applySnapshot,
				undo,
				redo,
			}}
		>
			{children}
		</CollaborationContext.Provider>
	);
};

export const useCollaboration = () =>
	useContext(CollaborationContext);
