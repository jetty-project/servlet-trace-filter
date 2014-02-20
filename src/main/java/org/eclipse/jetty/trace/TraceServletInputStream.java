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

import java.io.IOException;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;

public class TraceServletInputStream extends ServletInputStream
{
    private final ServletInputStream delegate;
    private final TraceFile tracer;

    public TraceServletInputStream(ServletInputStream stream, TraceFile tracer)
    {
        this.delegate = stream;
        this.tracer = tracer;
    }

    @Override
    public boolean isFinished()
    {
        return delegate.isFinished();
    }

    @Override
    public boolean isReady()
    {
        return delegate.isReady();
    }

    @Override
    public void setReadListener(ReadListener readListener)
    {
        delegate.setReadListener(readListener);
    }

    @Override
    public int read() throws IOException
    {
        try
        {
            int ret = delegate.read();
            if (ret != (-1))
            {
                tracer.logRequestContentByte((byte)ret);
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

    @Override
    public void close() throws IOException
    {
        tracer.logRequestContentClose();
        try
        {
            delegate.close();
            tracer.log("Closed: %s",delegate);
        }
        catch (IOException e)
        {
            tracer.log(e);
            throw e;
        }
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException
    {
        try
        {
            int ret = delegate.read(b,off,len);
            if (ret != (-1))
            {
                tracer.logRequestContentByte(b,off,ret);
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
