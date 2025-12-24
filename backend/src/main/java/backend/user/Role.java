package backend.user;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "User permission level for diagram collaboration: READER (view only), WRITER (can edit), OWNER (full control)")
public enum Role {
	@Schema(description = "View-only access")
	READER, 
	@Schema(description = "Edit access")
	WRITER, 
	@Schema(description = "Full ownership and control")
	OWNER
}