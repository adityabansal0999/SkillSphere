package com.skillsphere.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.chip.Chip;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.skillsphere.app.databinding.ActivityCreateProjectBinding;
import com.skillsphere.app.models.Project;
import com.skillsphere.app.utils.Constants;
import com.skillsphere.app.utils.SessionManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreateProjectActivity extends AppCompatActivity {

    private ActivityCreateProjectBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private List<String> selectedCategories = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCreateProjectBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnSaveProject.setOnClickListener(v -> saveProject());
        binding.btnSelectCategories.setOnClickListener(v -> showCategoryPickerDialog());
    }

    private void showCategoryPickerDialog() {
        List<String> allCategories = Constants.CATEGORIES;
        String[] categoryArray = allCategories.toArray(new String[0]);
        boolean[] checkedItems = new boolean[categoryArray.length];
        for (int i = 0; i < categoryArray.length; i++) {
            checkedItems[i] = selectedCategories.contains(categoryArray[i]);
        }

        new AlertDialog.Builder(this)
                .setTitle("Select Project Categories")
                .setMultiChoiceItems(categoryArray, checkedItems, (dialog, which, isChecked) -> {
                    if (isChecked) {
                        if (!selectedCategories.contains(categoryArray[which]))
                            selectedCategories.add(categoryArray[which]);
                    } else {
                        selectedCategories.remove(categoryArray[which]);
                    }
                })
                .setPositiveButton("Done", (dialog, which) -> updateCategoryChips())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateCategoryChips() {
        binding.chipGroupCategories.removeAllViews();
        for (String category : selectedCategories) {
            Chip chip = new Chip(this);
            chip.setText(category);
            chip.setCloseIconVisible(true);
            chip.setOnCloseIconClickListener(v -> {
                selectedCategories.remove(category);
                binding.chipGroupCategories.removeView(chip);
            });
            binding.chipGroupCategories.addView(chip);
        }
    }

    private void saveProject() {
        String title = binding.etProjectTitle.getText().toString().trim();
        String desc = binding.etProjectDescription.getText().toString().trim();

        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Please log in to create a project", Toast.LENGTH_SHORT).show();
            return;
        }

        if (title.isEmpty()) { binding.etProjectTitle.setError("Title required"); return; }
        if (desc.isEmpty()) { binding.etProjectDescription.setError("Description required"); return; }
        if (selectedCategories.isEmpty()) {
            Toast.makeText(this, "Please select at least one category", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnSaveProject.setEnabled(false);

        String userId = auth.getCurrentUser().getUid();
        String userName = SessionManager.getInstance(this).getUserName();

        DocumentReference projectRef = db.collection("projects").document();
        String projectId = projectRef.getId();

        ArrayList<String> members = new ArrayList<>();
        members.add(userId);

        // Using first category as the primary 'category' field, and the list for skills/tags
        String primaryCategory = selectedCategories.get(0);

        Project newProject = new Project(projectId, title, desc, userId, selectedCategories, primaryCategory);
        newProject.setLeadName(userName != null && !userName.isEmpty() ? userName : "Lead");
        newProject.setMaxMembers(5);
        newProject.setMembers(members);
        
        Map<String, Project.MemberDetail> memberDetails = new HashMap<>();
        memberDetails.put(userId, new Project.MemberDetail(
                newProject.getLeadName(), "lead", System.currentTimeMillis()
        ));
        newProject.setMemberDetails(memberDetails);

        projectRef.set(newProject);

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
