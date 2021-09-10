package com.java.sunxun.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.snackbar.Snackbar;
import com.java.sunxun.R;
import com.java.sunxun.components.RecyclerViewAdapter;
import com.java.sunxun.dao.DetailCacheDB;
import com.java.sunxun.data.DetailViewModel;
import com.java.sunxun.databinding.FragmentDetailBinding;
import com.java.sunxun.models.*;
import com.java.sunxun.network.ApplicationNetwork;
import com.java.sunxun.network.NetworkHandler;
import com.java.sunxun.network.PlatformNetwork;
import com.java.sunxun.utils.Share;

import org.w3c.dom.Text;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 使用 Bundle 传參，导航方式形如 NavController.navigate(R.id.nav_detail, bundle);
 * <p>
 * bundle 中要求有一个整数项 subject（使用序号表示）以及三个字符串项 name、category 和 uri。
 */
public class DetailFragment extends Fragment {

    @Nullable
    FragmentDetailBinding binding;

    private DetailViewModel viewModel;
    private final ArrayList<Pair<String, String>> shortEntityProperty = new ArrayList<>();
    private final ArrayList<Pair<String, String>> longEntityProperty = new ArrayList<>();
    private ArrayList<Pair<String, String>> imageProperty = new ArrayList<>();
    private ArrayList<Pair<String, InfoByName>> subjectRelationList = new ArrayList<>();
    private ArrayList<Pair<String, InfoByName>> objectRelationList = new ArrayList<>();

    private static final String[] alphabet = new String[]{"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N"};

    private int optionChosen = -1;

    /**
     * 用于根据给定数据绘制列表的函数
     * @param data      数据源，类型为 Arraylist
     * @param layout    所要采取的布局
     * @param attachTo  所属的 LinearLayout
     * @param converter 将数据转换到 UI 的转换函数
     * @param emptyTip  数据为空的时候的提示文字，置空则不会在数据为空的时候产生空组件
     * @param <T>       数据的类型
     */
    private <T> void draw(ArrayList<T> data, int layout, LinearLayout attachTo, BiConsumer<T, View> converter, String emptyTip) {
        if (data.size() == 0 && !emptyTip.isEmpty()) {
            TextView caption = new TextView(DetailFragment.this.getActivity());
            caption.setText(emptyTip);
            attachTo.addView(caption);
            return;
        }
        for (T item: data) {
            @SuppressLint("InflateParams")
            View view = LayoutInflater.from(this.getActivity()).inflate(layout, null);
            converter.accept(item, view);
            attachTo.addView(view);
        }
    }

    private void drawUI(InfoByName info, boolean isCache, Subject subject) {
        ArrayList<Pair<String, String>> entityProperty = info.getPropertyList();
        subjectRelationList = info.getSubjectRelationList();
        objectRelationList = info.getObjectRelationList();
        imageProperty = info.getImgProperty();
        shortEntityProperty.clear();
        longEntityProperty.clear();
        for (int i = 0; i < entityProperty.size(); ++i)
            (entityProperty.get(i).second.length() > 30 ? longEntityProperty : shortEntityProperty)
                    .add(entityProperty.get(i));

        // 编写属性列表的 UI
        draw(shortEntityProperty, R.layout.item_detail_property, binding.propertyList, (data, view) -> {
            ((TextView) view.findViewById(R.id.property_key)).setText(data.first);
            ((TextView) view.findViewById(R.id.property_val)).setText(data.second);
        }, "*暂无可使用的属性。");

        // 编写知识卡片的 UI
        draw(longEntityProperty, R.layout.item_detail_long_property, binding.longPropertyList, (data, view) -> {
            ((TextView) view.findViewById(R.id.long_property_key)).setText(data.first);
            ((TextView) view.findViewById(R.id.long_property_val)).setText(data.second);
        }, "");

        // 编写实体关系的 UI
        BiConsumer<String, View> navToNeighbor = (label, v) -> PlatformNetwork.searchInstance(subject, label, new NetworkHandler<ArrayList<SearchResult>>(DetailFragment.this) {
            @Override
            public void onSuccess(ArrayList<SearchResult> result) {
                if (result.size() == 0) {
                    Snackbar.make(v, R.string.do_not_support_linking, Snackbar.LENGTH_SHORT).show();
                    return;
                }
                SearchResult res = result.get(0);
                Bundle mBundle = new Bundle();
                mBundle.putString("name", res.getLabel());
                mBundle.putString("uri", res.getUri());
                mBundle.putString("category", res.getCategory());
                mBundle.putInt("subject", subject.ordinal());
                NavHostFragment.findNavController(DetailFragment.this).navigate(R.id.nav_detail, mBundle);
            }

            @Override
            public void onError(Exception e) {
                e.printStackTrace();
            }
        });
        draw(objectRelationList, R.layout.item_detail_relation, binding.relationList, (data, view) -> {
            ((TextView) view.findViewById(R.id.relation_head)).setText("【当前实体】");
            ((TextView) view.findViewById(R.id.relation_name)).setText(data.first);
            ((TextView) view.findViewById(R.id.relation_target)).setText(data.second.getLabel());
            ((TextView) view.findViewById(R.id.relation_target)).setTextColor(getResources().getColor(R.color.teal_700, DetailFragment.this.getActivity().getTheme()));
            if (!isCache) ((TextView) view.findViewById(R.id.relation_target)).setOnClickListener(v -> navToNeighbor.accept(data.second.getLabel(), v));
        }, "*暂无可用的从实体关系。");
        draw(subjectRelationList, R.layout.item_detail_relation, binding.relationList, (data, view) -> {
            ((TextView) view.findViewById(R.id.relation_target)).setText("【当前实体】");
            ((TextView) view.findViewById(R.id.relation_name)).setText(data.first);
            ((TextView) view.findViewById(R.id.relation_head)).setText(data.second.getLabel());
            ((TextView) view.findViewById(R.id.relation_head)).setTextColor(getResources().getColor(R.color.teal_700, DetailFragment.this.getActivity().getTheme()));
            if (!isCache) ((TextView) view.findViewById(R.id.relation_head)).setOnClickListener(v -> navToNeighbor.accept(data.second.getLabel(), v));
        }, "*暂无可用的主实体关系。");

        // 编写图片的 UI
        if (!isCache) {
            draw(imageProperty, R.layout.item_detail_img, binding.imgList, (data, view) -> {
                ImageView imgView = new ImageView(DetailFragment.this.getActivity());
                Glide.with(DetailFragment.this.getActivity()).load(data.second).into(imgView);
                binding.imgList.addView(imgView);
            }, "*暂无相关图片。");
        } else {
            TextView caption = new TextView(DetailFragment.this.getActivity());
            caption.setText("*暂无相关图片");
            binding.imgList.addView(caption);
        }
    }

