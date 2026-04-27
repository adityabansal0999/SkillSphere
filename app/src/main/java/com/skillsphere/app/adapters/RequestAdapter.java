package com.skillsphere.app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.skillsphere.app.R;
import com.skillsphere.app.models.Request;

import java.util.List;

public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.ViewHolder> {

    public interface OnRequestActionListener {
        void onAccept(Request request);
        void onReject(Request request);
    }

    private final Context context;
    private final List<Request> requests;
    private final OnRequestActionListener listener;

    public RequestAdapter(Context context, List<Request> requests, OnRequestActionListener listener) {
        this.context = context;
        this.requests = requests;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_request, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Request request = requests.get(position);
        holder.tvName.setText(request.getFromUserName() != null ? request.getFromUserName() : "Unknown");
        holder.tvProject.setText(request.getProjectTitle() != null ? request.getProjectTitle() : "");

        String skills = request.getFromUserSkills() != null && !request.getFromUserSkills().isEmpty()
                ? String.join(", ", request.getFromUserSkills().subList(0, Math.min(3, request.getFromUserSkills().size())))
                : "No skills listed";
        holder.tvSkills.setText(skills);

        holder.btnAccept.setOnClickListener(v -> {
            if (listener != null) listener.onAccept(request);
        });
        holder.btnReject.setOnClickListener(v -> {
            if (listener != null) listener.onReject(request);
        });
    }

    @Override
    public int getItemCount() { return requests.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvProject, tvSkills;
        Button btnAccept, btnReject;
        ViewHolder(View view) {
            super(view);
            tvName = view.findViewById(R.id.tv_request_name);
            tvProject = view.findViewById(R.id.tv_request_project);
            tvSkills = view.findViewById(R.id.tv_request_skills);
            btnAccept = view.findViewById(R.id.btn_accept_request);
            btnReject = view.findViewById(R.id.btn_reject_request);
        }
    }
}
