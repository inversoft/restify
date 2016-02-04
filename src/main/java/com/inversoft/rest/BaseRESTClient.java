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

import javax.net.ssl.HttpsURLConnection;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.inversoft.net.ssl.SSLTools;

/**
 * RESTful WebService call builder. This provides the ability to call RESTful WebServices using a builder pattern to
 * set up all the necessary request information and parse the response.
 *
 * @author Brian Pontarelli
 */
@SuppressWarnings("unchecked")
public abstract class BaseRESTClient<T extends BaseRESTClient<T, RS, ERS>, RS, ERS> {
  private static final Logger logger = LoggerFactory.getLogger(BaseRESTClient.class);

  public final Map<String, String> headers = new HashMap<>();

  public final Map<String, List<Object>> parameters = new LinkedHashMap<>();

  public final StringBuilder url = new StringBuilder();

  public String certificate;

  public int connectTimeout = 2000;

  public String key;

  public HTTPMethod method;

  public int readTimeout = 2000;

  protected BaseRESTClient() {
  }

  public T authorization(String key) {
    this.headers.put("Authorization", key);
    return (T) this;
  }

  public T basicAuthorization(String username, String password) {
    if (username != null && password != null) {
      String credentials = username + ":" + password;
      Base64.Encoder encoder = Base64.getEncoder();
      String encoded = encoder.encodeToString(credentials.getBytes());
      this.headers.put("Authorization", "Basic " + encoded);
    }
    return (T) this;
  }

  public T certificate(String certificate) {
    this.certificate = certificate;
    return (T) this;
  }

  public T connectTimeout(int connectTimeout) {
    this.connectTimeout = connectTimeout;
    return (T) this;
  }

  public T delete() {
    this.method = HTTPMethod.DELETE;
    return (T) this;
  }

  public T get() {
    this.method = HTTPMethod.GET;
    return (T) this;
  }

  public ClientResponse<RS, ERS> go() {
    if (url.length() == 0) {
      throw new IllegalStateException("You must specify a URL");
    }

    Objects.requireNonNull(method, "You must specify a HTTP method");

    ClientResponse<RS, ERS> response = new ClientResponse<>();
    HttpURLConnection huc;
    try {
      if (parameters.size() > 0) {
        if (url.indexOf("?") == -1) {
          url.append("?");
        }

        for (Iterator<Entry<String, List<Object>>> i = parameters.entrySet().iterator(); i.hasNext(); ) {
          Entry<String, List<Object>> entry = i.next();

          for (Iterator<Object> j = entry.getValue().iterator(); j.hasNext(); ) {
            Object value = j.next();
            url.append(URLEncoder.encode(entry.getKey(), "UTF-8")).append("=").append(URLEncoder.encode(value.toString(), "UTF-8"));
            if (j.hasNext()) {
              url.append("&");
            }
          }

          if (i.hasNext()) {
            url.append("&");
          }
        }
      }

      URL urlObject = new URL(url.toString());
      huc = (HttpURLConnection) urlObject.openConnection();
      if (urlObject.getProtocol().toLowerCase().equals("https") && certificate != null) {
        HttpsURLConnection hsuc = (HttpsURLConnection) huc;
        if (key != null) {
          hsuc.setSSLSocketFactory(SSLTools.getSSLServerContext(certificate, key).getSocketFactory());
        } else {
          hsuc.setSSLSocketFactory(SSLTools.getSSLSocketFactory(certificate));
        }
      }

      byte[] body = makeBody();
      huc.setDoOutput(body != null);
      huc.setConnectTimeout(connectTimeout);
      huc.setReadTimeout(readTimeout);
      huc.setRequestMethod(method.toString());

      if (headers.size() > 0) {
        headers.forEach(huc::addRequestProperty);
      }

      if (body != null) {
        huc.addRequestProperty("Content-Type", contentType());
        huc.addRequestProperty("Content-Length", "" + body.length);
      }

      huc.connect();

      if (body != null) {
        try (OutputStream os = huc.getOutputStream()) {
          os.write(body);
          os.flush();
        }
      }
    } catch (Exception e) {
      logger.debug("Error calling REST WebService at [" + url + "]", e);
      response.exception = e;
      return response;
    }

    int status;
    try {
      status = huc.getResponseCode();
    } catch (Exception e) {
      logger.debug("Error calling REST WebService at [" + url + "]", e);
      response.exception = e;
      return response;
    }

    response.status = status;

    if (status < 200 || status > 299) {
      if (!handleErrorResponse()) {
        return response;
      }

      try {
        byte[] responseBody = readResponseBody(huc.getErrorStream());
        if (responseBody != null && responseBody.length > 0) {
          response.errorResponse = parseErrorResponse(responseBody);
        }
      } catch (Exception e) {
        logger.debug("Error calling REST WebService at [" + url + "]", e);
        response.exception = e;
        return response;
      }
    } else {
      if (!handleSuccessResponse()) {
        return response;
      }

      try {
        byte[] responseBody = readResponseBody(huc.getInputStream());
        if (responseBody != null && responseBody.length > 0) {
          response.successResponse = parseSuccessResponse(responseBody);
        }
      } catch (Exception e) {
        logger.debug("Error calling REST WebService at [" + url + "]", e);
        response.exception = e;
        return response;
      }
    }

    return response;
  }

