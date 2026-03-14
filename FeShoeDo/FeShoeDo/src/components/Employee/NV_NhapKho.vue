<template>

<div class="toast-container position-fixed top-0 start-50 translate-middle-x p-3 mt-2" style="z-index: 1090;">
  <transition name="toast-fade">
    <div
      v-if="toast.show"
      :key="toast.id"
      class="toast show align-items-center text-white border-0 shadow-lg overflow-hidden"
      :class="`bg-${toast.type}`"
      role="alert"
    >
      <div class="d-flex position-relative z-1">
        <div class="toast-body d-flex align-items-center fs-6">
          <i
            class="bi me-2 fs-5"
            :class="{
              'bi-check-circle-fill': toast.type === 'success',
              'bi-exclamation-triangle-fill': toast.type === 'warning',
              'bi-x-circle-fill': toast.type === 'danger',
              'bi-info-circle-fill': toast.type === 'info'
            }"
          ></i>
          {{ toast.message }}
        </div>
        <button
          type="button"
          class="btn-close btn-close-white me-2 m-auto"
          @click="toast.show = false"
        ></button>
      </div>
      <div class="toast-progress-bar"></div>
    </div>
  </transition>
</div>

  <div class="employee-layout">
    <NV_Sidebar />

    <main class="main-content">
      <div class="page-container">
        <ul class="nav nav-tabs" role="tablist">
          <li class="nav-item" role="presentation">
            <a
              class="nav-link"
              :class="{ active: activeTab === 'import' }"
              @click="activeTab = 'import'"
              style="cursor: pointer"
            >
              <i class="bi bi-box-seam me-2"></i>Nhập kho
            </a>
          </li>
          <li class="nav-item" role="presentation">
            <a
              class="nav-link"
              :class="{ active: activeTab === 'history' }"
              @click="activeTab = 'history'"
              style="cursor: pointer"
            >
              <i class="bi bi-clock-history me-2"></i>Lịch sử nhập kho
            </a>
          </li>
        </ul>

        <div class="tab-content p-3 border border-top-0">
          <div v-show="activeTab === 'import'" class="tab-pane show active">
            <div class="row g-2 mb-3">
              <div class="col-md-3">
                <input
                  type="text"
                  v-model="filterKeyword"
                  placeholder="Tìm theo tên sản phẩm..."
                  class="form-control"
                />
              </div>
              <div class="col-md-2">
                <select v-model="filterCategory" class="form-select">
                  <option value="">Tất cả danh mục</option>
                  <option
                    v-for="cat in categories"
                    :key="cat.maDM"
                    :value="cat.maDM"
                  >
                    {{ cat.tenDM }}
                  </option>
                </select>
              </div>
              <div class="col-md-2">
                <select v-model="filterStatus" class="form-select">
                  <option value="">Tất cả trạng thái</option>
                  <option value="Còn hàng">Còn hàng (SL > 10)</option>
                  <option value="Sắp hết">Sắp hết (SL 1-10)</option>
                  <option value="Hết hàng">Hết hàng (SL = 0)</option>
                </select>
              </div>
              <div class="col-md-2">
                <button @click="resetFilters" class="btn btn-secondary">
                  <i class="bi bi-arrow-clockwise me-2"></i>Reset
                </button>
              </div>
            </div>

            <div class="row g-2 mb-3 align-items-center">
              <div class="col-md-4">
                <div class="input-group">
                  <span class="input-group-text bg-light fw-bold"
                    >Số lượng chung</span
                  >
                  <input
                    type="number"
                    v-model="bulkQuantity"
                    min="1"
                    class="form-control form-control-lg"
                    placeholder="Nhập số lượng"
                  />
                  <button @click="applyBulkQuantity" class="btn btn-dark">
                    <i class="bi bi-check-lg me-2"></i>Áp dụng
                  </button>
                </div>
              </div>

              <div class="col-md-8">
                <div
                  class="d-flex justify-content-end align-items-center gap-2"
                >
                  <span class="text-muted me-3">
                    Đã chọn:
                    <strong class="text-primary">{{ selectedCount }}</strong> /
                    {{ filteredProducts.length }} phân loại
                  </span>
                  <button
                    @click="unselectAll"
                    class="btn btn-outline-secondary"
                  >
                    <i class="bi bi-x-circle me-2"></i>Bỏ chọn tất cả
                  </button>
                  <button @click="handleBulkImport" class="btn btn-dark">
                    <i class="bi bi-box-arrow-in-down me-2"></i>
                    Nhập hàng loạt
                  </button>
                </div>
              </div>
            </div>

            <div class="table-responsive">
              <table class="table table-bordered table-hover">
                <thead class="table-dark">
                  <tr>
                    <th>
                      <div class="form-check">
                        <input
                          class="form-check-input"
                          type="checkbox"
                          v-model="isAllSelected"
                        />
                      </div>
                    </th>
                    <th class="text-center align-middle">Hình</th>
                    <th class="text-center align-middle">Tên sản phẩm</th>
                    <th class="text-center align-middle">Phân loại</th>
                    <th class="text-center align-middle">Giá</th>
                    <th class="text-center align-middle">SL tồn</th>
                    <th class="text-center align-middle">Trạng thái</th>
                    <th style="width: 180px" class="text-center align-middle">Số lượng nhập</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="item in filteredProducts" :key="item.maSKU">
                    <td>
                      <div class="form-check">
                        <input
                          class="form-check-input"
                          type="checkbox"
                          v-model="item.selected"
                        />
                      </div>
                    </td>
                    <td style="width: 72px">
                      <img
                        :src="
                          item.hinhAnh
                            ? `/images/${item.hinhAnh}`
                            : 'https://placehold.co/80x80'
                        "
                        alt="Product"
                        style="
                          width: 48px;
                          height: 48px;
                          object-fit: cover;
                          border-radius: 8px;
                        "
                      />
                    </td>
                    <td>
                      <strong>{{ item.sanPham?.tenSP }}</strong>
                      <div class="text-muted small">
                        Mã SKU: {{ item.maSKU }}
                      </div>
                    </td>
                    <td>
                      <span class="badge bg-info text-dark"
                        >{{ item.tenMau }} - Size {{ item.size?.coGiay }}</span
                      >
                    </td>
                    <td class="text-end">
                      {{ item.donGia?.toLocaleString("vi-VN") }} ₫
                    </td>
                    <td class="text-end">
                      <span
                        :class="[
                          'fw-bold',
                          item.soLuong > 10
                            ? 'text-success'
                            : item.soLuong > 0
                            ? 'text-warning'
                            : 'text-danger',
                        ]"
                      >
                        {{ item.soLuong }}
                      </span>
                    </td>
                    <td>
                      <span
                        :class="[
                          'badge',
                          item.soLuong > 10
                            ? 'bg-success'
                            : item.soLuong > 0
                            ? 'bg-warning text-dark'
                            : 'bg-danger',
                        ]"
                      >
                        {{ item.soLuong > 0 ? "Còn hàng" : "Hết hàng" }}
                      </span>
                    </td>
                    <td>
                      <div class="d-flex align-items-center gap-2">
                        <div class="input-group" style="min-width: 150px">
                          <input
                            type="number"
                            v-model="item.soLuongNhap"
                            class="form-control"
                            min="1"
                            placeholder="Số lượng"
                            style="padding: 10px; font-size: 14px"
                          />
                          <button
                            @click="handleImportSingle(item)"
                            class="btn btn-outline-dark"
                          >
                            <i class="bi bi-arrow-down-circle me-1"></i>Nhập
                          </button>
                        </div>
                      </div>
                    </td>
                  </tr>
                  <tr v-if="filteredProducts.length === 0">
                    <td colspan="8" class="text-center py-4 text-muted">
                      Không tìm thấy sản phẩm nào phù hợp.
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>

          <div v-show="activeTab === 'history'" class="tab-pane show active">
            
            <div class="row g-2 mb-3">
              <div class="col-md-5">
                <div class="input-group">
                  <span class="input-group-text"><i class="bi bi-search"></i></span>
                  <input type="text" v-model="historyKeyword" placeholder="Tìm theo tên sản phẩm..." class="form-control">
                </div>
              </div>
              <div class="col-md-4">
                <div class="input-group">
                  <span class="input-group-text fw-bold">Lọc theo ngày</span>
                  <input type="date" v-model="historyDate" class="form-control">
                </div>
              </div>
              <div class="col-md-3">
                <button @click="resetHistoryFilters" class="btn btn-secondary w-100">
                  <i class="bi bi-arrow-clockwise me-2"></i>Reset
                </button>
              </div>
            </div>
            
            <div class="table-responsive">
              <table class="table table-bordered">
                <thead class="table-dark">
                  <tr>
                    <th class="text-center">Mã NK</th>
                    <th>Sản phẩm</th>
                    <th>Danh mục</th>
                    <th>Phân loại</th>
                    <th>Số lượng</th>
                    <th>Ngày nhập</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="log in filteredHistory" :key="log.maNK">
                    <td class="text-center fw-bold text-primary">NK{{ log.maNK }}</td>
                    <td>
                      <strong>{{ log.sanPhamChiTiet?.sanPham?.tenSP }}</strong>
                      <div class="text-muted small">Mã SKU: {{ log.sanPhamChiTiet?.maSKU }}</div>
                    </td>
                    <td>
                      <span class="badge bg-secondary">Lịch sử</span>
                    </td>
                    <td>
                      <span class="badge bg-info text-dark">{{ log.sanPhamChiTiet?.tenMau }} - Size {{ log.sanPhamChiTiet?.size?.coGiay }}</span>
                    </td>
                    <td>
                      <span class="badge bg-success">+{{ log.soLuong }}</span>
                    </td>
                    <td>{{ formatDate(log.ngayNhap) }}</td>
                  </tr>
                  <tr v-if="filteredHistory.length === 0">
                    <td colspan="6" class="text-center py-4 text-muted">Không tìm thấy lịch sử phù hợp.</td>
                  </tr>
                </tbody>
              </table>
            </div>

            <div class="text-muted text-center mt-2" v-if="filteredHistory.length > 0">
              Tổng <strong>{{ filteredHistory.length }}</strong> bản ghi
            </div>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted, watch, computed } from "vue";
