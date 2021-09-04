package com.java.sunxun.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.android.material.snackbar.Snackbar;
import com.java.sunxun.R;
import com.java.sunxun.components.RecyclerViewAdapter;
import com.java.sunxun.databinding.FragmentStarBinding;
import com.java.sunxun.network.ApplicationNetwork;
import com.java.sunxun.network.NetworkHandler;

import java.util.ArrayList;
import java.util.List;

public class StarFragment extends Fragment {

    @Nullable
    FragmentStarBinding binding;

    private final List<StarItem> starList = new ArrayList<>();

    private void updateStarList(RecyclerViewAdapter<StarItem> adapter) {
        ApplicationNetwork.getStarList(new NetworkHandler<ArrayList<String>>(this) {
            @Override
            public void onSuccess(ArrayList<String> result) {
                starList.clear();
                result.forEach(uri -> starList.add(new StarItem(uri, true)));
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onError(Exception e) {
                if (binding != null && e.getMessage() != null) {
                    Snackbar.make(binding.starListRecyclerView, e.getMessage(), Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentStarBinding.inflate(inflater, container, false);
        binding.starReturnIcon.setOnClickListener(view -> NavHostFragment.findNavController(this).navigateUp());
        binding.starSubjectText.setOnClickListener(view -> {
            PopupMenu popupMenu = new PopupMenu(getContext(), view);
            popupMenu.getMenuInflater().inflate(R.menu.subject_menu, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(menuItem -> {
                binding.starSubjectText.setText(menuItem.getTitle());
                return true;
            });
            popupMenu.show();
        });

        RecyclerViewAdapter<StarItem> adapter = new RecyclerViewAdapter<StarItem>(getContext(), R.layout.item_star_entity, starList) {
            @Override
            public void convert(ViewHolder holder, StarItem data) {
                ((TextView) holder.getViewById(R.id.star_entity_name)).setText(data.uri);
                ((TextView) holder.getViewById(R.id.star_entity_category)).setText(data.uri);
                ImageView star = holder.getViewById(R.id.star_entity_star);
                star.setImageResource(data.star ? R.drawable.ic_star : R.drawable.ic_star_border);
                star.setOnClickListener(view -> ApplicationNetwork.star(data.uri, new NetworkHandler<Boolean>(view) {
                    @Override
                    public void onSuccess(Boolean result) {
                        star.setImageResource(result ? R.drawable.ic_star : R.drawable.ic_star_border);
                    }

                    @Override
                    public void onError(Exception e) {
                        Snackbar.make(view, R.string.network_error, Snackbar.LENGTH_LONG).show();
                    }
                }));
            }
        };

        binding.starListRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.starListRecyclerView.setAdapter(adapter);
        updateStarList(adapter);

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private static class StarItem {
        String uri;
        boolean star;

        StarItem(String uri, boolean star) {
            this.uri = uri;
            this.star = star;
        }
    }

}