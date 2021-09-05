package com.java.sunxun.data;

import android.text.SpannableString;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class LinkingViewModel extends ViewModelWithSubject {
    private final MutableLiveData<Mode> mode = new MutableLiveData<>(Mode.EDIT);

    private final MutableLiveData<SpannableString> result = new MutableLiveData<>();

    public LiveData<Mode> getMode() {
        return mode;
    }

    public LiveData<SpannableString> getResult() {
        return result;
    }

    public void setMode(Mode mode) {
        this.mode.setValue(mode);
    }

    public void setResult(SpannableString result) {
        this.result.setValue(result);
    }

    public enum Mode {
        EDIT, RUNNING, DONE
    }
}
