package br.com.bg.collection.repository;

import br.com.bg.collection.domain.CollectionEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CollectionEntryRepository extends JpaRepository<CollectionEntry, Long> {

    @Query("select ce from CollectionEntry ce join fetch ce.game where ce.user.id = :userId order by ce.addedAt desc")
    List<CollectionEntry> findByUser_IdOrderByAddedAtDesc(@Param("userId") Long userId);

    Optional<CollectionEntry> findByUser_IdAndGame_Id(Long userId, Long gameId);

    boolean existsByUser_IdAndGame_Id(Long userId, Long gameId);
}
