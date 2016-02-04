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

import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * Tests the SSL Utilities class
 *
 * @author Seth Musselman
 */
public class SSLToolsTest {

  static final String almostGoodCert =
      "MIIDUjCCArugAwIBAgIJANZCTNN98L9ZMA0GCSqGSIb3DQEBBQUAMHoxCzAJBgNV\n" +
          "BAYTAlVTMQswCQYDVQQIEwJDTzEPMA0GA1UEBxMGZGVudmVyMQ8wDQYDVQQKEwZz\n" +
          "ZXRoLXMxCjAIBgNVBAsTAXMxDjAMBgNVBAMTBWludmVyMSAwHgYJKoZIhvcNAQkB\n" +
          "FhFzamZkZkBsc2tkamZjLmNvbTAeFw0xNDA0MDkyMTA2MDdaFw0xNDA1MDkyMTA2\n" +
          "MDdaMHoxCzAJBgNVBAYTAlVTMQswCQYDVQQIEwJDTzEPMA0GA1UEBxMGZGVudmVy\n" +
          "MQ8wDQYDVQQKEwZzZXRoLXMxCjAIBgNVBAsTAXMxDjAMBgNVBAMTBWludmVyMSAw\n" +
          "HgYJKoZIhvcNAQkBFhFzamZkZkBsc2tkamZjLmNvbTCBnzANBgkqhkiG9w0BAQEF\n" +
          "AAOBjQAwgYkCgYEAxnQBqyuYvjUE4aFQ6vVZU5RqHmy3KiTg2NcxELIlZztUTK3a\n" +
          "VFbJoBB4ixHXCCYslujthILyBjgT3F+IhSpPAcrlu8O5LVPaPCysh/SNrGNwH4lq\n" +
          "eiW9Z5WAhRO/nG7NZNa0USPHAei6b9Sv9PxuKCY+GJfAIwlO4/bltIH06/kCAwEA\n" +
          "AaOB3zCB3DAdBgNVHQ4EFgQUU4SqJEFm1zW+CcLxmLlARrqtMN0wgawGA1UdIwSB\n" +
          "pDCBoYAUU4SqJEFm1zW+CcLxmLlARrqtMN2hfqR8MHoxCzAJBgNVBAYTAlVTMQsw\n" +
          "CQYDVQQIEwJDTzEPMA0GA1UEBxMGZGVudmVyMQ8wDQYDVQQKEwZzZXRoLXMxCjAI\n" +
          "BgNVBAsTAXMxDjAMBgNVBAMTBWludmVyMSAwHgYJKoZIhvcNAQkBFhFzamZkZkBs\n" +
          "c2tkamZjLmNvbYIJANZCTNN98L9ZMAwGA1UdEwQFMAMBAf8wDQYJKoZIhvcNAQEF\n" +
          "BQADgYEAY/cJsi3w6R4hF4PzAXLhGOg1tzTDYvol3w024WoehJur+qM0AY6UqtoJ\n" +
          "neCq9af32IKbbOKkoaok+t1+/tylQVF/0FXMTKepxaMbG22vr4TmN3idPUYYbPfW\n" +
          "5GkF7Hh96BjerrtiUPGuBZL50HoLZ5aR5oZUMAu7TXhOFp+vZp8=\n";

  static final String goodCert =
      "-----BEGIN CERTIFICATE-----\n" +
          "MIIDUjCCArugAwIBAgIJANZCTNN98L9ZMA0GCSqGSIb3DQEBBQUAMHoxCzAJBgNV\n" +
          "BAYTAlVTMQswCQYDVQQIEwJDTzEPMA0GA1UEBxMGZGVudmVyMQ8wDQYDVQQKEwZz\n" +
          "ZXRoLXMxCjAIBgNVBAsTAXMxDjAMBgNVBAMTBWludmVyMSAwHgYJKoZIhvcNAQkB\n" +
          "FhFzamZkZkBsc2tkamZjLmNvbTAeFw0xNDA0MDkyMTA2MDdaFw0xNDA1MDkyMTA2\n" +
          "MDdaMHoxCzAJBgNVBAYTAlVTMQswCQYDVQQIEwJDTzEPMA0GA1UEBxMGZGVudmVy\n" +
          "MQ8wDQYDVQQKEwZzZXRoLXMxCjAIBgNVBAsTAXMxDjAMBgNVBAMTBWludmVyMSAw\n" +
          "HgYJKoZIhvcNAQkBFhFzamZkZkBsc2tkamZjLmNvbTCBnzANBgkqhkiG9w0BAQEF\n" +
          "AAOBjQAwgYkCgYEAxnQBqyuYvjUE4aFQ6vVZU5RqHmy3KiTg2NcxELIlZztUTK3a\n" +
          "VFbJoBB4ixHXCCYslujthILyBjgT3F+IhSpPAcrlu8O5LVPaPCysh/SNrGNwH4lq\n" +
          "eiW9Z5WAhRO/nG7NZNa0USPHAei6b9Sv9PxuKCY+GJfAIwlO4/bltIH06/kCAwEA\n" +
          "AaOB3zCB3DAdBgNVHQ4EFgQUU4SqJEFm1zW+CcLxmLlARrqtMN0wgawGA1UdIwSB\n" +
          "pDCBoYAUU4SqJEFm1zW+CcLxmLlARrqtMN2hfqR8MHoxCzAJBgNVBAYTAlVTMQsw\n" +
          "CQYDVQQIEwJDTzEPMA0GA1UEBxMGZGVudmVyMQ8wDQYDVQQKEwZzZXRoLXMxCjAI\n" +
          "BgNVBAsTAXMxDjAMBgNVBAMTBWludmVyMSAwHgYJKoZIhvcNAQkBFhFzamZkZkBs\n" +
          "c2tkamZjLmNvbYIJANZCTNN98L9ZMAwGA1UdEwQFMAMBAf8wDQYJKoZIhvcNAQEF\n" +
          "BQADgYEAY/cJsi3w6R4hF4PzAXLhGOg1tzTDYvol3w024WoehJur+qM0AY6UqtoJ\n" +
          "neCq9af32IKbbOKkoaok+t1+/tylQVF/0FXMTKepxaMbG22vr4TmN3idPUYYbPfW\n" +
          "5GkF7Hh96BjerrtiUPGuBZL50HoLZ5aR5oZUMAu7TXhOFp+vZp8=\n" +
          "-----END CERTIFICATE-----";

