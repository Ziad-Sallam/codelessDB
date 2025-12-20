import { useState } from "react";
import { shareDiagram } from "../pages/diagrams/fetch";
import { 
	Dialog,
	DialogTitle,
	DialogContent,
	TextField,
	FormControl,
	InputLabel,
	Select,
	MenuItem,
	DialogActions,
	Button,
	CircularProgress
 } from "@mui/material";
 import { useNotification } from "./NotificationContext";

export default function ShareWindow({diagramId,shareOpen,setShareOpen}){
		const { showSuccess, showError } = useNotification();

        // const [shareOpen, setShareOpen] = useState(false);
        const [shareUsername, setShareUsername] = useState("");
        const [shareRole, setShareRole] = useState("READER");
        const [shareLoading, setShareLoading] = useState(false);


		async function handleShareSubmit() {
				if (!shareUsername.trim()) return;
				setShareLoading(true);
				try {
					await shareDiagram(diagramId, shareUsername, shareRole);
					setShareOpen(false);
					showSuccess(`Shared with ${shareUsername}`);
				
				} catch (err) {
					showError(err || "Failed to share diagram");
				
				} finally {
					setShareLoading(false);
				}
			}

    return(
        <div>
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
        </div>
    );
}