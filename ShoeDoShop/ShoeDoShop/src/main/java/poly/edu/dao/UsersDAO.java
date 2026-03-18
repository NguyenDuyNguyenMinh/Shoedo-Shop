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
            "      (:roleFilter = 'QT' AND qt IS NOT NULL) OR " +
            "      (:roleFilter = 'KH' AND kh IS NOT NULL) )")
    Page<Users> findByFilter(@Param("keyword") String keyword,
                             @Param("roleFilter") String roleFilter,
                             @Param("isActive") Boolean isActive,
                             Pageable pageable);

    @Query("SELECT DISTINCT u FROM Users u " +
            "JOIN u.quanTri qt " +
            "WHERE (:keyword IS NULL OR :keyword = '' OR " +
            "      u.mail LIKE %:keyword% OR " +
            "      u.userName LIKE %:keyword% OR " +
            "      qt.tenQT LIKE %:keyword%) " +
            "AND (:isActive IS NULL OR u.isActive = :isActive) " +
            "AND qt.role = true")
    Page<Users> findByAdmin(@Param("keyword") String keyword,
                            @Param("isActive") Boolean isActive,
                            Pageable pageable);

    // Lọc employee (qt IS NOT NULL AND qt.role = false)
    @Query("SELECT DISTINCT u FROM Users u " +
            "JOIN u.quanTri qt " +
            "WHERE (:keyword IS NULL OR :keyword = '' OR " +
            "      u.mail LIKE %:keyword% OR " +
            "      u.userName LIKE %:keyword% OR " +
            "      qt.tenQT LIKE %:keyword%) " +
            "AND (:isActive IS NULL OR u.isActive = :isActive) " +
            "AND qt.role = false")
    Page<Users> findByEmployee(@Param("keyword") String keyword,
                               @Param("isActive") Boolean isActive,
                               Pageable pageable);

    // Lọc customer (kh IS NOT NULL)
    @Query("SELECT DISTINCT u FROM Users u " +
            "JOIN u.khachHang kh " +
            "WHERE (:keyword IS NULL OR :keyword = '' OR " +
            "      u.mail LIKE %:keyword% OR " +
            "      u.userName LIKE %:keyword% OR " +
            "      kh.tenKH LIKE %:keyword% OR " +
            "      kh.sdt LIKE %:keyword%) " +
            "AND (:isActive IS NULL OR u.isActive = :isActive)")
    Page<Users> findByCustomer(@Param("keyword") String keyword,
                               @Param("isActive") Boolean isActive,
                               Pageable pageable);
}