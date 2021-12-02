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
import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class RequestContextFilter extends HttpFilter {
  private static final long serialVersionUID = 1517497440413815384L;
  private static final String CLOUD_TRACE_CONTEXT_HEADER = "x-cloud-trace-context";
  private static final String W3C_TRACEPARENT_HEADER = "traceparent";

  private final ContextHandler contextHandler = new ContextHandler();

  @Override
  protected void doFilter(HttpServletRequest req, HttpServletResponse resp, FilterChain chain)
      throws IOException, ServletException {
    Context oldContext = contextHandler.getCurrentContext();
    try {
      HttpRequest logHttpRequest = generateLogEntryHttpRequest(req, resp);
      Context.Builder builder = Context.newBuilder().setRequest(logHttpRequest);
      String tracingHeader = req.getHeader(W3C_TRACEPARENT_HEADER);
      if (tracingHeader != null) {
        builder.loadW3CTraceParentContext(tracingHeader);
      } else {
        builder.loadCloudTraceContext(req.getHeader(CLOUD_TRACE_CONTEXT_HEADER));
      }
      contextHandler.setCurrentContext(builder.build());
      super.doFilter(req, resp, chain);
    } finally {
      contextHandler.setCurrentContext(oldContext);
    }
  }

  private static HttpRequest generateLogEntryHttpRequest(
      HttpServletRequest req, HttpServletResponse resp) {
    if (req == null) {
      return null;
    }
    HttpRequest.Builder builder = HttpRequest.newBuilder();
    builder
        .setReferer(req.getHeader("referer"))
        .setRemoteIp(req.getRemoteAddr())
        .setRequestMethod(HttpRequest.RequestMethod.valueOf(req.getMethod()))
        .setRequestSize(req.getContentLengthLong())
        .setRequestUrl(req.getRequestURL().append("?").append(req.getQueryString()).toString())
        .setServerIp(req.getLocalAddr())
        .setUserAgent(req.getHeader("user-agent"));
    if (resp != null) {
      builder.setStatus(resp.getStatus()).setResponseSize(resp.getBufferSize());
    }
    return builder.build();
  }
}
