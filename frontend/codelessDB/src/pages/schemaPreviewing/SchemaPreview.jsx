import { useState, useEffect } from "react";
import { useLocation, useNavigate, useParams } from "react-router-dom";
import {
  Box,
  Container,
  Card,
  CardContent,
  CardHeader,
  CardMedia,
  Typography,
  Tabs,
  Tab,
  Button,
  Chip,
  Stack,
  TextField,
  Tooltip,
  CircularProgress,
  IconButton
} from "@mui/material";

import {
  ArrowBack as ArrowLeftIcon,
  Share as ShareIcon,
  CallSplit as ForkIcon,
  Star as StarIcon,
  Edit as EditIcon,
  ContentCopy as CopyIcon,
  Check as CheckIcon,
  Description as BookOpenIcon,
  Code as CodeIcon,
  Storage as DatabaseIcon,
  ListAlt as FileTextIcon
} from "@mui/icons-material";

import LeftPanel from "../../components/LeftPanel";
import SimpleTopBar from "../../components/SimpleTopBar";
import { useNotification } from "../../components/NotificationContext";
import {
  getPublicDiagram,
  forkPublicDiagram,
  starPublicDiagram,
  unstarPublicDiagram
} from "./fetch";

import { MarkdownRenderer } from "./preview/MarkdownRenderer";
import { ContributorsSection } from "./preview/ContributorsSection";
import { AboutSection } from "./preview/AboutSection";
import { QueriesSection } from "./preview/QueriesSection";

// const mockSchema = {
//   diagramId: "1",
//   name: "E-Commerce System",
//   thumbnail: null,
//   shortDescription: "",
//   detailedDescription: "",
//   hashtags: [],
//   createdAt: new Date(),
//   lastModified: new Date(),
//   stars: null,
//   forks: null,
//   views: null,
//   ddl: "",
//   contributors: null,
//   cannedQueries: []
// };

