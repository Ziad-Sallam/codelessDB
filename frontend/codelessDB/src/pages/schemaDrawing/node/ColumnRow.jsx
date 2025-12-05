import React , {memo} from "react";
import dataTypes from "./DataTypes";
import { MdExpandMore, MdExpandLess } from "react-icons/md";

const ColumnRow = ({id,col , nodes , onColumnChange , deleteColumn , toggleConstraint, isConstraintWindowOpen}) => {
    const isIntegerType = (dataType) => {
    const intTypes = ["INT", "BIGINT", "SMALLINT", "TINYINT", "MEDIUMINT"];
    return intTypes.includes(dataType);
  };
    return (
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
                {isConstraintWindowOpen ? (
                  <MdExpandLess />
                ) : (
                  <MdExpandMore />
                )}
              </button>

              {isConstraintWindowOpen && (
                <div className="column-constraints">
                  <div className="simple-constraints">
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
                          onColumnChange(
                            col.id,
                            "UNIQUE",
                            e.target.checked,
                            true
                          )
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
                        disabled={col.constraints.PRIMARY_KEY}
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
                  </div>
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
                    </label>
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
                          <option value="SET_NULL">SET NULL</option>
                          <option value="RESTRICT">RESTRICT</option>
                          <option value="SET_DEFAULT">SET DEFAULT</option>
                          <option value="NO_ACTION">NO ACTION</option>
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
                          <option value="SET_NULL">SET NULL</option>
                          <option value="RESTRICT">RESTRICT</option>
                          <option value="SET_DEFAULT">SET DEFAULT</option>
                          <option value="NO_ACTION">NO ACTION</option>
                        </select>
                        <div className="fk-references">
                          Refr. Table:
                          {/* Select Target Table */}
                          <select
                            className="nodrag"
                            value={col.references?.tableName || ""}
                            onChange={(e) =>
                              onColumnChange(
                                col.id,
                                "tableName",
                                e.target.value,
                                false,
                                true
                              )
                            }
                          >
                            <option value="">Select Entity...</option>
                            {nodes
                              .filter((n) => n.id !== id) // Exclude self
                              .map((n) => (
                                <option key={n.id} value={n.data.tableName}>
                                  {n.data.tableName}
                                </option>
                              ))}
                          </select>
                          {/* Select Target Column (based on selected table) */}
                          Refr. Attr:
                          <select
                            className="nodrag"
                            value={col.references?.columnName || ""}
                            disabled={!col.references?.tableName}
                            onChange={(e) =>
                              onColumnChange(
                                col.id,
                                "columnName",
                                e.target.value,
                                false,
                                true
                              )
                            }
                          >
                            <option value="">Select Attr...</option>
                            {nodes
                              .find(
                                (n) =>
                                  n.data.tableName === col.references?.tableName
                              )
                              ?.data.columns.map((c) => (
                                <option key={c.id} value={c.name}>
                                  {c.name}{" "}
                                  {c.constraints.PRIMARY_KEY ? "(PK)" : ""}
                                </option>
                              ))}
                          </select>
                        </div>
                      </>
                    )}
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
    );
}
export default memo(ColumnRow);