package com.meetingnotes.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

/**
 * Abstraction over file storage. The default implementation writes to local
 * disk ({@link LocalStorageService}); swap in an S3-backed implementation for
 * production without touching the rest of the app.
 */
public interface StorageService {

    /**
     * Persist an uploaded file and return an opaque storage key that can later
     * be passed to {@link #load(String)}.
     */
    String store(MultipartFile file);

    /** Load a previously stored file as a Spring Resource. */
    Resource load(String storageKey);

    /** Delete a stored file. No-op if it doesn't exist. */
    void delete(String storageKey);
}
