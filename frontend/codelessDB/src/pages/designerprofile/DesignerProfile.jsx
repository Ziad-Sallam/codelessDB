import { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";
import {
  Box,
  Container,
  Card,
  CardContent,
  Avatar,
  Typography,
  Tabs,
  Tab,
  Chip,
  Stack,
  Divider,
  CircularProgress
} from "@mui/material";
import {
  Star as StarIcon,
  CallSplit as ForkIcon,
  LocationOn as MapPinIcon,
  Link as LinkIcon,
  CalendarToday as CalendarIcon,
  Storage as DatabaseIcon,
  Description as BookOpenIcon,
  EmojiEvents as TrophyIcon
} from "@mui/icons-material";

import LeftPanel from "../../components/LeftPanel";
import SimpleTopBar from "../../components/SimpleTopBar";
import { fetchDesignerProfile, fetchPublicDiagrams, fetchStarredDiagrams } from "./fetch";
import { useNotification } from "../../components/NotificationContext";

export default function DesignerProfile() {
  const navigate = useNavigate();
  const { username } = useParams();
  const [leftNav, setLeftNav] = useState("public");
  const [activeTab, setActiveTab] = useState(0);

  const { showError } = useNotification();

  const [isLoadingProfile, setIsLoadingProfile] = useState(true);
  const [profile, setProfile] = useState(null);

  const [isLoadingDiagrams, setIsLoadingDiagrams] = useState(false);
  const [diagrams, setDiagrams] = useState([]);
  const [diagramsPage, setDiagramsPage] = useState(0);
  const [diagramsTotal, setDiagramsTotal] = useState(0);

  // Load Profile
  useEffect(() => {
    if (!username) return;
    setIsLoadingProfile(true);
    fetchDesignerProfile(username)
      .then((data) => {
        setProfile(data);
        console.log(data);
      })
      .catch((err) => {
        console.error(err);
        showError("Failed to load designer profile");
      })
      .finally(() => {
        setIsLoadingProfile(false);
      });
  }, [username]);

  // Load Diagrams (Public or Starred based on tab)
  useEffect(() => {
    if (!username) return;
    setIsLoadingDiagrams(true);
    const fetchFn = activeTab === 0 ? fetchPublicDiagrams : fetchStarredDiagrams;

    // For now request page 0, size 50 to get a good list. Pagination can be added later if needed.
    fetchFn(username, 0, 50)
      .then((data) => {
        setDiagrams(data.content || []);
        setDiagramsTotal(data.totalElements || 0);
      })
      .catch((err) => {
        console.error(err);
        showError("Failed to load diagrams");
      })
      .finally(() => {
        setIsLoadingDiagrams(false);
      });
  }, [username, activeTab]);

  const handleTabChange = (event, newValue) => {
    setActiveTab(newValue);
    setDiagrams([]);
  };

  const handleSchemaClick = (diagramId) => {
    navigate(`/schema/preview/${diagramId}`);
  };

  const formatDate = (dateString) => {
    if (!dateString) return "";
    return new Date(dateString).toLocaleDateString("en-US", {
      year: 'numeric', month: 'long', day: 'numeric'
    });
  };

  if (isLoadingProfile) {
    return (
      <Box sx={{ display: "flex", minHeight: "100vh", bgcolor: "background.default", justifyContent: "center", alignItems: "center" }}>
        <CircularProgress />
      </Box>
    );
  }

  if (!profile) {
    return (
      <Box sx={{ display: "flex", minHeight: "100vh", bgcolor: "background.default" }}>
        <LeftPanel leftNav={leftNav} setLeftNav={setLeftNav} />
        <Box sx={{ flexGrow: 1, p: 4, textAlign: 'center' }}>
          <Typography variant="h5">User not found</Typography>
        </Box>
      </Box>
    );
  }

  return (
    <Box sx={{ display: "flex", minHeight: "100vh", bgcolor: "background.default" }}>
      <LeftPanel leftNav={leftNav} setLeftNav={setLeftNav} />

      <Box sx={{ flexGrow: 1, display: "flex", flexDirection: "column", overflow: "hidden" }}>
        <SimpleTopBar title="Designer Profile" />

        <Box sx={{ flexGrow: 1, overflowY: "auto", p: 4 }}>
          <Container maxWidth="xl">
            <Box sx={{ display: 'flex', flexDirection: { xs: 'column', lg: 'row' }, gap: 4 }}>

              {/* Left Column - Profile Info */}
              <Box sx={{ width: { lg: 320 }, flexShrink: 0 }}>
                <Box sx={{ position: 'sticky', top: 24 }}>
                  <Card variant="outlined" sx={{ textAlign: 'center', p: 3 }}>
                    <Avatar
                      src={profile.picture}
                      alt={profile.name}
                      sx={{ width: 128, height: 128, mx: 'auto', mb: 2, border: 4, borderColor: 'background.paper', boxShadow: 2 }}
                    />
                    <Typography variant="h5" fontWeight="bold" gutterBottom sx={{ wordWrap: 'break-word', overflowWrap: 'break-word' }}>
                      {profile.name || profile.username}
                    </Typography>
                    <Typography variant="body1" color="text.secondary" gutterBottom sx={{ wordWrap: 'break-word', overflowWrap: 'break-word' }}>
                      @{profile.username}
                    </Typography>

                    {profile.bio && (
                      <Typography variant="body2" sx={{ mt: 2, mb: 3, wordWrap: 'break-word', overflowWrap: 'break-word', whiteSpace: 'pre-wrap' }}>
                        {profile.bio}
                      </Typography>
                    )}

                    <Divider sx={{ my: 2 }} />

                    <Stack spacing={1.5} alignItems="center" sx={{ color: 'text.secondary', fontSize: '0.875rem' }}>
                      {/* Location is not in current DTO, skipping */}

                      {profile.url && (
                        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, width: '100%' }}>
                          <LinkIcon fontSize="small" sx={{ flexShrink: 0 }} />
                          <a
                            href={profile.url.startsWith("http") ? profile.url : `https://${profile.url}`}
                            target="_blank"
                            rel="noopener noreferrer"
                            style={{ color: 'inherit', textDecoration: 'none', wordWrap: 'break-word', overflowWrap: 'break-word', flex: 1, minWidth: 0 }}
                          >
                            {profile.url}
                          </a>
                        </Box>
                      )}

                      {profile.email && (
                        <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 1, width: '100%', wordWrap: 'break-word', overflowWrap: 'break-word' }}>
                          {profile.email}
                        </Box>
                      )}

                      {profile.createdAt && (
                        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                          <CalendarIcon fontSize="small" />
                          Joined {formatDate(profile.createdAt)}
                        </Box>
                      )}
                    </Stack>

                    <Box sx={{ mt: 3, display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 2 }}>
                      <Box sx={{ p: 1.5, bgcolor: 'action.hover', borderRadius: 2 }}>
                        <TrophyIcon sx={{ color: 'warning.main', mb: 0.5 }} />
                        <Typography variant="h6" fontWeight="bold">{profile.totalStars || 0}</Typography>
                        <Typography variant="caption" color="text.secondary">Total Stars</Typography>
                      </Box>
                      <Box sx={{ p: 1.5, bgcolor: 'action.hover', borderRadius: 2 }}>
                        <DatabaseIcon sx={{ color: 'primary.main', mb: 0.5 }} />
                        <Typography variant="h6" fontWeight="bold">{profile.publicCount || 0}</Typography>
                        <Typography variant="caption" color="text.secondary">Schemas</Typography>
                      </Box>
                    </Box>
                  </Card>
                </Box>
              </Box>

              {/* Right Column - Content */}
              <Box sx={{ flex: 1, minWidth: 0 }}>
                <Box sx={{ borderBottom: 1, borderColor: 'divider', mb: 3 }}>
                  <Tabs value={activeTab} onChange={handleTabChange}>
                    <Tab icon={<BookOpenIcon fontSize="small" />} iconPosition="start" label={`Schemas (${activeTab === 0 ? (diagramsTotal || 0) : (profile.publicCount || 0)})`} />
                    <Tab icon={<StarIcon fontSize="small" />} iconPosition="start" label="Starred" />
                  </Tabs>
                </Box>

                {isLoadingDiagrams ? (
                  <Box sx={{ display: 'flex', justifyContent: 'center', py: 4 }}>
                    <CircularProgress />
                  </Box>
                ) : (
                  <Stack spacing={3}>
                    {diagrams.map((diagram) => (
                      <Card
                        key={diagram.diagramId || diagram.id}
                        variant="outlined"
                        sx={{
                          cursor: 'pointer',
                          transition: 'all 0.2s',
                          '&:hover': { borderColor: 'primary.main', boxShadow: 2 }
                        }}
                        onClick={() => handleSchemaClick(diagram.diagramId || diagram.id)}
                      >
                        <CardContent sx={{ display: 'flex', gap: 3 }}>
                          {/* Thumbnail */}
                          <Box
                            sx={{
                              width: 160,
                              height: 100,
                              bgcolor: 'action.hover',
                              borderRadius: 1,
                              flexShrink: 0,
                              display: { xs: 'none', sm: 'flex' },
                              alignItems: 'center',
                              justifyContent: 'center',
                              overflow: 'hidden'
                            }}
                          >
                            {diagram.thumbnail ? (
                              <Box component="img" src={diagram.thumbnail} sx={{ width: '100%', height: '100%', objectFit: 'cover' }} />
                            ) : (
                              <DatabaseIcon sx={{ fontSize: 32, color: 'text.secondary', opacity: 0.5 }} />
                            )}
                          </Box>

                          <Box sx={{ flex: 1, minWidth: 0 }}>
                            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', mb: 1 }}>
                              <Typography variant="h6" fontWeight="bold" color="primary.main" noWrap>
                                {diagram.name}
                              </Typography>
                            </Box>

                            <Typography variant="body2" color="text.secondary" sx={{ mb: 2, display: '-webkit-box', WebkitLineClamp: 2, WebkitBoxOrient: 'vertical', overflow: 'hidden' }}>
                              {diagram.shortDescription || "No description provided."}
                            </Typography>

                            <Box sx={{ display: 'flex', alignItems: 'center', flexWrap: 'wrap', gap: 2 }}>
                              <Box sx={{ display: 'flex', gap: 1 }}>
                                {diagram.hashtags && diagram.hashtags.slice(0, 3).map((tag) => (
                                  <Chip key={tag} label={tag} size="small" sx={{ height: 20, fontSize: '0.75rem' }} />
                                ))}
                              </Box>

                              <Box sx={{ flexGrow: 1 }} />

                              <Box sx={{ display: 'flex', gap: 2, color: 'text.secondary', fontSize: '0.75rem' }}>
                                <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
                                  <StarIcon sx={{ fontSize: 14 }} /> {diagram.stars || 0}
                                </Box>
                                <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
                                  <ForkIcon sx={{ fontSize: 14 }} /> {diagram.forks || 0}
                                </Box>
                                <Box>
                                  Updated {formatDate(diagram.lastModified)}
                                </Box>
                              </Box>
                            </Box>
                          </Box>
                        </CardContent>
                      </Card>
                    ))}

                    {diagrams.length === 0 && (
                      <Box sx={{ textAlign: 'center', py: 8, color: 'text.secondary', border: '1px dashed', borderColor: 'divider', borderRadius: 2 }}>
                        {activeTab === 0 ? (
                          <>
                            <DatabaseIcon sx={{ fontSize: 48, opacity: 0.5, mb: 2 }} />
                            <Typography>No public schemas yet</Typography>
                          </>
                        ) : (
                          <>
                            <StarIcon sx={{ fontSize: 48, opacity: 0.5, mb: 2 }} />
                            <Typography>No starred schemas yet</Typography>
                          </>
                        )}
                      </Box>
                    )}
                  </Stack>
                )}
              </Box>
            </Box>
          </Container>
        </Box>
      </Box>
    </Box>
  );
}
