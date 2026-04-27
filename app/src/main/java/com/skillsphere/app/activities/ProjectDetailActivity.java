package com.skillsphere.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.skillsphere.app.databinding.ActivityProjectDetailBinding;
import com.skillsphere.app.models.Project;
import com.skillsphere.app.models.Request;
import com.skillsphere.app.utils.Constants;
import com.skillsphere.app.utils.FirebaseHelper;
import com.skillsphere.app.utils.SessionManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ProjectDetailActivity extends AppCompatActivity {

    private ActivityProjectDetailBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String projectId;
    private Project currentProject;

    // Button states
    private static final String STATE_APPLY = "apply";
    private static final String STATE_REQUESTED = "requested";
    private static final String STATE_MEMBER = "member";
    private static final String STATE_LEAD = "lead";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProjectDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        projectId = getIntent().getStringExtra("PROJECT_ID");

        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnDelete.setOnClickListener(v -> showDeleteConfirmation());

        if (projectId != null) {
            loadProjectDetails(projectId);
        }
    }

    private void loadProjectDetails(String id) {
        binding.progressBar.setVisibility(View.VISIBLE);
        db.collection("projects").document(id).get()
                .addOnSuccessListener(documentSnapshot -> {
                    binding.progressBar.setVisibility(View.GONE);
                    currentProject = documentSnapshot.toObject(Project.class);
                    if (currentProject != null) {
                        currentProject.setId(documentSnapshot.getId());
                        populateUI(currentProject);
                        checkUserState(currentProject);
                    }
                })
                .addOnFailureListener(e -> {
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void populateUI(Project project) {
        binding.tvProjectTitle.setText(project.getTitle());
        binding.tvProjectDescription.setText(project.getDescription());
        binding.tvProjectCategory.setText(project.getCategory() != null ? project.getCategory() : "");
        binding.tvLeadName.setText(project.getLeadName() != null ? project.getLeadName() : "Unknown");

        String membersText = project.getMembers() != null
                ? project.getMembers().size() + "/" + project.getMaxMembers() + " members"
                : "0 members";
        binding.tvMembersCount.setText(membersText);

        String status = project.getStatus() != null ? project.getStatus() : "forming";
        binding.tvStatus.setText(status.substring(0, 1).toUpperCase() + status.substring(1));

        if (project.getSkillsRequired() != null && !project.getSkillsRequired().isEmpty()) {
            List<String> cleanSkills = new ArrayList<>();
            for (String s : project.getSkillsRequired()) {
                if (s != null && !s.contains("@") && !s.isEmpty()) {
                    cleanSkills.add(s.trim());
                }
            }
            if (!cleanSkills.isEmpty()) {
                binding.tvSkillsRequired.setText(String.join(" · ", cleanSkills));
            } else {
                binding.tvSkillsRequired.setText("Various skills");
            }
        } else {
            binding.tvSkillsRequired.setText("Any skills welcome");
        }
    }

    private void checkUserState(Project project) {
        if (auth.getCurrentUser() == null) return;
        String userId = auth.getCurrentUser().getUid();

        boolean isLead = project.isUserLead(userId);
        binding.btnDelete.setVisibility(isLead ? View.VISIBLE : View.GONE);

        if (isLead) {
            setButtonState(STATE_LEAD);
        } else if (project.isUserMember(userId)) {
            setButtonState(STATE_MEMBER);
        } else {
            // Check if user already sent a request
            db.collection(Constants.COLLECTION_REQUESTS)
                    .whereEqualTo("fromUserId", userId)
                    .whereEqualTo("projectId", projectId)
                    .whereEqualTo("status", Constants.STATUS_PENDING)
                    .get()
                    .addOnSuccessListener(query -> {
                        if (!query.isEmpty()) {
                            setButtonState(STATE_REQUESTED);
                        } else {
                            setButtonState(STATE_APPLY);
                        }
                    });
        }
    }

    private void setButtonState(String state) {
        switch (state) {
            case STATE_APPLY:
                binding.btnApply.setEnabled(true);
                binding.btnApply.setText("Apply to Join");
                binding.btnApply.setOnClickListener(v -> sendJoinRequest());
                break;
            case STATE_REQUESTED:
                binding.btnApply.setEnabled(false);
                binding.btnApply.setText("Request Sent ✓");
                break;
            case STATE_MEMBER:
            case STATE_LEAD:
                binding.btnApply.setEnabled(true);
                binding.btnApply.setText("Open Workspace →");
                binding.btnApply.setOnClickListener(v -> openWorkspace());
                break;
        }
    }

    private void showDeleteConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Project")
                .setMessage("Are you sure you want to delete this project? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> deleteProject())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteProject() {
        binding.progressBar.setVisibility(View.VISIBLE);
        db.collection("projects").document(projectId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Project deleted successfully", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Failed to delete project: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void sendJoinRequest() {
        if (auth.getCurrentUser() == null || currentProject == null) return;

        String userId = auth.getCurrentUser().getUid();
        String userName = SessionManager.getInstance(this).getUserName();
        String leadId = currentProject.getLeadId();

        // Create request document
        Request request = new Request();
        request.setType(Constants.REQUEST_TYPE_JOIN);
        request.setFromUserId(userId);
        request.setFromUserName(userName != null ? userName : "Unknown");
        request.setToUserId(leadId);
        request.setProjectId(projectId);
        request.setProjectTitle(currentProject.getTitle());
        request.setStatus(Constants.STATUS_PENDING);
        request.setCreatedAt(System.currentTimeMillis());

        db.collection(Constants.COLLECTION_REQUESTS)
                .add(request)
                .addOnSuccessListener(docRef -> {
                    request.setId(docRef.getId());
                    // Send notification to lead
                    FirebaseHelper.sendNotification(
                            leadId,
                            Constants.NOTIF_TYPE_JOIN_REQUEST,
                            "New Join Request",
                            userName + " wants to join " + currentProject.getTitle(),
                            projectId,
                            currentProject.getTitle(),
                            userId,
                            userName != null ? userName : "Unknown"
                    );

                    Toast.makeText(this, "Request sent to project lead!", Toast.LENGTH_SHORT).show();
                    setButtonState(STATE_REQUESTED);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to send request: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void openWorkspace() {
        Intent intent = new Intent(this, WorkspaceActivity.class);
        intent.putExtra("PROJECT_ID", projectId);
        if (currentProject != null) {
            intent.putExtra("LEAD_ID", currentProject.getLeadId());
        }
        startActivity(intent);
    }
}
