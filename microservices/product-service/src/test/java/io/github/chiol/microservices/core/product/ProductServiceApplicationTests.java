package io.github.chiol.microservices.core.product;

import io.github.chiol.api.core.product.Product;
import io.github.chiol.microservices.core.product.persistence.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static reactor.core.publisher.Mono.just;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@SpringBootTest(webEnvironment = RANDOM_PORT,properties = {"spring.data.mongodb.port: 0"})
class ProductServiceApplicationTests {

    @Autowired
    private ProductRepository repository;

    @BeforeEach
    public void setupDb() {
        repository.deleteAll();
    }

    @Test
    public void getProductById(@Autowired WebTestClient client) {

        int productId = 1;

        postAndVerifyProduct(productId, OK, client);

        assertTrue(repository.findByProductId(productId).isPresent());

        getAndVerifyProduct(productId, OK, client)
                .jsonPath("$.productId").isEqualTo(productId);
    }

    @Test
    public void duplicateError(@Autowired WebTestClient client) {

        int productId = 1;

        postAndVerifyProduct(productId, OK, client);

        assertTrue(repository.findByProductId(productId).isPresent());

        postAndVerifyProduct(productId, UNPROCESSABLE_ENTITY, client)
                .jsonPath("$.path").isEqualTo("/product")
                .jsonPath("$.message").isEqualTo("Duplicate key, Product Id: " + productId);
    }

    @Test
    public void deleteProduct(@Autowired WebTestClient client) {

        int productId = 1;

        postAndVerifyProduct(productId, OK, client);
        assertTrue(repository.findByProductId(productId).isPresent());

        deleteAndVerifyProduct(productId, OK, client);
        assertFalse(repository.findByProductId(productId).isPresent());

        deleteAndVerifyProduct(productId, OK,client);
    }

    @Test
    public void getProductInvalidParameterString(@Autowired WebTestClient client) {

        getAndVerifyProduct("/no-integer", BAD_REQUEST, client)
                .jsonPath("$.path").isEqualTo("/product/no-integer")
                .jsonPath("$.message").isEqualTo("Type mismatch.");
    }

    @Test
    public void getProductNotFound(@Autowired WebTestClient client) {

        int productIdNotFound = 13;
        getAndVerifyProduct(productIdNotFound, NOT_FOUND, client)
                .jsonPath("$.path").isEqualTo("/product/" + productIdNotFound)
                .jsonPath("$.message").isEqualTo("No product found for productId: " + productIdNotFound);
    }

    @Test
    public void getProductInvalidParameterNegativeValue(@Autowired WebTestClient client) {

        int productIdInvalid = -1;

        getAndVerifyProduct(productIdInvalid, UNPROCESSABLE_ENTITY, client)
                .jsonPath("$.path").isEqualTo("/product/" + productIdInvalid)
                .jsonPath("$.message").isEqualTo("Invalid productId: " + productIdInvalid);
    }

    private WebTestClient.BodyContentSpec getAndVerifyProduct(int productId, HttpStatus expectedStatus, WebTestClient client) {
        return getAndVerifyProduct("/" + productId, expectedStatus, client);
    }

    private WebTestClient.BodyContentSpec getAndVerifyProduct(String productIdPath, HttpStatus expectedStatus, WebTestClient client) {
        return client.get()
                .uri("/product" + productIdPath)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus)
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody();
    }

    private WebTestClient.BodyContentSpec postAndVerifyProduct(int productId, HttpStatus expectedStatus, WebTestClient client) {
        Product product = new Product(productId, "Name " + productId, productId, "SA");
        return client.post()
                .uri("/product")
                .body(just(product), Product.class)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus)
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody();
    }

    private WebTestClient.BodyContentSpec deleteAndVerifyProduct(int productId, HttpStatus expectedStatus, WebTestClient client) {
        return client.delete()
                .uri("/product/" + productId)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus)
                .expectBody();
    }
}
