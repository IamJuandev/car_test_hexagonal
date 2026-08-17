package com.example.cars.cars.application.port.in;

/** The raw image as it arrived, before the domain decides whether it is acceptable. */
public record UploadCarPhotoCommand(byte[] content, String contentType) {
}
