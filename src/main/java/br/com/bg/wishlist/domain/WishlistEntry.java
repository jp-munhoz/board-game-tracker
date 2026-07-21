package br.com.bg.wishlist.domain;

import br.com.bg.game.domain.Game;
import br.com.bg.user.domain.AppUser;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;

@Entity
@Table(name = "wishlist_entry", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "game_id"}))
public class WishlistEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    @Column(name = "added_at", nullable = false)
    private Instant addedAt;

    protected WishlistEntry() {
    }

    public WishlistEntry(AppUser user, Game game, String note) {
        this.user = user;
        this.game = game;
        this.note = note;
        this.addedAt = Instant.now();
    }

    public void updateNote(String note) {
        this.note = note;
    }

    public Long getId() {
        return id;
    }

    public AppUser getUser() {
        return user;
    }

    public Game getGame() {
        return game;
    }

    public String getNote() {
        return note;
    }

    public Instant getAddedAt() {
        return addedAt;
    }
}
