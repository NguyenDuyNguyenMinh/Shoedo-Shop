package poly.edu.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import poly.edu.entity.SanPhamDanhMuc;
import poly.edu.entity.SanPhamDanhMuc.SanPhamDanhMucId;
import java.util.List;

public interface SanPhamDanhMucDAO extends JpaRepository<SanPhamDanhMuc, SanPhamDanhMucId> {

    /**
     * Lấy tất cả danh mục liên kết của một sản phẩm theo MaSP.
     */
    List<SanPhamDanhMuc> findBySanPham_MaSP(Integer maSP);

    /**
     * Lấy tất cả sản phẩm thuộc một danh mục theo MaDM.
     */
    List<SanPhamDanhMuc> findByDanhMuc_MaDM(Integer maDM);
}