/*
 * Copyright (c) 2014-2018, Inversoft Inc., All Rights Reserved
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

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPrivateCrtKeySpec;
import java.util.Base64;

import sun.security.util.DerInputStream;
import sun.security.util.DerValue;

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

  /**
   * RSA Private Key file (PKCS#1) End Tag
   */
  public static final String PKCS_1_KEY_END = "-----END RSA PRIVATE KEY";

  /**
   * RSA Private Key file (PKCS#1)  Start Tag
   */
  public static final String PKCS_1_KEY_START = "BEGIN RSA PRIVATE KEY-----";

  // Disable SNI so that it doesn't mess up our use of JSSE with some certificates
  static {
    System.setProperty("jsse.enableSNIExtension", "false");
  }

  /**
   * Disabling SSL validation is strongly discouraged. This is generally only intended for use during testing or perhaps
   * when used in a private network with a self signed certificate.
   *
   * <p>Even when using with a self signed certificate it is recommended that instead of disabling SSL validation you
   * instead add your self signed certificate to the Java keystore.</p>
   */
  public static void disableSSLValidation() {
    try {
      SSLContext context = SSLContext.getInstance("SSL");
      context.init(null, new TrustManager[]{new UnsafeTrustManager()}, null);
      HttpsURLConnection.setDefaultSSLSocketFactory(context.getSocketFactory());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Enable SSL Validation.
   */
  public static void enableSSLValidation() {
    try {
      SSLContext.getInstance("SSL").init(null, null, null);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
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
    PrivateKey key = generatePrivateKeyFromPKCS8DER(keyBytes);
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
   * Sign the provided string with the private key and the provided signature. The provided private key string is expected to be in one of
   * the two formats: PKCS#1 or PKCS#8.
   *
   * @param string    The string to sign.
   * @param keyString The private to use when signing the string.
   * @return the signed string.
   */
  public static String signWithRSA(String string, String keyString) throws GeneralSecurityException, IOException {
    try {
      RSAPrivateKey privateKey;
      // If PKCS#1
      if (keyString.contains(PKCS_1_KEY_START)) {
        byte[] bytes = parseDERFromPEM(keyString, PKCS_1_KEY_START, PKCS_1_KEY_END);
        privateKey = generatePrivateKeyFromPKCS10DER(bytes);
      } else {
        // else, assume PKCS#8
        byte[] bytes = parseDERFromPEM(keyString, P8_KEY_START, P8_KEY_END);
        privateKey = generatePrivateKeyFromPKCS8DER(bytes);
      }

      Signature rsa = Signature.getInstance("NONEwithRSA");
      rsa.initSign(privateKey);
      rsa.update(string.getBytes());
      byte[] signed = rsa.sign();

      return new String(Base64.getEncoder().encode(signed));
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }
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

  private static RSAPrivateKey generatePrivateKeyFromPKCS10DER(byte[] keyBytes) throws GeneralSecurityException, IOException {
    DerInputStream derReader = new DerInputStream(keyBytes);
    DerValue[] seq = derReader.getSequence(0);

    if (seq.length < 9) {
      throw new GeneralSecurityException("Could not parse a PKCS1 private key.");
    }

    // skip version seq[0];
    BigInteger modulus = seq[1].getBigInteger();
    BigInteger publicExponent = seq[2].getBigInteger();
    BigInteger privateExponent = seq[3].getBigInteger();
    BigInteger primeP = seq[4].getBigInteger();
    BigInteger primeQ = seq[5].getBigInteger();
    BigInteger primeExponentP = seq[6].getBigInteger();
    BigInteger primeExponentQ = seq[7].getBigInteger();
    BigInteger crtCoefficient = seq[8].getBigInteger();
    RSAPrivateCrtKeySpec keySpec = new RSAPrivateCrtKeySpec(modulus, publicExponent, privateExponent, primeP, primeQ, primeExponentP, primeExponentQ, crtCoefficient);

    return (RSAPrivateKey) KeyFactory.getInstance("RSA").generatePrivate(keySpec);
  }

  private static RSAPrivateKey generatePrivateKeyFromPKCS8DER(byte[] keyBytes) throws InvalidKeySpecException, NoSuchAlgorithmException {
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

    // Strip all the whitespace since the PEM and DER allow them but they aren't valid in Base 64 encoding
    String base64 = pem.substring(startIndex + beginDelimiter.length(), endIndex).replaceAll("\\s", "");
    return Base64.getDecoder().decode(base64);
  }
}