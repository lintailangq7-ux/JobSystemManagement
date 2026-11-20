package saru.api;

import java.awt.image.BufferedImage;

/** 画像の指紋 (dHash)。似た画像は、ビットの違いが少ない */
public final class ImageHash {
	private ImageHash() {}

	public static long dHash(BufferedImage src) {
		int w = src.getWidth();
		int h = src.getHeight();
		double[] sum = new double[9 * 8];
		int[] count = new int[9 * 8];
		for (int y = 0; y < h; y++) {
			int cy = Math.min(7, y * 8 / h);
			for (int x = 0; x < w; x++) {
				int cx = Math.min(8, x * 9 / w);
				int rgb = src.getRGB(x, y);
				double gray = 0.299 * ((rgb >> 16) & 0xFF) + 0.587 * ((rgb >> 8) & 0xFF) + 0.114 * (rgb & 0xFF);
				sum[cy * 9 + cx] += gray;
				count[cy * 9 + cx]++;
			}
		}
		double[] avg = new double[9 * 8];
		for (int i = 0; i < avg.length; i++) avg[i] = count[i] == 0 ? 0 : sum[i] / count[i];

		long hash = 0;
		for (int y = 0; y < 8; y++) {
			for (int x = 0; x < 8; x++) {
				hash = (hash << 1) | (avg[y * 9 + x] > avg[y * 9 + x + 1] ? 1L : 0L);
			}
		}
		return hash;
	}
}