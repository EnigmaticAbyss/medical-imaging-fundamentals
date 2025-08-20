//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import ij.process.FloatProcessor;
import ij.gui.GenericDialog;


public class Task_4_Filters implements PlugInFilter {

    protected int[][] SobelX = new int[][]{{1, 0, -1}, {2, 0, -2}, {1, 0, -1}};
    protected int[][] SobelY = new int[][]{{1, 2, 1}, {0, 0, 0}, {-1, -2, -1}};
    protected int[][] ScharrX = new int[][]{{47, 0, -47}, {162, 0, -162}, {47, 0, -47}};
    protected int[][] ScharrY = new int[][]{{47, 162, 47}, {0, 0, 0}, {-47, -162, -47}};
    protected int[][] PrewittX = new int[][]{{1, 0, -1}, {1, 0, -1}, {1, 0, -1}};
    protected int[][] PrewittY = new int[][]{{1, 1, 1}, {0, 0, 0}, {-1, -1, -1}};

    @Override
    public int setup(String s, ImagePlus imagePlus) {
        return DOES_8G;
    }

    @Override
    public void run(ImageProcessor ip) {
        // Convert to float for better precision
        FloatProcessor fp = ip.convertToFloatProcessor();

        // Build dialog
        String[] filters = {"Sobel", "Scharr", "Prewitt"};
        GenericDialog gd = new GenericDialog("Edge Detection");
        gd.addChoice("Filter:", filters, filters[0]);
        gd.showDialog();
        if (gd.wasCanceled()) return;

        int index = gd.getNextChoiceIndex();
        int[][] kx, ky;
        //Choose the Filter
        if (index ==0){
            kx = SobelX;  ky = SobelY;

        } else if (index==1) {

            kx = ScharrX;  ky = ScharrY;
        } else if (index == 2) {
            kx = PrewittX; ky = PrewittY;

        }
        else {
            return;
        }

        // Apply X and Y derivatives
        FloatProcessor derivX = applyFilter(fp, kx);
        FloatProcessor derivY = applyFilter(fp, ky);

        // Combine into gradient magnitude
        FloatProcessor gradient = getGradient(derivX, derivY);

        // Show result
        new ImagePlus(filters[index] + " Edges", gradient).show();
    }

    /**
     * Convolve `in` with a 3×3 integer kernel. This is infact  correlation
     * however since the kernels are fliped 180 already it does trick just like pytorch!
     * Ignores the outermost 1‐pixel border.
     */
    public FloatProcessor applyFilter(FloatProcessor in, int[][] kernel) {
        int w = in.getWidth(), h = in.getHeight();
        FloatProcessor out = new FloatProcessor(w, h);

        for (int y = 1; y < h - 1; y++) {
            for (int x = 1; x < w - 1; x++) {
                float sum = 0;
                // 3×3 neighborhood correlation
                for (int ky = -1; ky <= 1; ky++) {
                    for (int kx = -1; kx <= 1; kx++) {
                        float pixelValue = in.getf(x + kx, y + ky);
                        sum += kernel[ky + 1][kx + 1] * pixelValue;
                    }
                }
                out.setf(x, y, sum);
            }
        }
        return out;
    }

    /**
     * Compute gradient magnitude √(Ix² + Iy²) per pixel.
     * Both inputs must be the same size.
     */
    public FloatProcessor getGradient(FloatProcessor coinX, FloatProcessor coinY) {
        int w = coinX.getWidth(), h = coinX.getHeight();
        if (w != coinY.getWidth() || h != coinY.getHeight()) {
            throw new IllegalArgumentException("Both Input images must match in size");
        }

        FloatProcessor out = new FloatProcessor(w, h);
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                float gradx = coinX.getf(x, y);
                float grady = coinY.getf(x, y);
                out.setf(x, y, (float)Math.hypot(gradx, grady));
            }
        }
        return out;
    }
}