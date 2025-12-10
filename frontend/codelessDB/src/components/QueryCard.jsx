import React, { useState } from "react";
import { Box, IconButton, Snackbar, Alert } from "@mui/material";
import ContentCopyIcon from "@mui/icons-material/ContentCopy";
import EditIcon from "@mui/icons-material/Edit";
import DeleteIcon from "@mui/icons-material/Delete";
import PlayArrowIcon from "@mui/icons-material/PlayArrow";
import "../pages/cannedquery/CannedQueries.css";

export default function QueryCard({ query, onEdit, onDelete }) {
  const [snackbarOpen, setSnackbarOpen] = useState(false);

  const copyToClipboard = () => {
    navigator.clipboard.writeText(query.body);
    setSnackbarOpen(true);
  };

  const handleCloseSnackbar = () => {
    setSnackbarOpen(false);
  };

  return (
    <>
      <Box className="query-card" sx={{ position: "relative" }}>
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

        <Box className="query-card-header">
          <h3 className="query-card-title">{query.title}</h3>
          <p className="query-card-description">{query.description}</p>
        </Box>

        <Box className="query-code-block">
          <pre>
            <code>{query.body}</code>
          </pre>
        </Box>
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
      <Snackbar
        open={snackbarOpen}
        autoHideDuration={2000}
        onClose={handleCloseSnackbar}
        anchorOrigin={{ vertical: "bottom", horizontal: "right" }}
      >
        <Alert
          onClose={handleCloseSnackbar}
          severity="success"
          sx={{ width: "100%" }}
        >
          Query copied to clipboard!
        </Alert>
      </Snackbar>
    </>
  );
}