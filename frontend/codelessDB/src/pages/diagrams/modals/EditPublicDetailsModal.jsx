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

  const [isSaving, setIsSaving] = useState(false);
  const [confirmCloseOpen, setConfirmCloseOpen] = useState(false);
  const [hasChanges, setHasChanges] = useState(false);

  useEffect(() => {
    if (open && diagramId) {
      const loadPublicDiagram = async () => {
        const data = await getPublicDiagram(diagramId);
        console.log(data);
        setPublicDiagram(data);
        setShortDescription(data.shortDescription || "");
        setDetailedDescription(data.detailedDescription || "");
        setSelectedHashtags(data.hashTags || []);
        setQueries(data.cannedQueries || []);
        setHasChanges(false);
      }
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
      JSON.stringify(selectedHashtags) !== JSON.stringify(publicDiagram.hashtags || []) ||
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
      setPublicDiagram({
        ...publicDiagram,
        shortDescription,
        detailedDescription,
        hashtags: selectedHashtags,
        cannedQueries: queries,
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
      >
        <DialogTitle sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <Typography variant="h6" fontWeight="bold">Edit Public Details</Typography>
          <IconButton onClick={handleCloseAttempt} size="small">
            <CloseIcon />
          </IconButton>
        </DialogTitle>
        <Divider />
        <DialogContent sx={{ p: 4 }}>
          <Grid container spacing={2} sx={{ display: 'flex', flexDirection: 'column' }}>
            {/* Short Description & Hashtags */}
            <Grid item xs={12} md={6}>
              <Typography variant="subtitle1" fontWeight="bold" gutterBottom>Short Description</Typography>
              <TextField
                fullWidth
                variant="outlined"
                value={shortDescription}
                onChange={(e) => setShortDescription(e.target.value)}
                placeholder="A brief one-line description"
                helperText={`${shortDescription.length}/300 characters`}
                inputProps={{ maxLength: 300 }}
              />
            </Grid>
            <Grid item xs={12} md={6} mb={-3}>
              <Typography variant="subtitle1" fontWeight="bold" gutterBottom>Hashtags</Typography>
              <HashtagInput
                hashtags={allHashtags}
                selectedHashtags={selectedHashtags}
                onSelect={setSelectedHashtags}
                allowCreation={true}
              />
            </Grid>

            {/* Detailed Description */}
            <Grid item xs={12}>
              <Typography variant="subtitle1" fontWeight="bold" gutterBottom sx={{ mb: -1 }}>Detailed Description</Typography>
              <MarkdownEditor
                value={detailedDescription}
                onChange={setDetailedDescription}
                placeholder="Write a comprehensive description..."
              />
            </Grid>

            {/* DDL Reference */}
            <Grid item xs={12}>
              <Typography variant="subtitle1" fontWeight="bold" gutterBottom>DDL Reference</Typography>
              <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                Use this DDL as a reference for writing your canned queries.
              </Typography>
              <Paper
                variant="outlined"
                sx={{
                  p: 2,
                  bgcolor: 'action.hover',
                  maxHeight: '300px',
                  overflow: 'auto',
                  width: '100%',
                }}
              >
                <Typography
                  variant="body2"
                  component="pre"
                  sx={{
                    whiteSpace: 'pre-wrap',
                    wordBreak: 'break-word',
                    fontFamily: 'monospace',
                    fontSize: '0.875rem'
                  }}
                >
                  {publicDiagram?.ddl || "No DDL available for this diagram."}
                </Typography>
              </Paper>
            </Grid>

            {/* Canned Queries */}
            <Grid item xs={12}>
              <Typography variant="subtitle1" fontWeight="bold" gutterBottom>Predefined Transactions (Canned Queries)</Typography>
              <QueryBuilder
                queries={queries}
                onChange={setQueries}
              />
            </Grid>
          </Grid>
        </DialogContent>
        <Divider />
        <DialogActions sx={{ p: 2, px: 4 }}>
          <Button onClick={handleCloseAttempt} variant="outlined" disabled={isSaving}>
            Close
          </Button>
          <Button
            onClick={handleSave}
            variant="contained"
            disabled={isSaving || !hasChanges}
            startIcon={isSaving ? <CircularProgress size={20} /> : <SaveIcon />}
            sx={{ px: 4 }}
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
        message="You have unsaved changes. Are you sure you want to close? Your changes will be lost."
        confirmText="Close Anyway"
        cancelText="Stay"
        confirmButtonStyle="danger"
      />
    </>
  );
}
