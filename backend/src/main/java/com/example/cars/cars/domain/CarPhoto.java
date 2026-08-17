package com.example.cars.cars.domain;

import com.example.cars.shared.domain.InvalidInputException;

import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * An image file on its way to storage. Everything that decides whether a photo
 * is acceptable lives here, so the rules hold no matter which adapter received
 * the bytes: an HTTP multipart request today, a queue or a CLI tomorrow.
 *
 * <p>The stored name is always generated. A client-supplied name is never
 * trusted, which is what keeps an upload called {@code ../../application.yml}
 * from escaping the uploads directory.
 */
public class CarPhoto {

    public static final int MAX_SIZE_BYTES = 5 * 1024 * 1024;

    private static final Map<String, String> EXTENSION_BY_CONTENT_TYPE = Map.of(
            "image/jpeg", "jpg",
            "image/png", "png",
            "image/webp", "webp",
            "image/gif", "gif");

    private final byte[] content;
    private final String contentType;
    private final String storedFileName;

    private CarPhoto(byte[] content, String contentType, String storedFileName) {
        this.content = content;
        this.contentType = contentType;
        this.storedFileName = storedFileName;
    }

    public static CarPhoto of(byte[] content, String contentType) {
        if (content == null || content.length == 0) {
            throw new InvalidInputException("The image file is empty");
        }
        if (content.length > MAX_SIZE_BYTES) {
            throw new InvalidInputException("The image must not be larger than 5 MB");
        }

        String normalizedType = normalize(contentType);
        String extension = EXTENSION_BY_CONTENT_TYPE.get(normalizedType);
        if (extension == null) {
            throw new InvalidInputException(
                    "Unsupported image type: use JPEG, PNG, WebP or GIF");
        }

        return new CarPhoto(content, normalizedType, UUID.randomUUID() + "." + extension);
    }

    /** Strips the parameters browsers append, e.g. {@code image/jpeg; charset=binary}. */
    private static String normalize(String contentType) {
        if (contentType == null || contentType.isBlank()) {
            throw new InvalidInputException("The image content type is missing");
        }
        int parameterStart = contentType.indexOf(';');
        String mediaType = parameterStart < 0 ? contentType : contentType.substring(0, parameterStart);
        return mediaType.trim().toLowerCase(Locale.ROOT);
    }

    public byte[] content() {
        return content;
    }

    public String contentType() {
        return contentType;
    }

    public String storedFileName() {
        return storedFileName;
    }
}
