import React, { useState, useCallback } from 'react';
import {
  addEdge,
  MiniMap,
  Controls,
  Background,
  ReactFlow,
  applyNodeChanges,
  applyEdgeChanges,
  useInternalNode,
  getSmoothStepPath,
  BaseEdge,
  EdgeLabelRenderer,
  Position,
  MarkerType
} from "@xyflow/react";
import '@xyflow/react/dist/style.css';
import { nodeTypes, edgeTypes as initialEdgeTypes } from './index';
import dataTypes from './node/dataTypes';
import "./Schema.css";

// --- MATH HELPERS FOR SHORTEST DISTANCE (Floating Edge) ---
function getIntersection(n, n2) {
  const w = n.measured.width || 0;
  const h = n.measured.height || 0;
  const x = n.internals.positionAbsolute.x;
  const y = n.internals.positionAbsolute.y;

  const w2 = n2.measured.width || 0;
  const h2 = n2.measured.height || 0;
  const x2 = n2.internals.positionAbsolute.x;
  const y2 = n2.internals.positionAbsolute.y;

  const xx1 = x + w / 2;
  const yy1 = y + h / 2;
  const xx2 = x2 + w2 / 2;
  const yy2 = y2 + h2 / 2;

  const dx = xx2 - xx1;
  const dy = yy2 - yy1;

  if (dx === 0 && dy === 0) return { x: xx1, y: yy1 };

  const slope = dy / (dx || 1);

  if (Math.abs(dx) * h > Math.abs(dy) * w) {
    const hitX = dx > 0 ? x + w : x;
    const hitY = yy1 + slope * (hitX - xx1);
    return { x: hitX, y: hitY };
  } else {
    const hitY = dy > 0 ? y + h : y;
    const hitX = xx1 + (hitY - yy1) / slope;
    return { x: hitX, y: hitY };
  }
}

function getEdgePosition(node, intersectionPoint) {
  const n = node.internals.positionAbsolute;
  const w = node.measured.width || 0;
  const h = node.measured.height || 0;
  const x = intersectionPoint.x;
  const y = intersectionPoint.y;
  const tolerance = 2;

  if (Math.abs(y - n.y) < tolerance) return Position.Top;
  if (Math.abs(y - (n.y + h)) < tolerance) return Position.Bottom;
  if (Math.abs(x - n.x) < tolerance) return Position.Left;
  if (Math.abs(x - (n.x + w)) < tolerance) return Position.Right;

  return Position.Top;
}

function getEdgeParams(source, target) {
  const sourceIntersection = getIntersection(source, target);
  const targetIntersection = getIntersection(target, source);

  const sourcePos = getEdgePosition(source, sourceIntersection);
  const targetPos = getEdgePosition(target, targetIntersection);

  return {
    sx: sourceIntersection.x,
    sy: sourceIntersection.y,
    tx: targetIntersection.x,
    ty: targetIntersection.y,
    sourcePos,
    targetPos,
  };
}

// Calculates where to place the label relative to the intersection point
function getLabelCoords(x, y, pos) {
  const offset = 12; // Distance from the border
  if (pos === Position.Top) return { x, y: y - offset };
  if (pos === Position.Bottom) return { x, y: y + offset };
  if (pos === Position.Left) return { x: x - offset, y };
  if (pos === Position.Right) return { x: x + offset, y };
  return { x, y };
}

// --- FLOATING SMOOTH STEP EDGE COMPONENT ---
function FloatingEdge({ id, source, target, markerEnd, style, data }) {
  const sourceNode = useInternalNode(source);
  const targetNode = useInternalNode(target);

  if (!sourceNode || !targetNode) {
    return null;
  }

  const { sx, sy, tx, ty, sourcePos, targetPos } = getEdgeParams(sourceNode, targetNode);

  const [edgePath] = getSmoothStepPath({
    sourceX: sx,
    sourceY: sy,
    sourcePosition: sourcePos,
    targetX: tx,
    targetY: ty,
    targetPosition: targetPos,
    borderRadius: 10,
    offset: 20
  });

  // Split the type string "1:N" into ["1", "N"]
  const relationType = data?.type || '1:N';
  const [startLabel, endLabel] = relationType.split(':');

  // Calculate positions for the labels
  const sourceLabelPos = getLabelCoords(tx, ty, targetPos);
  const targetLabelPos = getLabelCoords(sx, sy, sourcePos);

  const labelStyle = {
    position: 'absolute',
    background: 'white', // White background to hide line behind text
    padding: '0px 3px',
    borderRadius: 3,
    fontSize: 12,
    fontWeight: 'bold',
    color: '#333',
    pointerEvents: 'none',
    zIndex: 10,
    transform: 'translate(-50%, -50%)', // Center the div on the coordinate
  };

  return (
    <>
      <BaseEdge id={id} path={edgePath} markerEnd={markerEnd} style={style} />

      <EdgeLabelRenderer>
        {/* Source Label (e.g., '1') */}
        <div style={{ ...labelStyle, left: sourceLabelPos.x, top: sourceLabelPos.y }}>
          {startLabel}
        </div>

        {/* Target Label (e.g., 'N') */}
        <div style={{ ...labelStyle, left: targetLabelPos.x, top: targetLabelPos.y }}>
          {endLabel}
        </div>
      </EdgeLabelRenderer>
    </>
  );
}

