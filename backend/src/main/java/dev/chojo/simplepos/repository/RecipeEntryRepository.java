package dev.chojo.simplepos.repository;

import dev.chojo.simplepos.entity.RecipeEntry;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecipeEntryRepository extends JpaRepository<RecipeEntry, Integer> {
}
