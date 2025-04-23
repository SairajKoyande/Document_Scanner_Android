package com.example.sr_document_scan;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions;
import com.google.mlkit.vision.documentscanner.GmsDocumentScanner;
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning;
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private LinearLayout foldersContainer;
    private List<DocumentFolder> documentFolders = new ArrayList<>();
    private DocumentFolder currentFolder;
    private DocumentStorageManager storageManager;

    private final ActivityResultLauncher<IntentSenderRequest> scannerLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartIntentSenderForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK) {
                            GmsDocumentScanningResult scanningResult =
                                    GmsDocumentScanningResult.fromActivityResultIntent(result.getData());

                            if (scanningResult != null) {
                                // Create a new folder for this scan
                                currentFolder = new DocumentFolder();

                                // Prompt user to name the folder
                                showFolderNameDialog(currentFolder);

                                if (scanningResult.getPages() != null && !scanningResult.getPages().isEmpty()) {
                                    // Add each scanned page to the folder
                                    for (int i = 0; i < scanningResult.getPages().size(); i++) {
                                        Uri pageUri = scanningResult.getPages().get(i).getImageUri();
                                        currentFolder.addDocument(new ScannedDocument(pageUri));
                                    }
                                }

                                if (scanningResult.getPdf() != null) {
                                    try {
                                        // Save PDF
                                        InputStream inputStream = getContentResolver().openInputStream(scanningResult.getPdf().getUri());
                                        if (inputStream != null) {
                                            File pdfFile = storageManager.savePdf(inputStream, currentFolder.getUniqueId(), currentFolder.getName());

                                            Toast.makeText(getApplicationContext(),
                                                    "PDF saved with " + scanningResult.getPdf().getPageCount() + " pages",
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    } catch (Exception e) {
                                        Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                }

                                // Add the folder to our list and display it
                                documentFolders.add(currentFolder);
                                storageManager.updateFolder(currentFolder);
                                displayDocumentFolders();
                            }
                        }
                    }
            );

    private final ActivityResultLauncher<Intent> documentViewerLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        // Reload folders after document viewer activity
                        loadFolders();
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize storage manager
        storageManager = new DocumentStorageManager(this);

        foldersContainer = findViewById(R.id.folders_container);
        Button scanButton = findViewById(R.id.scan_button);

        // Configure the document scanner options
        GmsDocumentScannerOptions options = new GmsDocumentScannerOptions.Builder()
                .setScannerMode(GmsDocumentScannerOptions.SCANNER_MODE_FULL)
                .setPageLimit(5) // Increased page limit
                .setGalleryImportAllowed(true) // Allow gallery import
                .setResultFormats(
                        GmsDocumentScannerOptions.RESULT_FORMAT_JPEG,
                        GmsDocumentScannerOptions.RESULT_FORMAT_PDF
                )
                .build();

        // Get the scanner client
        GmsDocumentScanner scanner = GmsDocumentScanning.getClient(options);

        // Set up the scan button
        scanButton.setOnClickListener(view -> {
            scanner.getStartScanIntent(this)
                    .addOnSuccessListener(new OnSuccessListener<IntentSender>() {
                        @Override
                        public void onSuccess(IntentSender intentSender) {
                            IntentSenderRequest request = new IntentSenderRequest.Builder(intentSender).build();
                            scannerLauncher.launch(request);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        });

        // Load existing folders
        loadFolders();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadFolders();
    }

    private void loadFolders() {
        documentFolders = storageManager.loadFolders();
        displayDocumentFolders();
    }

    private void showFolderNameDialog(final DocumentFolder folder) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter Document Name");

        // Set up the input
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText(folder.getName());
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String name = input.getText().toString().trim();
                if (!name.isEmpty()) {
                    folder.setName(name);
                    storageManager.updateFolder(folder);
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void displayDocumentFolders() {
        foldersContainer.removeAllViews();
        LayoutInflater inflater = getLayoutInflater();

        if (documentFolders.isEmpty()) {
            TextView emptyView = new TextView(this);
            emptyView.setText("No documents found. Tap 'Scan Document' to get started.");
            emptyView.setPadding(16, 16, 16, 16);
            foldersContainer.addView(emptyView);
            return;
        }

        for (DocumentFolder folder : documentFolders) {
            // Inflate a custom layout for each folder
            View folderView = inflater.inflate(R.layout.folder_item, foldersContainer, false);

            CardView folderCard = folderView.findViewById(R.id.folder_card);
            TextView folderName = folderView.findViewById(R.id.folder_name);
            TextView folderTimestamp = folderView.findViewById(R.id.folder_timestamp);
            TextView documentCount = folderView.findViewById(R.id.document_count);
            ImageView thumbnailView = folderView.findViewById(R.id.folder_thumbnail);

            // Set folder information
            folderName.setText(folder.getName());
            folderTimestamp.setText(folder.getTimestamp());
            documentCount.setText(folder.getDocuments().size() + " pages");

            // Set a thumbnail if available
            if (!folder.getDocuments().isEmpty()) {
                Glide.with(this)
                        .load(folder.getDocuments().get(0).getImageUri())
                        .into(thumbnailView);
            }

            // Set click listener to open folder
            folderCard.setOnClickListener(v -> openFolder(folder));

            // Add the view to the container
            foldersContainer.addView(folderView);
        }
    }

    private void openFolder(DocumentFolder folder) {
        if (folder != null && folder.getUniqueId() != null) {
            // Launch document viewer activity
            Intent intent = new Intent(this, DocumentViewerActivity.class);
            intent.putExtra("FOLDER_ID", folder.getUniqueId());
            intent.putExtra("FOLDER_NAME", folder.getName());
            documentViewerLauncher.launch(intent);
        }
    }
}