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
import com.skillsphere.app.models.Notification;
import com.skillsphere.app.utils.FirebaseHelper;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    public interface OnNotificationActionListener {
        void onAccept(Notification notification);
        void onReject(Notification notification);
    }

    private final Context context;
    private final List<Notification> notifications;
    private final OnNotificationActionListener listener;

    public NotificationAdapter(Context context, List<Notification> notifications,
                                OnNotificationActionListener listener) {
        this.context = context;
        this.notifications = notifications;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_notification, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Notification notif = notifications.get(position);
        holder.tvTitle.setText(notif.getTitle() != null ? notif.getTitle() : "Notification");
        holder.tvBody.setText(notif.getBody() != null ? notif.getBody() : "");
        holder.tvTime.setText(FirebaseHelper.getRelativeTime(notif.getCreatedAt()));

        if (!notif.isRead()) {
            holder.itemView.setBackgroundResource(R.drawable.bg_notification_unread);
        } else {
            holder.itemView.setBackground(null);
        }

        if ("accept_reject".equals(notif.getActionType())) {
            holder.btnAccept.setVisibility(View.VISIBLE);
            holder.btnReject.setVisibility(View.VISIBLE);
            holder.btnAccept.setOnClickListener(v -> {
                if (listener != null) listener.onAccept(notif);
            });
            holder.btnReject.setOnClickListener(v -> {
                if (listener != null) listener.onReject(notif);
            });
        } else {
            holder.btnAccept.setVisibility(View.GONE);
            holder.btnReject.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() { return notifications.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvBody, tvTime;
        Button btnAccept, btnReject;
        ViewHolder(View view) {
            super(view);
            tvTitle = view.findViewById(R.id.tv_notif_title);
            tvBody = view.findViewById(R.id.tv_notif_body);
            tvTime = view.findViewById(R.id.tv_notif_time);
            btnAccept = view.findViewById(R.id.btn_accept);
            btnReject = view.findViewById(R.id.btn_reject);
        }
    }
}
