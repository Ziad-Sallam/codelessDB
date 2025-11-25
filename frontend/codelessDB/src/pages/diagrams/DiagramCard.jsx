import React, { useState } from "react";
import {
	Card,
	CardContent,
	CardMedia,
	Box,
	Typography,
	IconButton,
	Avatar,
	Popover,
	MenuList,
	MenuItem,
	ListItemIcon,
	ListItemText,
	Tooltip,
	List,
	ListItem,
	ListItemAvatar,
	ListItemText as MUIListItemText,
	Divider,
} from "@mui/material";

import MoreVertIcon from "@mui/icons-material/MoreVert";
import InsertPhotoIcon from "@mui/icons-material/InsertPhoto";
import DriveFileRenameOutlineIcon from "@mui/icons-material/DriveFileRenameOutline";
import DeleteIcon from "@mui/icons-material/Delete";
import ShareIcon from "@mui/icons-material/Share";

import "./card.css";

/* MOCK CONTRIBUTORS - uses your uploaded sample image for visible testing */
const MOCK_CONTRIBUTORS = [
	{ name: "Omar Hassan", role: "Frontend Dev", image: "" },
	{ name: "Lina Mohamed", role: "Backend Dev", image: "/mnt/data/b1c293cc-244f-4bdc-b26f-431d39e9dc76.png" },
	{ name: "Nader Karim", role: "Reviewer", image: "" },
	{ name: "Salma Adel", role: "PM", image: "" },
	{ name: "Youssef Sami", role: "QA", image: "" },
];

// Helper function to get initials from name
const getInitials = (name) => {
	if (!name) return "";
	return name
		.split(" ")
		.map(n => n[0])
		.slice(0, 2)
		.join("")
		.toUpperCase();
};

