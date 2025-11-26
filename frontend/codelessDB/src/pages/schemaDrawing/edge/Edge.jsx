import {
    useInternalNode,
    getSmoothStepPath,
    BaseEdge,
    EdgeLabelRenderer
} from "@xyflow/react";
import getLabelCoords from "./HelperFunctions";
import { getEdgeParams } from "./HelperFunctions";

export default function FloatingEdge({ id, source, target, markerEnd, style, data }) {
    const sourceNode = useInternalNode(source);
    const targetNode = useInternalNode(target);

    if (!sourceNode || !targetNode) {
        return null;
    }

    const { sx, sy, tx, ty, sourcePos, targetPos } = getEdgeParams(sourceNode, targetNode);

    const [edgePath] = getSmoothStepPath({
        sourceX: sx,
        sourceY: sy,
        sourcePosition: sourcePos,
        targetX: tx,
        targetY: ty,
        targetPosition: targetPos,
        borderRadius: 10,
        offset: 20
    });

    // Split the type string "1:N" into ["1", "N"]
    const relationType = data?.type || '1:N';
    const [startLabel, endLabel] = relationType.split(':');

    // Calculate positions for the labels
    const sourceLabelPos = getLabelCoords(tx, ty, targetPos);
    const targetLabelPos = getLabelCoords(sx, sy, sourcePos);

    const labelStyle = {
        position: 'absolute',
        background: 'white', // White background to hide line behind text
        padding: '0px 3px',
        borderRadius: 3,
        fontSize: 12,
        fontWeight: 'bold',
        color: '#333',
        pointerEvents: 'none',
        zIndex: 10,
        transform: 'translate(-50%, -50%)', // Center the div on the coordinate
    };

    return (
        <>
            <BaseEdge id={id} path={edgePath} markerEnd={markerEnd} style={style} />

            <EdgeLabelRenderer>
                {/* Source Label (e.g., '1') */}
                <div style={{ ...labelStyle, left: sourceLabelPos.x, top: sourceLabelPos.y }}>
                    {startLabel}
                </div>

                {/* Target Label (e.g., 'N') */}
                <div style={{ ...labelStyle, left: targetLabelPos.x, top: targetLabelPos.y }}>
                    {endLabel}
                </div>
            </EdgeLabelRenderer>
        </>
    );
}
