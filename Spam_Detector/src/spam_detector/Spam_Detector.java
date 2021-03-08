
package spam_detector;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

public class Spam_Detector extends Application {

    File mainDirectory;

    TableView table;
    TextField accuracyTF;
    TextField precisionTF;

    static ObservableList<TestFileForTable> testFiles = FXCollections.observableArrayList();

    static HashMap<String, Integer> trainSpamFreq = new HashMap<String, Integer>();
    static HashMap<String, Integer> trainHamFreq = new HashMap<String, Integer>();

    static HashMap<String, Double> Pr_SW;

    int numTrueNegatives = 0;
    int numTruePositives = 0;
    int numFalsePositives = 0;

    int totalTestFiles = 0;

    @Override
    public void start(Stage primaryStage) {

        VBox vbox = getView();

        Scene scene = new Scene(vbox, 540, 500);
        primaryStage.setTitle("Spam Master");

        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setInitialDirectory(new File("."));
        mainDirectory = directoryChooser.showDialog(primaryStage);
        
        if (mainDirectory != null) {
            readData();
        }

        table.setItems(testFiles);

        Double accuracy;
        Double precision;
        
        accuracy = new Double(numTruePositives+numTrueNegatives)/new Double(totalTestFiles);
        precision = new Double(numTruePositives)/new Double(numTruePositives+numFalsePositives);
        
        
        DecimalFormat df = new DecimalFormat("0.00000");
        
        accuracyTF.setText(df.format(accuracy));
        precisionTF.setText(df.format(precision));

        primaryStage.setScene(scene);
        primaryStage.show();

    }

    private VBox getView() {

        table = new TableView();

        table.setEditable(true);

        TableColumn fileCol = new TableColumn("File");
        fileCol.setCellValueFactory(new PropertyValueFactory<>("fileName"));
        TableColumn classCol = new TableColumn("Actual Class");
        classCol.setCellValueFactory(new PropertyValueFactory<>("actualClass"));
        TableColumn probCol = new TableColumn("Spam Probability");
        probCol.setCellValueFactory(new PropertyValueFactory<>("spamProbability"));

        table.getColumns().addAll(fileCol, classCol, probCol);

        Label lbl1 = new Label("Accuracy: ");
        accuracyTF = new TextField();
        Label lbl2 = new Label("Precision: ");
        precisionTF = new TextField();

        HBox hbox1 = new HBox();
        hbox1.setSpacing(5);
        hbox1.setPadding(new Insets(10, 0, 0, 10));
        hbox1.getChildren().addAll(lbl1, accuracyTF);

        HBox hbox2 = new HBox();
        hbox2.setSpacing(5);
        hbox2.setPadding(new Insets(10, 0, 0, 10));
        hbox2.getChildren().addAll(lbl2, precisionTF);

        VBox vbox = new VBox();
        vbox.setSpacing(5);
        vbox.setPadding(new Insets(10, 0, 0, 10));
        vbox.getChildren().addAll(table, hbox1, hbox2);

        return vbox;

    }

    private void readData() {

        File trainingFolder = null;
        File testingFolder = null;

        for (final File fileEntry : mainDirectory.listFiles()) {
            if (fileEntry.isDirectory()) {

                if (fileEntry.getName().equals("train")) {
                    trainingFolder = fileEntry;
                } else if (fileEntry.getName().equals("test")) {
                    testingFolder = fileEntry;
                }
            }
        }
        if (trainingFolder != null) {
            System.out.println("Perform Training");
            performTraining(trainingFolder);

            if (testingFolder != null) {
                System.out.println("Perform Testing");
                performTesting(testingFolder);
                System.out.println("Completed");

            }

        }
    }

    private void performTraining(File directory) {

        HashMap<String, Double> Pr_WS;
        HashMap<String, Double> Pr_WH;

        int spamFiles = 0;
        int hamFiles = 0;

        for (final File fileEntry : directory.listFiles()) {
            if (fileEntry.isDirectory()) {

                if (fileEntry.getName().equals("ham")) {
                    System.out.println("Reading Ham Folder");
                    hamFiles = getData(fileEntry, trainHamFreq);
                } else if (fileEntry.getName().equals("spam")) {
                    System.out.println("Reading Spam Folder");
                    spamFiles = getData(fileEntry, trainSpamFreq);
                }
            }
        }

        //Finding Pr W/S
        Pr_WS = new HashMap();

        for (String i : trainSpamFreq.keySet()) {

            //System.out.println("key: " + i + " value: " + trainHamFreq.get(i));
            Double probability = Double.parseDouble(trainSpamFreq.get(i).toString()) / new Double(spamFiles);

            Pr_WS.put(i, probability);
        }

        //Finding Pr W/H
        Pr_WH = new HashMap();

        for (String i : trainHamFreq.keySet()) {

            Double probability = Double.parseDouble(trainHamFreq.get(i).toString()) / new Double(hamFiles);

            Pr_WH.put(i, probability);
        }

        //Finding Pr S/W
        Pr_SW = new HashMap();

        for (String word : trainSpamFreq.keySet()) {

            //System.out.println(word);
            double ws = 0.0;
            ws = Pr_WS.get(word);

            double wh = 0.0;
            try {
                wh = Pr_WH.get(word);
            } catch (Exception e) {
                //If word not found in ham
                wh = 0.0;
            }
            Double denominator = ws + wh;
            Double res = ws / denominator;
            //System.out.println("Result : "+res);
            Pr_SW.put(word, res);
        }

    }

