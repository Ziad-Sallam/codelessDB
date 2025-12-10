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
  Collapse,
  Alert,
  CircularProgress,
  Chip,
} from "@mui/material";
import CloseIcon from "@mui/icons-material/Close";
import SchemaIcon from "@mui/icons-material/Schema";
import CheckCircleIcon from "@mui/icons-material/CheckCircle";
import ErrorIcon from "@mui/icons-material/Error";
import ExpandMoreIcon from "@mui/icons-material/ExpandMore";
import ExpandLessIcon from "@mui/icons-material/ExpandLess";
import CodeMirror from "@uiw/react-codemirror";
import { sql } from "@codemirror/lang-sql";
import { linter } from "@codemirror/lint";
import { parse } from "sql-parser-cst";
import "../pages/cannedquery/QueryDialog.css";
import { databaseApi } from "../pages/cannedquery/cannedQueriesApi";

// SQL Linter function for real-time syntax validation
const sqlLinter = linter((view) => {
  const diagnostics = [];
  const content = view.state.doc.toString();

  if (!content.trim()) {
    return diagnostics;
  }

  try {
    // Try to parse the SQL query
    parse(content, { dialect: 'mysql' });
  } catch (error) {
    // If parsing fails, add diagnostic for the error
    let from = 0;
    let to = content.length;

    // Try to extract position from error if available
    if (error.location) {
      const lines = content.split('\n');
      let pos = 0;
      for (let i = 0; i < error.location.start.line - 1; i++) {
        pos += lines[i].length + 1;
      }
      from = pos + error.location.start.column - 1;
      to = Math.min(from + 10, content.length); // Underline ~10 chars
    }

    diagnostics.push({
      from: from,
      to: to,
      severity: "error",
      message: error.message || "SQL syntax error",
    });
  }

  return diagnostics;
});

export default function QueryDialog({ open, onClose, query, onSave, databaseId }) {
  const [formData, setFormData] = useState({
    title: "",
    description: "",
    body: "",
  });
  const [showDDL, setShowDDL] = useState(false);
  const [ddlContent, setDdlContent] = useState("");
  const [loadingDDL, setLoadingDDL] = useState(false);
  const [syntaxCheck, setSyntaxCheck] = useState(null);
  const [checkingSyntax, setCheckingSyntax] = useState(false);

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
    setSyntaxCheck(null);
  }, [query, open]);
  useEffect(() => {
    setDdlContent("");
    setShowDDL(false);
  }, [databaseId]);

  const fetchDDL = async () => {
    if (!databaseId) return;

    try {
      setLoadingDDL(true);
      const ddl = await databaseApi.getDatabaseDDL(databaseId);
      setDdlContent(ddl || "No DDL available");
    } catch (error) {
      console.error("Error fetching DDL:", error);
      setDdlContent("Failed to load DDL");
    } finally {
      setLoadingDDL(false);
    }
  };

  const handleToggleDDL = () => {
    if (!showDDL && !ddlContent) {
      fetchDDL();
    }
    setShowDDL(!showDDL);
  };

  const checkSyntax = () => {
    if (!formData.body.trim()) {
      setSyntaxCheck({
        valid: false,
        message: "Query body is empty",
      });
      return;
    }

    try {
      setCheckingSyntax(true);
      parse(formData.body, { dialect: 'mysql' });

      setSyntaxCheck({
        valid: true,
        message: "✓ Query syntax is valid! No errors found.",
        errors: [],
      });
    } catch (error) {
      console.error("SQL syntax error:", error);

      const errorMessage = error.message || "Syntax error detected";
      const errors = [];

      if (error.location) {
        errors.push(`Error at line ${error.location.start.line}, column ${error.location.start.column}`);
      }

      setSyntaxCheck({
        valid: false,
        message: `✗ ${errorMessage}`,
        errors: errors,
      });
    } finally {
      setCheckingSyntax(false);
    }
  };

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
      maxWidth="lg"
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
                rows={3}
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

            {/* DDL Viewer Section */}
            <Box className="ddl-section">
              <Button
                startIcon={showDDL ? <ExpandLessIcon /> : <ExpandMoreIcon />}
                endIcon={<SchemaIcon />}
                onClick={handleToggleDDL}
                className="ddl-toggle-button"
                variant="outlined"
                size="small"
                sx={{ mb: 1 }}
              >
                {showDDL ? "Hide" : "View"} Database Schema (DDL)
              </Button>

              <Collapse in={showDDL}>
                <Box className="ddl-viewer">
                  {loadingDDL ? (
                    <Box sx={{ display: "flex", justifyContent: "center", p: 3 }}>
                      <CircularProgress size={24} />
                    </Box>
                  ) : (
                    <pre className="ddl-content">
                      <code>{ddlContent}</code>
                    </pre>
                  )}
                </Box>
              </Collapse>
            </Box>

            <Box className="query-form-field">
              <Box sx={{ display: "flex", justifyContent: "space-between", alignItems: "center", mb: 1 }}>
                <label htmlFor="query-body" className="query-form-label">
                  Query Body
                </label>
                <Button
                  size="small"
                  variant="outlined"
                  onClick={checkSyntax}
                  disabled={checkingSyntax || !formData.body.trim()}
                  className="syntax-check-button"
                  startIcon={checkingSyntax ? <CircularProgress size={16} /> : <CheckCircleIcon />}
                >
                  {checkingSyntax ? "Checking..." : "Check Syntax"}
                </Button>
              </Box>

              {syntaxCheck && (
                <Alert
                  severity={syntaxCheck.valid ? "success" : "error"}
                  icon={syntaxCheck.valid ? <CheckCircleIcon /> : <ErrorIcon />}
                  sx={{ mb: 1 }}
                  onClose={() => setSyntaxCheck(null)}
                >
                  <Box>
                    <strong>{syntaxCheck.message}</strong>
                    {syntaxCheck.errors && syntaxCheck.errors.length > 0 && (
                      <Box sx={{ mt: 1 }}>
                        {syntaxCheck.errors.map((error, idx) => (
                          <Box key={idx} sx={{ fontSize: "0.875rem", mt: 0.5 }}>
                            • {error}
                          </Box>
                        ))}
                      </Box>
                    )}
                  </Box>
                </Alert>
              )}

              <Box className="codemirror-container" sx={{ border: '1px solid #d0d0d0', borderRadius: 1, overflow: 'hidden' }}>
                <CodeMirror
                  value={formData.body}
                  height="300px"
                  extensions={[sql(), sqlLinter]}
                  onChange={(value) => {
                    setFormData({ ...formData, body: value });
                    if (syntaxCheck) {
                      setSyntaxCheck(null);
                    }
                  }}
                  placeholder="SELECT ..."
                  theme="light"
                  basicSetup={{
                    lineNumbers: true,
                    highlightActiveLineGutter: true,
                    highlightSpecialChars: true,
                    foldGutter: true,
                    drawSelection: true,
                    dropCursor: true,
                    allowMultipleSelections: true,
                    indentOnInput: true,
                    bracketMatching: true,
                    closeBrackets: true,
                    autocompletion: true,
                    rectangularSelection: true,
                    crosshairCursor: true,
                    highlightActiveLine: true,
                    highlightSelectionMatches: true,
                    closeBracketsKeymap: true,
                    searchKeymap: true,
                    foldKeymap: true,
                    completionKeymap: true,
                    lintKeymap: true,
                  }}
                />
              </Box>
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