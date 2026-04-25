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
import com.google.firebase.firestore.FirebaseFirestore;
import com.skillsphere.app.activities.ProjectDetailActivity;
import com.skillsphere.app.adapters.ProjectAdapter;
import com.skillsphere.app.databinding.FragmentHomeBinding;
import com.skillsphere.app.models.Project;
import java.util.List;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private FirebaseFirestore db;

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
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Fetch projects from Firestore
        db.collection("projects").get().addOnSuccessListener(queryDocumentSnapshots -> {
            List<Project> projects = queryDocumentSnapshots.toObjects(Project.class);

            ProjectAdapter adapter = new ProjectAdapter(projects, project -> {
                Intent intent = new Intent(getContext(), ProjectDetailActivity.class);
                intent.putExtra("PROJECT_ID", project.getId());
                startActivity(intent);
            });

            binding.recyclerView.setAdapter(adapter);
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Error loading projects", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}