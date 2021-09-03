package com.java.sunxun.ui;

import android.app.Activity;
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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.android.material.tabs.TabLayout;
import com.java.sunxun.R;
import com.java.sunxun.components.RecyclerViewAdapter;
import com.java.sunxun.databinding.FragmentHomeBinding;
import com.java.sunxun.models.Entity;
import com.java.sunxun.models.Subject;
import com.java.sunxun.network.ApplicationNetwork;
import com.java.sunxun.network.NetworkHandler;
import com.scwang.smartrefresh.layout.footer.ClassicsFooter;
import com.scwang.smartrefresh.layout.header.ClassicsHeader;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class HomeFragment extends Fragment {

    @Nullable
    FragmentHomeBinding binding;

    final private List<Entity> baseEntities = new ArrayList<>();

    private Subject selectedSubject = Subject.chinese;
    private int requestCnt = 0;
    private int entityNumEveryRequest = 15; // TODO: Make it editable later
    private int randomSeed = 0;

    /**
     * This function will refresh entity list & update UI according to data from net.
     * @param isRefresh When true, it means refreshing. When false, it means loading more.
     */
    private void updateEntityList(boolean isRefresh) {
        if (isRefresh) {
            requestCnt = 0;
            randomSeed = new Random().nextInt();
        } else ++requestCnt;

        ApplicationNetwork.getEntityList(selectedSubject, requestCnt, entityNumEveryRequest, randomSeed, new NetworkHandler<String>(this) {
            @Override
            public void onSuccess(String result) {
                // We put clear entities here for safety
                if (isRefresh) baseEntities.clear();

                JSONArray arr = JSON.parseArray(result);
                for (int i = 0; i < arr.size(); ++i) {
                    JSONObject o = arr.getJSONObject(i);
                    Entity newEntity = new Entity(selectedSubject, o.getString("label"), o.getString("category"), o.getString("id"));
                    baseEntities.add(newEntity);
                }
                ((RecyclerViewAdapter<Entity>) binding.entityList.getAdapter()).updateData(baseEntities);
                if (isRefresh) binding.entityListWrapper.finishRefresh();
                else binding.entityListWrapper.finishLoadMore();
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
        // binding.homeHistoryIcon.setOnClickListener(view -> NavHostFragment.findNavController(this).navigate(R.id.nav_history));

        binding.entityList.setLayoutManager(new LinearLayoutManager(HomeFragment.this.getActivity()));
        binding.entityList.setAdapter(new RecyclerViewAdapter<Entity>(
                HomeFragment.this.getActivity(), R.layout.item_home_entity, baseEntities
        ) {
            @Override
            public void convert(RecyclerViewAdapter.ViewHolder holder, Entity data) {
                ((TextView) holder.getViewById(R.id.entity_name)).setText(data.getLabel());
                ((TextView) holder.getViewById(R.id.entity_category)).setText(data.getCategory());
            }
        });

        binding.entityListWrapper.setEnableRefresh(true);
        binding.entityListWrapper.setOnRefreshListener(refreshLayout -> updateEntityList(true));

        binding.entityListWrapper.setEnableLoadMore(true);
        binding.entityListWrapper.setOnLoadMoreListener(refreshLayout -> updateEntityList(false));

        binding.entityListWrapper.setRefreshHeader(new ClassicsHeader(this.getActivity()));
        binding.entityListWrapper.setRefreshFooter(new ClassicsFooter(this.getActivity()));

        binding.subjectTab.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                selectedSubject = Subject.fromName(HomeFragment.this.getActivity(), "" + tab.getText());
                updateEntityList(true);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) { }

            @Override
            public void onTabReselected(TabLayout.Tab tab) { }
        });

        updateEntityList(true);

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
