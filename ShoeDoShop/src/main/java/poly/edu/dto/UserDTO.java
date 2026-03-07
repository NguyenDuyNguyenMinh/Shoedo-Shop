package poly.edu.dto;

import lombok.Data;
import java.util.Date;
import java.util.List;

@Data
public class UserDTO {
    private Integer maUser;
    private String userName;
    private String mail;
    private Boolean isActive;
    private Date createAt;
    private String role;
    private String hoTen;
    private String sdt;
    private List<DiaChiJsonDTO> diaChis;
    private List<HoaDonDTO> hoaDons;
}