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

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

public class TraceServletRequest extends HttpServletRequestWrapper
{
    private final TraceFile tracer;
    private TraceServletInputStream stream;
    private TraceServletReader reader;

    public TraceServletRequest(HttpServletRequest request, TraceFile tracer)
    {
        super(request);
        this.tracer = tracer;
        this.tracer.logRequestHeaders(request);
    }

    @Override
    public BufferedReader getReader() throws IOException
    {
        tracer.log("Get Reader");
        if (reader != null)
        {
            return reader;
        }
        // make sure servlet spec is still followed
        if (stream != null)
        {
            throw new IllegalStateException("getInputStream() previously called");
        }
        BufferedReader delegate = super.getReader();
        this.reader = new TraceServletReader(delegate,tracer);
        return this.reader;
    }

    @Override
    public ServletInputStream getInputStream() throws IOException
    {
        tracer.log("Get InputStream");
        if (stream != null)
        {
            return stream;
        }
        // make sure servlet spec is still followed
        if (reader != null)
        {
            throw new IllegalStateException("getReader() previously called");
        }
        ServletInputStream delegate = super.getInputStream();
        this.stream = new TraceServletInputStream(delegate,tracer);
        return this.stream;
    }
}
