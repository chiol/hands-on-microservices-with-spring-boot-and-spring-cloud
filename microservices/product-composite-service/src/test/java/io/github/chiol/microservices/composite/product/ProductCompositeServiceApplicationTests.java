package io.github.chiol.microservices.composite.product;

import io.github.chiol.api.core.product.Product;
import io.github.chiol.api.core.recommendation.Recommendation;
import io.github.chiol.api.core.review.Review;
import io.github.chiol.microservices.composite.product.services.ProductCompositeIntegration;
import io.github.chiol.util.exceptions.InvalidInputException;
import io.github.chiol.util.exceptions.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static java.util.Collections.singletonList;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@SpringBootTest(webEnvironment = RANDOM_PORT,
        classes = {ProductCompositeServiceApplication.class, TestSecurityConfig.class },
        properties = {"spring.main.allow-bean-definition-overriding=true","eureka.client.enabled=false"}
        )
class ProductCompositeServiceApplicationTests {

    private static final int PRODUCT_ID_OK = 1;
    private static final int PRODUCT_ID_NOT_FOUND = 2;
    private static final int PRODUCT_ID_INVALID = 3;



    @MockBean
    private ProductCompositeIntegration compositeIntegration;

    @BeforeEach
    public void setUp() {

        when(compositeIntegration.getProduct(PRODUCT_ID_OK)).
                thenReturn(Mono.just(new Product(PRODUCT_ID_OK, "name", 1, "mock-address")));

        when(compositeIntegration.getRecommendations(PRODUCT_ID_OK)).
                thenReturn(Flux.fromIterable(singletonList(new Recommendation(PRODUCT_ID_OK, 1, "author", 1, "content", "mock address"))));

        when(compositeIntegration.getReviews(PRODUCT_ID_OK)).
                thenReturn(Flux.fromIterable(singletonList(new Review(PRODUCT_ID_OK, 1, "author", "subject", "content", "mock address"))));

        when(compositeIntegration.getProduct(PRODUCT_ID_NOT_FOUND)).thenThrow(new NotFoundException("NOT FOUND: " + PRODUCT_ID_NOT_FOUND));

        when(compositeIntegration.getProduct(PRODUCT_ID_INVALID)).thenThrow(new InvalidInputException("INVALID: " + PRODUCT_ID_INVALID));
    }

    @Test
    public void contextLoads() {
    }

    @Test
    public void getProductById(@Autowired WebTestClient client) {

        getAndVerifyProduct(PRODUCT_ID_OK, OK, client)
                .jsonPath("$.productId").isEqualTo(PRODUCT_ID_OK)
                .jsonPath("$.recommendations.length()").isEqualTo(1)
                .jsonPath("$.reviews.length()").isEqualTo(1);
    }

    @Test
    public void getProductNotFound(@Autowired WebTestClient client) {

        getAndVerifyProduct(PRODUCT_ID_NOT_FOUND, NOT_FOUND, client)
                .jsonPath("$.path").isEqualTo("/product-composite/" + PRODUCT_ID_NOT_FOUND)
                .jsonPath("$.message").isEqualTo("NOT FOUND: " + PRODUCT_ID_NOT_FOUND);
    }

    @Test
    public void getProductInvalidInput(@Autowired WebTestClient client) {

        getAndVerifyProduct(PRODUCT_ID_INVALID, UNPROCESSABLE_ENTITY, client)
                .jsonPath("$.path").isEqualTo("/product-composite/" + PRODUCT_ID_INVALID)
                .jsonPath("$.message").isEqualTo("INVALID: " + PRODUCT_ID_INVALID);
    }

    private WebTestClient.BodyContentSpec getAndVerifyProduct(int productId, HttpStatus expectedStatus, WebTestClient client) {
        return client.get()
                .uri("/product-composite/" + productId)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus)
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody();
    }
}
