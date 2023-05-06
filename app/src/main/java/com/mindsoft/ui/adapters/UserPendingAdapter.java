package com.mindsoft.ui.adapters;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mindsoft.R;
import com.mindsoft.data.model.User;
import com.mindsoft.databinding.UserPendingItemBinding;

import java.util.List;

public class UserPendingAdapter extends RecyclerView.Adapter<UserPendingAdapter.ViewHolder> {
    public List<User> users;

    private OnUserClickListener listener;

    public UserPendingAdapter(List<User> users, OnUserClickListener listener) {
        this.users = users;
        this.listener = listener;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        UserPendingItemBinding binding = UserPendingItemBinding.inflate(inflater, parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = users.get(position);
        holder.bind(user);
        PopupMenu menu = new PopupMenu(holder.binding.getRoot().getContext(), holder.binding.getRoot());
        menu.getMenuInflater().inflate(R.menu.user_menu, menu.getMenu());
        listener.onMenuInitialize(user, menu.getMenu());

        menu.setOnMenuItemClickListener(item -> listener.onUserClickListener(user, position, item));

        holder.binding.getRoot().setOnClickListener(v -> {
            menu.show();
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public UserPendingItemBinding binding;

        public ViewHolder(UserPendingItemBinding binding) {
            super(binding.getRoot());

            this.binding = binding;

        }

        public void bind(User user) {
            binding.name.setText(user.getFullName());
            if (user.getTitle() != 0) {
                binding.title.setText(user.getTitle());
            } else {
                binding.title.setText(user.getPreferredTitle());
            }

        }
    }

    public interface OnUserClickListener {
        boolean onUserClickListener(User user, int position, MenuItem item);

        void onMenuInitialize(User user, Menu menu);
    }
}
