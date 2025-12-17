import { Handle, Position } from "@xyflow/react";
import React, { memo, useCallback } from "react";
import "./Node.css";

import { useNotification } from "../../../components/NotificationContext";
import { useCollaboration } from "../collab/CollaborationContext.jsx";
import ColumnRow from "./ColumnRow";

const Node = ({ id, data, dragging }) => {
  const { showSuccess, showError, showWarning } = useNotification();

  const { updateNodeData: updateYjsNode, nodes } = useCollaboration();

  const [constraintsWindow, setConstraintsWindow] = React.useState({});

  const toggleConstraint = (id) => {
    setConstraintsWindow((prev) => {
      const next = {};
      for (const key of Object.keys(prev)) {
        next[key] = false;
      }
      next[id] = !prev[id];
      return next;
    });
  };

  const updateNodeData = useCallback(
    (newData) => {
      // Call the context function to update Yjs
      updateYjsNode(id, newData);
    },
    [id, updateYjsNode]
  );

  const onNameChange = useCallback(
    (evt) => {
      updateNodeData({ tableName: evt.target.value });
    },
    [updateNodeData]
  );

  const onColumnChange = useCallback(
    (colId, field, value, isConstraint = false, isReference = false) => {
      const newColumns = data.columns.map((col) => {
        if (col.id === colId) {
          if (isReference) {
            return {
              ...col,
              references: { ...col.references, [field]: value },
            };
          }
          if (isConstraint) {
            let newConstraints = { ...col.constraints, [field]: value };
            if (field === "PRIMARY_KEY" && value === true) {
              newConstraints.UNIQUE = false;
              newConstraints.indexed = false;
            }
            if (
              field === "UNIQUE" &&
              value === true &&
              col.constraints.PRIMARY_KEY
            )
              newConstraints.UNIQUE = false;
            if (
              field === "indexed" &&
              value === true &&
              col.constraints.PRIMARY_KEY
            )
              newConstraints.indexed = false;
            if (field === "autoIncrement" && value === true) {
              const intTypes = [
                "INT",
                "BIGINT",
                "SMALLINT",
                "TINYINT",
                "MEDIUMINT",
              ];

              if (!intTypes.includes(col.dataType)) {
                showWarning(
                  "Auto Increment can only be applied to integer types"
                );
                return col;
              }
            }
            return { ...col, constraints: newConstraints };
          }
          if (field === "dataType") {
            const intTypes = [
              "INT",
              "BIGINT",
              "SMALLINT",
              "TINYINT",
              "MEDIUMINT",
            ];
            if (!intTypes.includes(value) && col.constraints.autoIncrement) {
              return {
                ...col,
                [field]: value,
                constraints: { ...col.constraints, autoIncrement: false },
              };
            }
          }
          return { ...col, [field]: value };
        }
        return col;
      });
      updateNodeData({ columns: newColumns });
    },
    [data.columns, updateNodeData, showWarning]
  );

  const addColumn = () => {
    const newCol = {
      id: `col_${Date.now()}`,
      name: `attr_${data.columns.length}`,
      dataType: "VARCHAR",
      dataTypeLength: 45,
      constraints: {},
    };
    updateNodeData({ columns: [...data.columns, newCol] });
  };

  const deleteColumn = (colId) => {
    const newColumns = data.columns.filter((col) => col.id !== colId);
    updateNodeData({ columns: newColumns });
  };

  return (
    <div className="table-node">
      {/* INVISIBLE INTERACTION HANDLES 
          These cover the borders so you can drag from anywhere on the side.
      */}
      <Handle
        type="source"
        position={Position.Top}
        id="top"
        className="handle-top"
      />
      <Handle
        type="source"
        position={Position.Right}
        id="right"
        className="handle-right"
      />
      <Handle
        type="source"
        position={Position.Bottom}
        id="bottom"
        className="handle-bottom"
      />
      <Handle
        type="source"
        position={Position.Left}
        id="left"
        className="handle-left"
      />

      {/* Target handles allow incoming connections from any side */}
      <Handle
        type="target"
        position={Position.Top}
        id="top-t"
        className="handle-top"
      />
      <Handle
        type="target"
        position={Position.Right}
        id="right-t"
        className="handle-right"
      />
      <Handle
        type="target"
        position={Position.Bottom}
        id="bottom-t"
        className="handle-bottom"
      />
      <Handle
        type="target"
        position={Position.Left}
        id="left-t"
        className="handle-left"
      />

      <div className="table-header">
        <input
          className="nodrag table-name-input"
          placeholder="Entity Name"
          value={data.tableName}
          onChange={onNameChange}
        />
      </div>
      {!dragging ? (
        <div className="table-body">
          {data.columns.map((col) => (
            <ColumnRow
              key={col.id}
              id={id}
              col={col}
              nodes={nodes}
              onColumnChange={onColumnChange}
              deleteColumn={deleteColumn}
              toggleConstraint={toggleConstraint}
              isConstraintWindowOpen={constraintsWindow[col.id] || false}
            />
          ))}
        </div>
      ) : (
        <div className="table-body">
          {data.columns.map((col) => (
            <div
              key={col.id}
              style={{
                height: "45px",
                borderBottom: "1px solid #eee",
                backgroundColor: "#fafafa",
              }}
            />
          ))}
        </div>
      )}
      <button className="nodrag add-btn" onClick={addColumn}>
        + Add Column
      </button>
    </div>
  );
};

export default memo(Node);
