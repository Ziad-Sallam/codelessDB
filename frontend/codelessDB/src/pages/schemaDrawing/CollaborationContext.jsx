// CollaborationContext.js
import React, {
	createContext,
	useContext,
	useEffect,
	useState,
	useCallback,
	useRef,
} from "react";
import * as Y from "yjs";
import { UndoManager } from "yjs";
import { WebsocketProvider } from "y-websocket";
import { applyNodeChanges, applyEdgeChanges } from "@xyflow/react";
import throttle from "lodash/throttle";
import { delay } from "lodash";
import { useAuth } from "../../components/AuthProvider";

const CollaborationContext = createContext(null);

const base64ToBytes = (base64) => {
	if (!base64 || typeof base64 !== 'string') return new Uint8Array(0);

	try {
		// 1. Strip whitespace/newlines just in case
		const cleanBase64 = base64.replace(/\s/g, '');

		// 2. Decode
		const binaryString = window.atob(cleanBase64);
		const len = binaryString.length;
		const bytes = new Uint8Array(len);
		for (let i = 0; i < len; i++) {
			bytes[i] = binaryString.charCodeAt(i);
		}
		return bytes;
	} catch (error) {
		console.error("❌ Failed to decode Base64 string:", error);
		return new Uint8Array(0); // Return empty safety buffer
	}
};

