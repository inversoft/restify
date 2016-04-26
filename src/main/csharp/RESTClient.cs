using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Net;
using System.Collections;

namespace com.inversoft.rest
{
    public class RESTClient<RS, ERS>
    {
        private static readonly Logger logger = LoggerFactory.getLogger(RESTClient.class);

        public readonly Dictionary<string, string> headers = new Dictionary<string, string>();

        public readonly Dictionary<string, List<Object>> parameters = new Dictionary<string, List<object>>();

        public readonly StringBuilder url = new StringBuilder();

        public BodyHandler bodyHandler;

        public string certificate;

        public int connectTimeout = 2000;

        public ResponseHandler<ERS> errorResponseHandler;

        public string key;

        public HTTPMethod method;

        public int readTimeout = 2000;

        public ResponseHandler<RS> successResponseHandler;

        public RESTClient()
        {

        }

        public RESTClient<RS, ERS> authorization(string key)
        {
            this.headers.Add("Authorization", key);
            return this;
        }

        public RESTClient<RS, ERS> basicAuthorization(string username, string password)
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

        public RESTClient<RS, ERS> ConnectTimeout(int connectTimeout)
        {
            this.connectTimeout = connectTimeout;
            return this;
        }

        public RESTClient<RS, ERS> delete()
        {
            this.method = HTTPMethod.DELETE;
            return this;
        }

        public RESTClient<RS, ERS> errorResponseHandlerMethod(ResponseHandler<ERS> errorResponseHandler)
        {
            this.errorResponseHandler = errorResponseHandler;
            return this;
        }

        public RESTClient<RS, ERS> get()
        {
            this.method = HTTPMethod.GET;
            return this;
        }

        public ClientResponse<RS, ERS> go()
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
            WebRequest request;
            try
            {
                if (parameters.size() > 0)
                {
                    if (url.indexOf("?") == -1)
                    {
                        url.append("?");
                    }

                    for (Iterator<Entry<string, List<Object>>> i = parameters.entrySet().iterator(); i.hasNext();)
                    {
                        Entry<string, List<Object>> entry = i.next();

                        for (Iterator<Object> j = entry.getValue().iterator(); j.hasNext();)
                        {
                            Object value = j.next();
                            url.append(URLEncoder.encode(entry.getKey(), "UTF-8")).append("=").append(URLEncoder.encode(value.toString(), "UTF-8"));
                            if (j.hasNext())
                            {
                                url.append("&");
                            }
                        }

                        if (i.hasNext())
                        {
                            url.append("&");
                        }
                    }
                }

                URL urlObject = new URL(url.toString());
                huc = (HttpURLConnection)urlObject.openConnection();
                if (urlObject.getProtocol().toLowerCase().equals("https") && certificate != null)
                {
                    HttpsURLConnection hsuc = (HttpsURLConnection)huc;
                    if (key != null)
                    {
                        hsuc.setSSLSocketFactory(SSLTools.getSSLServerContext(certificate, key).getSocketFactory());
                    }
                    else
                    {
                        hsuc.setSSLSocketFactory(SSLTools.getSSLSocketFactory(certificate));
                    }
                }

                huc.setDoOutput(bodyHandler != null);
                huc.setConnectTimeout(connectTimeout);
                huc.setReadTimeout(readTimeout);
                huc.setRequestMethod(method.toString());

                if (headers.size() > 0)
                {
                    headers.forEach(huc::addRequestProperty);
                }

                if (bodyHandler != null)
                {
                    bodyHandler.setHeaders(huc);
                }

                huc.connect();

                if (bodyHandler != null)
                {
                    try (OutputStream os = huc.getOutputStream()) {
                        bodyHandler.accept(os);
                        os.flush();
                    }
                    }
    } catch (Exception e)
            {
                logger.debug("Error calling REST WebService at [" + url + "]", e);
                response.exception = e;
                return response;
            }

            int status;
            try
            {
                status = huc.getResponseCode();
            }
            catch (Exception e)
            {
                logger.debug("Error calling REST WebService at [" + url + "]", e);
                response.exception = e;
                return response;
            }

            response.status = status;

            if (status < 200 || status > 299)
            {
                if (errorResponseHandler == null)
                {
                    return response;
                }

                try (InputStream is = huc.getErrorStream()) {
                    response.errorResponse = errorResponseHandler.apply(is);
                } catch (Exception e)
                {
                    logger.debug("Error calling REST WebService at [" + url + "]", e);
                    response.exception = e;
                    return response;
                }
            }
            else
            {
                if (successResponseHandler == null)
                {
                    return response;
                }

                try (InputStream is = huc.getInputStream()) {
                    response.successResponse = successResponseHandler.apply(is);
                } catch (Exception e)
                {
                    logger.debug("Error calling REST WebService at [" + url + "]", e);
                    response.exception = e;
                    return response;
                }
            }

            return response;
        }

        public RESTClient<RS, ERS> header(string name, string value)
        {
            this.headers.Add(name, value);
            return this;
        }

        public RESTClient<RS, ERS> headersGroup(Dictionary<string, string> newHeaders)
        {
            foreach (var header in newHeaders)
            {
                this.headers.Add(header.Key, header.Value);
            }

            return this;
        }

        public RESTClient<RS, ERS> keyMethod(string newKey)
        {
            this.key = newKey;
            return this;
        }

        public RESTClient<RS, ERS> post()
        {
            this.method = HTTPMethod.POST;
            return this;
        }

        public RESTClient<RS, ERS> put()
        {
            this.method = HTTPMethod.PUT;
            return this;
        }

        public RESTClient<RS, ERS> readTimeoutMethod(int readTimeout)
        {
            this.readTimeout = readTimeout;
            return this;
        }

        public RESTClient<RS, ERS> successResponseHandlerMethod(ResponseHandler<RS> successResponseHandler)
        {
            this.successResponseHandler = successResponseHandler;
            return this;
        }

        public RESTClient<RS, ERS> uri(string uri)
        {
            if (url.Length == 0)
            {
                return this;
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

        public RESTClient<RS, ERS> urlMethod(string url)
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
        public RESTClient<RS, ERS> urlParameter(string name, Object value)
        {
            if (value == null)
            {
                return this;
            }

            List<Object> values = this.parameters[name];
            if (values == null)
            {
                values = new List<Object>();
                this.parameters.Add(name, values);
            }

            if (value is DateTime)
            {
                values.Add(((DateTime)value).ToUniversalTime().Subtract(new DateTime(1970, 1, 1, 0, 0, 0, DateTimeKind.Utc)).TotalMilliseconds);
            }
            else if (value is IEnumerable) 
            {
                foreach (var val in (IEnumerable) value)
                {
                    values.Add(val);
                }
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
        public RESTClient<RS, ERS> urlSegment(Object value)
        {
            if (value != null)
            {
                if (url.charAt(url.length() - 1) != '/')
                {
                    url.append('/');
                }
                url.append(value.toString());
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
            T apply(InputStream is) throws IOException;
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
        void accept(OutputStream os) throws IOException;

        /**
         * Sets any headers for the HTTP body that will be written.
         *
         * @param huc The HttpURLConnection to set headers into.
         */
        void setHeaders(HttpURLConnection huc);
    }

    public sealed class RESTVoid
    {

    }
    
}

