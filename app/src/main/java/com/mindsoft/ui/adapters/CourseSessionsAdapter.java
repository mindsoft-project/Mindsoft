package com.mindsoft.ui.adapters;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.mindsoft.R;
import com.mindsoft.data.model.CourseSession;
import com.mindsoft.data.model.User;
import com.mindsoft.databinding.CourseSessionItemBinding;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class CourseSessionsAdapter extends RecyclerView.Adapter<CourseSessionsAdapter.ViewHolder> {
    private List<CourseSession> sessionList;
    private CourseSessionsAdapter.OnCourseClickListener listener;

    public CourseSessionsAdapter(List<CourseSession> sessionList, CourseSessionsAdapter.OnCourseClickListener listener) {
        this.sessionList = sessionList;
        this.listener = listener;
    }

    public List<CourseSession> getSessionList() {
        return sessionList;
    }

    public void setSessionList(List<CourseSession> sessionList) {
        this.sessionList = sessionList;
    }

    @NonNull
    @Override
    public CourseSessionsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        CourseSessionItemBinding binding = CourseSessionItemBinding.inflate(inflater, parent, false);
        return new CourseSessionsAdapter.ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CourseSessionsAdapter.ViewHolder holder, int position) {
        CourseSession session = sessionList.get(position);
        holder.bind(session);
        holder.binding.getRoot().setOnClickListener(v -> {
            this.listener.onClick(session);
        });
    }

    @Override
    public int getItemCount() {
        return sessionList.size();
    }

    public interface OnCourseClickListener {
        void onClick(CourseSession session);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public CourseSessionItemBinding binding;

        public ViewHolder(CourseSessionItemBinding binding) {
            super(binding.getRoot());

            this.binding = binding;
        }

        public void bind(CourseSession session) {
            binding.sessionTitle.setText(session.getTitle());
            session.getInstructor().get().addOnSuccessListener(command -> {
                User instructor = command.toObject(User.class);
                binding.instructorName.setText("By " + instructor.getFullName());
            });

            Handler handler = new Handler();
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    Timestamp start = session.getDate();
                    Timestamp now = Timestamp.now();
                    long estimated = now.toDate().getTime() - start.toDate().getTime();
                    int seconds = (int) (estimated / 1000) % 60;
                    int minutes = (int) (estimated / 1000 / 60) % 60;
                    int hours = (int) (estimated / 1000 / 60 / 60) % 24;
                    binding.time.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));
                    handler.postDelayed(this, 1000);
                }
            };

            switch (session.getStatusValue()) {
                case ENDED:
                    binding.status.setText("Ended");
                    binding.time.setText(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.US).format(session.getDate().toDate()));
                    break;
                case ONGOING:
                    binding.status.setText("Ongoing");
                    binding.status.setTextColor(binding.getRoot().getResources().getColor(R.color.success, binding.getRoot().getContext().getTheme()));
                    binding.time.setTextColor(binding.getRoot().getResources().getColor(R.color.success, binding.getRoot().getContext().getTheme()));
                    handler.post(runnable);
                    break;
            }
        }
    }
}
