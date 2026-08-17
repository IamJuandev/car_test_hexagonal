package com.example.cars.cars.application.service;

import com.example.cars.cars.application.port.in.UploadCarPhotoCommand;
import com.example.cars.cars.application.port.in.UploadCarPhotoUseCase;
import com.example.cars.cars.application.port.out.ImageConverterPort;
import com.example.cars.cars.application.port.out.PhotoStoragePort;
import com.example.cars.cars.domain.CarPhoto;
import org.springframework.stereotype.Service;

/**
 * Validates the incoming file, normalises it to WebP and stores it.
 *
 * <p>Every photo is kept in a single format so the rest of the system never has
 * to care what the browser happened to send. Deliberately thin: the rules are
 * in the domain, the encoding and the writing are in adapters.
 */
@Service
public class CarPhotoService implements UploadCarPhotoUseCase {

    private final PhotoStoragePort photoStorage;
    private final ImageConverterPort imageConverter;

    public CarPhotoService(PhotoStoragePort photoStorage, ImageConverterPort imageConverter) {
        this.photoStorage = photoStorage;
        this.imageConverter = imageConverter;
    }

    @Override
    public String upload(UploadCarPhotoCommand command) {
        CarPhoto photo = CarPhoto.of(command.content(), command.contentType());
        return photoStorage.store(imageConverter.toWebp(photo));
    }
}
