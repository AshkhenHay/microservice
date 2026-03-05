package com.epam.songservice.service;

import com.epam.songservice.dto.SongDto;
import com.epam.songservice.entity.Song;
import com.epam.songservice.exception.ConflictException;
import com.epam.songservice.exception.NotFoundException;
import com.epam.songservice.exception.ValidationException;
import com.epam.songservice.repository.SongRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SongService {
    private final SongRepository repo;

    public Song createSong(SongDto songDto) {
        if (songDto.getId() != null && songDto.getId() > 0 && repo.existsById(songDto.getId()))
            throw new ConflictException("Metadata for resource ID=" + songDto.getId() + " already exists");
        validate(songDto);
        // validate that resource exists -- call to Resource service, optional if strictly needed
        var song = new Song();
        BeanUtils.copyProperties(songDto, song);
        return repo.save(song);
    }

    public Song getSong(Long id) {
        if (id == null || id < 1)
            throw new ValidationException("ID must be positive");
        return repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Song metadata for ID=" + id + " not found"));
    }

    public List<Long> deleteSongs(List<Long> ids) {
        List<Long> existing = repo.findAllById(ids).stream().map(Song::getId).toList();
        repo.deleteAllById(existing);
        return existing;
    }

    private void validate(SongDto song) {
        Map<String, String> errors = new HashMap<>();
        // Validate name
        if (song.getName() == null || song.getName().isBlank())
            errors.put("name", "Song name is required");
        else if (song.getName().length() > 100)
            errors.put("name", "Song name must be between 1 and 100 characters");

        // Validate artist
        if (song.getArtist() == null || song.getArtist().isBlank())
            errors.put("artist", "Artist name is required");
        else if (song.getArtist().length() > 100)
            errors.put("artist", "Artist name must be between 1 and 100 characters");

        // Validate album
        if (song.getAlbum() == null || song.getAlbum().isBlank())
            errors.put("album", "Album name is required");
        else if (song.getAlbum().length() > 100)
            errors.put("album", "Album name must be between 1 and 100 characters");

        // Validate duration
        if (song.getDuration() == null || song.getDuration().isBlank())
            errors.put("duration", "Duration is required");
        else if (!song.getDuration().matches("\\d{2}:\\d{2}"))
            errors.put("duration", "Duration must be in mm:ss format with leading zeros");

        // Validate year
        if (song.getYear() == null || song.getYear().isBlank())
            errors.put("year", "Year is required");
        else if (!song.getYear().matches("19\\d{2}|20\\d{2}"))
            errors.put("year", "Year must be between 1900 and 2099");

        if (!errors.isEmpty())
            throw new ValidationException("Validation error", errors);
    }
}