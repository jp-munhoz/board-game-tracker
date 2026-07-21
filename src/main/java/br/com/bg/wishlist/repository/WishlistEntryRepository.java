package br.com.bg.wishlist.repository;

import br.com.bg.wishlist.domain.WishlistEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface WishlistEntryRepository extends JpaRepository<WishlistEntry, Long> {

    @Query("select we from WishlistEntry we join fetch we.game where we.user.id = :userId order by we.addedAt desc")
    List<WishlistEntry> findByUser_IdOrderByAddedAtDesc(@Param("userId") Long userId);

    Optional<WishlistEntry> findByUser_IdAndGame_Id(Long userId, Long gameId);

    boolean existsByUser_IdAndGame_Id(Long userId, Long gameId);
}
