import ij.IJ;
import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

public class Task_2_EvaluateSegmentation implements PlugInFilter {
    @Override
    public int setup(String s, ImagePlus imagePlus) {
        return DOES_8G;
    }

    @Override
    public void run(ImageProcessor imageProcessor) {
        // Use the current image as the segmented result
        ImageProcessor segmentation = imageProcessor;

        // Open a dialog to select the reference image
        ImagePlus refImage = IJ.openImage();
        if (refImage == null) {
            throw new RuntimeException("Reference image could not be loaded");
        }
        ImageProcessor reference = refImage.getProcessor();

        // Evaluate segmentation
        EvaluationResult result = evaluateSegmentation(segmentation, reference);
        if (result == null) {
            IJ.error("EvaluateSegmentation","Image dimensions do not match between segmentation and reference");
            return;
        }

        // Print results
        IJ.log("Sensitivity: " + result.getSensitivity());
        IJ.log("Specificity: " + result.getSpecificity());
    }

    private EvaluationResult evaluateSegmentation(ImageProcessor segmentation, ImageProcessor reference) {
        int width = segmentation.getWidth();
        int height = segmentation.getHeight();
        // checking that test and reference image have the same size
        if (width != reference.getWidth() || height != reference.getHeight()) {
            return null;
        }

        long TP = 0, TN = 0, FP = 0, FN = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                boolean segPixel = segmentation.getPixel(x, y) != 0;
                boolean refPixel = reference.getPixel(x, y) != 0;
                //same as reference or foreground
                if (segPixel && refPixel) {
                    TP++;
                    //Both wrong or background
                } else if (!segPixel && !refPixel) {
                    TN++;
                    // wrong reference
                } else if (segPixel && !refPixel) {
                    FP++;
                    //wrong classified
                } else { // !segFore && refFore
                    FN++;
                }
            }
        }

// Calculate sensitivity (true positive rate), considering the case of division by zero and handle it
        double sensitivity;
        long totalActPositives = TP + FN;
        if (totalActPositives > 0) {
            sensitivity = (double) TP / totalActPositives;
        } else {
            sensitivity = 0.0;
        }

// Calculate specificity (true negative rate), considering the case of division by zero and handle it
        double specificity;
        long totalActNegatives = TN + FP;
        if (totalActNegatives > 0) {
            specificity = (double) TN / totalActNegatives;
        } else {
            specificity = 0.0;
        }

        return new EvaluationResult(specificity, sensitivity);
    }
}
