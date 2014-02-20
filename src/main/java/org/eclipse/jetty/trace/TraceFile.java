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

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class TraceFile implements Closeable, AsyncListener
{
    private static final Logger LOG = Logger.getLogger(TraceFile.class.getName());
    private final File outputFile;
    private final PrintWriter out;
    private final long start;
    private HttpServletResponse response;

    public TraceFile(File outputFile) throws FileNotFoundException
    {
        this.outputFile = outputFile;
        this.out = new PrintWriter(outputFile);
        LOG.log(Level.FINE,"Created Trace: " + outputFile);
        this.start = System.currentTimeMillis();
    }

    @Override
    public void close()
    {
        if (response != null)
        {
            logResponseHeaders();
        }
        long end = System.currentTimeMillis();
        log("Trace completed in %,dms",(end - start));
        this.out.close();
    }

    public File getOutputFile()
    {
        return outputFile;
    }

    public void log(String format, Object... args)
    {
        synchronized (out)
        {
            Calendar now = Calendar.getInstance();
            out.printf("%tF %<tT.%<tL - ",now);
            out.printf(format,args);
            out.println();
        }
    }

    public void log(Throwable t)
    {
        synchronized (out)
        {
            Calendar now = Calendar.getInstance();
            out.printf("%tF %<tT.%<tL - %s%n",now,t.getMessage());
            t.printStackTrace(out);
        }
    }

    public void logRequestContentByte(byte b)
    {
        // TODO Auto-generated method stub
    }

    public void logRequestContentByte(byte[] b, int off, int ret)
    {
        // TODO Auto-generated method stub
    }

    public void logRequestContentChar(char ret)
    {
        // TODO Auto-generated method stub
    }

    public void logRequestContentChar(char[] cbuf, int off, int ret)
    {
        // TODO Auto-generated method stub
    }

    public void logRequestContentClose()
    {
        // TODO Auto-generated method stub
    }

    public void logRequestHeaders(HttpServletRequest httpReq)
    {
        log("Request Headers:");
        synchronized (out)
        {
            out.printf("  (request method): %s%n",httpReq.getMethod());
            out.print("  (request url): ");
            out.print(httpReq.getRequestURL().toString());
            if (httpReq.getQueryString() != null)
            {
                out.print("?");
                out.print(httpReq.getQueryString());
            }
            out.println();
            out.println("  (request headers)");
            Enumeration<String> enames = httpReq.getHeaderNames();
            while (enames.hasMoreElements())
            {
                String name = enames.nextElement();
                out.printf("  %s: %s%n",name,httpReq.getHeader(name));
            }
            Map<String, String[]> params = httpReq.getParameterMap();
            if ((params != null) && (params.size() > 0))
            {
                out.println("  (request parameters)");
                for (Map.Entry<String, String[]> entry : params.entrySet())
                {
                    out.print("  ");
                    out.print(entry.getKey());
                    out.print(" = [");
                    boolean delim = false;
                    for (String value : entry.getValue())
                    {
                        if (delim)
                        {
                            out.print(", ");
                        }
                        out.print(value);
                        delim = true;
                    }
                    out.println("]");
                }
            }
            // TODO: log mime-type parts - httpReq.getPart()
        }
    }

    public void logResponseContentByte(int b)
    {
        // TODO Auto-generated method stub
    }

    public void logResponseContentBytes(byte[] b)
    {
        // TODO Auto-generated method stub
    }

    public void logResponseContentBytes(byte[] b, int off, int len)
    {
        // TODO Auto-generated method stub
    }

    public void logResponseContentChar(char[] buf)
    {
        // TODO Auto-generated method stub
    }

    public void logResponseContentChar(char[] buf, int off, int len)
    {
        // TODO Auto-generated method stub
    }

    public void logResponseContentChar(int c)
    {
        // TODO Auto-generated method stub
    }

    public void logResponseContentChar(String s)
    {
        // TODO Auto-generated method stub
    }

    public void logResponseContentChar(String s, int off, int len)
    {
        // TODO Auto-generated method stub
    }

    public void logResponseContentClose()
    {
        // TODO Auto-generated method stub
    }

    public void logResponseContentReset()
    {
        // TODO Auto-generated method stub
    }

    public void logResponseError(int sc, String msg)
    {
        // TODO Auto-generated method stub
    }

    public void logResponseFlush()
    {
        // TODO Auto-generated method stub
    }

    private void logResponseHeaders()
    {
        log("Response Headers:");
        synchronized (out)
        {
            int status = response.getStatus();
            // log if app set this.
            if (status >= 100)
            {
                out.printf("  (response status code): %d%n",status);
            }
            for (String name : response.getHeaderNames())
            {
                out.printf("  %s: %s%n",name,response.getHeader(name));
            }
        }
    }

    public void logResponseRedirect(String location)
    {
        // TODO Auto-generated method stub
    }

    public void logResponseReset()
    {
        // TODO Auto-generated method stub
    }

    @Override
    public void onComplete(AsyncEvent event) throws IOException
    {
        this.close();
    }

    @Override
    public void onError(AsyncEvent event) throws IOException
    {
    }

    @Override
    public void onStartAsync(AsyncEvent event) throws IOException
    {
    }

    @Override
    public void onTimeout(AsyncEvent event) throws IOException
    {
    }

    public void setResponse(HttpServletResponse response)
    {
        this.response = response;
    }
}
