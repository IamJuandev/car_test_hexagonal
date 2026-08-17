package com.example.cars.cars.application.service;

import com.example.cars.cars.application.port.in.UploadCarPhotoCommand;
import com.example.cars.cars.application.port.out.PhotoStoragePort;
import com.example.cars.cars.domain.CarPhoto;
import com.example.cars.shared.domain.InvalidInputException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CarPhotoServiceTest {

    @Mock
    private PhotoStoragePort photoStorage;

    private CarPhotoService service;

    @BeforeEach
    void setUp() {
        service = new CarPhotoService(photoStorage);
    }

    @Test
    void stores_a_valid_image_and_returns_the_url_the_storage_reports() {
        when(photoStorage.store(any(CarPhoto.class))).thenReturn("/uploads/generated-name.png");

        String url = service.upload(new UploadCarPhotoCommand(new byte[] {1, 2, 3}, "image/png"));

        assertThat(url).isEqualTo("/uploads/generated-name.png");
    }

    @Test
    void hands_the_storage_a_photo_with_a_generated_name() {
        when(photoStorage.store(any(CarPhoto.class))).thenReturn("/uploads/whatever.jpg");
        ArgumentCaptor<CarPhoto> stored = ArgumentCaptor.forClass(CarPhoto.class);

        service.upload(new UploadCarPhotoCommand(new byte[] {9}, "image/jpeg"));

        verify(photoStorage).store(stored.capture());
        assertThat(stored.getValue().storedFileName()).endsWith(".jpg");
    }

    /** Validation belongs to the domain, so nothing ever reaches the disk. */
    @Test
    void does_not_touch_the_storage_when_the_file_is_not_a_supported_image() {
        assertThatThrownBy(() -> service.upload(new UploadCarPhotoCommand(new byte[] {1}, "text/html")))
                .isInstanceOf(InvalidInputException.class);

        verify(photoStorage, never()).store(any());
    }
}
