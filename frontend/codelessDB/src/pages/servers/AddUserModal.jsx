import { useState, useEffect } from "react";
import {
    Dialog, DialogTitle, DialogContent, DialogActions,
    TextField, Button, Box, Typography, Avatar, Chip,
    CircularProgress, IconButton, MenuItem, Select, FormControl, InputLabel,
    Pagination
} from "@mui/material";
import CloseIcon from "@mui/icons-material/Close";
import PersonAddIcon from "@mui/icons-material/PersonAdd";
import SearchIcon from "@mui/icons-material/Search";

export default function AddUserModal({ open, onClose, databaseId, databaseName, onUserAdded }) {
    const [searchQuery, setSearchQuery] = useState("");
    const [searchResults, setSearchResults] = useState([]);
    const [loading, setLoading] = useState(false);
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [selectedRoles, setSelectedRoles] = useState({});

    useEffect(() => {
        if (open && searchQuery.trim()) {
            const timeoutId = setTimeout(() => {
                searchUsers();
            }, 500); // Debounce search by 500ms
            return () => clearTimeout(timeoutId);
        } else if (open && !searchQuery.trim()) {
            searchUsers(); // Load initial results
        }
    }, [searchQuery, page, open]);

    const searchUsers = async () => {
        try {
            setLoading(true);
            const response = await fetch(
                `${import.meta.env.VITE_BACKEND_URL}/user/search?query=${encodeURIComponent(searchQuery)}&page=${page}&size=10&excludeDatabaseId=${databaseId}`,
                {
                    headers: {
                        'Authorization': `Bearer ${localStorage.getItem('authToken')}`,
                        'Content-Type': 'application/json'
                    }
                }
            );

            if (!response.ok) {
                throw new Error('Failed to search users');
            }

            const data = await response.json();
            setSearchResults(data.content || []);
            setTotalPages(data.totalPages || 0);
        } catch (error) {
            console.error("Error searching users:", error);
            setSearchResults([]);
        } finally {
            setLoading(false);
        }
    };

    const handleAddUser = async (userId) => {
        const role = selectedRoles[userId] || "READER";

        try {
            const response = await fetch(
                `${import.meta.env.VITE_BACKEND_URL}/database/add-database-to-user`,
                {
                    method: 'POST',
                    headers: {
                        'Authorization': `Bearer ${localStorage.getItem('authToken')}`,
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify({
                        databaseId: databaseId,
                        userId: userId,
                        role: role
                    })
                }
            );

            if (!response.ok) {
                const errorText = await response.text();
                throw new Error(errorText || 'Failed to add user');
            }

            // Remove the user from search results
            setSearchResults(searchResults.filter(user => user.id !== userId));

            // Show success message but don't close modal
            onUserAdded?.(`User added successfully with ${role} role`, 'success');
        } catch (error) {
            console.error("Error adding user:", error);
            onUserAdded?.(error.message, 'error');
        }
    };

    const handleRoleChange = (userId, role) => {
        setSelectedRoles(prev => ({ ...prev, [userId]: role }));
    };

    const handleClose = () => {
        setSearchQuery("");
        setSearchResults([]);
        setPage(0);
        setSelectedRoles({});
        onClose();
    };

    const handlePageChange = (event, value) => {
        setPage(value - 1); // MUI Pagination is 1-indexed, API is 0-indexed
    };

    return (
        <Dialog
            open={open}
            onClose={handleClose}
            maxWidth="md"
            fullWidth
            PaperProps={{
                sx: {
                    borderRadius: 2,
                    minHeight: '500px'
                }
            }}
        >
            <DialogTitle sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', pb: 2 }}>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                    <PersonAddIcon />
                    <Typography variant="h6">Add User to {databaseName}</Typography>
                </Box>
                <IconButton onClick={handleClose} size="small">
                    <CloseIcon />
                </IconButton>
            </DialogTitle>

            <DialogContent sx={{ pt: 1 }}>
                <TextField
                    fullWidth
                    placeholder="Search by username or bio..."
                    value={searchQuery}
                    onChange={(e) => setSearchQuery(e.target.value)}
                    sx={{ mb: 3 }}
                    InputProps={{
                        startAdornment: <SearchIcon sx={{ mr: 1, color: 'text.secondary' }} />
                    }}
                />

                {loading ? (
                    <Box sx={{ display: 'flex', justifyContent: 'center', py: 8 }}>
                        <CircularProgress />
                    </Box>
                ) : searchResults.length === 0 ? (
                    <Box sx={{ textAlign: 'center', py: 8, color: 'text.secondary' }}>
                        <Typography variant="h6">No users found</Typography>
                        <Typography variant="body2">
                            {searchQuery ? "Try a different search term" : "Start typing to search for users"}
                        </Typography>
                    </Box>
                ) : (
                    <>
                        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, mb: 2 }}>
                            {searchResults.map((user) => (
                                <Box
                                    key={user.id}
                                    sx={{
                                        display: 'flex',
                                        alignItems: 'center',
                                        gap: 2,
                                        p: 2,
                                        border: '1px solid #e0e0e0',
                                        borderRadius: 1,
                                        '&:hover': {
                                            backgroundColor: '#f5f5f5'
                                        }
                                    }}
                                >
                                    <Avatar
                                        src={user.picture}
                                        alt={user.username}
                                        sx={{ width: 48, height: 48 }}
                                    >
                                        {!user.picture && user.username?.charAt(0).toUpperCase()}
                                    </Avatar>
                                    <Box sx={{ flex: 1 }}>
                                        <Typography variant="subtitle1" fontWeight={600}>
                                            {user.username}
                                        </Typography>
                                        <Typography variant="body2" color="text.secondary">
                                            {user.email}
                                        </Typography>
                                        {user.bio && (
                                            <Typography variant="caption" color="text.secondary" sx={{ display: 'block', mt: 0.5 }}>
                                                {user.bio}
                                            </Typography>
                                        )}
                                    </Box>
                                    <FormControl sx={{ minWidth: 120 }}>
                                        <InputLabel size="small">Role</InputLabel>
                                        <Select
                                            size="small"
                                            value={selectedRoles[user.id] || "READER"}
                                            label="Role"
                                            onChange={(e) => handleRoleChange(user.id, e.target.value)}
                                        >
                                            <MenuItem value="READER">Reader</MenuItem>
                                            <MenuItem value="WRITER">Writer</MenuItem>
                                        </Select>
                                    </FormControl>
                                    <Button
                                        variant="contained"
                                        size="small"
                                        startIcon={<PersonAddIcon />}
                                        onClick={() => handleAddUser(user.id)}
                                    >
                                        Add
                                    </Button>
                                </Box>
                            ))}
                        </Box>

                        {totalPages > 1 && (
                            <Box sx={{ display: 'flex', justifyContent: 'center', mt: 3 }}>
                                <Pagination
                                    count={totalPages}
                                    page={page + 1}
                                    onChange={handlePageChange}
                                    color="primary"
                                />
                            </Box>
                        )}
                    </>
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
