/*
 * Copyright (c) 2016, Inversoft Inc., All Rights Reserved
 */
package com.inversoft.rest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Response handler that reads the entire body and converts it to a UTF-8 String.
 *
 * @author Brian Pontarelli
 */
public class ByteArrayResponseHandler implements RESTClient.ResponseHandler<byte[]> {
  @Override
  public byte[] apply(InputStream is) throws IOException {
    if (is == null) {
      return null;
    }

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    byte[] buf = new byte[1024];
    int read;
    while ((read = is.read(buf)) != -1) {
      baos.write(buf, 0, read);
    }

    return baos.toByteArray();
  }
}
