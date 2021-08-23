package com.java.sunxun.ui;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.RecyclerView;

import com.java.sunxun.R;
import com.java.sunxun.databinding.FragmentHomeBinding;
import com.java.sunxun.network.ApplicationNetwork;
import com.java.sunxun.network.NetworkHandler;

import org.jetbrains.annotations.NotNull;

public class HomeFragment extends Fragment {

    @Nullable
    FragmentHomeBinding binding;

    static class EntityListRecyclerViewAdapter extends RecyclerView.Adapter<EntityListRecyclerViewAdapter.ViewHolder> {

        final private LayoutInflater mInflater;
        final private String[] data;

        public EntityListRecyclerViewAdapter(Context context) {
            this.mInflater = LayoutInflater.from(context);
            this.data = new String[20];
            for (int i = 0; i < 20; ++i) {
                this.data[i] = "item" + (i + 1);
            }
        }

        @Override
        public @NotNull ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
            return new ViewHolder(mInflater.inflate(R.layout.fragment_home, parent, false));
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.itemNameView.setText(data[position]);
            holder.itemCategoryView.setText(data[position]);
        }

        @Override
        public int getItemCount() {
            return data.length;
        }

        public static class ViewHolder extends RecyclerView.ViewHolder {

            public TextView itemNameView;
            public TextView itemCategoryView;

            public ViewHolder(View view) {
                super(view);
                this.itemNameView = (TextView) view.findViewById(R.id.entity_name);
                this.itemCategoryView = (TextView) view.findViewById(R.id.entity_category);
            }
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        binding.homeMenuIcon.setOnClickListener(view -> ((DrawerLayout) ((Activity) getContext()).findViewById(R.id.main_drawer)).open());
        binding.homeSearchInput.setOnFocusChangeListener((view, b) -> NavHostFragment.findNavController(this).navigate(R.id.nav_search));
        binding.homeHistoryIcon.setOnClickListener(view -> NavHostFragment.findNavController(this).navigate(R.id.nav_history));

        ApplicationNetwork.getEntityList(new NetworkHandler<String>(this) {
            @Override
            public void onSuccess(String result) {
                binding.entityList.setAdapter(new EntityListRecyclerViewAdapter(HomeFragment.this.getActivity()));
            }

            @Override
            public void onError(Exception e) { e.printStackTrace(); }
        });

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
