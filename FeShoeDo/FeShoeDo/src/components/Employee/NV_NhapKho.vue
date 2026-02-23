<template>
  <div class="employee-layout">
    <NV_Sidebar />
    
    <main class="main-content">
      <div class="page-container">
        <!-- Thông báo -->
        <div v-if="message" class="alert alert-success alert-dismissible fade show" role="alert">
          <span>{{ message }}</span>
          <button type="button" class="btn-close" @click="message = ''"></button>
        </div>

        <div v-if="error" class="alert alert-danger alert-dismissible fade show" role="alert">
          <span>{{ error }}</span>
          <button type="button" class="btn-close" @click="error = ''"></button>
        </div>

          <!-- Tabs -->
          <ul class="nav nav-tabs" role="tablist">
            <li class="nav-item" role="presentation">
              <a class="nav-link" :class="{ active: activeTab === 'import' }" 
                 @click="setActiveTab('import')">
                <i class="bi bi-box-seam me-2"></i>Nhập kho
              </a>
            </li>
            <li class="nav-item" role="presentation">
              <a class="nav-link" :class="{ active: activeTab === 'history' }" 
                 @click="setActiveTab('history'); fetchImportHistory()">
                <i class="bi bi-clock-history me-2"></i>Lịch sử nhập kho
              </a>
            </li>
          </ul>

          <div class="tab-content p-3 border border-top-0">
            <!-- Tab Nhập kho -->
            <div v-show="activeTab === 'import'" class="tab-pane show active">
              <!-- Filters -->
              <div class="row g-2 mb-3">
                <div class="col-md-3">
                  <input type="text" v-model="filters.keyword" placeholder="Tìm theo tên sản phẩm..." 
                         class="form-control" @input="fetchPLSanPhams">
                </div>
                <div class="col-md-2">
                  <select class="form-select" v-model="filters.danhMuc" @change="fetchPLSanPhams">
                    <option value="">Tất cả danh mục</option>
                    <option v-for="dm in danhMucList" :key="dm.maDM" :value="dm.maDM">
                      {{ dm.tenDM }}
                    </option>
                  </select>
                </div>
                <div class="col-md-2">
                  <select class="form-select" v-model="filters.trangThai" @change="fetchPLSanPhams">
                    <option value="">Tất cả trạng thái</option>
                    <option value="Còn hàng">Còn hàng</option>
                    <option value="Hết hàng">Hết hàng</option>
                  </select>
                </div>
                <div class="col-md-2">
                  <button class="btn btn-secondary" @click="resetFilters">
                    <i class="bi bi-arrow-clockwise me-2"></i>Reset
                  </button>
                </div>
              </div>

              <!-- Bulk quantity input section -->
              <div class="row g-2 mb-3 align-items-center">
                <div class="col-md-4">
                  <div class="input-group">
                    <span class="input-group-text bg-light fw-bold">Số lượng chung</span>
                    <input type="number" min="1" class="form-control form-control-lg" 
                           v-model.number="bulkQuantity" placeholder="Nhập số lượng">
                    <button class="btn btn-success" @click="applyBulkQuantity" 
                            :disabled="!bulkQuantity || bulkQuantity <= 0 || selectedProducts.length === 0">
                      <i class="bi bi-check-lg me-2"></i>Áp dụng
                    </button>
                  </div>
                </div>
                
                <!-- Action buttons -->
                <div class="col-md-8">
                  <div class="d-flex justify-content-end align-items-center gap-2">
                    <span class="text-muted me-3">
                      Đã chọn: <strong class="text-primary">{{ selectedProducts.length }}</strong> / {{ phanLoaiSanPhams.length }} sản phẩm
                    </span>
                    <button class="btn btn-outline-secondary" @click="resetBulkSelection" 
                            :disabled="selectedProducts.length === 0">
                      <i class="bi bi-x-circle me-2"></i>Bỏ chọn tất cả
                    </button>
                    <button class="btn btn-success" @click="bulkImport" 
                            :disabled="selectedProducts.length === 0 || bulkImporting">
                      <span v-if="bulkImporting" class="spinner-border spinner-border-sm me-2"></span>
                      <i v-else class="bi bi-box-arrow-in-down me-2"></i>
                      Nhập {{ selectedProducts.length }} sản phẩm
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
                            <input class="form-check-input" type="checkbox" 
                                   v-model="selectAll" @change="toggleSelectAll">
                          </div>
                        </th>
                        <th>Hình</th>
                        <th>Tên sản phẩm</th>
                        <th>Phân loại</th>
                        <th>Giá</th>
                        <th>SL tồn</th>
                        <th>Trạng thái</th>
                        <th style="width: 180px;">Số lượng nhập</th>
                      </tr>
                    </thead>
                    <tbody>
                      <tr v-for="plsp in phanLoaiSanPhams" :key="plsp.maPLSP" 
                          :class="{ 'table-active': selectedProducts.includes(plsp.maPLSP) }">
                        <td>
                          <div class="form-check">
                            <input class="form-check-input" type="checkbox" 
                                   :value="plsp.maPLSP" v-model="selectedProducts">
                          </div>
                        </td>
                        <td style="width: 72px;">
                          <img :src="getImageUrl(plsp.hinh)" alt=""
                               style="width: 48px; height: 48px; object-fit: cover; border-radius: 8px;"
                               @error="handleImageError">
                        </td>
                        <td>
                          <strong>{{ plsp.sanPham?.tenSP || 'Không có tên' }}</strong>
                          <div class="text-muted small">
                            Mã SP: {{ plsp.sanPham?.maSP || 'N/A' }}
                          </div>
                        </td>
                        <td>
                          <span class="badge bg-info">{{ plsp.phanLoai || 'Mặc định' }}</span>
                        </td>
                        <td class="text-end">
                          {{ formatPrice(plsp.donGia) }}
                        </td>
                        <td text-end>
                          <span :class="getStockClass(plsp.soLuong)">
                            {{ plsp.soLuong || 0 }}
                          </span>
                        </td>
                        <td>
                          <span :class="getStatusClass(plsp.trangThai)">
                            {{ plsp.trangThai || 'Không xác định' }}
                          </span>
                        </td>
                        <td>
                          <div class="d-flex align-items-center gap-2">
                            <div class="input-group" style="min-width: 150px;">
                              <input type="number" v-model.number="importQuantities[plsp.maPLSP]" 
                                     class="form-control" min="1" placeholder="Nhập số lượng"
                                     style="padding: 10px; font-size: 14px;">
                              <button class="btn btn-outline-success" 
                                      @click="singleImport(plsp)"
                                      :disabled="importing">
                                <i class="bi bi-arrow-down-circle me-1"></i>Nhập
                              </button>
                            </div>
                          </div>
                        </td>
                      </tr>
                    </tbody>
                  </table>
                </div>
            </div>

            <!-- Tab Lịch sử nhập kho -->
            <div v-show="activeTab === 'history'" class="tab-pane show active">
              <!-- History Filters -->
              <div class="row g-2 mb-3">
                <div class="col-md-6">
                  <div class="input-group">
                    <span class="input-group-text">
                      <i class="bi bi-search"></i>
                    </span>
                    <input type="text" v-model="historyFilters.keyword" 
                         placeholder="Tìm theo tên sản phẩm..." 
                         class="form-control" @input="fetchImportHistory">
                  </div>
                </div>
                <div class="col-md-2">
                  <button class="btn btn-secondary w-100" @click="resetHistoryFilters">
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
                      <tr v-for="nk in importHistory" :key="nk.maNK">
                        <td class="text-center">{{ nk.maNK }}</td>
                        <td>
                          <strong>{{ nk.plSanPham?.sanPham?.tenSP || '' }}</strong>
                          <div class="text-muted small">
                            Mã SP: {{ nk.plSanPham?.sanPham?.maSP || 'N/A' }}
                          </div>
                        </td>
                        <td>
                          <span class="badge bg-secondary">
                            {{ nk.plSanPham?.sanPham?.danhMuc?.tenDM || 'Không có DM' }}
                          </span>
                        </td>
                        <td>
                          <span class="badge bg-info">
                            {{ nk.plSanPham?.phanLoai || '' }}
                          </span>
                        </td>
                        <td>
                          <span class="badge bg-success">+{{ nk.soLuong }}</span>
                        </td>
                        <td>{{ formatDateTimeDDMMYYYY(nk.ngayNK) }}</td>
                      </tr>
                    </tbody>
                  </table>
                </div>

                <!-- Pagination -->
                <nav v-if="totalPages > 1" aria-label="Page navigation">
                  <ul class="pagination justify-content-center">
                    <li class="page-item" :class="{ disabled: currentPage === 1 }">
                      <button class="page-link" @click="changePage(currentPage - 1)">Trước</button>
                    </li>
                    
                    <li v-for="page in visiblePages" :key="page" 
                        class="page-item" :class="{ active: page === currentPage }">
                      <button class="page-link" @click="changePage(page)">{{ page }}</button>
                    </li>
                    
                    <li class="page-item" :class="{ disabled: currentPage === totalPages }">
                      <button class="page-link" @click="changePage(currentPage + 1)">Sau</button>
                    </li>
                  </ul>
                </nav>

                <div class="text-muted text-center mt-2">
                  Trang {{ currentPage }} / {{ totalPages }} • Tổng {{ totalRecords }} bản ghi
                </div>
              </div>
            </div>
          </div>
    </main>
  </div>
