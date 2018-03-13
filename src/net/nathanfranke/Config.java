package net.nathanfranke;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;

enum OperatingSystem {WINDOWS, MAC, LINUX}

public class Config {
	
	public static String configAutoPath = ("Screenshot/");
	public static String keyValueSplitter = ("\t");
	
	public static File getConfigFile (String path) {
		String prePath = getConfigPath();
		return new File(prePath + path);
	}
	
	public static String getConfigPath () {
		OperatingSystem os = getOperatingSystem();
		if(os == null) {
			throw new IllegalStateException("Operating System is null!? Report this bug on GitHub. Your OS: " + System.getProperty("os.name"));
		}
		if(os.equals(OperatingSystem.WINDOWS)) {
			return (System.getProperty("user.home") + "/AppData/Roaming/" + configAutoPath);
		} else if (os.equals(OperatingSystem.MAC)) {
			return (System.getProperty("user.home") + "/Library/Applications/." + configAutoPath); // TODO: Check this
		} else if (os.equals(OperatingSystem.LINUX)) {
			return (System.getProperty("user.home") + "/.config/" + configAutoPath);
		}
		// If you get here, you are a hacker m8
		return null;
	}
	
	public static OperatingSystem getOperatingSystem () {
		String osName = System.getProperty("os.name").toLowerCase();
		if(osName.contains("win")) {
			return OperatingSystem.WINDOWS;
		} else if (osName.contains("osx") || osName.contains("mac")) {
			return OperatingSystem.MAC;
		} else {
			return OperatingSystem.LINUX;
		}
	}
	
	public static void set (String file, String info) {
		File f = getConfigFile(file);
		f.getParentFile().mkdirs();
		try {
			Files.write(f.toPath(), info.getBytes(), new OpenOption[0]);
		} catch (IOException e) {
			
		}
	}
	
	public static String get (String file) {
		File f = getConfigFile(file);
		f.getParentFile().mkdirs();
		try {
			return new String(Files.readAllBytes(f.toPath()));
		} catch (IOException e) {
			
		}
		return ("");
	}
	
	public static String get (String file, String defaultValue) {
		File f = getConfigFile(file);
		f.getParentFile().mkdirs();
		try {
			return new String(Files.readAllBytes(f.toPath()));
		} catch (IOException e) {
			Config.set(file, defaultValue);
		}
		return (defaultValue);
	}
	
	public static int getInt (String file, int defaultValue) {

		try {
			int returns = (Integer.parseInt(Config.get(file)));
			return returns;
		} catch (NumberFormatException e) {
			Config.set(file, String.valueOf(defaultValue));
			return defaultValue;
		}
		
	}
	
	public static float getFloat (String file, float defaultValue) {

		try {
			float returns = (Float.parseFloat(Config.get(file)));
			return returns;
		} catch (NumberFormatException e) {
			Config.set(file, String.valueOf(defaultValue));
			return defaultValue;
		}
		
	}
	
}
