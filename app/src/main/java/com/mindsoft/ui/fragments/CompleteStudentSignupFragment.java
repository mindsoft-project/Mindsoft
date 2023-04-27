package com.mindsoft.ui.fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.mindsoft.R;
import com.mindsoft.data.model.Department;
import com.mindsoft.data.model.Student;
import com.mindsoft.data.model.User;
import com.mindsoft.databinding.FragmentCompleteStudentSignupBinding;
import com.mindsoft.ui.activity.HomeActivity;
import com.mindsoft.ui.viewmodel.SignupViewModel;

import java.util.Map;

public class CompleteStudentSignupFragment extends Fragment {

    public FragmentCompleteStudentSignupBinding binding;
    private SignupViewModel viewModel;

    private String program;

    private boolean isFaceTrained = false;

    public CompleteStudentSignupFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentCompleteStudentSignupBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(this).get(SignupViewModel.class);

        Spinner program = binding.program;

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this.getContext(), R.array.program, android.R.layout.simple_spinner_dropdown_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        program.setAdapter(adapter);
        program.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                CompleteStudentSignupFragment.this.program = adapterView.getItemAtPosition(position).toString().toLowerCase().replaceAll(" ", "_");
                if (CompleteStudentSignupFragment.this.program.equals("prosthetics_and_orthotics")) {
                    CompleteStudentSignupFragment.this.program = "prosthetics";
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });


        binding.next.setOnClickListener(v -> {
            AlertDialog dialog = new AlertDialog.Builder(requireActivity()).create();
            dialog.setTitle("Required fields");

            if (binding.studentCode.getText().toString().isEmpty()) {
                dialog.setMessage("Student should be set.");
                dialog.show();
                return;
            } else if (binding.semester.getText().toString().isEmpty()) {
                dialog.setMessage("Semester should be set.");
                dialog.show();
                return;
            } else if (binding.section.getText().toString().isEmpty()) {
                dialog.setMessage("Section should be set.");
                dialog.show();
                return;
            }

            if (binding.studentCode.getText().toString().length() > 8) {
                dialog.setTitle("Limit exceeds");
                dialog.setMessage("Student code is too long.");
                dialog.show();
                return;
            } else if (Integer.parseInt(binding.semester.getText().toString()) > 16) {
                dialog.setTitle("Limit exceeds");
                dialog.setMessage("Semester cannot be longer than 16.");
                dialog.show();
                return;
            }

            User user = new User();
            Student student = new Student();
            student.setId(user.getId());
            student.setStudentCode(binding.studentCode.getText().toString());
            student.setSection(Integer.parseInt(binding.section.getText().toString()));
            student.setSemester(Integer.parseInt(binding.semester.getText().toString()));

            for (Map.Entry<Integer, Department> entry : Department.departments.entrySet()) {
                if (entry.getValue().getName().equals(this.program)) {
                    student.setDepartment(entry.getValue());
                    break;
                }
            }

            assert getArguments() != null;

            user.setFirstName(getArguments().getString("first_name"));
            user.setLastName(getArguments().getString("last_name"));
            user.setRoles(getArguments().getInt("roles"));
            user.setEmail(getArguments().getString("email"));
            user.setNationalID(getArguments().getString("national_id"));

            String password = getArguments().getString("password");

            binding.next.setAlpha(0.5f);
            binding.next.setEnabled(false);

            viewModel.singUpUser(getArguments().getString("email"), password, user, student, new SignupViewModel.OnSignupListener() {
                @Override
                public void onSuccess() {
                    Intent intent = new Intent(getContext(), HomeActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    requireActivity().startActivity(intent);
                }

                @Override
                public void onError(String message) {
                    binding.next.setAlpha(1f);
                    binding.next.setEnabled(true);
                    AlertDialog alertDialog = new AlertDialog.Builder(getContext()).setTitle("Failed").setMessage(message).create();
                    alertDialog.show();
                }

            });
        });
        return binding.getRoot();
    }
}