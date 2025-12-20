import {
	Avatar,
	Box,
	Button,
	Card,
	CardContent,
	CardMedia,
	Chip,
	CircularProgress,
	Dialog,
	DialogActions,
	DialogContent,
	DialogTitle,
	FormControl,
	IconButton,
	InputLabel,
	ListItemIcon,
	ListItemText,
	MenuItem,
	MenuList,
	Popover,
	Select,
	TextField,
	Tooltip,
	Typography,
} from "@mui/material";
import { useEffect, useState } from "react";

import AdminPanelSettingsIcon from "@mui/icons-material/AdminPanelSettings";
import DeleteIcon from "@mui/icons-material/Delete";
import DriveFileRenameOutlineIcon from "@mui/icons-material/DriveFileRenameOutline";
import EditIcon from "@mui/icons-material/Edit";
import InsertPhotoIcon from "@mui/icons-material/InsertPhoto";
import MoreVertIcon from "@mui/icons-material/MoreVert";
import ShareIcon from "@mui/icons-material/Share";
import VisibilityIcon from "@mui/icons-material/Visibility";

import "./card.css";
import Contributors from "./Contributors.jsx";
import { deleteDiagram, renameDiagram, shareDiagram } from "./fetch.js";

import { useNotification } from "../../components/NotificationContext";

function getInitials(name) {
	if (!name) return "";
	const parts = name.trim().split(/\s+/);
	if (parts.length === 1) return (parts[0][0] || "").toUpperCase();
	return ((parts[0][0] || "") + (parts[1][0] || "")).toUpperCase();
}

function getRoleConfig(role) {
	const configs = {
		OWNER: {
			label: "Owner",
			icon: <AdminPanelSettingsIcon sx={{ fontSize: 14 }} />,
			color: "primary",
		},
		WRITER: {
			label: "Editor",
			icon: <EditIcon sx={{ fontSize: 14 }} />,
			color: "secondary",
		},
		READER: {
			label: "Reader",
			icon: <VisibilityIcon sx={{ fontSize: 14 }} />,
			color: "default",
		},
	};
	return configs[role] || configs.READER;
}

