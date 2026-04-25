package com.example.skillsphere.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.skillsphere.R;
import com.example.skillsphere.adapters.TaskAdapter;
import com.example.skillsphere.models.Task;
import com.example.skillsphere.utils.Constants;
import com.example.skillsphere.utils.SessionManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TasksFragment extends Fragment implements TaskAdapter.OnTaskCheckedListener {

    private static final String ARG_PROJECT_ID = "project_id";

    private String projectId;

    private RecyclerView rvTasks;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private FloatingActionButton fabAddTask;

    private FirebaseFirestore db;
    private SessionManager sessionManager;
    private TaskAdapter taskAdapter;
    private List<Task> taskList;

    public static TasksFragment newInstance(String projectId) {
        TasksFragment fragment = new TasksFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PROJECT_ID, projectId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            projectId = getArguments().getString(ARG_PROJECT_ID);
        }
        db = FirebaseFirestore.getInstance();
        sessionManager = new SessionManager(requireContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tasks, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvTasks = view.findViewById(R.id.rv_tasks);
        progressBar = view.findViewById(R.id.progress_bar);
        tvEmpty = view.findViewById(R.id.tv_empty);
        fabAddTask = view.findViewById(R.id.fab_add_task);

        taskList = new ArrayList<>();
        taskAdapter = new TaskAdapter(requireContext(), taskList, this);

        rvTasks.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvTasks.setAdapter(taskAdapter);

        fabAddTask.setOnClickListener(v -> showAddTaskDialog());

        loadTasks();
    }

    private void loadTasks() {
        if (projectId == null) return;
        progressBar.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);

        db.collection(Constants.COLLECTION_PROJECTS)
                .document(projectId)
                .collection(Constants.COLLECTION_TASKS)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!isAdded()) return;
                    progressBar.setVisibility(View.GONE);
                    taskList.clear();

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Task task = doc.toObject(Task.class);
                        task.setTaskId(doc.getId());
                        taskList.add(task);
                    }

                    taskAdapter.notifyDataSetChanged();

                    if (taskList.isEmpty()) {
                        tvEmpty.setVisibility(View.VISIBLE);
                        tvEmpty.setText("No tasks yet. Add one!");
                    }
                })
                .addOnFailureListener(e -> {
                    if (isAdded()) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(requireContext(), "Failed to load tasks", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showAddTaskDialog() {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_add_task, null);

        EditText etTitle = dialogView.findViewById(R.id.et_task_title);
        EditText etDescription = dialogView.findViewById(R.id.et_task_description);
        EditText etAssignee = dialogView.findViewById(R.id.et_task_assignee);

        new AlertDialog.Builder(requireContext())
                .setTitle("Add New Task")
                .setView(dialogView)
                .setPositiveButton("Add", (dialog, which) -> {
                    String title = etTitle.getText().toString().trim();
                    String description = etDescription.getText().toString().trim();
                    String assignee = etAssignee.getText().toString().trim();

                    if (TextUtils.isEmpty(title)) {
                        Toast.makeText(requireContext(), "Task title is required", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    createTask(title, description, assignee);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void createTask(String title, String description, String assigneeName) {
        Task task = new Task();
        task.setTitle(title);
        task.setDescription(description);
        task.setAssigneeName(assigneeName);
        task.setCreatedBy(sessionManager.getUserId());
        task.setDone(false);
        task.setCreatedAt(new Date());

        db.collection(Constants.COLLECTION_PROJECTS)
                .document(projectId)
                .collection(Constants.COLLECTION_TASKS)
                .add(task)
                .addOnSuccessListener(docRef -> {
                    if (!isAdded()) return;
                    task.setTaskId(docRef.getId());
                    taskList.add(0, task);
                    taskAdapter.notifyItemInserted(0);
                    rvTasks.scrollToPosition(0);
                    tvEmpty.setVisibility(View.GONE);
                    Toast.makeText(requireContext(), "Task added!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    if (isAdded()) Toast.makeText(requireContext(), "Failed to add task", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onTaskChecked(Task task, boolean isDone, int position) {
        db.collection(Constants.COLLECTION_PROJECTS)
                .document(projectId)
                .collection(Constants.COLLECTION_TASKS)
                .document(task.getTaskId())
                .update("done", isDone)
                .addOnSuccessListener(aVoid -> {
                    if (!isAdded()) return;
                    task.setDone(isDone);
                    taskAdapter.notifyItemChanged(position);
                })
                .addOnFailureListener(e -> {
                    if (isAdded()) {
                        // Revert checkbox on failure
                        task.setDone(!isDone);
                        taskAdapter.notifyItemChanged(position);
                        Toast.makeText(requireContext(), "Failed to update task", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}