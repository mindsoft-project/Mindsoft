package com.mindsoft.ui.activity;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.google.common.util.concurrent.ListenableFuture;
import com.mindsoft.databinding.ActivityTestBinding;

import java.util.concurrent.ExecutionException;

public class TestActivity extends AppCompatActivity {

    private ActivityTestBinding binding;

    private Preview preview;
    private ImageCapture imageCapture;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTestBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        preview = new Preview.Builder().build();
        PreviewView previewView = binding.cameraPreview;

        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .build();
                imageCapture = new ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build();

                Camera camera = cameraProvider.bindToLifecycle(
                        ((LifecycleOwner) this),
                        cameraSelector,
                        preview,
                        imageCapture);


                preview.setSurfaceProvider(
                        previewView.getSurfaceProvider());


            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }

        }, ContextCompat.getMainExecutor(this));

    }
}
