package com.mindsoft.tflite;

import android.graphics.Bitmap;
import android.graphics.RectF;

import androidx.annotation.NonNull;

import java.util.List;

public interface SimilarityClassifier {
    void register(String name, Recognition recognition);

    void enableStatLogging(final boolean debug);

    void close();

    void setNumThreads(int num);

    void setUseNNAPI(boolean isChecked);

    List<Recognition> recognizeImage(Bitmap bitmap, boolean getExtra);

    String getStatString();

    class Recognition {
        private final String id;

        private String title;

        private Float distance;

        private Object extra;

        private RectF location;

        private Integer color;

        private Bitmap crop;

        public Recognition(final String id, final String title, final Float distance, final RectF location) {
            this.id = id;
            this.title = title;
            this.distance = distance;
            this.location = location;
            this.color = null;
            this.extra = null;
            this.crop = null;
        }

        public String getId() {
            return id;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public Float getDistance() {
            return distance;
        }

        public void setDistance(Float distance) {
            this.distance = distance;
        }

        public Object getExtra() {
            return extra;
        }

        public void setExtra(Object extra) {
            this.extra = extra;
        }

        public RectF getLocation() {
            return location;
        }

        public void setLocation(RectF location) {
            this.location = location;
        }

        public Integer getColor() {
            return color;
        }

        public void setColor(Integer color) {
            this.color = color;
        }

        public Bitmap getCrop() {
            return crop;
        }

        public void setCrop(Bitmap crop) {
            this.crop = crop;
        }

        @NonNull
        @Override
        public String toString() {
            String resultString = "";
            if (id != null) {
                resultString += "[" + id + "] ";
            }

            if (title != null) {
                resultString += title + " ";
            }

            if (distance != null) {
                resultString += String.format("(%.1f%%) ", distance * 100.0f);
            }

            if (location != null) {
                resultString += location + " ";
            }

            return resultString.trim();
        }
    }
}
