<template>
  <div class="customer-layout d-flex flex-column min-vh-100">
    <KH_Navbar />

    <main class="container">
      <div v-if="error" class="alert alert-danger" role="alert">{{ error }}</div>

      <div v-if="loading" class="text-center p-5">
        <div class="spinner-border text-primary" role="status">
          <span class="visually-hidden">Loading...</span>
        </div>
      </div>

      <div v-else-if="order">
        <div class="d-flex justify-content-between align-items-center">
          <h2 class="mt-4 mb-3">Chi Tiết Đơn Hàng #{{ order.maHD }}</h2>
          <router-link to="/customer/orders" class="btn btn-outline-secondary">
            <i class="bi bi-arrow-left"></i> Quay Lại
          </router-link>
        </div>

        <div class="card mb-4">
          <div class="card-header">
            <h5 class="mb-0">Thông Tin Đơn Hàng</h5>
          </div>
          <div class="card-body">
            <div class="row">
              <div class="col-md-6">
                <p><strong>Mã đơn hàng:</strong> #{{ order.maHD }}</p>
                <p><strong>Ngày đặt hàng:</strong> {{ formatDate(order.ngayMua) }}</p>
                <p><strong>Ngày nhận hàng:</strong> {{ order.ngayDen ? formatDate(order.ngayDen) : 'Chưa nhận' }}</p>
                <p><strong>Trạng thái:</strong>
                  <span :class="getStatusClass(order.trangThai)" class="badge fw-bold">
                    {{ order.trangThai }}
                  </span>
                </p>
                <p><strong>Phương thức thanh toán:</strong> {{ order.phuongThucTT || 'Chưa cập nhật' }}</p>
                <p v-if="order.ghiChu"><strong>Ghi chú:</strong> {{ order.ghiChu }}</p>
              </div>

              <div class="col-md-6">
                <h6><strong>Địa chỉ giao hàng:</strong></h6>
                <div v-if="diaChi" class="p-3 bg-light rounded border">
                  <p class="mb-1"><strong>Người nhận:</strong> {{ diaChi.tenNN || 'Không có thông tin' }}</p>
                  <p class="mb-1"><strong>SĐT:</strong> {{ diaChi.sdt || 'Không có thông tin' }}</p>
                  <p class="mb-0"><strong>Địa chỉ:</strong> {{ diaChi.diemGiao || 'Không có thông tin' }}</p>
                </div>
                <div v-else-if="order.diaChiJson && typeof order.diaChiJson === 'string'" class="p-3 bg-light rounded border">
                  <p class="mb-0">{{ order.diaChiJson }}</p>
                </div>
                <div v-else class="p-3 bg-light rounded border text-muted">
                  <p class="mb-0">Không có thông tin địa chỉ</p>
                </div>
                <div v-if="order.voucherApDung" class="mt-3 p-3 bg-light rounded border">
                  <h6><strong>Voucher áp dụng:</strong></h6>
                  <p class="mb-1"><strong>Tên voucher:</strong> {{ order.voucherApDung.tenVoucher }}</p>
                  <p class="mb-1"><strong>Giảm giá:</strong> {{ formatPrice(order.voucherApDung.giaTriGiam) }}</p>
                  <p v-if="order.voucherApDung.donToiThieu && order.voucherApDung.donToiThieu > 0" class="mb-0">
                    <strong>Đơn tối thiểu:</strong> {{ formatPrice(order.voucherApDung.donToiThieu) }}
                  </p>
                </div>
                <div v-if="diaChiError" class="alert alert-warning mt-2">{{ diaChiError }}</div>
              </div>
            </div>
          </div>
        </div>

        <div class="card">
          <div class="card-header">
            <h5 class="mb-0">Danh Sách Sản Phẩm</h5>
          </div>
          <div class="table-responsive">
            <table class="table table-hover mb-0">
              <thead class="table-light">
              <tr>
                <th class="ps-4 col-sm-5 text-start">Sản phẩm</th>
                <th class="col-sm-1 text-center">Số lượng</th>
                <th class="col-sm-2 text-center">Đơn giá</th>
                <th class="col-sm-2 text-center">Thành tiền</th>
                <th class="col-sm-2 text-center">Đánh giá</th>
              </tr>
              </thead>
              <tbody>
              <template v-if="order.hoaDonCTs && order.hoaDonCTs.length">
                <tr v-for="item in order.hoaDonCTs" :key="item.maHDCT" class="align-middle">
                  <td>
                    <div class="ps-3 col-sm-6 d-flex align-items-center" style="height: 70px;">
                      <div class="product-image-wrapper">
                        <img :src="getImageUrl(item.hinhAnh)" alt="Hình sản phẩm" class="product-image" @error="handleImageError">
                      </div>
                      <div class="product-name-container ms-3">
                          <span class="product-name-text" :title="item.plSanPham?.sanPham?.tenSP || item.plSanPham?.tenSP || item.tenSP">
                            {{ item.plSanPham?.sanPham?.tenSP || item.plSanPham?.tenSP || item.tenSP }}
                          </span>
                        <small class="product-variant-text" v-if="item.plSanPham?.phanLoai || item.tenMau || item.size"
                               :title="`Phân loại: ${item.plSanPham?.phanLoai || ''} ${item.tenMau ? item.tenMau : ''} ${item.size ? 'Size ' + item.size : ''}`">
                          {{ item.plSanPham?.phanLoai || '' }}
                          {{ item.tenMau ? item.tenMau : '' }}
                          {{ item.size ? 'Size ' + item.size : '' }}
                        </small>
                      </div>
                    </div>
                  </td>
                  <td class="col-sm-1 text-center" style="height: 70px;">{{ item.soLuong }}</td>
                  <td class="col-sm-2 text-center" style="height: 70px;">
                    <div>
                      <template v-if="item.khuyenMaiPhanTram && item.khuyenMaiPhanTram > 0">
                          <span class="text-muted text-decoration-line-through">
                            {{ formatPrice(item.giaGoc) }}
                          </span>
                        <div class="fw-bold text-danger">{{ formatPrice(item.donGia) }}</div>
                        <small class="text-success">-{{ item.khuyenMaiPhanTram }}%</small>
                      </template>
                      <template v-else>
                        <span class="fw-bold">{{ formatPrice(item.giaGoc || item.donGia) }}</span>
                      </template>
                    </div>
                  </td>
                  <td class="col-sm-2 text-center" style="height: 70px;">
                    <div>
                      <template v-if="item.khuyenMaiPhanTram && item.khuyenMaiPhanTram > 0">
                          <span class="text-muted text-decoration-line-through">
                            {{ formatPrice(item.thanhTienGoc) }}
                          </span>
                        <div class="fw-bold text-danger">{{ formatPrice(item.thanhTienSauKmSp) }}</div>
                        <small class="text-muted">(Tiết kiệm: {{ formatPrice(item.giamGiaSp) }})</small>
                      </template>
                      <template v-else>
                        <span class="fw-bold">{{ formatPrice(item.thanhTienGoc || (item.soLuong * item.donGia)) }}</span>
                      </template>
                    </div>
                  </td>
                  <td class="col-sm-2 text-center d-flex justify-content-end" style="height: 70px; min-width: 120px;">
                    <div v-if="item.daDanhGia">
                      <button class="btn btn-outline-warning btn-sm" @click="openViewReviewModal(item)" title="Xem đánh giá">
                        <i class="bi bi-star-fill text-warning me-1"></i>Đã đánh giá
                      </button>
                    </div>
                    <button v-else-if="canReview(item)" class="btn btn-outline-warning btn-sm" @click="openCreateReviewModal(item)">
                      <i class="bi bi-star me-1"></i>Đánh giá
                    </button>
                  </td>
                </tr>
              </template>

              <template v-else-if="order.chiTiet && order.chiTiet.length">
                <tr v-for="(item, index) in order.chiTiet" :key="index" class="align-middle">
                  <td>
                    <div class="ps-3 col-sm-5 d-flex align-items-center" style="height: 70px;">
                      <div class="product-image-wrapper">
                        <img :src="getImageUrl(item.hinhAnh)" alt="Hình sản phẩm" class="product-image" @error="handleImageError">
                      </div>
                      <div class="product-name-container ms-3">
                        <span class="product-name-text" :title="item.tenSP">{{ item.tenSP }}</span>
                        <small class="product-variant-text" v-if="item.tenMau || item.size" :title="`Phân loại: ${item.tenMau || ''} ${item.size ? 'Size ' + item.size : ''}`">
                          {{ item.tenMau || '' }} {{ item.size ? 'Size ' + item.size : '' }}
                        </small>
                      </div>
                    </div>
                  </td>
                  <td class="col-sm-1 text-center" style="height: 70px;">{{ item.soLuong }}</td>
                  <td class="col-sm-2 text-center" style="height: 70px;">
                    <div class="price-info">
                      <template v-if="item.khuyenMaiPhanTram && item.khuyenMaiPhanTram > 0">
                          <span class="text-muted text-decoration-line-through d-block small">
                            {{ formatPrice(item.giaGoc) }}
                          </span>
                        <span class="fw-bold text-danger">
                            {{ formatPrice(item.donGia) }}
                          </span>
                        <small class="text-success d-block">
                          <i class="bi bi-tag"></i> -{{ item.khuyenMaiPhanTram }}%
                        </small>
                      </template>
                      <template v-else>
                          <span class="fw-bold">
                            {{ formatPrice(item.giaGoc) }}
                          </span>
                      </template>
                    </div>
                  </td>
                  <td class="col-sm-2 text-center" style="height: 70px;">
                    <div class="total-info">
                      <template v-if="item.khuyenMaiPhanTram && item.khuyenMaiPhanTram > 0">
                          <span class="text-muted text-decoration-line-through d-block small">
                            {{ formatPrice(item.thanhTienGoc) }}
                          </span>
                        <span class="fw-bold text-danger">
                            {{ formatPrice(item.thanhTienSauKmSp) }}
                          </span>
                        <small class="text-success d-block">
                          <i class="bi bi-piggy-bank"></i> Tiết kiệm: {{ formatPrice(item.giamGiaSp) }}
                        </small>
                      </template>
                      <template v-else>
                          <span class="fw-bold">
                            {{ formatPrice(item.thanhTienGoc) }}
                          </span>
                      </template>
                    </div>
                  </td>
                  <td class="col-sm-2 text-center" style="height: 70px; min-width: 120px;">
                    <div v-if="item.daDanhGia">
                      <button class="btn btn-outline-warning btn-sm" @click="openViewReviewModal(item)" title="Xem đánh giá">
                        <i class="bi bi-star-fill text-warning me-1"></i>Đã đánh giá
                      </button>
                    </div>
                    <button v-else-if="canReview(item)" class="btn btn-outline-warning btn-sm" @click="openCreateReviewModal(item)">
                      <i class="bi bi-star me-1"></i>Đánh giá
                    </button>
                  </td>
                </tr>
              </template>
              </tbody>
            </table>
          </div>
          <div class="card-footer">
            <div class="border-bottom pb-2 mb-2">
              <h6 class="mb-0 fw-bold">CHI TIẾT THANH TOÁN</h6>
            </div>

            <div class="d-flex justify-content-between align-items-center mb-2">
              <span class="text-muted">Tổng tiền gốc:</span>
              <span class="fw-bold fs-5">{{ formatPrice(order.tongTienGoc) }}</span>
            </div>

            <div v-if="order.tongGiamGiaKmSp && order.tongGiamGiaKmSp > 0" class="d-flex justify-content-between align-items-center mb-2 text-success">
              <span>
                <i class="bi bi-tag-fill me-1"></i>
                Giảm giá từ khuyến mãi sản phẩm:
              </span>
              <span class="fw-bold fs-5">-{{ formatPrice(order.tongGiamGiaKmSp) }}</span>
            </div>

            <div v-if="order.tongGiamGiaVoucher && order.tongGiamGiaVoucher > 0" class="d-flex justify-content-between align-items-center mb-2 text-success">
              <span>
                <i class="bi bi-ticket-perforated-fill me-1"></i>
                Giảm giá từ voucher ({{ order.voucherApDung?.tenVoucher }}):
              </span>
              <span class="fw-bold fs-5">-{{ formatPrice(order.tongGiamGiaVoucher) }}</span>
            </div>

            <div class="d-flex justify-content-between align-items-center mt-3 pt-2 border-top">
              <span class="fw-bold fs-5">Tổng tiền thanh toán:</span>
              <h4 class="mb-0 text-danger fw-bold">{{ formatPrice(order.tongTien) }}</h4>
            </div>

            <div class="d-flex justify-content-between align-items-center mt-2">
              <span class="fw-bold">Số tiền tiết kiệm được:</span>
              <h4 class="mb-0 text-success fw-bold">
                {{ formatPrice((order.tongTienGoc || 0) - (order.tongTien || 0)) }}
              </h4>
            </div>
            <small class="text-muted d-block text-end mt-1">
              ({{ ((order.tongGiamGiaKmSp || 0) + (order.tongGiamGiaVoucher || 0)) > 0 ?
                Math.round(((order.tongGiamGiaKmSp || 0) + (order.tongGiamGiaVoucher || 0)) / (order.tongTienGoc || 1) * 100) : 0 }}% so với giá gốc)
            </small>
          </div>
        </div>

        <div class="mt-3 d-flex justify-content-end align-items-center gap-2 flex-wrap">
          <button v-if="canCancelOrder(order)" class="btn btn-danger m-1" @click="openCancelModal(order)" :disabled="cancellingOrderId === order.maHD">
            <span v-if="cancellingOrderId === order.maHD" class="spinner-border spinner-border-sm me-2"></span>
            <i class="bi bi-x-circle me-1"></i> Hủy đơn hàng
          </button>
          <button class="btn btn-warning" @click="openReportIssueModal(order)" :disabled="reportingOrderId === order?.maHD" v-if="canReportIssue(order)">
            <span v-if="reportingOrderId === order?.maHD" class="spinner-border spinner-border-sm me-2"></span>
            <i class="bi bi-exclamation-triangle me-1"></i> Báo lỗi
          </button>
          <router-link to="/customer/orders" class="btn btn-outline-secondary">
            <i class="bi bi-arrow-left"></i> Quay Lại
          </router-link>
        </div>

        <div v-if="canReportIssue(order)" class="mt-2">
          <div class="d-flex justify-content-end">
            <div class="report-info d-flex align-items-center gap-3 flex-wrap justify-content-end">
              <div class="timer-display-small">
                <i class="bi bi-clock-history me-1"></i>
                <span>Còn {{ remainingTime.formatted }}</span>
              </div>
              <div class="deadline-info-small">
                <i class="bi bi-calendar-check me-1"></i>
                Hạn: {{ formatDeadline }}
              </div>
            </div>
          </div>
        </div>
      </div>
    </main>

    <!-- Modal Hủy đơn hàng -->
    <div v-if="showCancelModal" class="modal fade show d-block" tabindex="-1" style="background-color: rgba(0,0,0,0.5);">
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header bg-danger text-white">
            <h5 class="modal-title">
              <i class="bi bi-x-circle me-2"></i>Hủy đơn hàng
            </h5>
            <button type="button" class="btn-close btn-close-white" @click="closeCancelModal"></button>
          </div>
          <div class="modal-body">
            <div v-if="selectedCancelOrder">
              <p class="mb-3"><strong>Mã đơn hàng:</strong> #{{ selectedCancelOrder.maHD }}</p>
              <p class="mb-3"><strong>Ngày đặt:</strong> {{ formatDate(selectedCancelOrder.ngayMua) }}</p>
              <p class="mb-3"><strong>Tổng tiền:</strong> {{ formatPrice(totalPrice) }}</p>

              <div class="mb-3">
                <label class="form-label fw-bold">Lý do hủy đơn <span class="text-danger">*</span></label>
                <select class="form-select" v-model="cancelReason">
                  <option value="">-- Chọn lý do hủy --</option>
                  <option value="Đặt nhầm sản phẩm">Đặt nhầm sản phẩm</option>
                  <option value="Thay đổi ý định mua hàng">Thay đổi ý định mua hàng</option>
                  <option value="Tìm thấy sản phẩm giá tốt hơn">Tìm thấy sản phẩm giá tốt hơn</option>
                  <option value="Thời gian giao hàng quá lâu">Thời gian giao hàng quá lâu</option>
                  <option value="Sản phẩm không còn nhu cầu">Sản phẩm không còn nhu cầu</option>
                  <option value="Lý do khác">Lý do khác</option>
                </select>
              </div>

              <div class="mb-3">
                <label class="form-label">Mô tả chi tiết (tùy chọn)</label>
                <textarea class="form-control" rows="3" v-model="cancelNote" placeholder="Vui lòng mô tả chi tiết lý do hủy đơn hàng..."></textarea>
              </div>
            </div>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" @click="closeCancelModal">
              <i class="bi bi-arrow-left me-1"></i>Quay lại
            </button>
            <button type="button" class="btn btn-danger" @click="submitCancelOrder" :disabled="!cancelReason || cancelling">
              <span v-if="cancelling" class="spinner-border spinner-border-sm me-2"></span>
              <i class="bi bi-check-circle me-1"></i>Xác nhận hủy
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Modal báo lỗi -->
    <div v-if="showReportIssueModal" class="modal fade show d-block" tabindex="-1" style="background-color: rgba(0,0,0,0.5);">
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header bg-danger text-white">
            <h5 class="modal-title"><i class="bi bi-exclamation-triangle me-2"></i>Báo lỗi đơn hàng</h5>
            <button type="button" class="btn-close btn-close-white" @click="closeReportIssueModal"></button>
          </div>
          <div class="modal-body">
            <div v-if="selectedOrder">
              <p><strong>Mã đơn hàng:</strong> #{{ selectedOrder.maHD }}</p>
              <p><strong>Ngày mua:</strong> {{ formatDate(selectedOrder.ngayMua) }}</p>
              <p><strong>Ngày nhận:</strong> {{ formatDate(selectedOrder.ngayDen) }}</p>
              <div class="mb-3">
                <label class="form-label fw-bold">Lý do báo lỗi <span class="text-danger">*</span></label>
                <select class="form-select" v-model="reportReason">
                  <option value="">-- Chọn lý do --</option>
                  <option value="Sản phẩm bị lỗi">Sản phẩm bị lỗi</option>
                  <option value="Sai kích thước">Sai kích thước</option>
                  <option value="Sai màu sắc">Sai màu sắc</option>
                  <option value="Sai mẫu mã">Sai mẫu mã</option>
                  <option value="Giao thiếu hàng">Giao thiếu hàng</option>
                  <option value="Hàng bị hư hỏng">Hàng bị hư hỏng</option>
                  <option value="Lý do khác">Lý do khác</option>
                </select>
              </div>
              <div class="mb-3">
                <label class="form-label">Mô tả chi tiết</label>
                <textarea class="form-control" rows="4" v-model="reportNote" placeholder="Vui lòng mô tả chi tiết vấn đề bạn gặp phải..."></textarea>
              </div>
            </div>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" @click="closeReportIssueModal">Hủy</button>
            <button type="button" class="btn btn-danger" @click="submitReportIssue" :disabled="!reportReason || reporting">
              <span v-if="reporting" class="spinner-border spinner-border-sm me-2"></span> Gửi báo lỗi
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Modal đánh giá: tạo mới, xem -->
    <div v-if="showReviewModal" class="modal fade show d-block" tabindex="-1" style="background-color: rgba(0,0,0,0.5);">
      <div class="modal-dialog">
        <div class="modal-content review-modal">
          <div class="modal-header">
            <h5 class="modal-title">
              <i class="bi" :class="reviewModalMode === 'view' ? 'bi-star-fill' : 'bi-star-fill'"></i>
              {{ reviewModalMode === 'view' ? 'Chi tiết đánh giá' : 'Đánh giá sản phẩm' }}
            </h5>
            <button type="button" class="close-btn" @click="closeReviewModal">
              <i class="bi bi-x-lg"></i>
            </button>
          </div>
          <div class="modal-body">
            <div v-if="reviewModalLoading" class="text-center py-4">
              <div class="spinner-border text-primary" role="status">
                <span class="visually-hidden">Loading...</span>
              </div>
              <p class="mt-2 text-muted">Đang tải thông tin...</p>
            </div>

            <div v-else-if="reviewModalData">
              <div class="product-info-section mb-3">
                <div class="d-flex align-items-center">
                  <img :src="getImageUrl(reviewModalData.productInfo?.hinhAnh)" alt="Product" class="product-thumbnail me-3">
                  <div class="product-info">
                    <h6 class="product-name mb-1">{{ reviewModalData.productInfo?.tenSP }}</h6>
                    <div class="product-variant">
                      <span class="badge-variant" v-if="reviewModalData.productInfo?.tenMau">
                        <i class="bi bi-palette-fill me-1"></i>{{ reviewModalData.productInfo.tenMau }}
                      </span>
                      <span class="badge-variant" v-if="reviewModalData.productInfo?.size">
                        <i class="bi bi-rulers me-1"></i>Size {{ reviewModalData.productInfo.size }}
                      </span>
                    </div>
                  </div>
                </div>
              </div>

              <template v-if="reviewModalMode === 'view'">
                <div class="reviewer-info d-flex justify-content-between align-items-center mb-3 pb-2 border-bottom">
                  <div class="reviewer">
                    <i class="bi bi-person-circle me-2"></i>
                    <strong>{{ reviewModalData.tenKH || 'Khách hàng' }}</strong>
                  </div>
                  <div class="review-date">
                    <i class="bi bi-calendar3 me-1"></i>
                    {{ formatFullDateTime(reviewModalData.ngayDG) }}
                  </div>
                </div>
                <div class="rating-section text-center mb-4">
                  <div class="stars-container mb-2">
                    <span v-for="star in 5" :key="star" class="review-star">
                      <i :class="star <= reviewModalData.sao ? 'bi bi-star-fill text-warning' : 'bi bi-star text-secondary'"></i>
                    </span>
                  </div>
                  <div class="rating-score">
                    <div class="score-number-wrapper">
                      <span class="score-number">{{ reviewModalData.sao }}/5</span>
                    </div>
                  </div>
                </div>
                <div class="review-content-section mb-4">
                  <div class="review-label">
                    <i class="bi bi-chat-dots-fill me-2"></i>
                    <strong>Nhận xét chi tiết</strong>
                  </div>
                  <div class="review-text">
                    <p>{{ reviewModalData.danhGiaCT || 'Không có nhận xét chi tiết.' }}</p>
                  </div>
                </div>
              </template>

              <template v-else>
                <div class="rating-section mb-3">
                  <label class="form-label fw-bold mb-2">Đánh giá của bạn <span class="text-danger">*</span></label>
                  <div class="stars-container">
                    <span v-for="star in 5" :key="star" class="star-rating"
                          @click="reviewFormRating = star"
                          @mouseover="reviewFormHoverRating = star"
                          @mouseleave="reviewFormHoverRating = 0">
                      <i :class="star <= (reviewFormHoverRating || reviewFormRating) ? 'bi bi-star-fill text-warning' : 'bi bi-star text-secondary'"></i>
                    </span>
                  </div>
                  <div class="rating-score mt-2">
                    <div class="score-number-wrapper text-end">
                      <span class="score-number">{{ reviewFormRating }}/5 sao</span>
                    </div>
                  </div>
                  <div v-if="reviewFormRating > 0 && reviewFormRating <= 3" class="alert alert-warning mt-3 py-2">
                    <i class="bi bi-info-circle me-1"></i>
                    <small>Với đánh giá {{ reviewFormRating }} sao, vui lòng cung cấp nhận xét chi tiết.</small>
                  </div>
                </div>
                <div class="review-content-section">
                  <label class="form-label fw-bold">
                    Nhận xét của bạn
                    <span v-if="reviewFormRating <= 3" class="text-danger">*</span>
                    <span v-else class="text-muted">(không bắt buộc)</span>
                  </label>
                  <textarea class="form-control" rows="4" v-model="reviewFormComment"
                            :placeholder="reviewFormRating <= 3 ? 'Vui lòng chia sẻ cảm nhận chi tiết của bạn về sản phẩm...' : 'Chia sẻ cảm nhận của bạn về sản phẩm...'"
                            style="border-radius: 8px; border: 1px solid #e0e0e0;"></textarea>
                  <small class="text-muted d-block mt-1" v-if="reviewFormRating <= 3 && reviewFormComment && reviewFormComment.trim() !== ''">
                    <i class="bi bi-check-circle-fill text-success"></i> Đã nhập nhận xét
                  </small>
                  <small class="text-danger d-block mt-1" v-else-if="reviewFormRating <= 3 && (!reviewFormComment || reviewFormComment.trim() === '')">
                    <i class="bi bi-exclamation-triangle-fill"></i> Vui lòng nhập nhận xét
                  </small>
                </div>
              </template>
            </div>
          </div>
          <div class="modal-footer">
            <template v-if="reviewModalMode === 'view'">
              <button type="button" class="btn btn-outline-secondary" @click="closeReviewModal">
                <i class="bi bi-x-circle me-1"></i>Đóng
              </button>
            </template>
            <template v-else>
              <button type="button" class="btn btn-outline-secondary" @click="closeReviewModal">
                <i class="bi bi-x-circle me-1"></i>Hủy
              </button>
              <button type="button" class="btn btn-primary" @click="submitReviewForm"
                      :disabled="reviewFormRating === 0 || reviewFormSubmitting || (reviewFormRating <= 3 && (!reviewFormComment || reviewFormComment.trim() === ''))">
                <span v-if="reviewFormSubmitting" class="spinner-border spinner-border-sm me-2"></span>
                <i class="bi bi-check-circle me-1"></i>Gửi đánh giá
              </button>
            </template>
          </div>
        </div>
      </div>
    </div>

    <!-- Modal thông báo thành công -->
    <div v-if="showSuccessModal" class="modal fade show d-block" tabindex="-1" style="background-color: rgba(0,0,0,0.5);">
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content">
          <div class="modal-header bg-success text-white">
            <h5 class="modal-title"><i class="bi bi-check-circle me-2"></i>Thành công</h5>
            <button type="button" class="btn-close btn-close-white" @click="showSuccessModal = false"></button>
          </div>
          <div class="modal-body text-center py-4">
            <i class="bi bi-check-circle-fill text-success" style="font-size: 4rem;"></i>
            <h5 class="mt-3">{{ successMessage }}</h5>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-success" @click="showSuccessModal = false">
              <i class="bi bi-check me-1"></i>Đóng
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Modal thông báo lỗi -->
    <div v-if="showErrorModal" class="modal fade show d-block" tabindex="-1" style="background-color: rgba(0,0,0,0.5);">
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content">
          <div class="modal-header bg-danger text-white">
            <h5 class="modal-title"><i class="bi bi-exclamation-triangle me-2"></i>Lỗi</h5>
            <button type="button" class="btn-close btn-close-white" @click="showErrorModal = false"></button>
          </div>
          <div class="modal-body text-center py-4">
            <i class="bi bi-x-circle-fill text-danger" style="font-size: 4rem;"></i>
            <h5 class="mt-3">{{ errorMessage }}</h5>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-danger" @click="showErrorModal = false">
              <i class="bi bi-x me-1"></i>Đóng
            </button>
          </div>
        </div>
      </div>
    </div>

    <Footer />
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, computed, watch } from 'vue';
import { useRoute } from 'vue-router';
import api from '@/services/api';
import KH_Navbar from '@/components/shared/KH_Navbar.vue';
import Footer from '@/components/shared/Footer.vue';

