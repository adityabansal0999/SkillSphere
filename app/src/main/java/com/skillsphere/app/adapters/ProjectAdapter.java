package com.skillsphere.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.skillsphere.app.R;
import com.skillsphere.app.models.Project;
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

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Project project = projectList.get(position);
        holder.title.setText(project.getTitle());
        holder.category.setText(project.getCategory());
        holder.itemView.setOnClickListener(v -> listener.onItemClick(project));
    }

    @Override
    public int getItemCount() { return projectList.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, category;
        ViewHolder(View view) {
            super(view);
            title = view.findViewById(R.id.tvProjectTitle);
            category = view.findViewById(R.id.tvProjectCategory);
        }
    }
}