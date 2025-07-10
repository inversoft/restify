/*
 * Copyright (c) 2016-2025, Inversoft Inc., All Rights Reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */
package com.inversoft.rest;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;

import com.inversoft.http.Cookie;
import com.inversoft.http.HTTPStrings;
import com.inversoft.net.ssl.SSLTools;

/**
 * RESTful WebService call builder. This provides the ability to call RESTful WebServices using a builder pattern to
 * set up all the necessary request information and parse the response.
 *
 * @author Brian Pontarelli
 */
public class RESTClient<RS, ERS> {
  private final List<Cookie> cookies = new ArrayList<>();

  private final Class<ERS> errorType;

  private final Map<String, List<String>> headers = new HashMap<>();

  private final Map<String, List<String>> parameters = new LinkedHashMap<>();

  private final Class<RS> successType;

  private final StringBuilder url = new StringBuilder();

  private BodyHandler bodyHandler;

  private String certificate;

  private int connectTimeout = 2000;

  private ResponseHandler<ERS> errorResponseHandler;

  private boolean followRedirects = true;

  private String key;

  private String method;

  private ProxyInfo proxyInfo;

  private int readTimeout = 2000;

  private boolean sniVerificationDisabled;

  private ResponseHandler<RS> successResponseHandler;

  private String userAgent = "Restify (https://github.com/inversoft/restify)";

  // Under no circumstances should a POST request be retried due to an exception.
  // https://bugs.java.com/bugdatabase/view_bug.do?bug_id=6382788
  static {
    System.setProperty("sun.net.http.retryPost", "false");
  }

  public RESTClient(Class<RS> successType, Class<ERS> errorType) {
    if (successType == Void.class || errorType == Void.class) {
      throw new IllegalArgumentException("Void.class isn't valid. Use Void.TYPE instead.");
    }

    this.successType = successType;
    this.errorType = errorType;
  }

  /**
   * Adds the given header to the list of headers. If another header with the same name already exists, this adds an additional value for
   * that header.
   *
   * @param name  The name of the header.
   * @param value The value of the header.
   * @return This.
   */
  public RESTClient<RS, ERS> addHeader(String name, String value) {
    if (name == null || value == null) {
      return this;
    }

    this.headers.computeIfAbsent(name, key -> new ArrayList<>()).add(value);

    return this;
  }

  /**
   * Adds the given headers to the list of headers. If the given Map contains any headers that already have been added, this adds the
   * additional values in the given Map.
   *
   * @param headers The map of headers.
   * @return This.
   */
  public RESTClient<RS, ERS> addHeaders(Map<String, List<String>> headers) {
    if (headers == null) {
      return this;
    }

    headers.forEach((key, values) -> values.forEach(value -> addHeader(key, value)));
    return this;
  }

  /**
   * Add a URL parameter as a key value pair. If another URL parameter with the same name already exists, this adds an additional values for
   * that URL parameter. The handling depends on the type of the value. See the comment for the value parameter for more information.
   *
   * @param name  The URL parameter name.
   * @param value The URL parameter value. The <code>.toString()</code> method will be used to get the <code>String</code> used in the URL
   *              parameter. If the object type is a {@link Collection} a key value pair will be added for each value in the collection.
   *              {@link ZonedDateTime} will also be handled uniquely in that the <code>long</code> will be used to set in the request using
   *              <code>ZonedDateTime.toInstant().toEpochMilli()</code>
   * @return This.
   */
  public RESTClient<RS, ERS> addURLParameter(String name, Object value) {
    if (value instanceof ZonedDateTime) {
      addURLParameter(name, Long.toString(((ZonedDateTime) value).toInstant().toEpochMilli()));
    } else if (value instanceof Collection) {
      //noinspection rawtypes
      for (Object o : (Collection) value) {
        if (o != null) {
          addURLParameter(name, o.toString());
        }
      }
    } else if (value != null) {
      addURLParameter(name, value.toString());
    }

    return this;
  }

