import { use, useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import {
  Box,
  Typography,
  CardContent,
  Grid,
  Button,
  Pagination,
  CircularProgress,
  Divider
} from "@mui/material";

import {
  TrendingUp as TrendingUpIcon,
  Storage as DatabaseIcon,
  FilterList as FilterIcon
} from "@mui/icons-material";

import LeftPanel from "../../components/LeftPanel";
import DiscoverTopBar from "./DiscoverTopBar";
import UserCarousel from "../../components/UserCarousel";
import DiscoverDiagramCard from "./DiscoverDiagramCard";
import HashtagInput from "../../components/HashtagInput";

import { fetchPublicDiagrams, fetchHashtags } from "./fetch.js";
import { useNotification } from "../../components/NotificationContext";


// Mock Data
const mockSchemas = [
  {
    id: "1",
    name: "College Management System",
    description: "Complete database schema for managing college operations including students, courses, and faculty",
    owner: { name: "Ahmed Hassan", username: "@ahmed_dev", avatar: "https://api.dicebear.com/7.x/avataaars/svg?seed=Ahmed" },
    thumbnail: "", // Placeholder will be used
    stars: 245,
    forks: 89,
    views: 1205,
    hashtags: ["college", "education", "management"],
    collaborators: [{ name: "Ahmed Hassan", username: "@ahmed_dev", avatar: "https://api.dicebear.com/7.x/avataaars/svg?seed=Ahmed" },
    { name: "Sarah Ahmed", username: "@sarah_db", avatar: "https://api.dicebear.com/7.x/avataaars/svg?seed=Sarah" },
    { name: "Mohammed Ali", username: "@mo_ali", avatar: "https://api.dicebear.com/7.x/avataaars/svg?seed=Mohammed" },
    { name: "Fatima Khan", username: "@fatima_k", avatar: "https://api.dicebear.com/7.x/avataaars/svg?seed=Fatima" },
    { name: "Mohammed Ali", username: "@mo_ali", avatar: "https://api.dicebear.com/7.x/avataaars/svg?seed=Mohammed" },
    { name: "Fatima Khan", username: "@fatima_k", avatar: "https://api.dicebear.com/7.x/avataaars/svg?seed=Fatima" },
    ],
    createdAt: "2023-01-15",
    lastModified: "2023-02-20"
  }
];

const mockUsers = [
  {
    name: "Ahmed Hassan",
    username: "@ahmed_dev",
    avatar: "https://api.dicebear.com/7.x/avataaars/svg?seed=Ahmed",
    bio: "Database architect & software engineer with 10+ years of experience",
    totalStars: 4500,
    publicSchemas: 12,
  }
];

const ITEMS_PER_PAGE = 12;
const HASHTAGS_PER_PAGE = 10;

export default function Discover() {
  const navigate = useNavigate();
  const { showSuccess, showError } = useNotification();

  const [leftNav, setLeftNav] = useState("all");
  const [showFilters, setShowFilters] = useState(true);
  const [publicDiagrams, setPublicDiagrams] = useState([]);
  const [page, setPage] = useState(1);
  const [loading, setLoading] = useState(false);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [hashtags, setHashtags] = useState([]);
  
  const [selectedHashtags, setSelectedHashtags] = useState([]);

  const loadHashtags = async () => {
    setLoading(true);
    try {
      const resp = await fetchHashtags();
      setHashtags(resp);
    } catch (err) {
      setHashtags([]);
      showError && showError(err?.message || String(err));
    } finally {
      setLoading(false);
    }
  };

  const loadPublicDiagrams = async (pageNumber = page) => {
    setLoading(true);
    try {
      // const resp = await fetchPublicDiagrams(pageNumber - 1, ITEMS_PER_PAGE);
      setPublicDiagrams(resp?.content || []);
      setTotalPages(resp?.totalPages || 0);
      setTotalElements(resp?.totalElements || 0);
    } catch (err) {
      setPublicDiagrams([]);
      setTotalPages(0);
      setTotalElements(0);
      showError && showError(err?.message || String(err));

    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadHashtags();
    loadPublicDiagrams();
  }, []);

  const handlePageChange = (event, value) => {
    setPage(value);
    window.scrollTo({ top: 0, behavior: "smooth" });
  };

  const handleOpenPublicDiagram = (s) => {
    if (s?.id) navigate(`/schema/preview/${s.id}`);
  };

  const onSearchResults = (resp) => {
    setDiagrams(resp?.content || []);
    setTotalPages(resp?.totalPages || 0);
    setTotalElements(resp?.totalElements || 0);

    // setPage(1);
  };

  return (
    <Box sx={{
      display: "flex",
      minHeight: "100vh",
      bgcolor: "background.light",
      width: "100%",
      px: 4,
      pr: 6,
      py: 2,
    }}>
      <LeftPanel leftNav={leftNav} setLeftNav={setLeftNav} />

      <Box sx={{ flexGrow: 1, display: "flex", flexDirection: "column", overflow: "hidden" }}>

        {/* New TopBar with integrated Search logic */}
        <DiscoverTopBar
          onSearchResults={onSearchResults} pageSize={ITEMS_PER_PAGE} page={page} loadPublicDiagrams={loadPublicDiagrams}
        />

        <Box sx={{ flexGrow: 1, overflowY: "auto", pb: 4 }}>

          <Box sx={{ px: 3, py: 4 }}>
            <Box sx={{ mb: 2 }}>
              <UserCarousel users={mockUsers} />
            </Box>

            <Divider sx={{ mb: 2 }} />

            {/* Filters */}
            <Box >
              <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', mb: 2 }}>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                  <TrendingUpIcon color="primary" fontSize="small" />
                  <Typography variant="subtitle1" fontWeight="bold">
                    Filter by Tags
                  </Typography>
                </Box>
                <Button
                  size="small"
                  onClick={() => setShowFilters(!showFilters)}
                  startIcon={!showFilters && <FilterIcon />}
                >
                  {showFilters ? "Hide Filters" : "Show Filters"}
                </Button>
              </Box>

              {showFilters && (
                <CardContent>
                  <HashtagInput
                    hashtags={hashtags}
                    selectedHashtags={selectedHashtags}
                    onSelect={setSelectedHashtags}
                  />
                </CardContent>
              )}
            </Box>

            <Divider sx={{ mb: 2 }} />

            {/* Schemas Grid */}
            <Box sx={{ mb: 2, display: 'flex', alignItems: 'center', gap: 1 }}>
              <DatabaseIcon color="primary" />
              <Typography variant="h6" fontWeight="bold">
                Database Schemas
              </Typography>
              <Typography variant="body2" color="text.secondary">
                ({totalElements})
              </Typography>
            </Box>

            {loading ? (
              <Box sx={{ display: "flex", justifyContent: "center", p: 6 }}>
                <CircularProgress />
              </Box>
            ) : (
              <>
                <Grid container spacing={3}>
                  {publicDiagrams.map((diagram) => (
                    <Grid item xs={12} sm={6} md={4} lg={3} key={diagram.id}>
                      <DiscoverDiagramCard
                        d={diagram}
                        onClick={handleOpenPublicDiagram}
                      />
                    </Grid>
                  ))}

                  {publicDiagrams.length === 0 && (
                    <Grid item xs={12}>
                      <Box sx={{ p: 6, textAlign: "center", bgcolor: "white", borderRadius: 2, boxShadow: 1 }}>
                        <Typography variant="h6">No Schemas found</Typography>
                        <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
                          Try clearing filters or search bar.
                        </Typography>
                      </Box>
                    </Grid>
                  )}
                </Grid>

                {publicDiagrams.length > 0 && totalPages > 1 && (
                  <Box sx={{ display: "flex", justifyContent: "center", mt: 4 }}>
                    <Pagination count={totalPages} page={page} onChange={handlePageChange} color="primary" size="large" />
                  </Box>
                )}
              </>
            )}
          </Box>
        </Box>
      </Box>
    </Box>
  );
}
