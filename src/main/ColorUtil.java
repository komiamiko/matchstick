package main;

import java.awt.Color;

public class ColorUtil {

	private ColorUtil() {}
	
	/**
	 * Fake primaries to interpolate using
	 */
	private static final int[][] primaries = {
			{253, 156, 186},
			{249, 164, 134},
			{209, 182, 110},
			{149, 198, 131},
			{76, 204, 183},
			{33, 201, 236},
			{131, 188, 255},
			{212, 168, 237},
	};
	
	public static final Color background = new Color(59,59,59);
	
	public static double flerp(double a, double b, double t) {
		return a + (b-a) * t;
	}
	
	public static double invflerp(double a, double b, double x) {
		double d = b-a;
		if(d == 0)return 0;
		return (x - a) / d;
	}
	
	public static double remap(double a, double b, double c, double d, double x) {
		final double e = b-a;
		final double f = e==0?0:(x-a)/e;
		return flerp(c, d, f);
	}
	
	/**
	 * Given luma and hue parameters, generate a color
	 * 
	 * @param l
	 * @param h
	 * @return
	 */
	public static Color lh(double l, double h) {
		h %= 1;
		if(h < 0)h += 1;
		int i = (int) (h * 8), j = (i + 1) & 7;
		double t = (h * 8) % 1;
		double r0 = primaries[i][0], g0 = primaries[i][1], b0 = primaries[i][2];
		double r1 = primaries[j][0], g1 = primaries[j][1], b1 = primaries[j][2];
		double r = flerp(r0, r1, t) * l, g = flerp(g0, g1, t) * l, b = flerp(b0, b1, t) * l;
		int ir = (int) r, ig = (int) g, ib = (int) b;
		return new Color(ir, ig, ib);
	}
	
}