  /**
   * Add a URL parameter as a key value pair. If another URL parameter with the same name already exists, this adds an additional values for
   * * that URL parameter.
   *
   * @param name  The URL parameter name.
   * @param value The URL parameter value.
   * @return This.
   */
  public RESTClient<RS, ERS> addURLParameter(String name, String value) {
    if (name == null || value == null) {
      return this;
    }

    this.parameters.computeIfAbsent(name, k -> new ArrayList<>()).add(value);

    return this;
  }

  /**
   * Add URL parameters in the given map. If any URL parameters already exist, the values in the given map are appended to the existing
   * list.
   *
   * @param urlParameters The URL parameters <code>Map</code> to add.  For each item in the <code>Map</code> this will call
   *                      <code>addURLParameter(String, Object)</code>
   * @return This.
   */
  public RESTClient<RS, ERS> addURLParameterObjects(Map<String, Object> urlParameters) {
    if (urlParameters != null) {
      urlParameters.forEach(this::addURLParameter);
    }

    return this;
  }

  /**
   * Add URL parameters from map. If any URL parameters already exist, the values in the given map are appended to the existing
   * list.
   *
   * @param urlParameters The URL parameters <code>Map</code> to add.  For each item in the <code>Map</code>
   *                      this will call <code>urlParameter(String, Object)</code>
   * @return This.
   */
  public RESTClient<RS, ERS> addURLParameters(Map<String, List<String>> urlParameters) {
    if (urlParameters != null) {
      urlParameters.forEach(this::addURLParameter);
    }

    return this;
  }

  public RESTClient<RS, ERS> authorization(String key) {
    if (key != null && !key.isEmpty()) {
      this.headers.put("Authorization", Collections.singletonList(key));
    } else {
      this.headers.remove("Authorization");
    }
    return this;
  }

  public RESTClient<RS, ERS> basicAuthorization(String username, String password) {
    if (username != null && password != null) {
      this.headers.put("Authorization", Collections.singletonList(base64Basic(username, password)));
    }
    return this;
  }

  public RESTClient<RS, ERS> bodyHandler(BodyHandler bodyHandler) {
    this.bodyHandler = bodyHandler;
    return this;
  }

  public RESTClient<RS, ERS> certificate(String certificate) {
    this.certificate = certificate;
    return this;
  }

  public RESTClient<RS, ERS> connectTimeout(int connectTimeout) {
    this.connectTimeout = connectTimeout;
    return this;
  }

  public RESTClient<RS, ERS> cookie(Cookie cookie) {
    this.cookies.add(cookie);
    return this;
  }

  public RESTClient<RS, ERS> cookies(Cookie... cookies) {
    this.cookies.addAll(Arrays.asList(cookies));
    return this;
  }

  public RESTClient<RS, ERS> cookies(List<Cookie> cookies) {
    this.cookies.addAll(cookies);
    return this;
  }

  public RESTClient<RS, ERS> delete() {
    this.method = HTTPMethod.DELETE.name();
    return this;
  }

  public RESTClient<RS, ERS> disableSNIVerification() {
    this.sniVerificationDisabled = true;
    return this;
  }

  public RESTClient<RS, ERS> errorResponseHandler(ResponseHandler<ERS> errorResponseHandler) {
    this.errorResponseHandler = errorResponseHandler;
    return this;
  }

  public RESTClient<RS, ERS> followRedirects(boolean followRedirects) {
    this.followRedirects = followRedirects;
    return this;
  }

  public RESTClient<RS, ERS> get() {
    this.method = HTTPMethod.GET.name();
    return this;
  }

  public URI getURI() {
    return URI.create(url.toString());
  }

