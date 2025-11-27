import React, { useState, useCallback, useEffect } from "react";
import { useParams } from "react-router-dom";
import {
  addEdge,
  MiniMap,
  Controls,
  Background,
  ReactFlow,
  applyNodeChanges,
  applyEdgeChanges,
  useReactFlow,
  
} from "@xyflow/react";
import "@xyflow/react/dist/style.css";
import { nodeTypes, edgeTypes } from "./index";
import "./Schema.css";
import applyRelationLogic from "./connectingLogic/ConnectingLogic";
import { validateSchema } from "./generate/CheckCorrectness";
import { convertToJSON } from "./generate/JsonConverter";
import CodeEditor from "./code-editor/CodeEditor.jsx";
import axios from "axios";
import {
  generateSQLFromBackend,
  updateDiagram,
  fetchDiagram,
} from "./fetch.js";
import { useNotification } from "../../components/NotificationContext";
import { uploadToCloudinary } from "../../components/uploadImage.js";

// 1. IMPORT HTML-TO-IMAGE
import { toPng } from 'html-to-image';

export default function Schema() {
  const { showSuccess, showError, showWarning } = useNotification();
  const { id } = useParams();

  const [nodes, setNodes] = useState([]);
  const [edges, setEdges] = useState([]);
  const [selectedRelationType, setSelectedRelationType] = useState("1:N");
  const [isSqlPanelOpen, setIsSqlPanelOpen] = useState(false);
  const [generatedSql, setGeneratedSql] = useState("");
  const [schemaName, setSchemaName] = useState("");
  const [isReadOnly, setIsReadOnly] = useState(false);
  const [isSaving, setIsSaving] = useState(false);
  const [reactFlowInstance, setReactFlowInstance] = useState(null);

  const loadDiagram = async () => {
    try {
      const response = await fetchDiagram(id);
      const content = JSON.parse(response.content || "{}");
      setNodes(content.nodes || []);
      setEdges(content.edges || []);
      setSchemaName(response.name);
      setIsReadOnly(response.role == "READER" ? true : false);
    } catch (err) {
      showError && showError(err?.message || String(err));
    }
  };

  useEffect(() => {
    loadDiagram();
  }, []);

  const takeSnapshot = async () => {
    
    const viewport = document.querySelector('.react-flow__viewport');

    await reactFlowInstance.fitView({ padding: 50 });

    if (!viewport) return;

    try {
      const thumbnail = await toPng(viewport, {
        backgroundColor: "#ffffff",
        quality: 1
      });


      return thumbnail;
    } catch (err) {
      console.log("Error exporting:", err);
    }
  };

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

  const onSaveDiagram = async () => {
    if (!id) {
      showError("Diagram ID is missing. Cannot save.");
      return;
    }

    setIsSaving(true);

    const thumbnailPNG = await takeSnapshot();

    const thumbnailURL = await uploadToCloudinary(thumbnailPNG, id);

    const payload = {
      name: schemaName,
      jsonContent: JSON.stringify({ nodes, edges }),
      thumbnail: thumbnailURL
    };

    try {
      await updateDiagram(id, payload);
      // Generate and upload snapshot after saving data
      await takeSnapshot(); 
      showSuccess("Diagram saved successfully!");
    } catch (err) {
      console.error("Error saving diagram:", err);
      showError("Failed to save diagram.");
    } finally {
      setIsSaving(false);
    }
  };

  const onGenerateSQL = async () => {
    const validation = validateSchema(nodes);

    if (!validation.isValid) {
      showError(`Validation Failed:\n- ${validation.errors.join("\n- ")}`);
      return;
    }

    const finalJson = convertToJSON(schemaName, nodes);

    try {
      const response = await generateSQLFromBackend(finalJson);
      setGeneratedSql(response.data);
    } catch (err) {
      console.log("ERROR:", err);
    }

    setIsSqlPanelOpen(true);
  };

  return (
    <div className="drawing-container">
      {isSqlPanelOpen && (
        <CodeEditor
          initialCode={generatedSql}
          onClose={() => setIsSqlPanelOpen(false)}
        />
      )}
      <div className="header">
        <input
          placeholder="Database Name"
          value={schemaName}
          onChange={(e) => setSchemaName(e.target.value)}
          disabled={isReadOnly}
        />
        {!isReadOnly && (
          <button
            className="save-btn"
            onClick={onSaveDiagram}
            disabled={isSaving}
          >
            {isSaving ? "Saving..." : "Save Diagram"}
          </button>
        )}
      </div>
      <ReactFlow
        onInit={(instance) => setReactFlowInstance(instance)}
        nodes={nodes}
        edges={edges}
        onNodesChange={onNodesChange}
        onEdgesChange={onEdgesChange}
        onConnect={onConnect}
        nodeTypes={nodeTypes}
        edgeTypes={edgeTypes}
        fitView
        connectionMode="loose"
        nodesDraggable={!isReadOnly}
        nodesConnectable={!isReadOnly}
        elementsSelectable={!isReadOnly}
        zoomOnDoubleClick={!isReadOnly}
        defaultEdgeOptions={{
          style: { strokeWidth: 2, stroke: "#94a3b8" },
        }}
      >
        <MiniMap
          style={{ borderRadius: 8, border: "1px solid #e2e8f0" }}
          nodeColor="#cbd5e1"
          maskColor="rgba(241, 245, 249, 0.6)"
        />
        {!isReadOnly && (
          <Controls
            style={{
              borderRadius: 8,
              overflow: "hidden",
              border: "none",
              boxShadow: "0 4px 6px -1px rgba(0,0,0,0.1)",
            }}
          />
        )}
        <Background color="#cbd5e1" gap={20} size={1} />
      </ReactFlow>

      {!isSqlPanelOpen && (
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
      )}
      <button className="generate" onClick={onGenerateSQL}>
        Generate SQL
      </button>
    </div>
  );
}

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