import axios from "axios";
import NV_Sidebar from "@/components/Shared/NV_Sidebar.vue";

axios.defaults.withCredentials = true;

const activeTab = ref("import");
const products = ref([]);
const history = ref([]);
const bulkQuantity = ref(10);

// --- STATE CHO BỘ LỌC ---
const categories = ref([]);
const filterKeyword = ref("");
const filterCategory = ref("");
const filterStatus = ref("");

// Hàm lấy danh sách Danh Mục
const fetchCategories = async () => {
  try {
    const response = await axios.get(
      "http://localhost:8080/api/nhapkho/danhmuc"
    );
    categories.value = response.data;
  } catch (error) {
    console.error("Lỗi lấy danh mục:", error);
  }
};

// Hàm lấy danh sách Sản Phẩm
const fetchProducts = async () => {
  try {
    const response = await axios.get(
      "http://localhost:8080/api/nhapkho/sanpham"
    );
    products.value = response.data.map((item) => ({
      ...item,
      soLuongNhap: 1,
      selected: false,
    }));
  } catch (error) {
    console.error("Lỗi lấy danh sách sản phẩm:", error);
  }
};

const fetchHistory = async () => {
  try {
    const response = await axios.get(
      "http://localhost:8080/api/nhapkho/lichsu"
    );
    history.value = response.data;
  } catch (error) {
    console.error("Lỗi lấy lịch sử:", error);
  }
};