const route = useRoute();
const order = ref(null);
const diaChi = ref(null);
const loading = ref(false);
const error = ref('');
const diaChiError = ref('');
const reportingOrderId = ref(null);
const cancellingOrderId = ref(null);

// Modal hủy đơn
const showCancelModal = ref(false);
const cancelling = ref(false);
const selectedCancelOrder = ref(null);
const cancelReason = ref('');
const cancelNote = ref('');

// Modal báo lỗi
const showReportIssueModal = ref(false);
const selectedOrder = ref(null);
const reportReason = ref('');
const reportNote = ref('');
const reporting = ref(false);

// Modal đánh giá
const showReviewModal = ref(false);
const reviewModalMode = ref('create'); // 'create', 'view'
const reviewModalData = ref(null);
const reviewModalLoading = ref(false);
const reviewFormRating = ref(0);
const reviewFormHoverRating = ref(0);
const reviewFormComment = ref('');
const reviewFormSubmitting = ref(false);

// Modal thông báo
const showSuccessModal = ref(false);
const successMessage = ref('');
const showErrorModal = ref(false);
const errorMessage = ref('');

// Timer
let timerInterval = null;
const timeTick = ref(Date.now());

const startTimer = () => {
  if (timerInterval) clearInterval(timerInterval);
  timerInterval = setInterval(() => {
    timeTick.value = Date.now();
  }, 1000);
};

