/*
 * Copyright (c) 2016, Inversoft Inc., All Rights Reserved
 */
using System;
using System.Collections;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Net;
using System.Net.Security;
using System.Web;
using System.IO;
using NLog;

namespace Com.Inversoft.Rest
{
    /**
 * RESTful WebService call builder. This provides the ability to call RESTful WebServices using a builder pattern to
 * set up all the necessary request information and parse the response.
 */
    public class RESTClient<RS, ERS>
    {
        private static readonly Logger logger = LogManager.GetCurrentClassLogger();

        public readonly IDictionary<string, string> headers = new Dictionary<string, string>();

        public readonly IDictionary<string, List<Object>> parameters = new Dictionary<string, List<object>>();

        public readonly StringBuilder url = new StringBuilder();

        public BodyHandler bodyHandler;

        public string certificate;

        public int? timeout = null;

        public ResponseHandler<ERS> errorResponseHandler;

        public string key;

        public HTTPMethod method;

        public int? readWriteTimeout = null;

        public ResponseHandler<RS> successResponseHandler;

        public RESTClient()
        {

        }

        public RESTClient<RS, ERS> Authorization(string key)
        {
            this.headers.Add("Authorization", key);
            return this;
        }

        public RESTClient<RS, ERS> BasicAuthorization(string username, string password)
        {
            if (username != null && password != null)
            {
                string credentials = username + ":" + password;
                string encoded = Convert.ToBase64String(System.Text.Encoding.UTF8.GetBytes(credentials));
                this.headers.Add("Authorization", "Basic " + encoded);
            }
            return this;
        }

        public RESTClient<RS, ERS> BodyHandler(BodyHandler bodyHandler)
        {
            this.bodyHandler = bodyHandler;
            return this;
        }

        public RESTClient<RS, ERS> Certificate(string certificate)
        {
            this.certificate = certificate;
            return this;
        }

        public RESTClient<RS, ERS> Timeout(int connectTimeout)
        {
            this.timeout = connectTimeout;
            return this;
        }

        public RESTClient<RS, ERS> Delete()
        {
            this.method = HTTPMethod.DELETE;
            return this;
        }

        public RESTClient<RS, ERS> ErrorResponseHandler(ResponseHandler<ERS> errorResponseHandler)
        {
            this.errorResponseHandler = errorResponseHandler;
            return this;
        }

        public RESTClient<RS, ERS> Get()
        {
            this.method = HTTPMethod.GET;
            return this;
        }

        public ClientResponse<RS, ERS> Go()
        {
            if (url.Length == 0)
            {
                throw new InvalidOperationException("You must specify a URL");
            }

            if (method == HTTPMethod.UNSET)
            {
                throw new InvalidOperationException("You must specify a HTTP method");
            }

            if (typeof(RS) != typeof(RESTVoid) && successResponseHandler == null)
            {
                throw new InvalidOperationException("You specified a success response type, you must then provide a success response handler.");
            }

            if (typeof(ERS) != typeof(RESTVoid) && errorResponseHandler == null)
            {
                throw new InvalidOperationException("You specified an error response type, you must then provide an error response handler.");
            }

            ClientResponse<RS, ERS> response = new ClientResponse<RS, ERS>();
            HttpWebRequest request;

            try
            {
                if (parameters.Count > 0)
                {
                    if (!url.ToString().Contains("?"))
                    {
                        url.Append("?");
                    }

                    foreach (var pair in parameters)
                    {
                        foreach (var value in pair.Value)
                        {
                            url.Append(HttpUtility.UrlEncode(pair.Key, Encoding.UTF8)).Append("=").Append(HttpUtility.UrlEncode(value.ToString(), Encoding.UTF8)).Append("&");
                        }
                    }

                    url.Remove(url.Length-1, 1);        
                }

                Uri urlObject = new Uri(url.ToString());
                request = (HttpWebRequest)WebRequest.Create(urlObject);

                //Need to resolve SSL issue

                //if (urlObject.Scheme.ToLower().Equals("https") && certificate != null)
                //{
                //    HttpWebRequest wr = request;
                //    if (key != null)
                //    {
                //        wr.setSSLSocketFactory(SSLTools.getSSLServerContext(certificate, key).getSocketFactory());
                //    }
                //    else
                //    {
                //        wr.setSSLSocketFactory(SSLTools.getSSLSocketFactory(certificate));
                //    }
                //}

                //request.setDoOutput(bodyHandler != null);

                if (timeout != null)
                {
                    request.Timeout = (int)timeout;
                }              
                if (readWriteTimeout != null)
                {
                    request.ReadWriteTimeout = (int)readWriteTimeout;
                }
                request.Method = method.ToString();

                if (headers.Count > 0)
                {
                    foreach (var header in headers)
                    {
                        request.Headers.Add(header.Key, header.Value);
                    }
                }

                if (bodyHandler != null)
                {
                    bodyHandler.SetHeaders(request);
                }

                if (bodyHandler != null)
                {
                    using (Stream stream = request.GetRequestStream()) 
                    {
                        bodyHandler.Accept(stream);
                        stream.Flush();
                    }
                }
            }

            catch (Exception e)
            {
                logger.Debug(e, "Error calling REST WebService at [" + url + "]");
                response.exception = e;
                return response;
            }

            try
            {
                HttpWebResponse resp = (HttpWebResponse)request.GetResponse();
                response.status = (int)resp.StatusCode;

                if (successResponseHandler == null)
                {
                    return response;
                }

                try
                {
                    using (Stream str = request.GetResponse().GetResponseStream())
                    {
                        response.successResponse = successResponseHandler.Apply(str);
                    }
                }

                catch (Exception e)
                {
                    logger.Debug(e, "Error calling REST WebService at [" + url + "]");
                    response.exception = e;
                    return response;
                }
            }

            catch (WebException e)
            {                               
                using (WebResponse webResp = e.Response)
                {
                    HttpWebResponse httpResp = (HttpWebResponse)webResp;
                    response.status = (int)httpResp.StatusCode;

                    if (errorResponseHandler == null)
                    {
                        return response;
                    }

                    try
                    {
                        response.errorResponse = errorResponseHandler.Apply(httpResp.GetResponseStream());
                    }
                                    
                    catch (Exception ex)
                    {
                        logger.Debug(e, "Error calling REST WebService at [" + url + "]");
                        response.exception = ex;
                        return response;
                    }    
                }         
            }
       
            return response;    
        }

