<template>
  <div class="employee-layout">
    <!-- Toast Component -->
    <Toast />
    
    <NV_Sidebar @toggle-collapse="handleSidebarCollapse" />

    <!-- modal xem chi tiết -->
    <div class="modal fade" id="detailModal" tabindex="-1" aria-hidden="true" ref="detailModal" data-bs-backdrop="static" data-bs-keyboard="false">
      <div class="modal-dialog modal-dialog-centered modal-lg modal-zoom-in">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">
              <i class="bi bi-chat-square-text me-2"></i>Chi tiết đánh giá
            </h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
          </div>
          <div class="modal-body">
            <div v-if="selectedReview" class="modal-content-animate">
              <div class="row mb-3">
                <div class="col-md-6 detail-item" style="--delay: 0.05s">
                  <label class="form-label fw-bold">Tên khách hàng</label>
                  <p class="form-control-plaintext">{{ getTenKhachHang(selectedReview) }}</p>
                </div>
                <div class="col-md-6 detail-item" style="--delay: 0.1s">
                  <label class="form-label fw-bold">Username</label>
                  <p class="form-control-plaintext">{{ getUsername(selectedReview) }}</p>
                </div>
              </div>

              <div class="mb-3 detail-item" style="--delay: 0.15s">
                <label class="form-label fw-bold">Sản phẩm</label>
                <p class="form-control-plaintext">{{ getTenSanPham(selectedReview) }}</p>
              </div>
              
              <div class="row mb-3">
                <div class="col-md-6 detail-item" style="--delay: 0.2s">
                  <label class="form-label fw-bold">Phân loại (Màu sắc)</label>
                  <p class="form-control-plaintext">
                    <i class="bi bi-palette me-1"></i>
                    {{ getMauSac(selectedReview) || 'Không xác định' }}
                  </p>
                </div>
                <div class="col-md-6 detail-item" style="--delay: 0.25s">
                  <label class="form-label fw-bold">Size</label>
                  <p class="form-control-plaintext">
                    <i class="bi bi-rulers me-1"></i>
                    {{ getSize(selectedReview) || 'Không xác định' }}
                  </p>
                </div>
              </div>

              <div class="mb-3 detail-item" style="--delay: 0.3s">
                <label class="form-label fw-bold">Đơn giá khi mua</label>
                <p class="form-control-plaintext text-primary fw-bold">
                  {{ formatPrice(getDonGia(selectedReview)) }}
                </p>
              </div>

              <div class="mb-3 detail-item" style="--delay: 0.35s">
                <label class="form-label fw-bold">Đánh giá</label>
                <div class="rating mb-2">
                  <span v-for="star in 5" :key="star" class="star star-animate" :style="{ '--star-delay': `${star * 0.05}s` }">
                    <i class="bi" :class="star <= selectedReview.sao ? 'bi-star-fill text-warning' : 'bi-star text-muted'"></i>
                  </span>
                  <span class="ms-2 text-muted small">
                    {{ formatDate(selectedReview.ngayDG) }}
                  </span>
                </div>
              </div>

              <div class="mb-3 detail-item" style="--delay: 0.4s">
                <label class="form-label fw-bold">Nội dung đánh giá</label>
                <div class="border rounded p-3 bg-light" style="max-height: 300px; overflow-y: auto;">
                  <p class="mb-0" style="white-space: pre-wrap;">{{ selectedReview.danhGiaCT || 'Không có nội dung' }}</p>
                </div>
              </div>
            </div>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary btn-hover-scale" data-bs-dismiss="modal">
              Hủy
            </button>
            <button v-if="selectedReview" type="button" class="btn btn-danger btn-hover-scale" @click="deleteFromModal">
              <i class="bi bi-eye-slash"></i> Ẩn
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- modal xác nhận ẩn -->
    <div class="modal fade" id="confirmDeleteModal" tabindex="-1" aria-hidden="true" ref="confirmDeleteModal" data-bs-backdrop="static" data-bs-keyboard="false">
      <div class="modal-dialog modal-dialog-centered modal-zoom-in">
        <div class="modal-content modal-shake">
          <div class="modal-header">
            <h5 class="modal-title">
              <i class="bi bi-exclamation-triangle me-2 icon-pulse"></i>Xác nhận ẩn
            </h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
          </div>
          <div class="modal-body text-center py-4">
            <i class="bi bi-eye-slash display-4 text-danger mb-3 icon-float"></i>
            <p class="fw-bold">Bạn có chắc chắn muốn ẩn đánh giá này?</p>
            <p class="text-muted small">Hành động này không thể hoàn tác.</p>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary btn-hover-scale" data-bs-dismiss="modal">
              Hủy
            </button>
            <button type="button" class="btn btn-danger btn-hover-scale" @click="executeDelete" :disabled="deleting">
              <span v-if="deleting" class="spinner-border spinner-border-sm me-2"></span>
              <i class="bi bi-eye-slash"></i> Ẩn
            </button>
          </div>
        </div>
      </div>
    </div>

    <main class="main-content" :class="{ 'expanded': isSidebarCollapsed }">
      <div class="page-container">
        <!-- tổng quan đánh giá -->
        <div class="row g-3 mb-4">
          <div class="col-md-3">
            <div class="stats-mini bg-gradient-1 animate-slide-up" style="--delay: 0.1s">
              <h4 class="counter-animate">{{ totalReviews }}</h4>
              <p>Tổng đánh giá</p>
            </div>
          </div>
          <div class="col-md-3">
            <div class="stats-mini bg-gradient-2 animate-slide-up" style="--delay: 0.2s">
              <h4 class="counter-animate">{{ averageRating }}</h4>
              <p>Đánh giá trung bình</p>
            </div>
          </div>
          <div class="col-md-3">
            <div class="stats-mini bg-gradient-4 animate-slide-up" style="--delay: 0.3s">
              <h4 class="counter-animate">{{ positiveReviews }}</h4>
              <p>Đánh giá tích cực (4-5⭐)</p>
            </div>
          </div>
          <div class="col-md-3">
            <div class="stats-mini bg-gradient-3 animate-slide-up" style="--delay: 0.4s">
              <h4 class="counter-animate">{{ negativeReviews }}</h4>
              <p>Đánh giá tiêu cực (1-3⭐)</p>
            </div>
          </div>
        </div>

        <!-- danh sách đánh giá -->
        <div class="content-card animate-fade-in">
          <div class="d-flex justify-content-between align-items-center mb-4">
            <div class="title-animate">
              <h5 class="page-title">Quản lý đánh giá</h5>
              <p class="page-subtitle">
                Quản lý và xử lý các đánh giá từ khách hàng
              </p>
            </div>
          </div>

          <!-- bộ lọc -->
          <div class="filter-section animate-slide-down">
            <div class="row g-3">
              <div class="col-md-3 filter-item" style="--delay: 0.1s">
                <div class="input-group">
                  <span class="input-group-text bg-light">
                    <i class="bi bi-search"></i>
                  </span>
                  <input type="text" v-model="filterKeyword" class="form-control input-focus-animate" placeholder="Tìm theo tên KH hoặc SP"/>
                </div>
              </div>
              <div class="col-md-2 filter-item" style="--delay: 0.15s">
                <select v-model="filterRating" class="form-select select-animate">
                  <option value="">Tất cả số sao</option>
                  <option value="5">5 sao</option>
                  <option value="4">4 sao</option>
                  <option value="3">3 sao</option>
                  <option value="2">2 sao</option>
                  <option value="1">1 sao</option>
                </select>
              </div>
              <div class="col-md-2 filter-item" style="--delay: 0.2s">
                <select v-model="filterStatus" class="form-select select-animate">
                  <option value="all">Tất cả trạng thái</option>
                  <option value="visible">Hiển thị</option>
                  <option value="hidden">Đã ẩn</option>
                </select>
              </div>
              <div class="col-md-2 filter-item" style="--delay: 0.25s">
                <select v-model="sortOrder" class="form-select select-animate">
                  <option value="desc">Mới nhất</option>
                  <option value="asc">Cũ nhất</option>
                </select>
              </div>
              <div class="col-md-2 filter-item" style="--delay: 0.3s">
                <input type="date" v-model="filterDate" class="form-control input-focus-animate" placeholder="Lọc theo ngày"/>
              </div>
              <div class="col-md-1 filter-item" style="--delay: 0.35s">
                <button @click="resetFilters" class="btn btn-outline-secondary w-100 btn-hover-scale">
                  <i class="bi bi-arrow-counterclockwise"></i>
                </button>
              </div>
            </div>
          </div>

          <!-- list đánh giá -->
          <div class="reviews-list">
            <div class="row">
              <div class="col-12" v-for="(item, index) in paginatedReviews" :key="item.maDG">
                <div class="review-card animate-slide-up review-item-animate" 
                     :style="{ '--delay': `${0.05 * index}s` }"
                     :class="{ 'review-hidden': isHiddenReview(item) }">
                  <div class="row align-items-start">
                    <div class="col-md-9">
                      <div class="d-flex align-items-center mb-2">
                        <div class="reviewer-info">
                          <i class="bi bi-person-circle me-2 user-icon"></i>
                          <strong>{{ getTenKhachHang(item) }}</strong>
                          <span class="text-muted ms-2">|</span>
                          <span class="text-muted ms-2">
                            <i class="bi bi-tag me-1"></i>
                            {{ getTenSanPham(item) }}
                          </span>
                          <span v-if="isHiddenReview(item)" class="badge bg-danger ms-2 badge-pulse">
                            <i class="bi bi-eye-slash me-1"></i>Đã ẩn
                          </span>
                        </div>
                      </div>
                      
                      <div class="rating mb-2">
                        <span v-for="star in 5" :key="star" class="star star-animate" :style="{ '--star-delay': `${star * 0.05}s` }">
                          <i 
                            class="bi" 
                            :class="star <= item.sao ? 'bi-star-fill text-warning' : 'bi-star text-muted'"
                          ></i>
                        </span>
                        <span class="ms-2 text-muted small">
                          {{ formatDate(item.ngayDG) }}
                        </span>
                      </div>

                      <div class="review-content">
                        <p class="mb-0">
                          {{ getShortContent(item.danhGiaCT) }}
                          <button
                            v-if="item.danhGiaCT && item.danhGiaCT.length > 100"
                            class="btn btn-link btn-sm p-0 ms-2 read-more-btn"
                            @click="viewDetail(item)"
                            style="text-decoration: none;"
                          >
                            Xem thêm...
                          </button>
                        </p>
                      </div>
                    </div>
                    
                    <div class="col-md-3 text-end">
                      <div class="action-buttons">
                        <button
                          type="button"
                          class="btn btn-outline-primary btn-sm me-2 btn-hover-scale"
                          @click="viewDetail(item)"
                          title="Xem chi tiết"
                        >
                          <i class="bi bi-eye"></i> Xem chi tiết
                        </button>
                        <button
                          type="button"
                          class="btn btn-outline-danger btn-sm btn-hover-scale"
                          @click="confirmDelete(item)"
                          title="Ẩn đánh giá"
                        >
                          <i class="bi bi-eye-slash"></i> Ẩn
                        </button>
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <!-- phân trang -->
              <div v-if="totalPages > 1" class="d-flex justify-content-center align-items-center mt-4 mb-2">
                <nav aria-label="Page navigation">
                  <ul class="pagination pagination-sm mb-0">
                    <li class="page-item page-item-animate" :class="{ disabled: currentPage === 1 }">
                      <button class="page-link text-dark page-link-hover" @click="goToPage(currentPage - 1)">
                        <i class="bi bi-chevron-left"></i> Trước
                      </button>
                    </li>

                    <li class="page-item page-item-animate" v-for="page in totalPages" :key="page" :class="{ active: currentPage === page }" :style="{ '--delay': `${0.05 * page}s` }">
                      <button class="page-link page-link-hover" :class=" currentPage === page? 'bg-dark border-dark text-white' : 'text-dark'" @click="goToPage(page)">
                        {{ page }}
                      </button>
                    </li>

                    <li class="page-item page-item-animate" :class="{ disabled: currentPage === totalPages }">
                      <button class="page-link text-dark page-link-hover" @click="goToPage(currentPage + 1)">
                        Sau <i class="bi bi-chevron-right"></i>
                      </button>
                    </li>
                  </ul>
                </nav>
              </div>

              <div v-if="filteredReviews.length === 0 && !loading" class="text-center py-5 text-muted empty-state-animate">
                <i class="bi bi-chat-square-text display-4 icon-float"></i>
                <p class="mt-3">Chưa có đánh giá nào</p>
              </div>
              
              <div v-if="loading" class="text-center py-5">
                <div class="spinner-border text-dark loading-spinner" role="status">
                  <span class="visually-hidden">Loading...</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from "vue";
