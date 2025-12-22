import { useState, useEffect } from "react";
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  Grid,
  Box,
  Typography,
  TextField,
  Paper,
  IconButton,
  CircularProgress,
  Divider,
} from "@mui/material";
import { Close as CloseIcon, Save as SaveIcon } from "@mui/icons-material";
import { MarkdownEditor } from "../../schemaPublishing/create/MarkdownEditor";
import { QueryBuilder } from "../../schemaPublishing/create/QueryBuilder";
import HashtagInput from "../../../components/HashtagInput";
import ConfirmationModal from "../../../components/ConfirmationModal/ConfirmationModal";
import { useNotification } from "../../../components/NotificationContext";
import { getPublicDiagram } from "../../schemaPreviewing/fetch.js";
import { updatePublicSchema } from "../../schemaPublishing/fetch.js";
import { fetchHashtags } from "../../discover/fetch.js";

export default function EditPublicDetailsModal({ open, onClose, diagramId }) {
  const { showSuccess, showError } = useNotification();

  const [shortDescription, setShortDescription] = useState("");
  const [detailedDescription, setDetailedDescription] = useState("");
  const [selectedHashtags, setSelectedHashtags] = useState([]);
  const [queries, setQueries] = useState([]);
  const [allHashtags, setAllHashtags] = useState([]);
  const [publicDiagram, setPublicDiagram] = useState(null);

  const [isLoading, setIsLoading] = useState(true);
  const [isSaving, setIsSaving] = useState(false);
  const [confirmCloseOpen, setConfirmCloseOpen] = useState(false);
  const [hasChanges, setHasChanges] = useState(false);

  useEffect(() => {
    if (open && diagramId) {
      const loadPublicDiagram = async () => {
        setIsLoading(true);
        try {
          const data = await getPublicDiagram(diagramId);
          setPublicDiagram(data);
          setShortDescription(data.shortDescription || "");
          setDetailedDescription(data.detailedDescription || "");
          setSelectedHashtags(data.hashTags || []);
          setQueries(data.cannedQueries || []);
          setHasChanges(false);
        } catch (err) {
          showError("Failed to load public diagram details");
          onClose();
        } finally {
          setIsLoading(false);
        }
      };
      loadPublicDiagram();
    }
  }, [open, diagramId]);

  useEffect(() => {
    const loadHashtags = async () => {
      try {
        const resp = await fetchHashtags();
        setAllHashtags(resp);
      } catch (err) {
        console.error("Failed to fetch hashtags:", err);
      }
    };
    loadHashtags();
  }, []);

  useEffect(() => {
    if (!publicDiagram) return;
    const changed =
      shortDescription !== (publicDiagram.shortDescription || "") ||
      detailedDescription !== (publicDiagram.detailedDescription || "") ||
      JSON.stringify(selectedHashtags) !== JSON.stringify(publicDiagram.hashTags || []) ||
      JSON.stringify(queries) !== JSON.stringify(publicDiagram.cannedQueries || []);
    setHasChanges(changed);
  }, [shortDescription, detailedDescription, selectedHashtags, queries, publicDiagram]);

  const handleCloseAttempt = () => {
    if (hasChanges) {
      setConfirmCloseOpen(true);
    } else {
      onClose();
    }
  };

  const handleSave = async () => {
    if (!shortDescription.trim()) {
      showError("Short description is required");
      return;
    }

    setIsSaving(true);
    try {
      await updatePublicSchema(
        diagramId,
        shortDescription,
        detailedDescription,
        selectedHashtags,
        queries
      );

      const updatedData = {
        shortDescription,
        detailedDescription,
        hashTags: selectedHashtags,
        cannedQueries: queries,
      };

      setPublicDiagram({
        ...publicDiagram,
        ...updatedData
      });
      showSuccess("Public details updated successfully");
      onClose();
    } catch (err) {
      showError(err.message || "Failed to update public details");
    } finally {
      setIsSaving(false);
    }
  };

  return (
    <>
      <Dialog
        open={open}
        onClose={handleCloseAttempt}
        maxWidth="lg"
        fullWidth
        scroll="paper"
        PaperProps={{
          sx: { borderRadius: 3, minHeight: '60vh' }
        }}
      >
        <DialogTitle sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', p: 3 }}>
          <Box>
            <Typography variant="h5" fontWeight="800">Edit Diagram Details</Typography>
            <Typography variant="body2" color="text.secondary">Update your diagram's public presence</Typography>
          </Box>
          <IconButton onClick={handleCloseAttempt} size="small" sx={{ color: 'text.secondary' }}>
            <CloseIcon />
          </IconButton>
        </DialogTitle>
        <Divider />
        <DialogContent sx={{ p: 4 }}>
          {isLoading ? (
            <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', py: 10, gap: 2 }}>
              <CircularProgress size={40} />
              <Typography color="text.secondary">Loading public details...</Typography>
            </Box>
          ) : (
            <Grid container spacing={3} display="flex" flexDirection="column" >
              <Grid item xs={12}>
                <Box sx={{ mb: 2 }}>
                  <Typography variant="subtitle1" fontWeight="700" gutterBottom>Short Description</Typography>
                  <TextField
                    fullWidth
                    variant="outlined"
                    value={shortDescription}
                    onChange={(e) => setShortDescription(e.target.value)}
                    placeholder="A brief one-line description for the card"
                    helperText={`${shortDescription.length}/300 characters`}
                    inputProps={{ maxLength: 300 }}
                    sx={{ bgcolor: 'background.paper' }}
                  />
                </Box>
              </Grid>

              <Grid item xs={12}>
                <Box sx={{ mb: 2 }}>
                  <Typography variant="subtitle1" fontWeight="700" gutterBottom>Hashtags</Typography>
                  <HashtagInput
                    hashtags={allHashtags}
                    selectedHashtags={selectedHashtags}
                    onSelect={setSelectedHashtags}
                    allowCreation={true}
                  />
                </Box>
              </Grid>

              <Grid item xs={12}>
                <Box sx={{ mb: 2 }}>
                  <Typography variant="subtitle1" fontWeight="700" gutterBottom>Detailed Description (Markdown)</Typography>
                  <MarkdownEditor
                    value={detailedDescription}
                    onChange={setDetailedDescription}
                    placeholder="Write a comprehensive guide, documentation, or explanation..."
                  />
                </Box>
              </Grid>

              <Grid item xs={12}>
                <Box sx={{ mb: 2 }}>
                  <Typography variant="subtitle1" fontWeight="700" gutterBottom>DDL Reference</Typography>
                  <Typography variant="body2" color="text.secondary" sx={{ mb: 1.5 }}>
                    Reference the schema structure while writing queries.
                  </Typography>
                  <Paper
                    variant="outlined"
                    sx={{
                      p: 2,
                      bgcolor: 'grey.50',
                      maxHeight: '250px',
                      overflow: 'auto',
                      borderRadius: 2,
                      borderStyle: 'dashed'
                    }}
                  >
                    <Typography
                      variant="body2"
                      component="pre"
                      sx={{
                        whiteSpace: 'pre-wrap',
                        wordBreak: 'break-word',
                        fontFamily: 'JetBrains Mono, monospace',
                        fontSize: '0.75rem',
                        color: 'text.primary'
                      }}
                    >
                      {publicDiagram?.ddl || "No DDL available for this diagram."}
                    </Typography>
                  </Paper>
                </Box>
              </Grid>

              <Grid item xs={12}>
                <Box sx={{ mb: 2 }}>
                  <Typography variant="subtitle1" fontWeight="700" gutterBottom>Canned Queries</Typography>
                  <Typography variant="body2" color="text.secondary" sx={{ mb: 1.5 }}>
                    Commonly used SQL transactions for this schema.
                  </Typography>
                  <QueryBuilder
                    queries={queries}
                    onChange={setQueries}
                  />
                </Box>
              </Grid>
            </Grid>
          )}
        </DialogContent>
        <Divider />
        <DialogActions sx={{ p: 3, px: 4, gap: 1 }}>
          <Button onClick={handleCloseAttempt} variant="text" color="inherit" disabled={isSaving} sx={{ fontWeight: 600 }}>
            Discard Changes
          </Button>
          <Button
            onClick={handleSave}
            variant="contained"
            disabled={isSaving || !hasChanges || isLoading}
            startIcon={isSaving ? <CircularProgress size={20} color="inherit" /> : <SaveIcon />}
            sx={{
              px: 4,
              py: 1.2,
              borderRadius: 2,
              fontWeight: 700,
              boxShadow: '0 4px 12px rgba(25, 118, 210, 0.2)'
            }}
          >
            {isSaving ? "Saving..." : "Save Changes"}
          </Button>
        </DialogActions>
      </Dialog>

      <ConfirmationModal
        isOpen={confirmCloseOpen}
        onConfirm={() => {
          setConfirmCloseOpen(false);
          onClose();
        }}
        onCancel={() => setConfirmCloseOpen(false)}
        title="Unsaved Changes"
        message="You have unsaved changes that will be lost. Are you sure you want to close?"
        confirmText="Close Anyway"
        cancelText="Keep Editing"
        confirmButtonStyle="danger"
      />
    </>
  );
}
