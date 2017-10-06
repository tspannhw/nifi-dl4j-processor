package com.dataflowdeveloper.processors.process;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by tomhanlon on 7/16/17.
 */
class ImageNetResults {
	// {'label' : 'armadillo', 'prediction': 0.2536}
	private String label;
	private float prediction;
	private Map<Float, String> predictions = new TreeMap<Float, String>(Collections.reverseOrder());

	// constructor to set values
	ImageNetResults(String labelin, Float predictionin) {
		label = labelin;
		prediction = predictionin;
		predictions.put(predictionin, labelin);
	}

	public String getLabel() {
		return label;
	}

	public Float getPrediction() {

		return prediction;

	}

	public ImageNetResults results() {
		return this;
	}

	// private Map<Float, String> predictions = new TreeMap<Float,
	// String>(Collections.reverseOrder());
}