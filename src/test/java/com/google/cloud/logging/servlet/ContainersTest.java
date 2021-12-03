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

import org.eclipse.jetty.client.HttpClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.cloud.logging.servlet.container.ServletContainer;

/**
 * Set of unit tests to validate behavior of the servlet filter in popular Web
 * servers: Tomcat, Jetty, Undertow
 */
public class ContainersTest {
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

    @Test
    public void testRequest() {

    }

    @Test
    public void testAsyncRequest() {
        
    }
}
