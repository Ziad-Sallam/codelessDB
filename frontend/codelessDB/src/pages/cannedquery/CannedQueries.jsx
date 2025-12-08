import { useState, useMemo } from "react";
import { Box, Button } from "@mui/material";
import AddIcon from "@mui/icons-material/Add";
import { sampleQueries } from "./sampleData";
import LeftPanel from "../../components/LeftPanel";
import TopBar from "../../components/TopBar";
import QueryCard from "../../components/QueryCard"; 
import "./CannedQueries.css";

export default function CannedQueriesPage() {
  const [queries, setQueries] = useState(sampleQueries);
  const [searchTerm, setSearchTerm] = useState("");
  const [currentDatabase] = useState("ProductionDB");

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

  const handleEdit = (query) => {
    console.log("Edit query:", query);
  };

  const handleDelete = (id) => {
    setQueries(queries.filter((q) => q.id !== id));
  };

  return (
    <Box className="canned-queries-container" sx={{ display: "flex" }}>
      <LeftPanel />
      <Box sx={{ flexGrow: 1, display: "flex", flexDirection: "column" }}>
        <TopBar loadDiagrams={() => {}} />

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
        </Box>
      </Box>
    </Box>
  );
}
