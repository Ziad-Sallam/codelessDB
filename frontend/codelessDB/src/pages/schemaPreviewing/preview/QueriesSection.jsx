import React from 'react';
import { Card, CardContent, CardHeader, Typography, Button, Box, Paper } from '@mui/material';
import { Code as CodeIcon, ContentCopy as CopyIcon } from '@mui/icons-material';
import { useNotification } from '../../../components/NotificationContext';

export const QueriesSection = ({ queries }) => {
  const { showSuccess } = useNotification();

  // const copyQuery = (query) => {
  //   navigator.clipboard.writeText(query);
  //   showSuccess("Query copied to clipboard!");
  // };

  return (
    <Card variant="outlined">
      <CardHeader
        title={
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            {/* <CodeIcon color="primary" /> */}
            <Box>
              <Typography variant="h6" fontSize="1rem" fontWeight="bold">Predefined Transactions</Typography>
              <Typography variant="body2" color="text.secondary">Ready-to-use queries for common operations</Typography>
            </Box>
          </Box>
        }
      />
      <CardContent>
        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
          {queries.map((q) => (
            <Paper
              key={q.id}
              variant="outlined"
              sx={{ p: 2, '&:hover': { borderColor: 'primary.main' }, transition: 'border-color 0.2s' }}
            >
              <Box sx={{ display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between', gap: 2, mb: 1 }}>
                <Box sx={{ flex: 1 }}>
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 0.5 }}>
                    <Typography variant="subtitle2" fontWeight="bold">{q.name}</Typography>
                  </Box>
                  <Typography variant="body2" color="text.secondary">{q.description}</Typography>
                </Box>
                {/* <Button
                  variant="outlined"
                  size="small"
                  startIcon={<CopyIcon />}
                  onClick={() => copyQuery(q.query)}
                >
                  Copy
                </Button> */}
              </Box>
              <Box
                sx={{
                  mt: 2,
                  p: 1.5,
                  bgcolor: 'action.hover',
                  borderRadius: 1,
                  overflowX: 'auto',
                  fontFamily: 'monospace',
                  fontSize: '0.875rem',
                  color: 'text.primary'
                }}
              >
                {q.query}
              </Box>
            </Paper>
          ))}
        </Box>
      </CardContent>
    </Card>
  );
};
