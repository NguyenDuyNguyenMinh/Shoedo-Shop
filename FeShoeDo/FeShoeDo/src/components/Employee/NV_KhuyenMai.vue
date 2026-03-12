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
        <div class="d-flex align-items-center mb-4 pb-2 border-bottom">
          <h4 class="mb-0 text-dark">
            <i class="bi bi-tags-fill me-2"></i>Quản lý Khuyến Mãi (Flash Sale)
          </h4>
        </div>

        <div class="bg-white p-4 rounded-3 shadow-sm border">
          <div class="row g-2 mb-4">
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
      <option v-for="cat in categories" :key="cat.maDM" :value="cat.maDM">{{ cat.tenDM }}</option>
    </select>
  </div>
  <div class="col-md-2">
    <select v-model="filterFlashSale" class="form-select">
      <option value="">Tất cả KM</option>
      <option value="DangSale">🔥 Đang Flash Sale (> 0%)</option>
      <option value="KhongSale">Không giảm giá (0%)</option>
    </select>
  </div>
  <div class="col-md-3">
    <select v-model="filterActive" class="form-select">
      <option value="">Tất cả trạng thái</option>
      <option value="true">Đang bán</option>
      <option value="false">Đã ẩn</option>
    </select>
  </div>
  <div class="col-md-2">
    <button @click="resetFilters" class="btn btn-secondary w-100">
      <i class="bi bi-arrow-clockwise me-1"></i>Đặt lại
    </button>
  </div>
          </div>

          <div
            class="row g-2 mb-3 align-items-center bg-light p-3 rounded border"
          >
            <div class="col-md-5">
              <div class="input-group">
                <span class="input-group-text bg-dark\ text-dark fw-bold">
                  <i class="bi bi-percent me-1"></i> Mức KM chung
                </span>
                <input
                  type="number"
                  v-model="bulkDiscount"
                  min="0"
                  max="100"
                  class="form-control form-control-lg"
                  placeholder="Nhập % (0-100)"
                />
                <button
                  @click="applyBulkDiscount"
                  class="btn btn-dark fw-bold"
                >
                  <i class="bi bi-check-lg me-2"></i>Áp dụng
                </button>
              </div>
            </div>

            <div class="col-md-7">
              <div class="d-flex justify-content-end align-items-center gap-2">
                <span class="text-muted me-3">
                  Đã chọn:
                  <strong class="text-danger">{{ selectedCount }}</strong> /
                  {{ filteredProducts.length }} sản phẩm
                </span>
                <button
                  @click="unselectAll"
                  class="btn btn-outline-secondary"
                  :disabled="selectedCount === 0"
                >
                  <i class="bi bi-x-circle me-1"></i>Bỏ chọn
                </button>
                <button
                  @click="handleBulkSave"
                  class="btn btn-danger fw-bold"
                  :disabled="selectedCount === 0"
                >
                  <i class="bi bi-lightning-fill me-1"></i>
                  Lưu KM hàng loạt
                </button>
              </div>
            </div>
          </div>

          <div class="table-responsive">
