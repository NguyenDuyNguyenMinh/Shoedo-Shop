package poly.edu.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import java.util.Date;
import java.util.List;

@Entity
@Data
@Table(name = "Users")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Users {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaUser")
    private Integer maUser;

    @Column(name = "UserName", unique = true)
    private String userName;

    @Column(name = "Mail", unique = true)
    private String mail;

    @Column(name = "PassWord")
    private String passWord;

    @Column(name = "IsActive")
    private Boolean isActive;

    @Column(name = "CreateAt")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createAt;

    @OneToOne(mappedBy = "user", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @JsonIgnoreProperties("user")
    private KhachHang khachHang;

    @OneToOne(mappedBy = "user", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @JsonIgnoreProperties("user")
    private QuanTri quanTri;
}