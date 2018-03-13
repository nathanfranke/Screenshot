package net.nathanfranke;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;

public class Capture {

	public static BufferedImage capture (Rectangle bounds) {
		
		Main.last = bounds;
		
		Rectangle fullScreenshot = new Rectangle(0, 0, 0, 0);
		for (GraphicsDevice gd : GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()) {
		    fullScreenshot = fullScreenshot.union(gd.getDefaultConfiguration().getBounds());
		}
		BufferedImage fullScreenshotImage = Main.robot.createScreenCapture(fullScreenshot);
		BufferedImage scr = fullScreenshotImage.getSubimage(bounds.x, bounds.y, bounds.width, bounds.height);
		
		if(Main.openCaptured || Main.openCapturedOnce) {
			
			JDialog dialog = new JDialog();
			
			dialog.setTitle("Capture");
			
			dialog.setSize(bounds.width, bounds.height);
			dialog.setLocationRelativeTo(null);
			
			dialog.add(new JLabel(new ImageIcon(scr)));
			
			dialog.setVisible(true);
			
		}
		
		return scr;
		
	}
	
}
