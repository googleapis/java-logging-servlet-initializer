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

import org.junit.Test;

@SuppressWarnings("serial")
public class RequestContextFilterTest extends RequestContextFilter {

  @Test
  public void testHttpRequestContext() {

  }
  // Test that onDestroy cleans the context
  // Test that onInitialize set the context with the right trace and span and
  // httprequest info
  // - one test for request info
  // - one test for w3c trace
  // - one test for gcp trace
  // - one test for w3c AND gcp trace (w3c should be set)

}