const stopTimer = () => {
  if (timerInterval) {
    clearInterval(timerInterval);
    timerInterval = null;
  }
};

const fetchOrderDetail = async () => {
  loading.value = true;
  error.value = '';
  diaChiError.value = '';

  try {
    const orderId = route.params.id;
    const response = await api.getOrderDetail(orderId);

    if (response.data.success) {
      order.value = response.data.order || response.data;

      if (order.value.diaChiJson) {
        try {
          if (typeof order.value.diaChiJson === 'string') {
            const parsed = JSON.parse(order.value.diaChiJson);
            diaChi.value = {
              tenNN: parsed.TenNN || parsed.tenNN || '',
              sdt: parsed.SDT || parsed.sdt || '',
              diemGiao: parsed.DiemGiao || parsed.diemGiao || ''
            };
          } else {
            const parsed = order.value.diaChiJson;
            diaChi.value = {
              tenNN: parsed.TenNN || parsed.tenNN || '',
              sdt: parsed.SDT || parsed.sdt || '',
              diemGiao: parsed.DiemGiao || parsed.diemGiao || ''
            };
          }
        } catch (e) {
          diaChiError.value = 'Lỗi parse địa chỉ';
          diaChi.value = {
            tenNN: '',
            sdt: '',
            diemGiao: order.value.diaChiJson.substring(0, 100) + '...'
          };
        }
      } else {
        diaChi.value = {
          tenNN: order.value.tenNguoiNhan || order.value.tenNN || '',
          sdt: order.value.sdtNguoiNhan || order.value.sdt || '',
          diemGiao: order.value.diaChiGiaoHang || order.value.diemGiao || ''
        };
      }
    } else {
      error.value = response.data.message || 'Không tìm thấy đơn hàng';
    }
  } catch (err) {
    error.value = err.response?.data?.message || 'Lỗi khi tải chi tiết đơn hàng';
  } finally {
    loading.value = false;
  }
};

