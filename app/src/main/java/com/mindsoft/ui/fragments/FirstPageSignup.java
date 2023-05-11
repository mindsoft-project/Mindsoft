package com.mindsoft.ui.fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.mindsoft.R;
import com.mindsoft.data.model.Role;
import com.mindsoft.data.model.User;
import com.mindsoft.databinding.FragmentFirstPageSignupBinding;
import com.mindsoft.ui.activity.HomeActivity;
import com.mindsoft.ui.viewmodel.SignupViewModel;
import com.mindsoft.utils.UserUtils;

public class FirstPageSignup extends Fragment {
    private FragmentFirstPageSignupBinding binding;

    private String userType;
    private SignupViewModel viewModel;

    public FirstPageSignup() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentFirstPageSignupBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(this).get(SignupViewModel.class);

        Spinner userType = binding.userType;

        binding.showPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (binding.password.getTransformationMethod() == PasswordTransformationMethod.getInstance()) {
                    // Show the password
                    binding.showPass.setText("   Hide Password___ ");
                    binding.password.setTransformationMethod(HideReturnsTransformationMethod.getInstance());}
                else {
                    binding.showPass.setText("   Show Password__ ");

                    // Hide the password
                    binding.password.setTransformationMethod(PasswordTransformationMethod.getInstance());}
                binding.password.setSelection(binding.password.getText().length());
            }
        });
         TextWatcher passwordTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String password = binding.password.getText().toString().trim();
                if (password.length() > 8) {
                    binding.password.setError("Password must be 8 characters long");
                } else {
                    binding.password.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };
        binding.password.addTextChangedListener(passwordTextWatcher);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this.getContext(), R.array.user_type, android.R.layout.simple_spinner_dropdown_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        NavController navController = NavHostFragment.findNavController(this);

        userType.setAdapter(adapter);
        userType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                FirstPageSignup.this.userType = adapterView.getItemAtPosition(position).toString().toLowerCase().replaceAll(" ", "_");
                if (!FirstPageSignup.this.userType.equals("student")) {
                    binding.next.setText("Submit");
                } else {
                    binding.next.setText("Next");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                FirstPageSignup.this.userType = "student";
            }
        });

        binding.next.setOnClickListener(v -> {
            AlertDialog dialog = new AlertDialog.Builder(requireActivity()).create();
            dialog.setTitle("Invalid details");

            if (binding.email.getText().toString().isEmpty() || binding.password.getText().toString().isEmpty() || binding.firstName.getText().toString().isEmpty() || binding.lastName.getText().toString().isEmpty() || binding.nationalId.getText().toString().isEmpty()) {
                dialog.setMessage("One or more required field is empty.");
                dialog.show();
                return;
            }

            if (binding.nationalId.getText().toString().length() != 14) {
                dialog.setMessage("Invalid national id.");
                dialog.show();
                return;
            }

            if ("student".equals(this.userType)) {
                Bundle extras = new Bundle();
                extras.putString("email", binding.email.getText().toString());
                extras.putString("password", binding.password.getText().toString());
                extras.putInt("roles", UserUtils.USER_TYPE.getOrDefault(this.userType, Role.STUDENT).getId());
                extras.putString("national_id", binding.nationalId.getText().toString());
                extras.putString("first_name", binding.firstName.getText().toString());
                extras.putString("last_name", binding.lastName.getText().toString());

                navController.navigate(R.id.action_first_page_to_complete_student, extras);
            } else {
                User user = new User();
                user.setFirstName(binding.firstName.getText().toString());
                user.setLastName(binding.lastName.getText().toString());
                user.setNationalID(binding.nationalId.getText().toString());
                user.setTitle(0);
                user.setRoles(UserUtils.USER_TYPE.getOrDefault(this.userType, Role.STUDENT).getId());

                binding.next.setAlpha(0.5f);
                binding.next.setEnabled(false);

                viewModel.singUpUser(binding.email.getText().toString(), binding.password.getText().toString(), user, new SignupViewModel.OnSignupListener() {
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
            }
        });

        return binding.getRoot();
    }
}