package com.example.cars.cars.infrastructure.adapter.out.storage;

import com.example.cars.cars.application.port.out.PhotoStoragePort;
import com.example.cars.cars.domain.CarPhoto;
import com.example.cars.shared.domain.InvalidInputException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * Writes photos to a directory on disk, served back by
 * {@code StaticUploadsConfig} under {@code /uploads/**}.
 *
 * <p>Local storage is fine for this application: it is single-node and the
 * files are small. On more than one instance the directory would have to be a
 * shared volume, or this adapter replaced by an object-storage one, which is
 * exactly the swap the port exists for.
 */
@Component
public class FileSystemPhotoStorageAdapter implements PhotoStoragePort {

    public static final String PUBLIC_PATH = "/uploads";

    private final Path directory;

    public FileSystemPhotoStorageAdapter(@Value("${app.uploads.dir}") String directory) {
        this.directory = Path.of(directory).toAbsolutePath().normalize();
    }

    @Override
    public String store(CarPhoto photo) {
        Path target = directory.resolve(photo.storedFileName()).normalize();
        if (!target.startsWith(directory)) {
            // Unreachable while names are generated; kept so a future change to
            // naming cannot silently turn into a path traversal.
            throw new InvalidInputException("Invalid image name");
        }
        try {
            Files.createDirectories(directory);
            Files.write(target, photo.content(),
                    StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not store the image", e);
        }
        return PUBLIC_PATH + "/" + photo.storedFileName();
    }
}
