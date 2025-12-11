import { Card, CardContent, Typography, Box, CircularProgress } from "@mui/material";
import { Check as CheckIcon, Storage as DatabaseIcon, CalendarToday as CalendarIcon } from "@mui/icons-material";
import { useState } from "react";

export default function DiagramSelector({ loading, diagramsCards, selectedDiagram, onSelect }) {
  const [selected, setSelected] = useState(null);

  const handleSelect = (diagram) => {
    setSelected(diagram);
    onSelect && onSelect(diagram);
  };

  const currentSelection = selectedDiagram || selected;

  return (
    <Box
      sx={{
        display: "flex",
        gap: 2,
        overflowX: "auto",
        maxWidth: 1150,
        p: 1,
        "&::-webkit-scrollbar": { height: 8 },
        "&::-webkit-scrollbar-thumb": { backgroundColor: "#ccc", borderRadius: 4 },
      }}
    >
      {loading ? (
        <Box sx={{ p: 2, display: 'flex', justifyContent: 'center', width: '100%' }}>
          <CircularProgress size={24} />
        </Box>
      ) : diagramsCards.length === 0 ? (
        <Typography variant="body2" color="text.secondary" sx={{ display: 'flex', justifyContent: 'center', width: '100%', fontStyle: 'italic', p: 1 }}>
          No diagrams found
        </Typography>
      ) : (
        diagramsCards.map((diagram) => (
          <Box
            key={diagram.diagramId}
            sx={{
              minWidth: 300,
              maxWidth: 300,
              flexShrink: 0,
              display: "flex",
              flexDirection: "column",
            }}
          >
            <Card
              variant="outlined"
              onClick={() => handleSelect(diagram)}
              sx={{
                height: "100%",
                display: "flex",
                flexDirection: "column",
                cursor: "pointer",
                transition: "all 0.2s",
                position: "relative",
                borderColor: currentSelection?.diagramId === diagram.diagramId ? "primary.main" : "divider",
                borderWidth: currentSelection?.diagramId === diagram.diagramId ? 2 : 1,
                boxShadow: currentSelection?.diagramId === diagram.diagramId ? 4 : 0,
                "&:hover": {
                  borderColor: "primary.main",
                  boxShadow: 2,
                },
              }}
            >
              <CardContent sx={{ p: 0, flexGrow: 1, display: "flex", flexDirection: "column" }}>
                {/* Thumbnail */}
                <Box sx={{ position: "relative", paddingTop: "56.25%", bgcolor: "action.hover" }}>
                  {diagram.thumbnail ? (
                    <Box
                      component="img"
                      src={diagram.thumbnail}
                      alt={diagram.name}
                      sx={{
                        position: "absolute",
                        top: 0,
                        left: 0,
                        width: "100%",
                        height: "100%",
                        objectFit: "cover",
                      }}
                    />
                  ) : (
                    <Box
                      sx={{
                        position: "absolute",
                        top: 0,
                        left: 0,
                        width: "100%",
                        height: "100%",
                        display: "flex",
                        alignItems: "center",
                        justifyContent: "center",
                      }}
                    >
                      <DatabaseIcon sx={{ fontSize: 48, color: "text.secondary", opacity: 0.5 }} />
                    </Box>
                  )}

                  {/* Selection indicator */}
                  {currentSelection?.diagramId === diagram.diagramId && (
                    <Box
                      sx={{
                        position: "absolute",
                        top: 8,
                        right: 8,
                        bgcolor: "primary.main",
                        color: "white",
                        borderRadius: "50%",
                        p: 0.5,
                        display: "flex",
                        boxShadow: 2,
                      }}
                    >
                      <CheckIcon fontSize="small" />
                    </Box>
                  )}
                </Box>

                {/* Info */}
                <Box sx={{ p: 2, flexGrow: 1 }}>
                  <Typography
                    variant="subtitle1"
                    fontWeight={600}
                    sx={{
                      mb: 0.5,
                      color: currentSelection?.diagramId === diagram.diagramId ? "primary.main" : "text.primary",
                      display: "-webkit-box",
                      WebkitLineClamp: 1,
                      WebkitBoxOrient: "vertical",
                      overflow: "hidden",
                    }}
                    title={diagram.name}
                  >
                    {diagram.name}
                  </Typography>
                  <Box
                    sx={{
                      display: "flex",
                      alignItems: "center",
                      gap: 0.5,
                      color: "text.secondary",
                    }}
                  >
                    <CalendarIcon sx={{ fontSize: 12 }} />
                    <Typography variant="caption">
                      Created {new Date(diagram.createdAt).toLocaleDateString()}
                    </Typography>
                  </Box>
                </Box>
              </CardContent>
            </Card>
          </Box>
        ))
      )}
    </Box>
  );
}