<table class="table table-bordered table-hover align-middle">
  <thead class="table-dark">
    <tr>
      <th style="width: 40px" class="text-center">
        <div class="form-check d-flex justify-content-center">
          <input
            class="form-check-input"
            type="checkbox"
            v-model="isAllSelected"
          />
        </div>
      </th>
      <th class="text-center align-middle" style="width: 80px">Hình</th>
      <th class="text-center align-middle">Sản Phẩm</th>
      <th class="text-center align-middle">Trạng Thái Flash Sale</th>
      <th class="text-center align-middle">Giá Hiện Tại</th>
      <th class="text-center align-middle" style="width: 200px">Thiết Lập KM Mới (%)</th>
      <th class="text-center align-middle bg-secondary">Preview Giá Mới</th>
    </tr>
  </thead>
  <tbody>
    <tr v-for="item in filteredProducts" :key="item.maSP">
      <td class="text-center">
        <div class="form-check d-flex justify-content-center">
          <input
            class="form-check-input"
            type="checkbox"
            v-model="item.selected"
          />
        </div>
      </td>
      
      <td>
        <img
          :src="item.hinhAnh ? `/images/${item.hinhAnh}` : 'https://placehold.co/80x80'"
          alt="Product"
          class="img-thumbnail"
          style="width: 60px; height: 60px; object-fit: cover;"
        />
      </td>
      
      <td>
        <div>
        <strong>{{ item.tenSP }}</strong>
        <br>
        <span v-if="item.isActive === false || item.active === false" class="badge bg-secondary" style="font-size: 0.7rem;">
            <i class="bi bi-eye-slash me-1"></i>Đã ẩn
          </span>
          <span v-else class="badge border border-success text-success" style="font-size: 0.7rem;">
            Đang bán
          </span>
          </div>
        <div class="text-muted small">
          Đã bán: {{ item.daBan }} | Mã SP: {{ item.maSP }}
        </div>
      </td>
      
      <td class="text-center">
        <span v-if="item.khuyenMai > 0" class="badge bg-danger px-3 py-2">
          🔥 Đang Sale ({{ item.khuyenMai }}%)
        </span>
        <span v-else class="badge bg-secondary px-3 py-2">
          Không Sale
        </span>
      </td>

      <td class="text-end">
        <div v-if="item.khuyenMai > 0">
          <span class="text-muted text-decoration-line-through small d-block">
            {{ formatCurrency(item.donGia) }}
          </span>
          <strong class="text-danger">
            {{ formatCurrency(item.donGia * (1 - item.khuyenMai / 100)) }}
          </strong>
        </div>
        <div v-else>
          <strong>{{ formatCurrency(item.donGia) }}</strong>
        </div>
      </td>

      <td>
        <div class="input-group">
          <input
            type="number"
            v-model="item.khuyenMaiMoi"
            class="form-control text-center fw-bold text-primary"
            min="0"
            max="100"
            placeholder="%"
          />
          <button @click="handleSingleSave(item)" class="btn btn-outline-dark">
            <i class="bi bi-save me-1"></i>Lưu
          </button>
        </div>
      </td>

      <td class="text-end bg-light">
        <div v-if="item.khuyenMaiMoi > 0">
          <span class="text-muted text-decoration-line-through small d-block">
            {{ formatCurrency(item.donGia) }}
          </span>
          <span class="fs-5 fw-bold text-success">
            {{ formatCurrency(item.donGia * (1 - item.khuyenMaiMoi / 100)) }}
          </span>
        </div>
        <div v-else class="fs-6 fw-bold text-dark mt-2">
          {{ formatCurrency(item.donGia) }}
        </div>
      </td>
    </tr>
    
    <tr v-if="filteredProducts.length === 0">
      <td colspan="7" class="text-center py-5 text-muted">
        <i class="bi bi-inbox fs-1 d-block mb-2"></i>
        Không tìm thấy sản phẩm nào phù hợp với bộ lọc.
      </td>
    </tr>
  </tbody>
</table>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from "vue";
import axios from "axios";
import NV_Sidebar from "@/components/Shared/NV_Sidebar.vue";

axios.defaults.withCredentials = true;

const products = ref([]);
const categories = ref([]);
const bulkDiscount = ref(0); // Mặc định là 0%

// --- STATE CHO BỘ LỌC ---
const filterKeyword = ref("");
const filterCategory = ref("");
const filterFlashSale = ref("");
const filterActive = ref("");

// Lấy danh mục
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

// Lấy danh sách Sản Phẩm (Gốc)
const fetchProducts = async () => {
  try {
    // Lưu ý: Đổi endpoint này thành endpoint lấy danh sách bảng SanPham của bạn
    const response = await axios.get(
      "http://localhost:8080/api/khuyenmai/sanpham"
    );
    products.value = response.data.map((item) => ({
      ...item,
      // Gán khuyến mãi mới mặc định bằng với khuyến mãi hiện tại để dễ thao tác
      khuyenMaiMoi: item.khuyenMai || 0,
      selected: false,
    }));
  } catch (error) {
    console.error("Lỗi lấy danh sách sản phẩm:", error);
  }
};

// --- LOGIC LỌC SẢN PHẨM ---
// --- LOGIC LỌC SẢN PHẨM ---
const filteredProducts = computed(() => {
  return products.value.filter((item) => {
    // 1. Lọc theo tên
    const matchKeyword = !filterKeyword.value || item.tenSP?.toLowerCase().includes(filterKeyword.value.toLowerCase());

    // 2. Lọc theo trạng thái Flash Sale
    let matchFlashSale = true;
    if (filterFlashSale.value === "DangSale") {
      matchFlashSale = item.khuyenMai > 0;
    } else if (filterFlashSale.value === "KhongSale") {
      matchFlashSale = item.khuyenMai === 0;
    }

    // 3. Lọc theo danh mục
    let matchCategory = true;
    if (filterCategory.value && item.sanPhamDanhMucs) {
      matchCategory = item.sanPhamDanhMucs.some(
        (dm) => dm.danhMuc?.maDM === filterCategory.value || dm.maDM === filterCategory.value
      );
    }

// 4. LỌC THEO TRẠNG THÁI HOẠT ĐỘNG
    let matchActive = true;
    if (filterActive.value !== "") {
      // Mẹo: Lấy giá trị isActive (nếu có) hoặc active (nếu Spring Boot tự đổi tên)
      const status = item.isActive !== undefined ? item.isActive : item.active;
      
      if (filterActive.value === "true") {
        // Chấp nhận cả boolean, số 1, hoặc chuỗi "true"
        matchActive = status === true || status === 1 || status === "true";
      } else if (filterActive.value === "false") {
        // Chấp nhận cả boolean, số 0, hoặc chuỗi "false"
        matchActive = status === false || status === 0 || status === "false";
      }
    }

    return matchKeyword && matchFlashSale && matchCategory && matchActive;
  });
});

