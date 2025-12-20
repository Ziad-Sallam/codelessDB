import {
  ChevronLeft,
  ChevronRight,
  Storage as DatabaseIcon,
  Star as StarIcon,
  PersonAdd as UserPlusIcon,
  Group as UsersIcon
} from "@mui/icons-material";
import {
  Avatar,
  Box,
  Button,
  Card,
  CardContent,
  IconButton,
  Stack,
  Typography
} from "@mui/material";
import { useRef } from "react";
import { useNavigate } from "react-router-dom";

const UserCarousel = ({ users, title = "Featured Designers", onFollowToggle, currentUser }) => {
  const navigate = useNavigate();
  const scrollContainerRef = useRef(null);

  const scroll = (direction) => {
    if (scrollContainerRef.current) {
      const scrollAmount = 320;
      scrollContainerRef.current.scrollBy({
        left: direction === "left" ? -scrollAmount : scrollAmount,
        behavior: "smooth",
      });
    }
  };

  return (
    <Box sx={{ width: "100%", overflow: "hidden" }}>
      <Box
        sx={{
          display: "flex",
          alignItems: "center",
          justifyContent: "space-between",
          mb: 2,
        }}
      >
        <Box sx={{ display: "flex", alignItems: "center", gap: 2 }}>
          <Box
            sx={{
              width: 40,
              height: 40,
              borderRadius: 3,
              background: "linear-gradient(135deg, #1976d2 0%, #42a5f5 100%)",
              display: "flex",
              alignItems: "center",
              justifyContent: "center",
              boxShadow: "0 4px 12px rgba(25, 118, 210, 0.2)",
            }}
          >
            <UsersIcon sx={{ color: "white", fontSize: 20 }} />
          </Box>
          <Box>
            <Typography variant="h6" sx={{ fontWeight: 600, lineHeight: 1.2 }}>
              {title}
            </Typography>
          </Box>
        </Box>
        <Box sx={{ display: "flex", gap: 1 }}>
          <IconButton
            onClick={() => scroll("left")}
            size="small"
            sx={{ border: "1px solid", borderColor: "divider" }}
          >
            <ChevronLeft />
          </IconButton>
          <IconButton
            onClick={() => scroll("right")}
            size="small"
            sx={{ border: "1px solid", borderColor: "divider" }}
          >
            <ChevronRight />
          </IconButton>
        </Box>
      </Box>

      <Box
        ref={scrollContainerRef}
        sx={{
          display: "flex",
          gap: 2,
          overflowX: "auto",
          pb: 2,
          scrollSnapType: "x mandatory",
          "&::-webkit-scrollbar": { display: "none" },
          scrollbarWidth: "none",
        }}
      >
        {users.map((user, index) => (
          <Card
            key={user.id}
            sx={{
              minWidth: 280,
              maxWidth: 280,
              flexShrink: 0,
              scrollSnapAlign: "start",
              cursor: "pointer",
              transition: "all 0.3s ease",
              "&:hover": {
                transform: "translateY(-4px)",
                boxShadow: 6,
                borderColor: "primary.main",
              },
              position: "relative",
              overflow: "visible",
              mt: 4,
              mb: 1,
            }}
            variant="outlined"
            onClick={() =>
              navigate(`/designer/${user.username.replace("@", "")}`)
            }
          >
            <Box
              sx={{
                height: 60,
                borderRadius: "4px 4px 0 0",
                position: "absolute",
                top: -32,
                left: 0,
                right: 0,
                zIndex: 0,
              }}
            />

            <Box sx={{ position: "relative", px: 2, pt: 0, zIndex: 1 }}>
              <Avatar
                src={user.picture}
                alt={user.name}
                sx={{
                  width: 64,
                  height: 64,
                  border: "4px solid white",
                  boxShadow: 2,
                  mt: -4,
                  mb: 1.5,
                }}
              >
                {user.name.charAt(0)}
              </Avatar>

              <CardContent
                sx={{
                  p: 0,
                  pb: 2,
                  minWidth: 0,
                  height: "100%",
                  display: "flex",
                  flexDirection: "column",
                }}
              >
                <Box sx={{ mb: 2, minWidth: 0 }}>
                  <Typography
                    variant="h6"
                    sx={{
                      fontWeight: 700,
                      lineHeight: 1.2,
                      overflow: "hidden",
                      display: "-webkit-box",
                      WebkitLineClamp: 2,
                      WebkitBoxOrient: "vertical",
                      textOverflow: "ellipsis",
                      wordBreak: "normal",
                      overflowWrap: "anywhere",
                    }}
                  >
                    {user.name}
                  </Typography>
                  <Typography
                    variant="body2"
                    color="text.secondary"
                    sx={{ display: "block", mt: 0.5, fontWeight: 500 }}
                  >
                    @{user.username}
                  </Typography>
                </Box>

                <Typography
                  variant="body2"
                  color="text.secondary"
                  sx={{
                    mb: 2,
                    minHeight: 60,
                    overflow: "hidden",
                    display: "-webkit-box",
                    WebkitLineClamp: 3,
                    WebkitBoxOrient: "vertical",
                    textOverflow: "ellipsis",
                    wordBreak: "break-word",
                  }}
                >
                  {user.bio}
                </Typography>

                <Stack spacing={1} sx={{ mt: "auto", mb: 2 }}>
                  <Stack direction="row" justifyContent="center" spacing={8} alignItems="center">
                    <Box sx={{ display: "flex", alignItems: "center", gap: 0.5 }}>
                      <StarIcon sx={{ fontSize: 16, color: "#ffb400" }} />
                      <Typography variant="body2" fontWeight={500}>
                        {user.totalStars}
                      </Typography>
                      <Typography variant="caption" color="text.secondary">
                        stars
                      </Typography>
                    </Box>
                    <Box sx={{ display: "flex", alignItems: "center", gap: 0.5 }}>
                      <DatabaseIcon sx={{ fontSize: 16, color: "text.secondary" }} />
                      <Typography variant="body2" fontWeight={500}>
                        {user.publicCount}
                      </Typography>
                      <Typography variant="caption" color="text.secondary">
                        schemas
                      </Typography>
                    </Box>
                  </Stack>

                  <Stack direction="row" justifyContent="center" spacing={5} alignItems="center">
                    <Box sx={{ display: "flex", alignItems: "center", gap: 0.5 }}>
                      <UsersIcon sx={{ fontSize: 16, color: "#1976d2" }} />
                      <Typography variant="body2" fontWeight={600}>
                        {user.followersCount}
                      </Typography>
                      <Typography variant="caption" color="text.secondary">
                        Followers
                      </Typography>
                    </Box>
                    <Box sx={{ display: "flex", alignItems: "center", gap: 0.5 }}>
                      <UsersIcon sx={{ fontSize: 16, color: "#9c27b0" }} />
                      <Typography variant="body2" fontWeight={600}>
                        {user.followingCount}
                      </Typography>
                      <Typography variant="caption" color="text.secondary">
                        Following
                      </Typography>
                    </Box>
                  </Stack>
                </Stack>
                <Button
                  variant={user.isFollowed ? "contained" : "outlined"}
                  color={user.isFollowed ? "secondary" : "primary"}
                  size="small"
                  fullWidth
                  disabled={currentUser && (currentUser.username === user.username || currentUser.id === user.id)}
                  startIcon={user.isFollowed ? <UsersIcon /> : <UserPlusIcon />}
                  onClick={(e) => {
                    e.stopPropagation();
                    onFollowToggle && onFollowToggle(user);
                  }}
                >
                  {currentUser && (currentUser.username === user.username || currentUser.id === user.id) 
                    ? "You" 
                    : (user.isFollowed ? "Unfollow" : "Follow")}
                </Button>
              </CardContent>
            </Box>
          </Card>
        ))}
      </Box>
    </Box>
  );
};

export default UserCarousel;
