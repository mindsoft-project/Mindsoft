package com.mindsoft.ui.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.ImageReader;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Size;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.mindsoft.R;
import com.mindsoft.databinding.ActivityCameraBinding;
import com.mindsoft.env.ImageUtils;
import com.mindsoft.ui.fragments.CameraConnectionFragment;
import com.mindsoft.ui.fragments.LegacyCameraConnectionFragment;

public abstract class CameraActivity
        extends AppCompatActivity
        implements ImageReader.OnImageAvailableListener,
        Camera.PreviewCallback,
        CompoundButton.OnCheckedChangeListener,
        View.OnClickListener {

    private static final String KEY_USING_FACE = "using_face";

    private static final String PERMISSION_CAMERA = Manifest.permission.CAMERA;

    private static final int PERMISSIONS_REQUEST = 1;

    private boolean useCamera2API;

    private Integer usingFace = null;

    private String cameraId = null;

    public ActivityCameraBinding binding;

    protected int previewWidth = 0;

    protected int previewHeight = 0;
    private boolean debug;
    private Runnable postInferenceCallback;
    private Runnable imageConverter;
    private int[] rgbBytes = null;
    private Handler handler;
    private HandlerThread handlerThread;

    protected Integer getCameraFacing() {
        return usingFace;
    }

    private boolean isProcessingFrame = false;
    private byte[][] yuvBytes = new byte[3][];
    private int yRowStride;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(null);

        Intent intent = getIntent();
        usingFace = intent.getIntExtra(KEY_USING_FACE, CameraCharacteristics.LENS_FACING_FRONT);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        binding = ActivityCameraBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        if (hasPermission()) {
            setFragment();
        } else {
            requestPermission();
        }

        binding.switchCamera.setOnClickListener(v -> {
            switchCamera();
        });
    }

    protected int[] getRgbBytes() {
        imageConverter.run();
        return rgbBytes;
    }

    @Override
    public synchronized void onStart() {
        super.onStart();
    }

    @Override
    public synchronized void onResume() {
        super.onResume();

        handlerThread = new HandlerThread("inference");
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());
    }

    @Override
    public synchronized void onPause() {

        handlerThread.quitSafely();
        try {
            handlerThread.join();
            handlerThread = null;
            handler = null;
        } catch (final InterruptedException e) {
        }

        super.onPause();
    }

    @Override
    public synchronized void onStop() {
        super.onStop();
    }

    @Override
    public synchronized void onDestroy() {
        super.onDestroy();
    }

    protected synchronized void runInBackground(final Runnable r) {
        if (handler != null) {
            handler.post(r);
        }
    }

    protected int getScreenOrientation() {
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        if (rotation == Surface.ROTATION_270) {
            return 270;
        } else if (rotation == Surface.ROTATION_180) {
            return 180;
        } else if (rotation == Surface.ROTATION_90) {
            return 90;
        }
        return 0;
    }

    public boolean isDebug() {
        return debug;
    }


    private void requestPermission() {
        if (shouldShowRequestPermissionRationale(PERMISSION_CAMERA)) {
            Toast.makeText(
                            CameraActivity.this,
                            "Camera permission is required for this demo",
                            Toast.LENGTH_LONG)
                    .show();
        }
        requestPermissions(new String[]{PERMISSION_CAMERA}, PERMISSIONS_REQUEST);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
    }

    public void switchCamera() {

        Intent intent = getIntent();

        if (usingFace == CameraCharacteristics.LENS_FACING_FRONT) {
            usingFace = CameraCharacteristics.LENS_FACING_BACK;
        } else {
            usingFace = CameraCharacteristics.LENS_FACING_FRONT;
        }

        intent.putExtra(KEY_USING_FACE, usingFace);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);

        restartWith(intent);

    }

    private void restartWith(Intent intent) {
        finish();
        overridePendingTransition(0, 0);
        startActivity(intent);
        overridePendingTransition(0, 0);
    }

    private String chooseCamera() {

        final CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

        try {


            for (final String cameraId : manager.getCameraIdList()) {
                final CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);


                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    System.out.println("CHARED1 " + characteristics.getKeysNeedingPermission());
                }
                for (CameraCharacteristics.Key<?> key : characteristics.getKeys()) {
                    System.out.println("FFAR " + key.getName() + " = " + characteristics.get(key));
                }
                final StreamConfigurationMap map =
                        characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

                if (map == null) {
                    continue;
                }

                final Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);

                if (usingFace != null &&
                        facing != null &&
                        !facing.equals(usingFace)
                ) {
                    continue;
                }


                useCamera2API = (facing == CameraCharacteristics.LENS_FACING_EXTERNAL)
                        || isHardwareLevelSupported(
                        characteristics, CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_FULL);

                Toast.makeText(this, "ENA " + useCamera2API, Toast.LENGTH_SHORT).show();
                return cameraId;
            }
        } catch (CameraAccessException ignored) {
        }

        return null;
    }

    private boolean isHardwareLevelSupported(
            CameraCharacteristics characteristics, int requiredLevel) {
        int deviceLevel = characteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL);
        Toast.makeText(this, "T " + deviceLevel + " , " + requiredLevel, Toast.LENGTH_SHORT).show();
        if (deviceLevel == CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY) {
            return requiredLevel == deviceLevel;
        }
        return requiredLevel <= deviceLevel;
    }

    private void setFragment() {
        this.cameraId = chooseCamera();

        Fragment fragment;
//        if (useCamera2API) {
//            CameraConnectionFragment camera2Fragment =
//                    CameraConnectionFragment.newInstance(
//                            (CameraConnectionFragment.ConnectionCallback) (size, rotation) -> {
//                                previewHeight = size.getHeight();
//                                previewWidth = size.getWidth();
//                                CameraActivity.this.onPreviewSizeChosen(size, rotation);
//                            },
//                            this,
//                            getLayoutId(),
//                            getDesiredPreviewFrameSize());
//
//            camera2Fragment.setCamera(cameraId);
//            fragment = camera2Fragment;
//
//        }
        {
            System.out.println("FAFAF");
            int facing = (usingFace == CameraCharacteristics.LENS_FACING_BACK) ?
                    Camera.CameraInfo.CAMERA_FACING_BACK :
                    Camera.CameraInfo.CAMERA_FACING_FRONT;
            LegacyCameraConnectionFragment frag = new LegacyCameraConnectionFragment(this,
                    getLayoutId(),
                    getDesiredPreviewFrameSize(), facing);
            fragment = frag;

        }

        getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment).commit();
    }

    private boolean hasPermission() {
        return checkSelfPermission(PERMISSION_CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    protected abstract int getLayoutId();

    protected abstract Size getDesiredPreviewFrameSize();

    protected abstract void onPreviewSizeChosen(final Size size, final int rotation);

    protected abstract void setUseNNAPI(boolean isChecked);

    protected abstract void processImage();

    protected void readyForNextImage() {
        if (postInferenceCallback != null) {
            postInferenceCallback.run();
        }
    }

    @Override
    public void onPreviewFrame(final byte[] bytes, final Camera camera) {
        if (isProcessingFrame) {
            return;
        }

        try {
            // Initialize the storage bitmaps once when the resolution is known.
            if (rgbBytes == null) {
                Camera.Size previewSize = camera.getParameters().getPreviewSize();
                previewHeight = previewSize.height;
                previewWidth = previewSize.width;
                rgbBytes = new int[previewWidth * previewHeight];
                int rotation = 90;
                if (usingFace == CameraCharacteristics.LENS_FACING_FRONT) {
                    rotation = 270;
                }
                onPreviewSizeChosen(new Size(previewSize.width, previewSize.height), rotation);
            }
        } catch (final Exception e) {
            System.out.println(e.getMessage());
            return;
        }

        isProcessingFrame = true;
        yuvBytes[0] = bytes;
        yRowStride = previewWidth;

        imageConverter =
                () -> ImageUtils.convertYUV420SPToARGB8888(bytes, previewWidth, previewHeight, rgbBytes);

        postInferenceCallback =
                () -> {
                    camera.addCallbackBuffer(bytes);
                    isProcessingFrame = false;
                };
        processImage();
    }


    @Override
    public void onImageAvailable(ImageReader reader) {

    }


    @Override
    public void onClick(View v) {

    }
}
