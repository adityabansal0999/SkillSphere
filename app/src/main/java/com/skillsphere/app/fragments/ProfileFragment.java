package com.skillsphere.app.fragments;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.material.chip.Chip;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.skillsphere.app.R;
import com.skillsphere.app.activities.EditProfileActivity;
import com.skillsphere.app.activities.LoginActivity;
import com.skillsphere.app.databinding.FragmentProfileBinding;
import com.skillsphere.app.models.User;
import com.skillsphere.app.utils.Constants;
import com.skillsphere.app.utils.SessionManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private FirebaseStorage storage;
    private SessionManager sessionManager;
    private User currentUser;

    private boolean isLoadingData = false;

    private final ActivityResultLauncher<Intent> editProfileLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> loadUserProfile()
    );

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    if (imageUri != null) {
                        uploadProfileImage(imageUri);
                    }
                }
            }
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        sessionManager = SessionManager.getInstance(requireContext());

        setupListeners();
        loadUserProfile();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUserProfile();
    }

    private void setupListeners() {
        binding.btnEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), EditProfileActivity.class);
            editProfileLauncher.launch(intent);
        });

        binding.ivProfile.setOnClickListener(v -> showImageOptions());
        binding.tvInitials.setOnClickListener(v -> showImageOptions());
        binding.btnChangePhoto.setOnClickListener(v -> showImageOptions());

        binding.btnLogout.setOnClickListener(v -> confirmLogout());

        binding.btnChangePassword.setOnClickListener(v ->
                Toast.makeText(getContext(),
                        "Password reset email sent to " + (currentUser != null ? currentUser.getEmail() : "your email"),
                        Toast.LENGTH_LONG).show()
        );

        // ── TOGGLES ──────────────────────────────────────────────────────────
        
        // Push Notifications
        binding.switchPush.setOnCheckedChangeListener((btn, isChecked) -> {
            if (isLoadingData) return;
            sessionManager.setPushNotifications(isChecked);
            updateFirestoreSetting("pushNotifications", isChecked);
            showToast("Notifications " + (isChecked ? "Enabled" : "Disabled"));
        });

        // Do Not Disturb
        binding.switchDnd.setOnCheckedChangeListener((btn, isChecked) -> {
            if (isLoadingData) return;
            sessionManager.setDoNotDisturb(isChecked);
            updateFirestoreSetting("doNotDisturb", isChecked);
            showToast("Do Not Disturb " + (isChecked ? "Enabled" : "Disabled"));
        });

        // Show Online Status
        binding.switchOnline.setOnCheckedChangeListener((btn, isChecked) -> {
            if (isLoadingData) return;
            sessionManager.setShowOnlineStatus(isChecked);
            updateFirestoreSetting("showOnlineStatus", isChecked);
            showToast("Online Status " + (isChecked ? "Visible" : "Hidden"));
        });

        // Appear in Discover
        binding.switchDiscover.setOnCheckedChangeListener((btn, isChecked) -> {
            if (isLoadingData) return;
            sessionManager.setAppearInDiscover(isChecked);
            updateFirestoreSetting("appearInDiscover", isChecked);
            showToast("Privacy Mode " + (isChecked ? "Disabled" : "Enabled"));
        });
    }

    private void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void loadUserProfile() {
        if (auth.getCurrentUser() == null) return;
        String userId = auth.getCurrentUser().getUid();

        db.collection(Constants.COLLECTION_USERS).document(userId).get()
                .addOnSuccessListener(doc -> {
                    if (!isAdded()) return;
                    if (doc.exists()) {
                        currentUser = doc.toObject(User.class);
                        if (currentUser != null) {
                            currentUser.setId(doc.getId());
                            sessionManager.saveUser(currentUser);
                            populateUI(currentUser);
                        }
                    } else {
                        currentUser = new User(userId, "", auth.getCurrentUser().getEmail());
                        populateUI(currentUser);
                    }
                })
                .addOnFailureListener(e -> {
                    if (isAdded())
                        Toast.makeText(getContext(), "Failed to load profile", Toast.LENGTH_SHORT).show();
                });
    }

    private void populateUI(User user) {
        isLoadingData = true;

        binding.tvName.setText(safeStr(user.getName(), "No Name"));
        binding.tvEmail.setText(safeStr(user.getEmail(), ""));

        String bioPreview = safeStr(user.getBio(), "");
        if (!bioPreview.isEmpty()) {
            binding.tvBioPreview.setVisibility(View.VISIBLE);
            binding.tvBioPreview.setText(bioPreview.length() > 60
                    ? bioPreview.substring(0, 57) + "…" : bioPreview);
        } else {
            binding.tvBioPreview.setVisibility(View.GONE);
        }

        String institution = safeStr(user.getUniversity(), "");
        if (!institution.isEmpty()) {
            binding.tvInstitution.setVisibility(View.VISIBLE);
            binding.tvInstitution.setText(institution);
        } else {
            binding.tvInstitution.setVisibility(View.GONE);
        }

        if (user.getPhotoUrl() != null && !user.getPhotoUrl().isEmpty()) {
            binding.tvInitials.setVisibility(View.GONE);
            binding.ivProfile.setVisibility(View.VISIBLE);
            Glide.with(this)
                    .load(user.getPhotoUrl())
                    .placeholder(R.drawable.bg_avatar_circle)
                    .into(binding.ivProfile);
        } else {
            binding.ivProfile.setVisibility(View.GONE);
            binding.tvInitials.setVisibility(View.VISIBLE);
            binding.tvInitials.setText(user.getInitials());
        }

        renderSkillChips(user.getSkills());

        // Visual state restoration
        binding.switchPush.setChecked(user.isPushNotifications());
        binding.switchDnd.setChecked(user.isDoNotDisturb());
        binding.switchOnline.setChecked(user.isShowOnlineStatus());
        binding.switchDiscover.setChecked(user.isAppearInDiscover());

        isLoadingData = false;
    }

    private void renderSkillChips(List<String> skills) {
        binding.chipGroupSkills.removeAllViews();
        if (skills == null || skills.isEmpty()) {
            binding.tvSkillsEmpty.setVisibility(View.VISIBLE);
            binding.chipGroupSkills.setVisibility(View.GONE);
        } else {
            binding.tvSkillsEmpty.setVisibility(View.GONE);
            binding.chipGroupSkills.setVisibility(View.VISIBLE);
            for (String skill : skills) {
                Chip chip = new Chip(requireContext());
                chip.setText(skill);
                chip.setCheckable(false);
                chip.setClickable(false);
                chip.setChipBackgroundColorResource(R.color.chip_green_bg);
                chip.setTextColor(getResources().getColor(R.color.chip_green_text, null));
                binding.chipGroupSkills.addView(chip);
            }
        }
    }

    private void showImageOptions() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Profile Photo")
                .setItems(new String[]{"Choose from Gallery", "Remove Photo", "Cancel"},
                        (dialog, which) -> {
                            if (which == 0) {
                                Intent intent = new Intent(Intent.ACTION_PICK);
                                intent.setType("image/*");
                                imagePickerLauncher.launch(intent);
                            } else if (which == 1) {
                                removeProfileImage();
                            }
                        })
                .show();
    }

    private void uploadProfileImage(Uri imageUri) {
        if (auth.getCurrentUser() == null) return;
        String userId = auth.getCurrentUser().getUid();

        binding.photoProgressBar.setVisibility(View.VISIBLE);
        StorageReference fileRef = storage.getReference()
                .child(Constants.STORAGE_PATH_PROFILE + userId + ".jpg");

        fileRef.putFile(imageUri).continueWithTask(task -> {
            if (!task.isSuccessful()) {
                throw task.getException();
            }
            return fileRef.getDownloadUrl();
        }).addOnSuccessListener(uri -> {
            String url = uri.toString();
            Map<String, Object> map = new HashMap<>();
            map.put("photoUrl", url);
            db.collection(Constants.COLLECTION_USERS).document(userId)
                    .set(map, SetOptions.merge())
                    .addOnSuccessListener(v -> {
                        if (!isAdded()) return;
                        binding.photoProgressBar.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "Photo updated!", Toast.LENGTH_SHORT).show();
                        loadUserProfile();
                    });
        }).addOnFailureListener(e -> {
            if (!isAdded()) return;
            binding.photoProgressBar.setVisibility(View.GONE);
            Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void removeProfileImage() {
        if (auth.getCurrentUser() == null) return;
        String userId = auth.getCurrentUser().getUid();

        binding.photoProgressBar.setVisibility(View.VISIBLE);
        Map<String, Object> map = new HashMap<>();
        map.put("photoUrl", null);
        db.collection(Constants.COLLECTION_USERS).document(userId)
                .set(map, SetOptions.merge())
                .addOnSuccessListener(v -> {
                    if (!isAdded()) return;
                    binding.photoProgressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Photo removed", Toast.LENGTH_SHORT).show();
                    loadUserProfile();
                })
                .addOnFailureListener(e -> {
                    if (!isAdded()) return;
                    binding.photoProgressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Failed to remove photo", Toast.LENGTH_SHORT).show();
                });
    }

    private void updateFirestoreSetting(String key, Object value) {
        if (auth.getCurrentUser() == null) return;
        Map<String, Object> map = new HashMap<>();
        map.put(key, value);
        db.collection(Constants.COLLECTION_USERS)
                .document(auth.getCurrentUser().getUid())
                .set(map, SetOptions.merge())
                .addOnFailureListener(e -> {
                    if (isAdded())
                        Toast.makeText(getContext(), "Setting not saved: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void confirmLogout() {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.logout_title)
                .setMessage(R.string.logout_message)
                .setPositiveButton(R.string.logout, (dialog, which) -> {
                    auth.signOut();
                    sessionManager.logout();
                    Intent intent = new Intent(getContext(), LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private String safeStr(String value, String fallback) {
        return (value != null && !value.isEmpty()) ? value : fallback;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
