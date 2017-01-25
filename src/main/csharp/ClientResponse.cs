/*
 * Copyright (c) 2016, Inversoft Inc., All Rights Reserved
 */

using System;

namespace Inversoft.Restify
{
  public class ClientResponse<T, U>
  {
    public U errorResponse;

    public Exception exception;

    public HTTPMethod method;

    public object request;

    public int status;

    public T successResponse;

    public Uri url;

    public bool WasSuccessful()
    {
      return status >= 200 && status <= 299 && exception == null;
    }
  }
}