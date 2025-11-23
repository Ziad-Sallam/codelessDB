import React from 'react';

export default function ManyToManyEdge({
  id, sourceX, sourceY, targetX, targetY, data
}) {
  const color = '#797979ff'; // crimson
  const midX = (sourceX + targetX) / 2;
  const midY = (sourceY + targetY) / 2;
  const path = `
  M ${sourceX},${sourceY}
  L ${midX},${sourceY}
  L ${midX},${targetY}
  L ${targetX},${targetY}
`;


  // thicker line to denote M:N
  return (
    <g id={id}>
      <path d={path} stroke={color} strokeWidth={3} fill="none" markerEnd="url(#arrow)" strokeDasharray="6 4" />
      <text x={midX} y={midY - 6} fontSize={12} textAnchor="middle" fill={color} style={{ fontWeight: 800 }}>
        {data?.type || 'M:N'}
      </text>
    </g>
  );
}
