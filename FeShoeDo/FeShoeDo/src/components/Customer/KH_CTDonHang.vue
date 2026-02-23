<template>
  <div class="customer-layout">
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
                <p><strong>Trạng thái:</strong>
                  <span :class="getStatusClass(order.trangThai)" class="status-badge fw-bold">
                    {{ order.trangThai }}
                  </span>
                </p>
              </div>
              <div class="col-md-6">
                <h6><strong>Địa chỉ giao hàng:</strong></h6>
                <div v-if="diaChi" class="p-3 bg-light rounded border">
                  <p class="mb-1"><strong>Người nhận:</strong> {{ diaChi.TenNN }}</p>
                  <p class="mb-1"><strong>Số điện thoại:</strong> {{ diaChi.SDT }}</p>
                  <p class="mb-0"><strong>Địa chỉ:</strong> {{ diaChi.DiemGiao }}</p>
                </div>
                <div v-if="diaChiError" class="alert alert-warning">{{ diaChiError }}</div>
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
                  <th class="ps-4 col-sm-6 text-start">Sản phẩm</th>
                  <th class="col-sm-2 text-center">Số lượng</th>
                  <th class="col-sm-2 text-center">Đơn giá</th>
                  <th class="col-sm-2 text-center">Thành tiền</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="item in order.hoaDonCTs" :key="item.maHDCT" class="align-middle">
                  <td>
                    <div class="ps-3 col-sm-6 d-flex align-items-center" style="height: 70px;">
                      <img :src="getImageUrl(item.plSanPham?.hinh)" 
                           alt="Hình sản phẩm" 
                           class="img-thumbnail me-3" 
                           style="max-width: 70px;">
                      <div>
                        <p class="mb-0 fw-bold">{{ item.plSanPham?.sanPham?.tenSP}}</p>
                        <small class="text-muted">
                          Phân loại: {{ item.plSanPham?.phanLoai }}
                        </small>
                      </div>
                    </div>
                  </td>
                  <td class="col-sm-2 text-center" style="height: 70px;">
                    {{ item.soLuong }}
                  </td>
                  <td class="col-sm-2 text-center" style="height: 70px;">
                    {{ formatPrice(item.donGia) }}
                  </td>
                  <td class="col-sm-2 text-center" style="height: 70px;">
                    {{ formatPrice(item.soLuong * item.donGia) }}
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
          <div class="card-footer d-flex justify-content-end align-items-center">
            <h5 class="mb-0 me-3">Tổng Cộng:</h5>
            <h4 class="mb-0 text-danger fw-bold">{{ formatPrice(totalPrice) }}</h4>
          </div>
        </div>
      </div>
    </main>
    <Footer />
  </div>
</template>

<script>
import { ref, onMounted, computed } from 'vue';
import { useRoute } from 'vue-router';
import api from '@/services/api';
import KH_Navbar from '@/components/shared/KH_Navbar.vue';
import Footer from '@/components/shared/Footer.vue';

export default {
  name: 'CTDonHang',
  components: {
    KH_Navbar,
    Footer
  },
  setup() {
    const route = useRoute();
    const order = ref(null);
    const diaChi = ref(null);
    const loading = ref(false);
    const error = ref('');
    const diaChiError = ref('');

    const getImageUrl = (imageName) => {
      if (!imageName) return 'https://via.placeholder.com/70';
      return `http://localhost:8080/images/${imageName}`;
    };

    const formatPrice = (price) => {
      return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND'
      }).format(price);
    };

    const formatDate = (dateString) => {
      if (!dateString) return '';
      const date = new Date(dateString);
      return date.toLocaleDateString('vi-VN', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric',
      });
    };

    const getStatusClass = (status) => {
      switch (status) {
        case 'Đã nhận':
        case 'Giao thành công': return 'badge bg-success';
        case 'Đang giao': return 'badge bg-primary';
        case 'Chờ duyệt': return 'badge bg-warning text-dark';
        case 'Đã từ chối': return 'badge bg-danger';
        default: return 'badge bg-secondary';
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
          order.value = response.data.order;
          diaChi.value = response.data.diaChi;
          
          if (!diaChi.value) {
            diaChiError.value = 'Không có thông tin địa chỉ';
          }
        } else {
          error.value = response.data.message || 'Không thể tải chi tiết đơn hàng';
        }
      } catch (err) {
        error.value = 'Lỗi kết nối máy chủ';
        console.error('Error fetching order detail:', err);
      } finally {
        loading.value = false;
      }
    };

    const totalPrice = computed(() => {
      if (!order.value || !order.value.hoaDonCTs) return 0;
      return order.value.hoaDonCTs.reduce((total, item) => {
        return total + (item.soLuong * item.donGia);
      }, 0);
    });

    onMounted(() => {
      fetchOrderDetail();
    });

    return {
      order,
      diaChi,
      loading,
      error,
      diaChiError,
      totalPrice,
      getImageUrl,
      formatPrice,
      formatDate,
      getStatusClass
    };
  }
};
</script>

<style scoped>
.card { 
  border: 2px solid #000000; 
  border-radius: 10px;
}

.card-header {
  font-weight: bold;
  background-color: #000000;
  color: #ffffff;
  border-bottom: 2px solid #000000;
}

.badge {
  font-size: 0.8rem;
  padding: 5px 10px;
}

.table thead th {
  background-color: #f8f9fa;
  border-bottom: 2px solid #000000;
}
</style>