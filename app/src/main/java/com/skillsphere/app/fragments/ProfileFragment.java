package com.skillsphere.app.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.skillsphere.app.R;
import com.skillsphere.app.activities.EditProfileActivity;
import com.skillsphere.app.activities.LoginActivity;
import com.skillsphere.app.databinding.FragmentProfileBinding;
import com.skillsphere.app.models.User;
import com.skillsphere.app.utils.Constants;
import com.skillsphere.app.utils.SessionManager;

import java.util.HashMap;
import java.util.Map;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private SessionManager sessionManager;
    private User currentUser;

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

        binding.btnChangePassword.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Change Password coming soon", Toast.LENGTH_SHORT).show();
        });

        // Toggle switches with Firestore updates
        binding.switchPush.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateUserSetting("pushNotifications", isChecked);
        });

        binding.switchOnline.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateUserSetting("showOnlineStatus", isChecked);
        });

        binding.switchDiscover.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateUserSetting("appearInDiscover", isChecked);
        });
        
        binding.switchDnd.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Toast.makeText(getContext(), "DND " + (isChecked ? "Enabled" : "Disabled"), Toast.LENGTH_SHORT).show();
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
                })
                .addOnFailureListener(e -> {
                    if (isAdded()) Toast.makeText(getContext(), "Failed to load profile", Toast.LENGTH_SHORT).show();
                });
    }

    private void populateUI(User user) {
        binding.tvInitials.setText(user.getInitials());
        binding.tvName.setText(user.getName() != null ? user.getName() : "");
        binding.tvEmail.setText(user.getEmail() != null ? user.getEmail() : "");

        // Set switches state from user data
        binding.switchPush.setChecked(user.isPushNotifications());
        binding.switchOnline.setChecked(user.isShowOnlineStatus());
        binding.switchDiscover.setChecked(user.isAppearInDiscover());
    }

    private void updateUserSetting(String key, boolean value) {
        if (auth.getCurrentUser() == null) return;
        String userId = auth.getCurrentUser().getUid();

        db.collection(Constants.COLLECTION_USERS).document(userId)
                .update(key, value)
                .addOnFailureListener(e -> {
                    if (isAdded()) Toast.makeText(getContext(), "Failed to update setting", Toast.LENGTH_SHORT).show();
                });
    }

    private void confirmLogout() {
        new AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.logout_title))
                .setMessage(getString(R.string.logout_message))
                .setPositiveButton(getString(R.string.logout), (dialog, which) -> {
                    auth.signOut();
                    sessionManager.logout();
                    Intent intent = new Intent(getContext(), LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .setNegativeButton(getString(R.string.cancel), null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
