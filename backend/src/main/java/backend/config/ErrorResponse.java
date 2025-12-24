package backend.config;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
@Schema(description = "Standard dynamic error response structure")
public class ErrorResponse {
	@Schema(description = "Detailed error message", example = "Invalid request parameters")
	private String message;
	@Schema(description = "HTTP status code", example = "400")
	private int status;
}