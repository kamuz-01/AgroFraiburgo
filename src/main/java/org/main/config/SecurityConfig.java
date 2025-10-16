package org.main.config;

import java.util.Map;
import org.main.models.Usuario;
import org.main.services.JwtService;
import org.main.services.UsuarioService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
public class SecurityConfig {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.access-token-ttl-seconds}")
    private long jwtTtl;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http, JwtService jwtService, JwtAuthenticationFilter jwtAuthFilter,
                                    JwtRefreshFilter jwtRefreshFilter, UsuarioService usuarioService) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(Customizer.withDefaults())
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterAfter(jwtRefreshFilter, JwtAuthenticationFilter.class)
            .authorizeHttpRequests(auth -> auth
                // Recursos públicos
                .requestMatchers(
                    "/",
                    "/pagina_inicial.html",
                    "/pagina_inicial",
                    "/criar_conta.html",
                    "/login.html",
                    "/login",
                    "/criar_conta",
                    "/upload_documentos",
                    "/produtos",
                    "/sobre",
                    "/upload_documentos.html",
                    "/produtos.html",
                    "/sobre.html",
                    "/manifest.json",
                    "/service-worker.js",
                    "/imagens/**",
                    "/css/**",
                    "/api/auth/register/**",
                    "/api/usuarios/cadastro-multipart", 
                    "/oauth2/**",
                    "/login/oauth2/**",
                    "/js/**"
                ).permitAll()
                .requestMatchers("/favicon.ico").permitAll()
                // Recursos restringidos por perfil
                .requestMatchers("/home_consumidor", "/home_consumidor.html").hasRole("CONSUMIDOR")
                .requestMatchers("/home_produtor", "/home_produtor.html").hasRole("PRODUTOR")
				.requestMatchers("/cadastro_produtos", "/cadastro_produtos.html").hasRole("PRODUTOR")
				.requestMatchers("/lista_produtos").hasRole("PRODUTOR")
                .requestMatchers("/home_moderador", "/home_moderador.html").hasRole("MODERADOR")
                .requestMatchers("/home_moderador/**").hasRole("MODERADOR")
                .requestMatchers("/produtores_pendentes", "/produtores_pendentes.html").hasRole("MODERADOR")
                .requestMatchers("/produtores_pendentes").hasRole("MODERADOR")
                .requestMatchers("/listagem_feiras", "/listagem_feiras.html").hasRole("MODERADOR")
                .requestMatchers("/cadastro_feira", "/cadastro_feira.html").hasRole("MODERADOR")
                .requestMatchers("/moderacao_usuarios", "/moderacao_usuarios.html").hasRole("MODERADOR")
                // APIs específicas por perfil
                .requestMatchers(HttpMethod.GET, "/api/auth/me").authenticated()
                .requestMatchers("/api/produtor/**").hasRole("PRODUTOR")
                .requestMatchers("/api/produtos/**").hasRole("PRODUTOR")
                .requestMatchers("/api/produtos/me").hasRole("PRODUTOR")
                .requestMatchers("/api/consumidor/**").hasRole("CONSUMIDOR")
                .requestMatchers("/api/moderador/produtores/*/documento/*/download").hasRole("MODERADOR")
                .requestMatchers("/api/moderador/produtores/alterar-status").hasRole("MODERADOR")
                .requestMatchers("/api/moderador/feiras/**").hasRole("MODERADOR")
                .requestMatchers("/api/moderacao/usuarios").hasRole("MODERADOR")
                .requestMatchers("/api/moderacao/usuarios/*/status").hasRole("MODERADOR")
                .requestMatchers("/api/moderador/**").hasRole("MODERADOR")
                .requestMatchers("/api/auth/login").permitAll()
                .requestMatchers("/login.html", "/criar_conta.html", "/api/auth/**").permitAll()
                // Qualquer outra requisição que requer login
                .anyRequest().authenticated()
            )
            // Login formulário tradicional
            .formLogin(form -> form
            	    .loginPage("/login.html")
            	    .failureHandler(new CustomAuthFailureHandler())
            	    .permitAll()
            	    .successHandler((request, response, authentication) -> {
            	        // Pega o usuário local correspondente
            	        Usuario usuarioLocal = usuarioService.buscarPorNomeLogin(authentication.getName())
            	                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

            	        // Gera claims com fotos e informações do usuário
            	        Map<String, Object> claims = JwtService.defaultClaims(authentication, usuarioLocal);
            	        String token = jwtService.generateToken(claims);

            	        Cookie cookie = new Cookie("AF_AUTH", token);
            	        cookie.setHttpOnly(true);
            	        cookie.setSecure(true); // use true em produção com HTTPS
            	        cookie.setPath("/");
            	        cookie.setMaxAge((int) jwtTtl);
            	        cookie.setAttribute("SameSite", "None");
            	        response.addCookie(cookie);

            	        // Redireciona conforme perfil
            	        String redirectUrl = getRedirectUrl(authentication);
            	        response.sendRedirect(redirectUrl);
            	    })
            	)
            .exceptionHandling(ex -> ex
            	    .authenticationEntryPoint((request, response, authException) -> {
            	        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            	        response.setContentType("application/json;charset=UTF-8");
            	        response.getWriter().write("""
            	            {
            	        	  "status": 401,
            	              "error": "Usuário ou senha inválidos",
            	              "message": "Usuário ou senha inválidos",
            	              "path": "%s"
            	            }
            	            """.formatted(request.getRequestURI()));
            	    })
            	    .accessDeniedHandler(new CustomAccessDeniedHandler())
            	)

            // Login OAuth2 (Google e Facebook → sempre consumidores)
            .oauth2Login(oauth -> oauth
            	    .loginPage("/login.html")
            	    .successHandler((request, response, authentication) -> {
            	        if (authentication instanceof OAuth2AuthenticationToken oauthToken) {
            	            String provider = oauthToken.getAuthorizedClientRegistrationId();
            	            Map<String, Object> attributes = oauthToken.getPrincipal().getAttributes();

            	            // Cria ou busca usuário local
            	            Usuario u = usuarioService.processOAuthPostLogin(provider, attributes);

            	            // Claims consistentes (já com uid e sub = id local)
            	            Map<String, Object> claims = JwtService.defaultClaims(attributes, u);

            	            String token = jwtService.generateToken(claims);

            	            Cookie cookie = new Cookie("AF_AUTH", token);
            	            cookie.setHttpOnly(true);
            	            cookie.setSecure(true);
            	            cookie.setPath("/");
            	            cookie.setMaxAge((int) jwtTtl);
            	            cookie.setAttribute("SameSite", "Lax");
            	            response.addCookie(cookie);
            	            response.sendRedirect("/home_consumidor");
            	        }
            	    })
            	);

        return http.build();
    }

    private String getRedirectUrl(Authentication authentication) {
        if (authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_CONSUMIDOR"))) {
            return "/home_consumidor";
        } else if (authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_PRODUTOR"))) {
            return "/home_produtor";
        } else if (authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_MODERADOR"))) {
            return "/home_moderador";
        }
        return "/pagina_inicial.html"; // fallback padrão
    }
}