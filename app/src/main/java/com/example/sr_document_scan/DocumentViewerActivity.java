package com.example.sr_document_scan;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.List;

public class DocumentViewerActivity extends AppCompatActivity {

    private LinearLayout documentsContainer;
    private DocumentFolder currentFolder;
    private String folderId;
    private String folderName;
    private DocumentStorageManager storageManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_document_viewer);

        // Initialize storage manager
        storageManager = new DocumentStorageManager(this);

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Get folder information from intent
        folderId = getIntent().getStringExtra("FOLDER_ID");
        folderName = getIntent().getStringExtra("FOLDER_NAME");

        getSupportActionBar().setTitle(folderName);

        documentsContainer = findViewById(R.id.documents_container);

        // Load the folder
        loadFolder();
    }

    private void loadFolder() {
        // Load folder from storage
        currentFolder = storageManager.getFolderById(folderId);

        if (currentFolder != null) {
            // Display the documents
            displayDocuments();
        } else {
            Toast.makeText(this, "Folder not found", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void displayDocuments() {
        documentsContainer.removeAllViews();
        LayoutInflater inflater = getLayoutInflater();

        List<ScannedDocument> documents = currentFolder.getDocuments();
        if (documents.isEmpty()) {
            // Show empty state
            TextView emptyView = new TextView(this);
            emptyView.setText("No documents found");
            emptyView.setPadding(16, 16, 16, 16);
            documentsContainer.addView(emptyView);
            return;
        }

        for (ScannedDocument doc : documents) {
            // Inflate a custom layout for each document
            View documentView = inflater.inflate(R.layout.document_item_detailed, documentsContainer, false);

            CardView docCard = documentView.findViewById(R.id.document_card);
            ImageView imageView = documentView.findViewById(R.id.document_image);
            TextView nameView = documentView.findViewById(R.id.document_name);
            TextView timestampView = documentView.findViewById(R.id.document_timestamp);
            ImageView editButton = documentView.findViewById(R.id.edit_button);

            // Set document information
            nameView.setText(doc.getName());
            timestampView.setText(doc.getTimestamp());

            // Load the image
            Glide.with(this)
                    .load(doc.getImageUri())
                    .into(imageView);

            // Set click listener for edit button
            editButton.setOnClickListener(v -> showEditDocumentDialog(doc));

            // Set click listener for document to view full-screen
            docCard.setOnClickListener(v -> viewDocument(doc));

            // Add the view to the container
            documentsContainer.addView(documentView);
        }

        // Add PDF viewer button if PDF exists
        File pdfFile = storageManager.getPdfFile(currentFolder.getUniqueId(), currentFolder.getName());
        if (pdfFile != null && pdfFile.exists()) {
            Button viewPdfButton = new Button(this);
            viewPdfButton.setText("View PDF");
            viewPdfButton.setOnClickListener(v -> openPdf(pdfFile));
            documentsContainer.addView(viewPdfButton);
        }
    }

    private void openPdf(File pdfFile) {
        try {
            Uri pdfUri = FileProvider.getUriForFile(
                    this,
                    getApplicationContext().getPackageName() + ".provider",
                    pdfFile);

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(pdfUri, "application/pdf");
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "No PDF viewer app found: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showEditDocumentDialog(ScannedDocument document) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Document Name");

        // Set up the input
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText(document.getName());
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String name = input.getText().toString().trim();
                if (!name.isEmpty()) {
                    document.setName(name);
                    // Save the update
                    storageManager.updateFolder(currentFolder);
                    displayDocuments(); // Refresh the view
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

    private void viewDocument(ScannedDocument document) {
        // Open document in full-screen viewer
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(document.getName());

        View view = getLayoutInflater().inflate(R.layout.document_fullscreen, null);
        ImageView imageView = view.findViewById(R.id.fullscreen_image);

        Glide.with(this)
                .load(document.getImageUri())
                .into(imageView);

        builder.setView(view);
        builder.setPositiveButton("Close", null);
        builder.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.document_viewer_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
            return true;
        } else if (id == R.id.action_rename_folder) {
            showRenameFolderDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showRenameFolderDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Rename Folder");

        // Set up the input
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText(currentFolder.getName());
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String name = input.getText().toString().trim();
                if (!name.isEmpty()) {
                    currentFolder.setName(name);
                    getSupportActionBar().setTitle(name);

                    // Save the renamed folder
                    storageManager.updateFolder(currentFolder);
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
}