import { useState, useEffect } from "react";
import {
  Chip,
  TextField,
  Box,
  Typography,
  Paper,
  InputAdornment,
  Button,
  IconButton,
  Stack,
  Divider,
  CircularProgress
} from "@mui/material";

import {
  ChevronRight as ChevronRightIcon,
  ChevronLeft as ChevronLeftIcon,
  Search as SearchIcon,
  Add as PlusIcon,
} from "@mui/icons-material";

import { useNotification } from "./NotificationContext";

const HASHTAGS_PER_PAGE = 10;


export default function HashtagInput({ hashtags, selectedHashtags, onSelect, allowCreation = false }) {
  const { showError } = useNotification();

  const [hashtagSearch, setHashtagSearch] = useState("");
  const [hashtagPage, setHashtagPage] = useState(1);
  const [loading, setLoading] = useState(false);

  const handleHashtagSelect = (hashtag) => {
    if (!selectedHashtags.includes(hashtag)) {
      onSelect([...selectedHashtags, hashtag]);
    }
  };

  const handleHashtagRemove = (hashtag) => {
    onSelect(selectedHashtags.filter((h) => h !== hashtag));
  };

  // Filter available hashtags
  const filteredHashtags = hashtags.filter(tag =>
    !selectedHashtags.includes(tag) &&
    tag.toLowerCase().includes(hashtagSearch.toLowerCase())
  );

  // Pagination logic
  const totalHashtagPages = Math.ceil(filteredHashtags.length / HASHTAGS_PER_PAGE);
  const currentHashtags = filteredHashtags.slice(
    (hashtagPage - 1) * HASHTAGS_PER_PAGE,
    hashtagPage * HASHTAGS_PER_PAGE
  );

  // Handle input change and reset page
  const handleHashtagSearchChange = (e) => {
    setHashtagSearch(e.target.value);
    setHashtagPage(1);
  };

  const handleAddCustomTag = () => {
    if (hashtagSearch.trim() && !selectedHashtags.includes(hashtagSearch.trim())) {
      onSelect([...selectedHashtags, hashtagSearch.trim()]);
      setHashtagSearch("");
    }
  };

  return (
    <Box sx={{ mb: 4 }}>
      <Paper variant="outlined" sx={{ p: 2, bgcolor: 'background.paper', borderRadius: 2 }}>

        {/* Selected Hashtags Section */}
        {selectedHashtags.length > 0 && (
          <Box sx={{ mb: 2 }}>
            <Typography variant="caption" color="text.secondary" sx={{ mb: 1, display: 'block' }}>
              Selected ({selectedHashtags.length})
            </Typography>
            <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1 }}>
              {selectedHashtags.map(tag => (
                <Chip
                  key={tag}
                  label={`#${tag}`}
                  onDelete={() => handleHashtagRemove(tag)}
                  color="primary"
                  size="small"
                />
              ))}
              <Button
                size="small"
                onClick={() => onSelect([])}
                sx={{ fontSize: '0.75rem', minWidth: 'auto' }}
              >
                Clear all
              </Button>
            </Box>
            <Divider sx={{ my: 2 }} />
          </Box>
        )}

        {/* Available Hashtags Header + Search */}
        <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', mb: 2 }}>
          <Typography variant="caption" color="text.secondary">
            Available Tags
          </Typography>

          {/* Mini Search & Pagination */}
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
            <TextField
              placeholder={allowCreation ? "Filter or add tags..." : "Filter tags..."}
              variant="outlined"
              size="small"
              value={hashtagSearch}
              onChange={handleHashtagSearchChange}
              onKeyDown={(e) => {
                if (allowCreation && e.key === 'Enter') {
                  e.preventDefault();
                  handleAddCustomTag();
                }
              }}
              InputProps={{
                startAdornment: <InputAdornment position="start"><SearchIcon sx={{ fontSize: 16 }} /></InputAdornment>,
                endAdornment: allowCreation && (
                  <InputAdornment position="end">
                    <IconButton
                      size="small"
                      onClick={handleAddCustomTag}
                      disabled={!hashtagSearch.trim() || selectedHashtags.includes(hashtagSearch.trim())}
                      color={hashtagSearch.trim() ? "primary" : "default"}
                    >
                      <PlusIcon sx={{ fontSize: 16 }} />
                    </IconButton>
                  </InputAdornment>
                ),
                sx: { height: 32, fontSize: '0.875rem' }
              }}
              sx={{ width: allowCreation ? 200 : 150 }}
            />

            <Stack direction="row" alignItems="center" spacing={1}>
              <IconButton
                size="small"
                disabled={hashtagPage === 1}
                onClick={() => setHashtagPage(prev => Math.max(1, prev - 1))}
              >
                <ChevronLeftIcon fontSize="small" />
              </IconButton>
              <Typography variant="caption" color="text.secondary">
                {totalHashtagPages > 0 ? `${hashtagPage} / ${totalHashtagPages}` : "0 / 0"}
              </Typography>
              <IconButton
                size="small"
                disabled={hashtagPage >= totalHashtagPages}
                onClick={() => setHashtagPage(prev => Math.min(totalHashtagPages, prev + 1))}
              >
                <ChevronRightIcon fontSize="small" />
              </IconButton>
            </Stack>
          </Box>
        </Box>

        {/* Available Hashtags List */}
        <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1, minHeight: 40 }}>
          {loading ? (
            <CircularProgress size={20} />
          ) : filteredHashtags.length > 0 ? (
            currentHashtags.map((tag) => (
              <Chip
                key={tag}
                label={`#${tag}`}
                onClick={() => handleHashtagSelect(tag)}
                variant="outlined"
                size="small"
                clickable
                sx={{
                  bgcolor: 'background.default',
                  transition: 'all 0.2s',
                  '&:hover': { bgcolor: 'action.hover', borderColor: 'primary.main', color: 'primary.main' }
                }}
              />
            ))
          ) : (
            <Typography variant="body2" color="text.secondary" sx={{ fontStyle: 'italic', width: '100%', textAlign: 'center', py: 1 }}>
              No tags found
            </Typography>
          )}
        </Box>

      </Paper>
    </Box>
  );
};
