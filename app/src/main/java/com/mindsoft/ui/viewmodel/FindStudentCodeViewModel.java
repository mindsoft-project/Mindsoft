package com.mindsoft.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.mindsoft.data.repoistory.SystemRepository;

public class FindStudentCodeViewModel extends ViewModel {
    private static final SystemRepository system = new SystemRepository();

    public LiveData<String> getStudentCode(String nationalID) {
        return system.getStudentCode(nationalID);
    }
}
