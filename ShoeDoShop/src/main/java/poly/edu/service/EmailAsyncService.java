package poly.edu.service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import poly.edu.entity.HoaDon;
import poly.edu.entity.KhachHang;

@Service
public class EmailAsyncService {
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private PdfService pdfService;

    @Async
    public void sendSuccessEmail(HoaDon hd) {
    	try {
            KhachHang kh = hd.getKhachHang();
            if (kh == null || kh.getUser() == null || kh.getUser().getMail() == null) return;
            
            String email = kh.getUser().getMail();
            String tenKH = kh.getTenKH();
            Integer maHD = hd.getMaHD();
            
            byte[] pdfBytes = pdfService.generateInvoice(hd);
            
            String subject = "SHOEDO SHOP - Đơn hàng #HD" + String.format("%04d", hd.getMaHD()) + " đã giao thành công";
            String htmlContent = "<!DOCTYPE html>"
                    + "<html><head><meta charset='UTF-8'>"
                    + "<style>body{font-family:Arial,sans-serif}.container{max-width:600px;margin:0 auto;padding:20px;border:1px solid #ddd;border-radius:10px}.header{background:#000;color:#fff;padding:20px;text-align:center;border-radius:10px 10px 0 0}.content{ padding: 20px; background: #f9f9f9;}.warning{background:#fff3cd;padding:10px;border-radius:5px;margin:15px 0}</style>"
                    + "</head><body>"
                    + "<div class='container'>"
                    + "<div class='header'><h2>ShoeDo Shop - Giao hàng thành công</h2></div>"
                    + "<div class='content'>"
                    + "<p>Xin chào <strong>" + tenKH + "</strong>,</p>"
                    + "<p>Đơn hàng <strong>#HD" + String.format("%04d", hd.getMaHD()) + "</strong> của bạn đã được giao thành công.</p>"
                    + "<p>Bạn có thể xem chi tiết hóa đơn trong file đính kèm của email này.</p>"
                    + "<div class='warning'>"
                    + "<p><strong>Lưu ý:</strong> Bạn có <strong>1 THÁNG</strong> để báo lỗi/ bảo hành kể từ ngày đơn hàng được giao thành công</p>" + hd.getNgayDen()
                    + "<p>Sau 1 tháng, đơn hàng sẽ được xác nhận hoàn tất và không thể thay đổi.</p>"
                    + "</div>"
                    + "<p>Cảm ơn bạn đã tin tưởng và mua sắm tại SHOEDO SHOP!</p>"
                    + "<p>Truy cập <a href='http://localhost:5173/customer/orders/" + maHD + "'>ShoeDo Shop</a> để biết thêm chi tiết</p>"
                    + "</div></div></body></html>";
            
            emailService.sendHtmlEmailWithAttachment(email, subject, htmlContent, 
                "HD" + String.format("%04d", hd.getMaHD()) + ".pdf", pdfBytes);
            
        } catch (Exception e) {
            System.err.println("Lỗi gửi email: " + e.getMessage());
        }
    }

