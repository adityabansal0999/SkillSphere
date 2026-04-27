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
import com.skillsphere.app.models.User;
import com.skillsphere.app.utils.Constants;

import java.util.ArrayList;
import java.util.List;

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

        peopleAdapter = new PeopleAdapter(requireContext(), filteredPeople, null);

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

    private void startListening() {
        // Real-time listener for Projects
        projectsListener = db.collection(Constants.COLLECTION_PROJECTS)
                .whereEqualTo("visibility", "public")
                .addSnapshotListener((querySnapshot, e) -> {
                    if (!isAdded()) return;
                    if (e != null) {
                        Toast.makeText(getContext(), "Failed to load projects", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (querySnapshot != null) {
                        allProjects.clear();
                        for (int i = 0; i < querySnapshot.size(); i++) {
                            Project p = querySnapshot.toObjects(Project.class).get(i);
                            p.setId(querySnapshot.getDocuments().get(i).getId());
                            allProjects.add(p);
                        }
                        if (showingProjects) applySearch(binding.etSearch.getText().toString());
                    }
                });

        // Real-time listener for People
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
                if (q.isEmpty() || (p.getTitle() != null && p.getTitle().toLowerCase().contains(q))
                        || (p.getCategory() != null && p.getCategory().toLowerCase().contains(q))) {
                    filteredProjects.add(p);
                }
            }
            projectAdapter.notifyDataSetChanged();
        } else {
            filteredPeople.clear();
            for (User u : allPeople) {
                if (q.isEmpty() || (u.getName() != null && u.getName().toLowerCase().contains(q))
                        || (u.getDepartment() != null && u.getDepartment().toLowerCase().contains(q))) {
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
