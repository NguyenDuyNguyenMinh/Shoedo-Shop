<template>
  <div class="employee-layout">
    <NV_Sidebar />
    
    <main class="main-content">
      <div class="page-container">
        <h1 class="h3 mb-4 text-gray-800">Bảng điều khiển - Thống kê</h1>

        <!-- Thông báo -->
        <div v-if="error" class="alert alert-danger">{{ error }}</div>

        <!-- Cards -->
        <div class="row" v-if="stats">
          <!-- Tổng doanh thu -->
          <div class="col-xl-4 col-md-6 mb-4">
            <div class="card border-left-primary shadow h-100 py-2">
              <div class="card-body">
                <div class="row no-gutters align-items-center">
                  <div class="col mr-2">
                    <div class="text-xs font-weight-bold text-primary text-uppercase mb-1">
                      TỔNG DOANH THU
                    </div>
                    <div class="h5 mb-0 font-weight-bold text-gray-800">
                      {{ formatPrice(stats.totalRevenue) }}
                    </div>
                  </div>
                  <div class="col-auto">
                    <i class="fas fa-dollar-sign fa-2x text-gray-300"></i>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Tổng khách hàng -->
          <div class="col-xl-4 col-md-6 mb-4">
            <div class="card border-left-success shadow h-100 py-2">
              <div class="card-body">
                <div class="row no-gutters align-items-center">
                  <div class="col mr-2">
                    <div class="text-xs font-weight-bold text-success text-uppercase mb-1">
                      TỔNG KHÁCH HÀNG
                    </div>
                    <div class="h5 mb-0 font-weight-bold text-gray-800">
                      {{ stats.totalUsers }}
                    </div>
                  </div>
                  <div class="col-auto">
                    <i class="fas fa-users fa-2x text-gray-300"></i>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Tổng nhân viên -->
          <div class="col-xl-4 col-md-6 mb-4">
            <div class="card border-left-info shadow h-100 py-2">
              <div class="card-body">
                <div class="row no-gutters align-items-center">
                  <div class="col mr-2">
                    <div class="text-xs font-weight-bold text-info text-uppercase mb-1">
                      TỔNG NHÂN VIÊN
                    </div>
                    <div class="h5 mb-0 font-weight-bold text-gray-800">
                      {{ stats.totalEmployees }}
                    </div>
                  </div>
                  <div class="col-auto">
                    <i class="fas fa-user-tie fa-2x text-gray-300"></i>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Tổng sản phẩm -->
          <div class="col-xl-4 col-md-6 mb-4">
            <div class="card border-left-warning shadow h-100 py-2">
              <div class="card-body">
                <div class="row no-gutters align-items-center">
                  <div class="col mr-2">
                    <div class="text-xs font-weight-bold text-warning text-uppercase mb-1">
                      TỔNG SẢN PHẨM
                    </div>
                    <div class="h5 mb-0 font-weight-bold text-gray-800">
                      {{ stats.totalProducts }}
                    </div>
                  </div>
                  <div class="col-auto">
                    <i class="fas fa-box-open fa-2x text-gray-300"></i>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Tổng tài khoản -->
          <div class="col-xl-4 col-md-6 mb-4">
            <div class="card border-left-danger shadow h-100 py-2">
              <div class="card-body">
                <div class="row no-gutters align-items-center">
                  <div class="col mr-2">
                    <div class="text-xs font-weight-bold text-danger text-uppercase mb-1">
                      TỔNG TÀI KHOẢN
                    </div>
                    <div class="h5 mb-0 font-weight-bold text-gray-800">
                      {{ stats.totalAccounts }}
                    </div>
                  </div>
                  <div class="col-auto">
                    <i class="fas fa-key fa-2x text-gray-300"></i>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Tổng danh mục -->
          <div class="col-xl-4 col-md-6 mb-4">
            <div class="card border-left-secondary shadow h-100 py-2">
              <div class="card-body">
                <div class="row no-gutters align-items-center">
                  <div class="col mr-2">
                    <div class="text-xs font-weight-bold text-secondary text-uppercase mb-1">
                      TỔNG DANH MỤC
                    </div>
                    <div class="h5 mb-0 font-weight-bold text-gray-800">
                      {{ stats.totalCategories }}
                    </div>
                  </div>
                  <div class="col-auto">
                    <i class="fas fa-list-alt fa-2x text-gray-300"></i>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Biểu đồ ở dưới cùng -->
        <div class="row">
          <div class="col-12">
            <div class="card shadow">
              <div class="card-header bg-dark py-3 d-flex flex-row align-items-center justify-content-between">
                <h6 class="m-0 font-weight-bold text-light">Biểu đồ Doanh thu Theo Ngày</h6>
              </div>
              <div class="card-body">
                <div class="chart-area">
                  <canvas id="revenueChart" ref="chartCanvas"></canvas>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script>
