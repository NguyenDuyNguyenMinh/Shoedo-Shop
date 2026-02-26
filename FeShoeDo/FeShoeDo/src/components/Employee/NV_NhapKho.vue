<template>
  <div class="employee-layout">
    <NV_Sidebar />
    
    <main class="main-content">
      <div class="page-container">

        <ul class="nav nav-tabs" role="tablist">
          <li class="nav-item" role="presentation">
            <a class="nav-link" :class="{ active: activeTab === 'import' }" 
               @click="activeTab = 'import'" style="cursor: pointer;">
              <i class="bi bi-box-seam me-2"></i>Nhập kho
            </a>
          </li>
          <li class="nav-item" role="presentation">
            <a class="nav-link" :class="{ active: activeTab === 'history' }" 
               @click="activeTab = 'history'" style="cursor: pointer;">
              <i class="bi bi-clock-history me-2"></i>Lịch sử nhập kho
            </a>
          </li>
        </ul>

        <div class="tab-content p-3 border border-top-0">
          <div v-show="activeTab === 'import'" class="tab-pane show active">
            <div class="row g-2 mb-3">
              <div class="col-md-3">
                <input type="text" placeholder="Tìm theo tên sản phẩm..." class="form-control">
              </div>
              <div class="col-md-2">
                <select class="form-select">
                  <option value="">Tất cả danh mục</option>
                  <option value="1">Giày sneaker</option>
                  <option value="2">Giày thể thao</option>
                  <option value="3">Giày công sở</option>
                </select>
              </div>
              <div class="col-md-2">
                <select class="form-select">
                  <option value="">Tất cả trạng thái</option>
                  <option value="Còn hàng">Còn hàng</option>
                  <option value="Sắp hết">Sắp hết</option>
                  <option value="Hết hàng">Hết hàng</option>
                </select>
              </div>
              <div class="col-md-2">
                <button class="btn btn-secondary">
                  <i class="bi bi-arrow-clockwise me-2"></i>Reset
                </button>
              </div>
            </div>

            <div class="row g-2 mb-3 align-items-center">
              <div class="col-md-4">
                <div class="input-group">
                  <span class="input-group-text bg-light fw-bold">Số lượng chung</span>
                  <input type="number" min="1" class="form-control form-control-lg" placeholder="Nhập số lượng" value="10">
                  <button class="btn btn-success">
                    <i class="bi bi-check-lg me-2"></i>Áp dụng
                  </button>
                </div>
              </div>
              
              <div class="col-md-8">
                <div class="d-flex justify-content-end align-items-center gap-2">
                  <span class="text-muted me-3">
                    Đã chọn: <strong class="text-primary">2</strong> / 120 sản phẩm
                  </span>
                  <button class="btn btn-outline-secondary">
                    <i class="bi bi-x-circle me-2"></i>Bỏ chọn tất cả
                  </button>
                  <button class="btn btn-success">
                    <i class="bi bi-box-arrow-in-down me-2"></i>
                    Nhập 2 sản phẩm
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
                        <input class="form-check-input" type="checkbox">
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
                  <tr>
                    <td>
                      <div class="form-check">
                        <input class="form-check-input" type="checkbox" checked>
                      </div>
                    </td>
                    <td style="width: 72px;">
                      <img src="https://placehold.co/80x80" alt="Product" style="width: 48px; height: 48px; object-fit: cover; border-radius: 8px;">
                    </td>
                    <td>
                      <strong>Sneaker Classic 01</strong>
                      <div class="text-muted small">Mã SP: SP001</div>
                    </td>
                    <td>
                      <span class="badge bg-info text-dark">Trắng - Size 42</span>
                    </td>
                    <td class="text-end">1.290.000 ₫</td>
                    <td class="text-end">
                      <span class="text-success fw-bold">120</span>
                    </td>
                    <td>
                      <span class="badge bg-success">Còn hàng</span>
                    </td>
                    <td>
                      <div class="d-flex align-items-center gap-2">
                        <div class="input-group" style="min-width: 150px;">
                          <input type="number" class="form-control" min="1" placeholder="Số lượng" value="50" style="padding: 10px; font-size: 14px;">
                          <button class="btn btn-outline-success">
                            <i class="bi bi-arrow-down-circle me-1"></i>Nhập
                          </button>
                        </div>
                      </div>
                    </td>
                  </tr>

                  <tr class="table-active">
                    <td>
                      <div class="form-check">
                        <input class="form-check-input" type="checkbox" checked>
                      </div>
                    </td>
                    <td style="width: 72px;">
                      <img src="https://placehold.co/80x80" alt="Product" style="width: 48px; height: 48px; object-fit: cover; border-radius: 8px;">
                    </td>
                    <td>
                      <strong>Runner Pro 2.0</strong>
                      <div class="text-muted small">Mã SP: SP002</div>
                    </td>
                    <td>
                      <span class="badge bg-info text-dark">Đen - Size 41</span>
                    </td>
                    <td class="text-end">1.990.000 ₫</td>
                    <td class="text-end">
                      <span class="text-warning fw-bold">5</span>
                    </td>
                    <td>
                      <span class="badge bg-warning text-dark">Sắp hết</span>
                    </td>
                    <td>
                      <div class="d-flex align-items-center gap-2">
                        <div class="input-group" style="min-width: 150px;">
                          <input type="number" class="form-control" min="1" placeholder="Số lượng" value="10" style="padding: 10px; font-size: 14px;">
                          <button class="btn btn-outline-success">
                            <i class="bi bi-arrow-down-circle me-1"></i>Nhập
                          </button>
                        </div>
                      </div>
                    </td>
                  </tr>

                  <tr>
                    <td>
                      <div class="form-check">
                        <input class="form-check-input" type="checkbox">
                      </div>
                    </td>
                    <td style="width: 72px;">
                      <img src="https://placehold.co/80x80" alt="Product" style="width: 48px; height: 48px; object-fit: cover; border-radius: 8px;">
                    </td>
                    <td>
                      <strong>Office Leather 88</strong>
                      <div class="text-muted small">Mã SP: SP003</div>
                    </td>
                    <td>
                      <span class="badge bg-info text-dark">Nâu - Size 40</span>
                    </td>
                    <td class="text-end">2.490.000 ₫</td>
                    <td class="text-end">
                      <span class="text-danger fw-bold">0</span>
                    </td>
                    <td>
                      <span class="badge bg-danger">Hết hàng</span>
                    </td>
                    <td>
                      <div class="d-flex align-items-center gap-2">
                        <div class="input-group" style="min-width: 150px;">
                          <input type="number" class="form-control" min="1" placeholder="Số lượng" style="padding: 10px; font-size: 14px;">
                          <button class="btn btn-outline-success">
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

          <div v-show="activeTab === 'history'" class="tab-pane show active">
            <div class="row g-2 mb-3">
              <div class="col-md-6">
                <div class="input-group">
                  <span class="input-group-text">
                    <i class="bi bi-search"></i>
                  </span>
                  <input type="text" placeholder="Tìm theo tên sản phẩm..." class="form-control">
                </div>
              </div>
              <div class="col-md-2">
                <button class="btn btn-secondary w-100">
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
                  <tr>
                    <td class="text-center fw-bold text-primary">NK00124</td>
                    <td>
                      <strong>Sneaker Classic 01</strong>
                      <div class="text-muted small">Mã SP: SP001</div>
                    </td>
                    <td>
                      <span class="badge bg-secondary">Giày sneaker</span>
                    </td>
                    <td>
                      <span class="badge bg-info text-dark">Trắng - Size 42</span>
                    </td>
                    <td>
                      <span class="badge bg-success">+50</span>
                    </td>
                    <td>24/02/2026 08:30</td>
                  </tr>

                  <tr>
                    <td class="text-center fw-bold text-primary">NK00123</td>
                    <td>
                      <strong>Runner Pro 2.0</strong>
                      <div class="text-muted small">Mã SP: SP002</div>
                    </td>
                    <td>
                      <span class="badge bg-secondary">Giày thể thao</span>
                    </td>
                    <td>
                      <span class="badge bg-info text-dark">Đen - Size 41</span>
                    </td>
                    <td>
                      <span class="badge bg-success">+100</span>
                    </td>
                    <td>20/02/2026 14:15</td>
                  </tr>
                  
                  <tr>
                    <td class="text-center fw-bold text-primary">NK00122</td>
                    <td>
                      <strong>Office Leather 88</strong>
                      <div class="text-muted small">Mã SP: SP003</div>
                    </td>
                    <td>
                      <span class="badge bg-secondary">Giày công sở</span>
                    </td>
                    <td>
                      <span class="badge bg-info text-dark">Nâu - Size 40</span>
                    </td>
                    <td>
                      <span class="badge bg-success">+20</span>
                    </td>
                    <td>15/02/2026 09:00</td>
                  </tr>
                </tbody>
              </table>
            </div>

            <nav aria-label="Page navigation">
              <ul class="pagination justify-content-center">
                <li class="page-item disabled"><button class="page-link">Trước</button></li>
                <li class="page-item active"><button class="page-link">1</button></li>
                <li class="page-item"><button class="page-link">2</button></li>
                <li class="page-item"><button class="page-link">3</button></li>
                <li class="page-item"><button class="page-link">Sau</button></li>
              </ul>
            </nav>
            <div class="text-muted text-center mt-2">
              Trang 1 / 3 • Tổng 24 bản ghi
            </div>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import NV_Sidebar from '@/components/Shared/NV_Sidebar.vue';

// Chỉ giữ lại đúng biến này để bấm chuyển Tab cho giao diện có tương tác nhẹ
const activeTab = ref('import');
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
</style>