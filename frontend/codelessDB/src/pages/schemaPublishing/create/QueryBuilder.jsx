import { useState } from "react";
import {
  Button,
  TextField,
  Card,
  CardContent,
  Typography,
  Box,
  IconButton,
  Grid,
  Paper
} from "@mui/material";
import {
  Add as PlusIcon,
  Delete as TrashIcon,
  DragIndicator as DragIcon,
  Edit as EditIcon
} from "@mui/icons-material";

export const QueryBuilder = ({ queries, onChange }) => {
  const [editingId, setEditingId] = useState(null);

  const addQuery = () => {
    const newQuery = {
      id: Math.floor(Math.random() * 1000000),
      name: "",
      description: "",
      query: "",
    };
    onChange([...queries, newQuery]);
    setEditingId(newQuery.id);
  };

  const updateQuery = (id, field, value) => {
    onChange(
      queries.map((q) => (q.id === id ? { ...q, [field]: value } : q))
    );
  };

  const deleteQuery = (id) => {
    onChange(queries.filter((q) => q.id !== id));
    if (editingId === id) setEditingId(null);
  };

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
      {queries.map((q) => (
        <Card key={q.id} variant="outlined">
          <CardContent sx={{ p: 2, '&:last-child': { pb: 2 } }}>
            {editingId === q.id ? (
              <Box sx={{ display: 'flex', gap: 2 }}>
                <DragIcon sx={{ color: 'text.secondary', mt: 2, cursor: 'move' }} />
                <Box sx={{ flex: 1, display: 'flex', flexDirection: 'column', gap: 2 }}>
                  <Grid container spacing={2}>
                    <Grid item xs={12}>
                      <TextField
                        label="Query Name"
                        fullWidth
                        size="small"
                        value={q.name}
                        onChange={(e) => updateQuery(q.id, "name", e.target.value)}
                        placeholder="e.g., Get All Students"
                      />
                    </Grid>
                  </Grid>

                  <TextField
                    label="Description"
                    fullWidth
                    size="small"
                    value={q.description}
                    onChange={(e) => updateQuery(q.id, "description", e.target.value)}
                    placeholder="Brief description of what this query does"
                  />

                  <TextField
                    label="SQL Query"
                    fullWidth
                    multiline
                    rows={4}
                    value={q.query}
                    onChange={(e) => updateQuery(q.id, "query", e.target.value)}
                    placeholder="SELECT * FROM table_name WHERE..."
                    InputProps={{
                      style: { fontFamily: 'monospace', fontSize: '0.875rem' }
                    }}
                  />

                  <Box sx={{ display: 'flex', gap: 1 }}>
                    <Button
                      variant="outlined"
                      size="small"
                      onClick={() => setEditingId(null)}
                    >
                      Done
                    </Button>
                    <Button
                      variant="outlined"
                      color="error"
                      size="small"
                      startIcon={<TrashIcon />}
                      onClick={() => deleteQuery(q.id)}
                    >
                      Delete
                    </Button>
                  </Box>
                </Box>
              </Box>
            ) : (
              <Box sx={{ display: 'flex', gap: 2, alignItems: 'flex-start' }}>
                <DragIcon sx={{ color: 'text.secondary', mt: 0.5, cursor: 'move' }} />
                <Box sx={{ flex: 1 }}>
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 0.5 }}>
                    <Typography variant="subtitle2" fontWeight="bold">
                      {q.name || "Untitled Query"}
                    </Typography>
                  </Box>
                  {q.description && (
                    <Typography variant="body2" color="text.secondary" sx={{ mb: 1 }}>
                      {q.description}
                    </Typography>
                  )}
                  {q.query && (
                    <Paper
                      variant="outlined"
                      sx={{
                        p: 1.5,
                        bgcolor: 'action.hover',
                        fontFamily: 'monospace',
                        fontSize: '0.75rem',
                        overflowX: 'auto'
                      }}
                    >
                      {q.query}
                    </Paper>
                  )}
                </Box>
                <Box sx={{ display: 'flex', gap: 0.5 }}>
                  <Button
                    variant="outlined"
                    size="small"
                    onClick={() => setEditingId(q.id)}
                    sx={{ minWidth: 0, px: 1.5 }}
                  >
                    Edit
                  </Button>
                  <IconButton
                    size="small"
                    onClick={() => deleteQuery(q.id)}
                  >
                    <TrashIcon fontSize="small" />
                  </IconButton>
                </Box>
              </Box>
            )}
          </CardContent>
        </Card>
      ))}

      <Button
        variant="outlined"
        startIcon={<PlusIcon />}
        onClick={addQuery}
        fullWidth
        sx={{ borderStyle: 'dashed' }}
      >
        Add Query
      </Button>
    </Box>
  );
};