const customEdgeTypes = {
  ...initialEdgeTypes,
  'oneToOne': FloatingEdge,
  'oneToMany': FloatingEdge,
  'manyToOne': FloatingEdge,
  'manyToMany': FloatingEdge,
};

const initialNodes = [];

export default function Schema() {
  const [nodes, setNodes] = useState(initialNodes);
  const [edges, setEdges] = useState([]);
  const [selectedRelationType, setSelectedRelationType] = useState('1:N');

  const onNodesChange = useCallback((changes) => setNodes((ns) => applyNodeChanges(changes, ns)), []);
  const onEdgesChange = useCallback((changes) => setEdges((es) => applyEdgeChanges(changes, es)), []);

  const findPrimaryKeyColumns = (node) => {
    if (!node?.data?.columns) return [];
    return node.data.columns.filter(c => c.constraints?.PRIMARY_KEY === true);
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
      references: { tableName: refNode.data.name, columnName: pkCol.name }
    };
  };

  const applyRelationLogic = (srcId, tgtId, relation) => {
    const srcNode = nodes.find(n => n.id === srcId);
    const tgtNode = nodes.find(n => n.id === tgtId);
    if (srcNode === tgtNode) return;
    if (!srcNode || !tgtNode) return;

    let newNodes = nodes.map(n => ({ ...n, data: { ...n.data, columns: n.data.columns ? [...n.data.columns] : [] }, position: n.position }));
    const srcClone = newNodes.find(n => n.id === srcId);
    const tgtClone = newNodes.find(n => n.id === tgtId);
    const srcPKs = findPrimaryKeyColumns(srcClone);
    const tgtPKs = findPrimaryKeyColumns(tgtClone);

    const addFKsToNode = (targetNodeToModify, refNode, refPKs) => {
      if (!refPKs || refPKs.length === 0) return;
      refPKs.forEach(pkCol => {
        const fkCol = buildForeignKeyColumn(refNode, pkCol);
        targetNodeToModify.data.columns.push(fkCol);
      });
    };

    if (relation === '1:1') {
      addFKsToNode(srcClone, tgtClone, tgtPKs);
    } else if (relation === '1:N') {
      addFKsToNode(srcClone, tgtClone, tgtPKs);
    } else if (relation === 'N:1') {
      addFKsToNode(tgtClone, srcClone, srcPKs);
    } else if (relation === 'M:N') {
      const junctionId = `jn_${srcId}_${tgtId}_${Date.now()}`;
      const junctionName = `${srcClone.data.tableName}_${tgtClone.data.tableName}`;

      const junctionColumns = [];
      srcPKs.forEach(pk => {
        junctionColumns.push({
          id: `jcn_${srcId}_${pk.name}_${Date.now()}`,
          name: `${srcClone.data.tableName}_${pk.name}`,
          dataType: pk.dataType || 'INT',
          dataTypeLength: pk.dataTypeLength,
          dataTypePrecision: pk.dataTypePrecision,
          dataTypeScale: pk.dataTypeScale,
          dataTypeValues: pk.dataTypeValues,
          constraints: { PRIMARY_KEY: true, FOREIGN_KEY: true },
          references: { tableName: srcClone.data.tableName, columnName: pk.name }
        });
      });
      tgtPKs.forEach(pk => {
        junctionColumns.push({
          id: `jcn_${tgtId}_${pk.name}_${Date.now()}`,
          name: `${tgtClone.data.tableName}_${pk.name}`,
          dataType: pk.dataType || 'INT',
          dataTypeLength: pk.dataTypeLength,
          dataTypePrecision: pk.dataTypePrecision,
          dataTypeScale: pk.dataTypeScale,
          dataTypeValues: pk.dataTypeValues,
          constraints: { PRIMARY_KEY: true, FOREIGN_KEY: true },
          references: { tableName: tgtClone.data.tableName, columnName: pk.name }
        });
      });

      const junctionNode = {
        id: junctionId,
        type: "Defult-Node",
        position: { x: (srcClone.position.x + tgtClone.position.x) / 2, y: (srcClone.position.y + tgtClone.position.y) / 2 + 100 },
        data: { tableName: junctionName, columns: junctionColumns }
      };
      newNodes.push(junctionNode);

      setEdges((eds) => [
        ...eds,
        {
          id: `e_${srcId}_${junctionId}`,
          source: srcId,
          target: junctionId,
          type: 'oneToMany',
          // markerEnd: { type: MarkerType.ArrowClosed },
          data: { type: '1:M' }
        },
        {
          id: `e_${junctionId}_${tgtId}`,
          source: junctionId,
          target: tgtId,
          type: 'manyToOne',
          // markerEnd: { type: MarkerType.ArrowClosed },
          data: { type: 'M:1' }
        },
      ]);
    }
    setNodes(newNodes);
  };

  const onConnect = useCallback((params) => {
    if (!params || !params.source || !params.target) return;

    applyRelationLogic(params.source, params.target, selectedRelationType, params);

    const typeKey = {
      '1:1': 'oneToOne',
      '1:N': 'oneToMany',
      'N:1': 'manyToOne',
      'M:N': 'manyToMany'
    }[selectedRelationType] || 'oneToMany';

    const newEdge = {
      source: params.source,
      target: params.target,
      id: `e_${params.source}_${params.target}_${Date.now()}`,
      type: typeKey,
      // markerEnd: { type: MarkerType.ArrowClosed, width: 20, height: 20 },
      data: { type: selectedRelationType }
    };
    setEdges((eds) => addEdge(newEdge, eds));
  }, [selectedRelationType, nodes]);

  const addNode = () => {
    const id = `${nodes.length + 1}_${Date.now()}`;
    setNodes((nds) => [
      ...nds,
      {
        id,
        type: "Defult-Node",
        data: {
          tableName: `Entity_${nodes.length + 1}`,
          columns: [{ id: `attr1_${id}`, name: 'id', dataType: 'INT', constraints: { PRIMARY_KEY: true } }]
        },
        position: { x: Math.random() * 400, y: Math.random() * 400 },
      },
    ]);
  };

 return (
    <div style={{ width: '100vw', height: '100vh', position: 'relative', background: '#f8fafc' }}>
      <ReactFlow
        nodes={nodes}
        edges={edges}
        onNodesChange={onNodesChange}
        onEdgesChange={onEdgesChange}
        onConnect={onConnect}
        nodeTypes={nodeTypes}
        edgeTypes={customEdgeTypes} 
        fitView
        connectionMode="loose"
        defaultEdgeOptions={{
          style: { strokeWidth: 2, stroke: '#94a3b8' },
        }}
      >
        <MiniMap style={{ borderRadius: 8, border: '1px solid #e2e8f0' }} nodeColor="#cbd5e1" maskColor="rgba(241, 245, 249, 0.6)" />
        <Controls style={{ borderRadius: 8, overflow: 'hidden', border: 'none', boxShadow: '0 4px 6px -1px rgba(0,0,0,0.1)' }} />
        <Background color="#cbd5e1" gap={20} size={1} />
      </ReactFlow>

      {/* NEW TOOLBAR STRUCTURE */}
      <div className="schema-toolbar">
        <RelationButton active={selectedRelationType === '1:1'} color="#3b82f6" onClick={() => setSelectedRelationType('1:1')} label="1 : 1" />
        <RelationButton active={selectedRelationType === '1:N'} color="#10b981" onClick={() => setSelectedRelationType('1:N')} label="1 : N" />
        <RelationButton active={selectedRelationType === 'N:1'} color="#f59e0b" onClick={() => setSelectedRelationType('N:1')} label="N : 1" />
        <RelationButton active={selectedRelationType === 'M:N'} color="#ef4444" onClick={() => setSelectedRelationType('M:N')} label="M : N" />
        <div style={{ width: 1, height: 24, background: '#e2e8f0', margin: '0 4px' }}></div>
        <button className="add-node-btn" onClick={addNode}>+ Add Table</button>
      </div>
      <button className='generate' >Generate SQL</button>
    </div>
  );
}

// Updated Button Component to use new classes
function RelationButton({ active, color, onClick, label }) {
  return (
    <button 
      onClick={onClick} 
      className={`relation-btn ${active ? 'active' : ''}`}
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