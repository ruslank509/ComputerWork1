package com.example.computerwork;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.UserViewHolder> {

    private final List<OrderModel> inventoryList;

    public OrderAdapter(List<OrderModel> userList) {
        this.inventoryList = userList;
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView loginuser, nameproduct, modelproduct;

        public UserViewHolder(View itemView) {
            super(itemView);
            loginuser = itemView.findViewById(R.id.textOrderLogin);
            nameproduct = itemView.findViewById(R.id.textOrderName);
            modelproduct = itemView.findViewById(R.id.textOrderModel);
        }
    }

    @NonNull
    @Override
    public OrderAdapter.UserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.order_item, parent, false);
        return new UserViewHolder(v);
    }

    @Override
    public void onBindViewHolder(UserViewHolder holder, int position) {
        OrderModel user = inventoryList.get(position);
        holder.loginuser.setText(user.LoginUser);
        holder.nameproduct.setText(user.NameProduct);
        holder.modelproduct.setText(user.ModelProduct);
    }

    @Override
    public int getItemCount() {
        return inventoryList.size();
    }
}
