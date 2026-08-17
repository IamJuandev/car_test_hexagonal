package com.example.cars.cars.application.port.out;

import com.example.cars.cars.domain.CarPhoto;

/**
 * Outbound port for image storage. The local filesystem implements it today;
 * swapping in S3 or Azure Blob means writing one adapter and changing nothing
 * else.
 */
public interface PhotoStoragePort {

    /** Persists the photo and returns the URL under which it is served. */
    String store(CarPhoto photo);
}
