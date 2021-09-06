package com.java.sunxun.data;

import android.util.Pair;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.java.sunxun.models.Subject;

import java.util.ArrayList;

public class QaViewModel extends ViewModelWithSubject {
    private final MutableLiveData<ArrayList<Pair<Subject, String>>> qaList = new MutableLiveData<>(new ArrayList<>());

    private final MutableLiveData<QaFastLink> currentFastLink = new MutableLiveData<>();

    public LiveData<ArrayList<Pair<Subject, String>>> getQaList() {
        return qaList;
    }

    public LiveData<QaFastLink> getCurrentFastLink() {
        return currentFastLink;
    }

    // if subject is null, then the content is an answer
    public void pushToQaList(@Nullable Subject subject, String content) {
        ArrayList<Pair<Subject, String>> data = qaList.getValue();
        if (data != null) {
            data.add(new Pair<>(subject, content));
            qaList.setValue(data);
        }
    }

    public void pushToQaListIfEmpty(@Nullable Subject subject, String content) {
        ArrayList<Pair<Subject, String>> list = qaList.getValue();
        if (list != null && list.isEmpty()) {
            ArrayList<Pair<Subject, String>> data = qaList.getValue();
            if (data != null) {
                data.add(new Pair<>(subject, content));
                qaList.setValue(data);
            }
        }
    }

    public void setCurrentFastLink(Subject subject, String uri, String name, String category) {
        if (subject != null && uri != null && !uri.isEmpty() && name != null && !name.isEmpty() && category != null && !category.isEmpty())
            currentFastLink.setValue(new QaFastLink(subject, uri, name, category));
        else
            currentFastLink.setValue(null);
    }

    public static class QaFastLink {
        @NonNull
        public Subject subject;

        @NonNull
        public String uri;

        @NonNull
        public String name;

        @NonNull
        public String category;

        public QaFastLink(@NonNull Subject subject, @NonNull String uri, @NonNull String name, @NonNull String category) {
            this.subject = subject;
            this.uri = uri;
            this.name = name;
            this.category = category;
        }
    }

}
