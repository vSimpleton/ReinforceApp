package utils;

import java.io.*;

public class DxUtil {

    /**
     * 找到aar中的jar包
     */
    public static File jar2Dex(File aarFile) throws IOException, InterruptedException {
        File fakeDex = new File(aarFile.getParent() + File.separator + "temp");
        // 解压aar到 fakeDex 目录下
        ZipUtil.unZip(aarFile, fakeDex);
        // 过滤找到对应的fakeDex下的classes.jar
        File[] files = fakeDex.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                return s.equals("classes.jar");
            }
        });
        if (files == null || files.length <= 0) {
            throw new RuntimeException("the aar is invalidate");
        }
        File classes_jar = files[0];
        // 将classes.jar 变成classes.dex
        File aarDex = new File(classes_jar.getParentFile(), "classes.dex");

        // 我们要将jar转变成为dex需要使用android tools里面的dx.bat
        // 使用java调用windows下的命令
        dxCommand(aarDex, classes_jar);
        return aarDex;
    }

    /**
     * 通过Android sdk中的dx.bat把aar中的jar包转换成dex
     */
    public static void dxCommand(File aarDex, File classes_jar) throws IOException, InterruptedException {
        Runtime runtime = Runtime.getRuntime();
        Process process = runtime.exec("cmd.exe /C dx --dex --output=" + aarDex.getAbsolutePath() + " " +
                classes_jar.getAbsolutePath());

        try {
            process.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw e;
        }
        if (process.exitValue() != 0) {
            InputStream inputStream = process.getErrorStream();
            int len;
            byte[] buffer = new byte[2048];
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            while ((len = inputStream.read(buffer)) != -1) {
                bos.write(buffer, 0, len);
            }
            throw new RuntimeException("dx run failed");
        }
        process.destroy();
    }

}
