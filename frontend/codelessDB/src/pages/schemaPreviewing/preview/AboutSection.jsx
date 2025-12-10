import { Card, CardContent, CardHeader, Typography, Chip, Box, Divider, Stack } from '@mui/material';
import {
  Tag as HashIcon,
  Star as StarIcon,
  Visibility as ViewIcon,
  CallSplit as ForkIcon,
  CalendarToday as CalendarIcon,
  EditCalendar as EditCalendarIcon
} from '@mui/icons-material';

export const AboutSection = ({ shortDescription, hashtags, stats, dates }) => {
  const formatDate = (dateString) => {
    if (!dateString) return 'N/A';
    return new Date(dateString).toLocaleDateString(undefined, {
      year: 'numeric',
      month: 'long',
      day: 'numeric'
    });
  };

  return (
    <Card variant="outlined">
      <CardHeader
        title={<Typography variant="subtitle2" fontWeight="bold">About</Typography>}
        sx={{ pb: 1 }}
      />
      <CardContent sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
        {/* Description */}
        <Typography variant="body2" color="text.secondary" sx={{ lineHeight: 1.6 }}>
          {shortDescription}
        </Typography>

        {/* Stats */}
        {stats && (
          <Box sx={{ display: 'flex', gap: 2, color: 'text.secondary' }}>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
              <StarIcon sx={{ fontSize: 16, color: 'warning.main' }} />
              <Typography variant="caption" fontWeight="bold">{stats.stars}</Typography>
              <Typography variant="caption">stars</Typography>
            </Box>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
              <ViewIcon sx={{ fontSize: 16 }} />
              <Typography variant="caption" fontWeight="bold">{stats.views}</Typography>
              <Typography variant="caption">views</Typography>
            </Box>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
              <ForkIcon sx={{ fontSize: 16 }} />
              <Typography variant="caption" fontWeight="bold">{stats.forks}</Typography>
              <Typography variant="caption">forks</Typography>
            </Box>
          </Box>
        )}

        <Divider />

        {/* Dates */}
        {dates && (
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1 }}>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
              <CalendarIcon sx={{ fontSize: 16, color: 'text.secondary' }} />
              <Typography variant="caption" color="text.secondary">Created:</Typography>
              <Typography variant="caption" fontWeight="medium">{formatDate(dates.createdAt)}</Typography>
            </Box>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
              <EditCalendarIcon sx={{ fontSize: 16, color: 'text.secondary' }} />
              <Typography variant="caption" color="text.secondary">Updated:</Typography>
              <Typography variant="caption" fontWeight="medium">{formatDate(dates.lastModified)}</Typography>
            </Box>
          </Box>
        )}

        {/* Hashtags */}
        {hashtags && hashtags.length > 0 && (
          <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1 }}>
            {hashtags.map((tag) => (
              <Chip
                key={tag}
                icon={<HashIcon style={{ fontSize: 12 }} />}
                label={tag}
                size="small"
                variant="outlined"
                sx={{
                  fontSize: '0.75rem',
                  height: 24,
                  '&:hover': { bgcolor: 'primary.main', color: 'white', borderColor: 'primary.main' }
                }}
                clickable
              />
            ))}
          </Box>
        )}
      </CardContent>
    </Card>
  );
};