const canCancelOrder = (order) => {
  if (!order) return false;
  return order.trangThai === 'Đang xử lý';
};

const canReportIssue = (order) => {
  if (!order) return false;
  if (order.trangThai === 'Hoàn tất') {
    const receivedDate = order.ngayDen ? new Date(order.ngayDen) : new Date(order.ngayMua);
    const deadline = new Date(receivedDate);
    deadline.setDate(deadline.getDate() + 30);
    return new Date() <= deadline;
  }
  return false;
};

const remainingTime = computed(() => {
  const _ = timeTick.value;
  if (!order.value || !canReportIssue(order.value)) {
    return { formatted: '00 ngày 00:00:00' };
  }
  const receivedDate = order.value.ngayDen ? new Date(order.value.ngayDen) : new Date(order.value.ngayMua);
  const deadline = new Date(receivedDate);
  deadline.setDate(deadline.getDate() + 30);
  const diffMs = deadline - new Date();
  if (diffMs <= 0) return { formatted: '00 ngày 00:00:00' };
  const totalSeconds = Math.floor(diffMs / 1000);
  const days = Math.floor(totalSeconds / (24 * 60 * 60));
  const hours = Math.floor((totalSeconds % (24 * 60 * 60)) / (60 * 60));
  const minutes = Math.floor((totalSeconds % (60 * 60)) / 60);
  const seconds = totalSeconds % 60;
  return {
    formatted: `${String(days).padStart(2, '0')} ngày ${String(hours).padStart(2, '0')}:${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`
  };
});

