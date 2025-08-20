import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;

import java.awt.*;

public class Task_3_Otsu implements PlugInFilter {
// Number of channels we do every part on all of them to find the best one
    private static final int L = 256;
    // For dealing with devision by zero
    private static final double EPSILON = 1e-9;

    @Override
    public int setup(String s, ImagePlus imagePlus) {
        return DOES_8G;
    }

    @Override
    public void run(ImageProcessor ip) {
        // Perform Otsu segmentation and display result
        ByteProcessor result = otsuSegmentation(ip);
        new ImagePlus("Otsu Segmented", result).show();
    }

    // Compute normalized histogram of the image
    public double[] getHistogram(ImageProcessor in) {
        int width = in.getWidth();
        int height = in.getHeight();
        double[] histogram = new double[L];
        // Count pixels
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int bin_value = in.getPixel(x, y);
                histogram[bin_value]++;
            }
        }
        // Normalize to probability distribution
        double total = width * height;
        for (int i = 0; i < L; i++) {
            histogram[i] /= total;
        }
        return histogram;
    }
 // We do alghoritms all each value of bin

    // Cumulative background probability P1(theta) the left side of teta
    public double[] getP1(double[] histogram) {
        double[] P1 = new double[L];
        double sum = 0.0;
        for (int t = 0; t < L; t++) {
            sum += histogram[t];
            P1[t] = sum;
        }
        return P1;
    }

    // Foreground probability P2(theta) = 1 - P1 it is infact the rest of it on right side
    public double[] getP2(double[] P1) {
        double[] P2 = new double[L];
        for (int t = 0; t < L; t++) {
            P2[t] = 1.0 - P1[t];
        }
        return P2;
    }

    // Mean intensity of background up to threshold
    public double[] getMu1(double[] histogram, double[] P1) {
        double[] mu1 = new double[L];
        double culimationSum = 0.0;
        for (int t = 0; t < L; t++) {
            culimationSum += (t+1) * histogram[t];
            mu1[t] = culimationSum / (P1[t] + EPSILON);
        }
        return mu1;
    }

    // Mean intensity of foreground above threshold 1- total till then is used here as well
    public double[] getMu2(double[] histogram, double[] P2) {
        double[] mu2 = new double[L];
        double totalMean = 0.0;
        // Precompute overall mean
        for (int i = 0; i < L; i++) {
            totalMean += (i+1) * histogram[i];
        }
        double culimationSum = 0.0;

        for (int t = 0; t < L; t++) {
            culimationSum += (t+1) * histogram[t];

            mu2[t] = (totalMean - culimationSum) / (P2[t] + EPSILON);
        }
        return mu2;
    }

    // Between-class variance for each threshold
    public double[] getSigmas(double[] P1, double[] P2, double[] mu1, double[] mu2) {
        double[] sigmas = new double[L];
        for (int t = 0; t < L; t++) {
            double difference = mu1[t] - mu2[t];
            double product = P1[t] * P2[t];
            sigmas[t] = product * difference * difference;
        }
        return sigmas;
    }

    // Find threshold index with maximum sigma (last occurrence of max)
    public int getMaximum(double[] sigmas) {
        int index = 0;
        double max = sigmas[0];
        for (int t = 1; t < sigmas.length; t++) {
            if (sigmas[t] >= max) {
                max = sigmas[t];
                index = t;
            }
        }
        return index;
    }

    // Perform full Otsu segmentation with illumination correction
    public ByteProcessor otsuSegmentation(ImageProcessor ip) {
        //  Illumination correction using Task_1
        Task_1_Threshold thresholdkit = new Task_1_Threshold();
        ImageProcessor corrected = thresholdkit.correctIllumination(ip);

        //  Compute histogram and parameters
        double[] hist = getHistogram(corrected);
        double[] P1 = getP1(hist);
        double[] P2 = getP2(P1);
        double[] mu1 = getMu1(hist, P1);
        double[] mu2 = getMu2(hist, P2);
        double[] sigmas = getSigmas(P1, P2, mu1, mu2);

        // Determine Otsu threshold
        int otsuT = getMaximum(sigmas);
        System.out.println("Otsu threshold: " + otsuT);

        // Apply threshold to create binary segmented image
        ByteProcessor result = thresholdkit.threshold(corrected,otsuT);

        return result;
    }
}
