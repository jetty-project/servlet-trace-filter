//
//  ========================================================================
//  Copyright (c) 1995-2014 Mort Bay Consulting Pty. Ltd.
//  ------------------------------------------------------------------------
//  All rights reserved. This program and the accompanying materials
//  are made available under the terms of the Eclipse Public License v1.0
//  and Apache License v2.0 which accompanies this distribution.
//
//      The Eclipse Public License is available at
//      http://www.eclipse.org/legal/epl-v10.html
//
//      The Apache License v2.0 is available at
//      http://www.opensource.org/licenses/apache2.0.php
//
//  You may elect to redistribute this code under either of these licenses.
//  ========================================================================
//

package org.eclipse.jetty.trace;

import java.io.BufferedReader;
import java.io.IOException;

public class TraceServletReader extends BufferedReader
{
    private final BufferedReader delegate;
    private final TraceFile tracer;

    public TraceServletReader(BufferedReader delegate, TraceFile tracer)
    {
        super(delegate);
        this.delegate = delegate;
        this.tracer = tracer;
    }

    @Override
    public void close() throws IOException
    {
        try
        {
            tracer.logRequestContentClose();
            super.close();
            tracer.log("Closed: %s",delegate);
        }
        catch (IOException e)
        {
            tracer.log(e);
            throw e;
        }
    }

    @Override
    public int read() throws IOException
    {
        try
        {
            int ret = super.read();

            if (ret != (-1))
            {
                tracer.logRequestContentChar((char)ret);
            }
            else
            {
                tracer.log("EOF reached on %s",delegate);
            }

            return ret;
        }
        catch (IOException e)
        {
            tracer.log(e);
            throw e;
        }
    }
}
