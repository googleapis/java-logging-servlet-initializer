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

import com.google.cloud.logging.Context;
import com.google.cloud.logging.ContextHandler;
import com.google.cloud.logging.HttpRequest;
import javax.servlet.ServletRequest;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.http.HttpServletRequest;

// import com.google.cloud.logging.Context.Builder;

public class RequestListener implements ServletRequestListener {
  private final ContextHandler contextHandler = new ContextHandler();

  @Override
  public void requestInitialized(ServletRequestEvent sre) {
    HttpServletRequest req = castToHttpServletRequest(sre.getServletRequest());
    if (req == null) {
      return;
    }
    Context.Builder builder = Context.newBuilder().setRequest(convertToHttpRequest(req));
    String tracingHeader = req.getHeader(Context.W3C_TRACEPARENT_HEADER);
    if (tracingHeader != null) {
      builder.loadW3CTraceParentContext(tracingHeader);
    } else {
      builder.loadW3CTraceParentContext(req.getHeader(Context.CLOUD_TRACE_CONTEXT_HEADER));
    }
    contextHandler.setCurrentContext(builder.build());
  }

  @Override
  public void requestDestroyed(ServletRequestEvent sre) {
    contextHandler.removeCurrentContext();
  }

  private static HttpServletRequest castToHttpServletRequest(ServletRequest req) {
    if (req instanceof HttpServletRequest) {
      return (HttpServletRequest) req;
    }
    return null;
  }

  private static HttpRequest convertToHttpRequest(HttpServletRequest req) {
    return null;
  }
}
