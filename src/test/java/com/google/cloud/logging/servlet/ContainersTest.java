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

package com.google.cloud.logging.servlet;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.cloud.logging.ContextHandler;
import com.google.cloud.logging.servlet.container.JettyContainer;
import com.google.cloud.logging.servlet.container.ServletContainer;
import com.google.cloud.logging.servlet.container.TomcatContainer;
import com.google.cloud.logging.servlet.container.UndertowContainer;
import java.net.MalformedURLException;
import java.net.URL;
import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Set of unit tests to validate behavior of the servlet filter in popular Web servers: Tomcat,
 * Jetty, Undertow
 */
public class ContainersTest {
  private static final String MOCKED_URL = "http://localhost:8080/";
  private static final String MOCKED_ASYNC_URL = MOCKED_URL + "async";
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
      // TODO: check context
    }
  }

  /** Servlet to dispatch another request asynchronously and then to resume own operation */
  // @SuppressWarnings("serial")
  // public static class AsyncServlet extends HttpServlet {
  //   @Override
  //   protected void doGet(HttpServletRequest req, HttpServletResponse resp)
  //       throws MalformedURLException {
  //     // TODO: check context before starting async
  //     AsyncContext asyncContext = req.startAsync();
  //     asyncContext.dispatch(new URL(MOCKED_URL).getPath());
  //     // TODO: check context after async op
  //   }
  // }

  /** Test regular request that is executed synchronously in the request serving thread. */
  @ValueSource(classes = {JettyContainer.class, TomcatContainer.class, UndertowContainer.class})
  @ParameterizedTest
  public void testRequest(Class<? extends ServletContainer> containerClass) throws Exception {
    container = containerClass.getDeclaredConstructor().newInstance();
    container.addServletContainerInitializer(ContextCaptureInitializer.class);
    container.addServlet(TestServlet.class, new URL(MOCKED_URL).getPath());
    container.start();

    ContentResponse response = client.GET(MOCKED_URL);
    assertEquals(response.getStatus(), 200);
  }

  /** Test regular request that is executed synchronously in the request serving thread. */
  // @ValueSource(classes = {JettyContainer.class, TomcatContainer.class, UndertowContainer.class})
  // @ParameterizedTest
  // public void testAsyncRequest(Class<? extends ServletContainer> containerClass) throws Exception {
  //   container = containerClass.getDeclaredConstructor().newInstance();
  //   container.addServletContainerInitializer(ContextCaptureInitializer.class);
  //   container.addServlet(TestServlet.class, "/async/test");
  //   container.addServlet(AsyncServlet.class, new URL(MOCKED_ASYNC_URL).getPath());
  //   container.start();
  //   ContentResponse response = client.GET(MOCKED_ASYNC_URL);
  //   assertEquals(response.getStatus(), 200);
  // }
}
