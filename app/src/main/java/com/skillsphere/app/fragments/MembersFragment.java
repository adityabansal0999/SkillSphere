package com.example.skillsphere.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.skillsphere.R;
import com.example.skillsphere.adapters.PeopleAdapter;
import com.example.skillsphere.models.User;
import com.example.skillsphere.utils.Constants;
import com.example.skillsphere.utils.SessionManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class MembersFragment extends Fragment {

    private static final String ARG_PROJECT_ID = "project_id";
    private static final String ARG_OWNER_ID = "owner_id";

    private String projectId;
    private String ownerId;

    private RecyclerView rvMembers;
    private ProgressBar progressBar;
    private FloatingActionButton fabInvite;

    private FirebaseFirestore db;
    private SessionManager sessionManager;
    private PeopleAdapter membersAdapter;
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
        sessionManager = new SessionManager(requireContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_members, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvMembers = view.findViewById(R.id.rv_members);
        progressBar = view.findViewById(R.id.progress_bar);
        fabInvite = view.findViewById(R.id.fab_invite);

        memberList = new ArrayList<>();
        membersAdapter = new PeopleAdapter(requireContext(), memberList, null);

        rvMembers.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvMembers.setAdapter(membersAdapter);

        // Only project owner can invite
        if (sessionManager.getUserId().equals(ownerId)) {
            fabInvite.setVisibility(View.VISIBLE);
            fabInvite.setOnClickListener(v -> showInviteDialog());
        } else {
            fabInvite.setVisibility(View.GONE);
        }

        loadMembers();
    }

    private void loadMembers() {
        if (projectId == null) return;
        progressBar.setVisibility(View.VISIBLE);

        db.collection(Constants.COLLECTION_PROJECTS)
                .document(projectId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!isAdded()) return;
                    List<String> memberIds = (List<String>) documentSnapshot.get("memberIds");
                    if (memberIds == null || memberIds.isEmpty()) {
                        progressBar.setVisibility(View.GONE);
                        return;
                    }
                    fetchMemberDetails(memberIds);
                })
                .addOnFailureListener(e -> {
                    if (isAdded()) progressBar.setVisibility(View.GONE);
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
                            user.setUserId(doc.getId());
                            memberList.add(user);
                        }
                        loadedCount[0]++;
                        if (loadedCount[0] == memberIds.size()) {
                            progressBar.setVisibility(View.GONE);
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
                .setPositiveButton("Invite", null) // Set null to override later
                .setNegativeButton("Cancel", null)
                .create();

        dialog.setOnShowListener(d -> {
            Button positiveBtn = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveBtn.setOnClickListener(v -> {
                String email = etEmail.getText().toString().trim();
                if (email.isEmpty()) {
                    etEmail.setError("Enter email");
                    return;
                }
                inviteMemberByEmail(email, dialog);
            });
        });

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

                    DocumentSnapshot userDoc = querySnapshot.getDocuments().get(0);
                    String invitedUserId = userDoc.getId();

                    // Add to project memberIds
                    db.collection(Constants.COLLECTION_PROJECTS)
                            .document(projectId)
                            .update("memberIds", com.google.firebase.firestore.FieldValue.arrayUnion(invitedUserId))
                            .addOnSuccessListener(aVoid -> {
                                if (!isAdded()) return;
                                Toast.makeText(requireContext(), "Member invited successfully!", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                                loadMembers();
                            })
                            .addOnFailureListener(e -> {
                                if (isAdded()) Toast.makeText(requireContext(), "Failed to invite", Toast.LENGTH_SHORT).show();
                            });
                });
    }
}