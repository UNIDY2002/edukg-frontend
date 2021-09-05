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
import com.java.sunxun.models.InfoByUri;
import com.java.sunxun.models.SearchResult;
import com.java.sunxun.models.Subject;
import com.java.sunxun.network.NetworkHandler;
import com.java.sunxun.network.PlatformNetwork;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

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

    private class Adapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private final int BASE = 0;

        private final int WITH_CONTENT = 1;

        boolean loaded = false;

        private List<SearchResult> data = new ArrayList<>();

        private class BaseViewHolder extends RecyclerView.ViewHolder {
            public View baseResultContainer;
            public TextView baseResultName;
            public TextView baseResultType;

            public BaseViewHolder(@NonNull View view) {
                super(view);
                baseResultContainer = view.findViewById(R.id.search_base_result_container);
                baseResultName = view.findViewById(R.id.search_base_result_name);
                baseResultType = view.findViewById(R.id.search_base_result_type);
            }
        }

        private class ViewHolderWithContent extends BaseViewHolder {
            public TextView content;

            public ViewHolderWithContent(@NonNull View view) {
                super(view);
                content = view.findViewById(R.id.search_result_content);
            }
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            switch (viewType) {
                case WITH_CONTENT:
                    return new ViewHolderWithContent(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_search_result_with_content, parent, false));
                default:
                    return new BaseViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_search_base_result, parent, false));
            }

        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof ViewHolderWithContent) {
                SearchResultWithContent item = (SearchResultWithContent) data.get(position);
                ViewHolderWithContent viewHolderWithContent = (ViewHolderWithContent) holder;
                viewHolderWithContent.baseResultContainer.setOnClickListener(v -> {
                    Bundle bundle = new Bundle();
                    bundle.putInt("subject", 0);
                    bundle.putString("name", item.getLabel());
                    bundle.putString("uri", item.getUri());
                    NavHostFragment.findNavController(SearchFragment.this).navigate(R.id.nav_detail, bundle);
                });
                viewHolderWithContent.baseResultName.setText(item.getLabel());
                viewHolderWithContent.baseResultType.setText(item.getCategory());
                viewHolderWithContent.content.setText(item.content);
            } else if (holder instanceof BaseViewHolder) {
                SearchResult item = data.get(position);
                BaseViewHolder baseViewHolder = (BaseViewHolder) holder;
                baseViewHolder.baseResultContainer.setOnClickListener(v -> {
                    Bundle bundle = new Bundle();
                    bundle.putInt("subject", 0);
                    bundle.putString("name", item.getLabel());
                    bundle.putString("uri", item.getUri());
                    NavHostFragment.findNavController(SearchFragment.this).navigate(R.id.nav_detail, bundle);
                });
                baseViewHolder.baseResultName.setText(item.getLabel());
                baseViewHolder.baseResultType.setText(item.getCategory());
                new Timer(true).schedule(new TimerTask() {
                    @Override
                    public void run() {
                        PlatformNetwork.queryByUri(Subject.chinese, item.getUri(), new NetworkHandler<InfoByUri>(SearchFragment.this) {
                            @Override
                            public void onSuccess(InfoByUri result) {
                                List<String> feature = result.getFeature("内容");
                                if (feature != null) {
                                    data.set(position, new SearchResultWithContent(item, feature.get(0)));
                                    activity.runOnUiThread(() -> {
                                        notifyItemChanged(position);
                                    });
                                }
                            }

                            @Override
                            public void onError(Exception e) {

                            }
                        });
                    }
                }, position * 200L);
            }
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        @Override
        public int getItemViewType(int position) {
            if (data.get(position) instanceof SearchResultWithContent) {
                return WITH_CONTENT;
            } else {
                return BASE;
            }
        }

        public void updateData(List<SearchResult> source) {
            data = source;
            notifyDataSetChanged();
            loaded = true;
        }

        private class SearchResultWithContent extends SearchResult {
            String content;

            public SearchResultWithContent(SearchResult searchResult, String content) {
                super(searchResult.getLabel(), searchResult.getCategory(), searchResult.getUri());
                this.content = content;
            }
        }

    }
}