package poly.edu.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import poly.edu.dao.SanPhamDAO;
import poly.edu.dao.SanPhamChiTietDAO;
import poly.edu.entity.SanPham;
import java.util.*;

@RestController
@RequestMapping("/api/public/bot")
public class BotDataController {

    @Autowired
    private SanPhamDAO sanPhamDAO;

    @Autowired
    private SanPhamChiTietDAO sctDAO;

    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchProductForBot(@RequestParam(defaultValue = "") String keyword) {
        List<Map<String, Object>> productList = new ArrayList<>();
        
        try {
            System.out.println("=== BOT ĐANG TÌM KIẾM VỚI TỪ KHÓA: " + keyword + " ===");
            
            List<SanPham> dsSanPham = sanPhamDAO.searchByNameOrDescription(keyword);

            System.out.println("=> Database tìm thấy: " + dsSanPham.size() + " sản phẩm khớp tên hoặc mô tả.");
            
            for (SanPham sp : dsSanPham) {
                Map<String, Object> item = new HashMap<>();

                Double giaMin = sctDAO.findGiaThapNhat(sp.getMaSP()).orElse(0.0);
                Integer tongSoLuong = sctDAO.tinhTongSoLuong(sp.getMaSP());
                

                int soLuongThucTe = (tongSoLuong != null) ? tongSoLuong : 0;
                
                item.put("ten_san_pham", sp.getTenSP());
                item.put("gia_ban", String.format("%,.0f VNĐ", giaMin));
                item.put("gia_tri_so", giaMin);
                item.put("trang_thai", soLuongThucTe > 0 ? "Còn hàng (" + soLuongThucTe + " đôi)" : "Hết hàng");
                item.put("link_chi_tiet", "http://localhost:5173/customer/detail-product/" + sp.getMaSP());
                
                productList.add(item);
                System.out.println("Đã thêm thành công vào danh sách: " + sp.getTenSP());
            }
        } catch (Exception e) {
            System.out.println("!!! CÓ LỖI XẢY RA TRONG CODE JAVA: !!!");
            e.printStackTrace(); 
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", productList);
        response.put("message", "Tìm thấy " + productList.size() + " sản phẩm.");

        return ResponseEntity.ok(response);
    }	
}