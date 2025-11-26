import AdminPanelSettingsIcon from "@mui/icons-material/AdminPanelSettings";
import CloseIcon from "@mui/icons-material/Close";
import DeleteIcon from "@mui/icons-material/Delete";
import EditIcon from "@mui/icons-material/Edit";
import VisibilityIcon from "@mui/icons-material/Visibility";
import {
	Avatar,
	Box,
	Button,
	CircularProgress,
	Dialog,
	DialogActions,
	DialogContent,
	DialogTitle,
	Divider,
	IconButton,
	List,
	ListItem,
	ListItemAvatar,
	ListItemText,
	MenuItem,
	Select,
	Tooltip,
	Typography,
} from "@mui/material";

import PropTypes from "prop-types";
import { useEffect, useState } from "react";
import { useNotification } from "../../components/NotificationContext";

function getInitials(name) {
	if (!name) return "";
	const parts = String(name).trim().split(/\s+/);
	if (parts.length === 1) return (parts[0][0] || "").toUpperCase();
	return ((parts[0][0] || "") + (parts[1][0] || "")).toUpperCase();
}

function getRoleIcon(role) {
	const icons = {
		OWNER: <AdminPanelSettingsIcon sx={{ fontSize: 16, mr: 0.5 }} color="primary" />,
		WRITER: <EditIcon sx={{ fontSize: 16, mr: 0.5 }} color="secondary" />,
		READER: <VisibilityIcon sx={{ fontSize: 16, mr: 0.5 }} color="action" />,
	};
	return icons[role] || icons.READER;
}

/**
 * Contributors Dialog Component
 *
 * Props:
 * - open (bool): Whether dialog is open
 * - onClose (fn): Callback to close dialog
 * - contributors (array): Array of contributor objects [{ id, name, role, image }]
 * - currentUserRole (string): Current user's role for this diagram ("OWNER" | "WRITER" | "READER")
 * - currentUserId (string|number): ID of current user
 * - onRoleChange (fn): Callback when role changes (id, newRole) => Promise
 * - onDeleteContributor (fn): Callback when contributor removed (id) => Promise
 */
