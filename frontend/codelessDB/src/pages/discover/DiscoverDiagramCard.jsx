import {
  CallSplit as ForkIcon,
  InsertPhoto as InsertPhotoIcon,
  Star as StarIcon,
  Visibility as ViewIcon,
} from "@mui/icons-material";
import {
  Avatar,
  Box,
  Card,
  CardContent,
  CardMedia,
  Chip,
  Stack,
  Tooltip,
  Typography
} from "@mui/material";

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
        transition: "all 0.3s cubic-bezier(0.4, 0, 0.2, 1)",
        height: "100%",
        borderRadius: 3,
        overflow: "hidden",
        border: "1px solid",
        borderColor: "divider",
        "&:hover": {
          transform: "translateY(-4px)",
          boxShadow: "0 12px 24px rgba(0,0,0,0.1)",
          borderColor: "primary.main",
          "& .thumbnail": {
            transform: "scale(1.05)",
          }
        },
      }}
      onClick={() => onClick(d)}
    >
      <Box sx={{ position: "relative", overflow: "hidden" }}>
        {d.thumbnail ? (
          <CardMedia
            component="img"
            height="180"
            image={d.thumbnail}
            alt={d.name}
            className="thumbnail"
            sx={{ 
              objectFit: "cover",
              transition: "transform 0.3s ease" 
            }}
          />
        ) : (
          <Box
            className="thumbnail"
            sx={{
              height: 180,
              bgcolor: "grey.100",
              display: "flex",
              alignItems: "center",
              justifyContent: "center",
              transition: "transform 0.3s ease" 
            }}
          >
            <InsertPhotoIcon
              sx={{ fontSize: 48, color: "text.disabled", opacity: 0.5 }}
            />
          </Box>
        )}

        {/* Floating Stats Badge */}
        <Stack
          direction="row"
          spacing={1}
          sx={{
            position: "absolute",
            top: 12,
            right: 12,
            zIndex: 1,
          }}
        >
          <Box
            sx={{
              bgcolor: "rgba(255, 255, 255, 0.9)",
              backdropFilter: "blur(8px)",
              borderRadius: 20,
              px: 1,
              py: 0.5,
              display: "flex",
              alignItems: "center",
              gap: 0.5,
              boxShadow: "0 2px 8px rgba(0,0,0,0.1)",
            }}
          >
            <StarIcon sx={{ fontSize: 14, color: "#ffb400" }} />
            <Typography variant="caption" fontWeight={700} sx={{ lineHeight: 1 }}>
              {stars}
            </Typography>
          </Box>
        </Stack>
      </Box>

      <CardContent
        sx={{ 
          flexGrow: 1, 
          p: 2.5, 
          display: "flex", 
          flexDirection: "column",
          gap: 1.5
        }}
      >
        <Box>
          <Typography
            variant="h6"
            sx={{ 
              fontWeight: 700, 
              fontSize: "1.1rem",
              lineHeight: 1.3,
              mb: 0.5,
              overflow: "hidden",
              textOverflow: "ellipsis",
              whiteSpace: "nowrap"
            }}
          >
            {d.name}
          </Typography>
          <Typography variant="caption" color="text.secondary" sx={{ display: "block" }}>
            Updated {d.lastModified || "recently"}
          </Typography>
        </Box>

        <Typography
          variant="body2"
          color="text.secondary"
          sx={{
            display: "-webkit-box",
            WebkitLineClamp: 2,
            WebkitBoxOrient: "vertical",
            overflow: "hidden",
            minHeight: "2.5em", // Reserve space for 2 lines approximately
            lineHeight: 1.5,
          }}
        >
          {d.shortDescription || "No description provided."}
        </Typography>

        <Box sx={{ display: "flex", gap: 0.5, flexWrap: "wrap", minHeight: 24 }}>
          {hashtags.slice(0, 3).map((tag) => (
            <Chip
              key={tag}
              label={`#${tag}`}
              size="small"
              sx={{
                height: 22,
                fontSize: "0.7rem",
                bgcolor: "primary.50",
                color: "primary.main",
                fontWeight: 500,
                border: "1px solid",
                borderColor: "primary.100"
              }}
            />
          ))}
        </Box>

        <Box sx={{ borderTop: '1px solid', borderColor: 'divider', my: 1 }} />

        <Box
          sx={{
            mt: "auto",
            display: "flex",
            justifyContent: "space-between",
            alignItems: "center",
          }}
        >
          <Box
            sx={{
              display: "flex",
              alignItems: "center",
              pl: 0.5 // Offset for overlapping avatars
            }}
          >
            {contributors.slice(0, 4).map((c, i) => (
              <Tooltip key={i} title={c.name}>
                <Avatar
                  src={c.picture}
                  alt={c.name}
                  sx={{
                    width: 28,
                    height: 28,
                    fontSize: 12,
                    border: "2px solid white",
                    ml: -1,
                    bgcolor: !c.picture ? `hsl(${(c.name?.charCodeAt(0) || 0) * 10}, 70%, 50%)` : undefined,
                    cursor: "pointer",
                    transition: "transform 0.1s",
                    "&:hover": { transform: "scale(1.1) translateY(-2px)", zIndex: 10 }
                  }}
                  onClick={(e) => handleContributorClick(e, c)}
                >
                  {!c.picture && getInitials(c.name)}
                </Avatar>
              </Tooltip>
            ))}
            {contributors.length > 4 && (
              <Box
                 sx={{
                    width: 28,
                    height: 28,
                    borderRadius: "50%",
                    bgcolor: "grey.100",
                    color: "text.secondary",
                    fontSize: "0.7rem",
                    fontWeight: "bold",
                    display: "flex",
                    alignItems: "center",
                    justifyContent: "center",
                    border: "2px solid white",
                    ml: -1,
                    zIndex: 0
                  }}
              >
                +{contributors.length - 4}
              </Box>
            )}
          </Box>

          <Stack direction="row" spacing={2} alignItems="center">
            <Tooltip title="Forks">
              <Box sx={{ display: "flex", alignItems: "center", gap: 0.5 }}>
                <ForkIcon sx={{ fontSize: 16, color: "text.secondary" }} />
                <Typography variant="caption" fontWeight={600} color="text.secondary">
                  {forks}
                </Typography>
              </Box>
            </Tooltip>
            <Tooltip title="Views">
              <Box sx={{ display: "flex", alignItems: "center", gap: 0.5 }}>
                <ViewIcon sx={{ fontSize: 16, color: "text.secondary" }} />
                <Typography variant="caption" fontWeight={600} color="text.secondary">
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
