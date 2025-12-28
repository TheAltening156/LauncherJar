package fr.altening.utils;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import org.apache.commons.io.FileUtils;

public class Utils {

	public static boolean isValidMinecraftUsername(String username) {
		return username.matches("^[a-zA-Z0-9_]{3,16}$");
	}

	public static void download(String url, File directory, String name) {
		try {
			saveFileFromUrlWithCommonsIO(String.valueOf(directory.toString()) + "/" + name, url);
			System.out.println("Finished downloading " + name);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void saveFileFromUrlWithCommonsIO(String fileName, String fileUrl)
			throws MalformedURLException, IOException {
		FileUtils.copyURLToFile(new URL(fileUrl), new File(fileName));
	}

	public static boolean openWebpage(URI uri) {
		Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
		if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE))
			try {
				desktop.browse(uri);
				return true;
			} catch (Exception e) {
				e.printStackTrace();
			}
		return false;
	}

	public enum EnumOS {
		WINDOWS, MACOS, LINUX, SOLARIS, UNKNOWN;
	}

	public static EnumOS getOSType() {
		String s = System.getProperty("os.name").toLowerCase();
		return s.contains("win") ? EnumOS.WINDOWS
				: (s.contains("mac") ? EnumOS.MACOS
				: (s.contains("solaris") ? EnumOS.SOLARIS
				: (s.contains("sunos") ? EnumOS.SOLARIS
				: (s.contains("linux") ? EnumOS.LINUX
				: (s.contains("unix") ? EnumOS.LINUX 
				: EnumOS.UNKNOWN)))));
	}

	public static File getAppData() {
		EnumOS os = getOSType();
		if (os == EnumOS.WINDOWS)
			return new File(System.getenv("APPDATA"));
		if (os == EnumOS.MACOS)
			return new File(System.getProperty("user.home") + "/Library/Application Support");
		return new File(System.getProperty("user.home"));
	}
}
