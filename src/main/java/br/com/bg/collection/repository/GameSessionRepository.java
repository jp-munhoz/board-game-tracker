package br.com.bg.collection.repository;

import br.com.bg.collection.domain.GameSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GameSessionRepository extends JpaRepository<GameSession, Long> {

    List<GameSession> findByCollectionEntry_IdOrderByPlayedAtDescCreatedAtDesc(Long collectionEntryId);

    Optional<GameSession> findByIdAndCollectionEntry_Id(Long id, Long collectionEntryId);

    void deleteByCollectionEntry_Id(Long collectionEntryId);
}
