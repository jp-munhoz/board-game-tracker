package br.com.bg.collection.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "game_session")
public class GameSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "collection_entry_id", nullable = false)
    private CollectionEntry collectionEntry;

    @Column(name = "played_at", nullable = false)
    private LocalDate playedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SessionStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected GameSession() {
    }

    public GameSession(CollectionEntry collectionEntry, LocalDate playedAt) {
        this.collectionEntry = collectionEntry;
        this.playedAt = playedAt;
        this.status = SessionStatus.ONGOING;
        this.createdAt = Instant.now();
    }

    public void updatePlayedAt(LocalDate playedAt) {
        this.playedAt = playedAt;
    }

    public void complete() {
        this.status = SessionStatus.COMPLETED;
    }

    public Long getId() {
        return id;
    }

    public CollectionEntry getCollectionEntry() {
        return collectionEntry;
    }

    public LocalDate getPlayedAt() {
        return playedAt;
    }

    public SessionStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
