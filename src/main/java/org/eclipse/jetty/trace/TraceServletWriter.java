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

import java.io.PrintWriter;

public class TraceServletWriter extends PrintWriter
{
    private final static String LINESEP = System.lineSeparator();
    private final static int LINESEPLEN = LINESEP.length();
    private final PrintWriter delegate;
    private final TraceFile tracer;

    public TraceServletWriter(PrintWriter delegate, TraceFile tracer)
    {
        super(delegate);
        this.delegate = delegate;
        this.tracer = tracer;
    }

    @Override
    public void close()
    {
        tracer.logResponseContentClose();
        tracer.log("Closed: %s",delegate);
        super.close();
    }

    @Override
    public void flush()
    {
        tracer.log("Flushed: %s",delegate);
        super.flush();
    }

    @Override
    public void println()
    {
        tracer.logResponseContentChar(LINESEP,0,LINESEPLEN);
        super.println();
    }

    @Override
    public void write(char[] buf, int off, int len)
    {
        tracer.logResponseContentChar(buf,off,len);
        super.write(buf,off,len);
    }

    @Override
    public void write(int c)
    {
        tracer.logResponseContentChar(c);
        super.write(c);
    }

    @Override
    public void write(String s, int off, int len)
    {
        tracer.logResponseContentChar(s,off,len);
        super.write(s,off,len);
    }
}
