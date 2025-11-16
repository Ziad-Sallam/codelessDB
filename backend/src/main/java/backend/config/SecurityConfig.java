package backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

   @Bean
   public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
      // http
      //   .authorizeHttpRequests(auth -> auth
      //           .requestMatchers("/", "/public/**").permitAll()
      //           .anyRequest().authenticated()
      //   )

      http
         .csrf(csrf -> csrf.disable())
         .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
         .oauth2Login(oauth -> oauth.loginPage("/login"))
         .logout(logout -> logout.logoutSuccessUrl("/").permitAll());

      return http.build();
   }
}

// @Configuration
// public class SecurityConfig {

//     @Bean
//     public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//         http
//             .csrf(csrf -> csrf.disable())
//             .authorizeHttpRequests(auth -> auth
//                 .requestMatchers("/auth/**", "/oauth2/**").permitAll()
//                 .anyRequest().authenticated()
//             )
//             .oauth2Login(oauth -> oauth
//                 .successHandler(new GoogleSuccessHandler())
//             );

//         return http.build();
//     }
// }

// public class GoogleSuccessHandler implements AuthenticationSuccessHandler {

//     @Override
//     public void onAuthenticationSuccess(
//             HttpServletRequest request,
//             HttpServletResponse response,
//             Authentication authentication) throws IOException {

//         OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

//         String email = oAuth2User.getAttribute("email");
//         String name = oAuth2User.getAttribute("name");
//         String googleId = oAuth2User.getAttribute("sub");

//         // TODO: Create or load the user from DB
//         // User user = userService.processOAuthPostLogin(email, googleId);

//         // TODO: Generate JWT
//         String jwt = jwtService.generateToken(email);

//         // Redirect to frontend with JWT
//         String redirectUrl = "http://localhost:3000/auth/success?token=" + jwt;
//         response.sendRedirect(redirectUrl);
//     }
// }
