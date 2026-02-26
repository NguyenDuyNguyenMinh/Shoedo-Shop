package poly.edu.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import poly.edu.entity.*;
import java.util.*;

public interface KhachHangDAO extends JpaRepository<KhachHang, Integer> {
    KhachHang findByUser_MaUser(Integer maUser);
    
    KhachHang findBySdt(String sdt);
    List<KhachHang> findByTenKHContaining(String tenKH);
    Optional<KhachHang> findByUser(Users user);
    
    @Query("SELECT kh FROM KhachHang kh WHERE kh.user.isActive = true")
    List<KhachHang> findAllActiveCustomers();
}