package utils;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileOutputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class AESUtil {

    public static final String DEFAULT_PWD = "abcdefghijklmnop";
    private static final String ALGORITHM_STR = "AES/ECB/PKCS5Padding";

    private static Cipher mEncryptCipher; // 加密Cipher
    private static Cipher mDecryptCipher; // 解密Cipher

    public static void init(String password) {
        try {
            mEncryptCipher = Cipher.getInstance(ALGORITHM_STR);
            mDecryptCipher = Cipher.getInstance(ALGORITHM_STR);
            byte[] keyByte = password.getBytes();
            // 生成一个AES对称秘钥
            SecretKeySpec key = new SecretKeySpec(keyByte, "AES");
            mEncryptCipher.init(Cipher.ENCRYPT_MODE, key);
            mDecryptCipher.init(Cipher.DECRYPT_MODE, key);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException e) {
            e.printStackTrace();
        }
    }

    /**
     * 加密dex文件并返回新的dex文件
     *
     * @param srcApkFile 需要加固的apk路径
     * @param dstApkFile 解压的目标路径
     */
    public static void encryptAPKFile(File srcApkFile, File dstApkFile) throws Exception {
        if (srcApkFile == null) {
            System.out.println("encryptAPKFile :srcAPKfile null");
            return;
        }

        ZipUtil.unZip(srcApkFile, dstApkFile);
        // 过滤拿到所有dex文件
        File[] dexFiles = dstApkFile.listFiles((dir, name) -> name.endsWith(".dex"));

        for (File dexFile : dexFiles) {
            // 读取数据
            byte[] buffer = ByteUtil.getBytes(dexFile);
            // 拿到读取的数据并加密
            byte[] encryptBytes = encrypt(buffer);
            // 写数据--替换原来文件中的数据
            FileOutputStream fos = new FileOutputStream(dexFile);
            if (encryptBytes != null) {
                fos.write(encryptBytes);
            }
            fos.flush();
            fos.close();
        }
    }

    /**
     * 对数据进行加密
     * @param bytes 需要加密的数据
     */
    public static byte[] encrypt(byte[] bytes) {
        try {
            return mEncryptCipher.doFinal(bytes);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 对数据进行解密
     * @param bytes 需要解密的数据
     */
    public static byte[] decrypt(byte[] bytes) {
        try {
            return mDecryptCipher.doFinal(bytes);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
        }
        return null;
    }

}
