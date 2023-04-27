package com.mindsoft.ui.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mindsoft.data.model.Department;
import com.mindsoft.databinding.DepartmentItemBinding;

import java.util.List;

public class DeptsCheckboxAdapter extends RecyclerView.Adapter<DeptsCheckboxAdapter.ViewHolder> {
    private List<Department> departments;

    public DeptsCheckboxAdapter(List<Department> departments) {
        this.departments = departments;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        DepartmentItemBinding binding = DepartmentItemBinding.inflate(inflater, parent, false);
        return new DeptsCheckboxAdapter.ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(departments.get(position));
    }

    @Override
    public int getItemCount() {
        return departments.size();
    }

    public List<Department> getDepartments() {
        return departments;
    }

    public void setDepartments(List<Department> departments) {
        this.departments = departments;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private DepartmentItemBinding binding;

        private Department department;

        public ViewHolder(@NonNull DepartmentItemBinding binding) {
            super(binding.getRoot());

            this.binding = binding;
        }

        public void bind(Department department) {
            this.binding.getRoot().setText(department.getResourceId());
            this.department = department;
        }

        public boolean isChecked() {
            return binding.getRoot().isChecked();
        }

        public Department getDepartment() {
            return department;
        }

        public void setDepartment(Department department) {
            this.department = department;
        }
    }
}
