package com.lumina.backend.swagger;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@DisplayName("Testes unitários SwaggerHtmlFilter")
class SwaggerHtmlFilterTest {

    private final SwaggerHtmlFilter filter = new SwaggerHtmlFilter();

    @Test
    @DisplayName("Deve injetar custom.css no HTML do Swagger UI")
    void deveInjetarCssNoSwaggerHtml() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/swagger-ui/index.html");
        MockHttpServletResponse response = new MockHttpServletResponse();

        FilterChain chain = (req, res) -> {
            res.getOutputStream().write("<html><head><title>Swagger</title></head><body></body></html>"
                    .getBytes(StandardCharsets.UTF_8));
        };

        filter.doFilter(request, response, chain);

        String body = response.getContentAsString(StandardCharsets.UTF_8);
        assertTrue(body.contains("<link rel=\"stylesheet\" type=\"text/css\" href=\"/custom.css\" /></head>"));
    }

    @Test
    @DisplayName("Deve seguir fluxo normal para rotas que não são do Swagger UI")
    void deveSeguirFluxoNormalParaOutrasRotas() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/clientes");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        assertFalse(response.getContentAsString().contains("custom.css"));
    }
}
