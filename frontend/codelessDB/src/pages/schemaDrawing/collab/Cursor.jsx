import React, { memo } from 'react';
// import { MdnearMe } from "react-icons/md"; // Or use an SVG path

const Cursor = ({ x, y, color, name }) => {
  return (
    <div
      style={{
        position: 'absolute',
        top: 0,
        left: 0,
        // Using transform is much faster for performance than changing top/left
        transform: `translateX(${x}px) translateY(${y}px)`,
        zIndex: 9999,
        pointerEvents: 'none', // Crucial: lets clicks pass through to nodes below
        transition: 'transform 0.05s linear', // Smooths out the network jitter
      }}
    >
      {/* The Cursor Icon */}
      <svg
        style={{ position: 'absolute', left: 0, top: 0 }}
        width="24"
        height="36"
        viewBox="0 0 24 36"
        fill="none"
        xmlns="http://www.w3.org/2000/svg"
      >
        <path
          d="M5.65376 12.3673H5.46026L5.31717 12.4976L0.500002 16.8829L0.500002 1.19134L11.7841 12.3673H5.65376Z"
          fill={color}
          stroke="white"
        />
      </svg>

      {/* The Name Tag */}
      <div
        style={{
          position: 'absolute',
          top: 20,
          left: 10,
          backgroundColor: color,
          color: 'white',
          padding: '2px 8px',
          borderRadius: 4,
          fontSize: 12,
          whiteSpace: 'nowrap',
          boxShadow: '0 2px 4px rgba(0,0,0,0.2)'
        }}
      >
        {name}
      </div>
    </div>
  );
};

export default memo(Cursor);