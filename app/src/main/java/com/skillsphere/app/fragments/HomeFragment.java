package com.skillsphere.app.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.skillsphere.app.activities.NotificationsActivity;
import com.skillsphere.app.activities.ProjectDetailActivity;
import com.skillsphere.app.adapters.ProjectAdapter;
import com.skillsphere.app.adapters.RequestAdapter;
import com.skillsphere.app.databinding.FragmentHomeBinding;
import com.skillsphere.app.models.Project;
import com.skillsphere.app.models.Request;
import com.skillsphere.app.utils.Constants;
import com.skillsphere.app.utils.FirebaseHelper;
import com.skillsphere.app.utils.SessionManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeFragment extends Fragment implements RequestAdapter.OnRequestActionListener {

    private FragmentHomeBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private ProjectAdapter projectAdapter;
    private RequestAdapter requestAdapter;
    private List<Project> myProjects;
    private List<Request> pendingRequests;
    
    private ListenerRegistration projectsListener;
    private ListenerRegistration requestsListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        myProjects = new ArrayList<>();
        pendingRequests = new ArrayList<>();

        binding.tvGreeting.setText(FirebaseHelper.getGreeting());
        String userName = SessionManager.getInstance(requireContext()).getUserName();
        binding.tvUserName.setText(userName != null && !userName.isEmpty() ? "Hey, " + userName + " 👋" : "Hey there 👋");

        projectAdapter = new ProjectAdapter(myProjects, project -> {
            Intent intent = new Intent(getContext(), ProjectDetailActivity.class);
            intent.putExtra("PROJECT_ID", project.getId());
            startActivity(intent);
        });
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerView.setAdapter(projectAdapter);

        requestAdapter = new RequestAdapter(requireContext(), pendingRequests, this);
        binding.rvRequests.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvRequests.setAdapter(requestAdapter);

        binding.btnNotifications.setOnClickListener(v -> {
            startActivity(new Intent(getContext(), NotificationsActivity.class));
        });

        startListening();
    }

    private void startListening() {
        if (auth.getCurrentUser() == null) return;
        String userId = auth.getCurrentUser().getUid();

        binding.progressBar.setVisibility(View.VISIBLE);

        projectsListener = db.collection(Constants.COLLECTION_PROJECTS)
                .whereArrayContains("members", userId)
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (!isAdded()) return;
                    binding.progressBar.setVisibility(View.GONE);
                    if (e != null) return;
                    if (queryDocumentSnapshots != null) {
                        myProjects.clear();
                        List<Project> projects = queryDocumentSnapshots.toObjects(Project.class);
                        for (int i = 0; i < projects.size(); i++) {
                            projects.get(i).setId(queryDocumentSnapshots.getDocuments().get(i).getId());
                        }
                        myProjects.addAll(projects);
                        projectAdapter.notifyDataSetChanged();
                        binding.tvNoProjects.setVisibility(myProjects.isEmpty() ? View.VISIBLE : View.GONE);
                    }
                });

        requestsListener = db.collection(Constants.COLLECTION_REQUESTS)
                .whereEqualTo("toUserId", userId)
                .whereEqualTo("status", Constants.STATUS_PENDING)
                .addSnapshotListener((querySnapshot, e) -> {
                    if (!isAdded()) return;
                    if (e != null) return;
                    if (querySnapshot != null) {
                        pendingRequests.clear();
                        for (int i = 0; i < querySnapshot.size(); i++) {
                            Request req = querySnapshot.toObjects(Request.class).get(i);
                            req.setId(querySnapshot.getDocuments().get(i).getId());
                            pendingRequests.add(req);
                        }
                        requestAdapter.notifyDataSetChanged();
                        binding.cardRequests.setVisibility(pendingRequests.isEmpty() ? View.GONE : View.VISIBLE);
                    }
                });
    }

    @Override
    public void onAccept(Request request) {
        if (auth.getCurrentUser() == null) return;
        String currentUserId = auth.getCurrentUser().getUid();

        // 1. Identify who is joining
        String tempUserId;
        String tempUserName;

        if (Constants.REQUEST_TYPE_INVITE.equals(request.getType())) {
            // I was invited -> I am joining
            tempUserId = currentUserId;
            tempUserName = SessionManager.getInstance(requireContext()).getUserName();
        } else {
            // I am the lead, accepting a join request -> the sender is joining
            tempUserId = request.getFromUserId();
            tempUserName = request.getFromUserName();
        }

        final String joiningUserId = tempUserId;
        final String joiningUserName = (tempUserName == null || tempUserName.isEmpty()) ? "New Member" : tempUserName;

        // 2. Use a Transaction to update both Project and Request atomically
        db.runTransaction(transaction -> {
            DocumentReference projectRef = db.collection(Constants.COLLECTION_PROJECTS).document(request.getProjectId());
            DocumentReference requestRef = db.collection(Constants.COLLECTION_REQUESTS).document(request.getId());

            // Prepare member details
            Map<String, Object> details = new HashMap<>();
            details.put("name", joiningUserName);
            details.put("role", "member");
            details.put("joinedAt", System.currentTimeMillis());

            // Update Project: members array + memberDetails map
            transaction.update(projectRef, 
                "members", FieldValue.arrayUnion(joiningUserId),
                "memberDetails." + joiningUserId, details
            );

            // Update Request status
            transaction.update(requestRef, 
                "status", Constants.STATUS_ACCEPTED, 
                "respondedAt", System.currentTimeMillis()
            );

            return null;
        }).addOnSuccessListener(aVoid -> {
            if (getContext() != null) {
                Toast.makeText(getContext(), "Accepted successfully!", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            if (getContext() != null) {
                Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onReject(Request request) {
        db.collection(Constants.COLLECTION_REQUESTS).document(request.getId())
                .update("status", Constants.STATUS_REJECTED, "respondedAt", System.currentTimeMillis())
                .addOnSuccessListener(aVoid -> {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Request rejected", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (projectsListener != null) projectsListener.remove();
        if (requestsListener != null) requestsListener.remove();
        binding = null;
    }
}
