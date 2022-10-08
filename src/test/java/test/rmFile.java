package test;

import java.io.File;

public class rmFile {
    public static void main(String[] args) {
        Boolean flag=false;
        File file = new File("/org/Zor0/ByteCode/nrwMUTsT9m.class");
        // 判断目录或文件是否存在
        if (!file.exists()) {  // 不存在返回 false
            System.out.println(flag);
        } else {
            // 判断是否为文件
            if (file.isFile()) {  // 为文件时调用删除文件方法
                file.delete();
            }
        }
    }
}
