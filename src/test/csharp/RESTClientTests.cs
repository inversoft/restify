using Microsoft.VisualStudio.TestTools.UnitTesting;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace com.inversoft.rest.Tests
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
        public void ConnectTimeout_Test()
        {
            int testTime = 9;
            RESTClient<int, int> restTest = new RESTClient<int, int>();

            restTest.ConnectTimeout(testTime);
            int actualTime = (int)restTest.timeout;

            Assert.AreEqual(testTime, actualTime);
        }

        [TestMethod()]
        public void ConnectTimeout_IsZero_Test()
        {
            int testTime = 0;
            RESTClient<int, int> restTest = new RESTClient<int, int>();

            restTest.ConnectTimeout(testTime);
            int actualTime = (int)restTest.timeout;

            Assert.AreEqual(testTime, actualTime);
        }

        [TestMethod()]
        public void Delete_Test()
        {
            RESTClient<int, int>.HTTPMethod testMethod = RESTClient<int, int>.HTTPMethod.DELETE;
            RESTClient<int, int> testRest = new RESTClient<int, int>();

            testRest.delete();
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

            testRest.get();
            RESTClient<int, int>.HTTPMethod actualMethod = testRest.method;

            Assert.AreEqual(testMethod, actualMethod);
        }

        [TestMethod()]
        public void Go_Test()
        {

        }

        
    }
}