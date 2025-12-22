import React, { useState, useEffect } from "react";
import {
    Box,
    Typography,
    Avatar,
    TextField,
    Button,
    List,
    ListItem,
    ListItemAvatar,
    ListItemText,
    IconButton,
    Menu,
    MenuItem,
    Divider,
    Paper,
    CircularProgress
} from "@mui/material";
import {
    MoreVert as MoreVertIcon,
    Send as SendIcon,
    Delete as DeleteIcon,
    Edit as EditIcon,
    ThumbUp as ThumbUpIcon,
    ThumbUpOutlined as ThumbUpOutlinedIcon,
    ThumbDown as ThumbDownIcon,
    ThumbDownOutlined as ThumbDownOutlinedIcon,
    ExpandMore as ExpandMoreIcon,
    ExpandLess as ExpandLessIcon,
} from "@mui/icons-material";
import { useNotification } from "../../../components/NotificationContext";
import { useAuth } from "../../../components/AuthProvider";
import { getComments, addComment, updateComment, deleteComment, reactToComment } from "../fetch";

function timeAgo(dateString) {
    const date = new Date(dateString);
    const now = new Date();
    const seconds = Math.floor((now - date) / 1000);

    let interval = Math.floor(seconds / 31536000);
    if (interval > 1) return interval + " years ago";
    if (interval === 1) return "1 year ago";

    interval = Math.floor(seconds / 2592000);
    if (interval > 1) return interval + " months ago";
    if (interval === 1) return "1 month ago";

    interval = Math.floor(seconds / 86400);
    if (interval > 1) return interval + " days ago";
    if (interval === 1) return "1 day ago";

    interval = Math.floor(seconds / 3600);
    if (interval > 1) return interval + " hours ago";
    if (interval === 1) return "1 hour ago";

    interval = Math.floor(seconds / 60);
    if (interval > 1) return interval + " minutes ago";
    if (interval === 1) return "1 minute ago";

    return "just now";
}

