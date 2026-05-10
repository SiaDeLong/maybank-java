package com.maybank.demo.filter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import com.maybank.demo.util.LogUtil;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RequestResponseLogFilter
        extends OncePerRequestFilter {

    private final LogUtil logUtil;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        ContentCachingRequestWrapper requestWrapper =
                (request instanceof ContentCachingRequestWrapper)
                        ? (ContentCachingRequestWrapper) request
                        : new ContentCachingRequestWrapper(request, 1024 * 1024);

        ContentCachingResponseWrapper responseWrapper =
                new ContentCachingResponseWrapper(response);

        try {

            filterChain.doFilter(
                    requestWrapper,
                    responseWrapper
            );

            String requestBody = new String(
                    requestWrapper.getContentAsByteArray(),
                    StandardCharsets.UTF_8
            );

            String responseBody = new String(
                    responseWrapper.getContentAsByteArray(),
                    StandardCharsets.UTF_8
            );

            logUtil.logRequest(
                    request.getMethod(),
                    request.getRequestURI(),
                    requestBody
            );

            logUtil.logResponse(
                    response.getStatus(),
                    responseBody
            );

        } catch (Exception e) {
            logUtil.logError(e);
            throw e;
        } finally {
            responseWrapper.copyBodyToResponse();
        }
    }
}