package com.sagarv.webclient;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpMethod;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

public class RequestBuilderCompleteTest {

    private WireMockServer wireMockServer;
    private WebClient webClient;

    @BeforeEach
    public void setUp() {
        wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());
        wireMockServer.start();
        WireMock.configureFor("localhost", wireMockServer.port());

        webClient = WebClient.create("http://localhost:" + wireMockServer.port());
    }

    @AfterEach
    public void tearDown() {
        wireMockServer.stop();
    }

    @ParameterizedTest
    @MethodSource("provideHttpMethods")
    public void testSimpleHttpMethods(HttpMethod method, String endpoint, String expectedBody) {
        stubFor(request(method.name(), urlEqualTo(endpoint))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(expectedBody)));

        RequestBuilder requestBuilder = new RequestBuilder(webClient, method, endpoint);

        StepVerifier.create(requestBuilder.executeReactive(String.class))
                .expectNext(expectedBody)
                .verifyComplete();
    }

    private static Stream<org.junit.jupiter.params.provider.Arguments> provideHttpMethods() {
        return Stream.of(
                org.junit.jupiter.params.provider.Arguments.of(HttpMethod.GET, "/get", "GET Success"),
                org.junit.jupiter.params.provider.Arguments.of(HttpMethod.DELETE, "/delete", "DELETE Success")
                // Add more methods if they fit the straightforward pattern
        );
    }

    @Test
    public void testPostWithBody() {
        stubFor(post(urlEqualTo("/post"))
                .withRequestBody(equalTo("Post Body"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody("POST Success")));

        RequestBuilder requestBuilder = new RequestBuilder(webClient, HttpMethod.POST, "/post")
                .body("Post Body");

        StepVerifier.create(requestBuilder.executeReactive(String.class))
                .expectNext("POST Success")
                .verifyComplete();
    }

    @Test
    public void testJsonResponseMapping() {
        stubFor(get(urlEqualTo("/json"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody("{\"name\":\"John Doe\",\"age\":30}")
                        .withHeader("Content-Type", "application/json")));

        RequestBuilder requestBuilder = new RequestBuilder(webClient, HttpMethod.GET, "/json");

        StepVerifier.create(requestBuilder.executeReactive(Person.class))
                .expectNextMatches(person -> person.getName().equals("John Doe") && person.getAge() == 30)
                .verifyComplete();
    }

    @Test
    public void testByteResponse() {
        byte[] byteArray = "Byte Data".getBytes(StandardCharsets.UTF_8);
        stubFor(get(urlEqualTo("/bytes"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(byteArray)));

        RequestBuilder requestBuilder = new RequestBuilder(webClient, HttpMethod.GET, "/bytes");

        StepVerifier.create(requestBuilder.executeReactive(byte[].class))
                .expectNextMatches(response -> new String(response, StandardCharsets.UTF_8).equals("Byte Data"))
                .verifyComplete();
    }

    @Test
    public void testXmlResponseHandlingAsString() {
        stubFor(get(urlEqualTo("/xml"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody("<person><name>John Doe</name><age>30</age></person>")
                        .withHeader("Content-Type", "application/xml")));

        RequestBuilder requestBuilder = new RequestBuilder(webClient, HttpMethod.GET, "/xml");

        StepVerifier.create(requestBuilder.executeReactive(String.class))
                .expectNext("<person><name>John Doe</name><age>30</age></person>")
                .verifyComplete();
    }

    @Test
    public void testEmptyResponse() {
        stubFor(get(urlEqualTo("/empty"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody("")));

        RequestBuilder requestBuilder = new RequestBuilder(webClient, HttpMethod.GET, "/empty");

        StepVerifier.create(requestBuilder.executeReactive(String.class))
                .verifyComplete(); // Expect the Mono to complete without emitting any items
    }

    @Test
    public void testInvalidJsonResponseHandling() {
        stubFor(get(urlEqualTo("/invalidJson"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody("Invalid JSON")));

        RequestBuilder requestBuilder = new RequestBuilder(webClient, HttpMethod.GET, "/invalidJson");

        StepVerifier.create(requestBuilder.executeReactive(Person.class))
                .expectErrorMatches(throwable -> throwable instanceof RestClientException)
                .verify();
    }

    @Test
    public void testCustomExceptionMapping() {
        stubFor(get(urlEqualTo("/notfound"))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withBody("Not Found")));

        RequestBuilder requestBuilder = new RequestBuilder(webClient, HttpMethod.GET, "/notfound")
                .exceptionMapping(404, e -> new ResourceNotFoundException("Resource not found", e));

        StepVerifier.create(requestBuilder.executeReactive(String.class))
                .expectErrorMatches(throwable -> throwable instanceof ResourceNotFoundException &&
                        throwable.getMessage().equals("Resource not found"))
                .verify();
    }
}
