package com.skillsphere.app.activities;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.skillsphere.app.R;
import com.skillsphere.app.databinding.ActivityMainBinding;
import com.skillsphere.app.fragments.HomeFragment;
import com.skillsphere.app.fragments.DiscoverFragment;
import com.skillsphere.app.fragments.WorkspaceFragment;
import com.skillsphere.app.fragments.ProfileFragment;
import com.skillsphere.app.utils.MockDataGenerator;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Generate mock data for the showcase
        MockDataGenerator.generateMockData();

        // Load Home by default
        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
        }

        binding.bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) loadFragment(new HomeFragment());
            else if (id == R.id.nav_discover) loadFragment(new DiscoverFragment());
            else if (id == R.id.nav_workspace) loadFragment(new WorkspaceFragment());
            else if (id == R.id.nav_profile) loadFragment(new ProfileFragment());

            return true;
        });

        // FAB Click -> Go to CreateProjectActivity
        binding.fabCreateProject.setOnClickListener(v -> {
            startActivity(new Intent(this, CreateProjectActivity.class));
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }
}