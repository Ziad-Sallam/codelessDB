import { useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import {
  Box,
  Container,
  Card,
  CardContent,
  CardHeader,
  Avatar,
  Typography,
  Tabs,
  Tab,
  Grid,
  Chip,
  Stack,
  Divider,
  Button
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

const mockUser = {
  id: "1",
  name: "Ahmed Hassan",
  username: "ahmed_dev",
  avatar: "https://api.dicebear.com/7.x/avataaars/svg?seed=Ahmed",
  bio: "Database architect & software engineer. Passionate about designing scalable database systems and helping teams build better data infrastructure.",
  location: "Cairo, Egypt",
  website: "https://ahmedhassan.dev",
  joinedDate: "January 2023",
  publicSchemas: 12,
  totalStars: 3450,
};

const mockPublicDiagrams = [
  {
    id: "1",
    name: "College Management System",
    description: "Complete database schema for managing college operations including students, courses, and faculty",
    thumbnail: "", // Placeholder
    stars: 245,
    forks: 89,
    views: 1520,
    hashtags: ["college", "education", "management"],
    lastUpdated: "2 days ago",
  },
  {
    id: "2",
    name: "Hospital Database",
    description: "Comprehensive healthcare management system schema",
    thumbnail: "",
    stars: 189,
    forks: 67,
    views: 980,
    hashtags: ["hospital", "healthcare", "medical"],
    lastUpdated: "1 week ago",
  },
  {
    id: "3",
    name: "E-commerce Platform",
    description: "Scalable database design for online shopping platforms",
    thumbnail: "",
    stars: 312,
    forks: 124,
    views: 2100,
    hashtags: ["ecommerce", "shopping", "retail"],
    lastUpdated: "3 days ago",
  },
];

export default function DesignerProfile() {
  const navigate = useNavigate();
  const { username } = useParams();
  const [leftNav, setLeftNav] = useState("public");
  const [activeTab, setActiveTab] = useState(0);

  const handleTabChange = (event, newValue) => {
    setActiveTab(newValue);
  };

  const handleSchemaClick = (diagramId) => {
    navigate(`/schema/preview/${diagramId}`);
  };

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
                      src={mockUser.avatar}
                      alt={mockUser.name}
                      sx={{ width: 128, height: 128, mx: 'auto', mb: 2, border: 4, borderColor: 'background.paper', boxShadow: 2 }}
                    />
                    <Typography variant="h5" fontWeight="bold" gutterBottom>
                      {mockUser.name}
                    </Typography>
                    <Typography variant="body1" color="text.secondary" gutterBottom>
                      @{mockUser.username}
                    </Typography>

                    <Typography variant="body2" sx={{ mt: 2, mb: 3 }}>
                      {mockUser.bio}
                    </Typography>

                    <Divider sx={{ my: 2 }} />

                    <Stack spacing={1.5} alignItems="center" sx={{ color: 'text.secondary', fontSize: '0.875rem' }}>
                      {mockUser.location && (
                        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                          <MapPinIcon fontSize="small" />
                          {mockUser.location}
                        </Box>
                      )}
                      {mockUser.website && (
                        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                          <LinkIcon fontSize="small" />
                          <a
                            href={mockUser.website}
                            target="_blank"
                            rel="noopener noreferrer"
                            style={{ color: 'inherit', textDecoration: 'none' }}
                          >
                            {mockUser.website.replace('https://', '')}
                          </a>
                        </Box>
                      )}
                      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                        <CalendarIcon fontSize="small" />
                        Joined {mockUser.joinedDate}
                      </Box>
                    </Stack>

                    <Box sx={{ mt: 3, display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 2 }}>
                      <Box sx={{ p: 1.5, bgcolor: 'action.hover', borderRadius: 2 }}>
                        <TrophyIcon sx={{ color: 'warning.main', mb: 0.5 }} />
                        <Typography variant="h6" fontWeight="bold">{mockUser.totalStars}</Typography>
                        <Typography variant="caption" color="text.secondary">Total Stars</Typography>
                      </Box>
                      <Box sx={{ p: 1.5, bgcolor: 'action.hover', borderRadius: 2 }}>
                        <DatabaseIcon sx={{ color: 'primary.main', mb: 0.5 }} />
                        <Typography variant="h6" fontWeight="bold">{mockUser.publicSchemas}</Typography>
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
                    <Tab icon={<BookOpenIcon fontSize="small" />} iconPosition="start" label={`Schemas (${mockPublicDiagrams.length})`} />
                    <Tab icon={<StarIcon fontSize="small" />} iconPosition="start" label="Starred" />
                  </Tabs>
                </Box>

                {activeTab === 0 && (
                  <Stack spacing={3}>
                    {mockPublicDiagrams.map((diagram) => (
                      <Card
                        key={diagram.id}
                        variant="outlined"
                        sx={{
                          cursor: 'pointer',
                          transition: 'all 0.2s',
                          '&:hover': { borderColor: 'primary.main', boxShadow: 2 }
                        }}
                        onClick={() => handleSchemaClick(diagram.id)}
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
                              justifyContent: 'center'
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
                              {diagram.description}
                            </Typography>

                            <Box sx={{ display: 'flex', alignItems: 'center', flexWrap: 'wrap', gap: 2 }}>
                              <Box sx={{ display: 'flex', gap: 1 }}>
                                {diagram.hashtags.slice(0, 3).map((tag) => (
                                  <Chip key={tag} label={tag} size="small" sx={{ height: 20, fontSize: '0.75rem' }} />
                                ))}
                              </Box>

                              <Box sx={{ flexGrow: 1 }} />

                              <Box sx={{ display: 'flex', gap: 2, color: 'text.secondary', fontSize: '0.75rem' }}>
                                <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
                                  <StarIcon sx={{ fontSize: 14 }} /> {diagram.stars}
                                </Box>
                                <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
                                  <ForkIcon sx={{ fontSize: 14 }} /> {diagram.forks}
                                </Box>
                                <Box>Updated {diagram.lastUpdated}</Box>
                              </Box>
                            </Box>
                          </Box>
                        </CardContent>
                      </Card>
                    ))}

                    {mockPublicDiagrams.length === 0 && (
                      <Box sx={{ textAlign: 'center', py: 8, color: 'text.secondary', border: '1px dashed', borderColor: 'divider', borderRadius: 2 }}>
                        <DatabaseIcon sx={{ fontSize: 48, opacity: 0.5, mb: 2 }} />
                        <Typography>No public schemas yet</Typography>
                      </Box>
                    )}
                  </Stack>
                )}

                {activeTab === 1 && (
                  <Box sx={{ textAlign: 'center', py: 8, color: 'text.secondary', border: '1px dashed', borderColor: 'divider', borderRadius: 2 }}>
                    <StarIcon sx={{ fontSize: 48, opacity: 0.5, mb: 2 }} />
                    <Typography>No starred schemas yet</Typography>
                  </Box>
                )}

              </Box>
            </Box>
          </Container>
        </Box>
      </Box>
    </Box>
  );
}
