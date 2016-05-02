using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace com.inversoft.rest
{
    public class ClientResponse<T, U>
    {
        public U errorResponse;

        public Exception exception;

        public int status;

        public T successResponse;

        public bool wasSuccessful()
        {
            return status >= 200 && status <= 299 && exception == null;
        }
    }
}
