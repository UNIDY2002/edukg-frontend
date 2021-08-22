package com.java.sunxun.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.java.sunxun.MainActivity;
import com.java.sunxun.R;
import com.java.sunxun.databinding.FragmentHomeBinding;
import com.java.sunxun.network.ApplicationNetwork;
import com.java.sunxun.network.NetworkHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeFragment extends Fragment {

    @Nullable
    FragmentHomeBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        binding.homeMenuIcon.setOnClickListener(view -> ((DrawerLayout) ((Activity) getContext()).findViewById(R.id.main_drawer)).open());
        binding.homeSearchInput.setOnFocusChangeListener((view, b) -> NavHostFragment.findNavController(this).navigate(R.id.nav_search));
        binding.homeHistoryIcon.setOnClickListener(view -> NavHostFragment.findNavController(this).navigate(R.id.nav_history));

        ApplicationNetwork.getEntityList(new NetworkHandler<String>(this) {
            @Override
            public void onSuccess(String result) {
                List<Map<String, Object>> entityInfoList = new ArrayList<>();

                // TODO: These data is fake, replace it!
                String[] entityName = {"定语顺序排列不当", "强加关系或弄错关系", "成分残缺", "状语顺序排列不当", "“与”作复音虚词", "表示推测、估量的语气副词"};
                String[] entityCategory = {"变调", "变调", "??", "变调", "变调", "变调"};

                for (int i = 0; i < entityName.length; ++i) {
                    Map<String, Object> entityInfo = new HashMap<>();
                    entityInfo.put("name", entityName[i]);
                    entityInfo.put("category", entityCategory[i]);
                    entityInfoList.add(entityInfo);
                }

                binding.entityList.setAdapter(new SimpleAdapter(
                        HomeFragment.this.getActivity(), entityInfoList,
                        R.layout.home_entity_item,
                        new String[] {"name", "category"},
                        new int[] {R.id.entity_name, R.id.entity_category}
                        ));
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
