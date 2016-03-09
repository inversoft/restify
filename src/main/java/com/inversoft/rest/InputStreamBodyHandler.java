/*
 * Copyright (c) 2016, Inversoft Inc., All Rights Reserved
 */
package com.inversoft.rest;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;

/**
 * @author Brian Pontarelli
 */
public class InputStreamBodyHandler implements RESTClient.BodyHandler {
  public String contentType;

  public Long length;

  public InputStream request;

  public InputStreamBodyHandler(String contentType, InputStream request) {
    this.contentType = contentType;
    this.request = request;
  }

  public InputStreamBodyHandler(String contentType, Long length, InputStream request) {
    this.contentType = contentType;
    this.length = length;
    this.request = request;
  }

  @Override
  public void accept(OutputStream os) throws IOException {
    if (request != null) {
      byte[] buf = new byte[1024];
      int read;
      while ((read = request.read(buf)) != -1) {
        os.write(buf, 0, read);
      }

      os.flush();
    }
  }

  @Override
  public void setHeaders(HttpURLConnection huc) {
    if (contentType != null) {
      huc.addRequestProperty("Content-Type", contentType);
    }

    if (length != null) {
      huc.addRequestProperty("Content-Length", "" + length);
    }
  }
}
