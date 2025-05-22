package com.example.computerwork;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UserViewHolder> {

    private List<UserModel> userList;

    public UsersAdapter(List<UserModel> userList) {
        this.userList = userList;
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView login, email, password, status;

        public UserViewHolder(View itemView) {
            super(itemView);
            login = itemView.findViewById(R.id.textLogin);
            email = itemView.findViewById(R.id.textEmail);
            password = itemView.findViewById(R.id.textPassword);
            status = itemView.findViewById(R.id.textStatus);
        }
    }

    @Override
    public UsersAdapter.UserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.park_item, parent, false);
        return new UserViewHolder(v);
    }

    @Override
    public void onBindViewHolder(UserViewHolder holder, int position) {
        UserModel user = userList.get(position);
        holder.login.setText(user.Login);
        holder.email.setText(user.Email);
        holder.password.setText(user.Password);
        holder.status.setText(user.Status);

    }

    @Override
    public int getItemCount() {
        return userList.size();
    }
}