</template>

<script>
import { ref, onMounted, computed, watch } from 'vue';
import api from '@/services/api';
import NV_Sidebar from '@/components/shared/NV_Sidebar.vue';

export default {
  name: 'NhapKho',
  components: {
    NV_Sidebar
  },
  setup() {
    const activeTab = ref('import');
    const phanLoaiSanPhams = ref([]);
    const danhMucList = ref([]);
    const importHistory = ref([]);
    const loadingProducts = ref(false);
    const loadingHistory = ref(false);
    const importing = ref(false);
    const bulkImporting = ref(false);
    const message = ref('');
    const error = ref('');
    const errorProducts = ref('');

    const selectedProducts = ref([]);
    const selectAll = ref(false);
    const importQuantities = ref({});
    const bulkQuantity = ref(10);

    const filters = ref({
      keyword: '',
      danhMuc: '',
      trangThai: ''
    });

    const historyFilters = ref({
      startDate: '',
      endDate: '',
      keyword: '',
      pageSize: 50
    });

    const currentPage = ref(1);
    const totalPages = ref(1);
    const totalRecords = ref(0);

    const getImageUrl = (imageName) => {
      return `http://localhost:8080/images/${imageName || 'default.jpg'}`;
    };

    const handleImageError = (event) => {
      event.target.src = 'http://localhost:8080/images/no-image.png';
    };

    const formatPrice = (price) => {
      if (!price) return '0 ₫';
      return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND'
      }).format(price);
    };

    const formatDateTime = (dateString) => {
      if (!dateString) return '';
      const date = new Date(dateString);
      return date.toLocaleDateString('vi-VN') + ' ' + 
             date.toLocaleTimeString('vi-VN', {
               hour: '2-digit',
               minute: '2-digit'
             });
    };

    const formatDateTimeDDMMYYYY = (dateString) => {
      if (!dateString) return '';
      const date = new Date(dateString);
      const day = date.getDate().toString().padStart(2, '0');
      const month = (date.getMonth() + 1).toString().padStart(2, '0');
      const year = date.getFullYear();
      return `${day}/${month}/${year}`;
    };

    const getStockClass = (quantity) => {
      if (!quantity || quantity <= 0) return 'text-danger fw-bold';
      if (quantity < 10) return 'text-warning fw-bold';
      return 'text-success';
    };

    const getStatusClass = (status) => {
      const statusMap = {
        'Còn hàng': 'badge bg-success',
        'Hết hàng': 'badge bg-danger',
        'Ngừng kinh doanh': 'badge bg-secondary'
      };
      return statusMap[status] || 'badge bg-secondary';
    };

    const fetchPLSanPhams = async () => {
      loadingProducts.value = true;
      errorProducts.value = '';
      
      try {
        const response = await api.getPLSanPhams();
        if (response.data.success) {
          let data = response.data.plSanPhams || [];

          if (filters.value.keyword) {
            const keyword = filters.value.keyword.toLowerCase();
            data = data.filter(plsp => 
              plsp.sanPham?.tenSP?.toLowerCase().includes(keyword) ||
              plsp.phanLoai?.toLowerCase().includes(keyword) ||
              plsp.sanPham?.danhMuc?.tenDM?.toLowerCase().includes(keyword)
            );
          }
          
          if (filters.value.danhMuc) {
            data = data.filter(plsp => 
              plsp.sanPham?.danhMuc?.maDM?.toString() === filters.value.danhMuc.toString()
            );
          }
          
          if (filters.value.trangThai) {
            data = data.filter(plsp => plsp.trangThai === filters.value.trangThai);
          }
          
          phanLoaiSanPhams.value = data;

          phanLoaiSanPhams.value.forEach(plsp => {
            if (!importQuantities.value[plsp.maPLSP]) {
              importQuantities.value[plsp.maPLSP] = 1;
            }
          });
        }
      } catch (err) {
        console.error('Error fetching PLSanPhams:', err);
        errorProducts.value = 'Không thể tải danh sách sản phẩm';
      } finally {
        loadingProducts.value = false;
      }
    };

    const fetchDanhMucList = async () => {
      try {
        const response = await api.getCategories();
        
        if (response.data && response.data.success) {
          danhMucList.value = response.data.data || [];
        } else {
          extractCategoriesFromProducts();
        }
      } catch (error) {
        console.error('Error fetching danh muc list:', error);
        extractCategoriesFromProducts();
      }
    };

    const extractCategoriesFromProducts = () => {
      const uniqueCategories = new Map();
      phanLoaiSanPhams.value.forEach(plsp => {
        const dm = plsp.sanPham?.danhMuc;
        if (dm && dm.maDM) {
          uniqueCategories.set(dm.maDM, {
            maDM: dm.maDM,
            tenDM: dm.tenDM || `Danh mục ${dm.maDM}`
          });
        }
      });
      danhMucList.value = Array.from(uniqueCategories.values());
    };

    const fetchImportHistory = async () => {
      loadingHistory.value = true;
      
      try {
        const params = {
          page: currentPage.value,
          size: historyFilters.value.pageSize
        };
        

        if (historyFilters.value.keyword && historyFilters.value.keyword.trim()) {
          return await searchImportHistoryByKeyword(params);
        } else {
          return await getRegularImportHistory(params);
        }
      } catch (err) {
        console.error('Error fetching import history:', err);
        error.value = 'Không thể tải lịch sử nhập kho';
        loadingHistory.value = false;
      }
    };

    const searchImportHistoryByKeyword = async (params) => {
      try {
        const response = await api.searchImportHistory({
          ...params,
          keyword: historyFilters.value.keyword.trim()
        });
        
        if (response.data.success) {
          importHistory.value = response.data.importHistory || [];
          totalRecords.value = response.data.totalRecords || 0;
          totalPages.value = response.data.totalPages || 1;
        }
      } catch (err) {
        console.error('Error searching import history via API:', err);
        await fallbackSearch();
      } finally {
        loadingHistory.value = false;
      }
    };

    const fallbackSearch = async () => {
      const allHistory = await getAllImportHistory();
      const keyword = historyFilters.value.keyword.trim().toLowerCase();
      
      const filteredHistory = allHistory.filter(nk => {
        const productName = nk.plSanPham?.sanPham?.tenSP || '';
        return productName.toLowerCase().includes(keyword);
      });
      
      const startIndex = (currentPage.value - 1) * historyFilters.value.pageSize;
      const endIndex = startIndex + historyFilters.value.pageSize;
      
      importHistory.value = filteredHistory.slice(startIndex, endIndex);
      totalRecords.value = filteredHistory.length;
      totalPages.value = Math.ceil(filteredHistory.length / historyFilters.value.pageSize);
    };

    const getRegularImportHistory = async (params) => {
      try {
        const response = await api.getImportHistory(params);
        if (response.data.success) {
          importHistory.value = response.data.importHistory || [];
          totalRecords.value = response.data.totalRecords || 0;
          totalPages.value = response.data.totalPages || 1;
        }
      } catch (err) {
        console.error('Error getting regular import history:', err);
        throw err;
      } finally {
        loadingHistory.value = false;
      }
    };

    const getAllImportHistory = async () => {
      try {
        const response = await api.getImportHistory({ 
          page: 1, 
          size: 1000
        });
        return response.data.success ? response.data.importHistory || [] : [];
      } catch (err) {
        console.error('Error getting all import history:', err);
        return [];
      }
    };

    const formatDateForInput = (date) => {
      if (!date) return '';
      const d = new Date(date);
      const year = d.getFullYear();
      const month = String(d.getMonth() + 1).padStart(2, '0');
      const day = String(d.getDate()).padStart(2, '0');
      return `${year}-${month}-${day}`;
    };

    const toggleSelectAll = () => {
      if (selectAll.value) {
        selectedProducts.value = phanLoaiSanPhams.value.map(plsp => plsp.maPLSP);
      } else {
        selectedProducts.value = [];
      }
    };

    const applyBulkQuantity = () => {
      if (!bulkQuantity.value || bulkQuantity.value <= 0) {
        error.value = 'Vui lòng nhập số lượng hợp lệ';
        return;
      }
      
      selectedProducts.value.forEach(maPLSP => {
        importQuantities.value[maPLSP] = bulkQuantity.value;
      });
      
      message.value = `Đã áp dụng số lượng ${bulkQuantity.value} cho ${selectedProducts.value.length} sản phẩm`;
      setTimeout(() => {
        message.value = '';
      }, 3000);
    };

    const resetBulkSelection = () => {
      selectedProducts.value = [];
      selectAll.value = false;
      bulkQuantity.value = 10;
    };

    const singleImport = async (plsp) => {
      const quantity = importQuantities.value[plsp.maPLSP];
      
      if (!quantity || quantity <= 0) {
        error.value = 'Vui lòng nhập số lượng hợp lệ';
        return;
      }

      importing.value = true;
      error.value = '';
      message.value = '';

      try {
        const response = await api.importSingleProduct({
          maPLSP: plsp.maPLSP,
          soLuong: quantity
        });
        
        if (response.data.success) {
          message.value = response.data.message || 'Nhập kho thành công!';

          await fetchPLSanPhams();
        } else {
          error.value = response.data.message || 'Nhập kho thất bại';
        }
      } catch (err) {
        console.error('Error single importing:', err);
        error.value = 'Lỗi kết nối máy chủ';
      } finally {
        importing.value = false;
      }
    };

    const bulkImport = async () => {
      if (selectedProducts.value.length === 0) {
        error.value = 'Vui lòng chọn ít nhất một sản phẩm';
        return;
      }

      const importItems = [];
      let hasError = false;

      selectedProducts.value.forEach(maPLSP => {
        const quantity = importQuantities.value[maPLSP];
        
        if (!quantity || quantity <= 0) {
          error.value = `Sản phẩm mã ${maPLSP} có số lượng không hợp lệ`;
          hasError = true;
          return;
        }

        importItems.push({
          maPLSP: maPLSP,
          soLuong: quantity
        });
      });

      if (hasError) return;
      if (importItems.length === 0) return;

      bulkImporting.value = true;
      error.value = '';
      message.value = '';

      try {
        const response = await api.bulkImportProducts(importItems);
        
        if (response.data.success) {
          message.value = response.data.message || `Đã nhập kho thành công ${importItems.length} sản phẩm`;
          
          await fetchPLSanPhams();
          resetBulkSelection();

          setTimeout(() => {
            activeTab.value = 'history';
            fetchImportHistory();
          }, 2000);
        } else {
          error.value = response.data.message || 'Nhập kho thất bại';
        }
      } catch (err) {
        console.error('Error bulk importing:', err);
        error.value = 'Lỗi kết nối máy chủ';
      } finally {
        bulkImporting.value = false;
      }
    };

    const changePage = (page) => {
      if (page < 1 || page > totalPages.value) return;
      currentPage.value = page;
      fetchImportHistory();
    };

    const setActiveTab = (tab) => {
      activeTab.value = tab;
      if (tab === 'import') {
        resetBulkSelection();
      }
    };

    const resetFilters = () => {
      filters.value = {
        keyword: '',
        danhMuc: '',
        trangThai: ''
      };
      fetchPLSanPhams();
    };

    const resetHistoryFilters = () => {
      historyFilters.value = {
        keyword: '',
        pageSize: 50
      };
      currentPage.value = 1;
      fetchImportHistory();
    };

    const visiblePages = computed(() => {
      const pages = [];
      const maxVisible = 5;
      let start = Math.max(1, currentPage.value - Math.floor(maxVisible / 2));
      let end = Math.min(totalPages.value, start + maxVisible - 1);
      
      if (end - start + 1 < maxVisible) {
        start = Math.max(1, end - maxVisible + 1);
      }
      
      for (let i = start; i <= end; i++) {
        pages.push(i);
      }
      return pages;
    });

    watch(selectedProducts, (newVal) => {
      selectAll.value = newVal.length === phanLoaiSanPhams.value.length && phanLoaiSanPhams.value.length > 0;
    });

    onMounted(() => {
      fetchPLSanPhams();
      fetchDanhMucList();
    });

    return {
      activeTab,
      phanLoaiSanPhams,
      danhMucList,
      importHistory,
      loadingProducts,
      loadingHistory,
      importing,
      bulkImporting,
      message,
      error,
      errorProducts,
      selectedProducts,
      selectAll,
      importQuantities,
      bulkQuantity,
      filters,
      historyFilters,
      currentPage,
      totalPages,
      totalRecords,
      visiblePages,

      getImageUrl,
      handleImageError,
      formatPrice,
      formatDateTime,
      formatDateTimeDDMMYYYY,
      getStockClass,
      getStatusClass,
      setActiveTab,
      toggleSelectAll,
      applyBulkQuantity,
      resetBulkSelection,
      singleImport,
      bulkImport,
      fetchPLSanPhams,
      fetchImportHistory,
      changePage,
      resetFilters,
      resetHistoryFilters
    };
  }
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

/* Style cho input group trong history tab */
.input-group-text {
  background-color: #f8f9fa;
  border-color: #dee2e6;
  font-size: 14px;
}
</style>