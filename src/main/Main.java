package main;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import javax.imageio.ImageIO;

public class Main {
	
	public static final double PRUNE_EPS = 0.25;
	public static final int PRUNE_FAC_SHIFT = 4;

	public static void main(String[] args) {
		if(args.length == 0) {
			System.out.println("Usage:\nwidth height frames ordLow ordHigh initZoom\nTips:\nIf you set initial zoom to -1, it will be automatically determined to exactly hit the second matchstick on the first frame.");
			return;
		}
		// parse args
		int width = Integer.valueOf(args[0]);
		int height = Integer.valueOf(args[1]);
		int frames = Integer.valueOf(args[2]);
		Ordinal ordLow = Ordinal.parse(args[3]);
		Ordinal ordHigh = Ordinal.parse(args[4]);
		double initZoom = Double.valueOf(args[5]);
		try {
			System.out.println("Arguments OK");
			// calculate constants
			double cy = height * 0.5f;
			// get matchstick diagram
			double xGap = 1f/width;
			ArrayList<Matchstick> matchsticks =
					Matchstick.generateDiagram(ordLow, ordHigh, 1, 0, 1, xGap);
			System.out.println("Matchsticks: " + matchsticks.size());
			if(initZoom == -1) {
				initZoom = 1 / matchsticks.get(1).x;
				System.out.println("Auto init zoom: " + initZoom);
			}
			// make output dir
			File outputDir = new File("render").getCanonicalFile();
			outputDir.mkdir();
			// render each frame
			for(int frame = 0; frame < frames; ++frame) {
				// calculate constants
				double xScale = width * Math.pow(initZoom, 1 - (double) frame / (frames - 1));
				// make image file
				File imageFile = new File(outputDir, String.format("%06d.png", frame));
				imageFile.createNewFile();
				// make image object
				BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
				// get graphics
				Graphics2D g = (Graphics2D) image.createGraphics();
				// set render hints
				g.setRenderingHint(
				        RenderingHints.KEY_ANTIALIASING,
				        RenderingHints.VALUE_ANTIALIAS_ON);
				// fill the background
				g.setPaint(ColorUtil.background);
				g.fill(new Rectangle2D.Double(0, 0, width, height));
				// draw each matchstick
				int drawn = 0;
				int canPrune = 0;
				for(var ms:matchsticks) {
					double my = ms.y;
					double mx = ms.x;
					double strokeWidth = 1.5 + 1.5 * my;
					my *= cy;
					mx *= xScale;
					if(mx - strokeWidth > width) {
						// clearly out of frame
						break;
					}
					Color mc = ms.color;
					g.setStroke(new BasicStroke((float)strokeWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
					g.setPaint(mc);
					g.draw(new Line2D.Double(mx, cy - my, mx, cy + my));
					if(ms.xtonext * xScale < PRUNE_EPS) { // too small to see
						++canPrune;
					}
					++drawn;
				}
				System.out.println("Writing: " + imageFile);
				// write image file
				ImageIO.write(image, "png", imageFile);
				// if necessary, prune for performance
				if(canPrune > (drawn >>> PRUNE_FAC_SHIFT)) {
					ArrayList<Matchstick> tms = matchsticks;
					matchsticks = new ArrayList<>();
					tms.sort(Comparator.comparing(ms -> ms.x));
					double lastx = Double.POSITIVE_INFINITY;
					for(int i = tms.size() - 1; i >= 0; --i) {
						Matchstick ms = tms.get(i);
						if((i == 0 || ms.y < tms.get(i-1).y) && (lastx - ms.x) * xScale < PRUNE_EPS) {
							continue;
						}
						ms.xtonext = lastx - ms.x;
						lastx = ms.x;
						matchsticks.add(ms);
					}
					Collections.reverse(matchsticks);
					System.out.println("Pruned to: " + matchsticks.size());
				}
			}
		}catch(IOException exc) {
			System.err.println(exc);
		}
	}

}
