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
