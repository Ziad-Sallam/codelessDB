import React, { useState, useEffect } from "react";
import {
  AppBar, Toolbar, Avatar,
  Typography, Box, Link,
} from "@mui/material";
import { useNavigate } from "react-router-dom";
import { getInitials } from "../pages/diagrams/Contributors";
import { useAuth } from "./AuthProvider.jsx";

export default function SimpleTopBar({ title, customContent }) {
  const navigate = useNavigate();
  const [username, setUserName] = useState("");
  const [userImage, setUserImage] = useState("");
  const { user } = useAuth();

  useEffect(() => {
    if (user) {
      setUserName(user.username);
      setUserImage(user.picture);
    }
  }, [user]);

  return (
    <AppBar
      position="static"
      color="transparent"
      elevation={0}
      sx={{
        borderBottom: "1px solid rgba(0,0,0,0.06)",
        bgcolor: "background.light",
      }}
    >
      <Toolbar sx={{ display: "flex", justifyContent: "space-between", py: 1 }}>
        {/* LEFT/CENTER - Title or Custom Content */}
        <Box sx={{ flex: 1, display: 'flex', alignItems: 'center' }}>
          {customContent ? (
            customContent
          ) : (
            title && (
              <Typography variant="h6" color="text.primary" fontWeight={600}>
                {title}
              </Typography>
            )
          )}
        </Box>

        {/* RIGHT - User Profile */}
        <Box sx={{ display: "flex", alignItems: "center", gap: 1 }}>
          <Link href="/profile" style={{ textDecoration: "none" }}>
            <Typography
              variant="body2"
              sx={{
                width: "auto",
                fontSize: 16,
                marginRight: 2,
                display: { xs: "none", sm: "block" },
                cursor: "pointer",
                fontWeight: 500,
                color: "text.primary",
              }}
            >
              {username}
            </Typography>
          </Link>

          <Avatar
            onClick={() => navigate("/profile")}
            src={userImage || undefined}
            alt={username}
            sx={{
              bgcolor: userImage ? undefined : "primary.main",
              cursor: "pointer",
              transition: "0.2s",
              "&:hover": {
                transform: "scale(1.07)",
                boxShadow: 3,
              },
            }}
          >
            {(!userImage || userImage.trim() === "") && getInitials(username)}
          </Avatar>
        </Box>
      </Toolbar>
    </AppBar>
  );
}
