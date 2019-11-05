package editor;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import javax.imageio.ImageIO;

public class NSpriteToPng {

	public static void main(String[] args) {
		searchFolder("C:/users/jesper/documents/github/Neon/res/sprites", "C:/users/jesper/documents/github/Neon/res/images/");
	}
	
	private static void exportFile(String path, String target) {
		try {
			String str = new String(Files.readAllBytes(Paths.get(path)));
			str = str.replaceAll("\r", "");
			ArrayList<Point> points = new ArrayList<Point>();
			String[] lines = str.split("\n");
			float width = Float.parseFloat(lines[0]);
			float height = Float.parseFloat(lines[1]);
			for (int i = 3; i < lines.length; i++) {
				String[] parts = lines[i].split(",");
				float x = Float.parseFloat(parts[0]);
				float y = Float.parseFloat(parts[1]);
				Point p = new Point(x, y);
				points.add(p);
			}
			createImage(points, path, target, (int) width, (int) height);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static void searchFolder(String path, String target) {
		File file = new File(path);
		File[] list = file.listFiles();
		for (int i = 0; i < list.length; i++) {
			if (list[i].isDirectory()) {
				searchFolder(list[i].getAbsolutePath(), target);
			} else if (list[i].getName().endsWith(".nspr")) {
				exportFile(list[i].getAbsolutePath(), target);
			}
		}
	}
	
	private static void createImage(ArrayList<Point> points, String path, String target, int width, int height) {
		String imagePath = target + new File(path).getName().replace(".nspr", ".png");
		BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = (Graphics2D) bi.getGraphics();
		RenderingHints rh = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	    g.setRenderingHints(rh);
		for (int i = 0; i < points.size() - 1; i++) {
			Point from = points.get(i);
			Point to = points.get(i + 1);
			drawLine(g, from, to);
		}
		File outputfile = new File(imagePath);
		try {
			ImageIO.write(bi, "png", outputfile);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	
	private static void drawLine(Graphics g, Point from, Point to) {
		float h = 0.5f;
		float v = (float) Math.atan2(to.getY() - from.getY(), to.getX() - from.getX());
		if (v < 0) {
			v += 2 * Math.PI;
		}
		float u = (float) (Math.PI / 2) + v;
		float x = (float) Math.cos(u) * h;
		float y = (float) Math.sin(u) * h;
		for (int i = -1; i < 2; i++) {
			int fromX = Math.round(from.getX() + i * x);
			int fromY = Math.round(from.getY() + i * y);
			int toX = Math.round(to.getX() + i * x);
			int toY = Math.round(to.getY() + i * y);
			java.awt.Color c = new java.awt.Color(1.0f, 1.0f, 1.0f, (float) (1.0 - (float) Math.abs(i) * 0.2));
			g.setColor(c);
			g.drawLine(fromX, fromY, toX, toY);
		}
	}
}
