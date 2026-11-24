package com.evidencevault.util;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.*;

public final class HashUtil {
    private HashUtil(){}

    public static String sha256(InputStream in) throws IOException {
        try {
            MessageDigest md=MessageDigest.getInstance("SHA-256");
            byte[] buf=new byte[8192]; int n;
            while((n=in.read(buf))!=-1) md.update(buf,0,n);
            return hex(md.digest());
        } catch(NoSuchAlgorithmException e){throw new IllegalStateException("SHA-256 unavailable",e);}
    }

    public static String sha256(File file)throws IOException{
        try(InputStream in=new BufferedInputStream(new FileInputStream(file))){return sha256(in);}
    }

    public static String sha256(String value) {
        try {
            MessageDigest md=MessageDigest.getInstance("SHA-256");
            return hex(md.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }

    private static String hex(byte[] bytes){
        StringBuilder s=new StringBuilder(bytes.length*2);
        for(byte b:bytes) s.append(String.format("%02x",b));
        return s.toString();
    }
}
