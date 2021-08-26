/*
 * Copyright (c) 2016-2019, Inversoft Inc., All Rights Reserved
 */
package com.inversoft.rest;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import static com.inversoft.rest.RESTClient.HTTPMethod;
import static com.inversoft.rest.RESTClient.ProxyInfo;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

/**
 * @author Brian Pontarelli
 */
@SuppressWarnings("rawtypes")
public class RESTClientTest {
  private HttpServer server;

  @AfterMethod
  public void afterMethod() {
    if (server != null) {
      server.stop(0);
      server = null;
    }
  }

  @Test
  public void delete_json() throws Exception {
    TestHandler handler = new TestHandler(null, null, null, "DELETE", 200, "{\"code\": 200}");
    startServer(handler);

    ClientResponse<Map, Map> response = new RESTClient<>(Map.class, Map.class)
        .url("http://localhost:7000/test")
        .errorResponseHandler(new JSONResponseHandler<>(Map.class))
        .successResponseHandler(new JSONResponseHandler<>(Map.class))
        .delete()
        .go();

    assertEquals(handler.count, 1);
    assertEquals(response.url, new URL("http://localhost:7000/test"));
    assertEquals(response.method, HTTPMethod.DELETE);
    assertEquals(response.status, 200);
    assertEquals(response.successResponse.get("code"), 200);

    assertNotNull(response.date);
  }

  @Test
  public void get_JSONParseException() throws Exception {
    TestHandler handler = new TestHandler(null, null, null, "GET", 403, "<html><body>Hello!</body></html>");
    startServer(handler);

    // Expecting JSON, but get HTML
    ClientResponse<Map, Map> response = new RESTClient<>(Map.class, Map.class)
        .url("http://localhost:7000/test")
        .errorResponseHandler(new JSONResponseHandler<>(Map.class))
        .successResponseHandler(new JSONResponseHandler<>(Map.class))
        .get()
        .go();

    assertEquals(handler.count, 1);
    assertEquals(response.url, new URL("http://localhost:7000/test"));
    assertEquals(response.method, HTTPMethod.GET);
    assertEquals(response.status, 403);
    assertFalse(response.wasSuccessful());
    assertNull(response.errorResponse);
    assertNull(response.successResponse);
    assertNotNull(response.exception);
    assertEquals(response.exception.getMessage(), "Failed to parse the HTTP response as JSON. Actual HTTP response body:\n" +
        "<html><body>Hello!</body></html>");
  }

  @Test
  public void get_JSONParseExceptionTruncatedResponse() throws Exception {
    TestHandler handler = new TestHandler(null, null, null, "GET", 403, "<html><body>Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum. Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum. Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.</body></html>");
    startServer(handler);

    // Expecting JSON, but get HTML
    ClientResponse<Map, Map> response = new RESTClient<>(Map.class, Map.class)
        .url("http://localhost:7000/test")
        .errorResponseHandler(new JSONResponseHandler<>(Map.class))
        .successResponseHandler(new JSONResponseHandler<>(Map.class))
        .get()
        .go();

    assertEquals(handler.count, 1);
    assertEquals(response.url, new URL("http://localhost:7000/test"));
    assertEquals(response.method, HTTPMethod.GET);
    assertEquals(response.status, 403);
    assertFalse(response.wasSuccessful());
    assertNull(response.errorResponse);
    assertNull(response.successResponse);
    assertNotNull(response.exception);
    assertEquals(response.exception.getMessage(), "Failed to parse the HTTP response as JSON. Actual HTTP response body:\n" +
        "Note: Output has been truncated to the first 1024 of 1363 bytes.\n" +
        "\n" +
        "<html><body>Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum. Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum. Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliq");
  }

  @Test
  public void get_emptyJSON() throws Exception {
    TestHandler handler = new TestHandler(null, null, null, "GET", 200, "");
    startServer(handler);

    ClientResponse<Map, Map> response = expectException(() -> new RESTClient<>(Map.class, Map.class)
        .url("http://localhost:7000/test")
        .errorResponseHandler(new JSONResponseHandler<>(Map.class))
        .successResponseHandler(new JSONResponseHandler<>(Map.class))
        .get()
        .go(), JSONException.class);

    assertEquals(handler.count, 1);
    assertNotNull(response);
    assertEquals(response.url, new URL("http://localhost:7000/test"));
    assertEquals(response.method, HTTPMethod.GET);
    assertEquals(response.status, 200);
    assertNull(response.exception);
    assertTrue(response.wasSuccessful());
    assertNull(response.successResponse);
  }