  public ClientResponse<RS, ERS> go() {
    if (url.length() == 0) {
      throw new IllegalStateException("You must specify a URL");
    }

    Objects.requireNonNull(method, "You must specify a HTTP method");

    if (successType != Void.TYPE && successResponseHandler == null) {
      throw new IllegalStateException("You specified a success response type, you must then provide a success response handler.");
    }

    if (errorType != Void.TYPE && errorResponseHandler == null) {
      throw new IllegalStateException("You specified an error response type, you must then provide an error response handler.");
    }

    ClientResponse<RS, ERS> response = new ClientResponse<>();
    response.request = (bodyHandler != null) ? bodyHandler.getBodyObject() : null;
    response.method = method;

    HttpURLConnection huc;
    try {
      if (parameters.size() > 0) {
        if (url.indexOf("?") == -1) {
          url.append("?");
        }

        for (Iterator<Entry<String, List<String>>> i = parameters.entrySet().iterator(); i.hasNext(); ) {
          Entry<String, List<String>> entry = i.next();

          for (Iterator<String> j = entry.getValue().iterator(); j.hasNext(); ) {
            String value = j.next();
            url.append(URLEncoder.encode(entry.getKey(), "UTF-8")).append("=").append(URLEncoder.encode(value, "UTF-8"));
            if (j.hasNext()) {
              url.append("&");
            }
          }

          if (i.hasNext()) {
            url.append("&");
          }
        }
      }

      response.url = new URL(url.toString());

      Proxy proxy = Proxy.NO_PROXY;
      if (proxyInfo != null) {
        if (proxyInfo.host != null && proxyInfo.port != -1) {
          proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyInfo.host, proxyInfo.port));
        }

        if (proxyInfo.username != null && proxyInfo.password != null) {
          headers.put("Proxy-Authorization", Collections.singletonList(base64Basic(proxyInfo.username, proxyInfo.password)));
        }
      }

      huc = (HttpURLConnection) response.url.openConnection(proxy);
      if (response.url.getProtocol().equalsIgnoreCase("https")) {
        HttpsURLConnection hsuc = (HttpsURLConnection) huc;
        if (certificate != null) {
          if (key != null) {
            hsuc.setSSLSocketFactory(SSLTools.getSSLServerContext(certificate, key).getSocketFactory());
          } else {
            hsuc.setSSLSocketFactory(SSLTools.getSSLSocketFactory(certificate));
          }
        }

        if (sniVerificationDisabled) {
          hsuc.setHostnameVerifier((hostname, session) -> true);
        }
      }

      huc.setInstanceFollowRedirects(followRedirects);
      huc.setDoOutput(bodyHandler != null);
      huc.setConnectTimeout(connectTimeout);
      huc.setReadTimeout(readTimeout);
      huc.setRequestMethod(method);

      if (headers.keySet().stream().noneMatch(name -> name.equalsIgnoreCase(HTTPStrings.Headers.UserAgent))) {
        headers.put(HTTPStrings.Headers.UserAgent, Collections.singletonList(userAgent));
      }

      headers.forEach((name, values) -> values.forEach(value -> huc.addRequestProperty(name, value)));

      if (headers.keySet().stream().noneMatch(name -> name.equalsIgnoreCase(HTTPStrings.Headers.Cookie)) && cookies.size() > 0) {
        String header = cookies.stream()
                               .map(Cookie::toRequestHeader)
                               .collect(Collectors.joining("; "));
        huc.addRequestProperty(HTTPStrings.Headers.Cookie, header);
      }

      if (bodyHandler != null) {
        bodyHandler.setHeaders(huc);
      }

      huc.connect();

