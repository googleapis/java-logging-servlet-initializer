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

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.After;
import org.junit.Before;
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
  private static final String MOCKED_W3C_TRACE_ID = "0a0a0a0a0a0a0a0a0a0a0a0a0a0a0a0a";
  private static final String MOCKED_W3C_SPAN_ID = "1b1b1b1b1b1b1b1b";

  private static final HttpRequest ExpectedHttpRequest =
      HttpRequest.newBuilder()
          .setReferer(MOCKED_REFERER)
          .setRemoteIp(MOCKED_REMOTE_ADDR)
          .setRequestMethod(HttpRequest.RequestMethod.valueOf(MOCKED_METHOD))
          .setRequestSize(MOCKED_CONTENT_LENGTH)
          .setRequestUrl(MOCKED_REQUEST_URL)
          .setResponseSize(MOCKED_BUFFER_SIZE)
          .setServerIp(MOCKED_LOCAL_ADDR)
          .setStatus(MOCKED_STATUS)
          .setUserAgent(MOCKED_USER_AGENT)
          .build();
  private final ContextHandler contextHandler = new ContextHandler();
  private HttpServletRequest mockedRequest;
  private HttpServletResponse mockedResponse;
  private FilterChain mockedChain;
  private Context testingContext;

  @Before
  public void setup() {
    mockedRequest = mockServletRequest();
    mockedResponse = mockServletResponse();
    mockedChain = createMock(FilterChain.class);
  }

  @After
  public void cleanup() {
    testingContext = null;
  }

  @Test
  public void testContextCleanup() throws IOException, ServletException {
    mockedChain.doFilter(anyObject(), anyObject());
    expectLastCall();
    replay(mockedRequest, mockedResponse, mockedChain);

    Context contextBeforeFilter = contextHandler.getCurrentContext();
    doFilter(mockedRequest, mockedResponse, mockedChain);
    Context contextAfterFilter = contextHandler.getCurrentContext();

    assertEquals(contextBeforeFilter, contextAfterFilter);
  }

  @Test
  public void testParsingRequestAndResponseData() throws IOException, ServletException {
    interceptCurrentContext();
    replay(mockedRequest, mockedResponse, mockedChain);

    doFilter(mockedRequest, mockedResponse, mockedChain);

    assertEquals(ExpectedHttpRequest, testingContext.getHttpRequest());
    assertNull(testingContext.getTraceId());
    assertNull(testingContext.getSpanId());
  }

  @Test
  public void testParsingRequestWithQueryParams() throws IOException, ServletException {
    HttpRequest expectedHttpRequestWithParams =
        ExpectedHttpRequest.toBuilder().setRequestUrl(MOCKED_REQUEST_FULL_URL).build();
    expect(mockedRequest.getQueryString()).andReturn(MOCKED_QUERY);
    interceptCurrentContext();
    replay(mockedRequest, mockedResponse, mockedChain);

    doFilter(mockedRequest, mockedResponse, mockedChain);

    assertEquals(expectedHttpRequestWithParams, testingContext.getHttpRequest());
  }

  @Test
  public void testParsingW3CTracingHeader() throws IOException, ServletException {
    expect(mockedRequest.getHeader(W3C_TRACEPARENT_HEADER))
        .andReturn(TraceHeaderData.W3C_TRACE_HEADER.HeaderValue);
    interceptCurrentContext();
    replay(mockedRequest, mockedResponse, mockedChain);

    doFilter(mockedRequest, mockedResponse, mockedChain);

    assertEquals(ExpectedHttpRequest, testingContext.getHttpRequest());
    assertEquals(TraceHeaderData.W3C_TRACE_HEADER.TraceId, testingContext.getTraceId());
    assertEquals(TraceHeaderData.W3C_TRACE_HEADER.SpanId, testingContext.getSpanId());
  }

  @Test
  public void testParsingCloudTracingHeader() throws IOException, ServletException {
    expect(mockedRequest.getHeader(CLOUD_TRACE_HEADER))
        .andReturn(TraceHeaderData.CLOUD_TRACE_HEADER.HeaderValue);
    interceptCurrentContext();
    replay(mockedRequest, mockedResponse, mockedChain);

    doFilter(mockedRequest, mockedResponse, mockedChain);

    assertEquals(ExpectedHttpRequest, testingContext.getHttpRequest());
    assertEquals(TraceHeaderData.CLOUD_TRACE_HEADER.TraceId, testingContext.getTraceId());
    assertEquals(TraceHeaderData.CLOUD_TRACE_HEADER.SpanId, testingContext.getSpanId());
  }

  @Test
  public void testParsingW3CAndCloudTracingHeaders() throws IOException, ServletException {
    expect(mockedRequest.getHeader(W3C_TRACEPARENT_HEADER))
        .andReturn(TraceHeaderData.W3C_TRACE_HEADER.HeaderValue);
    expect(mockedRequest.getHeader(CLOUD_TRACE_HEADER))
        .andReturn(TraceHeaderData.CLOUD_TRACE_HEADER.HeaderValue);
    interceptCurrentContext();
    replay(mockedRequest, mockedResponse, mockedChain);

    doFilter(mockedRequest, mockedResponse, mockedChain);

    assertEquals(ExpectedHttpRequest, testingContext.getHttpRequest());
    assertEquals(TraceHeaderData.W3C_TRACE_HEADER.TraceId, testingContext.getTraceId());
    assertEquals(TraceHeaderData.W3C_TRACE_HEADER.SpanId, testingContext.getSpanId());
  }

  private HttpServletRequest mockServletRequest() {
    // have to use create*Nice*Mock() here to return null for unexpected calls to getHeader()
    // so tests can provide additional mocks of the getHeader() for specific headers later
    HttpServletRequest request = createNiceMock(HttpServletRequest.class);
    expect(request.getHeader("referer")).andReturn(MOCKED_REFERER);
    expect(request.getHeader("user-agent")).andReturn(MOCKED_USER_AGENT);
    expect(request.getRemoteAddr()).andReturn(MOCKED_REMOTE_ADDR);
    expect(request.getMethod()).andReturn(MOCKED_METHOD);
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

  private void interceptCurrentContext() throws IOException, ServletException {
    mockedChain.doFilter(anyObject(), anyObject());
    expectLastCall()
        .andAnswer(
            () -> {
              testingContext = contextHandler.getCurrentContext();
              return null;
            });
  }

  private enum TraceHeaderData {
    W3C_TRACE_HEADER(
        "00-" + MOCKED_W3C_TRACE_ID + "-" + MOCKED_W3C_SPAN_ID + "-00",
        MOCKED_W3C_TRACE_ID,
        MOCKED_W3C_SPAN_ID),
    CLOUD_TRACE_HEADER(
        MOCKED_CLOUD_TRACE_ID + "/" + MOCKED_CLOUD_SPAN_ID + ";o=TRACE_TRUE",
        MOCKED_CLOUD_TRACE_ID,
        MOCKED_CLOUD_SPAN_ID);

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
