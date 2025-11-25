import React, { memo } from "react";
import { Handle, Position, useReactFlow } from "@xyflow/react";
import "./Node.css";
import dataTypes from "./DataTypes";
import { MdExpandMore, MdExpandLess } from "react-icons/md";

const Node = ({ id, data }) => {
  const { setNodes } = useReactFlow();
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

  const updateNodeData = (newData) => {
    setNodes((nodes) =>
      nodes.map((node) => {
        if (node.id === id) {
          return { ...node, data: { ...node.data, ...newData } };
        }
        return node;
      })
    );
  };

  const onNameChange = (evt) => {
    updateNodeData({ tableName: evt.target.value });
  };

  const onColumnChange = (colId, field, value, isConstraint = false) => {
    const newColumns = data.columns.map((col) => {
      if (col.id === colId) {
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
              alert("Auto Increment can only be applied to integer types");
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
  };

  const addColumn = () => {
    const newCol = {
      id: `col_${Date.now()}`,
      name: `attr_${data.columns.length}`,
      dataType: "VARCHAR",
      dataTypeLength: 45,
      dataTypePrecision: 10,
      dataTypeScale: 0,
      dataTypeValues: [],
      constraints: {
        PRIMARY_KEY: false,
        NOT_NULL: false,
        FOREIGN_KEY: false,
        ForeignKeyOnDelete: "",
        ForeignKeyOnUpdate: "",
        UNIQUE: false,
        DEFAULT: false,
        defaultValue: "",
        CHECK: false,
        checkCondition: "",
        checkValue: "",
        autoIncrement: false,
        indexed: false,
      },
    };
    updateNodeData({ columns: [...data.columns, newCol] });
  };

  const deleteColumn = (colId) => {
    const newColumns = data.columns.filter((col) => col.id !== colId);
    updateNodeData({ columns: newColumns });
  };

  const isIntegerType = (dataType) => {
    const intTypes = ["INT", "BIGINT", "SMALLINT", "TINYINT", "MEDIUMINT"];
    return intTypes.includes(dataType);
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

      <div className="table-body">
        {data.columns.map((col) => (
          <div
            key={col.id}
            className="table-column"
            style={{
              background: col.constraints.PRIMARY_KEY
                ? "#fff5f5"
                : col.constraints.FOREIGN_KEY
                ? "#f0f8ff"
                : "#fff",
            }}
          >
            <div className="column-inputs">
              <span
                style={{
                  minWidth: "30px",
                  fontSize: "11px",
                  fontWeight: "bold",
                }}
              >
                {col.constraints.PRIMARY_KEY && (
                  <span style={{ color: "#e74c3c" }} title="Primary Key">
                    PK
                  </span>
                )}
                {col.constraints.FOREIGN_KEY && (
                  <span style={{ color: "#3498db" }} title="Foreign Key">
                    FK
                  </span>
                )}
              </span>

              <input
                className="nodrag column-name-input"
                value={col.name}
                placeholder="Attr Name"
                onChange={(e) => onColumnChange(col.id, "name", e.target.value)}
              />

              <select
                className="nodrag column-type-select"
                value={col.dataType}
                onChange={(e) =>
                  onColumnChange(col.id, "dataType", e.target.value)
                }
              >
                {dataTypes.map((type) => (
                  <option key={type} value={type}>
                    {type}
                  </option>
                ))}
              </select>

              {(col.dataType === "VARCHAR" || col.dataType === "CHAR") && (
                <input
                  title="Length"
                  type="number"
                  className="nodrag data-type-params"
                  placeholder="Length"
                  value={col.dataTypeLength || ""}
                  onChange={(e) =>
                    onColumnChange(col.id, "dataTypeLength", e.target.value)
                  }
                />
              )}

              {(col.dataType === "DECIMAL" || col.dataType === "NUMERIC") && (
                <>
                  <input
                    type="number"
                    className="nodrag data-type-params"
                    placeholder="Precision"
                    title="Percision"
                    value={col.dataTypePrecision || ""}
                    onChange={(e) =>
                      onColumnChange(
                        col.id,
                        "dataTypePrecision",
                        e.target.value
                      )
                    }
                  />
                  <input
                    type="number"
                    className="nodrag data-type-params"
                    placeholder="Scale"
                    title="Scale"
                    value={col.dataTypeScale}
                    onChange={(e) =>
                      onColumnChange(col.id, "dataTypeScale", e.target.value)
                    }
                  />
                </>
              )}
              {(col.dataType === "ENUM" || col.dataType === "SET") && (
                <input
                  type="text"
                  className="nodrag data-type-enums"
                  placeholder="Values(comma separated)"
                  value={
                    col.dataTypeValues ? col.dataTypeValues.join(", ") : ""
                  }
                  onChange={(e) =>
                    onColumnChange(
                      col.id,
                      "dataTypeValues",
                      e.target.value.split(",").map((v) => v.trim())
                    )
                  }
                />
              )}

              <button
                className="nodrag constraints-btn"
                onClick={() => toggleConstraint(col.id)}
              >
                {constraintsWindow[col.id] ? (
                  <MdExpandLess />
                ) : (
                  <MdExpandMore />
                )}
              </button>

              {constraintsWindow[col.id] && (
                <div className="column-constraints">
                  <label title="Primary Key">
                    PK
                    <input
                      type="checkbox"
                      className="nodrag"
                      checked={col.constraints.PRIMARY_KEY || false}
                      onChange={(e) =>
                        onColumnChange(
                          col.id,
                          "PRIMARY_KEY",
                          e.target.checked,
                          true
                        )
                      }
                    />
                  </label>
                  <label title="Not Null">
                    NN
                    <input
                      type="checkbox"
                      className="nodrag"
                      checked={col.constraints.NOT_NULL || false}
                      onChange={(e) =>
                        onColumnChange(
                          col.id,
                          "NOT_NULL",
                          e.target.checked,
                          true
                        )
                      }
                    />
                  </label>
                  <label title="Unique">
                    UQ
                    <input
                      type="checkbox"
                      className="nodrag"
                      checked={col.constraints.UNIQUE || false}
                      disabled={col.constraints.PRIMARY_KEY}
                      onChange={(e) =>
                        onColumnChange(col.id, "UNIQUE", e.target.checked, true)
                      }
                    />
                  </label>
                  <label title="Auto Increment">
                    AI
                    <input
                      type="checkbox"
                      className="nodrag"
                      checked={col.constraints.autoIncrement || false}
                      disabled={!isIntegerType(col.dataType)}
                      onChange={(e) =>
                        onColumnChange(
                          col.id,
                          "autoIncrement",
                          e.target.checked,
                          true
                        )
                      }
                    />
                  </label>
                  <label title="Indexed">
                    IX
                    <input
                      type="checkbox"
                      className="nodrag"
                      checked={col.constraints.indexed || false}
                      onChange={(e) =>
                        onColumnChange(
                          col.id,
                          "indexed",
                          e.target.checked,
                          true
                        )
                      }
                    />
                  </label>
                  <div className="forginKey-constraint">
                    <label title="Foreign Key">
                      FK
                      <input
                        type="checkbox"
                        className="nodrag"
                        checked={col.constraints.FOREIGN_KEY || false}
                        onChange={(e) =>
                          onColumnChange(
                            col.id,
                            "FOREIGN_KEY",
                            e.target.checked,
                            true
                          )
                        }
                      />
                      {col.constraints.FOREIGN_KEY && (
                        <>
                          OnDelete:{" "}
                          <select
                            className="nodrag foreign-key-action-select"
                            value={col.constraints.ForeignKeyOnDelete || ""}
                            onChange={(e) =>
                              onColumnChange(
                                col.id,
                                "ForeignKeyOnDelete",
                                e.target.value,
                                true
                              )
                            }
                          >
                            <option value="CASCADE">CASCADE</option>
                            <option value="SET NULL">SET NULL</option>
                            <option value="RESTRICT">RESTRICT</option>
                            <option value="SET DEFAULT">SET DEFAULT</option>
                            <option value="NO ACTION">NO ACTION</option>
                          </select>
                          OnUpdate:{" "}
                          <select
                            className="nodrag foreign-key-action-select"
                            value={col.constraints.ForeignKeyOnUpdate || ""}
                            onChange={(e) =>
                              onColumnChange(
                                col.id,
                                "ForeignKeyOnUpdate",
                                e.target.value,
                                true
                              )
                            }
                          >
                            <option value="CASCADE">CASCADE</option>
                            <option value="SET NULL">SET NULL</option>
                            <option value="RESTRICT">RESTRICT</option>
                            <option value="SET DEFAULT">SET DEFAULT</option>
                            <option value="NO ACTION">NO ACTION</option>
                          </select>
                        </>
                      )}
                    </label>
                  </div>
                  <div className="check-constraint">
                    <label title="Check">
                      CH
                      <input
                        type="checkbox"
                        className="nodrag"
                        checked={col.constraints.CHECK || false}
                        onChange={(e) =>
                          onColumnChange(
                            col.id,
                            "CHECK",
                            e.target.checked,
                            true
                          )
                        }
                      />
                      {col.constraints.CHECK && (
                        <>
                          <select
                            className="nodrag check-condition-select"
                            value={col.constraints.checkCondition || ""}
                            onChange={(e) =>
                              onColumnChange(
                                col.id,
                                "checkCondition",
                                e.target.value,
                                true
                              )
                            }
                          >
                            <option value=">">{">"}</option>
                            <option value="<">{"<"}</option>
                            <option value="=">{"="}</option>
                            <option value=">=">{">="}</option>
                            <option value="<=">{"<="}</option>
                          </select>
                          <input
                            type="number"
                            placeholder="Value"
                            className="nodrag check-condition-input"
                            value={col.constraints.checkValue || ""}
                            onChange={(e) =>
                              onColumnChange(
                                col.id,
                                "checkValue",
                                e.target.value,
                                true
                              )
                            }
                          />
                        </>
                      )}
                    </label>
                  </div>
                  <div className="default-constraint">
                    <label title="Default">
                      DF
                      <input
                        type="checkbox"
                        className="nodrag"
                        checked={col.constraints.DEFAULT || false}
                        onChange={(e) =>
                          onColumnChange(
                            col.id,
                            "DEFAULT",
                            e.target.checked,
                            true
                          )
                        }
                      />
                      {col.constraints.DEFAULT && (
                        <input
                          type="text"
                          placeholder="Default Value"
                          className="nodrag default-value-input"
                          value={col.constraints.defaultValue || ""}
                          onChange={(e) =>
                            onColumnChange(
                              col.id,
                              "defaultValue",
                              e.target.value,
                              true
                            )
                          }
                        />
                      )}
                    </label>
                  </div>
                </div>
              )}
              <button
                className="nodrag delete-btn"
                onClick={() => deleteColumn(col.id)}
              >
                ×
              </button>
            </div>
          </div>
        ))}
      </div>
      <button className="nodrag add-btn" onClick={addColumn}>
        + Add Column
      </button>
    </div>
  );
};

export default memo(Node);
