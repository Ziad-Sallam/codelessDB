import { useState, useEffect } from "react";
import {
    Dialog, DialogTitle, DialogContent, DialogActions,
    Button, Box, Typography, Avatar, CircularProgress,
    IconButton, List, ListItem, ListItemAvatar, ListItemText,
    Select, MenuItem, FormControl
} from "@mui/material";
import CloseIcon from "@mui/icons-material/Close";
import PeopleIcon from "@mui/icons-material/People";

export default function ViewUsersModal({ open, onClose, databaseId, databaseName }) {
    const [users, setUsers] = useState([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const [updating, setUpdating] = useState(null);

    useEffect(() => {
        if (open && databaseId) {
            fetchUsers();
        }
    }, [open, databaseId]);

    const fetchUsers = async () => {
        try {
            setLoading(true);
            setError(null);
            const response = await fetch(
                `${import.meta.env.VITE_BACKEND_URL}/database/get-database-users/${databaseId}`,
                {
                    headers: {
                        'Authorization': `Bearer ${localStorage.getItem('authToken')}`,
                        'Content-Type': 'application/json'
                    }
                }
            );

            if (!response.ok) {
                throw new Error('Failed to fetch users');
            }

            const data = await response.json();
            setUsers(data || []);
        } catch (error) {
            console.error("Error fetching users:", error);
            setError(error.message);
            setUsers([]);
        } finally {
            setLoading(false);
        }
    };

    const handleRoleChange = async (userId, newRole) => {
        try {
            setUpdating(userId);
            const response = await fetch(
                `${import.meta.env.VITE_BACKEND_URL}/database/update-user-role`,
                {
                    method: 'PUT',
                    headers: {
                        'Authorization': `Bearer ${localStorage.getItem('authToken')}`,
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify({
                        databaseId: databaseId,
                        userId: userId,
                        role: newRole
                    })
                }
            );

            if (!response.ok) {
                const errorText = await response.text();
                throw new Error(errorText || 'Failed to update role');
            }

            // Update local state
            setUsers(users.map(user =>
                user.userId === userId ? { ...user, role: newRole } : user
            ));
        } catch (error) {
            console.error("Error updating role:", error);
            alert(error.message);
            // Refresh to get correct state
            fetchUsers();
        } finally {
            setUpdating(null);
        }
    };

    const getRoleColor = (role) => {
        switch (role) {
            case 'OWNER':
                return '#d32f2f';
            case 'WRITER':
                return '#ed6c02';
            case 'READER':
                return '#2e7d32';
            default:
                return '#757575';
        }
    };

    const handleClose = () => {
        setUsers([]);
        setError(null);
        onClose();
    };

    return (
        <Dialog
            open={open}
            onClose={handleClose}
            maxWidth="sm"
            fullWidth
            PaperProps={{
                sx: {
                    borderRadius: 2,
                    minHeight: '400px'
                }
            }}
        >
            <DialogTitle sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', pb: 2 }}>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                    <PeopleIcon />
                    <Typography variant="h6">Users - {databaseName}</Typography>
                </Box>
                <IconButton onClick={handleClose} size="small">
                    <CloseIcon />
                </IconButton>
            </DialogTitle>

            <DialogContent sx={{ pt: 1 }}>
                {loading ? (
                    <Box sx={{ display: 'flex', justifyContent: 'center', py: 8 }}>
                        <CircularProgress />
                    </Box>
                ) : error ? (
                    <Box sx={{ textAlign: 'center', py: 8, color: 'error.main' }}>
                        <Typography variant="h6">Error</Typography>
                        <Typography variant="body2">{error}</Typography>
                    </Box>
                ) : users.length === 0 ? (
                    <Box sx={{ textAlign: 'center', py: 8, color: 'text.secondary' }}>
                        <Typography variant="h6">No users found</Typography>
                        <Typography variant="body2">
                            This database has no users with access yet.
                        </Typography>
                    </Box>
                ) : (
                    <List>
                        {users.map((user) => (
                            <ListItem
                                key={user.userId}
                                sx={{
                                    border: '1px solid #e0e0e0',
                                    borderRadius: 1,
                                    mb: 1,
                                    '&:hover': {
                                        backgroundColor: '#f5f5f5'
                                    }
                                }}
                            >
                                <ListItemAvatar>
                                    <Avatar src={user.picture} alt={user.username}>
                                        {!user.picture && user.username?.charAt(0).toUpperCase()}
                                    </Avatar>
                                </ListItemAvatar>
                                <ListItemText
                                    primary={
                                        <Typography variant="subtitle1" fontWeight={600}>
                                            {user.username}
                                        </Typography>
                                    }
                                    secondary={
                                        <Typography variant="body2" color="text.secondary">
                                            {user.email}
                                        </Typography>
                                    }
                                />
                                {user.role === 'OWNER' ? (
                                    <Box
                                        sx={{
                                            px: 2,
                                            py: 0.5,
                                            borderRadius: 1,
                                            backgroundColor: getRoleColor(user.role),
                                            color: 'white',
                                            fontWeight: 600,
                                            fontSize: '0.875rem'
                                        }}
                                    >
                                        OWNER
                                    </Box>
                                ) : (
                                    <FormControl size="small" sx={{ minWidth: 120 }}>
                                        <Select
                                            value={user.role}
                                            onChange={(e) => handleRoleChange(user.userId, e.target.value)}
                                            disabled={updating === user.userId}
                                            sx={{
                                                backgroundColor: getRoleColor(user.role),
                                                color: 'white',
                                                fontWeight: 600,
                                                '& .MuiOutlinedInput-notchedOutline': {
                                                    borderColor: 'transparent'
                                                },
                                                '&:hover .MuiOutlinedInput-notchedOutline': {
                                                    borderColor: 'white'
                                                },
                                                '&.Mui-focused .MuiOutlinedInput-notchedOutline': {
                                                    borderColor: 'white'
                                                },
                                                '& .MuiSvgIcon-root': {
                                                    color: 'white'
                                                }
                                            }}
                                        >
                                            <MenuItem value="READER">READER</MenuItem>
                                            <MenuItem value="WRITER">WRITER</MenuItem>
                                        </Select>
                                    </FormControl>
                                )}
                            </ListItem>
                        ))}
                    </List>
                )}
            </DialogContent>

            <DialogActions sx={{ px: 3, pb: 3 }}>
                <Button onClick={handleClose} variant="outlined">
                    Close
                </Button>
            </DialogActions>
        </Dialog>
    );
}
