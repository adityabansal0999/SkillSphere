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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.skillsphere.app.R;
import com.skillsphere.app.activities.EditProfileActivity;
import com.skillsphere.app.activities.LoginActivity;
import com.skillsphere.app.databinding.FragmentProfileBinding;
import com.skillsphere.app.models.User;
import com.skillsphere.app.utils.Constants;
import com.skillsphere.app.utils.SessionManager;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private FirebaseStorage storage;
    private SessionManager sessionManager;
    private User currentUser;

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
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
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

    private void setupListeners() {
        binding.btnLogout.setOnClickListener(v -> confirmLogout());
        
        binding.btnEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), EditProfileActivity.class);
            startActivity(intent);
        });

        binding.btnChangePhoto.setOnClickListener(v -> showImageOptions());

        binding.switchPush.setOnCheckedChangeListener((buttonView, isChecked) -> updateUserSetting("pushNotifications", isChecked));
        binding.switchOnline.setOnCheckedChangeListener((buttonView, isChecked) -> updateUserSetting("showOnlineStatus", isChecked));
        binding.switchDiscover.setOnCheckedChangeListener((buttonView, isChecked) -> updateUserSetting("appearInDiscover", isChecked));
    }

    private void showImageOptions() {
        String[] options = {"Change Photo", "Remove Photo", "Cancel"};
        new AlertDialog.Builder(requireContext())
                .setTitle("Profile Photo")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        Intent intent = new Intent(Intent.ACTION_PICK);
                        intent.setType("image/*");
                        imagePickerLauncher.launch(intent);
                    } else if (which == 1) {
                        removeProfileImage();
                    }
                }).show();
    }

    private void uploadProfileImage(Uri imageUri) {
        if (auth.getCurrentUser() == null) return;
        String userId = auth.getCurrentUser().getUid();
        
        binding.photoProgressBar.setVisibility(View.VISIBLE);
        StorageReference fileRef = storage.getReference().child("profile_photos/" + userId + ".jpg");

        fileRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String url = uri.toString();
                    db.collection(Constants.COLLECTION_USERS).document(userId)
                            .update("photoUrl", url)
                            .addOnSuccessListener(aVoid -> {
                                if (isAdded()) {
                                    binding.photoProgressBar.setVisibility(View.GONE);
                                    Toast.makeText(getContext(), "Photo updated!", Toast.LENGTH_SHORT).show();
                                    loadUserProfile();
                                }
                            });
                }))
                .addOnFailureListener(e -> {
                    if (isAdded()) {
                        binding.photoProgressBar.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "Upload failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void removeProfileImage() {
        if (auth.getCurrentUser() == null) return;
        String userId = auth.getCurrentUser().getUid();

        binding.photoProgressBar.setVisibility(View.VISIBLE);
        db.collection(Constants.COLLECTION_USERS).document(userId)
                .update("photoUrl", null)
                .addOnSuccessListener(aVoid -> {
                    if (isAdded()) {
                        binding.photoProgressBar.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "Photo removed", Toast.LENGTH_SHORT).show();
                        loadUserProfile();
                    }
                });
    }

    private void loadUserProfile() {
        if (auth.getCurrentUser() == null) return;
        String userId = auth.getCurrentUser().getUid();

        db.collection(Constants.COLLECTION_USERS).document(userId).get()
                .addOnSuccessListener(doc -> {
                    if (!isAdded()) return;
                    currentUser = doc.toObject(User.class);
                    if (currentUser != null) {
                        currentUser.setId(doc.getId());
                        populateUI(currentUser);
                    }
                });
    }

    private void populateUI(User user) {
        binding.tvName.setText(user.getName());
        binding.tvEmail.setText(user.getEmail());

        if (user.getPhotoUrl() != null && !user.getPhotoUrl().isEmpty()) {
            binding.tvInitials.setVisibility(View.GONE);
            binding.ivProfile.setVisibility(View.VISIBLE);
            Glide.with(this).load(user.getPhotoUrl()).into(binding.ivProfile);
        } else {
            binding.ivProfile.setVisibility(View.GONE);
            binding.tvInitials.setVisibility(View.VISIBLE);
            binding.tvInitials.setText(user.getInitials());
        }

        binding.switchPush.setChecked(user.isPushNotifications());
        binding.switchOnline.setChecked(user.isShowOnlineStatus());
        binding.switchDiscover.setChecked(user.isAppearInDiscover());
    }

    private void updateUserSetting(String key, boolean value) {
        if (auth.getCurrentUser() == null) return;
        db.collection(Constants.COLLECTION_USERS).document(auth.getCurrentUser().getUid()).update(key, value);
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