export default function DiagramCard({ d = {}, onOpen }) {
	const [menuAnchor, setMenuAnchor] = useState(null);
	const [contributorsAnchor, setContributorsAnchor] = useState(null);

	const handleRename = () => { };
	const handleDelete = () => { };
	const handleShare = () => { };

	const openMenu = (e) => {
		e.stopPropagation();
		setMenuAnchor(e.currentTarget);
	};
	const closeMenu = () => setMenuAnchor(null);

	const contributors = d?.contributors && d.contributors.length ? d.contributors : MOCK_CONTRIBUTORS;

	const handleContributorsClick = (e) => {
		e.stopPropagation();
		setContributorsAnchor(e.currentTarget);
	};

	const handleContributorsClose = () => {
		setContributorsAnchor(null);
	};

	return (
		<Card
			className="diagram-card"
			onClick={() => onOpen && onOpen(d)}
			sx={{ position: "relative" }}
		>
			{/* Thumbnail */}
			{d?.thumb ? (
				<CardMedia component="img" height="210" image={d.thumb} alt={d.name} className="thumbnail" />
			) : (
				<Box className="thumbnail thumbnail-placeholder">
					<InsertPhotoIcon sx={{ fontSize: 56, color: "var(--placeholder, #9aa4b2)", opacity: 0.5 }} />
				</Box>
			)}

			<Popover
				open={Boolean(menuAnchor)}
				anchorEl={menuAnchor}
				onClose={closeMenu}
				anchorOrigin={{ vertical: "bottom", horizontal: "right" }}
			>
				<MenuList sx={{ width: 180 }}>
					<MenuItem
						onClick={(e) => {
							e.stopPropagation();
							closeMenu();
							handleRename();
						}}
					>
						<ListItemIcon><DriveFileRenameOutlineIcon fontSize="small" /></ListItemIcon>
						<ListItemText>Rename</ListItemText>
					</MenuItem>

					<MenuItem
						onClick={(e) => {
							e.stopPropagation();
							closeMenu();
							handleShare();
						}}
					>
						<ListItemIcon><ShareIcon fontSize="small" /></ListItemIcon>
						<ListItemText>Share</ListItemText>
					</MenuItem>

					<MenuItem
						onClick={(e) => {
							e.stopPropagation();
							closeMenu();
							handleDelete();
						}}
					>
						<ListItemIcon><DeleteIcon fontSize="small" /></ListItemIcon>
						<ListItemText>Delete</ListItemText>
					</MenuItem>
				</MenuList>
			</Popover>

			<CardContent className="card-content">
				<Box className="content-row" sx={{ alignItems: "flex-start" }}>
					<Box className="text-section" sx={{ pr: 1 }}>
						<Typography variant="h6" noWrap>
							{d?.name}
						</Typography>

						<Typography variant="subtitle" color="text.secondary" display="block">
							Created: {d?.createdAt ? new Date(d.createdAt).toLocaleDateString() : "—"}
						</Typography>

						<Typography variant="subtitle" color="text.secondary" display="block">
							Modified: {d?.lastModified ? new Date(d.lastModified).toLocaleDateString() : "—"}
						</Typography>
					</Box>

					<IconButton
						aria-label="more"
						onClick={openMenu}
					>
						<MoreVertIcon />
					</IconButton>
				</Box>

				{/* Avatar stack (overlapped) - now clickable */}
				<Box
					className="avatar-stack"
					sx={{ display: "flex", alignItems: "center", mt: 2, cursor: "pointer", justifyContent: "flex-end", mt: 2 }}
					// anchorOrigin={{ vertical: "bottom", horizontal: "left" }}
					// transformOrigin={{ vertical: "top", horizontal: "left" }}
					onClick={handleContributorsClick}
				>
					{contributors.slice(0, 6).map((c, i) => (
						<Box
							key={i}
							sx={{ zIndex: contributors.length - i }}
						>
							<Tooltip title={c.name}>
								<Avatar
									src={c.image || undefined}
									alt={c.name}
									sx={{
										width: 32,
										height: 32,
										fontSize: 12,
										border: "2px solid white",
										boxShadow: 1,
										ml: i === 0 ? 0 : -1.2,
									}}
								>
									{!c.image && getInitials(c.name)}
								</Avatar>
							</Tooltip>
						</Box>
					))}

					{contributors.length > 6 && (
						<Avatar sx={{ width: 32, height: 32, fontSize: 12, ml: -1.2, border: "2px solid white" }}>
							+{contributors.length - 6}
						</Avatar>
					)}
				</Box>
			</CardContent>

			{/* Popover that shows ALL contributors (click to open, positioned at bottom) */}
			<Popover
				open={Boolean(contributorsAnchor)}
				anchorEl={contributorsAnchor}
				onClose={handleContributorsClose}
				anchorOrigin={{ vertical: "bottom", horizontal: "left" }}
				transformOrigin={{ vertical: "top", horizontal: "left" }}
				sx={{ mt: 1 }}
			>
				<Box sx={{ width: 300, maxHeight: 300, overflowY: "auto" }}>
					<Box sx={{ p: 2 }}>
						<Typography variant="subtitle1">Contributors</Typography>
						<Typography variant="body2" color="text.secondary" sx={{ mt: 0.5 }}>
							{contributors.length} members
						</Typography>
					</Box>
					<Divider />
					<List sx={{ p: 0 }}>
						{contributors.map((c, i) => (
							<ListItem key={i} sx={{ alignItems: "flex-start" }}>
								<ListItemAvatar>
									<Avatar src={c.image || undefined} alt={c.name}>
										{!c.image && getInitials(c.name)}
									</Avatar>
								</ListItemAvatar>
								<MUIListItemText
									primary={
										<Typography sx={{ fontWeight: 700, fontSize: 14 }}>{c.name}</Typography>
									}
									secondary={
										<Typography sx={{ color: "text.secondary", fontSize: 13 }}>{c.role}</Typography>
									}
								/>
							</ListItem>
						))}
					</List>
				</Box>
			</Popover>
		</Card>
	);
}