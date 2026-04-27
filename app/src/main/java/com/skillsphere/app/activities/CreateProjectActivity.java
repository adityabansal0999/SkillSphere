package com.skillsphere.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.skillsphere.app.databinding.ActivityCreateProjectBinding;
import com.skillsphere.app.models.Project;
import com.skillsphere.app.utils.SessionManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Please log in to create a project", Toast.LENGTH_SHORT).show();
            return;
        }

        if (title.isEmpty()) { binding.etProjectTitle.setError("Title required"); return; }
        if (desc.isEmpty()) { binding.etProjectDescription.setError("Description required"); return; }
        if (cat.isEmpty()) { binding.etCategory.setError("Category required"); return; }

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnSaveProject.setEnabled(false);

        String userId = auth.getCurrentUser().getUid();
        String userName = SessionManager.getInstance(this).getUserName();

        // 1. Generate ID and create Project object
        DocumentReference projectRef = db.collection("projects").document();
        String projectId = projectRef.getId();

        ArrayList<String> members = new ArrayList<>();
        members.add(userId);
        List<String> skills = Arrays.asList(cat.split(",\\s*"));

        Project newProject = new Project(projectId, title, desc, userId, skills, cat);
        newProject.setLeadName(userName != null && !userName.isEmpty() ? userName : "Lead");
        newProject.setMaxMembers(5);
        newProject.setMembers(members);
        
        Map<String, Project.MemberDetail> memberDetails = new HashMap<>();
        memberDetails.put(userId, new Project.MemberDetail(
                newProject.getLeadName(), "lead", System.currentTimeMillis()
        ));
        newProject.setMemberDetails(memberDetails);

        // 2. Start save operation (Firestore handles this locally first)
        projectRef.set(newProject);

        // 3. Forced Redirection after 5 seconds to prevent hanging on slow networks
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (!isFinishing()) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(this, "Project Posted Successfully!", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        }, 5000);
    }
}
