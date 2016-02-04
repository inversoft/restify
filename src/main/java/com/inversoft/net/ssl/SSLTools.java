/*
 * Copyright (c) 2014, Inversoft Inc., All Rights Reserved
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
package com.inversoft.net.ssl;

import javax.net.ssl.*;
import javax.xml.bind.DatatypeConverter;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;

/**
 * Useful for setting up SSL on the client side to trust a supplied certificate string.
 * <p>
 * Useful for setting up SSL on the server side to use a key and certificate based on strings.
 * <p>
 * Also, a utility method that assist in determining if a certificate is valid.
 *
 * @author Seth Musselman
 */
public class SSLTools {
  public static final String CERT_END = "-----END CERTIFICATE";

  public static final String CERT_START = "BEGIN CERTIFICATE-----";

  public static final String P8_KEY_END = "-----END PRIVATE KEY";

  public static final String P8_KEY_START = "BEGIN PRIVATE KEY-----";

  // Disable SNI so that it doesn't mess up our use of JSSE with some certificates
  static {
    System.setProperty("jsse.enableSNIExtension", "false");
  }

  /**
   * This creates an in-memory keystore containing the certificate and private key and initializes the SSLContext with
   * the key material it contains.
   * <p>
   * For using with an HttpsServer: {@code SSLContext sslContext = getSSLServerContext(...); HttpsServer server =
   * HttpsServer.create(); server.setHttpsConfigurator (new HttpsConfigurator(sslContext));}
   *
   * @param certificateString a PEM formatted Certificate
   * @param keyString         a PKCS8 PEM formatted Private Key
   * @return a SSLContext configured with the Certificate and Private Key
   */
  public static SSLContext getSSLServerContext(String certificateString, String keyString) throws CertificateException, KeyStoreException, IOException, NoSuchAlgorithmException, UnrecoverableKeyException, KeyManagementException, InvalidKeySpecException {
    byte[] certBytes = parseDERFromPEM(certificateString, CERT_START, CERT_END);
    byte[] keyBytes = parseDERFromPEM(keyString, P8_KEY_START, P8_KEY_END);

    X509Certificate cert = generateCertificateFromDER(certBytes);
    PrivateKey key = generatePrivateKeyFromDER(keyBytes);
    KeyStore keystore = KeyStore.getInstance("JKS");
    keystore.load(null);
    keystore.setCertificateEntry("cert-alias", cert);
    keystore.setKeyEntry("key-alias", key, "changeit".toCharArray(), new Certificate[]{cert});

    KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
    kmf.init(keystore, "changeit".toCharArray());

    KeyManager[] km = kmf.getKeyManagers();

    SSLContext context = SSLContext.getInstance("TLS");
    context.init(km, null, null);

    return context;
  }

  /**
   * This creates an in-memory keystore containing the certificate and initializes the SSLContext with the the trust
   * material it contains.
   * <p>
   * With HttpsURLConnection con, set the connection to use the SSLSocketFactory before {@code con.connect()}: {@code
   * con.setSSLSocketFactory(sslSocketFactory)}
   *
   * @param certificateString the PEM formatted Certificate
   * @return a SSLSocketFactory that can be hooked into an HttpsURLConnection via setSSLSocketFactory
   */
  public static SSLSocketFactory getSSLSocketFactory(String certificateString) throws CertificateException,
      KeyStoreException, IOException, NoSuchAlgorithmException, UnrecoverableKeyException, KeyManagementException {
    byte[] certBytes = parseDERFromPEM(certificateString, CERT_START, CERT_END);

    X509Certificate cert = generateCertificateFromDER(certBytes);
    KeyStore keystore = KeyStore.getInstance("JKS");
    keystore.load(null);
    keystore.setCertificateEntry("cert-alias", cert);

    TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
    tmf.init(keystore);
    TrustManager[] tm = tmf.getTrustManagers();
    SSLContext context = SSLContext.getInstance("TLS");

    context.init(null, tm, null);

    return context.getSocketFactory();
  }

  /**
   * Checks the given certificate String to ensure it is a PEM formatted certificate.
   *
   * @param certificateString The certificate String.
   * @return True if the certificate String is valid, false if it isn't (this happens when an exception is thrown
   * attempting to parse the PEM format).
   */
  public static boolean validCertificateString(String certificateString) {
    try {
      byte[] certBytes = parseDERFromPEM(certificateString, CERT_START, CERT_END);
      generateCertificateFromDER(certBytes);
    } catch (Exception e) {
      return false;
    }
    return true;
  }

  private static X509Certificate generateCertificateFromDER(byte[] certBytes) throws CertificateException {
    CertificateFactory factory = CertificateFactory.getInstance("X.509");
    return (X509Certificate) factory.generateCertificate(new ByteArrayInputStream(certBytes));
  }

  private static RSAPrivateKey generatePrivateKeyFromDER(byte[] keyBytes) throws InvalidKeySpecException, NoSuchAlgorithmException {
    PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
    KeyFactory factory = KeyFactory.getInstance("RSA");
    return (RSAPrivateKey) factory.generatePrivate(spec);
  }

  private static byte[] parseDERFromPEM(String pem, String beginDelimiter, String endDelimiter) {
    int startIndex = pem.indexOf(beginDelimiter);
    if (startIndex < 0) {
      throw new IllegalArgumentException("Invalid PEM format");
    }

    int endIndex = pem.indexOf(endDelimiter);
    if (endIndex < 0) {
      throw new IllegalArgumentException("Invalid PEM format");
    }

    String base64 = pem.substring(startIndex + beginDelimiter.length(), endIndex);
    return DatatypeConverter.parseBase64Binary(base64);
  }
}