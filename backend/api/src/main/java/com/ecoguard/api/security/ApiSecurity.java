package com.ecoguard.api.security;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;

@Configuration
public class ApiSecurity {
  @Bean
  SecurityFilterChain securityFilterChain(
      HttpSecurity http, @Value("${ecoguard.rate-limit.requests:120}") int limit) throws Exception {
    SlidingWindow limiter = new SlidingWindow(limit, 60);
    return http.csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
        .headers(
            headers ->
                headers
                    .contentSecurityPolicy(
                        csp ->
                            csp.policyDirectives(
                                "default-src 'none'; frame-ancestors 'none'; base-uri 'none'"))
                    .frameOptions(HeadersConfigurer.FrameOptionsConfig::deny)
                    .referrerPolicy(
                        referrer ->
                            referrer.policy(
                                org.springframework.security.web.header.writers
                                    .ReferrerPolicyHeaderWriter.ReferrerPolicy.NO_REFERRER)))
        .addFilterAfter(rateLimit(limiter), BasicAuthenticationFilter.class)
        .build();
  }

  private static OncePerRequestFilter rateLimit(SlidingWindow limiter) {
    return new OncePerRequestFilter() {
      @Override
      protected void doFilterInternal(
          jakarta.servlet.http.HttpServletRequest request,
          jakarta.servlet.http.HttpServletResponse response,
          jakarta.servlet.FilterChain chain)
          throws java.io.IOException, jakarta.servlet.ServletException {
        String path = request.getRequestURI();
        if ("/health".equals(path) || path.startsWith("/docs") || "/openapi.json".equals(path)) {
          chain.doFilter(request, response);
          return;
        }
        String key = request.getRemoteAddr() == null ? "unknown" : request.getRemoteAddr();
        if (!limiter.allow(key, Instant.now().getEpochSecond())) {
          response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
          response.setHeader("Retry-After", "60");
          response.setContentType("application/json");
          response.getWriter().write("{\"detail\":\"rate limit exceeded\"}");
          return;
        }
        chain.doFilter(request, response);
      }
    };
  }

  static final class SlidingWindow {
    private final int limit;
    private final long windowSeconds;
    private final ConcurrentHashMap<String, Deque<Long>> hits = new ConcurrentHashMap<>();

    SlidingWindow(int limit, long windowSeconds) {
      this.limit = limit;
      this.windowSeconds = windowSeconds;
    }

    boolean allow(String key, long nowSeconds) {
      Deque<Long> bucket = hits.computeIfAbsent(key, ignored -> new ArrayDeque<>());
      synchronized (bucket) {
        long cutoff = nowSeconds - windowSeconds;
        while (!bucket.isEmpty() && bucket.peekFirst() <= cutoff) {
          bucket.removeFirst();
        }
        if (bucket.size() >= limit) {
          return false;
        }
        bucket.addLast(nowSeconds);
        return true;
      }
    }
  }
}
