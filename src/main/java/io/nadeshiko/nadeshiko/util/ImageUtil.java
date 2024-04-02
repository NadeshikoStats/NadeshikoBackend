package io.nadeshiko.nadeshiko.util;

import lombok.experimental.UtilityClass;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@UtilityClass
public class ImageUtil {

	/**
	 * Creates a {@link BufferedImage} from a byte array
	 * @param imageData A raw byte[] representing an image
	 * @return A new BufferedImage instance of the image represented by the byte[]
	 * @throws IOException If something went wrong reading the image data
	 */
	public BufferedImage createImageFromBytes(byte[] imageData) throws IOException {
		ByteArrayInputStream inputStream = new ByteArrayInputStream(imageData);
		return ImageIO.read(inputStream);
	}

	/**
	 * Gets a byte[] from a {@link BufferedImage}
	 * @param image A BufferedImage instance of an image to read
	 * @return A raw byte[] representation of the image, in PNG format
	 * @throws IOException If something went wrong writing the image data
	 */
	public byte[] getBytesFromImage(BufferedImage image) throws IOException {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		ImageIO.write(image, "png", outputStream);
		return outputStream.toByteArray();
	}
}