  public T header(String name, String value) {
    this.headers.put(name, value);
    return (T) this;
  }

  public T headers(Map<String, String> headers) {
    this.headers.putAll(headers);
    return (T) this;
  }

  public T key(String key) {
    this.key = key;
    return (T) this;
  }

  public T post() {
    this.method = HTTPMethod.POST;
    return (T) this;
  }

  public T put() {
    this.method = HTTPMethod.PUT;
    return (T) this;
  }

  public T readTimeout(int readTimeout) {
    this.readTimeout = readTimeout;
    return (T) this;
  }

  public T uri(String uri) {
    if (url.length() == 0) {
      return (T) this;
    }

    if (url.charAt(url.length() - 1) == '/' && uri.startsWith("/")) {
      url.append(uri.substring(1));
    } else if (url.charAt(url.length() - 1) != '/' && !uri.startsWith("/")) {
      url.append("/").append(uri);
    } else {
      url.append(uri);
    }

    return (T) this;
  }

  public T url(String url) {
    this.url.delete(0, this.url.length());
    this.url.append(url);
    return (T) this;
  }

  /**
   * Add a URL parameter as a key value pair.
   *
   * @param name  The URL parameter name.
   * @param value The url parameter value. The <code>.toString()</code> method will be used to
   *              get the <code>String</code> used in the URL parameter. If the object type is a
   *              {@link Collection} a key value pair will be added for each value in the collection.
   *              {@link ZonedDateTime} will also be handled uniquely in that the <code>long</code> will
   *              be used to set in the request using <code>ZonedDateTime.toInstant().toEpochMilli()</code>
   * @return This.
   */
  public T urlParameter(String name, Object value) {
    if (value == null) {
      return (T) this;
    }

    List<Object> values = this.parameters.get(name);
    if (values == null) {
      values = new ArrayList<>();
      this.parameters.put(name, values);
    }

    if (value instanceof ZonedDateTime) {
      values.add(((ZonedDateTime) value).toInstant().toEpochMilli());
    } else if (value instanceof Collection) {
      values.addAll((Collection) value);
    } else {
      values.add(value);
    }
    return (T) this;
  }

  /**
   * Append a url path segment. <p>
   * For Example: <pre>
   *     .url("http://www.foo.com")
   *     .urlSegment("bar")
   *   </pre>
   * This will result in a url of <code>http://www.foo.com/bar</code>
   *
   * @param value The url path segment. A null value will be ignored.
   * @return This.
   */
  public T urlSegment(Object value) {
    if (value != null) {
      if (url.charAt(url.length() - 1) != '/') {
        url.append('/');
      }
      url.append(value.toString());
    }
    return (T) this;
  }

  protected abstract String contentType();

  protected abstract boolean handleErrorResponse();

  protected abstract boolean handleSuccessResponse();

  protected abstract byte[] makeBody();

  protected abstract ERS parseErrorResponse(byte[] responseBody) throws Exception;

  protected abstract RS parseSuccessResponse(byte[] responseBody) throws Exception;

  private byte[] readResponseBody(InputStream stream) throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
    try (InputStream is = stream) {
      if (is != null) {
        byte[] buf = new byte[1024];
        int length;
        while ((length = is.read(buf)) != -1) {
          baos.write(buf, 0, length);
        }
      }
    }
    return baos.toByteArray();
  }

  public enum HTTPMethod {
    GET,
    POST,
    PUT,
    DELETE
  }
}
