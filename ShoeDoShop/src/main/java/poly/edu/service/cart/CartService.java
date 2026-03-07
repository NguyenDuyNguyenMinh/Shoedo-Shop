package poly.edu.service.cart;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import poly.edu.dao.GioHangDAO;
import poly.edu.dao.KhachHangDAO;
import poly.edu.dao.SanPhamChiTietDAO;
import poly.edu.dto.cart.CartItemDTO;
import poly.edu.entity.GioHang;
import poly.edu.entity.KhachHang;
import poly.edu.entity.SanPhamChiTiet;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CartService {

    @Autowired
    private GioHangDAO gioHangDAO;

    @Autowired
    private KhachHangDAO khachHangDAO;

    @Autowired
    private SanPhamChiTietDAO sanPhamChiTietDAO;

    /**
     * Lấy danh sách sản phẩm trong giỏ hàng của khách
     * @param maKH Mã khách hàng
     * @return Danh sách CartItemDTO
     */
    public List<CartItemDTO> getCartItems(Integer maKH) {
        List<GioHang> gioHangs = gioHangDAO.findByKhachHangMaKH(maKH);
        List<CartItemDTO> cartItems = new ArrayList<>();

        for (GioHang gh : gioHangs) {
            CartItemDTO item = new CartItemDTO();
            item.setMaGH(gh.getMaGH());
            item.setMaSKU(gh.getSanPhamChiTiet().getMaSKU());
            item.setSoLuong(gh.getSoLuong());
            item.setDonGia(gh.getSanPhamChiTiet().getDonGia());
            item.setThanhTien(gh.getSoLuong() * gh.getSanPhamChiTiet().getDonGia());

            // Lấy thông tin sản phẩm từ SanPhamChiTiet -> MauSac -> SanPham
            if (gh.getSanPhamChiTiet().getMauSac() != null) {
                item.setTenMau(gh.getSanPhamChiTiet().getMauSac().getTenMau());
                item.setHinhAnh(gh.getSanPhamChiTiet().getMauSac().getHinhAnh());
                if (gh.getSanPhamChiTiet().getMauSac().getSanPham() != null) {
                    item.setTenSP(gh.getSanPhamChiTiet().getMauSac().getSanPham().getTenSP());
                }
            }

            // Lấy thông tin size
            if (gh.getSanPhamChiTiet().getSize() != null) {
                item.setCoGiay(gh.getSanPhamChiTiet().getSize().getCoGiay());
            }

            cartItems.add(item);
        }

        return cartItems;
    }

    /**
     * Thêm sản phẩm vào giỏ hàng
     * Nếu sản phẩm đã có trong giỏ thì cộng dồn số lượng
     * @param maKH Mã khách hàng
     * @param maSKU Mã sản phẩm chi tiết
     * @param soLuong Số lượng thêm
     * @return CartItemDTO vừa thêm/cập nhật
     */
    @Transactional
    public CartItemDTO addToCart(Integer maKH, Integer maSKU, Integer soLuong) {
        // Kiểm tra khách hàng tồn tại
        KhachHang khachHang = khachHangDAO.findById(maKH)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng với mã: " + maKH));

        // Kiểm tra sản phẩm chi tiết tồn tại
        SanPhamChiTiet sanPhamChiTiet = sanPhamChiTietDAO.findById(maSKU)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với mã: " + maSKU));

        // Kiểm tra sản phẩm đã có trong giỏ hàng chưa
        Optional<GioHang> existingCart = gioHangDAO.findByKhachHangMaKHAndSanPhamChiTietMaSKU(maKH, maSKU);

        GioHang gioHang;
        if (existingCart.isPresent()) {
            // Nếu đã có, cộng dồn số lượng
            gioHang = existingCart.get();
            gioHang.setSoLuong(gioHang.getSoLuong() + soLuong);
        } else {
            // Nếu chưa có, tạo mới
            gioHang = new GioHang();
            gioHang.setKhachHang(khachHang);
            gioHang.setSanPhamChiTiet(sanPhamChiTiet);
            gioHang.setSoLuong(soLuong);
        }

        gioHang = gioHangDAO.save(gioHang);

        // Trả về CartItemDTO
        return convertToDTO(gioHang);
    }

    /**
     * Cập nhật số lượng sản phẩm trong giỏ hàng
     * @param maGH Mã giỏ hàng
     * @param soLuong Số lượng mới
     * @return CartItemDTO đã cập nhật
     */
    @Transactional
    public CartItemDTO updateQuantity(Integer maGH, Integer soLuong) {
        GioHang gioHang = gioHangDAO.findById(maGH)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm trong giỏ hàng với mã: " + maGH));

        if (soLuong <= 0) {
            // Nếu số lượng <= 0 thì xóa khỏi giỏ
            gioHangDAO.delete(gioHang);
            return null;
        }

        gioHang.setSoLuong(soLuong);
        gioHang = gioHangDAO.save(gioHang);

        return convertToDTO(gioHang);
    }

    /**
     * Xóa sản phẩm khỏi giỏ hàng
     * @param maGH Mã giỏ hàng
     */
    @Transactional
    public void removeFromCart(Integer maGH) {
        GioHang gioHang = gioHangDAO.findById(maGH)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm trong giỏ hàng với mã: " + maGH));
        gioHangDAO.delete(gioHang);
    }

    /**
     * Chuyển đổi GioHang entity sang CartItemDTO
     * @param gioHang Entity GioHang
     * @return CartItemDTO
     */
    private CartItemDTO convertToDTO(GioHang gh) {
        CartItemDTO item = new CartItemDTO();
        item.setMaGH(gh.getMaGH());
        item.setMaSKU(gh.getSanPhamChiTiet().getMaSKU());
        item.setSoLuong(gh.getSoLuong());
        item.setDonGia(gh.getSanPhamChiTiet().getDonGia());
        item.setThanhTien(gh.getSoLuong() * gh.getSanPhamChiTiet().getDonGia());

        if (gh.getSanPhamChiTiet().getMauSac() != null) {
            item.setTenMau(gh.getSanPhamChiTiet().getMauSac().getTenMau());
            item.setHinhAnh(gh.getSanPhamChiTiet().getMauSac().getHinhAnh());
            if (gh.getSanPhamChiTiet().getMauSac().getSanPham() != null) {
                item.setTenSP(gh.getSanPhamChiTiet().getMauSac().getSanPham().getTenSP());
            }
        }

        if (gh.getSanPhamChiTiet().getSize() != null) {
            item.setCoGiay(gh.getSanPhamChiTiet().getSize().getCoGiay());
        }

        return item;
    }
}
