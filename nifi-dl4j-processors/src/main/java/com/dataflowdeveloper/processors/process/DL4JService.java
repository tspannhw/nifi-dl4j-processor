package com.dataflowdeveloper.processors.process;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

//see:   https://github.com/skymindio/Demos-for-sales-marketting/blob/master/training-demos/src/main/java/ai/skymind/training/InceptionWebApp.java

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;

import org.datavec.image.loader.NativeImageLoader;
import org.deeplearning4j.nn.graph.ComputationGraph;
import org.deeplearning4j.nn.modelimport.keras.trainedmodels.Utils.ImageNetLabels;
import org.deeplearning4j.parallelism.ParallelInference;
import org.deeplearning4j.parallelism.inference.InferenceMode;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.shade.jackson.core.JsonProcessingException;
import org.nd4j.shade.jackson.databind.ObjectMapper;

/**
 * copied from DL4J example
 * 
 * @author tspann
 *
 */
public class DL4JService {

	private static final ObjectMapper mapper = new ObjectMapper();
	private static final int imgWidth = 299;
	private static final int imgHeight = 299;
	private static final int imgChannels = 3;
	// private static final int numClasses = 1000;
	private static final NativeImageLoader imageLoader = new NativeImageLoader(imgHeight, imgWidth, imgChannels);

	// to do vgg16
	// https://github.com/tspannhw/Demos-for-sales-marketting/blob/master/training-demos/src/main/java/ai/skymind/training/VGG16SparkJavaWebApp.java

	/***
	 * byte[] imageBytes = readAllBytesOrExit(Paths.get(imageFile));
	 * 
	 * @param imageFile
	 * @param modelDir
	 * @return String
	 */
	public String getInception(String imageFile, String modelDir) {

		File locationToSave = new File(modelDir + "/trained_inception_model.zip");
		ComputationGraph model = null;
		try {
			model = ModelSerializer.restoreComputationGraph(locationToSave);
		} catch (IOException e) {
			e.printStackTrace();
		}

		ParallelInference modelWrapper = new ParallelInference.Builder(model).inferenceMode(InferenceMode.BATCHED)
				.batchLimit(5).workers(3).build();

		long pipelineTime = System.currentTimeMillis();

		INDArray image = null;
		try (InputStream input = new FileInputStream(Paths.get(imageFile).toFile())) {
			image = imageLoader.asMatrix(input).divi(255.0).subi(0.5).muli(2);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		long ffTime = System.currentTimeMillis();

		INDArray[] output = modelWrapper.output(new INDArray[] { image });

		ffTime = System.currentTimeMillis() - ffTime;

		// sort to get top 5
		INDArray[] sorted = Nd4j.sortWithIndices(output[0], 1, false);

		// VGGResults class just builds an array of results in nice format
		ImageNetResults[] vggResultsArray = new ImageNetResults[5];

		// finish benchmark
		pipelineTime = System.currentTimeMillis() - pipelineTime;

		// Get top 5
		for (int i = 0; i < 5; i++) {
			// Get prediction percent
			float prediction = sorted[1].getFloat(i) * 100;

			// extract label for prediction
			String Label = ImageNetLabels.getLabel(sorted[0].getInt(i));

			// put both in Result array
			vggResultsArray[i] = new ImageNetResults(Label, prediction);
		}

		String predictions = "";
		try {
			predictions = mapper.writeValueAsString(vggResultsArray);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		// String predictions = mapper.writeValueAsString(map);
		// output json to screen
		return "{" + "\"data\":" + predictions + ", \"performance\":{ \"feedforward\":" + ffTime + ",\"total\":"
				+ pipelineTime + "}" + ", \"network\":{ \"parameters\":" + model.numParams() + ",\"layers\":"
				+ model.getNumLayers() + "}}";
	}
}