import axios from "axios";
import NV_Sidebar from "@/components/Shared/NV_Sidebar.vue";
import Toast from "@/components/Shared/Toast.vue";
import { Modal } from "bootstrap";

axios.defaults.withCredentials = true;

const reviews = ref([]);
const filterKeyword = ref("");
const filterRating = ref("");
const filterStatus = ref("all");
const filterDate = ref("");
const sortOrder = ref("desc");
const currentPage = ref(1);
const itemsPerPage = 10;
const selectedReview = ref(null);
const deleteTargetId = ref(null);
const loading = ref(false);
const deleting = ref(false);

const detailModal = ref(null);
const confirmDeleteModal = ref(null);
let detailModalInstance = null;
let confirmDeleteModalInstance = null;

const fetchReviews = async () => {
  loading.value = true;
  try {
    const response = await axios.get("http://localhost:8080/api/danhgia/list");
    if (response.data.success) {
      reviews.value = response.data.data;
    } else {
      window.showToast(response.data.message || "Không thể tải danh sách đánh giá", "danger");
    }
  } catch (error) {
    console.error("Lỗi lấy danh sách đánh giá:", error);
    window.showToast("Không thể tải danh sách đánh giá", "danger");
  } finally {
    loading.value = false;
  }
};

const getTenKhachHang = (review) => {
  try {
    if (review.hoaDonCT?.hoaDon?.khachHang?.tenKH) {
      return review.hoaDonCT.hoaDon.khachHang.tenKH;
    }
  } catch (e) {
    console.error("Lỗi lấy tên khách hàng:", e);
  }
  return "Khách hàng ẩn danh";
};

