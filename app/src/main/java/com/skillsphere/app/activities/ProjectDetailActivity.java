package com.skillsphere.app.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.skillsphere.app.databinding.ActivityProjectDetailBinding;
import com.skillsphere.app.models.Project;

public class ProjectDetailActivity extends AppCompatActivity {

    private ActivityProjectDetailBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String projectId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProjectDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Get the ID passed from HomeFragment
        projectId = getIntent().getStringExtra("PROJECT_ID");

        if (projectId != null) {
            loadProjectDetails(projectId);
        }

        binding.btnApply.setOnClickListener(v -> applyToProject());
    }

    private void loadProjectDetails(String id) {
        db.collection("projects").document(id).get()
                .addOnSuccessListener(documentSnapshot -> {
                    Project project = documentSnapshot.toObject(Project.class);
                    if (project != null) {
                        binding.tvProjectTitle.setText(project.getTitle());
                        binding.tvProjectDescription.setText(project.getDescription());
                        binding.tvProjectCategory.setText(project.getCategory());

                        // Optional: Check if user is already a member to disable button
                        if (project.isUserMember(auth.getCurrentUser().getUid())) {
                            binding.btnApply.setEnabled(false);
                            binding.btnApply.setText("Already Joined");
                        }
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void applyToProject() {
        if (auth.getCurrentUser() == null) return;

        String currentUserId = auth.getCurrentUser().getUid();

        // Adds the user ID to the 'members' list in Firestore without overwriting existing members
        db.collection("projects").document(projectId)
                .update("members", FieldValue.arrayUnion(currentUserId))
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Successfully joined!", Toast.LENGTH_SHORT).show();
                    binding.btnApply.setEnabled(false);
                    binding.btnApply.setText("Joined");
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to join: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}