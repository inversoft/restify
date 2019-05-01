/*
 * Copyright (c) 2016-2019, Inversoft Inc., All Rights Reserved
 */
package com.inversoft.rest;

/**
 * @author Brian Pontarelli
 */
public class JSONException extends RuntimeException {
  public JSONException() {
    super();
  }

  public JSONException(Throwable cause) {
    super(cause);
  }

  public JSONException(String message, Throwable cause) {
    super(message, cause);
  }
}
