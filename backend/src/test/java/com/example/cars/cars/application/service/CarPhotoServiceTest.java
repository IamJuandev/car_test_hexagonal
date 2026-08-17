package com.example.cars.cars.application.service;

import com.example.cars.cars.application.port.in.UploadCarPhotoCommand;
import com.example.cars.cars.application.port.out.ImageConverterPort;
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
    @Mock
    private ImageConverterPort imageConverter;

    private CarPhotoService service;

    @BeforeEach
    void setUp() {
        service = new CarPhotoService(photoStorage, imageConverter);
    }

    @Test
    void stores_a_valid_image_and_returns_the_url_the_storage_reports() {
        when(imageConverter.toWebp(any(CarPhoto.class))).thenAnswer(call -> call.getArgument(0));
        when(photoStorage.store(any(CarPhoto.class))).thenReturn("/uploads/generated-name.webp");

        String url = service.upload(new UploadCarPhotoCommand(new byte[] {1, 2, 3}, "image/png"));

        assertThat(url).isEqualTo("/uploads/generated-name.webp");
    }

    /** Whatever the browser sent, what reaches the disk is the converted photo. */
    @Test
    void stores_the_converted_photo_and_never_the_original() {
        CarPhoto converted = CarPhoto.of(new byte[] {7, 7}, "image/webp");
        when(imageConverter.toWebp(any(CarPhoto.class))).thenReturn(converted);
        when(photoStorage.store(any(CarPhoto.class))).thenReturn("/uploads/whatever.webp");
        ArgumentCaptor<CarPhoto> stored = ArgumentCaptor.forClass(CarPhoto.class);

        service.upload(new UploadCarPhotoCommand(new byte[] {9}, "image/jpeg"));

        verify(photoStorage).store(stored.capture());
        assertThat(stored.getValue()).isSameAs(converted);
        assertThat(stored.getValue().storedFileName()).endsWith(".webp");
    }

    /** Validation belongs to the domain, so nothing is converted or written. */
    @Test
    void does_not_convert_or_store_when_the_file_is_not_a_supported_image() {
        assertThatThrownBy(() -> service.upload(new UploadCarPhotoCommand(new byte[] {1}, "text/html")))
                .isInstanceOf(InvalidInputException.class);

        verify(imageConverter, never()).toWebp(any());
        verify(photoStorage, never()).store(any());
    }
}
