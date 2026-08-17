package com.example.cars.cars.domain;

import com.example.cars.shared.domain.InvalidInputException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CarPhotoTest {

    private static final byte[] SOME_BYTES = {1, 2, 3, 4};

    @Test
    void accepts_a_supported_image_and_derives_its_extension() {
        CarPhoto photo = CarPhoto.of(SOME_BYTES, "image/png");

        assertThat(photo.storedFileName()).endsWith(".png");
        assertThat(photo.content()).isEqualTo(SOME_BYTES);
    }

    @Test
    void ignores_the_charset_suffix_a_browser_may_add_to_the_content_type() {
        CarPhoto photo = CarPhoto.of(SOME_BYTES, "IMAGE/JPEG; charset=binary");

        assertThat(photo.storedFileName()).endsWith(".jpg");
    }

    @Test
    void never_reuses_a_name_so_two_uploads_cannot_overwrite_each_other() {
        CarPhoto first = CarPhoto.of(SOME_BYTES, "image/png");
        CarPhoto second = CarPhoto.of(SOME_BYTES, "image/png");

        assertThat(first.storedFileName()).isNotEqualTo(second.storedFileName());
    }

    /**
     * The stored name is generated, never taken from the client, so an upload
     * called "../../application.yml" cannot escape the uploads directory.
     */
    @Test
    void generates_a_name_free_of_path_separators() {
        CarPhoto photo = CarPhoto.of(SOME_BYTES, "image/webp");

        assertThat(photo.storedFileName()).doesNotContain("/", "\\", "..");
    }

    @Test
    void rejects_a_content_type_that_is_not_a_supported_image() {
        assertThatThrownBy(() -> CarPhoto.of(SOME_BYTES, "application/pdf"))
                .isInstanceOf(InvalidInputException.class)
                .hasMessageContaining("Unsupported image type");
    }

    @Test
    void rejects_a_missing_content_type() {
        assertThatThrownBy(() -> CarPhoto.of(SOME_BYTES, null))
                .isInstanceOf(InvalidInputException.class);
    }

    @Test
    void rejects_an_empty_file() {
        assertThatThrownBy(() -> CarPhoto.of(new byte[0], "image/png"))
                .isInstanceOf(InvalidInputException.class)
                .hasMessageContaining("empty");
    }

    @Test
    void rejects_a_file_larger_than_the_allowed_size() {
        byte[] tooBig = new byte[CarPhoto.MAX_SIZE_BYTES + 1];

        assertThatThrownBy(() -> CarPhoto.of(tooBig, "image/png"))
                .isInstanceOf(InvalidInputException.class)
                .hasMessageContaining("5 MB");
    }
}
