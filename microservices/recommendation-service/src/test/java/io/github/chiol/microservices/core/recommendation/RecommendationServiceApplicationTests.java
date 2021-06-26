package io.github.chiol.microservices.core.recommendation;

import io.github.chiol.api.core.recommendation.Recommendation;
import io.github.chiol.microservices.core.recommendation.persistence.RecommendationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static reactor.core.publisher.Mono.just;

@SpringBootTest(webEnvironment = RANDOM_PORT, properties = {"spring.data.mongodb.port: 0"})
class RecommendationServiceApplicationTests {

	@Autowired
	private RecommendationRepository repository;


	@BeforeEach
	public void setupDb() {
		repository.deleteAll();
	}

	@Test
	public void getRecommendationsByProductId(@Autowired WebTestClient client) {

		int productId = 1;

		postAndVerifyRecommendation(productId, 1, OK,client);
		postAndVerifyRecommendation(productId, 2, OK,client);
		postAndVerifyRecommendation(productId, 3, OK,client);

		assertEquals(3, repository.findByProductId(productId).size());

		getAndVerifyRecommendationsByProductId(productId, OK, client)
				.jsonPath("$.length()").isEqualTo(3)
				.jsonPath("$[2].productId").isEqualTo(productId)
				.jsonPath("$[2].recommendationId").isEqualTo(3);
	}

	@Test
	public void duplicateError(@Autowired WebTestClient client) {

		int productId = 1;
		int recommendationId = 1;

		postAndVerifyRecommendation(productId, recommendationId, OK, client)
				.jsonPath("$.productId").isEqualTo(productId)
				.jsonPath("$.recommendationId").isEqualTo(recommendationId);

		assertEquals(1, repository.count());

		postAndVerifyRecommendation(productId, recommendationId, UNPROCESSABLE_ENTITY,client)
				.jsonPath("$.path").isEqualTo("/recommendation")
				.jsonPath("$.message").isEqualTo("Duplicate key, Product Id: 1, Recommendation Id:1");

		assertEquals(1, repository.count());
	}

	@Test
	public void deleteRecommendations(@Autowired WebTestClient client) {

		int productId = 1;
		int recommendationId = 1;

		postAndVerifyRecommendation(productId, recommendationId, OK,client);
		assertEquals(1, repository.findByProductId(productId).size());

		deleteAndVerifyRecommendationsByProductId(productId, OK,client);
		assertEquals(0, repository.findByProductId(productId).size());

		deleteAndVerifyRecommendationsByProductId(productId, OK,client);
	}

	@Test
	public void getRecommendationsMissingParameter(@Autowired WebTestClient client) {

		getAndVerifyRecommendationsByProductId("", BAD_REQUEST,client)
				.jsonPath("$.path").isEqualTo("/recommendation")
				.jsonPath("$.message").isEqualTo("Required int parameter 'productId' is not present");
	}

	@Test
	public void getRecommendationsInvalidParameter(@Autowired WebTestClient client) {

		getAndVerifyRecommendationsByProductId("?productId=no-integer", BAD_REQUEST,client)
				.jsonPath("$.path").isEqualTo("/recommendation")
				.jsonPath("$.message").isEqualTo("Type mismatch.");
	}

	@Test
	public void getRecommendationsNotFound(@Autowired WebTestClient client) {

		getAndVerifyRecommendationsByProductId("?productId=113", OK, client)
				.jsonPath("$.length()").isEqualTo(0);
	}

	@Test
	public void getRecommendationsInvalidParameterNegativeValue(@Autowired WebTestClient client) {

		int productIdInvalid = -1;

		getAndVerifyRecommendationsByProductId("?productId=" + productIdInvalid, UNPROCESSABLE_ENTITY,client)
				.jsonPath("$.path").isEqualTo("/recommendation")
				.jsonPath("$.message").isEqualTo("Invalid productId: " + productIdInvalid);
	}

	private WebTestClient.BodyContentSpec getAndVerifyRecommendationsByProductId(int productId, HttpStatus expectedStatus,WebTestClient client) {
		return getAndVerifyRecommendationsByProductId("?productId=" + productId, expectedStatus,client);
	}

	private WebTestClient.BodyContentSpec getAndVerifyRecommendationsByProductId(String productIdQuery, HttpStatus expectedStatus,WebTestClient client) {
		return client.get()
				.uri("/recommendation" + productIdQuery)
				.accept(APPLICATION_JSON)
				.exchange()
				.expectStatus().isEqualTo(expectedStatus)
				.expectHeader().contentType(APPLICATION_JSON)
				.expectBody();
	}

	private WebTestClient.BodyContentSpec postAndVerifyRecommendation(int productId, int recommendationId, HttpStatus expectedStatus,WebTestClient client) {
		Recommendation recommendation = new Recommendation(productId, recommendationId, "Author " + recommendationId, recommendationId, "Content " + recommendationId, "SA");
		return client.post()
				.uri("/recommendation")
				.body(just(recommendation), Recommendation.class)
				.accept(APPLICATION_JSON)
				.exchange()
				.expectStatus().isEqualTo(expectedStatus)
				.expectHeader().contentType(APPLICATION_JSON)
				.expectBody();
	}

	private WebTestClient.BodyContentSpec deleteAndVerifyRecommendationsByProductId(int productId, HttpStatus expectedStatus,WebTestClient client) {
		return client.delete()
				.uri("/recommendation?productId=" + productId)
				.accept(APPLICATION_JSON)
				.exchange()
				.expectStatus().isEqualTo(expectedStatus)
				.expectBody();
	}
}
