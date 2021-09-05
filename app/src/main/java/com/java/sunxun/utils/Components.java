package com.java.sunxun.utils;

import android.content.Context;
import android.widget.PopupMenu;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import com.java.sunxun.R;
import com.java.sunxun.data.ViewModelWithSubject;
import com.java.sunxun.models.Subject;

public class Components {
    public static void bindSubjectSelectorToViewModel(Fragment fragment, ViewModelWithSubject viewModel, TextView subjectText) {
        Context context = fragment.getContext();
        if (context == null) return;
        subjectText.setOnClickListener(view -> {
            PopupMenu popupMenu = new PopupMenu(context, view);
            popupMenu.getMenuInflater().inflate(R.menu.subject_menu, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(menuItem -> {
                viewModel.setSubject(Subject.fromName(context, menuItem.getTitle().toString()));
                return true;
            });
            popupMenu.show();
        });

        viewModel.getSubject().observe(fragment.getViewLifecycleOwner(), subject -> subjectText.setText(subject.toName(context)));
    }
}