// --- LOGIC LỌC SẢN PHẨM (COMPUTED) ---
const filteredProducts = computed(() => {
  return products.value.filter((item) => {
    // 1. Lọc theo tên sản phẩm
    const matchKeyword =
      !filterKeyword.value ||
      item.sanPham?.tenSP
        ?.toLowerCase()
        .includes(filterKeyword.value.toLowerCase());

    // 2. Lọc theo trạng thái
    let matchStatus = true;
    if (filterStatus.value === "Còn hàng") {
      matchStatus = item.soLuong > 10;
    } else if (filterStatus.value === "Sắp hết") {
      matchStatus = item.soLuong > 0 && item.soLuong <= 10;
    } else if (filterStatus.value === "Hết hàng") {
      matchStatus = item.soLuong === 0;
    }

    // 3. Lọc theo danh mục
    let matchCategory = true;
    if (filterCategory.value) {
      // Vì dữ liệu API của bạn đang ẩn List danh mục (do @JsonIgnore ở Entity),
      // logic này sẽ hoạt động ngay khi backend mở trả danh sách danh mục về.
      if (item.sanPham?.sanPhamDanhMucs) {
        matchCategory = item.sanPham.sanPhamDanhMucs.some(
          (dm) =>
            dm.danhMuc?.maDM === filterCategory.value ||
            dm.maDM === filterCategory.value
        );
      }
    }

    return matchKeyword && matchStatus && matchCategory;
  });
});