      if (bodyHandler != null) {
        try (OutputStream os = huc.getOutputStream()) {
          bodyHandler.accept(os);
          os.flush();
        }
      }
    } catch (Exception e) {
      response.status = -1;
      response.exception = e;
      return response;
    }

    int status;
    try {
      status = huc.getResponseCode();
    } catch (Exception e) {
      response.status = -1;
      response.exception = e;
      return response;
    }

    response.setHeaders(huc.getHeaderFields());
    response.status = status;

    if (status < 200 || status > 299) {
      if (errorResponseHandler == null) {
        return response;
      }

      try (InputStream is = huc.getErrorStream()) {
        response.errorResponse = errorResponseHandler.apply(is);
      } catch (Exception e) {
        response.exception = e;
        return response;
      }
    } else {
      if (successResponseHandler == null || method.equalsIgnoreCase(HTTPMethod.HEAD.name())) {
        return response;
      }

      try (InputStream is = huc.getInputStream()) {
        response.successResponse = successResponseHandler.apply(is);
      } catch (Exception e) {
        response.exception = e;
        return response;
      }
    }

    return response;
  }

  public RESTClient<RS, ERS> head() {
    this.method = HTTPMethod.HEAD.name();
    return this;
  }

  /**
   * Synonym for {@link #addHeader(String, String)}.
   */
  public RESTClient<RS, ERS> header(String name, String value) {
    return addHeader(name, value);
  }

  /**
   * Synonym for {@link #addHeaders(Map)}.
   */
  public RESTClient<RS, ERS> headers(Map<String, List<String>> headers) {
    return addHeaders(headers);
  }

  public RESTClient<RS, ERS> key(String key) {
    this.key = key;
    return this;
  }

  public RESTClient<RS, ERS> method(String method) {
    try {
      // Set the override for PATCH
      if (method.equals(HTTPMethod.PATCH.name())) {
        this.method = HTTPMethod.POST.name();
        this.headers.put("X-HTTP-Method-Override", Collections.singletonList(method));
      } else {
        this.method = HTTPMethod.valueOf(method).name();
      }
    } catch (Exception e) {
      this.method = HTTPMethod.POST.name();
      this.headers.put("X-HTTP-Method-Override", Collections.singletonList(method));
    }

    return this;
  }

  public RESTClient<RS, ERS> method(HTTPMethod method) {
    return method(method.name());
  }

  public Map<String, List<String>> parameters() {
    return parameters;
  }

  public RESTClient<RS, ERS> patch() {
    this.method = HTTPMethod.POST.name();
    this.headers.put("X-HTTP-Method-Override", Collections.singletonList("PATCH"));
    return this;
  }

  public RESTClient<RS, ERS> post() {
    this.method = HTTPMethod.POST.name();
    return this;
  }

  public RESTClient<RS, ERS> proxy(ProxyInfo proxyInfo) {
    this.proxyInfo = proxyInfo;
    return this;
  }

  public RESTClient<RS, ERS> put() {
    this.method = HTTPMethod.PUT.name();
    return this;
  }

  public RESTClient<RS, ERS> readTimeout(int readTimeout) {
    this.readTimeout = readTimeout;
    return this;
  }

  /**
   * Replaces all the headers with the given Map.
   *
   * @param headers The new headers map.
   * @return This.
   */
  public RESTClient<RS, ERS> replaceHeaders(Map<String, List<String>> headers) {
    this.headers.clear();
    setHeaders(headers);

    return this;
  }

  /**
   * Replaces all the URL parameters with the given Map.
   *
   * @param urlParameters The url parameters <code>Map</code> to add.
   * @return This.
   */
  public RESTClient<RS, ERS> replaceURLParameters(Map<String, List<String>> urlParameters) {
    this.parameters.clear();
    setURLParameters(urlParameters);

    return this;
  }

  /**
   * Replaces the given header. If another header with the same name already exists, it is replaced with the single value given.
   *
   * @param name  The name of the header.
   * @param value The value of the header.
   * @return This.
   */
  public RESTClient<RS, ERS> setHeader(String name, String value) {
    if (name == null) {
      return this;
    }

    if (value == null) {
      headers.remove(name);
    } else {
      List<String> list = new ArrayList<>();
      list.add(value);
      this.headers.put(name, list);
    }

    return this;
  }

  /**
   * Replaces the given header with the list of values. If another header with the same name already exists, all the existing values are
   * replaced with the values given.
   *
   * @param name   The name of the header.
   * @param values The list of new values of the header.
   * @return This.
   */
  public RESTClient<RS, ERS> setHeaders(String name, List<String> values) {
    if (values == null) {
      headers.remove(name);
    } else {
      this.headers.put(name, new ArrayList<>(values.stream().filter(Objects::nonNull).collect(Collectors.toList())));
    }

    return this;
  }

  /**
   * Replaces the given header with Map of values. If another header with the same name as a key in the Map already exists, all the
   * existing values for that key are replaced with the values given.
   *
   * @param headers The new headers map.
   * @return This.
   */
  public RESTClient<RS, ERS> setHeaders(Map<String, List<String>> headers) {
    if (headers != null) {
      for (Entry<String, List<String>> e : headers.entrySet()) {
        if (e.getKey() != null && e.getValue() != null && e.getValue().stream().anyMatch(Objects::nonNull)) {
          this.headers.put(e.getKey(), e.getValue().stream().filter(Objects::nonNull).collect(Collectors.toList()));
        }
      }
    }

    return this;
  }

  /**
   * Replaces a URL parameter as a key value pair. If a URL parameter with the same name exists, it is replaced.
   *
   * @param name  The URL parameter name.
   * @param value The url parameter value.
   * @return This.
   */
  public RESTClient<RS, ERS> setURLParameter(String name, String value) {
    if (name == null) {
      return this;
    }

    if (value == null) {
      parameters.remove(name);
    } else {
      List<String> list = new ArrayList<>();
      list.add(value);
      parameters.put(name, list);
    }

    return this;
  }

  /**
   * Replaces a URL parameter as a key value pair. If another URL parameter with the same name already exists, this replaces all the
   * existing values for that URL parameter. The handling depends on the type of the value. See the comment for the value parameter for more
   * information.
   *
   * @param name  The URL parameter name.
   * @param value The URL parameter value. The <code>.toString()</code> method will be used to get the <code>String</code> used in the URL
   *              parameter. If the object type is a {@link Collection} a key value pair will be added for each value in the collection.
   *              {@link ZonedDateTime} will also be handled uniquely in that the <code>long</code> will be used to set in the request using
   *              <code>ZonedDateTime.toInstant().toEpochMilli()</code>
   * @return This.
   */
  public RESTClient<RS, ERS> setURLParameter(String name, Object value) {
    if (value instanceof ZonedDateTime) {
      setURLParameter(name, Long.toString(((ZonedDateTime) value).toInstant().toEpochMilli()));
    } else if (value instanceof Collection) {
      //noinspection rawtypes
      for (Object o : (Collection) value) {
        if (o != null) {
          setURLParameter(name, o.toString());
        }
      }
    } else if (value != null) {
      setURLParameter(name, value.toString());
    }

    return this;
  }

  /**
   * Replaces URL parameters from a {@code Map<String, Object>}. If any URL parameters exist with the same name as those in the Map, they
   * are replaced with the values in the Map.
   *
   * @param urlParameters The url parameters <code>Map</code> to add.
   * @return This.
   */
  public RESTClient<RS, ERS> setURLParameterObjects(Map<String, Object> urlParameters) {
    if (urlParameters != null) {
      urlParameters.forEach(this::setURLParameter);
    }
    return this;
  }

  /**
   * Replaces the URL parameters from the Map. If any URL parameters exist with the same name as those in the Map, their values are replaced
   * completely.
   *
   * @param urlParameters The url parameters <code>Map</code> to add.
   * @return This.
   */
  public RESTClient<RS, ERS> setURLParameters(Map<String, List<String>> urlParameters) {
    if (urlParameters != null) {
      for (Entry<String, List<String>> e : urlParameters.entrySet()) {
        if (e.getKey() != null && e.getValue() != null && e.getValue().stream().anyMatch(Objects::nonNull)) {
          this.parameters.put(e.getKey(), e.getValue().stream().filter(Objects::nonNull).collect(Collectors.toList()));
        }
      }
    }

    return this;
  }

  public RESTClient<RS, ERS> successResponseHandler(ResponseHandler<RS> successResponseHandler) {
    this.successResponseHandler = successResponseHandler;
    return this;
  }

  public RESTClient<RS, ERS> uri(String uri) {
    if (url.length() == 0) {
      return this;
    }

    if (url.charAt(url.length() - 1) == '/' && uri.startsWith("/")) {
      url.append(uri.substring(1));
    } else if (url.charAt(url.length() - 1) != '/' && !uri.startsWith("/")) {
      url.append("/").append(uri);
    } else {
      url.append(uri);
    }

    return this;
  }

  public String url() {
    return url.toString();
  }

  public RESTClient<RS, ERS> url(String url) {
    this.url.delete(0, this.url.length());
    this.url.append(url);
    return this;
  }

  /**
   * Synonym for {@link #addURLParameter(String, Object)}.
   */
  public RESTClient<RS, ERS> urlParameter(String name, Object value) {
    return addURLParameter(name, value);
  }

  /**
   * Synonym for {@link #addURLParameter(String, String)}.
   */
  public RESTClient<RS, ERS> urlParameter(String name, String value) {
    return addURLParameter(name, value);
  }

  /**
   * Synonym for {@link #addURLParameterObjects(Map)}.
   */
  public RESTClient<RS, ERS> urlParameterObjects(Map<String, Object> urlParameters) {
    return addURLParameterObjects(urlParameters);
  }

  /**
   * Synonym for {@link #addURLParameters(Map)}.
   */
  public RESTClient<RS, ERS> urlParameters(Map<String, List<String>> urlParameters) {
    return addURLParameters(urlParameters);
  }

  /**
   * Append a url path segment.
   * <p>
   * For Example: <pre>
   *     .url("http://www.foo.com")
   *     .urlSegment("bar")
   *   </pre>
   * This will result in a url of <code>http://www.foo.com/bar</code>
   *
   * @param value The url path segment. A null value will be ignored.
   * @return This.
   */
  public RESTClient<RS, ERS> urlSegment(Object value) {
    if (value != null) {
      if (url.charAt(url.length() - 1) != '/') {
        url.append('/');
      }
      url.append(value);
    }
    return this;
  }

  public RESTClient<RS, ERS> userAgent(String userAgent) {
    this.userAgent = userAgent;
    return this;
  }

  private String base64Basic(String username, String password) {
    String credentials = username + ":" + password;
    Base64.Encoder encoder = Base64.getEncoder();
    return "Basic " + encoder.encodeToString(credentials.getBytes());
  }

  /**
   * Standard HTTP methods.
   */
  public enum HTTPMethod {
    CONNECT,
    DELETE,
    GET,
    HEAD,
    OPTIONS,
    PATCH,
    POST,
    PUT,
    TRACE
  }

  /**
   * Body handler that manages sending the bytes of the HTTP request body to the HttpURLConnection. This also is able to
   * manage any HTTP headers that are associated with the body such as Content-Type and Content-Length.
   */
  public interface BodyHandler {
    /**
     * Accepts the OutputStream and writes the bytes of the HTTP request body to it.
     *
     * @param os The OutputStream to write the body to.
     * @throws IOException If the write failed.
     */
    void accept(OutputStream os) throws IOException;

    /**
     * Returns the processed body. This may be used if there is use externally to get the body length or to generate a body signature. This
     * may or may not be called, so any serialization must be done before returning from {@link #setHeaders(HttpURLConnection)} or this
     * method. The request processing to build the body should only be performed once.
     *
     * @return a byte array representing the body to be written to the output stream.
     */
    byte[] getBody();

    /**
     * @return The unprocessed body object. This might be a JSON object, a Map of key value pairs or a String. By default, this returns
     * null.
     */
    default Object getBodyObject() {
      return null;
    }

    /**
     * Sets any headers for the HTTP body that will be written.
     *
     * @param huc The HttpURLConnection to set headers into.
     */
    void setHeaders(HttpURLConnection huc);
  }

  /**
   * Handles responses from the HTTP server.
   *
   * @param <T> The type that is returned from the handler.
   */
  public interface ResponseHandler<T> {
    /**
     * Handles the InputStream that is the HTTP response and reads it in and converts it to a value.
     *
     * @param is The InputStream to read from.
     * @return The value.
     *
     * @throws IOException If the read failed.
     */
    T apply(InputStream is) throws IOException;
  }
}