    @Async
    public void sendApologyEmail(HoaDon hd) {
    	try {
            KhachHang kh = hd.getKhachHang();
            if (kh == null || kh.getUser() == null || kh.getUser().getMail() == null) return;
            
            String email = kh.getUser().getMail();
            String tenKH = kh.getTenKH();
            Integer maHD = hd.getMaHD();
            byte[] pdfBytes = pdfService.generateInvoice(hd);
            
            String subject = "SHOEDO SHOP - Xin lỗi về sự cố đơn hàng #HD" + String.format("%04d", hd.getMaHD());
            String htmlContent = "<!DOCTYPE html>"
                    + "<html><head><meta charset='UTF-8'>"
                    + "<style>body{font-family:Arial,sans-serif}.container{max-width:600px;margin:0 auto;padding:20px;border:1px solid #ddd;border-radius:10px}.header{background:#000;color:#fff;padding:20px;text-align:center;border-radius:10px 10px 0 0}.content{ padding: 20px; background: #f9f9f9; }.apology{background:#f8d7da;color:#721c24;padding:15px;border-radius:5px;margin:15px 0}</style>"
                    + "</head><body>"
                    + "<div class='container'>"
                    + "<div class='header'><h2>ShoeDo Shop - Xin lỗi quý khách</h2></div>"
                    + "<div class='content'>"
                    + "<p>Xin chào <strong>" + tenKH + "</strong>,</p>"
                    + "<div class='apology'>"
                    + "<p>Chúng tôi chân thành xin lỗi về sự cố đơn hàng <strong>#HD" + String.format("%04d", hd.getMaHD()) + "</strong> mà bạn đã gặp phải.</p>"
                    + "<p><strong>Lỗi:</strong> " + (hd.getGhiChu() != null ? hd.getGhiChu() : "Không xác định") + "</p>"
                    + "</div>"
                    + "<p>Đội ngũ ShoeDo Shop đã xử lý sự cố này và đã khắc phục thành công. Vui lòng xem file hóa đơn đính kèm để kiểm tra chi tiết.</p>"
                    + "<p>Nếu bạn cần hỗ trợ thêm, vui lòng liên hệ hotline 1900 6869 của chúng tôi.</p>"
                    + "<p>Một lần nữa, chúng tôi xin lỗi về sự bất tiện này và hy vọng sẽ phục vụ bạn tốt hơn trong tương lai.</p>"
                    + "<p>Truy cập <a href='http://localhost:5173/customer/orders/" + maHD + "'>ShoeDo Shop</a> để biết thêm chi tiết</p>"
                    + "</div></div></body></html>";
            
            emailService.sendHtmlEmailWithAttachment(email, subject, htmlContent,
                "HD" + String.format("%04d", hd.getMaHD()) + ".pdf", pdfBytes);
            
        } catch (Exception e) {
            System.err.println("Lỗi gửi email: " + e.getMessage());
        }
    }
    
    @Async
    public void sendShippingEmail(HoaDon hd) {
        try {
            KhachHang kh = hd.getKhachHang();
            if (kh == null || kh.getUser() == null || kh.getUser().getMail() == null) return;
            
            String email = kh.getUser().getMail();
            String tenKH = kh.getTenKH();
            Integer maHD = hd.getMaHD();
            byte[] pdfBytes = pdfService.generateInvoice(hd);
            
            String subject = "SHOEDO SHOP - Đơn hàng #HD" + String.format("%04d", hd.getMaHD()) + " đang được vận chuyển";
            String htmlContent = "<!DOCTYPE html>"
                    + "<html><head><meta charset='UTF-8'>"
                    + "<style>body{font-family:Arial,sans-serif}.container{max-width:600px;margin:0 auto;padding:20px;border:1px solid #ddd;border-radius:10px}.header{background:#000;color:#fff;padding:20px;text-align:center;border-radius:10px 10px 0 0}.content{padding:20px;background:#f9f9f9;}.shipping-info{background:#e8f4fd;padding:15px;border-radius:5px;margin:15px 0;border-left:4px solid #000;}.tracking{background:#fff3cd;padding:10px;border-radius:5px;margin:15px 0}</style>"
                    + "</head><body>"
                    + "<div class='container'>"
                    + "<div class='header'><h2>ShoeDo Shop - Thông báo vận chuyển</h2></div>"
                    + "<div class='content'>"
                    + "<p>Xin chào <strong>" + tenKH + "</strong>,</p>"
                    + "<div class='shipping-info'>"
                    + "<p><strong>Đơn hàng #HD" + String.format("%04d", hd.getMaHD()) + "</strong> đã được xác nhận và đang trong quá trình vận chuyển đến bạn.</p>"
                    + "<p>Dự kiến thời gian giao hàng: 3 - 7 ngày tùy vào khu vực và điều kiện thời tiết</p>"
                    + "</div>"
                    + "<p>Chi tiết đơn hàng của bạn đã được đính kèm trong file PDF bên dưới. Vui lòng kiểm tra lại thông tin đơn hàng và địa chỉ nhận hàng.</p>"
                    + "<div class='tracking'>"
                    + "<p><strong>Lưu ý:</strong></p>"
                    + "<p>• Vui lòng theo dõi email để nhận thông báo khi đơn hàng được giao thành công</p>"
                    + "<p>• Nếu có bất kỳ thay đổi về thông tin nhận hàng, vui lòng liên hệ ngay với chúng tôi qua hotline 1900 6869</p>"
                    + "<p>• Truy cập <a href='http://localhost:5173/customer/orders/" + maHD + "'>ShoeDo Shop</a> để biết thêm chi tiết</p>"
                    + "</div>"
                    + "<p>Cảm ơn bạn đã tin tưởng và mua sắm tại ShoeDo Shop!</p>"
                    + "<p>Trân trọng,<br/>Đội ngũ ShoeDo Shop</p>"
                    + "</div></div></body></html>";
            
            emailService.sendHtmlEmailWithAttachment(email, subject, htmlContent, 
                "HD" + String.format("%04d", hd.getMaHD()) + ".pdf", pdfBytes);
            
        } catch (Exception e) {
            System.err.println("Lỗi gửi email thông báo vận chuyển: " + e.getMessage());
        }
    }
    
