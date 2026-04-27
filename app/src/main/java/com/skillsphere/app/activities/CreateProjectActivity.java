package com.skillsphere.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.skillsphere.app.databinding.ActivityCreateProjectBinding;
import com.skillsphere.app.models.Project;
import com.skillsphere.app.utils.SessionManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnSaveProject.setOnClickListener(v -> saveProject());
    }

    private void saveProject() {
        String title = binding.etProjectTitle.getText().toString().trim();
        String desc = binding.etProjectDescription.getText().toString().trim();
        String cat = binding.etCategory.getText().toString().trim();

        if (auth.getCurrentUser() == null) return;
        String userId = auth.getCurrentUser().getUid();
        String userName = SessionManager.getInstance(this).getUserName();

        if (title.isEmpty()) { binding.etProjectTitle.setError("Title required"); return; }
        if (desc.isEmpty()) { binding.etProjectDescription.setError("Description required"); return; }
        if (cat.isEmpty()) { binding.etCategory.setError("Category required"); return; }

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnSaveProject.setEnabled(false);

        ArrayList<String> members = new ArrayList<>();
        members.add(userId);

        // Simple parsing of category as a single item list if needed, 
        // but here we treat 'category' as a String and 'skillsRequired' as a List
        List<String> skills = Arrays.asList(cat.split(",\\s*"));

        Project newProject = new Project("", title, desc, userId, skills, cat);
        newProject.setLeadName(userName != null ? userName : "Unknown");
        newProject.setMaxMembers(5);
        newProject.setVisibility("public");
        newProject.setMembers(members);

        db.collection("projects").add(newProject)
                .addOnSuccessListener(documentReference -> {
                    String projectId = documentReference.getId();
                    documentReference.update("id", projectId)
                            .addOnCompleteListener(task -> {
                                binding.progressBar.setVisibility(View.GONE);
                                Toast.makeText(this, "Project created successfully!", Toast.LENGTH_SHORT).show();
                                
                                Intent intent = new Intent(this, ProjectDetailActivity.class);
                                intent.putExtra("PROJECT_ID", projectId);
                                startActivity(intent);
                                finish();
                            });
                })
                .addOnFailureListener(e -> {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.btnSaveProject.setEnabled(true);
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
