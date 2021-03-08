package spam_detector;

/**
 *
 * @author Mahnoor Yousaf
 */
public class TestFileForTable {
    
    private String fileName;
    private String spamProbability;
    private String actualClass;
    
    public TestFileForTable(String filename, String spamProbability, String actualClass) {
        this.fileName = filename;
        this.spamProbability = spamProbability;
        this.actualClass = actualClass;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getSpamProbability() {
        return spamProbability;
    }

    public void setSpamProbability(String spamProbability) {
        this.spamProbability = spamProbability;
    }

    public String getActualClass() {
        return actualClass;
    }

    public void setActualClass(String actualClass) {
        this.actualClass = actualClass;
    }
    
    
    
}
