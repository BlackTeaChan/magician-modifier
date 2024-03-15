package com.blackteachan.mmimage;

import java.io.File;
import java.net.URL;

/**
 * @author blackteachan
 * @since 2024-03-15 13:30
 */
public class Main {

    private static final String FILE_NAME = "blacktea_2_86_62";

    public static void main(String[] args) {
//        new Main().png2raw();
        new Main().raw2png();
    }

    public void png2raw(){
        URL resourceUrl = Main.class.getClassLoader().getResource("pic/" + FILE_NAME + ".png");
        if (resourceUrl != null) {
            File file = new File(resourceUrl.getPath());
            RawPicture rawPicture = new RawPicture(file);
            rawPicture.output();
        }
    }

    public void raw2png(){
        URL resourceUrl = Main.class.getClassLoader().getResource("raw/" + FILE_NAME + ".raw");
        if (resourceUrl != null) {
            File file = new File(resourceUrl.getPath());
            RawPicture rawPicture = new RawPicture(file)
                    .setWidth(86)
                    .setHeight(62);
            rawPicture.output();
        }
    }

}
