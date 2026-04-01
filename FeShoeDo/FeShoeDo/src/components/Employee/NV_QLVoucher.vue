<template>
  <div class="employee-layout">
    <NV_Sidebar @toggle-collapse="handleSidebarCollapse" />

    <div class="toast-container position-fixed top-0 end-0 p-3">
      <div v-if="toast.show" class="toast show align-items-center text-white border-0" :class="`bg-${toast.type}`" role="alert">
        <div class="d-flex">
          <div class="toast-body">
            <i class="bi me-2" :class="toast.type === 'success' ? 'bi-check-circle' : 'bi-exclamation-triangle'"></i>
            {{ toast.message }}
          </div>
          <button type="button" class="btn-close btn-close-white me-2 m-auto" @click="toast.show = false"></button>
        </div>
        <div class="toast-progress-bar"></div>
      </div>
    </div>

    <div class="modal fade" id="voucherModal" tabindex="-1" aria-hidden="true" ref="voucherModal">
      <div class="modal-dialog modal-dialog-centered modal-lg">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">
              <i class="bi" :class="isEdit ? 'bi-pencil-square' : 'bi-plus-circle'"></i> 
              {{ isEdit ? 'Cập nhật Voucher' : 'Thêm Voucher Mới' }}
            </h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
          </div>
          <div class="modal-body">
            <form @submit.prevent="saveVoucher">
              <div class="row mb-3">
                <div class="col-md-12">
                  <label class="form-label fw-bold">Tên Voucher <span class="text-danger">*</span></label>
                  <input type="text" class="form-control" v-model="formData.tenVoucher" required placeholder="Vd: Giảm 50K cho đơn từ 500K">
                </div>
              </div>

              <div class="row mb-3">
                <div class="col-md-4">
                  <label class="form-label fw-bold">Điểm cần đổi <span class="text-danger">*</span></label>
                  <input type="number" class="form-control" v-model="formData.diemCanDoi" required min="1" :disabled="isEdit">
                </div>
                <div class="col-md-4">
                  <label class="form-label fw-bold">Giá trị giảm (VNĐ) <span class="text-danger">*</span></label>
                  <input type="number" class="form-control" v-model="formData.giaTriGiam" required min="1" :disabled="isEdit">
                </div>
                <div class="col-md-4">
                  <label class="form-label fw-bold">Đơn tối thiểu (VNĐ)</label>
                  <input type="number" class="form-control" v-model="formData.donToiThieu" min="0" :disabled="isEdit">
                </div>
              </div>

              <div class="row mb-3">
                <div class="col-md-4">
                  <label class="form-label fw-bold">Số lượng phát hành <span class="text-danger">*</span></label>
                  <input type="number" class="form-control" v-model="formData.soLuong" required min="0">
                  <small v-if="isEdit" class="text-muted">Có thể tăng thêm số lượng.</small>
                </div>
                <div class="col-md-4">
                  <label class="form-label fw-bold">Ngày bắt đầu <span class="text-danger">*</span></label>
                  <input type="datetime-local" class="form-control" v-model="formData.ngayBatDau" required>
                </div>
                <div class="col-md-4">
                  <label class="form-label fw-bold">Ngày kết thúc</label>
                  <input type="datetime-local" class="form-control" v-model="formData.ngayKetThuc">
                  <small v-if="isEdit" class="text-muted">Có thể gia hạn hoặc rút ngắn.</small>
                </div>
              </div>

              <div class="modal-footer px-0 pb-0">
                <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Hủy</button>
                <button type="submit" class="btn btn-dark" :disabled="saving">
                  <span v-if="saving" class="spinner-border spinner-border-sm me-2"></span>
                  <i v-else class="bi bi-save me-2"></i>Lưu lại
                </button>
              </div>
            </form>
          </div>
        </div>
      </div>
    </div>

