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
import com.google.firebase.firestore.FirebaseFirestore;
import com.skillsphere.app.activities.WorkspaceActivity;
import com.skillsphere.app.adapters.ProjectAdapter;
import com.skillsphere.app.databinding.FragmentWorkspaceBinding;
import com.skillsphere.app.models.Project;
import com.skillsphere.app.utils.Constants;

import java.util.ArrayList;
import java.util.List;

public class WorkspaceFragment extends Fragment {

    private FragmentWorkspaceBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private ProjectAdapter adapter;
    private List<Project> myProjects;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentWorkspaceBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        myProjects = new ArrayList<>();

        adapter = new ProjectAdapter(myProjects, project -> {
            Intent intent = new Intent(getContext(), WorkspaceActivity.class);
            intent.putExtra("PROJECT_ID", project.getId());
            intent.putExtra("LEAD_ID", project.getLeadId());
            startActivity(intent);
        });

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerView.setAdapter(adapter);

        loadMyWorkspaces();
    }

    private void loadMyWorkspaces() {
        if (auth.getCurrentUser() == null) return;
        String userId = auth.getCurrentUser().getUid();

        db.collection(Constants.COLLECTION_PROJECTS)
                .whereArrayContains("members", userId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!isAdded()) return;
                    myProjects.clear();
                    for (int i = 0; i < querySnapshot.size(); i++) {
                        Project p = querySnapshot.toObjects(Project.class).get(i);
                        p.setId(querySnapshot.getDocuments().get(i).getId());
                        myProjects.add(p);
                    }
                    adapter.notifyDataSetChanged();
                    binding.tvEmpty.setVisibility(myProjects.isEmpty() ? View.VISIBLE : View.GONE);
                })
                .addOnFailureListener(e -> {
                    if (isAdded()) Toast.makeText(getContext(), "Failed to load workspaces", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
