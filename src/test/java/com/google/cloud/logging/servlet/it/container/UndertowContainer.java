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

package com.google.cloud.logging.servlet.it.container;

import static io.undertow.servlet.Servlets.defaultContainer;
import static io.undertow.servlet.Servlets.deployment;
import static io.undertow.servlet.Servlets.servlet;

import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.PathHandler;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.servlet.api.ServletContainerInitializerInfo;
import jakarta.servlet.Servlet;
import jakarta.servlet.ServletContainerInitializer;

import java.util.Collections;

public class UndertowContainer implements ServletContainer {
  private Undertow server;
  private final DeploymentInfo servletBuilder;

  public UndertowContainer() {
    servletBuilder =
        deployment()
            .setClassLoader(UndertowContainer.class.getClassLoader())
            .setContextPath("/")
            .setDeploymentName("test.war");
  }

  @Override
  public void start() throws Exception {
    DeploymentManager manager = defaultContainer().addDeployment(servletBuilder);
    manager.deploy();
    HttpHandler servletHandler = manager.start();
    PathHandler path = Handlers.path(Handlers.redirect("/")).addPrefixPath("/", servletHandler);
    server = Undertow.builder().addHttpListener(8080, "localhost").setHandler(path).build();
    server.start();
  }

  @Override
  public void stop() throws Exception {
    if (server != null) {
      server.stop();
      server = null;
    }
  }

  @Override
  public void addServlet(Class<? extends Servlet> servletClass, String path) {
    servletBuilder.addServlets(
        servlet(servletClass).addMapping(path).setLoadOnStartup(1).setAsyncSupported(true));
  }

  @Override
  public void addServletContainerInitializer(Class<? extends ServletContainerInitializer> sciClass)
      throws Exception {
    servletBuilder.addServletContainerInitializer(
        new ServletContainerInitializerInfo(sciClass, Collections.emptySet()));
  }
}
