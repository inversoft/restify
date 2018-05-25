/*
 * Copyright (c) 2018, Inversoft Inc., All Rights Reserved
 */
package com.inversoft.net.ssl;

import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;

/**
 * @author Daniel DeGroff
 */
public class UnsafeTrustManager implements X509TrustManager {
  @Override
  public void checkClientTrusted(X509Certificate[] x509Certificates, String s) {
  }

  @Override
  public void checkServerTrusted(X509Certificate[] x509Certificates, String string) {
  }

  @Override
  public X509Certificate[] getAcceptedIssuers() {
    return new X509Certificate[0];
  }
}
