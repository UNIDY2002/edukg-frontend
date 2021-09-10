package com.java.sunxun.ui;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
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
import com.java.sunxun.models.Star;
import com.java.sunxun.models.Subject;
import com.java.sunxun.network.ApplicationNetwork;
import com.java.sunxun.network.NetworkHandler;

import java.util.ArrayList;
import java.util.List;

public class StarFragment extends Fragment {

    @Nullable
    FragmentStarBinding binding;

    private String folderName = null;

    private Subject subject = null;

    private final List<StarItem> starList = new ArrayList<>();

    private void updateStarList(RecyclerViewAdapter<StarItem> adapter) {
        ApplicationNetwork.getStarList(folderName, new NetworkHandler<ArrayList<Star>>(this) {
            @Override
            public void onSuccess(ArrayList<Star> result) {
                starList.clear();
                result.forEach(item -> {
                    if (item.getSubject() != null && (subject == null || subject.equals(item.getSubject()))) {
                        starList.add(new StarItem(item, true));
                    }
                });
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

        RecyclerViewAdapter<StarItem> adapter = new RecyclerViewAdapter<StarItem>(getContext(), R.layout.item_star_entity, starList) {
            @Override
            public void convert(ViewHolder holder, StarItem data, int position, List<StarItem> allData) {
                ((TextView) holder.getViewById(R.id.star_entity_name)).setText(getString(R.string.dash_template, data.subject.toName(getContext()), data.name));
                ((TextView) holder.getViewById(R.id.star_entity_category)).setText(data.category);
                ImageView star = holder.getViewById(R.id.star_entity_star);
                star.setImageResource(data.isStar ? R.drawable.ic_star : R.drawable.ic_star_border);
                star.setOnClickListener(view -> {
                    List<String> folderStar = new ArrayList<>();
                    List<String> folderUnstar = new ArrayList<>();
                    String folderNameUpload = folderName == null ? "default" : folderName;
                    if (data.isStar) {
                        folderUnstar.add(folderNameUpload);
                    } else {
                        folderStar.add(folderNameUpload);
                    }
                    ApplicationNetwork.star(data.subject, data.uri, data.name, data.category, folderStar, folderUnstar, new NetworkHandler<Boolean>(view) {
                        @Override
                        public void onSuccess(Boolean result) {
                            data.isStar = true;
                            notifyItemChanged(position);
                        }

                        @Override
                        public void onError(Exception e) {
                            Snackbar.make(view, R.string.network_error, Snackbar.LENGTH_LONG).show();
                        }
                    });
                });
                holder.getViewById(R.id.star_entity_container).setOnClickListener(view -> {
                    Bundle bundle = new Bundle();
                    bundle.putInt("subject", 0);
                    bundle.putString("name", data.name);
                    bundle.putString("uri", data.uri);
                    bundle.putString("category", data.category);
                    NavHostFragment.findNavController(StarFragment.this).navigate(R.id.nav_detail, bundle);
                });
            }
        };

        binding.starReturnIcon.setOnClickListener(view -> NavHostFragment.findNavController(this).navigateUp());
        binding.starCategoryText.setOnClickListener(view -> ApplicationNetwork.getFolderList(new NetworkHandler<List<String>>(StarFragment.this) {
            @Override
            public void onSuccess(List<String> folderList) {
                PopupMenu popupMenu = new PopupMenu(getContext(), view);
                Menu menu = popupMenu.getMenu();
                folderList.forEach(menu::add);
                popupMenu.setOnMenuItemClickListener(menuItem -> {
                    binding.starCategoryText.setText(menuItem.getTitle());
                    folderName = menuItem.getTitle().toString();
                    if (folderName.equals(getString(R.string.default_folder))) folderName = null;
                    updateStarList(adapter);
                    binding.starDeleteFolderButton.setVisibility(folderName == null ? View.GONE : View.VISIBLE);
                    return true;
                });
                popupMenu.show();
            }

            @Override
            public void onError(Exception e) {
                Snackbar.make(view, R.string.network_error, Snackbar.LENGTH_SHORT).show();
            }
        }));
        if (folderName != null) binding.starCategoryText.setText(folderName);

        binding.starSubjectText.setOnClickListener(view -> {
            PopupMenu popupMenu = new PopupMenu(getContext(), view);
            popupMenu.getMenuInflater().inflate(R.menu.subject_menu_with_all, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(menuItem -> {
                Context context = getContext();
                if (context == null) return false;
                subject = Subject.fromName(context, menuItem.getTitle().toString());
                binding.starSubjectText.setText(menuItem.getTitle());
                updateStarList(adapter);
                return true;
            });
            popupMenu.show();
        });
        if (subject != null) binding.starSubjectText.setText(subject.toName(getContext()));

        binding.starListRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.starListRecyclerView.setAdapter(adapter);
        updateStarList(adapter);

        binding.starDeleteFolderButton.setOnClickListener(v -> ApplicationNetwork.removeFolder(folderName, new NetworkHandler<Boolean>(StarFragment.this) {
            @Override
            public void onSuccess(Boolean result) {
                if (result) {
                    folderName = null;
                    updateStarList(adapter);
                    binding.starDeleteFolderButton.setVisibility(View.GONE);
                } else {
                    Snackbar.make(v, R.string.network_error, Snackbar.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(Exception e) {
                Snackbar.make(v, R.string.network_error, Snackbar.LENGTH_SHORT).show();
            }
        }));

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private static class StarItem {
        Subject subject;
        String uri;
        String name;
        String category;
        boolean isStar;

        StarItem(Star star, boolean isStar) {
            this.subject = star.getSubject();
            this.uri = star.getUri();
            this.name = star.getName();
            this.category = star.getCategory();
            this.isStar = isStar;
        }
    }

}