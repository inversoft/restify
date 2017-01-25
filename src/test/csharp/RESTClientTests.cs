/*
 * Copyright (c) 2016, Inversoft Inc., All Rights Reserved
 */

using System;
using System.Collections.Generic;
using System.Text;
using System.IO;
using System.Net;
using NUnit.Framework;

namespace Inversoft.Restify.Tests
{
  [TestFixture]
  public class RESTClientTests
  {
    [Test]
    public void Authorization()
    {
      var testKey = "abc";
      var restTest = new RESTClient<int, int>();
      restTest.Authorization(testKey);
      var actualKey = restTest.headers["Authorization"];
      Assert.AreEqual(testKey, actualKey);
    }

    [Test]
    public void Authorization_IsNull()
    {
      string testKey = null;
      var restTest = new RESTClient<int, int>();
      restTest.Authorization(testKey);
      var actualKey = restTest.headers["Authorization"];
      Assert.IsNull(actualKey);
    }

    [Test]
    public void BasicAuthorization_UsrPwd()
    {
      var testUsr = "username";
      var testPwd = "password";
      var cred = testUsr + ":" + testPwd;
      var enc = Convert.ToBase64String(Encoding.UTF8.GetBytes(cred));
      var expected = "Basic " + enc;
      var restTest = new RESTClient<int, int>();

      restTest.BasicAuthorization(testUsr, testPwd);
      string actual = restTest.headers["Authorization"];

      Assert.AreEqual(expected, actual);
    }

    [Test]
    public void BasicAuthorization_UsrIsNull()
    {
      string testUsr = null;
      var testPwd = "password";
      var cred = testUsr + ":" + testPwd;
      var enc = Convert.ToBase64String(Encoding.UTF8.GetBytes(cred));
      var expected = "Basic " + enc;
      var restTest = new RESTClient<int, int>();

      restTest.BasicAuthorization(testUsr, testPwd);

      Assert.IsFalse(restTest.headers.ContainsKey("Authorization"));
    }

    [Test]
    public void BasicAuthorization_PwdIsNull()
    {
      var testUsr = "username";
      string testPwd = null;
      var cred = testUsr + ":" + testPwd;
      var enc = Convert.ToBase64String(Encoding.UTF8.GetBytes(cred));
      var expected = "Basic " + enc;
      var restTest = new RESTClient<int, int>();

      restTest.BasicAuthorization(testUsr, testPwd);

      Assert.IsFalse(restTest.headers.ContainsKey("Authorization"));
    }

    [Test]
    public void BasicAuthorization_BothNull()
    {
      string testUsr = null;
      string testPwd = null;
      var cred = testUsr + ":" + testPwd;
      var enc = Convert.ToBase64String(Encoding.UTF8.GetBytes(cred));
      var expected = "Basic " + enc;
      var restTest = new RESTClient<int, int>();

      restTest.BasicAuthorization(testUsr, testPwd);

      Assert.IsFalse(restTest.headers.ContainsKey("Authorization"));
    }

    [Test]
    public void BodyHandler()
    {
      BodyHandler bodyTest = new JSONBodyHandler();
      var restTest = new RESTClient<int, int>();

      restTest.BodyHandler(bodyTest);
      var actualBody = restTest.bodyHandler;

      Assert.AreEqual(bodyTest, actualBody);
    }

    [Test]
    public void BodyHandler_IsNull()
    {
      BodyHandler bodyTest = null;
      var restTest = new RESTClient<int, int>();

      restTest.BodyHandler(bodyTest);
      var actualBody = restTest.bodyHandler;

      Assert.AreEqual(bodyTest, actualBody);
    }

    //[Test]
    //public void Certificate()
    //{
    //    string testCert = "certificate";
    //    var restTest = new RESTClient<int, int>();

    //    restTest.Certificate(testCert);
    //    string actualCert = restTest.certificate;

    //    Assert.AreEqual(testCert, actualCert);
    //}

    //[Test]
    //public void Certificate_IsNull()
    //{
    //    string testCert = null;
    //    var restTest = new RESTClient<int, int>();

    //    restTest.Certificate(testCert);
    //    string actualCert = restTest.certificate;

    //    Assert.AreEqual(testCert, actualCert);
    //}

    [Test]
    public void Timeout()
    {
      var testTime = 9;
      var restTest = new RESTClient<int, int>();

      restTest.Timeout(testTime);
      var actualTime = restTest.timeout;

      Assert.AreEqual(testTime, actualTime);
    }

    [Test]
    public void Timeout_IsZero()
    {
      var testTime = 0;
      var restTest = new RESTClient<int, int>();

      restTest.Timeout(testTime);
      var actualTime = restTest.timeout;

      Assert.AreEqual(testTime, actualTime);
    }

