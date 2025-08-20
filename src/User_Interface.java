import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

public class User_Interface implements PlugInFilter {

    Task_3_Otsu Otsu = new Task_3_Otsu();



    @Override
    public int setup(String s, ImagePlus imagePlus) {
        return DOES_8G;
    }

    @Override
    public void run(ImageProcessor imageProcessor) {

        Task_1_Threshold Threshold = new Task_1_Threshold();
        Task_2_EvaluateSegmentation Evaluate = new Task_2_EvaluateSegmentation();
        Task_3_Otsu Otsu = new Task_3_Otsu();
        Task_4_Filters Filters = new Task_4_Filters();
        Task_5_CannyEdgeDetection Canny = new Task_5_CannyEdgeDetection();


        GenericDialog GD = new GenericDialog("Selection panel");
        String[] firstChoice ={"Thresholding","Edge-Detection"};
        GD.addChoice("Choose an action: ", firstChoice,firstChoice[0]);
        GD.showDialog();

        if (GD.wasCanceled()){
            return;
        }

        int firstChoiceIdx = GD.getNextChoiceIndex();

        if (firstChoiceIdx ==0){

            String[] ThreshChoice ={"regular Thresholding","Otsu"};
            GD.addChoice("Choose Thresholding-technique:",ThreshChoice,ThreshChoice[0]);
            GD.addCheckbox("Evaluate Segmentation?",false);
            GD.showDialog();

            int temp = GD.getNextChoiceIndex();
            int ThreshChoiceIdx = GD.getNextChoiceIndex();
            boolean eval = GD.getNextBoolean();

            if (ThreshChoiceIdx == 0){
                Threshold.run(imageProcessor);
            }
            else{
                Otsu.run(imageProcessor);
            }

            if (eval){
                Evaluate.run(imageProcessor);
            }

        }

        else{
            String[] EDChoice ={"Primitive","Canny-Edge-Detection"};
            GD.addChoice("Choose edge-detection-technique:",EDChoice,EDChoice[0]);
            GD.showDialog();
            int temp = GD.getNextChoiceIndex();
            int EDChoiceIdx = GD.getNextChoiceIndex();

            if (EDChoiceIdx == 0){
                Filters.run(imageProcessor);
            }
            else{
                Canny.run(imageProcessor);
            }
        }
    }
}

