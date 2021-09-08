package com.java.sunxun.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.*;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;
import com.java.sunxun.R;
import com.java.sunxun.dao.DetailCacheDB;
import com.java.sunxun.dao.SearchHistoryDB;
import com.java.sunxun.databinding.FragmentSearchBinding;
import com.java.sunxun.models.InfoByUri;
import com.java.sunxun.models.SearchResult;
import com.java.sunxun.models.Subject;
import com.java.sunxun.network.NetworkHandler;
import com.java.sunxun.network.PlatformNetwork;

import java.util.*;
import java.util.stream.Collectors;

/**
 * The ugliest code I've ever written.
 */
public class SearchFragment extends Fragment {

    @Nullable
    FragmentSearchBinding binding;

    private Subject subject = Subject.chinese;

    private boolean searching = true;

    Adapter adapter = null;

    Set<String> searchingSet = new HashSet<>();

    private void updateHistory(@NonNull LinearLayout container, @NonNull TextView subjectText, @NonNull EditText searchEditText, @NonNull View searchActionButton) {
        try {
            List<Pair<Subject, String>> history = SearchHistoryDB.getInstance().getHistory();
            container.removeAllViews();
            history.subList(0, Math.min(history.size(), 6)).forEach(record -> {
                @SuppressLint("InflateParams")
                View recordView = getLayoutInflater().inflate(R.layout.item_search_history, null);
                TextView recordText = recordView.findViewById(R.id.search_history_record_text);
                View deleteIcon = recordView.findViewById(R.id.search_history_delete_icon);
                recordText.setText(getString(R.string.dash_template, record.first.toName(getContext()), record.second));
                recordText.setOnClickListener(v -> {
                    try {
                        subject = record.first;
                        subjectText.setText(record.first.toName(getContext()));
                        searchEditText.setText(record.second);
                        searchActionButton.performClick();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
                deleteIcon.setOnClickListener(v -> {
                    try {
                        SearchHistoryDB.getInstance().removeHistory(record.first, record.second);
                        updateHistory(container, subjectText, searchEditText, searchActionButton);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
                container.addView(recordView);
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSearchBinding.inflate(inflater, container, false);
        binding.searchReturnIcon.setOnClickListener(view -> NavHostFragment.findNavController(this).navigateUp());
        binding.searchSubjectText.setOnClickListener(view -> {
            PopupMenu popupMenu = new PopupMenu(getContext(), view);
            popupMenu.getMenuInflater().inflate(R.menu.subject_menu, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(menuItem -> {
                Context context = getContext();
                if (context == null) return false;
                subject = Subject.fromName(context, menuItem.getTitle().toString());
                binding.searchSubjectText.setText(menuItem.getTitle());
                return true;
            });
            popupMenu.show();
        });
        binding.searchSubjectText.setText(subject.toName(getContext()));
        binding.searchSearchInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                searching = true;
                binding.searchHistoryContainer.setVisibility(View.VISIBLE);
                binding.searchRecyclerView.setVisibility(View.GONE);
                binding.searchNoResultText.setVisibility(View.GONE);
            }
        });
        binding.searchSearchInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                binding.searchActionIcon.performClick();
            }
            return false;
        });
        binding.searchActionIcon.setOnClickListener(v -> {
            Editable editText = binding.searchSearchInput.getText();
            if (editText != null) {
                binding.searchSearchInput.clearFocus();
                try {
                    SearchHistoryDB.getInstance().addHistory(subject, editText.toString());
                    updateHistory(
                            binding.searchHistoryContainer,
                            binding.searchSubjectText,
                            binding.searchSearchInput,
                            binding.searchActionIcon
                    );
                } catch (Exception e) {
                    e.printStackTrace();
                }
                searching = false;
                binding.searchHistoryContainer.setVisibility(View.GONE);
                binding.searchLoadingSpinner.setVisibility(View.VISIBLE);
                PlatformNetwork.searchInstance(subject, editText.toString(), new NetworkHandler<ArrayList<SearchResult>>(v) {
                    @Override
                    public void onSuccess(ArrayList<SearchResult> result) {
                        binding.searchLoadingSpinner.setVisibility(View.GONE);
                        if (result.isEmpty()) {
                            binding.searchRecyclerView.setVisibility(View.GONE);
                            binding.searchNoResultText.setVisibility(View.VISIBLE);
                        } else {
                            searchingSet.clear();
                            binding.searchRecyclerView.setVisibility(View.VISIBLE);
                            binding.searchRecyclerView.setAdapter(adapter = new Adapter(result));
                        }
                    }

                    @Override
                    public void onError(Exception e) {
                        binding.searchLoadingSpinner.setVisibility(View.GONE);
                        Snackbar.make(v, R.string.search_fail, Snackbar.LENGTH_SHORT).show();
                    }
                });
            }
        });
        binding.searchRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        if (adapter != null) binding.searchRecyclerView.setAdapter(adapter);
        updateHistory(
                binding.searchHistoryContainer,
                binding.searchSubjectText,
                binding.searchSearchInput,
                binding.searchActionIcon
        );
        if (searching) {
            binding.searchSearchInput.requestFocus();
            binding.searchHistoryContainer.setVisibility(View.VISIBLE);
            binding.searchRecyclerView.setVisibility(View.GONE);
            binding.searchNoResultText.setVisibility(View.GONE);
        } else {
            binding.searchHistoryContainer.setVisibility(View.GONE);
            binding.searchRecyclerView.setVisibility(View.VISIBLE);
        }
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private class Adapter extends RecyclerView.Adapter<Adapter.BaseViewHolder> {

        private final int WITH_CONTENT = 1;

        private final int WITH_CONTENT_LR = 2;

        private final int WITH_IMAGE = 3;

        private final int HEADER = 4;

        private final List<BaseSearchResult> data = new ArrayList<>();

        Adapter(List<SearchResult> source) {
            data.add(new HeaderSearchResult());
            data.addAll(source.stream().map(BaseSearchResult::new).collect(Collectors.toList()));
        }

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

        public class HeaderViewHolder extends BaseViewHolder {
            public RadioGroup sortMethodRadioGroup;
            public TextView filterCategoryText;

            public HeaderViewHolder(@NonNull View view) {
                super(view);
                sortMethodRadioGroup = view.findViewById(R.id.search_result_header_sort_method_radio_group);
                filterCategoryText = view.findViewById(R.id.search_result_header_filter_category_text);
            }
        }

        private class ViewHolderWithContent extends BaseViewHolder {
            public TextView content;

            public ViewHolderWithContent(@NonNull View view) {
                super(view);
                content = view.findViewById(R.id.search_result_content);
            }
        }

        private class ViewHolderWithContentLR extends BaseViewHolder {
            public TextView content;

            public ViewHolderWithContentLR(@NonNull View view) {
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
                case WITH_CONTENT_LR:
                    return new ViewHolderWithContentLR(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_search_result_with_content_lr, parent, false));
                case WITH_IMAGE:
                    return new ViewHolderWithImage(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_search_result_with_image, parent, false));
                case HEADER:
                    return new HeaderViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_search_result_header, parent, false));
                default:
                    return new BaseViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_search_base_result, parent, false));
            }

        }

