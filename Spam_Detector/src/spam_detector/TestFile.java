package spam_detector;

import java.text.DecimalFormat;

public class TestFile {

    private String fileName;
    private double spamProbability;
    private String actualClass;

    public TestFile(String filename, double spamProbability, String actualClass) {
        this.fileName = filename;
        this.spamProbability = spamProbability;
        this.actualClass = actualClass;
    }

    public String getFileName() {
        return this.fileName;
    }

    public double getSpamProbability() {
        return this.spamProbability;
    }

    public String getSpamProbRounded() {
        DecimalFormat df = new DecimalFormat("0.00000");
        return df.format(this.spamProbability);
    }

    public String getActualClass() {
        return this.actualClass;
    }

    public void setFileName(String value) {
        this.fileName = value;
    }

    public void setSpamProbability(double val) {
        this.spamProbability = val;
    }

    public void setActualClass(String value) {
        this.actualClass = value;
    }
    
}