const formatDeadline = computed(() => {
  if (!order.value) return '';
  const receivedDate = order.value.ngayDen ? new Date(order.value.ngayDen) : new Date(order.value.ngayMua);
  const deadline = new Date(receivedDate);
  deadline.setDate(deadline.getDate() + 30);
  return formatFullDateTime(deadline);
});

const formatFullDateTime = (dateString) => {
  if (!dateString) return '';
  const date = new Date(dateString);
  return date.toLocaleDateString('vi-VN', {
    day: '2-digit', month: '2-digit', year: 'numeric',
    hour: '2-digit', minute: '2-digit', second: '2-digit', hour12: false
  }).replace(',', '');
};

const canReview = (item) => {
  if (!order.value) return false;
  if (order.value.trangThai !== 'Hoàn tất') return false;
  return !item.daDanhGia;
};

const openCancelModal = (orderItem) => {
  selectedCancelOrder.value = orderItem;
  cancelReason.value = '';
  cancelNote.value = '';
  showCancelModal.value = true;
};

const closeCancelModal = () => {
  showCancelModal.value = false;
  selectedCancelOrder.value = null;
  cancelReason.value = '';
  cancelNote.value = '';
};

const submitCancelOrder = async () => {
  if (!cancelReason.value) {
    errorMessage.value = 'Vui lòng chọn lý do hủy đơn';
    showErrorModal.value = true;
    return;
  }

  cancelling.value = true;
  cancellingOrderId.value = selectedCancelOrder.value?.maHD;

  try {
    const fullReason = cancelNote.value
        ? `${cancelReason.value} - ${cancelNote.value}`
        : cancelReason.value;

    const response = await api.cancelOrder(selectedCancelOrder.value?.maHD, fullReason);

    if (response.data.success) {
      closeCancelModal();
      successMessage.value = 'Đơn hàng đã được hủy thành công!';
      showSuccessModal.value = true;

      if (order.value && order.value.maHD === selectedCancelOrder.value?.maHD) {
        order.value.trangThai = 'Đã từ chối';
        order.value.ghiChu = fullReason;
      }

      setTimeout(() => {
        fetchOrderDetail();
      }, 1500);
    } else {
      errorMessage.value = response.data.message || 'Không thể hủy đơn hàng';
      showErrorModal.value = true;
    }
  } catch (err) {
    errorMessage.value = err.response?.data?.message || err.message || 'Lỗi khi hủy đơn hàng';
    showErrorModal.value = true;
  } finally {
    cancelling.value = false;
    cancellingOrderId.value = null;
  }
};

const openReportIssueModal = (orderItem) => {
  selectedOrder.value = orderItem;
  reportReason.value = '';
  reportNote.value = '';
  showReportIssueModal.value = true;
};

const closeReportIssueModal = () => {
  showReportIssueModal.value = false;
  selectedOrder.value = null;
  reportReason.value = '';
  reportNote.value = '';
};

const submitReportIssue = async () => {
  if (!reportReason.value) {
    errorMessage.value = 'Vui lòng chọn lý do báo lỗi';
    showErrorModal.value = true;
    return;
  }
  reporting.value = true;
  reportingOrderId.value = selectedOrder.value?.maHD;
  try {
    const response = await api.reportIssue({
      orderId: selectedOrder.value?.maHD,
      reason: reportReason.value,
      note: reportNote.value
    });
    if (response.data.success) {
      closeReportIssueModal();
      successMessage.value = 'Báo lỗi đã được ghi nhận!';
      showSuccessModal.value = true;
      if (order.value && order.value.maHD === selectedOrder.value?.maHD) {
        order.value.trangThai = 'Báo lỗi';
      }
      setTimeout(() => fetchOrderDetail(), 1000);
    } else {
      errorMessage.value = response.data.message || 'Không thể gửi báo lỗi';
      showErrorModal.value = true;
    }
  } catch (err) {
    errorMessage.value = err.response?.data?.message || 'Lỗi khi gửi báo lỗi';
    showErrorModal.value = true;
  } finally {
    reporting.value = false;
    reportingOrderId.value = null;
  }
};

const openCreateReviewModal = (product) => {
  reviewModalMode.value = 'create';
  reviewModalData.value = {
    productInfo: {
      tenSP: product.tenSP,
      tenMau: product.tenMau,
      size: product.size,
      hinhAnh: product.hinhAnh
    },
    maHDCT: product.maHDCT
  };
  reviewFormRating.value = 0;
  reviewFormComment.value = '';
  showReviewModal.value = true;
};

const openViewReviewModal = (product) => {
  reviewModalMode.value = 'view';
  reviewModalLoading.value = true;
  showReviewModal.value = true;

  if (product.danhGia) {
    reviewModalData.value = {
      ...product.danhGia,
      productInfo: {
        tenSP: product.tenSP,
        tenMau: product.tenMau,
        size: product.size,
        hinhAnh: product.hinhAnh
      }
    };
    reviewModalLoading.value = false;
  } else {
    fetchReviewDetailForModal(product.maHDCT, product);
  }
};

