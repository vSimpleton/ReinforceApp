package utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class SignatureUtil {

    /**
     * 执行命令行并校验执行结果
     */
    private static void exec(String[] cmd, String execName) throws IOException, InterruptedException {
        Process process = Runtime.getRuntime().exec(cmd);
        System.out.println("start " + execName);
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
            throw new RuntimeException(execName + " execute fail");
        }
        System.out.println("finish " + execName);
        process.destroy();
    }

    /**
     * V1签名
     */
    private static String v1Signature(File unsignedApk, String keyStore, String keyPwd, String alias, String alisaPwd)
            throws InterruptedException, IOException {
        String path = unsignedApk.getAbsolutePath();
        String v1Name = path.substring(0, path.indexOf(".apk")) + "_v1.apk";
        String cmd[] = { "cmd.exe", "/C ", "jarsigner", "-sigalg", "SHA1withRSA", "-digestalg", "SHA1", "-keystore",
                keyStore, "-storepass", keyPwd, "-keypass", alisaPwd, "-signedjar", v1Name,
                unsignedApk.getAbsolutePath(), alias };

        exec(cmd, "v1 sign");

        if (unsignedApk.exists()) {
            unsignedApk.delete();
        }

        return v1Name;
    }

    /**
     * 对v1签名过后的apk进行对齐操作
     * zipalign -p 4 input/app-release-unsigned.apk output/app-release-unsigned_align.apk
     */
    private static String apkZipalign(String v1Apk) throws IOException, InterruptedException {
        String zipalignName = v1Apk.substring(0, v1Apk.indexOf(".apk")) + "_align.apk";
        String cmd[] = {"cmd.exe", "/C ", "zipalign", "-p", "4", v1Apk, zipalignName};

        exec(cmd, "zipalign");

        new File(v1Apk).delete();

        return zipalignName;
    }

    /**
     * V2签名
     */
    public static void v2Signature(File unsignedApk, File signedApk, String keyStore, String keyPwd, String alias, String alisaPwd) throws IOException, InterruptedException {
        String v1Name = v1Signature(unsignedApk, keyStore, keyPwd, alias, alisaPwd);
        String zipalignName = apkZipalign(v1Name);
        String cmd[] = { "cmd.exe", "/C ", "apksigner", "sign",
                "--ks", keyStore,
                "--ks-pass", "pass:" + keyPwd,
                "--ks-key-alias", alias,
                "--key-pass", "pass:" + alisaPwd,
                "--out", signedApk.getAbsolutePath(), zipalignName
        };

        exec(cmd, "v2 sign");

        new File(zipalignName).delete();
        File idsigFile = new File(signedApk.getAbsolutePath() + "idsig");
        if (idsigFile.exists()) {
            idsigFile.delete();
        }

    }

}
