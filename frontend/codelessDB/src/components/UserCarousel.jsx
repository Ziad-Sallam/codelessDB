import React, { useRef } from "react";
import { useNavigate } from "react-router-dom";
import {
  Card,
  CardContent,
  Avatar,
  Button,
  Typography,
  Box,
  IconButton,
  Stack,
  Chip
} from "@mui/material";
import {
  ChevronLeft,
  ChevronRight,
  Group as UsersIcon,
  Storage as DatabaseIcon,
  PersonAdd as UserPlusIcon,
  Person as PersonIcon,
  Star as StarIcon
} from "@mui/icons-material";

const UserCarousel = ({ users, title = "Featured Designers" }) => {
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
    <Box sx={{ width: '100%', overflow: 'hidden' }}>
      {/* Header */}
      <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', mb: 2 }}>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
          <Box
            sx={{
              width: 40,
              height: 40,
              borderRadius: 3,
              background: 'linear-gradient(135deg, #1976d2 0%, #42a5f5 100%)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              boxShadow: '0 4px 12px rgba(25, 118, 210, 0.2)'
            }}
          >
            <UsersIcon sx={{ color: 'white', fontSize: 20 }} />
          </Box>
          <Box>
            <Typography variant="h6" sx={{ fontWeight: 600, lineHeight: 1.2 }}>
              {title}
            </Typography>
            {/* <Typography variant="body2" color="text.secondary">
              Connect with database designers
            </Typography> */}
          </Box>
        </Box>
        <Box sx={{ display: 'flex', gap: 1 }}>
          <IconButton onClick={() => scroll("left")} size="small" sx={{ border: '1px solid', borderColor: 'divider' }}>
            <ChevronLeft />
          </IconButton>
          <IconButton onClick={() => scroll("right")} size="small" sx={{ border: '1px solid', borderColor: 'divider' }}>
            <ChevronRight />
          </IconButton>
        </Box>
      </Box>

      {/* Carousel */}
      <Box
        ref={scrollContainerRef}
        sx={{
          display: 'flex',
          gap: 2,
          overflowX: 'auto',
          pb: 2,
          scrollSnapType: 'x mandatory',
          '&::-webkit-scrollbar': { display: 'none' },
          scrollbarWidth: 'none',
        }}
      >
        {users.map((user, index) => (
          <Card
            key={user.id}
            sx={{
              minWidth: 280,
              maxWidth: 280,
              flexShrink: 0,
              scrollSnapAlign: 'start',
              cursor: 'pointer',
              transition: 'all 0.3s ease',
              '&:hover': {
                transform: 'translateY(-4px)',
                boxShadow: 6,
                borderColor: 'primary.main'
              },
              position: 'relative',
              overflow: 'visible',
              mt: 4, // Space for avatar
              mb: 1
            }}
            variant="outlined"
            onClick={() => navigate(`/designer/${user.username.replace("@", "")}`)}
          >
            {/* Gradient Header */}
            <Box
              sx={{
                height: 60,
                // background: 'linear-gradient(135deg, rgba(25, 118, 210, 0.08) 0%, rgba(25, 118, 210, 0.02) 100%)',
                borderRadius: '4px 4px 0 0',
                position: 'absolute',
                top: -32,
                left: 0,
                right: 0,
                zIndex: 0
              }}
            />

            <Box sx={{ position: 'relative', px: 2, pt: 0, zIndex: 1 }}>
              <Avatar
                src={user.picture}
                alt={user.name}
                sx={{
                  width: 64,
                  height: 64,
                  border: '4px solid white',
                  boxShadow: 2,
                  mt: -4,
                  mb: 1.5
                }}
              >
                {/* {user.name.charAt(0)} */}
              </Avatar>

              <CardContent sx={{ p: 0, pb: 2 }}>
                <Box sx={{ mb: 1 }}>
                  <Typography variant="subtitle1" sx={{ fontWeight: 600, lineHeight: 1.2 }}>
                    {user.name}
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    {user.username}
                  </Typography>
                </Box>

                <Typography
                  variant="body2"
                  color="text.secondary"
                  sx={{
                    mb: 2,
                    minHeight: 40,
                    display: '-webkit-box',
                    WebkitLineClamp: 2,
                    WebkitBoxOrient: 'vertical',
                    overflow: 'hidden'
                  }}
                >
                  {user.bio}
                </Typography>

                <Stack direction="row" spacing={10} sx={{ mb: 2 }}>
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
                    <StarIcon sx={{ fontSize: 16, color: '#ffb400' }} />
                    <Typography variant="body2" fontWeight={500}>
                      {user.totalStars}
                      {/* {user.totalStars ? user.totalStars.toLocaleString() : (user.followers ? user.followers.toLocaleString() : 0)} */}
                    </Typography>
                    <Typography variant="caption" color="text.secondary">
                      stars
                    </Typography>
                  </Box>
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
                    <DatabaseIcon sx={{ fontSize: 16, color: 'text.secondary' }} />
                    <Typography variant="body2" fontWeight={500}>
                      {user.publicCount}
                    </Typography>
                    <Typography variant="caption" color="text.secondary">
                      schemas
                    </Typography>
                  </Box>
                </Stack>

                {/* <Button
                  variant={user.isFollowing ? "contained" : "outlined"}
                  color={user.isFollowing ? "secondary" : "primary"}
                  size="small"
                  fullWidth
                  startIcon={<UserPlusIcon />}
                  onClick={(e) => {
                    e.stopPropagation();
                    // Handle follow
                  }}
                >
                  {user.isFollowing ? "Following" : "Follow"}
                </Button> */}
              </CardContent>
            </Box>
          </Card>
        ))}
      </Box>
    </Box>
  );
};

export default UserCarousel;
