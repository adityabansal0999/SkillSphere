package com.skillsphere.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.skillsphere.app.R;
import com.skillsphere.app.models.Project;
import java.util.ArrayList;
import java.util.List;

public class ProjectAdapter extends RecyclerView.Adapter<ProjectAdapter.ViewHolder> {
    private List<Project> projectList;
    private OnItemClickListener listener;

    public interface OnItemClickListener { void onItemClick(Project project); }

    public ProjectAdapter(List<Project> projectList, OnItemClickListener listener) {
        this.projectList = projectList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_project, parent, false);
        return new ViewHolder(view);
    }

    private String clean(String s, String fallback) {
        if (s == null || s.trim().isEmpty() || s.startsWith("[Ljava.lang.String;") || s.contains("@")) {
            return fallback;
        }
        // Filter out Firebase UIDs (usually 28 chars) or Document IDs (20 chars)
        if (s.length() >= 20 && s.matches("^[a-zA-Z0-9_-]+$")) {
            return fallback;
        }
        return s.trim();
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Project project = projectList.get(position);
        
        holder.title.setText(clean(project.getTitle(), "Untitled Project"));
        holder.description.setText(clean(project.getDescription(), "No description available"));
        holder.icon.setText(project.getIcon() != null ? project.getIcon() : "🤖");
        
        String category = clean(project.getCategory(), "General");
        String university = clean(project.getUniversity(), "SkillSphere");
        holder.stack.setText(String.format("%s · %s", category, university));
        
        holder.memberCount.setText(String.format("%d/%d", 
                project.getMembers() != null ? project.getMembers().size() : 0, 
                project.getMaxMembers() > 0 ? project.getMaxMembers() : 5));

        // --- Membership Check Logic ---
        String currentUserId = FirebaseAuth.getInstance().getUid();
        boolean isAlreadyPart = false;

        if (currentUserId != null) {
            boolean isLead = currentUserId.equals(project.getLeadId());
            boolean isMember = project.getMembers() != null && project.getMembers().contains(currentUserId);
            isAlreadyPart = isLead || isMember;
        }

        if (isAlreadyPart) {
            holder.btnAction.setText("Open Workspace");
        } else {
            holder.btnAction.setText("View & Apply");
        }
        // ------------------------------

        // Handle Chips - Filter out IDs and show real skills
        holder.chipGroup.removeAllViews();
        List<String> displaySkills = new ArrayList<>();
        
        if (project.getSkillsRequired() != null) {
            for (String s : project.getSkillsRequired()) {
                String cleaned = clean(s, "");
                if (!cleaned.isEmpty()) displaySkills.add(cleaned);
            }
        }
        
        if (displaySkills.isEmpty() && project.getCategories() != null) {
            for (String c : project.getCategories()) {
                String cleaned = clean(c, "");
                if (!cleaned.isEmpty()) displaySkills.add(cleaned);
            }
        }

        if (displaySkills.isEmpty()) {
            displaySkills.add(category);
        }

        for (String skillText : displaySkills) {
            Chip chip = new Chip(holder.itemView.getContext());
            chip.setText(skillText);
            chip.setTextSize(11f);
            chip.setClickable(false);
            chip.setCheckable(false);
            holder.chipGroup.addView(chip);
        }

        View.OnClickListener clickListener = v -> listener.onItemClick(project);
        holder.itemView.setOnClickListener(clickListener);
        holder.btnAction.setOnClickListener(clickListener);
    }

    @Override
    public int getItemCount() { return projectList.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, description, icon, stack, memberCount;
        ChipGroup chipGroup;
        MaterialButton btnAction;

        ViewHolder(View view) {
            super(view);
            title = view.findViewById(R.id.tvProjectTitle);
            description = view.findViewById(R.id.tvProjectDescription);
            icon = view.findViewById(R.id.tvProjectIcon);
            stack = view.findViewById(R.id.tvProjectStack);
            memberCount = view.findViewById(R.id.tvMemberCount);
            chipGroup = view.findViewById(R.id.chipGroupTech);
            btnAction = view.findViewById(R.id.btnAction);
        }
    }
}
