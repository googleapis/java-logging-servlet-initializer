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

package com.google.cloud.logging.servlet.container;

import javax.servlet.Servlet;
import javax.servlet.ServletContainerInitializer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

public class JettyContainer implements ServletContainer {

  private final Server server;
  private final ServletContextHandler contextHandler;

  public JettyContainer() {
    server = new Server();
    ServerConnector serverConnector = new ServerConnector(server);
    serverConnector.setPort(8080);
    server.addConnector(serverConnector);

    contextHandler = new ServletContextHandler();
    contextHandler.setContextPath("/");
    server.setHandler(contextHandler);
  }

  @Override
  public void start() throws Exception {
    server.start();
  }

  @Override
  public void stop() throws Exception {
    server.stop();
  }

  @Override
  public void addServlet(Class<? extends Servlet> servletClass, String path) {
    ServletHolder servletHolder = contextHandler.addServlet(servletClass, path);
    servletHolder.setAsyncSupported(true);
    servletHolder.setInitOrder(1);
  }

  @Override
  public void addServletContainerInitializer(Class<? extends ServletContainerInitializer> sciClass)
      throws Exception {
    contextHandler.addBean(
        new ServletContextHandler.Initializer(
            contextHandler, sciClass.getDeclaredConstructor().newInstance()));
  }
}
