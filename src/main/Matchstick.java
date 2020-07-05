package main;

import static main.ColorUtil.flerp;
import static main.ColorUtil.invflerp;
import static main.ColorUtil.remap;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;

public class Matchstick {

	public double x;
	public double y;
	public Color color;
	public double xtonext;
	
	public Matchstick(double x, double y, Color color) {
		this.x = x;
		this.y = y;
		this.color = color;
		xtonext = 1;
	}
	
	/**
	 * Magic constant C
	 * Used in the common function x to 1 - C/(x+C)
	 */
	public static final double C = 8;
	public static final double HUE_GAP = 0.002f;
	
	/**
	 * A reciprocal shaped function that starts at (0, 0)
	 * and converges at (inf, 1)
	 * 
	 * @param x
	 * @return
	 */
	public static double cf(double x) {
		return 1 - C/(x + C);
	}
	
	/**
	 * Inverse of cf
	 * 
	 * @param x
	 * @return
	 */
	public static double invcf(double x) {
		return C/(1 - x) - C;
	}
	
	/**
	 * What hue should this ordinal be coloured?
	 * 
	 * @param o
	 * @return
	 */
	public static double hueOf(Ordinal o) {
		if(o._cnf.size() == 0) {
			return cf(o._nat);
		}
		Ordinal a = Ordinal.WW;
		int cmpr = o.compareTo(a);
		while(cmpr > 0) {
			a = a.wexp();
			cmpr = o.compareTo(a);
		}
		double cl = 0, cr = 1;
		while(cr - cl > HUE_GAP && a.kind() == OrdinalKind.LIMIT) {
			int l = 0, r = 1<<16, shift = 7;
			while(l < r) {
				int m = l + ((r - l) >>> shift);
				shift = Math.max(1, shift - 2);
				Ordinal b = a.fundamental(m);
				if(b.equals(o)) {
					return flerp(cl,cr,cf(m));
				} else if(b.compareTo(o) < 0) {
					l = m + 1;
				} else {
					r = m;
				}
			}
			a = a.fundamental(l);
			double tcl = flerp(cl, cr, cf(l-1));
			cr = flerp(cl, cr, cf(l));
			cl = tcl;
			if(a.equals(o)) {
				return cr;
			}
		}
		return (cl + cr) * 0.5f;
	}
	
	/**
	 * Get the color for a specific ordinal
	 * @param o
	 * @return
	 */
	public static Color colorOf(Ordinal o) {
		double hue = hueOf(o);
		return ColorUtil.lh(1, hue);
	}
	
	/**
	 * Get the color as an integer
	 * 
	 * @param o
	 * @return
	 */
	public static int icolorOf(Ordinal o) {
		return colorOf(o).getRGB();
	}
	
	private static ArrayList<Matchstick>
		generateDiagramPre(Ordinal ordLow, Ordinal ordHigh,
				double yScale, double xLow, double xHigh, double xGap) {
		final double xGapBound = 1 - xGap;
		if(ordLow.equals(ordHigh)) {
			return new ArrayList<>();
		}
		ArrayList<Matchstick> result = new ArrayList<>();
		Ordinal ordRange = ordHigh.lsub(ordLow);
		int wcmp = ordRange.compareTo(Ordinal.W);
		if(wcmp <= 0) { // special handling for n or w
			double ixTop = xHigh * xGapBound;
			int ilim = ixTop > xLow?((int) invcf(invflerp(xLow, xHigh, ixTop))):0;
			// handle n
			if(wcmp < 0) {
				ilim = Math.min(ilim, ordRange._nat - 1);
			}
			for(int i = ilim; i >= 0; --i) {
				double iz = cf(i);
				double ix = flerp(xLow, xHigh, iz);
				double iy = yScale * (1 - iz);
				Ordinal io = new Ordinal(ordLow._cnf, ordLow._nat + i);
				Color ic = colorOf(io);
				result.add(new Matchstick(ix, iy, ic));
			}
		} else { // > w, general case
			assert ordRange.kind() == OrdinalKind.LIMIT;
			double ixTop = xHigh * xGapBound;
			if(ixTop > xLow) {
				int n = 0;
				Ordinal cut = ordHigh.fundamental(n);
				while(cut.compareTo(ordLow) <= 0) {
					++n;
					cut = ordHigh.fundamental(n);
				}
				double gzLow = cf(n);
				int ilim = (int) invcf(remap(0, 1, gzLow, 1, invflerp(xLow, xHigh, ixTop)));
				Ordinal ioLow = null;
				for(int i = ilim; i >= n; --i) {
					Ordinal ioHigh = ioLow!=null?ioLow:ordHigh.fundamental(i);
					ioLow = i==0?ordLow:ordHigh.fundamental(i-1);
					double izHigh = remap(gzLow, 1, 0, 1, cf(i+1));
					double izLow = remap(gzLow, 1, 0, 1, cf(i));
					double ixHigh = flerp(xLow, xHigh, izHigh);
					double ixLow = flerp(xLow, xHigh, izLow);
					double iyScale = yScale * (1 - izLow);
					result.addAll(generateDiagramPre(ioLow, ioHigh, iyScale, ixLow, ixHigh, xGap));
				}
			} else {
				// to avoid dropping it entirely, we add 1 matchstick at low
				double iy = yScale;
				double ix = xLow;
				Color ic = colorOf(ordLow);
				result.add(new Matchstick(ix, iy, ic));
			}
		}
		return result;
	}
	
	public static ArrayList<Matchstick>
		generateDiagram(Ordinal ordLow, Ordinal ordHigh,
				double yScale, double xLow, double xHigh, double xGap) {
		ArrayList<Matchstick> result = generateDiagramPre(ordLow, ordHigh, yScale, xLow, xHigh, xGap);
		result.sort(Comparator.comparing(ms -> ms.x));
		double lastx = Double.POSITIVE_INFINITY;
		for(int i = result.size() - 1; i >= 0; --i) {
			Matchstick ms = result.get(i);
			ms.xtonext = lastx - ms.x;
			lastx = ms.x;
		}
		return result;
	}
	
}
