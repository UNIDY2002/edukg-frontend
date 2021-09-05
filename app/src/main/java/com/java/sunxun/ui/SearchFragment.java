package com.java.sunxun.ui;

import android.os.Bundle;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
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

    private class Adapter extends RecyclerView.Adapter<Adapter.BaseViewHolder> {

        private final int WITH_CONTENT = 1;

        private final int WITH_IMAGE = 2;

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

        private class ViewHolderWithImage extends BaseViewHolder {
            public ImageView image;

            public ViewHolderWithImage(@NonNull View view) {
                super(view);
                image = view.findViewById(R.id.search_result_image);
            }
        }

        @NonNull
        @Override
        public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            switch (viewType) {
                case WITH_CONTENT:
                    return new ViewHolderWithContent(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_search_result_with_content, parent, false));
                case WITH_IMAGE:
                    return new ViewHolderWithImage(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_search_result_with_image, parent, false));
                default:
                    return new BaseViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_search_base_result, parent, false));
            }

        }

        @Override
        public void onBindViewHolder(@NonNull BaseViewHolder holder, int position) {
            SearchResult item = data.get(position);
            holder.baseResultContainer.setOnClickListener(v -> {
                Bundle bundle = new Bundle();
                bundle.putInt("subject", 0);
                bundle.putString("name", item.getLabel());
                bundle.putString("uri", item.getUri());
                NavHostFragment.findNavController(SearchFragment.this).navigate(R.id.nav_detail, bundle);
            });
            holder.baseResultName.setText(item.getLabel());
            holder.baseResultType.setText(item.getCategory());

            if (holder instanceof ViewHolderWithContent) {
                SearchResultWithContent itemWithContent = (SearchResultWithContent) data.get(position);
                ViewHolderWithContent viewHolderWithContent = (ViewHolderWithContent) holder;
                viewHolderWithContent.content.setText(itemWithContent.content);
            } else if (holder instanceof ViewHolderWithImage) {
                SearchResultWithImage itemWithImage = (SearchResultWithImage) data.get(position);
                ViewHolderWithImage viewHolderWithImage = (ViewHolderWithImage) holder;
                Glide.with(SearchFragment.this).load(itemWithImage.imageUrl).into(viewHolderWithImage.image);
            } else {
                new Timer(true).schedule(new TimerTask() {
                    @Override
                    public void run() {
                        PlatformNetwork.queryByUri(Subject.chinese, item.getUri(), new NetworkHandler<InfoByUri>(SearchFragment.this) {
                            @Override
                            public void onSuccess(InfoByUri result) {
                                List<String> featureImage = result.getFeature("图片");
                                if (featureImage != null) {
                                    data.set(position, new SearchResultWithImage(item, featureImage.get(0)));
                                    activity.runOnUiThread(() -> notifyItemChanged(position));
                                    return;
                                }

                                List<String> featureContent = result.getFeature("内容");
                                if (featureContent != null) {
                                    data.set(position, new SearchResultWithContent(item, featureContent.get(0)));
                                    activity.runOnUiThread(() -> notifyItemChanged(position));
                                    return;
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
            } else if (data.get(position) instanceof SearchResultWithImage) {
                return WITH_IMAGE;
            } else {
                return 0;
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

        private class SearchResultWithImage extends SearchResult {
            String imageUrl;

            public SearchResultWithImage(SearchResult searchResult, String imageUrl) {
                super(searchResult.getLabel(), searchResult.getCategory(), searchResult.getUri());
                this.imageUrl = imageUrl;
            }
        }

    }
}