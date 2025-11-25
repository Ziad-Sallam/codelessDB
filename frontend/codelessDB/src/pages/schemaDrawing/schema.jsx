import React, { useState, useCallback } from "react";
import {
  addEdge,
  MiniMap,
  Controls,
  Background,
  ReactFlow,
  applyNodeChanges,
  applyEdgeChanges,
} from "@xyflow/react";
import "@xyflow/react/dist/style.css";
import { nodeTypes, edgeTypes } from "./index";
import "./Schema.css";
import applyRelationLogic from "./connectingLogic/ConnectingLogic";

export default function Schema() {
  const [nodes, setNodes] = useState([]);
  const [edges, setEdges] = useState([]);
  const [selectedRelationType, setSelectedRelationType] = useState("1:N");

  const onNodesChange = useCallback(
    (changes) => setNodes((ns) => applyNodeChanges(changes, ns)),
    []
  );
  const onEdgesChange = useCallback(
    (changes) => setEdges((es) => applyEdgeChanges(changes, es)),
    []
  );

  const onConnect = useCallback(
    (params) => {
      if (!params || !params.source || !params.target) return;

      applyRelationLogic(
        params.source,
        params.target,
        selectedRelationType,
        nodes,
        setNodes,
        setEdges
      );

      const typeKey =
        {
          "1:1": "oneToOne",
          "1:N": "oneToMany",
          "N:1": "manyToOne",
          "M:N": "manyToMany",
        }[selectedRelationType] || "oneToMany";

      const newEdge = {
        source: params.source,
        target: params.target,
        id: `e_${params.source}_${params.target}_${Date.now()}`,
        type: typeKey,
        // markerEnd: { type: MarkerType.ArrowClosed, width: 20, height: 20 },
        data: { type: selectedRelationType },
      };
      setEdges((eds) => addEdge(newEdge, eds));
    },
    [selectedRelationType, nodes]
  );

  const addNode = () => {
    const id = `${nodes.length + 1}_${Date.now()}`;
    setNodes((nds) => [
      ...nds,
      {
        id,
        type: "Defult-Node",
        data: {
          tableName: `Entity_${nodes.length + 1}`,
          columns: [
            {
              id: `attr1_${id}`,
              name: "id",
              dataType: "INT",
              constraints: { PRIMARY_KEY: true },
            },
          ],
        },
        position: { x: Math.random() * 400, y: Math.random() * 400 },
      },
    ]);
  };

  return (
    <div
      style={{
        width: "100vw",
        height: "100vh",
        position: "relative",
        background: "#f8fafc",
      }}
    >
      <ReactFlow
        nodes={nodes}
        edges={edges}
        onNodesChange={onNodesChange}
        onEdgesChange={onEdgesChange}
        onConnect={onConnect}
        nodeTypes={nodeTypes}
        edgeTypes={edgeTypes}
        fitView
        connectionMode="loose"
        defaultEdgeOptions={{
          style: { strokeWidth: 2, stroke: "#94a3b8" },
        }}
      >
        <MiniMap
          style={{ borderRadius: 8, border: "1px solid #e2e8f0" }}
          nodeColor="#cbd5e1"
          maskColor="rgba(241, 245, 249, 0.6)"
        />
        <Controls
          style={{
            borderRadius: 8,
            overflow: "hidden",
            border: "none",
            boxShadow: "0 4px 6px -1px rgba(0,0,0,0.1)",
          }}
        />
        <Background color="#cbd5e1" gap={20} size={1} />
      </ReactFlow>

      {/* NEW TOOLBAR STRUCTURE */}
      <div className="schema-toolbar">
        <RelationButton
          active={selectedRelationType === "1:1"}
          color="#3b82f6"
          onClick={() => setSelectedRelationType("1:1")}
          label="1 : 1"
        />
        <RelationButton
          active={selectedRelationType === "1:N"}
          color="#10b981"
          onClick={() => setSelectedRelationType("1:N")}
          label="1 : N"
        />
        <RelationButton
          active={selectedRelationType === "N:1"}
          color="#f59e0b"
          onClick={() => setSelectedRelationType("N:1")}
          label="N : 1"
        />
        <RelationButton
          active={selectedRelationType === "M:N"}
          color="#ef4444"
          onClick={() => setSelectedRelationType("M:N")}
          label="M : N"
        />
        <div
          style={{
            width: 1,
            height: 24,
            background: "#e2e8f0",
            margin: "0 4px",
          }}
        ></div>
        <button className="add-node-btn" onClick={addNode}>
          + Add Entity
        </button>
      </div>
      <button className="generate">Generate SQL</button>
    </div>
  );
}

// Updated Button Component to use new classes
function RelationButton({ active, color, onClick, label }) {
  return (
    <button
      onClick={onClick}
      className={`relation-btn ${active ? "active" : ""}`}
      style={active ? { borderColor: color, color: color } : {}}
    >
      <span
        className="relation-color-indicator"
        style={{ background: color }}
      />
      {label}
    </button>
  );
}
