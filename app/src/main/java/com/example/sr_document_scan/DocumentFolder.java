package com.example.sr_document_scan;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DocumentFolder {
    private String name;
    private String timestamp;
    private List<ScannedDocument> documents;
    private String uniqueId;

    public DocumentFolder() {
        this.documents = new ArrayList<>();
        this.uniqueId = String.valueOf(System.currentTimeMillis());

        // Generate timestamp in format Apr 23, 2025 14:30:45
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm:ss", Locale.getDefault());
        this.timestamp = sdf.format(new Date());

        // Default name based on date/time
        SimpleDateFormat nameSdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
        this.name = "Scan_" + nameSdf.format(new Date());
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public List<ScannedDocument> getDocuments() {
        return documents;
    }

    public void addDocument(ScannedDocument document) {
        documents.add(document);
    }

    public String getUniqueId() {
        return uniqueId;
    }

    // Add this setter method for uniqueId
    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }
}