const getUsername = (review) => {
  try {
    const user = review.hoaDonCT?.hoaDon?.khachHang?.user;
    if (user && user.userName) {
      return user.userName;
    }
  } catch (e) {
    console.error("Lỗi lấy username:", e);
  }
  return "Không có username";
};

const getTenSanPham = (review) => {
  try {
    if (review.hoaDonCT?.sanPhamChiTiet?.sanPham?.tenSP) {
      return review.hoaDonCT.sanPhamChiTiet.sanPham.tenSP;
    }
  } catch (e) {
    console.error("Lỗi lấy tên sản phẩm:", e);
  }
  return "Sản phẩm không xác định";
};

const getMauSac = (review) => {
  try {
    const spct = review.hoaDonCT?.sanPhamChiTiet;
    if (spct && spct.tenMau) {
      return spct.tenMau;
    }
  } catch (e) {
    console.error("Lỗi lấy màu sắc:", e);
  }
  return null;
};

const getSize = (review) => {
  try {
    const size = review.hoaDonCT?.sanPhamChiTiet?.size;
    if (size) {
      return size.coGiay === 0 ? "Freesize" : size.coGiay;
    }
  } catch (e) {
    console.error("Lỗi lấy size:", e);
  }
  return null;
};

const getDonGia = (review) => {
  try {
    return review.hoaDonCT?.donGia || 0;
  } catch (e) {
    return 0;
  }
};

