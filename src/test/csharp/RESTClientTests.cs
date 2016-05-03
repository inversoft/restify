/*
 * Copyright (c) 2016, Inversoft Inc., All Rights Reserved
 */

using Microsoft.VisualStudio.TestTools.UnitTesting;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.IO;
using System.Net;

namespace Com.Inversoft.Rest.Tests
{
    [TestClass()]
    public class RESTClientTests
    {
        [TestMethod()]
        public void Authorization_Test()
        {
            //arrange
            string testKey = "abc";
            RESTClient<int, int> restTest = new RESTClient<int, int>();
            //act
            restTest.Authorization(testKey);
            string actualKey = restTest.headers["Authorization"];
            //assert
            Assert.AreEqual(testKey, actualKey);
        }

        [TestMethod()]
        public void Authorization_IsNull_Test()
        {
            //arrange
            string testKey = null;
            RESTClient<int, int> restTest = new RESTClient<int, int>();
            //act
            restTest.Authorization(testKey);
            string actualKey = restTest.headers["Authorization"];
            //assert
            Assert.IsNull(actualKey);
        }

        [TestMethod()]
        public void BasicAuthorization_UsrPwd_Test()
        {
            string testUsr = "username";
            string testPwd = "password";
            string cred = testUsr + ":" + testPwd;
            string enc = Convert.ToBase64String(System.Text.Encoding.UTF8.GetBytes(cred));
            string expected = "Basic " + enc;
            RESTClient<int, int> restTest = new RESTClient<int, int>();

            restTest.BasicAuthorization(testUsr, testPwd);
            string actual = restTest.headers["Authorization"];

            Assert.AreEqual(expected, actual);
        }

        [TestMethod()]
        public void BasicAuthorization_UsrIsNull_Test()
        {
            string testUsr = null;
            string testPwd = "password";
            string cred = testUsr + ":" + testPwd;
            string enc = Convert.ToBase64String(System.Text.Encoding.UTF8.GetBytes(cred));
            string expected = "Basic " + enc;
            RESTClient<int, int> restTest = new RESTClient<int, int>();

            restTest.BasicAuthorization(testUsr, testPwd);
            //string actual = restTest.headers["Authorization"];

            Assert.IsFalse(restTest.headers.ContainsKey("Authorization"));
        }

        [TestMethod()]
        public void BasicAuthorization_PwdIsNull_Test()
        {
            string testUsr = "username";
            string testPwd = null;
            string cred = testUsr + ":" + testPwd;
            string enc = Convert.ToBase64String(System.Text.Encoding.UTF8.GetBytes(cred));
            string expected = "Basic " + enc;
            RESTClient<int, int> restTest = new RESTClient<int, int>();

            restTest.BasicAuthorization(testUsr, testPwd);
            //string actual = restTest.headers["Authorization"];

            Assert.IsFalse(restTest.headers.ContainsKey("Authorization"));
        }

        [TestMethod()]
        public void BasicAuthorization_BothNull_Test()
        {
            string testUsr = null;
            string testPwd = null;
            string cred = testUsr + ":" + testPwd;
            string enc = Convert.ToBase64String(System.Text.Encoding.UTF8.GetBytes(cred));
            string expected = "Basic " + enc;
            RESTClient<int, int> restTest = new RESTClient<int, int>();

            restTest.BasicAuthorization(testUsr, testPwd);
            //string actual = restTest.headers["Authorization"];

            Assert.IsFalse(restTest.headers.ContainsKey("Authorization"));
        }

        [TestMethod()]
        public void BodyHandler_Test()
        {
            BodyHandler bodyTest = new JSONBodyHandler();
            RESTClient<int, int> restTest = new RESTClient<int, int>();

            restTest.BodyHandler(bodyTest);
            BodyHandler actualBody = restTest.bodyHandler;

            Assert.AreEqual(bodyTest, actualBody);
        }

        [TestMethod()]
        public void BodyHandler_IsNull_Test()
        {
            BodyHandler bodyTest = null;
            RESTClient<int, int> restTest = new RESTClient<int, int>();

            restTest.BodyHandler(bodyTest);
            BodyHandler actualBody = restTest.bodyHandler;

            Assert.AreEqual(bodyTest, actualBody);
        }

        [TestMethod()]
        public void Certificate_Test()
        {
            string testCert = "certificate";
            RESTClient<int, int> restTest = new RESTClient<int, int>();

            restTest.Certificate(testCert);
            string actualCert = restTest.certificate;

            Assert.AreEqual(testCert, actualCert);
        }

        [TestMethod()]
        public void Certificate_IsNull_Test()
        {
            string testCert = null;
            RESTClient<int, int> restTest = new RESTClient<int, int>();

            restTest.Certificate(testCert);
            string actualCert = restTest.certificate;

            Assert.AreEqual(testCert, actualCert);
        }

