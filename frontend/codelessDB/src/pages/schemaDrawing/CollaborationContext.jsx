// CollaborationContext.js
import React, { createContext, useContext, useEffect, useState, useCallback ,useRef} from "react";
import * as Y from "yjs";
import { WebsocketProvider } from "y-websocket";
import { applyNodeChanges, applyEdgeChanges } from "@xyflow/react";
import throttle from "lodash/throttle";

const CollaborationContext = createContext(null);

const getRandomColor = () => '#' + Math.floor(Math.random()*16777215).toString(16);
const getRandomName = () => `User ${Math.floor(Math.random() * 100)}`;

export const CollaborationProvider = ({ roomId, children }) => {
  const [nodes, setNodes] = useState([]);
  const [edges, setEdges] = useState([]);
  const [cursors, setCursors] = useState([]);

  const [ydoc, setYdoc] = useState(null);
  const [provider, setProvider] = useState(null);
  const [currentUser] = useState({ name: getRandomName(), color: getRandomColor() });

  const pendingUpdates = useRef(new Map());

  useEffect(() => {
    const doc = new Y.Doc();
    const nodesMap = doc.getMap("nodes");
    const edgesMap = doc.getMap("edges");
    console.log(roomId)
    // Connect to Spring Boot
    const wsProvider = new WebsocketProvider(
      `ws://localhost:8080/ws/collab/`,
      roomId,
      doc
    );

    setYdoc(doc);
    setProvider(wsProvider);

    // --- AWARENESS SETUP (Cursors) ---
    const awareness = wsProvider.awareness;

    // 1. Set MY local details (so others see me)
    awareness.setLocalStateField('user', currentUser);

    // 2. Listen for OTHERS changing
    const handleAwarenessChange = () => {
      const states = awareness.getStates(); // Map<ClientId, State>
      const cursorList = [];

      states.forEach((state, clientId) => {
        // Ignore my own cursor
        if (clientId === awareness.clientID) return;

        if (state.cursor && state.user) {
          cursorList.push({
            id: clientId,
            x: state.cursor.x,
            y: state.cursor.y,
            name: state.user.name,
            color: state.user.color,
          });
        }
      });
      setCursors(cursorList);
    };

    awareness.on('change', handleAwarenessChange);

    // Observer: Sync Yjs -> React
    const observer = () => {
      setNodes(Array.from(nodesMap.values()));
      setEdges(Array.from(edgesMap.values()));
    };

    nodesMap.observeDeep(observer); // observeDeep detects nested data changes
    edgesMap.observeDeep(observer);

    return () => {
      awareness.off('change', handleAwarenessChange);
      wsProvider.destroy();
      doc.destroy();
    };
  }, [roomId,currentUser]);

  const updateCursor = useCallback(
    throttle((x, y) => {
      if (provider && provider.awareness) {
        provider.awareness.setLocalStateField('cursor', { x, y });
      }
    }, 50),
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
        });
      });

      // Clear the buffer after sending
      pendingUpdates.current.clear();
    }, 50), // <-- 50ms Throttle Time (Adjust as needed)
    [ydoc]
  );

  // --- ACTIONS ---

  // 1. Update Node Data (Used by Node.jsx)
  const updateNodeData = useCallback((nodeId, newData) => {
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
          ...newData
        }
      };
      // Save back to Yjs (Triggering sync)
      nodesMap.set(nodeId, updatedNode);
    }
  }, [ydoc]);

  // 2. React Flow Hooks
  const onNodesChange = useCallback((changes) => {
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
      } 
      else if (change.type === "remove") {
        nodesMap.delete(change.id);
        pendingUpdates.current.delete(change.id); // Remove from buffer if deleted
      } 
      else if (change.type === "add") {
        nodesMap.set(change.item.id, change.item);
      }
    });
  }, [ydoc, flushUpdatesToYjs]);

  const onEdgesChange = useCallback((changes) => {
    if (!ydoc) return;
    const edgesMap = ydoc.getMap("edges");
    setEdges((es) => applyEdgeChanges(changes, es));

    changes.forEach((change) => {
      if (change.type === "remove") {
        edgesMap.delete(change.id);
      }
    });
  }, [ydoc]);


  const addNodeYjs = (node) => {
      if(!ydoc) return;
      ydoc.getMap('nodes').set(node.id, node);
  }

  const addEdgeYjs = (edge) => {
    if(!ydoc) return;
    ydoc.getMap('edges').set(edge.id, edge);
  }

  return (
    <CollaborationContext.Provider
      value={{
        nodes,
        edges,
        cursors,
        updateCursor,
        onNodesChange,
        onEdgesChange,
        updateNodeData, // <--- This is what Node.jsx needs
        addNodeYjs,
        addEdgeYjs,
        provider
      }}
    >
      {children}
    </CollaborationContext.Provider>
  );
};

export const useCollaboration = () => useContext(CollaborationContext);