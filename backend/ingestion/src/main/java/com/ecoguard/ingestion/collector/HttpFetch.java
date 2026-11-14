package com.ecoguard.ingestion.collector;

import java.net.URI;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

/** Bounded retries for collector HTTP. Timeouts live on the shared RestClient. */
final class HttpFetch {
  static final int MAX_ATTEMPTS = 3;
  private static final long BASE_BACKOFF_MILLIS = 200;

  private HttpFetch() {}

  static <T> T get(RestClient http, String uri, Class<T> bodyType) {
    return get(http, URI.create(uri), bodyType, java.util.Map.of());
  }

  static <T> T get(RestClient http, URI uri, Class<T> bodyType) {
    return get(http, uri, bodyType, java.util.Map.of());
  }

  static <T> T get(
      RestClient http, URI uri, Class<T> bodyType, java.util.Map<String, String> headers) {
    RuntimeException last = null;
    for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
      try {
        var spec =
            http.get()
                .uri(uri)
                .header("Accept", "application/json, text/plain, */*")
                .header("User-Agent", "EcoGuard-ingestion/0.1");
        for (var header : headers.entrySet()) {
          spec = spec.header(header.getKey(), header.getValue());
        }
        T body = spec.retrieve().body(bodyType);
        if (body == null) {
          throw new IllegalStateException("empty response from " + uri);
        }
        return body;
      } catch (RestClientResponseException ex) {
        int status = ex.getStatusCode().value();
        if (status < 500 && status != 429) {
          throw ex;
        }
        last = ex;
      } catch (ResourceAccessException ex) {
        last = ex;
      }
      if (attempt < MAX_ATTEMPTS) {
        pause(BASE_BACKOFF_MILLIS << (attempt - 1));
      }
    }
    throw last;
  }

  private static void pause(long millis) {
    try {
      Thread.sleep(millis);
    } catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException("HTTP retry interrupted", ex);
    }
  }
}
