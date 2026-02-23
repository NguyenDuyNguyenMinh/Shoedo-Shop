<template>
  <div class="customer-layout">
    <KH_Navbar />
    
    <main class="container">
      <h2 class="mt-4 mb-3">Đơn Hàng Của Bạn</h2>

      <div v-if="error" class="alert alert-danger">{{ error }}</div>
      
      <div v-if="loading" class="text-center p-5">
        <div class="spinner-border text-primary" role="status">
          <span class="visually-hidden">Loading...</span>
        </div>
      </div>

      <div v-else-if="orders.length === 0" class="alert alert-info text-center">
        Bạn chưa có đơn hàng nào.
      </div>

      <div v-else>
        <div v-for="order in sortedOrders" :key="order.maHD" class="card p-3 mb-3">
          <div class="d-flex justify-content-between align-items-center mb-2">
            <span class="text-muted small">
              <i class="bi bi-calendar me-1"></i>
              {{ formatDate(order.ngayMua) }}
            </span>
            <span :class="getStatusClass(order.trangThai)">
              {{ order.trangThai }}
            </span>
          </div>
          
          <div class="d-flex justify-content-between align-items-center mb-2">
            <span v-if="order.hoaDonCTs && order.hoaDonCTs.length > 0">
              <strong>{{ order.hoaDonCTs[0]?.plSanPham?.sanPham?.tenSP
                  || order.hoaDonCTs[0]?.plSanPham?.tenSP
                  || "Không có tên sản phẩm" }}</strong>
              <small class="text-muted" v-if="order.hoaDonCTs.length > 1">
                và {{ order.hoaDonCTs.length - 1 }} sản phẩm khác
              </small>
            </span>
          </div>
          
          <hr class="my-2">
          
          <div class="d-flex justify-content-between align-items-center">
            <span class="text-muted">Thành Tiền</span>
            <strong class="text-danger">
              {{ formatPrice(calculateOrderTotal(order)) }}
            </strong>     
          </div>

          <div class="d-flex justify-content-between align-items-end mt-3">
            <!-- Hotline bên trái -->
            <div class="d-flex align-items-end">
              <i class="bi bi-telephone-fill me-1"></i>
              <small>Hotline hỗ trợ: <strong>0000000001</strong></small>
            </div>

            <!-- Các nút bên phải -->
            <div class="d-flex align-items-center">
              <button 
                v-if="order.trangThai === 'Đang giao'" 
                class="btn btn-success me-2"
                @click="confirmReceived(order.maHD)"
                :disabled="receivingOrderId === order.maHD"
              >
                <span v-if="receivingOrderId === order.maHD" class="spinner-border spinner-border-sm me-2"></span>
                Đã nhận hàng
              </button>

              <router-link :to="`/customer/order/${order.maHD}`" class="btn btn-outline-dark">
                Xem chi tiết
              </router-link>
            </div>
          </div>
        </div>
      </div>
    </main>
    <Footer />
  </div>
</template>

<script>
import { ref, onMounted, computed } from 'vue';
import api from '@/services/api';
import KH_Navbar from '@/components/shared/KH_Navbar.vue';
import Footer from '@/components/shared/Footer.vue';

export default {
  name: 'QLDonHang',
  components: {
    KH_Navbar,
    Footer
  },
  setup() {
    const orders = ref([]);
    const loading = ref(false);
    const error = ref('');
    const receivingOrderId = ref(null);

    const sortedOrders = computed(() => {
      return [...orders.value].sort((a, b) => {
        return new Date(b.ngayMua) - new Date(a.ngayMua);
      });
    });
    const fetchOrders = async () => {
      loading.value = true;
      error.value = '';
      
      try {
        const response = await api.getOrders();
        if (response.data.success) {
          orders.value = response.data.orders.sort((a, b) => {
            return new Date(b.ngayMua) - new Date(a.ngayMua);
          });
        } else {
          error.value = response.data.message || 'Không thể tải danh sách đơn hàng';
        }
      } catch (err) {
        error.value = 'Lỗi kết nối máy chủ';
        console.error('Error fetching orders:', err);
      } finally {
        loading.value = false;
      }
    };

    const calculateOrderTotal = (order) => {
      if (!order.hoaDonCTs) return 0;
      return order.hoaDonCTs.reduce((total, item) => {
        return total + (item.soLuong * item.donGia);
      }, 0);
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

    const confirmReceived = async (orderId) => {
      if (!confirm('Bạn đã nhận được hàng? Hành động này không thể hoàn tác.')) {
        return;
      }

      receivingOrderId.value = orderId;
      
      try {
        const response = await api.updateOrderStatus(orderId, 'Giao thành công');
        
        if (response.data.success) {
          alert('Cập nhật trạng thái thành công!');
          await fetchOrders();
        } else {
          alert(response.data.message || 'Cập nhật thất bại');
        }
      } catch (err) {
        alert('Lỗi kết nối máy chủ');
        console.error('Error updating order status:', err);
      } finally {
        receivingOrderId.value = null;
      }
    };

    onMounted(() => {
      fetchOrders();
    });

    return {
      orders,
      loading,
      error,
      receivingOrderId,
      sortedOrders,
      calculateOrderTotal,
      formatPrice,
      formatDate,
      getStatusClass,
      confirmReceived
    };
  }
};
</script>

<style scoped>
.card { 
  border: 2px solid #000000; 
  border-radius: 10px;
}

.badge {
  font-size: 0.8rem;
  padding: 5px 10px;
}
</style>