export default function DiagramCard({ d = {}, onOpen, onUpdate, onDelete }) {
	const { showSuccess, showError } = useNotification();

	const [menuAnchor, setMenuAnchor] = useState(null);
	const [contributorsAnchor, setContributorsAnchor] = useState(null);
	const [localContributors, setLocalContributors] = useState(d.contributors || []);

	useEffect(() => {
		setLocalContributors(d.contributors || []);
	}, [d.contributors]);

	const [renameOpen, setRenameOpen] = useState(false);
	const [newName, setNewName] = useState("");
	const [renameLoading, setRenameLoading] = useState(false);

	const [shareOpen, setShareOpen] = useState(false);
	const [shareUsername, setShareUsername] = useState("");
	const [shareRole, setShareRole] = useState("READER");
	const [shareLoading, setShareLoading] = useState(false);

	const [deleteOpen, setDeleteOpen] = useState(false);
	const [deleteLoading, setDeleteLoading] = useState(false);

	const userRole = d?.role || "READER";
	const isOwner = userRole === "OWNER";
	const roleConfig = getRoleConfig(userRole);

	function openMenu(e) {
		e.stopPropagation();
		setMenuAnchor(e.currentTarget);
	}

	function closeMenu() {
		setMenuAnchor(null);
	}

	function handleRenameClick() {
		setNewName(d?.name || "");
		setRenameOpen(true);
	}

	async function handleRenameSubmit() {
		if (!newName.trim()) return;
		setRenameLoading(true);
		try {
			const res = await renameDiagram(d.diagramId, newName);
			const updated = res?.diagram || { ...d, name: newName };
			setRenameOpen(false);
			if (onUpdate) onUpdate(updated);
			showSuccess("Diagram renamed successfully");
		
		} catch (err) {
			showError(err.message || "Failed to rename diagram");
		
		} finally {
			setRenameLoading(false);
		}
	}

	function handleShareClick() {
		setShareUsername("");
		setShareRole("READER");
		setShareOpen(true);
	}

	async function handleShareSubmit() {
		if (!shareUsername.trim()) return;
		setShareLoading(true);
		try {
			const response = await shareDiagram(d.diagramId, shareUsername, shareRole);
			const newContributor = {
				name: shareUsername,
				image: response?.picture || "",
				role: shareRole,
			};

			const updatedContributors = [...localContributors, newContributor];
			setLocalContributors(updatedContributors);
			const updatedDiagram = { ...d, contributors: updatedContributors };
			
			if (onUpdate) onUpdate(updatedDiagram);
			setShareOpen(false);
			showSuccess(`Shared with ${shareUsername}`);
		
		} catch (err) {
			showError(err.message);
		
		} finally {
			setShareLoading(false);
		}
	}

	function handleDeleteClick() {
		setDeleteOpen(true);
	}

	async function handleDeleteConfirm() {
		setDeleteLoading(true);
		try {
			await deleteDiagram(d.diagramId);
			setDeleteOpen(false);
			if (onDelete) onDelete(d);
			showSuccess("Diagram deleted successfully");
		
		} catch (err) {
			showError(err.message || "Failed to delete diagram");
		
		} finally {
			setDeleteLoading(false);
		}
	}

	function handleContributorsClick(e) {
		e.stopPropagation();
		setContributorsAnchor(e.currentTarget);
	}

	function handleContributorsClose() {
		setContributorsAnchor(null);
	}

	function handleCardClick(e) {
		if (contributorsAnchor) {
			handleContributorsClose();
			e.stopPropagation();
			return;
		}
		if (menuAnchor) {
			closeMenu();
			e.stopPropagation();
			return;
		}
		if (onOpen) onOpen(d);
	}

	return (
		<>
			<Card className="diagram-card" onClick={handleCardClick} sx={{ position: "relative" }}>
				{d.thumbnail ? (
					<CardMedia
						component="img"
						height="210"
						image={d.thumbnail}
						alt={d.name}
						className="thumbnail"
					/>
				) : (
					<Box className="thumbnail thumbnail-placeholder">
						<InsertPhotoIcon
							sx={{ fontSize: 56, color: "var(--placeholder, #9aa4b2)", opacity: 0.5 }}
						/>
					</Box>
				)}

				<Popover
					open={Boolean(menuAnchor)}
					anchorEl={menuAnchor}
					onClose={closeMenu}
					anchorOrigin={{ vertical: "bottom", horizontal: "right" }}
					transformOrigin={{ vertical: "top", horizontal: "right" }}
				>
					<MenuList sx={{ width: 200 }}>
						{isOwner && (
							<MenuItem
								onClick={(e) => {
									e.stopPropagation();
									closeMenu();
									handleRenameClick();
								}}
							>
								<ListItemIcon>
									<DriveFileRenameOutlineIcon fontSize="small" />
								</ListItemIcon>
								<ListItemText>Rename</ListItemText>
							</MenuItem>
						)}

						{isOwner && (
							<MenuItem
								onClick={(e) => {
									e.stopPropagation();
									closeMenu();
									handleShareClick();
								}}
							>
								<ListItemIcon>
									<ShareIcon fontSize="small" />
								</ListItemIcon>
								<ListItemText>Share</ListItemText>
							</MenuItem>
						)}

						<MenuItem
							onClick={(e) => {
								e.stopPropagation();
								closeMenu();
								handleDeleteClick();
							}}
						>
							<ListItemIcon>
								<DeleteIcon fontSize="small" />
							</ListItemIcon>
							<ListItemText>Delete</ListItemText>
						</MenuItem>

						{!isOwner && (
							<MenuItem disabled>
								<ListItemText>
									<Typography variant="body2" color="text.secondary">
										Limited access
									</Typography>
								</ListItemText>
							</MenuItem>
						)}
					</MenuList>
				</Popover>

				<CardContent className="card-content">
					<Box className="content-row" sx={{ alignItems: "flex-start" }}>
						<Box className="text-section" sx={{ pr: 1 }}>
							<Typography variant="h6" noWrap>
								{d?.name}
							</Typography>

							<Typography variant="subtitle2" color="text.secondary" display="block">
								Created: {d.createdAt}
							</Typography>

							<Typography variant="subtitle2" color="text.secondary" display="block">
								Modified:{" "}
								{d.lastModified}
							</Typography>
						</Box>
					</Box>

					<IconButton
						aria-label="more"
						onClick={openMenu}
						className="always-visible-icon"
					>
						<MoreVertIcon />
					</IconButton>

					<Box
						sx={{
							display: "flex",
							justifyContent: "space-between",
							alignItems: "center",
						}}
					>
						<Chip
							icon={roleConfig.icon}
							label={roleConfig.label}
							size="small"
							color={roleConfig.color}
							sx={{
								fontWeight: 600,
								fontSize: 12,
								height: 28,
							}}
						/>

						<Box
							className="avatar-stack"
							sx={{
								display: "flex",
								alignItems: "center",
								cursor: "pointer",
							}}
							onClick={handleContributorsClick}
						>
							{localContributors.slice(0, 6).map((c, i) => (
								<Box key={i} sx={{ zIndex: localContributors.length - i }}>
									<Tooltip title={c.name}>
										<Avatar
											src={c.picture || undefined}
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
											}}
										>
											{!c.picture && getInitials(c.name)}
										</Avatar>
									</Tooltip>
								</Box>
							))}

							{localContributors.length > 6 && (
								<Avatar
									sx={{
										width: 32,
										height: 32,
										fontSize: 12,
										ml: -1.2,
										border: "2px solid white",
									}}
								>
									+{localContributors.length - 6}
								</Avatar>
							)}
						</Box>
					</Box>
				</CardContent>

				<Contributors
					open={Boolean(contributorsAnchor)}
					onClose={handleContributorsClose}
					contributors={localContributors}
					setOuterContributors={setLocalContributors}
					currentUserRole={userRole}
					currentUserId={d.currentUserId}
					diagramId={d.diagramId}
					getInitials={getInitials}
				/>
			</Card>

			<Dialog open={renameOpen} onClose={() => setRenameOpen(false)} maxWidth="sm" fullWidth>
				<DialogTitle>Rename Diagram</DialogTitle>
				<DialogContent>
					<TextField
						autoFocus
						margin="dense"
						label="New Name"
						fullWidth
						variant="outlined"
						value={newName}
						onChange={(e) => setNewName(e.target.value)}
						onKeyPress={(e) => {
							if (e.key === "Enter" && !renameLoading && newName.trim()) {
								handleRenameSubmit();
							}
						}}
					/>
				</DialogContent>
				<DialogActions>
					<Button onClick={() => setRenameOpen(false)} disabled={renameLoading}>
						Cancel
					</Button>
					<Button
						onClick={handleRenameSubmit}
						variant="contained"
						disabled={renameLoading || !newName.trim()}
					>
						{renameLoading ? <CircularProgress size={20} /> : "Rename"}
					</Button>
				</DialogActions>
			</Dialog>

			<Dialog open={shareOpen} onClose={() => setShareOpen(false)} maxWidth="sm" fullWidth>
				<DialogTitle>Share Diagram</DialogTitle>
				<DialogContent>
					<TextField
						autoFocus
						margin="dense"
						label="Username"
						fullWidth
						variant="outlined"
						value={shareUsername}
						onChange={(e) => setShareUsername(e.target.value)}
						sx={{ mb: 2, mt: 1 }}
					/>
					<FormControl fullWidth variant="outlined">
						<InputLabel id="role-label">Role</InputLabel>
						<Select
							labelId="role-label"
							value={shareRole}
							onChange={(e) => setShareRole(e.target.value)}
							label="Role"
						>
							<MenuItem value="READER">Reader</MenuItem>
							<MenuItem value="WRITER">Editor</MenuItem>
						</Select>
					</FormControl>
				</DialogContent>
				<DialogActions>
					<Button onClick={() => setShareOpen(false)} disabled={shareLoading}>
						Cancel
					</Button>
					<Button
						onClick={handleShareSubmit}
						variant="contained"
						disabled={shareLoading || !shareUsername.trim()}
					>
						{shareLoading ? <CircularProgress size={20} /> : "Share"}
					</Button>
				</DialogActions>
			</Dialog>

			<Dialog open={deleteOpen} onClose={() => setDeleteOpen(false)} maxWidth="sm" fullWidth>
				<DialogTitle>Delete Diagram</DialogTitle>
				<DialogContent>
					<Typography>
						Are you sure you want to delete "{d?.name}"? This action cannot be undone.
					</Typography>
				</DialogContent>
				<DialogActions>
					<Button onClick={() => setDeleteOpen(false)} disabled={deleteLoading}>
						Cancel
					</Button>
					<Button
						onClick={handleDeleteConfirm}
						variant="contained"
						color="error"
						disabled={deleteLoading}
					>
						{deleteLoading ? <CircularProgress size={20} /> : "Delete"}
					</Button>
				</DialogActions>
			</Dialog>
		</>
	);
}
