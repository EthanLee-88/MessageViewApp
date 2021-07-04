package com.example.messageviewapp.ui.home;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.messageviewapp.R;
import com.example.messageviewapp.databinding.FragmentHomeBinding;
import com.example.messageviewapp.view.MessageView;

import org.jetbrains.annotations.NotNull;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    private FragmentHomeBinding binding;

    private TextView textView;
    private ImageView launcherImage;
    private TextView bottomImage;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        textView = root.findViewById(R.id.text_dashboard);
        launcherImage = root.findViewById(R.id.launcher_image);
        bottomImage = root.findViewById(R.id.bottom_image);

        final TextView textView = binding.textHome;
        homeViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        return root;
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initRes();
    }

    private void initRes() {
        MessageView.bindView(textView);
        MessageView.bindView(launcherImage);
        MessageView.bindView(bottomImage);

        MessageView.setViewDismissListener((View view) -> {
            Log.d("tag", "ViewDismiss");
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        MessageView.release();
    }
}