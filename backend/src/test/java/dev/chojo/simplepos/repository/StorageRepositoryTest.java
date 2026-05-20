package dev.chojo.simplepos.repository;

import dev.chojo.simplepos.TestcontainersConfiguration;
import dev.chojo.simplepos.entity.Category;
import dev.chojo.simplepos.entity.Ingredient;
import dev.chojo.simplepos.entity.Storage;
import dev.chojo.simplepos.entity.dto.StorageSummary;
import jakarta.persistence.Tuple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class StorageRepositoryTest {
    @Autowired
    StorageRepository storageRepository;
    @Autowired
    IngredientRepository ingredientRepository;
    @Autowired
    CategoryRepository categoryRepository;

    private Ingredient ingredient;

    @BeforeEach
    void setUp() {
        Category category = categoryRepository.save(new Category(null, "StorageCat"));
        ingredient = ingredientRepository.save(
                new Ingredient(null, category, "StorageIngredient", 1.0, 24, 0, 0, 5));
    }

    @Test
    void shouldSaveStorage() {
        Storage storage = storageRepository.save(
                new Storage(null, ingredient, Instant.now(), 1.5, 24, 0));
        assertNotNull(storage.getId());
    }

    @Test
    void shouldFindById() {
        Storage saved = storageRepository.save(
                new Storage(null, ingredient, Instant.now(), 1.0, 10, 0));
        Optional<Storage> found = storageRepository.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals(10, found.get().getAmount());
    }

    @Test
    void shouldReturnSummaryById() {
        storageRepository.save(new Storage(null, ingredient, Instant.now(), 1.0, 20, 5));
        storageRepository.save(new Storage(null, ingredient, Instant.now(), 1.2, 10, 2));

        Tuple summary = storageRepository.summaryById(ingredient.getId());
        StorageSummary storageSummary = new StorageSummary(summary);
        assertEquals(ingredient.getId(), storageSummary.getIngredient().getId());
        assertEquals(23, storageSummary.getStock()); // (20-5) + (10-2) = 23
    }

    @Test
    void shouldReturnZeroStockWhenEmpty() {
        Tuple summary = storageRepository.summaryById(ingredient.getId());
        StorageSummary storageSummary = new StorageSummary(summary);
        assertEquals(0, storageSummary.getStock());
    }

    @Test
    void shouldReturnSummaryForAllIngredients() {
        Category category = categoryRepository.save(new Category(null, "SummaryCat"));
        Ingredient other = ingredientRepository.save(
                new Ingredient(null, category, "OtherIngredient", 2.0, 6, 0, 0, 0));

        storageRepository.save(new Storage(null, ingredient, Instant.now(), 1.0, 10, 3));
        storageRepository.save(new Storage(null, other, Instant.now(), 2.0, 6, 1));

        List<Tuple> summaries = storageRepository.summary();
        assertTrue(summaries.size() >= 2);
    }

    @Test
    void shouldFindNextIngredientFIFO() {
        Instant earlier = Instant.now().minus(2, ChronoUnit.HOURS);
        Instant later = Instant.now();

        Storage old = storageRepository.save(new Storage(null, ingredient, earlier, 1.0, 5, 0));
        storageRepository.save(new Storage(null, ingredient, later, 1.5, 10, 0));

        Optional<Integer> next = storageRepository.findNextIngredient(ingredient.getId());
        assertTrue(next.isPresent());
        assertEquals(old.getId(), next.get());
    }

    @Test
    void shouldSkipFullySoldStorage() {
        Instant earlier = Instant.now().minus(2, ChronoUnit.HOURS);
        Instant later = Instant.now();

        storageRepository.save(new Storage(null, ingredient, earlier, 1.0, 5, 5)); // fully sold
        Storage available = storageRepository.save(new Storage(null, ingredient, later, 1.5, 10, 3));

        Optional<Integer> next = storageRepository.findNextIngredient(ingredient.getId());
        assertTrue(next.isPresent());
        assertEquals(available.getId(), next.get());
    }

    @Test
    void shouldReturnEmptyWhenAllSold() {
        storageRepository.save(new Storage(null, ingredient, Instant.now(), 1.0, 5, 5));

        Optional<Integer> next = storageRepository.findNextIngredient(ingredient.getId());
        assertTrue(next.isEmpty());
    }

    @Test
    void shouldFindAllByIngredient() {
        storageRepository.save(new Storage(null, ingredient, Instant.now(), 1.0, 10, 0));
        storageRepository.save(new Storage(null, ingredient, Instant.now(), 1.5, 5, 0));

        List<Storage> results = storageRepository.findAllByIngredient(ingredient, PageRequest.of(0, 10));
        assertEquals(2, results.size());
    }

    @Test
    void shouldTrackSold() {
        Storage storage = storageRepository.save(new Storage(null, ingredient, Instant.now(), 1.0, 10, 0));
        storage.take();
        storage.take();
        storageRepository.save(storage);

        Storage found = storageRepository.findById(storage.getId()).orElseThrow();
        assertEquals(2, found.getSold());
    }

    @Test
    void shouldDetectLowStock() {
        // ingredient has minStock=5, add storage with stock=3
        storageRepository.save(new Storage(null, ingredient, Instant.now(), 1.0, 3, 0));

        List<Tuple> lowStock = storageRepository.lowStock(PageRequest.of(0, 10));
        boolean found = lowStock.stream()
                .map(StorageSummary::new)
                .anyMatch(s -> s.getIngredient().getId().equals(ingredient.getId()));
        assertTrue(found);
    }
}