// Hàm Reset bộ lọc
const resetFilters = () => {
  filterKeyword.value = "";
  filterCategory.value = "";
  filterStatus.value = "";
};

// --- LOGIC CHO NHẬP HÀNG LOẠT ---

// Cập nhật lại: Đếm số lượng chọn dựa trên danh sách đang hiển thị
const selectedCount = computed(
  () => filteredProducts.value.filter((p) => p.selected).length
);

// Cập nhật lại: Nút chọn tất cả chỉ áp dụng cho danh sách đang hiển thị
const isAllSelected = computed({
  get: () =>
    filteredProducts.value.length > 0 &&
    filteredProducts.value.every((p) => p.selected),
  set: (val) => filteredProducts.value.forEach((p) => (p.selected = val)),
});

const unselectAll = () => {
  products.value.forEach((p) => (p.selected = false));
};

const applyBulkQuantity = () => {
  if (bulkQuantity.value < 1) {
    showToast("Số lượng phải lớn hơn 0");
    return;
  }
  // Chỉ áp dụng cho sản phẩm đang lọc và được tick
  filteredProducts.value.forEach((p) => {
    if (p.selected) p.soLuongNhap = bulkQuantity.value;
  });
};

const handleBulkImport = async () => {
  const selectedItems = products.value.filter((p) => p.selected);
  if (selectedItems.length === 0) {
    showToast("Vui lòng tích chọn ít nhất 1 sản phẩm để nhập kho!","warning");
    return;
  }

  const payload = selectedItems.map((item) => ({
    maSKU: item.maSKU,
    soLuongNhap: item.soLuongNhap,
  }));

  try {
    await axios.post(
      "http://localhost:8080/api/nhapkho/nhap-hang-loat",
      payload
    );
    showToast(`Đã nhập kho thành công ${selectedItems.length} sản phẩm!`);
    unselectAll(); // Xóa tick sau khi nhập xong
    await fetchProducts();
  } catch (error) {
    showToast("Có lỗi xảy ra khi nhập kho hàng loạt!","danger");
    console.error(error);
  }
};

const handleImportSingle = async (item) => {
  if (!item.soLuongNhap || item.soLuongNhap < 1) {
    showToast("Số lượng nhập phải lớn hơn 0","warning");
    return;
  }
  try {
    await axios.post("http://localhost:8080/api/nhapkho/nhap", {
      maSKU: item.maSKU,
      soLuongNhap: item.soLuongNhap,
    });
    showToast("Nhập kho thành công!");
    await fetchProducts();
  } catch (error) {
    showToast("Có lỗi xảy ra!","danger");
    console.error(error);
  }
};

const formatDate = (dateString) => {
  if (!dateString) return "";
  const date = new Date(dateString);
  return date.toLocaleDateString("vi-VN");
};

