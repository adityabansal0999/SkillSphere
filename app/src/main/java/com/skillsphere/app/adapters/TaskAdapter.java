package com.skillsphere.app.adapters;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.skillsphere.app.R;
import com.skillsphere.app.models.Task;

import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    public interface OnTaskCheckedListener {
        void onTaskChecked(Task task, boolean isDone, int position);
    }

    private final Context context;
    private final List<Task> tasks;
    private final OnTaskCheckedListener listener;

    public TaskAdapter(Context context, List<Task> tasks, OnTaskCheckedListener listener) {
        this.context = context;
        this.tasks = tasks;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = tasks.get(position);

        holder.tvTitle.setText(task.getTitle());
        
        if (task.getDescription() != null && !task.getDescription().isEmpty()) {
            holder.tvDescription.setText(task.getDescription());
            holder.tvDescription.setVisibility(View.VISIBLE);
        } else {
            holder.tvDescription.setVisibility(View.GONE);
        }

        holder.tvAssignee.setText(
                task.getAssignedName() != null && !task.getAssignedName().isEmpty()
                        ? "Assigned to: " + task.getAssignedName()
                        : "Unassigned"
        );

        // A task is considered "done" if its status is "done"
        boolean isDone = "done".equalsIgnoreCase(task.getStatus());

        // Prevent listener from firing during bind
        holder.cbDone.setOnCheckedChangeListener(null);
        holder.cbDone.setChecked(isDone);

        // Strikethrough on completed tasks
        applyDoneStyle(holder, isDone);

        holder.cbDone.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (listener != null) {
                listener.onTaskChecked(task, isChecked, holder.getBindingAdapterPosition());
            }
        });
    }

    private void applyDoneStyle(TaskViewHolder holder, boolean isDone) {
        if (isDone) {
            holder.tvTitle.setPaintFlags(holder.tvTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.tvTitle.setAlpha(0.5f);
            holder.tvDescription.setAlpha(0.5f);
        } else {
            holder.tvTitle.setPaintFlags(holder.tvTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            holder.tvTitle.setAlpha(1f);
            holder.tvDescription.setAlpha(1f);
        }
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        CheckBox cbDone;
        TextView tvTitle, tvDescription, tvAssignee;

        TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            cbDone = itemView.findViewById(R.id.cb_task_done);
            tvTitle = itemView.findViewById(R.id.tv_task_title);
            tvDescription = itemView.findViewById(R.id.tv_task_description);
            tvAssignee = itemView.findViewById(R.id.tv_task_assignee);
        }
    }
}