const closeReviewModal = () => {
  showReviewModal.value = false;
  reviewModalMode.value = 'create';
  reviewModalData.value = null;
  reviewFormRating.value = 0;
  reviewFormComment.value = '';
  reviewFormHoverRating.value = 0;
  reviewModalLoading.value = false;
};

const submitReviewForm = async () => {
  if (reviewFormRating.value === 0) {
    errorMessage.value = 'Vui lòng chọn số sao đánh giá';
    showErrorModal.value = true;
    return;
  }
  if (reviewFormRating.value <= 3 && (!reviewFormComment.value || reviewFormComment.value.trim() === '')) {
    errorMessage.value = 'Vui lòng nhập nhận xét chi tiết cho đánh giá ' + reviewFormRating.value + ' sao';
    showErrorModal.value = true;
    return;
  }

  reviewFormSubmitting.value = true;
  try {
    const response = await api.addReview({
      maHDCT: reviewModalData.value.maHDCT,
      sao: reviewFormRating.value,
      danhGiaCT: reviewFormComment.value
    });

    if (response.data.success) {
      closeReviewModal();
      successMessage.value = 'Cảm ơn bạn đã đánh giá sản phẩm!';
      showSuccessModal.value = true;
      setTimeout(() => fetchOrderDetail(), 1000);
    } else {
      errorMessage.value = response.data.message || 'Không thể xử lý đánh giá';
      showErrorModal.value = true;
    }
  } catch (err) {
    errorMessage.value = err.response?.data?.message || 'Lỗi khi xử lý đánh giá';
    showErrorModal.value = true;
  } finally {
    reviewFormSubmitting.value = false;
  }
};

const fetchReviewDetailForModal = async (maHDCT, product) => {
  try {
    const response = await api.getReview(maHDCT);
    if (response.data.success && response.data.danhGia) {
      reviewModalData.value = {
        ...response.data.danhGia,
        productInfo: {
          tenSP: product?.tenSP || 'Sản phẩm',
          tenMau: product?.tenMau || '',
          size: product?.size || '',
          hinhAnh: product?.hinhAnh || ''
        }
      };
    } else {
      errorMessage.value = 'Không tìm thấy đánh giá';
      showErrorModal.value = true;
      closeReviewModal();
    }
  } catch (err) {
    errorMessage.value = err.response?.data?.message || 'Lỗi khi lấy đánh giá';
    showErrorModal.value = true;
    closeReviewModal();
  } finally {
    reviewModalLoading.value = false;
  }
};

const getImageUrl = (imageName) => {
  if (!imageName) return 'https://via.placeholder.com/70';
  if (imageName.startsWith('http')) return imageName;
  if (imageName.startsWith('/')) return imageName;
  return `/images/${imageName}`;
};

const handleImageError = (e) => {
  e.target.src = 'https://via.placeholder.com/70';
};

const formatPrice = (price) => {
  if (!price) return '0 ₫';
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(price);
};

const formatDate = (dateString) => {
  if (!dateString) return '';
  const date = new Date(dateString);
  return date.toLocaleDateString('vi-VN', {
    day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit'
  });
};

const getStatusClass = (status) => {
  switch (status) {
    case 'Hoàn tất': return 'badge bg-success';
    case 'Đang giao': return 'badge bg-primary';
    case 'Đang xử lý': return 'badge bg-warning text-dark';
    case 'Đã từ chối': return 'badge bg-danger';
    case 'Báo lỗi': return 'badge bg-dark';
    case 'Hoàn hàng/trả hàng': return 'badge bg-secondary';
    default: return 'badge bg-secondary';
  }
};

const totalPrice = computed(() => {
  if (!order.value) return 0;
  if (order.value.hoaDonCTs && Array.isArray(order.value.hoaDonCTs)) {
    return order.value.hoaDonCTs.reduce((total, item) => total + (item.soLuong * item.donGia), 0);
  }
  if (order.value.chiTiet && Array.isArray(order.value.chiTiet)) {
    return order.value.chiTiet.reduce((total, item) => total + (item.soLuong * item.donGia), 0);
  }
  return order.value.tongTien || 0;
});

watch(() => order.value, (newOrder) => {
  if (newOrder && canReportIssue(newOrder)) {
    startTimer();
  } else {
    stopTimer();
  }
}, { immediate: true });

onBeforeUnmount(() => {
  stopTimer();
});

onMounted(() => {
  fetchOrderDetail();
});
</script>

<style scoped>
/* ========== CARD STYLES ========== */
.card {
  border: 2px solid #000000;
  border-radius: 10px;
  overflow: hidden;
}

.card-header {
  font-weight: bold;
  background-color: #000000;
  color: #ffffff;
  border-bottom: 2px solid #ffffff;
  padding: 1rem 1.25rem;
}

.card-header h5 {
  margin: 0;
  font-weight: 600;
}

.card-body {
  padding: 1.25rem;
}

.card-footer {
  background-color: #f8f9fa;
  border-top: 1px solid #e0e0e0;
  padding: 1rem 1.25rem;
}

/* ========== BADGE STYLES ========== */
.badge {
  font-size: 0.75rem;
  padding: 0.35rem 0.65rem;
  border-radius: 6px;
  font-weight: 500;
}

