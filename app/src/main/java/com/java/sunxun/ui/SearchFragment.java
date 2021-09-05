package com.java.sunxun.ui;

import android.os.Bundle;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.snackbar.Snackbar;
import com.java.sunxun.R;
import com.java.sunxun.databinding.FragmentSearchBinding;
import com.java.sunxun.models.SearchResult;
import com.java.sunxun.models.Subject;
import com.java.sunxun.network.NetworkHandler;
import com.java.sunxun.network.PlatformNetwork;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment {

    @Nullable
    FragmentSearchBinding binding;

    Adapter adapter = new Adapter();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSearchBinding.inflate(inflater, container, false);
        binding.searchReturnIcon.setOnClickListener(view -> NavHostFragment.findNavController(this).navigateUp());
        binding.searchSearchInput.requestFocus();
        binding.searchSearchInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                binding.searchActionIcon.performClick();
            }
            return false;
        });
        binding.searchActionIcon.setOnClickListener(v -> {
            Editable editText = binding.searchSearchInput.getText();
            if (editText != null) {
                PlatformNetwork.searchInstance(Subject.chinese, editText.toString(), new NetworkHandler<ArrayList<SearchResult>>(v) {
                    @Override
                    public void onSuccess(ArrayList<SearchResult> result) {
                        adapter.updateData(result);
                    }

                    @Override
                    public void onError(Exception e) {
                        Snackbar.make(v, R.string.search_fail, Snackbar.LENGTH_SHORT).show();
                    }
                });
            }
        });
        binding.searchRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.searchRecyclerView.setAdapter(adapter);
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {

        boolean loaded = false;

        private List<SearchResult> data = new ArrayList<>();

        private class ViewHolder extends RecyclerView.ViewHolder {
            TextView normalResultText;

            public ViewHolder(@NonNull View view) {
                super(view);
                normalResultText = view.findViewById(R.id.search_normal_result_text);
                normalResultText.setOnClickListener(v -> {
                    Bundle bundle = new Bundle();
                    bundle.putInt("subject", 0);
                    bundle.putString("name", "UNKNOWN");
                    bundle.putString("uri", "uri");
                    NavHostFragment.findNavController(SearchFragment.this).navigate(R.id.nav_detail, bundle);
                });
            }
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_search_normal_result, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.normalResultText.setText(data.get(position).getLabel());
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        public void updateData(List<SearchResult> source) {
            data = source;
            notifyDataSetChanged();
            loaded = true;
        }

    }
}