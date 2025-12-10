import { useState, useEffect } from "react";
import {
  AppBar, Toolbar, Avatar,
  Typography, Paper, InputBase, Box, Link,
} from "@mui/material";

import SearchIcon from "@mui/icons-material/Search";
import { useNavigate } from "react-router-dom";
import { getInitials } from "../diagrams/Contributors.jsx";
import { useAuth } from "../../components/AuthProvider.jsx";


export default function DiscoverTopBar(props) {
  const { onSearch } = props;
  const navigate = useNavigate();

  const [username, setUserName] = useState("");
  const [userImage, setUserImage] = useState("");

  const [search, setSearch] = useState("");

  const { user } = useAuth();

  useEffect(() => {
    if (user) {
      setUserName(user.username);
      setUserImage(user.picture);
    }
  }, [user]);

  const handleSearch = () => {
    onSearch && onSearch(search);
  };

  const handleKeyPress = (e) => {
    if (e.key === "Enter") {
      handleSearch(0);
    }
  };

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
        {/* CENTER — search bar */}
        <Paper
          elevation={2}
          sx={{
            display: "flex",
            alignItems: "center",
            px: 2,
            py: 0.8,
            borderRadius: 7,
            flex: 1,
            maxWidth: 650,
            bgcolor: "background.paper",
            transition: "box-shadow 0.2s ease, transform 0.2s ease",
            "&:hover": {
              boxShadow: 6,
              transform: "translateY(-1px)",
            },
          }}
        >
          <InputBase
            value={search || ""}
            onChange={(e) => setSearch(e.target.value)}
            onKeyPress={handleKeyPress}
            placeholder="Search schemas, users, or hashtags..."
            sx={{ flex: 1, fontSize: 15, paddingLeft: 2 }}
          />

          <SearchIcon
            sx={{
              mr: 1.4,
              color: "text.secondary",
              fontSize: 22,
              cursor: "pointer",
            }}
            onClick={() => handleSearch(0)}
          />
        </Paper>

        {/* <Box sx={{ display: "flex", alignItems: "center", gap: 1, ml: 2 }}> */}
        <Box sx={{ display: "flex", alignItems: "center", gap: 1 }}>
          <Link href="/profile" style={{ textDecoration: "none" }}>
            <Typography
              variant="body2"
              sx={{
                width: "auto",
                fontSize: 23,
                marginRight: 2,
                display: { sm: "block" },
                cursor: "pointer",
                fontWeight: 540,
                color: "background.dark",
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