const formatPrice = (price) => {
  if (!price || price === 0) return "0 ₫";
  return price.toLocaleString("vi-VN") + " ₫";
};

const getShortContent = (content) => {
  if (!content) return "Không có nội dung";
  if (content.length <= 100) return content;
  return content.substring(0, 100) + "...";
};

const formatDate = (dateString) => {
  if (!dateString) return "";
  try {
    const date = new Date(dateString);
    return date.toLocaleDateString("vi-VN", {
      day: "2-digit",
      month: "2-digit",
      year: "numeric",
      hour: "2-digit",
      minute: "2-digit",
    });
  } catch (e) {
    return dateString;
  }
};

const isHiddenReview = (review) => {
  return review.danhGiaCT === "Ẩn đánh giá do vi phạm tiêu chuẩn cộng đồng";
};

const matchesFilters = (item) => {
  const tenKH = getTenKhachHang(item).toLowerCase();
  const tenSP = getTenSanPham(item).toLowerCase();
  const userName = getUsername(item).toLowerCase();
  const keyword = filterKeyword.value.toLowerCase();
  
  const matchKeyword = !filterKeyword.value || 
    tenKH.includes(keyword) || 
    tenSP.includes(keyword) ||
    userName.includes(keyword);

  const matchRating = !filterRating.value || item.sao === parseInt(filterRating.value);

  let matchStatus = true;
  if (filterStatus.value === "visible") {
    matchStatus = !isHiddenReview(item);
  } else if (filterStatus.value === "hidden") {
    matchStatus = isHiddenReview(item);
  }
  
  let matchDate = true;
  if (filterDate.value && item.ngayDG) {
    const reviewDate = new Date(item.ngayDG).toISOString().split('T')[0];
    matchDate = reviewDate === filterDate.value;
  }

  return matchKeyword && matchRating && matchStatus && matchDate;
};

