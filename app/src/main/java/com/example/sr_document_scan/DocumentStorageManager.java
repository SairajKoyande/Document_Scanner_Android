package com.example.sr_document_scan;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class DocumentStorageManager {
    private static final String TAG = "DocumentStorageMgr";
    private static final String PREF_NAME = "document_folders";
    private static final String KEY_FOLDERS = "folders";

    private final Context context;

    public DocumentStorageManager(@NonNull Context context) {
        this.context = context.getApplicationContext();
    }

    // Save a list of document folders to SharedPreferences
    public void saveFolders(List<DocumentFolder> folders) {
        try {
            SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            JSONArray foldersArray = new JSONArray();

            for (DocumentFolder folder : folders) {
                JSONObject folderObj = new JSONObject();
                folderObj.put("id", folder.getUniqueId());
                folderObj.put("name", folder.getName());
                folderObj.put("timestamp", folder.getTimestamp());

                JSONArray docsArray = new JSONArray();
                for (ScannedDocument doc : folder.getDocuments()) {
                    JSONObject docObj = new JSONObject();
                    docObj.put("uri", doc.getImageUri().toString());
                    docObj.put("name", doc.getName());
                    docObj.put("timestamp", doc.getTimestamp());
                    docsArray.put(docObj);
                }

                folderObj.put("documents", docsArray);
                foldersArray.put(folderObj);
            }

            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(KEY_FOLDERS, foldersArray.toString());
            editor.apply();

            Log.d(TAG, "Saved " + folders.size() + " folders to SharedPreferences");

        } catch (JSONException e) {
            Log.e(TAG, "Error saving folders: " + e.getMessage());
        }
    }

    // Load document folders from SharedPreferences
    public List<DocumentFolder> loadFolders() {
        List<DocumentFolder> folders = new ArrayList<>();

        try {
            SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            String foldersJson = prefs.getString(KEY_FOLDERS, "[]");
            JSONArray foldersArray = new JSONArray(foldersJson);

            Log.d(TAG, "Loading " + foldersArray.length() + " folders from SharedPreferences");

            for (int i = 0; i < foldersArray.length(); i++) {
                JSONObject folderObj = foldersArray.getJSONObject(i);

                DocumentFolder folder = new DocumentFolder();
                // Properly set the uniqueId
                folder.setUniqueId(folderObj.getString("id"));
                folder.setName(folderObj.getString("name"));

                // Get documents array
                JSONArray docsArray = folderObj.getJSONArray("documents");
                for (int j = 0; j < docsArray.length(); j++) {
                    JSONObject docObj = docsArray.getJSONObject(j);
                    Uri uri = Uri.parse(docObj.getString("uri"));

                    ScannedDocument document = new ScannedDocument(uri);
                    document.setName(docObj.getString("name"));
                    folder.addDocument(document);
                }

                folders.add(folder);
                Log.d(TAG, "Loaded folder: " + folder.getName() + " with ID: " + folder.getUniqueId());
            }

        } catch (JSONException e) {
            Log.e(TAG, "Error loading folders: " + e.getMessage());
        }

        return folders;
    }

    // Get a specific folder by ID
    public DocumentFolder getFolderById(String folderId) {
        List<DocumentFolder> folders = loadFolders();
        for (DocumentFolder folder : folders) {
            if (folder.getUniqueId().equals(folderId)) {
                Log.d(TAG, "Found folder with ID: " + folderId);
                return folder;
            }
        }
        Log.e(TAG, "Folder not found with ID: " + folderId);
        return null;
    }

    // Update a specific folder
    public void updateFolder(DocumentFolder folder) {
        if (folder == null || folder.getUniqueId() == null) {
            Log.e(TAG, "Cannot update null folder or folder with null ID");
            return;
        }

        List<DocumentFolder> folders = loadFolders();
        boolean folderFound = false;

        for (int i = 0; i < folders.size(); i++) {
            DocumentFolder existingFolder = folders.get(i);
            if (existingFolder.getUniqueId().equals(folder.getUniqueId())) {
                Log.d(TAG, "Updating existing folder with ID: " + folder.getUniqueId());
                folders.set(i, folder);
                folderFound = true;
                break;
            }
        }

        if (!folderFound) {
            Log.d(TAG, "Adding new folder with ID: " + folder.getUniqueId());
            folders.add(folder);
        }

        saveFolders(folders);
    }

    // Save PDF file
    public File savePdf(InputStream pdfInputStream, String folderId, String fileName) throws IOException {
        File folderDir = new File(context.getFilesDir(), folderId);
        if (!folderDir.exists()) {
            folderDir.mkdirs();
        }

        File pdfFile = new File(folderDir, fileName + ".pdf");
        FileOutputStream fos = new FileOutputStream(pdfFile);

        byte[] buffer = new byte[1024];
        int read;
        while ((read = pdfInputStream.read(buffer)) != -1) {
            fos.write(buffer, 0, read);
        }

        fos.flush();
        fos.close();
        pdfInputStream.close();

        Log.d(TAG, "Saved PDF to: " + pdfFile.getAbsolutePath());

        return pdfFile;
    }

    // Get PDF file for a folder
    public File getPdfFile(String folderId, String fileName) {
        if (folderId == null || fileName == null) {
            Log.e(TAG, "Cannot get PDF file with null folderId or fileName");
            return null;
        }

        File folderDir = new File(context.getFilesDir(), folderId);
        File pdfFile = new File(folderDir, fileName + ".pdf");

        if (pdfFile.exists()) {
            Log.d(TAG, "Found PDF file: " + pdfFile.getAbsolutePath());
            return pdfFile;
        }

        Log.e(TAG, "PDF file not found: " + pdfFile.getAbsolutePath());
        return null;
    }
}