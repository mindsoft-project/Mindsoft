package com.mindsoft.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.mindsoft.R;
import com.mindsoft.data.model.Role;
import com.mindsoft.data.model.User;
import com.mindsoft.databinding.FragmentPendingRequestsBinding;
import com.mindsoft.ui.adapters.UserPendingAdapter;

import java.util.ArrayList;

public class PendingRequestsFragment extends Fragment {
    private FragmentPendingRequestsBinding binding;
    private UserPendingAdapter adapter;
    private RecyclerView recyclerView;
    private ListenerRegistration listner;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentPendingRequestsBinding.inflate(inflater, container, false);

        recyclerView = binding.recyclerView;
        recyclerView.setLayoutManager(new LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false));


        adapter = new UserPendingAdapter(new ArrayList<>(), new UserPendingAdapter.OnUserClickListener() {
            @Override
            public boolean onUserClickListener(User user, int position, MenuItem item) {
                if (item.getItemId() == R.id.accept) {
                    acceptUser(user, position);
                    return true;
                } else if (item.getItemId() == R.id.profile) {
                    return true;
                }
                return false;
            }

            @Override
            public void onMenuInitialize(User user, Menu menu) {
                if (user.getId().equals(User.current.getId())) {
                    menu.findItem(R.id.profile).setVisible(false);
                }
                if (User.current.hasRole(Role.ADMIN) && !user.isValidated()) {
                    menu.findItem(R.id.accept).setVisible(true);
                }
            }
        });
        recyclerView.setAdapter(adapter);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        listner = db.collection(User.COLLECTION).addSnapshotListener(requireActivity(), (value, error) -> {
            if (error != null) {
                error.printStackTrace();
                return;
            }
            if (value != null) {
                for (DocumentChange dc : value.getDocumentChanges()) {
                    System.out.println(dc.getDocument());
                    User user = dc.getDocument().toObject(User.class);
                    int i = 0;
                    boolean rem = false;

                    for (User u : adapter.users) {
                        if (u.getId().equals(user.getId()) && user.isValidated() && !u.isValidated()) {
                            adapter.users.remove(u);
                            adapter.notifyItemRemoved(i);
                            rem = true;
                        }
                        i++;
                    }


                    if (!rem && !user.isValidated()) {
                        adapter.users.add(user);
                        adapter.notifyItemInserted(adapter.users.size() - 1);
                    }

                    if (adapter.users.size() > 0) {
                        binding.count.setVisibility(View.VISIBLE);
                    } else {
                        binding.count.setVisibility(View.GONE);
                    }

                    if (adapter.users.size() > 99) {
                        binding.count.setText("+99");
                    } else {
                        binding.count.setText(String.valueOf(adapter.users.size()));
                    }
                }
            }
        });

        return binding.getRoot();
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (listner != null) {
            listner.remove();
        }
    }


    private void acceptUser(User user, int position) {
        user.setValidated(true);

        user.getReference().set(user).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                int i = adapter.users.indexOf(user);
                adapter.getUsers().remove(user);
                adapter.notifyItemRemoved(i);
            }
            if (adapter.users.size() > 0) {
                binding.count.setVisibility(View.VISIBLE);
            } else {
                binding.count.setVisibility(View.GONE);
            }

            if (adapter.users.size() > 99) {
                binding.count.setText("+99");
            } else {
                binding.count.setText(String.valueOf(adapter.users.size()));
            }
        });
    }
}
