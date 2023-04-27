package com.mindsoft.ui.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mindsoft.R;
import com.mindsoft.data.model.Course;
import com.mindsoft.data.model.StudentAttended;
import com.mindsoft.databinding.StudentAttendanceItemBinding;

import java.util.List;

public class StudentAttendanceAdapter extends RecyclerView.Adapter<StudentAttendanceAdapter.ViewHolder> {
    public List<StudentAttended> students;

    public StudentAttendanceAdapter(List<StudentAttended> students) {
        this.students = students;
    }


    @NonNull
    @Override
    public StudentAttendanceAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        StudentAttendanceItemBinding binding = StudentAttendanceItemBinding.inflate(inflater, parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull StudentAttendanceAdapter.ViewHolder holder, int position) {
        StudentAttended student = students.get(position);
        holder.bind(student);
    }

    @Override
    public int getItemCount() {
        return students.size();
    }

    public List<StudentAttended> getStudents() {
        return students;
    }

    public void setStudents(List<StudentAttended> students) {
        this.students = students;
    }

    public interface OnCourseClickListener {
        void onClick(Course course);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public StudentAttendanceItemBinding binding;

        public ViewHolder(StudentAttendanceItemBinding binding) {
            super(binding.getRoot());

            this.binding = binding;
        }

        public void bind(StudentAttended student) {
            binding.name.setText(student.getFullName());
            setAttending(student.isAttended());
        }

        public void setAttending(boolean attended) {
            if (attended) {
                binding.status.setText("Attended");
                binding.status.setTextColor(binding.getRoot().getResources().getColor(R.color.success, binding.getRoot().getContext().getTheme()));
            } else {
                binding.status.setText("Not Attended");
                binding.status.setTextColor(binding.getRoot().getResources().getColor(R.color.danger, binding.getRoot().getContext().getTheme()));
            }
        }
    }
}
