package org.main.loggings;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

@Component
public class InterceptorLoggingApi implements HandlerInterceptor {

	private static final Logger accessLog = LoggerFactory.getLogger("API_ACCESS_LOG");

	private static final String START_TIME = "startTime";
	private static final String REDACTED = "<redacted>";
	private static final Set<String> SENSITIVE_QUERY_PARAMS = Set.of(
			"token",
			"access_token",
			"id_token",
			"refresh_token",
			"jwt",
			"code",
			"state",
			"client_secret",
			"authorization",
			"password",
			"senha",
			"signature",
			"sig"
	);

	@Override
	public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) {

		request.setAttribute(START_TIME, System.currentTimeMillis());
		return true;
	}

	@Override
	public void afterCompletion(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler,
			@Nullable Exception ex) {

		long startTime = (long) request.getAttribute(START_TIME);
		long duration = System.currentTimeMillis() - startTime;

		String ip = request.getRemoteAddr();
		String method = request.getMethod();
		String uri = sanitizedRequestTarget(request);
		int status = response.getStatus();

		accessLog.info("IP={} METHOD={} URI={} STATUS={} TIME={}ms", ip, method, uri, status, duration);
	}

	public static String sanitizedRequestTarget(HttpServletRequest request) {
		String uri = request.getRequestURI();
		String query = request.getQueryString();
		if (query == null || query.isBlank()) {
			return uri;
		}

		String sanitizedQuery = Arrays.stream(query.split("&", -1))
				.map(InterceptorLoggingApi::sanitizeQueryParam)
				.collect(Collectors.joining("&"));

		return uri + "?" + sanitizedQuery;
	}

	private static String sanitizeQueryParam(String queryParam) {
		int separatorIndex = queryParam.indexOf('=');
		String name = separatorIndex >= 0 ? queryParam.substring(0, separatorIndex) : queryParam;
		String normalizedName = decode(name).toLowerCase(Locale.ROOT);

		if (SENSITIVE_QUERY_PARAMS.contains(normalizedName)) {
			return separatorIndex >= 0 ? name + "=" + REDACTED : name;
		}

		return queryParam;
	}

	private static String decode(String value) {
		try {
			return URLDecoder.decode(value, StandardCharsets.UTF_8);
		} catch (IllegalArgumentException ex) {
			return value;
		}
	}
}