import { ref, onMounted, nextTick } from 'vue';
import { Chart } from 'chart.js/auto';
import api from '@/services/api';
import NV_Sidebar from '@/components/shared/NV_Sidebar.vue';

export default {
  name: 'EmployeeDashboard',
  components: {
    NV_Sidebar
  },
  setup() {
    const stats = ref(null);
    const chartData = ref([]);
    const loading = ref(false);
    const error = ref('');
    const chartCanvas = ref(null);
    let revenueChart = null;

    const fetchDashboardData = async () => {
      loading.value = true;
      error.value = '';
      
      try {
        const response = await api.getDashboardStats();
        
        if (response.data.success) {
          stats.value = response.data.stats;
          chartData.value = response.data.chartData;
          
          await nextTick();
          renderChart();
        } else {
          error.value = response.data.message || 'Không thể tải dữ liệu dashboard';
        }
      } catch (err) {
        error.value = 'Lỗi kết nối máy chủ';
        console.error('Error fetching dashboard data:', err);
      } finally {
        loading.value = false;
      }
    };

    const formatPrice = (price) => {
      return new Intl.NumberFormat('vi-VN').format(price) + ' đ';
    };

    const renderChart = () => {
      if (!chartCanvas.value || !chartData.value || chartData.value.length === 0) return;

      if (revenueChart) {
        revenueChart.destroy();
      }

      const ctx = chartCanvas.value.getContext('2d');
      const labels = chartData.value.map(item => item.date);
      const data = chartData.value.map(item => item.revenue);

      revenueChart = new Chart(ctx, {
        type: 'line',
        data: {
          labels: labels,
          datasets: [{
            label: 'Doanh thu',
            data: data,
            backgroundColor: 'rgba(78, 115, 223, 0.05)',
            borderColor: 'rgba(78, 115, 223, 1)',
            pointRadius: 3,
            pointBackgroundColor: 'rgba(78, 115, 223, 1)',
            pointBorderColor: 'rgba(78, 115, 223, 1)',
            pointHoverRadius: 4,
            pointHoverBackgroundColor: 'rgba(78, 115, 223, 1)',
            pointHoverBorderColor: 'rgba(78, 115, 223, 1)',
            borderWidth: 2,
            lineTension: 0.3
          }]
        },
        options: {
          maintainAspectRatio: false,
          responsive: true,
          scales: {
            y: {
              beginAtZero: true,
              ticks: {
                callback: function(value) {
                  return formatPrice(value);
                }
              }
            }
          },
          plugins: {
            tooltip: {
              callbacks: {
                label: function(context) {
                  let label = context.dataset.label || '';
                  if (label) {
                    label += ': ';
                  }
                  if (context.parsed.y !== null) {
                    label += formatPrice(context.parsed.y);
                  }
                  return label;
                }
              }
            }
          }
        }
      });
    };

    onMounted(() => {
      fetchDashboardData();
    });

    return {
      stats,
      chartData,
      loading,
      error,
      chartCanvas,
      formatPrice
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
.card {
  border: none;
  border-radius: 0.75rem;
  box-shadow: 0 0.15rem 1.75rem 0 rgba(58, 59, 69, 0.15);
  transition: transform .2s ease-in-out, box-shadow .2s ease-in-out;
  animation: fadeIn 0.5s ease-in-out;
}

.card:hover {
  transform: translateY(-3px);
  box-shadow: 0 0.5rem 1.75rem 0 rgba(58, 59, 69, 0.25);
  cursor: pointer;
}

.border-left-primary   { border-left: .25rem solid #0d6efd !important; }
.border-left-success   { border-left: .25rem solid #198754 !important; }
.border-left-info      { border-left: .25rem solid #0dcaf0 !important; }
.border-left-warning   { border-left: .25rem solid #ffc107 !important; }
.border-left-danger    { border-left: .25rem solid #dc3545 !important; }
.border-left-secondary { border-left: .25rem solid #6c757d !important; }


.text-primary   { color: #0d6efd !important; }
.text-success   { color: #198754 !important; }
.text-info      { color: #0dcaf0 !important; }
.text-warning   { color: #ffc107 !important; }
.text-danger    { color: #dc3545 !important; }
.text-secondary { color: #6c757d !important; }

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.chart-area {
  height: 600px;
}
</style>