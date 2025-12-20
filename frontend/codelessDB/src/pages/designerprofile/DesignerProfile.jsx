import {
  Description as BookOpenIcon,
  CalendarToday as CalendarIcon,
  Storage as DatabaseIcon,
  CallSplit as ForkIcon,
  Link as LinkIcon,
  Star as StarIcon,
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
import { Link, useNavigate, useParams } from "react-router-dom";

import LeftPanel from "../../components/LeftPanel";
import { useNotification } from "../../components/NotificationContext";
import SimpleTopBar from "../../components/SimpleTopBar";
import { validateToken } from "../auth/fetch";
import {
  fetchDesignerProfile,
  fetchFollowers,
  fetchFollowings,
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

  const [isLoadingSocial, setIsLoadingSocial] = useState(false);
  const [socialUsers, setSocialUsers] = useState([]);
  const [socialTotal, setSocialTotal] = useState(0);

  useEffect(() => {
    if (!username) return;
    setIsLoadingProfile(true);
    setProfile(null);
    setDiagrams([]);
    setSocialUsers([]);
    setActiveTab(0);
    window.scrollTo(0, 0);

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

    if (activeTab === 0 || activeTab === 1) {
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
    } else {
      setIsLoadingSocial(true);
      const fetchFn = activeTab === 2 ? fetchFollowers : fetchFollowings;

      fetchFn(username, 0, 50)
        .then((data) => {
          setSocialUsers(data.content || []);
          setSocialTotal(data.totalElements || 0);
        })
        .catch((err) => {
          console.error(err);
          showError("Failed to load users");
        })
        .finally(() => {
          setIsLoadingSocial(false);
        });
    }
  }, [username, activeTab]);

  const handleTabChange = (event, newValue) => {
    setActiveTab(newValue);
    setDiagrams([]);
    setSocialUsers([]);
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
        showSuccess(`Followed ${profile.name || profile.username}`);
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
                      <Typography variant="h4" fontWeight="800" gutterBottom sx={{ color: "text.primary" }}>
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
                            display: "flex",
                            flexDirection: "column",
                            alignItems: "center",
                            transition: "all 0.2s",
                            "&:hover": { bgcolor: "action.selected" }
                          }}
                        >
                          <StarIcon sx={{ color: "warning.main", mb: 0.5, fontSize: "1.5rem" }} />
                          <Typography variant="h6" fontWeight="800">
                            {profile.totalStars || 0}
                          </Typography>
                          <Typography variant="caption" color="text.secondary" sx={{ textTransform: "uppercase", fontWeight: 800, letterSpacing: 1, fontSize: "0.6rem" }}>
                            Total Stars
                          </Typography>
                        </Box>
                        <Box
                          sx={{
                            p: 1.5,
                            bgcolor: "action.hover",
                            borderRadius: 2,
                            display: "flex",
                            flexDirection: "column",
                            alignItems: "center",
                            transition: "all 0.2s",
                            "&:hover": { bgcolor: "action.selected" }
                          }}
                        >
                          <DatabaseIcon
                            sx={{ color: "primary.main", mb: 0.5, fontSize: "1.5rem" }}
                          />
                          <Typography variant="h6" fontWeight="800">
                            {profile.publicCount || 0}
                          </Typography>
                          <Typography variant="caption" color="text.secondary" sx={{ textTransform: "uppercase", fontWeight: 800, letterSpacing: 1, fontSize: "0.6rem" }}>
                            Schemas
                          </Typography>
                        </Box>
                        <Box
                          sx={{
                            p: 1.5,
                            bgcolor: "action.hover",
                            borderRadius: 2,
                            display: "flex",
                            flexDirection: "column",
                            alignItems: "center",
                            transition: "all 0.2s",
                            "&:hover": { bgcolor: "action.selected" }
                          }}
                        >
                          <UsersIcon sx={{ color: "info.main", mb: 0.5, fontSize: "1.5rem" }} />
                          <Typography variant="h6" fontWeight="800">
                            {profile.followersCount || 0}
                          </Typography>
                          <Typography variant="caption" color="text.secondary" sx={{ textTransform: "uppercase", fontWeight: 800, letterSpacing: 1, fontSize: "0.6rem" }}>
                            Followers
                          </Typography>
                        </Box>
                        <Box
                          sx={{
                            p: 1.5,
                            bgcolor: "action.hover",
                            borderRadius: 2,
                            display: "flex",
                            flexDirection: "column",
                            alignItems: "center",
                            transition: "all 0.2s",
                            "&:hover": { bgcolor: "action.selected" }
                          }}
                        >
                          <UsersIcon sx={{ color: "secondary.main", mb: 0.5, fontSize: "1.5rem" }} />
                          <Typography variant="h6" fontWeight="800">
                            {profile.followingCount || 0}
                          </Typography>
                          <Typography variant="caption" color="text.secondary" sx={{ textTransform: "uppercase", fontWeight: 800, letterSpacing: 1, fontSize: "0.6rem" }}>
                            Following
                          </Typography>
                        </Box>
                      </Box>

                      {currentUser?.username !== profile.username && (
                        <Button
                          fullWidth
                          variant={profile.isFollowed ? "contained" : "outlined"}
                          color={profile.isFollowed ? "secondary" : "primary"}
                          onClick={handleFollowToggle}
                          sx={{ mt: 3, borderRadius: 2, py: 1 }}
                          startIcon={profile.isFollowed ? <UsersIcon /> : <UserPlusIcon />}
                        >
                          {profile.isFollowed ? "Unfollow" : "Follow"}
                        </Button>
                      )}
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
                        label={`Starred (${profile?.starredCount || 0})`}
                      />
                      <Tab
                        icon={<UsersIcon fontSize="small" />}
                        iconPosition="start"
                        label={`Followers (${profile?.followersCount || 0})`}
                      />
                      <Tab
                        icon={<UsersIcon fontSize="small" />}
                        iconPosition="start"
                        label={`Following (${profile?.followingCount || 0})`}
                      />
                    </Tabs>
                  </Box>

                  {activeTab <= 1 ? (
                    isLoadingDiagrams ? (
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
                                    fontWeight="800"
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
                    )
                  ) : (
                    isLoadingSocial ? (
                      <Box
                        sx={{ display: "flex", justifyContent: "center", py: 4 }}
                      >
                        <CircularProgress />
                      </Box>
                    ) : (
                      <Stack spacing={2}>
                        {socialUsers.map((user) => (
                          <Card
                            key={user.username}
                            variant="outlined"
                            component={Link}
                            to={`/designer/${user.username.replace("@", "")}`}
                            sx={{
                              cursor: "pointer",
                              transition: "all 0.2s",
                              textDecoration: "none",
                              color: "inherit",
                              "&:hover": {
                                borderColor: "primary.main",
                                boxShadow: 2,
                              },
                            }}
                          >
                            <CardContent sx={{ p: 3 }}>
                              <Box
                                sx={{
                                  display: "flex",
                                  flexDirection: { xs: "column", md: "row" },
                                  gap: { xs: 2, md: 4 },
                                  alignItems: { xs: "stretch", md: "center" },
                                  mb: 2,
                                }}
                              >
                                <Box sx={{ display: "flex", gap: 3, alignItems: "center", flexGrow: 1 }}>
                                  <Avatar
                                    src={user.picture}
                                    sx={{
                                      width: { xs: 72, md: 80 },
                                      height: { xs: 72, md: 80 },
                                      border: "3px solid",
                                      borderColor: "divider",
                                      boxShadow: 2,
                                      flexShrink: 0
                                    }}
                                  >
                                    {user.name?.[0]}
                                  </Avatar>

                                  <Box sx={{ minWidth: 0 }}>
                                    <Typography variant="h5" fontWeight="800" noWrap sx={{ color: "text.primary", mb: 0.2, fontSize: { xs: "1.2rem", md: "1.4rem" } }}>
                                      {user.name}
                                    </Typography>
                                    <Typography
                                      variant="subtitle1"
                                      color="primary.main"
                                      fontWeight="600"
                                      sx={{ fontSize: { xs: "0.85rem", md: "0.95rem" } }}
                                    >
                                      @{user.username}
                                    </Typography>
                                  </Box>
                                </Box>

                                <Box
                                  sx={{
                                    display: "grid",
                                    gridTemplateColumns: "repeat(4, 1fr)",
                                    gap: 1.5,
                                    alignItems: "center",
                                    width: { xs: "100%", md: "320px" }
                                  }}
                                >
                                  <Box sx={{ textAlign: "center", p: 1, bgcolor: "action.hover", borderRadius: 2 }}>
                                    <DatabaseIcon sx={{ color: "primary.main", mb: 0.5, fontSize: "1.2rem" }} />
                                    <Typography variant="subtitle2" fontWeight="800">
                                      {user.publicCount}
                                    </Typography>
                                    <Typography variant="caption" color="text.secondary" sx={{ display: "block", textTransform: "uppercase", fontSize: "0.5rem", fontWeight: 800, letterSpacing: 0.5 }}>
                                      Schemas
                                    </Typography>
                                  </Box>
                                  <Box sx={{ textAlign: "center", p: 1, bgcolor: "action.hover", borderRadius: 2 }}>
                                    <StarIcon sx={{ color: "warning.main", mb: 0.5, fontSize: "1.2rem" }} />
                                    <Typography variant="subtitle2" fontWeight="800">
                                      {user.totalStars}
                                    </Typography>
                                    <Typography variant="caption" color="text.secondary" sx={{ display: "block", textTransform: "uppercase", fontSize: "0.5rem", fontWeight: 800, letterSpacing: 0.5 }}>
                                      Stars
                                    </Typography>
                                  </Box>
                                  <Box sx={{ textAlign: "center", p: 1, bgcolor: "action.hover", borderRadius: 2 }}>
                                    <UsersIcon sx={{ color: "info.main", mb: 0.5, fontSize: "1.2rem" }} />
                                    <Typography variant="subtitle2" fontWeight="800">
                                      {user.followersCount}
                                    </Typography>
                                    <Typography variant="caption" color="text.secondary" sx={{ display: "block", textTransform: "uppercase", fontSize: "0.5rem", fontWeight: 800, letterSpacing: 0.5 }}>
                                      Followers
                                    </Typography>
                                  </Box>
                                  <Box sx={{ textAlign: "center", p: 1, bgcolor: "action.hover", borderRadius: 2 }}>
                                    <UsersIcon sx={{ color: "secondary.main", mb: 0.5, fontSize: "1.2rem" }} />
                                    <Typography variant="subtitle2" fontWeight="800">
                                      {user.followingCount}
                                    </Typography>
                                    <Typography variant="caption" color="text.secondary" sx={{ display: "block", textTransform: "uppercase", fontSize: "0.5rem", fontWeight: 800, letterSpacing: 0.5 }}>
                                      Following
                                    </Typography>
                                  </Box>
                                </Box>
                              </Box>

                              <Divider sx={{ mb: 2.5, opacity: 0.4 }} />

                              <Box>
                                <Typography
                                  variant="body2"
                                  color="text.secondary"
                                  sx={{
                                    display: "-webkit-box",
                                    WebkitLineClamp: 3,
                                    WebkitBoxOrient: "vertical",
                                    overflow: "hidden",
                                    minHeight: "3em",
                                    lineHeight: 1.5,
                                  }}
                                >
                                  {user.bio || "No bio available"}
                                </Typography>
                              </Box>
                            </CardContent>
                          </Card>
                        ))}
                        {socialUsers.length === 0 && (
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
                            <UsersIcon sx={{ fontSize: 48, opacity: 0.5, mb: 2 }} />
                            <Typography>
                              {activeTab === 2
                                ? "No followers yet"
                                : "Not following anyone yet"}
                            </Typography>
                          </Box>
                        )}
                      </Stack>
                    )
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
