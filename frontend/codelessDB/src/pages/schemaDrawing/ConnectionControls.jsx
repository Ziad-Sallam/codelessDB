export default function Toolbar({addNode,selectedRelationType,setSelectedRelationType}){
    return(
        <div className="schema-toolbar">
          <RelationButton
            active={selectedRelationType === "1:1"}
            color="#3b82f6"
            onClick={() => setSelectedRelationType("1:1")}
            label="1 : 1"
          />
          <RelationButton
            active={selectedRelationType === "1:N"}
            color="#10b981"
            onClick={() => setSelectedRelationType("1:N")}
            label="1 : N"
          />
          <RelationButton
            active={selectedRelationType === "N:1"}
            color="#f59e0b"
            onClick={() => setSelectedRelationType("N:1")}
            label="N : 1"
          />
          <RelationButton
            active={selectedRelationType === "M:N"}
            color="#ef4444"
            onClick={() => setSelectedRelationType("M:N")}
            label="M : N"
          />
          <div
            style={{
              width: 1,
              height: 24,
              background: "#e2e8f0",
              margin: "0 4px",
            }}
          ></div>
          <button className="add-node-btn" onClick={addNode}>
            + Add Entity
          </button>
        </div>
    );
}

function RelationButton({ active, color, onClick, label }) {
  return (
    <button
      onClick={onClick}
      className={`relation-btn ${active ? "active" : ""}`}
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