  @Test
  public void get_emptyJSON_error_404() throws Exception {
    TestHandler handler = new TestHandler(null, null, null, "GET", 404, "");
    startServer(handler);

    ClientResponse<Map, Map> response = new RESTClient<>(Map.class, Map.class)
        .url("http://localhost:7000/test")
        .errorResponseHandler(new JSONResponseHandler<>(Map.class))
        .successResponseHandler(new JSONResponseHandler<>(Map.class))
        .get()
        .go();

    assertEquals(handler.count, 1);
    assertEquals(response.url, new URL("http://localhost:7000/test"));
    assertEquals(response.method, HTTPMethod.GET);
    assertEquals(response.status, 404);
    assertFalse(response.wasSuccessful());
    assertNull(response.exception);
    assertNull(response.errorResponse);
    assertNull(response.successResponse);
  }

  @Test
  public void get_forgotErrorResponseHandler() throws Exception {
    TestHandler handler = new TestHandler(null, null, null, "GET", 200, "");
    startServer(handler);

    expectException(() ->
        new RESTClient<>(Map.class, Map.class)
            .url("http://localhost:7000/test")
            .successResponseHandler(new JSONResponseHandler<>(Map.class))
            .get()
            .go(), IllegalStateException.class);

    expectException(() ->
        new RESTClient<>(Map.class, Map.class)
            .url("http://localhost:7000/test")
            .errorResponseHandler(new JSONResponseHandler<>(Map.class))
            .get()
            .go(), IllegalStateException.class);
  }

  @Test
  public void get_headers() throws Exception {
    Map<String, String> headers = new HashMap<>();
    headers.put("Authorization", "key");
    headers.put("header1", "value1");

    TestHandler handler = new TestHandler(null, null, headers, "GET", 200, null);
    startServer(handler);

    ClientResponse<String, String> response = new RESTClient<>(String.class, String.class)
        .url("http://localhost:7000/test")
        .authorization("key")
        .header("header1", "value1")
        .errorResponseHandler(new TextResponseHandler())
        .successResponseHandler(new TextResponseHandler())
        .get()
        .go();

    assertEquals(handler.count, 1);
    assertEquals(response.url, new URL("http://localhost:7000/test"));
    assertEquals(response.method, HTTPMethod.GET);
    assertEquals(response.status, 200);
    assertEquals(response.successResponse, "");
  }

  @Test
  public void get_json() throws Exception {
    TestHandler handler = new TestHandler(null, null, null, "GET", 200, "{\"code\": 200}");
    startServer(handler);

    ClientResponse<Map, Map> response = new RESTClient<>(Map.class, Map.class)
        .url("http://localhost:7000/test")
        .errorResponseHandler(new JSONResponseHandler<>(Map.class))
        .successResponseHandler(new JSONResponseHandler<>(Map.class))
        .get()
        .go();

    assertEquals(handler.count, 1);
    assertEquals(response.url, new URL("http://localhost:7000/test"));
    assertEquals(response.method, HTTPMethod.GET);
    assertEquals(response.status, 200);
    assertEquals(response.successResponse.get("code"), 200);
  }

  @Test
  public void get_ssl_get_parameters() {
    ZonedDateTime now = ZonedDateTime.now();

    // Test null segment, null parameter, ZoneDateTime parameter, and a collection parameter
    RESTClient<Void, Void> client = new RESTClient<>(Void.TYPE, Void.TYPE)
        .url("https://www.inversoft.com")
        .urlSegment(null)
        .urlSegment("latest-clean-speak-version")
        .urlParameter("time", now)
        .urlParameter("foo", "bar")
        .urlParameter("baz", null)
        .urlParameter("ids", new ArrayList<>(Arrays.asList(new UUID(1, 0), new UUID(2, 0))))
        .get();

    assertEquals(client.url.toString(), "https://www.inversoft.com/latest-clean-speak-version");

    assertEquals(client.parameters.get("time").size(), 1);
    assertEquals(client.parameters.get("time").get(0), now.toInstant().toEpochMilli());

    assertEquals(client.parameters.get("foo").size(), 1);
    assertEquals(client.parameters.get("foo").get(0), "bar");

    assertNull(client.parameters.get("baz"));

    client.go(); // finish building the final URL
    assertEquals(client.url.toString(), "https://www.inversoft.com/latest-clean-speak-version?time="
        + now.toInstant().toEpochMilli() + "&foo=bar&ids=" + new UUID(1, 0).toString() + "&ids=" + new UUID(2, 0).toString());
  }

