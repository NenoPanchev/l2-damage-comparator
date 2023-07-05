package org.example;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Size;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;

import java.io.IOException;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.example.Utils.*;

public class Main {
    private static final String TESSERACT_DIR_PATH = "C:\\Program Files\\Tesseract-OCR\\tessdata";
    private static final String CRITICAL_HIT = "Critical hit!";
    private static final String MISS = "miss";
    private static final String DAMAGE_PREFIX = "You hit for ";
    private static final String DAMAGE_POSTFIX = " damage.";
    private static final String NON_NUMERIC = "\\D";
    private static final int MINUTES_TO_RUN = 10;
    private static final long NANOSEC_PER_SEC = 1000l*1000*1000;
    static Tesseract tesseract = new Tesseract();
    static List<Hit> hits = new ArrayList<>();
    public static void main(String[] args) throws AWTException, TesseractException, IOException {
        nu.pattern.OpenCV.loadLocally();
        tesseract.setDatapath(TESSERACT_DIR_PATH);
//        ExecutorService executorService = Executors.newFixedThreadPool(4);

        Robot robot = new Robot();

        // Define the top-left and bottom-right coordinates of the desired region
        int startX = 22;
        int startY = 1072;
        int width = 120;
        int height = 102;
        Rectangle screenRect = new Rectangle(startX, startY, width, height);

        Mat previous = null;

        long startTime = System.nanoTime();
        while ((System.nanoTime()-startTime)< MINUTES_TO_RUN*60*NANOSEC_PER_SEC){

            BufferedImage screenCapture = robot.createScreenCapture(screenRect);
            LocalTime now = LocalTime.now();
            // Convert BufferedImage to compatible type
            BufferedImage convertedImage = convertToCompatibleType(screenCapture);

            // Convert BufferedImage to Mat
            Mat mat = bufferedImageToMat(convertedImage);
            Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGR2GRAY);
            if (previous == null) {
                previous = mat;
            }
            if (!isSame(previous, mat)) {
                previous = mat;
//                executorService.submit(processImage(previous, now));
                processImage(previous, now);
            }


//            resizeImage(mat, 2.0);
//            showMat(mat);

            try {
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        showFinalInfo(hits);
    }

    private static void showFinalInfo(List<Hit> hits) {
//        double misses = 0;
        double criticals = 0;
        List<Double> critDamageList = new ArrayList<>();
        List<Double> normalDamageList = new ArrayList<>();
        double totalCritDmg = 0;
        double totalNormalDmg = 0;
        int totalDmg = 0;
        List<Double> attackTimes = new ArrayList<>();
        for (int i = 0; i < hits.size(); i++) {
            Hit hit = hits.get(i);
//            if (hit.isMiss()) {
//                misses++;
//            }
            if (hit.isCritical()) {
                criticals++;
                totalCritDmg += hit.getDamage();
                critDamageList.add((double) hit.getDamage());
            } else {
                totalNormalDmg += hit.getDamage();
                normalDamageList.add((double) hit.getDamage());
            }
            if (i != 0) {
                attackTimes.add(ChronoUnit.MILLIS.between(hits.get(i - 1).getTimeOfHit(), hits.get(i).getTimeOfHit()) / 1000.0);
            }
        }

//        System.out.printf("Misses: %.2f%%%n", misses / hits.size() * 100);
        System.out.printf("Total hits for %d minutes: %d%n", MINUTES_TO_RUN, hits.size());
        System.out.printf("Criticals: %.2f%%%n", criticals / hits.size() * 100);
        System.out.printf("Total damage: %.2f dmg%n", totalCritDmg + totalNormalDmg);
        System.out.printf("Average normal hit: %.2f dmg%n", totalNormalDmg / normalDamageList.size());
        System.out.printf("Average critical hit: %.2f dmg%n", totalCritDmg / critDamageList.size());
        System.out.printf("Average hit time: %.2fs%n", attackTimes.stream().mapToDouble(i->i).average().getAsDouble());
    }

    private static BufferedImage convertToCompatibleType(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();

        BufferedImage compatibleImage = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        compatibleImage.getGraphics().drawImage(image, 0, 0, null);

        return compatibleImage;
    }

    private static void showMat(Mat mat) {
        MatOfByte matOfByte = new MatOfByte();
        Imgcodecs.imencode(".jpg", mat, matOfByte);
        byte[] byteArray = matOfByte.toArray();

        // Decode the encoded image data and display using HighGui.imshow()
        Mat decodedMat = Imgcodecs.imdecode(new MatOfByte(byteArray), Imgcodecs.IMREAD_UNCHANGED);
        HighGui.imshow("Screen Capture", decodedMat);
        HighGui.waitKey(1);
    }

    private static boolean isSame(Mat previous, Mat current) {
        if (previous == null) {
            return false;
        }
        // Perform element-wise comparison
        Mat diffMat = new Mat();
        Core.compare(previous, current, diffMat, Core.CMP_NE);
        return Core.countNonZero(diffMat) == 0;
    }

    private static Runnable processImage(Mat image, LocalTime now) throws TesseractException {
//        System.out.println(Thread.currentThread().getName());
        image = cropLastTwoRowsFromImage(image);
//        viewImage(image);
        image = resizeImage(image, 2);
//        viewImage(image);

        // Apply thresholding
        Imgproc.threshold(image, image, 127, 255, Imgproc.THRESH_BINARY | Imgproc.THRESH_OTSU);
//        viewImage(image);
        // Perform noise removal
        Imgproc.blur(image, image, new Size(2, 2));
//        viewImage(image);
        BufferedImage bufferedImage = matToBufferedImage(image);
        tesseract.setLanguage("eng");
        tesseract.setTessVariable("user_defined_dpi", "150");
        String result = tesseract.doOCR(bufferedImage);
        System.out.println(result);
        String damageString = result.replaceAll(NON_NUMERIC, "");
        Runnable runnable = new Runnable() {
            @Override
            public void run() {

            }
        };

        Integer dmg = 1094;
        Random rn = new Random();
        int answer = rn.nextInt(10) + 1;
        if (answer >= 7) {
            dmg = 6438;
        }
        try {
            dmg = Integer.parseInt(damageString);
        } catch (Exception e) {

        }
        boolean crit = false;
        if (dmg > 3000) {
            crit = true;
        }
        Hit hit = new Hit(dmg, now, crit);
        if (dmg != null) {
            hits.add(hit);
        }

        return runnable;
    }

}
