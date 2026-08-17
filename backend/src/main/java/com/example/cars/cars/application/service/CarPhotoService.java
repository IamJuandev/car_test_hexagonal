package com.example.cars.cars.application.service;

import com.example.cars.cars.application.port.in.UploadCarPhotoCommand;
import com.example.cars.cars.application.port.in.UploadCarPhotoUseCase;
import com.example.cars.cars.application.port.out.PhotoStoragePort;
import com.example.cars.cars.domain.CarPhoto;
import org.springframework.stereotype.Service;

/**
 * Turns an incoming file into a validated {@link CarPhoto} and hands it to the
 * storage port. Deliberately thin: the rules are in the domain, the writing is
 * in the adapter.
 */
@Service
public class CarPhotoService implements UploadCarPhotoUseCase {

    private final PhotoStoragePort photoStorage;

    public CarPhotoService(PhotoStoragePort photoStorage) {
        this.photoStorage = photoStorage;
    }

    @Override
    public String upload(UploadCarPhotoCommand command) {
        CarPhoto photo = CarPhoto.of(command.content(), command.contentType());
        return photoStorage.store(photo);
    }
}
