package com.nestnav.mobile.ui.main;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.nestnav.mobile.concurrency.ThreadPool;

import com.nestnav.mobile.models.SearchRequest;
import com.nestnav.mobile.service.network.NetworkService;

public class NetworkViewModel extends ViewModel {
    private final MutableLiveData<Object> responseLiveData = new MutableLiveData<>();
    private final NetworkService networkService;

    public NetworkViewModel() {
        ThreadPool threadPool = new ThreadPool(4, 10); // Adjust as necessary

        //10.0.2.2 IS THE LOOPBACK OF THE PHONE IN ESSENCE TO CONNECT TO THE LOCALHOST OF THE HOST COMPUTER
        this.networkService = new NetworkService(threadPool, "10.0.2.2", 8000, 10);

    }

    public void sendFilterRequest(String filterCriteria) {
        networkService.sendRequest(new SearchRequest(null,null,null,0,0,0), new NetworkService.ResponseHandler() {
            @Override
            public void handleResponse(Object response) {
                responseLiveData.postValue(response);
            }

            @Override
            public void handleError(Exception exception) {
                responseLiveData.postValue(exception.getMessage());
            }
        });
    }

    public void sendBookingRequest(String bookingDetails) {
        networkService.sendRequest(new BookingRequest(bookingDetails), new NetworkService.ResponseHandler() {
            @Override
            public void handleResponse(Object response) {
                responseLiveData.postValue(response);
            }

            @Override
            public void handleError(Exception exception) {
                responseLiveData.postValue(exception.getMessage());
            }
        });
    }

    public LiveData<Object> getResponseLiveData() {
        return responseLiveData;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        networkService.shutdown();
    }
}