const CommentItem = ({
    comment,
    depth = 0,
    editingComment,
    setEditingComment,
    handleUpdateComment,
    replyingTo,
    setReplyingTo,
    replyContent,
    setReplyContent,
    handleAddComment,
    handleMenuOpen,
    handleReaction,
    submitting,
    timeAgo
}) => {
    const isEditing = editingComment && editingComment.id === comment.id;
    const [showReplies, setShowReplies] = useState(false);

    const renderAvatar = (user, size = 32) => {
        const hasPicture = user.userPicture && user.userPicture !== "";
        return (
            <Avatar
                alt={user.username}
                src={hasPicture ? user.userPicture : null}
                sx={{
                    width: size,
                    height: size,
                    bgcolor: hasPicture ? "transparent" : "#0d47a1",
                    color: "white",
                    fontWeight: "bold",
                    fontSize: size * 0.5,
                    border: "2px solid white",
                    boxShadow: "0 0 0 1px #e0e0e0"
                }}
            >
                {!hasPicture && user.username ? user.username.charAt(0).toUpperCase() : null}
            </Avatar>
        );
    };

    return (
        <Box sx={{ ml: depth > 0 ? { xs: 2, sm: 4 } : 0, mt: 1 }}>
            <ListItem
                alignItems="flex-start"
                secondaryAction={
                    comment.owner && !isEditing ? (
                        <IconButton edge="end" onClick={(e) => {
                            e.stopPropagation();
                            handleMenuOpen(e, comment.id);
                        }}>
                            <MoreVertIcon />
                        </IconButton>
                    ) : null
                }
                sx={{ px: 0, py: 0.5 }}
            >
                <ListItemAvatar sx={{ minWidth: 40 }}>
                    {renderAvatar(comment, 32)}
                </ListItemAvatar>
                <Box sx={{ width: "100%", pr: comment.owner && !isEditing ? 4 : 0 }}>
                    {isEditing ? (
                        <Box sx={{ mt: 1 }}>
                            <TextField
                                fullWidth
                                multiline
                                minRows={2}
                                size="small"
                                value={editingComment.content}
                                onChange={(e) => setEditingComment({ ...editingComment, content: e.target.value })}
                                sx={{ mb: 1, bgcolor: "white" }}
                                InputProps={{ style: { fontSize: '1rem' } }}
                                autoFocus
                            />
                            <Box sx={{ display: "flex", gap: 1, justifyContent: "flex-end" }}>
                                <Button size="small" onClick={() => setEditingComment(null)}>Cancel</Button>
                                <Button size="small" variant="contained" onClick={handleUpdateComment}>Save</Button>
                            </Box>
                        </Box>
                    ) : (
                        <>
                            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 0.2 }}>
                                <Typography variant="subtitle1" component="span" fontWeight="bold" sx={{ fontSize: '1rem' }}>
                                    {comment.username}
                                </Typography>
                                <Typography variant="caption" color="text.secondary" sx={{ fontSize: '0.875rem' }}>
                                    {timeAgo(comment.createdAt)} {comment.edited && "(edited)"}
                                </Typography>
                            </Box>
                            <Typography variant="body1" color="text.primary" sx={{
                                whiteSpace: "pre-wrap",
                                lineHeight: 1.6,
                                overflowWrap: "break-word",
                                wordBreak: "break-word"
                            }}>
                                {comment.content}
                            </Typography>
                            <Box sx={{ mt: 0.5, display: 'flex', alignItems: 'center', gap: 1 }}>
                                <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.2 }}>
                                    <IconButton size="small" onClick={() => handleReaction(comment.id, 'LIKE')} sx={{ p: 0.5 }}>
                                        {comment.userReaction === 'LIKE' ? <ThumbUpIcon sx={{ fontSize: '1.1rem' }} color="primary" /> : <ThumbUpOutlinedIcon sx={{ fontSize: '1.1rem' }} />}
                                    </IconButton>
                                    <Typography variant="caption" sx={{ fontSize: '0.8rem' }}>{comment.likesCount}</Typography>
                                </Box>
                                <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.2 }}>
                                    <IconButton size="small" onClick={() => handleReaction(comment.id, 'DISLIKE')} sx={{ p: 0.5 }}>
                                        {comment.userReaction === 'DISLIKE' ? <ThumbDownIcon sx={{ fontSize: '1.1rem' }} color="error" /> : <ThumbDownOutlinedIcon sx={{ fontSize: '1.1rem' }} />}
                                    </IconButton>
                                    <Typography variant="caption" sx={{ fontSize: '0.8rem' }}>{comment.dislikesCount}</Typography>
                                </Box>
                                <Button
                                    size="small"
                                    sx={{ ml: 1, p: 0, minWidth: 'auto', textTransform: 'none', color: 'text.secondary', fontWeight: 'bold', fontSize: '0.8rem', '&:hover': { bgcolor: 'transparent', textDecoration: 'underline' } }}
                                    onClick={() => {
                                        setReplyingTo(replyingTo === comment.id ? null : comment.id);
                                        setReplyContent("");
                                    }}
                                >
                                    Reply
                                </Button>
                            </Box>
                        </>
                    )}

                    {replyingTo === comment.id && (
                        <Box sx={{ mt: 2, mb: 1 }}>
                            <TextField
                                fullWidth
                                multiline
                                minRows={2}
                                size="small"
                                placeholder={`Reply to ${comment.username}...`}
                                value={replyContent}
                                onChange={(e) => setReplyContent(e.target.value)}
                                sx={{ mb: 1, bgcolor: "white" }}
                                InputProps={{ style: { fontSize: '1rem' } }}
                                autoFocus
                            />
                            <Box sx={{ display: "flex", gap: 1, justifyContent: "flex-end" }}>
                                <Button size="small" onClick={() => setReplyingTo(null)}>Cancel</Button>
                                <Button size="small" variant="contained" disabled={!replyContent.trim() || submitting} onClick={() => handleAddComment(comment.id)}>
                                    Post Reply
                                </Button>
                            </Box>
                        </Box>
                    )}
                </Box>
            </ListItem>

            {comment.replies && comment.replies.length > 0 && (
                <Box sx={{ ml: 4, mt: 0.5 }}>
                    <Button
                        size="small"
                        startIcon={showReplies ? <ExpandLessIcon /> : <ExpandMoreIcon />}
                        onClick={() => setShowReplies(!showReplies)}
                        sx={{
                            textTransform: 'none',
                            fontWeight: 'bold',
                            color: 'primary.main',
                            '&:hover': { bgcolor: 'transparent', textDecoration: 'underline' },
                            p: 0,
                            mb: showReplies ? 1 : 0
                        }}
                    >
                        {comment.replies.length} {comment.replies.length === 1 ? 'reply' : 'replies'}
                    </Button>

                    {showReplies && (
                        <Box sx={{ borderLeft: '2px solid', borderColor: 'divider', pl: 1 }}>
                            {comment.replies.map(reply => (
                                <CommentItem
                                    key={reply.id}
                                    comment={reply}
                                    depth={depth + 1}
                                    editingComment={editingComment}
                                    setEditingComment={setEditingComment}
                                    handleUpdateComment={handleUpdateComment}
                                    replyingTo={replyingTo}
                                    setReplyingTo={setReplyingTo}
                                    replyContent={replyContent}
                                    setReplyContent={setReplyContent}
                                    handleAddComment={handleAddComment}
                                    handleMenuOpen={handleMenuOpen}
                                    handleReaction={handleReaction}
                                    submitting={submitting}
                                    timeAgo={timeAgo}
                                />
                            ))}
                        </Box>
                    )}
                </Box>
            )}
        </Box>
    );
};

