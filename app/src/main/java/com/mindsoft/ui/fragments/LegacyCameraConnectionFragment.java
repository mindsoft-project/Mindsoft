package com.mindsoft.ui.fragments;

/*
 * Copyright 2019 The TensorFlow Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.annotation.SuppressLint;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.os.Bundle;
import android.os.HandlerThread;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.mindsoft.databinding.FragmentCameraConnectionBinding;
import com.mindsoft.env.ImageUtils;
import com.mindsoft.ui.customview.AutoFitTextureView;

import java.io.IOException;
import java.util.List;

@SuppressLint("ValidFragment")
public class LegacyCameraConnectionFragment extends Fragment {
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    private static final String KEY_FACING = "camera_facing";

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    private Camera camera;
    private Camera.PreviewCallback imageListener;
    private Size desiredSize;
    private int facing;
    private int layout;
    private FragmentCameraConnectionBinding binding;

    private AutoFitTextureView textureView;
    private final TextureView.SurfaceTextureListener surfaceTextureListener =
            new TextureView.SurfaceTextureListener() {
                @Override
                public void onSurfaceTextureAvailable(
                        final SurfaceTexture texture, final int width, final int height) {

                    int index = getCameraId();
                    camera = Camera.open(index);

                    try {
                        Camera.Parameters parameters = camera.getParameters();
                        List<String> focusModes = parameters.getSupportedFocusModes();
                        if (focusModes != null
                                && focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                        }
                        List<Camera.Size> cameraSizes = parameters.getSupportedPreviewSizes();
                        Size[] sizes = new Size[cameraSizes.size()];
                        int i = 0;
                        for (Camera.Size size : cameraSizes) {
                            sizes[i++] = new Size(size.width, size.height);
                        }
                        Size previewSize =
                                CameraConnectionFragment.chooseOptimalSize(
                                        sizes, desiredSize.getWidth(), desiredSize.getHeight());
                        parameters.setPreviewSize(previewSize.getWidth(), previewSize.getHeight());
                        camera.setDisplayOrientation(90);
                        camera.setParameters(parameters);
                        camera.setPreviewTexture(texture);
                    } catch (IOException exception) {
                        camera.release();
                    }

                    camera.setPreviewCallbackWithBuffer(imageListener);
                    Camera.Size s = camera.getParameters().getPreviewSize();
                    camera.addCallbackBuffer(new byte[ImageUtils.getYUVByteSize(s.height, s.width)]);

                    textureView.setAspectRatio(s.height, s.width);

                    camera.startPreview();
                }

                @Override
                public void onSurfaceTextureSizeChanged(
                        final SurfaceTexture texture, final int width, final int height) {
                }

                @Override
                public boolean onSurfaceTextureDestroyed(final SurfaceTexture texture) {
                    return true;
                }

                @Override
                public void onSurfaceTextureUpdated(final SurfaceTexture texture) {
                }
            };
    /**
     * An additional thread for running tasks that shouldn't block the UI.
     */
    private HandlerThread backgroundThread;

    public LegacyCameraConnectionFragment(
            final Camera.PreviewCallback imageListener, final int layout, final Size desiredSize, int facing) {
        this.imageListener = imageListener;
        this.layout = layout;
        this.desiredSize = desiredSize;
        this.facing = facing;

    }

    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);

        this.facing = args.getInt(KEY_FACING, CameraInfo.CAMERA_FACING_FRONT);

    }

    @Override
    public View onCreateView(
            final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        binding = FragmentCameraConnectionBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(final View view, final Bundle savedInstanceState) {
        textureView = binding.texture;
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        startBackgroundThread();

        if (textureView.isAvailable()) {
            if (camera != null) {
                camera.startPreview();
            }
        } else {
            textureView.setSurfaceTextureListener(surfaceTextureListener);
        }
    }

    @Override
    public void onPause() {
        stopCamera();
        stopBackgroundThread();
        super.onPause();
    }

    private void startBackgroundThread() {
        backgroundThread = new HandlerThread("CameraBackground");
        backgroundThread.start();
    }

    private void stopBackgroundThread() {
        backgroundThread.quitSafely();
        try {
            backgroundThread.join();
            backgroundThread = null;
        } catch (final InterruptedException ignored) {
        }
    }

    protected void stopCamera() {
        if (camera != null) {
            camera.stopPreview();
            camera.setPreviewCallback(null);
            camera.release();
            camera = null;
        }
    }

    private int getCameraId() {
        CameraInfo ci = new CameraInfo();
        for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
            Camera.getCameraInfo(i, ci);
            if (ci.facing == this.facing) return i;
        }
        return -1;
    }
}
