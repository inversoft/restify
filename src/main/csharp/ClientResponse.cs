/*
 * Copyright (c) 2016, Inversoft Inc., All Rights Reserved
 */
using System;
using System.Net;

namespace Com.Inversoft.Rest
{
    public class ClientResponse<T, U>
    {
        public U errorResponse;

        public Exception exception;

        public Object request;

        public int status;

        public T successResponse;

        public Uri url;

        public bool WasSuccessful()
        {
            return status >= 200 && status <= 299 && exception == null;
        }
    }
}