const filteredReviews = computed(() => {
  let result = reviews.value.filter(matchesFilters);
  return result.sort((a, b) => {
    const dateA = new Date(a.ngayDG);
    const dateB = new Date(b.ngayDG);
    return sortOrder.value === 'desc' ? dateB - dateA : dateA - dateB;
  });
});

const totalReviews = computed(() => filteredReviews.value.length);

const averageRating = computed(() => {
  const filtered = filteredReviews.value;
  if (filtered.length === 0) return "0.0";
  const sum = filtered.reduce((acc, r) => acc + r.sao, 0);
  return (sum / filtered.length).toFixed(1);
});

const positiveReviews = computed(() => filteredReviews.value.filter(r => r.sao >= 4).length);
const negativeReviews = computed(() => filteredReviews.value.filter(r => r.sao <= 3).length);

const totalPages = computed(() => Math.ceil(filteredReviews.value.length / itemsPerPage));

const paginatedReviews = computed(() => {
  const start = (currentPage.value - 1) * itemsPerPage;
  const end = start + itemsPerPage;
  return filteredReviews.value.slice(start, end);
});

const goToPage = (page) => {
  if (page >= 1 && page <= totalPages.value) {
    currentPage.value = page;
  }
};

const resetFilters = () => {
  filterKeyword.value = "";
  filterRating.value = "";
  filterDate.value = "";
  filterStatus.value = "all";
  sortOrder.value = "desc";
  currentPage.value = 1;
};

const viewDetail = (review) => {
  selectedReview.value = review;
  if (detailModalInstance) {
    detailModalInstance.show();
  }
};

const confirmDelete = (review) => {
  deleteTargetId.value = review.maDG;
  if (confirmDeleteModalInstance) {
    confirmDeleteModalInstance.show();
  }
};

const deleteFromModal = () => {
  if (selectedReview.value) {
    if (detailModalInstance) {
      detailModalInstance.hide();
    }
    deleteTargetId.value = selectedReview.value.maDG;
    if (confirmDeleteModalInstance) {
      confirmDeleteModalInstance.show();
    }
  }
};

const executeDelete = async () => {
  if (!deleteTargetId.value) return;
  
  deleting.value = true;
  try {
    const response = await axios.delete(`http://localhost:8080/api/danhgia/delete/${deleteTargetId.value}`);
    if (response.data.success) {
      window.showToast("Ẩn đánh giá thành công!", "success");
      await fetchReviews();
      
      if (confirmDeleteModalInstance) {
        confirmDeleteModalInstance.hide();
      }
      
      selectedReview.value = null;
    } else {
      window.showToast(response.data.message || "Ẩn thất bại", "danger");
    }
  } catch (error) {
    console.error("Lỗi ẩn đánh giá:", error);
    window.showToast(error.response?.data?.message || "Không thể ẩn đánh giá", "danger");
  } finally {
    deleting.value = false;
    deleteTargetId.value = null;
  }
};

const handleFilterChange = () => {
  currentPage.value = 1;
};

watch([filterKeyword, filterRating, filterStatus, filterDate, sortOrder], () => {
  handleFilterChange();
});

