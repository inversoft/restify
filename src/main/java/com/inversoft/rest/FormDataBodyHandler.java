/*
 * Copyright (c) 2016-2021, Inversoft Inc., All Rights Reserved
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

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * @author Brian Pontarelli
 */
public class FormDataBodyHandler implements RESTClient.BodyHandler {
  private final Map<String, List<String>> request;

  private byte[] body;

  public FormDataBodyHandler(Map<String, List<String>> request) {
    this.request = request;
  }

  @Override
  public void accept(OutputStream os) throws IOException {
    if (body != null && os != null) {
      os.write(body);
    }
  }

  @Override
  public byte[] getBody() {
    if (request != null) {
      serializeRequest();
    }
    return body;
  }

  @Override
  public Object getBodyObject() {
    return request;
  }

  @Override
  public void setHeaders(HttpURLConnection huc) {
    if (request != null) {
      serializeRequest();
      huc.addRequestProperty("Content-Type", "application/x-www-form-urlencoded");
      huc.addRequestProperty("Content-Length", "" + body.length);
    }
  }

  private void serializeRequest() {
    if (body == null) {
      StringBuilder build = new StringBuilder();
      request.forEach((key, values) -> {
        for (String value : values) {
          if (build.length() > 0) {
            build.append("&");
          }

          try {
            build.append(URLEncoder.encode(key, "UTF-8")).append("=").append(URLEncoder.encode(value, "UTF-8"));
          } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
          }
        }
      });

      body = build.toString().getBytes(StandardCharsets.UTF_8);
    }
  }
}