export default function SchemaPreview() {
  const { id } = useParams();
  const location = useLocation();
  const navigate = useNavigate();
  const { showSuccess, showError } = useNotification();

  const [activeTab, setActiveTab] = useState(0);
  const [copied, setCopied] = useState(false);
  const [isStarred, setIsStarred] = useState(false);
  const [leftNav, setLeftNav] = useState("public");

  const [schemaData, setSchemaData] = useState(null);
  const [loading, setLoading] = useState(!location.state?.schemaData);
  const [ddlContent, setDdlContent] = useState(schemaData?.ddl || "");

  useEffect(() => {
    if (location.state?.schemaData) {
      setSchemaData({
        ...location.state.schemaData,
        createdAt: new Date(),
        lastModified: new Date(),
      });
      return;
    }
    const fetchData = async () => {
      if (!id) return;
      console.log("Fetching data for id:", id);

      setLoading(true);
      try {
        const data = await getPublicDiagram(id);
        console.log("Fetched data:", data);
        setSchemaData(data);
        // setIsStarred(data.starredByCurrentUser || false);
      } catch (err) {
        showError(err.message);
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, [id]);

  useEffect(() => {
    if (schemaData) {
      setDdlContent(schemaData.ddl || "");
    }
  }, [schemaData]);

  const handleTabChange = (event, newValue) => {
    setActiveTab(newValue);
  };

  const handleCopyDDL = () => {
    navigator.clipboard.writeText(ddlContent);
    setCopied(true);
    showSuccess("DDL copied to clipboard");
    setTimeout(() => setCopied(false), 2000);
  };

  const handleStar = async () => {
    try {
      if (isStarred) {
        const resp = await unstarPublicDiagram(schemaData.diagramId || id);
        setIsStarred(false);
        showSuccess(resp.message);
      } else {
        const resp = await starPublicDiagram(schemaData.diagramId || id);
        setIsStarred(true);
        showSuccess(resp.message);
      }
    } catch (err) {
      showError(err.message);
    }
  };

  const handleClone = async () => {
    try {
      const resp = await forkPublicDiagram(schemaData.diagramId || id);
      showSuccess(resp.message);
    } catch (err) {
      showError(err.message);
    }
  };

  const handleShare = () => {
    navigator.clipboard.writeText(window.location.href);
    showSuccess("Link copied to clipboard");
  };

  // Custom Header Content for TopBar
  const headerContent = schemaData ? (
    <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, width: '100%' }}>
      <IconButton onClick={() => navigate(-1)} size="small">
        <ArrowLeftIcon />
      </IconButton>
      <Box>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
          <Typography variant="h6" fontWeight="bold" noWrap>
            {schemaData.name || "Untitled Schema"}
          </Typography>
        </Box>
        <Typography variant="caption" color="text.secondary" noWrap display="block">
          {schemaData.shortDescription || "No description"}
        </Typography>
      </Box>

      <Box sx={{ flexGrow: 1 }} />

      {/* Actions */}
      <Stack direction="row" spacing={1} sx={{ pr: 2 }}>

        <Button variant="outlined" size="small" startIcon={<ShareIcon />} onClick={handleShare}>
          Share
        </Button>
        <Button variant="outlined" size="small" startIcon={<ForkIcon />} onClick={handleClone}>
          Clone
        </Button>
        <Button
          variant={isStarred ? "contained" : "outlined"}
          color={isStarred ? "warning" : "primary"}
          size="small"
          startIcon={<StarIcon />}
          onClick={handleStar}
        >
          {isStarred ? "Starred" : "Star"}
        </Button>
      </Stack>
    </Box>
  ) : null;

  if (loading) {
    return (
      <Box sx={{ display: "flex", minHeight: "100vh", bgcolor: "background.default" }}>
        <LeftPanel leftNav={leftNav} setLeftNav={setLeftNav} />
        <Box sx={{ flexGrow: 1, display: "flex", flexDirection: "column" }}>
          <SimpleTopBar title="Preview" />
          <Box sx={{ flexGrow: 1, display: 'flex', alignItems: 'center', justifyContent: 'center', p: 4 }}>
            <CircularProgress />
          </Box>
        </Box>
      </Box>
    );
  }

  if (!schemaData) {
    return (
      <Box sx={{ display: "flex", minHeight: "100vh", bgcolor: "background.default" }}>
        <LeftPanel leftNav={leftNav} setLeftNav={setLeftNav} />
        <Box sx={{ flexGrow: 1, display: "flex", flexDirection: "column" }}>
          <SimpleTopBar title="Preview" />
          <Box sx={{ flexGrow: 1, display: 'flex', alignItems: 'center', justifyContent: 'center', p: 4 }}>
            <Card sx={{ maxWidth: 400, textAlign: 'center', p: 4 }}>
              <DatabaseIcon sx={{ fontSize: 64, color: 'text.secondary', opacity: 0.5, mb: 2 }} />
              <Typography variant="h6" gutterBottom>No Schema to Preview</Typography>
              <Typography variant="body2" color="text.secondary" paragraph>
                Select a schema from the Discover page or create a new one.
              </Typography>
              <Button variant="contained" onClick={() => navigate("/schema/create")}>
                Create Schema
              </Button>
            </Card>
          </Box>
        </Box>
      </Box>
    );
  }

  return (
    <Box sx={{ display: "flex", minHeight: "100vh", bgcolor: "background.default" }}>
      <LeftPanel leftNav={leftNav} setLeftNav={setLeftNav} />

      <Box sx={{ flexGrow: 1, display: "flex", flexDirection: "column", overflow: "hidden" }}>
        <SimpleTopBar customContent={headerContent} />

        <Box sx={{ flexGrow: 1, overflowY: "auto", p: 4 }}>
          <Container maxWidth="xl">
            <Box sx={{ display: 'flex', flexDirection: { xs: 'column', lg: 'row' }, gap: 4 }}>

              {/* Left Column - Main Content */}
              <Box sx={{ flex: 1, minWidth: 0 }}>
                {/* Schema Diagram Thumbnail */}
                <Card variant="outlined" sx={{ mb: 4, overflow: 'hidden' }}>
                  <Box sx={{ position: 'relative', paddingTop: '56.25%', bgcolor: 'action.hover' }}>
                    {schemaData.thumbnail ? (
                      <CardMedia
                        component="img"
                        image={schemaData.thumbnail}
                        alt="Schema Diagram"
                        sx={{ position: 'absolute', top: 0, left: 0, width: '100%', height: '100%', objectFit: 'cover' }}
                      />
                    ) : (
                      <Box sx={{ position: 'absolute', top: 0, left: 0, width: '100%', height: '100%', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                        <Box sx={{ textAlign: 'center' }}>
                          <DatabaseIcon sx={{ fontSize: 48, color: 'text.secondary', opacity: 0.5, mb: 1 }} />
                          <Typography variant="body2" color="text.secondary">Schema diagram will appear here</Typography>
                        </Box>
                      </Box>
                    )}
                  </Box>
                </Card>

                {/* Tabs */}
                <Box sx={{ borderBottom: 1, borderColor: 'divider' }}>
                  <Tabs value={activeTab} onChange={handleTabChange} aria-label="schema tabs">
                    <Tab icon={<BookOpenIcon fontSize="small" />} iconPosition="start" label="Detailed Description" />
                    <Tab icon={<CodeIcon fontSize="small" />} iconPosition="start" label="DDL" />
                    {schemaData.cannedQueries?.length > 0 && (
                      <Tab
                        icon={<FileTextIcon fontSize="small" />}
                        iconPosition="start"
                        label={
                          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                            Canned Queries
                            <Chip label={schemaData.cannedQueries.length} size="small" sx={{ height: 16, fontSize: '0.6rem' }} />
                          </Box>
                        }
                      />
                    )}
                  </Tabs>
                </Box>

                <Box sx={{ mt: 3 }}>
                  {/* README Tab */}
                  {activeTab === 0 && (
                    <Card variant="outlined">
                      <CardContent>
                        {schemaData.detailedDescription ? (
                          <MarkdownRenderer content={schemaData.detailedDescription} />
                        ) : (
                          <Box sx={{ textAlign: 'center', py: 6, color: 'text.secondary' }}>
                            <FileTextIcon sx={{ fontSize: 48, opacity: 0.5, mb: 2 }} />
                            <Typography>No detailed description provided</Typography>
                          </Box>
                        )}
                      </CardContent>
                    </Card>
                  )}

                  {/* DDL Tab */}
                  {activeTab === 1 && (
                    <Card variant="outlined">
                      <CardHeader
                        title="Database DDL"
                        subheader="SQL statements to create this database schema"
                        action={
                          <Button variant="outlined" size="small" startIcon={copied ? <CheckIcon /> : <CopyIcon />} onClick={handleCopyDDL}>
                            {copied ? "Copied" : "Copy"}
                          </Button>
                        }
                      />
                      <CardContent>
                        {ddlContent ? (
                          <TextField
                            multiline
                            fullWidth
                            minRows={15}
                            value={ddlContent}
                            variant="outlined"
                            InputProps={{
                              readOnly: true,
                              style: { fontFamily: 'monospace', fontSize: '0.875rem' }
                            }}
                            sx={{ bgcolor: 'action.hover' }}
                          />
                        ) : (
                          <Box sx={{ textAlign: 'center', py: 6, color: 'text.secondary' }}>
                            <CodeIcon sx={{ fontSize: 48, opacity: 0.5, mb: 2 }} />
                            <Typography>No DDL available.</Typography>
                          </Box>
                        )}
                      </CardContent>
                    </Card>
                  )}

                  {/* Queries Tab */}
                  {activeTab === 2 && (
                    <QueriesSection queries={schemaData.cannedQueries} />
                  )}
                </Box>
              </Box>

              {/* Right Sidebar */}
              <Box sx={{ width: { lg: 320 }, flexShrink: 0, display: 'flex', flexDirection: 'column', gap: 3 }}>
                <AboutSection
                  shortDescription={schemaData.shortDescription || "No description provided"}
                  hashtags={schemaData.hashtags || []}
                  stats={{
                    stars: schemaData.stars || 0,
                    forks: schemaData.forks || 0,
                    views: schemaData.views || 0
                  }}
                  dates={{
                    createdAt: schemaData.createdAt,
                    lastModified: schemaData.lastModified
                  }}
                />

                {schemaData.contributors && schemaData.contributors.length > 0 && (
                  <ContributorsSection collaborators={schemaData.contributors} />
                )}

                {/* <Card variant="outlined" sx={{ bgcolor: 'primary.50', borderColor: 'primary.200' }}>
                  <CardContent>
                    <Typography variant="body2" color="text.secondary" paragraph>
                      This is a preview. Clone this schema to edit it in your workspace.
                    </Typography>
                    <Button variant="contained" fullWidth onClick={handleClone}>
                      Clone Schema
                    </Button>
                  </CardContent>
                </Card> */}
              </Box>

            </Box>
          </Container>
        </Box>
      </Box>
    </Box>
  );
}
