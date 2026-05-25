package com.lumina.backend.swagger;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class SwaggerHtmlFilter implements Filter {

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		HttpServletRequest req = (HttpServletRequest) request;

		// Intercepta apenas a chamada do HTML inicial do Swagger
		if (req.getRequestURI().contains("/swagger-ui/index.html")
				|| req.getRequestURI().contains("/swagger-ui.html")) {

			ContentCachingResponseWrapper wrapper = new ContentCachingResponseWrapper((HttpServletResponse) response);

			chain.doFilter(request, wrapper);

			byte[] responseArray = wrapper.getContentAsByteArray();
			String html = new String(responseArray, StandardCharsets.UTF_8);

			// Injeta o CSS customizado da LUMINA
			html = html.replace(
					"</head>",
					"<link rel=\"stylesheet\" type=\"text/css\" href=\"/custom.css\" /></head>");

			byte[] modifiedResponse = html.getBytes(StandardCharsets.UTF_8);
			response.setContentLength(modifiedResponse.length);
			response.getOutputStream().write(modifiedResponse);
			response.flushBuffer();

		} else {
			// Para qualquer outra rota, segue o fluxo normal
			chain.doFilter(request, response);
		}
	}
}