const isSidebarCollapsed = ref(false);

const handleSidebarCollapse = (collapsedState) => {
  isSidebarCollapsed.value = collapsedState;
};

onMounted(() => {
  fetchReviews();

  if (detailModal.value) {
    detailModalInstance = new Modal(detailModal.value);
  }
  if (confirmDeleteModal.value) {
    confirmDeleteModalInstance = new Modal(confirmDeleteModal.value);
  }
});
</script>

<style scoped>
/* ===== ANIMATION KEYFRAMES ===== */
@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes zoomIn {
  from {
    opacity: 0;
    transform: scale(0.9);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

@keyframes float {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-5px);
  }
}

@keyframes pulse {
  0%, 100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.05);
  }
}

@keyframes shake {
  0%, 100% {
    transform: translateX(0);
  }
  10%, 30%, 50%, 70%, 90% {
    transform: translateX(-5px);
  }
  20%, 40%, 60%, 80% {
    transform: translateX(5px);
  }
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

@keyframes starGlow {
  0%, 100% {
    transform: scale(1);
    text-shadow: 0 0 5px rgba(255, 193, 7, 0.5);
  }
  50% {
    transform: scale(1.2);
    text-shadow: 0 0 15px rgba(255, 193, 7, 0.8);
  }
}

/* ===== ANIMATION CLASSES ===== */
.animate-fade-in {
  animation: fadeIn 0.6s ease-out forwards;
}

.animate-slide-up {
  animation: slideUp 0.5s ease-out forwards;
  opacity: 0;
  animation-delay: var(--delay, 0s);
}

.animate-slide-down {
  animation: slideDown 0.5s ease-out forwards;
  opacity: 0;
}

.modal-zoom-in {
  animation: zoomIn 0.3s ease-out forwards;
}

.modal-shake {
  animation: shake 0.5s ease-in-out;
}

.icon-float {
  animation: float 3s ease-in-out infinite;
}

.icon-pulse {
  animation: pulse 2s ease-in-out infinite;
}

.badge-pulse {
  animation: pulse 1.5s ease-in-out infinite;
}

.loading-spinner {
  animation: spin 1s linear infinite;
}

/* ===== FILTER ITEMS ===== */
.filter-item {
  animation: slideUp 0.4s ease-out forwards;
  opacity: 0;
  animation-delay: var(--delay, 0s);
}

.input-focus-animate {
  border: 1px solid #dee2e6;
}

.input-focus-animate:focus {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  border-color: #000000;
}

.select-animate {
  transition: all 0.3s ease;
  cursor: pointer;
}

.select-animate:hover {
  border-color: #000000;
}

.select-animate:focus {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  border-color: #000000;
}

/* ===== BUTTON HOVER EFFECTS ===== */
.btn-hover-scale {
  transition: all 0.3s ease;
}

.btn-hover-scale:hover {
  transform: scale(1.05) translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.btn-hover-scale:active {
  transform: scale(0.98);
}

/* ===== REVIEW CARD ANIMATIONS ===== */
.review-item-animate {
  animation: slideUp 0.5s ease-out forwards;
  opacity: 0;
  animation-delay: var(--delay, 0s);
}

.review-card {
  background: white;
  border: 1px solid #e0e0e0;
  border-radius: 12px;
  padding: 20px;
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
  margin-bottom: 15px;
}

.review-card:hover {
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.12);
  transform: translateY(-3px);
  border-color: #000000;
}

.review-hidden {
  opacity: 0.7;
  background: #f8f9fa;
}

.review-hidden:hover {
  opacity: 0.9;
}

.user-icon {
  transition: transform 0.3s ease;
}

.review-card:hover .user-icon {
  transform: scale(1.1);
  color: #000000;
}

/* ===== STAR ANIMATIONS ===== */
.star-animate {
  display: inline-block;
  animation: starGlow 0.5s ease-out forwards;
  animation-delay: var(--star-delay, 0s);
  opacity: 0;
}

.star {
  font-size: 18px;
  margin-right: 2px;
  transition: transform 0.2s ease;
}

.star:hover {
  transform: scale(1.2);
}

/* ===== READ MORE BUTTON ===== */
.read-more-btn {
  color: #000000;
  transition: all 0.3s ease;
  position: relative;
}

.read-more-btn::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 0;
  height: 1px;
  background: #000000;
  transition: width 0.3s ease;
}

