// components/ActiveUsers.jsx
import React from "react";
import { useCollaboration } from "./CollaborationContext.jsx"; // Check path
import { Box, Tooltip, Avatar } from "@mui/material";

function getInitials(name) {
  if (!name) return "";
  const parts = name.trim().split(/\s+/);
  if (parts.length === 1) return (parts[0][0] || "").toUpperCase();
  return ((parts[0][0] || "") + (parts[1][0] || "")).toUpperCase();
}

const ActiveUsers = () => {
  const { connectedUsers } = useCollaboration();

  return (
    <div style={{ display: "flex", alignItems: "center" }}>
      <Box
        className="avatar-stack"
        sx={{
          display: "flex",
          alignItems: "center",
          cursor: "pointer",
        }}
      >
      {connectedUsers.map((c, i) => (
        <Box key={i} sx={{ zIndex: connectedUsers.length - i }}>
          <Tooltip title={c.name}>
            <Avatar
              src={c.picture || undefined}
              alt={c.name}
              sx={{
                width: 42,
                height: 42,
                fontSize: 20,
                border: `2px solid ${c.color}`,
                boxShadow: 1,
                ml: i === 0 ? 0 : -1.2,
                bgcolor: c.picture ? undefined : c.color,
                color: c.picture ? undefined : "white",
              }}
            >
              {!c.picture && getInitials(c.name)}
            </Avatar>
          </Tooltip>
        </Box>
      ))}
      </Box>

      {connectedUsers.length === 0 && (
        <span style={{ fontSize: "12px", color: "#999" }}>Connecting...</span>
      )}
    </div>
  );
};

export default ActiveUsers;
