// components/ActiveUsers.jsx
import React from "react";
import { useCollaboration } from "./CollaborationContext.jsx";
import { Box, Tooltip, Avatar } from "@mui/material";

function getInitials(name) {
  if (!name) return "?";
  const parts = name.trim().split(/\s+/);
  if (parts.length === 1) return (parts[0][0] || "").toUpperCase();
  return ((parts[0][0] || "") + (parts[1][0] || "")).toUpperCase();
}

const ActiveUsers = () => {
  const { connectedUsers = [] } = useCollaboration() || {};

  return (
    <div style={{ display: "flex", alignItems: "center" }}>
      <Box
        className="avatar-stack"
        sx={{
          display: "flex",
          alignItems: "center",
          cursor: "pointer",
          p: 0.5,
        }}
      >
        {connectedUsers.map((c, i) => (
          <Box
            key={c.clientId}
            sx={{
              zIndex: connectedUsers.length - i,
              ml: i === 0 ? 0 : -1.5,
              transition: "transform 0.2s",
              "&:hover": {
                zIndex: 100,
                transform: "translateY(-4px)",
              },
            }}
          >
            <Tooltip title={c.name || "Unknown User"} arrow>
              <Avatar
                src={c.picture || undefined}
                alt={c.name}
                sx={{
                  width: 38,
                  height: 38,
                  fontSize: 16,
                  border: `2px solid ${c.color || "#ccc"}`,
                  bgcolor: c.picture ? "transparent" : (c.color || "#ccc"),
                  color: "#fff",
                  boxShadow: "0 2px 4px rgba(0,0,0,0.2)",
                }}
              >
                {!c.picture && getInitials(c.name)}
              </Avatar>
            </Tooltip>
          </Box>
        ))}
      </Box>

      {connectedUsers.length === 0 && (
        <span style={{ fontSize: "12px", color: "#999", marginLeft: "8px" }}>
          Connecting...
        </span>
      )}
    </div>
  );
};

export default ActiveUsers;