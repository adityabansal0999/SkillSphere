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

public class MemberAdapter extends RecyclerView.Adapter<MemberAdapter.ViewHolder> {

    private final Context context;
    private final List<User> members;

    public MemberAdapter(Context context, List<User> members) {
        this.context = context;
        this.members = members;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_member, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = members.get(position);
        holder.tvInitials.setText(user.getInitials());
        holder.tvName.setText(user.getName() != null ? user.getName() : "Unknown");
        holder.tvDept.setText(user.getDepartment() != null ? user.getDepartment() : "");
    }

    @Override
    public int getItemCount() { return members.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvInitials, tvName, tvDept;
        ViewHolder(View view) {
            super(view);
            tvInitials = view.findViewById(R.id.tv_member_initials);
            tvName = view.findViewById(R.id.tv_member_name);
            tvDept = view.findViewById(R.id.tv_member_dept);
        }
    }
}
