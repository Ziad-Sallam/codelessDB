import React from "react";
import {
	Card,
	CardContent,
	CardMedia,
	Box,
	Typography,
	IconButton
} from "@mui/material";
import InsertPhotoIcon from "@mui/icons-material/InsertPhoto";
import OpenInNewIcon from "@mui/icons-material/OpenInNew";

import "./card.css";

export default function DiagramCard({ d, onOpen }) {
	return (
		<Card
			className="diagram-card"
			onClick={() => onOpen(d)}
		>
			{d.thumb ? (
				<CardMedia
					component="img"
					height="210"
					image={d.thumb}
					alt={d.name}
					className="thumbnail"
				/>
			) : (
				<Box className="thumbnail thumbnail-placeholder">
					<InsertPhotoIcon sx={{ fontSize: 56, color: "background.light", opacity: 0.5 }} />
				</Box>
			)}

			<CardContent className="card-content">
				<Box className="content-row">
					<Box className="text-section">
						<Typography variant="h5" noWrap sx={{ fontSize: 18, fontWeight: 530 }}>
							{d.name}
						</Typography>

						<Typography variant="caption" color="text.secondary" display="block">
							Created: {new Date(d.createdAt).toLocaleDateString()}
						</Typography>

						<Typography variant="caption" color="text.secondary" display="block">
							Modified: {new Date(d.modifiedAt).toLocaleDateString()}
						</Typography>
					</Box>

					<IconButton
						className="open-icon"
						size="small"
						onClick={(e) => {
							e.stopPropagation();
							onOpen(d);
						}}
					>
						<OpenInNewIcon fontSize="small" />
					</IconButton>
				</Box>
			</CardContent>
		</Card>
	);
}
