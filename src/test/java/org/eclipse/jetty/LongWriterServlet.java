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

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.toolchain.test.IO;
import org.eclipse.jetty.toolchain.test.MavenTestingUtils;

@WebServlet(name = "LongChar", urlPatterns = { "/long-char" })
@SuppressWarnings("serial")
public class LongWriterServlet extends HttpServlet
{
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        resp.setContentType("text/plain");
        Writer writer = resp.getWriter();
        File quotes = MavenTestingUtils.getTestResourceFile("quotes.txt");
        try (FileReader reader = new FileReader(quotes))
        {
            IO.copy(reader,writer);
        }
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

            resp.setContentType("text/plain");
            resp.getWriter().printf("Read %,d characters",length);
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
            resp.setContentType("text/plain");
            resp.getWriter().printf("Read %,d bytes",length);
        }
    }
}
