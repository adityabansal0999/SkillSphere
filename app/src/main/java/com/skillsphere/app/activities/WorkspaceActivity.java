package com.skillsphere.app.activities;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.skillsphere.app.databinding.ActivityWorkspaceBinding;
import com.skillsphere.app.models.Project;

public class WorkspaceActivity extends AppCompatActivity {

    private ActivityWorkspaceBinding binding;
    private FirebaseFirestore db;
    private String projectId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWorkspaceBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        projectId = getIntent().getStringExtra("PROJECT_ID");

        if (projectId != null) {
            loadProjectInfo(projectId);
        }

        binding.btnViewChat.setOnClickListener(v ->
                Toast.makeText(this, "Chat loading...", Toast.LENGTH_SHORT).show());

        binding.btnViewTasks.setOnClickListener(v ->
                Toast.makeText(this, "Task list loading...", Toast.LENGTH_SHORT).show());
    }

    private void loadProjectInfo(String id) {
        db.collection("projects").document(id).get()
                .addOnSuccessListener(documentSnapshot -> {
                    Project project = documentSnapshot.toObject(Project.class);
                    if (project != null) {
                        binding.tvProjectTitle.setText(project.getTitle());
                    }
                });
    }
}