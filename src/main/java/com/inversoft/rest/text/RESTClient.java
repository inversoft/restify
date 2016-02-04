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
package com.inversoft.rest.text;

import com.inversoft.rest.BaseRESTClient;

/**
 * RESTful WebService call builder. This provides the ability to call RESTful WebServices using a builder pattern to
 * setup all the necessary request information and parse the response.
 *
 * @author Brian Pontarelli
 */
public class RESTClient extends BaseRESTClient<RESTClient, String, String> {
  public String encoding = "UTF-8";

  public RESTClient withEncoding(String encoding) {
    this.encoding = encoding;
    return this;
  }

  @Override
  protected String contentType() {
    return "text/plain";
  }

  @Override
  protected boolean handleErrorResponse() {
    return true;
  }

  @Override
  protected boolean handleSuccessResponse() {
    return true;
  }

  @Override
  protected byte[] makeBody() {
    return null;
  }

  @Override
  protected String parseErrorResponse(byte[] responseBody) throws Exception {
    return new String(responseBody, "UTF-8");
  }

  @Override
  protected String parseSuccessResponse(byte[] responseBody) throws Exception {
    return new String(responseBody, "UTF-8");
  }
}