export function CommentsSection({ diagramId }) {
    const { user } = useAuth();
    const { showSuccess, showError } = useNotification();
    const [comments, setComments] = useState([]);
    const [loading, setLoading] = useState(true);
    const [newComment, setNewComment] = useState("");
    const [submitting, setSubmitting] = useState(false);
    const [editingComment, setEditingComment] = useState(null);
    const [replyingTo, setReplyingTo] = useState(null);
    const [replyContent, setReplyContent] = useState("");
    const [anchorEl, setAnchorEl] = useState(null);
    const [selectedCommentId, setSelectedCommentId] = useState(null);

    useEffect(() => {
        loadComments();
    }, [diagramId]);

    const loadComments = async () => {
        setLoading(true);
        try {
            const data = await getComments(diagramId);
            setComments(data);
        } catch (err) {
            console.error("Failed to load comments", err);
        } finally {
            setLoading(false);
        }
    };

    const handleAddComment = async (parentId = null) => {
        const content = parentId ? replyContent : newComment;
        if (!content.trim()) return;

        setSubmitting(true);
        try {
            const comment = await addComment(diagramId, content, parentId);
            if (parentId) {
                setComments(prev => addReplyToTree(prev, parentId, comment));
                setReplyingTo(null);
                setReplyContent("");
            } else {
                setComments([comment, ...comments]);
                setNewComment("");
            }
            showSuccess(parentId ? "Reply posted" : "Comment added");
        } catch (err) {
            showError(err.message);
        } finally {
            setSubmitting(false);
        }
    };

    const addReplyToTree = (list, parentId, newComment) => {
        return list.map(c => {
            if (c.id === parentId) {
                return { ...c, replies: [newComment, ...(c.replies || [])] };
            }
            if (c.replies && c.replies.length > 0) {
                return { ...c, replies: addReplyToTree(c.replies, parentId, newComment) };
            }
            return c;
        });
    };

    const handleReaction = async (commentId, type) => {
        try {
            const updatedComment = await reactToComment(commentId, type);
            // Re-use logic to update the comment in the tree
            setComments(prev => updateCommentInTree(prev, commentId, updatedComment));
        } catch (err) {
            showError(err.message);
        }
    };

    const handleUpdateComment = async () => {
        if (!editingComment.content.trim()) return;

        try {
            const updated = await updateComment(editingComment.id, editingComment.content);
            setComments(prev => updateCommentInTree(prev, editingComment.id, updated));
            setEditingComment(null);
            showSuccess("Comment updated");
        } catch (err) {
            showError(err.message);
        }
    };

    const updateCommentInTree = (list, id, updated) => {
        return list.map(c => {
            if (c.id === id) return { ...updated, replies: c.replies };
            if (c.replies) return { ...c, replies: updateCommentInTree(c.replies, id, updated) };
            return c;
        });
    };

    const handleDeleteClick = async () => {
        if (window.confirm("Are you sure you want to delete this comment?")) {
            try {
                await deleteComment(selectedCommentId);
                setComments(prev => deleteFromTree(prev, selectedCommentId));
                showSuccess("Comment deleted");
            } catch (err) {
                showError(err.message);
            }
        }
        handleMenuClose();
    };

    const deleteFromTree = (list, id) => {
        return list
            .filter(c => c.id !== id)
            .map(c => ({
                ...c,
                replies: c.replies ? deleteFromTree(c.replies, id) : []
            }));
    };

    const handleMenuOpen = (event, commentId) => {
        setAnchorEl(event.currentTarget);
        setSelectedCommentId(commentId);
    };

    const handleMenuClose = () => {
        setAnchorEl(null);
        setSelectedCommentId(null);
    };

    const handleEditClick = () => {
        const findComment = (list, id) => {
            for (const c of list) {
                if (c.id === id) return c;
                if (c.replies) {
                    const found = findComment(c.replies, id);
                    if (found) return found;
                }
            }
            return null;
        };
        const comment = findComment(comments, selectedCommentId);
        if (comment) {
            setEditingComment({ id: comment.id, content: comment.content });
        }
        handleMenuClose();
    };

    return (
        <Box sx={{ pb: 4 }}>
            <Typography variant="h5" fontWeight="bold" gutterBottom sx={{ mb: 3 }}>
                Comments
            </Typography>

            <Paper elevation={0} variant="outlined" sx={{ p: 2, mb: 4, bgcolor: "background.paper" }}>
                <Box sx={{ display: "flex", gap: 2 }}>
                    {user ? (
                        <Avatar
                            alt={user.username}
                            src={user.picture && user.picture !== "" ? user.picture : null}
                            sx={{
                                width: 40,
                                height: 40,
                                bgcolor: user.picture && user.picture !== "" ? "transparent" : "#0d47a1",
                                color: "white",
                                fontWeight: "bold",
                                border: "2px solid white",
                                boxShadow: "0 0 0 1px #e0e0e0"
                            }}
                        >
                            {!(user.picture && user.picture !== "") && user.username ? user.username.charAt(0).toUpperCase() : null}
                        </Avatar>
                    ) : (
                        <Avatar sx={{ width: 40, height: 40 }} />
                    )}
                    <Box sx={{ flexGrow: 1 }}>
                        <TextField
                            fullWidth
                            multiline
                            minRows={2}
                            placeholder="Write a comment..."
                            variant="outlined"
                            value={newComment}
                            onChange={(e) => setNewComment(e.target.value)}
                            sx={{ mb: 1, bgcolor: "white" }}
                            InputProps={{ style: { fontSize: '1rem' } }}
                        />
                        <Box sx={{ display: "flex", justifyContent: "flex-end" }}>
                            <Button
                                variant="contained"
                                endIcon={submitting && !replyingTo ? <CircularProgress size={16} color="inherit" /> : <SendIcon />}
                                disabled={!newComment.trim() || submitting}
                                onClick={() => handleAddComment()}
                            >
                                Post Comment
                            </Button>
                        </Box>
                    </Box>
                </Box>
            </Paper>

            {loading ? (
                <Box sx={{ display: "flex", justifyContent: "center", py: 4 }}>
                    <CircularProgress />
                </Box>
            ) : (
                <List sx={{ width: '100%', bgcolor: 'background.paper' }}>
                    {comments.map((comment, index) => (
                        <React.Fragment key={comment.id}>
                            <CommentItem
                                comment={comment}
                                editingComment={editingComment}
                                setEditingComment={setEditingComment}
                                handleUpdateComment={handleUpdateComment}
                                replyingTo={replyingTo}
                                setReplyingTo={setReplyingTo}
                                replyContent={replyContent}
                                setReplyContent={setReplyContent}
                                handleAddComment={handleAddComment}
                                handleMenuOpen={handleMenuOpen}
                                handleReaction={handleReaction}
                                submitting={submitting}
                                timeAgo={timeAgo}
                            />
                            {index < comments.length - 1 && <Divider variant="inset" component="li" sx={{ my: 1 }} />}
                        </React.Fragment>
                    ))}

                    {comments.length === 0 && !loading && (
                        <Box sx={{ textAlign: "center", py: 4, color: "text.secondary" }}>
                            <Typography>No comments yet. Be the first to share your thoughts!</Typography>
                        </Box>
                    )}
                </List>
            )}

            <Menu
                anchorEl={anchorEl}
                open={Boolean(anchorEl)}
                onClose={handleMenuClose}
                anchorOrigin={{
                    vertical: 'bottom',
                    horizontal: 'right',
                }}
                transformOrigin={{
                    vertical: 'top',
                    horizontal: 'right',
                }}
            >
                <MenuItem onClick={handleEditClick}>
                    <EditIcon fontSize="small" sx={{ mr: 1 }} /> Edit
                </MenuItem>
                <MenuItem onClick={handleDeleteClick} sx={{ color: "error.main" }}>
                    <DeleteIcon fontSize="small" sx={{ mr: 1 }} /> Delete
                </MenuItem>
            </Menu>
        </Box>
    );
}
