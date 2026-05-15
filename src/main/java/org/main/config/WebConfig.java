package org.main.config;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.main.loggings.InterceptorLoggingApi;
import org.main.web.resolver.CurrentUserIdArgumentResolver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.lang.NonNull;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.cors.allowed-origins:http://localhost:8080}")
    private String allowedOrigins;

    private final InterceptorLoggingApi interceptorLoggingApi;
    private final CurrentUserIdArgumentResolver currentUserIdArgumentResolver;

    public WebConfig(InterceptorLoggingApi interceptorLoggingApi,
                     CurrentUserIdArgumentResolver currentUserIdArgumentResolver) {
		this.interceptorLoggingApi = interceptorLoggingApi;
        this.currentUserIdArgumentResolver = currentUserIdArgumentResolver;
	}

    @Override
    public void addInterceptors(@NonNull InterceptorRegistry registry) {
        registry.addInterceptor(interceptorLoggingApi);
    }

    @Override
    public void addCorsMappings(@NonNull CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(allowedOrigins.split(","))
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }

	@Override
	public void addArgumentResolvers(@NonNull List<HandlerMethodArgumentResolver> resolvers) {
		resolvers.add(currentUserIdArgumentResolver);
	}
    
    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        Path imagensUsuariosDir = Paths.get(System.getProperty("user.dir"), "imagens-usuarios");
        String imagensPath = imagensUsuariosDir.toUri().toString();

        registry.addResourceHandler("/imagens-usuarios/**")
                .addResourceLocations(imagensPath);
    }
}