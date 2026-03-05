package com.epam.songservice.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "songs")
public class Song {
    @Id
    private Long id; // resource ID, shared

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 100)
    private String artist;

    @Column(length = 100)
    private String album;

    @Column(nullable = false)
    private String duration;

    @Column(nullable = false, length = 4)
    private String year;
}