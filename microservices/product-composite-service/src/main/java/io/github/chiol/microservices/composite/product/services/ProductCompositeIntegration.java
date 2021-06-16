package io.github.chiol.microservices.composite.product.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.chiol.api.core.product.Product;
import io.github.chiol.api.core.product.ProductService;
import io.github.chiol.api.core.recommendation.Recommendation;
import io.github.chiol.api.core.recommendation.RecommendationService;
import io.github.chiol.api.core.review.Review;
import io.github.chiol.api.core.review.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.springframework.http.HttpMethod.GET;

@Component
public class ProductCompositeIntegration implements ProductService, RecommendationService, ReviewService {
    private final RestTemplate restTemplate;
    private final ObjectMapper mapper;
    private final String productServiceUrl;
    private final String recommendationServiceUrl;
    private final String reviewServiceUrl;

    @Autowired
    public ProductCompositeIntegration(
            RestTemplate restTemplate,
            ObjectMapper mapper,
            @Value("app.product-service.host") String productServiceHost,
            @Value("app.product-service.host") String productServicePort,
            @Value("app.recommendation-service.host") String recommendationServiceHost,
            @Value("app.recommendation-service.host") String recommendationServicePort,
            @Value("app.review-service.host") String reviewServiceHost,
            @Value("app.review-service.host") String reviewServicePort) {
        this.restTemplate = restTemplate;
        this.mapper = mapper;
        this.productServiceUrl = "http://" + productServiceHost + ":" + productServicePort + "/product/";
        this.recommendationServiceUrl = "http://" + recommendationServiceHost + ":" + recommendationServicePort + "/recommendation?productId=";
        this.reviewServiceUrl = "http://" + reviewServiceHost + ":" + reviewServicePort + "/review?productId=";
    }

    @Override
    public Product getProduct(int productId) {
        String url = productServiceUrl + productId;
        Product product = restTemplate.getForObject(url,Product.class);
        return product;
    }

    @Override
    public List<Recommendation> getRecommendations(int productId) {
        String url = recommendationServiceUrl + productId;
        List<Recommendation> recommendations = restTemplate.exchange(url, GET, null, new ParameterizedTypeReference<List<Recommendation>>() {}).getBody();
        return recommendations;
    }

    @Override
    public List<Review> getReviews(int productId) {
        String url = reviewServiceUrl + productId;
        List<Review> reviews = restTemplate.exchange(url, GET, null, new ParameterizedTypeReference<List<Review>>() {
        }).getBody();

        return reviews;
    }
}
