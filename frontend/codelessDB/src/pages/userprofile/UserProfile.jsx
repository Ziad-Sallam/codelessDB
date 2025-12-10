import { useState, useEffect } from "react";
import {
  Avatar, Box, Button, Card, CardContent, TextField, Typography, Alert, Snackbar,
  CircularProgress, IconButton, Dialog, DialogTitle, DialogContent, DialogActions,
  Menu, MenuItem, Switch
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
  const [profileData, setProfileData] = useState({ username: "", email: "", picture: "", createdAt: "", bio: "", publicProfile: "" });
  const [editMode, setEditMode] = useState({});
  const [tempData, setTempData] = useState({});
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [snackbar, setSnackbar] = useState({ open: false, message: "", severity: "success" });
  const [showUrlDialog, setShowUrlDialog] = useState(false);
  const [imageUrl, setImageUrl] = useState("");
  const [anchorEl, setAnchorEl] = useState(null);

  useEffect(() => {
    if (!user) return;

    setProfileData({
      username: user.username,
      email: user.email,
      picture: user.picture,
      createdAt: user.createdAt,
      bio: user.bio || "",
      publicProfile: user.publicProfile || "",
      profileWebsiteUrl: user.profileWebsiteUrl || "",
    });
    setLoading(false);

    const resetSuccess = localStorage.getItem("passwordResetSuccess");
    if (resetSuccess === "true") {
      showSnackbar("Password reset successfully!", "success");
      localStorage.removeItem("passwordResetSuccess");
    }
  }, [user]);

  const showSnackbar = (message, severity) => setSnackbar({ open: true, message, severity });
  const handleCloseSnackbar = () => setSnackbar({ ...snackbar, open: false });

  const handleEdit = (field) => {
    setEditMode({ ...editMode, [field]: true });
    setTempData({ ...tempData, [field]: profileData[field] });
  };

  const handleSave = async (field, value = null) => {
    setSaving(true);
    try {
      const newValue = value ?? tempData[field];
      await updateUserField(field, newValue);

      setProfileData((prev) => ({ ...prev, [field]: newValue }));
      setUser((prev) => ({
        ...prev,
        [field]: newValue
      }));

      setEditMode((prev) => ({ ...prev, [field]: false }));
      showSnackbar(`${field.charAt(0).toUpperCase() + field.slice(1)} updated successfully`, "success");
    } catch (error) {
      console.error(`Error updating ${field}:`, error);
      showSnackbar(error.response?.data?.message || `Error updating ${field}`, "error");
    } finally {
      setSaving(false);
    }
  };

  const handleCancel = (field) => {
    setEditMode({ ...editMode, [field]: false });
    setTempData({ ...tempData, [field]: "" });
  };

  const handleResetPassword = async () => {
    try {
      const token = await resetPassword(profileData.email);
      localStorage.setItem("token_for_reset", token);
      localStorage.setItem("email", profileData.email);
      localStorage.setItem("resetSource", "profile");

      navigate("/password-reset?flow=reset");

      // navigate("/register?flow=reset");
    } catch (error) {
      console.error("Error initiating password reset:", error);
      showSnackbar(error.response?.data?.message || "Error initiating password reset", "error");
    }
  };

  const handleCameraClick = (event) => setAnchorEl(event.currentTarget);
  const handleMenuClose = () => setAnchorEl(null);

  const handleFileSelect = async (event) => {
    const file = event.target.files[0];
    if (!file || !file.type.startsWith("image/") || file.size > 1024 * 1024) {
      return showSnackbar(!file ? "No file selected" : file.size > 1024 * 1024 ? "Image must be <1MB" : "Only image files allowed", "error");
    }
    try {
      showSnackbar("Uploading image...", "info");
      const url = await uploadToCloudinary(file, profileData.email);
      if (url) await handleSave("picture", url);
    } catch {
      showSnackbar("Error uploading picture", "error");
    }
    handleMenuClose();
  };

  const handleUrlSubmit = async () => {
    if (!imageUrl.trim()) return showSnackbar("Please enter a valid URL", "error");
    await handleSave("picture", imageUrl);
    setShowUrlDialog(false);
    setImageUrl("");
  };

  const handleUrlUpload = () => { setShowUrlDialog(true); handleMenuClose(); };
  const handleLogout = () => {
    localStorage.removeItem("authToken");
    setUser(null);
    navigate("/login");
  };

  const getInitials = () => profileData.username ? profileData.username.substring(0, 2).toUpperCase() : "U";

  const ProfileField = ({ label, field, icon: Icon, type = "text", editable = true }) => {
    const isEditing = editMode[field];
    const currentValue = profileData[field];

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
          <Box className="profile-field-edit" onClick={(e) => e.stopPropagation()}>
            <TextField
              fullWidth size="small" type={type} value={tempData[field]}
              onChange={(e) => setTempData({ ...tempData, [field]: e.target.value })} autoFocus
            />
            <Button variant="contained" size="small" onClick={() => handleSave(field)} disabled={saving} className="save-button">Save</Button>
            <Button variant="outlined" size="small" onClick={() => handleCancel(field)} className="cancel-button">Close</Button>
          </Box>
        ) : (
          <Box className="profile-field-value">
            <Typography variant="body1">{currentValue}</Typography>
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
      <Box className="profile-field-value"><Typography variant="body1">••••••••</Typography></Box>
    </Box>
  );

  if (loading) return <ThemeProvider theme={theme}><Box className="loading-container"><CircularProgress /></Box></ThemeProvider>;

  return (
    <ThemeProvider theme={theme}>
      <LeftPanel leftNav={leftNav} setLeftNav={setLeftNav} />
      <Box className="profile-layout">
        <Box component="main" className="profile-main">
          <Box sx={{ display: 'flex', justifyContent: 'flex-end', alignItems: 'center', padding: '8px 24px', backgroundColor: '#f6f8fb' }}>
            <Button variant="outlined" startIcon={<LogoutIcon />} onClick={handleLogout} sx={{ textTransform: 'none', borderColor: '#b71c1c', color: '#ffffff', background: 'linear-gradient(45deg, #e53935 30%, #b71c1c 90%)', borderRadius: '10px', marginRight: '50px', '&:hover': { borderColor: '#ff7961', background: 'linear-gradient(45deg, #d32f2f 30%, #7f0000 90%)', boxShadow: '0 4px 8px 3px rgba(127, 0, 0, .4)' } }}>Logout</Button>
          </Box>

          <Box className="profile-container">
            <Card className="profile-header-card">
              <CardContent className="profile-header-content">
                <Box className="profile-header-inner">
                  <Box className="profile-avatar-container">
                    <Avatar src={profileData.picture} className="profile-avatar">{!profileData.picture && getInitials()}</Avatar>
                    <input accept="image/*" style={{ display: "none" }} id="upload-photo" type="file" onChange={handleFileSelect} />
                    <IconButton onClick={handleCameraClick} className="camera-button" size="small"><CameraAltIcon fontSize="small" /></IconButton>
                    <Menu anchorEl={anchorEl} open={Boolean(anchorEl)} onClose={handleMenuClose}>
                      <MenuItem><label htmlFor="upload-photo" style={{ cursor: 'pointer', width: '100%' }}>Upload from Computer</label></MenuItem>
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
                <ProfileField label="Username" field="username" icon={PersonIcon} editable={true} />
                <ProfileField label="Email" field="email" icon={EmailIcon} type="email" editable={false} />
                <ProfileField label="Bio" field="bio" icon={PersonIcon} editable={true} multiline={true} rows={4} />

                <ProfileField label="Public Profile" field="publicProfile" icon={PersonIcon} editable={true} multiline={true} rows={2} />

                <ProfileField label="Website URL" field="profileWebsiteUrl" icon={LanguageIcon} editable={true} type="url" />

                <PasswordField />
              </CardContent>
            </Card>
          </Box>
        </Box>
      </Box>

      <Snackbar open={snackbar.open} autoHideDuration={4000} onClose={handleCloseSnackbar} anchorOrigin={{ vertical: "bottom", horizontal: "right" }}>
        <Alert onClose={handleCloseSnackbar} severity={snackbar.severity}>{snackbar.message}</Alert>
      </Snackbar>

      <Dialog open={showUrlDialog} onClose={() => setShowUrlDialog(false)} maxWidth="sm" fullWidth>
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
    </ThemeProvider>
  );
}
