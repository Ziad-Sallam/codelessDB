import { useState, useEffect } from "react";
import {
  Avatar, Box, Button, Card, CardContent, TextField, Typography, Alert, Snackbar, CircularProgress, IconButton, Dialog, DialogTitle, DialogContent, DialogActions, Menu, MenuItem,
} from "@mui/material";
import EditIcon from "@mui/icons-material/Edit";
import PersonIcon from "@mui/icons-material/Person";
import EmailIcon from "@mui/icons-material/Email";
import LockIcon from "@mui/icons-material/Lock";
import CameraAltIcon from "@mui/icons-material/CameraAlt";
import { ThemeProvider } from "@mui/material/styles";
import { useNavigate } from "react-router-dom";
import axios from "axios";
import LeftPanel from "../diagrams/LeftPanel.jsx";
import "./UserProfile.css";
import theme from '../../theme.js';

const apiClient = axios.create({
  baseURL: "http://localhost:8080",
  headers: {
    "Content-Type": "application/json",
  },
});

apiClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem("authToken");
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401 || error.response?.status === 403) {
      localStorage.removeItem("authToken");
      window.location.href = "/login";
    }
    return Promise.reject(error);
  }
);

export default function UserProfile() {
  const navigate = useNavigate();
  const [leftNav, setLeftNav] = useState("profile");
  const [username, setUsername] = useState("");
  const [email, setEmail] = useState("");
  const [picture, setPicture] = useState("");
  const [createdAt, setCreatedAt] = useState("");
  const [editMode, setEditMode] = useState({ username: false });
  const [tempData, setTempData] = useState({ username: "" });
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [snackbar, setSnackbar] = useState({ open: false, message: "", severity: "success" });
  const [showUrlDialog, setShowUrlDialog] = useState(false);
  const [imageUrl, setImageUrl] = useState("");
  const [anchorEl, setAnchorEl] = useState(null);


  useEffect(() => {
    const token = localStorage.getItem("authToken");

    if (!token) {
      navigate("/login");
      return;
    }

    fetchUserInfo();

    const resetSuccess = localStorage.getItem('passwordResetSuccess');
    if (resetSuccess === 'true') {
      showSnackbar("Password reset successfully!", "success");
      localStorage.removeItem('passwordResetSuccess');
    }
  }, []);

  const fetchUserInfo = async () => {
    try {
      const response = await apiClient.get("/user/info");

      const data = response.data;
      setUsername(data.username || "");
      setEmail(data.email || "");
      setPicture(data.picture || "");
      setCreatedAt(data.createdAt || "");

    } catch (error) {
      console.error("Error fetching user info:", error);

      if (error.code === "ERR_NETWORK" || error.message.includes("Network Error")) {
        showSnackbar("Network error. Please check your connection.", "error");
      } else if (error.response?.status === 401 || error.response?.status === 403) {
        showSnackbar("Session expired. Please login again.", "error");
        navigate("/login");
      } else {
        showSnackbar(error.response?.data?.message || "Error loading profile", "error");
      }
    } finally {
      setLoading(false);
    }
  };

  const handleEdit = (field) => {
    if (editMode[field]) {
      return;
    }
    setEditMode({ ...editMode, [field]: true });
    setTempData({
      ...tempData,
      [field]: eval(field)
    });
  };

  const handleSave = async (field) => {
    setSaving(true);

    try {

      const updateDto = {
        [field]: tempData[field]
      };

      const response = await apiClient.put("/user/update", updateDto);

      if (response.status === 200) {
        if (field === "username") setUsername(tempData[field]);

        setEditMode({ ...editMode, [field]: false });
        showSnackbar(`${field.charAt(0).toUpperCase() + field.slice(1)} updated successfully`, "success");
      }

    } catch (error) {
      console.error(`Error updating ${field}:`, error);

      if (error.code === "ERR_NETWORK" || error.message.includes("Network Error")) {
        showSnackbar("Network error. Please check your connection.", "error");
      } else if (error.response?.status === 401 || error.response?.status === 403) {
        showSnackbar("Session expired. Please login again.", "error");
        navigate("/login");
      } else {
        showSnackbar(error.response?.data?.message || `Error updating ${field}`, "error");
      }
    } finally {
      setSaving(false);
    }
  };

  const handleCancel = (field) => {
    setEditMode({ ...editMode, [field]: false });
    setTempData({ ...tempData, [field]: "" });
  };

  const handleResetPassword = () => {
    const token = localStorage.getItem("authToken");

    localStorage.setItem("token for_reset", token);
    localStorage.setItem("otpPurpose", "reset");
    localStorage.setItem("email", email);
    localStorage.setItem("resetSource", "profile");

    navigate("/reset");
  };

  const handleCameraClick = (event) => {
    setAnchorEl(event.currentTarget);
  };

  const handleMenuClose = () => {
    setAnchorEl(null);
  };

  const handleUrlSubmit = async () => {
    if (!imageUrl.trim()) {
      showSnackbar("Please enter a valid URL", "error");
      return;
    }

    try {

      const updateDto = {
        picture: imageUrl
      };
      console.log("Updating profile picture with URL:", imageUrl);
      console.log(updateDto);

      const response = await apiClient.put("/user/update", updateDto);

      if (response.status === 200) {
        setPicture(imageUrl);
        showSnackbar("Profile picture updated successfully", "success");
        setShowUrlDialog(false);
        setImageUrl("");
      }

    } catch (error) {
      console.error("Error uploading picture:", error);

      if (error.code === "ERR_NETWORK" || error.message.includes("Network Error")) {
        showSnackbar("Network error. Please check your connection.", "error");
      } else if (error.response?.status === 401 || error.response?.status === 403) {
        showSnackbar("Session expired. Please login again.", "error");
        navigate("/login");
      } else {
        showSnackbar(error.response?.data?.message || "Error uploading picture", "error");
      }
    }
  };

  const showSnackbar = (message, severity) => {
    setSnackbar({ open: true, message, severity });
  };

  const handleCloseSnackbar = () => {
    setSnackbar({ ...snackbar, open: false });
  };

  const getInitials = () => {
    return username ? username.substring(0, 2).toUpperCase() : "U";
  };

  const ProfileField = ({ label, field, icon: Icon, type = "text", editable = true }) => {
    const isEditing = editMode[field];
    const currentValue = eval(field);

    return (
      <Box className="profile-field" onClick={() => !isEditing && editable && handleEdit(field)}>
        <Box className="profile-field-header">
          <Box className="profile-field-label">
            <Icon sx={{ fontSize: 18, color: "text.secondary", mr: 1 }} />
            <Typography variant="body2" sx={{ fontWeight: 500, color: "text.secondary" }}>
              {label}
            </Typography>
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
              fullWidth
              size="small"
              type={type}
              value={tempData[field]}
              onChange={(e) => setTempData({ ...tempData, [field]: e.target.value })}
              autoFocus
            />
            <Button
              variant="contained"
              size="small"
              onClick={() => handleSave(field)}
              disabled={saving}
              className="save-button"
            >
              Save
            </Button>
            <Button
              variant="outlined"
              size="small"
              onClick={() => handleCancel(field)}
              className="cancel-button"
            >
              Close
            </Button>
          </Box>
        ) : (
          <Box className="profile-field-value">
            <Typography variant="body1">
              {currentValue}
            </Typography>
          </Box>
        )}
      </Box>
    );
  };

  const PasswordField = () => {
    return (
      <Box className="profile-field">
        <Box className="profile-field-header">
          <Box className="profile-field-label">
            <LockIcon sx={{ fontSize: 18, color: "text.secondary", mr: 1 }} />
            <Typography variant="body2" sx={{ fontWeight: 500, color: "text.secondary" }}>
              Password
            </Typography>
          </Box>
          <Button
            size="small"
            onClick={handleResetPassword}
            className="reset-password-button"
            variant="text"
            sx={{ textTransform: 'none', fontSize: '0.875rem' }}
          >
            Reset Password
          </Button>
        </Box>

        <Box className="profile-field-value">
          <Typography variant="body1">
            ••••••••
          </Typography>
        </Box>
      </Box>
    );
  };

  const handleFileSelect = async (event) => {
    const file = event.target.files[0];
    if (file) {
      // const url = await uploadToCloudinary(file);

      setImageUrl(url)

      handleUrlSubmit()
    }

    handleMenuClose();
  };

  const handleUrlUpload = () => {
    setShowUrlDialog(true);
    handleMenuClose();
  };

  if (loading) {
    return (
      <ThemeProvider theme={theme}>
        <Box className="loading-container">
          <CircularProgress />
        </Box>
      </ThemeProvider>
    );
  }

  return (
    <ThemeProvider theme={theme}>
      <LeftPanel leftNav={leftNav} setLeftNav={setLeftNav} />

      <Box className="profile-layout">
        <Box component="main" className="profile-main">

          <Box className="profile-container">
            {/* Header Card with Gradient */}
            <Card className="profile-header-card">
              <CardContent className="profile-header-content">
                <Box className="profile-header-inner">
                  <Box className="profile-avatar-container">
                    <Avatar
                      src={picture}
                      className="profile-avatar"
                    >
                      {!picture && getInitials()}
                    </Avatar>
                    {/* Hidden file input */}
                    <input
                      accept="image/*"
                      style={{ display: "none" }}
                      id="upload-photo"
                      type="file"
                      onChange={handleFileSelect}
                    />

                    {/* Camera button */}
                    <IconButton
                      onClick={handleCameraClick}
                      className="camera-button"
                      size="small"
                    >
                      <CameraAltIcon fontSize="small" />
                    </IconButton>

                    {/* Menu for choosing upload method */}
                    <Menu
                      anchorEl={anchorEl}
                      open={Boolean(anchorEl)}
                      onClose={handleMenuClose}
                    >
                      <MenuItem>
                        <label htmlFor="upload-photo" style={{ cursor: 'pointer', width: '100%' }}>
                          Upload from Computer
                        </label>
                      </MenuItem>
                      <MenuItem onClick={handleUrlUpload}>
                        Upload from URL
                      </MenuItem>
                    </Menu>

                  </Box>

                  <Box className="profile-header-info">
                    <Typography variant="h4" className="profile-username">
                      {username}
                    </Typography>
                    {createdAt && (
                      <Typography variant="body2" className="profile-member-since">
                        Member since {new Date(createdAt).toLocaleDateString()}
                      </Typography>
                    )}
                  </Box>
                </Box>
              </CardContent>
            </Card>

            {/* Profile Information Card */}
            <Card className="profile-info-card">
              <CardContent className="profile-info-content">
                <Typography variant="h6" className="profile-info-title">
                  Basic Info
                </Typography>

                <ProfileField label="Username" field="username" icon={PersonIcon} editable={true} />
                <ProfileField label="Email" field="email" icon={EmailIcon} type="email" editable={false} />
                <PasswordField />
              </CardContent>
            </Card>
          </Box>
        </Box>
      </Box>

      <Snackbar
        open={snackbar.open}
        autoHideDuration={4000}
        onClose={handleCloseSnackbar}
        anchorOrigin={{ vertical: "bottom", horizontal: "right" }}
      >
        <Alert
          onClose={handleCloseSnackbar}
          severity={snackbar.severity}
        >
          {snackbar.message}
        </Alert>
      </Snackbar>

      {/* URL Upload Dialog */}
      <Dialog open={showUrlDialog} onClose={() => setShowUrlDialog(false)} maxWidth="sm" fullWidth>
        <DialogTitle>Upload Profile Picture from URL</DialogTitle>
        <DialogContent>
          <TextField
            autoFocus
            margin="dense"
            label="Image URL"
            type="url"
            fullWidth
            variant="outlined"
            value={imageUrl}
            onChange={(e) => setImageUrl(e.target.value)}
            placeholder="https://example.com/image.jpg"
            sx={{ mt: 2 }}
          />
          <Typography variant="caption" color="text.secondary" sx={{ mt: 1, display: 'block' }}>
            Enter a direct link to an image (jpg, png, gif, etc.)
          </Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => { setShowUrlDialog(false); setImageUrl(""); }}>
            Cancel
          </Button>
          <Button onClick={handleUrlSubmit} variant="contained">
            Upload
          </Button>
        </DialogActions>
      </Dialog>
    </ThemeProvider>
  );
}