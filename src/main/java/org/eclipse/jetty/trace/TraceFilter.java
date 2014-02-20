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

import java.io.File;
import java.io.IOException;

import javax.servlet.AsyncContext;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class TraceFilter implements Filter
{
    private File traceDir;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException
    {
        traceDir = new File(System.getProperty("java.io.tmpdir"));
        String tempdir = filterConfig.getInitParameter("trace-dir");
        if (tempdir != null)
        {
            traceDir = new File(tempdir);
        }
        if (!traceDir.exists())
        {
            throw new ServletException("'trace-dir' does not exist: " + traceDir);
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException
    {
        // only interested in http requests
        if ((request instanceof HttpServletRequest) && (response instanceof HttpServletResponse))
        {
            HttpServletRequest httpReq = (HttpServletRequest)request;
            HttpServletResponse httpResp = (HttpServletResponse)response;

            // allow skipping / excluding by details in the request
            if (isExcluded(httpReq))
            {
                // pass request through, without tracing
                chain.doFilter(request,response);
            }
            else
            {
                // trace the request / response
                TraceFile tracer = newTracer();
                TraceServletRequest traceReq = new TraceServletRequest(httpReq,tracer);
                TraceServletResponse traceResp = new TraceServletResponse(httpResp,tracer);
                chain.doFilter(traceReq,traceResp);
                if (httpReq.isAsyncStarted())
                {
                    AsyncContext async = httpReq.getAsyncContext();
                    async.addListener(tracer);
                }
                else
                {
                    tracer.close();
                }
            }
        }
        else
        {
            // pass request through unchanged
            chain.doFilter(request,response);
        }
    }

    private boolean isExcluded(HttpServletRequest httpReq)
    {
        // TODO add support to exclude trace behavior on specific requests
        // return false to trace everything
        return false;
    }

    private TraceFile newTracer() throws IOException
    {
        File outputFile = File.createTempFile("tracer-",".log",traceDir);
        return new TraceFile(outputFile);
    }

    @Override
    public void destroy()
    {
    }
}
