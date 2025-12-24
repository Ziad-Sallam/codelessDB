import throttle from "lodash/throttle";
import { applyNodeChanges, applyEdgeChanges } from "@xyflow/react";
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

const BACKEND_WS_URL = import.meta.env.VITE_BACKEND_WS_URL;

function hslToHex(h, s, l) {
	s /= 100;
	l /= 100;

	const k = n => (n + h / 30) % 12;
	const a = s * Math.min(l, 1 - l);
	const f = n =>
		l - a * Math.max(-1, Math.min(k(n) - 3, Math.min(9 - k(n), 1)));

	return (
		"#" +
		[f(0), f(8), f(4)]
			.map(x => Math.round(255 * x).toString(16).padStart(2, "0"))
			.join("")
	);
}

function getReadableRandomHex() {
	const h = Math.floor(Math.random() * 360);
	const s = 65 + Math.random() * 20;
	const l = 45 + Math.random() * 10;

	return hslToHex(h, s, l);
}


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

		const provider = new WebsocketProvider(
			`${BACKEND_WS_URL}/ws/collab`,
			roomId,
			doc,
			{
				params: { token: localStorage.getItem("authToken") },
			}
		);

		providerRef.current = provider;

		// ----------------- Undo Configuration -----------------
		
		undoManagerRef.current = new UndoManager(
			[nodesMap, edgesMap],
			{
				captureTimeout: 500, 
				trackedOrigins: new Set([null, "user", "position"]),
			}
		);

		// ----------------- Awareness -----------------
		const awareness = provider.awareness;

		const localUser = {
			name: user.username,
			picture: user.picture,
			color: getReadableRandomHex(),
		};

		// Set local user state
		awareness.setLocalStateField("user", localUser);

		const handleAwarenessChange = () => {
			const states = awareness.getStates();
			const newCursors = [];

			const uniqueUsersMap = new Map();

			states.forEach((state, clientId) => {
				if (!state.user) return;

				if (!uniqueUsersMap.has(state.user.name)) {
					uniqueUsersMap.set(state.user.name, {
						clientId: clientId,
						...state.user,
						isMe: state.user.name === user.username,
					});
				}

				if (state.cursor && clientId !== awareness.clientID) {
					newCursors.push({
						id: clientId,
						...state.cursor,
						name: state.user.name,
						color: state.user.color,
					});
				}
			});

			setConnectedUsers(Array.from(uniqueUsersMap.values()));
			setCursors(newCursors);
		};

		awareness.on("change", handleAwarenessChange);

		// ----------------- Yjs → React Sync -----------------
		const syncObserver = (event, transaction) => {
			
			if (transaction && transaction.origin === "position") {
				return;
			}

			setNodes((prevNodes) => {
				const yNodes = Array.from(nodesMap.values());
				const prevMap = new Map((prevNodes || []).map((n) => [n.id, n]));

				return yNodes.map((yNode) => {
					const prevNode = prevMap.get(yNode.id);
					if (prevNode) {
						return {
							...prevNode,
							...yNode,
							// Strictly prioritize local position if dragging
							position: prevNode.dragging ? prevNode.position : yNode.position,
							selected: prevNode.selected,
							dragging: prevNode.dragging,
						};
					}
					return yNode;
				});
			});

			setEdges((prevEdges) => {
				const yEdges = Array.from(edgesMap.values());
				const prevMap = new Map((prevEdges || []).map((e) => [e.id, e]));

				return yEdges.map((yEdge) => {
					const prevEdge = prevMap.get(yEdge.id);
					if (prevEdge) {
						return {
							...prevEdge,
							...yEdge,
							selected: prevEdge.selected,
						};
					}
					return yEdge;
				});
			});

			const name = metaMap.get("name");
			if (name) setSchemaName(name);
		};

		nodesMap.observe(syncObserver);
		edgesMap.observe(syncObserver);
		metaMap.observe(syncObserver);

		// ----------------- Cleanup -----------------
		const handleBeforeUnload = () => {
			awareness.setLocalState(null);
			provider.disconnect();
		};

		window.addEventListener("beforeunload", handleBeforeUnload);

		return () => {
			window.removeEventListener("beforeunload", handleBeforeUnload);
			awareness.setLocalState(null);
			awareness.off("change", handleAwarenessChange);
			provider.destroy();
			doc.destroy();
		};
	}, [roomId, user]);

	// ----------------- Snapshot Loader -----------------
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
		// 1. Immediate Local Update (Smoother Dragging)
		setNodes((nds) => applyNodeChanges(changes, nds));

		const doc = ydocRef.current;
		if (!doc) return;

		const nodesMap = doc.getMap("nodes");

		doc.transact(() => {
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
		});
	}, []);

	const onEdgesChange = useCallback((changes) => {
		// 1. Immediate Local Update
		setEdges((eds) => applyEdgeChanges(changes, eds));

		const doc = ydocRef.current;
		if (!doc) return;

		const edgesMap = doc.getMap("edges");

		// 2. Sync to Yjs
		doc.transact(() => {
			changes.forEach((change) => {
				if (change.type === "remove") {
					edgesMap.delete(change.id);
				}
			});
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
	const undo = () => {
		undoManagerRef.current?.undo();
	};

	const redo = () => {
		undoManagerRef.current?.redo();
	};

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