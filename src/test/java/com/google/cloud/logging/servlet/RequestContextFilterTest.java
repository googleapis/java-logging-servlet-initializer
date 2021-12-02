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

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.anyString;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.google.cloud.logging.Context;
import com.google.cloud.logging.ContextHandler;
import com.google.cloud.logging.HttpRequest;
import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.easymock.IAnswer;
import org.junit.Test;

@SuppressWarnings("serial")
public class RequestContextFilterTest extends RequestContextFilter {
  private static final String W3C_TRACEPARENT_HEADER = "traceparent";
  private static final String CLOUD_TRACE_HEADER = "x-cloud-trace-context";
  private static final String MOCKED_REFERER = "https://referer.mocked";
  private static final String MOCKED_USER_AGENT = "Mozilla/5.0 (Mocked)";
  private static final String MOCKED_REMOTE_ADDR = "10.0.0.1";
  private static final String MOCKED_LOCAL_ADDR = "10.0.0.2";
  private static final String MOCKED_METHOD = "GET";
  private static final Long MOCKED_CONTENT_LENGTH = 100L;
  private static final String MOCKED_REQUEST_URL = "https://mocked.url";
  private static final String MOCKED_QUERY = "mocked=true";
  private static final String MOCKED_REQUEST_FULL_URL = MOCKED_REQUEST_URL + "?" + MOCKED_QUERY;
  private static final Integer MOCKED_STATUS = 300;
  private static final Integer MOCKED_BUFFER_SIZE = 100;
  private static final String MOCKED_CLOUD_TRACE_ID = "mocked_cloud_trace";
  private static final String MOCKED_CLOUD_SPAN_ID = "mocked_cloud_span";
  private static final String MOCKED_W3C_TRACE_ID = "mocked_cloud_trace";
  private static final String MOCKED_W3C_SPAN_ID = "mocked_cloud_span";

  private static final HttpRequest ExpectedHttpRequest = HttpRequest.newBuilder().setReferer(MOCKED_REFERER)
      .setRemoteIp(MOCKED_REMOTE_ADDR).setRequestMethod(HttpRequest.RequestMethod.valueOf(MOCKED_METHOD))
      .setRequestSize(MOCKED_CONTENT_LENGTH).setRequestUrl(MOCKED_REQUEST_FULL_URL).setResponseSize(MOCKED_BUFFER_SIZE)
      .setServerIp(MOCKED_LOCAL_ADDR).setStatus(MOCKED_STATUS).setUserAgent(MOCKED_USER_AGENT).build();
  private final ContextHandler contextHandler = new ContextHandler();

  @Test
  public void testContextCleanup() throws IOException, ServletException {
    HttpServletRequest req = mockServletRequest();
    HttpServletResponse resp = mockServletResponse();
    FilterChain chain = mockFilterChain(null);
    replay(req, resp, chain);

    Context contextBeforeFilter = contextHandler.getCurrentContext();
    doFilter(req, resp, chain);
    Context contextAfterFilter = contextHandler.getCurrentContext();

    assertEquals(contextBeforeFilter, contextAfterFilter);
  }

  @Test
  public void testParsingRequestAndResponseData() throws IOException, ServletException {
    HttpServletRequest req = mockServletRequest();
    HttpServletResponse resp = mockServletResponse();
    final Context[] context = new Context[1];
    FilterChain chain = mockFilterChain(() -> {
      context[0] = contextHandler.getCurrentContext();
      return null;
    });
    replay(req, resp, chain);

    doFilter(req, resp, chain);

    assertEquals(ExpectedHttpRequest, context[0].getHttpRequest());
    assertNull(context[0].getTraceId());
    assertNull(context[0].getSpanId());
  }

  @Test
  public void testParsingW3CTracingHeader() throws IOException, ServletException {
    HttpServletRequest req = mockServletRequest();
    HttpServletResponse resp = mockServletResponse();
    final Context[] context = new Context[1];
    FilterChain chain = mockFilterChain(() -> {
      context[0] = contextHandler.getCurrentContext();
      return null;
    });
    expect(req.getHeader(W3C_TRACEPARENT_HEADER)).andReturn(TraceHeaderData.W3C_TRACE_HEADER.HeaderValue);
    replay(req, resp, chain);

    doFilter(req, resp, chain);

    assertEquals(ExpectedHttpRequest, context[0].getHttpRequest());
    assertEquals(TraceHeaderData.W3C_TRACE_HEADER.TraceId, context[0].getTraceId());
    assertEquals(TraceHeaderData.W3C_TRACE_HEADER.SpanId, context[0].getSpanId());
  }

