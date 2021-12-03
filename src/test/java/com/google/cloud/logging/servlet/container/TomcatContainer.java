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

import java.io.File;
import java.util.Collections;
import javax.servlet.Servlet;
import javax.servlet.ServletContainerInitializer;
import org.apache.catalina.Context;
import org.apache.catalina.Wrapper;
import org.apache.catalina.startup.Tomcat;

public class TomcatContainer implements ServletContainer {
  private final Tomcat tomcat;
  private final Context context;

  public TomcatContainer() {
    tomcat = new Tomcat();
    tomcat.setPort(8080);
    tomcat.getConnector();
    context = tomcat.addContext("", new File(".").getAbsolutePath());
  }

  @Override
  public void start() throws Exception {
    tomcat.start();
  }

  @Override
  public void stop() throws Exception {
    tomcat.stop();
    tomcat.destroy();
  }

  @Override
  public void addServlet(Class<? extends Servlet> servletClass, String path) {
    String servletName = servletClass.getName() + "-" + Integer.toHexString(this.hashCode());
    Wrapper wrapper = Tomcat.addServlet(context, servletName, servletClass.getName());
    wrapper.setAsyncSupported(true);
    wrapper.setLoadOnStartup(1);
    context.addServletMappingDecoded(path, servletName);
  }

  @Override
  public void addServletContainerInitializer(Class<? extends ServletContainerInitializer> sciClass)
      throws Exception {
    context.addServletContainerInitializer(
        sciClass.getDeclaredConstructor().newInstance(), Collections.emptySet());
  }
}
