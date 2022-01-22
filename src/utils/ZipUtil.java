package utils;

import java.io.*;
import java.util.Enumeration;
import java.util.zip.*;

public class ZipUtil {

    /**
     * 解压apk文件
     */
    public static void unZip(File srcFile, File dstFile) {
        try {
            ZipFile zipFile = new ZipFile(srcFile);
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry zipEntry = entries.nextElement();
                String name = zipEntry.getName();

                if (name.equals("META-INF/CERT.RSA") || name.equals("META-INF/CERT.SF") || name
                        .equals("META-INF/MANIFEST.MF")) {
                    continue;
                }

                if (!zipEntry.isDirectory()) {
                    File file = new File(dstFile, name);
                    if (!file.getParentFile().exists()) {
                        file.getParentFile().mkdirs();
                    }
                    FileOutputStream fos = new FileOutputStream(file);
                    InputStream is = zipFile.getInputStream(zipEntry);
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = is.read(buffer)) != -1) {
                        fos.write(buffer, 0, len);
                    }
                    is.close();
                    fos.close();
                }
            }
            zipFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 压缩文件，打包apk
     */
    public static void zip(File src, File dst) throws Exception {
        dst.delete();
        CheckedOutputStream cos = new CheckedOutputStream(new FileOutputStream(dst), new CRC32());
        ZipOutputStream zos = new ZipOutputStream(cos);
        compress(src, zos, "");
        zos.flush();
        zos.close();
    }

    private static void compress(File srcFile, ZipOutputStream zos, String basePath) throws Exception {
        if (srcFile.isDirectory()) {
            compressDir(srcFile, zos, basePath);
        } else {
            compressFile(srcFile, zos, basePath);
        }
    }

    private static void compressDir(File src, ZipOutputStream zos, String basePath) throws Exception {
        File[] files = src.listFiles();
        if (files.length < 1) {
            ZipEntry entry = new ZipEntry(basePath + src.getName() + File.separator);
            zos.putNextEntry(entry);
            zos.closeEntry();
        }
        for (File file : files) {
            compress(file, zos, basePath + src.getName() + File.separator);
        }
    }

    private static void compressFile(File file, ZipOutputStream zos, String dir) throws Exception {
        String dirName = dir + file.getName();
        String[] dirNameNew = dirName.split("\\\\");
        StringBuffer buffer = new StringBuffer();

        if (dirNameNew.length > 1) {
            for (int i = 1; i < dirNameNew.length; i++) {
                buffer.append(File.separator);
                buffer.append(dirNameNew[i]);

            }
        } else {
            buffer.append(File.separator);
        }

        ZipEntry entry = new ZipEntry(buffer.toString().substring(1));

        // 适配Android 11，在Android 11上，resources.arsc不要压缩，并且要进行4字节对齐
        if ("resources.arsc".equals(file.getName())) {
            entry.setMethod(ZipEntry.STORED);
            entry.setSize(file.length());
            entry.setCrc(calFileCRC32(file));
        }

        zos.putNextEntry(entry);
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
        int count;
        byte data[] = new byte[1024];
        while ((count = bis.read(data, 0, 1024)) != -1) {
            zos.write(data, 0, count);
        }
        bis.close();
        zos.closeEntry();
    }

    private static long calFileCRC32(File file) throws IOException {
        FileInputStream fi = new FileInputStream(file);
        CheckedInputStream checksum = new CheckedInputStream(fi, new CRC32());
        long temp = checksum.getChecksum().getValue();
        fi.close();
        checksum.close();
        return temp;
    }

}
