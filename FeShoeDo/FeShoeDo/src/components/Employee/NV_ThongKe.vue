<template>
  <div class="employee-layout">
    <NV_Sidebar />

    <main class="main-content">
      <div class="page-container">

        <!-- Header -->
        <div class="d-flex justify-content-between align-items-center mb-4">
          <div>
            <h4 class="page-title mb-1"><i class="bi bi-speedometer2 me-2"></i>Thống kê tổng quan</h4>
            <p class="page-subtitle">Tổng hợp dữ liệu kinh doanh Shoedo Shop</p>
          </div>
          <!-- Date Filter -->
          <div class="d-flex gap-2 align-items-center">
            <div class="btn-group time-filter" role="group">
              <button type="button" 
                      class="btn btn-sm" 
                      :class="selectedFilter === '1' ? 'btn-dark' : 'btn-outline-dark'"
                      @click="setFilter('1')">Hôm nay</button>
              <button type="button" 
                      class="btn btn-sm" 
                      :class="selectedFilter === '7' ? 'btn-dark' : 'btn-outline-dark'"
                      @click="setFilter('7')">7 ngày</button>
              <button type="button" 
                      class="btn btn-sm" 
                      :class="selectedFilter === '30' ? 'btn-dark' : 'btn-outline-dark'"
                      @click="setFilter('30')">30 ngày</button>
              <button type="button" 
                      class="btn btn-sm" 
                      :class="selectedFilter === '90' ? 'btn-dark' : 'btn-outline-dark'"
                      @click="setFilter('90')">90 ngày</button>
            </div>
          </div>
        </div>

        <!-- Loading State -->
        <div v-if="loading" class="text-center py-5">
          <div class="spinner-border text-dark" role="status">
            <span class="visually-hidden">Loading...</span>
          </div>
          <p class="mt-2 text-muted">Đang tải dữ liệu thống kê...</p>
        </div>

        <!-- Error State -->
        <div v-else-if="error" class="alert alert-danger" role="alert">
          <i class="bi bi-exclamation-triangle me-2"></i>{{ error }}
        </div>

        <!-- Stats Cards -->
        <div v-else class="row g-3 mb-4">
          <div class="col-md-3">
            <div class="stats-card">
              <div class="stats-card-icon bg-gradient-revenue">
                <i class="bi bi-cash-stack"></i>
              </div>
              <div class="stats-card-content">
                <p class="stats-label">Tổng doanh thu</p>
                <h3 class="stats-value">{{ formatCurrency(thongKeData?.tongQuan?.tongDoanhThu) }}<small class="stats-unit">VND</small></h3>
              </div>
            </div>
          </div>
          <div class="col-md-3">
            <div class="stats-card">
              <div class="stats-card-icon bg-gradient-orders">
                <i class="bi bi-bag-check"></i>
              </div>
              <div class="stats-card-content">
                <p class="stats-label">Đơn hàng</p>
                <h3 class="stats-value">{{ thongKeData?.tongQuan?.tongDonHang || 0 }}</h3>
              </div>
            </div>
          </div>
          <div class="col-md-3">
            <div class="stats-card">
              <div class="stats-card-icon bg-gradient-products">
                <i class="bi bi-box-seam"></i>
              </div>
              <div class="stats-card-content">
                <p class="stats-label">Sản phẩm đã bán</p>
                <h3 class="stats-value">{{ thongKeData?.tongQuan?.tongSanPhamBan || 0 }}</h3>
              </div>
            </div>
          </div>
          <div class="col-md-3">
            <div class="stats-card">
              <div class="stats-card-icon bg-gradient-customers">
                <i class="bi bi-people"></i>
              </div>
              <div class="stats-card-content">
                <p class="stats-label">Giá trị đơn TB</p>
                <h3 class="stats-value">{{ formatCurrency(thongKeData?.tongQuan?.giaTriDonTrungBinh) }}<small class="stats-unit">VND</small></h3>
              </div>
            </div>
          </div>
        </div>

        <!-- Charts Row 1: Revenue Line Chart -->
        <div v-if="!loading && !error" class="content-card mb-4">
          <div class="d-flex justify-content-between align-items-center mb-3">
            <div>
              <h6 class="chart-title"><i class="bi bi-graph-up me-2"></i>Biểu đồ doanh thu</h6>
              <p class="chart-subtitle">Doanh thu {{ selectedFilter }} ngày gần nhất (VND)</p>
            </div>
          </div>
          <div class="chart-container">
            <canvas ref="revenueChart"></canvas>
          </div>
        </div>

        <!-- Charts Row 2: Bar + Doughnut -->
        <div v-if="!loading && !error" class="row g-4 mb-4">
          <div class="col-md-7">
            <div class="content-card h-100">
              <h6 class="chart-title"><i class="bi bi-bar-chart me-2"></i>Top 5 sản phẩm bán chạy</h6>
              <p class="chart-subtitle">Số lượng đã bán</p>
              <div class="chart-container">
                <canvas ref="topProductsChart"></canvas>
              </div>
            </div>
          </div>
          <div class="col-md-5">
            <div class="content-card h-100">
              <h6 class="chart-title"><i class="bi bi-pie-chart me-2"></i>Trạng thái đơn hàng</h6>
              <p class="chart-subtitle">Phân bổ theo trạng thái</p>
              <div class="chart-container-doughnut">
                <canvas ref="orderStatusChart"></canvas>
              </div>
            </div>
          </div>
        </div>

        <!-- Tables Row: Recent Orders + Top Customers -->
        <div v-if="!loading && !error" class="row g-4 mb-4">
          <!-- Đơn hàng gần đây -->
          <div class="col-md-7">
            <div class="content-card h-100">
              <div class="d-flex justify-content-between align-items-center mb-3">
                <h6 class="chart-title mb-0"><i class="bi bi-receipt-cutoff me-2"></i>Đơn hàng gần đây</h6>
                <button class="btn btn-outline-dark btn-sm">Xem tất cả</button>
              </div>
              <div class="table-responsive">
                <table class="table table-hover align-middle recent-table">
                  <thead>
                    <tr>
                      <th>Mã HĐ</th>
                      <th>Khách hàng</th>
                      <th>Ngày mua</th>
                      <th>Tổng tiền</th>
                      <th>Trạng thái</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="order in (thongKeData?.donHangGanday || [])" :key="order.maHD">
                      <td class="fw-semibold">#{{ order.maHD }}</td>
                      <td>
                        <div class="d-flex align-items-center gap-2">
                          <div class="avatar-sm bg-customer-sm">{{ getInitials(order.tenKH) }}</div>
                          {{ order.tenKH }}
                        </div>
                      </td>
                      <td>{{ formatDate(order.ngayMua) }}</td>
                      <td class="fw-semibold">{{ formatCurrency(order.tongTien) }}đ</td>
                      <td><span :class="'badge order-badge badge-' + getStatusClass(order.trangThai)">{{ order.trangThai }}</span></td>
                    </tr>
                    <tr v-if="!thongKeData?.donHangGanday?.length">
                      <td colspan="5" class="text-center text-muted py-4">Không có đơn hàng gần đây</td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>
          </div>

          <!-- Top Khách hàng -->
          <div class="col-md-5">
            <div class="content-card h-100">
              <h6 class="chart-title mb-3"><i class="bi bi-trophy me-2"></i>Top khách hàng</h6>
              <div class="top-customer-list">
                <div v-for="(customer, index) in (thongKeData?.topKhachHang || [])" :key="customer.maKH" class="top-customer-item">
                  <div class="d-flex align-items-center gap-3">
                    <div :class="'rank-badge rank-' + (index + 1)">{{ index + 1 }}</div>
                    <div class="avatar-sm bg-customer-sm">{{ getInitials(customer.tenKH) }}</div>
                    <div>
                      <div class="fw-semibold">{{ customer.tenKH }}</div>
                      <div class="text-muted small">{{ customer.soDonMua }} đơn hàng</div>
                    </div>
                  </div>
                  <div class="text-end">
                    <div class="fw-bold text-dark">{{ formatCurrency(customer.tongChiTieu) }}đ</div>
                  </div>
                </div>
                <div v-if="!thongKeData?.topKhachHang?.length" class="text-center text-muted py-4">
                  Chưa có dữ liệu khách hàng
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Category Stats -->
        <div v-if="!loading && !error" class="content-card">
          <h6 class="chart-title mb-3"><i class="bi bi-grid me-2"></i>Thống kê theo danh mục</h6>
          <div class="row g-3">
            <div v-for="category in (thongKeData?.theoDanhMuc || [])" :key="category.tenDanhMuc" class="col-md-2">
              <div class="category-stat-card">
                <div class="category-icon"><i class="bi bi-briefcase"></i></div>
                <h6 class="mb-1">{{ category.tenDanhMuc || 'N/A' }}</h6>
                <div class="d-flex justify-content-between align-items-center">
                  <span class="text-muted small">{{ category.soLoaiSanPham || 0 }} sản phẩm</span>
                  <span class="fw-bold text-dark">{{ category.soSanPham || 0 }} đã bán</span>
                </div>
                <div class="progress mt-2" style="height: 4px;">
                  <div class="progress-bar bg-dark" :style="{ width: Math.min((category.soSanPham || 0) * 5, 100) + '%' }"></div>
                </div>
              </div>
            </div>
            <div v-if="!thongKeData?.theoDanhMuc?.length" class="col-12 text-center text-muted py-4">
              Chưa có dữ liệu danh mục
            </div>
          </div>
        </div>

      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { Chart, registerables } from 'chart.js';
