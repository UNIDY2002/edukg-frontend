package com.java.sunxun.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import com.java.sunxun.R;
import com.java.sunxun.databinding.FragmentStarBinding;

public class StarFragment extends Fragment {

    @Nullable
    FragmentStarBinding binding;

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

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}