package Utils;



import functions.ImageOperation;

import java.awt.image.BufferedImage;

public class RgbMaster {
    private BufferedImage image;
    private int height;
    private int width;
    private boolean hasAlphaChannel;
    private int[] pixels;

    public RgbMaster(BufferedImage image) {
        this.image = image;
        height = image.getHeight();
        width = image.getWidth();
        hasAlphaChannel = image.getAlphaRaster() != null;
        pixels = new int[width*height];
        image.getRGB(0, 0, width, height, pixels, 0, width);
    }

    public BufferedImage getImage() {
        return image;
    }

    public void changeImage(ImageOperation operation) throws Exception {
        for (int i = 0; i < pixels.length; i++) {
            float[] pixel = ImageUtils.rgbIntToArray(pixels[i]);
            float[] newPixel = operation.execute(pixel);
            pixels[i] = ImageUtils.rgbIntToArray(newPixel);


        }
        final int type = hasAlphaChannel ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB;
        image.setRGB(0,0,width,height,pixels,0,width);

    }


}
