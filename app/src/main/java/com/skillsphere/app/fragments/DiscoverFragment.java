package com.skillsphere.app.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.skillsphere.app.activities.ProjectDetailActivity;
import com.skillsphere.app.adapters.PeopleAdapter;
import com.skillsphere.app.adapters.ProjectAdapter;
import com.skillsphere.app.databinding.FragmentDiscoverBinding;
import com.skillsphere.app.models.Project;
import com.skillsphere.app.models.Request;
import com.skillsphere.app.models.User;
import com.skillsphere.app.utils.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DiscoverFragment extends Fragment {

    private FragmentDiscoverBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private List<Project> allProjects = new ArrayList<>();
    private List<Project> filteredProjects = new ArrayList<>();
    private List<User> allPeople = new ArrayList<>();
    private List<User> filteredPeople = new ArrayList<>();

    private ProjectAdapter projectAdapter;
    private PeopleAdapter peopleAdapter;

    private boolean showingProjects = true;
    
    private ListenerRegistration projectsListener;
    private ListenerRegistration peopleListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentDiscoverBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        projectAdapter = new ProjectAdapter(filteredProjects, project -> {
            Intent intent = new Intent(getContext(), ProjectDetailActivity.class);
            intent.putExtra("PROJECT_ID", project.getId());
            startActivity(intent);
        });

        peopleAdapter = new PeopleAdapter(requireContext(), filteredPeople, new PeopleAdapter.OnPersonClickListener() {
            @Override
            public void onPersonClick(User user) {
                // Potential feature: view user profile
            }

            @Override
            public void onInviteClick(User user) {
                showInviteDialog(user);
            }
        });

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerView.setAdapter(projectAdapter);

        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                showingProjects = tab.getPosition() == 0;
                binding.recyclerView.setAdapter(showingProjects ? projectAdapter : peopleAdapter);
                applySearch(binding.etSearch.getText().toString());
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                applySearch(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        startListening();
    }

    private void showInviteDialog(User invitedUser) {
        if (auth.getCurrentUser() == null) return;
        String currentUserId = auth.getCurrentUser().getUid();

        // Fetch projects where current user is lead
        db.collection(Constants.COLLECTION_PROJECTS)
                .whereEqualTo("leadId", currentUserId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Project> myProjects = queryDocumentSnapshots.toObjects(Project.class);
                    for (int i = 0; i < myProjects.size(); i++) {
                        myProjects.get(i).setId(queryDocumentSnapshots.getDocuments().get(i).getId());
                    }

                    if (myProjects.isEmpty()) {
                        Toast.makeText(getContext(), "You don't have any projects to invite people to.", Toast.LENGTH_LONG).show();
                        return;
                    }

                    String[] projectTitles = new String[myProjects.size()];
                    for (int i = 0; i < myProjects.size(); i++) {
                        projectTitles[i] = myProjects.get(i).getTitle();
                    }

                    new AlertDialog.Builder(requireContext())
                            .setTitle("Invite " + invitedUser.getName() + " to...")
                            .setItems(projectTitles, (dialog, which) -> {
                                Project selectedProject = myProjects.get(which);
                                checkAndSendInvite(invitedUser, selectedProject);
                            })
                            .setNegativeButton("Cancel", null)
                            .show();
                });
    }

    private void checkAndSendInvite(User invitedUser, Project project) {
        // 1. Check if user is already a member
        if (project.getMembers() != null && project.getMembers().contains(invitedUser.getId())) {
            Toast.makeText(getContext(), invitedUser.getName() + " is already in this project.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 2. Check for duplicate pending invite
        db.collection(Constants.COLLECTION_REQUESTS)
                .whereEqualTo("projectId", project.getId())
                .whereEqualTo("toUserId", invitedUser.getId())
                .whereEqualTo("status", Constants.STATUS_PENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        Toast.makeText(getContext(), "An invitation is already pending for this user.", Toast.LENGTH_SHORT).show();
                    } else {
                        sendInvite(invitedUser, project);
                    }
                });
    }

    private void sendInvite(User invitedUser, Project project) {
        String leadId = auth.getCurrentUser().getUid();
        String leadName = auth.getCurrentUser().getDisplayName(); // fallback if null

        // Get lead name from session if display name is null
        if (leadName == null || leadName.isEmpty()) {
            leadName = "Project Lead"; 
        }

        Request invite = new Request();
        invite.setType(Constants.REQUEST_TYPE_INVITE);
        invite.setFromUserId(leadId);
        invite.setFromUserName(leadName);
        invite.setToUserId(invitedUser.getId());
        invite.setProjectId(project.getId());
        invite.setProjectTitle(project.getTitle());
        invite.setStatus(Constants.STATUS_PENDING);
        invite.setCreatedAt(System.currentTimeMillis());

        db.collection(Constants.COLLECTION_REQUESTS)
                .add(invite)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(getContext(), "Invitation sent to " + invitedUser.getName(), Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to send invitation", Toast.LENGTH_SHORT).show();
                });
    }

    private void startListening() {
        projectsListener = db.collection(Constants.COLLECTION_PROJECTS)
                .whereEqualTo("visibility", "public")
                .addSnapshotListener((querySnapshot, e) -> {
                    if (!isAdded()) return;
                    if (e != null) return;
                    if (querySnapshot != null) {
                        allProjects.clear();
                        List<Project> projects = querySnapshot.toObjects(Project.class);
                        for (int i = 0; i < projects.size(); i++) {
                            projects.get(i).setId(querySnapshot.getDocuments().get(i).getId());
                        }
                        allProjects.addAll(projects);
                        if (showingProjects) applySearch(binding.etSearch.getText().toString());
                    }
                });

        if (auth.getCurrentUser() == null) return;
        String currentUserId = auth.getCurrentUser().getUid();

        peopleListener = db.collection(Constants.COLLECTION_USERS)
                .whereEqualTo("appearInDiscover", true)
                .addSnapshotListener((querySnapshot, e) -> {
                    if (!isAdded()) return;
                    if (e != null) return;
                    if (querySnapshot != null) {
                        allPeople.clear();
                        for (int i = 0; i < querySnapshot.size(); i++) {
                            User u = querySnapshot.toObjects(User.class).get(i);
                            u.setId(querySnapshot.getDocuments().get(i).getId());
                            if (!u.getId().equals(currentUserId)) allPeople.add(u);
                        }
                        if (!showingProjects) applySearch(binding.etSearch.getText().toString());
                    }
                });
    }

    private void applySearch(String query) {
        String q = query.toLowerCase().trim();
        if (showingProjects) {
            filteredProjects.clear();
            for (Project p : allProjects) {
                String title = p.getTitle() != null ? p.getTitle().toLowerCase() : "";
                String category = p.getCategory() != null ? p.getCategory().toLowerCase() : "";
                
                if (q.isEmpty() || title.contains(q) || category.contains(q)) {
                    filteredProjects.add(p);
                }
            }
            projectAdapter.notifyDataSetChanged();
        } else {
            filteredPeople.clear();
            for (User u : allPeople) {
                String name = u.getName() != null ? u.getName().toLowerCase() : "";
                String dept = u.getDepartment() != null ? u.getDepartment().toLowerCase() : "";
                
                if (q.isEmpty() || name.contains(q) || dept.contains(q)) {
                    filteredPeople.add(u);
                }
            }
            peopleAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (projectsListener != null) projectsListener.remove();
        if (peopleListener != null) peopleListener.remove();
        binding = null;
    }
}
