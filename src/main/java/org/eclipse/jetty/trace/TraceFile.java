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
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * File representing the event details encountered at the filter level.
 */
public class TraceFile implements Closeable, AsyncListener
{
    private class ByteContentLogFormatter
    {
        private final static int MAX_BUF = 16;
        private final static int HEX_DISP_WIDTH = (MAX_BUF + (MAX_BUF * 2) + 1);
        private final String mode;
        private final ByteBuffer buf;
        private long length = 0;
        private boolean closed = false;

        public ByteContentLogFormatter(String mode)
        {
            this.mode = mode;
            this.buf = ByteBuffer.allocate(MAX_BUF);
            log("[" + mode + "] Byte Content");
        }

        public void close()
        {
            if (closed)
            {
                return;
            }
            else
            {
                processBuf(true);
                log(String.format("[%s] Closed :: Seen %,d bytes",mode,length));
            }
            closed = true;
        }

        public void dump(byte b)
        {
            length += 1;
            processBuf(false);
            buf.put(b);
        }

        private synchronized void processBuf(boolean partialOk)
        {
            if (partialOk || (buf.remaining() <= 0))
            {
                buf.flip();
                // time to dump the buffer contents to the log
                StringBuilder hexed = new StringBuilder();
                StringBuilder asciid = new StringBuilder();
                int i = 0;
                while (buf.remaining() > 0)
                {
                    if (i++ == (MAX_BUF / 2))
                    {
                        hexed.append(" ");
                    }
                    byte c = buf.get();
                    hexed.append(String.format("%02X ",c));
                    // only show simple printable chars
                    if ((c >= 0x20) && (c <= 0x7E))
                    {
                        asciid.append((char)c);
                    }
                    else
                    {
                        asciid.append(".");
                    }
                }
                buf.flip();
                log(String.format("[%s] Content:: %-" + HEX_DISP_WIDTH + "s | %s",mode,hexed,asciid));
            }
        }
    }

    private class CharContentLogFormatter
    {
        private final static int MAX_BUF = 128;
        private final String mode;
        private CharBuffer buf;
        private long length = 0;
        private boolean closed = false;

        public CharContentLogFormatter(String mode)
        {
            this.mode = mode;
            log("[" + mode + "] Character Based");
            buf = ByteBuffer.allocate(MAX_BUF).asCharBuffer();
        }

        public void close()
        {
            if (closed)
            {
                return;
            }
            else
            {
                processBuf(true);
                log(String.format("[%s] Closed :: Seen %,d characters",mode,length));
            }
            closed = true;
        }

        public void dump(char c)
        {
            length += 1;
            processBuf(false);
            buf.append(c);
        }

        private synchronized void processBuf(boolean partialOk)
        {
            if (partialOk || (buf.remaining() <= 0))
            {
                buf.flip();
                // time to dump the buffer contents to the log
                StringBuilder line = new StringBuilder();
                line.append('[').append(mode).append("] Content:: ");
                while (buf.remaining() > 0)
                {
                    char c = buf.get();
                    switch (c)
                    {
                        case '\r':
                            line.append("\\r");
                            break;
                        case '\n':
                            line.append("\\n");
                            break;
                        case '\t':
                            line.append("\\t");
                            break;
                        default:
                            line.append(c);
                            break;
                    }
                }
                buf.flip();
                log(line.toString());
            }
        }
    }

    private static final Logger LOG = Logger.getLogger(TraceFile.class.getName());

    private final File outputFile;
    private final PrintWriter out;
    private final long start;
    private HttpServletResponse response;
    private CharContentLogFormatter requestContentCharFormatter;
    private ByteContentLogFormatter requestContentByteFormatter;
    private CharContentLogFormatter responseContentCharFormatter;
    private ByteContentLogFormatter responseContentByteFormatter;

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
        logRequestContentClose();
        logResponseContentClose();
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

    private synchronized ByteContentLogFormatter getRequestContentByteFormatter()
    {
        if (requestContentByteFormatter == null)
        {
            requestContentByteFormatter = new ByteContentLogFormatter("Request");
        }
        return requestContentByteFormatter;
    }

    private synchronized CharContentLogFormatter getRequestContentCharFormatter()
    {
        if (requestContentCharFormatter == null)
        {
            requestContentCharFormatter = new CharContentLogFormatter("Request");
        }
        return requestContentCharFormatter;
    }

    private synchronized ByteContentLogFormatter getResponseContentByteFormatter()
    {
        if (responseContentByteFormatter == null)
        {
            responseContentByteFormatter = new ByteContentLogFormatter("Response");
        }
        return responseContentByteFormatter;
    }

    private synchronized CharContentLogFormatter getResponseContentCharFormatter()
    {
        if (responseContentCharFormatter == null)
        {
            responseContentCharFormatter = new CharContentLogFormatter("Response");
        }
        return responseContentCharFormatter;
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
        getRequestContentByteFormatter().dump(b);
    }

    public void logRequestContentChar(char ret)
    {
        getRequestContentCharFormatter().dump(ret);
    }

    public void logRequestContentClose()
    {
        if (requestContentByteFormatter != null)
        {
            requestContentByteFormatter.close();
        }
        if (requestContentCharFormatter != null)
        {
            requestContentCharFormatter.close();
        }
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
        getResponseContentByteFormatter().dump((byte)(b & 0xff));
    }

    public void logResponseContentChar(char c)
    {
        getResponseContentCharFormatter().dump(c);
    }

    public void logResponseContentChar(char[] cbuf, int off, int len)
    {
        CharContentLogFormatter formatter = getResponseContentCharFormatter();
        for (int i = 0; i < len; i++)
        {
            formatter.dump(cbuf[off + i]);
        }
    }

    public void logResponseContentChar(int c)
    {
        getResponseContentCharFormatter().dump((char)(c & 0xffff));
    }

    public void logResponseContentChar(String s, int off, int len)
    {
        CharContentLogFormatter formatter = getResponseContentCharFormatter();
        for (int i = 0; i < len; i++)
        {
            formatter.dump(s.charAt(off + i));
        }
    }

    public void logResponseContentClose()
    {
        if (responseContentByteFormatter != null)
        {
            responseContentByteFormatter.close();
        }
        if (responseContentCharFormatter != null)
        {
            responseContentCharFormatter.close();
        }
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
