package com.riffsphere.backend.models;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    private String email;
    private String personalityType;
    private String currentMood;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "user_id")
    private List<Playlist> customPlaylists = new ArrayList<>();

    @ElementCollection
    private List<String> favoriteSongIds = new ArrayList<>();

    @ElementCollection
    private List<String> historySongIds = new ArrayList<>();
}
