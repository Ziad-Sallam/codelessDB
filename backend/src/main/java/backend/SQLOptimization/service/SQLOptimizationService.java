package backend.SQLOptimization.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import backend.SQLOptimization.dto.OptimizeSQLResponse;
import backend.SQLOptimization.exceptions.SQLOptimizationException.GeminiAPIException;
import backend.SQLOptimization.exceptions.SQLOptimizationException.InvalidSQLException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class SQLOptimizationService {

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    @Value("${gemini.api.url}")
    private String geminiApiUrl;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public OptimizeSQLResponse optimizeSQL(String sqlCode) {
        if (sqlCode == null || sqlCode.trim().isEmpty()) {
            throw new InvalidSQLException("SQL code cannot be empty");
        }

        try {
            String prompt = buildOptimizationPrompt(sqlCode);
            String geminiResponse = callGeminiAPI(prompt);
            return parseGeminiResponse(geminiResponse);

        } catch (GeminiAPIException e) {
            log.error("Gemini API error: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during SQL optimization", e);
            throw new GeminiAPIException("Failed to optimize SQL: " + e.getMessage());
        }
    }

    private String buildOptimizationPrompt(String sqlCode) {
        return String.format(
                """
                        You are an expert database architect and SQL optimization specialist. Your task is to analyze and optimize the provided SQL code for better performance, readability, and best practices.

                        **Original SQL:**
                        ```sql
                        %s
                        ```

                        **Your Task:**
                        1. Analyze the SQL code for potential performance issues, redundancies, and anti-patterns
                        2. Optimize the code following these priorities:
                           - Performance: Add appropriate indexes, optimize JOIN operations, remove redundant queries
                           - Readability: Improve formatting, naming conventions, and code structure
                           - Best Practices: Follow SQL standards, use proper data types, add constraints where needed
                           - Maintainability: Add comments for complex logic, use meaningful names

                        3. Provide your response in the following JSON format ONLY (no markdown, no additional text):

                        {
                          "optimizedSQL": "The complete optimized SQL code here",
                          "summary": "A concise 2-3 sentence summary explaining the key optimizations made and their expected impact on performance and maintainability"
                        }

                        **Important Guidelines:**
                        - Keep all original table names and column names unless there's a critical naming issue
                        - Maintain the original schema structure and relationships
                        - Focus on practical optimizations that provide real value
                        - If the SQL is already well-optimized, make minor improvements and acknowledge this in the summary
                        - Ensure the optimized SQL is syntactically correct and executable
                        - Add strategic indexes for foreign keys and frequently queried columns
                        - Use IF NOT EXISTS for safer execution
                        - Add helpful inline comments for complex operations

                        Return ONLY the JSON object, nothing else.
                        """,
                sqlCode);
    }

    private String callGeminiAPI(String prompt) {
        try {
            String requestBody = String.format("""
                    {
                      "contents": [{
                        "parts": [{
                          "text": %s
                        }]
                      }]
                    }
                    """, objectMapper.writeValueAsString(prompt));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            String urlWithKey = geminiApiUrl + "?key=" + geminiApiKey;

            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    urlWithKey,
                    HttpMethod.POST,
                    entity,
                    String.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new GeminiAPIException("Gemini API returned error: " + response.getStatusCode());
            }

            return response.getBody();

        } catch (Exception e) {
            log.error("Error calling Gemini API", e);
            throw new GeminiAPIException("Failed to call Gemini API: " + e.getMessage());
        }
    }

    private OptimizeSQLResponse parseGeminiResponse(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);

            String text = root.path("candidates")
                    .get(0)
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text")
                    .asText();

            String cleanedText = text.trim();
            if (cleanedText.startsWith("```json")) {
                cleanedText = cleanedText.replaceAll("```json\\n?", "").replaceAll("```\\n?", "");
            } else if (cleanedText.startsWith("```")) {
                cleanedText = cleanedText.replaceAll("```\\n?", "");
            }

            JsonNode result = objectMapper.readTree(cleanedText);

            String optimizedSQL = result.path("optimizedSQL").asText();
            String summary = result.path("summary").asText();

            if (optimizedSQL.isEmpty() || summary.isEmpty()) {
                throw new GeminiAPIException("Invalid response format from Gemini");
            }

            return new OptimizeSQLResponse(optimizedSQL, summary);

        } catch (Exception e) {
            log.error("Error parsing Gemini response", e);
            throw new GeminiAPIException("Failed to parse Gemini response: " + e.getMessage());
        }
    }
}
