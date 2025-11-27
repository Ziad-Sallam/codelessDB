/**
 * Validates the schema nodes before generating SQL/JSON.
 * @param {Array} nodes - The React Flow nodes array containing entity data.
 * @returns {Object} - { isValid: boolean, errors: Array<string> }
 */
export const validateSchema = (nodes) => {
  const errors = [];
  const entityMap = new Map();
  const entityNames = new Set();

  nodes.forEach(node => {
    const tableName = node.data.tableName?.trim();
    if (tableName) {
      entityMap.set(tableName, node.data.columns);
    }
  });

  if (!nodes || nodes.length === 0) {
    return { isValid: false, errors: ["Schema is empty. Please add entities."] };
  }

  nodes.forEach((node) => {
    // 1. Check Table Name
    const tableName = node.data.tableName ? node.data.tableName.trim() : "";
    
    if (!tableName) {
      errors.push(`Node (ID: ${node.id}) is missing an Entity Name.`);
    } else {
      if (entityNames.has(tableName.toLowerCase())) {
        errors.push(`Duplicate Entity Name found: "${tableName}". Names must be unique.`);
      }
      entityNames.add(tableName.toLowerCase());
    }

    // 2. Check Attributes
    const columns = node.data.columns || [];
    const columnNames = new Set();
    let hasPrimaryKey = false;

    if (columns.length === 0) {
      errors.push(`Entity "${tableName || node.id}" has no attributes.`);
    }

    columns.forEach((col) => {
      const colName = col.name ? col.name.trim() : "";

      // 2a. Attribute Name Checks
      if (!colName) {
        errors.push(`Entity "${tableName}" has an attribute with an empty name.`);
      } else {
        if (columnNames.has(colName.toLowerCase())) {
          errors.push(`Entity "${tableName}" has duplicate attribute name: "${colName}".`);
        }
        columnNames.add(colName.toLowerCase());
      }

      // 2b. Data Type Specific Checks
      // ENUM and SET validation
      if (["ENUM", "SET"].includes(col.dataType)) {
        const values = col.dataTypeValues;
        // Check if array is empty or contains only empty strings
        const hasValidValues = values && values.length > 0 && values.some(v => v.trim() !== "");
        
        if (!hasValidValues) {
          errors.push(`Attribute "${colName}" in "${tableName}" is type ${col.dataType} but has no values defined.`);
        }
      }

      // VARCHAR/CHAR length validation
      if (["VARCHAR", "CHAR"].includes(col.dataType)) {
        if (!col.dataTypeLength || col.dataTypeLength < 1) {
          errors.push(`Attribute "${colName}" in "${tableName}" is type ${col.dataType} but missing a valid length.`);
        }
      }

      // DECIMAL/NUMERIC validation
      if (["DECIMAL", "NUMERIC"].includes(col.dataType)) {
        if (!col.dataTypePrecision || col.dataTypePrecision < 1) {
          errors.push(`Attribute "${colName}" in "${tableName}" is type ${col.dataType} but missing Precision.`);
        }
      }

      // 2c. Constraint Checks
      if (col.constraints.PRIMARY_KEY) {
        hasPrimaryKey = true;
      }

      // Check constraint value validation
      if (col.constraints.CHECK) {
        if (!col.constraints.checkValue || col.constraints.checkValue.toString().trim() === "") {
           errors.push(`Attribute "${colName}" in "${tableName}" has a CHECK constraint enabled but no value provided.`);
        }
      }
      
      // Default value validation
      if (col.constraints.DEFAULT) {
         if (col.constraints.defaultValue === undefined || col.constraints.defaultValue === "") {
             // Depending on logic, empty string might be a valid default, but usually worth a warning or ensuring it's intended.
         }
      }
      if (col.constraints.FOREIGN_KEY) {
        
        const refTable = col.references?.tableName;
        const refColName = col.references?.columnName;

        // Check 1: Is a reference selected?
        if (!refTable || !refColName) {
          errors.push(`Attribute "${colName}" in "${tableName}" is marked as FK but references are missing.`);
          return; // Stop checking this FK
        }

        // Check 2: Does the referenced table exist?
        const targetColumns = entityMap.get(refTable);
        if (!targetColumns) {
          errors.push(`Attribute "${colName}" references non-existent table "${refTable}".`);
          return;
        }

        // Check 3: Does the referenced column exist?
        const targetCol = targetColumns.find(c => c.name === refColName);
        if (!targetCol) {
          errors.push(`Attribute "${colName}" references missing column "${refColName}" in table "${refTable}".`);
          return;
        }

        // Check 4: Data Type Mismatch (Strict)
        // Usually FK and PK must be same type (e.g. INT and INT)
        if (col.dataType !== targetCol.dataType) {
          errors.push(
            `Type Mismatch: "${colName}" (${col.dataType}) cannot refer to "${refTable}.${refColName}" (${targetCol.dataType}). Types must match.`
          );
        }

        // Check 5:Referenced column should usually be a Primary Key or Unique
        if (!targetCol.constraints.PRIMARY_KEY && !targetCol.constraints.UNIQUE) {
             errors.push(`Referenced column "${refTable}.${refColName}" is not a Primary Key or Unique.`);
        }
    }
    });

    // 3. Entity Level Checks
    if (!hasPrimaryKey && tableName) {
      errors.push(`Entity "${tableName}" does not have a Primary Key defined.`);
    }
  });

  return {
    isValid: errors.length === 0,
    errors,
  };
};