<div class="modal fade" id="confirmActivateModal" tabindex="-1" aria-hidden="true" ref="confirmActivateModal">
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title text-success">
              <i class="bi bi-play-circle me-2"></i>Mở lại Voucher
            </h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
          </div>
          <div class="modal-body">
            <p>Bạn có muốn phát hành lại voucher <strong>{{ selectedVoucher?.tenVoucher }}</strong> không?</p>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Hủy</button>
            <button type="button" class="btn btn-success" @click="executeActivateVoucher" :disabled="activating">
              <span v-if="activating" class="spinner-border spinner-border-sm me-2"></span>
              <i v-else class="bi bi-play-circle me-2"></i>Mở lại
            </button>
          </div>
        </div>
      </div>
    </div>

    <div class="modal fade" id="confirmStopModal" tabindex="-1" aria-hidden="true" ref="confirmStopModal">
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title text-danger">
              <i class="bi bi-exclamation-triangle me-2"></i>Ngừng hoạt động Voucher
            </h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
          </div>
          <div class="modal-body">
            <p>Bạn có chắc chắn muốn ngừng hoạt động voucher <strong>{{ selectedVoucher?.tenVoucher }}</strong>?</p>
            <p class="text-muted small">Khách hàng sẽ không thể đổi hoặc sử dụng voucher này nữa.</p>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Hủy</button>
            <button type="button" class="btn btn-danger" @click="executeStopVoucher" :disabled="deactivating">
              <span v-if="deactivating" class="spinner-border spinner-border-sm me-2"></span>
              <i v-else class="bi bi-stop-circle me-2"></i>Ngừng hoạt động
            </button>
          </div>
        </div>
      </div>
    </div>

    <main class="main-content" :class="{ 'expanded': isSidebarCollapsed }">
      <div class="page-container">
        <div class="row g-3 mb-4">
          <div class="col-md-3">
            <div class="stats-mini bg-gradient-1">
              <h4>{{ totalVouchers }}</h4>
              <p>Tổng chiến dịch</p>
            </div>
          </div>
          <div class="col-md-3">
            <div class="stats-mini bg-gradient-2">
              <h4>{{ activeVouchers }}</h4>
              <p>Đang hoạt động</p>
            </div>
          </div>
          <div class="col-md-3">
            <div class="stats-mini bg-gradient-4">
              <h4>{{ outOfStockVouchers }}</h4>
              <p>Đã hết lượt (Số lượng = 0)</p>
            </div>
          </div>
          <div class="col-md-3">
            <div class="stats-mini bg-gradient-3">
              <h4>{{ inactiveVouchers }}</h4>
              <p>Đã ngưng / Hết hạn</p>
            </div>
          </div>
        </div>

        <div class="content-card">
          <div class="d-flex justify-content-between align-items-center mb-4">
            <div>
              <h5 class="page-title">Quản lý Voucher</h5>
              <p class="page-subtitle">Thêm mới, theo dõi và cấu hình mã giảm giá</p>
            </div>
            <button class="btn btn-dark" @click="openAddModal">
              <i class="bi bi-plus-lg me-2"></i>Thêm Voucher
            </button>
          </div>

          <div class="filter-section">
            <div class="row g-3">
              <div class="col-md-4">
                <div class="input-group">
                  <span class="input-group-text bg-light"><i class="bi bi-search"></i></span>
                  <input type="text" v-model="filterKeyword" class="form-control" placeholder="Tìm theo tên voucher"/>
                </div>
              </div>
              <div class="col-md-3">
                <select v-model="filterStatus" class="form-select">
                  <option value="">Tất cả trạng thái</option>
                  <option value="1">Đang phát hành</option>
                  <option value="0">Đã ngưng / Hết hạn đổi</option>
                </select>
              </div>
              <div class="col-md-3">
                <select v-model="sortOrder" class="form-select">
                  <option value="desc">Mới nhất</option>
                  <option value="asc">Cũ nhất</option>
                </select>
              </div>
              <div class="col-md-2">
                <button @click="resetFilters" class="btn btn-outline-secondary w-100">
                  <i class="bi bi-arrow-clockwise me-2"></i>Reset
                </button>
              </div>
            </div>
          </div>

          <div class="table-responsive custom-table-wrapper">
            <table class="table table-hover align-middle mb-0">
              <thead class="table-light">
                <tr>
                  <th>Tên Voucher</th>
                  <th>Điểm đổi</th>
                  <th>Giá trị giảm</th>
                  <th>Đơn tối thiểu</th>
                  <th>Số lượng</th>
                  <th>Thời gian đổi</th>
                  <th>Trạng thái</th>
                  <th class="text-end">Thao tác</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="item in paginatedVouchers" :key="item.maVoucher">
                  <td>{{ item.tenVoucher }}</td>
                  <td><span class="badge bg-warning text-dark">{{ item.diemCanDoi }} điểm</span></td>
                  <td class="text-danger fw-bold">{{ formatPrice(item.giaTriGiam) }}</td>
                  <td>{{ formatPrice(item.donToiThieu) }}</td>
                  <td>
                    <span :class="item.soLuong > 0 ? 'text-success' : 'text-danger fw-bold'">
                      {{ item.soLuong }}
                    </span>
                  </td>
                  <td>
                    <div class="small">
                      <div>Từ: {{ formatDate(item.ngayBatDau) }}</div>
                      <div>Đến: <span :class="{'text-danger': isExpired(item.ngayKetThuc)}">{{ formatDate(item.ngayKetThuc) || 'Không giới hạn' }}</span></div>
                    </div>
                  </td>
                  <td>
                    <span class="badge" :class="getBadgeClass(item)">
                      {{ getStatusText(item) }}
                    </span>
                  </td>
                  <td class="text-end">
                    <button class="btn btn-outline-primary btn-sm me-2" @click="openEditModal(item)" title="Chỉnh sửa">
                      <i class="bi bi-pencil"></i>
                    </button>
                    
                    <button v-if="item.isActive === false || item.isActive === 0" 
                            class="btn btn-outline-success btn-sm" 
                            @click="confirmActivate(item)" 
                            title="Mở lại hoạt động" 
                            :disabled="isExpired(item.ngayKetThuc)">
                      <i class="bi bi-play-circle"></i>
                    </button>

                    <button v-else 
                            class="btn btn-outline-danger btn-sm" 
                            @click="confirmStop(item)" 
                            title="Ngừng hoạt động" 
                            :disabled="isExpired(item.ngayKetThuc)">
                      <i class="bi bi-stop-circle"></i>
                    </button>
                  </td>
                </tr>
                <tr v-if="filteredVouchers.length === 0 && !loading">
                  <td colspan="9" class="text-center py-4 text-muted">
                    <i class="bi bi-ticket-perforated display-6 d-block mb-2"></i>
                    Không tìm thấy voucher nào
                  </td>
                </tr>
              </tbody>
            </table>
          </div>

          <div v-if="totalPages > 1" class="d-flex justify-content-center align-items-center mt-4">
            <nav aria-label="Page navigation">
              <ul class="pagination pagination-sm mb-0">
                <li class="page-item" :class="{ disabled: currentPage === 1 }">
                  <button class="page-link text-dark" @click="goToPage(currentPage - 1)">
                    <i class="bi bi-chevron-left"></i> Trước
                  </button>
                </li>
                <li class="page-item" v-for="page in totalPages" :key="page" :class="{ active: currentPage === page }">
                  <button class="page-link" :class="currentPage === page ? 'bg-dark border-dark text-white' : 'text-dark'" @click="goToPage(page)">
                    {{ page }}
                  </button>
                </li>
                <li class="page-item" :class="{ disabled: currentPage === totalPages }">
                  <button class="page-link text-dark" @click="goToPage(currentPage + 1)">
                    Sau <i class="bi bi-chevron-right"></i>
                  </button>
                </li>
              </ul>
            </nav>
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
import { Modal } from "bootstrap";

