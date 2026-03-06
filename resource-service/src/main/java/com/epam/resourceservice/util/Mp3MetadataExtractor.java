package com.epam.resourceservice.util;

import com.epam.resourceservice.dto.SongMetaCreateDto;
import com.epam.resourceservice.exception.ValidationException;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaCoreProperties;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.audio.AudioParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public class Mp3MetadataExtractor {
    private static final Logger log = LoggerFactory.getLogger(Mp3MetadataExtractor.class);

    public static SongMetaCreateDto extractMetadata(byte[] mp3Data, Long resourceId) {
        try {
            Metadata metadata = new Metadata();
            ContentHandler handler = new DefaultHandler();
            ParseContext context = new ParseContext();

            AudioParser parser = new AudioParser();
            parser.parse(new ByteArrayInputStream(mp3Data), handler, metadata, context);

            SongMetaCreateDto dto = new SongMetaCreateDto();
            dto.setId(resourceId);

            // Extract title
            String title = metadata.get(TikaCoreProperties.TITLE);
            dto.setName(title != null ? title : "Unknown Title");

            // Extract artist
            String artist = metadata.get("xmpDM:artist");
            if (artist == null) {
                artist = metadata.get("Author");
            }
            dto.setArtist(artist != null ? artist : "Unknown Artist");

            // Extract album
            String album = metadata.get("xmpDM:album");
            dto.setAlbum(album != null ? album : "Unknown Album");

            // Extract duration and convert to mm:ss format
            String duration = metadata.get("xmpDM:duration");
            if (duration != null) {
                try {
                    double durationSeconds = Double.parseDouble(duration);
                    int minutes = (int) (durationSeconds / 1000) / 60;
                    int seconds = ((int) (durationSeconds / 1000)) % 60;
                    dto.setDuration(String.format("%02d:%02d", minutes, seconds));
                } catch (NumberFormatException e) {
                    log.warn("Could not parse duration: {}", duration);
                    dto.setDuration("00:00");
                }
            } else {
                dto.setDuration("00:00");
            }

            // Extract year
            String year = metadata.get(TikaCoreProperties.CREATED);
            if (year != null && year.length() >= 4) {
                year = year.substring(0, 4);
            }
            if (year == null || year.isEmpty()) {
                year = metadata.get("xmpDM:releaseDate");
                if (year != null && year.length() >= 4) {
                    year = year.substring(0, 4);
                }
            }
            dto.setYear(year != null && !year.isEmpty() ? year : "2025");

            log.info("Extracted metadata for resource {}: {} by {}", resourceId, dto.getName(), dto.getArtist());
            return dto;
        } catch (TikaException | SAXException | IOException e) {
            log.error("Failed to extract MP3 metadata", e);
            throw new ValidationException("Invalid MP3 file format or corrupted metadata");
        }
    }
}

