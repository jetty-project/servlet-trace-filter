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

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;

public class TraceServletOutputStream extends ServletOutputStream
{
    private final ServletOutputStream delegate;
    private final TraceFile tracer;

    public TraceServletOutputStream(ServletOutputStream delegate, TraceFile tracer)
    {
        this.delegate = delegate;
        this.tracer = tracer;
    }

    @Override
    public void flush() throws IOException
    {
        try
        {
            super.flush();
            tracer.log("Flushed: %s",delegate);
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
        try
        {
            tracer.logResponseContentClose();
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
    public boolean isReady()
    {
        return delegate.isReady();
    }

    @Override
    public void setWriteListener(WriteListener writeListener)
    {
        this.delegate.setWriteListener(writeListener);
    }

    @Override
    public void write(byte[] b) throws IOException
    {
        try
        {
            tracer.logResponseContentBytes(b);
            delegate.write(b);
        }
        catch (IOException e)
        {
            tracer.log(e);
            throw e;
        }
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException
    {
        try
        {
            tracer.logResponseContentBytes(b,off,len);
            delegate.write(b,off,len);
        }
        catch (IOException e)
        {
            tracer.log(e);
            throw e;
        }
    }

    @Override
    public void write(int b) throws IOException
    {
        try
        {
            tracer.logResponseContentByte(b);
            delegate.write(b);
        }
        catch (IOException e)
        {
            tracer.log(e);
            throw e;
        }
    }
}
