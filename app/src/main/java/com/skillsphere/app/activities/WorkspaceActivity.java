package com.skillsphere.app.activities;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.firestore.FirebaseFirestore;
import com.skillsphere.app.databinding.ActivityWorkspaceBinding;
import com.skillsphere.app.fragments.ChatFragment;
import com.skillsphere.app.fragments.MembersFragment;
import com.skillsphere.app.fragments.TasksFragment;
import com.skillsphere.app.models.Project;

public class WorkspaceActivity extends AppCompatActivity {

    private ActivityWorkspaceBinding binding;
    private FirebaseFirestore db;
    private String projectId;
    private String leadId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWorkspaceBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        projectId = getIntent().getStringExtra("PROJECT_ID");
        leadId    = getIntent().getStringExtra("LEAD_ID");

        if (projectId != null) {
            if (leadId != null) {
                // leadId already provided by caller — set up immediately (1 call)
                loadProjectTitle(projectId);
                setupTabs();
            } else {
                // Fetch project to get leadId, THEN set up tabs (1 call, inside callback)
                loadProjectInfoThenSetupTabs(projectId);
            }
        }

        binding.btnBack.setOnClickListener(v -> finish());
    }

    /** Just updates the title bar — tabs are already set up */
    private void loadProjectTitle(String id) {
        db.collection("projects").document(id).get()
                .addOnSuccessListener(doc -> {
                    if (doc == null) return;
                    Project p = doc.toObject(Project.class);
                    if (p != null) binding.tvProjectTitle.setText(p.getTitle());
                });
    }

    /**
     * Fetches the full project document, sets the title, then calls setupTabs()
     * exactly once. Only used when leadId was not passed in the Intent.
     */
    private void loadProjectInfoThenSetupTabs(String id) {
        db.collection("projects").document(id).get()
                .addOnSuccessListener(doc -> {
                    if (doc == null) return;
                    Project p = doc.toObject(Project.class);
                    if (p != null) {
                        binding.tvProjectTitle.setText(p.getTitle());
                        leadId = p.getLeadId();
                        setupTabs(); // single call
                    }
                });
    }

    /** Creates ViewPager2 adapter and attaches TabLayoutMediator. Called exactly once. */
    private void setupTabs() {
        String[] tabTitles = {"💬 Chat", "👥 Members", "📋 Tasks"};
        WorkspacePagerAdapter adapter = new WorkspacePagerAdapter(this, projectId, leadId);
        binding.viewPager.setAdapter(adapter);
        new TabLayoutMediator(binding.tabLayout, binding.viewPager,
                (tab, position) -> tab.setText(tabTitles[position])
        ).attach();
    }

    static class WorkspacePagerAdapter extends FragmentStateAdapter {
        private final String projectId;
        private final String leadId;

        WorkspacePagerAdapter(@NonNull FragmentActivity fa, String projectId, String leadId) {
            super(fa);
            this.projectId = projectId;
            this.leadId    = leadId;
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0:  return ChatFragment.newInstance(projectId);
                case 1:  return MembersFragment.newInstance(projectId, leadId);
                case 2:  return TasksFragment.newInstance(projectId);
                default: return ChatFragment.newInstance(projectId);
            }
        }

        @Override
        public int getItemCount() { return 3; }
    }
}
