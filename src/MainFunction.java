import utils.*;

import java.io.File;
import java.io.FileOutputStream;

public class MainFunction {

    public static void main(String[] args) throws Exception {

        // 创建源dex解压后的文件路径
        File tempFileApk = new File("source/apk/temp");
        // 如果该路径已存在，则把该路径下的内容清空
        if (tempFileApk.exists()) {
            File[] files = tempFileApk.listFiles();
            for (File file : files) {
                if (file.isFile()) {
                    file.delete();
                }
            }
        }

        // 创建壳dex解压后的文件路径
        File tempFileAar = new File("source/aar/temp");
        // 如果该路径已存在，则把该路径下的内容清空
        if (tempFileAar.exists()) {
            File[] files = tempFileAar.listFiles();
            for (File file : files) {
                if (file.isFile()) {
                    file.delete();
                }
            }
        }

        // 第一步：拿到需要加固的apk，对dex进行加密（这里用的是AES对称加密）
        AESUtil.init(AESUtil.DEFAULT_PWD);
        // 解压apk，目录以及apk名字可自行更改
        File apkFile = new File("source/apk/app-release.apk");
        File newApkFile = new File(apkFile.getParent() + File.separator + "temp");
        if(!newApkFile.exists()) {
            newApkFile.mkdirs();
        }
        AESUtil.encryptAPKFile(apkFile, newApkFile);


        // 第二步：处理aar，获得壳dex，壳dex不加密
        File aarFile = new File("source/aar/base-release.aar");
        File aarDex  = DxUtil.jar2Dex(aarFile);

        // 在上面的apk解压目录下创建一个新的文件，并把壳dex写入到这个文件中
        File tempMainDex = new File(newApkFile.getPath() + File.separator + "classes0.dex");
        if (!tempMainDex.exists()) {
            tempMainDex.createNewFile();
        }
        FileOutputStream fos = new FileOutputStream(tempMainDex);
        byte[] bytes = ByteUtil.getBytes(aarDex);
        fos.write(bytes);
        fos.flush();
        fos.close();


        // 第三步：打包并签名
        File unsignedApk = new File("result/apk-unsigned.apk");
        if (!unsignedApk.getParentFile().exists()) {
            unsignedApk.getParentFile().mkdirs();
        }
        ZipUtil.zip(newApkFile, unsignedApk);

        // 使用原来的签名文件进行签名
        File signedApk = new File("result/apk-signed.apk");
        SignatureUtil.v2Signature(unsignedApk, signedApk, "key.keystore", "123456", "key0", "123456");

    }

}
