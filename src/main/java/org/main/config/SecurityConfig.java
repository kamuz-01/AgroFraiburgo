package org.main.config;

import java.util.Map;
import java.io.IOException;
import org.main.models.Usuario;
import org.main.services.JwtService;
import org.main.services.LoginProtecaoService;
import org.main.services.LoginRateLimitService;
import org.main.services.UsuarioService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
public class SecurityConfig {

    @Value("${jwt.access-token-ttl-seconds}")
    private long jwtTtl;

    @Value("${app.cookie.secure:false}")
    private boolean cookieSecure;

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
                                    JwtRefreshFilter jwtRefreshFilter, UsuarioService usuarioService,
                                    LoginProtecaoService loginProtecaoService,
                                    LoginRateLimitService loginRateLimitService,
                                    CustomAuthFailureHandler customAuthFailureHandler) throws Exception {
        String contentSecurityPolicy = "default-src 'self'; "
                + "base-uri 'self'; "
                + "object-src 'none'; "
                + "frame-ancestors 'self'; "
                + "form-action 'self'; "
                + "img-src 'self' data: https:; "
                + "font-src 'self' https: data:; "
                + "style-src 'self' 'unsafe-inline' https:; "
                + "script-src 'self' 'unsafe-inline' https:; "
                + "connect-src 'self' https:;";

        CsrfTokenRequestAttributeHandler csrfRequestHandler = new CsrfTokenRequestAttributeHandler();
        csrfRequestHandler.setCsrfRequestAttributeName("_csrf");

        http
            .csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .csrfTokenRequestHandler(csrfRequestHandler)
            )
            .cors(Customizer.withDefaults())
            .headers(headers -> headers
                .contentSecurityPolicy(csp -> csp.policyDirectives(contentSecurityPolicy))
                .frameOptions(frame -> frame.sameOrigin())
                .contentTypeOptions(Customizer.withDefaults())
                .httpStrictTransportSecurity(hsts -> hsts
                    .includeSubDomains(true)
                    .preload(true)
                    .maxAgeInSeconds(31536000))
            )
            .addFilterAfter(new CsrfCookieFilter(), CsrfFilter.class)
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterAfter(jwtRefreshFilter, JwtAuthenticationFilter.class)
            .authorizeHttpRequests(auth -> auth
                // Avaliações
                .requestMatchers(HttpMethod.POST, "/produtores/*/avaliar").hasRole("CONSUMIDOR")
                .requestMatchers(HttpMethod.POST, "/api/avaliacoes/**").hasRole("CONSUMIDOR")
                .requestMatchers(HttpMethod.GET, "/api/avaliacoes/**").permitAll()
                // Favoritos (consumidor e moderador)
                .requestMatchers(HttpMethod.POST, "/api/favoritos/**").hasAnyRole("CONSUMIDOR", "MODERADOR")
                // Recursos públicos
                .requestMatchers(
                    "/",
                    "/pagina_inicial.html",
                    "/pagina_inicial",
                    "/inicio_usuarios",
                    "/inicio_usuarios.html",
                    "/inicio_usuarios/**",
                    "/criar_conta.html",
                    "/login.html",
                    "/login",
                    "/criar_conta",
                    "/recuperar_senha",
                    "/redefinir_senha",
                    "/upload_documentos",
                    "/produtos",
                    "/produto/**",
                    "/produtores",
                    "/produtores/**",
                    "/feira",
                    "/sobre",
                    "/buscar",
                    "/offline",
                    "/upload_documentos.html",
                    "/produtos.html",
                    "/sobre.html",
                    "/error",
                    "/error/**",
                    "/manifest.json",
                    "/service-worker.js",
                    "/imagens/**",
                    "/css/**",
                    "/api/usuarios/cadastro-multipart", 
                    "/oauth2/**",
                    "/login/oauth2/**",
                    "/js/**"
                ).permitAll()
                .requestMatchers("/favicon.ico").permitAll()
                // Recursos restringidos por perfil
                .requestMatchers("/home_consumidor", "/home_consumidor.html").hasRole("CONSUMIDOR")
                .requestMatchers("/home_produtor", "/home_produtor.html").hasRole("PRODUTOR")
                .requestMatchers("/catalogo_produtor", "/catalogo_produtor.html").hasRole("PRODUTOR")
				.requestMatchers("/cadastro_produtos", "/cadastro_produtos.html").hasRole("PRODUTOR")
				.requestMatchers("/lista_produtos").hasRole("PRODUTOR")
                .requestMatchers("/home_moderador", "/home_moderador.html").hasRole("MODERADOR")
                .requestMatchers("/home_moderador/**").hasRole("MODERADOR")
                .requestMatchers("/produtores_pendentes", "/produtores_pendentes.html").hasRole("MODERADOR")
                .requestMatchers("/produtores_pendentes/**").hasRole("MODERADOR")
                .requestMatchers("/produtores_pendentes").hasRole("MODERADOR")
                .requestMatchers("/listagem_feiras", "/listagem_feiras.html").hasRole("MODERADOR")
                .requestMatchers("/cadastro_feira", "/cadastro_feira.html").hasRole("MODERADOR")
                .requestMatchers("/cadastro_moderadores", "/cadastro_moderadores.html").hasRole("MODERADOR")
                .requestMatchers("/administrar_usuarios", "/administrar_usuarios.html").hasRole("MODERADOR")
                .requestMatchers("/moderacao_usuarios", "/moderacao_usuarios.html").hasRole("MODERADOR")
                // APIs específicas por perfil
                .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/auth/facebook").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/auth/me").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/auth/logout").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/cadastro/moderador").hasRole("MODERADOR")
                .requestMatchers("/api/produtor/**").hasRole("PRODUTOR")
                .requestMatchers("/api/produtos/**").hasRole("PRODUTOR")
                .requestMatchers("/api/produtos/me").hasRole("PRODUTOR")
                .requestMatchers("/api/consumidor/**").hasRole("CONSUMIDOR")
                .requestMatchers("/api/moderador/produtores/pendentes").hasRole("MODERADOR")
                .requestMatchers("/api/moderador/produtores/*/documento/*/download").hasRole("MODERADOR")
                .requestMatchers("/api/moderador/produtores/alterar-status").hasRole("MODERADOR")
                .requestMatchers("/api/moderador/feiras/**").hasRole("MODERADOR")
                .requestMatchers("/api/moderacao/usuarios").hasRole("MODERADOR")
                .requestMatchers("/api/moderacao/usuarios/*/status").hasRole("MODERADOR")
                .requestMatchers("/api/moderador/**").hasRole("MODERADOR")
                .requestMatchers("/login.html", "/criar_conta.html").permitAll()
                // Qualquer outra requisição que requer login
                .anyRequest().authenticated()
            )
            // Login formulário tradicional
            .formLogin(form -> form
            	    .loginPage("/login.html")
                    .failureHandler(customAuthFailureHandler)
            	    .permitAll()
            	    .successHandler((request, response, authentication) -> {
                        String ip = extrairIpCliente(request);

            	        // Pega o usuário local correspondente
            	        Usuario usuarioLocal = usuarioService.buscarPorNomeLogin(authentication.getName())
            	                .orElse(null);

            	        if (usuarioLocal == null) {
            	            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Usuário não encontrado");
            	            return;
            	        }

                        loginRateLimitService.registrarSucesso(ip);
                        loginProtecaoService.registrarSucesso(usuarioLocal);

            	        // Gera claims com fotos e informações do usuário
            	        Map<String, Object> claims = JwtService.defaultClaims(authentication, usuarioLocal);
            	        String token = jwtService.generateToken(claims);

            	        Cookie cookie = new Cookie("AF_AUTH", token);
            	        cookie.setHttpOnly(true);
            	        cookie.setSecure(cookieSecure);
            	        cookie.setPath("/");
            	        cookie.setMaxAge((int) jwtTtl);
            	        cookie.setAttribute("SameSite", cookieSecure ? "None" : "Lax");
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
                        String ip = extrairIpCliente(request);

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
            	            cookie.setSecure(cookieSecure);
            	            cookie.setPath("/");
            	            cookie.setMaxAge((int) jwtTtl);
            	            cookie.setAttribute("SameSite", "Lax");
            	            response.addCookie(cookie);

                            loginRateLimitService.registrarSucesso(ip);
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

    private String extrairIpCliente(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }

        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }

        return request.getRemoteAddr();
    }

    private static final class CsrfCookieFilter extends OncePerRequestFilter {
        @Override
        protected void doFilterInternal(HttpServletRequest request,
                                        HttpServletResponse response,
                                        FilterChain filterChain) throws ServletException, IOException {
            CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
            if (csrfToken != null) {
                csrfToken.getToken();
            }
            filterChain.doFilter(request, response);
        }
    }
}
