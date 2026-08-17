package com.example.cars.cars.application.port.out;

import com.example.cars.cars.domain.CarPhoto;

/**
 * Outbound port for image transcoding. Storing every photo in one format is an
 * application decision; which encoder performs it is not, so the codec stays
 * behind this contract.
 */
public interface ImageConverterPort {

    /** Returns the photo re-encoded as WebP, or the same instance if it already is. */
    CarPhoto toWebp(CarPhoto photo);
}
