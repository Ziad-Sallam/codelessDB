package backend.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import backend.security.GoogleSuccessHandler;
import backend.security.JwtAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig implements WebMvcConfigurer {
	@Value("${frontend.url}")
	private String frontendUrl;
	@Autowired
	private JwtAuthenticationFilter jwtFilter;
	@Autowired
	private GoogleSuccessHandler googleSuccessHandler;

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
				.cors(cors -> {
				})
				.csrf(csrf -> csrf.ignoringRequestMatchers("/**"))
				.authorizeHttpRequests(auth -> auth
						.requestMatchers("/user/login/**",
								"/user/signup/**",
								"/user/signature/**",
								"/oauth2/**",

								"/login/oauth2/**")
						// "/agent-ws/**",
						// "/api/messages/send",
						// "/database/create-mysql-container")
						.permitAll().anyRequest().authenticated())

				.oauth2Login(oauth -> oauth.redirectionEndpoint(redirect -> redirect.baseUri("/login/oauth2/google"))
						.successHandler(googleSuccessHandler).failureUrl("/login?error=true"))
				.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/**")
				.allowedOrigins(frontendUrl)
				.allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
				.allowedHeaders("*")
				.allowCredentials(true)
				.maxAge(3600);
	}
}