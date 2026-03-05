package com.epam.songservice.controller;

import com.epam.songservice.dto.SongDto;
import com.epam.songservice.dto.SongIdDto;
import com.epam.songservice.dto.SongMetaCreateDto;
import com.epam.songservice.exception.ValidationException;
import com.epam.songservice.service.SongService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/songs")
@RequiredArgsConstructor
public class SongController {
    private final SongService service;

    @PostMapping
    public ResponseEntity<SongIdDto> createSong(@RequestBody SongDto dto) {
        var song = service.createSong(dto);
        return ResponseEntity.ok(new SongIdDto(song.getId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SongDto> getSong(@PathVariable Long id) {
        var song = service.getSong(id);
        var dto = new SongDto();
        BeanUtils.copyProperties(song, dto);
        return ResponseEntity.ok(dto);
    }

    @DeleteMapping
    public ResponseEntity<Map<String, List<Long>>> deleteSongs(@RequestParam String id) {
        if (id.length() > 200) throw new ValidationException("CSV too long");
        List<Long> ids = Arrays.stream(id.split(","))
                .map(Long::parseLong)
                .toList();
        List<Long> deleted = service.deleteSongs(ids);
        return ResponseEntity.ok(Map.of("ids", deleted));
    }

    // Endpoints used by Resource Service via Feign client
    @PostMapping("/metadata")
    public ResponseEntity<Void> createSongMeta(@RequestBody SongMetaCreateDto meta) {
        // Convert to SongDto and save
        SongDto dto = new SongDto();
        dto.setId(meta.getResourceId());
        dto.setName(meta.getTitle());
        dto.setArtist(meta.getArtist());
        dto.setAlbum(meta.getAlbum());
        dto.setDuration(meta.getDuration());
        dto.setYear(meta.getReleaseYear());
        service.createSong(dto);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/metadata")
    public ResponseEntity<Void> deleteSongMetas(@RequestParam List<Long> resourceIds) {
        service.deleteSongs(resourceIds);
        return ResponseEntity.ok().build();
    }
}