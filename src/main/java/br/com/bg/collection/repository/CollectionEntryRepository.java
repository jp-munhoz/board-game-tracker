package br.com.bg.collection.repository;

import br.com.bg.collection.domain.CollectionEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CollectionEntryRepository extends JpaRepository<CollectionEntry, Long> {

    List<CollectionEntry> findByUser_IdOrderByAddedAtDesc(Long userId);

    Optional<CollectionEntry> findByUser_IdAndGame_Id(Long userId, Long gameId);

    boolean existsByUser_IdAndGame_Id(Long userId, Long gameId);
}
