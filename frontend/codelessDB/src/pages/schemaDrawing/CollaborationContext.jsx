// CollaborationContext.js
import React, { createContext, useContext, useEffect, useState, useCallback } from "react";
import * as Y from "yjs";
import { WebsocketProvider } from "y-websocket";
import { applyNodeChanges, applyEdgeChanges } from "@xyflow/react";

const CollaborationContext = createContext(null);

export const CollaborationProvider = ({ roomId, children }) => {
  const [nodes, setNodes] = useState([]);
  const [edges, setEdges] = useState([]);
  const [ydoc, setYdoc] = useState(null);
  const [provider, setProvider] = useState(null);

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

    // Observer: Sync Yjs -> React
    const observer = () => {
      setNodes(Array.from(nodesMap.values()));
      setEdges(Array.from(edgesMap.values()));
    };

    nodesMap.observeDeep(observer); // observeDeep detects nested data changes
    edgesMap.observeDeep(observer);

    return () => {
      wsProvider.destroy();
      doc.destroy();
    };
  }, [roomId]);

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

    // Optimistic UI update
    setNodes((ns) => applyNodeChanges(changes, ns));

    changes.forEach((change) => {
      if (change.type === "position" && change.position) {
        const node = nodesMap.get(change.id);
        if (node) {
          nodesMap.set(change.id, { ...node, position: change.position });
        }
      
      } else if (change.type === "remove") {
        nodesMap.delete(change.id);
      
      } else if (change.type === "select") {
        // Optional: Handle selection
      }
    });
  }, [ydoc]);

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