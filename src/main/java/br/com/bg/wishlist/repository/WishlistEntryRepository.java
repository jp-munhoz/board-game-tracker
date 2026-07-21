package br.com.bg.wishlist.repository;

import br.com.bg.wishlist.domain.WishlistEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WishlistEntryRepository extends JpaRepository<WishlistEntry, Long> {

    List<WishlistEntry> findByUser_IdOrderByAddedAtDesc(Long userId);

    Optional<WishlistEntry> findByUser_IdAndGame_Id(Long userId, Long gameId);

    boolean existsByUser_IdAndGame_Id(Long userId, Long gameId);
}
