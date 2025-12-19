import { useState, useEffect, useRef } from "react";
import {
  Avatar, Box, Button, Card, CardContent, TextField, Typography, Alert, Snackbar,
  CircularProgress, IconButton, Dialog, DialogTitle, DialogContent, DialogActions,
  Menu, MenuItem
} from "@mui/material";
import { ThemeProvider } from "@mui/material/styles";
import { useNavigate } from "react-router-dom";

import EditIcon from "@mui/icons-material/Edit";
import PersonIcon from "@mui/icons-material/Person";
import EmailIcon from "@mui/icons-material/Email";
import LockIcon from "@mui/icons-material/Lock";
import CameraAltIcon from "@mui/icons-material/CameraAlt";
import LogoutIcon from "@mui/icons-material/Logout";
import LanguageIcon from "@mui/icons-material/Language";

import LeftPanel from "../../components/LeftPanel.jsx";
import theme from "../../theme.js";
import { useAuth } from "../../components/AuthProvider.jsx";
import { uploadToCloudinary } from "../../uploadToCloudinary.js";
import { updateUserField, resetPassword } from "./userFetch.js";

import "./UserProfile.css";

export default function UserProfile() {
  const navigate = useNavigate();
  const { user, setUser } = useAuth();

  const [leftNav, setLeftNav] = useState("profile");
  const [profileData, setProfileData] = useState({
    username: "",
    email: "",
    picture: "",
    createdAt: "",
    bio: "",
    publicProfile: "",
    profileWebsiteUrl: ""
  });
  const [editMode, setEditMode] = useState({});
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [snackbar, setSnackbar] = useState({ open: false, message: "", severity: "success" });
  const [showUrlDialog, setShowUrlDialog] = useState(false);
  const [imageUrl, setImageUrl] = useState("");
  const [anchorEl, setAnchorEl] = useState(null);

  const fieldRefs = useRef({});
  const fileInputRef = useRef(null);

  useEffect(() => {
    if (!user) return;
    setProfileData({
      username: user.username,
      email: user.email,
      picture: user.picture,
      createdAt: user.createdAt,
      bio: user.bio || "",
      publicProfile: user.publicProfile || "",
      profileWebsiteUrl: user.profileWebsiteUrl || ""
    });
    setLoading(false);

    const resetSuccess = localStorage.getItem("passwordResetSuccess");
    if (resetSuccess === "true") {
      showSnackbar("Password reset successfully!", "success");
      localStorage.removeItem("passwordResetSuccess");
    }
  }, [user]);

  useEffect(() => {
    Object.keys(editMode).forEach((field) => {
      if (editMode[field]) {
        const el = fieldRefs.current[field];
        if (el && typeof el.focus === "function") {
          setTimeout(() => el.focus && el.focus(), 80);
        }
      }
    });
  }, [editMode]);

  const showSnackbar = (message, severity = "success") => setSnackbar({ open: true, message, severity });
  const handleCloseSnackbar = () => setSnackbar({ ...snackbar, open: false });

  const handleEdit = (field) => {
    setEditMode((p) => ({ ...p, [field]: true }));
  };

  const validateUrl = (raw) => {
    if (!raw) return false;
    try {
      const candidate = raw.startsWith("http://") || raw.startsWith("https://") ? raw : `https://${raw}`;
      new URL(candidate);
      return candidate;
    } catch {
      return false;
    }
  };

  const friendlyLabel = (field) => {
    switch (field) {
      case "profileWebsiteUrl": return "Website URL";
      case "publicProfile": return "Public Profile";
      default: return field.charAt(0).toUpperCase() + field.slice(1);
    }
  };

  const handleSave = async (field) => {
    const ref = fieldRefs.current[field];
    const rawValue = ref ? ref.value : profileData[field];
    const newValue = (rawValue !== undefined && rawValue !== null) ? String(rawValue).trim() : "";

    if (field === "profileWebsiteUrl" && newValue) {
      const normalized = validateUrl(newValue);
      if (!normalized) {
        return showSnackbar("Please enter a valid website URL (e.g. https://example.com)", "error");
      }
      return await saveField(field, normalized);
    }

    return await saveField(field, newValue);
  };

  const saveField = async (field, value) => {
    setSaving(true);
    try {
      await updateUserField(field, value);
      setProfileData((p) => ({ ...p, [field]: value }));
      setUser((u) => u ? ({ ...u, [field]: value }) : u);
      setEditMode((p) => ({ ...p, [field]: false }));
      showSnackbar(`${friendlyLabel(field)} updated`, "success");
    } catch (err) {
      console.error("Error updating field", field, err);
      const message = err?.response?.data?.message || `Error updating ${friendlyLabel(field)}`;
      showSnackbar(message, "error");
    } finally {
      setSaving(false);
    }
  };

  const handleCancel = (field) => {
    setEditMode((p) => ({ ...p, [field]: false }));
    const ref = fieldRefs.current[field];
    if (ref) ref.value = profileData[field] ?? "";
  };

  const handleResetPassword = async () => {
    try {
      const token = await resetPassword(profileData.email);
      localStorage.setItem("token_for_reset", token);
      localStorage.setItem("email", profileData.email);
      localStorage.setItem("resetSource", "profile");
      navigate("/password-reset?flow=reset");
    } catch (err) {
      console.error("Error initiating password reset", err);
      showSnackbar(err?.response?.data?.message || "Error initiating password reset", "error");
    }
  };

  // Menu handling
  const handleCameraClick = (e) => setAnchorEl((prev) => (prev ? null : e.currentTarget));
  const handleMenuClose = () => setAnchorEl(null);

  const handleUploadFromComputer = () => {
    handleMenuClose(); // close menu first
    requestAnimationFrame(() => {
      fileInputRef.current?.click();
    });
  };

  const handleFileSelect = async (e) => {
    const file = e.target.files && e.target.files[0];
    if (!file) return showSnackbar("No file selected", "error");
    if (!file.type.startsWith("image/")) return showSnackbar("Only image files allowed", "error");
    if (file.size > 1024 * 1024) return showSnackbar("Image must be < 1MB", "error");
    try {
      showSnackbar("Uploading image...", "info");
      const url = await uploadToCloudinary(file, profileData.email);
      if (url) await saveField("picture", url);
    } catch (err) {
      console.error(err);
      showSnackbar("Error uploading picture", "error");
    } finally {
      if (fileInputRef.current) fileInputRef.current.value = "";
    }
  };

  const handleUrlSubmit = async () => {
    if (!imageUrl.trim()) return showSnackbar("Please enter a valid URL", "error");
    const normalized = validateUrl(imageUrl.trim());
    if (!normalized) return showSnackbar("Please enter a valid website URL for the image", "error");
    await saveField("picture", normalized);
    setShowUrlDialog(false);
    setImageUrl("");
  };

  const handleUrlUpload = () => {
    handleMenuClose(); // close menu first
    setShowUrlDialog(true);
  };

  const handleLogout = () => {
    localStorage.removeItem("authToken");
    setUser(null);
    navigate("/login");
  };

  // Close menu when clicking outside
  useEffect(() => {
    const handleClickOutside = (event) => {
      if (anchorEl && !anchorEl.contains(event.target)) {
        handleMenuClose();
      }
    };
    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, [anchorEl]);

  const getInitials = () => profileData.username ? profileData.username.slice(0, 2).toUpperCase() : "U";

  const ProfileField = ({ label, field, icon: Icon, type = "text", editable = true, multiline = false, rows = 1, ...textFieldProps }) => {
    const isEditing = !!editMode[field];
    const value = profileData[field];

    return (
      <Box className="profile-field" onClick={() => !isEditing && editable && handleEdit(field)}>
        <Box className="profile-field-header">
          <Box className="profile-field-label">
            <Icon sx={{ fontSize: 18, color: "text.secondary", mr: 1 }} />
            <Typography variant="body2" sx={{ fontWeight: 500, color: "text.secondary" }}>{label}</Typography>
          </Box>
          {!isEditing && editable && (
            <IconButton size="small" onClick={(e) => { e.stopPropagation(); handleEdit(field); }} className="edit-button">
              <EditIcon fontSize="small" />
            </IconButton>
          )}
        </Box>

        {isEditing ? (
          <Box className="profile-field-edit" sx={{ display: 'flex', flexDirection: 'column', gap: 1 }}>
            <TextField
              fullWidth
              size="small"
              type={type}
              defaultValue={value ?? ""}
              inputRef={(el) => (fieldRefs.current[field] = el)}
              multiline={multiline}
              rows={rows}
              {...textFieldProps}
              onKeyDown={(e) => {
                if (e.key === "Enter" && !e.shiftKey) {
                  e.preventDefault();
                  handleSave(field);
                }
              }}
            />
            <Box sx={{ display: 'flex', gap: 1, mt: 1 }}>
              <Button variant="contained" size="small" onClick={() => handleSave(field)} disabled={saving} className="save-button">Save</Button>
              <Button variant="outlined" size="small" onClick={() => handleCancel(field)} className="cancel-button">Close</Button>
            </Box>
          </Box>
        ) : (
          <Box className="profile-field-value">
            {value ? (
              <Typography variant="body1" sx={{ wordBreak: "break-word" }}>{value}</Typography>
            ) : editable ? (
              <Button size="small" variant="outlined" onClick={(e) => { e.stopPropagation(); handleEdit(field); }}>
                Add {label}
              </Button>
            ) : (
              <Typography variant="body1" color="text.disabled">Not set</Typography>
            )}
          </Box>
        )}
      </Box>
    );
  };

  const PasswordField = () => (
    <Box className="profile-field">
      <Box className="profile-field-header">
        <Box className="profile-field-label">
          <LockIcon sx={{ fontSize: 18, color: "text.secondary", mr: 1 }} />
          <Typography variant="body2" sx={{ fontWeight: 500, color: "text.secondary" }}>Password</Typography>
        </Box>
        <Button size="small" onClick={handleResetPassword} className="reset-password-button" variant="text" sx={{ textTransform: 'none', fontSize: '0.875rem' }}>Reset Password</Button>
      </Box>
    </Box>
  );

  if (loading) return <ThemeProvider theme={theme}><Box className="loading-container"><CircularProgress /></Box></ThemeProvider>;

  return (
    <ThemeProvider theme={theme}>
      <Box className="profile-layout">
        <LeftPanel leftNav={leftNav} setLeftNav={setLeftNav} />

        <Box component="main" className="profile-main">
          <Box className="profile-container">
            <Box sx={{ display: 'flex', justifyContent: 'flex-end', mb: 2 }}>
              <Button className="logout-button" startIcon={<LogoutIcon />} onClick={handleLogout}>Logout</Button>
            </Box>

            <Card className="profile-header-card">
              <CardContent className="profile-header-content">
                <Box className="profile-header-inner">
                  <Box className="profile-avatar-container">
                    <Avatar src={profileData.picture} className="profile-avatar">{!profileData.picture && getInitials()}</Avatar>

                    <input ref={fileInputRef} accept="image/*" style={{ display: "none" }} type="file" onChange={handleFileSelect} />
                    <IconButton onClick={handleCameraClick} className="camera-button" size="small">
                      <CameraAltIcon fontSize="small" />
                    </IconButton>

                    <Menu anchorEl={anchorEl} open={Boolean(anchorEl)} onClose={handleMenuClose} disablePortal={false}>
                      <MenuItem onClick={handleUploadFromComputer}>Upload from Computer</MenuItem>
                      <MenuItem onClick={handleUrlUpload}>Upload from URL</MenuItem>
                    </Menu>
                  </Box>

                  <Box className="profile-header-info">
                    <Typography variant="h4" className="profile-username">{profileData.username}</Typography>
                    {profileData.createdAt && <Typography variant="body2" className="profile-member-since">Member since {new Date(profileData.createdAt).toLocaleDateString()}</Typography>}
                  </Box>
                </Box>
              </CardContent>
            </Card>

            <Card className="profile-info-card">
              <CardContent className="profile-info-content">
                <Typography variant="h6" className="profile-info-title">Basic Info</Typography>
                <ProfileField label="Username" field="username" icon={PersonIcon} editable={false} />
                <ProfileField label="Email" field="email" icon={EmailIcon} type="email" editable={false} />
                <ProfileField label="Bio" field="bio" icon={PersonIcon} editable={true} multiline rows={4} type="text" inputProps={{ maxLength: 300 }} />
                <ProfileField label="Public Profile Name" field="publicProfile" icon={PersonIcon} editable={true} rows={1} inputProps={{ maxLength: 50 }} />
                <ProfileField label="Website URL" field="profileWebsiteUrl" icon={LanguageIcon} editable={true} type="url" />
                <PasswordField />
              </CardContent>
            </Card>
          </Box>
        </Box>

        <Snackbar open={snackbar.open} autoHideDuration={4000} onClose={handleCloseSnackbar} anchorOrigin={{ vertical: "bottom", horizontal: "right" }}>
          <Alert onClose={handleCloseSnackbar} severity={snackbar.severity} sx={{ width: '100%' }}>{snackbar.message}</Alert>
        </Snackbar>

        <Dialog open={showUrlDialog} onClose={() => { setShowUrlDialog(false); setImageUrl(""); }} maxWidth="sm" fullWidth>
          <DialogTitle>Upload Profile Picture from URL</DialogTitle>
          <DialogContent>
            <TextField autoFocus margin="dense" label="Image URL" type="url" fullWidth variant="outlined" value={imageUrl} onChange={(e) => setImageUrl(e.target.value)} placeholder="https://example.com/image.jpg" sx={{ mt: 2 }} />
            <Typography variant="caption" color="text.secondary" sx={{ mt: 1, display: 'block' }}>Enter a direct link to an image (jpg, png, gif, etc.)</Typography>
          </DialogContent>
          <DialogActions>
            <Button onClick={() => { setShowUrlDialog(false); setImageUrl(""); }}>Cancel</Button>
            <Button onClick={handleUrlSubmit} variant="contained">Upload</Button>
          </DialogActions>
        </Dialog>
      </Box>
    </ThemeProvider>
  );
}
