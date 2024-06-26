package com.nestnav.mobile.ui.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.nestnav.mobile.databinding.FragmentMainBinding;

public class PlaceholderFragment extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";

    private PageViewModel pageViewModel;
    private FragmentMainBinding binding;
    private NetworkViewModel networkViewModel;

    public static PlaceholderFragment newInstance(int index) {
        PlaceholderFragment fragment = new PlaceholderFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_SECTION_NUMBER, index);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pageViewModel = new ViewModelProvider(this).get(PageViewModel.class);
        networkViewModel = new ViewModelProvider(this).get(NetworkViewModel.class);
        int index = 1;
        if (getArguments() != null) {
            index = getArguments().getInt(ARG_SECTION_NUMBER);
        }
        pageViewModel.setIndex(index);
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        binding = FragmentMainBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.sectionLabel;
        pageViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });

        final EditText filterInput = binding.filterInput;
        final Button filterButton = binding.filterButton;
        final Button bookingButton = binding.bookingButton;
        final TextView responseTextView = binding.responseTextView;

        filterButton.setOnClickListener(v -> {
            String filterCriteria = filterInput.getText().toString();
            networkViewModel.sendFilterRequest(filterCriteria);
        });

        bookingButton.setOnClickListener(v -> {
            String bookingDetails = filterInput.getText().toString();
            networkViewModel.sendBookingRequest(bookingDetails);
        });

        networkViewModel.getResponseLiveData().observe(getViewLifecycleOwner(), response -> {
            if (response != null) {
                responseTextView.setText(response.toString());
            }
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
