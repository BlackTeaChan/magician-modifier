package com.blackteachan.mmimage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @author blackteachan
 * @since 2024-03-13 17:28
 */
public class PicToRaw {

    private static final String BASE_DIR = "/Users/blackteachan/IdeaProjects/magician-modifier/";

    public static void main(String[] args) {
        try {
            // 读取PNG图片
            BufferedImage image = ImageIO.read(new File(BASE_DIR + "pic/test.png"));

            // 获取图片的宽度和高度
            int width = image.getWidth();
            int height = image.getHeight();

            int bytesLength = width * height * 2;
            byte[] bytes = new byte[bytesLength];
            int bytesIndex = 0;
            System.out.println("pic byte size: " + bytesLength);

            // 遍历图片的每个像素
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    // 获取(x, y)位置的像素
                    int pixel = image.getRGB(x, y);

                    // 获取像素的ARGB分量
                    short a = (short) (((pixel & 0xff000000) >> 24) & 0xff);
                    short r = (short) (((pixel & 0x00ff0000) >> 16) & 0xff);
                    short g = (short) (((pixel & 0x0000ff00) >> 8) & 0xff);
                    short b = (short) (((pixel & 0x000000ff)) & 0xff);
                    // 按魔术师的布局（0xGBAR）排列
                    // 原像素值0xFF（0-255）转换成0xF（0-15）
                    int rawByte = (int) (g * 1.0 / 0xff * 0xf000)
                            + (int) (b * 1.0 / 0xff * 0xf00)
                            + (int) (a * 1.0 / 0xff * 0xf0)
                            + (int) (r * 1.0 / 0xff * 0xf);
                    int rawByte1 = (int) (g * 1.0 / 0xff * 0xf0)
                            + (int) (b * 1.0 / 0xff * 0xf);
                    int rawByte2 = (int) (a * 1.0 / 0xff * 0xf0)
                            + (int) (r * 1.0 / 0xff * 0xf);

                    // 根据需要处理这些颜色分量
//                    System.out.printf("%04X ", pixel);
                    System.out.printf("%04X ", rawByte);
                    bytes[bytesIndex++] = (byte) (rawByte1 & 0xff);
                    bytes[bytesIndex++] = (byte) (rawByte2 & 0xff);
                }
                System.out.println();
            }
            outputRaw(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void outputRaw(byte[] bytes) {
        String outputFilePath = "output.raw";
        try (FileOutputStream fos = new FileOutputStream(outputFilePath)) {
            fos.write(bytes);
            System.out.println("output successful");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
