// UserProfile.jsx
import React, { useState, useEffect } from "react";
import {
  Avatar,
  Box,
  Button,
  Card,
  CardContent,
  Divider,
  TextField,
  Typography,
  Alert,
  Snackbar,
  CircularProgress,
  Select,
  MenuItem,
  FormControl,
  InputLabel,
} from "@mui/material";
import { createTheme, ThemeProvider } from "@mui/material/styles";
import LeftPanel from "../diagrams/LeftPanel.jsx";
import TopBar from "../diagrams/TopBar.jsx";
import "./userprofile.css";

const APP_PRIMARY = "#0b3d91";

const theme = createTheme({
  palette: {
    primary: { main: APP_PRIMARY },
    background: { default: "#f6f8fb", paper: "#ffffff" },
    text: { primary: "#0f172a" },
  },
  typography: { fontFamily: "Inter, Roboto, Arial, sans-serif" },
});

export default function UserProfile() {
  const [leftNav, setLeftNav] = useState("profile");
  const [notifications] = useState([]);
  
  // Profile form state
  const [username, setUsername] = useState("");
  const [email, setEmail] = useState("");
  const [picture, setPicture] = useState("");
  const [createdAt, setCreatedAt] = useState("");
  const [server, setServer] = useState("");
  
  // Available servers list
  const [servers, setServers] = useState([
    { id: "us-east", name: "US East" },
    { id: "us-west", name: "US West" },
    { id: "eu-central", name: "EU Central" },
    { id: "asia-pacific", name: "Asia Pacific" },
  ]);
  
  // Password fields
  const [currentPassword, setCurrentPassword] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  
  // UI state
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [snackbar, setSnackbar] = useState({ open: false, message: "", severity: "success" });
  const [errors, setErrors] = useState({});

  // Profile photo upload
  const [selectedFile, setSelectedFile] = useState(null);
  const [previewUrl, setPreviewUrl] = useState("");

  useEffect(() => {
    fetchUserInfo();
  }, []);

  const fetchUserInfo = async () => {
    try {
      const response = await fetch("/api/user/info", {
        headers: {
          "Authorization": `Bearer ${localStorage.getItem("token")}`,
        },
      });
      
      if (response.ok) {
        const data = await response.json();
        setUsername(data.username || "");
        setEmail(data.email || "");
        setPicture(data.picture || "");
        setCreatedAt(data.createdAt || "");
        setServer(data.server || "us-east");
        setPreviewUrl(data.picture || "");
      } else {
        showSnackbar("Failed to load user info", "error");
      }
    } catch (error) {
      console.error("Error fetching user info:", error);
      showSnackbar("Error loading profile", "error");
    } finally {
      setLoading(false);
    }
  };

  const handleFileSelect = (event) => {
    const file = event.target.files[0];
    if (file) {
      setSelectedFile(file);
      const reader = new FileReader();
      reader.onloadend = () => {
        setPreviewUrl(reader.result);
      };
      reader.readAsDataURL(file);
    }
  };

  const uploadProfilePicture = async () => {
    if (!selectedFile) return picture;

    const formData = new FormData();
    formData.append("file", selectedFile);

    try {
      const response = await fetch("/api/user/upload-picture", {
        method: "POST",
        headers: {
          "Authorization": `Bearer ${localStorage.getItem("token")}`,
        },
        body: formData,
      });

      if (response.ok) {
        const data = await response.json();
        return data.pictureUrl;
      } else {
        showSnackbar("Failed to upload picture", "error");
        return picture;
      }
    } catch (error) {
      console.error("Error uploading picture:", error);
      showSnackbar("Error uploading picture", "error");
      return picture;
    }
  };

  const validateForm = () => {
    const newErrors = {};

    if (!username.trim()) {
      newErrors.username = "Username is required";
    }

    if (!email.trim()) {
      newErrors.email = "Email is required";
    } else if (!/\S+@\S+\.\S+/.test(email)) {
      newErrors.email = "Email is invalid";
    }

    if (!server) {
      newErrors.server = "Please select a server";
    }

    // Password validation (only if user is trying to change password)
    if (currentPassword || newPassword || confirmPassword) {
      if (!currentPassword) {
        newErrors.currentPassword = "Current password required to change password";
      }
      if (!newPassword) {
        newErrors.newPassword = "New password is required";
      } else if (newPassword.length < 6) {
        newErrors.newPassword = "Password must be at least 6 characters";
      }
      if (newPassword !== confirmPassword) {
        newErrors.confirmPassword = "Passwords do not match";
      }
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSave = async () => {
    if (!validateForm()) {
      showSnackbar("Please fix the errors", "error");
      return;
    }

    setSaving(true);

    try {
      // Upload picture first if there's a new file
      let pictureUrl = picture;
      if (selectedFile) {
        pictureUrl = await uploadProfilePicture();
      }

      // Prepare update payload
      const updateData = {
        username: username,
        email: email,
        picture: pictureUrl,
        server: server,
      };

      // Include password only if user is changing it
      if (newPassword) {
        updateData.password = newPassword;
      }

      const response = await fetch("/api/user/update", {
        method: "PUT",
        headers: {
          "Content-Type": "application/json",
          "Authorization": `Bearer ${localStorage.getItem("token")}`,
        },
        body: JSON.stringify(updateData),
      });

      if (response.ok) {
        showSnackbar("Profile updated successfully", "success");
        setPicture(pictureUrl);
        setSelectedFile(null);
        // Clear password fields
        setCurrentPassword("");
        setNewPassword("");
        setConfirmPassword("");
      } else {
        const errorData = await response.json();
        showSnackbar(errorData.message || "Failed to update profile", "error");
      }
    } catch (error) {
      console.error("Error updating profile:", error);
      showSnackbar("Error updating profile", "error");
    } finally {
      setSaving(false);
    }
  };

  const handleCancel = () => {
    fetchUserInfo();
    setSelectedFile(null);
    setCurrentPassword("");
    setNewPassword("");
    setConfirmPassword("");
    setErrors({});
    showSnackbar("Changes cancelled", "info");
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

  if (loading) {
    return (
      <Box sx={{ display: "flex", justifyContent: "center", alignItems: "center", height: "100vh" }}>
        <CircularProgress />
      </Box>
    );
  }

  return (
    <ThemeProvider theme={theme}>
      <Box sx={{ display: "flex", minHeight: "100vh", bgcolor: "background.default" }}>
        <LeftPanel leftNav={leftNav} setLeftNav={setLeftNav} />

        <Box component="main" sx={{ flexGrow: 1 }}>
          <TopBar notifications={notifications} onNotifOpen={() => {}} />

          {/* Profile Content */}
          <Box className="profile-container">
            <Box className="profile-header">
              <Box>
                <Typography variant="h5">Profile Information</Typography>
                <Typography variant="body2" color="text.secondary">
                  Manage your personal details and preferences
                </Typography>
              </Box>
              <Button 
                variant="contained" 
                onClick={handleSave}
                disabled={saving}
              >
                {saving ? <CircularProgress size={24} /> : "Save Changes"}
              </Button>
            </Box>

            <Box className="profile-content">
              {/* Profile Photo Card */}
              <Card className="profile-photo-card">
                <CardContent className="profile-photo-content">
                  <Typography variant="subtitle1" className="profile-photo-title">
                    Account Management
                  </Typography>
                  <Avatar 
                    className="profile-avatar"
                    src={previewUrl}
                  >
                    {!previewUrl && getInitials()}
                  </Avatar>
                  <input
                    accept="image/*"
                    style={{ display: "none" }}
                    id="upload-photo"
                    type="file"
                    onChange={handleFileSelect}
                  />
                  <label htmlFor="upload-photo">
                    <Button variant="outlined" component="span" fullWidth sx={{ mb: 1 }}>
                      Upload Photo
                    </Button>
                  </label>
                  <Typography variant="caption" color="text.secondary" display="block">
                    Recommended: Square image, at least 400x400px
                  </Typography>
                  {createdAt && (
                    <Typography variant="caption" color="text.secondary" display="block" sx={{ mt: 2 }}>
                      Member since: {new Date(createdAt).toLocaleDateString()}
                    </Typography>
                  )}
                </CardContent>
              </Card>

              {/* Profile Form */}
              <Card className="profile-form-card">
                <CardContent sx={{ p: 3 }}>
                  <Typography variant="h6" sx={{ mb: 2 }}>
                    Profile Information
                  </Typography>

                  <Box className="form-grid">
                    <TextField
                      label="Username"
                      value={username}
                      onChange={(e) => setUsername(e.target.value)}
                      fullWidth
                      error={!!errors.username}
                      helperText={errors.username}
                    />
                    <TextField
                      label="Email"
                      type="email"
                      value={email}
                      onChange={(e) => setEmail(e.target.value)}
                      fullWidth
                      error={!!errors.email}
                      helperText={errors.email}
                    />
                    <FormControl fullWidth error={!!errors.server}>
                      <InputLabel>Server Region</InputLabel>
                      <Select
                        value={server}
                        label="Server Region"
                        onChange={(e) => setServer(e.target.value)}
                      >
                        {servers.map((s) => (
                          <MenuItem key={s.id} value={s.id}>
                            {s.name}
                          </MenuItem>
                        ))}
                      </Select>
                      {errors.server && (
                        <Typography variant="caption" color="error" sx={{ mt: 0.5, ml: 2 }}>
                          {errors.server}
                        </Typography>
                      )}
                    </FormControl>
                  </Box>

                  <Divider sx={{ my: 3 }} />

                  <Typography variant="h6" sx={{ mb: 2 }}>
                    Change Password
                  </Typography>

                  <Box className="form-grid">
                    <TextField
                      label="Current Password"
                      type="password"
                      value={currentPassword}
                      onChange={(e) => setCurrentPassword(e.target.value)}
                      fullWidth
                      error={!!errors.currentPassword}
                      helperText={errors.currentPassword}
                    />
                    <Box /> {/* Empty space for grid alignment */}
                    <TextField
                      label="New Password"
                      type="password"
                      value={newPassword}
                      onChange={(e) => setNewPassword(e.target.value)}
                      fullWidth
                      error={!!errors.newPassword}
                      helperText={errors.newPassword || "Minimum 6 characters"}
                    />
                    <TextField
                      label="Confirm New Password"
                      type="password"
                      value={confirmPassword}
                      onChange={(e) => setConfirmPassword(e.target.value)}
                      fullWidth
                      error={!!errors.confirmPassword}
                      helperText={errors.confirmPassword}
                    />
                  </Box>

                  <Box className="form-actions">
                    <Button variant="outlined" onClick={handleCancel} disabled={saving}>
                      Cancel
                    </Button>
                    <Button variant="contained" onClick={handleSave} disabled={saving}>
                      {saving ? <CircularProgress size={24} /> : "Update Profile"}
                    </Button>
                  </Box>
                </CardContent>
              </Card>
            </Box>
          </Box>
        </Box>
      </Box>

      <Snackbar
        open={snackbar.open}
        autoHideDuration={4000}
        onClose={handleCloseSnackbar}
        anchorOrigin={{ vertical: "bottom", horizontal: "right" }}
      >
        <Alert onClose={handleCloseSnackbar} severity={snackbar.severity} sx={{ width: "100%" }}>
          {snackbar.message}
        </Alert>
      </Snackbar>
    </ThemeProvider>
  );
}