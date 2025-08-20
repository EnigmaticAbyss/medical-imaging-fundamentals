public class EvaluationResult {
    private double specificity;
    private double sensitivity;

    public EvaluationResult(double specificity, double sensitivity) {
        this.specificity = specificity;
        this.sensitivity = sensitivity;
    }

    public double getSpecificity() {
        return specificity;
    }

    public double getSensitivity() {
        return sensitivity;
    }
}

