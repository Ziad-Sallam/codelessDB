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
import {
  generateSQLFromBackend,
  updateDiagram,
  fetchDiagram,
} from "./fetch.js";
import { useNotification } from "../../components/NotificationContext";
import { uploadToCloudinary } from "../../components/uploadImage.js";

// 1. IMPORT HTML-TO-IMAGE
import { toPng } from 'html-to-image';
import Toolbar from "./ConnectionControls.jsx";

import { CollaborationProvider, useCollaboration } from "./CollaborationContext.jsx";


const SchemaContent = () => {
  
  const { showSuccess, showError, showWarning } = useNotification();
  
  // 3. USE THE CONTEXT
  const { 
      nodes, edges, onNodesChange, onEdgesChange, 
      addNodeYjs, addEdgeYjs, updateNodeData 
  } = useCollaboration();

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

  

  const onConnect = useCallback(
    (params) => {
      if (!params || !params.source || !params.target) return;

      applyRelationLogic(
        params.source,
        params.target,
        selectedRelationType,
        nodes,
        {updateNodeData,addNodeYjs,addEdgeYjs}
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
      addEdgeYjs(newEdge);
    },
    [selectedRelationType, addEdgeYjs]
  );

  const addNode = () => {
    const id = `${nodes.length + 1}_${Date.now()}`;
    const newNode = {
        id,
        type: "Defult-Node", // Make sure this matches your nodeTypes key
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
    };
    addNodeYjs(newNode);
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
      await takeSnapshot(); 
      showSuccess("Diagram saved successfully!");
    } catch (err) {
      showError(err.message);
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
      const data = await generateSQLFromBackend(finalJson);
      setGeneratedSql(data);
      setIsSqlPanelOpen(true);
    } catch (err) {
      showError(err.message);
      setIsSqlPanelOpen(false);
    }

  };

  const printNodes = () => { 
    console.log("Current Nodes:", nodes);
    console.log(roomId)
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
        //new props
        proOptions={{ hideAttribution: true }}
        nodeDragThreshold={2}
        onlyRenderVisibleElements={true}

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

      {!isSqlPanelOpen && !isReadOnly && (
        <Toolbar addNode={addNode} selectedRelationType={selectedRelationType} setSelectedRelationType={setSelectedRelationType}/>
      )}
      <button className="generate" onClick={onGenerateSQL}>
        Generate SQL
      </button>
      <button className="generate" onClick={printNodes} style={{top:"120px"}}>
        Print Nodes to Console
      </button>
    </div>
  );
};

export default function Schema() {
    const { roomId } = useParams();
    return (
        <CollaborationProvider roomId={roomId}>
            <SchemaContent />
        </CollaborationProvider>
    );
}