import NV_Sidebar from '@/components/Shared/NV_Sidebar.vue';
import api from '@/services/api';

Chart.register(...registerables);

const revenueChart = ref(null);
const topProductsChart = ref(null);
const orderStatusChart = ref(null);

// Loading state
const loading = ref(true);
const error = ref(null);

// Data states
const thongKeData = ref(null);
const selectedFilter = ref('7'); // Default 7 days

// Computed dates
const dateRange = computed(() => {
  const endDate = new Date();
  const startDate = new Date();
  startDate.setDate(endDate.getDate() - parseInt(selectedFilter.value));
  return {
    startDate: startDate.toISOString().split('T')[0],
    endDate: endDate.toISOString().split('T')[0]
  };
});

// Format currency
const formatCurrency = (value) => {
  if (!value) return '0';
  return new Intl.NumberFormat('vi-VN').format(value);
};

// Format date for display
const formatDate = (dateStr) => {
  if (!dateStr) return '';
  const date = new Date(dateStr);
  return date.toLocaleDateString('vi-VN');
};

// Load data from API
const loadThongKeData = async () => {
  loading.value = true;
  error.value = null;
  
  const endDate = new Date();
  const startDate = new Date();
  startDate.setDate(endDate.getDate() - parseInt(selectedFilter.value));
  
  const startDateStr = startDate.toISOString().split('T')[0];
  const endDateStr = endDate.toISOString().split('T')[0];
  
    try {
    // Fetch all required data in parallel
    const [tongQuan, theoNgay, theoDanhMuc, topSanPham, topKhachHang, donHangGanDay] = await Promise.all([
      api.getThongKeTongQuan(startDateStr, endDateStr),
      api.getThongKeNgay(startDateStr, endDateStr),
      api.getThongKeDanhMuc(startDateStr, endDateStr),
      api.getTopSanPham(startDateStr, endDateStr, 10),
      api.getTopKhachHang(startDateStr, endDateStr, 5),
      api.getDonHangGanDay(5)
    ]);
    
    thongKeData.value = {
      tongQuan: tongQuan.data,
      theoNgay: theoNgay.data,
      theoDanhMuc: theoDanhMuc.data,
      topSanPham: topSanPham.data,
      topKhachHang: topKhachHang.data,
      donHangGanday: donHangGanDay.data
    };
    
    // Initialize charts after data loaded
    setTimeout(() => {
      initCharts();
    }, 100);
  } catch (err) {
    console.error('Error loading statistics:', err);
    error.value = 'Không thể tải dữ liệu thống kê';
  } finally {
    loading.value = false;
  }
};

