/*
 * Copyright (c) 2016, Inversoft Inc., All Rights Reserved
 */
package com.inversoft.rest;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.util.Map;

/**
 * @author Brian Pontarelli
 */
public class FormDataBodyHandler implements RESTClient.BodyHandler {
  public Map<String, String> request;

  private byte[] body;

  public FormDataBodyHandler(Map<String, String> request) {
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
  public void setHeaders(HttpURLConnection huc) {
    if (request != null) {
      serializeRequest();
      huc.addRequestProperty("Content-Type", "application/x-www-form-urlencoded");
    }
  }

  private void serializeRequest() {
    if (body == null) {
      StringBuilder build = new StringBuilder();
      request.forEach((key, value) -> {
        if (build.length() > 0) {
          build.append("&");
        }

        try {
          build.append(URLEncoder.encode(key, "UTF-8")).append("=").append(URLEncoder.encode(value, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
          throw new IllegalStateException(e);
        }
      });

      try {
        body = build.toString().getBytes("UTF-8");
      } catch (UnsupportedEncodingException e) {
        throw new IllegalStateException(e);
      }
    }
  }
}