        @Override
        public void onBindViewHolder(@NonNull BaseViewHolder holder, int position) {
            BaseSearchResult item = data.get(position);
            if (!(holder instanceof HeaderViewHolder)) {
                holder.baseResultContainer.setOnClickListener(v -> {
                    Bundle bundle = new Bundle();
                    bundle.putInt("subject", subject.ordinal());
                    bundle.putString("name", item.getLabel());
                    bundle.putString("uri", item.getUri());
                    bundle.putString("category", item.getCategory());
                    holder.baseResultName.setTextColor(getResources().getColor(R.color.grey, null));
                    NavHostFragment.findNavController(SearchFragment.this).navigate(R.id.nav_detail, bundle);
                });
                holder.baseResultName.setText(item.getLabel());
                holder.baseResultType.setText(item.getCategory());
                try {
                    item.inCache = DetailCacheDB.getInstance().hasCache(item.getUri());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                holder.baseResultName.setTextColor(getResources().getColor(item.inCache ? R.color.grey : R.color.black, null));
                String categorySelected = ((HeaderSearchResult) data.get(0)).categorySelected;
                if (categorySelected == null || Objects.equals(categorySelected, item.getCategory())) {
                    holder.baseResultContainer.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                } else {
                    ViewGroup.LayoutParams params = holder.baseResultContainer.getLayoutParams();
                    params.height = 0;
                    holder.baseResultContainer.setLayoutParams(params);
                }
            }

            if (holder instanceof ViewHolderWithContent) {
                SearchResultWithContent itemWithContent = (SearchResultWithContent) data.get(position);
                ViewHolderWithContent viewHolderWithContent = (ViewHolderWithContent) holder;
                viewHolderWithContent.content.setText(itemWithContent.content);
            } else if (holder instanceof ViewHolderWithContentLR) {
                SearchResultWithContentLR itemWithContent = (SearchResultWithContentLR) data.get(position);
                ViewHolderWithContentLR viewHolderWithContent = (ViewHolderWithContentLR) holder;
                viewHolderWithContent.content.setText(itemWithContent.content);
            } else if (holder instanceof ViewHolderWithImage) {
                SearchResultWithImage itemWithImage = (SearchResultWithImage) data.get(position);
                ViewHolderWithImage viewHolderWithImage = (ViewHolderWithImage) holder;
                Glide.with(SearchFragment.this).load(itemWithImage.imageUrl).into(viewHolderWithImage.image);
            } else if (holder instanceof HeaderViewHolder) {
                HeaderSearchResult headerSearchResult = (HeaderSearchResult) data.get(position);
                HeaderViewHolder headerViewHolder = (HeaderViewHolder) holder;
                if (headerSearchResult.selection != null) {
                    headerViewHolder.sortMethodRadioGroup.check(headerSearchResult.selection);
                } else {
                    headerViewHolder.sortMethodRadioGroup.clearCheck();
                }
                headerViewHolder.sortMethodRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
                    if (binding == null) return;
                    if (headerSearchResult.selection == null && checkedId == -1 || headerSearchResult.selection != null && headerSearchResult.selection == checkedId)
                        return;
                    binding.searchRecyclerView.post(() -> {
                        headerSearchResult.selection = checkedId;
                        List<BaseSearchResult> results = new ArrayList<>(data.subList(1, data.size()));
                        if (checkedId == R.id.search_header_sort_length_ascending) {
                            results.sort(Comparator.comparingInt(o -> o.getLabel().length()));
                        } else if (checkedId == R.id.search_header_sort_length_descending) {
                            results.sort(Comparator.comparingInt(o -> -o.getLabel().length()));
                        } else if (checkedId == R.id.search_header_sort_alphabetical_ascending) {
                            results.sort(Comparator.comparing(SearchResult::getLabel));
                        } else if (checkedId == R.id.search_header_sort_alphabetical_descending) {
                            results.sort((o1, o2) -> o2.getLabel().compareTo(o1.getLabel()));
                        }
                        data.subList(1, data.size()).clear();
                        data.addAll(results);
                        notifyDataSetChanged();
                    });
                });
                headerViewHolder.filterCategoryText.setOnClickListener(v -> {
                    PopupMenu popupMenu = new PopupMenu(getContext(), v);
                    Menu menu = popupMenu.getMenu();
                    menu.add(R.string.all);
                    data.subList(1, data.size()).stream().map(SearchResult::getCategory).distinct().sorted(String::compareTo).forEach(menu::add);
                    popupMenu.setOnMenuItemClickListener(menuItem -> {
                        headerViewHolder.filterCategoryText.setText(menuItem.getTitle());
                        headerSearchResult.categorySelected = menuItem == menu.getItem(0) ? null : menuItem.getTitle().toString();
                        if (binding == null) return false;
                        binding.searchRecyclerView.post(this::notifyDataSetChanged);
                        return true;
                    });
                    popupMenu.show();
                });
                headerViewHolder.filterCategoryText.setText(headerSearchResult.categorySelected == null ? getString(R.string.all) : headerSearchResult.categorySelected);
            } else {
                if (searchingSet.contains(item.getUri())) return;
                searchingSet.add(item.getUri());
                PlatformNetwork.queryByUri(subject, item.getUri(), new NetworkHandler<InfoByUri>(SearchFragment.this) {
                    @Override
                    public void onSuccess(InfoByUri result) {
                        try {
                            if (item == data.get(position)) {
                                String[] interestedKeysWithImage = new String[]{"图片", "图示"};

                                for (String interestedKey : interestedKeysWithImage) {
                                    List<String> featureImage = result.getFeature(interestedKey);
                                    if (featureImage != null) {
                                        data.set(position, new SearchResultWithImage(item, featureImage.get(0)));
                                        activity.runOnUiThread(() -> notifyItemChanged(position));
                                        return;
                                    }
                                }

                                String[] interestedKeysWithPerhapsLongValue = new String[]{"内容", "定义", "解释"};

                                for (String interestedKey : interestedKeysWithPerhapsLongValue) {
                                    List<String> featureContent = result.getFeature(interestedKey);
                                    if (featureContent != null) {
                                        if (featureContent.get(0).length() > 40) {
                                            data.set(position, new SearchResultWithContentLR(item, featureContent.get(0)));
                                        } else {
                                            data.set(position, new SearchResultWithContent(item, featureContent.get(0)));
                                        }
                                        activity.runOnUiThread(() -> notifyItemChanged(position));
                                        return;
                                    }
                                }

                                String[] interestedKeys = new String[]{"职业", "国籍", "体裁", "年代", "读音"};

                                for (String interestedKey : interestedKeys) {
                                    List<String> featureDefinition = result.getFeature(interestedKey);
                                    if (featureDefinition != null) {
                                        data.set(position, new SearchResultWithContent(item, featureDefinition.get(0)));
                                        activity.runOnUiThread(() -> notifyItemChanged(position));
                                        return;
                                    }
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(Exception e) {

                    }
                });
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
            } else if (data.get(position) instanceof SearchResultWithContentLR) {
                return WITH_CONTENT_LR;
            } else if (data.get(position) instanceof SearchResultWithImage) {
                return WITH_IMAGE;
            } else if (data.get(position) instanceof HeaderSearchResult) {
                return HEADER;
            } else {
                return 0;
            }
        }

        private class BaseSearchResult extends SearchResult {
            boolean inCache = false;

            public BaseSearchResult(String label, String category, String uri) {
                super(label, category, uri);
            }

            public BaseSearchResult(SearchResult searchResult) {
                super(searchResult.getLabel(), searchResult.getCategory(), searchResult.getUri());
            }
        }

        private class HeaderSearchResult extends BaseSearchResult {
            @IdRes
            Integer selection = null;

            @Nullable
            String categorySelected = null;

            public HeaderSearchResult() {
                super("", "", "");
            }
        }

        private class SearchResultWithContent extends BaseSearchResult {
            String content;

            public SearchResultWithContent(SearchResult searchResult, String content) {
                super(searchResult);
                this.content = content;
            }
        }

        private class SearchResultWithContentLR extends BaseSearchResult {
            String content;

            public SearchResultWithContentLR(SearchResult searchResult, String content) {
                super(searchResult);
                this.content = content;
            }
        }

        private class SearchResultWithImage extends BaseSearchResult {
            String imageUrl;

            public SearchResultWithImage(SearchResult searchResult, String imageUrl) {
                super(searchResult);
                this.imageUrl = imageUrl;
            }
        }

    }
}