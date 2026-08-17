package com.example.cars.cars.infrastructure.adapter.in.web;

import com.example.cars.cars.application.port.in.UploadCarPhotoCommand;
import com.example.cars.cars.application.port.in.UploadCarPhotoUseCase;
import com.example.cars.cars.infrastructure.adapter.in.web.dto.PhotoUploadResponse;
import com.example.cars.shared.domain.InvalidInputException;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * Upload endpoint for car photos. It only turns the multipart request into a
 * command; whether the file is acceptable is decided by the domain.
 *
 * <p>The returned URL is what the client then sends as the car's
 * {@code photoUrl}, which is why pasting an external link keeps working
 * unchanged: both paths end in the same field.
 */
@RestController
@RequestMapping("/api/cars/photos")
public class CarPhotoController {

    private final UploadCarPhotoUseCase uploadCarPhoto;

    public CarPhotoController(UploadCarPhotoUseCase uploadCarPhoto) {
        this.uploadCarPhoto = uploadCarPhoto;
    }

    @Operation(summary = "Upload a car photo",
            description = "Accepts a JPEG, PNG, WebP or GIF file up to 5 MB and returns the URL to store as photoUrl.")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public PhotoUploadResponse upload(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidInputException("The image file is empty");
        }
        try {
            String url = uploadCarPhoto.upload(
                    new UploadCarPhotoCommand(file.getBytes(), file.getContentType()));
            return new PhotoUploadResponse(url);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read the uploaded image", e);
        }
    }
}
