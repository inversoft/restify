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
