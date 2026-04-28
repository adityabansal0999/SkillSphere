package com.skillsphere.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.skillsphere.app.R;
import com.skillsphere.app.databinding.ActivityMainBinding;
import com.skillsphere.app.fragments.DiscoverFragment;
import com.skillsphere.app.fragments.HomeFragment;
import com.skillsphere.app.fragments.ProfileFragment;
import com.skillsphere.app.fragments.WorkspaceFragment;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
        }

        binding.bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            Fragment fragment = null;
            
            if (id == R.id.nav_home) {
                fragment = new HomeFragment();
                binding.fabCreateProject.setVisibility(View.VISIBLE);
            } else if (id == R.id.nav_discover) {
                fragment = new DiscoverFragment();
                binding.fabCreateProject.setVisibility(View.VISIBLE);
            } else if (id == R.id.nav_workspace) {
                fragment = new WorkspaceFragment();
                binding.fabCreateProject.setVisibility(View.VISIBLE);
            } else if (id == R.id.nav_profile) {
                fragment = new ProfileFragment();
                binding.fabCreateProject.setVisibility(View.GONE);
            }
            
            if (fragment != null) {
                loadFragment(fragment);
            }
            return true;
        });

        binding.fabCreateProject.setOnClickListener(v ->
                startActivity(new Intent(this, CreateProjectActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        // If the active fragment is ProfileFragment, trigger a data refresh.
        Fragment current = getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
        if (current instanceof ProfileFragment) {
            ((ProfileFragment) current).onResume();
            binding.fabCreateProject.setVisibility(View.GONE);
        } else {
            binding.fabCreateProject.setVisibility(View.VISIBLE);
        }
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }
}