.badge.bg-success { background-color: #28a745 !important; }
.badge.bg-primary { background-color: #007bff !important; }
.badge.bg-warning { background-color: #ffc107 !important; color: #000000 !important; }
.badge.bg-danger { background-color: #dc3545 !important; }
.badge.bg-dark { background-color: #343a40 !important; }
.badge.bg-secondary { background-color: #6c757d !important; }

/* ========== TABLE STYLES ========== */
.table {
  margin-bottom: 0;
}

.table thead th {
  background-color: #f8f9fa;
  border-bottom: 2px solid #000000;
  font-weight: 600;
  color: #333;
  padding: 1rem;
}

.table tbody tr {
  transition: background-color 0.2s ease;
}

.table tbody tr:hover {
  background-color: #f8f9fa;
}

.table tbody td {
  padding: 0.75rem;
  vertical-align: middle;
}

/* ========== PRODUCT IMAGE STYLES ========== */
.product-image-wrapper {
  width: 70px;
  height: 70px;
  flex-shrink: 0;
  overflow: hidden;
  border-radius: 8px;
  background-color: #f8f9fa;
  border: 1px solid #e0e0e0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.product-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  object-position: center;
  transition: transform 0.2s ease;
}

.product-image:hover {
  transform: scale(1.05);
}

.product-name-container {
  max-width: 300px;
  flex: 1;
}

.product-name-text {
  display: block;
  font-weight: 600;
  margin-bottom: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  color: #333;
}

.product-variant-text {
  display: block;
  font-size: 0.75rem;
  color: #6c757d;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* ========== BUTTON STYLES ========== */
.btn-outline-secondary {
  border: 1px solid #000000;
  color: #000000;
  background-color: transparent;
  transition: all 0.3s ease;
  border-radius: 6px;
  padding: 0.4rem 1rem;
}

.btn-outline-secondary:hover {
  background-color: #000000;
  color: #ffffff;
  border-color: #000000;
}

.btn-success {
  background-color: #28a745;
  border-color: #28a745;
  color: #ffffff;
  border-radius: 6px;
  padding: 0.5rem 1rem;
  transition: all 0.3s ease;
}

.btn-success:hover {
  background-color: #218838;
  border-color: #1e7e34;
}

.btn-warning {
  background-color: #ffc107;
  border-color: #ffc107;
  color: #000000;
  border-radius: 6px;
  padding: 0.5rem 1rem;
  transition: all 0.3s ease;
}

.btn-warning:hover {
  background-color: #e0a800;
  border-color: #d39e00;
  color: #000000;
}

.btn-danger {
  background-color: #dc3545;
  border-color: #dc3545;
  color: #ffffff;
  border-radius: 6px;
  padding: 0.5rem 1rem;
  transition: all 0.3s ease;
}

.btn-danger:hover {
  background-color: #c82333;
  border-color: #bd2130;
}

.btn-outline-warning {
  color: #ffc107;
  border-color: #ffc107;
  background-color: transparent;
  transition: all 0.3s;
  font-weight: 500;
  padding: 0.4rem 0.75rem;
  border-width: 2px;
  border-radius: 6px;
}

.btn-outline-warning:hover {
  background-color: #ffc107;
  border-color: #ffc107;
  color: #000000;
}

.btn-outline-warning .bi-star-fill {
  color: #ffde07 !important;
}

.btn-outline-warning:hover .bi-star-fill {
  color: #000000 !important;
}

/* ========== MODAL STYLES ========== */
.modal {
  z-index: 1050;
}

.modal-content {
  border-radius: 12px;
  border: none;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
  overflow: hidden;
}

.modal-header {
  background-color: #000000 !important;
  border-bottom: 2px solid #ffffff;
  padding: 1rem 1.5rem;
}

.modal-header .modal-title {
  color: #ffffff;
  font-weight: 600;
  font-size: 1.1rem;
}

.modal-header .btn-close-white {
  filter: brightness(0) invert(1);
  opacity: 0.8;
  transition: opacity 0.3s;
}

.modal-header .btn-close-white:hover {
  opacity: 1;
}

.modal-body {
  padding: 1.5rem;
}

.modal-footer {
  border-top: 1px solid #e0e0e0;
  padding: 1rem 1.5rem;
  background-color: #f8f9fa;
}

.modal-footer .btn-secondary,
.modal-footer .btn-primary,
.modal-footer .btn-danger,
.modal-footer .btn-success {
  border-radius: 6px;
  padding: 0.5rem 1.2rem;
  transition: all 0.3s;
}

.modal-footer .btn-secondary {
  background-color: #6c757d;
  border-color: #6c757d;
  color: #ffffff;
}

.modal-footer .btn-secondary:hover {
  background-color: #5a6268;
  border-color: #545b62;
}

.modal-footer .btn-primary {
  background-color: #000000;
  border-color: #000000;
  color: #ffffff;
}

.modal-footer .btn-primary:hover {
  background-color: #333333;
  border-color: #333333;
}

.modal-footer .btn-danger {
  background-color: #dc3545;
  border-color: #dc3545;
}

.modal-footer .btn-danger:hover {
  background-color: #c82333;
  border-color: #bd2130;
}

.modal-footer .btn-success {
  background-color: #28a745;
  border-color: #28a745;
}

.modal-footer .btn-success:hover {
  background-color: #218838;
  border-color: #1e7e34;
}

/* ========== REVIEW MODAL STYLES ========== */
.review-modal .modal-content {
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
  border: 1px solid #e0e0e0;
}

.review-modal .modal-header {
  background-color: #000000 !important;
  border-bottom: 2px solid #ffffff;
  padding: 1rem 1.5rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.review-modal .modal-header .modal-title {
  font-weight: 600;
  font-size: 1.2rem;
  color: #ffffff;
  margin: 0;
}

.review-modal .modal-header .close-btn {
  background: rgba(255, 255, 255, 0.2);
  border: none;
  font-size: 1.2rem;
  cursor: pointer;
  padding: 0;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  color: #ffffff;
  margin-left: auto;
}

.review-modal .modal-header .close-btn:hover {
  background: rgba(255, 255, 255, 0.4);
  transform: rotate(90deg);
}

.review-modal .modal-header .close-btn i {
  font-size: 1rem;
}


.review-modal .modal-footer {
  border-top: 1px solid #e0e0e0;
  padding: 1rem 1.5rem;
  background-color: #ffffff;
}

.review-modal .modal-footer .btn-outline-secondary {
  border-radius: 6px;
  padding: 0.4rem 1.2rem;
  transition: all 0.3s;
  border: 1px solid #000000;
  color: #000000;
  background-color: transparent;
}

.review-modal .modal-footer .btn-outline-secondary:hover {
  background-color: #000000;
  color: #ffffff;
  border-color: #000000;
}

.review-modal .modal-footer .btn-primary {
  background-color: #000000;
  border: 1px solid #ffffff;
  border-radius: 6px;
  padding: 0.4rem 1.2rem;
  transition: all 0.3s;
  color: #ffffff;
}

.review-modal .modal-footer .btn-primary:hover {
  background-color: #ffffff;
  color: #000000;
  border-color: #000000;
}

/* ========== REVIEW DETAIL CONTAINER ========== */
.review-detail-container {
  max-height: 60vh;
  overflow-y: auto;
  padding-right: 5px;
}

.review-detail-container::-webkit-scrollbar {
  width: 5px;
}

.review-detail-container::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 10px;
}

.review-detail-container::-webkit-scrollbar-thumb {
  background: #888;
  border-radius: 10px;
}

.review-detail-container::-webkit-scrollbar-thumb:hover {
  background: #555;
}

/* ========== PRODUCT INFO SECTION ========== */
.product-info-section {
  background-color: #f8f9fa;
  border-radius: 10px;
  padding: 1rem;
  margin-bottom: 1.5rem;
  border: 1px solid #e0e0e0;
}

.product-thumbnail {
  width: 70px;
  height: 70px;
  border-radius: 8px;
  object-fit: cover;
  border: 1px solid #e0e0e0;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.product-name {
  font-size: 1rem;
  font-weight: 600;
  color: #333;
  margin-bottom: 0.5rem;
}

.product-variant {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.badge-variant {
  background-color: #e9ecef;
  color: #495057;
  padding: 4px 10px;
  border-radius: 4px;
  font-size: 0.7rem;
  font-weight: 500;
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.badge-variant i {
  font-size: 0.7rem;
}

/* ========== REVIEWER INFO ========== */
.reviewer-info {
  padding-bottom: 0.75rem;
  margin-bottom: 1rem;
  border-bottom: 1px solid #e0e0e0;
}

.reviewer {
  font-size: 0.9rem;
  color: #495057;
}

.reviewer i {
  color: #000000;
  font-size: 1rem;
}

.review-date {
  font-size: 0.8rem;
  color: #6c757d;
}

.review-date i {
  font-size: 0.8rem;
}

/* ========== RATING SECTION ========== */
.rating-section {
  background-color: #f8f9fa;
  border-radius: 10px;
  padding: 1rem;
  margin-bottom: 1.5rem;
  border: 1px solid #e0e0e0;
}

.stars-container {
  display: flex;
  justify-content: center;
  gap: 0.5rem;
  margin-bottom: 0.5rem;
}

.review-star {
  font-size: 1.8rem;
  transition: transform 0.2s;
  cursor: default;
}

.review-star i {
  color: #ffc107;
}

.review-star:hover {
  transform: scale(1.05);
}

/* Star rating styles for review modal */
.star-rating {
  font-size: 1.8rem;
  cursor: pointer;
  transition: all 0.2s;
}

.star-rating:hover {
  transform: scale(1.1);
}

.star-rating i {
  transition: color 0.2s;
}

/* Rating score */
.rating-score {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 100%;
}

.score-number-wrapper {
  width: 100%;
  text-align: right;
}

.score-number {
  font-size: 0.8rem;
  font-weight: 700;
  color: #6c757d;
  display: inline-block;
}

/* ========== REVIEW CONTENT SECTION ========== */
.review-content-section {
  margin-bottom: 1rem;
}

.review-label {
  margin-bottom: 0.5rem;
  color: #495057;
  font-size: 0.9rem;
}

.review-label i {
  color: #000000;
}

.review-text {
  background-color: #f8f9fa;
  border-radius: 8px;
  padding: 0.75rem;
  border-left: 3px solid #000000;
}

.review-text p {
  margin: 0;
  line-height: 1.5;
  color: #495057;
  font-size: 0.9rem;
}

/* ========== FORM STYLES ========== */
.form-label {
  font-weight: 600;
  margin-bottom: 0.5rem;
  color: #333;
}

.form-control {
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  padding: 0.6rem 0.75rem;
  transition: all 0.3s;
}

.form-control:focus {
  border-color: #000000;
  box-shadow: 0 0 0 0.2rem rgba(0, 0, 0, 0.1);
  outline: none;
}

.form-select {
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  padding: 0.6rem 0.75rem;
  transition: all 0.3s;
}

.form-select:focus {
  border-color: #000000;
  box-shadow: 0 0 0 0.2rem rgba(0, 0, 0, 0.1);
  outline: none;
}

/* ========== ALERT STYLES ========== */
.alert {
  border-radius: 8px;
  padding: 0.75rem 1rem;
  margin-bottom: 1rem;
}

.alert-danger {
  background-color: #f8d7da;
  border-color: #f5c6cb;
  color: #721c24;
}

.alert-warning {
  background-color: #fff3cd;
  border-color: #ffeaa7;
  color: #856404;
  font-size: 0.85rem;
  border-radius: 8px;
}

.alert-warning i {
  color: #856404;
}

.alert-info {
  background-color: #d1ecf1;
  border-color: #bee5eb;
  color: #0c5460;
}

.alert-success {
  background-color: #d4edda;
  border-color: #c3e6cb;
  color: #155724;
}

/* ========== TIMER STYLES ========== */
.report-info {
  color: #bf0707;
  font-size: 1rem;
  background: transparent;
  padding: 0;
}

.timer-display-small {
  color: #bf0707;
  font-family: 'Courier New', monospace;
  background: transparent;
  padding: 2px 0;
}

.timer-display-small i {
  color: #bf0707;
  font-size: 1rem;
}

.deadline-info-small {
  color: #717171;
  font-family: 'Courier New', monospace;
  background: transparent;
  padding: 2px 0;
}

.deadline-info-small i {
  color: #717171;
  font-size: 1rem;
}

/* ========== TEXT STYLES ========== */
.text-danger {
  color: #dc3545 !important;
}

.text-success {
  color: #28a745 !important;
}

.text-warning {
  color: #ffc107 !important;
}

.text-muted {
  color: #6c757d !important;
}

.text-end {
  text-align: right;
}

.text-center {
  text-align: center;
}

/* ========== UTILITY CLASSES ========== */
.mb-0 { margin-bottom: 0 !important; }
.mb-1 { margin-bottom: 0.25rem !important; }
.mb-2 { margin-bottom: 0.5rem !important; }
.mb-3 { margin-bottom: 1rem !important; }
.mb-4 { margin-bottom: 1.5rem !important; }

.mt-1 { margin-top: 0.25rem !important; }
.mt-2 { margin-top: 0.5rem !important; }
.mt-3 { margin-top: 1rem !important; }
.mt-4 { margin-top: 1.5rem !important; }

.me-1 { margin-right: 0.25rem !important; }
.me-2 { margin-right: 0.5rem !important; }
.me-3 { margin-right: 1rem !important; }

.ms-1 { margin-left: 0.25rem !important; }
.ms-2 { margin-left: 0.5rem !important; }
.ms-3 { margin-left: 1rem !important; }

.py-2 { padding-top: 0.5rem !important; padding-bottom: 0.5rem !important; }
.py-3 { padding-top: 1rem !important; padding-bottom: 1rem !important; }
.py-4 { padding-top: 1.5rem !important; padding-bottom: 1.5rem !important; }

.px-3 { padding-left: 1rem !important; padding-right: 1rem !important; }

.d-flex { display: flex !important; }
.d-block { display: block !important; }
.justify-content-end { justify-content: flex-end !important; }
.justify-content-between { justify-content: space-between !important; }
.align-items-center { align-items: center !important; }
.flex-column { flex-direction: column !important; }
.flex-wrap { flex-wrap: wrap !important; }
.gap-2 { gap: 0.5rem !important; }
.gap-3 { gap: 1rem !important; }

.w-100 { width: 100% !important; }

/* ========== PRICE DISPLAY STYLES ========== */
.price-info, .total-info {
  line-height: 1.3;
}

.price-info .text-decoration-line-through,
.total-info .text-decoration-line-through {
  font-size: 0.7rem;
}

.price-info .fw-bold,
.total-info .fw-bold {
  font-size: 0.95rem;
}

.price-info small,
.total-info small {
  font-size: 0.65rem;
}

/* ========== SAVINGS SUMMARY STYLES ========== */
.bg-opacity-10 {
  --bs-bg-opacity: 0.1;
}

.border-top {
  border-top: 1px solid #dee2e6 !important;
}

.border-bottom {
  border-bottom: 1px solid #dee2e6 !important;
}

.card-footer .total-amount {
  font-size: 1.8rem;
  font-weight: bold;
  color: #dc3545;
}

.card-footer .saved-amount {
  font-size: 1.5rem;
  font-weight: bold;
  color: #28a745;
}

/* ========== RESPONSIVE STYLES ========== */
@media (max-width: 768px) {
  .product-image-wrapper {
    width: 55px;
    height: 55px;
  }

  .product-name-container {
    max-width: 150px;
  }

  .product-name-text {
    font-size: 0.85rem;
  }

  .product-variant-text {
    font-size: 0.7rem;
  }

  .review-star,
  .star-rating {
    font-size: 1.3rem;
  }

  .score-number {
    font-size: 0.7rem;
  }

  .product-thumbnail {
    width: 55px;
    height: 55px;
  }

  .product-name {
    font-size: 0.85rem;
  }

  .reviewer-info {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.3rem;
  }

  .review-date {
    align-self: flex-start;
  }

  .product-info-section {
    padding: 0.75rem;
  }

  .modal-body {
    padding: 1rem;
  }

  .modal-footer {
    flex-direction: column;
    gap: 0.5rem;
  }

  .modal-footer .btn {
    width: 100%;
  }

  .col-sm-2.text-center {
    min-width: 120px;
  }

  .btn-outline-warning {
    font-size: 0.75rem;
    padding: 0.3rem 0.6rem;
  }
}

@media (max-width: 576px) {
  .card-header h5,
  .modal-header .modal-title {
    font-size: 1rem;
  }

  .table thead th {
    font-size: 0.8rem;
    padding: 0.5rem;
  }

  .table tbody td {
    font-size: 0.8rem;
    padding: 0.5rem;
  }

  .product-image-wrapper {
    width: 45px;
    height: 45px;
  }

  .btn-sm {
    font-size: 0.7rem;
    padding: 0.25rem 0.5rem;
  }

  .rating-section {
    padding: 0.75rem;
  }
}
</style>