    private int getData(File folder, HashMap<String, Integer> freq) {

        int noOfFiles = 0;

        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isFile()) {

                HashSet<String> words = new HashSet<>();

                noOfFiles++;
                //System.out.println("Reading : " + fileEntry.getName());
                try {
                    String data = new String(Files.readAllBytes(Paths.get(fileEntry.getPath())));

                    String allWords[] = data.split("\\d|\\s|,|=|\\.|-|_|\\$|/|\\\\|<|>|\\||@|!|#|%|\\^|&|\\*|\\(|\\)|\\{|\\}|\\[|\\]|;|:|\\?|\"|'");

                    //String allWords[] = data.split(" ");
                    for (String word : allWords) {
                        word = word.toLowerCase();
                        if (words.contains(word)) {
                        } else {
                            words.add(word);
                        }
                    }
                    for (String word : words) {
                        word = word.toLowerCase();
                        if (freq.containsKey(word)) {
                            int value = freq.get(word);
                            value++;
                            freq.replace(word, value);
                        } else {
                            freq.put(word, 1);
                        }
                    }

                    //totalWords.addAll(words);
                    words.clear();

                } catch (IOException ex) {
                    Logger.getLogger(Spam_Detector.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        }

        System.out.println("Total words: " + freq.size());

        return noOfFiles;

    }

    private void performTesting(File directory) {

        for (final File fileEntry : directory.listFiles()) {

            if (fileEntry.isDirectory()) {

                if (fileEntry.getName().equals("ham")) {
                    System.out.println("Testing Ham Folder");
                    testFiles(fileEntry);
                } else if (fileEntry.getName().equals("spam")) {
                    System.out.println("Testing Spam Folder");
                    testFiles(fileEntry);
                }
            }
        }

    }

    private void testFiles(File directory) {

        for (final File fileEntry : directory.listFiles()) {

            if (fileEntry.isFile()) {

                totalTestFiles++;

                File f = new File(fileEntry.getPath());

                HashSet<String> words = new HashSet<>();
                try {
                    String data = new String(Files.readAllBytes(Paths.get(f.getPath())));

                    String allWords[] = data.split("\\d|\\s|,|=|\\.|-|_|\\$|/|\\\\|<|>|\\||@|!|#|%|\\^|&|\\*|\\(|\\)|\\{|\\}|\\[|\\]|;|:|\\?|\"|'");
                    //String allWords[] = data.split(" ");

                    for (String word : allWords) {
                        word = word.toLowerCase();
                        if (words.contains(word)) {
                        } else {
                            words.add(word);
                        }
                    }
                } catch (IOException ex) {
                    Logger.getLogger(Spam_Detector.class.getName()).log(Level.SEVERE, null, ex);
                }

                Double lambda = 0.0;

                for (String word : words) {
                    word = word.toLowerCase();
                    //System.out.println("Finding word: "+ word);

                    Double probability;

                    try {
                        probability = Pr_SW.get(word);
                    } catch (Exception e) {
                        //If word not found
                        probability = 0.0;
                    }
                    if (probability != null) {
                        Double val1 = 1.0 - probability;
                        Double data1 = Math.log(val1);
                        Double data2 = Math.log(probability);
                        if (data1.isNaN() || data1.isInfinite()) {
                            data1 = 0.0;
                        }
                        if (data2.isNaN() || data2.isInfinite()) {
                            data2 = 0.0;
                        }
                        lambda = lambda + data1 - data2;
                    }

                }

                //Pr SF
                Double denominator = Math.pow(Math.E, lambda);
                denominator = 1.0 + denominator;
                Double result = 1.0 / denominator;

                TestFile tf = new TestFile(f.getName(), result, directory.getName());
                testFiles.add(new TestFileForTable(tf.getFileName(), tf.getSpamProbRounded(), tf.getActualClass()));

                if (tf.getActualClass().equals("ham")) {
                    if (tf.getSpamProbability() < 0.5) {
                        numTrueNegatives++;
                    }
                } else if (tf.getActualClass().equals("spam")) {
                    if (tf.getSpamProbability() > 0.5) {
                        numTruePositives++;
                    }else{
                        numFalsePositives++;
                    }
                }

            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

}
