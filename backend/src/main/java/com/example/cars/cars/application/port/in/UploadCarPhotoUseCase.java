package com.example.cars.cars.application.port.in;

public interface UploadCarPhotoUseCase {

    /**
     * Stores the image and returns the URL to save as a car's {@code photoUrl}.
     * The URL is relative to the API host, so it survives a change of domain.
     */
    String upload(UploadCarPhotoCommand command);
}
