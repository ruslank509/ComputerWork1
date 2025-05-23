package com.example.computerwork;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class QueryAdapter extends RecyclerView.Adapter<QueryAdapter.UserViewHolder> {

    private final List<QueryModel> inventoryList;

    public QueryAdapter(List<QueryModel> userList) {
        this.inventoryList = userList;
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView loginuser, crush, status;

        public UserViewHolder(View itemView) {
            super(itemView);
            loginuser = itemView.findViewById(R.id.textLoginUser);
            crush = itemView.findViewById(R.id.textCrush);
            status = itemView.findViewById(R.id.textStatusQuery);
        }
    }

    @NonNull
    @Override
    public QueryAdapter.UserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.query_item, parent, false);
        return new UserViewHolder(v);
    }

    @Override
    public void onBindViewHolder(UserViewHolder holder, int position) {
        QueryModel user = inventoryList.get(position);
        holder.loginuser.setText(user.LoginUser);
        holder.crush.setText(user.Crush);
        holder.status.setText(user.Status);
    }

    @Override
    public int getItemCount() {
        return inventoryList.size();
    }
}
