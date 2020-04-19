// vim: noet

package vktec.geomexport;

import java.nio.ByteBuffer;
import net.minecraft.client.texture.NativeImage;
import vktec.geomexport.duck.NativeImageDuck;

public class ImageMixer {
	public static NativeImage tintImage(NativeImage image, int rgb) {
		NativeImage.Format format = image.getFormat();
		if (format != NativeImage.Format.RGB && format != NativeImage.Format.RGBA) {
			throw new UnsupportedOperationException("Cannot tint a luminance image");
		}

		NativeImage target = new NativeImage(format, image.getWidth(), image.getHeight(), false);
		target.copyFrom(image);
		ByteBuffer buf = ((NativeImageDuck)(Object)target).getDataBuffer();

		int[] color = {rgb >> 16 & 0xFF, rgb >> 8 & 0xFF, rgb & 0xFF};

		byte[] pixel = new byte[format.getChannelCount()];
		while (buf.remaining() >= pixel.length) {
			buf.mark();
			buf.get(pixel);

			for (int i = 0; i < 3; i++) {
				pixel[i] = (byte)(((((int)pixel[i]) & 0xFF) * color[i]) / 255);
			}

			buf.reset();
			buf.put(pixel);
		}

		return target;
	}
}