// Filter button handlers
const setFilter = (days) => {
  selectedFilter.value = days;
  loadThongKeData();
};

// Initialize charts with real data
const initCharts = () => {
  if (!thongKeData.value) return;
  
  const data = thongKeData.value;
  
  // Revenue Chart Data
  const revenueData = data.theoNgay || [];
  const revenueLabels = revenueData.map(item => formatDate(item.thoiGian));
  const revenueValues = revenueData.map(item => item.doanhThu || 0);

  // Revenue Line Chart
  new Chart(revenueChart.value, {
    type: 'line',
    data: {
      labels: revenueLabels.length > 0 ? revenueLabels : ['No data'],
      datasets: [{
        label: 'Doanh thu (VND)',
        data: revenueValues.length > 0 ? revenueValues : [0],
        borderColor: '#212529',
        backgroundColor: 'rgba(33, 37, 41, 0.08)',
        borderWidth: 3,
        pointBackgroundColor: '#212529',
        pointBorderColor: '#fff',
        pointBorderWidth: 2,
        pointRadius: 6,
        pointHoverRadius: 8,
        fill: true,
        tension: 0.4
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: { display: false },
        tooltip: {
          backgroundColor: '#212529',
          titleFont: { size: 13 },
          bodyFont: { size: 12 },
          padding: 12,
          cornerRadius: 8,
          callbacks: {
            label: (context) => {
              return new Intl.NumberFormat('vi-VN').format(context.parsed.y) + ' VND';
            }
          }
        }
      },
      scales: {
        y: {
          beginAtZero: true,
          ticks: {
            callback: (value) => new Intl.NumberFormat('vi-VN', { notation: 'compact' }).format(value),
            font: { size: 12 },
            color: '#6c757d'
          },
          grid: { color: 'rgba(0,0,0,0.05)' }
        },
        x: {
          ticks: { font: { size: 12 }, color: '#6c757d' },
          grid: { display: false }
        }
      }
    }
  });

  // Top Products Bar Chart
  const topProducts = data.topSanPham || [];
  new Chart(topProductsChart.value, {
    type: 'bar',
    data: {
      labels: topProducts.length > 0 ? topProducts.map(p => p.tenSP || p.maSP) : ['No data'],
      datasets: [{
        label: 'Số lượng đã bán',
        data: topProducts.length > 0 ? topProducts.map(p => p.tongSoLuong || 0) : [0],
        backgroundColor: [
          'rgba(33, 37, 41, 0.85)',
          'rgba(33, 37, 41, 0.70)',
          'rgba(33, 37, 41, 0.55)',
          'rgba(33, 37, 41, 0.40)',
          'rgba(33, 37, 41, 0.28)'
        ],
        borderColor: '#212529',
        borderWidth: 1,
        borderRadius: 8,
        barPercentage: 0.6
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      indexAxis: 'y',
      plugins: {
        legend: { display: false },
        tooltip: {
          backgroundColor: '#212529',
          padding: 10,
          cornerRadius: 8
        }
      },
      scales: {
        x: {
          beginAtZero: true,
          ticks: { font: { size: 12 }, color: '#6c757d' },
          grid: { color: 'rgba(0,0,0,0.05)' }
        },
        y: {
          ticks: { font: { size: 12, weight: '500' }, color: '#333' },
          grid: { display: false }
        }
      }
    }
  });

  // Order Status Doughnut Chart
  const tongQuan = data.tongQuan || {};
  new Chart(orderStatusChart.value, {
    type: 'doughnut',
    data: {
      labels: ['Hoàn tất', 'Đang giao', 'Đang xử lý', 'Đã từ chối'],
      datasets: [{
        data: [
          tongQuan.donHoanTat || 0,
          tongQuan.donDangGiao || 0,
          tongQuan.donDangXuLy || 0,
          tongQuan.donBiTuChoi || 0
        ],
        backgroundColor: [
          '#10b981',
          '#3b82f6',
          '#f59e0b',
          '#ef4444'
        ],
        borderWidth: 3,
        borderColor: '#fff',
        hoverOffset: 8
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      cutout: '65%',
      plugins: {
        legend: {
          position: 'bottom',
          labels: {
            padding: 16,
            usePointStyle: true,
            pointStyle: 'circle',
            font: { size: 12 }
          }
        },
        tooltip: {
          backgroundColor: '#212529',
          padding: 10,
          cornerRadius: 8,
          callbacks: {
            label: (context) => {
              return context.label + ': ' + context.parsed + ' đơn';
            }
          }
        }
      }
    }
  });
};

// Helper function for initials
const getInitials = (name) => {
  if (!name) return '?';
  return name.split(' ').map(n => n[0]).join('').substring(0, 2).toUpperCase();
};

// Helper function for status class
const getStatusClass = (status) => {
  const statusMap = {
    'Hoàn tất': 'completed',
    'Đang giao': 'shipping',
    'Đang xử lý': 'processing',
    'Đã từ chối': 'rejected',
    'Báo lỗi': 'error',
    'Hoàn hàng': 'returned'
  };
  return statusMap[status] || 'processing';
};

onMounted(() => {
  loadThongKeData();
});
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

/* Time Filter */
.time-filter .btn {
  border-radius: 8px;
  font-size: 13px;
  padding: 6px 16px;
}

/* Stats Cards - Horizontal Layout */
.stats-card {
  background: white;
  border-radius: 14px;
  padding: 22px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  display: flex;
  align-items: center;
  gap: 16px;
  transition: all 0.3s;
  border: 1px solid rgba(0, 0, 0, 0.04);
}

.stats-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
}

.stats-card-icon {
  width: 52px;
  height: 52px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  color: white;
  flex-shrink: 0;
}

.bg-gradient-revenue { background: linear-gradient(135deg, #212529, #000); }
.bg-gradient-orders { background: linear-gradient(135deg, #3b82f6, #1d4ed8); }
.bg-gradient-products { background: linear-gradient(135deg, #10b981, #059669); }
.bg-gradient-customers { background: linear-gradient(135deg, #f59e0b, #d97706); }

.stats-card-content {
  flex: 1;
}

.stats-label {
  font-size: 12px;
  color: #6c757d;
  margin: 0;
  text-transform: uppercase;
  letter-spacing: 0.3px;
  font-weight: 600;
}

.stats-value {
  font-size: 24px;
  font-weight: 800;
  color: #212529;
  margin: 2px 0;
  line-height: 1.2;
}

.stats-unit {
  font-size: 12px;
  font-weight: 500;
  color: #6c757d;
  margin-left: 4px;
}

.stats-change {
  font-size: 12px;
  font-weight: 600;
}

.stats-change.positive {
  color: #10b981;
}

.stats-change.negative {
  color: #ef4444;
}

.stats-change.neutral {
  color: #6c757d;
}

/* Content Card */
.content-card {
  background: white;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  border: 1px solid rgba(0, 0, 0, 0.04);
}

.chart-title {
  font-size: 16px;
  font-weight: 700;
  color: #333;
  margin-bottom: 4px;
}

.chart-subtitle {
  font-size: 13px;
  color: #6c757d;
  margin-bottom: 0;
}

/* Chart Containers */
.chart-container {
  height: 320px;
  position: relative;
}

.chart-container-doughnut {
  height: 300px;
  position: relative;
}

/* Recent Table */
.recent-table thead th {
  background: #f8f9fa;
  border-bottom: 2px solid #dee2e6;
  font-size: 12px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  color: #6c757d;
  padding: 10px 14px;
  white-space: nowrap;
}

.recent-table tbody td {
  padding: 12px 14px;
  vertical-align: middle;
  border-bottom: 1px solid #f0f0f0;
  font-size: 14px;
}

.recent-table tbody tr {
  transition: all 0.2s;
}

.recent-table tbody tr:hover {
  background: #f8f9ff;
}

/* Customer Avatar SM */
.avatar-sm {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
  font-weight: 700;
  color: white;
  flex-shrink: 0;
}

.bg-customer-sm {
  background: linear-gradient(135deg, #212529, #495057);
}

/* Order Badges */
.order-badge {
  padding: 5px 12px;
  border-radius: 20px;
  font-size: 11px;
  font-weight: 600;
}

.badge-completed {
  background: rgba(16, 185, 129, 0.12);
  color: #059669;
}

.badge-shipping {
  background: rgba(59, 130, 246, 0.12);
  color: #1d4ed8;
}

.badge-processing {
  background: rgba(245, 158, 11, 0.12);
  color: #d97706;
}

.badge-rejected {
  background: rgba(239, 68, 68, 0.12);
  color: #dc2626;
}

.badge-error {
  background: rgba(139, 92, 246, 0.12);
  color: #7c3aed;
}

.badge-returned {
  background: rgba(107, 114, 128, 0.12);
  color: #4b5563;
}

/* Top Customer */
.top-customer-list {
  display: flex;
  flex-direction: column;
  gap: 0;
}

.top-customer-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 0;
  border-bottom: 1px solid #f0f0f0;
  transition: all 0.2s;
}

.top-customer-item:last-child {
  border-bottom: none;
}

.top-customer-item:hover {
  background: #f8f9ff;
  margin: 0 -12px;
  padding-left: 12px;
  padding-right: 12px;
  border-radius: 8px;
}

.rank-badge {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 700;
  flex-shrink: 0;
}

.rank-1 {
  background: linear-gradient(135deg, #fbbf24, #f59e0b);
  color: white;
}

.rank-2 {
  background: linear-gradient(135deg, #94a3b8, #64748b);
  color: white;
}

.rank-3 {
  background: linear-gradient(135deg, #d97706, #b45309);
  color: white;
}

.rank-4 {
  background: #e5e7eb;
  color: #6b7280;
}

/* Category Stats */
.category-stat-card {
  background: #f8f9fa;
  border-radius: 12px;
  padding: 18px;
  border: 1px solid #eee;
  transition: all 0.3s;
}

.category-stat-card:hover {
  border-color: #212529;
  background: white;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.category-icon {
  font-size: 22px;
  margin-bottom: 10px;
  color: #212529;
}

.category-stat-card h6 {
  font-size: 14px;
  font-weight: 600;
  color: #333;
}

/* Responsive */
@media (max-width: 768px) {
  .main-content {
    margin-left: 0;
  }
  
  .page-container {
    padding: 15px;
  }
  
  .stats-card {
    flex-direction: column;
    text-align: center;
  }
}
</style>
