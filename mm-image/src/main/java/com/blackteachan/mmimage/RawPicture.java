package com.blackteachan.mmimage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;

/**
 * 魔术师.raw图片文件
 *
 * @author blackteachan
 * @since 2024-03-13 16:06
 */
public class RawPicture {

    private int mWidth;
    private int mHeight;
    /**
     * raw图片字节（每个字节表示两个颜色值，偶下标：GB，奇下标：AR）
     */
    private byte[] mBytes;
    private byte[] mAlphaArray;
    private byte[] mRedArray;
    private byte[] mGreenArray;
    private byte[] mBlueArray;
    private String mFileName;
    private String mOutputPath;
    private FileType mFileType;

    public RawPicture(File file) {
        String fileName = file.getName();
        String suffix = fileName.substring(fileName.lastIndexOf('.') + 1);
        System.out.println("fileName: " + fileName);
        this.mFileName = fileName.substring(0, fileName.lastIndexOf('.'));
        if (suffix.contains("raw")) {
            System.out.println("file is raw");
            // raw文件读入
            readRaw(file);
            this.mFileType = FileType.RAW;
        } else {
            System.out.println("file is picture");
            // 图片读入
            readPic(file);
            this.mFileType = FileType.PICTURE;
        }
    }

    public RawPicture setWidth(int width) {
        this.mWidth = width;
        return this;
    }

    public RawPicture setHeight(int height) {
        this.mHeight = height;
        return this;
    }

    public void output() {
        if (this.mFileType == FileType.RAW) {
            outputPng();
        }else {
            outputRaw();
        }
    }

