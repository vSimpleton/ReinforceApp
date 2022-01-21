package utils;

import java.io.File;
import java.io.RandomAccessFile;

public class ByteUtil {

    /**
     * 读取文件并输出为byte数组
     */
    public static byte[] getBytes(File dexFile) throws Exception {
        // "r"表示只读
        RandomAccessFile fis = new RandomAccessFile(dexFile, "r");
        byte[] buffer = new byte[(int)fis.length()];
        fis.readFully(buffer);
        fis.close();
        return buffer;
    }

}
