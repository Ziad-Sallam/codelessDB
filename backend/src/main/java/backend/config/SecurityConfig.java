package backend.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import backend.security.GoogleSuccessHandler;
import backend.security.JwtAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

   @Autowired
   private JwtAuthenticationFilter jwtFilter;

   @Autowired
   private GoogleSuccessHandler googleSuccessHandler;

   @Bean
   public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
      // Backend -> Google
      // oauth2/auth/google

      // Google -> Backend (redirect url)
      // /login/oauth2/google
      http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                  .requestMatchers("/oauth2/**", "/login/**", "/signup/**")
                  .permitAll().anyRequest().authenticated())
            .oauth2Login(oauth -> oauth
                  .authorizationEndpoint(a -> a.baseUri("/oauth2/auth/google"))
                  .redirectionEndpoint(r -> r.baseUri("/login/oauth2/google/**"))
                  .successHandler(googleSuccessHandler))
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

      return http.build();
   }
}