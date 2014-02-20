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

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.EnumSet;

import javax.servlet.DispatcherType;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.toolchain.test.FS;
import org.eclipse.jetty.toolchain.test.MavenTestingUtils;
import org.eclipse.jetty.trace.TraceFilter;
import org.eclipse.jetty.util.IO;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class TraceTest
{
    private static Server server;
    private static URI serverURI;

    @BeforeClass
    public static void startServer() throws Exception
    {
        server = new Server();
        ServerConnector connector = new ServerConnector(server);
        connector.setPort(0); // let os pick port
        server.addConnector(connector);

        // Setup a servlet context
        ServletContextHandler context = new ServletContextHandler();
        context.setContextPath("/");
        server.setHandler(context);

        // Add a servlet
        ServletHolder holderHello = new ServletHolder("hello",HelloWorldServlet.class);
        context.addServlet(holderHello,"/hello");

        // Add the trace filter
        FilterHolder holderTrace = new FilterHolder(TraceFilter.class);
        File traceDir = MavenTestingUtils.getTargetTestingDir("traces");
        FS.ensureDirExists(traceDir);
        holderTrace.setInitParameter("trace-dir",traceDir.getAbsolutePath());
        context.addFilter(holderTrace,"/*",EnumSet.of(DispatcherType.REQUEST,DispatcherType.INCLUDE,DispatcherType.FORWARD));

        server.start(); // start server on its own thread

        String host = connector.getHost();
        if (host == null)
        {
            host = "localhost";
        }
        int port = connector.getLocalPort();
        serverURI = new URI("http://" + host + ":" + port);
    }

    @Test
    public void makeClientRequest() throws IOException
    {
        URL url = serverURI.resolve("/hello").toURL();
        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
        int status = conn.getResponseCode();
        assertThat("response code",status,is(HttpURLConnection.HTTP_OK));
        InputStream stream = conn.getInputStream();
        String response = IO.toString(stream);
        assertThat("response",response,containsString("Hello World"));
        System.out.printf("Response: %s%n",response);
    }

    @AfterClass
    public static void stopServer() throws Exception
    {
        server.stop();
    }
}
