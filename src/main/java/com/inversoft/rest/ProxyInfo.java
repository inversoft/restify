/*
 * Copyright (c) 2021, Inversoft Inc., All Rights Reserved
 */
package com.inversoft.rest;

/**
 * @author Brian Pontarelli
 */
public class ProxyInfo {
  public final String host;

  public final String password;

  public final int port;

  public final String username;

  public ProxyInfo(String host, int port) {
    this(host, port, null, null);
  }

  public ProxyInfo(String host, int port, String username, String password) {
    this.host = host;
    this.port = port;
    this.username = username;
    this.password = password;
  }
}