package org.dockbox.hartshorn.web;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.message.BasicHeader;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class HttpMethodEndpointTests extends RestIntegrationTest {

    @Test
    void testGet() throws IOException, InterruptedException {
        final CloseableHttpResponse response = this.request("/get", HttpMethod.GET, "");
        RestAssert.assertStatus(HttpStatus.OK, response);
        RestAssert.assertBody("JUnit GET", response);
    }

    @Test
    public void testPostWithBody() throws IOException, InterruptedException {
        final CloseableHttpResponse response = this.request("/post", HttpMethod.POST, "Hello world!");
        RestAssert.assertStatus(HttpStatus.OK, response);
        RestAssert.assertBody("Hello world!", response);
    }

    @Test
    void testGetWithHeader() throws IOException, InterruptedException {
        final CloseableHttpResponse response = this.request("/header", HttpMethod.GET, "", new BasicHeader("http-demo", "Hello headers!"));
        RestAssert.assertStatus(HttpStatus.OK, response);
        RestAssert.assertBody("Hello headers!", response);
    }

    @Test
    void testGetWithInject() throws IOException, InterruptedException {
        final CloseableHttpResponse response = this.request("/inject", HttpMethod.GET, "");
        RestAssert.assertStatus(HttpStatus.OK, response);
        RestAssert.assertBody("true", response);
    }
}
