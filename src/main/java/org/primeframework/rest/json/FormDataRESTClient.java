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
package org.primeframework.rest.json;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.primeframework.json.JacksonModule;
import org.primeframework.rest.BaseRESTClient;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

/**
 * RESTful WebService call builder. This provides the ability to call RESTful WebServices using a builder pattern to
 * set up all the necessary request information and parse the response.
 *
 * @author Brian Pontarelli
 */
public class FormDataRESTClient<RS, ERS> extends BaseRESTClient<FormDataRESTClient<RS, ERS>, RS, ERS> {
  public final static ObjectMapper objectMapper = new ObjectMapper().setSerializationInclusion(Include.NON_NULL)
      .configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true)
      .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
      .configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true)
      .configure(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS, false)
      .configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false)
      .registerModule(new JacksonModule());

  public Class<ERS> errorType;

  public Map<String, String> request;

  public Class<RS> successType;

  public FormDataRESTClient(Class<RS> successType, Class<ERS> errorType) {
    this.successType = successType;
    this.errorType = errorType;
  }

  public FormDataRESTClient<RS, ERS> request(Map<String, String> request) {
    this.request = request;
    return this;
  }

  @Override
  protected String contentType() {
    return "application/x-www-form-urlencoded";
  }

  /**
   * Return true if a response body is expected and the client should attempt to read from the error stream. Providing
   * an Error Response Type of {@link Void#TYPE} indicates no response is expected.
   *
   * @return
   */
  @Override
  protected boolean handleErrorResponse() {
    if (errorType == Void.class) {
      throw new IllegalArgumentException("Void.class isn't valid. Use Void.TYPE instead.");
    }
    return errorType != Void.TYPE;
  }

  /**
   * Return true if a response body is expected and the client should attempt to read from the input stream. Providing
   * an Success Response Type of {@link Void#TYPE} indicates no response is expected.
   *
   * @return
   */
  @Override
  protected boolean handleSuccessResponse() {
    if (successType == Void.class) {
      throw new IllegalArgumentException("Void.class isn't valid. Use Void.TYPE instead.");
    }
    return successType != Void.TYPE;
  }

  @Override
  protected byte[] makeBody() {
    if (request != null) {
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
        return build.toString().getBytes("UTF-8");
      } catch (UnsupportedEncodingException e) {
        throw new IllegalStateException(e);
      }
    }

    return null;
  }

  @Override
  protected ERS parseErrorResponse(byte[] responseBody) throws Exception {
    try {
      return objectMapper.readValue(responseBody, errorType);
    } catch (IOException e) {
      throw new JSONException(e);
    }
  }

  @Override
  protected RS parseSuccessResponse(byte[] responseBody) throws Exception {
    try {
      return objectMapper.readValue(responseBody, successType);
    } catch (IOException e) {
      throw new JSONException(e);
    }
  }
}
