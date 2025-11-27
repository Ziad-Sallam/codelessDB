// Contributors.jsx
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
import { shareDiagram } from "./fetch.js";

/** Helper - get initials from name */
export function getInitials(name) {
	if (!name) return "";
	const parts = String(name).trim().split(/\s+/);
	if (parts.length === 1) return (parts[0][0] || "").toUpperCase();
	return ((parts[0][0] || "") + (parts[1][0] || "")).toUpperCase();
}

/** Small mapping to show an icon for the role */
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
 * - open (bool)
 * - onClose (fn)
 * - contributors (array) : [{ name, picture|null, role }]
 * - diagramId (string|number)
 * - currentUserRole (string) : "OWNER" | "WRITER" | "READER"
 * - currentUserName (string) : current user's username (unique)
 * - onRoleChange (fn) : optional callback (username, newRole, serverUpdated) => Promise|void
 * - onDeleteContributor (fn) : optional callback (username) => Promise|void
 */
export default function Contributors({
	open,
	onClose,
	contributors = [],
	diagramId,
	setOuterContributors,
	currentUserRole = "READER",
	currentUserName = null,
}) {
	const { showSuccess, showError, showWarning } = useNotification();

	// local copy to allow optimistic updates
	const [localContributors, setLocalContributors] = useState(contributors || []);
	// holds the username being changed / deleted
	const [changingRoleName, setChangingRoleName] = useState(null);
	const [deletingName, setDeletingName] = useState(null);

	useEffect(() => {
		setLocalContributors(Array.isArray(contributors) ? contributors : []);
	}, [contributors]);

	const isOwner = currentUserRole === "OWNER";

	/**
	 * Change role handler (uses username as unique identifier)
	 * - optimistic update
	 * - calls shareDiagram(diagramId, username, role) (backend)
	 * - rolls back on error
	 */
	async function handleRoleChange(username, newRole) {
		const target = localContributors.find((c) => String(c.name) === String(username));
		if (!target) return;

		if (target.role === "OWNER") {
			showWarning("Cannot change the role of an owner.");
			return;
		}
		if (String(username) === String(currentUserName)) {
			showWarning("Cannot change your own role.");
			return;
		}

		const previous = [...localContributors];

		// optimistic
		setLocalContributors((prev) =>
			prev.map((c) => (String(c.name) === String(username) ? { ...c, role: newRole } : c))
		);
		setChangingRoleName(username);

		try {
			// call backend — shareDiagram(diagramId, toUserName, role)
			const resp = await shareDiagram(diagramId, username, newRole);

			// build server-updated object if response contains updated contributor
			let serverUpdated = null;
			if (resp && typeof resp === "object") {
				serverUpdated = {
					...target,
					role: newRole,
					...(resp.contributor || {}),
					...(resp.picture ? { picture: resp.picture } : {}),
				};
			}

			setLocalContributors((prev) =>
				prev.map((c) => (String(c.name) === String(username) ? (serverUpdated || { ...c, role: newRole }) : c))
			);

			return true;
		} catch (err) {
			// rollback
			setLocalContributors(previous);
			console.error("Failed to change role:", err);
			showError("Failed to change role. Try again.");
			return false;
		} finally {
			setChangingRoleName(null);
		}
	}

	/**
	 * Delete contributor by username
	 */
	async function handleDelete(username) {
		const target = localContributors.find((c) => String(c.name) === String(username));
		if (!target) return;

		if (target.role === "OWNER") {
			showWarning("Cannot remove an owner.");
			return;
		}
		
		if (String(username) === String(currentUserName)) {
			showWarning("Cannot remove yourself.");
			return;
		}

		if (!window.confirm(`Remove ${target.name} from contributors?`)) return;

		setDeletingName(username);
		try {
			const response = await shareDiagram(diagramId, username, null, true)
			setLocalContributors((prev) => prev.filter((c) => String(c.name) !== String(username)));
			setOuterContributors((prev) => prev.filter((c) => String(c.name) !== String(username)))
			showSuccess(`Contributor ${username} is removed`);

		} catch (err) {
			showError(err)

		} finally {
			setDeletingName(null);
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
					<Typography id="contributors-dialog-title" variant="h6" sx={{ fontWeight: 700 }}>
						Contributors
					</Typography>
					<Typography variant="body2" color="text.secondary">
						{localContributors.length} member{localContributors.length !== 1 ? "s" : ""}
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
						const isSelf = String(contributor.name) === String(currentUserName);
						const canEdit = isOwner && !isContributorOwner && !isSelf;
						const canDelete = isOwner && !isContributorOwner && !isSelf;

						return (
							<ListItem
								key={contributor.name}
								sx={{
									alignItems: "center",
									px: 3,
									py: 1.5,
									"&:hover": {
										bgcolor: "action.hover",
									},
								}}
								// render our own secondary action area (icon button) via secondaryAction prop
								secondaryAction={
									isOwner ? (
										<Box sx={{ display: "flex", alignItems: "center", gap: 1 }}>
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
														onClick={() => handleDelete(contributor.name)}
														disabled={!canDelete || deletingName === contributor.name}
														aria-label={`delete-${contributor.name}`}
														sx={{ color: canDelete ? "error.main" : "action.disabled" }}
													>
														{deletingName === contributor.name ? <CircularProgress size={20} /> : <DeleteIcon />}
													</IconButton>
												</span>
											</Tooltip>
										</Box>
									) : null
								}
							>
								<ListItemAvatar>
									<Avatar
										src={contributor.picture || undefined}
										alt={contributor.name}
										sx={{
											bgcolor: contributor.picture ? undefined : "primary.main",
											color: contributor.picture ? undefined : "white",
										}}
									>
										{!contributor.picture && getInitials(contributor.name)}
									</Avatar>
								</ListItemAvatar>

								{/*
                  IMPORTANT: disableTypography to avoid ListItemText auto-wrapping primary/secondary inside <p>.
                  We render our own Typography/Box nodes to avoid invalid nesting (Select renders div/fieldset).
                */}
								<ListItemText
									disableTypography
									primary={
										<Box sx={{ display: "flex", alignItems: "center", gap: 0.5 }}>
											<Typography sx={{ fontWeight: 700 }}>{contributor.name}</Typography>
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
											<Box sx={{ mt: 0.5, display: "flex", alignItems: "center", gap: 1 }}>
												<Select
													size="small"
													value={contributor.role}
													onChange={(e) => handleRoleChange(contributor.name, e.target.value)}
													sx={{
														minWidth: 160,
														fontSize: 13,
														"& .MuiSelect-select": {
															display: "flex",
															alignItems: "center",
														},
													}}
													disabled={changingRoleName === contributor.name}
													inputProps={{ "aria-label": `role-select-${contributor.name}` }}
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
															Viewer
														</Box>
													</MenuItem>
												</Select>

												{changingRoleName === contributor.name && <CircularProgress size={18} />}
											</Box>
										) : (
											<Box sx={{ display: "flex", alignItems: "center", mt: 0.5 }}>
												{getRoleIcon(contributor.role)}
												<Typography sx={{ color: "text.secondary", fontSize: 13, ml: 0.5 }}>
													{contributor.role === "OWNER" ? "Owner" : contributor.role === "WRITER" ? "Editor" : "Viewer"}
												</Typography>
											</Box>
										)
									}
								/>
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
			// NOTE: contributors are identified by their unique username 'name'
			name: PropTypes.string.isRequired,
			picture: PropTypes.string,
			role: PropTypes.oneOf(["OWNER", "WRITER", "READER"]).isRequired,
		})
	),
	diagramId: PropTypes.oneOfType([PropTypes.string, PropTypes.number]),
	currentUserRole: PropTypes.oneOf(["OWNER", "WRITER", "READER"]),
	currentUserName: PropTypes.string,
	onRoleChange: PropTypes.func,
	onDeleteContributor: PropTypes.func,
};
