package org.Main.loggings;

import org.junit.jupiter.api.Test;
import org.main.loggings.InterceptorLoggingApi;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

class InterceptorLoggingApiTest {

    @Test
    void sanitizedRequestTargetDeveMascararParametrosSensiveisDoOAuth() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/login/oauth2/code/google");
        request.setQueryString("code=jwt-code-123&state=csrf-state-456&scope=email");

        String target = InterceptorLoggingApi.sanitizedRequestTarget(request);

        assertThat(target).isEqualTo("/login/oauth2/code/google?code=<redacted>&state=<redacted>&scope=email");
        assertThat(target).doesNotContain("jwt-code-123", "csrf-state-456");
    }

    @Test
    void sanitizedRequestTargetDeveMascararTokenDeQueryParam() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/redefinir_senha");
        request.setQueryString("token=abc.def.ghi");

        String target = InterceptorLoggingApi.sanitizedRequestTarget(request);

        assertThat(target).isEqualTo("/redefinir_senha?token=<redacted>");
        assertThat(target).doesNotContain("abc.def.ghi");
    }

    @Test
    void sanitizedRequestTargetDevePreservarUriSemQueryString() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/auth/login");

        assertThat(InterceptorLoggingApi.sanitizedRequestTarget(request)).isEqualTo("/api/auth/login");
    }
}
