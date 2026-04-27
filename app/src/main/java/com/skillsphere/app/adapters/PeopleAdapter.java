package com.skillsphere.app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.skillsphere.app.R;
import com.skillsphere.app.models.User;

import java.util.List;

public class PeopleAdapter extends RecyclerView.Adapter<PeopleAdapter.ViewHolder> {

    public interface OnPersonClickListener {
        void onPersonClick(User user);
    }

    private final Context context;
    private final List<User> people;
    private final OnPersonClickListener listener;

    public PeopleAdapter(Context context, List<User> people, OnPersonClickListener listener) {
        this.context = context;
        this.people = people;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_person_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = people.get(position);
        holder.tvInitials.setText(user.getInitials());
        holder.tvName.setText(user.getName() != null ? user.getName() : "Unknown");
        holder.tvDept.setText(user.getDepartment() != null ? user.getDepartment() : "");
        String skills = user.getSkills() != null && !user.getSkills().isEmpty()
                ? String.join(" · ", user.getSkills().subList(0, Math.min(3, user.getSkills().size())))
                : "No skills listed";
        holder.tvSkills.setText(skills);
        if (listener != null) {
            holder.itemView.setOnClickListener(v -> listener.onPersonClick(user));
        }
    }

    @Override
    public int getItemCount() { return people.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvInitials, tvName, tvDept, tvSkills;
        ViewHolder(View view) {
            super(view);
            tvInitials = view.findViewById(R.id.tv_person_initials);
            tvName = view.findViewById(R.id.tv_person_name);
            tvDept = view.findViewById(R.id.tv_person_dept);
            tvSkills = view.findViewById(R.id.tv_person_skills);
        }
    }
}
