package fr.altening.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.apache.commons.io.FileUtils;

public class Utils {

	public static boolean isValidMinecraftUsername(String username) {
		return username.matches("^[a-zA-Z0-9_]{3,16}$");
	}

	public static void download(String url, File dest) throws IOException{
		try (InputStream in = new URL(url).openStream()) {
			Files.copy(in, dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
		}
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