    // ==================== EMAIL AUTH ====================
    @Async
    public void sendRegistrationConfirmationEmail(String email, String fullname, String confirmationCode) {
        try {
            String subject = "SHOEDO SHOP - Xác nhận đăng ký tài khoản";
            
            String htmlContent = "<!DOCTYPE html>"
                    + "<html>"
                    + "<head>"
                    + "<meta charset='UTF-8'>"
                    + "<style>"
                    + "body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }"
                    + ".container { max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #ddd; border-radius: 10px; }"
                    + ".header { background: #000; color: #fff; padding: 20px; text-align: center; border-radius: 10px 10px 0 0; }"
                    + ".content { padding: 20px; background: #f9f9f9; }"
                    + ".code-box { background: #fff3cd; border: 1px solid #ffeaa7; padding: 15px; border-radius: 5px; text-align: center; margin: 20px 0; font-size: 24px; font-weight: bold; letter-spacing: 2px;  }"
                    + ".footer { text-align: center; padding: 20px; font-size: 12px; color: #666; }"
                    + ".warning { background: #f8d7da; color: #721c24; padding: 10px; border-radius: 5px; margin: 15px 0; }"
                    + "</style>"
                    + "</head>"
                    + "<body>"
                    + "<div class='container'>"
                    + "<div class='header'>"
                    + "<h2>ShoeDo Shop - Xác nhận đăng ký tài khoản</h2>"
                    + "</div>"
                    + "<div class='content'>"
                    + "<p>Xin chào <strong>" + fullname + "</strong>,</p>"
                    + "<p>Cảm ơn bạn đã đăng ký tài khoản tại ShoeDo Shop.</p>"
                    + "<p>Vui lòng nhập mã xác nhận bên dưới để hoàn tất quá trình đăng ký:</p>"
                    + "<div class='code-box'>"
                    + confirmationCode
                    + "</div>"
                    + "<p>Mã xác nhận này có hiệu lực trong <strong>10 phút</strong>.</p>"
                    + "<div class='warning'>"
                    + "<p><strong>Lưu ý:</strong> Nếu bạn không yêu cầu đăng ký tài khoản, vui lòng bỏ qua email này.</p>"
                    + "</div>"
                    + "</div>"
                    + "<div class='footer'>"
                    + "<p>Email này được gửi tự động từ hệ thống ShoeDo Shop.</p>"
                    + "<p>© 2026 ShoeDo Shop. All rights reserved.</p>"
                    + "</div>"
                    + "</div>"
                    + "</body>"
                    + "</html>";
            
            emailService.sendHtmlEmail(email, subject, htmlContent);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi gửi email xác nhận: " + e.getMessage());
        }
    }
    
