/**
 * Copyright (C) 2016 David Clark
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package groovyx.net.http;

import okhttp3.mockwebserver.MockResponse;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * Demonstration (and test) of using {@link HttpBuilder} via standard Java.
 */
@Ignore
public class JavaUsageTest {

    @Rule
    public MockWebServerRule serverRule = new MockWebServerRule();

    private static final String CONTENT = "This is CONTENT!!!";
    private HttpBuilder http;

    @Before
    public void before() {
        http = HttpBuilder.configure(config -> {
            config.getRequest().setUri(serverRule.getServerUrl());
        });
    }

    @Test
    @SuppressWarnings({"unchecked", "Convert2Lambda"})
    public void head_request() throws Exception {
        serverRule.dispatcher("HEAD","/foo", new MockResponse().setHeader("Content-Type", "text/plain"));

        List<FromServer.Header> headers = (List<FromServer.Header>) http.head(List.class, config -> {
            config.getRequest().getUri().setPath("/foo");
            config.getResponse().success(new BiFunction<FromServer, Object, Object>() {
                @Override
                public Object apply(final FromServer from, final Object o) {
                    assertFalse(from.getHasBody());
                    return from.getHeaders();
                }
            });
        });

        assertEquals(2, headers.size());
        assertEquals(headers.get(0).toString(), "Content-Length: 0");
        assertEquals(headers.get(1).toString(), "Content-Type: text/plain");

        CompletableFuture<List> future = http.headAsync(List.class, config -> {
            config.getRequest().getUri().setPath("/foo");
            config.getResponse().success((from, o) -> {
                assertFalse(from.getHasBody());
                return from.getHeaders();
            });
        });

        headers = (List<FromServer.Header>) future.get();

        assertEquals(2, headers.size());
        assertEquals(headers.get(0).toString(), "Content-Length: 0");
        assertEquals(headers.get(1).toString(), "Content-Type: text/plain");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void head_request_with_raw_function() throws Exception {
        serverRule.dispatcher("HEAD","/foo", new MockResponse().setHeader("Content-Type", "text/plain"));

        BiFunction<FromServer, Object, Object> successFunction = (from, body) -> {
            assertFalse(from.getHasBody());
            return from.getHeaders();
        };

        List<FromServer.Header> headers = (List<FromServer.Header>) http.head(List.class, config -> {
            config.getRequest().getUri().setPath("/foo");
            config.getResponse().success(successFunction);
        });

        assertEquals(2, headers.size());
        assertEquals(headers.get(0).toString(), "Content-Length: 0");
        assertEquals(headers.get(1).toString(), "Content-Type: text/plain");
    }

    @Test
    public void get_request() throws Exception {
        serverRule.dispatcher("GET","/foo", new MockResponse().setHeader("Content-Type", "text/plain").setBody(CONTENT));

        String result = http.get(String.class, config -> {
            config.getRequest().getUri().setPath("/foo");
        });

        assertEquals(result, CONTENT);

        CompletableFuture<String> future = http.getAsync(String.class, config -> {
            config.getRequest().getUri().setPath("/foo");
        });

        assertEquals(future.get(), CONTENT);
    }

    @Test
    public void post_request() throws Exception {
        serverRule.dispatcher("POST","/foo", new MockResponse().setHeader("Content-Type", "text/plain").setBody(CONTENT));

        String result = http.post(String.class, config -> {
            config.getRequest().getUri().setPath("/foo");
            config.getRequest().setBody(CONTENT);
            config.getRequest().setContentType("text/plain");
        });

        assertEquals(result, CONTENT);

        CompletableFuture<String> future = http.postAsync(String.class, config -> {
            config.getRequest().getUri().setPath("/foo");
            config.getRequest().setBody(CONTENT);
            config.getRequest().setContentType("text/plain");
        });

        assertEquals(future.get(), CONTENT);
    }

    @Test
    public void put_request() throws Exception {
        serverRule.dispatcher("PUT","/foo", new MockResponse().setHeader("Content-Type", "text/plain").setBody(CONTENT));

        String result = http.put(String.class, config -> {
            config.getRequest().getUri().setPath("/foo");
            config.getRequest().setBody(CONTENT);
            config.getRequest().setContentType("text/plain");
        });

        assertEquals(result, CONTENT);

        CompletableFuture<String> future = http.putAsync(String.class, config -> {
            config.getRequest().getUri().setPath("/foo");
            config.getRequest().setBody(CONTENT);
            config.getRequest().setContentType("text/plain");
        });

        assertEquals(future.get(), CONTENT);
    }

    @Test
    public void delete_request() throws Exception {
        serverRule.dispatcher("DELETE","/foo", new MockResponse().setHeader("Content-Type", "text/plain").setBody(CONTENT));

        String result = http.delete(String.class, config -> {
            config.getRequest().getUri().setPath("/foo");
        });

        assertEquals(result, CONTENT);

        CompletableFuture<String> future = http.deleteAsync(String.class, config -> {
            config.getRequest().getUri().setPath("/foo");
        });

        assertEquals(future.get(), CONTENT);
    }
}