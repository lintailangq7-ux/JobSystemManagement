package saru.api;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

/** WAV の音色の特徴 (MFCC の平均と標準偏差)。16bit の PCM の WAV だけ読める */
public final class AudioFeature {
	private static final int NUM_FILTERS = 26;
	private static final int NUM_COEFS = 13; // c1〜c13 を使う (音量に左右される c0 は使わない)
	private static final double FRAME_SEC = 0.025;
	private static final double HOP_SEC = 0.010;

	private AudioFeature() {}

	/** 特徴 (平均13個 + 標準偏差13個 = 26個)。読めない形式や短すぎる音は null */
	public static float[] mfcc(byte[] wav) {
		try (AudioInputStream in = AudioSystem.getAudioInputStream(new ByteArrayInputStream(wav))) {
			AudioFormat f = in.getFormat();
			if (f.getEncoding() != AudioFormat.Encoding.PCM_SIGNED || f.getSampleSizeInBits() != 16) return null;
			byte[] raw = in.readAllBytes();
			int channels = f.getChannels();
			int frameBytes = 2 * channels;
			int n = raw.length / frameBytes;
			boolean big = f.isBigEndian();
			double[] x = new double[n];
			for (int i = 0; i < n; i++) {
				double s = 0;
				for (int c = 0; c < channels; c++) {
					int o = i * frameBytes + c * 2;
					int v = big ? (raw[o] << 8) | (raw[o + 1] & 0xFF) : (raw[o + 1] << 8) | (raw[o] & 0xFF);
					s += v;
				}
				x[i] = s / channels / 32768.0; // 複数チャンネルは平均してモノラルにする
			}
			return compute(x, f.getSampleRate());
		} catch (UnsupportedAudioFileException | IOException e) {
			return null;
		}
	}

	private static float[] compute(double[] x, double sr) {
		int frameLen = (int) Math.round(FRAME_SEC * sr);
		int hop = (int) Math.round(HOP_SEC * sr);
		int fftSize = Integer.highestOneBit(Math.max(frameLen, 2) - 1) << 1; // frameLen 以上の2のべき乗
		if (x.length < frameLen * 3) return null; // 短すぎる

		// 直流成分を引いて、プリエンファシス (高い音を強める)
		double mean = 0;
		for (double v : x) mean += v;
		mean /= x.length;
		double[] y = new double[x.length];
		y[0] = x[0] - mean;
		for (int i = 1; i < x.length; i++) y[i] = (x[i] - mean) - 0.97 * (x[i - 1] - mean);

		double[] window = new double[frameLen];
		for (int i = 0; i < frameLen; i++) window[i] = 0.54 - 0.46 * Math.cos(2 * Math.PI * i / (frameLen - 1));

		double[][] bank = melFilterBank(fftSize, sr);
		int bins = fftSize / 2 + 1;
		double[] re = new double[fftSize];
		double[] im = new double[fftSize];

		List<double[]> frames = new ArrayList<>();
		List<Double> energies = new ArrayList<>();
		for (int start = 0; start + frameLen <= y.length; start += hop) {
			Arrays.fill(re, 0);
			Arrays.fill(im, 0);
			double energy = 0;
			for (int i = 0; i < frameLen; i++) {
				re[i] = y[start + i] * window[i];
				energy += re[i] * re[i];
			}
			fft(re, im);
			double[] logMel = new double[NUM_FILTERS];
			for (int m = 0; m < NUM_FILTERS; m++) {
				double s = 0;
				for (int k = 0; k < bins; k++) s += bank[m][k] * (re[k] * re[k] + im[k] * im[k]);
				logMel[m] = Math.log(s + 1e-10);
			}
			double[] c = new double[NUM_COEFS];
			for (int j = 1; j <= NUM_COEFS; j++) { // DCT で c1〜c13 にまとめる
				double s = 0;
				for (int m = 0; m < NUM_FILTERS; m++) s += logMel[m] * Math.cos(Math.PI * j * (m + 0.5) / NUM_FILTERS);
				c[j - 1] = s;
			}
			frames.add(c);
			energies.add(energy);
		}

		double maxE = 0;
		for (double e : energies) maxE = Math.max(maxE, e);
		double[] sum = new double[NUM_COEFS];
		double[] sq = new double[NUM_COEFS];
		int used = 0;
		for (int i = 0; i < frames.size(); i++) {
			if (energies.get(i) < maxE * 0.01) continue; // ほぼ無音のフレームは除く
			double[] c = frames.get(i);
			for (int j = 0; j < NUM_COEFS; j++) {
				sum[j] += c[j];
				sq[j] += c[j] * c[j];
			}
			used++;
		}
		if (used < 10) return null;
		float[] out = new float[NUM_COEFS * 2];
		for (int j = 0; j < NUM_COEFS; j++) {
			double m = sum[j] / used;
			out[j] = (float) m;
			out[NUM_COEFS + j] = (float) Math.sqrt(Math.max(0, sq[j] / used - m * m));
		}
		return out;
	}