.read-more-btn:hover {
  color: #333333;
}

.read-more-btn:hover::after {
  width: 100%;
}

/* ===== PAGE LINK ANIMATIONS ===== */
.page-item-animate {
  animation: fadeIn 0.4s ease-out forwards;
  opacity: 0;
  animation-delay: var(--delay, 0s);
}

.page-link-hover {
  transition: all 0.3s ease;
}

.page-link-hover:hover {
  transform: translateY(-2px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

/* ===== MODAL CONTENT ANIMATIONS ===== */
.modal-content-animate {
  animation: fadeIn 0.4s ease-out forwards;
}

.detail-item {
  animation: slideUp 0.4s ease-out forwards;
  opacity: 0;
  animation-delay: var(--delay, 0s);
}

/* ===== EMPTY STATE ===== */
.empty-state-animate {
  animation: fadeIn 0.6s ease-out forwards;
}

.empty-state-animate .icon-float {
  animation: float 3s ease-in-out infinite;
}

/* ===== TITLE ANIMATION ===== */
.title-animate {
  animation: slideDown 0.5s ease-out forwards;
}

/* ===== COUNTER ANIMATION ===== */
.counter-animate {
  animation: pulse 0.5s ease-out;
}

/* ===== STATS CARD ===== */
.stats-mini {
  transition: all 0.3s ease;
}

.stats-mini:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}

/* ===== EXISTING STYLES ===== */
.main-content {
  margin-left: 260px;
  min-height: 100vh;
  transition: margin-left 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
}

.main-content.expanded {
  margin-left: 80px;
}

.page-container {
  padding: 30px;
  background: #f7f7f9;
  min-height: 100vh;
}

.stats-mini {
  padding: 25px 20px;
  border-radius: 12px;
  text-align: center;
  color: rgb(255, 255, 255);
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
}

.stats-mini h4 {
  font-size: 32px;
  font-weight: bold;
  margin: 0;
}

.stats-mini p {
  margin: 8px 0 0;
  font-size: 14px;
  opacity: 0.9;
}

.bg-gradient-1,
.bg-gradient-2,
.bg-gradient-3,
.bg-gradient-4 {
  background: linear-gradient(135deg, #212529, #000000);
}

.content-card {
  background: white;
  border-radius: 16px;
  padding: 30px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
}

.page-title {
  font-size: 24px;
  font-weight: bold;
  color: #333;
  margin-bottom: 5px;
}

.page-subtitle {
  color: #666;
  font-size: 14px;
  margin-bottom: 0;
}

.filter-section {
  background: #f8f9fa;
  padding: 20px;
  border-radius: 12px;
  margin-bottom: 25px;
  border: 1px solid #eee;
  transition: box-shadow 0.3s ease;
}

.filter-section:hover {
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.05);
}

.reviewer-info {
  font-size: 16px;
}

.review-content {
  color: #555;
  line-height: 1.5;
}

.action-buttons {
  display: flex;
  gap: 8px;
  justify-content: flex-end;
}

.modal-content {
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
  border: none;
  border-radius: 16px;
}

.modal-header {
  border-bottom: 2px solid #e9ecef;
  padding: 1rem 1.5rem;
}

.modal-footer {
  border-top: 2px solid #e9ecef;
  padding: 1rem 1.5rem;
}

body.modal-open {
  overflow: hidden;
  padding-right: 0 !important;
}

/* Responsive */
@media (max-width: 768px) {
  .main-content {
    margin-left: 0;
  }

  .page-container {
    padding: 15px;
  }

  .review-card .row {
    flex-direction: column;
  }

  .action-buttons {
    justify-content: flex-start;
    margin-top: 15px;
  }
}
</style>

<style>
.modal-backdrop {
  background-color: rgba(0, 0, 0, 0.25) !important;
  backdrop-filter: blur(5px);
  -webkit-backdrop-filter: blur(12px) brightness(0.9);
  animation: fadeIn 0.3s ease-out;
}

.modal-backdrop.show {
  opacity: 1 !important;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}
</style>