package com.java.sunxun.ui;

import android.content.Context;
import android.os.Bundle;
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
import com.java.sunxun.data.DetailViewModel;
import com.java.sunxun.databinding.FragmentDetailBinding;
import com.java.sunxun.models.InfoByName;
import com.java.sunxun.models.Subject;
import com.java.sunxun.network.ApplicationNetwork;
import com.java.sunxun.network.NetworkHandler;
import com.java.sunxun.network.PlatformNetwork;
import com.java.sunxun.utils.Share;

import java.util.ArrayList;

/**
 * 使用 Bundle 传參，导航方式形如 NavController.navigate(R.id.nav_detail, bundle);
 * <p>
 * bundle 中要求有一个整数项 subject（使用序号表示）以及两个字符串项 name 和 uri。
 */
public class DetailFragment extends Fragment {

    @Nullable
    FragmentDetailBinding binding;

    private DetailViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(DetailViewModel.class);
        binding = FragmentDetailBinding.inflate(inflater, container, false);
        binding.detailReturnIcon.setOnClickListener(view -> NavHostFragment.findNavController(this).navigateUp());

        Bundle bundle = getArguments();
        Context context = getContext();
        if (bundle != null && context != null) {
            Subject subject = Subject.values()[bundle.getInt("subject", 0)];
            String name = bundle.getString("name", "");
            String uri = bundle.getString("uri", "");
            binding.detailHeaderText.setText(name);

            PlatformNetwork.queryByName(subject, name, new NetworkHandler<InfoByName>(this) {
                @Override
                public void onSuccess(InfoByName result) {

                }

                @Override
                public void onError(Exception e) {

                }
            });

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

            RecyclerViewAdapter<String> shareFolderListAdapter = new RecyclerViewAdapter<String>(context, R.layout.item_star_folder, new ArrayList<>()) {
                @Override
                public void convert(ViewHolder holder, String data, int position) {
                    TextView textView = holder.getViewById(R.id.star_folder_name_text);
                    textView.setText(data);
                    textView.setOnClickListener(v -> ApplicationNetwork.star(uri, new NetworkHandler<Boolean>(v) {
                        @Override
                        public void onSuccess(Boolean result) {
                            viewModel.setStarStatus(result);
                            binding.detailShadow.callOnClick();
                        }

                        @Override
                        public void onError(Exception e) {
                            Snackbar.make(v, R.string.star_fail, Snackbar.LENGTH_SHORT).show();
                        }
                    }));
                }
            };

            binding.detailShareButton.setOnClickListener(view -> Share.share(view.getContext(), getString(R.string.detail_share_template, name, subject.toName(view.getContext()), uri)));

            binding.detailStarButton.setOnClickListener(v -> {
                Boolean starStatus = viewModel.getStarStatus().getValue();
                if (starStatus != null && starStatus) {
                    ApplicationNetwork.star(uri, new NetworkHandler<Boolean>(v) {
                        @Override
                        public void onSuccess(Boolean result) {
                            viewModel.setStarStatus(result);
                        }

                        @Override
                        public void onError(Exception e) {
                            Snackbar.make(v, R.string.cancel_star_fail, Snackbar.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    binding.detailShadow.setVisibility(View.VISIBLE);
                    binding.detailSharePopupContainer.setVisibility(View.VISIBLE);
                    ArrayList<String> fakeFolderList = new ArrayList<>();
                    fakeFolderList.add(getString(R.string.default_folder));
                    shareFolderListAdapter.updateData(fakeFolderList);
                }
            });

            binding.detailShadow.setOnClickListener(v -> {
                binding.detailShadow.setVisibility(View.GONE);
                binding.detailSharePopupContainer.setVisibility(View.GONE);
            });

            binding.detailSharePopupContainer.setOnClickListener(v -> {
            });

            binding.detailSharePopupRecyclerView.setLayoutManager(new LinearLayoutManager(context));

            binding.detailSharePopupRecyclerView.setAdapter(shareFolderListAdapter);
        }

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