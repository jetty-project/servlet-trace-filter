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

package org.eclipse.jetty;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(name = "ShortChar", urlPatterns = { "/short-char" })
@SuppressWarnings("serial")
public class ShortWriterServlet extends HttpServlet
{
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        resp.setContentType("text/plain");
        resp.getWriter().println("Hello World");
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        if (req.getContentType().contains("text/plain"))
        {
            long length = 0;
            try (Reader reader = req.getReader())
            {
                while (reader.read() != (-1))
                {
                    length++;
                }
            }
            log(String.format("Read %,d characters",length));
            resp.setContentType("text/plain");
            resp.getWriter().println("Hello Client");
        }
        else
        {
            long length = 0;
            try (InputStream stream = req.getInputStream())
            {
                while (stream.read() != (-1))
                {
                    length++;
                }
            }
            log(String.format("Read %,d bytes",length));
            resp.setContentType("text/plain");
            resp.getWriter().println("Hello Client");
        }
    }
}