    private void postProblemRes(String label, String uri, boolean isCorrect, View v) {
        Snackbar.make(v, isCorrect ? "您作答正确！" : "您作答错误。", Snackbar.LENGTH_LONG).show();
        ApplicationNetwork.uploadTestResult(label, uri, isCorrect, new NetworkHandler<Boolean>(this) {
            @Override
            public void onSuccess(Boolean result) {

            }

            @Override
            public void onError(Exception e) {

            }
        });
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(DetailViewModel.class);
        binding = FragmentDetailBinding.inflate(inflater, container, false);
        binding.detailReturnIcon.setOnClickListener(view -> NavHostFragment.findNavController(this).navigateUp());

        // 从 bundle 中获取参数
        Bundle bundle = getArguments();
        Context context = getContext();
        if (bundle != null && context != null) {
            Subject subject = Subject.values()[bundle.getInt("subject", 0)];
            String name = bundle.getString("name", "");
            String uri = bundle.getString("uri", "");
            String category = bundle.getString("category", "");
            binding.detailHeaderText.setText(name);

            // 向后端询问当前实体是否已收藏，并设置 starStatus 的值
            ApplicationNetwork.isStar(uri, new NetworkHandler<List<Pair<String, Boolean>>>(this) {
                @Override
                public void onSuccess(List<Pair<String, Boolean>> result) {
                    viewModel.setStarStatus(!result.isEmpty());
                }

                @Override
                public void onError(Exception e) {
                    e.printStackTrace();
                }
            });

            // 设置收藏按钮的点击事件
            binding.detailStarButton.setOnClickListener(v -> ApplicationNetwork.isStar(uri, new NetworkHandler<List<Pair<String, Boolean>>>(DetailFragment.this) {
                @Override
                public void onSuccess(List<Pair<String, Boolean>> starResult) {
                    // 展示弹窗
                    binding.detailShadow.setVisibility(View.VISIBLE);
                    binding.detailSharePopupContainer.setVisibility(View.VISIBLE);
                    binding.detailSharePopupConfirmButton.setEnabled(false);

                    // 设置收藏文件夹的 RecyclerView
                    StarFolderListAdapter starFolderListAdapter = new StarFolderListAdapter(
                            starResult.stream().map(result -> new StarFolder(result.first, result.second)).collect(Collectors.toList()),
                            binding.detailSharePopupConfirmButton
                    );
                    binding.detailSharePopupRecyclerView.setLayoutManager(new LinearLayoutManager(context));
                    binding.detailSharePopupRecyclerView.setAdapter(starFolderListAdapter);

                    // 设置收藏新建收藏夹的点击事件
                    binding.detailSharePopupCreateFolder.setOnClickListener(v -> starFolderListAdapter.setModeToCreateFolder());

                    // 设置收藏确认按钮的点击事件
                    binding.detailSharePopupConfirmButton.setOnClickListener(v -> {
                        List<String> folderStar = new ArrayList<>();
                        List<String> folderUnstar = new ArrayList<>();
                        starFolderListAdapter.getData().forEach(folder -> {
                            if (folder.isStar != folder.wantToStar) {
                                String folderNameUpload = folder.name.equals(getString(R.string.default_folder)) ? "default" : folder.name;
                                if (folder.wantToStar) {
                                    folderStar.add(folderNameUpload);
                                } else {
                                    folderUnstar.add(folderNameUpload);
                                }
                            }
                        });
                        ApplicationNetwork.star(subject, uri, name, category, folderStar, folderUnstar, new NetworkHandler<Boolean>(v) {
                            @Override
                            public void onSuccess(Boolean result) {
                                viewModel.setStarStatus(starFolderListAdapter.getData().stream().anyMatch(folder -> folder.wantToStar));
                                binding.detailShadow.callOnClick();
                            }

                            @Override
                            public void onError(Exception e) {

                            }
                        });
                    });
                }

                @Override
                public void onError(Exception e) {
                    Snackbar.make(v, R.string.network_error, Snackbar.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }));

            // 设置分享按钮的点击事件
            binding.detailShareButton.setOnClickListener(view -> Share.share(view.getContext(), getString(R.string.detail_share_template, name, subject.toName(view.getContext()), uri)));

            // 点击 detailShadow 隐藏收藏遮罩
            binding.detailShadow.setOnClickListener(v -> {
                binding.detailShadow.setVisibility(View.GONE);
                binding.detailSharePopupContainer.setVisibility(View.GONE);
            });

            // 阻拦按到弹出框上的点击事件
            binding.detailSharePopupContainer.setOnClickListener(v -> {
            });

            // 从缓存中加载
            try {
                InfoByName cache = DetailCacheDB.getInstance().getCache(uri);
                if (cache != null) drawUI(cache, true, subject);
            } catch (Exception e) {
                e.printStackTrace();
            }

            // 利用 bundle 中的学科和实体名称进行实体详情的查询
            PlatformNetwork.queryByName(subject, name, new NetworkHandler<InfoByName>(this) {
                @Override
                public void onSuccess(InfoByName result) {
                    // 存入缓存
                    try {
                        DetailCacheDB.getInstance().addCache(uri, result);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    drawUI(result, false, subject);
                }

                @Override
                public void onError(Exception e) {
                    e.printStackTrace();
                }
            });

            // 利用 bundle 中的实体名称进行相关试题的查询
            PlatformNetwork.relatedProblems(name, new NetworkHandler<ArrayList<Problem>>(this) {
                @Override
                public void onSuccess(ArrayList<Problem> result) {
                    // 向后端添加访问历史记录
                    ApplicationNetwork.addHistory(subject, uri, name, category, result.size() > 0, new NetworkHandler<Boolean>(DetailFragment.this) {
                        @Override
                        public void onSuccess(Boolean result) {

                        }

                        @Override
                        public void onError(Exception e) {

                        }
                    });

                    // 提示文字
                    if (result.size() == 0) {
                        TextView caption = new TextView(DetailFragment.this.getActivity());
                        caption.setText("*暂无相关试题");
                        binding.problemList.addView(caption);
                        return;
                    }

                    // 随机渲染一个试题
                    Problem problem = result.get(new Random().nextInt(result.size()));
                    @SuppressLint("InflateParams")
                    View problemContainer = LayoutInflater.from(DetailFragment.this.getActivity()).inflate(R.layout.item_detail_problem, null);
                    ((TextView) problemContainer.findViewById(R.id.detail_problem_question)).setText(problem.getQuestion());
                    LinearLayout optionContainer = problemContainer.findViewById(R.id.detail_problem_options);

                    Pair<String[], Integer> options = problem.genRandomOptions(alphabet.length);
                    for (int i = 0; i < options.first.length; ++i) {
                        String option = options.first[i];
                        @SuppressLint("InflateParams")
                        View view = getLayoutInflater().inflate(R.layout.item_user_test_option, null);
                        TextView textView = view.findViewById(R.id.user_test_option_text);
                        String optionText = getString(R.string.user_test_option, alphabet[i], option);
                        textView.setText(optionText);
                        final int j = i;
                        view.setOnClickListener(v -> {
                            for (int k = 0; k < optionContainer.getChildCount(); k++) {
                                optionContainer.getChildAt(k).setEnabled(false);
                            }
                            textView.setTextColor(getResources().getColor(R.color.red, null));
                            ((TextView) optionContainer.getChildAt(options.second).findViewById(R.id.user_test_option_text)).setTextColor(getResources().getColor(R.color.green, null));
                            textView.setTextColor(getResources().getColor((j == options.second ? R.color.green : R.color.red), null));
                            ApplicationNetwork.uploadTestResult(uri, name, j == options.second, new NetworkHandler<Boolean>(DetailFragment.this) {
                                @Override
                                public void onSuccess(Boolean result) {

                                }

                                @Override
                                public void onError(Exception e) {

                                }
                            });
                        });
                        optionContainer.addView(view);
                    }
                    binding.problemList.addView(problemContainer);
                }

                @Override
                public void onError(Exception e) {
                    // 网络请求失败的时候的提示文字
                    TextView caption = new TextView(DetailFragment.this.getActivity());
                    caption.setText("*暂无相关试题");
                    binding.problemList.addView(caption);

                    // 向后端添加访问历史记录
                    ApplicationNetwork.addHistory(subject, uri, name, category, null, new NetworkHandler<Boolean>(DetailFragment.this) {
                        @Override
                        public void onSuccess(Boolean result) {

                        }

                        @Override
                        public void onError(Exception e) {

                        }
                    });
                    e.printStackTrace();
                }
            });
        }

        // 监听 starStatus 的值，设置标题栏中五角形的图标
        viewModel.getStarStatus().observe(getViewLifecycleOwner(), starStatus -> {
            if (starStatus != null) {
                binding.detailStarButton.setVisibility(View.VISIBLE);
                binding.detailStarButton.setCompoundDrawablesWithIntrinsicBounds(
                        starStatus ? R.drawable.ic_star : R.drawable.ic_star_border, 0, 0, 0
                );
            }
        });

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private static class StarFolder {
        String name;
        boolean isStar;
        boolean wantToStar;

        StarFolder(String name, boolean isStar){
            this.name = name;
            this.isStar = this.wantToStar = isStar;
        }
    }

    private class StarFolderListAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
        private final List<StarFolder> starFolders;
        private final Button confirmButton;
        private boolean createFolder = false;

        StarFolderListAdapter(List<StarFolder> starFolders, Button confirmButton) {
            this.starFolders = starFolders;
            this.confirmButton = confirmButton;
        }

        @NonNull
        @Override
        public RecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new RecyclerViewAdapter.ViewHolder(LayoutInflater.from(getContext()).inflate(
                    viewType == 1 ? R.layout.item_star_create_folder : R.layout.item_star_folder, parent, false
            ));
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerViewAdapter.ViewHolder holder, int position) {
            if (position == starFolders.size()) {
                EditText editText = holder.getViewById(R.id.star_folder_create_input);
                Button button = holder.getViewById(R.id.star_folder_create_action);
                editText.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        button.setEnabled(s != null && !s.toString().isEmpty() && !s.toString().equals("默认文件夹") && !s.toString().equals("default"));
                    }
                });
                button.setOnClickListener(v -> {
                    String folder = editText.toString();
                    ApplicationNetwork.addFolder(folder, new NetworkHandler<Boolean>(v) {
                        @Override
                        public void onSuccess(Boolean result) {
                            if (result) {
                                starFolders.add(new StarFolder(folder, false));
                                createFolder = false;
                                notifyItemChanged(starFolders.size() - 1);
                            } else {
                                Snackbar.make(v, R.string.network_error, Snackbar.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onError(Exception e) {
                            Snackbar.make(v, R.string.network_error, Snackbar.LENGTH_SHORT).show();
                        }
                    });
                });
            } else {
                StarFolder data = starFolders.get(position);
                TextView textView = holder.getViewById(R.id.star_folder_name_text);
                CheckBox checkBox = holder.getViewById(R.id.star_folder_name_checked);
                textView.setText(data.name);
                textView.setOnClickListener(v -> checkBox.setChecked(!checkBox.isChecked()));
                checkBox.setChecked(data.isStar);
                checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    data.wantToStar = isChecked;
                    confirmButton.setEnabled(starFolders.stream().anyMatch(starFolder -> starFolder.isStar != starFolder.wantToStar));
                });
            }
        }

        @Override
        public int getItemCount() {
            return starFolders.size() + (createFolder ? 1 : 0);
        }

        @Override
        public int getItemViewType(int position) {
            return position == starFolders.size() ? 1 : 0;
        }

        public List<StarFolder> getData() {
            return this.starFolders;
        }

        public void setModeToCreateFolder(){
            if (!createFolder) {
                createFolder = true;
                notifyItemInserted(starFolders.size());
            }
        }
    }
}