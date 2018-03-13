package net.nathanfranke;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

import net.dv8tion.Uploader;

public class Main {
	
	public static Point mouseStart;

	public static int borderSize = 2;
	public static int selectEdge = 2;
	
	public static JFrame frame;
	public static JPanel drag;
	
	public static Robot robot;
	
	public static boolean xclip;
	public static boolean capture;
	public static boolean autoClose;
	
	public static boolean openCaptured;
	
	public static Rectangle last = null;
	
	public static void main (String[] args) {
		
		String argsCombinedTMP = (" ");
		for (int i = 0; i < args.length; i++) {
			argsCombinedTMP+=(args[i] + " ");
		}
		final String argsCombined = argsCombinedTMP;
		
		int delay = Config.getInt("openDelay", 0);
		
		if(delay == 0) {
			
			init(argsCombined);
			
		} else {
			
			new Timer().schedule(new TimerTask() {
				
				@Override
				public void run() {
					
					init(argsCombined);
					
				}
				
			}, delay);
			
		}
		
	}
	
	public static boolean openCapturedOnce;
	
	private static final String DEFAULT_BORDER = ("#0000FF");
	private static final String DEFAULT_PANEL = ("#FF6666");
	private static final String DEFAULT_MASK = ("#000000");
	
	public static void init (String argsCombined) {
		
		System.out.println("Program Starting");
		
		Config.getInt("modifiers/capturelastclipboard", -1);
		Config.getInt("modifiers/capturelastimgur", -1);
		Config.getInt("modifiers/openimage", -1);
		Config.getInt("deleteTime", 3600);
		Config.getInt("openDelay", 0);
		Config.get("theme/colors/selection/border", DEFAULT_BORDER);
		Config.get("theme/colors/selection/panel", DEFAULT_PANEL);
		Config.getFloat("theme/colors/opacity/mask", 0.25f);
		Config.get("theme/colors/mask", DEFAULT_MASK);
		Config.get("hashes");
		Config.getInt("captureDelay", 0);
		
		borderSize = Config.getInt("theme/borderSize", 2);
		selectEdge = Config.getInt("theme/selectEdge", 3);
		
		if(argsCombined.contains(" --help ")) {
			
			System.out.print("" + 
					"-------------------------- Traditional Commands --------------------------\n" + 
					"--auto-capture	   - Automatically start capturing a screenshot\n" + 
					"--auto-close	   - Automatically close the program after capture\n" + 
					"--startup		   - Attempt to copy the program to startup (With args)\n" + 
					"--delete-uploaded - Delete all images uploaded to Imgur\n" + 
					"--open-captured   - Open an image dialog once captured\n" + 
					"--open-config	   - Opens the Config Directory\n" + 
					"\n" + 
					"----------------------------- Force Commands -----------------------------\n" + 
					"--xclip		   - Use xclip for clipboard\n" + 
					"\n" + 
			"");
			
			System.exit(0);
			
		}
		
		if(argsCombined.contains(" --open-config ")) {
			
			try {
				Desktop.getDesktop().open(new File(Config.getConfigPath()));
			} catch (IOException e) {
				JOptionPane.showMessageDialog(null, e.getMessage());
				e.printStackTrace();
			}
			
			System.exit(0);
			
		}
		
		ImageDeleter.checkDeletion(argsCombined.contains(" --delete-uploaded "));
		
		LogManager.getLogManager().reset();
		
		Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
		logger.setLevel(Level.OFF);
		
		try {
			GlobalScreen.registerNativeHook();
		} catch (NativeHookException e) {
			e.printStackTrace();
		}
		
		screenBounds = getScreenBounds();
		fullMonitorSize = getFullMonitorSize();
		lowestMonitorPoint = getLowestMonitorPoint();
		mouseOffset = getMouseOffset();

		autoClose = argsCombined.contains(" --auto-close ");
		capture = argsCombined.contains(" --auto-capture ");
		xclip = argsCombined.contains(" --xclip ") || new File("/usr/bin/xclip").exists();
		openCaptured = argsCombined.contains(" --open-captured ");
		
		if(argsCombined.contains(" --startup ")) {
			String os = System.getProperty("os.name").toLowerCase();
			String home = System.getProperty("user.home");
			String args2 = argsCombined.replace(" --startup ", " ");
			args2 = args2.substring(0, args2.length()-1);
			if(os.equals("windows")) {
				String path = (home + "\\AppData\\Roaming\\Microsoft\\Windows\\Start Menu\\Programs\\Startup");
				String path2 = (home + "\\AppData\\LocalLow\\Screenshot");
				new File(path).getParentFile().mkdirs();
				new File(path2).getParentFile().mkdirs();
				if(!new File(path2 + "/screenshot.jar").exists()) {
					try {
						Files.copy(getWorkingFile().toPath(), new File(path2 + "/screenshot.jar").toPath(), new CopyOption[0]);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				if(!new File(path + "/screenshot.bat").exists()) {
					String cmd = ("java -jar " + home + "\\AppData\\LocalLow\\Screenshot\\screenshot.jar" + args2);
					try {
						Files.write(new File(path + "/screenshot.bat").toPath(), cmd.getBytes(), new OpenOption[0]);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				JOptionPane.showMessageDialog(null, "Copied program to startup! It is located at " + path);
				System.exit(0);
			} else if(os.equals("linux") || os.equals("unix")) {
				String path = (home + "/.local/bin/screenshot");
				new File(path).mkdirs();
				if(new File(path + "/screenshot.jar").exists()) {
					new File(path + "/screenshot.jar").delete();
				}
				try {
					Files.copy(getWorkingFile().toPath(), new File(path + "/screenshot.jar").toPath(), new CopyOption[0]);
				} catch (IOException e) {
					e.printStackTrace();
				}
				String session = System.getenv("DESKTOP_SESSION");
				String cmd = ("if [ \"$DESKTOP_SESSION\" == \"" + session + "\" ]; then\n    java -jar " + home + "/.local/bin/screenshot/screenshot.jar" + args2 + "\nfi");
				File profile = new File(home + "/.profile");
				try {
					String current = new String(Files.readAllBytes(profile.toPath()));
					current = current.replace(cmd, "");
					while (current.endsWith("\n")) {
						current = current.substring(0, current.length()-1);
					}
					current+=("\n\n" + cmd + "\n");
					Files.write(profile.toPath(), current.getBytes(), new OpenOption[0]);
				} catch (IOException e) {
					e.printStackTrace();
				}
				JOptionPane.showMessageDialog(null, "Copied program to startup! It is located at ~/.local/bin/screenshot/screenshot.jar and was added to ~/.profile");
				System.exit(0);
			}
		}
		
		try {
			robot = new Robot();
		} catch (AWTException e) {
			e.printStackTrace();
		}
		
		GlobalScreen.addNativeKeyListener(new NativeKeyListener() {
			
			@Override public void nativeKeyTyped(NativeKeyEvent event) { }
			
			@Override public void nativeKeyReleased(NativeKeyEvent event) { }
			
			@Override
			public void nativeKeyPressed(NativeKeyEvent event) {
				
				if(event.getKeyCode() == 1) {
					if(frame != null) {
						stopCapture();
					}
				}
				
				if(event.getKeyCode() == 3639) {
					if(event.getModifiers() == Config.getInt("modifiers/capturelastclipboard", -1)) {
						if(last != null) {
							BufferedImage image = Capture.capture(last);
							copyImage(image);
						}
					} else if (event.getModifiers() == Config.getInt("modifiers/capturelastimgur", -1)) {
						if(last != null) {
							BufferedImage image = Capture.capture(last);
							uploadImage(image);
						}
					} else {
						if(frame == null) {
							openCapturedOnce = false;
							if(event.getModifiers() == Config.getInt("modifiers/openimage", -1)) {
								openCapturedOnce = true;
							}
							capture();
						} else {
							stopCapture();
						}
					}
				}
				
			}
			
		});
		
		if(capture) {
			capture();
		}
		
	}
	
	public static ArrayList<Rectangle> screenBounds;

	public static ArrayList<Rectangle> getScreenBounds() {

		ArrayList<Rectangle> bounds = new ArrayList<Rectangle>();

		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice gds[] = ge.getScreenDevices();

		for (GraphicsDevice gd : gds) {
			GraphicsConfiguration gc = gd.getDefaultConfiguration();
			Rectangle screenBounds = gc.getBounds();
			bounds.add(screenBounds);
		}

		return bounds;
	}
	
	public static Dimension fullMonitorSize;
	
	public static Dimension getFullMonitorSize () {
		
		Dimension returns = new Dimension();
		
		for (int i = 0; i < screenBounds.size(); i++) {
			returns.setSize(returns.width+screenBounds.get(i).width, returns.height+screenBounds.get(i).height);
		}
		
		return returns;
		
	}
	
	public static Point lowestMonitorPoint;
	
	public static Point getLowestMonitorPoint () {
		
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice gd = ge.getDefaultScreenDevice();
		
		Rectangle rect = gd.getDefaultConfiguration().getBounds();
		
		Point returns = new Point(-rect.x, -rect.y);
		
		return returns;
		
	}
	
	public static Point mouseOffset;
	
	public static Point getMouseLocation () {
		Point mPos = MouseInfo.getPointerInfo().getLocation();
		return new Point(mPos.x+mouseOffset.x, mPos.y+mouseOffset.y);
	}
	
	public static Point getMouseOffset () {
		
		int xOffset = 0;
		int yOffset = 0;
		for (Rectangle bounds : screenBounds) {
			if (bounds.x < xOffset) {
				xOffset = bounds.x;
			}
			if (bounds.y < yOffset) {
				yOffset = bounds.y;
			}
		}
		if (xOffset < 0) {
			xOffset *= -1;
		}
		if (yOffset < 0) {
			yOffset *= -1;
		}
		
		return new Point(xOffset, yOffset);
		
	}
	
	public static void stopCapture () {
		
		drag = null;
		frame.dispose();
		frame = null;
		
	}
	
	public static void capture () {
		
		Rectangle bounds = new Rectangle(lowestMonitorPoint.x, lowestMonitorPoint.y, fullMonitorSize.width, fullMonitorSize.height);
		
		frame = new JFrame("Screenshot");
		frame.setLayout(null);
		frame.setType(javax.swing.JFrame.Type.UTILITY);

		frame.getContentPane().setBackground(Color.decode(Config.get("theme/colors/mask", DEFAULT_MASK)));
		frame.setBackground(Color.decode(Config.get("theme/colors/mask", DEFAULT_MASK)));
		
		frame.setBounds(bounds);
		frame.setUndecorated(true);
		frame.setOpacity(Config.getFloat("theme/colors/opacity/mask", 0.25f));
		
		//frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		drag = new JPanel();
		
		drag.setBackground(Color.decode(Config.get("theme/colors/selection/panel", DEFAULT_PANEL)));
		drag.setBorder(BorderFactory.createMatteBorder(borderSize, borderSize, borderSize, borderSize, Color.decode(Config.get("theme/colors/selection/border", DEFAULT_BORDER))));
		
		frame.add(drag);
		
		drag.setLocation(0, 0);
		drag.setSize(0, 0);

		drag.setVisible(false);
		
		frame.setVisible(true);
		
		frame.addMouseListener(new MouseListener() {
			
			@Override
			public void mousePressed(MouseEvent e) {
				
				mouseStart = e.getPoint();
				
				drag.setVisible(false);
				
			}
			
			@Override
			public void mouseReleased(MouseEvent e) {
				
				stopCapture();
				
				if(e.getButton() == 1 || e.getButton() == 3) {
					
					Dimension size = new Dimension(e.getPoint().x-mouseStart.x, e.getPoint().y-mouseStart.y);
					if(size.width == 0) size.width = 1;
					if(size.height == 0) size.height = 1;
					
					boolean horizontalNegative = size.getWidth() < 0;
					boolean verticalNegative = size.getHeight() < 0;
					Dimension newSize = new Dimension();
					Point newLocation = new Point();
					if(horizontalNegative) {
						newLocation.x = e.getPoint().x;
						newSize.width = -size.width;
					} else {
						newLocation.x = mouseStart.x;
						newSize.width = size.width;
					}
					if(verticalNegative) {
						newLocation.y = e.getPoint().y;
						newSize.height = -size.height;
					} else {
						newLocation.y = mouseStart.y;
						newSize.height = size.height;
					}
					
					int delay = Config.getInt("captureDelay", 0);
					
					if(delay == 0) {
						
						Rectangle rect = new Rectangle(newLocation.x, newLocation.y, newSize.width, newSize.height);
						
						BufferedImage image = Capture.capture(rect);
						
						openCapturedOnce = false;
						
						if(e.getButton() == 1) {
							copyImage(image);
						} else {
							uploadImage(image);
						}
						
						if(autoClose) {
							System.exit(-1);
						}
						
						System.out.println("Captured! x=" + (int)rect.getX() + ", y=" + (int)rect.getY() + "; w=" + (int)rect.getWidth() + ", h=" + (int)rect.getHeight());
						
					} else {
						
						System.out.println("Capturing in " + delay + "ms");
						
						new Timer().schedule(new TimerTask() {
							
							@Override
							public void run() {
								
								Rectangle rect = new Rectangle(newLocation.x, newLocation.y, newSize.width, newSize.height);
								
								BufferedImage image = Capture.capture(rect);
								
								openCapturedOnce = false;
								
								if(e.getButton() == 1) {
									copyImage(image);
								} else {
									uploadImage(image);
								}
								
								if(autoClose) {
									System.exit(-1);
								}

								System.out.println("Captured! x=" + (int)rect.getX() + ", y=" + (int)rect.getY() + "; w=" + (int)rect.getWidth() + ", h=" + (int)rect.getHeight());
								
							}
							
						}, delay);
						
					}
					
					mouseStart = null;
					
				}
				
			}
			
			@Override public void mouseExited(MouseEvent e) { }
			@Override public void mouseEntered(MouseEvent e) { }
			@Override public void mouseClicked(MouseEvent e) { }
			
		});
		
		frame.addMouseMotionListener(new MouseMotionListener() {
			
			@Override
			public void mouseDragged(MouseEvent e) {
				
				if(mouseStart != null) {
					Dimension size = new Dimension(e.getPoint().x-mouseStart.x, e.getPoint().y-mouseStart.y);
					if(size.width != 0) {
						if(size.height != 0) {
							boolean horizontalNegative = size.getWidth() < 0;
							boolean verticalNegative = size.getHeight() < 0;
							Dimension newSize = new Dimension();
							Point newLocation = new Point();
							if(horizontalNegative) {
								newLocation.x = e.getPoint().x-selectEdge;
								newSize.width = -size.width+selectEdge*2;
							} else {
								newLocation.x = mouseStart.x-selectEdge;
								newSize.width = size.width+selectEdge*2;
							}
							if(verticalNegative) {
								newLocation.y = e.getPoint().y-selectEdge;
								newSize.height = -size.height+selectEdge*2;
							} else {
								newLocation.y = mouseStart.y-selectEdge;
								newSize.height = size.height+selectEdge*2;
							}
							
							drag.setLocation(newLocation);
							drag.setSize(newSize);
							
							drag.setVisible(true);
							
						}
					}
				}
				
			}
			
			@Override public void mouseMoved(MouseEvent e) { }
			
		});
		
	}
	
	public static String tmpDir = System.getProperty("java.io.tmpdir") + "/Screenshots";
	
	public static void uploadImage (BufferedImage image) {
		String url = ("https://i.imgur.com/XXXXX.png");
		File f = new File(tmpDir + "/Screenshot_" + UUID.randomUUID().toString().replaceAll("-", "") + ".png");
		try {
			f.getParentFile().mkdirs();
			ImageIO.write(image, "PNG", f);
		} catch (IOException e) {
			e.printStackTrace();
		}
		String result = (Uploader.upload(f));
		url = result.substring(result.indexOf("\"link\":")).split("\"")[3].replaceAll("\\\\/", "/");
		String hash = result.substring(result.indexOf("\"deletehash\":")).split("\"")[3].replaceAll("\\\\/", "/");
		if(xclip) {
			f = new File(tmpDir + "/Screenshot_" + UUID.randomUUID().toString().replaceAll("-", "") + ".png");
			f.getParentFile().mkdirs();
			try {
				Files.write(f.toPath(), url.getBytes(), new OpenOption[0]);
			} catch (IOException e) {
				e.printStackTrace();
			}
			String command = ("xclip -selection clipboard -i " + f.getAbsolutePath());
			try {
				Runtime.getRuntime().exec(command);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			Clipboard c = Toolkit.getDefaultToolkit().getSystemClipboard();
			StringSelection data = new StringSelection(url); 
			c.setContents(data, data);
		}
		ImageDeleter.addHashForFutureDeletion(hash);
	}
	
	public static void copyImage (BufferedImage image) {
		if(xclip) {
			File f = new File(tmpDir + "/Screenshot_" + UUID.randomUUID().toString().replaceAll("-", "") + ".png");
			f.getParentFile().mkdirs();
			try {
				ImageIO.write(image, "PNG", f);
			} catch (IOException e) {
				e.printStackTrace();
			}
			String command = ("xclip -selection clipboard -t image/png -i " + f.getAbsolutePath());
			try {
				Runtime.getRuntime().exec(command);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			TransferableImage trans = new TransferableImage(image);
			Clipboard c = Toolkit.getDefaultToolkit().getSystemClipboard();
			c.setContents(trans, new ClipboardOwner() {
				@Override public void lostOwnership(Clipboard clipboard, Transferable contents) {
					
				}
			});
		}
	}
	
	public static File getWorkingFile () {
		try {
			return new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return null;
	}
	
}

class TransferableImage implements Transferable {

	Image i;

	public TransferableImage(Image i) {
		this.i = i;
	}

	public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
		if (flavor.equals( DataFlavor.imageFlavor) && i != null) {
			return i;
		}
		else {
			throw new UnsupportedFlavorException(flavor);
		}
	}

	public DataFlavor[] getTransferDataFlavors() {
		DataFlavor[] flavors = new DataFlavor[ 1 ];
		flavors[ 0 ] = DataFlavor.imageFlavor;
		return flavors;
	}

	public boolean isDataFlavorSupported( DataFlavor flavor ) {
		DataFlavor[] flavors = getTransferDataFlavors();
		for ( int i = 0; i < flavors.length; i++ ) {
			if ( flavor.equals( flavors[ i ] ) ) {
				return true;
			}
		}

		return false;
	}
	
}