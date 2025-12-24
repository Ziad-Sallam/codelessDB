import { useState, useCallback } from "react";
import { shareDiagram, searchPublicUsers } from "../pages/diagrams/fetch";
import { debounce } from "lodash";
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
	CircularProgress,
	Autocomplete,
	Avatar,
	Typography,
	Box
} from "@mui/material";
import { useNotification } from "./NotificationContext";

export default function ShareWindow({ diagramId, shareOpen, setShareOpen }) {
	const { showSuccess, showError } = useNotification();

	// const [shareOpen, setShareOpen] = useState(false);
	const [shareUsername, setShareUsername] = useState("");
	const [shareRole, setShareRole] = useState("READER");
	const [shareLoading, setShareLoading] = useState(false);

	const [options, setOptions] = useState([]);
	const [isSearchLoading, setIsSearchLoading] = useState(false);

	const fetchOptions = useCallback(
		debounce(async (query) => {
			if (!query) {
				setOptions([]);
				return;
			}
			setIsSearchLoading(true);
			try {
				const data = await searchPublicUsers(query);
				setOptions(data.content || []);
			} catch (e) {
				console.error(e);
			} finally {
				setIsSearchLoading(false);
			}
		}, 500),
		[]
	);

	async function handleShareSubmit() {
		if (!shareUsername.trim()) return;
		setShareLoading(true);
		try {
			await shareDiagram(diagramId, shareUsername, shareRole);
			setShareOpen(false);
			showSuccess(`Shared with ${shareUsername}`);

		} catch (err) {
			showError(err.message);

		} finally {
			setShareLoading(false);
		}
	}

	return (
		<div>
			<Dialog open={shareOpen} onClose={() => setShareOpen(false)} maxWidth="sm" fullWidth>
				<DialogTitle>Share Diagram</DialogTitle>
				<DialogContent>
					<Autocomplete
						freeSolo
						options={options}
						getOptionLabel={(option) => typeof option === "string" ? option : option.username}
						onInputChange={(event, newInputValue) => {
							setShareUsername(newInputValue);
							fetchOptions(newInputValue);
						}}
						onChange={(event, newValue) => {
							if (typeof newValue === "string") {
								setShareUsername(newValue);
							} else if (newValue && newValue.username) {
								setShareUsername(newValue.username);
							}
						}}
						renderOption={(props, option) => {
							const { key, ...optionProps } = props;
							return (
								<li key={key} {...optionProps}>
									<Box sx={{ display: 'flex', alignItems: 'center', width: '100%', gap: 2 }}>
										<Avatar src={option.picture} alt={option.username} sx={{ width: 30, height: 30 }} />
										<Box sx={{ flexGrow: 1, minWidth: 0 }}>
											<Typography variant="body1" noWrap sx={{ fontWeight: 'bold' }}>
												{option.username}
											</Typography>
											{option.name && option.name !== option.username && (
												<Typography variant="caption" color="text.secondary" noWrap display="block">
													{option.name}
												</Typography>
											)}
										</Box>
									</Box>
								</li>
							);
						}}
						renderInput={(params) => (
							<TextField
								{...params}
								autoFocus
								margin="dense"
								label="Username"
								fullWidth
								variant="outlined"
								sx={{ mb: 2, mt: 1 }}
								InputProps={{
									...params.InputProps,
									endAdornment: (
										<>
											{isSearchLoading ? <CircularProgress color="inherit" size={20} /> : null}
											{params.InputProps.endAdornment}
										</>
									),
								}}
							/>
						)}
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