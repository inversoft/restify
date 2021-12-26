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
  public byte[] getBody() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Object getBodyObject() {
    return request;
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
