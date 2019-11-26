/*
 * Copyright (c) 2016-2018, Inversoft Inc., All Rights Reserved
 */

using System;
using System.Collections;
using System.Collections.Generic;
using System.Text;
using System.Net;
using System.Security.Cryptography.X509Certificates;
using System.IO;
using System.Web;

namespace Inversoft.Restify
{
  /**
   * A local interface for logging. If this isn't implemented or specified, the RESTClient will not log.
   */
  public interface Logger
  {
    void Debug(string message, Exception e);
  }

  public class NoOpLogger : Logger
  {
    public void Debug(string message, Exception e)
    {
      // No-op
    }
  }


  /**
  * RESTful WebService call builder. This provides the ability to call RESTful WebServices using a builder pattern to
  * set up all the necessary request information and parse the response.
  */
  public class RESTClient<RS, ERS>
  {
    public readonly IDictionary<string, string> headers = new Dictionary<string, string>();

    public readonly IDictionary<string, List<object>> parameters = new Dictionary<string, List<object>>();

    public readonly StringBuilder url = new StringBuilder();

    public BodyHandler bodyHandler;

    public X509Certificate certificate;

    public ResponseHandler<ERS> errorResponseHandler;

    public Logger logger;

    public HTTPMethod method;

    public int readWriteTimeout = 2000;

    public ResponseHandler<RS> successResponseHandler;

    public int timeout = 2000;

    public IWebProxy webProxy;

    public RESTClient()
    {
      logger = new NoOpLogger();
    }

    public RESTClient(Logger logger)
    {
      this.logger = logger;
    }

    public RESTClient<RS, ERS> Authorization(string key)
    {
      headers["Authorization"] = key; 
      return this;
    }

    public RESTClient<RS, ERS> BasicAuthorization(string username, string password)
    {
      if (username != null && password != null)
      {
        var credentials = username + ":" + password;
        var encoded = Convert.ToBase64String(Encoding.UTF8.GetBytes(credentials));
        headers["Authorization"] = "Basic " + encoded;
      }
      return this;
    }

    public RESTClient<RS, ERS> BodyHandler(BodyHandler bodyHandler)
    {
      this.bodyHandler = bodyHandler;
      return this;
    }

    public RESTClient<RS, ERS> Certificate(X509Certificate certificate)
    {
      this.certificate = certificate;
      return this;
    }

    public RESTClient<RS, ERS> Timeout(int connectTimeout)
    {
      timeout = connectTimeout;
      return this;
    }

    public RESTClient<RS, ERS> Delete()
    {
      method = HTTPMethod.DELETE;
      return this;
    }

    public RESTClient<RS, ERS> ErrorResponseHandler(ResponseHandler<ERS> errorResponseHandler)
    {
      this.errorResponseHandler = errorResponseHandler;
      return this;
    }

    public RESTClient<RS, ERS> Get()
    {
      method = HTTPMethod.GET;
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
        throw new InvalidOperationException(
                                            "You specified a success response type, you must then provide a success response handler.");
      }

      if (typeof(ERS) != typeof(RESTVoid) && errorResponseHandler == null)
      {
        throw new InvalidOperationException(
                                            "You specified an error response type, you must then provide an error response handler.");
      }

