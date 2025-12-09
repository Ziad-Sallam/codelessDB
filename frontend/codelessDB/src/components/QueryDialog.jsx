import React, { useState, useEffect } from "react";
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  Button,
  IconButton,
  Box,
} from "@mui/material";
import CloseIcon from "@mui/icons-material/Close";
import "../pages/cannedquery/QueryDialog.css";

export default function QueryDialog({ open, onClose, query, onSave }) {
  const [formData, setFormData] = useState({
    title: "",
    description: "",
    body: "",
  });

  useEffect(() => {
    if (query) {
      setFormData({
        title: query.title,
        description: query.description,
        body: query.body,
      });
    } else {
      setFormData({
        title: "",
        description: "",
        body: "",
      });
    }
  }, [query, open]);

  const handleSubmit = (e) => {
    e.preventDefault();
    if (formData.title.trim() && formData.body.trim()) {
      onSave(formData);
      onClose();
    }
  };

  const handleCancel = () => {
    onClose();
  };

  return (
    <Dialog
      open={open}
      onClose={onClose}
      maxWidth="md"
      fullWidth
      PaperProps={{
        className: "query-dialog-paper",
      }}
    >
      <DialogTitle className="query-dialog-title">
        <span>{query ? "Edit Query" : "Create New Query"}</span>
        <IconButton onClick={onClose} size="small" className="query-dialog-close">
          <CloseIcon />
        </IconButton>
      </DialogTitle>

      <form onSubmit={handleSubmit}>
        <DialogContent dividers className="query-dialog-content">
          <Box className="query-dialog-form">
            <Box className="query-form-field">
              <label htmlFor="query-name" className="query-form-label">
                Query Name
              </label>
              <TextField
                id="query-name"
                fullWidth
                placeholder="Get Active Users"
                value={formData.title}
                onChange={(e) =>
                  setFormData({ ...formData, title: e.target.value })
                }
                variant="outlined"
                size="medium"
                className="query-form-input"
              />
            </Box>
            <Box className="query-form-field">
              <label htmlFor="query-description" className="query-form-label">
                Description
              </label>
              <TextField
                id="query-description"
                fullWidth
                multiline
                rows={4}
                placeholder="Retrieves all users who have been active in the last 30 days with their activity count."
                value={formData.description}
                onChange={(e) =>
                  setFormData({ ...formData, description: e.target.value })
                }
                variant="outlined"
                size="medium"
                className="query-form-input"
              />
            </Box>
            <Box className="query-form-field">
              <label htmlFor="query-body" className="query-form-label">
                Query Body
              </label>
              <TextField
                id="query-body"
                fullWidth
                multiline
                rows={12}
                placeholder="SELECT ..."
                value={formData.body}
                onChange={(e) =>
                  setFormData({ ...formData, body: e.target.value })
                }
                variant="outlined"
                size="medium"
                className="query-form-input query-body-input"
              />
            </Box>
          </Box>
        </DialogContent>
        <DialogActions className="query-dialog-actions">
          <Button
            onClick={handleCancel}
            variant="outlined"
            className="query-dialog-cancel-btn"
          >
            Cancel
          </Button>
          <Button
            type="submit"
            variant="contained"
            disabled={!formData.title.trim() || !formData.body.trim()}
            className="query-dialog-save-btn"
          >
            {query ? "Save Changes" : "Create Query"}
          </Button>
        </DialogActions>
      </form>
    </Dialog>
  );
}