import { useState, useEffect } from "react";
import {
    Box, Card, CardContent, Typography, Alert, Snackbar,
    CircularProgress, MenuItem, Select, FormControl, InputLabel, Chip, IconButton, Tooltip,
    Button, Dialog, DialogTitle, DialogContent, DialogActions, TextField
} from "@mui/material";
import { useNavigate } from "react-router-dom";

import StorageIcon from "@mui/icons-material/Storage";
import DatabaseIcon from "@mui/icons-material/Storage";
import PersonAddIcon from "@mui/icons-material/PersonAdd";
import PeopleIcon from "@mui/icons-material/People";
import AddIcon from "@mui/icons-material/Add";

import TopBars from "../../components/Topbarforcannedquery";

import LeftPanel from "../../components/LeftPanel.jsx";
import { useAuth } from "../../components/AuthProvider.jsx";
import { serversApi } from "./serversApi.js";
import AddUserModal from "./AddUserModal.jsx";
import ViewUsersModal from "./ViewUsersModal.jsx";

import "./ServersPage.css";

export default function ServersPage() {
    const navigate = useNavigate();
    const { user, setUser } = useAuth();

    const [leftNav, setLeftNav] = useState("all");
    const [servers, setServers] = useState([]);
    const [databases, setDatabases] = useState([]);
    const [selectedServerId, setSelectedServerId] = useState("");
    const [loading, setLoading] = useState(true);
    const [snackbar, setSnackbar] = useState({ open: false, message: "", severity: "success" });

    // Add Server State
    const [isAddServerOpen, setIsAddServerOpen] = useState(false);
    const [newServerName, setNewServerName] = useState("");
    const [creatingServer, setCreatingServer] = useState(false);
    const [addUserModalOpen, setAddUserModalOpen] = useState(false);
    const [viewUsersModalOpen, setViewUsersModalOpen] = useState(false);
    const [selectedDatabase, setSelectedDatabase] = useState(null);

    useEffect(() => {
        fetchServersAndDatabases();
    }, []);

    const fetchServersAndDatabases = async () => {
        try {
            setLoading(true);
            const [serversData, databasesData] = await Promise.all([
                serversApi.getUserServers(),
                serversApi.getUserDatabases()
            ]);

            setServers(serversData);
            setDatabases(databasesData.databases || []);

            if (serversData.length > 0) {
                setSelectedServerId(serversData[0].serverId);
            }
        } catch (error) {
            console.error("Error fetching servers and databases:", error);

            // Determine error type and message
            let errorType = "Error";
            let rawErrorMessage = "Failed to load servers and databases";

            if (error.response) {
                // Server responded with error status
                errorType = `Server Error (${error.response.status})`;
                rawErrorMessage = error.response.data?.message || error.response.data || error.message;
            } else if (error.request) {
                // Request was made but no response received
                errorType = "Network Error";
                rawErrorMessage = "No response from server. Please check your connection.";
            } else {
                // Something else happened
                errorType = "Request Error";
                rawErrorMessage = error.message;
            }

            const errorMessage = (typeof rawErrorMessage === 'object')
                ? JSON.stringify(rawErrorMessage)
                : rawErrorMessage;

            showSnackbar(`[${errorType}] ${errorMessage}`, "error");
        } finally {
            setLoading(false);
        }
    };

    const showSnackbar = (message, severity) => setSnackbar({ open: true, message, severity });
    const handleCloseSnackbar = () => setSnackbar({ ...snackbar, open: false });

    const handleOpenAddUserModal = (database) => {
        setSelectedDatabase(database);
        setAddUserModalOpen(true);
    };

    const handleOpenViewUsersModal = (database) => {
        setSelectedDatabase(database);
        setViewUsersModalOpen(true);
    };

    const handleUserAdded = (message, severity = "success") => {
        showSnackbar(message, severity);
        // Don't close modal - let user add multiple users
    };

    const handleServerChange = (event) => {
        setSelectedServerId(event.target.value);
    };
    const filteredDatabases = selectedServerId
        ? databases.filter(db => {
            const server = servers.find(s => s.serverId === selectedServerId);
            return server && db.serverName === server.serverName;
        })
        : [];

    const handleAddServer = async () => {
        if (!newServerName.trim()) return;
        setCreatingServer(true);
        try {
            await serversApi.createServer(newServerName);
            showSnackbar("Server created successfully", "success");
            setNewServerName("");
            setIsAddServerOpen(false);
            fetchServersAndDatabases();
        } catch (error) {
            console.error("Error creating server:", error);
            const errorMessage = error.response?.data?.message || "Failed to create server";
            showSnackbar(errorMessage, "error");
        } finally {
            setCreatingServer(false);
        }
    };

    if (loading) {
        return (
            <Box className="loading-container">
                <CircularProgress />
            </Box>
        );
    }

    return (
        <Box sx={{ display: "flex" }}>
            <LeftPanel leftNav={leftNav} setLeftNav={setLeftNav} />
            <Box sx={{ flexGrow: 1, display: "flex", flexDirection: "column", backgroundColor: "#f6f8fb", minHeight: "100vh" }}>
                <TopBars loadDiagrams={() => { }} />
                <Box sx={{ p: 3 }}>
                    <Box className="servers-header" sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
                        <Box>
                            <Typography variant="h4" className="servers-title">
                                Server Management
                            </Typography>
                            <Typography className="servers-subtitle">
                                Manage your database servers and view connection status
                            </Typography>
                        </Box>
                        <Button
                            variant="contained"
                            startIcon={<AddIcon />}
                            onClick={() => setIsAddServerOpen(true)}
                            sx={{ height: 'fit-content' }}
                        >
                            Add Server
                        </Button>
                    </Box>
                    <Card className="servers-selection-card">
                        <CardContent>
                            <FormControl fullWidth>
                                <InputLabel id="server-select-label">Select Server</InputLabel>
                                <Select
                                    labelId="server-select-label"
                                    id="server-select"
                                    value={selectedServerId}
                                    label="Select Server"
                                    onChange={handleServerChange}
                                    MenuProps={{
                                        disableScrollLock: true,
                                        hideBackdrop: true,
                                        sx: {
                                            pointerEvents: 'none',
                                            "& .MuiPaper-root": {
                                                pointerEvents: 'auto',
                                            }
                                        },
                                        anchorOrigin: {
                                            vertical: 'bottom',
                                            horizontal: 'left',
                                        },
                                        transformOrigin: {
                                            vertical: 'top',
                                            horizontal: 'left',
                                        },
                                    }}
                                >
                                    {servers.map((server) => (
                                        <MenuItem key={server.serverId} value={server.serverId}>
                                            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                                                <StorageIcon fontSize="small" />
                                                {server.serverName}
                                            </Box>
                                        </MenuItem>
                                    ))}
                                </Select>
                            </FormControl>
                        </CardContent>
                    </Card>
                    {selectedServerId && (
                        <Box className="databases-section">
                            <Typography variant="h6" sx={{ mb: 2, fontWeight: 600 }}>
                                Databases ({filteredDatabases.length})
                            </Typography>

                            {filteredDatabases.length === 0 ? (
                                <Box sx={{ textAlign: 'center', py: 8, color: 'text.secondary' }}>
                                    <Typography variant="h6">No databases found</Typography>
                                    <Typography variant="body2">
                                        This server has no databases configured.
                                    </Typography>
                                </Box>
                            ) : (
                                <Box className="databases-grid">
                                    {filteredDatabases.map((database) => (
                                        <Box
                                            key={database.databaseId}
                                            className="database-card"
                                            sx={{ cursor: 'pointer', position: 'relative' }}
                                        >
                                            <Box
                                                onClick={() => navigate(`/database-manager/${database.databaseId}`)}
                                                sx={{ flex: 1 }}
                                            >
                                                <Box className="database-card-header">
                                                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                                                        <Typography variant="h6" className="database-name">
                                                            {database.databaseName}
                                                        </Typography>
                                                        {database.role === 'OWNER' && (
                                                            <Box sx={{ display: 'flex', gap: 0.5 }}>
                                                                <Tooltip title="View Users">
                                                                    <IconButton
                                                                        size="small"
                                                                        onClick={(e) => {
                                                                            e.stopPropagation();
                                                                            handleOpenViewUsersModal(database);
                                                                        }}
                                                                        sx={{
                                                                            color: 'info.main',
                                                                            '&:hover': { backgroundColor: 'info.light' }
                                                                        }}
                                                                    >
                                                                        <PeopleIcon fontSize="small" />
                                                                    </IconButton>
                                                                </Tooltip>
                                                                <Tooltip title="Add User">
                                                                    <IconButton
                                                                        size="small"
                                                                        onClick={(e) => {
                                                                            e.stopPropagation();
                                                                            handleOpenAddUserModal(database);
                                                                        }}
                                                                        sx={{
                                                                            color: 'primary.main',
                                                                            '&:hover': { backgroundColor: 'primary.light' }
                                                                        }}
                                                                    >
                                                                        <PersonAddIcon fontSize="small" />
                                                                    </IconButton>
                                                                </Tooltip>
                                                            </Box>
                                                        )}
                                                    </Box>
                                                    <Chip
                                                        label={database.connected ? "Connected" : "Disconnected"}
                                                        size="small"
                                                        sx={{
                                                            backgroundColor: database.connected ? '#4caf50' : '#f44336',
                                                            color: 'white',
                                                            fontWeight: 600,
                                                            fontSize: '11px',
                                                            height: '24px',
                                                            '&::before': {
                                                                content: '"●"',
                                                                marginRight: '4px',
                                                                fontSize: '10px'
                                                            }
                                                        }}
                                                    />
                                                </Box>
                                                <Typography className="database-server-name">
                                                    Server: {database.serverName}
                                                </Typography>
                                                {database.databaseddl && (
                                                    <Box className="database-ddl">
                                                        <Box className="ddl-preview">
                                                            <pre>
                                                                <code>{database.databaseddl}</code>
                                                            </pre>
                                                        </Box>
                                                    </Box>
                                                )}
                                            </Box>
                                        </Box>
                                    ))}
                                </Box>
                            )}
                        </Box>
                    )}

                    {!selectedServerId && servers.length === 0 && (
                        <Box sx={{ textAlign: 'center', py: 8, color: 'text.secondary' }}>
                            <Typography variant="h6">No servers found</Typography>
                            <Typography variant="body2">
                                You don't have any database servers configured yet.
                            </Typography>
                        </Box>
                    )}
                </Box>
            </Box>

            <Dialog open={isAddServerOpen} onClose={() => setIsAddServerOpen(false)} maxWidth="sm" fullWidth>
                <DialogTitle>Add New Server</DialogTitle>
                <DialogContent>
                    <TextField
                        autoFocus
                        margin="dense"
                        id="name"
                        label="Server Name"
                        type="text"
                        fullWidth
                        variant="outlined"
                        value={newServerName}
                        onChange={(e) => setNewServerName(e.target.value)}
                        placeholder="e.g., Production Server"
                        disabled={creatingServer}
                    />
                </DialogContent>
                <DialogActions>
                    <Button onClick={() => setIsAddServerOpen(false)} color="inherit" disabled={creatingServer}>
                        Cancel
                    </Button>
                    <Button onClick={handleAddServer} variant="contained" color="primary" disabled={!newServerName.trim() || creatingServer}>
                        {creatingServer ? <CircularProgress size={24} color="inherit" /> : "Add Server"}
                    </Button>
                </DialogActions>
            </Dialog>

            <Snackbar
                open={snackbar.open}
                autoHideDuration={4000}
                onClose={handleCloseSnackbar}
                anchorOrigin={{ vertical: "bottom", horizontal: "right" }}
            >
                <Alert onClose={handleCloseSnackbar} severity={snackbar.severity}>
                    {snackbar.message}
                </Alert>
            </Snackbar>

            {selectedDatabase && (
                <>
                    <AddUserModal
                        open={addUserModalOpen}
                        onClose={() => setAddUserModalOpen(false)}
                        databaseId={selectedDatabase.databaseId}
                        databaseName={selectedDatabase.databaseName}
                        onUserAdded={handleUserAdded}
                    />
                    <ViewUsersModal
                        open={viewUsersModalOpen}
                        onClose={() => setViewUsersModalOpen(false)}
                        databaseId={selectedDatabase.databaseId}
                        databaseName={selectedDatabase.databaseName}
                    />
                </>
            )}
        </Box>
    );
}
