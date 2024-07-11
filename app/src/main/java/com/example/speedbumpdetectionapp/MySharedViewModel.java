package com.example.speedbumpdetectionapp;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.maps.model.LatLng;
public class MySharedViewModel extends ViewModel {
    private MutableLiveData<LatLng> latLngLiveData;

    public MySharedViewModel() {
        latLngLiveData = new MutableLiveData<>(null);
    }
    public void addMarkerFromFragment_addSpeedBump(LatLng latLng) {
        latLngLiveData.setValue(latLng);
    }
    public LiveData<LatLng> getLatLngLiveData() {
        return latLngLiveData;
    }
}

//---------------------------------------------- end -----------------------------------------------