import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.process.ByteProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;

public class Task_5_CannyEdgeDetection implements PlugInFilter {

    @Override
    public void run(ImageProcessor imageProcessor) {
        // Sobel kernels
        int[][] SobelX = {{1, 0, -1}, {2, 0, -2}, {1, 0, -1}};
        int[][] SobelY = {{1, 2, 1}, {0, 0, 0}, {-1, -2, -1}};

        // Parameter dialog
        ij.gui.GenericDialog gd = new ij.gui.GenericDialog("Canny Edge Parameters");
        gd.addNumericField("Gaussian sigma:", 2.0, 1);
        gd.addNumericField("Upper threshold :", 15, 0);
        gd.addNumericField("Lower threshold :", 5, 0);
        gd.showDialog();
        if (gd.wasCanceled()) return;
        double sigma = gd.getNextNumber();
        int upper = (int) gd.getNextNumber();
        int lower = (int) gd.getNextNumber();

        // Convert to float and blur
        FloatProcessor fp = (FloatProcessor) imageProcessor.convertToFloat();
        new ij.plugin.filter.GaussianBlur().blurFloat(fp, sigma, sigma, 0.01);
        int w = fp.getWidth(), h = fp.getHeight();


        //Add the class from task4
        Task_4_Filters filterKit = new Task_4_Filters();


        // Apply X and Y derivatives
        FloatProcessor derivX = filterKit.applyFilter(fp, SobelX);
        FloatProcessor derivY = filterKit.applyFilter(fp, SobelY);



        // Gradient magnitude
        FloatProcessor gradient = filterKit.getGradient(derivX, derivY);

        // Quantize directions
        ByteProcessor dir = getDir(derivX, derivY);

        // Non-maximum suppression
        FloatProcessor non_max_sup = nonMaxSuppress(gradient, dir);

        // Hysteresis thresholding
        ByteProcessor edges = hysteresisThreshold(non_max_sup, upper, lower);

        new ImagePlus("Canny Edges", edges).show();
    }

    public ByteProcessor getDir(ij.process.FloatProcessor fx, ij.process.FloatProcessor fy) {
        int w = fx.getWidth(), h = fx.getHeight();
        ByteProcessor dir = new ByteProcessor(w, h);
        int[] angles = {0, 45, 90, 135, 180};
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                double gradx = fx.getf(x, y);
                double grady = fy.getf(x, y);
                //cartesian to array direction change
                double theta = Math.toDegrees(Math.atan2(-grady, gradx));
                //Below 0 we go 180 further
                if (theta < 0) theta += 180;
                int best = 0;
                double minDiff = Double.MAX_VALUE;
                //find the nearest angle and set the value of that for our angle
                for (int angle : angles) {
                    double d = Math.abs(theta - angle);
                    if (d < minDiff) { minDiff = d; best = angle; }
                }
                // if it is 180 consider it 0
                if (best == 180) best = 0;
                dir.set(x, y, best);
            }
        }
        return dir;
    }

    public FloatProcessor nonMaxSuppress(ij.process.FloatProcessor grad, ByteProcessor dir) {
        int w = grad.getWidth(), h = grad.getHeight();
        //All values to zero including below threshold and we add non zero ones
        FloatProcessor out = new FloatProcessor(w, h);
        for (int y = 1; y < h - 1; y++) {
            for (int x = 1; x < w - 1; x++) {
                float pixelv = grad.getf(x, y);
                int angle = dir.get(x, y);
                float beforep = 0, afterp = 0;
                //setting value basd on specific image values
                if (angle == 0) {
                    beforep = grad.getf(x - 1, y);
                    afterp = grad.getf(x + 1, y);
                } else if (angle == 45) {
                    beforep = grad.getf(x - 1, y - 1);
                    afterp = grad.getf(x + 1, y + 1);
                } else if (angle == 90) {
                    beforep = grad.getf(x, y - 1);
                    afterp = grad.getf(x, y + 1);
                } else if (angle == 135) {
                    beforep = grad.getf(x + 1, y - 1);
                    afterp = grad.getf(x - 1, y + 1);
                }
   //if it is maximum in pendicular image direction
                if (pixelv >= beforep && pixelv >= afterp) {
                    out.setf(x, y, pixelv);
                } else {
                    out.setf(x, y, 0f);
                }
            }
        }

        return out;
    }

    public ByteProcessor hysteresisThreshold(FloatProcessor In, int upper, int lower) {
        int w = In.getWidth(), h = In.getHeight();
        ByteProcessor Out = new ByteProcessor(w, h);
        float max = (float) In.getMax();
        float tHigh = max * upper / 100f;
        float tLow = max * lower / 100f;
        // strong edges
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                if (In.getf(x, y) > tHigh) Out.set(x, y, 255);
            }
        }
        // weak edges
        boolean changed = true;
        while (changed) {
            changed = false;
            for (int x = 0; x < In.getWidth(); x++) {
                for (int y = 0; y < In.getHeight(); y++) {
                    if (In.getPixelValue(x, y) > tLow && hasNeighbours(Out, x, y) && Out.getPixel(x,y)==0) {
                        Out.set(x, y, 255);
                        changed = true;
                    }
                }
            }
        }

        return Out;
    }
    public boolean hasNeighbours(ByteProcessor BP, int x, int y ){
        int count = (BP.getPixel(x+1,y)+BP.getPixel(x-1,y)+BP.getPixel(x,y+1)+BP.getPixel(x,y-1)+BP.getPixel(x+1,y+1)+
                BP.getPixel(x-1,y-1)+BP.getPixel(x-1,y+1)+BP.getPixel(x+1,y-1));
        count/=255;
        return (count>0) ;
    }


    @Override
    public int setup(String s, ImagePlus imagePlus) {
        return DOES_8G;
    }
}
