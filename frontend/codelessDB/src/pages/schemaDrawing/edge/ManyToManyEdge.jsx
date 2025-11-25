import React from 'react';

export default function ManyToManyEdge({id, sourceX, sourceY, targetX, targetY, data}) {
   const color = '#d00000';
  const midX = (sourceX + targetX) / 2;
  const midY = (sourceY + targetY) / 2;
  const path = `M ${sourceX},${sourceY} L ${midX},${sourceY} L ${midX},${targetY} L ${targetX},${targetY}`;
  
  return (
    <>
      <defs>
        <marker id={`arrow-${id}`} markerWidth="10" markerHeight="10" refX="9" refY="3" orient="auto" markerUnits="strokeWidth">
          <path d="M0,0 L0,6 L9,3 z" fill={color} />
        </marker>
      </defs>
      <g id={id}>
        <path d={path} stroke={color} strokeWidth={3} fill="none" markerEnd={`url(#arrow-${id})`} strokeDasharray="6 4" />
        <text x={midX} y={midY - 6} fontSize={12} textAnchor="middle" fill={color} style={{ fontWeight: 800 }}>
          {data?.type || 'M:N'}
        </text>
      </g>
    </>
  );
}
