import React, { useState } from "react";
import { TextField, Tabs, Tab, Box, Typography, Paper } from "@mui/material";
import { Edit as EditIcon, Visibility as EyeIcon } from "@mui/icons-material";
import { MarkdownRenderer } from "../../schemaPreviewing/preview/MarkdownRenderer";

export const MarkdownEditor = ({ value, onChange, placeholder }) => {
  const [activeTab, setActiveTab] = useState(0);

  const handleTabChange = (event, newValue) => {
    setActiveTab(newValue);
  };

  return (
    <Box sx={{ width: '100%' }}>
      <Box sx={{ borderBottom: 1, borderColor: 'divider' }}>
        <Tabs value={activeTab} onChange={handleTabChange} aria-label="markdown editor tabs">
          <Tab icon={<EditIcon fontSize="small" />} iconPosition="start" label="Write" />
          <Tab icon={<EyeIcon fontSize="small" />} iconPosition="start" label="Preview" />
        </Tabs>
      </Box>

      <Box sx={{ mt: 2 }}>
        {activeTab === 0 && (
          <Box>
            <TextField
              multiline
              fullWidth
              minRows={15}
              value={value}
              onChange={(e) => onChange(e.target.value)}
              placeholder={placeholder}
              variant="outlined"
              InputProps={{
                style: { fontFamily: 'monospace', fontSize: '0.875rem' }
              }}
              sx={{ bgcolor: 'background.paper' }}
            />
            <Typography variant="caption" color="text.secondary" sx={{ mt: 1, display: 'block' }}>
              Supports Markdown formatting. Use ** for bold, * for italic, # for headings, etc.
            </Typography>
          </Box>
        )}

        {activeTab === 1 && (
          <Paper variant="outlined" sx={{ minHeight: 400, p: 3, bgcolor: 'background.paper' }}>
            {value ? (
              <MarkdownRenderer content={value} />
            ) : (
              <Typography color="text.secondary" fontStyle="italic">
                Nothing to preview yet...
              </Typography>
            )}
          </Paper>
        )}
      </Box>
    </Box>
  );
};
