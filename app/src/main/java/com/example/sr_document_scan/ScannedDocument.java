package com.example.sr_document_scan;

import android.net.Uri;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ScannedDocument {
    private Uri imageUri;
    private String timestamp;
    private String name;

    public ScannedDocument(Uri imageUri) {
        this.imageUri = imageUri;

        // Create timestamp in the format: Apr 23, 2025 14:30:45
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm:ss", Locale.getDefault());
        this.timestamp = sdf.format(new Date());

        // Create a default name using timestamp
        SimpleDateFormat nameSdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
        this.name = "Page_" + nameSdf.format(new Date());
    }

    public Uri getImageUri() {
        return imageUri;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}