	private static double mel(double hz) { return 2595 * Math.log10(1 + hz / 700); }
	private static double hzOf(double mel) { return 700 * (Math.pow(10, mel / 2595) - 1); }

	private static double[][] melFilterBank(int fftSize, double sr) {
		double fMax = Math.min(sr / 2, 8000);
		double melMin = mel(20);
		double melMax = mel(fMax);
		int[] bin = new int[NUM_FILTERS + 2];
		for (int i = 0; i < bin.length; i++) {
			bin[i] = (int) Math.floor(fftSize * hzOf(melMin + (melMax - melMin) * i / (NUM_FILTERS + 1)) / sr);
		}
		double[][] bank = new double[NUM_FILTERS][fftSize / 2 + 1];
		for (int m = 1; m <= NUM_FILTERS; m++) {
			for (int k = bin[m - 1]; k < bin[m]; k++) bank[m - 1][k] = (double) (k - bin[m - 1]) / (bin[m] - bin[m - 1]);
			for (int k = bin[m]; k < bin[m + 1]; k++) bank[m - 1][k] = (double) (bin[m + 1] - k) / (bin[m + 1] - bin[m]);
		}
		return bank;
	}

	private static void fft(double[] re, double[] im) {
		int n = re.length;
		for (int i = 1, j = 0; i < n; i++) {
			int bit = n >> 1;
			for (; (j & bit) != 0; bit >>= 1) j ^= bit;
			j ^= bit;
			if (i < j) {
				double t = re[i]; re[i] = re[j]; re[j] = t;
				t = im[i]; im[i] = im[j]; im[j] = t;
			}
		}
		for (int len = 2; len <= n; len <<= 1) {
			double ang = -2 * Math.PI / len;
			double wr = Math.cos(ang);
			double wi = Math.sin(ang);
			for (int i = 0; i < n; i += len) {
				double cr = 1;
				double ci = 0;
				for (int j = 0; j < len / 2; j++) {
					int a = i + j;
					int b = i + j + len / 2;
					double tr = re[b] * cr - im[b] * ci;
					double ti = re[b] * ci + im[b] * cr;
					re[b] = re[a] - tr;
					im[b] = im[a] - ti;
					re[a] += tr;
					im[a] += ti;
					double ncr = cr * wr - ci * wi;
					ci = cr * wi + ci * wr;
					cr = ncr;
				}
			}
		}
	}

	// ---------- 保存と比較 ----------

	public static String toText(float[] v) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < v.length; i++) {
			if (i > 0) sb.append(',');
			sb.append(v[i]);
		}
		return sb.toString();
	}

	public static float[] parse(String s) {
		String[] parts = s.split(",");
		float[] v = new float[parts.length];
		for (int i = 0; i < parts.length; i++) v[i] = Float.parseFloat(parts[i]);
		return v;
	}

	/** query に近い順に、投稿の id を返す(同じ投稿に複数の音声があれば、一番近いもの) */
	public static List<Integer> rank(float[] query, List<Integer> postIds, List<float[]> vecs, int limit) {
		int d = query.length;
		// 項目ごとに、値の大きさがそろうように、標準化してから比べる
		double[] mu = new double[d];
		double[] sd = new double[d];
		for (float[] v : vecs) for (int j = 0; j < d; j++) mu[j] += v[j];
		for (int j = 0; j < d; j++) mu[j] /= vecs.size();
		for (float[] v : vecs) for (int j = 0; j < d; j++) sd[j] += (v[j] - mu[j]) * (v[j] - mu[j]);
		for (int j = 0; j < d; j++) sd[j] = Math.max(Math.sqrt(sd[j] / vecs.size()), 1e-3);

		Map<Integer, Double> best = new HashMap<>();
		for (int i = 0; i < vecs.size(); i++) {
			double dist = 0;
			for (int j = 0; j < d; j++) {
				double diff = (vecs.get(i)[j] - query[j]) / sd[j];
				dist += diff * diff;
			}
			best.merge(postIds.get(i), dist, Math::min);
		}
		return best.entrySet().stream()
				.sorted(Map.Entry.comparingByValue())
				.limit(limit)
				.map(Map.Entry::getKey)
				.toList();
	}
}