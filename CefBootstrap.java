import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;

import fr.altening.utils.Utils;

public class CefBootstrap {

    private static String getCefUrl() {
        switch (Utils.getOSType()) {
            case WINDOWS:
                return "https://github.com/jcefmaven/jcefmaven/releases/download/141.0.10/jcef-natives-windows-amd64-jcef-2caef5a+cef-141.0.10+g1d65b0d+chromium-141.0.7390.123.jar";
            case LINUX:
                return "https://github.com/jcefmaven/jcefmaven/releases/download/141.0.10/jcef-natives-linux-amd64-jcef-2caef5a+cef-141.0.10+g1d65b0d+chromium-141.0.7390.123.jar";
            case MACOS:
                return "https://github.com/jcefmaven/jcefmaven/releases/download/141.0.10/jcef-natives-macosx-amd64-jcef-2caef5a+cef-141.0.10+g1d65b0d+chromium-141.0.7390.123.jar";
            default:
                throw new RuntimeException("Unsupported OS");
        }
    }
    private static File getJcefDir() {
        return new File(Utils.getAppData(), "HonertisLauncher/jcef");
    }
    
    public static void init() {
        try {
            File baseDir = getJcefDir();
            File jarFile = new File(baseDir, "jcef-natives.jar");
            File tarGzFile = new File(baseDir, "jcef-natives.tar.gz");
            File tarFile = new File(baseDir, "jcef-natives.tar");

            baseDir.mkdirs();

            if (!jarFile.exists()) {
                System.out.println("[JCEF] Downloading natives...");
                download(getCefUrl(), jarFile);
            }

            if (!tarGzFile.exists()) {
                extractTarGzFromJar(jarFile, tarGzFile);
            }

            if (!tarFile.exists()) {
                gunzip(tarGzFile, tarFile);
            }

            extractTar(tarFile, baseDir);

            System.out.println("[JCEF] Ready at " + baseDir);

        } catch (Exception e) {
            throw new RuntimeException("JCEF init failed", e);
        }
    }

    private static void extractTarGzFromJar(File jar, File outTarGz) throws IOException {
        try (ZipInputStream zip = new ZipInputStream(new FileInputStream(jar))) {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                if (entry.getName().endsWith(".tar.gz") || entry.getName().endsWith(".tgz")) {
                    try (FileOutputStream fos = new FileOutputStream(outTarGz)) {
                        byte[] buffer = new byte[8192];
                        int len;
                        while ((len = zip.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }
                    return;
                }
            }
        }
        throw new IOException("No .tar.gz found in JCEF jar");
    }
    
    private static void gunzip(File gzip, File tar) throws IOException {
        try (GZIPInputStream gis = new GZIPInputStream(new FileInputStream(gzip));
             FileOutputStream fos = new FileOutputStream(tar)) {

            byte[] buffer = new byte[8192];
            int len;
            while ((len = gis.read(buffer)) > 0) {
                fos.write(buffer, 0, len);
            }
        }
    }
    private static void download(String url, File dest) throws IOException {
    	if (!dest.exists()) {
    		try (InputStream in = new URL(url).openStream()) {
            	Files.copy(in, dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
        	}
    	}
    }
    
    @SuppressWarnings("deprecation")
	private static void extractTar(File tarFile, File outDir) throws IOException {
        try (TarArchiveInputStream tis =
                     new TarArchiveInputStream(new FileInputStream(tarFile))) {

            TarArchiveEntry entry;
            while ((entry = tis.getNextTarEntry()) != null) {
                File out = new File(outDir, entry.getName());

                if (entry.isDirectory()) {
                    out.mkdirs();
                } else {
                    out.getParentFile().mkdirs();
                    try (FileOutputStream fos = new FileOutputStream(out)) {
                        byte[] buffer = new byte[8192];
                        int len;
                        while ((len = tis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }
                }
            }
        }
    }
}