        public RESTClient<RS, ERS> Header(string name, string value)
        {
            this.headers.Add(name, value);
            return this;
        }

        public RESTClient<RS, ERS> Headers(Dictionary<string, string> newHeaders)
        {
            foreach (var header in newHeaders)
            {
                this.headers.Add(header.Key, header.Value);
            }

            return this;
        }

        public RESTClient<RS, ERS> KeyMethod(string newKey)
        {
            this.key = newKey;
            return this;
        }

        public RESTClient<RS, ERS> Post()
        {
            this.method = HTTPMethod.POST;
            return this;
        }

        public RESTClient<RS, ERS> Put()
        {
            this.method = HTTPMethod.PUT;
            return this;
        }

        public RESTClient<RS, ERS> ReadWriteTimeout(int readTimeout)
        {
            this.readWriteTimeout = readTimeout;
            return this;
        }

        public RESTClient<RS, ERS> SuccessResponseHandler(ResponseHandler<RS> successResponseHandler)
        {
            this.successResponseHandler = successResponseHandler;
            return this;
        }

        public RESTClient<RS, ERS> Uri(string uri)
        {
            if (url.Length == 0)
            {
                return this; //throw an exception?
            }

            if (url[url.Length - 1] == '/' && uri[0] == '/')
            {
                url.Append(uri.Substring(1));
            }
            else if (url[url.Length - 1] != '/' && uri[0] != '/')
            {
                url.Append("/").Append(uri);
            }
            else
            {
                url.Append(uri);
            }

            return this;
        }

        public RESTClient<RS, ERS> Url(string url)
        {
            this.url.Clear();
            this.url.Append(url);
            return this;
        }

        /**
         * Add a URL parameter as a key value pair.
         *
         * @param name  The URL parameter name.
         * @param value The url parameter value. The <code>.tostring()</code> method will be used to
         *              get the <code>String</code> used in the URL parameter. If the object type is a
         *              {@link Collection} a key value pair will be added for each value in the collection.
         *              {@link ZonedDateTime} will also be handled uniquely in that the <code>long</code> will
         *              be used to set in the request using <code>ZonedDateTime.toInstant().toEpochMilli()</code>
         * @return This.
         */
        public RESTClient<RS, ERS> UrlParameter(string name, Object value)
        {
            if (value == null)
            {
                return this;
            }

            List<Object> values;
            this.parameters.TryGetValue(name, out values);

            if (values == null)
            {
                values = new List<Object>();
                this.parameters.Add(name, values);
            }

            if (value is DateTime)
            {
                values.Add(((DateTime)value).ToUniversalTime().Subtract(new DateTime(1970, 1, 1, 0, 0, 0, DateTimeKind.Utc)).TotalMilliseconds);
            }
            else if (value is ICollection) 
            {
                foreach (var val in (ICollection) value)
                {
                    values.Add(val);
                }
            }
            else if (value is bool)
            {
                values.Add(value.ToString().ToLower());
            }
            else 
            {
                values.Add(value);
            }

            return this;
        }

        /**
         * Append a url path segment. <p>
         * For Example: <pre>
         *     .url("http://www.foo.com")
         *     .urlSegment("bar")
         *   </pre>
         * This will result in a url of <code>http://www.foo.com/bar</code>
         *
         * @param value The url path segment. A null value will be ignored.
         * @return This.
         */
        public RESTClient<RS, ERS> UrlSegment(Object value)
        {
            if (value != null)
            {
                if (url[url.Length - 1] != '/')
                {
                    url.Append('/');
                }
                url.Append(value.ToString());
            }
            return this;
        }

        /**
         * Standard HTTP methods. This doesn't have CONNECT, TRACE, HEAD or OPTIONS.
         */
        public enum HTTPMethod
        {
            UNSET,
            GET,
            POST,
            PUT,
            DELETE
        }      
    }

    /**
     * Body handler that manages sending the bytes of the HTTP request body to the HttpURLConnection. This also is able to
     * manage any HTTP headers that are associated with the body such as Content-Type and Content-Length.
     */
    public interface BodyHandler
    {
        /**
         * Accepts the OutputStream and writes the bytes of the HTTP request body to it.
         *
         * @param os The OutputStream to write the body to.
         * @throws IOException If the write failed.
         */
        void Accept(Stream sw);

        /**
         * Sets any headers for the HTTP body that will be written.
         *
         * @param huc The HttpURLConnection to set headers into.
         */
        void SetHeaders(HttpWebRequest req);
    }

    /**
         * Handles responses from the HTTP server.
         *
         * @param <T> The type that is returned from the handler.
         */
    public interface ResponseHandler<T>
    {
        /**
         * Handles the InputStream that is the HTTP response and reads it in and converts it to a value.
         *
         * @param is The InputStream to read from.
         * @return The value.
         * @throws IOException If the read failed.
         */
        T Apply(Stream sr);
    }

    public sealed class RESTVoid
    {

    }  
}

