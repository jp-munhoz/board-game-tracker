package br.com.bg.collection.repository;

import br.com.bg.collection.domain.SessionNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SessionNoteRepository extends JpaRepository<SessionNote, Long> {

    List<SessionNote> findByGameSession_IdOrderByCreatedAtAsc(Long gameSessionId);

    Optional<SessionNote> findByGameSession_IdAndAuthor_Id(Long gameSessionId, Long authorId);

    boolean existsByGameSession_IdAndAuthor_Id(Long gameSessionId, Long authorId);

    void deleteByGameSession_Id(Long gameSessionId);

    void deleteByGameSession_CollectionEntry_Id(Long collectionEntryId);

    void deleteByGameSession_IdAndAuthor_Id(Long gameSessionId, Long authorId);

    @Query("""
            select sn from SessionNote sn
            where sn.author.id = :userId and sn.gameSession.collectionEntry.user.id <> :userId
            order by sn.gameSession.createdAt desc
            """)
    List<SessionNote> findParticipatingNotes(@Param("userId") Long userId);
}
