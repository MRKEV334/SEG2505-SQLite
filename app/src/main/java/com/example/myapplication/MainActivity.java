package com.example.myapplication;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private FeedReaderDbHelper dbHelper;
    private EditText editId, editTitle, editSubtitle;
    private TextView textResults;

    private final ExecutorService executor =
            Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Garder les champs visibles sous les barres du téléphone.
        android.view.View root =
                findViewById(android.R.id.content);

        ViewCompat.setOnApplyWindowInsetsListener(root, (view, insets) -> {
            Insets bars = insets.getInsets(
                    WindowInsetsCompat.Type.systemBars());

            view.setPadding(
                    bars.left, bars.top, bars.right, bars.bottom);

            return insets;
        });
        ViewCompat.requestApplyInsets(root);

        dbHelper = new FeedReaderDbHelper(getApplicationContext());

        editId = findViewById(R.id.editId);
        editTitle = findViewById(R.id.editTitle);
        editSubtitle = findViewById(R.id.editSubtitle);
        textResults = findViewById(R.id.textResults);

        findViewById(R.id.buttonAdd).setOnClickListener(v -> {
            if (!validateTitle()) {
                return;
            }

            String title = editTitle.getText().toString().trim();
            String subtitle = editSubtitle.getText().toString().trim();

            executeDatabaseTask(() -> {
                long id = dbHelper.addEntry(title, subtitle);

                if (id == -1) {
                    return "Échec de l'ajout.";
                }

                return "Entrée ajoutée. Identifiant : " + id
                        + "\n\n" + dbHelper.readEntries();
            });
        });

        findViewById(R.id.buttonRead).setOnClickListener(v ->
                executeDatabaseTask(() -> dbHelper.readEntries()));

        findViewById(R.id.buttonUpdate).setOnClickListener(v -> {
            Long id = readId();

            if (id == null || !validateTitle()) {
                return;
            }

            String title = editTitle.getText().toString().trim();
            String subtitle = editSubtitle.getText().toString().trim();

            executeDatabaseTask(() -> {
                int count = dbHelper.updateEntry(id, title, subtitle);

                return (count > 0
                        ? "Entrée modifiée."
                        : "Identifiant introuvable.")
                        + "\n\n" + dbHelper.readEntries();
            });
        });

        findViewById(R.id.buttonDelete).setOnClickListener(v -> {
            Long id = readId();

            if (id == null) {
                return;
            }

            executeDatabaseTask(() -> {
                int count = dbHelper.deleteEntry(id);

                return (count > 0
                        ? "Entrée supprimée."
                        : "Identifiant introuvable.")
                        + "\n\n" + dbHelper.readEntries();
            });
        });
    }

    private boolean validateTitle() {
        if (editTitle.getText().toString().trim().isEmpty()) {
            editTitle.setError("Saisis un titre.");
            return false;
        }

        return true;
    }

    private Long readId() {
        try {
            long id = Long.parseLong(
                    editId.getText().toString().trim());

            if (id <= 0) {
                editId.setError("Saisis un identifiant positif.");
                return null;
            }

            return id;
        } catch (NumberFormatException e) {
            editId.setError("Saisis un identifiant valide.");
            return null;
        }
    }

    private interface DatabaseTask {
        String run();
    }

    private void executeDatabaseTask(DatabaseTask task) {
        executor.execute(() -> {
            String message;

            try {
                message = task.run();
            } catch (Exception e) {
                message = "Erreur SQLite : " + e.getMessage();
            }

            final String result = message;

            runOnUiThread(() -> {
                if (!isFinishing() && !isDestroyed()) {
                    textResults.setText(result);
                }
            });
        });
    }

    @Override
    protected void onDestroy() {
        // Fermer la base après les opérations déjà en attente.
        executor.execute(() -> dbHelper.close());
        executor.shutdown();
        super.onDestroy();
    }
}