      var response = new ClientResponse<RS, ERS>();
      response.request = (bodyHandler != null) ? bodyHandler.GetBodyObject() : null;
      response.method = method;

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
              url.Append(HttpUtility.UrlEncode(pair.Key, Encoding.UTF8)).Append("=")
                 .Append(HttpUtility.UrlEncode(value.ToString(), Encoding.UTF8)).Append("&");
            }
          }

          url.Remove(url.Length - 1, 1);
        }

        response.url = new Uri(url.ToString());
        request = (HttpWebRequest) WebRequest.Create(response.url);

        if (webProxy != null)
        {
          request.Proxy = webProxy;
        }

        // Handle SSL certificates
        if (response.url.Scheme.ToLower().Equals("https") && certificate != null)
        {
          ServicePointManager.CertificatePolicy = new CertPolicy();
          request.ClientCertificates.Add(certificate);
        }

        request.Timeout = timeout;
        request.ReadWriteTimeout = readWriteTimeout;
        request.Method = method.ToString();

        if (headers.Count > 0)
        {
          foreach (var header in headers)
          {
            request.Headers[header.Key] = header.Value;
          }
        }

        if (bodyHandler != null)
        {
          bodyHandler.SetHeaders(request);
        }

        if (bodyHandler != null)
        {
          using (var stream = request.GetRequestStream())
          {
            bodyHandler.Accept(stream);
            stream.Flush();
          }
        }
      }

      catch (Exception e)
      {
        logger.Debug("Error calling REST WebService at [" + url + "]", e);
        response.status = -1;
        response.exception = e;
        return response;
      }

      try
      {
        using (var resp = (HttpWebResponse) request.GetResponse())
        {
          response.status = (int) resp.StatusCode;
          if (successResponseHandler == null)
          {
            return response;
          }

          try
          {
            using (var str = resp.GetResponseStream())
            {
              response.successResponse = successResponseHandler.Apply(str);
            }
          }
          catch (Exception e)
          {
            logger.Debug("Error calling REST WebService at [" + url + "]", e);
            response.status = -1;
            response.exception = e;
            return response;
          }
        }
      }
      catch (WebException e)
      {
        response.status = -1;

        // The response will be null if the server couldn't be contacted, the connection broke, or
        // the communication with the server failed
        if (e.Response == null)
        {
          response.exception = e;
          return response;
        }

        using (var webResp = e.Response)
        {
          var httpResp = (HttpWebResponse) webResp;
          response.status = (int) httpResp.StatusCode;

          if (errorResponseHandler == null)
          {
            return response;
          }

          try
          {
            using (var str = httpResp.GetResponseStream())
            {
              response.errorResponse = errorResponseHandler.Apply(str);
            }
          }
          catch (Exception ex)
          {
            logger.Debug("Error calling REST WebService at [" + url + "]", e);
            response.exception = ex;
            return response;
          }
        }
      }

      return response;
    }

    public RESTClient<RS, ERS> Header(string name, string value)
    {
      headers[name] = value;
      return this;
    }

    public RESTClient<RS, ERS> Headers(Dictionary<string, string> newHeaders)
    {
      foreach (var header in newHeaders)
      {
        headers[header.Key] = header.Value;
      }

      return this;
    }

    public RESTClient<RS, ERS> Patch()
    {
      method = HTTPMethod.PATCH;
      return this;
    }

    public RESTClient<RS, ERS> Post()
    {
      method = HTTPMethod.POST;
      return this;
    }

    public RESTClient<RS, ERS> Proxy(IWebProxy webProxy)
    {
      this.webProxy = webProxy;
      return this;
    }

    public RESTClient<RS, ERS> Put()
    {
      method = HTTPMethod.PUT;
      return this;
    }

    public RESTClient<RS, ERS> ReadWriteTimeout(int readTimeout)
    {
      readWriteTimeout = readTimeout;
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
      if (this.url.Length != 0)
      {
        this.url.Remove(0, this.url.Length - 1);
      }

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
    public RESTClient<RS, ERS> UrlParameter(string name, object value)
    {
      if (value == null)
      {
        return this;
      }

      List<object> values;
      parameters.TryGetValue(name, out values);

      if (values == null)
      {
        values = new List<object>();
        parameters[name] = values;
      }

      if (value is DateTime)
      {
        values.Add(((DateTime) value).ToUniversalTime().Subtract(new DateTime(1970, 1, 1, 0, 0, 0, DateTimeKind.Utc))
                                     .TotalMilliseconds);
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
    public RESTClient<RS, ERS> UrlSegment(object value)
    {
      if (value != null)
      {
        if (url[url.Length - 1] != '/')
        {
          url.Append('/');
        }
        url.Append(value);
      }
      return this;
    }
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
    DELETE,
    PATCH
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
     * @return The unprocessed body object. This might be a JSON object, a Map of key value pairs or a String. By default, this returns
     * null.
     */
    object GetBodyObject();

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

  public sealed class CertPolicy : ICertificatePolicy
  {
    public bool CheckValidationResult(ServicePoint servicePoint, X509Certificate certificate, WebRequest webRequest,
                                      int certificateProblem)
    {
      return true;
    }
  }
}