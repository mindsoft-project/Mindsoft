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

package com.mindsoft.ui.activity;

import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.hardware.camera2.CameraCharacteristics;
import android.media.ImageReader.OnImageAvailableListener;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Size;
import android.util.TypedValue;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;
import com.mindsoft.R;
import com.mindsoft.data.model.FaceImage;
import com.mindsoft.data.model.Student;
import com.mindsoft.data.model.User;
import com.mindsoft.env.BorderedText;
import com.mindsoft.env.ImageUtils;
import com.mindsoft.env.MultiBoxTracker;
import com.mindsoft.tflite.SimilarityClassifier;
import com.mindsoft.tflite.TFLiteObjectDetectionAPIModel;
import com.mindsoft.ui.customview.OverlayView;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class DetectorActivity extends CameraActivity implements OnImageAvailableListener {
    private static final String TAG = "FACE RECOGNITION APP";

    public static final int MAX_FACES = 10;

    public static final int SUCCESS_RECOGNIZE = 2;
    public static final int FAIL_RECOGNIZE = 3;

    private static final int TF_OD_API_INPUT_SIZE = 112;
    private static final boolean TF_OD_API_IS_QUANTIZED = false;
    private static final String TF_OD_API_MODEL_FILE = "mobile_face_net.tflite";
    private static final String TF_OD_API_LABELS_FILE = "file:///android_asset/labelmap.txt";
    private static final boolean MAINTAIN_ASPECT = false;

    private static final Size DESIRED_PREVIEW_SIZE = new Size(640, 480);
    private static final boolean SAVE_PREVIEW_BITMAP = false;
    private static final float TEXT_SIZE_DIP = 10;
    private OverlayView trackingOverlay;
    private Integer sensorOrientation;

    private SimilarityClassifier detector;

    private Bitmap rgbFrameBitmap = null;
    private Bitmap croppedBitmap = null;
    private Bitmap portraitBmp = null;
    private Bitmap faceBmp = null;

    private boolean computingDetection = false;
    private long timestamp = 0;

    private Matrix frameToCropTransform;
    private Matrix cropToFrameTransform;

    private MultiBoxTracker tracker;
    private boolean nmo = false;
    private BorderedText borderedText;

    private static int TRIES = 5;

    private FaceDetector faceDetector;

    private List<SimilarityClassifier.Recognition> faces = new ArrayList<>();

    private boolean face_detected = false;

    private boolean isTraining = false;

    private boolean collectedFaces = false;

    public static onDetectCompleteListener onComplete;

    public interface onDetectCompleteListener {
        void onComplete(boolean success);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        isTraining = getIntent().getExtras().getBoolean("train_mode", false);
        binding.title.setText(getIntent().getExtras().getString("title", ""));

        if (isTraining) {
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.progressBar.setVisibility(View.GONE);
        }

        if (User.current != null) {
            binding.details.setVisibility(View.VISIBLE);
            binding.fullName.setText(User.current.getFullName());
            binding.department.setText(Student.current.getDepartment().getResourceId());
            binding.year.setText(Student.current.getYear() + " / " + (Student.current.getSemester() % 2 == 1 ? "1st" : "2nd"));
        }

        FaceDetectorOptions options =
                new FaceDetectorOptions.Builder()
                        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                        .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
                        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                        .build();


        faceDetector = FaceDetection.getClient(options);
    }

    @Override
    public void onPreviewSizeChosen(final Size size, final int rotation) {
        final float textSizePx =
                TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, TEXT_SIZE_DIP, getResources().getDisplayMetrics());
        borderedText = new BorderedText(textSizePx);
        borderedText.setTypeface(Typeface.MONOSPACE);

        tracker = new MultiBoxTracker(this);

        try {
            detector =
                    TFLiteObjectDetectionAPIModel.create(
                            getAssets(),
                            TF_OD_API_MODEL_FILE,
                            TF_OD_API_LABELS_FILE,
                            TF_OD_API_INPUT_SIZE,
                            TF_OD_API_IS_QUANTIZED);
            fetch();
        } catch (final IOException e) {
            e.printStackTrace();
            Toast toast =
                    Toast.makeText(
                            getApplicationContext(), "Classifier could not be initialized", Toast.LENGTH_SHORT);
            toast.show();
            finish();
        }

        previewWidth = size.getWidth();
        previewHeight = size.getHeight();

        sensorOrientation = rotation - getScreenOrientation();
        rgbFrameBitmap = Bitmap.createBitmap(previewWidth, previewHeight, Config.ARGB_8888);


        int targetW, targetH;
        if (sensorOrientation == 90 || sensorOrientation == 270) {
            targetH = previewWidth;
            targetW = previewHeight;
        } else {
            targetW = previewWidth;
            targetH = previewHeight;
        }
        int cropW = (int) (targetW / 2.0);
        int cropH = (int) (targetH / 2.0);

        croppedBitmap = Bitmap.createBitmap(cropW, cropH, Config.ARGB_8888);

        portraitBmp = Bitmap.createBitmap(targetW, targetH, Config.ARGB_8888);
        faceBmp = Bitmap.createBitmap(TF_OD_API_INPUT_SIZE, TF_OD_API_INPUT_SIZE, Config.ARGB_8888);

        frameToCropTransform =
                ImageUtils.getTransformationMatrix(
                        previewWidth, previewHeight,
                        cropW, cropH,
                        sensorOrientation, MAINTAIN_ASPECT);

        cropToFrameTransform = new Matrix();
        frameToCropTransform.invert(cropToFrameTransform);

        trackingOverlay = findViewById(R.id.tracking_overlay);
        trackingOverlay.addCallback(
                canvas -> {
                    tracker.draw(canvas);
                    if (isDebug()) {
                        tracker.drawDebug(canvas);
                    }
                });

        tracker.setFrameConfiguration(previewWidth, previewHeight, sensorOrientation);
    }


    @Override
    protected void processImage() {
        ++timestamp;
        final long currTimestamp = timestamp;
        trackingOverlay.postInvalidate();

        // No mutex needed as this method is not reentrant.
        if (computingDetection) {
            readyForNextImage();
            return;
        }
        computingDetection = true;

        System.out.println("Preparing image " + currTimestamp + " for detection in bg thread.");

        rgbFrameBitmap.setPixels(getRgbBytes(), 0, previewWidth, 0, 0, previewWidth, previewHeight);

        readyForNextImage();

        final Canvas canvas = new Canvas(croppedBitmap);
        canvas.drawBitmap(rgbFrameBitmap, frameToCropTransform, null);
        if (SAVE_PREVIEW_BITMAP) {
            ImageUtils.saveBitmap(croppedBitmap);
        }

        InputImage image = InputImage.fromBitmap(croppedBitmap, 0);
        faceDetector
                .process(image)
                .addOnSuccessListener(faces -> {
                    if (faces.size() == 0) {
                        updateResults(currTimestamp, new LinkedList<>());
                        return;
                    }

                    runInBackground(
                            () -> {
                                onFacesDetected(currTimestamp, faces);
                                face_detected = false;
                            });
                });

        this.runOnUiThread(() -> {
        });

    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_camera_connection;
    }

    @Override
    protected Size getDesiredPreviewFrameSize() {
        return DESIRED_PREVIEW_SIZE;
    }


    @Override
    protected void setUseNNAPI(final boolean isChecked) {
        runInBackground(() -> detector.setUseNNAPI(isChecked));
    }


    // Face Processing
    private Matrix createTransform(
            final int srcWidth,
            final int srcHeight,
            final int dstWidth,
            final int dstHeight,
            final int applyRotation) {

        Matrix matrix = new Matrix();
        if (applyRotation != 0) {
            if (applyRotation % 90 != 0) {
            }

            // Translate so center of image is at origin.
            matrix.postTranslate(-srcWidth / 2.0f, -srcHeight / 2.0f);

            // Rotate around origin.
            matrix.postRotate(applyRotation);
        }

        if (applyRotation != 0) {
            // Translate back from origin centered reference to destination frame.
            matrix.postTranslate(dstWidth / 2.0f, dstHeight / 2.0f);
        }

        return matrix;
    }

    private void sendTrainRequest(final List<SimilarityClassifier.Recognition> mappedRecognitions, OnCompleteListener<Void> listener) throws JSONException {
        tracker.trackResults(mappedRecognitions, 0);
        trackingOverlay.postInvalidate();
        computingDetection = false;


        if (mappedRecognitions.size() > 0) {
            FaceImage img = new FaceImage();
            Map<String, List<Float>> embeddings = new HashMap<>();
            img.setId(User.current.getId());
            int i = 0;
            for (SimilarityClassifier.Recognition face : mappedRecognitions) {
                float[] xd = ((float[][]) face.getExtra())[0];
                List<Float> embedding = new ArrayList<>();
                for (float x : xd) {
                    embedding.add(x);
                }
                embeddings.put("" + (i++), embedding);
            }
            img.setEmbeddings(embeddings);

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection(FaceImage.COLLECTION)
                    .document(User.current.getId())
                    .set(img)
                    .addOnCompleteListener(listener);
        }
    }

    private void updateResults(long currTimestamp, final List<SimilarityClassifier.Recognition> mappedRecognitions) {

        tracker.trackResults(mappedRecognitions, currTimestamp);
        trackingOverlay.postInvalidate();
        computingDetection = false;

        if (mappedRecognitions.size() > 0) {
            if (face_detected) {
            }
        }
    }

    private void fetch() {
        if (User.current != null) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            db.collection(FaceImage.COLLECTION).document(User.current.getId())
                    .get().addOnSuccessListener(command -> {
                        FaceImage img = command.toObject(FaceImage.class);
                        if (img == null) return;

                        for (Map.Entry<String, List<Float>> embedding : img.getEmbeddings().entrySet()) {
                            SimilarityClassifier.Recognition recognition = new SimilarityClassifier.Recognition(User.current.getId(), User.current.getFullName(), 0.0f, null);
                            int size = embedding.getValue().size();
                            float[][] extra = new float[1][size];
                            for (int i = 0; i < size; ++i) {
                                extra[0][i] = embedding.getValue().get(i);
                            }
                            recognition.setColor(Color.GREEN);
                            recognition.setExtra(extra);
                            detector.register(User.current.getFullName(), recognition);
                        }
                    });

        }
    }


    private void onFacesDetected(long currTimestamp, List<Face> faces) {
        final List<SimilarityClassifier.Recognition> mappedRecognitions = new LinkedList<>();
        face_detected = true;

        int sourceW = rgbFrameBitmap.getWidth();
        int sourceH = rgbFrameBitmap.getHeight();
        int targetW = portraitBmp.getWidth();
        int targetH = portraitBmp.getHeight();
        Matrix transform = createTransform(
                sourceW,
                sourceH,
                targetW,
                targetH,
                sensorOrientation);
        final Canvas cv = new Canvas(portraitBmp);

        cv.drawBitmap(rgbFrameBitmap, transform, null);
        final Canvas cvFace = new Canvas(faceBmp);

        for (Face face : faces) {
            final RectF boundingBox = new RectF(face.getBoundingBox());

            cropToFrameTransform.mapRect(boundingBox);
            RectF faceBB = new RectF(boundingBox);
            transform.mapRect(faceBB);

            float sx = ((float) TF_OD_API_INPUT_SIZE) / faceBB.width();
            float sy = ((float) TF_OD_API_INPUT_SIZE) / faceBB.height();
            Matrix matrix = new Matrix();
            matrix.postTranslate(-faceBB.left, -faceBB.top);
            matrix.postScale(sx, sy);

            cvFace.drawBitmap(portraitBmp, matrix, null);

            String label = "";
            float confidence = -1f;
            int color = Color.RED;
            Object extra = null;
            Bitmap crop = null;

            try {
                crop = Bitmap.createBitmap(portraitBmp,
                        (int) faceBB.left,
                        (int) faceBB.top,
                        (int) faceBB.width(),
                        (int) faceBB.height());
            } catch (Exception ignored) {
            }


            try {
                final long startTime = SystemClock.uptimeMillis();
                final List<SimilarityClassifier.Recognition> resultsAux = detector.recognizeImage(faceBmp, true);
                long lastProcessingTimeMs = SystemClock.uptimeMillis() - startTime;

                if (resultsAux.size() > 0) {
                    SimilarityClassifier.Recognition result = resultsAux.get(0);
                    extra = result.getExtra();
                    float conf = result.getDistance();
                    if (conf < 1.0f) {
                        confidence = conf;
                        label = result.getTitle();
                        if (result.getId().equals("0")) {
                            color = Color.GREEN;
                        }
                    }

                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            if (getCameraFacing() == CameraCharacteristics.LENS_FACING_FRONT) {
                Matrix flip = new Matrix();
                if (sensorOrientation == 90 || sensorOrientation == 270) {
                    flip.postScale(1, -1, previewWidth / 2.0f, previewHeight / 2.0f);
                } else {
                    flip.postScale(-1, 1, previewWidth / 2.0f, previewHeight / 2.0f);
                }
                flip.mapRect(boundingBox);
            }

            final SimilarityClassifier.Recognition result = new SimilarityClassifier.Recognition(
                    "0", label, confidence, boundingBox);

            if (confidence > 0.50f) {
                if (onComplete != null) {
                    onComplete.onComplete(true);
                }
                finish();
            } else {
                TRIES -= 1;
                if (TRIES == 0) {
                    if (onComplete != null) {
                        onComplete.onComplete(false);
                    }
                    finish();
                }

            }

            result.setColor(color);
            result.setLocation(boundingBox);
            result.setExtra(extra);
            result.setCrop(crop);


            if (this.isTraining && !this.collectedFaces) {
                this.faces.add(result);
                binding.progressBar.setProgress((int) Math.floor((this.faces.size() / (MAX_FACES + 0.0)) * 100.0), true);
            }
            mappedRecognitions.add(result);
        }

        if (!this.isTraining) {
            updateResults(currTimestamp, mappedRecognitions);
        } else {
            if (this.faces.size() >= MAX_FACES && !this.collectedFaces) {
                try {
                    sendTrainRequest(this.faces, task -> {
                        if (task.isSuccessful()) {
                            binding.status.setText("Successfully");
                            binding.status.setTextColor(getResources().getColor(R.color.success, getTheme()));
                            isTraining = false;
                            User.current.setFaceValidated(true);
                            User.current.getReference().set(User.current).addOnSuccessListener(command -> {
                                fetch();

                                Timer timer = new Timer();
                                timer.schedule(new TimerTask() {
                                    @Override
                                    public void run() {
                                        finish();
                                    }
                                }, 3000);

                            });
                        } else {
                            binding.status.setText("Failed");
                            binding.status.setTextColor(getResources().getColor(R.color.danger, getTheme()));
                        }
                    });
                    this.faces.clear();
                    this.collectedFaces = true;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                tracker.trackResults(mappedRecognitions, currTimestamp);
                trackingOverlay.postInvalidate();
                computingDetection = false;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {
            if (grantResults.length > 0) {
                boolean CameraAccessPermission = grantResults[0] == PackageManager.PERMISSION_GRANTED;

                if (CameraAccessPermission) {
                    Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }


}