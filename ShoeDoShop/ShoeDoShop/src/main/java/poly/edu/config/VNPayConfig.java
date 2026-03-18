package poly.edu.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

@Configuration(proxyBeanMethods = false)
public class VNPayConfig {

    @Value("${vnpay.tmnCode}")
    private String vnp_TmnCode;

    @Value("${vnpay.hashSecret}")
    private String vnp_HashSecret;

    @Value("${vnpay.url}")
    private String vnp_Url;

    @Value("${vnpay.url.refund}")
    private String vnp_UrlRefund;

    @Value("${vnpay.api.url}")
    private String vnp_ApiUrl;

    @Value("${vnpay.return.url}")
    private String vnp_ReturnUrl;

    @Value("${vnpay.ipn.url}")
    private String vnp_IpnUrl;

    public static String vnp_TmnCode_Static;
    public static String vnp_HashSecret_Static;
    public static String vnp_Url_Static;
    public static String vnp_UrlRefund_Static;
    public static String vnp_ApiUrl_Static;
    public static String vnp_ReturnUrl_Static;
    public static String vnp_IpnUrl_Static;

    private static Mac mac;

    static {
        try {
            mac = Mac.getInstance("HmacSHA512");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Lỗi khởi tạo Mac", e);
        }
    }

    @PostConstruct
    public void init() {
        vnp_TmnCode_Static = vnp_TmnCode;
        vnp_HashSecret_Static = vnp_HashSecret;
        vnp_Url_Static = vnp_Url;
        vnp_UrlRefund_Static = vnp_UrlRefund;
        vnp_ApiUrl_Static = vnp_ApiUrl;
        vnp_ReturnUrl_Static = vnp_ReturnUrl;
        vnp_IpnUrl_Static = vnp_IpnUrl;
    }

    public String getVnpTmnCode() {
        return vnp_TmnCode;
    }

    public String getVnpHashSecret() {
        return vnp_HashSecret;
    }

    public String getVnpUrl() {
        return vnp_Url;
    }

    public String getVnpUrlRefund() {
        return vnp_UrlRefund;
    }

    public String getVnpApiUrl() {
        return vnp_ApiUrl;
    }

    public String getVnpReturnUrl() {
        return vnp_ReturnUrl;
    }

    public String getVnpIpnUrl() {
        return vnp_IpnUrl;
    }

    public static String hmacSHA512(String key, String data) {
        try {
            mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512"));
            byte[] hmacData = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder result = new StringBuilder();
            for (byte b : hmacData) {
                result.append(String.format("%02x", b));
            }
            return result.toString();
        } catch (InvalidKeyException e) {
            throw new RuntimeException("Lỗi tạo HMAC SHA512", e);
        }
    }

    public static String hashAllFields(Map<String, String> fields) {
        List<String> fieldNames = new ArrayList<>(fields.keySet());
        Collections.sort(fieldNames);
        StringBuilder sb = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = fields.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                sb.append(fieldName);
                sb.append("=");
                sb.append(java.net.URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                if (itr.hasNext()) {
                    sb.append("&");
                }
            }
        }
        return hmacSHA512(vnp_HashSecret_Static, sb.toString());
    }

    public static String getRandomNumber(int len) {
        Random rnd = new Random();
        String chars = "0123456789";
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        }
        return sb.toString();
    }
}
