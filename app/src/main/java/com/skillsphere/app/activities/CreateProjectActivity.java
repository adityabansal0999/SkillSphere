package com.skillsphere.app.activities;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.skillsphere.app.databinding.ActivityCreateProjectBinding;
import com.skillsphere.app.models.Project;
import java.util.ArrayList;

public class CreateProjectActivity extends AppCompatActivity {

    private ActivityCreateProjectBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCreateProjectBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        binding.btnSaveProject.setOnClickListener(v -> saveProject());
    }

    private void saveProject() {
        String title = binding.etProjectTitle.getText().toString().trim();
        String desc = binding.etProjectDescription.getText().toString().trim();
        String cat = binding.etCategory.getText().toString().trim();
        String userId = auth.getCurrentUser().getUid();

        if (title.isEmpty() || desc.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Using your Project model
        Project newProject = new Project("", title, desc, userId, new ArrayList<>(), cat);

        db.collection("projects").add(newProject)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Project Posted Successfully!", Toast.LENGTH_SHORT).show();
                    finish(); // Close activity
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}