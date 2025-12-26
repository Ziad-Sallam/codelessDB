import {
	Alert,
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
import PublicIcon from "@mui/icons-material/Public";
import PublicOffIcon from "@mui/icons-material/PublicOff";
import ShareIcon from "@mui/icons-material/Share";
import VisibilityIcon from "@mui/icons-material/Visibility";

import "./card.css";
import Contributors from "./Contributors.jsx";
import { deleteDiagram, renameDiagram, shareDiagram, unpublishDiagram } from "./fetch.js";

import { useNotification } from "../../components/NotificationContext";
import EditPublicDetailsModal from "./modals/EditPublicDetailsModal";

// Card-level error handling
const CardErrorHandler = {
	validateInput: (value, fieldName) => {
		if (!value?.toString().trim()) {
			throw new Error(`${fieldName} cannot be empty`);
		}
		return value.trim();
	},
	handleActionError: (action, error) => {
		console.error(`[Card Action: ${action}]`, {
			message: error?.message,
			status: error?.response?.status,
			timestamp: new Date().toISOString(),
		});
		return error?.message || `Failed to ${action}. Please try again.`;
	},
};

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
	const [localContributors, setLocalContributors] = useState(d.contributorDtos || d.contributors || []);

	useEffect(() => {
		setLocalContributors(d.contributorDtos || d.contributors || []);
	}, [d.contributorDtos, d.contributors]);

	const [renameOpen, setRenameOpen] = useState(false);
	const [newName, setNewName] = useState("");
	const [renameLoading, setRenameLoading] = useState(false);
	const [renameError, setRenameError] = useState(null);

	const [shareOpen, setShareOpen] = useState(false);
	const [shareUsername, setShareUsername] = useState("");
	const [shareRole, setShareRole] = useState("READER");
	const [shareLoading, setShareLoading] = useState(false);
	const [shareError, setShareError] = useState(null);

	const [deleteOpen, setDeleteOpen] = useState(false);
	const [deleteLoading, setDeleteLoading] = useState(false);
	const [deleteError, setDeleteError] = useState(null);

	const [unpublishOpen, setUnpublishOpen] = useState(false);
	const [unpublishLoading, setUnpublishLoading] = useState(false);
	const [unpublishError, setUnpublishError] = useState(null);

	const [editOpen, setEditOpen] = useState(false);

	const userRole = d?.role || "READER";
	const isOwner = userRole === "OWNER";
	const isPublic = d?.public || false;
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
		setRenameError(null);
		try {
			if (!newName.trim()) {
				throw new Error("Name cannot be empty");
			}
			setRenameLoading(true);
			const res = await renameDiagram(d.diagramId, newName.trim());
			const updated = res?.diagram || { ...d, name: newName.trim() };
			setRenameOpen(false);
			if (onUpdate) onUpdate(updated);
			showSuccess?.("Diagram renamed successfully");

		} catch (err) {
			const errMsg = CardErrorHandler.handleActionError("rename", err);
			setRenameError(errMsg);
			showError?.(errMsg);

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
		setShareError(null);
		try {
			if (!shareUsername.trim()) {
				throw new Error("Username cannot be empty");
			}
			setShareLoading(true);
			const response = await shareDiagram(d.diagramId, shareUsername.trim(), shareRole);
			const newContributor = {
				name: shareUsername.trim(),
				picture: response?.picture || response?.contributor?.picture || "",
				role: shareRole,
			};

			const updatedContributors = [...localContributors, newContributor];
			setLocalContributors(updatedContributors);
			const updatedDiagram = {
				...d,
				contributors: updatedContributors,
				contributorDtos: updatedContributors
			};

			if (onUpdate) onUpdate(updatedDiagram);
			setShareOpen(false);
			showSuccess?.(`Shared with ${shareUsername.trim()}`);

		} catch (err) {
			const errMsg = CardErrorHandler.handleActionError("share", err);
			setShareError(errMsg);
			showError?.(errMsg);

		} finally {
			setShareLoading(false);
		}
	}

	function handleDeleteClick() {
		if (isPublic) {
			showError?.("Cannot delete a public diagram. Please unpublish it first.");
			return;
		}
		setDeleteError(null);
		setDeleteOpen(true);
	}

	async function handleDeleteConfirm() {
		setDeleteError(null);
		try {
			setDeleteLoading(true);
			await deleteDiagram(d.diagramId);
			setDeleteOpen(false);
			if (onDelete) onDelete(d);
			showSuccess?.("Diagram deleted successfully");

		} catch (err) {
			const errMsg = CardErrorHandler.handleActionError("delete", err);
			setDeleteError(errMsg);
			showError?.(errMsg);

		} finally {
			setDeleteLoading(false);
		}
	}

	function handleUnpublishClick() {
		setUnpublishError(null);
		setUnpublishOpen(true);
	}

	async function handleUnpublishConfirm() {
		setUnpublishError(null);
		try {
			setUnpublishLoading(true);
			const resp = await unpublishDiagram(d.diagramId);
			const updatedDiagram = { ...d, public: false };
			if (onUpdate) onUpdate(updatedDiagram);
			setUnpublishOpen(false);
			showSuccess?.(resp || "Diagram unpublished successfully");
		} catch (err) {
			const errMsg = CardErrorHandler.handleActionError("unpublish", err);
			setUnpublishError(errMsg);
			showError?.(errMsg);
		} finally {
			setUnpublishLoading(false);
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
			<Card className="diagram-card" onClick={handleCardClick}>
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

						{isOwner && isPublic && (
							<MenuItem
								onClick={(e) => {
									e.stopPropagation();
									closeMenu();
									handleUnpublishClick();
								}}
							>
								<ListItemIcon>
									<PublicOffIcon fontSize="small" />
								</ListItemIcon>
								<ListItemText>Unpublish</ListItemText>
							</MenuItem>
						)}

						{isOwner && isPublic && (
							<MenuItem
								onClick={(e) => {
									e.stopPropagation();
									closeMenu();
									setEditOpen(true);
								}}
							>
								<ListItemIcon>
									<EditIcon fontSize="small" />
								</ListItemIcon>
								<ListItemText>Edit Public Details</ListItemText>
							</MenuItem>
						)}

						{isOwner && (
							<MenuItem
								onClick={(e) => {
									e.stopPropagation();
									closeMenu();
									handleDeleteClick();
								}}
								disabled={isPublic}
							>
								<ListItemIcon>
									<DeleteIcon fontSize="small" />
								</ListItemIcon>
								<ListItemText>
									Delete
								</ListItemText>
							</MenuItem>
						)}

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

				<CardContent className="card-content" sx={{ flexGrow: 1, display: "flex", flexDirection: "column", justifyContent: "space-between", px: 2, py: 2 }}>
					{/* Title and options button row */}
					<Box sx={{ display: "flex", justifyContent: "space-between", alignItems: "flex-start", gap: 1, mb: 1 }}>
						<Typography variant="h6" sx={{ flex: 1, wordBreak: "break-word", minWidth: 0 }}>
							{d?.name}
						</Typography>
						<IconButton
							aria-label="more"
							onClick={openMenu}
							className="always-visible-icon"
							size="small"
							sx={{ flexShrink: 0 }}
						>
							<MoreVertIcon fontSize="small" />
						</IconButton>
					</Box>

					{/* Metadata */}
					<Box sx={{ mb: 2 }}>
						<Typography variant="caption" color="text.secondary" display="block" sx={{ fontSize: "0.75rem" }}>
							Created: {d.createdAt}
						</Typography>
						<Typography variant="caption" color="text.secondary" display="block" sx={{ fontSize: "0.75rem" }}>
							Modified: {d.lastModified}
						</Typography>
					</Box>

					{/* Role and Public chips + Contributors */}
					<Box
						sx={{
							display: "flex",
							justifyContent: "space-between",
							alignItems: "center",
							gap: 1,
							flexWrap: "wrap",
						}}
					>
						<Box sx={{ display: "flex", gap: 1, flexWrap: "wrap" }}>
							<Chip
								icon={roleConfig.icon}
								label={roleConfig.label}
								size="small"
								color={roleConfig.color}
								sx={{
									fontWeight: 600,
									fontSize: 11,
									height: 26,
								}}
							/>
							{isPublic && (
								<Chip
									icon={<PublicIcon sx={{ fontSize: 14 }} />}
									label="Public"
									size="small"
									color="success"
									sx={{
										fontWeight: 600,
										fontSize: 11,
										height: 26,
									}}
								/>
							)}
						</Box>

						<Box
							className="avatar-stack"
							sx={{
								display: "flex",
								alignItems: "center",
								cursor: "pointer",
								justifyContent: "flex-end",
							}}
							onClick={handleContributorsClick}
						>
							{localContributors && localContributors.length > 0 ? (
								<>
									{localContributors.slice(0, 3).map((c, i) => (
										<Box key={i} sx={{ zIndex: localContributors.length - i }}>
											<Tooltip title={c.name || "Contributor"}>
												<Avatar
													src={c.picture || undefined}
													alt={c.name || "Contributor"}
													sx={{
														width: 28,
														height: 28,
														fontSize: 11,
														border: "2px solid white",
														boxShadow: 1,
														ml: i === 0 ? 0 : -1,
														bgcolor: c.picture ? undefined : "primary.main",
														color: c.picture ? undefined : "white",
													}}
												>
													{!c.picture && getInitials(c.name)}
												</Avatar>
											</Tooltip>
										</Box>
									))}

									{localContributors.length > 3 && (
										<Avatar
											sx={{
												width: 28,
												height: 28,
												fontSize: 10,
												ml: -1,
												border: "2px solid white",
												bgcolor: "grey.400",
												color: "white",
											}}
										>
											+{localContributors.length - 3}
										</Avatar>
									)}
								</>
							) : null}
						</Box>
					</Box>
				</CardContent>

				<Contributors
					open={Boolean(contributorsAnchor)}
					onClose={handleContributorsClose}
					contributors={localContributors}
					setOuterContributors={setLocalContributors}
					currentUserRole={userRole}
					currentUserName={d.currentUserName}
					diagramId={d.diagramId}
				/>
			</Card>

			<Dialog open={renameOpen} onClose={() => setRenameOpen(false)} maxWidth="sm" fullWidth>
				<DialogTitle>Rename Diagram</DialogTitle>
				<DialogContent>
					{renameError && (
						<Alert severity="error" sx={{ mb: 2 }}>
							{renameError}
						</Alert>
					)}
					<TextField
						autoFocus
						margin="dense"
						label="New Name"
						fullWidth
						variant="outlined"
						value={newName}
						onChange={(e) => setNewName(e.target.value)}
						error={renameError !== null}
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
					{shareError && (
						<Alert severity="error" sx={{ mb: 2 }}>
							{shareError}
						</Alert>
					)}
					<TextField
						autoFocus
						margin="dense"
						label="Username"
						fullWidth
						variant="outlined"
						value={shareUsername}
						onChange={(e) => setShareUsername(e.target.value)}
						error={shareError !== null}
						sx={{ mb: 2, mt: 1 }}
					/>
					<FormControl fullWidth variant="outlined">
						<InputLabel>Role</InputLabel>
						<Select
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
					{deleteError && (
						<Alert severity="error" sx={{ mb: 2 }}>
							{deleteError}
						</Alert>
					)}
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

			<Dialog open={unpublishOpen} onClose={() => setUnpublishOpen(false)} maxWidth="sm" fullWidth>
				<DialogTitle>Unpublish Diagram</DialogTitle>
				<DialogContent>
					{unpublishError && (
						<Alert severity="error" sx={{ mb: 2 }}>
							{unpublishError}
						</Alert>
					)}
					<Typography>
						Are you sure you want to unpublish "{d?.name}"? This will remove it from the public gallery.
					</Typography>
				</DialogContent>
				<DialogActions>
					<Button onClick={() => setUnpublishOpen(false)} disabled={unpublishLoading}>
						Cancel
					</Button>
					<Button
						onClick={handleUnpublishConfirm}
						variant="contained"
						color="warning"
						disabled={unpublishLoading}
					>
						{unpublishLoading ? <CircularProgress size={20} /> : "Unpublish"}
					</Button>
				</DialogActions>
			</Dialog>

			<EditPublicDetailsModal
				open={editOpen}
				onClose={() => setEditOpen(false)}
				diagramId={d.diagramId}
			/>
		</>
	);
}
