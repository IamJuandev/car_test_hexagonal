package com.example.cars.cars.infrastructure.adapter.out.image;

import com.example.cars.cars.domain.CarPhoto;
import com.example.cars.shared.domain.InvalidInputException;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Exercises the real encoder, native library included: a converter that only
 * passes against a mock would prove nothing about whether WebP actually works
 * on this machine.
 */
class ImageIoWebpConverterAdapterTest {

    private final ImageIoWebpConverterAdapter converter = new ImageIoWebpConverterAdapter();

    @Test
    void converts_a_png_into_a_real_webp_file() throws IOException {
        CarPhoto png = CarPhoto.of(pngBytes(120, 80), "image/png");

        CarPhoto webp = converter.toWebp(png);

        assertThat(webp.contentType()).isEqualTo("image/webp");
        assertThat(webp.storedFileName()).endsWith(".webp");
        assertThat(hasWebpSignature(webp.content()))
                .as("bytes must start with the RIFF....WEBP magic number")
                .isTrue();
    }

    @Test
    void converts_a_jpeg_too() throws IOException {
        CarPhoto jpeg = CarPhoto.of(jpegBytes(64, 64), "image/jpeg");

        CarPhoto webp = converter.toWebp(jpeg);

        assertThat(hasWebpSignature(webp.content())).isTrue();
    }

    /** Re-encoding an image that is already WebP would only lose quality. */
    @Test
    void leaves_an_image_that_is_already_webp_untouched() throws IOException {
        CarPhoto original = converter.toWebp(CarPhoto.of(pngBytes(32, 32), "image/png"));

        CarPhoto result = converter.toWebp(original);

        assertThat(result).isSameAs(original);
    }

    @Test
    void rejects_bytes_that_are_not_a_readable_image() {
        CarPhoto notAnImage = CarPhoto.of("this is not a png".getBytes(), "image/png");

        assertThatThrownBy(() -> converter.toWebp(notAnImage))
                .isInstanceOf(InvalidInputException.class)
                .hasMessageContaining("could not be read");
    }

    private static boolean hasWebpSignature(byte[] bytes) {
        return bytes.length > 12
                && bytes[0] == 'R' && bytes[1] == 'I' && bytes[2] == 'F' && bytes[3] == 'F'
                && bytes[8] == 'W' && bytes[9] == 'E' && bytes[10] == 'B' && bytes[11] == 'P';
    }

    private static byte[] pngBytes(int width, int height) throws IOException {
        return imageBytes(width, height, "png", BufferedImage.TYPE_INT_ARGB);
    }

    private static byte[] jpegBytes(int width, int height) throws IOException {
        return imageBytes(width, height, "jpg", BufferedImage.TYPE_INT_RGB);
    }

    private static byte[] imageBytes(int width, int height, String format, int imageType)
            throws IOException {
        BufferedImage image = new BufferedImage(width, height, imageType);
        Graphics2D graphics = image.createGraphics();
        graphics.setColor(Color.BLUE);
        graphics.fillRect(0, 0, width, height);
        graphics.dispose();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(image, format, out);
        return out.toByteArray();
    }
}
