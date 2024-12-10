package com.sagarv.webclient;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class RequestBuilderGetIntegrationTest {

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

    @Test
    public void testGetReactiveSuccess() {
        stubFor(get(urlEqualTo("/test"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody("Success")));

        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "application/json");

        RequestBuilder requestBuilder = new RequestBuilder(webClient, HttpMethod.GET, "/test")
                .headers(headers);

        StepVerifier.create(requestBuilder.executeReactive(String.class))
                .expectNext("Success")
                .verifyComplete();
    }

    @Test
    public void testGetReactiveNotFoundWithCustomException() {
        stubFor(get(urlEqualTo("/test"))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withBody("Not Found")));

        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "application/json");

        RequestBuilder requestBuilder = new RequestBuilder(webClient, HttpMethod.GET, "/test")
                .headers(headers)
                .exceptionMapping(404, e -> new ResourceNotFoundException("Resource not found", e));

        StepVerifier.create(requestBuilder.executeReactive(String.class))
                .expectErrorMatches(throwable -> throwable instanceof ResourceNotFoundException &&
                        throwable.getMessage().equals("Resource not found"))
                .verify();
    }

    @Test
    public void testGetReactiveDefaultExceptionHandling() {
        stubFor(get(urlEqualTo("/test"))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withBody("Internal Server Error")));

        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "application/json");

        RequestBuilder requestBuilder = new RequestBuilder(webClient, HttpMethod.GET, "/test")
                .headers(headers);

        StepVerifier.create(requestBuilder.executeReactive(String.class))
                .expectErrorMatches(throwable -> throwable instanceof RestClientException &&
                        throwable.getMessage().contains("Unexpected error: Internal Server Error"))
                .verify();
    }

    @Test
    public void testGetSyncSuccess() {
        stubFor(get(urlEqualTo("/test"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody("Success")));

        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "application/json");

        RequestBuilder requestBuilder = new RequestBuilder(webClient, HttpMethod.GET, "/test")
                .headers(headers);

        // Use block() to make a synchronous call
        String response = requestBuilder.executeReactive(String.class).block();
        assertEquals("Success", response);
    }

    @Test
    public void testGetSyncNotFoundWithCustomException() {
        stubFor(get(urlEqualTo("/test"))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withBody("Not Found")));

        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "application/json");

        RequestBuilder requestBuilder = new RequestBuilder(webClient, HttpMethod.GET, "/test")
                .headers(headers)
                .exceptionMapping(404, e -> new ResourceNotFoundException("Resource not found", e));

        // Use block() to make a synchronous call and handle exceptions
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                requestBuilder.executeReactive(String.class).block());
        assertEquals("Resource not found", exception.getMessage());
    }

    @Test
    public void testGetListReactiveSuccess() {
        stubFor(get(urlEqualTo("/list"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody("[\"item1\",\"item2\"]")
                        .withHeader("Content-Type", "application/json")));

        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "application/json");

        RequestBuilder requestBuilder = new RequestBuilder(webClient, HttpMethod.GET, "/list")
                .headers(headers);

        StepVerifier.create(requestBuilder.executeReactive(new ParameterizedTypeReference<List<String>>() {}))
                .expectNextMatches(list -> list.size() == 2 && list.contains("item1") && list.contains("item2"))
                .verifyComplete();
    }

    @Test
    public void testGetListSyncSuccess() {
        stubFor(get(urlEqualTo("/list"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody("[\"item1\",\"item2\"]")
                        .withHeader("Content-Type", "application/json")));

        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "application/json");

        RequestBuilder requestBuilder = new RequestBuilder(webClient, HttpMethod.GET, "/list")
                .headers(headers);

        // Use block() to make a synchronous call
        List<String> response = requestBuilder.executeReactive(new ParameterizedTypeReference<List<String>>() {}).block();
        assertEquals(2, response.size());
        assertEquals("item1", response.get(0));
        assertEquals("item2", response.get(1));
    }

    @Test
    public void testGetFluxSuccess() {
        stubFor(get(urlEqualTo("/stream"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody("item1\nitem2\nitem3\n") // Simulating a newline-delimited stream
                        .withHeader("Content-Type", "text/plain")));

        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "text/plain");

        RequestBuilder requestBuilder = new RequestBuilder(webClient, HttpMethod.GET, "/stream")
                .headers(headers);

        StepVerifier.create(requestBuilder.executeReactiveAsFlux(String.class))
                .expectNext("item1", "item2", "item3")
                .verifyComplete();
    }

    @Test
    public void testStringResponse() {
        stubFor(get(urlEqualTo("/string"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody("Hello, World!")));

        RequestBuilder requestBuilder = new RequestBuilder(webClient, HttpMethod.GET, "/string");

        StepVerifier.create(requestBuilder.executeReactive(String.class))
                .expectNext("Hello, World!")
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
    public void testJsonResponse() {
        stubFor(get(urlEqualTo("/json"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody("{\"key\":\"value\"}")
                        .withHeader("Content-Type", "application/json")));

        RequestBuilder requestBuilder = new RequestBuilder(webClient, HttpMethod.GET, "/json");

        StepVerifier.create(requestBuilder.executeReactive(String.class))
                .expectNext("{\"key\":\"value\"}")
                .verifyComplete();
    }

    @Test
    public void testJsonMappedToPojo() {
        stubFor(get(urlEqualTo("/pojo"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody("{\"name\":\"John Doe\",\"age\":30}")
                        .withHeader("Content-Type", "application/json")));

        RequestBuilder requestBuilder = new RequestBuilder(webClient, HttpMethod.GET, "/pojo");

        StepVerifier.create(requestBuilder.executeReactive(Person.class))
                .expectNextMatches(person -> person.getName().equals("John Doe") && person.getAge() == 30)
                .verifyComplete();
    }

    @Test
    public void testJsonListResponse() {
        stubFor(get(urlEqualTo("/jsonList"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody("[{\"name\":\"John Doe\",\"age\":30},{\"name\":\"Jane Doe\",\"age\":25}]")
                        .withHeader("Content-Type", "application/json")));

        RequestBuilder requestBuilder = new RequestBuilder(webClient, HttpMethod.GET, "/jsonList");

        StepVerifier.create(requestBuilder.executeReactive(new ParameterizedTypeReference<List<Person>>() {}))
                .expectNextMatches(list -> list.size() == 2 &&
                        list.get(0).getName().equals("John Doe") && list.get(0).getAge() == 30 &&
                        list.get(1).getName().equals("Jane Doe") && list.get(1).getAge() == 25)
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
    public void testInvalidJsonResponse() {
        stubFor(get(urlEqualTo("/invalidJson"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody("Not a JSON")));

        RequestBuilder requestBuilder = new RequestBuilder(webClient, HttpMethod.GET, "/invalidJson");

        StepVerifier.create(requestBuilder.executeReactive(Person.class))
                .expectErrorMatches(throwable -> throwable instanceof RestClientException)
                .verify();
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
}

class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

class Person {
    private String name;
    private int age;

    // Getters and setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }
}