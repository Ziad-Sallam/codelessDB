// QueryCard.jsx
import React from "react";
import { Box, IconButton } from "@mui/material";
import ContentCopyIcon from "@mui/icons-material/ContentCopy";
import EditIcon from "@mui/icons-material/Edit";
import DeleteIcon from "@mui/icons-material/Delete";
import PlayArrowIcon from "@mui/icons-material/PlayArrow";
import "../pages/cannedquery/CannedQueries.css";

export default function QueryCard({ query, onEdit, onDelete }) {
  const copyToClipboard = () => {
    navigator.clipboard.writeText(query.body);
  };

  return (
    <Box className="query-card" sx={{ position: "relative" }}>
      {/* Action Buttons */}
      <Box className="query-actions">
        <IconButton
          size="small"
          className="query-action-button"
          onClick={copyToClipboard}
          title="Copy to clipboard"
        >
          <ContentCopyIcon fontSize="small" />
        </IconButton>
        <IconButton
          size="small"
          className="query-action-button"
          onClick={() => onEdit(query)}
          title="Edit query"
        >
          <EditIcon fontSize="small" />
        </IconButton>
        <IconButton
          size="small"
          className="query-action-button delete"
          onClick={() => onDelete(query.id)}
          title="Delete query"
        >
          <DeleteIcon fontSize="small" />
        </IconButton>
      </Box>

      {/* Card Header */}
      <Box className="query-card-header">
        <h3 className="query-card-title">{query.title}</h3>
        <p className="query-card-description">{query.description}</p>
      </Box>

      {/* Code Block */}
      <Box className="query-code-block">
        <pre>
          <code>{query.body}</code>
        </pre>
      </Box>

      {/* Card Footer */}
      <Box className="query-card-footer">
        <span className="query-updated-date">
          Updated{" "}
          {query.updatedAt.toLocaleDateString("en-US", {
            month: "numeric",
            day: "numeric",
            year: "numeric",
          })}
        </span>
        <button className="test-query-button">
          <PlayArrowIcon sx={{ fontSize: 16 }} />
          Test Query
        </button>
      </Box>
    </Box>
  );
}