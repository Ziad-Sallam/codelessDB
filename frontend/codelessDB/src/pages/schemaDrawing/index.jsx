import Node from "./node/node.jsx";
import FloatingEdge from "./edge/Edge.jsx";

export const nodeTypes = { "Defult-Node": Node };
export const edgeTypes = {
  oneToOne: FloatingEdge,
  oneToMany: FloatingEdge,
  manyToOne: FloatingEdge,
  manyToMany: FloatingEdge
};
