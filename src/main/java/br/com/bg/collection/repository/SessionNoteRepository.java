package br.com.bg.collection.repository;

import br.com.bg.collection.domain.SessionNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SessionNoteRepository extends JpaRepository<SessionNote, Long> {

    @Query("select sn from SessionNote sn join fetch sn.author where sn.gameSession.id = :gameSessionId order by sn.createdAt asc")
    List<SessionNote> findByGameSession_IdOrderByCreatedAtAsc(@Param("gameSessionId") Long gameSessionId);

    @Query("""
            select sn from SessionNote sn join fetch sn.author
            where sn.gameSession.collectionEntry.id = :collectionEntryId
            order by sn.gameSession.id asc, sn.createdAt asc
            """)
    List<SessionNote> findByGameSession_CollectionEntry_Id(@Param("collectionEntryId") Long collectionEntryId);

    Optional<SessionNote> findByGameSession_IdAndAuthor_Id(Long gameSessionId, Long authorId);

    boolean existsByGameSession_IdAndAuthor_Id(Long gameSessionId, Long authorId);

    void deleteByGameSession_Id(Long gameSessionId);

    void deleteByGameSession_CollectionEntry_Id(Long collectionEntryId);

    void deleteByGameSession_IdAndAuthor_Id(Long gameSessionId, Long authorId);

    @Query("""
            select sn from SessionNote sn
            join fetch sn.author
            join fetch sn.gameSession gs
            join fetch gs.collectionEntry ce
            join fetch ce.user owner
            join fetch ce.game
            where sn.author.id = :userId and ce.user.id <> :userId
            order by gs.createdAt desc
            """)
    List<SessionNote> findParticipatingNotes(@Param("userId") Long userId);
}
