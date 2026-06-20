package com.chargeflow.security;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class PrivateNetworkCorsFilter implements Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        // If preflight options request asks for private network access, allow it explicitly
        if (request.getHeader("Access-Control-Request-Private-Network") != null) {
            response.setHeader("Access-Control-Allow-Private-Network", "true");
        }

        // In all cases, set the loopback permission header for browser Private Network Access (PNA) compatibility
        response.addHeader("Access-Control-Allow-Private-Network", "true");

        chain.doFilter(req, res);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void destroy() {}
}
