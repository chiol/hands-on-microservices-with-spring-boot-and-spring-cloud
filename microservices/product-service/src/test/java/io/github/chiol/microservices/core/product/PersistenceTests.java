package io.github.chiol.microservices.core.product;

import io.github.chiol.microservices.core.product.persistence.ProductEntity;
import io.github.chiol.microservices.core.product.persistence.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.stream.IntStream.rangeClosed;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.data.domain.Sort.Direction.ASC;

@DataMongoTest
public class PersistenceTests {
    @Autowired
    private ProductRepository repository;
    private ProductEntity savedEntity;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
        ProductEntity entity = new ProductEntity(1,"n",1);
        savedEntity = repository.save(entity);
        assertEqualsProduct(entity, savedEntity);
    }

    @Test
    void create() {
        ProductEntity newEntity = new ProductEntity(2,"n",2);
        savedEntity = repository.save(newEntity);
        ProductEntity foundEntity = repository.findById(newEntity.getId()).get();
        assertEqualsProduct(foundEntity, savedEntity);
        assertEquals(2, repository.count());
    }

    @Test
    void update() {
        assertEquals(0, (long)savedEntity.getVersion());
        savedEntity.setName("n2");
        repository.save(savedEntity);
        ProductEntity foundEntity = repository.findById(savedEntity.getId()).get();
        assertEquals(1, (long)foundEntity.getVersion());
        assertEquals("n2", foundEntity.getName());
    }

    @Test
    void delete() {
        repository.delete(savedEntity);
        assertFalse(repository.existsById(savedEntity.getId()));
    }

    @Test
    void getByProductId() {
        Optional<ProductEntity> entity = repository.findByProductId(savedEntity.getProductId());
        assertTrue(entity.isPresent());
        assertEqualsProduct(savedEntity, entity.get());
    }

    @Test
    void duplicateError() {
        ProductEntity entity = new ProductEntity(savedEntity.getProductId(),"n",1);
        assertThrows(DuplicateKeyException.class, () -> {repository.save(entity);});
    }

    @Test
    void optimisticLock() {
        ProductEntity entity1 = repository.findById(savedEntity.getId()).get();
        ProductEntity entity2 = repository.findById(savedEntity.getId()).get();

        entity1.setName("n1");
        repository.save(entity1);

        entity2.setName("n2");
        assertThrows(OptimisticLockingFailureException.class, () -> {repository.save(entity2);});

        ProductEntity updateEntity = repository.findById(savedEntity.getId()).get();

        assertEquals(1, (int)updateEntity.getVersion());
        assertEquals("n1", updateEntity.getName());
    }

    @Test
    void paging() {
        repository.deleteAll();
        List<ProductEntity> newProducts = rangeClosed(1001, 1010).mapToObj(i -> new ProductEntity(i, "name " + i, i)).collect(Collectors.toList());
        repository.saveAll(newProducts);
        Pageable nextPage = PageRequest.of(0, 4, ASC, "productId");

        nextPage = testNextPage(nextPage, "[1001, 1002, 1003, 1004]", true);
        nextPage = testNextPage(nextPage, "[1005, 1006, 1007, 1008]", true);
        nextPage = testNextPage(nextPage, "[1009, 1010]", false);


    }
    private Pageable testNextPage(Pageable nextPage, String expectedProductIds, boolean expectsNextPage) {
        Page<ProductEntity> productPage = repository.findAll(nextPage);
        assertEquals(expectedProductIds, productPage.getContent().stream().map(p -> p.getProductId()).collect(Collectors.toList()).toString());
        assertEquals(expectsNextPage, productPage.hasNext());
        return productPage.nextPageable();
    }

    private void assertEqualsProduct(ProductEntity expectedEntity, ProductEntity actualEntity) {
        assertEquals(expectedEntity.getId(),               actualEntity.getId());
        assertEquals(expectedEntity.getVersion(),          actualEntity.getVersion());
        assertEquals(expectedEntity.getProductId(),        actualEntity.getProductId());
        assertEquals(expectedEntity.getName(),           actualEntity.getName());
        assertEquals(expectedEntity.getWeight(),           actualEntity.getWeight());
    }
}
