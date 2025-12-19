import {
  Card,
  CardContent,
  CardMedia,
  Typography,
  Box,
  Chip,
  Avatar,
  Stack,
  Tooltip,
} from "@mui/material";
import {
  Star as StarIcon,
  CallSplit as ForkIcon,
  Visibility as ViewIcon,
  InsertPhoto as InsertPhotoIcon,
} from "@mui/icons-material";

import { useNavigate } from "react-router-dom";

function getInitials(name) {
  if (!name) return "";
  const parts = name.trim().split(/\s+/);
  if (parts.length === 1) return (parts[0][0] || "").toUpperCase();
  return ((parts[0][0] || "") + (parts[1][0] || "")).toUpperCase();
}

export default function DiscoverDiagramCard({ d, onClick }) {
  const stars = d.stars || 0;
  const forks = d.forks || 0;
  const views = d.views || 0;
  const hashtags = d.hashTags || [];

  const navigate = useNavigate();

  const contributors = d.contributors || (d.owner ? [d.owner] : []);

  const handleContributorClick = (e, user) => {
    e.stopPropagation();
    if (user?.name) {
      navigate(`/designer/${user.name.replace("@", "").replace("%20", "")}`);
    }
  };

  return (
    <Card
      className="diagram-card"
      sx={{
        display: "flex",
        flexDirection: "column",
        cursor: "pointer",
        position: "relative",
        transition: "all 0.2s ease-in-out",
        "&:hover": {
          transform: "translateY(-4px)",
          boxShadow: 6,
        },
        height: "100%",
      }}
      onClick={() => onClick(d)}
    >
      <Box sx={{ position: "relative" }}>
        {d.thumbnail ? (
          <CardMedia
            component="img"
            height="210"
            image={d.thumbnail}
            alt={d.name}
            className="thumbnail"
            sx={{ objectFit: "cover" }}
          />
        ) : (
          <Box
            sx={{
              height: 210,
              bgcolor: "grey.200",
              display: "flex",
              alignItems: "center",
              justifyContent: "center",
            }}
          >
            <InsertPhotoIcon
              sx={{ fontSize: 56, color: "#9aa4b2", opacity: 0.5 }}
            />
          </Box>
        )}

        <Box
          sx={{
            position: "absolute",
            top: 8,
            right: 8,
            bgcolor: "rgba(255, 255, 255, 0.9)",
            backdropFilter: "blur(4px)",
            borderRadius: 12,
            px: 1,
            py: 0.5,
            display: "flex",
            alignItems: "center",
            gap: 0.5,
            boxShadow: 1,
          }}
        >
          <StarIcon sx={{ fontSize: 14, color: "#ffb400" }} />
          <Typography variant="caption" fontWeight="bold">
            {stars}
          </Typography>
        </Box>
      </Box>

      <CardContent
        className="card-content"
        sx={{ flexGrow: 1, p: 2, display: "flex", flexDirection: "column" }}
      >
        <Box sx={{ mb: 1 }}>
          <Typography
            variant="h6"
            noWrap
            sx={{ fontWeight: 600, fontSize: "1rem" }}
          >
            {d.name}
          </Typography>
          <Typography variant="caption" color="text.secondary" display="block">
            Updated: {d.lastModified || "Recently"}
          </Typography>
        </Box>

        <Typography
          variant="body2"
          color="text.secondary"
          sx={{
            mb: 2,
            display: "-webkit-box",
            WebkitLineClamp: 2,
            WebkitBoxOrient: "vertical",
            overflow: "hidden",
            height: 40,
            lineHeight: 1.43,
          }}
        >
          {d.shortDescription || "No description available."}
        </Typography>

        <Box
          sx={{
            display: "flex",
            flexWrap: "wrap",
            gap: 0.5,
            mb: 2,
            height: 24,
            overflow: "hidden",
          }}
        >
          {hashtags.slice(0, 3).map((tag) => (
            <Chip
              key={tag}
              label={`#${tag}`}
              size="small"
              sx={{
                height: 20,
                fontSize: "0.65rem",
                bgcolor: "primary.50",
                color: "primary.main",
              }}
            />
          ))}
        </Box>

        <Box
          sx={{
            mt: "auto",
            display: "flex",
            justifyContent: "space-between",
            alignItems: "center",
          }}
        >
          <Box
            className="avatar-stack"
            sx={{
              display: "flex",
              alignItems: "center",
              gap: 1,
            }}
          >
            {contributors.slice(0, 6).map((c, i) => (
              <Box key={i} sx={{ zIndex: contributors.length - i }}>
                <Tooltip title={c.name}>
                  <Avatar
                    src={c.picture}
                    alt={c.name}
                    sx={{
                      width: 32,
                      height: 32,
                      fontSize: 12,
                      border: "2px solid white",
                      boxShadow: 1,
                      ml: i === 0 ? 0 : -1.2,
                      bgcolor: c.picture ? undefined : "primary.main",
                      color: c.picture ? undefined : "white",
                      cursor: "pointer",
                    }}
                    onClick={(e) => handleContributorClick(e, c)}
                  >
                    {!c.picture && getInitials(c.name)}
                  </Avatar>
                </Tooltip>
              </Box>
            ))}

            {contributors.length > 6 && (
              <Avatar
                sx={{
                  width: 32,
                  height: 32,
                  fontSize: 12,
                  ml: -1.2,
                  border: "2px solid white",
                  bgcolor: "grey.400",
                }}
              >
                +{contributors.length - 6}
              </Avatar>
            )}
          </Box>

          <Stack direction="row" spacing={1.5} alignItems="center">
            <Tooltip title="Forks">
              <Box sx={{ display: "flex", alignItems: "center", gap: 0.5 }}>
                <ForkIcon sx={{ fontSize: 14, color: "text.secondary" }} />
                <Typography variant="caption" color="text.secondary">
                  {forks}
                </Typography>
              </Box>
            </Tooltip>
            <Tooltip title="Views">
              <Box sx={{ display: "flex", alignItems: "center", gap: 0.5 }}>
                <ViewIcon sx={{ fontSize: 14, color: "text.secondary" }} />
                <Typography variant="caption" color="text.secondary">
                  {views}
                </Typography>
              </Box>
            </Tooltip>
          </Stack>
        </Box>
      </CardContent>
    </Card>
  );
}