export default function Contributors({
	open,
	onClose,
	contributors = [],
	currentUserRole = "READER",
	currentUserId = null,
	onRoleChange,
	onDeleteContributor,
}) {
	
	const { showSuccess, showError, showWarning } = useNotification();

	const [localContributors, setLocalContributors] = useState(contributors || []);
	const [changingRoleId, setChangingRoleId] = useState(null);
	const [deletingId, setDeletingId] = useState(null);

	useEffect(() => {
		setLocalContributors(contributors || []);
	}, [contributors]);

	const isOwner = currentUserRole === "OWNER";

	async function handleRoleChange(contributorId, newRole) {
		const target = localContributors.find((c) => c.id === contributorId);
		if (!target) return;

		// Prevent changing owner's role
		if (target.role === "OWNER") {
			console.warn("Cannot change the role of an owner.");
			return;
		}

		// Prevent self role change
		if (String(contributorId) === String(currentUserId)) {
			console.warn("Cannot change your own role.");
			return;
		}

		const previousContributors = [...localContributors];

		// Optimistic update
		setLocalContributors((prev) =>
			prev.map((c) => (c.id === contributorId ? { ...c, role: newRole } : c))
		);
		setChangingRoleId(contributorId);

		try {
			if (onRoleChange) {
				await Promise.resolve(onRoleChange(contributorId, newRole));
			}
		} catch (err) {
			// Rollback on error
			setLocalContributors(previousContributors);
			console.error("Failed to change role:", err);
		} finally {
			setChangingRoleId(null);
		}
	}

	async function handleDelete(contributorId) {
		const target = localContributors.find((c) => c.id === contributorId);
		if (!target) return;

		// Prevent deleting owner
		if (target.role === "OWNER") {
			console.warn("Cannot remove an owner.");
			return;
		}

		// Prevent self deletion
		if (String(contributorId) === String(currentUserId)) {
			console.warn("Cannot remove yourself.");
			return;
		}

		const previousContributors = [...localContributors];

		// Optimistic update
		setLocalContributors((prev) => prev.filter((c) => c.id !== contributorId));
		setDeletingId(contributorId);

		try {
			if (onDeleteContributor) {
				await Promise.resolve(onDeleteContributor(contributorId));
			}
		} catch (err) {
			// Rollback on error
			setLocalContributors(previousContributors);
			console.error("Failed to delete contributor:", err);
		} finally {
			setDeletingId(null);
		}
	}

	return (
		<Dialog
			open={Boolean(open)}
			onClose={onClose}
			fullWidth
			maxWidth="sm"
			aria-labelledby="contributors-dialog-title"
		>
			<DialogTitle
				sx={{
					display: "flex",
					alignItems: "center",
					justifyContent: "space-between",
					gap: 2,
				}}
			>
				<Box>
					<Typography
						id="contributors-dialog-title"
						variant="h6"
						sx={{ fontWeight: 700 }}
					>
						Contributors
					</Typography>
					<Typography variant="body2" color="text.secondary">
						{localContributors.length} member
						{localContributors.length !== 1 ? "s" : ""}
					</Typography>
				</Box>

				<IconButton aria-label="close contributors" onClick={onClose} size="large">
					<CloseIcon />
				</IconButton>
			</DialogTitle>

			<Divider />

			<DialogContent dividers sx={{ px: 0 }}>
				<List sx={{ p: 0, maxHeight: 420, overflowY: "auto" }}>
					{localContributors.length === 0 && (
						<Box sx={{ p: 3, textAlign: "center" }}>
							<Typography color="text.secondary">No contributors yet</Typography>
						</Box>
					)}

					{localContributors.map((contributor) => {
						const isContributorOwner = contributor.role === "OWNER";
						const isSelf = String(contributor.id) === String(currentUserId);
						const canEdit = isOwner && !isContributorOwner && !isSelf;
						const canDelete = isOwner && !isContributorOwner && !isSelf;

						return (
							<ListItem
								key={contributor.id}
								sx={{
									alignItems: "center",
									px: 3,
									py: 1.5,
									"&:hover": {
										bgcolor: "action.hover",
									},
								}}
							>
								<ListItemAvatar>
									<Avatar
										src={contributor.image || undefined}
										alt={contributor.name}
										sx={{
											bgcolor: contributor.image ? undefined : "primary.main",
											color: contributor.image ? undefined : "white",
										}}
									>
										{!contributor.image && getInitials(contributor.name)}
									</Avatar>
								</ListItemAvatar>

								<ListItemText
									primary={
										<Box sx={{ display: "flex", alignItems: "center", gap: 0.5 }}>
											<Typography sx={{ fontWeight: 700 }}>
												{contributor.name}
											</Typography>
											{isSelf && (
												<Typography
													variant="caption"
													sx={{
														color: "primary.main",
														bgcolor: "primary.light",
														px: 0.75,
														py: 0.25,
														borderRadius: 1,
														fontWeight: 600,
													}}
												>
													You
												</Typography>
											)}
										</Box>
									}
									secondary={
										canEdit ? (
											<Box
												sx={{
													mt: 0.5,
													display: "flex",
													alignItems: "center",
													gap: 1,
												}}
											>
												<Select
													size="small"
													value={contributor.role}
													onChange={(e) =>
														handleRoleChange(contributor.id, e.target.value)
													}
													sx={{
														minWidth: 140,
														fontSize: 13,
														"& .MuiSelect-select": {
															display: "flex",
															alignItems: "center",
														},
													}}
													disabled={changingRoleId === contributor.id}
												>
													<MenuItem value="WRITER">
														<Box sx={{ display: "flex", alignItems: "center" }}>
															{getRoleIcon("WRITER")}
															Editor
														</Box>
													</MenuItem>
													<MenuItem value="READER">
														<Box sx={{ display: "flex", alignItems: "center" }}>
															{getRoleIcon("READER")}
															READER
														</Box>
													</MenuItem>
												</Select>
												{changingRoleId === contributor.id && (
													<CircularProgress size={18} />
												)}
											</Box>
										) : (
											<Box
												sx={{
													display: "flex",
													alignItems: "center",
													mt: 0.5,
												}}
											>
												{getRoleIcon(contributor.role)}
												<Typography sx={{ color: "text.secondary", fontSize: 13 }}>
													{contributor.role === "OWNER"
														? "Owner"
														: contributor.role === "WRITER"
															? "Editor"
															: "READER"}
												</Typography>
											</Box>
										)
									}
								/>

								{isOwner && (
									<Tooltip
										title={
											!canDelete
												? isContributorOwner
													? "Cannot remove owner"
													: isSelf
														? "Cannot remove yourself"
														: ""
												: "Remove contributor"
										}
									>
										<span>
											<IconButton
												edge="end"
												onClick={() => handleDelete(contributor.id)}
												disabled={!canDelete || deletingId === contributor.id}
												aria-label={`delete-${contributor.id}`}
												sx={{
													color: canDelete ? "error.main" : "action.disabled",
												}}
											>
												{deletingId === contributor.id ? (
													<CircularProgress size={20} />
												) : (
													<DeleteIcon />
												)}
											</IconButton>
										</span>
									</Tooltip>
								)}
							</ListItem>
						);
					})}
				</List>
			</DialogContent>

			<DialogActions sx={{ px: 3, py: 2 }}>
				<Button onClick={onClose} variant="contained">
					Close
				</Button>
			</DialogActions>
		</Dialog>
	);
}

Contributors.propTypes = {
	open: PropTypes.bool,
	onClose: PropTypes.func,
	contributors: PropTypes.arrayOf(
		PropTypes.shape({
			id: PropTypes.oneOfType([PropTypes.string, PropTypes.number]).isRequired,
			name: PropTypes.string.isRequired,
			role: PropTypes.oneOf(["OWNER", "WRITER", "READER"]).isRequired,
			image: PropTypes.string,
		})
	),
	currentUserRole: PropTypes.oneOf(["OWNER", "WRITER", "READER"]),
	currentUserId: PropTypes.oneOfType([PropTypes.string, PropTypes.number]),
	onRoleChange: PropTypes.func,
	onDeleteContributor: PropTypes.func,
};