  @Test
  public void get_url_parameters_from_map() {
    ZonedDateTime now = ZonedDateTime.now();

    Map<String, Object> parameters = new LinkedHashMap<>();
    parameters.put("time", now);
    parameters.put("string", "value");
    parameters.put("null", null);
    parameters.put("list", new ArrayList<>(Arrays.asList(new UUID(1, 0), new UUID(2, 0))));

    // Test null parameter, ZoneDateTime parameter, and a collection parameter as added from a Map
    RESTClient<Void, Void> client = new RESTClient<>(Void.TYPE, Void.TYPE)
        .url("https://www.inversoft.com")
        .urlParameters(null)
        .urlParameters(parameters)
        .get();

    assertEquals(client.parameters.get("time").size(), 1);
    assertEquals(client.parameters.get("time").get(0), now.toInstant().toEpochMilli());

    assertEquals(client.parameters.get("string").size(), 1);
    assertEquals(client.parameters.get("string").get(0), "value");

    assertNull(client.parameters.get("null"));

    client.go(); // finish building the final URL
    assertEquals(client.url.toString(), "https://www.inversoft.com?time="
        + now.toInstant().toEpochMilli() + "&string=value&list=" + new UUID(1, 0).toString() + "&list=" + new UUID(2, 0).toString());
  }

  @Test
  public void head() throws Exception {
    TestHandler handler = new TestHandler(null, null, null, "HEAD", 200, "{\"code\": 200}");
    startServer(handler);

    ClientResponse<Map, Map> response = new RESTClient<>(Map.class, Map.class)
        .url("http://localhost:7000/test")
        .errorResponseHandler(new JSONResponseHandler<>(Map.class))
        .successResponseHandler(new JSONResponseHandler<>(Map.class))
        .head()
        .go();

    assertEquals(handler.count, 1);
    assertEquals(response.url, new URL("http://localhost:7000/test"));
    assertEquals(response.method, HTTPMethod.HEAD);
    assertEquals(response.status, 200);
    assertNull(response.successResponse);
  }

  @Test
  public void patch_json_json() throws Exception {
    TestHandler handler = new TestHandler("{\"test1\":\"value1\",\"test2\":\"value2\"}", "application/json", null, "PATCH", 200, "{\"code\": 200}");
    startServer(handler);

    Map<String, String> parameters = new LinkedHashMap<>();
    parameters.put("test1", "value1");
    parameters.put("test2", "value2");

    ClientResponse<Map, Map> response = new RESTClient<>(Map.class, Map.class)
        .url("http://localhost:7000/test")
        .bodyHandler(new JSONBodyHandler(parameters))
        .errorResponseHandler(new JSONResponseHandler<>(Map.class))
        .successResponseHandler(new JSONResponseHandler<>(Map.class))
        .patch()
        .go();

    assertEquals(handler.count, 1);
    assertSame(response.request, parameters);
    assertEquals(response.url, new URL("http://localhost:7000/test"));
    // We're using POST with X-HTTP-Method-Override for PATCH
    assertEquals(response.method, HTTPMethod.POST);
    assertEquals(response.status, 200);
    assertEquals(response.successResponse.get("code"), 200);
  }

