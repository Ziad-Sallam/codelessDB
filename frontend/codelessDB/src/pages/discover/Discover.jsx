import {
  Box,
  Button,
  CardContent,
  CircularProgress,
  Divider,
  Grid,
  Pagination,
  Typography
} from "@mui/material";
import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";

import {
  Storage as DatabaseIcon,
  TrendingUp as TrendingUpIcon
} from "@mui/icons-material";

import HashtagInput from "../../components/HashtagInput";
import LeftPanel from "../../components/LeftPanel";
import UserCarousel from "../../components/UserCarousel";
import DiscoverDiagramCard from "./DiscoverDiagramCard";
import DiscoverTopBar from "./DiscoverTopBar";

import { useNotification } from "../../components/NotificationContext";
import { validateToken } from "../auth/fetch";
import { fetchHashtags, fetchPublicDiagrams, fetchPublicUsers, followUser, unfollowUser } from "./fetch.js";

const ITEMS_PER_PAGE = 12;
const HASHTAGS_PER_PAGE = 10;

export default function Discover() {
  const navigate = useNavigate();
  const { showSuccess, showError } = useNotification();

  const [leftNav, setLeftNav] = useState("all");
  const [searchQuery, setSearchQuery] = useState("");
  const [publicDiagrams, setPublicDiagrams] = useState([]);
  const [page, setPage] = useState(1);
  const [loading, setLoading] = useState(false);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [hashtags, setHashtags] = useState([]);
  const [featuredUsers, setFeaturedUsers] = useState([]);
  const [currentUser, setCurrentUser] = useState(null);

  const [selectedHashtags, setSelectedHashtags] = useState([]);

  const loadHashtags = async () => {
    try {
      const resp = await fetchHashtags();
      console.log(resp);
      setHashtags(resp);
    } catch (err) {
      setHashtags([]);
      showError && showError(err?.message || String(err));
    }
  };

  const loadCurrentUser = async () => {
      try {
          const user = await validateToken();
          setCurrentUser(user);
      } catch (err) {
          console.error("Failed to load current user", err);
      }
  };

  const fetchData = async () => {
    setLoading(true);
    try {
      const [diagramsResp, usersResp] = await Promise.all([
        fetchPublicDiagrams(page - 1, ITEMS_PER_PAGE, searchQuery, selectedHashtags),
        fetchPublicUsers(0, ITEMS_PER_PAGE, searchQuery, selectedHashtags)
      ]);

      setPublicDiagrams(diagramsResp.content || []);
      setTotalPages(diagramsResp?.totalPages || 0);
      setTotalElements(diagramsResp?.totalElements || 0);

      console.log(diagramsResp);
      setFeaturedUsers(usersResp.content || []);
    } catch (err) {
      console.error(err);
      setPublicDiagrams([]);
      setFeaturedUsers([]);
      // showError && showError(err?.message || String(err));
    } finally {
      setLoading(false);
    }
  };

  const handleFollowToggle = async (user) => {
    try {
      if (user.isFollowed) {
        await unfollowUser(user.username);
        showSuccess(`Unfollowed ${user.name}`);
      } else {
        await followUser(user.username);
        showSuccess(`Followed ${user.name}`);
      }
      
      setFeaturedUsers((prev) =>
        prev.map((u) => {
          if (u.id === user.id) {
            return {
              ...u,
              isFollowed: !u.isFollowed,
              followersCount: user.isFollowed
                ? Math.max(0, (u.followersCount || 0) - 1)
                : (u.followersCount || 0) + 1,
            };
          }
          if (currentUser && (u.username === currentUser.username || u.id === currentUser.id)) {
            return {
              ...u,
              followingCount: user.isFollowed
                ? Math.max(0, (u.followingCount || 0) - 1)
                : (u.followingCount || 0) + 1,
            };
          }
          return u;
        })
      );

      if (currentUser) {
        setCurrentUser((prev) => ({
          ...prev,
          followingCount: user.isFollowed
            ? Math.max(0, (prev.followingCount || 0) - 1)
            : (prev.followingCount || 0) + 1,
        }));
      }
    } catch (err) {
      console.error(err);
      showError(err.message || "Action failed");
    }
  };

  useEffect(() => {
    loadHashtags();
    loadCurrentUser();
  }, []);

  useEffect(() => {
    fetchData();
  }, [page, searchQuery, selectedHashtags]);

  const handlePageChange = (event, value) => {
    setPage(value);
    window.scrollTo({ top: 0, behavior: "smooth" });
  };

  const handleSearch = (query) => {
    setSearchQuery(query);
    setPage(1);
  };

  const handleOpenPublicDiagram = (s) => {
    navigate(`/schema/preview/${s.diagramId}`);
  };

  const handlePublishDiagram = () => {
    navigate(`/schema/create`);
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
          onSearch={handleSearch}
          pageSize={ITEMS_PER_PAGE}
        />

        <Box sx={{ flexGrow: 1, overflowY: "auto", pb: 4 }}>

          {/* Filters */}
          <Box sx={{ px: 3, pt: 2 }}>
            <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', mb: 2 }}>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                <TrendingUpIcon color="primary" fontSize="small" />
                <Typography variant="subtitle1" fontWeight="bold">
                  Filter by Tags
                </Typography>
              </Box>
              <Button variant="contained" sx={{ marginLeft: "auto", mr: 2 }} onClick={handlePublishDiagram}>
                Publish Diagram
              </Button>
            </Box>

            <CardContent>
              <HashtagInput
                hashtags={hashtags}
                selectedHashtags={selectedHashtags}
                onSelect={setSelectedHashtags}
              />
            </CardContent>
          </Box>

          <Divider sx={{ mb: 2 }} />

          <Box sx={{ px: 3 }}>
            <Box sx={{ mb: 2 }}>
              <UserCarousel users={featuredUsers} onFollowToggle={handleFollowToggle} currentUser={currentUser} />
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
                    <Grid item xs={12} sm={6} md={4} lg={3} key={diagram.diagramId}>
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
