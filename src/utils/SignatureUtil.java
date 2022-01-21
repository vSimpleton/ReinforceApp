package utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

public class SignatureUtil {

    public static void signature(File unsignedApk, File signedApk) throws IOException {
        // 执行签名相关的命令行
        // TODO .keystore路径要修改为自己的签名文件路径
        String cmd[] = {"cmd.exe", "/C ", "apksigner", "-sigalg", "MD5withRSA",
                "-digestalg", "SHA1",
                "-keystore", "D:/ASProjects/ReinforceApp/key.keystore",
                "-storepass", "android",
                "-keypass", "android",
                "-signedjar", signedApk.getAbsolutePath(),
                unsignedApk.getAbsolutePath(),
                "androiddebugkey"};
        Process process = Runtime.getRuntime().exec(cmd);

        if (process.exitValue() != 0) {
            int len;
            byte[] buffer = new byte[2048];
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            while ((len = process.getErrorStream().read(buffer)) != -1) {
                bos.write(buffer, 0, len);
            }
            throw new RuntimeException("签名执行失败");
        }
        process.destroy();
    }

}
