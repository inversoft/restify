/*
 * Copyright (c) 2016, Inversoft Inc., All Rights Reserved
 */
package com.inversoft.rest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Brian Pontarelli
 */
public class TextResponseHandler implements RESTClient.ResponseHandler<String> {
  @Override
  public String apply(InputStream is) throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    byte[] buf = new byte[1024];
    int read;
    while ((read = is.read(buf)) != -1) {
      baos.write(buf, 0, read);
    }

    return baos.toString("UTF-8");
  }
}