  @Test
  public void post_formData_string() throws Exception {
    TestHandler handler = new TestHandler("test1=value1&test2=value2", "application/x-www-form-urlencoded", null, "POST", 200, "Testing 123");
    startServer(handler);

    Map<String, String> parameters = new LinkedHashMap<>();
    parameters.put("test1", "value1");
    parameters.put("test2", "value2");

    ClientResponse<String, String> response = new RESTClient<>(String.class, String.class)
        .url("http://localhost:7000/test")
        .bodyHandler(new FormDataBodyHandler(parameters))
        .errorResponseHandler(new TextResponseHandler())
        .successResponseHandler(new TextResponseHandler())
        .post()
        .go();

    assertEquals(handler.count, 1);
    assertSame(response.request, parameters);
    assertEquals(response.url, new URL("http://localhost:7000/test"));
    assertEquals(response.method, HTTPMethod.POST);
    assertEquals(response.status, 200);
    assertEquals(response.successResponse, "Testing 123");
  }

  @Test
  public void post_inputStream_json() throws Exception {
    TestHandler handler = new TestHandler("Testing 123", "application/octet-stream", null, "POST", 200, "{\"code\": 200}");
    startServer(handler);

    ByteArrayInputStream bais = new ByteArrayInputStream("Testing 123".getBytes());
    ClientResponse<Map, Map> response = new RESTClient<>(Map.class, Map.class)
        .url("http://localhost:7000/test")
        .bodyHandler(new InputStreamBodyHandler("application/octet-stream", bais))
        .errorResponseHandler(new JSONResponseHandler<>(Map.class))
        .successResponseHandler(new JSONResponseHandler<>(Map.class))
        .post()
        .go();

    assertEquals(handler.count, 1);
    assertSame(response.request, bais);
    assertEquals(response.url, new URL("http://localhost:7000/test"));
    assertEquals(response.method, HTTPMethod.POST);
    assertEquals(response.status, 200);
    assertEquals(response.successResponse.get("code"), 200);
  }

  @Test
  public void post_json_json() throws Exception {
    TestHandler handler = new TestHandler("{\"test1\":\"value1\",\"test2\":\"value2\"}", "application/json", null, "POST", 200, "{\"code\": 200}");
    startServer(handler);

    Map<String, String> parameters = new LinkedHashMap<>();
    parameters.put("test1", "value1");
    parameters.put("test2", "value2");

    ClientResponse<Map, Map> response = new RESTClient<>(Map.class, Map.class)
        .url("http://localhost:7000/test")
        .bodyHandler(new JSONBodyHandler(parameters))
        .errorResponseHandler(new JSONResponseHandler<>(Map.class))
        .successResponseHandler(new JSONResponseHandler<>(Map.class))
        .post()
        .go();

    assertEquals(handler.count, 1);
    assertSame(response.request, parameters);
    assertEquals(response.url, new URL("http://localhost:7000/test"));
    assertEquals(response.method, HTTPMethod.POST);
    assertEquals(response.status, 200);
    assertEquals(response.successResponse.get("code"), 200);
  }

  @Test(enabled = false)
  public void proxy() {
    ClientResponse<String, String> response = new RESTClient<>(String.class, String.class)
        .url("http://info.cern.ch")
        .errorResponseHandler(new TextResponseHandler())
        .successResponseHandler(new TextResponseHandler())
        .proxy(new ProxyInfo("localhost", 12345, "admin", "password"))
        .get()
        .go();
    assertTrue(response.wasSuccessful());
    assertNotNull(response.successResponse);
    assertTrue(response.successResponse.contains("<html"));

    response = new RESTClient<>(String.class, String.class)
        .url("http://info.cern.ch")
        .errorResponseHandler(new TextResponseHandler())
        .successResponseHandler(new TextResponseHandler())
        .proxy(new ProxyInfo("localhost", 12345))
        .get()
        .go();
    assertFalse(response.wasSuccessful());
    assertEquals(response.status, 407);
  }

  @Test
  public void put_formData_errorString() throws Exception {
    TestHandler handler = new TestHandler("test1=value1&test2=value2", "application/x-www-form-urlencoded", null, "PUT", 500, "Testing 123");
    startServer(handler);

    Map<String, String> parameters = new LinkedHashMap<>();
    parameters.put("test1", "value1");
    parameters.put("test2", "value2");

    ClientResponse<String, String> response = new RESTClient<>(String.class, String.class)
        .url("http://localhost:7000/test")
        .bodyHandler(new FormDataBodyHandler(parameters))
        .errorResponseHandler(new TextResponseHandler())
        .successResponseHandler(new TextResponseHandler())
        .put()
        .go();

    assertEquals(handler.count, 1);
    assertSame(response.request, parameters);
    assertEquals(response.url, new URL("http://localhost:7000/test"));
    assertEquals(response.method, HTTPMethod.PUT);
    assertEquals(response.status, 500);
    assertEquals(response.errorResponse, "Testing 123");
  }

