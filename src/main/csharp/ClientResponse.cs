/*
 * Copyright (c) 2016, Inversoft Inc., All Rights Reserved
 */
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace Com.Inversoft.Rest
{
    public class ClientResponse<T, U>
    {
        public U errorResponse;

        public Exception exception;

        public int status;

        public T successResponse;

        public bool WasSuccessful()
        {
            return status >= 200 && status <= 299 && exception == null;
        }
    }
}