    @Async
    public void sendForgotPasswordConfirmationEmail(String email, String fullname, String confirmationCode) {
        try {
            String subject = "SHOEDO SHOP - Xác nhận khôi phục mật khẩu";
            
            String htmlContent = "<!DOCTYPE html>"
                    + "<html>"
                    + "<head>"
                    + "<meta charset='UTF-8'>"
                    + "<style>"
                    + "body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }"
                    + ".container { max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #ddd; border-radius: 10px; }"
                    + ".header { background: #000; color: #fff; padding: 20px; text-align: center; border-radius: 10px 10px 0 0; }"
                    + ".content { padding: 20px; background: #f9f9f9; }"
                    + ".code-box { background: #fff3cd; border: 1px solid #ffeaa7; padding: 15px; border-radius: 5px; text-align: center; margin: 20px 0; font-size: 24px; font-weight: bold; letter-spacing: 2px;  }"
                    + ".footer { text-align: center; padding: 20px; font-size: 12px; color: #666; }"
                    + ".warning { background: #f8d7da; color: #721c24; padding: 10px; border-radius: 5px; margin: 15px 0; }"
                    + "</style>"
                    + "</head>"
                    + "<body>"
                    + "<div class='container'>"
                    + "<div class='header'>"
                    + "<h2>ShoeDo Shop - Khôi phục mật khẩu</h2>"
                    + "</div>"
                    + "<div class='content'>"
                    + "<p>Xin chào <strong>" + fullname + "</strong>,</p>"
                    + "<p>Chúng tôi đã nhận được yêu cầu khôi phục mật khẩu cho tài khoản của bạn.</p>"
                    + "<p>Vui lòng nhập mã xác nhận bên dưới để xác nhận yêu cầu:</p>"
                    + "<div class='code-box'>"
                    + confirmationCode
                    + "</div>"
                    + "<p>Mã xác nhận này có hiệu lực trong <strong>10 phút</strong>.</p>"
                    + "<div class='warning'>"
                    + "<p><strong>Lưu ý:</strong> Nếu bạn không yêu cầu khôi phục mật khẩu, vui lòng bỏ qua email này.</p>"
                    + "</div>"
                    + "</div>"
                    + "<div class='footer'>"
                    + "<p>Email này được gửi tự động từ hệ thống ShoeDo Shop.</p>"
                    + "<p>© 2026 ShoeDo Shop. All rights reserved.</p>"
                    + "</div>"
                    + "</div>"
                    + "</body>"
                    + "</html>";
            
            emailService.sendHtmlEmail(email, subject, htmlContent);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi gửi email xác nhận: " + e.getMessage());
        }
    }
    
    @Async
    public void sendPasswordResetEmail(String email, String fullname, String newPassword) {
        try {
        	String subject = "SHOEDO SHOP - Khôi phục mật khẩu";
            
            String htmlContent = "<!DOCTYPE html>"
                    + "<html>"
                    + "<head>"
                    + "<meta charset='UTF-8'>"
                    + "<style>"
                    + "body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }"
                    + ".container { max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #ddd; border-radius: 10px; }"
                    + ".header { background: #000; color: #fff; padding: 20px; text-align: center; border-radius: 10px 10px 0 0; }"
                    + ".content { padding: 20px; background: #f9f9f9; }"
                    + ".password-box { background: #fff3cd; border: 1px solid #ffeaa7; padding: 15px; border-radius: 5px; text-align: center; margin: 20px 0; font-size: 24px; font-weight: bold; letter-spacing: 2px; }"
                    + ".footer { text-align: center; padding: 20px; font-size: 12px; color: #666; }"
                    + ".warning { background: #f8d7da; color: #721c24; padding: 10px; border-radius: 5px; margin: 15px 0; }"
                    + "</style>"
                    + "</head>"
                    + "<body>"
                    + "<div class='container'>"
                    + "<div class='header'>"
                    + "<h2>ShoeDo Shop - Khôi phục mật khẩu</h2>"
                    + "</div>"
                    + "<div class='content'>"
                    + "<p>Xin chào <strong>" + fullname + "</strong>,</p>"
                    + "<p>Chúng tôi đã nhận được yêu cầu khôi phục mật khẩu cho tài khoản của bạn.</p>"
                    + "<p>Mật khẩu mới của bạn là:</p>"
                    + "<div class='password-box'>"
                    + newPassword
                    + "</div>"
                    + "<div class='warning'>"
                    + "<p><strong>Lưu ý quan trọng:</strong></p>"
                    + "<p>• Vui lòng đăng nhập và thay đổi mật khẩu ngay sau khi truy cập hệ thống</p>"
                    + "<p>• Không chia sẻ mật khẩu này với bất kỳ ai</p>"
                    + "</div>"
                    + "<p>Nếu bạn không thực hiện yêu cầu này, vui lòng bỏ qua email.</p>"
                    + "</div>"
                    + "<div class='footer'>"
                    + "<p>Email này được gửi tự động từ hệ thống ShoeDo Shop.</p>"
                    + "<p>© 2026 ShoeDo Shop. All rights reserved.</p>"
                    + "</div>"
                    + "</div>"
                    + "</body>"
                    + "</html>";
            emailService.sendHtmlEmail(email, subject, htmlContent);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi gửi email: " + e.getMessage());
        }
    }