axios.defaults.withCredentials = true;

const isSidebarCollapsed = ref(false);
const vouchers = ref([]);
const loading = ref(false);
const saving = ref(false);
const deactivating = ref(false);
const activating = ref(false);
const confirmActivateModal = ref(null);
let confirmActivateModalInstance = null;


const filterKeyword = ref("");
const filterStatus = ref("");
const sortOrder = ref("desc");
const currentPage = ref(1);
const itemsPerPage = 10;


const isEdit = ref(false);
const selectedVoucher = ref(null);
const formData = ref({
  maVoucher: null,
  tenVoucher: "",
  diemCanDoi: 1,
  giaTriGiam: 0,
  donToiThieu: 0,
  soLuong: 0,
  ngayBatDau: "",
  ngayKetThuc: "",
  isActive: 1
});

const voucherModal = ref(null);
const confirmStopModal = ref(null);
let voucherModalInstance = null;
let confirmStopModalInstance = null;

const toast = ref({ show: false, message: "", type: "success" });
let toastTimeout = null;

const showToast = (message, type = "success") => {
  toast.value = { show: true, message, type };
  if (toastTimeout) clearTimeout(toastTimeout);
  toastTimeout = setTimeout(() => { toast.value.show = false; }, 5000);
};

const confirmActivate = (item) => {
  selectedVoucher.value = item;
  if (confirmActivateModalInstance) confirmActivateModalInstance.show();
};

const executeActivateVoucher = async () => {
  if (!selectedVoucher.value) return;
  activating.value = true;
  try {
    await axios.put(`http://localhost:8080/api/voucher/activate/${selectedVoucher.value.maVoucher}`);
    showToast("Đã mở lại voucher thành công!");
    fetchVouchers(); // Load lại bảng
    if (confirmActivateModalInstance) confirmActivateModalInstance.hide();
  } catch (error) {
    console.error("Lỗi mở lại voucher:", error);
    showToast(error.response?.data?.message || "Đã xảy ra lỗi khi mở lại", "danger");
  } finally {
    activating.value = false;
  }
};

