/**
 * Converts React Flow nodes to the specific JSON Schema format.
 * @param {Array} nodes - The nodes array from React Flow state.
 * @returns {Object} - The formatted JSON object containing { entities: [...] }
 */
export const convertToJSON = (schemaName ,nodes) => {
  // Map over the nodes to create the entities array
  const entities = nodes.map((node) => {
    // 1. Process Attributes (Columns)
    const attributes = (node.data.columns || []).map((col) => {
      
      // Build the constraints array based on the active boolean flags
      const constraints = [];

      // A. Primary Key
      if (col.constraints.PRIMARY_KEY) {
        constraints.push({ type: "PRIMARY_KEY" });
      }

      // B. Not Null
      if (col.constraints.NOT_NULL) {
        constraints.push({ type: "NOT_NULL" });
      }

      if(col.constraints.UNIQUE){
        constraints.push({type:"UNIQUE"})
      }

      // C. Check Constraint
      if (col.constraints.CHECK) {
        // Combine condition and value into a single expression string
        const expression = `${col.constraints.checkCondition || ""} ${col.constraints.checkValue || ""}`.trim();
        constraints.push({
          type: "CHECK",
          expression: expression || null,
        });
      }

      // D. Default Value
      if (col.constraints.DEFAULT) {
        // Attempt to parse number if it looks like one, otherwise keep as string
        const rawVal = col.constraints.defaultValue;
        const isNum = !isNaN(rawVal) && rawVal !== "";
        
        constraints.push({
          type: "DEFAULT",
          defaultValue: isNum ? Number(rawVal) : (rawVal || null),
        });
      }

      // E. Foreign Key
      if (col.constraints.FOREIGN_KEY) {
        constraints.push({
          type: "FOREIGN_KEY",
          referencedTable: col.references?.tableName || null,
          referencedColumn: col.references?.columnName || null,
          onDelete: col.constraints.ForeignKeyOnDelete || "CASCADE",
          onUpdate: col.constraints.ForeignKeyOnUpdate || "CASCADE",
        });
      }

      // 2. Return the Attribute Object
      return {
        name: col.name.replaceAll(" ","_"),
        // Match the strict dataType object structure: { name, length, precision, scale }
        dataType: {
          name: col.dataType,
          // Ensure integers or explicit nulls
          length: col.dataTypeLength ? parseInt(col.dataTypeLength, 10) : null,
          precision: col.dataTypePrecision ? parseInt(col.dataTypePrecision, 10) : null,
          scale: col.dataTypeScale ? parseInt(col.dataTypeScale, 10) : null,
          values: col.dataTypeValues,
        },
        // Indexed is a sibling of constraints in your target schema
        indexed: col.constraints.indexed,
        autoIncrement: col.constraints.autoIncrement,
        constraints: constraints,
      };
    });

    // 3. Return the Entity Object
    return {
      name: node.data.tableName.replaceAll(" ","_"),
      attributes: attributes,
    };
  });

  // Return the final root object
  return {
    schemaName:schemaName.replaceAll(" ","_")  ,
    entities
   };
};