const resetFilters = () => {
  filterKeyword.value = "";
  filterCategory.value = "";
  filterFlashSale.value = "";
  filterActive.value = "";
};

// --- LOGIC THAO TÁC HÀNG LOẠT ---
const selectedCount = computed(
  () => filteredProducts.value.filter((p) => p.selected).length
);

const isAllSelected = computed({
  get: () =>
    filteredProducts.value.length > 0 &&
    filteredProducts.value.every((p) => p.selected),
  set: (val) => filteredProducts.value.forEach((p) => (p.selected = val)),
});

const unselectAll = () => {
  products.value.forEach((p) => (p.selected = false));
};

// Validate giá trị KM từ 0 đến 100
const isValidDiscount = (value) => {
  return value !== null && value !== "" && value >= 0 && value <= 100;
};

const applyBulkDiscount = () => {
  if (!isValidDiscount(bulkDiscount.value)) {
    showToast("Phần trăm khuyến mãi phải nằm trong khoảng từ 0 đến 100!","warning");
    return;
  }

  filteredProducts.value.forEach((p) => {
    if (p.selected) p.khuyenMaiMoi = bulkDiscount.value;
  });
};

const handleBulkSave = async () => {
  const selectedItems = products.value.filter((p) => p.selected);

  // Check validate trước khi gửi
  const isAllValid = selectedItems.every((p) =>
    isValidDiscount(p.khuyenMaiMoi)
  );
  if (!isAllValid) {
    showToast("Có sản phẩm chứa giá trị khuyến mãi không hợp lệ (Phải từ 0-100).","warning");
    return;
  }

  const payload = selectedItems.map((item) => ({
    maSP: item.maSP,
    khuyenMai: item.khuyenMaiMoi,
  }));

  try {
    // Lưu ý: Sửa endpoint POST này cho khớp với backend của bạn
    await axios.post(
      "http://localhost:8080/api/khuyenmai/cap-nhat-hang-loat",
      payload
    );
    showToast(
      `Đã cập nhật khuyến mãi thành công cho ${selectedItems.length} sản phẩm!`
    );
    unselectAll();
    await fetchProducts();
  } catch (error) {
    showToast("Có lỗi xảy ra khi cập nhật khuyến mãi!","danger");
    console.error(error);
  }
};

const handleSingleSave = async (item) => {
  if (!isValidDiscount(item.khuyenMaiMoi)) {
    showToast("Phần trăm khuyến mãi phải nằm trong khoảng từ 0 đến 100!","warning");
    return;
  }

  try {
    // Lưu ý: Sửa endpoint POST này cho khớp với backend của bạn
    await axios.post("http://localhost:8080/api/khuyenmai/cap-nhat", {
      maSP: item.maSP,
      khuyenMai: item.khuyenMaiMoi,
    });
    showToast("Cập nhật khuyến mãi thành công!");
    await fetchProducts();
  } catch (error) {
    showToast("Có lỗi xảy ra!","danger");
    console.error(error);
  }
};

const formatCurrency = (value) => {
  if (!value) return "0 ₫";
  return new Intl.NumberFormat("vi-VN", {
    style: "currency",
    currency: "VND",
  }).format(value);
};
onMounted(() => {
  fetchProducts();
  fetchCategories();
});

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
</script>

<style scoped>
.main-content {
  margin-left: 260px;
  min-height: 100vh;
  background: #f8f9fa;
}
.page-container {
  padding: 30px;
}
.input-group .form-control-lg {
  font-size: 16px;
}
td .input-group {
  min-width: 150px;
}
td .form-control {
  height: 38px;
  padding: 8px 12px;
  font-size: 15px;
  font-weight: bold;
}
td .btn-outline-danger {
  padding: 8px 16px;
  height: 38px;
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