  @Test
  public void testParsingCloudTracingHeader() throws IOException, ServletException {
    HttpServletRequest req = mockServletRequest();
    HttpServletResponse resp = mockServletResponse();
    final Context[] context = new Context[1];
    FilterChain chain = mockFilterChain(() -> {
      context[0] = contextHandler.getCurrentContext();
      return null;
    });
    expect(req.getHeader(CLOUD_TRACE_HEADER)).andReturn(TraceHeaderData.CLOUD_TRACE_HEADER.HeaderValue);
    replay(req, resp, chain);

    doFilter(req, resp, chain);

    assertEquals(ExpectedHttpRequest, context[0].getHttpRequest());
    assertEquals(TraceHeaderData.CLOUD_TRACE_HEADER.TraceId, context[0].getTraceId());
    assertEquals(TraceHeaderData.CLOUD_TRACE_HEADER.SpanId, context[0].getSpanId());
  }

  @Test
  public void testParsingW3CAndCloudTracingHeaders() throws IOException, ServletException {
    HttpServletRequest req = mockServletRequest();
    HttpServletResponse resp = mockServletResponse();
    final Context[] context = new Context[1];
    FilterChain chain = mockFilterChain(() -> {
      context[0] = contextHandler.getCurrentContext();
      return null;
    });
    expect(req.getHeader(W3C_TRACEPARENT_HEADER)).andReturn(TraceHeaderData.W3C_TRACE_HEADER.HeaderValue);
    expect(req.getHeader(CLOUD_TRACE_HEADER)).andReturn(TraceHeaderData.CLOUD_TRACE_HEADER.HeaderValue);
    replay(req, resp, chain);

    doFilter(req, resp, chain);

    assertEquals(ExpectedHttpRequest, context[0].getHttpRequest());
    assertEquals(TraceHeaderData.W3C_TRACE_HEADER.TraceId, context[0].getTraceId());
    assertEquals(TraceHeaderData.W3C_TRACE_HEADER.SpanId, context[0].getSpanId());
  }

  private HttpServletRequest mockServletRequest() {
    // have to use create*Nice*Mock() here to return null for unexpected calls to getHeader()
    // so tests can provide additional mocks of the getHeader() for specific headers later
    HttpServletRequest request = createNiceMock(HttpServletRequest.class);
    expect(request.getHeader("referer")).andReturn(MOCKED_REFERER);
    expect(request.getHeader("user-agent")).andReturn(MOCKED_USER_AGENT);
    expect(request.getRemoteAddr()).andReturn(MOCKED_REMOTE_ADDR);
    expect(request.getMethod()).andReturn(MOCKED_METHOD);
    expect(request.getQueryString()).andReturn(MOCKED_QUERY);
    expect(request.getRequestURL()).andReturn(new StringBuffer(MOCKED_REQUEST_URL));
    expect(request.getContentLengthLong()).andReturn(MOCKED_CONTENT_LENGTH);
    expect(request.getLocalAddr()).andReturn(MOCKED_LOCAL_ADDR);
    return request;
  }

  private HttpServletResponse mockServletResponse() {
    HttpServletResponse response = createMock(HttpServletResponse.class);
    expect(response.getStatus()).andReturn(MOCKED_STATUS);
    expect(response.getBufferSize()).andReturn(MOCKED_BUFFER_SIZE);
    return response;
  }

  private FilterChain mockFilterChain(IAnswer<? extends Object> doFilterAnswer) throws IOException, ServletException {
    FilterChain chain = createMock(FilterChain.class);
    chain.doFilter(anyObject(), anyObject());
    if (doFilterAnswer != null) {
      expectLastCall().andAnswer(doFilterAnswer);
    } else {
      expectLastCall();
    }
    return chain;
  }

  private enum TraceHeaderData {
    W3C_TRACE_HEADER("00-"+MOCKED_W3C_TRACE_ID+"-"+MOCKED_W3C_SPAN_ID+"-flags", MOCKED_W3C_TRACE_ID, MOCKED_W3C_SPAN_ID),
    CLOUD_TRACE_HEADER(MOCKED_CLOUD_TRACE_ID+"/"+MOCKED_CLOUD_SPAN_ID+";o=TRACE_TRUE", MOCKED_CLOUD_TRACE_ID, MOCKED_CLOUD_SPAN_ID);

    public final String HeaderValue;
    public final String TraceId;
    public final String SpanId;

    TraceHeaderData(String headerValue, String traceId, String spanId) {
      this.HeaderValue = headerValue;
      this.TraceId = traceId;
      this.SpanId = spanId;
    }
  }
}
