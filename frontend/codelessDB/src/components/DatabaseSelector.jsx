import React, { useState, useEffect } from "react";
import {
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  CircularProgress,
  Box,
} from "@mui/material";
import { databaseApi } from "../pages/cannedquery/cannedQueriesApi";

export default function DatabaseSelector({ value, onChange, disabled = false }) {
  const [databases, setDatabases] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    fetchDatabases();
  }, []);

  const fetchDatabases = async () => {
    try {
      setLoading(true);
      const response = await databaseApi.getUserDatabases();
      const dbList = response.databases || [];
      setDatabases(dbList);

      if (dbList.length > 0 && !value) {
        onChange(dbList[0].databaseId, dbList[0].databaseName);
      }
    } catch (err) {
      console.error("Error fetching databases:", err);
      setError("Failed to load databases");
    } finally {
      setLoading(false);
    }
  };

  const handleChange = (event) => {
    const selectedId = event.target.value;
    const selectedDb = databases.find((db) => db.databaseId === selectedId);
    onChange(selectedId, selectedDb?.databaseName || "");
  };

  if (loading) {
    return (
      <Box sx={{ display: "flex", alignItems: "center", gap: 1 }}>
        <CircularProgress size={20} />
        <span>Loading databases...</span>
      </Box>
    );
  }

  if (error) {
    return <Box sx={{ color: "error.main" }}>{error}</Box>;
  }

  if (databases.length === 0) {
    return (
      <Box sx={{ color: "text.secondary" }}>
        No databases found. Create a database first.
      </Box>
    );
  }

  return (
    <FormControl fullWidth size="small" disabled={disabled}>
      <InputLabel id="database-select-label">Database</InputLabel>
      <Select
        labelId="database-select-label"
        id="database-select"
        value={value || ""}
        label="Database"
        onChange={handleChange}
        MenuProps={{
          disableScrollLock: true,
          hideBackdrop: true,
          sx: {
            pointerEvents: 'none',
            "& .MuiPaper-root": {
              pointerEvents: 'auto',
            }
          },
          anchorOrigin: {
            vertical: 'bottom',
            horizontal: 'left',
          },
          transformOrigin: {
            vertical: 'top',
            horizontal: 'left',
          },
        }}
      >
        {databases.map((db) => (
          <MenuItem key={db.databaseId} value={db.databaseId}>
            {db.databaseName}
            {db.serverName && (
              <span style={{ color: "#999", marginLeft: "8px" }}>
                ({db.serverName})
              </span>
            )}
          </MenuItem>
        ))}
      </Select>
    </FormControl>
  );
}