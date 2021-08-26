package com.java.sunxun.ui;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.java.sunxun.R;
import com.java.sunxun.databinding.FragmentHomeBinding;
import com.java.sunxun.models.Entity;
import com.java.sunxun.models.Subject;
import com.java.sunxun.network.ApplicationNetwork;
import com.java.sunxun.network.NetworkHandler;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    @Nullable
    FragmentHomeBinding binding;

    private Subject selectedSubject;
    final private List<Entity> baseEntities = new ArrayList<>();
    private int requestCnt = 0;
    private int entityNumEveryRequest = 10; // TODO: Make it editable later

    class EntityListRecyclerViewAdapter extends RecyclerView.Adapter<EntityListRecyclerViewAdapter.ViewHolder> {

        @Override
        public @NotNull ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
            return new ViewHolder(
                    LayoutInflater.from(parent.getContext()).inflate(R.layout.home_entity_item, parent, false));
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.itemNameView.setText(baseEntities.get(position).getLabel());
            holder.itemCategoryView.setText(baseEntities.get(position).getCategory());
        }

        @Override
        public int getItemCount() {
            return baseEntities.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            public TextView itemNameView;
            public TextView itemCategoryView;

            public ViewHolder(View view) {
                super(view);
                this.itemNameView = (TextView) view.findViewById(R.id.entity_name);
                this.itemCategoryView = (TextView) view.findViewById(R.id.entity_category);
            }
        }
    }

    /**
     * This function will refresh entity list & update UI according to data from net.
     */
    private void refreshEntityList() {
        ApplicationNetwork.getEntityList(selectedSubject, requestCnt, entityNumEveryRequest, new NetworkHandler<String>(this) {
            @Override
            public void onSuccess(String result) {
                JSONArray arr = JSON.parseArray(result);
                for (int i = 0; i < arr.size(); ++i) {
                    JSONObject o = arr.getJSONObject(i);
                    Entity newEntity = new Entity(selectedSubject, o.getString("label"), o.getString("category"), o.getString("id"));
                    baseEntities.add(newEntity);
                }
                binding.entityList.setLayoutManager(new LinearLayoutManager(HomeFragment.this.getActivity()));
                binding.entityList.setAdapter(new EntityListRecyclerViewAdapter());
            }

            @Override
            public void onError(Exception e) { e.printStackTrace(); }
        });
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        binding.homeMenuIcon.setOnClickListener(view -> ((DrawerLayout) ((Activity) getContext()).findViewById(R.id.main_drawer)).open());
        binding.homeSearchInput.setOnFocusChangeListener((view, b) -> NavHostFragment.findNavController(this).navigate(R.id.nav_search));
        binding.homeHistoryIcon.setOnClickListener(view -> NavHostFragment.findNavController(this).navigate(R.id.nav_history));

        refreshEntityList();

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onRefresh() {
        ++requestCnt;
        refreshEntityList();
    }
}