        [TestMethod()]
        public void Timeout_Test()
        {
            int testTime = 9;
            RESTClient<int, int> restTest = new RESTClient<int, int>();

            restTest.Timeout(testTime);
            int actualTime = (int)restTest.timeout;

            Assert.AreEqual(testTime, actualTime);
        }

        [TestMethod()]
        public void Timeout_IsZero_Test()
        {
            int testTime = 0;
            RESTClient<int, int> restTest = new RESTClient<int, int>();

            restTest.Timeout(testTime);
            int actualTime = (int)restTest.timeout;

            Assert.AreEqual(testTime, actualTime);
        }

        [TestMethod()]
        public void Delete_Test()
        {
            RESTClient<int, int>.HTTPMethod testMethod = RESTClient<int, int>.HTTPMethod.DELETE;
            RESTClient<int, int> testRest = new RESTClient<int, int>();

            testRest.Delete();
            RESTClient<int, int>.HTTPMethod actualMethod = testRest.method;

            Assert.AreEqual(testMethod, actualMethod);
        }

        [TestMethod()]
        public void ErrorResponseHandler_Test()
        {
            ResponseHandler<int> testResponse = new JSONResponseHandler<int>();
            RESTClient<int, int> testRest = new RESTClient<int, int>();

            testRest.ErrorResponseHandler(testResponse);
            ResponseHandler<int> actualResponse = testRest.errorResponseHandler;

            Assert.AreEqual(testResponse, actualResponse);
        }

        [TestMethod()]
        public void ErrorResponseHandler_IsNull_Test()
        {
            ResponseHandler<int> testResponse = null;
            RESTClient<int, int> testRest = new RESTClient<int, int>();

            testRest.ErrorResponseHandler(testResponse);
            ResponseHandler<int> actualResponse = testRest.errorResponseHandler;

            Assert.AreEqual(testResponse, actualResponse);
        }

        [TestMethod()]
        public void Get_Test()
        {
            RESTClient<int, int>.HTTPMethod testMethod = RESTClient<int, int>.HTTPMethod.GET;
            RESTClient<int, int> testRest = new RESTClient<int, int>();

            testRest.Get();
            RESTClient<int, int>.HTTPMethod actualMethod = testRest.method;

            Assert.AreEqual(testMethod, actualMethod);
        }

        [TestMethod()]
        public void Go_Google_Test()
        {
            ClientResponse<string, RESTVoid> restTest = new RESTClient<string, RESTVoid>()
                .Url("http://www.google.com")
                .SuccessResponseHandler(new TestHTMLResponseHandler())
                .Get()
                .Go();

            Assert.AreEqual(200, restTest.status);
            Assert.IsTrue(restTest.successResponse != null);
        }

        [TestMethod()]
        public void Go_Passport_Test()
        {
            ClientResponse<string, RESTVoid> restTest = new RESTClient<string, RESTVoid>()
                .Authorization("7844b96b-1e5f-40d7-bd5f-280b282a27e7")
                .Url("http://localhost:9011/api/application")
                .SuccessResponseHandler(new TestHTMLResponseHandler())
                .Get()
                .Go();

            Assert.AreEqual(200, restTest.status);
            Assert.IsTrue(restTest.successResponse != null);

            Console.WriteLine(restTest.successResponse);
        }

        [TestMethod()]
        public void Url_Parameter_Test()
        {
            DateTime now = DateTime.Now;
            double milliseconds = now.ToUniversalTime().Subtract(new DateTime(1970, 1, 1, 0, 0, 0, DateTimeKind.Utc)).TotalMilliseconds;

            RESTClient<RESTVoid, RESTVoid> restTest = new RESTClient<RESTVoid, RESTVoid>()
                .Url("http://www.google.com")
                .Uri("mee")
                .UrlSegment(null)
                .UrlSegment("garble")
                .UrlParameter("time", now)
                .UrlParameter("foo", "bar")
                .UrlParameter("baz", null)
                .UrlParameter("ids", new List<string> { "abc", "efg", "hij", "k" })
                .Get();

            Assert.AreEqual("http://www.google.com/mee/garble", restTest.url.ToString());
            Assert.AreEqual(1, restTest.parameters["time"].Count);
            Assert.AreEqual(now.ToUniversalTime().Subtract(new DateTime(1970, 1, 1, 0, 0, 0, DateTimeKind.Utc)).TotalMilliseconds, restTest.parameters["time"].First<Object>());
            Assert.AreEqual(1, restTest.parameters["foo"].Count);
            Assert.AreEqual("bar", restTest.parameters["foo"].First<Object>());
            Assert.IsFalse(restTest.parameters.ContainsKey("baz"));

            restTest.Go();

            Assert.AreEqual("http://www.google.com/mee/garble?time=" + milliseconds + "&foo=bar&ids=abc&ids=efg&ids=hij&ids=k", restTest.url.ToString());
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