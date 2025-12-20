import { Card, CardContent, CardHeader, Typography, Avatar, Box, Tooltip } from '@mui/material';
import {
  Group as UsersIcon,
  EmojiEvents as CrownIcon,
  Edit as EditIcon,
  Visibility as EyeIcon
} from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';

import { getInitials } from "../../diagrams/Contributors.jsx"

const getRoleIcon = (role) => {
  switch (role.toUpperCase()) {
    case "OWNER":
      return CrownIcon;
    case "WRITER":
    case "EDITOR":
      return EditIcon;
    default:
      return EyeIcon;
  }
};

const getRoleColor = (role) => {
  switch (role.toUpperCase()) {
    case "OWNER":
      return "warning.main";
    case "WRITER":
    case "EDITOR":
      return "info.main";
    default:
      return "text.secondary";
  }
};

const rolePriority = {
  OWNER: 1,
  WRITER: 2,
  EDITOR: 2,
  READER: 3
};

export const ContributorsSection = ({ collaborators, compact = false }) => {
  const navigate = useNavigate();
  const sortedCollaborators = [...collaborators].sort(
    (a, b) => (rolePriority[a.role.toUpperCase()] || 99) - (rolePriority[b.role.toUpperCase()] || 99)
  );

  const handleContributorClick = (user) => {
    if (user?.name) {

      navigate(`/designer/${user.name.replace("@", "").replace("%20", "")}`);
    }
  };

  return (
    <Card variant="outlined">
      <CardHeader
        title={
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            <UsersIcon fontSize="small" />
            <Typography variant="subtitle1" fontWeight="bold">Contributors</Typography>
          </Box>
        }
        sx={{ pb: 1 }}
      />
      <CardContent>
        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
          {sortedCollaborators.map((collaborator) => {
            const RoleIcon = getRoleIcon(collaborator.role);
            const roleColor = getRoleColor(collaborator.role);

            return (
              <Box key={collaborator.name} sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
                <Tooltip title={`${collaborator.name} (${collaborator.role})`}>
                  <Avatar
                    src={collaborator.picture}
                    alt={collaborator.name}
                    sx={{
                      bgcolor: collaborator.picture ? undefined : "primary.main",
                      cursor: "pointer",
                      transition: "0.2s",
                      "&:hover": {
                        transform: "scale(1.07)",
                        boxShadow: 3,
                      },
                      width: 40,
                      height: 40
                    }}
                    onClick={() => handleContributorClick(collaborator)}
                  >
                    {(!collaborator.picture || collaborator.picture.trim() === "") && getInitials(collaborator.name)}
                  </Avatar>
                </Tooltip>
                <Box sx={{ minWidth: 0, flex: 1 }}>
                  <Typography variant="body2" fontWeight={500} noWrap onClick={() => handleContributorClick(collaborator)} sx={{ cursor: 'pointer' }}>
                    {collaborator.name}
                  </Typography>
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
                    <RoleIcon sx={{ fontSize: 12, color: roleColor }} />
                    <Typography variant="caption" color="text.secondary" sx={{ textTransform: 'capitalize' }}>
                      {collaborator.role.toLowerCase()}
                    </Typography>
                  </Box>
                </Box>
              </Box>
            );
          })}
        </Box>
      </CardContent>
    </Card>
  );
};
