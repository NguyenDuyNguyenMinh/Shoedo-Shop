package poly.edu.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import poly.edu.entity.*;
import java.util.*;

public interface UsersDAO extends JpaRepository<Users, Integer> {
    Users findByMail(String mail);
    
    Users findByUserName(String userName);

    boolean existsByMail(String mail);
    boolean existsByUserName(String userName);
    
    @Query("SELECT u FROM Users u WHERE u.mail = :mail AND u.isActive = :isActive")
    Users findByMailAndIsActive(@Param("mail") String mail, @Param("isActive") Boolean isActive);
    
    List<Users> findByIsActiveTrue();
    
    List<Users> findByIsActiveFalseOrIsActiveIsNull();
    
    @Query("SELECT u FROM Users u WHERE u.mail LIKE %:keyword% AND u.isActive = :isActive")
    Page<Users> searchByKeywordAndActive(@Param("keyword") String keyword, 
                                         @Param("isActive") Boolean isActive, 
                                         Pageable pageable);

    @Query("SELECT DISTINCT u FROM Users u " +
            "LEFT JOIN u.khachHang kh " +
            "LEFT JOIN u.quanTri qt " +
            "WHERE (:keyword IS NULL OR :keyword = '' OR " +
            "      u.mail LIKE %:keyword% OR " +
            "      u.userName LIKE %:keyword% OR " +
            "      kh.tenKH LIKE %:keyword% OR " +
            "      kh.sdt LIKE %:keyword% OR " +
            "      qt.tenQT LIKE %:keyword%) " +
            "AND (:isActive IS NULL OR u.isActive = :isActive) " +
            "AND (:roleFilter = '' OR " +
            "      (:roleFilter = 'QT' AND EXISTS (SELECT qt2 FROM QuanTri qt2 WHERE qt2.user = u)) OR " +
            "      (:roleFilter = 'KH' AND EXISTS (SELECT kh2 FROM KhachHang kh2 WHERE kh2.user = u)) )")
    Page<Users> findByFilter(@Param("keyword") String keyword,
                             @Param("roleFilter") String roleFilter,
                             @Param("isActive") Boolean isActive,
                             Pageable pageable);
}