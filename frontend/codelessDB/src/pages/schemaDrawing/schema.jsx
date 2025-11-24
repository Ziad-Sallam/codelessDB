import React, { useState, useCallback } from 'react';
import {
  addEdge,
  MiniMap,
  Controls,
  Background,
  ReactFlow,
  applyNodeChanges,
  applyEdgeChanges,
  useReactFlow
} from "@xyflow/react";
import '@xyflow/react/dist/style.css';
import { nodeTypes,edgeTypes } from './index';
import dataTypes from './node/dataTypes';
// import './Schema.css';



const initialNodes = [
  // {
  //   id: 'n1', position: { x: 0, y: 0 }, data: {
  //     tableName: 'users',
  //     columns: [
  //       { id: 'col1', name: 'id', dataType: 'INT', constraints: { PRIMARY_KEY: true } },
  //       { id: 'col2', name: 'username', dataType: 'VARCHAR', constraints: {} },
  //     ]
  //   }, type: "Defult-Node"
  // },
  // {
  //   id: 'n2', position: { x: 0, y: 200 }, data: {
  //     tableName: 'orders',
  //     columns: [
  //       { id: 'col1', name: 'id', dataType: 'INT', constraints: { PRIMARY_KEY: true } },
  //       { id: 'col2', name: 'total', dataType: 'DECIMAL', constraints: {} },
  //     ]
  //   }, type: "Defult-Node"
  // },
];

