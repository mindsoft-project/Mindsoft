package com.mindsoft.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mindsoft.data.model.Course;
import com.mindsoft.data.model.User;
import com.mindsoft.databinding.CourseItemBinding;

import java.util.List;

public class CourseAdapter extends RecyclerView.Adapter<CourseAdapter.ViewHolder> {
    private List<Course> courseList;
    public OnCourseClickListener listener;
    private Options options;

    public CourseAdapter(List<Course> courseList, CourseAdapter.OnCourseClickListener listener) {
        this.courseList = courseList;
        this.listener = listener;
        this.options = new Options();
    }

    public CourseAdapter(List<Course> courseList, CourseAdapter.Options options, CourseAdapter.OnCourseClickListener listener) {
        this.courseList = courseList;
        this.listener = listener;
        this.options = options;
    }

    public List<Course> getCourseList() {
        return courseList;
    }

    public void setCourseList(List<Course> courseList) {
        this.courseList = courseList;
    }

    @NonNull
    @Override
    public CourseAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        CourseItemBinding binding = CourseItemBinding.inflate(inflater, parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CourseAdapter.ViewHolder holder, int position) {
        Course course = courseList.get(position);
        holder.bind(course);
        holder.binding.getRoot().setOnClickListener(v -> {
            this.listener.onClick(course);
        });
    }

    @Override
    public int getItemCount() {
        return courseList.size();
    }

    public interface OnCourseClickListener {
        void onClick(Course course);

        void onAdd(Course course);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public CourseItemBinding binding;

        public ViewHolder(CourseItemBinding binding) {
            super(binding.getRoot());
            ViewGroup.LayoutParams params = binding.getRoot().getLayoutParams();
            params.width = options.getWidth();
            binding.getRoot().setLayoutParams(params);
            this.binding = binding;
        }

        public void bind(Course course) {
            binding.courseName.setText(course.getName());
            binding.courseDescription.setText(course.getDescription());
            if (options.isHasAddButton())
                binding.add.setVisibility(View.VISIBLE);

            binding.add.setOnClickListener(v -> listener.onAdd(course));

            if (course.getProfessor() != null) {
                course.getProfessor().get().addOnSuccessListener(command -> {
                    User professor = command.toObject(User.class);
                    binding.professorName.setText("By Dr. " + professor.getFullName());
                });
            }
        }
    }

    public static class Options {
        private int width = ViewGroup.LayoutParams.MATCH_PARENT;

        private boolean hasAddButton = false;

        public int getWidth() {
            return width;
        }

        public void setWidth(int width) {
            this.width = width;
        }

        public boolean isHasAddButton() {
            return hasAddButton;
        }

        public void setHasAddButton(boolean hasAddButton) {
            this.hasAddButton = hasAddButton;
        }
    }
}
