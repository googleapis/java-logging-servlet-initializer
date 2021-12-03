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

import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.util.EnumSet;
import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration.Dynamic;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import org.junit.jupiter.api.Test;

public class ContextCaptureInitializerTest {

  /**
   * Validates that {@link ContextCaptureInitializer} registers {@link RequestContextFilter} to be
   * called on all requests.
   */
  @Test
  public void testRegistration() throws ServletException {
    ServletContext mockedServletContext = createStrictMock(ServletContext.class);
    Dynamic mockedDynamic = createStrictMock(Dynamic.class);
    expect(mockedServletContext.addFilter("RequestContextFilter", RequestContextFilter.class))
        .andReturn(mockedDynamic)
        .once();
    mockedDynamic.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), false, "/*");
    expectLastCall().once();
    mockedDynamic.setAsyncSupported(true);
    expectLastCall().once();
    replay(mockedServletContext, mockedDynamic);

    ContextCaptureInitializer inst = new ContextCaptureInitializer();
    inst.onStartup(null, mockedServletContext);

    verify(mockedServletContext, mockedDynamic);
  }
}
