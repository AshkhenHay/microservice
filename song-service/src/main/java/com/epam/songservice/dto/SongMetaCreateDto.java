package com.epam.songservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SongMetaCreateDto {
    private Long resourceId;
    private String title;
    private String artist;
    private String album;
    private String duration;
    private String releaseYear;
}

