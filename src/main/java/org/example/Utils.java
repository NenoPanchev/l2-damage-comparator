package org.example;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.highgui.HighGui;
import org.opencv.imgproc.Imgproc;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;

public class Utils {
    public static void viewImage(Mat image) {
        // Display the output image
        HighGui.imshow("Image view", image);
        HighGui.waitKey();
        HighGui.destroyAllWindows();
    }

    // Convert a BufferedImage object to a Mat
    public static Mat bufferedImageToMat(BufferedImage image) {
        int type = image.getType();
        int channels = 3; // Default to 3 channels (BGR)

        if (type == BufferedImage.TYPE_3BYTE_BGR) {
            channels = 3; // If the image type is 3-byte BGR, set channels to 3 (BGR)
        } else if (type == BufferedImage.TYPE_4BYTE_ABGR) {
            channels = 4; // If the image type is 4-byte ABGR, set channels to 4 (ABGR)
        }

        int width = image.getWidth();
        int height = image.getHeight();

        Mat mat = new Mat(height, width, CvType.CV_8UC(channels));

        if (type == BufferedImage.TYPE_3BYTE_BGR) {
            byte[] data = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
            mat.put(0, 0, data);
        } else if (type == BufferedImage.TYPE_4BYTE_ABGR) {
            int[] data = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
            byte[] byteData = new byte[data.length * 4];

            for (int i = 0; i < data.length; i++) {
                byteData[i * 4] = (byte) (data[i] >> 16); // Red
                byteData[i * 4 + 1] = (byte) (data[i] >> 8); // Green
                byteData[i * 4 + 2] = (byte) (data[i]); // Blue
                byteData[i * 4 + 3] = (byte) (data[i] >> 24); // Alpha
            }

            mat.put(0, 0, byteData);
        }

        return mat;
    }

    // Convert a Mat object to a BufferedImage
    public static BufferedImage matToBufferedImage(Mat mat) {
        int type = BufferedImage.TYPE_BYTE_GRAY;
        if (mat.channels() > 1) {
            type = BufferedImage.TYPE_3BYTE_BGR;
        }
        int bufferSize = mat.channels() * mat.cols() * mat.rows();
        byte[] buffer = new byte[bufferSize];
        mat.get(0, 0, buffer);

        BufferedImage image = new BufferedImage(mat.cols(), mat.rows(), type);
        final byte[] targetPixels = ((java.awt.image.DataBufferByte) image.getRaster().getDataBuffer()).getData();
        System.arraycopy(buffer, 0, targetPixels, 0, buffer.length);

        return image;
    }

    // Resize the image by a scale factor
    public static Mat resizeImage(Mat image, double scaleFactor) {
        int newWidth = (int) (image.width() * scaleFactor);
        int newHeight = (int) (image.height() * scaleFactor);

        Mat resizedImage = new Mat();
        Size newSize = new Size(newWidth, newHeight);
        Imgproc.resize(image, resizedImage, newSize);

        return resizedImage;
    }

    public static Mat cropLastTwoRowsFromImage(Mat image) {
        Rect roi = new Rect(0, image.height() - 32, image.width(), 32);
        return new Mat(image, roi);
    }
}
