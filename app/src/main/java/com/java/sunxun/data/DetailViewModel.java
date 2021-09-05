package com.java.sunxun.data;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class DetailViewModel extends ViewModel {
    private final MutableLiveData<Boolean> starStatus = new MutableLiveData<>(null);

    public LiveData<Boolean> getStarStatus(){
        return starStatus;
    }

    public void setStarStatus(@NonNull Boolean starStatus){
        this.starStatus.setValue(starStatus);
    }
}
