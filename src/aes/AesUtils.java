package aes;

import org.springframework.util.StringUtils;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import java.math.BigInteger;
import java.security.MessageDigest;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/**
 * @author 0
 * @date 2018/1/4 8:19
 * @since 1.0
 */
public class AesUtils
{
    /**
     * @author: lwz
     * @description: AES 加密
     * @date: 13:08 2018/1/17
     */
    public static String aesEncrypt(String str, String key) throws Exception {
        if (StringUtils.isEmpty(str) || StringUtils.isEmpty(key)){
            return "";
        }
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key.getBytes("utf-8"), "AES"));
        byte[] bytes = cipher.doFinal(str.getBytes("utf-8"));
        return new BASE64Encoder().encode(bytes);
    }

    /**
     * MD5加密
     * @param encryptStr 加密字符串
     * @return
     */
    public static String md5(String encryptStr){
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("MD5");
            md.update(encryptStr.getBytes("utf-8"));
            byte[] bPwd=md.digest();
            String pwd = new BigInteger(1, bPwd) .toString(16);
            if(pwd.length()%2==1){
                pwd = "0" + pwd;
            }
            return pwd;
        } catch (Exception var3) {
            throw new RuntimeException(var3);
        }
    }
}
