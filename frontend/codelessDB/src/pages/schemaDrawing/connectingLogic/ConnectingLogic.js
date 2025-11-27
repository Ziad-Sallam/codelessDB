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
      references: { tableName: refNode.data.tableName, columnName: pkCol.name }
    };
  };

  export default function applyRelationLogic(srcId, tgtId, relation,nodes,setNodes,setEdges) {
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
          data: { type: '1:M' }
        },
        {
          id: `e_${junctionId}_${tgtId}`,
          source: junctionId,
          target: tgtId,
          type: 'manyToOne',
          data: { type: 'M:1' }
        },
      ]);
    }
    setNodes(newNodes);
  };