    @Async
    public void sendPasswordResetByAdminEmail(String email, String fullname, String username, String newPassword) {
        try {
            String subject = "SHOEDO SHOP - Mật khẩu đã được reset bởi quản trị viên";

            String htmlContent = "<!DOCTYPE html>"
                    + "<html>"
                    + "<head>"
                    + "<meta charset='UTF-8'>"
                    + "<style>"
                    + "body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }"
                    + ".container { max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #ddd; border-radius: 10px; }"
                    + ".header { background: #000; color: #fff; padding: 20px; text-align: center; border-radius: 10px 10px 0 0; }"
                    + ".content { padding: 20px; background: #f9f9f9; }"
                    + ".password-box { background: #fff3cd; border: 1px solid #ffeaa7; padding: 15px; border-radius: 5px; text-align: center; margin: 20px 0; font-size: 24px; font-weight: bold; letter-spacing: 2px; font-family: monospace; }"
                    + ".footer { text-align: center; padding: 20px; font-size: 12px; color: #666; }"
                    + ".warning { background: #f8d7da; color: #721c24; padding: 10px; border-radius: 5px; margin: 15px 0; }"
                    + ".info { background: #d1ecf1; color: #0c5460; padding: 10px; border-radius: 5px; margin: 15px 0; }"
                    + "</style>"
                    + "</head>"
                    + "<body>"
                    + "<div class='container'>"
                    + "<div class='header'>"
                    + "<h2>ShoeDo Shop - Thông báo reset mật khẩu</h2>"
                    + "</div>"
                    + "<div class='content'>"
                    + "<p>Xin chào <strong>" + fullname + "</strong>,</p>"
                    + "<p>Mật khẩu của bạn đã được reset bởi quản trị viên.</p>"
                    + "<div class='info'>"
                    + "<p><strong>Thông tin tài khoản:</strong></p>"
                    + "<p>• Username: <strong>" + username + "</strong></p>"
                    + "<p>• Email: <strong>" + email + "</strong></p>"
                    + "</div>"
                    + "<p>Mật khẩu mới của bạn là:</p>"
                    + "<div class='password-box'>"
                    + newPassword
                    + "</div>"
                    + "<div class='warning'>"
                    + "<p><strong>Lưu ý quan trọng:</strong></p>"
                    + "<p>• Vui lòng đăng nhập và thay đổi mật khẩu ngay sau khi nhận được email này</p>"
                    + "<p>• Không chia sẻ mật khẩu này với bất kỳ ai</p>"
                    + "<p>• Nếu bạn không yêu cầu reset mật khẩu, vui lòng liên hệ với quản trị viên ngay lập tức</p>"
                    + "</div>"
                    + "<p>Trân trọng,<br>Đội ngũ ShoeDo Shop</p>"
                    + "</div>"
                    + "<div class='footer'>"
                    + "<p>Email này được gửi tự động từ hệ thống ShoeDo Shop.</p>"
                    + "<p>© 2026 ShoeDo Shop. All rights reserved.</p>"
                    + "</div>"
                    + "</div>"
                    + "</body>"
                    + "</html>";

            emailService.sendHtmlEmail(email, subject, htmlContent);

        } catch (Exception e) {
            System.err.println("Lỗi gửi email reset mật khẩu: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
