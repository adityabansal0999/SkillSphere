package com.skillsphere.app.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.skillsphere.app.R;
import com.skillsphere.app.adapters.MemberAdapter;
import com.skillsphere.app.databinding.FragmentMembersBinding;
import com.skillsphere.app.models.User;
import com.skillsphere.app.utils.Constants;
import com.skillsphere.app.utils.SessionManager;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class MembersFragment extends Fragment {

    private static final String ARG_PROJECT_ID = "project_id";
    private static final String ARG_OWNER_ID = "owner_id";

    private String projectId;
    private String ownerId;
    private FragmentMembersBinding binding;
    private FirebaseFirestore db;
    private SessionManager sessionManager;
    private MemberAdapter membersAdapter;
    private List<User> memberList;

    public static MembersFragment newInstance(String projectId, String ownerId) {
        MembersFragment fragment = new MembersFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PROJECT_ID, projectId);
        args.putString(ARG_OWNER_ID, ownerId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            projectId = getArguments().getString(ARG_PROJECT_ID);
            ownerId = getArguments().getString(ARG_OWNER_ID);
        }
        db = FirebaseFirestore.getInstance();
        sessionManager = SessionManager.getInstance(requireContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentMembersBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        memberList = new ArrayList<>();
        membersAdapter = new MemberAdapter(requireContext(), memberList);

        binding.rvMembers.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvMembers.setAdapter(membersAdapter);

        String currentUserId = sessionManager.getUserId();
        if (currentUserId != null && currentUserId.equals(ownerId)) {
            binding.fabInvite.setVisibility(View.VISIBLE);
            binding.fabInvite.setOnClickListener(v -> showInviteDialog());
        } else {
            binding.fabInvite.setVisibility(View.GONE);
        }

        loadMembers();
    }

    @SuppressWarnings("unchecked")
    private void loadMembers() {
        if (projectId == null) return;
        binding.progressBar.setVisibility(View.VISIBLE);

        db.collection(Constants.COLLECTION_PROJECTS)
                .document(projectId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!isAdded()) return;
                    List<String> memberIds = (List<String>) documentSnapshot.get("members");
                    if (memberIds == null || memberIds.isEmpty()) {
                        binding.progressBar.setVisibility(View.GONE);
                        binding.tvEmpty.setVisibility(View.VISIBLE);
                        return;
                    }
                    fetchMemberDetails(memberIds);
                })
                .addOnFailureListener(e -> {
                    if (isAdded()) binding.progressBar.setVisibility(View.GONE);
                });
    }

    private void fetchMemberDetails(List<String> memberIds) {
        memberList.clear();
        final int[] loadedCount = {0};

        for (String memberId : memberIds) {
            db.collection(Constants.COLLECTION_USERS)
                    .document(memberId)
                    .get()
                    .addOnSuccessListener(doc -> {
                        if (!isAdded()) return;
                        User user = doc.toObject(User.class);
                        if (user != null) {
                            user.setId(doc.getId());  // correct method name
                            memberList.add(user);
                        }
                        loadedCount[0]++;
                        if (loadedCount[0] == memberIds.size()) {
                            binding.progressBar.setVisibility(View.GONE);
                            if (memberList.isEmpty()) {
                                binding.tvEmpty.setVisibility(View.VISIBLE);
                            } else {
                                binding.tvEmpty.setVisibility(View.GONE);
                            }
                            membersAdapter.notifyDataSetChanged();
                        }
                    });
        }
    }

    private void showInviteDialog() {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_invite_member, null);
        EditText etEmail = dialogView.findViewById(R.id.et_invite_email);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Invite Member")
                .setView(dialogView)
                .setPositiveButton("Invite", null)
                .setNegativeButton("Cancel", null)
                .create();

        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(v -> {
                    String email = etEmail.getText().toString().trim();
                    if (email.isEmpty()) { etEmail.setError("Enter email"); return; }
                    inviteMemberByEmail(email, dialog);
                }));

        dialog.show();
    }

    private void inviteMemberByEmail(String email, AlertDialog dialog) {
        db.collection(Constants.COLLECTION_USERS)
                .whereEqualTo("email", email)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!isAdded()) return;
                    if (querySnapshot.isEmpty()) {
                        Toast.makeText(requireContext(), "User not found", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String invitedUserId = querySnapshot.getDocuments().get(0).getId();
                    db.collection(Constants.COLLECTION_PROJECTS)
                            .document(projectId)
                            .update("members", FieldValue.arrayUnion(invitedUserId))
                            .addOnSuccessListener(aVoid -> {
                                if (!isAdded()) return;
                                Toast.makeText(requireContext(), "Member invited!", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                                loadMembers();
                            })
                            .addOnFailureListener(e -> {
                                if (isAdded())
                                    Toast.makeText(requireContext(), "Failed to invite", Toast.LENGTH_SHORT).show();
                            });
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
