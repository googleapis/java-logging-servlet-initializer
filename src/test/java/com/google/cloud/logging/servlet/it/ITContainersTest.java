/*
 * Copyright 2021 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.cloud.logging.servlet.it;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.google.cloud.logging.Context;
import com.google.cloud.logging.ContextHandler;
import com.google.cloud.logging.servlet.ContextCaptureInitializer;
import com.google.cloud.logging.servlet.it.container.JettyContainer;
import com.google.cloud.logging.servlet.it.container.ServletContainer;
import com.google.cloud.logging.servlet.it.container.TomcatContainer;
import com.google.cloud.logging.servlet.it.container.UndertowContainer;
import java.net.MalformedURLException;
import java.net.URL;
import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Set of unit tests to validate behavior of the servlet filter in popular Web servers: Tomcat,
 * Jetty, Undertow
 */
public class ITContainersTest {
  private static final String TEST_URL = "http://localhost:8080/";
  private static final String TEST_ASYNC_URL = TEST_URL + "async";
  private static final String TEST_METHOD = "GET";
  private static final String TEST_AGENT = "Mozilla/5.0 (test)";
  private static final String TEST_TRACE_ID = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
  private static final String TEST_SPAN_ID = "bbbbbbbbbbbbbbbb";
  private static final String TRACE_HEADER = "traceparent";
  private static final String TRACE_VALUE = "00-" + TEST_TRACE_ID + "-" + TEST_SPAN_ID + "-00";
  private HttpClient client;
  private ServletContainer container;

  @BeforeEach
  public void setup() throws Exception {
    client = new HttpClient();
    client.start();
  }

  @AfterEach
  public void cleanup() throws Exception {
    client.stop();
    if (container != null) {
      container.stop();
    }
  }

  /** Servlet to process synchronous operation */
  @SuppressWarnings("serial")
  public static class TestServlet extends HttpServlet {
    final ContextHandler contextHandler = new ContextHandler();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
      Context context = contextHandler.getCurrentContext();
      assertNotNull(context);
      assertEquals(TEST_URL, context.getHttpRequest().getRequestUrl());
      assertEquals(TEST_TRACE_ID, context.getTraceId());
      assertEquals(TEST_SPAN_ID, context.getSpanId());
    }
  }

  /** Servlet to dispatch another request asynchronously and then to resume own operation */
  @SuppressWarnings("serial")
  public static class AsyncServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
        throws MalformedURLException {
      AsyncContext asyncContext = req.startAsync();
      asyncContext.dispatch("/");
    }
  }

  /** Test regular request that is executed synchronously in the request serving thread. */
  @ValueSource(classes = {JettyContainer.class, TomcatContainer.class, UndertowContainer.class})
  @ParameterizedTest
  public void testRequest(Class<? extends ServletContainer> containerClass) throws Exception {
    container = containerClass.getDeclaredConstructor().newInstance();
    container.addServletContainerInitializer(ContextCaptureInitializer.class);
    container.addServlet(TestServlet.class, new URL(TEST_URL).getPath());
    container.start();
    Request request = client.newRequest(TEST_URL);
    request.method(TEST_METHOD);
    request.agent(TEST_AGENT);
    request.header(TRACE_HEADER, TRACE_VALUE);
    request.accept("text/html");

    ContentResponse response = request.send();

    assertEquals(response.getStatus(), 200);
  }

  /** Test regular request that is executed synchronously in the request serving thread. */
  @ValueSource(classes = {JettyContainer.class, TomcatContainer.class, UndertowContainer.class})
  @ParameterizedTest
  public void testAsyncRequest(Class<? extends ServletContainer> containerClass) throws Exception {
    container = containerClass.getDeclaredConstructor().newInstance();
    container.addServletContainerInitializer(ContextCaptureInitializer.class);
    container.addServlet(TestServlet.class, "/");
    container.addServlet(AsyncServlet.class, "/async");
    container.start();
    Request request = client.newRequest(TEST_ASYNC_URL);
    request.method(TEST_METHOD);
    request.agent(TEST_AGENT);
    request.header(TRACE_HEADER, TRACE_VALUE);
    request.accept("text/html");

    ContentResponse response = request.send();

    assertEquals(response.getStatus(), 200);
  }
}