export default function Schema() {
  const [nodes, setNodes] = useState(initialNodes);
  const [edges, setEdges] = useState([]);
  const [selectedRelationType, setSelectedRelationType] = useState('1:N'); // default selection
  const onNodesChange = useCallback((changes) => setNodes((ns) => applyNodeChanges(changes, ns)), []);
  const onEdgesChange = useCallback((changes) => setEdges((es) => applyEdgeChanges(changes, es)), []);

  // helpers
  const findPrimaryKeyColumn = (node) => {
    if (!node || !node.data || !Array.isArray(node.data.columns)) return null;
    return node.data.columns.find(c => {
      const cons = c.constraints || {};
      return cons.PRIMARY_KEY === true;
    }) || null;
  };

  const buildForeignKeyColumn = (refNode, pkCol) => {
    const fkId = `fk_${refNode.id}_${pkCol.name}_${Date.now()}`;
    return {
      id: fkId,
      name: `${pkCol.name}_fk`,
      dataType: pkCol.dataType,
      dataTypeLength: pkCol.dataTypeLength,
      dataTypePrecision: pkCol.dataTypePrecision,
      dataTypeScale: pkCol.dataTypeScale,
      dataTypeValues: pkCol.dataTypeValues,
      constraints: { FOREIGN_KEY: true },
      references: { tableName: refNode.name, columnName: pkCol.name }
    };
  };

  const applyRelationLogic = (srcId, tgtId, relation) => {
    const srcNode = nodes.find(n => n.id === srcId);
    const tgtNode = nodes.find(n => n.id === tgtId);
    if (srcNode === tgtNode) return; // prevent self-referencing for now
    if (!srcNode || !tgtNode) return;

    // deep clone nodes array for immutability
    let newNodes = nodes.map(n => ({ ...n, data: { ...n.data, columns: n.data.columns ? [...n.data.columns] : [] }, position: n.position }));

    const srcClone = newNodes.find(n => n.id === srcId);
    const tgtClone = newNodes.find(n => n.id === tgtId);
    const srcPK = findPrimaryKeyColumn(srcClone);
    const tgtPK = findPrimaryKeyColumn(tgtClone);

    const addFKtoNode = (targetNodeToModify, refNode, refPK) => {
      if (!refPK) return;
      const fkCol = buildForeignKeyColumn(refNode, refPK);
      targetNodeToModify.data.columns = [...(targetNodeToModify.data.columns || []), fkCol];
    };

    if (relation === '1:1') {
      if (srcPK) addFKtoNode(tgtClone, srcClone, srcPK);
      else {
        tgtClone.data.columns.push({
          id: `fk_${srcClone.id}_id_${Date.now()}`,
          name: `${srcClone.data.tableName}_id_fk`,
          dataType: 'INT',
          constraints: { FOREIGN_KEY: true },
          references: { tableName: srcClone.name, columnName: 'id' }
        });
      }
    } else if (relation === '1:N') {
      // treat target as N side
      if (srcPK) addFKtoNode(tgtClone, srcClone, srcPK);
      else tgtClone.data.columns.push({
        id: `fk_${srcClone.id}_id_${Date.now()}`,
        name: `${srcClone.data.tableName}_id_fk`,
        dataType: 'INT',
        constraints: { FOREIGN_KEY: true },
        references: { tableName: srcClone.name, columnName: 'id' }
      });
    } else if (relation === 'N:1') {
      // source is N side
      if (tgtPK) addFKtoNode(srcClone, tgtClone, tgtPK);
      else srcClone.data.columns.push({
        id: `fk_${tgtClone.id}_id_${Date.now()}`,
        name: `${tgtClone.data.tableName}_id_fk`,
        dataType: 'INT',
        constraints: { FOREIGN_KEY: true },
        references: { tableName: tgtClone.name, columnName: 'id' }
      });
    } else if (relation === 'M:N') {
      const pkA = srcPK ? srcPK : { name: `${srcClone.data.tableName}_id`, dataType: 'INT', constraints: { PRIMARY_KEY: true } };
      const pkB = tgtPK ? tgtPK : { name: `${tgtClone.data.tableName}_id`, dataType: 'INT', constraints: { PRIMARY_KEY: true } };

      const colA = {
        id: `jcn_${srcId}_${pkA.name}_${Date.now()}`,
        name: `${srcClone.data.tableName}_${pkA.name}`,
        dataType: pkA.dataType || 'INT',
        constraints: { PRIMARY_KEY: true, FOREIGN_KEY: true },
        references: { tableName: srcClone.name, columnName: pkA.name }
      };
      const colB = {
        id: `jcn_${tgtId}_${pkB.name}_${Date.now()}`,
        name: `${tgtClone.data.tableName}_${pkB.name}`,
        dataType: pkB.dataType || 'INT',
        constraints: { PRIMARY_KEY: true, FOREIGN_KEY: true },
        references: { tableName: tgtClone.name, columnName: pkB.name }
      };

      const junctionId = `jn_${srcId}_${tgtId}_${Date.now()}`;
      const junctionName = `${srcClone.data.tableName}_${tgtClone.data.tableName}_junction`;

      const junctionNode = {
        id: junctionId,
        type: "Defult-Node",
        position: { x: (srcClone.position.x+tgtClone.position.x)/2, y: (srcClone.position.y + tgtClone.position.y)/2 + 100 },
        data: { tableName: junctionName, columns: [colA, colB] }
      };
      newNodes.push(junctionNode);

      // create edges from junction to both tables (visual)
      setEdges((eds) => [
        ...eds,
        { id: `e_${srcId}_${junctionId}`, source: srcId, target: junctionId, type: 'oneToMany', data: { type: '1:M' } },
        { id: `e_${junctionId}_${tgtId}`, source: junctionId, target: tgtId, type: 'manyToOne', data: { type: 'M:1' } },
      ]);
    }

    setNodes(newNodes);
  };

  const onConnect = useCallback((params) => {
    // params: { source, target, sourceHandle, targetHandle }
    if (!params || !params.source || !params.target) return;
    // apply relation logic (selectedRelationType)
    applyRelationLogic(params.source, params.target, selectedRelationType);

    // map selectedRelationType to an edge type key
    const typeKey = {
      '1:1': 'oneToOne',
      '1:N': 'oneToMany',
      'N:1': 'manyToOne',
      'M:N': 'manyToMany'
    }[selectedRelationType] || 'oneToMany';

    const newEdge = {
      id: `e_${params.source}_${params.target}_${Date.now()}`,
      source: params.source,
      target: params.target,
      type: typeKey,
      data: { type: selectedRelationType }
    };
    setEdges((eds) => addEdge(newEdge, eds));
  }, [selectedRelationType, nodes]);

  const addNode = () => {
    const id = `${nodes.length + 1}`;
    console.log(nodes)
    console.log(edges)
    setNodes((nds) => [
      ...nds,
      {
        id,
        type: "Defult-Node",
        data: {
          tableName: `Entity_${id}`,
          columns: [{ id: `attr1_${id}`, name: 'id', dataType: 'INT', constraints: { PRIMARY_KEY: true } }]
        },
        position: { x: Math.random() * 400, y: Math.random() * 400 },
      },
    ]);
  };


  return (
    <div style={{ width: '100vw', height: '100vh', position: 'relative' }}>
      <ReactFlow
        nodes={nodes}
        edges={edges}
        onNodesChange={onNodesChange}
        onEdgesChange={onEdgesChange}
        onConnect={onConnect}
        nodeTypes={nodeTypes}
        edgeTypes={edgeTypes}
        fitView
      >
        <MiniMap />
        <Controls />
        <Background />
      </ReactFlow>

      {/* toolbar bottom */}
      <div style={{
        position: 'absolute',
        left: '50%',
        transform: 'translateX(-50%)',
        bottom: 12,
        display: 'flex',
        gap: 8,
        zIndex: 9999,
        background: 'rgba(255,255,255,0.95)',
        padding: 8,
        borderRadius: 8,
        boxShadow: '0 6px 18px rgba(0,0,0,0.12)'
      }}>
        <RelationButton active={selectedRelationType === '1:1'} color="#007bff" onClick={() => setSelectedRelationType('1:1')} label="1 : 1" />
        <RelationButton active={selectedRelationType === '1:N'} color="#00d26a" onClick={() => setSelectedRelationType('1:N')} label="1 : N" />
        <RelationButton active={selectedRelationType === 'N:1'} color="#ffb300" onClick={() => setSelectedRelationType('N:1')} label="N : 1" />
        <RelationButton active={selectedRelationType === 'M:N'} color="#d00000" onClick={() => setSelectedRelationType('M:N')} label="M : N" />
        <button onClick={addNode} style={{ padding: '6px 10px' }}>Add Node</button>
      </div>
    </div>
  );
}

function RelationButton({ active, color, onClick, label }) {
  return (
    <button onClick={onClick}
      style={{
        padding: '8px 12px',
        borderRadius: 6,
        border: active ? `2px solid ${color}` : '1px solid #ccc',
        background: active ? `${color}11` : 'transparent',
        cursor: 'pointer',
        fontWeight: 600
      }}>
      <span style={{ display: 'inline-block', width: 10, height: 10, background: color, borderRadius: 3, marginRight: 8 }} />
      {label}
    </button>
  );
}
