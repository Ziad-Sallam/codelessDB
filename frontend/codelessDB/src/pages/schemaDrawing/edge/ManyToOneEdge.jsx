import React from 'react';

export default function ManyToOneEdge({
  id, sourceX, sourceY, targetX, targetY, data
}) {
  const color = '#130069ff'; // amber
  const midX = (sourceX + targetX) / 2;
  const midY = (sourceY + targetY) / 2;
  const path = `
  M ${sourceX},${sourceY}
  L ${midX},${sourceY}
  L ${midX},${targetY}
  L ${targetX},${targetY}
`;
  return (
    <g id={id}>
      <path d={path} stroke={color} strokeWidth={2} fill="none" markerEnd="url(#arrow)" />
      <text x={midX} y={midY - 6} fontSize={12} textAnchor="middle" fill={color} style={{ fontWeight: 700 }}>
        {data?.type || 'N:1'}
      </text>
    </g>
  );
}
