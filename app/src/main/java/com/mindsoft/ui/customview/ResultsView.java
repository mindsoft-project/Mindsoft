package com.mindsoft.ui.customview;

import com.mindsoft.tflite.SimilarityClassifier;

import java.util.List;

public interface ResultsView {
    void setResults(final List<SimilarityClassifier.Recognition> results);
}
