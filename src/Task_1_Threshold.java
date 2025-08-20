import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.filter.PlugInFilter;
import ij.process.Blitter;
import ij.process.ByteProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;

public class Task_1_Threshold implements PlugInFilter {
    @Override
    public int setup(String s, ImagePlus imagePlus) {
        return DOES_8G;
    }

    @Override
    public void run(ImageProcessor ip) {
        // Uncomment the following block once methods are implemented

        GenericDialog gd = new GenericDialog("Thresholding");
        gd.addNumericField("Threshold value:", 128, 0);
        gd.addCheckbox("Correct uneven illumination", false);
        gd.showDialog();

        // Check if the dialog was canceled
        if (gd.wasCanceled())
            return;

        // Get user choices
        int threshold = (int) gd.getNextNumber();
        boolean correct = gd.getNextBoolean();

        // Correct illumination if selected
        ImageProcessor ipCopy;
        if (correct) {
            ipCopy = correctIllumination(ip);
        } else {
            ipCopy = ip;
        }

        // Threshold the image
        ByteProcessor thresholdedIp = threshold(ipCopy, threshold);
        ImagePlus thresholdedImage = new ImagePlus("Thresholded Image", thresholdedIp);
        thresholdedImage.show();

    }


     // Performs binary thresholding on the given image.
     // Pixels above the threshold are set to white (255), others to black (0).


    public ByteProcessor threshold(ImageProcessor ip, int threshold) {
        int width = ip.getWidth();
        int height = ip.getHeight();
        ByteProcessor result = new ByteProcessor(width, height);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = ip.getPixel(x, y);
                // Set to white if above threshold, otherwise black
                if (pixel > threshold) {
                    result.set(x, y, 255);
                } else {
                    result.set(x, y, 0);
                }

            }
        }
        return result;
    }


     // Corrects uneven illumination by dividing the original image by a blurred version.
     // Uses a Gaussian blur with sigma = 75.

    public ByteProcessor correctIllumination(ImageProcessor ip) {
        // Convert input to FloatProcessor (does not modify original ip)
        FloatProcessor originalFloat = (FloatProcessor) ip.convertToFloatProcessor();

        // Create a blurred copy
        FloatProcessor blurred = (FloatProcessor) originalFloat.duplicate();
        blurred.blurGaussian(75);

        // Divide original by blurred image
        FloatProcessor corrected = (FloatProcessor) originalFloat.duplicate();
        corrected.copyBits(blurred, 0, 0, Blitter.DIVIDE);
        // Normalize result to 0-255 range
        corrected.resetMinAndMax();


        return corrected.convertToByteProcessor();
    }
}
