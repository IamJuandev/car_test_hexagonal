package com.example.cars.cars.infrastructure.adapter.out.image;

import com.example.cars.cars.application.port.out.ImageConverterPort;
import com.example.cars.cars.domain.CarPhoto;
import com.example.cars.shared.domain.InvalidInputException;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * Re-encodes uploads to WebP through ImageIO, using the native encoder from
 * {@code com.github.usefulness:webp-imageio}.
 *
 * <p>An animated GIF keeps only its first frame: ImageIO reads a single image
 * and the encoder writes a still one. That is an accepted trade-off here —
 * photos of cars are not animations.
 */
@Component
public class ImageIoWebpConverterAdapter implements ImageConverterPort {

    private static final String WEBP_CONTENT_TYPE = "image/webp";

    @Override
    public CarPhoto toWebp(CarPhoto photo) {
        if (WEBP_CONTENT_TYPE.equals(photo.contentType())) {
            return photo;
        }

        BufferedImage image = read(photo.content());
        return CarPhoto.of(encodeAsWebp(image), WEBP_CONTENT_TYPE);
    }

    private BufferedImage read(byte[] content) {
        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(content));
            if (image == null) {
                // A file whose extension or declared type lies about its content.
                throw new InvalidInputException("The image could not be read");
            }
            return image;
        } catch (IOException e) {
            throw new InvalidInputException("The image could not be read");
        }
    }

    private byte[] encodeAsWebp(BufferedImage image) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            if (!ImageIO.write(image, "webp", out)) {
                throw new IllegalStateException(
                        "No WebP writer available: the native encoder did not load");
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Could not encode the image as WebP", e);
        }
        return out.toByteArray();
    }
}
