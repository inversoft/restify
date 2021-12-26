/*
 * Copyright (c) 2015-2021, Inversoft Inc., All Rights Reserved
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

import java.net.URL;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.inversoft.http.Cookie;
import com.inversoft.http.HTTPStrings;

/**
 * Response information from a call to a REST API.
 *
 * @author Brian Pontarelli
 */
public class ClientResponse<T, U> {
  public final List<Cookie> cookies = new ArrayList<>();

  public final Map<String, List<String>> headers = new HashMap<>();

  public ZonedDateTime date;

  public U errorResponse;

  public Exception exception;

  public ZonedDateTime lastModified;

  public String method;

  public Object request;

  public int status;

  public T successResponse;

  public URL url;

  public List<Cookie> getCookies() {
    return cookies;
  }

  public ZonedDateTime getDate() {
    return date;
  }

  public U getErrorResponse() {
    return errorResponse;
  }

  public Exception getException() {
    return exception;
  }

  public String getHeader(String name) {
    List<String> values = headers.get(name.toLowerCase());
    if (values == null || values.isEmpty()) {
      return null;
    }

    return values.get(0);
  }

  public List<String> getHeaders(String name) {
    return headers.get(name.toLowerCase());
  }

  public ZonedDateTime getLastModified() {
    return lastModified;
  }

  public String getMethod() {
    return method;
  }

  public Object getRequest() {
    return request;
  }

  public int getStatus() {
    return status;
  }

  public T getSuccessResponse() {
    return successResponse;
  }

  public URL getUrl() {
    return url;
  }

  public void setHeaders(Map<String, List<String>> headers) {
    headers.forEach((key, values) -> {
      // Skip the Status line
      if (key == null) {
        return;
      }

      this.headers.put(key.toLowerCase(), values);
    });

    date = parseDateHeader(HTTPStrings.Headers.Date.toLowerCase());
    lastModified = parseDateHeader(HTTPStrings.Headers.LastModified.toLowerCase());

    // Parse the cookie headers
    List<String> cookies = this.headers.get(HTTPStrings.Headers.SetCookie.toLowerCase());
    if (cookies != null && cookies.size() > 0) {
      cookies.stream()
             .map(Cookie::fromResponseHeader)
             .filter(Objects::nonNull)
             .forEach(this.cookies::add);
    }
  }

  public boolean wasSuccessful() {
    return status >= 200 && status <= 299 && exception == null;
  }

  private ZonedDateTime parseDateHeader(String name) {
    List<String> values = headers.get(name);
    if (values != null && values.size() > 0) {
      return DateTools.parse(values.get(0));
    }

    return null;
  }
}