    [Test]
    public void Delete()
    {
      var testMethod = HTTPMethod.DELETE;
      var testRest = new RESTClient<int, int>();

      testRest.Delete();
      var actualMethod = testRest.method;

      Assert.AreEqual(testMethod, actualMethod);
    }

    [Test]
    public void ErrorResponseHandler()
    {
      ResponseHandler<int> testResponse = new JSONResponseHandler<int>();
      var testRest = new RESTClient<int, int>();

      testRest.ErrorResponseHandler(testResponse);
      var actualResponse = testRest.errorResponseHandler;

      Assert.AreEqual(testResponse, actualResponse);
    }

    [Test]
    public void ErrorResponseHandler_IsNull()
    {
      ResponseHandler<int> testResponse = null;
      var testRest = new RESTClient<int, int>();

      testRest.ErrorResponseHandler(testResponse);
      var actualResponse = testRest.errorResponseHandler;

      Assert.AreEqual(testResponse, actualResponse);
    }

    [Test]
    public void Get()
    {
      var testMethod = HTTPMethod.GET;
      var testRest = new RESTClient<int, int>();

      testRest.Get();
      var actualMethod = testRest.method;

      Assert.AreEqual(testMethod, actualMethod);
    }

    [Test]
    public void Go_Google()
    {
      var restTest = new RESTClient<string, RESTVoid>()
          .Url("http://www.google.com")
          .SuccessResponseHandler(new TestHTMLResponseHandler())
          .Get()
          .Go();

      Assert.AreEqual(200, restTest.status);
      Assert.IsTrue(restTest.successResponse != null);
    }

    [Test]
    public void Go_Google_With_Proxy()
    {
      var restTest = new RESTClient<string, RESTVoid>()
          .Url("https://www.google.com")
          .Proxy(new WebProxy("173.255.253.125:3128", true, null, new NetworkCredential("test", "password")))
          .SuccessResponseHandler(new TestHTMLResponseHandler())
          .Get()
          .Go();

      Console.WriteLine(restTest.successResponse);
      Assert.AreEqual(200, restTest.status);
      Assert.IsTrue(restTest.successResponse != null);
    }

    [Test]
    public void Go_Passport()
    {
      var restTest = new RESTClient<string, RESTVoid>()
          .Authorization("7844b96b-1e5f-40d7-bd5f-280b282a27e7")
          .Url("http://localhost:9011/api/application")
          .SuccessResponseHandler(new TestHTMLResponseHandler())
          .Get()
          .Go();

      Assert.AreEqual(200, restTest.status);
      Assert.IsTrue(restTest.successResponse != null);

      Console.WriteLine(restTest.successResponse);
    }

    [Test]
    public void Url_Parameter()
    {
      var now = DateTime.Now;
      var milliseconds = now.ToUniversalTime().Subtract(new DateTime(1970, 1, 1, 0, 0, 0, DateTimeKind.Utc))
                            .TotalMilliseconds;

      var restTest = new RESTClient<RESTVoid, RESTVoid>().Url("http://www.google.com")
                                                         .Uri("mee")
                                                         .UrlSegment(null)
                                                         .UrlSegment("garble")
                                                         .UrlParameter("time", now)
                                                         .UrlParameter("foo", "bar")
                                                         .UrlParameter("baz", null)
                                                         .UrlParameter("ids",
                                                                       new List<string> {"abc", "efg", "hij", "k"})
                                                         .Get();

      Assert.AreEqual("http://www.google.com/mee/garble", restTest.url.ToString());
      Assert.AreEqual(1, restTest.parameters["time"].Count);
      Assert.AreEqual(
                      now.ToUniversalTime().Subtract(new DateTime(1970, 1, 1, 0, 0, 0, DateTimeKind.Utc))
                         .TotalMilliseconds, restTest.parameters["time"][0]);
      Assert.AreEqual(1, restTest.parameters["foo"].Count);
      Assert.AreEqual("bar", restTest.parameters["foo"][0]);
      Assert.IsFalse(restTest.parameters.ContainsKey("baz"));

      restTest.Go();

      Assert.AreEqual(
                      "http://www.google.com/mee/garble?time=" + milliseconds
                      + "&foo=bar&ids=abc&ids=efg&ids=hij&ids=k", restTest.url.ToString());
    }

    public class TestHTMLResponseHandler : ResponseHandler<string>
    {
      public string Apply(Stream sr)
      {
        return new StreamReader(sr, Encoding.UTF8).ReadToEnd();
      }
    }
  }
}