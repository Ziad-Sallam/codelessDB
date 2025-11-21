import { useState, useCallback } from 'react';
import {
  addEdge,
  MiniMap,
  Controls,
  Background,
  ReactFlow,
  applyNodeChanges,
  applyEdgeChanges,

} from "@xyflow/react";
import '@xyflow/react/dist/style.css';
import { nodeTypes } from "./node/index";
import '@xyflow/react/dist/style.css';

const initialNodes = [
  {
    id: 'n1', position: { x: 0, y: 0 }, data: {
      tableName: 'users',
      columns: [
        { id: 'col1', name: 'id', dataType: 'INT', constraints: { isPrimaryKey: true } },
        { id: 'col2', name: 'username', dataType: 'VARCHAR', constraints: {} },
      ]
    }, type: "Defult-Node"
  },
  {
    id: 'n2', position: { x: 0, y: 100 }, data: {
      tableName: 'users',
      columns: [
        { id: 'col1', name: 'id', dataType: 'INT', constraints: { isPrimaryKey: true } },
        { id: 'col2', name: 'username', dataType: 'VARCHAR', constraints: {} },
      ]
    }, type: "Defult-Node"
  },
];
const initialEdges = [];

export default function Schema() {
  const [nodes, setNodes] = useState(initialNodes);
  const [edges, setEdges] = useState(initialEdges);

  const onNodesChange = useCallback(
    (changes) => setNodes((nodesSnapshot) => applyNodeChanges(changes, nodesSnapshot)),
    [],
  );
  const onEdgesChange = useCallback(
    (changes) => setEdges((edgesSnapshot) => applyEdgeChanges(changes, edgesSnapshot)),
    [],
  );
  const onConnect = useCallback(
    (params) => setEdges((edgesSnapshot) => addEdge(params, edgesSnapshot)),
    [],
  );

  const addNode = () => {
    const id = `${nodes.length}`;
    setNodes((nds) => [
      ...nds,
      {
        id,
        type: "Defult-Node",
        data: {
            tableName: `Entity ${id}`,
            columns: [
              { id: `attr1_${id}`, name: 'attribute1', dataType: 'VARCHAR', constraints: {} }
            ]
        },
        position: { x: Math.random() * 400, y: Math.random() * 400 },
      },
    ]);
  };

  const logNodes = () => {
    console.log("Current Nodes:", nodes);
  }



  return (
    <div style={{ width: '100vw', height: '100vh' }}>
      <ReactFlow
        nodes={nodes}
        edges={edges}
        onNodesChange={onNodesChange}
        onEdgesChange={onEdgesChange}
        onConnect={onConnect}
        nodeTypes={nodeTypes}
        fitView
      >
        <MiniMap />
        <Controls />
        <Background />
      </ReactFlow>

      <div className="buttons">
        <button className="queueButton" onClick={addNode}>Add Node</button>
        <button className="queueButton" onClick={logNodes}>Log Nodes</button>
      </div>
    </div>
  );
}