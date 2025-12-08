import { useState, useMemo } from "react";
import { Box, Button, Snackbar, Alert } from "@mui/material";
import AddIcon from "@mui/icons-material/Add";
import { sampleQueries } from "./sampleData";
import LeftPanel from "../../components/LeftPanel";
import TopBar from "../../components/TopBar";
import TopBars from "../../components/Topbarforcannedquery";

import QueryCard from "../../components/QueryCard";
import QueryDialog from "../../components/QueryDialog";
import "./CannedQueries.css";

export default function CannedQueriesPage() {
  const [queries, setQueries] = useState(sampleQueries);
  const [searchTerm, setSearchTerm] = useState("");
  const [currentDatabase] = useState("ProductionDB");
  const [dialogOpen, setDialogOpen] = useState(false);
  const [editingQuery, setEditingQuery] = useState(null);
  const [snackbar, setSnackbar] = useState({ open: false, message: "", severity: "success" });

  const filteredQueries = useMemo(() => {
    return queries.filter((query) => {
      const s = searchTerm.toLowerCase();
      return (
        s === "" ||
        query.title.toLowerCase().includes(s) ||
        query.description.toLowerCase().includes(s) ||
        query.body.toLowerCase().includes(s)
      );
    });
  }, [queries, searchTerm]);

  const handleCreate = () => {
    setEditingQuery(null);
    setDialogOpen(true);
  };

  const handleEdit = (query) => {
    setEditingQuery(query);
    setDialogOpen(true);
  };

  const handleDelete = (id) => {
    setQueries(queries.filter((q) => q.id !== id));
    setSnackbar({
      open: true,
      message: "Query deleted successfully",
      severity: "success",
    });
  };

  const handleSave = (formData) => {
    if (editingQuery) {
      // Update existing query
      setQueries(
        queries.map((q) =>
          q.id === editingQuery.id
            ? {
                ...q,
                title: formData.title,
                description: formData.description,
                body: formData.body,
                updatedAt: new Date(),
              }
            : q
        )
      );
      setSnackbar({
        open: true,
        message: "Query updated successfully",
        severity: "success",
      });
    } else {
      // Create new query
      const newQuery = {
        id: crypto.randomUUID(),
        title: formData.title,
        description: formData.description,
        body: formData.body,
        tags: [],
        database: currentDatabase,
        createdAt: new Date(),
        updatedAt: new Date(),
      };
      setQueries([newQuery, ...queries]);
      setSnackbar({
        open: true,
        message: "Query created successfully",
        severity: "success",
      });
    }
  };

  const handleCloseSnackbar = () => {
    setSnackbar({ ...snackbar, open: false });
  };

  return (
    <Box className="canned-queries-container" sx={{ display: "flex" }}>
      <LeftPanel />
      <Box sx={{ flexGrow: 1, display: "flex", flexDirection: "column" }}>
        <TopBars loadDiagrams={() => {}} />

        <Box sx={{ p: 3 }}>
          {/* Header */}
          <Box className="canned-queries-header">
            <Box>
              <Box className="canned-queries-title-row">
                <h1 className="canned-queries-title">Canned Queries</h1>
                <span className="database-badge">{currentDatabase}</span>
              </Box>
              <p className="canned-queries-subtitle">
                Manage predefined query patterns for your schema
              </p>
            </Box>

            <div className="header-actions">
              <Button
                variant="contained"
                startIcon={<AddIcon />}
                className="new-query-button"
                onClick={handleCreate}
                sx={{ textTransform: "none" }}
              >
                New Query
              </Button>
            </div>
          </Box>

          {/* Query Cards Grid */}
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4 mt-4">
            {filteredQueries.map((q) => (
              <QueryCard
                key={q.id}
                query={q}
                onEdit={handleEdit}
                onDelete={handleDelete}
              />
            ))}
          </div>

          {/* Empty State */}
          {filteredQueries.length === 0 && (
            <Box
              sx={{
                textAlign: "center",
                py: 8,
                color: "text.secondary",
              }}
            >
              <h3>No queries found</h3>
              <p>Try adjusting your search or create a new query.</p>
            </Box>
          )}
        </Box>
      </Box>

      {/* Query Dialog */}
      <QueryDialog
        open={dialogOpen}
        onClose={() => setDialogOpen(false)}
        query={editingQuery}
        onSave={handleSave}
      />

      {/* Snackbar for notifications */}
      <Snackbar
        open={snackbar.open}
        autoHideDuration={3000}
        onClose={handleCloseSnackbar}
        anchorOrigin={{ vertical: "bottom", horizontal: "right" }}
      >
        <Alert
          onClose={handleCloseSnackbar}
          severity={snackbar.severity}
          sx={{ width: "100%" }}
        >
          {snackbar.message}
        </Alert>
      </Snackbar>
    </Box>
  );
}