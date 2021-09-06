package com.java.sunxun.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.snackbar.Snackbar;
import com.java.sunxun.R;
import com.java.sunxun.components.RecyclerViewAdapter;
import com.java.sunxun.dao.DetailCacheDB;
import com.java.sunxun.data.DetailViewModel;
import com.java.sunxun.databinding.FragmentDetailBinding;
import com.java.sunxun.models.InfoByName;
import com.java.sunxun.models.Subject;
import com.java.sunxun.network.ApplicationNetwork;
import com.java.sunxun.network.NetworkHandler;
import com.java.sunxun.network.PlatformNetwork;
import com.java.sunxun.utils.Share;

import java.util.ArrayList;
import java.util.List;

/**
 * 使用 Bundle 传參，导航方式形如 NavController.navigate(R.id.nav_detail, bundle);
 * <p>
 * bundle 中要求有一个整数项 subject（使用序号表示）以及三个字符串项 name、category 和 uri。
 */
public class DetailFragment extends Fragment {

    @Nullable
    FragmentDetailBinding binding;

    private DetailViewModel viewModel;
    private List<Pair<String, String>> shortEntityProperty = new ArrayList<>();
    private List<Pair<String, String>> longEntityProperty = new ArrayList<>();

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
            ApplicationNetwork.isStar(uri, new NetworkHandler<Boolean>(this) {
                @Override
                public void onSuccess(Boolean result) {
                    viewModel.setStarStatus(result);
                }

                @Override
                public void onError(Exception e) {
                    e.printStackTrace();
                }
            });

            // 向后端添加访问历史记录
            ApplicationNetwork.addHistory(subject, uri, name, category, new NetworkHandler<Boolean>(this) {
                @Override
                public void onSuccess(Boolean result) {

                }

                @Override
                public void onError(Exception e) {

                }
            });

            // 用于展示收藏夹列表的 RecyclerViewAdapter
            RecyclerViewAdapter<String> shareFolderListAdapter = new RecyclerViewAdapter<String>(context, R.layout.item_star_folder, new ArrayList<>()) {
                @Override
                public void convert(ViewHolder holder, String data, int position) {
                    TextView textView = holder.getViewById(R.id.star_folder_name_text);
                    textView.setText(data);
                    textView.setOnClickListener(v -> ApplicationNetwork.star(subject, uri, name, category, new NetworkHandler<Boolean>(v) {
                        @Override
                        public void onSuccess(Boolean result) {
                            viewModel.setStarStatus(true);
                            binding.detailShadow.callOnClick();
                        }

                        @Override
                        public void onError(Exception e) {
                            Snackbar.make(v, R.string.star_fail, Snackbar.LENGTH_SHORT).show();
                        }
                    }));
                }
            };

            // 设置收藏文件夹的 RecyclerView
            binding.detailSharePopupRecyclerView.setLayoutManager(new LinearLayoutManager(context));
            binding.detailSharePopupRecyclerView.setAdapter(shareFolderListAdapter);

            // 设置收藏按钮的点击事件
            binding.detailStarButton.setOnClickListener(v -> {
                Boolean starStatus = viewModel.getStarStatus().getValue();
                if (starStatus != null && starStatus) {
                    // 取消收藏
                    ApplicationNetwork.unstar(uri, new NetworkHandler<Boolean>(v) {
                        @Override
                        public void onSuccess(Boolean result) {
                            viewModel.setStarStatus(false);
                        }

                        @Override
                        public void onError(Exception e) {
                            Snackbar.make(v, R.string.cancel_star_fail, Snackbar.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    // 收藏
                    binding.detailShadow.setVisibility(View.VISIBLE);
                    binding.detailSharePopupContainer.setVisibility(View.VISIBLE);
                    ArrayList<String> fakeFolderList = new ArrayList<>();
                    fakeFolderList.add(getString(R.string.default_folder));
                    shareFolderListAdapter.updateData(fakeFolderList);
                }
            });

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
                if (cache != null) {
                    // TODO: 加载缓存中获得的数据
                }
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

                    ArrayList<Pair<String, String>> entityProperty = result.getPropertyList();
                    shortEntityProperty.clear();
                    longEntityProperty.clear();
                    for (int i = 0; i < entityProperty.size(); ++i)
                        (entityProperty.get(i).second.length() > 30 ? longEntityProperty : shortEntityProperty)
                                .add(entityProperty.get(i));

                    // 编写属性列表的 UI
                    for (Pair<String, String> prop: shortEntityProperty) {
                        @SuppressLint("InflateParams")
                        View propItemView = LayoutInflater.from(this.activity).inflate(R.layout.item_detail_property, null);
                        ((TextView) propItemView.findViewById(R.id.property_key)).setText(prop.first);
                        ((TextView) propItemView.findViewById(R.id.property_val)).setText(prop.second);
                        binding.propertyList.addView(propItemView);
                    }

                    // 编写知识卡片的 UI
                    for (Pair<String, String> props: longEntityProperty) {
                        @SuppressLint("InflateParams")
                        View knowledgeCardView = LayoutInflater.from(this.activity).inflate(R.layout.item_detail_long_property, null);
                        ((TextView) knowledgeCardView.findViewById(R.id.long_property_key)).setText(props.first);
                        ((TextView) knowledgeCardView.findViewById(R.id.long_property_val)).setText(props.second);
                        binding.longPropertyList.addView(knowledgeCardView);
                    }
                }

                @Override
                public void onError(Exception e) {
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
}