export const CollaborationProvider = ({ roomId, children }) => {
	const { user } = useAuth();
	const [nodes, setNodes] = useState([]);
	const [edges, setEdges] = useState([]);
	const [schemaName, setSchemaName] = useState("Untitled Schema");
	const [cursors, setCursors] = useState([]);
	const [connectedUsers, setConnectedUsers] = useState([]);
	const [ydoc, setYdoc] = useState(null);
	const [provider, setProvider] = useState(null);
	const [undoManager, setUndoManager] = useState(null);


	const getRandomColor = () =>
		"#" + Math.floor(Math.random() * 16777215).toString(16);
	const getUserName = user.username;
	const getUserPicture = user.picture;
	const [currentUser] = useState({
		name: getUserName,
		picture: getUserPicture,
		color: getRandomColor(),
	});

	const pendingUpdates = useRef(new Map());

	useEffect(() => {
		const doc = new Y.Doc();
		const nodesMap = doc.getMap("nodes");
		const edgesMap = doc.getMap("edges");
		const metaMap = doc.getMap("meta");

		// Connect to Spring Boot
		const wsProvider = new WebsocketProvider(
			`ws://localhost:8080/ws/collab/`,
			roomId,
			doc,
			{
				params: {
					token: `${localStorage.getItem("authToken")}`,
				},
			}
		);

		setYdoc(doc);
		setProvider(wsProvider);

		const mgr = new UndoManager([nodesMap, edgesMap], {
			captureTimeout: 500, // Group changes occurring within 500ms into one undo step (helps with dragging)
		});

		setUndoManager(mgr);

		// --- AWARENESS SETUP (Cursors) ---
		const awareness = wsProvider.awareness;

		// 1. Set MY local details (so others see me)
		awareness.setLocalStateField("user", currentUser);

		// 2. Listen for OTHERS changing
		const handleAwarenessChange = () => {
			const states = awareness.getStates(); // Map<ClientId, State>

			const newCursors = [];
			const newUsers = []; // <--- Temp array for user list

			states.forEach((state, clientId) => {
				if (state.user) {
					// A. Build User List (Include Self)
					newUsers.push({
						id: clientId,
						name: state.user.name,
						picture: state.user.picture,
						color: state.user.color,
						isMe: clientId === awareness.clientID, // Flag to identify self
					});

					// B. Build Cursor List (Exclude Self)
					if (clientId !== awareness.clientID && state.cursor) {
						newCursors.push({
							id: clientId,
							x: state.cursor.x,
							y: state.cursor.y,
							name: state.user.name,
							color: state.user.color,
						});
					}
				}
			});

			setCursors(newCursors);
			setConnectedUsers(newUsers); // <--- Update the UI list
		};

		awareness.on("change", handleAwarenessChange);

		// Observer: Sync Yjs -> React
		const observer = () => {
			setNodes(Array.from(nodesMap.values()));
			setEdges(Array.from(edgesMap.values()));

			const syncedName = metaMap.get("name");
			if (syncedName) {
				setSchemaName(syncedName);
			}
		};

		nodesMap.observeDeep(observer); // observeDeep detects nested data changes
		edgesMap.observeDeep(observer);
		metaMap.observeDeep(observer);

		return () => {
			awareness.off("change", handleAwarenessChange);
			wsProvider.destroy();
			doc.destroy();
		};
	}, [roomId, currentUser]);

	const loadCompositeYjsData = useCallback((snapshotBase64, updatesListBase64) => {
		if (!ydoc) return;

		ydoc.transact(() => {
			// 1. Try applying the Snapshot (Database)
			if (snapshotBase64) {
				try {
					const snapshotBytes = base64ToBytes(snapshotBase64);
					if (snapshotBytes.byteLength > 0) {
						Y.applyUpdate(ydoc, snapshotBytes);
						console.log(`✅ Snapshot applied (${snapshotBytes.byteLength} bytes)`);
					}
				} catch (err) {
					console.error("❌ CRITICAL: Database Snapshot is corrupt!", err);
				}
			}

			// 2. Try applying Redis Updates
			if (updatesListBase64 && Array.isArray(updatesListBase64)) {
				updatesListBase64.forEach((updateBase64, index) => {
					try {
						const updateBytes = base64ToBytes(updateBase64);
						if (updateBytes.byteLength > 0) {
							Y.applyUpdate(ydoc, updateBytes);
						}
						console.log(`✅ Processed ${updatesListBase64.length} Redis updates`);
					
					} catch (err) {
						console.error(`❌ Redis Update #${index} is corrupt! Skipping.`, err);
						// We catch the error here so one bad update doesn't crash the whole app
					}
				});
			}
		});
	}, [ydoc]);


	const undo = useCallback(() => {
		if (undoManager) {
			undoManager.undo();
		}
	}, [undoManager]);

	const redo = useCallback(() => {
		if (undoManager) {
			undoManager.redo();
		}
	}, [undoManager]);

	const lastCursorRef = useRef({ x: 0, y: 0 });

	const updateCursor = useCallback(
		throttle((x, y) => {
			if (provider && provider.awareness) {

				const newX = Math.round(x);
				const newY = Math.round(y);

				if (
					Math.abs(newX - lastCursorRef.current.x) < 2 &&
					Math.abs(newY - lastCursorRef.current.y) < 2
				) {
					return;
				}

				lastCursorRef.current = { x: newX, y: newY };

				provider.awareness.setLocalStateField("cursor", { x: newX, y: newY });
			}
		}, 100),
		[provider]
	);

	const flushUpdatesToYjs = useCallback(
		throttle(() => {
			if (!ydoc || pendingUpdates.current.size === 0) return;

			const nodesMap = ydoc.getMap("nodes");

			// Transact ensures all updates go out as ONE message
			ydoc.transact(() => {
				pendingUpdates.current.forEach((position, id) => {
					const node = nodesMap.get(id);
					if (node) {
						// Only update if position actually changed
						nodesMap.set(id, { ...node, position });
					}
					// delay(1)
				});
			});

			// Clear the buffer after sending
			pendingUpdates.current.clear();
		}, 100), // <-- 50ms Throttle Time (Adjust as needed)
		[ydoc]
	);

	// --- ACTIONS ---

	// 1. Update Node Data (Used by Node.jsx)
	const updateNodeData = useCallback(
		(nodeId, newData) => {
			if (!ydoc) return;
			const nodesMap = ydoc.getMap("nodes");

			// Get the current node object from Yjs
			const currentNode = nodesMap.get(nodeId);

			if (currentNode) {
				// Merge existing data with new data
				const updatedNode = {
					...currentNode,
					data: {
						...currentNode.data,
						...newData,
					},
				};
				// Save back to Yjs (Triggering sync)
				nodesMap.set(nodeId, updatedNode);
			}
		},
		[ydoc]
	);

	// 2. React Flow Hooks
	const onNodesChange = useCallback(
		(changes) => {
			if (!ydoc) return;
			const nodesMap = ydoc.getMap("nodes");

			setNodes((ns) => applyNodeChanges(changes, ns));

			changes.forEach((change) => {
				//  Network Update (THROTTLED)
				if (change.type === "position" && change.position) {
					// Add to buffer
					pendingUpdates.current.set(change.id, change.position);
					// Trigger the throttled flush
					flushUpdatesToYjs();
				} else if (change.type === "remove") {
					nodesMap.delete(change.id);
					pendingUpdates.current.delete(change.id); // Remove from buffer if deleted
				} else if (change.type === "add") {
					nodesMap.set(change.item.id, change.item);
				}
			});
		},
		[ydoc, flushUpdatesToYjs]
	);

	const onEdgesChange = useCallback(
		(changes) => {
			if (!ydoc) return;
			const edgesMap = ydoc.getMap("edges");
			setEdges((es) => applyEdgeChanges(changes, es));

			changes.forEach((change) => {
				if (change.type === "remove") {
					edgesMap.delete(change.id);
				}
			});
		},
		[ydoc]
	);

	const addNodeYjs = (node) => {
		if (!ydoc) return;
		ydoc.getMap("nodes").set(node.id, node);
	};

	const addEdgeYjs = (edge) => {
		if (!ydoc) return;
		ydoc.getMap("edges").set(edge.id, edge);
	};

	const updateSchemaName = useCallback((name) => {
		if (!ydoc) return;
		ydoc.getMap("meta").set("name", name);
	}, [ydoc]);

	return (
		<CollaborationContext.Provider
			value={{
				ydoc,
				nodes,
				edges,
				schemaName,
				updateSchemaName,
				cursors,
				connectedUsers,
				updateCursor,
				onNodesChange,
				onEdgesChange,
				updateNodeData,
				addNodeYjs,
				addEdgeYjs,
				provider,
				loadCompositeYjsData,
				undo,
				redo
			}}
		>
			{children}
		</CollaborationContext.Provider>
	);
};

export const useCollaboration = () => useContext(CollaborationContext);
