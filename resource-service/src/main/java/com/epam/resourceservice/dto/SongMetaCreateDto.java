package com.epam.resourceservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SongMetaCreateDto {
    private Long id;
    private String name;
    private String artist;
    private String album;
    private String duration;
    private String year;
}

