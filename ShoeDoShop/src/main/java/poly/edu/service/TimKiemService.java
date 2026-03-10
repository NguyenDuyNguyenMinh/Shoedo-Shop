package poly.edu.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import poly.edu.dao.KhachHangDAO;
import poly.edu.dao.TimKiemDAO;
import poly.edu.dto.TimKiemDTO;
import poly.edu.entity.KhachHang;
import poly.edu.entity.TimKiem;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TimKiemService {

    private final TimKiemDAO   timKiemDAO;
    private final KhachHangDAO khachHangDAO;

    // ═══════════════════════════════════════════════════════════════
    //  Lấy lịch sử tìm kiếm của khách hàng (tối đa 10)
    // ═══════════════════════════════════════════════════════════════
    @Transactional(readOnly = true)
    public List<TimKiemDTO> layLichSu(Integer maKH) {
        return timKiemDAO.findTop10ByKhachHangMaKH(maKH)
                .stream()
                .limit(10)
                .map(tk -> TimKiemDTO.builder()
                        .keyword(tk.getNoiDungTimKiem())
                        .thoiGian(tk.getThoiGian())
                        .build())
                .collect(Collectors.toList());
    }

    // ═══════════════════════════════════════════════════════════════
    //  Lưu từ khóa tìm kiếm:
    //  - Nếu đã tồn tại → cập nhật ThoiGian (đưa lên đầu)
    //  - Nếu chưa có     → tạo mới
    //  - Giữ tối đa 10 từ khóa / tài khoản (xóa cũ nhất nếu vượt)
    // ═══════════════════════════════════════════════════════════════
    @Transactional
    public void luuTuKhoa(Integer maKH, String keyword) {
        if (keyword == null || keyword.isBlank()) return;
        String kw = keyword.trim();

        KhachHang kh = khachHangDAO.findById(maKH).orElse(null);
        if (kh == null) {
            log.warn("Không tìm thấy KhachHang maKH={}", maKH);
            return;
        }

        Optional<TimKiem> existing = timKiemDAO.findByMaKHAndKeyword(maKH, kw);

        if (existing.isPresent()) {
            // Đã có → cập nhật thời gian để đẩy lên đầu
            TimKiem tk = existing.get();
            tk.setThoiGian(LocalDateTime.now());
            timKiemDAO.save(tk);
        } else {
            // Kiểm tra số lượng, nếu đã đủ 10 thì xóa cũ nhất
            List<TimKiem> lichSu = timKiemDAO.findTop10ByKhachHangMaKH(maKH);
            if (lichSu.size() >= 10) {
                // Phần tử cuối = cũ nhất (sort DESC)
                timKiemDAO.delete(lichSu.get(lichSu.size() - 1));
            }

            TimKiem tk = new TimKiem();
            tk.setKhachHang(kh);
            tk.setNoiDungTimKiem(kw);
            tk.setThoiGian(LocalDateTime.now());
            timKiemDAO.save(tk);
        }
    }

    // ═══════════════════════════════════════════════════════════════
    //  Xóa toàn bộ lịch sử
    // ═══════════════════════════════════════════════════════════════
    @Transactional
    public void xoaTatCa(Integer maKH) {
        timKiemDAO.deleteAllByMaKH(maKH);
    }

    // ═══════════════════════════════════════════════════════════════
    //  Xóa 1 từ khóa
    // ═══════════════════════════════════════════════════════════════
    @Transactional
    public void xoaTuKhoa(Integer maKH, String keyword) {
        timKiemDAO.deleteByMaKHAndKeyword(maKH, keyword);
    }
}