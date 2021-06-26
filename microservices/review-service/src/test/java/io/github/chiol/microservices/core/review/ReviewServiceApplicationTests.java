package io.github.chiol.microservices.core.review;

import io.github.chiol.api.core.review.Review;
import io.github.chiol.microservices.core.review.persistence.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static reactor.core.publisher.Mono.just;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@SpringBootTest(webEnvironment = RANDOM_PORT, properties = {
		"spring.datasource.url=jdbc:h2:mem:review-db"})
class ReviewServiceApplicationTests {

	@Autowired
	private ReviewRepository repository;


	@BeforeEach
	public void setupDb() {
		repository.deleteAll();
	}

	@Test
	public void getReviewsByProductId(@Autowired WebTestClient client) {

		int productId = 1;

		assertEquals(0, repository.findByProductId(productId).size());

		postAndVerifyReview(productId, 1, OK, client);
		postAndVerifyReview(productId, 2, OK, client);
		postAndVerifyReview(productId, 3, OK, client);

		assertEquals(3, repository.findByProductId(productId).size());

		getAndVerifyReviewsByProductId(productId, OK, client)
				.jsonPath("$.length()").isEqualTo(3)
				.jsonPath("$[2].productId").isEqualTo(productId)
				.jsonPath("$[2].reviewId").isEqualTo(3);
	}

	@Test
	public void duplicateError(@Autowired WebTestClient client) {

		int productId = 1;
		int reviewId = 1;

		assertEquals(0, repository.count());

		postAndVerifyReview(productId, reviewId, OK, client)
				.jsonPath("$.productId").isEqualTo(productId)
				.jsonPath("$.reviewId").isEqualTo(reviewId);

		assertEquals(1, repository.count());

		postAndVerifyReview(productId, reviewId, UNPROCESSABLE_ENTITY, client)
				.jsonPath("$.path").isEqualTo("/review")
				.jsonPath("$.message").isEqualTo("Duplicate key, Product Id: 1, Review Id:1");

		assertEquals(1, repository.count());
	}

	@Test
	public void deleteReviews(@Autowired WebTestClient client) {

		int productId = 1;
		int recommendationId = 1;

		postAndVerifyReview(productId, recommendationId, OK, client);
		assertEquals(1, repository.findByProductId(productId).size());

		deleteAndVerifyReviewsByProductId(productId, OK, client);
		assertEquals(0, repository.findByProductId(productId).size());

		deleteAndVerifyReviewsByProductId(productId, OK, client);
	}

	@Test
	public void getReviewsMissingParameter(@Autowired WebTestClient client) {

		getAndVerifyReviewsByProductId("", BAD_REQUEST, client)
				.jsonPath("$.path").isEqualTo("/review")
				.jsonPath("$.message").isEqualTo("Required int parameter 'productId' is not present");
	}

	@Test
	public void getReviewsInvalidParameter(@Autowired WebTestClient client) {

		getAndVerifyReviewsByProductId("?productId=no-integer", BAD_REQUEST, client)
				.jsonPath("$.path").isEqualTo("/review")
				.jsonPath("$.message").isEqualTo("Type mismatch.");
	}

	@Test
	public void getReviewsNotFound(@Autowired WebTestClient client) {

		getAndVerifyReviewsByProductId("?productId=213", OK, client)
				.jsonPath("$.length()").isEqualTo(0);
	}

	@Test
	public void getReviewsInvalidParameterNegativeValue(@Autowired WebTestClient client) {

		int productIdInvalid = -1;

		getAndVerifyReviewsByProductId("?productId=" + productIdInvalid, UNPROCESSABLE_ENTITY, client)
				.jsonPath("$.path").isEqualTo("/review")
				.jsonPath("$.message").isEqualTo("Invalid productId: " + productIdInvalid);
	}

	private WebTestClient.BodyContentSpec getAndVerifyReviewsByProductId(int productId, HttpStatus expectedStatus,WebTestClient client) {
		return getAndVerifyReviewsByProductId("?productId=" + productId, expectedStatus,client);
	}

	private WebTestClient.BodyContentSpec getAndVerifyReviewsByProductId(String productIdQuery, HttpStatus expectedStatus,WebTestClient client) {
		return client.get()
				.uri("/review" + productIdQuery)
				.accept(APPLICATION_JSON)
				.exchange()
				.expectStatus().isEqualTo(expectedStatus)
				.expectHeader().contentType(APPLICATION_JSON)
				.expectBody();
	}

	private WebTestClient.BodyContentSpec postAndVerifyReview(int productId, int reviewId, HttpStatus expectedStatus,WebTestClient client) {
		Review review = new Review(productId, reviewId, "Author " + reviewId, "Subject " + reviewId, "Content " + reviewId, "SA");
		return client.post()
				.uri("/review")
				.body(just(review), Review.class)
				.accept(APPLICATION_JSON)
				.exchange()
				.expectStatus().isEqualTo(expectedStatus)
				.expectHeader().contentType(APPLICATION_JSON)
				.expectBody();
	}

	private WebTestClient.BodyContentSpec deleteAndVerifyReviewsByProductId(int productId, HttpStatus expectedStatus,WebTestClient client) {
		return client.delete()
				.uri("/review?productId=" + productId)
				.accept(APPLICATION_JSON)
				.exchange()
				.expectStatus().isEqualTo(expectedStatus)
				.expectBody();
	}
}
