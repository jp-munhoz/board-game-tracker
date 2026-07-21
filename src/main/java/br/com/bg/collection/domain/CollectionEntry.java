package br.com.bg.collection.domain;

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
@Table(name = "collection_entry", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "game_id"}))
public class CollectionEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    @Column(name = "added_at", nullable = false)
    private Instant addedAt;

    protected CollectionEntry() {
    }

    public CollectionEntry(AppUser user, Game game) {
        this.user = user;
        this.game = game;
        this.addedAt = Instant.now();
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

    public Instant getAddedAt() {
        return addedAt;
    }
}
