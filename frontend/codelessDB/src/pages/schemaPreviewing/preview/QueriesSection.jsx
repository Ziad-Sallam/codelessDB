import { Card, CardContent, CardHeader, Typography, Box, Paper } from '@mui/material';

export const QueriesSection = ({ queries }) => {

  return (
    <Card variant="outlined">
      <CardHeader
        title={
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
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
