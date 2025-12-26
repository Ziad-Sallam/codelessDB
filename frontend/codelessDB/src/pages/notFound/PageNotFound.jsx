import { Box, Typography, Button, Container } from "@mui/material";
import { useNavigate } from "react-router-dom";
import { Home as HomeIcon, ArrowBack as ArrowBackIcon } from "@mui/icons-material";
import { useAuth } from "../../components/AuthProvider.jsx";
import Snowfall from 'react-snowfall';

export default function PageNotFound() {
  const navigate = useNavigate();
  const { user } = useAuth();

  return (
    <Box
      sx={{
        minHeight: "100vh",
        display: "flex",
        alignItems: "center",
        justifyContent: "center",
        bgcolor: "background.light",
        background: "linear-gradient(135deg, #0b3d91 0%, #082043 100%)",
      }}
    >
      <Snowfall color={"#82c3d9"}/>
      <Container maxWidth="sm">
        <Box
          sx={{
            textAlign: "center",
            bgcolor: "white",
            borderRadius: 4,
            p: 6,
            boxShadow: "0 20px 60px rgba(0,0,0,0.4)",
          }}
        >
          <Typography
            variant="h4"
            fontWeight="bold"
            gutterBottom
            sx={{ color: "primary.main", mb: 2 }}
          >
            Page Not Found
          </Typography>

          <Typography
            variant="body1"
            color="text.secondary"
            sx={{ mb: 4, maxWidth: 400, mx: "auto" }}
          >
            Oops! The page you're looking for doesn't exist. It might have been
            moved or deleted.
          </Typography>

          <Box sx={{ display: "flex", gap: 2, justifyContent: "center", flexWrap: "wrap" }}>
            <Button
              variant="contained"
              size="large"
              startIcon={<HomeIcon />}
              onClick={() => navigate(user == null ? "/login" : "/diagrams")}
              sx={{
                px: 4,
                py: 1.5,
                borderRadius: 2,
                textTransform: "none",
                fontSize: "1rem",
                background: "linear-gradient(135deg, #0b3d91 0%, #4C84FF 100%)",
                "&:hover": {
                  boxShadow: "0 8px 25px rgba(11, 61, 145, 0.4)",
                  transform: "translateY(-2px)",
                },
                transition: "all 0.2s ease-in-out",
              }}
            >
              {user == null ? "Login" : "Go to your Diagrams"}
            </Button>

            <Button
              variant="outlined"
              size="large"
              startIcon={<ArrowBackIcon />}
              onClick={() => navigate(-1)}
              sx={{
                px: 4,
                py: 1.5,
                borderRadius: 2,
                textTransform: "none",
                fontSize: "1rem",
                borderColor: "primary.main",
                color: "primary.main",
                "&:hover": {
                  borderColor: "primary.dark",
                  bgcolor: "action.hover",
                },
              }}
            >
              Go Back
            </Button>
          </Box>
        </Box>
      </Container>
    </Box>
  );
}