  @Test
  public void put_formData_string() throws Exception {
    TestHandler handler = new TestHandler("test1=value1&test2=value2", "application/x-www-form-urlencoded", null, "PUT", 200, "Testing 123");
    startServer(handler);

    Map<String, String> parameters = new LinkedHashMap<>();
    parameters.put("test1", "value1");
    parameters.put("test2", "value2");

    ClientResponse<String, String> response = new RESTClient<>(String.class, String.class)
        .url("http://localhost:7000/test")
        .bodyHandler(new FormDataBodyHandler(parameters))
        .errorResponseHandler(new TextResponseHandler())
        .successResponseHandler(new TextResponseHandler())
        .put()
        .go();

    assertEquals(handler.count, 1);
    assertSame(response.request, parameters);
    assertEquals(response.url, new URL("http://localhost:7000/test"));
    assertEquals(response.method, HTTPMethod.PUT);
    assertEquals(response.status, 200);
    assertEquals(response.successResponse, "Testing 123");
  }

  private <T, U> ClientResponse<T, U> expectException(Supplier<ClientResponse<T, U>> supplier, Class<? extends Throwable> expected) {
    try {
      return supplier.get();
    } catch (Throwable e) {
      if (!e.getClass().equals(expected)) {
        fail("Expected exception [" + expected + "], but caught [" + e.getClass() + "].", e);
      }
      return null;
    }
  }

  private void startServer(TestHandler testHandler) throws Exception {
    InetSocketAddress addr = new InetSocketAddress(7000);
    server = HttpServer.create(addr, 0);
    server.createContext("/", testHandler);
    server.start();
  }

  private static class TestHandler implements HttpHandler {
    private final String contentType;

    private final String method;

    private final String request;

    private final Map<String, String> requestHeaders;

    private final String response;

    private final int responseCode;

    public int count;

    public TestHandler(String request, String contentType, Map<String, String> requestHeaders, String method, int responseCode, String response) {
      this.request = request;
      this.contentType = contentType;
      this.requestHeaders = requestHeaders;
      this.method = method;
      this.responseCode = responseCode;
      this.response = response;
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
      if (contentType != null) {
        assertEquals(httpExchange.getRequestHeaders().get("Content-Type").get(0), contentType);
      } else {
        assertNull(httpExchange.getRequestHeaders().get("Content-Type"));
      }

      if (requestHeaders != null) {
        for (Map.Entry<String, String> entry : requestHeaders.entrySet()) {
          if (entry.getValue() == null) {
            assertFalse(httpExchange.getRequestHeaders().containsKey(entry.getKey()));
          } else {
            assertEquals(httpExchange.getRequestHeaders().get(entry.getKey()).get(0), entry.getValue());
          }
        }
      }

      String expectedMethod = method.equalsIgnoreCase("PATCH") ? "POST" : method;
      assertEquals(httpExchange.getRequestMethod(), expectedMethod);

      // Read the request and save it
      StringBuilder body = new StringBuilder();
      try (BufferedReader reader = new BufferedReader(new InputStreamReader(httpExchange.getRequestBody(), StandardCharsets.UTF_8))) {
        String line;
        while ((line = reader.readLine()) != null) {
          body.append(line);
        }
      }

      if (request != null) {
        assertEquals(body.toString(), request);
      } else {
        assertTrue(body.toString().isEmpty(), "Body is [" + body.toString() + "]");
      }

      httpExchange.sendResponseHeaders(responseCode, response != null ? response.length() : 0);

      if (!method.equals("HEAD")) {
        if (response != null) {
          httpExchange.getResponseBody().write(response.getBytes());
          httpExchange.getResponseBody().flush();
        }
        httpExchange.getResponseBody().close();
      }
      count++;
    }
  }
}
