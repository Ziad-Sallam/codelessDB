import { Typography, Box } from '@mui/material';

export const MarkdownRenderer = ({ content }) => {
  if (!content) return null;

  // Simple parser for basic markdown
  const lines = content.split('\n');

  return (
    <Box sx={{ '& > * + *': { mt: 2 } }}>
      {lines.map((line, index) => {
        if (line.startsWith('# ')) {
          return <Typography key={index} variant="h4" gutterBottom fontWeight="bold">{line.replace('# ', '')}</Typography>;
        }
        if (line.startsWith('## ')) {
          return <Typography key={index} variant="h5" gutterBottom fontWeight="bold" sx={{ mt: 3 }}>{line.replace('## ', '')}</Typography>;
        }
        if (line.startsWith('### ')) {
          return <Typography key={index} variant="h6" gutterBottom fontWeight="bold" sx={{ mt: 2 }}>{line.replace('### ', '')}</Typography>;
        }
        if (line.startsWith('- ')) {
          return (
            <Box key={index} sx={{ display: 'flex', gap: 1, ml: 2 }}>
              <Typography variant="body1">•</Typography>
              <Typography variant="body1">{line.replace('- ', '')}</Typography>
            </Box>
          );
        }
        if (line.trim() === '') {
          return null;
        }
        return <Typography key={index} variant="body1" paragraph>{line}</Typography>;
      })}
    </Box>
  );
};
