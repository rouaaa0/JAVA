package com.example.pi;


import com.gluonhq.attach.storage.StorageService;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class MemoryStorageService implements StorageService {

    private final Map<String, byte[]> storage = new HashMap<>();

    @Override
    public Optional<File> getPrivateStorage() {
        return Optional.empty();
    }

    @Override
    public Optional<File> getPublicStorage(String s) {
        return Optional.empty();
    }

    @Override
    public boolean isExternalStorageWritable() {
        return false;
    }

    @Override
    public boolean isExternalStorageReadable() {
        return false;
    }
}
