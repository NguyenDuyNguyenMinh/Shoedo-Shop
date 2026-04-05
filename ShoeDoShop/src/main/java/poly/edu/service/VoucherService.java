package poly.edu.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import poly.edu.dao.VoucherDAO;
import poly.edu.entity.Voucher;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class VoucherService {

	@Autowired
    private VoucherDAO voucherDAO;


    public List<Voucher> findAll() {
        return voucherDAO.findAllOrderByNgayBatDauDesc();
    }


    public Voucher findById(Integer id) {
        Optional<Voucher> optional = voucherDAO.findById(id);
        if (optional.isEmpty()) {
            throw new RuntimeException("Không tìm thấy Voucher với mã: " + id);
        }
        return optional.get();
    }


    public Voucher create(Voucher voucher) {
        if (voucher.getNgayKetThuc() != null && voucher.getNgayKetThuc().before(voucher.getNgayBatDau())) {
            throw new RuntimeException("Ngày kết thúc phải lớn hơn ngày bắt đầu!");
        }
        voucher.setIsActive(true);
        return voucherDAO.save(voucher);
    }

    public Voucher update(Integer id, Voucher voucherDetails) {
        Voucher existingVoucher = findById(id);
        
        if (voucherDetails.getNgayKetThuc() != null && voucherDetails.getNgayKetThuc().before(existingVoucher.getNgayBatDau())) {
            throw new RuntimeException("Ngày kết thúc phải lớn hơn ngày bắt đầu!");
        }
        existingVoucher.setTenVoucher(voucherDetails.getTenVoucher());
        existingVoucher.setSoLuong(voucherDetails.getSoLuong());
        existingVoucher.setNgayBatDau(voucherDetails.getNgayBatDau());
        existingVoucher.setNgayKetThuc(voucherDetails.getNgayKetThuc());
        
        return voucherDAO.save(existingVoucher);
    }

    public Voucher activate(Integer id) {
        Voucher existingVoucher = findById(id);
        // Kiểm tra an toàn: Lỡ nó quá hạn rồi thì không cho bật lại
        if (existingVoucher.getNgayKetThuc() != null && existingVoucher.getNgayKetThuc().before(new Date())) {
            throw new RuntimeException("Không thể mở lại voucher đã quá thời gian kết thúc!");
        }
        existingVoucher.setIsActive(true);
        return voucherDAO.save(existingVoucher);
    }
    
    public Voucher deactivate(Integer id) {
        Voucher existingVoucher = findById(id);
        existingVoucher.setIsActive(false);
        return voucherDAO.save(existingVoucher);
    }
    @Scheduled(fixedRate = 60000) 
    public void autoDeactivateExpiredVouchers() {
        Date now = new Date();
        List<Voucher> vouchers = voucherDAO.findAll();
        
        for (Voucher v : vouchers) {
            if (v.getIsActive() && v.getNgayKetThuc() != null && v.getNgayKetThuc().before(now)) {
                v.setIsActive(false);
                voucherDAO.save(v);
                System.out.println("Đã tự động tắt voucher hết giờ: " + v.getTenVoucher());
            }
        }
    }
}
