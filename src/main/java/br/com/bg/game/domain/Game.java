package br.com.bg.game.domain;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "game")
public class Game {

    @Id
    private Long id;

    @Column(nullable = false)
    private String name;

    private String originalName;

    private String imageUrl;

    private Integer yearPublished;

    private Integer nationalYear;

    private Integer minPlayers;

    private Integer maxPlayers;

    private Integer playingTimeMinutes;

    private Integer minAge;

    private String link;

    @Lob
    private String description;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "game_mechanics", joinColumns = @JoinColumn(name = "game_id"))
    @Column(name = "mechanic")
    private List<String> mechanics = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "game_categories", joinColumns = @JoinColumn(name = "game_id"))
    @Column(name = "category")
    private List<String> categories = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "game_themes", joinColumns = @JoinColumn(name = "game_id"))
    @Column(name = "theme")
    private List<String> themes = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "game_designers", joinColumns = @JoinColumn(name = "game_id"))
    @Column(name = "designer")
    private List<String> designers = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "game_artists", joinColumns = @JoinColumn(name = "game_id"))
    @Column(name = "artist")
    private List<String> artists = new ArrayList<>();

    @Column(name = "last_synced_at", nullable = false)
    private Instant lastSyncedAt;

    protected Game() {
    }

    public Game(Long id) {
        this.id = id;
    }

    public void update(String name, String originalName, String imageUrl, Integer yearPublished, Integer nationalYear,
                        Integer minPlayers, Integer maxPlayers, Integer playingTimeMinutes, Integer minAge,
                        String link, String description, List<String> mechanics, List<String> categories,
                        List<String> themes, List<String> designers, List<String> artists) {
        this.name = name;
        this.originalName = originalName;
        this.imageUrl = imageUrl;
        this.yearPublished = yearPublished;
        this.nationalYear = nationalYear;
        this.minPlayers = minPlayers;
        this.maxPlayers = maxPlayers;
        this.playingTimeMinutes = playingTimeMinutes;
        this.minAge = minAge;
        this.link = link;
        this.description = description;
        this.mechanics = new ArrayList<>(mechanics);
        this.categories = new ArrayList<>(categories);
        this.themes = new ArrayList<>(themes);
        this.designers = new ArrayList<>(designers);
        this.artists = new ArrayList<>(artists);
        this.lastSyncedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getOriginalName() {
        return originalName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public Integer getYearPublished() {
        return yearPublished;
    }

    public Integer getNationalYear() {
        return nationalYear;
    }

    public Integer getMinPlayers() {
        return minPlayers;
    }

    public Integer getMaxPlayers() {
        return maxPlayers;
    }

    public Integer getPlayingTimeMinutes() {
        return playingTimeMinutes;
    }

    public Integer getMinAge() {
        return minAge;
    }

    public String getLink() {
        return link;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getMechanics() {
        return mechanics;
    }

    public List<String> getCategories() {
        return categories;
    }

    public List<String> getThemes() {
        return themes;
    }

    public List<String> getDesigners() {
        return designers;
    }

    public List<String> getArtists() {
        return artists;
    }

    public Instant getLastSyncedAt() {
        return lastSyncedAt;
    }
}
