package com.blackteachan.mmimage;

import cn.hutool.core.io.file.FileReader;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * RAW文件有10664个字节
 * 一个像素占用两个字节（推测是使用16bit颜色）
 * 分析国旗.RAW文件，可以看出起始和末尾都有172个像素
 * 图片的宽高应该是86*62
 * 分析tea.raw得出：
 * 黑色：0xFF0F
 * 白色：0xFFFF
 * 红色：0x22FF
 * <br/>
 * 从魔术师设计师提供的文件分析：
 * 黑色：0xF000
 * 白色：0x0FFF
 * 透明：0xFFFF ？
 * <br/>
 * 猜测：
 * raw文件中使用16进制方式读取，每两个字节对应像素的GBAR值，
 * 也就是说16进制读取到的文件是0x12FF，那么对应像素RGBA(255, 16, 32, 1.0)
 * 1 => 16bit绿色 => 1/16*255 => 16
 * 2 => 16bit蓝色 => 2/16*255 => 32
 * F => 16bit透明度 => 16/16*1.0 => 1.0
 * F => 16bit红色 => 16/16*255 => 255
 *
 * @author blackteachan
 * @since 2022-08-15 14:25
 */
public class MmImageApplication {

    private static final String FILE_DIR = "/Users/blackteachan/IdeaProjects/magician-modifier/files/";
    private static final String IMAGE_DIR = "/Users/blackteachan/IdeaProjects/magician-modifier/images/";

    public static void main(String[] args) {
        // raw图片文件
//        String fileName = "tea.raw";
        String fileName = "output.raw";
        // 读取图片
        FileReader fileReader = new FileReader(fileName);
        byte[] bytes = fileReader.readBytes();
        //
        int length = bytes.length;
        // 10664
        System.out.println("raw byte size: " + length);
        // 预览源bytes
//        for (byte bt : bytes) {
//            System.out.print(String.format("%02X ", bt));
//        }
        System.out.println();
        // bytes重新排序
//        byte[] tempBytes = new byte[length];
//        for (int i = 0; i < length / 2; i++) {
//            tempBytes[i*2] = bytes[length - i*2 - 2];
//            tempBytes[i*2 +1] = bytes[length - i*2 - 1];
//        }
//        bytes = tempBytes;
        // 预览新bytes
//        for (byte bt : bytes) {
//            System.out.print(String.format("%02X ", bt));
//        }
        System.out.println();

//        int fileIndex = 0;
//        for (int w = length / 2; w >0 ; w--) {
//            int h = length / 2 / w;
//            if (h > 200 || w > 200){
//                continue;
//            }
////            System.out.println("i: " + i + ", w: " + w + ", h: " + h);
//            // 写入到文件
//            String filename = FILE_DIR + fileIndex + ".txt";
//            File file = new File(filename);
//            FileUtil.touch(file);
//            FileWriter fileWriter = new FileWriter(filename);
//            int index = 0;
//            for (int i = 0; i < w; i++) {
//                for (int j = 0; j < h; j++) {
//                    String content = String.format("%02X%02X ", bytes[index++], bytes[index++]);
////                    System.out.print(content);
//                    fileWriter.append(content);
//                }
//                fileWriter.append("\n");
//            }
//            fileIndex++;
//        }
//        int rgb = red;
//        rgb = (rgb << 8) + green;
//        rgb = (rgb << 8) + blue;

        // 写入图片 index: 138, w: 63, h: 84
        int w = 86, h = 62;
        generateBmp(bytes, w, h, fileName);
//        int fileIndex = 0;
//        for (w = length / 2; w >0 ; w-=2) {
//            h = length / 2 / w;
//            if (h > 200 || w > 200){
//                continue;
//            }
//            fileIndex++;
//        }

    }

    public static void generateBmp(byte[] bytes, int w, int h, String fileName) {
        File outputFile = new File(IMAGE_DIR + fileName + ".jpg");
        if (outputFile.exists()) {
            outputFile.delete();
        }
        // 统计
        Map<Integer, Integer> totalMap = new HashMap<>(10);
        // 解析RGB
        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        int index = 0;
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                // 24bit
//                    int rgb = (bytes[index++]) << 24 + (bytes[index++]) << 16 + (bytes[index++]) << 8 + (bytes[index++]);
                // 16bit
//                    int rgb = (bytes[index++] ^ 0xFF)
//                            + (bytes[index++] << 8);
//                    int rgb = (bytes[index++] << 8)
//                            + (bytes[index++] ^ 0xFF);
//                    int low = bytes[index++] << 8;
//                    int high = bytes[index++]^0xFF + 0x0F;
//                    int rgb = low + high;
                // 8bit
//                    int rgb = (bytes[index++]);

//                int bt1 = (int) (((bytes[index++] & 0xFF)) << 8);
//                int bt2 = (int) (((bytes[index++] & 0xFF)));
//                    bt1 = 0xffff;
//                    bt2 = 0x00;
//                int rgb = (int) (bt1 + bt2);
//                rgb = rgb565ToRgb(bt1 + bt2);
//                System.out.printf("%04X ", rgb);

                // 24-03-13版本
                int rgb = (bytes[index++] & 0xFF) + ((bytes[index++] & 0xFF) << 8);
                String argb = String.format("%04X", rgb);
                System.out.printf(argb + " ");
                rgb = argb4444ToRgb(rgb);
                image.setRGB(j, i, rgb);
                // 进行统计
                Integer t = totalMap.get(rgb);
                if (t == null) {
                    t = 0;
                }
                totalMap.put(Integer.valueOf(rgb), t + 1);
            }
            System.out.println();

        }
        System.out.println("\ncolor count");
        for (Integer key : totalMap.keySet()) {
            Integer value = totalMap.get(key);
            if (value < 100) {
                continue;
            }
            System.out.println(String.format("%04X", key) + ": " + value);
        }
//        File outputFile = new File(IMAGE_DIR + fileName + ".bmp");
        try {
            ImageIO.write(image, "jpg", outputFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("w: " + w + ", h: " + h);
    }

    public static int argb4444ToRgb(int in) {
        short a = (short) ((in & 0xf000) >> 12);
        short r = (short) ((in & 0x0f00) >> 8);
        short g = (short) ((in & 0x00f0) >> 4);
        short b = (short) ((in & 0x000f));
        return (int) (r * 1.0 / 0xf * 0xff0000)
                + (int) (g * 1.0 / 0xf * 0xff00)
                + (int) (b * 1.0 / 0xf * 0xff);
    }

    public static int rgb565ToRgb(int rgb565) {
        short r = (short) ((rgb565 & 0xf800) >> 11);
        short g = (short) ((rgb565 & 0x07e0) >> 5);
        short b = (short) ((rgb565 & 0x001f));
        return (int)(r*1.0/0x1f*0xff0000)
                + (int)(g*1.0/0x3f*0xff00)
                + (int)(b*1.0/0x1f*0xff);
    }
}