const fetchVouchers = async () => {
  loading.value = true;
  try {
    const response = await axios.get("http://localhost:8080/api/voucher/list");
    // Giả sử API trả về mảng trực tiếp hoặc response.data.data
    vouchers.value = response.data.data || response.data;
  } catch (error) {
    console.error("Lỗi tải voucher:", error);
    showToast("Không thể tải danh sách voucher", "danger");
  } finally {
    loading.value = false;
  }
};


const formatPrice = (price) => {
  if (!price) return "0 ₫";
  return price.toLocaleString("vi-VN") + " ₫";
};


const formatDateForInput = (dateString) => {
  if (!dateString) return "";

  return dateString.substring(0, 16);
};


const formatDate = (dateString) => {
  if (!dateString) return "";
  const date = new Date(dateString);
  return date.toLocaleString("vi-VN", {
    day: "2-digit", 
    month: "2-digit", 
    year: "numeric", 
    hour: "2-digit", 
    minute: "2-digit"
  });
};

const isNotStarted = (startDateString) => {
  if (!startDateString) return false;
  return new Date(startDateString) > new Date();
};

const isExpired = (endDateString) => {
  if (!endDateString) return false;
  return new Date(endDateString) < new Date();
};

const getStatusText = (item) => {

  if (isExpired(item.ngayKetThuc)) return "Hết hạn đổi";
  
  if (item.isActive === 0 || item.isActive === false) return "Đã ngưng"; 
  
  if (isNotStarted(item.ngayBatDau)) return "Chưa bắt đầu";
  
  if (item.soLuong === 0) return "Hết lượt";
  
  return "Đang phát hành";
};

const getBadgeClass = (item) => {
  if (item.isActive === 0 || item.isActive === false || isExpired(item.ngayKetThuc)) {
    return "bg-secondary";
  }
  if (isNotStarted(item.ngayBatDau)) return "bg-info text-dark";
  if (item.soLuong === 0) return "bg-warning text-dark";
  return "bg-success";
};

const openAddModal = () => {
  isEdit.value = false;
  formData.value = {
    maVoucher: null,
    tenVoucher: "",
    diemCanDoi: 10,
    giaTriGiam: 10000,
    donToiThieu: 0,
    soLuong: 100,
    ngayBatDau: "",
    ngayKetThuc: "",
    isActive: 1
  };
  if (voucherModalInstance) voucherModalInstance.show();
};

const openEditModal = (item) => {
  isEdit.value = true;
  formData.value = {
    ...item,
    ngayBatDau: formatDateForInput(item.ngayBatDau),
    ngayKetThuc: formatDateForInput(item.ngayKetThuc)
  };
  if (voucherModalInstance) voucherModalInstance.show();
};

const saveVoucher = async () => {
  if (formData.value.ngayKetThuc && new Date(formData.value.ngayKetThuc) <= new Date(formData.value.ngayBatDau)) {
    showToast("Ngày kết thúc phải lớn hơn ngày bắt đầu", "danger");
    return;
  }

  saving.value = true;
  try {
    let url = "http://localhost:8080/api/voucher/add";
    let method = "post";

    if (isEdit.value) {
      url = `http://localhost:8080/api/voucher/update/${formData.value.maVoucher}`;
      method = "put";
    }

    const response = await axios[method](url, formData.value);
    
    if (response.data) {
      showToast(isEdit.value ? "Cập nhật thành công!" : "Thêm mới thành công!");
      fetchVouchers();
      if (voucherModalInstance) voucherModalInstance.hide();
    }
  } catch (error) {
    console.error("Lỗi lưu voucher:", error);
    showToast("Đã xảy ra lỗi khi lưu", "danger");
  } finally {
    saving.value = false;
  }
};

const confirmStop = (item) => {
  selectedVoucher.value = item;
  if (confirmStopModalInstance) confirmStopModalInstance.show();
};

const executeStopVoucher = async () => {
  if (!selectedVoucher.value) return;
  deactivating.value = true;
  try {
    await axios.put(`http://localhost:8080/api/voucher/deactivate/${selectedVoucher.value.maVoucher}`);
    showToast("Đã ngừng hoạt động voucher thành công!");
    fetchVouchers();
    if (confirmStopModalInstance) confirmStopModalInstance.hide();
  } catch (error) {
    console.error("Lỗi ngưng voucher:", error);
    showToast("Đã xảy ra lỗi khi ngừng hoạt động", "danger");
  } finally {
    deactivating.value = false;
  }
};


