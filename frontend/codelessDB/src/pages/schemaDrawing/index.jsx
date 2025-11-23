import Node from "./node/node.jsx";
import OneToOneEdge from "./edge/OneToOneEdge.jsx";
import OneToManyEdge from "./edge/OneToManyEdge.jsx";
import ManyToOneEdge from "./edge/ManyToOneEdge.jsx";
import ManyToManyEdge from "./edge/ManyToManyEdge.jsx";

export const nodeTypes = { "Defult-Node": Node };
export const edgeTypes = {
  oneToOne: OneToOneEdge,
  oneToMany: OneToManyEdge,
  manyToOne: ManyToOneEdge,
  manyToMany: ManyToManyEdge
};
