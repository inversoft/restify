/*
 * Copyright (c) 2015, Inversoft Inc., All Rights Reserved
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Response information from a call to a REST API.
 *
 * @author Brian Pontarelli
 */
public class ClientResponse<T, U> {
  public final Map<String, List<String>> headers = new HashMap<>();

  public ZonedDateTime date;

  public U errorResponse;

  public Exception exception;

  public ZonedDateTime lastModified;

  public RESTClient.HTTPMethod method;

  public Object request;

  public int status;

  public T successResponse;

  public URL url;

  public void setHeaders(Map<String, List<String>> headers) {
    headers.forEach((key, values) -> this.headers.put(key != null ? key.toLowerCase() : null, values));

    date = parseDateHeader("date");
    lastModified = parseDateHeader("last-modified");
  }

  public boolean wasSuccessful() {
    return status >= 200 && status <= 299 && exception == null;
  }

  private ZonedDateTime parseDateHeader(String name) {
    List<String> values = headers.get(name);
    if (values != null && values.size() > 0) {
      try {
        return ZonedDateTime.parse(values.get(0), DateTools.RFC_5322_DATE_TIME);
      } catch (Exception e) {
        // Ignore this exception so that we aren't depending on a valid web server
      }
    }

    return null;
  }
}
