package saru.api;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

/** 画像の指紋(dHash)を作る。64ビットの数字 */
public class DHash {

	/** 画像のバイト列から指紋を作る。読めない画像なら IOException */
	public static long compute(byte[] data) throws IOException {
		BufferedImage src = ImageIO.read(new ByteArrayInputStream(data));
		if (src == null) {
			throw new IOException("画像として読めません(対応: jpg / png / gif / bmp)");
		}
		int w = src.getWidth();
		int h = src.getHeight();

		// 9列×8行のマスに分けて、マスごとの明るさの平均を出す
		double[][] gray = new double[8][9];
		for (int cy = 0; cy < 8; cy++) {
			for (int cx = 0; cx < 9; cx++) {
				int x0 = cx * w / 9;
				int x1 = Math.min(w, Math.max(x0 + 1, (cx + 1) * w / 9));
				int y0 = cy * h / 8;
				int y1 = Math.min(h, Math.max(y0 + 1, (cy + 1) * h / 8));
				double sum = 0;
				int n = 0;
				for (int y = y0; y < y1; y++) {
					for (int x = x0; x < x1; x++) {
						int argb = src.getRGB(x, y);
						int a = (argb >>> 24) & 0xFF;
						int r = (argb >> 16) & 0xFF;
						int g = (argb >> 8) & 0xFF;
						int b = argb & 0xFF;
						double v = 0.299 * r + 0.587 * g + 0.114 * b;
						v = v * a / 255.0 + 255.0 * (255 - a) / 255.0; // 透明部分は白
						sum += v;
						n++;
					}
				}
				gray[cy][cx] = sum / n;
			}
		}

		// 横の隣どうしを比べる(左が明るければ1)。8行×8個=64ビット
		long hash = 0L;
		for (int y = 0; y < 8; y++) {
			for (int x = 0; x < 8; x++) {
				hash = (hash << 1) | (gray[y][x] > gray[y][x + 1] ? 1L : 0L);
			}
		}
		return hash;
	}

	/** 2つの指紋の違い(0〜64)。小さいほど似ている */
	public static int distance(long a, long b) {
		return Long.bitCount(a ^ b);
	}
}