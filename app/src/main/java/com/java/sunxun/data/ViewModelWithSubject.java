package com.java.sunxun.data;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.java.sunxun.models.Subject;

public abstract class ViewModelWithSubject extends ViewModel {
    private final MutableLiveData<Subject> subject = new MutableLiveData<>(Subject.chinese);

    public final LiveData<Subject> getSubject() {
        return subject;
    }

    public final void setSubject(Subject subject) {
        this.subject.setValue(subject);
    }

}