  static final String goodKey = "-----BEGIN PRIVATE KEY-----\n" +
      "MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBAMZ0AasrmL41BOGh\n" +
      "UOr1WVOUah5styok4NjXMRCyJWc7VEyt2lRWyaAQeIsR1wgmLJbo7YSC8gY4E9xf\n" +
      "iIUqTwHK5bvDuS1T2jwsrIf0jaxjcB+JanolvWeVgIUTv5xuzWTWtFEjxwHoum/U\n" +
      "r/T8bigmPhiXwCMJTuP25bSB9Ov5AgMBAAECgYBIcQJG7HQmQo5UmqFCE2uXEd8m\n" +
      "2gKGlM2q+eqEMLNsmoCsOO4wyTlyf3CxO0LmS6ENOVuyemZElOXTFOBt08Lgu8BX\n" +
      "NGVov+GwAcrd43neVZBE1YAv2Abq8C7+7GmL6QM26DENuYI2Ue2UZzUDd7oD3AVu\n" +
      "Xz/NLmnK8GirdeI1gQJBAP/qfmi5MyN5f8rCY3Q5ywTtvYFyrRcE5YMr48knLKyf\n" +
      "CEAPfyqmguzKc1eDrgJIdsLqtYTmgTZdbVxh/sOekVECQQDGhK8Lsu21YDvu6Y60\n" +
      "fKc3H4iW27JNfs8Ferl16HdDv1mx4NfPVFrbJpGLouFBl7dsR4PwmCinf1gVCM9z\n" +
      "u8YpAkEA0lSAtkNYM1S9YgCnyrNhF1jpwoVkmyGsHEWrPfzTa8B9AGF8K6qUulad\n" +
      "u5R/JGM4MHTE4Uitc+gPZLkpsM8XMQJAHjlxVTymyGigd12D5qLb6p6Ycy971CSs\n" +
      "bE8lEXiVP/FQPK7Y7K4RLzCOFE52MUBiItA4nFbuSaIvzPQMcbhzEQJBAKLida3/\n" +
      "NRaIquJndVAEG2JcTL3JJniGd+W1gOpCnXagjZGbSPpoRs2yxNlvUi7jdlJLkIbX\n" +
      "8T7yQ1ABvoEfQsM=\n" +
      "-----END PRIVATE KEY-----";

  @Test
  public void getSSLContext() {
    try {
      SSLTools.getSSLServerContext(almostGoodCert, goodKey);
      fail("Should not have worked");
    } catch (Exception e) {
      // Expected
    }

    try {
      SSLTools.getSSLServerContext(goodCert, goodKey);
    } catch (Exception e) {
      fail("Should not have failed", e);
    }
  }

  @Test
  public void getSSLSocket() {
    try {
      SSLTools.getSSLSocketFactory(almostGoodCert);
      fail("Should not have worked");
    } catch (Exception e) {
      // Expected
    }

    try {
      SSLTools.getSSLSocketFactory(goodCert);
    } catch (Exception e) {
      fail("Should not have failed");
    }
  }

  @Test
  public void validCertificate() {
    boolean res = SSLTools.validCertificateString("not a certificate");
    assertFalse(res);

    res = SSLTools.validCertificateString(almostGoodCert);
    assertFalse(res);

    res = SSLTools.validCertificateString(goodCert);
    assertTrue(res);
  }
}