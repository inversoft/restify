/*
 * Copyright (c) 2016-2019, Inversoft Inc., All Rights Reserved
 */
package com.inversoft.rest;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;

import com.inversoft.http.HTTPStrings;

/**
 * @author Brian Pontarelli
 */
public class ByteArrayBodyHandler implements RESTClient.BodyHandler {
  private final byte[] body;

  public ByteArrayBodyHandler(byte[] body) {
    this.body = body;
  }

  @Override
  public void accept(OutputStream os) throws IOException {
    if (body != null && os != null) {
      os.write(body);
    }
  }

  @Override
  public byte[] getBody() {
    return body;
  }

  @Override
  public void setHeaders(HttpURLConnection huc) {
    if (body != null) {
      huc.addRequestProperty(HTTPStrings.Headers.ContentLength, "" + body.length);
    }
  }
}
