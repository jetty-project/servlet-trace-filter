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
import java.io.PrintWriter;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

public class TraceServletResponse extends HttpServletResponseWrapper
{
    private final TraceFile tracer;
    private TraceServletOutputStream stream;
    private TraceServletWriter writer;

    public TraceServletResponse(HttpServletResponse response, TraceFile tracer)
    {
        super(response);
        this.tracer = tracer;
        // You might be tempted to log the response headers here
        // but don't, as the response is not yet committed.
        // As strange as it sounds, it would be wiser to to
        // write out the response headers after the Response has started.
        this.tracer.setResponse(response);
    }

    @Override
    public void flushBuffer() throws IOException
    {
        tracer.log("Response.flushBuffer()");
        tracer.logResponseFlush();
        super.flushBuffer();
    }

    @Override
    public void reset()
    {
        tracer.log("Response.reset()");
        tracer.logResponseReset();
        super.reset();
    }

    @Override
    public void resetBuffer()
    {
        tracer.log("Response.resetBuffer()");
        tracer.logResponseContentReset();
        super.resetBuffer();
    }

    @Override
    public void sendError(int sc) throws IOException
    {
        tracer.logResponseError(sc,null);
        super.sendError(sc);
    }

    @Override
    public void sendError(int sc, String msg) throws IOException
    {
        tracer.logResponseError(sc,msg);
        super.sendError(sc,msg);
    }

    @Override
    public void sendRedirect(String location) throws IOException
    {
        tracer.logResponseRedirect(location);
        super.sendRedirect(location);
    }
    
    @Override
    public ServletOutputStream getOutputStream() throws IOException
    {
        tracer.log("Get OutputStream");
        if (stream != null)
        {
            return stream;
        }
        // make sure servlet spec is still followed
        if (writer != null)
        {
            throw new IllegalStateException("getWriter() previously called");
        }
        ServletOutputStream delegate = super.getOutputStream();
        this.stream = new TraceServletOutputStream(delegate,tracer);
        return this.stream;
    }

    @Override
    public PrintWriter getWriter() throws IOException
    {
        tracer.log("Get Writer");
        if (writer != null)
        {
            return writer;
        }
        // make sure servlet spec is still followed
        if (stream != null)
        {
            throw new IllegalStateException("getOutputStream() previously called");
        }

        PrintWriter delegate = super.getWriter();
        this.writer = new TraceServletWriter(delegate,tracer);
        return this.writer;
    }
}