const filteredVouchers = computed(() => {
  let result = vouchers.value.filter((item) => {
    const matchKeyword = !filterKeyword.value || item.tenVoucher.toLowerCase().includes(filterKeyword.value.toLowerCase());
    
    let matchStatus = true;
    if (filterStatus.value === "1") matchStatus = item.isActive == true;
    if (filterStatus.value === "0") matchStatus = item.isActive == false;
    
    return matchKeyword && matchStatus;
  });

  return result.sort((a, b) => {
    return sortOrder.value === "desc" 
      ? b.maVoucher - a.maVoucher 
      : a.maVoucher - b.maVoucher;
  });
});

const totalVouchers = computed(() => vouchers.value.length);
const activeVouchers = computed(() => vouchers.value.filter(v => v.isActive && v.soLuong > 0 && !isExpired(v.ngayKetThuc)).length);
const outOfStockVouchers = computed(() => vouchers.value.filter(v => v.isActive && v.soLuong === 0).length);
const inactiveVouchers = computed(() => vouchers.value.filter(v => !v.isActive || isExpired(v.ngayKetThuc)).length);

const totalPages = computed(() => Math.ceil(filteredVouchers.value.length / itemsPerPage));
const paginatedVouchers = computed(() => {
  const start = (currentPage.value - 1) * itemsPerPage;
  return filteredVouchers.value.slice(start, start + itemsPerPage);
});

const goToPage = (page) => {
  if (page >= 1 && page <= totalPages.value) currentPage.value = page;
};

const resetFilters = () => {
  filterKeyword.value = "";
  filterStatus.value = "";
  sortOrder.value = "desc";
  currentPage.value = 1;
};

const handleSidebarCollapse = (collapsedState) => {
  isSidebarCollapsed.value = collapsedState;
};

watch([filterKeyword, filterStatus, sortOrder], () => { currentPage.value = 1; });

onMounted(() => {
  fetchVouchers();
  if (voucherModal.value) voucherModalInstance = new Modal(voucherModal.value);
  if (confirmStopModal.value) confirmStopModalInstance = new Modal(confirmStopModal.value);
  if (confirmActivateModal.value) confirmActivateModalInstance = new Modal(confirmActivateModal.value);
});
</script>

<style scoped>
.main-content { margin-left: 260px; min-height: 100vh; background: #f8f9fa; transition: margin-left 0.3s; }
.main-content.expanded { margin-left: 80px; }
.page-container { padding: 30px; }


.custom-table-wrapper {
  background: #fff;
  border-radius: 12px; 
  overflow: hidden; 
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08); 
  border: 1px solid #eaeaea; 
}

.custom-table-wrapper thead th {
  background-color: #f8f9fa;
  color: #495057;
  font-weight: 600;
  text-transform: uppercase;
  font-size: 13px;
  padding: 16px 12px;
  border-bottom: 2px solid #edf2f9;
  white-space: nowrap;
}

.custom-table-wrapper tbody td {
  padding: 16px 12px;
  color: #333;
  border-bottom: 1px solid #f1f3f5;
}

.custom-table-wrapper tbody tr:last-child td {
  border-bottom: none; 
}

.custom-table-wrapper tbody tr:hover td {
  background-color: #f4f6f8; 
}

.stats-mini { padding: 25px 20px; border-radius: 12px; text-align: center; color: rgb(255, 255, 255); box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1); }
.stats-mini h4 { font-size: 32px; font-weight: bold; margin: 0; }
.stats-mini p { margin: 8px 0 0; font-size: 14px; opacity: 0.9; }
.bg-gradient-1, .bg-gradient-2, .bg-gradient-3, .bg-gradient-4 { background: linear-gradient(135deg, #212529, #000000); }

.content-card { background: white; border-radius: 16px; padding: 30px; box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05); }
.page-title { font-size: 24px; font-weight: bold; color: #333; margin-bottom: 5px; }
.page-subtitle { color: #666; font-size: 14px; margin-bottom: 0; }
.filter-section { background: #f8f9fa; padding: 20px; border-radius: 12px; margin-bottom: 25px; border: 1px solid #eee; }

.toast-progress-bar { height: 4px; background-color: rgba(255, 255, 255, 0.7); width: 100%; position: absolute; bottom: 0; left: 0; border-bottom-left-radius: 0.375rem; animation: shrinkProgress 5s linear forwards; }
@keyframes shrinkProgress { from { width: 100%; } to { width: 0%; } }

@media (max-width: 768px) {
  .main-content { margin-left: 0; }
  .page-container { padding: 15px; }
}
</style>