    private void readRaw(File file) {
        try (FileInputStream fis = new FileInputStream(file);
             BufferedInputStream bis = new BufferedInputStream(fis)) {
            this.mBytes = new byte[(int) file.length()];
            bis.read(this.mBytes);
            System.out.println("read raw successful");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readPic(File file) {
        BufferedImage image;
        try {
            image = ImageIO.read(file);

            // 获取图片的宽度和高度
            this.mWidth = image.getWidth();
            this.mHeight = image.getHeight();

//            int bytesLength = this.mWidth * this.mHeight;
            int bytesLength = this.mWidth * this.mHeight * 2;
            this.mBytes = new byte[bytesLength];
            int bytesIndex = 0;
            System.out.println("pic byte size: " + bytesLength);

            // 遍历图片的每个像素
            System.out.println("write bytes: ");
            for (int y = 0; y < this.mHeight; y++) {
                for (int x = 0; x < this.mWidth; x++) {
                    // 获取(x, y)位置的像素
                    int pixel = image.getRGB(x, y);

                    // 获取像素的ARGB分量
                    short ffA = (short) (((pixel & 0xff000000) >> 24) & 0xff);
                    short ffR = (short) (((pixel & 0x00ff0000) >> 16) & 0xff);
                    short ffG = (short) (((pixel & 0x0000ff00) >> 8) & 0xff);
                    short ffB = (short) (((pixel & 0x000000ff)) & 0xff);
                    // 按魔术师的布局（0xGBAR）排列
                    // 原像素值0xFF（0-255）转换成0xF（0-15）
//                    int rawByte = (int) (g * 1.0 / 0xff * 0xf000)
//                            + (int) (b * 1.0 / 0xff * 0xf00)
//                            + (int) (a * 1.0 / 0xff * 0xf0)
//                            + (int) (r * 1.0 / 0xff * 0xf);
                    //TODO 优化
                    int fG = (int) (ffG * 1.0 / 0xff * 0xf);
                    int fB = (int) (ffB * 1.0 / 0xff * 0xf);
                    int fA = (int) (ffA * 1.0 / 0xff * 0xf);
                    int fR = (int) (ffR * 1.0 / 0xff * 0xf);
                    int rawByte1 = (fG << 4) + fB;
                    int rawByte2 = (fA << 4) + fR;

                    // 根据需要处理这些颜色分量
//                    this.mBytes[bytesIndex++] = (byte) (rawByte & 0xffff);
                    this.mBytes[bytesIndex++] = (byte) (rawByte1 & 0xff);
                    this.mBytes[bytesIndex++] = (byte) (rawByte2 & 0xff);
                    System.out.printf("%X%X%X%X ", fG, fB, fA, fR);
//                    System.out.printf("%02X%02X%02X%02X ", ffA, ffR, ffG, ffB);
//                    System.out.printf("%04X ", pixel);
//                    System.out.printf("%04X ", rawByte);
                }
                System.out.println();
            }
            System.out.println("read pic successful");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void outputRaw(){
        String outputFilePath = this.mFileName + ".raw";
        try (FileOutputStream fos = new FileOutputStream(outputFilePath)) {
            fos.write(this.mBytes);
            System.out.println("output raw successful");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void outputPic(int w, int h, String outputPath) {
        File outputFile = new File(outputPath);
        if (outputFile.exists()) {
            outputFile.delete();
        }
        // 解析RGB
        BufferedImage image = new BufferedImage(this.mWidth, this.mHeight, BufferedImage.TYPE_INT_RGB);
        int index = 0;
        for (int y = 0; y < this.mHeight; y++) {
            for (int x = 0; x < this.mWidth; x++) {
                //
                int rgb = (mBytes[index++] & 0xFF) + ((mBytes[index++] & 0xFF) << 8);
                rgb = argb4444ToRgb(rgb);
                image.setRGB(x, y, rgb);
                System.out.printf("%04X ", rgb);
            }
            System.out.println();
        }
        try {
            ImageIO.write(image, "jpg", outputFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("w: " + w + ", h: " + h);
    }

    private void outputPng() {
        outputPng(this.mWidth, this.mHeight);
    }

    private void outputPng(int w, int h) {
        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_4BYTE_ABGR);

//        System.out.println("argb bytes: ");
        for (int y = 0, index = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                // 读取颜色字节（来自raw）
                byte bGB = this.mBytes[index++];
                byte bAR = this.mBytes[index++];
                // 换算0xF颜色值（来自raw）
                short fG = (short) ((bGB >> 4) & 0xf);
                short fB = (short) (bGB & 0xf);
                short fA = (short) ((bAR >> 4) & 0xf);
                short fR = (short) (bAR & 0xf);
                // 换算0xFF颜色值
                int ffA = (int) (fA * 1.0 / 0xf * 0xff);
                int ffR = (int) (fR * 1.0 / 0xf * 0xff);
                int ffG = (int) (fG * 1.0 / 0xf * 0xff);
                int ffB = (int) (fB * 1.0 / 0xf * 0xff);
                // 像素点的ARGB颜色
                int argb = ((ffA << 24) & 0xff000000)
                        + ((ffR << 16) & 0xff0000)
                        + ((ffG << 8) & 0xff00)
                        + ((ffB) & 0xff);
//                System.out.printf("%02X%02X ", bAR, bGB);
//                System.out.printf("%X%X%X%X ", fA, fR, fG, fB);
//                System.out.printf("%02X%02X%02X%02X ", ffA, ffR, ffG, ffB);
//                System.out.printf("%08X ", argb);
                // 绘制像素点
                image.setRGB(x, y, argb);
            }
//            System.out.println();
        }

        // 输出图像到文件
        try {
            ImageIO.write(image, "PNG", new File(this.mFileName + ".png"));
            System.out.println("output pic successful");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int argb4444ToRgb(int in) {
//        short a = (short) ((in & 0xf000) >> 12);
        short r = (short) ((in & 0x0f00) >> 8);
        short g = (short) ((in & 0x00f0) >> 4);
        short b = (short) ((in & 0x000f));
        return (int) (r * 1.0 / 0xf * 0xff0000)
                + (int) (g * 1.0 / 0xf * 0xff00)
                + (int) (b * 1.0 / 0xf * 0xff);
    }

    public enum FileType {
        /**
         * 仪表RAW文件
         */
        RAW,
        /**
         * 图片文件
         */
        PICTURE
    }

}
