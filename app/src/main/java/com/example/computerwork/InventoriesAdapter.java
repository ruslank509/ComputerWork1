package com.example.computerwork;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class InventoriesAdapter extends RecyclerView.Adapter<InventoriesAdapter.UserViewHolder> {

    private final List<InventoryModel> inventoryList;

    public InventoriesAdapter(List<InventoryModel> userList) {
        this.inventoryList = userList;
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView name, model, IDInventory;

        public UserViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.InvName);
            model = itemView.findViewById(R.id.InvModel);
            IDInventory = itemView.findViewById(R.id.InvID);
        }
    }

    @NonNull
    @Override
    public InventoriesAdapter.UserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.inventory_item, parent, false);
        return new UserViewHolder(v);
    }

    @Override
    public void onBindViewHolder(UserViewHolder holder, int position) {
        InventoryModel user = inventoryList.get(position);
        holder.name.setText(user.Name);
        holder.model.setText(user.Model);
        holder.IDInventory.setText(user.Idinventory);

    }

    @Override
    public int getItemCount() {
        return inventoryList.size();
    }
}
