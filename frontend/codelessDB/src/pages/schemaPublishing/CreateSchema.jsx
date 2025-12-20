import {
    CheckCircle as CheckCircleIcon,
    RadioButtonUnchecked as CircleIcon,
    Storage as DatabaseIcon,
    Visibility as EyeIcon,
    Info as InfoIcon
} from "@mui/icons-material";
import {
    Box,
    Button,
    Card,
    CardContent,
    CardHeader,
    Container,
    LinearProgress,
    Stack,
    TextField,
    Typography
} from "@mui/material";
import { useEffect, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";

import LeftPanel from "../../components/LeftPanel";
import { useNotification } from "../../components/NotificationContext";
import SimpleTopBar from "../../components/SimpleTopBar";
import { fetchHashtags } from "../discover/fetch.js";
import { fetchToBePublished, publishSchema } from "./fetch";

import HashtagInput from "../../components/HashtagInput.jsx";
import DiagramSelector from "./create/DiagramSelector";
import { MarkdownEditor } from "./create/MarkdownEditor";
import { QueryBuilder } from "./create/QueryBuilder";

const defaultMarkdown = `# Your Schema Name

Add a comprehensive description of your database schema here. Use Markdown to format your content.

## Features

- List key features of your schema
- Explain the main entities and relationships
- Describe use cases

## Getting Started

Provide instructions on how to use this schema...

## License

Specify your license here...`;


export default function CreateSchema() {
  const navigate = useNavigate();
  const location = useLocation();
  const { showSuccess, showError } = useNotification();

  const [leftNav, setLeftNav] = useState("public");
  const [selectedDiagram, setSelectedDiagram] = useState(null);
  const [schemaName, setSchemaName] = useState("");
  const [shortDescription, setShortDescription] = useState("");
  const [description, setDescription] = useState(defaultMarkdown);
  const [queries, setQueries] = useState([]);
  const [isSaving, setIsSaving] = useState(false);
  const [loading, setLoading] = useState(true);
  const [hashtags, setHashtags] = useState([]);
  const [selectedHashtags, setSelectedHashtags] = useState([]);
  const [diagramsCards, setDiagramsCards] = useState([]);

  const loadToBePublished = async () => {
    try {
      const resp = await fetchToBePublished(0, 10);
      setDiagramsCards(resp.content || []);
    } catch (err) {
      setDiagramsCards([]);
      showError && showError(err?.message || String(err));
    } finally {
      setLoading(false);
    }
  }

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

  useEffect(() => {
    const savedData = localStorage.getItem("schemaData");
    if (savedData) {
      try {
        const schemaData = JSON.parse(savedData);
        setSelectedDiagram(schemaData);
        setSchemaName(schemaData.name);
        setShortDescription(schemaData.shortDescription);
        setDescription(schemaData.detailedDescription);
        setSelectedHashtags(schemaData.hashtags || []);
        setQueries(schemaData.cannedQueries || []);
        // Clear localStorage after restoration to prevent stale data
        localStorage.removeItem("schemaData");
      } catch (err) {
        console.error("Failed to restore schema data:", err);
        localStorage.removeItem("schemaData");
      }
    }
    loadHashtags();
    loadToBePublished();
  }, []);

  const handleDiagramSelect = (diagram) => {
    setSelectedDiagram(diagram);
    setSchemaName(diagram.name);
    // if (!schemaName) setSchemaName(diagram.name);
    showSuccess("Diagram selected!");
  };

  const completionSteps = [
    { label: "Select Diagram", completed: !!selectedDiagram },
    // { label: "Schema Name", completed: !!schemaName.trim() },
    { label: "Description", completed: !!shortDescription.trim() },
    // { label: "Hashtags", completed: selectedHashtags.length > 0 },
  ];
  const completedCount = completionSteps.filter(s => s.completed).length;
  const completionProgress = (completedCount / completionSteps.length) * 100;

  const handlePublish = async () => {
    if (!selectedDiagram) {
      showError("Please select a diagram first");
      return;
    }
    // if (!schemaName.trim()) {
    //   showError("Please enter a schema name");
    //   return;
    // }
    if (!shortDescription.trim()) {
      showError("Please enter a short description");
      return;
    }
    // if (selectedHashtags.length === 0) {
    //   showError("Please add at least one hashtag");
    //   return;
    // }

    setIsSaving(true);

    publishSchema(selectedDiagram.diagramId, shortDescription, description, selectedHashtags, queries)
      .then((resp) => {
        showSuccess(resp);
      })
      .catch((err) => {
        showError && showError(err?.message || String(err));
        console.log(err);
      });

    navigate("/discover", { state: null });
  };

  const handlePreview = () => {
    if (!selectedDiagram) {
      showError("Select a diagram first");
      return;
    }
    const schemaData = {
      diagramId: selectedDiagram.diagramId,
      name: schemaName,
      thumbnail: selectedDiagram.thumbnail,
      shortDescription,
      detailedDescription: description,
      hashtags: selectedHashtags,
      createdAt: selectedDiagram.createdAt,
      lastModified: selectedDiagram.lastModified,
      ddl: selectedDiagram.ddl,
      cannedQueries: queries,
    };
    localStorage.setItem("schemaData", JSON.stringify(schemaData));
    navigate("/schema/preview/draft");
  };

  return (
    <Box sx={{ display: "flex", minHeight: "100vh", bgcolor: "background.default" }}>
      <LeftPanel leftNav={leftNav} setLeftNav={setLeftNav} />

      <Box sx={{ flexGrow: 1, display: "flex", flexDirection: "column", overflow: "hidden" }}>
        <SimpleTopBar
          customContent={
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
              <Box sx={{ p: 1, borderRadius: 1, bgcolor: 'primary.light', color: 'primary.contrastText' }}>
                <DatabaseIcon />
              </Box>
              <Box>
                <Typography variant="h6" fontWeight="bold">Publish Diagram</Typography>
                <Typography variant="caption" color="text.secondary">Share your database design with the community</Typography>
              </Box>
            </Box>
          }
        />

        <Box sx={{ flexGrow: 1, overflowY: "auto", p: 4 }}>
          <Container maxWidth="xl">
            <Box sx={{ display: 'flex', flexDirection: { xs: 'column', lg: 'row' }, gap: 4 }}>

              {/* Main Form */}
              <Box sx={{ flex: 1, display: 'flex', flexDirection: 'column', gap: 4 }}>

                {/* Diagram Selection */}
                <Card variant="outlined">
                  <CardHeader
                    title={
                      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                        {selectedDiagram ? <CheckCircleIcon color="success" /> : <CircleIcon color="disabled" />}
                        <Typography variant="h6">Select Diagram</Typography>
                        {selectedDiagram && (
                          <Box sx={{ ml: 'auto', bgcolor: 'action.selected', px: 1, borderRadius: 1, display: 'flex', alignItems: 'center', gap: 0.5 }}>
                            <CheckCircleIcon fontSize="small" color="success" />
                            <Typography variant="caption" fontWeight="bold">Selected</Typography>
                          </Box>
                        )}
                      </Box>
                    }
                    subheader="Choose one of your diagrams to publish as a public schema"
                  />
                  <CardContent>
                    <DiagramSelector
                      loading={loading}
                      diagramsCards={diagramsCards}
                      selectedDiagram={selectedDiagram}
                      onSelect={handleDiagramSelect}
                    />
                  </CardContent>
                </Card>

                {/* Basic Information */}
                <Card variant="outlined">
                  <CardHeader
                    title={
                      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                        {schemaName.trim() && shortDescription.trim() ? <CheckCircleIcon color="success" /> : <CircleIcon color="disabled" />}
                        <Typography variant="h6">Basic Information</Typography>
                      </Box>
                    }
                    subheader="Provide essential details about your database schema"
                  />
                  <CardContent sx={{ display: 'flex', flexDirection: 'column', gap: 3 }}>
                    {/* <TextField
                      label="Schema Name"
                      required
                      fullWidth
                      value={schemaName}
                      onChange={(e) => setSchemaName(e.target.value)}
                      placeholder="e.g., CollegeDB, HospitalManagement"
                    /> */}
                    <Box>
                      <TextField
                        label="Short Description"
                        required
                        fullWidth
                        inputProps={{ minLength: 5, maxLength: 300 }}
                        value={shortDescription}
                        onChange={(e) => setShortDescription(e.target.value)}
                        placeholder="A brief one-line description of your schema"
                        helperText="This will be shown in search results and preview cards"
                      />
                    </Box>
                  </CardContent>
                </Card>

                {/* Hashtags */}
                <Card variant="outlined">
                  <CardHeader
                    title={
                      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                        {/* {selectedHashtags.length > 0 ? <CheckCircleIcon color="success" /> : <CircleIcon color="disabled" />} */}
                        <Typography variant="h6">Hashtags</Typography>
                        {/* <Typography variant="caption" color="error">*</Typography> */}
                      </Box>
                    }
                    subheader="Add tags to help others discover your schema"
                  />
                  <CardContent>
                    <HashtagInput
                      hashtags={hashtags}
                      selectedHashtags={selectedHashtags}
                      onSelect={setSelectedHashtags}
                      allowCreation={true}
                    />
                  </CardContent>
                </Card>

                {/* Detailed Description */}
                <Card variant="outlined">
                  <CardHeader
                    title={<Typography variant="h6">Detailed Description</Typography>}
                    subheader="Write a comprehensive README for your schema using Markdown"
                    sx={{ mb: -4 }}
                  />
                  <CardContent>
                    <MarkdownEditor
                      value={description}
                      onChange={setDescription}
                      placeholder="Write your schema description in Markdown..."
                    />
                  </CardContent>
                </Card>

                {/* DDL Display */}
                {selectedDiagram && (
                  <Card variant="outlined">
                    <CardHeader
                      title={<Typography variant="h6">Database DDL</Typography>}
                      subheader="The SQL DDL statements from your selected diagram"
                    />
                    <CardContent>
                      <Paper variant="outlined" sx={{ p: 2, bgcolor: 'action.hover', maxHeight: 300, overflow: 'auto' }}>
                        <Typography variant="body2" fontFamily="monospace" component="pre" sx={{ whiteSpace: 'pre-wrap' }}>
                          {selectedDiagram.ddl}
                        </Typography>
                      </Paper>
                    </CardContent>
                  </Card>
                )}

                {/* Predefined Queries */}
                <Card variant="outlined">
                  <CardHeader
                    title={<Typography variant="h6">Predefined Transactions</Typography>}
                    subheader="Add common queries that users can use with this schema (optional)"
                  />
                  <CardContent>
                    <QueryBuilder queries={queries} onChange={setQueries} />
                  </CardContent>
                </Card>

                {/* Action Buttons */}
                <Box sx={{ display: 'flex', justifyContent: 'flex-end', gap: 2, pt: 2 }}>
                  <Button variant="outlined" onClick={() => navigate("/discover")}>
                    Cancel
                  </Button>

                  <Button
                    variant="contained"
                    onClick={handlePublish}
                    disabled={isSaving || completionProgress < 100}
                    sx={{ px: 4 }}
                  >
                    {isSaving ? "Publishing..." : "Publish Schema"}
                  </Button>
                </Box>

              </Box>

              {/* Right Sidebar - Progress */}
              <Box sx={{ width: { lg: 300 }, flexShrink: 0 }}>
                <Box sx={{ position: 'sticky', top: 24, display: 'flex', flexDirection: 'column', gap: 3 }}>

                  {/* Completion Progress */}
                  <Card variant="outlined">
                    <CardHeader title={<Typography variant="subtitle1" fontWeight="bold">Completion Progress</Typography>} />
                    <CardContent>
                      <Box sx={{ mb: 2 }}>
                        <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1 }}>
                          <Typography variant="body2" color="text.secondary">Progress</Typography>
                          <Typography variant="body2" fontWeight="bold">{Math.round(completionProgress)}%</Typography>
                        </Box>
                        <LinearProgress variant="determinate" value={completionProgress} sx={{ height: 8, borderRadius: 4 }} />
                      </Box>
                      <Stack spacing={1}>
                        {completionSteps.map((step, index) => (
                          <Box key={index} sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                            {step.completed ? (
                              <CheckCircleIcon fontSize="small" color="success" />
                            ) : (
                              <CircleIcon fontSize="small" color="disabled" />
                            )}
                            <Typography variant="body2" color={step.completed ? "text.primary" : "text.secondary"}>
                              {step.label}
                            </Typography>
                          </Box>
                        ))}
                      </Stack>
                    </CardContent>
                  </Card>

                  {/* Tips */}
                  <Card variant="outlined" sx={{ bgcolor: 'primary.50', borderColor: 'primary.200' }}>
                    <CardHeader
                      title={
                        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                          <InfoIcon color="primary" fontSize="small" />
                          <Typography variant="subtitle2" fontWeight="bold" color="primary.main">Tips</Typography>
                        </Box>
                      }
                      sx={{ pb: 0 }}
                    />
                    <CardContent>
                      <Stack spacing={1}>
                        <Typography variant="caption" color="text.secondary">• Choose a descriptive name that reflects the schema's purpose</Typography>
                        <Typography variant="caption" color="text.secondary">• Add relevant hashtags to improve discoverability</Typography>
                        <Typography variant="caption" color="text.secondary">• Include sample queries to help users get started</Typography>
                        <Typography variant="caption" color="text.secondary">• Write detailed documentation in the description</Typography>
                      </Stack>
                    </CardContent>
                  </Card>

                  {/* Preview Button */}
                  <Button
                    variant="outlined"
                    fullWidth
                    startIcon={<EyeIcon />}
                    onClick={handlePreview}
                  >
                    Preview Schema
                  </Button>

                </Box>
              </Box>

            </Box>
          </Container>
        </Box>
      </Box>
    </Box>
  );
}
