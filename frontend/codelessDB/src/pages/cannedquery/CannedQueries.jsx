import { useState, useMemo, useEffect } from "react";
import { Box, Button, Snackbar, Alert, CircularProgress } from "@mui/material";
import AddIcon from "@mui/icons-material/Add";
import LeftPanel from "../../components/LeftPanel";
import TopBars from "../../components/Topbarforcannedquery";
import QueryCard from "../../components/QueryCard";
import QueryDialog from "../../components/QueryDialog";
import DatabaseSelector from "../../components/DatabaseSelector";
import { cannedQueriesApi } from "./cannedQueriesApi";
import "./CannedQueries.css";

export default function CannedQueriesPage() {
  const [queries, setQueries] = useState([]);
  const [searchTerm, setSearchTerm] = useState("");
  const [currentDatabaseId, setCurrentDatabaseId] = useState(null);
  const [currentDatabaseName, setCurrentDatabaseName] = useState("");
  const [dialogOpen, setDialogOpen] = useState(false);
  const [editingQuery, setEditingQuery] = useState(null);
  const [loading, setLoading] = useState(false);
  const [snackbar, setSnackbar] = useState({
    open: false,
    message: "",
    severity: "success",
  });

  useEffect(() => {
    if (currentDatabaseId) {
      fetchQueries();
    }
  }, [currentDatabaseId]);

  const fetchQueries = async () => {
    try {
      setLoading(true);
      const data = await cannedQueriesApi.getAllQueries(currentDatabaseId);
      const transformedQueries = data.map((q) => ({
        id: q.id,
        title: q.name,
        description: q.description || "",
        body: q.query,
        database: q.databaseName,
        createdAt: new Date(q.createdAt),
        updatedAt: new Date(q.updatedAt),
      }));
      setQueries(transformedQueries);
    } catch (error) {
      console.error("Error fetching queries:", error);
      const errorMessage = error.response?.data?.message || error.response?.data || error.message || "Unknown error";
      setSnackbar({
        open: true,
        message: "Failed to load queries: " + (typeof errorMessage === 'object' ? JSON.stringify(errorMessage) : errorMessage),
        severity: "error",
      });
    } finally {
      setLoading(false);
    }
  };

  const handleDatabaseChange = (databaseId, databaseName) => {
    setCurrentDatabaseId(databaseId);
    setCurrentDatabaseName(databaseName);
  };

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
    if (!currentDatabaseId) {
      setSnackbar({
        open: true,
        message: "Please select a database first",
        severity: "warning",
      });
      return;
    }
    setEditingQuery(null);
    setDialogOpen(true);
  };

  const handleEdit = (query) => {
    setEditingQuery(query);
    setDialogOpen(true);
  };

  const handleDelete = async (id) => {
    try {
      await cannedQueriesApi.deleteQuery(id, currentDatabaseId);
      setQueries(queries.filter((q) => q.id !== id));
      setSnackbar({
        open: true,
        message: "Query deleted successfully",
        severity: "success",
      });
    } catch (error) {
      console.error("Error deleting query:", error);
      const errorMessage = error.response?.data?.message || error.response?.data || error.message || "Unknown error";
      setSnackbar({
        open: true,
        message: "Failed to delete query: " + (typeof errorMessage === 'object' ? JSON.stringify(errorMessage) : errorMessage),
        severity: "error",
      });
    }
  };

  const handleSave = async (queryData) => {
    try {
      if (editingQuery) {
        // Data is already transformed by QueryDialog
        const updatedQuery = await cannedQueriesApi.updateQuery(
          editingQuery.id,
          queryData
        );
        setQueries(
          queries.map((q) =>
            q.id === editingQuery.id
              ? {
                id: updatedQuery.id,
                title: updatedQuery.name,
                description: updatedQuery.description,
                body: updatedQuery.query,
                database: updatedQuery.databaseName,
                createdAt: new Date(updatedQuery.createdAt),
                updatedAt: new Date(updatedQuery.updatedAt),
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
        // Data is already transformed by QueryDialog
        const newQuery = await cannedQueriesApi.createQuery(queryData);
        const transformedQuery = {
          id: newQuery.id,
          title: newQuery.name,
          description: newQuery.description,
          body: newQuery.query,
          database: newQuery.databaseName,
          createdAt: new Date(newQuery.createdAt),
          updatedAt: new Date(newQuery.updatedAt),
        };
        setQueries([transformedQuery, ...queries]);
        setSnackbar({
          open: true,
          message: "Query created successfully",
          severity: "success",
        });
      }
    } catch (error) {
      console.error("Error saving query:", error);
      const errorMessage = error.response?.data?.message || error.response?.data || error.message || "Unknown error";
      setSnackbar({
        open: true,
        message: "Failed to save query: " + (typeof errorMessage === 'object' ? JSON.stringify(errorMessage) : errorMessage),
        severity: "error",
      });
    }
  };

  const handleCloseSnackbar = () => {
    setSnackbar({ ...snackbar, open: false });
  };

  const handleSaveError = (errorMessage) => {
    setSnackbar({
      open: true,
      message: errorMessage,
      severity: "warning",
    });
  };

  return (
    <Box className="canned-queries-container" sx={{ display: "flex" }}>
      <LeftPanel />
      <Box sx={{ flexGrow: 1, display: "flex", flexDirection: "column" }}>
        <TopBars loadDiagrams={() => { }} />
        <Box sx={{ p: 3 }}>
          <Box className="canned-queries-header">
            <Box>
              <Box className="canned-queries-title-row">
                <h1 className="canned-queries-title">Canned Queries</h1>
                {currentDatabaseName && (
                  <span className="database-badge">{currentDatabaseName}</span>
                )}
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
                disabled={!currentDatabaseId}
                sx={{ textTransform: "none" }}
              >
                New Query
              </Button>
            </div>
          </Box>

          <Box sx={{ mb: 3, maxWidth: 400 }}>
            <DatabaseSelector
              value={currentDatabaseId}
              onChange={handleDatabaseChange}
            />
          </Box>
          {loading && (
            <Box
              sx={{
                display: "flex",
                justifyContent: "center",
                alignItems: "center",
                py: 8,
              }}
            >
              <CircularProgress />
            </Box>
          )}
          {!loading && currentDatabaseId && (
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
          )}
          {!loading && currentDatabaseId && filteredQueries.length === 0 && (
            <Box
              sx={{
                textAlign: "center",
                py: 8,
                color: "text.secondary",
              }}
            >
              <h3>No queries found</h3>
              <p>
                {queries.length === 0
                  ? "Create your first canned query to get started."
                  : "Try adjusting your search or create a new query."}
              </p>
            </Box>
          )}
          {!loading && !currentDatabaseId && (
            <Box
              sx={{
                textAlign: "center",
                py: 8,
                color: "text.secondary",
              }}
            >
              <h3>Select a database</h3>
              <p>Choose a database from the dropdown above to view and manage its canned queries.</p>
            </Box>
          )}
        </Box>
      </Box>
      <QueryDialog
        open={dialogOpen}
        onClose={() => setDialogOpen(false)}
        query={editingQuery}
        onSave={handleSave}
        onSaveError={handleSaveError}
        databaseId={currentDatabaseId}
      />
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