// Trạng thái của thông báo (Toast)
const toast = ref({
  id: 0, // Thêm ID để ép reset thanh tiến trình
  show: false,
  message: "",
  type: "success",
});

let toastTimeout = null;

// Hàm gọi thông báo dùng chung
const showToast = (message, type = "success") => {
  // Gán Date.now() làm ID giúp mỗi lần bật là một animation mới hoàn toàn
  toast.value = { id: Date.now(), show: true, message, type };
  
  if (toastTimeout) clearTimeout(toastTimeout);
  
  // Tự động tắt sau đúng 3 giây
  toastTimeout = setTimeout(() => {
    toast.value.show = false;
  }, 5000);
};

watch(activeTab, (newVal) => {
  if (newVal === "import") {
    fetchProducts();
    fetchCategories();
  }
  if (newVal === "history") fetchHistory();
});

onMounted(() => {
  fetchProducts();
  fetchCategories();
});

// --- STATE CHO BỘ LỌC LỊCH SỬ (TAB 2) ---
const historyKeyword = ref("");
const historyDate = ref("");

// --- LOGIC LỌC VÀ SẮP XẾP LỊCH SỬ ---
const filteredHistory = computed(() => {
  let result = history.value;

  // 1. Lọc theo tên sản phẩm
  if (historyKeyword.value) {
    const keyword = historyKeyword.value.toLowerCase();
    result = result.filter((log) =>
      log.sanPhamChiTiet?.sanPham?.tenSP?.toLowerCase().includes(keyword)
    );
  }

  // 2. Lọc theo ngày nhập
  if (historyDate.value) {
    result = result.filter((log) => {
      if (!log.ngayNhap) return false;
      // Format ngày từ database sang định dạng YYYY-MM-DD chuẩn của ô input type="date"
      const d = new Date(log.ngayNhap);
      const year = d.getFullYear();
      const month = String(d.getMonth() + 1).padStart(2, "0");
      const day = String(d.getDate()).padStart(2, "0");
      const formattedDate = `${year}-${month}-${day}`;

      return formattedDate === historyDate.value;
    });
  }

  // 3. Sắp xếp mới nhất lên đầu (Mã Nhập Kho giảm dần)
  return result.slice().sort((a, b) => b.maNK - a.maNK);
});

// Hàm làm mới bộ lọc lịch sử
const resetHistoryFilters = () => {
  historyKeyword.value = "";
  historyDate.value = "";
};
</script>

<style scoped>
.main-content {
  margin-left: 260px;
  min-height: 100vh;
}
.page-container {
  padding: 30px;
  background: #f7f7f9;
  min-height: 100vh;
}
.nav-tabs {
  border-bottom: 2px solid #dee2e6;
}
.nav-tabs .nav-link {
  color: #666;
  font-weight: 500;
  border: none;
  border-bottom: 3px solid transparent;
  padding: 12px 24px;
  transition: all 0.3s;
  cursor: pointer;
}
.nav-tabs .nav-link:hover {
  color: #00b7ffc7;
  border-color: transparent;
}
.nav-tabs .nav-link.active {
  color: #00b7ff;
  background: transparent;
  border-color: #00b7ff;
}
.tab-content {
  background: white;
  padding: 25px;
  border-radius: 0 0 12px 12px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.08);
}
.input-group .form-control-lg {
  font-size: 16px;
}
td .input-group {
  min-width: 180px;
}
td .form-control {
  height: 38px;
  padding: 8px 12px;
  font-size: 14px;
}
td .btn-outline-success {
  padding: 8px 16px;
  height: 38px;
}
.input-group-text {
  background-color: #f8f9fa;
  border-color: #dee2e6;
  font-size: 14px;
}
/* Hiệu ứng trượt từ trên xuống cho vị trí Center-Top */
.toast-fade-enter-active,
.toast-fade-leave-active {
  transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}
.toast-fade-enter-from {
  opacity: 0;
  transform: translateY(-50px);
}
.toast-fade-leave-to {
  opacity: 0;
  transform: translateY(-50px);
}
</style>