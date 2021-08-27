package com.java.sunxun.data;

import android.util.Pair;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.java.sunxun.models.Subject;

import java.util.ArrayList;

public class QaViewModel extends ViewModel {
    private final MutableLiveData<ArrayList<Pair<Subject, String>>> qaList = new MutableLiveData<>(new ArrayList<>());

    public LiveData<ArrayList<Pair<Subject, String>>> getQaList() {
        return qaList;
    }

    // if subject is null, then the content is an answer
    public void pushToQaList(@Nullable Subject subject, String content) {
        ArrayList<Pair<Subject, String>> data = qaList.getValue();
        if (data != null) {
            data.add(new Pair<>(subject, content));
            qaList.setValue(data);
        }
    }

}
