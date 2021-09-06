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

    public void setCurrentFastLink(Subject subject, String uri, String name) {
        if (subject != null && uri != null && !uri.isEmpty() && name != null && !name.isEmpty())
            currentFastLink.setValue(new QaFastLink(subject, uri, name));
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

        public QaFastLink(@NonNull Subject subject, @NonNull String uri, @NonNull String name) {
            this.subject = subject;
            this.uri = uri;
            this.name = name;
        }
    }

}
