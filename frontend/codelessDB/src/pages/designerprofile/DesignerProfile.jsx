import {
  Description as BookOpenIcon,
  CalendarToday as CalendarIcon,
  Storage as DatabaseIcon,
  CallSplit as ForkIcon,
  Link as LinkIcon,
  Star as StarIcon,
  EmojiEvents as TrophyIcon,
  PersonAdd as UserPlusIcon,
  Group as UsersIcon,
} from "@mui/icons-material";
import {
  Avatar,
  Box,
  Button,
  Card,
  CardContent,
  Chip,
  CircularProgress,
  Container,
  Divider,
  Stack,
  Tab,
  Tabs,
  Typography,
} from "@mui/material";
import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";

import LeftPanel from "../../components/LeftPanel";
import { useNotification } from "../../components/NotificationContext";
import SimpleTopBar from "../../components/SimpleTopBar";
import { validateToken } from "../auth/fetch";
import {
  fetchDesignerProfile,
  fetchPublicDiagrams,
  fetchStarredDiagrams,
  followUser,
  unfollowUser,
} from "./fetch";


export default function DesignerProfile() {
  const navigate = useNavigate();
  const { username } = useParams();
  const [leftNav, setLeftNav] = useState("public");
  const [activeTab, setActiveTab] = useState(0);
  const [currentUser, setCurrentUser] = useState(null);

  const { showError, showSuccess } = useNotification();

  const [isLoadingProfile, setIsLoadingProfile] = useState(true);
  const [profile, setProfile] = useState(null);

  const [isLoadingDiagrams, setIsLoadingDiagrams] = useState(false);
  const [diagrams, setDiagrams] = useState([]);
  const [diagramsPage, setDiagramsPage] = useState(0);
  const [diagramsTotal, setDiagramsTotal] = useState(0);

  useEffect(() => {
    if (!username) return;
    setIsLoadingProfile(true);

    Promise.all([
      fetchDesignerProfile(username),
      validateToken().catch(() => null)
    ])
      .then(([profileData, userData]) => {
        setProfile(profileData);
        setCurrentUser(userData);
      })
      .catch((err) => {
        console.error(err);
        showError("Failed to load designer profile");
      })
      .finally(() => {
        setIsLoadingProfile(false);
      });
  }, [username]);

  useEffect(() => {
    if (!username) return;
    setIsLoadingDiagrams(true);
    const fetchFn =
      activeTab === 0 ? fetchPublicDiagrams : fetchStarredDiagrams;

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

  const handleFollowToggle = async () => {
    if (!currentUser) {
      showError("Please log in to follow creators");
      return;
    }

    try {
      if (profile.isFollowed) {
        await unfollowUser(profile.username);
        showSuccess(`Unfollowed ${profile.name || profile.username}`);
      } else {
        await followUser(profile.username);
        showSuccess(`Following ${profile.name || profile.username}`);
      }

      setProfile((prev) => ({
        ...prev,
        isFollowed: !prev.isFollowed,
        followersCount: prev.isFollowed
          ? Math.max(0, (prev.followersCount || 0) - 1)
          : (prev.followersCount || 0) + 1,
      }));
    } catch (err) {
      showError(err.message || "Action failed");
    }
  };

  const formatDate = (dateString) => {
    if (!dateString) return "";
    return new Date(dateString).toLocaleDateString("en-US", {
      year: "numeric",
      month: "long",
      day: "numeric",
    });
  };

  return (
    <Box
      sx={{
        display: "flex",
        minHeight: "100vh",
        bgcolor: "background.default",
      }}
    >
      <LeftPanel leftNav={leftNav} setLeftNav={setLeftNav} />

      {isLoadingProfile ? (
        <Box
          sx={{
            flexGrow: 1,
            display: "flex",
            justifyContent: "center",
            alignItems: "center",
          }}
        >
          <CircularProgress />
        </Box>
      ) : !profile ? (
        <Box sx={{ flexGrow: 1, p: 4, textAlign: "center" }}>
          <Typography variant="h5">User not found</Typography>
        </Box>
      ) : (
        <Box
          sx={{
            flexGrow: 1,
            display: "flex",
            flexDirection: "column",
            overflow: "hidden",
          }}
        >
          <SimpleTopBar title="Designer Profile" />

          <Box sx={{ flexGrow: 1, overflowY: "auto", p: 4 }}>
            <Container maxWidth="xl">
              <Box
                sx={{
                  display: "flex",
                  flexDirection: { xs: "column", lg: "row" },
                  gap: 4,
                }}
              >
                <Box sx={{ width: { lg: 320 }, flexShrink: 0 }}>
                  <Box sx={{ position: "sticky", top: 24 }}>
                    <Card variant="outlined" sx={{ textAlign: "center", p: 3 }}>
                      <Avatar
                        src={profile.picture}
                        alt={profile.name}
                        sx={{
                          width: 128,
                          height: 128,
                          mx: "auto",
                          mb: 2,
                          border: 4,
                          borderColor: "background.paper",
                          boxShadow: 2,
                        }}
                      />
                      <Typography variant="h5" fontWeight="bold" gutterBottom>
                        {profile.name || profile.username}
                      </Typography>
                      <Typography
                        variant="body1"
                        color="text.secondary"
                        gutterBottom
                      >
                        @{profile.username}
                      </Typography>

                      {profile.bio && (
                        <Typography variant="body2" sx={{ mt: 2, mb: 3 }}>
                          {profile.bio}
                        </Typography>
                      )}

                      <Divider sx={{ my: 2 }} />

                      <Stack
                        spacing={1.5}
                        alignItems="center"
                        sx={{ color: "text.secondary", fontSize: "0.875rem" }}
                      >
                        {profile.url && (
                          <Box
                            sx={{
                              display: "flex",
                              alignItems: "center",
                              gap: 1,
                            }}
                          >
                            <LinkIcon fontSize="small" />
                            <a
                              href={
                                profile.url.startsWith("http")
                                  ? profile.url
                                  : `https://${profile.url}`
                              }
                              target="_blank"
                              rel="noopener noreferrer"
                              style={{
                                color: "inherit",
                                textDecoration: "none",
                              }}
                            >
                              {profile.url}
                            </a>
                          </Box>
                        )}

                        {profile.email && (
                          <Box
                            sx={{
                              display: "flex",
                              alignItems: "center",
                              gap: 1,
                            }}
                          >
                            {profile.email}
                          </Box>
                        )}

                        {profile.createdAt && (
                          <Box
                            sx={{
                              display: "flex",
                              alignItems: "center",
                              gap: 1,
                            }}
                          >
                            <CalendarIcon fontSize="small" />
                            Joined {formatDate(profile.createdAt)}
                          </Box>
                        )}
                      </Stack>

                      <Box
                        sx={{
                          mt: 3,
                          display: "grid",
                          gridTemplateColumns: "1fr 1fr",
                          gap: 2,
                        }}
                      >
                        <Box
                          sx={{
                            p: 1.5,
                            bgcolor: "action.hover",
                            borderRadius: 2,
                          }}
                        >
                          <TrophyIcon sx={{ color: "warning.main", mb: 0.5 }} />
                          <Typography variant="h6" fontWeight="bold">
                            {profile.totalStars || 0}
                          </Typography>
                          <Typography variant="caption" color="text.secondary">
                            Total Stars
                          </Typography>
                        </Box>
                        <Box
                          sx={{
                            p: 1.5,
                            bgcolor: "action.hover",
                            borderRadius: 2,
                          }}
                        >
                          <DatabaseIcon
                            sx={{ color: "primary.main", mb: 0.5 }}
                          />
                          <Typography variant="h6" fontWeight="bold">
                            {profile.publicCount || 0}
                          </Typography>
                          <Typography variant="caption" color="text.secondary">
                            Schemas
                          </Typography>
                        </Box>
                        <Box
                          sx={{
                            p: 1.5,
                            bgcolor: "action.hover",
                            borderRadius: 2,
                          }}
                        >
                          <UsersIcon sx={{ color: "info.main", mb: 0.5 }} />
                          <Typography variant="h6" fontWeight="bold">
                            {profile.followersCount || 0}
                          </Typography>
                          <Typography variant="caption" color="text.secondary">
                            Followers
                          </Typography>
                        </Box>
                        <Box
                          sx={{
                            p: 1.5,
                            bgcolor: "action.hover",
                            borderRadius: 2,
                          }}
                        >
                          <UsersIcon sx={{ color: "secondary.main", mb: 0.5 }} />
                          <Typography variant="h6" fontWeight="bold">
                            {profile.followingCount || 0}
                          </Typography>
                          <Typography variant="caption" color="text.secondary">
                            Following
                          </Typography>
                        </Box>
                      </Box>

                      <Button
                        fullWidth
                        variant={profile.isFollowed ? "contained" : "outlined"}
                        color={profile.isFollowed ? "secondary" : "primary"}
                        onClick={handleFollowToggle}
                        disabled={currentUser?.username === profile.username}
                        sx={{ mt: 3, borderRadius: 2, py: 1 }}
                        startIcon={profile.isFollowed ? <UsersIcon /> : <UserPlusIcon />}
                      >
                        {currentUser?.username === profile.username
                          ? "This is You"
                          : profile.isFollowed
                          ? "Unfollow"
                          : "Follow"}
                      </Button>
                    </Card>
                  </Box>
                </Box>

                <Box sx={{ flex: 1, minWidth: 0 }}>
                  <Box sx={{ borderBottom: 1, borderColor: "divider", mb: 3 }}>
                    <Tabs value={activeTab} onChange={handleTabChange}>
                      <Tab
                        icon={<BookOpenIcon fontSize="small" />}
                        iconPosition="start"
                        label={`Schemas (${
                          activeTab === 0
                            ? diagramsTotal || 0
                            : profile.publicCount || 0
                        })`}
                      />
                      <Tab
                        icon={<StarIcon fontSize="small" />}
                        iconPosition="start"
                        label="Starred"
                      />
                    </Tabs>
                  </Box>

                  {isLoadingDiagrams ? (
                    <Box
                      sx={{ display: "flex", justifyContent: "center", py: 4 }}
                    >
                      <CircularProgress />
                    </Box>
                  ) : (
                    <Stack spacing={3}>
                      {diagrams.map((diagram) => (
                        <Card
                          key={diagram.diagramId || diagram.id}
                          variant="outlined"
                          sx={{
                            cursor: "pointer",
                            transition: "all 0.2s",
                            "&:hover": {
                              borderColor: "primary.main",
                              boxShadow: 2,
                            },
                          }}
                          onClick={() =>
                            handleSchemaClick(diagram.diagramId || diagram.id)
                          }
                        >
                          <CardContent sx={{ display: "flex", gap: 3 }}>
                            <Box
                              sx={{
                                width: 160,
                                height: 100,
                                bgcolor: "action.hover",
                                borderRadius: 1,
                                flexShrink: 0,
                                display: { xs: "none", sm: "flex" },
                                alignItems: "center",
                                justifyContent: "center",
                                overflow: "hidden",
                              }}
                            >
                              {diagram.thumbnail ? (
                                <Box
                                  component="img"
                                  src={diagram.thumbnail}
                                  sx={{
                                    width: "100%",
                                    height: "100%",
                                    objectFit: "cover",
                                  }}
                                />
                              ) : (
                                <DatabaseIcon
                                  sx={{
                                    fontSize: 32,
                                    color: "text.secondary",
                                    opacity: 0.5,
                                  }}
                                />
                              )}
                            </Box>

                            <Box sx={{ flex: 1, minWidth: 0 }}>
                              <Box
                                sx={{
                                  display: "flex",
                                  justifyContent: "space-between",
                                  alignItems: "flex-start",
                                  mb: 1,
                                }}
                              >
                                <Typography
                                  variant="h6"
                                  fontWeight="bold"
                                  color="primary.main"
                                  noWrap
                                >
                                  {diagram.name}
                                </Typography>
                              </Box>

                              <Typography
                                variant="body2"
                                color="text.secondary"
                                sx={{
                                  mb: 2,
                                  display: "-webkit-box",
                                  WebkitLineClamp: 2,
                                  WebkitBoxOrient: "vertical",
                                  overflow: "hidden",
                                }}
                              >
                                {diagram.shortDescription ||
                                  "No description provided."}
                              </Typography>

                              <Box
                                sx={{
                                  display: "flex",
                                  alignItems: "center",
                                  flexWrap: "wrap",
                                  gap: 2,
                                }}
                              >
                                <Box sx={{ display: "flex", gap: 1 }}>
                                  {diagram.hashtags &&
                                    diagram.hashtags
                                      .slice(0, 3)
                                      .map((tag) => (
                                        <Chip
                                          key={tag}
                                          label={tag}
                                          size="small"
                                          sx={{
                                            height: 20,
                                            fontSize: "0.75rem",
                                          }}
                                        />
                                      ))}
                                </Box>

                                <Box sx={{ flexGrow: 1 }} />

                                <Box
                                  sx={{
                                    display: "flex",
                                    gap: 2,
                                    color: "text.secondary",
                                    fontSize: "0.75rem",
                                  }}
                                >
                                  <Box
                                    sx={{
                                      display: "flex",
                                      alignItems: "center",
                                      gap: 0.5,
                                    }}
                                  >
                                    <StarIcon sx={{ fontSize: 14 }} />{" "}
                                    {diagram.stars || 0}
                                  </Box>
                                  <Box
                                    sx={{
                                      display: "flex",
                                      alignItems: "center",
                                      gap: 0.5,
                                    }}
                                  >
                                    <ForkIcon sx={{ fontSize: 14 }} />{" "}
                                    {diagram.forks || 0}
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
                        <Box
                          sx={{
                            textAlign: "center",
                            py: 8,
                            color: "text.secondary",
                            border: "1px dashed",
                            borderColor: "divider",
                            borderRadius: 2,
                          }}
                        >
                          {activeTab === 0 ? (
                            <>
                              <DatabaseIcon
                                sx={{ fontSize: 48, opacity: 0.5, mb: 2 }}
                              />
                              <Typography>No public schemas yet</Typography>
                            </>
                          ) : (
                            <>
                              <StarIcon
                                sx={{ fontSize: 48, opacity: 0.5, mb: 2 }}
                              />
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
      )}
    </Box>
  );
}
