package poly.edu.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import poly.edu.entity.Voucher;
import java.util.List;
import java.util.Optional;
import java.util.Date;

public interface VoucherDAO extends JpaRepository<Voucher, Integer> {

}
