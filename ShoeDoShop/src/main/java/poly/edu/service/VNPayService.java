package poly.edu.service;

import org.springframework.stereotype.Service;
import poly.edu.config.VNPayConfig;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class VNPayService {

    public String createPaymentUrl(Integer maHD, long amount, String orderInfo) throws Exception {
        Map<String, String> vnpParams = new LinkedHashMap<>();
        vnpParams.put("vnp_Version", "2.1.0");
        vnpParams.put("vnp_Command", "pay");
        vnpParams.put("vnp_TmnCode", VNPayConfig.vnp_TmnCode_Static);
        vnpParams.put("vnp_Amount", String.valueOf(amount * 100));
        vnpParams.put("vnp_CurrCode", "VND");
        vnpParams.put("vnp_BankCode", "NCB");
        vnpParams.put("vnp_TxnRef", maHD.toString());
        vnpParams.put("vnp_OrderInfo", orderInfo);
        vnpParams.put("vnp_OrderType", "topup");
        vnpParams.put("vnp_Locale", "vn");
        vnpParams.put("vnp_ReturnUrl", VNPayConfig.vnp_ReturnUrl_Static);
        vnpParams.put("vnp_IpAddr", "127.0.0.1");
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        vnpParams.put("vnp_CreateDate", sdf.format(cld.getTime()));
        
        cld.add(Calendar.MINUTE, 15);
        vnpParams.put("vnp_ExpireDate", sdf.format(cld.getTime()));

        String signValue = VNPayConfig.hashAllFields(vnpParams);
        vnpParams.put("vnp_SecureHash", signValue);

        StringBuilder url = new StringBuilder(VNPayConfig.vnp_Url_Static);
        url.append("?");
        
        int i = 0;
        for (Map.Entry<String, String> entry : vnpParams.entrySet()) {
            if (i > 0) {
                url.append("&");
            }
            url.append(URLEncoder.encode(entry.getKey(), StandardCharsets.US_ASCII.toString()));
            url.append("=");
            url.append(URLEncoder.encode(entry.getValue(), StandardCharsets.US_ASCII.toString()));
            i++;
        }

        return url.toString();
    }

    public Map<String, String> processReturn(Map<String, String> params) {
        Map<String, String> result = new HashMap<>();
        
        String vnp_SecureHash = params.get("vnp_SecureHash");
        params.remove("vnp_SecureHash");
        
        String signValue = VNPayConfig.hashAllFields(params);
        
        if (signValue.equals(vnp_SecureHash)) {
            String vnp_ResponseCode = params.get("vnp_ResponseCode");
            String vnp_TxnRef = params.get("vnp_TxnRef");
            
            result.put("success", "00".equals(vnp_ResponseCode) ? "true" : "false");
            result.put("responseCode", vnp_ResponseCode);
            result.put("maHD", vnp_TxnRef);
            result.put("transactionNo", params.get("vnp_TransactionNo"));
            
            if ("00".equals(vnp_ResponseCode)) {
                result.put("message", "Thanh toán thành công");
            } else {
                result.put("message", getResponseMessage(vnp_ResponseCode));
            }
        } else {
            result.put("success", "false");
            result.put("message", "Chữ ký không hợp lệ");
        }
        
        return result;
    }

    public Map<String, String> processIpn(Map<String, String> params) {
        Map<String, String> result = new HashMap<>();
        
        String vnp_SecureHash = params.get("vnp_SecureHash");
        params.remove("vnp_SecureHash");
        
        String signValue = VNPayConfig.hashAllFields(params);
        
        if (signValue.equals(vnp_SecureHash)) {
            String vnp_ResponseCode = params.get("vnp_ResponseCode");
            String vnp_TransactionStatus = params.get("vnp_TransactionStatus");
            
            if ("00".equals(vnp_ResponseCode) && "00".equals(vnp_TransactionStatus)) {
                // IPN chỉ xác nhận chữ ký hợp lệ
                // HoaDon được tạo bởi vnpay-return handler
                result.put("RspCode", "00");
                result.put("Message", "Confirm Success");
            } else {
                result.put("RspCode", vnp_ResponseCode);
                result.put("Message", getResponseMessage(vnp_ResponseCode));
            }
        } else {
            result.put("RspCode", "97");
            result.put("Message", "Invalid signature");
        }
        
        return result;
    }

    public String refund(String vnp_TransactionNo, String vnp_TxnRef, long amount, String vnp_TransactionDate, String note) throws Exception {
        Map<String, String> vnpParams = new LinkedHashMap<>();
        vnpParams.put("vnp_Version", "2.1.0");
        vnpParams.put("vnp_Command", "refund");
        vnpParams.put("vnp_TmnCode", VNPayConfig.vnp_TmnCode_Static);
        vnpParams.put("vnp_TransactionType", "02");
        vnpParams.put("vnp_TxnRef", vnp_TxnRef);
        vnpParams.put("vnp_Amount", String.valueOf(amount * 100));
        vnpParams.put("vnp_TransactionNo", vnp_TransactionNo);
        vnpParams.put("vnp_TransactionDate", vnp_TransactionDate);
        vnpParams.put("vnp_CreateBy", "admin");
        vnpParams.put("vnp_CreateDate", new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));
        vnpParams.put("vnp_IpAddr", "127.0.0.1");
        vnpParams.put("vnp_OrderInfo", note != null ? note : "Hoan tien giao dich " + vnp_TxnRef);

        String signValue = VNPayConfig.hashAllFields(vnpParams);
        vnpParams.put("vnp_SecureHash", signValue);

        String url = VNPayConfig.vnp_ApiUrl_Static + "/refund";
        
        return sendRefundRequest(url, vnpParams);
    }

    private String sendRefundRequest(String urlString, Map<String, String> params) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setDoOutput(true);

        StringBuilder postData = new StringBuilder();
        int i = 0;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (i > 0) postData.append("&");
            postData.append(URLEncoder.encode(entry.getKey(), StandardCharsets.US_ASCII.toString()));
            postData.append("=");
            postData.append(URLEncoder.encode(entry.getValue(), StandardCharsets.US_ASCII.toString()));
            i++;
        }

        try (DataOutputStream dos = new DataOutputStream(conn.getOutputStream())) {
            dos.writeBytes(postData.toString());
        }

        int responseCode = conn.getResponseCode();
        BufferedReader reader = new BufferedReader(new InputStreamReader(
            responseCode == 200 ? conn.getInputStream() : conn.getErrorStream(), StandardCharsets.UTF_8));
        
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();

        return response.toString();
    }

    private String getResponseCode(String vnp_ResponseCode) {
        switch (vnp_ResponseCode) {
            case "00": return "Giao dịch thành công";
            case "07": return "Trừ tiền thành công. Giao dịch bị nghi ngờ (liên quan đến mối đe dọa an ninh)";
            case "09": return "Giao dịch không thành công do: Thẻ/Tài khoản của khách hàng chưa đăng ký dịch vụ InternetBanking tại ngân hàng";
            case "10": return "Giao dịch không thành công do: Khách hàng xác thực thông tin tài khoản/thẻ không đúng quá 3 lần";
            case "11": return "Giao dịch không thành công do: Đã hết hạn chờ thanh toán";
            case "12": return "Giao dịch không thành công do: Thẻ/Tài khoản bị khóa";
            case "13": return "Giao dịch không thành công do: Nhập sai mật khẩu xác thực giao dịch (OTP)";
            case "24": return "Giao dịch không thành công do: Khách hàng hủy giao dịch";
            case "51": return "Giao dịch không thành công do: Tài khoản không đủ số dư để thực hiện giao dịch";
            case "65": return "Giao dịch không thành công do: Tài khoản đã vượt quá hạn mức giao dịch trong ngày";
            case "75": return "Ngân hàng thanh toán đang bảo trì";
            case "79": return "Giao dịch không thành công do: KH nhập sai mật khẩu thanh toán quá số lần quy định";
            case "99": return "Lỗi không xác định";
            default: return "Mã lỗi: " + vnp_ResponseCode;
        }
    }

    private String getResponseMessage(String vnp_ResponseCode) {
        return getResponseCode(vnp_ResponseCode);
    }
}
