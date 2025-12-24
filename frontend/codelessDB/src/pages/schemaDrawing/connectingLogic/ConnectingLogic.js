// ConnectingLogic.js

// Helper: Find PKs (No changes needed)
const findPrimaryKeyColumns = (node) => {
  if (!node?.data?.columns) return [];
  return node.data.columns.filter((c) => c.constraints?.PRIMARY_KEY === true);
};

// Helper: Build FK Object (No changes needed)
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
    references: { tableName: refNode.data.tableName, columnName: pkCol.name },
  };
};

/**
 * Refactored to use Yjs Callbacks instead of local State setters
 * @param {string} srcId - ID of source node
 * @param {string} tgtId - ID of target node
 * @param {string} relation - '1:1', '1:N', 'N:1', 'M:N'
 * @param {Array} nodes - Current list of nodes (for reading data)
 * @param {Object} callbacks - { updateNodeData, addNodeYjs, addEdgeYjs }
 */
export default function applyRelationLogic(
  srcId,
  tgtId,
  relation,
  nodes,
  { updateNodeData, addNodeYjs, addEdgeYjs }
) {
  const srcNode = nodes.find((n) => n.id === srcId);
  const tgtNode = nodes.find((n) => n.id === tgtId);

  if (!srcNode || !tgtNode || srcNode === tgtNode) return;

  const srcPKs = findPrimaryKeyColumns(srcNode);
  const tgtPKs = findPrimaryKeyColumns(tgtNode);

  // --- Helper to Push FKs to Yjs ---
  const addFKsToNodeYjs = (targetNodeId, refNode, refPKs) => {
    if (!refPKs || refPKs.length === 0) return;

    // 1. Get the current node to ensure we don't overwrite other changes
    const targetNode = nodes.find((n) => n.id === targetNodeId);
    if (!targetNode) return;

    // 2. Build the new columns
    const newFKColumns = refPKs.map((pk) => buildForeignKeyColumn(refNode, pk));
    const currentColumns = targetNode.data.columns || [];

    // 3. Send update to Yjs (Granular Update)
    updateNodeData(targetNodeId, {
      columns: [...currentColumns, ...newFKColumns],
    });
  };

  // --- RELATION LOGIC ---

  if (relation === "1:1") {
    addFKsToNodeYjs(srcId, tgtNode, tgtPKs);
  } else if (relation === "1:N") {
    addFKsToNodeYjs(srcId, tgtNode, tgtPKs);
  } else if (relation === "N:1") {
    addFKsToNodeYjs(tgtId, srcNode, srcPKs);
  } else if (relation === "M:N") {
    // 1. Create Junction Node Data
    const junctionId = `jn_${srcId}_${tgtId}_${Date.now()}`;
    const junctionName = `${srcNode.data.tableName}_${tgtNode.data.tableName}`;
    
    const junctionColumns = [];

    // Add PKs from Source
    srcPKs.forEach((pk) => {
      junctionColumns.push({
        id: `jcn_${srcId}_${pk.name}_${Date.now()}`,
        name: `${srcNode.data.tableName}_${pk.name}`,
        dataType: pk.dataType || "INT",
        dataTypeLength: pk.dataTypeLength,
        dataTypePrecision: pk.dataTypePrecision,
        dataTypeScale: pk.dataTypeScale,
        dataTypeValues: pk.dataTypeValues,
        constraints: { PRIMARY_KEY: true, FOREIGN_KEY: true },
        references: { tableName: srcNode.data.tableName, columnName: pk.name },
      });
    });

    // Add PKs from Target
    tgtPKs.forEach((pk) => {
      junctionColumns.push({
        id: `jcn_${tgtId}_${pk.name}_${Date.now()}`,
        name: `${tgtNode.data.tableName}_${pk.name}`,
        dataType: pk.dataType || "INT",
        dataTypeLength: pk.dataTypeLength,
        dataTypePrecision: pk.dataTypePrecision,
        dataTypeScale: pk.dataTypeScale,
        dataTypeValues: pk.dataTypeValues,
        constraints: { PRIMARY_KEY: true, FOREIGN_KEY: true },
        references: { tableName: tgtNode.data.tableName, columnName: pk.name },
      });
    });

    const junctionNode = {
      id: junctionId,
      type: "Defult-Node", // Ensure this matches your nodeTypes
      position: {
        x: (srcNode.position.x + tgtNode.position.x) / 2,
        y: (srcNode.position.y + tgtNode.position.y) / 2 + 100,
      },
      data: { tableName: junctionName, columns: junctionColumns },
    };

    // 2. Add Node to Yjs
    addNodeYjs(junctionNode);

    // 3. Add Edges to Yjs
    const edge1 = {
      id: `e_${srcId}_${junctionId}`,
      source: srcId,
      target: junctionId,
      type: "oneToMany",
      data: { type: "1:M" },
    };

    const edge2 = {
      id: `e_${junctionId}_${tgtId}`,
      source: junctionId,
      target: tgtId,
      type: "manyToOne",
      data: { type: "M:1" },
    };

    addEdgeYjs(edge1);
    addEdgeYjs(edge2);
  }
}