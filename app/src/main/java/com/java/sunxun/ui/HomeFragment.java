package com.java.sunxun.ui;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.java.sunxun.R;
import com.java.sunxun.components.DraggableRecyclerViewAdapter;
import com.java.sunxun.components.ItemDragHelperCallback;
import com.java.sunxun.components.RecyclerViewAdapter;
import com.java.sunxun.databinding.FragmentHomeBinding;
import com.java.sunxun.models.Entity;
import com.java.sunxun.models.Subject;
import com.java.sunxun.models.User;
import com.java.sunxun.network.ApplicationNetwork;
import com.java.sunxun.network.NetworkHandler;
import com.scwang.smartrefresh.layout.footer.ClassicsFooter;
import com.scwang.smartrefresh.layout.header.ClassicsHeader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class HomeFragment extends Fragment {

    @Nullable
    FragmentHomeBinding binding;

    final private List<Entity> baseEntities = new ArrayList<>();

    private Subject selectedSubject = Subject.chinese;
    private int requestCnt = 0;
    private int entityNumEveryRequest = 15;
    private int randomSeed = 0;

    private String rawInput = "";

    final static private int subjectNum = 9;
    final private ArrayList<String> availableSubject = new ArrayList<>();
    private int availableNum = subjectNum;

    private RecyclerViewAdapter<Entity> adapter = null;

    // This lock prevents updating entity list when params are being reset
    private boolean requestLock = false;

    private RecyclerViewAdapter<Entity> getLatestAdapter() {
        return new RecyclerViewAdapter<Entity>(
                HomeFragment.this.getActivity(), R.layout.item_home_entity, baseEntities
        ) {
            @Override
            public void convert(RecyclerViewAdapter.ViewHolder holder, Entity data, int position) {
                ((TextView) holder.getViewById(R.id.entity_name)).setText(data.getLabel());
                ((TextView) holder.getViewById(R.id.entity_category)).setText(data.getCategory());
                holder.getViewById(R.id.forward).setOnClickListener(v -> {
                    Bundle mBundle = new Bundle();
                    mBundle.putInt("subject", selectedSubject.ordinal());
                    mBundle.putString("name", data.getLabel());
                    mBundle.putString("category", data.getCategory());
                    mBundle.putString("uri", data.getUri());
                    NavHostFragment.findNavController(HomeFragment.this).navigate(R.id.nav_detail, mBundle);
                });
            }
        };
    }

    /**
     * This function will refresh entity list & update UI according to data from net.
     * @param isRefresh When true, it means refreshing. When false, it means loading more.
     */
    private void updateEntityList(boolean isRefresh) {
        // When locked, do nothing
        if (requestLock) return;

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
                    Entity newEntity = new Entity(selectedSubject, o.getString("label"), o.getString("category"), o.getString("uri"));
                    baseEntities.add(newEntity);
                }
                binding.entityList.setAdapter(adapter = getLatestAdapter());
                if (isRefresh) binding.entityListWrapper.finishRefresh();
                else binding.entityListWrapper.finishLoadMore();
            }

            @Override
            public void onError(Exception e) { e.printStackTrace(); }
        });
    }

    /**
     * This function is used to reset tabs when **changing settings**.
     */
    private void updateSubjectTab() {
        requestLock = true;

        binding.subjectTab.removeAllTabs();
        for (int i = 0; i < availableNum; ++i) {
            binding.subjectTab.addTab(binding.subjectTab.newTab()
                    .setText(availableSubject.get(i)));
        }

        requestLock = false;
    }

    /**
     * This function is used to reset tabs when **switching back from other pages**.
     * CAUTION: It is important to know that the tab num always starts from 9.
     */
    private void resumeSubjectTab() {
        // This solution is ugly!
        Subject copy = selectedSubject;
        requestLock = true;

        binding.subjectTab.removeAllTabs();
        for (int i = 0; i < availableNum; ++i) {
            binding.subjectTab.addTab(binding.subjectTab.newTab()
                    .setText(availableSubject.get(i)));
        }

        // Set the selected state
        selectedSubject = copy;
        for (int i = 0; i < binding.subjectTab.getTabCount(); ++i) {
            TabLayout.Tab tab = binding.subjectTab.getTabAt(i);
            if (tab.getText().toString().equals(selectedSubject.toName(this.getActivity()))) tab.select();
        }

        requestLock = false;
    }

    private void setUpSubjectSelectionSection() {
        binding.subjectSelection.setLayoutManager(new GridLayoutManager(this.getActivity(), 3));
        ItemTouchHelper helper = new ItemTouchHelper(new ItemDragHelperCallback());
        helper.attachToRecyclerView(binding.subjectSelection);
        DraggableRecyclerViewAdapter<String> adapter = new DraggableRecyclerViewAdapter<>(this.getActivity(), helper, availableSubject, availableNum,
                () -> binding.headerBtn.setText("FINISH"),
                (mData, avail) -> {
                    availableNum = avail;
                    availableSubject.clear();
                    availableSubject.addAll(mData);
                });
        binding.subjectSelection.setAdapter(adapter);

        // Connect header & content
        binding.headerBtn.setOnClickListener(v -> {
            boolean isEdit = adapter.switchMode(binding.subjectSelection);
            binding.headerBtn.setText(isEdit ? "FINISH" : "EDIT");
        });
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (availableSubject.isEmpty()) {
            for (int i = 0; i < subjectNum; ++i) availableSubject.add(Subject.values()[i].toName(this.getActivity()));
        }
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        binding.homeMenuIcon.setOnClickListener(view -> ((DrawerLayout) ((Activity) getContext()).findViewById(R.id.main_drawer)).open());
        binding.homeSearchInput.setOnClickListener(view -> NavHostFragment.findNavController(this).navigate(R.id.nav_search));
        binding.homeHistoryIcon.setOnClickListener(view -> {
            NavHostFragment.findNavController(this).navigate(User.isVisitor() ? R.id.nav_login : R.id.nav_history);
        });

        binding.entityList.setLayoutManager(new LinearLayoutManager(HomeFragment.this.getActivity()));
        if (adapter != null) binding.entityList.setAdapter(adapter);

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

        binding.pageSizeInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                Editable rawInputHandler = binding.pageSizeInput.getText();
                rawInput = rawInputHandler == null ? "" : rawInputHandler.toString();
            }
        });

        setUpSubjectSelectionSection();

        binding.settings.setOnClickListener(v -> binding.expandableTabWrapper.toggle());
        binding.confirm.setOnClickListener(v -> {
            // Handle exception when the user checked nothing
            selectedSubject = Subject.fromName(this.getActivity(), availableSubject.get(0));
            updateSubjectTab();

            // We need to end editing mode here
            ((DraggableRecyclerViewAdapter<String>) binding.subjectSelection.getAdapter()).cancelEditMode(binding.subjectSelection);
            binding.headerBtn.setText("EDIT");

            // When user input nothing, we ignore it
            if (rawInput.length() > 0) {
                int newEntityNum;
                try {
                    newEntityNum = Integer.parseInt(rawInput);
                } catch (Exception e) {
                    Snackbar.make(v, R.string.entity_num_nan_alert, Snackbar.LENGTH_SHORT).show();
                    e.printStackTrace();
                    requestLock = false;
                    return;
                }
                if (newEntityNum < 5 || newEntityNum > 100) {
                    Snackbar.make(v, R.string.entity_num_out_of_range_alert, Snackbar.LENGTH_SHORT).show();
                    requestLock = false;
                    return;
                }
                entityNumEveryRequest = newEntityNum;
            }

            // Shrink the drawer & update entity list
            binding.expandableTabWrapper.toggle();
            updateEntityList(true);
        });

        // Adapter == null means it is initialization
        // Otherwise it means switching back from other pages
        if (adapter == null) {
            updateEntityList(true);
        } else {
            resumeSubjectTab();
        }

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
