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
        <div class="row row-cols-1 row-cols-md-2 row-cols-lg-3 row-cols-xl-5 g-3">
          <div v-for="order in sortedOrders" :key="order.maHD" class="col">
            <div class="card h-100 p-3 d-flex flex-column">
              <div class="d-flex justify-content-between align-items-center mb-2">
                <span class="text-muted small">
                  <i class="bi bi-calendar me-1"></i>
                  {{ formatDate(order.ngayMua) }}
                </span>
                          <span :class="getStatusClass(order.trangThai)" class="badge">
                  {{ order.trangThai }}
                </span>
              </div>

              <div class="d-flex justify-content-between align-items-center mb-2">
                <span v-if="order.hoaDonCTs && order.hoaDonCTs.length > 0">
                  <strong class="product-name">{{ order.hoaDonCTs[0]?.plSanPham?.sanPham?.tenSP
                  || order.hoaDonCTs[0]?.plSanPham?.tenSP
                  || "Không có tên sản phẩm" }}</strong>
                  <small class="text-muted d-block" v-if="order.hoaDonCTs.length > 1">
                    và {{ order.hoaDonCTs.length - 1 }} sản phẩm khác
                  </small>
                </span>
              </div>

              <hr class="my-2 w-100" style="margin-top: auto !important;">

              <div>
                <div class="d-flex justify-content-between align-items-center mb-2">
                  <span class="text-muted">Thành Tiền</span>
                  <strong class="text-danger">
                    {{ formatPrice(calculateOrderTotal(order)) }}
                  </strong>
                </div>

                <div class="d-flex flex-column mt-2">
                  <!-- Hotline -->
                  <div class="d-flex align-items-center mb-2 small">
                    <i class="bi bi-telephone-fill me-1"></i>
                    <span>Hotline: <strong>0000000001</strong></span>
                  </div>

                  <!-- Các nút -->
                  <div class="d-flex flex-column gap-2">
                    <button
                        v-if="order.trangThai === 'Đang giao'"
                        class="btn btn-success btn-sm w-100"
                        @click="confirmReceived(order.maHD)"
                        :disabled="receivingOrderId === order.maHD"
                    >
                      <span v-if="receivingOrderId === order.maHD" class="spinner-border spinner-border-sm me-2"></span>
                      Đã nhận hàng
                    </button>

                    <router-link :to="`/customer/order/${order.maHD}`" class="btn btn-outline-dark btn-sm w-100">
                      Xem chi tiết
                    </router-link>
                  </div>
                </div>
              </div>
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

    // Placeholder data function với tên giày đẹp
    const getPlaceholderOrders = () => {
      return [
        {
          maHD: 1,
          maKH: 1,
          maQT: 1,
          ngayMua: '2024-01-15T10:30:00',
          trangThai: 'Đang giao',
          phuongThucTT: 'Thanh toán khi nhận hàng',
          diaChiJson: JSON.stringify({
            diaChi: '123 Nguyễn Văn Linh',
            phuongXa: 'Phường Tân Phú',
            quanHuyen: 'Quận 7',
            tinhThanh: 'TP Hồ Chí Minh'
          }),
          ghiChu: 'Gọi trước khi giao - Giày quà tặng',
          hoaDonCTs: [
            {
              maHDCT: 1,
              maSKU: 1001,
              soLuong: 1,
              donGia: 4500000,
              plSanPham: {
                maPLSP: 101,
                maSKU: 1001,
                tenSP: 'Nike Air Force 1 Low White',
                hinh: 'nike-af1-white.jpg',
                phanLoai: 'Màu trắng - Size 42',
                sanPham: {
                  tenSP: 'Nike Air Force 1 Low White'
                }
              }
            },
            {
              maHDCT: 2,
              maSKU: 1002,
              soLuong: 1,
              donGia: 5200000,
              plSanPham: {
                maPLSP: 102,
                maSKU: 1002,
                tenSP: 'Adidas Samba OG Black White',
                hinh: 'adidas-samba.jpg',
                phanLoai: 'Màu đen trắng - Size 41',
                sanPham: {
                  tenSP: 'Adidas Samba OG Black White'
                }
              }
            }
          ]
        },
        {
          maHD: 2,
          maKH: 1,
          maQT: 1,
          ngayMua: '2024-01-10T14:20:00',
          trangThai: 'Hoàn tất',
          phuongThucTT: 'Chuyển khoản',
          diaChiJson: JSON.stringify({
            diaChi: '456 Lê Văn Việt',
            phuongXa: 'Phường Tăng Nhơn Phú A',
            quanHuyen: 'Quận 9',
            tinhThanh: 'TP Hồ Chí Minh'
          }),
          ghiChu: 'Gói quà cẩn thận',
          hoaDonCTs: [
            {
              maHDCT: 3,
              maSKU: 1003,
              soLuong: 2,
              donGia: 3200000,
              plSanPham: {
                maPLSP: 103,
                maSKU: 1003,
                tenSP: 'Converse Chuck Taylor All Star High Top',
                hinh: 'converse-chuck.jpg',
                phanLoai: 'Màu đen - Size 40',
                sanPham: {
                  tenSP: 'Converse Chuck Taylor All Star High Top'
                }
              }
            }
          ]
        },
        {
          maHD: 3,
          maKH: 1,
          maQT: 2,
          ngayMua: '2024-01-05T09:15:00',
          trangThai: 'Đang xử lý',
          phuongThucTT: 'Thanh toán khi nhận hàng',
          diaChiJson: JSON.stringify({
            diaChi: '789 Hoàng Diệu',
            phuongXa: 'Phường 10',
            quanHuyen: 'Quận Phú Nhuận',
            tinhThanh: 'TP Hồ Chí Minh'
          }),
          ghiChu: 'Kiểm tra hàng trước khi thanh toán',
          hoaDonCTs: [
            {
              maHDCT: 4,
              maSKU: 1004,
              soLuong: 1,
              donGia: 6800000,
              plSanPham: {
                maPLSP: 104,
                maSKU: 1004,
                tenSP: 'New Balance 550 White Green',
                hinh: 'nb-550.jpg',
                phanLoai: 'Màu trắng xanh - Size 43',
                sanPham: {
                  tenSP: 'New Balance 550 White Green'
                }
              }
            },
            {
              maHDCT: 5,
              maSKU: 1005,
              soLuong: 1,
              donGia: 2900000,
              plSanPham: {
                maPLSP: 105,
                maSKU: 1005,
                tenSP: 'Vans Old Skool Black White',
                hinh: 'vans-old-skool.jpg',
                phanLoai: 'Màu đen trắng - Size 41',
                sanPham: {
                  tenSP: 'Vans Old Skool Black White'
                }
              }
            }
          ]
        },
        {
          maHD: 4,
          maKH: 1,
          maQT: 2,
          ngayMua: '2023-12-28T16:45:00',
          trangThai: 'Đã từ chối',
          phuongThucTT: 'Thanh toán khi nhận hàng',
          diaChiJson: JSON.stringify({
            diaChi: '321 Cách Mạng Tháng 8',
            phuongXa: 'Phường 11',
            quanHuyen: 'Quận 3',
            tinhThanh: 'TP Hồ Chí Minh'
          }),
          ghiChu: 'Hết size giày',
          hoaDonCTs: [
            {
              maHDCT: 6,
              maSKU: 1006,
              soLuong: 1,
              donGia: 8900000,
              plSanPham: {
                maPLSP: 106,
                maSKU: 1006,
                tenSP: 'Jordan 1 Retro High OG Chicago',
                hinh: 'jordan1-chicago.jpg',
                phanLoai: 'Màu đỏ trắng - Size 42.5',
                sanPham: {
                  tenSP: 'Jordan 1 Retro High OG Chicago'
                }
              }
            }
          ]
        },
        {
          maHD: 5,
          maKH: 1,
          maQT: 3,
          ngayMua: '2024-01-12T11:30:00',
          trangThai: 'Hoàn hàng/trả hàng',
          phuongThucTT: 'Chuyển khoản',
          diaChiJson: JSON.stringify({
            diaChi: '654 Nguyễn Trãi',
            phuongXa: 'Phường 14',
            quanHuyen: 'Quận 5',
            tinhThanh: 'TP Hồ Chí Minh'
          }),
          ghiChu: 'Khách đổi size từ 42 sang 41',
          hoaDonCTs: [
            {
              maHDCT: 7,
              maSKU: 1007,
              soLuong: 1,
              donGia: 4200000,
              plSanPham: {
                maPLSP: 107,
                maSKU: 1007,
                tenSP: 'Asics Gel-Kayano 14 Cream',
                hinh: 'asics-kayano14.jpg',
                phanLoai: 'Màu kem - Size 42',
                sanPham: {
                  tenSP: 'Asics Gel-Kayano 14 Cream'
                }
              }
            }
          ]
        },
        {
          maHD: 6,
          maKH: 1,
          maQT: 3,
          ngayMua: '2024-01-18T15:20:00',
          trangThai: 'Đang giao',
          phuongThucTT: 'Thẻ tín dụng',
          diaChiJson: JSON.stringify({
            diaChi: '987 Lý Thường Kiệt',
            phuongXa: 'Phường 14',
            quanHuyen: 'Quận 10',
            tinhThanh: 'TP Hồ Chí Minh'
          }),
          ghiChu: 'Gọi điện trước 15 phút',
          hoaDonCTs: [
            {
              maHDCT: 8,
              maSKU: 1008,
              soLuong: 1,
              donGia: 5500000,
              plSanPham: {
                maPLSP: 108,
                maSKU: 1008,
                tenSP: 'Hoka One One Bondi 8 Black',
                hinh: 'hoka-bondi8.jpg',
                phanLoai: 'Màu đen - Size 44',
                sanPham: {
                  tenSP: 'Hoka One One Bondi 8 Black'
                }
              }
            },
            {
              maHDCT: 9,
              maSKU: 1009,
              soLuong: 1,
              donGia: 2100000,
              plSanPham: {
                maPLSP: 109,
                maSKU: 1009,
                tenSP: 'MLB Big Ball Chunky A Korea',
                hinh: 'mlb-bigball.jpg',
                phanLoai: 'Màu trắng đen - Size 240',
                sanPham: {
                  tenSP: 'MLB Big Ball Chunky A Korea'
                }
              }
            }
          ]
        },
        {
          maHD: 7,
          maKH: 1,
          maQT: 1,
          ngayMua: '2024-01-20T09:45:00',
          trangThai: 'Đang xử lý',
          phuongThucTT: 'Thanh toán khi nhận hàng',
          diaChiJson: JSON.stringify({
            diaChi: '147 Hai Bà Trưng',
            phuongXa: 'Phường 6',
            quanHuyen: 'Quận 3',
            tinhThanh: 'TP Hồ Chí Minh'
          }),
          ghiChu: 'Combo 2 giày tập luyện',
          hoaDonCTs: [
            {
              maHDCT: 10,
              maSKU: 1010,
              soLuong: 2,
              donGia: 2800000,
              plSanPham: {
                maPLSP: 110,
                maSKU: 1010,
                tenSP: 'UltraBOOST 22 Shoes',
                hinh: 'adidas-ultraboost.jpg',
                phanLoai: 'Màu hồng - Size 39',
                sanPham: {
                  tenSP: 'UltraBOOST 22 Shoes'
                }
              }
            }
          ]
        },
        {
          maHD: 8,
          maKH: 1,
          maQT: 2,
          ngayMua: '2024-01-22T13:15:00',
          trangThai: 'Hoàn tất',
          phuongThucTT: 'Chuyển khoản',
          diaChiJson: JSON.stringify({
            diaChi: '258 Võ Văn Ngân',
            phuongXa: 'Phường Linh Chiểu',
            quanHuyen: 'TP Thủ Đức',
            tinhThanh: 'TP Hồ Chí Minh'
          }),
          ghiChu: 'Đã giao thành công',
          hoaDonCTs: [
            {
              maHDCT: 11,
              maSKU: 1011,
              soLuong: 1,
              donGia: 7300000,
              plSanPham: {
                maPLSP: 111,
                maSKU: 1011,
                tenSP: 'Salomon XT-6 Advanced',
                hinh: 'salomon-xt6.jpg',
                phanLoai: 'Màu xám đen - Size 42',
                sanPham: {
                  tenSP: 'Salomon XT-6 Advanced'
                }
              }
            }
          ]
        },
        {
          maHD: 9,
          maKH: 1,
          maQT: 3,
          ngayMua: '2024-01-23T10:00:00',
          trangThai: 'Báo lỗi',
          phuongThucTT: 'Thanh toán khi nhận hàng',
          diaChiJson: JSON.stringify({
            diaChi: '369 Lê Văn Sỹ',
            phuongXa: 'Phường 12',
            quanHuyen: 'Quận 3',
            tinhThanh: 'TP Hồ Chí Minh'
          }),
          ghiChu: 'Sai màu sắc so với đơn hàng (đặt đen nhận trắng)',
          hoaDonCTs: [
            {
              maHDCT: 12,
              maSKU: 1012,
              soLuong: 1,
              donGia: 3950000,
              plSanPham: {
                maPLSP: 112,
                maSKU: 1012,
                tenSP: 'Puma Suede Classic XXI',
                hinh: 'puma-suede.jpg',
                phanLoai: 'Màu đen - Size 41',
                sanPham: {
                  tenSP: 'Puma Suede Classic XXI'
                }
              }
            }
          ]
        },
        {
          maHD: 10,
          maKH: 1,
          maQT: 1,
          ngayMua: '2024-01-24T16:30:00',
          trangThai: 'Đang giao',
          phuongThucTT: 'Thẻ tín dụng',
          diaChiJson: JSON.stringify({
            diaChi: '741 Nguyễn Oanh',
            phuongXa: 'Phường 17',
            quanHuyen: 'Quận Gò Vấp',
            tinhThanh: 'TP Hồ Chí Minh'
          }),
          ghiChu: 'Giao trong giờ hành chính',
          hoaDonCTs: [
            {
              maHDCT: 13,
              maSKU: 1013,
              soLuong: 1,
              donGia: 3350000,
              plSanPham: {
                maPLSP: 113,
                maSKU: 1013,
                tenSP: 'Reebok Club C 85 Vintage',
                hinh: 'reebok-clubc.jpg',
                phanLoai: 'Màu trắng kem - Size 42',
                sanPham: {
                  tenSP: 'Reebok Club C 85 Vintage'
                }
              }
            },
            {
              maHDCT: 14,
              maSKU: 1014,
              soLuong: 1,
              donGia: 1800000,
              plSanPham: {
                maPLSP: 114,
                maSKU: 1014,
                tenSP: 'Fila Disruptor II Premium',
                hinh: 'fila-disruptor.jpg',
                phanLoai: 'Màu trắng - Size 235',
                sanPham: {
                  tenSP: 'Fila Disruptor II Premium'
                }
              }
            }
          ]
        }
      ];
    };

    const fetchOrders = async () => {
      loading.value = true;
      error.value = '';

      try {
        await new Promise(resolve => setTimeout(resolve, 800));
        orders.value = getPlaceholderOrders();
      } catch (err) {
        error.value = 'Lỗi khi tải dữ liệu';
        console.error('Error loading orders:', err);
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
        case 'Hoàn tất':
          return 'badge bg-success';
        case 'Đang giao':
          return 'badge bg-primary';
        case 'Đang xử lý':
          return 'badge bg-warning text-dark';
        case 'Đã từ chối':
          return 'badge bg-danger';
        case 'Hoàn hàng/trả hàng':
          return 'badge bg-secondary';
        case 'Báo lỗi':
          return 'badge bg-dark';
        default:
          return 'badge bg-secondary';
      }
    };

    const confirmReceived = async (orderId) => {
      if (!confirm('Bạn đã nhận được hàng? Hành động này không thể hoàn tác.')) {
        return;
      }

      receivingOrderId.value = orderId;

      try {
        await new Promise(resolve => setTimeout(resolve, 1000));

        const orderIndex = orders.value.findIndex(o => o.maHD === orderId);
        if (orderIndex !== -1) {
          orders.value[orderIndex].trangThai = 'Hoàn tất';
          orders.value = [...orders.value];
          alert('Cập nhật trạng thái thành công!');
        } else {
          alert('Không tìm thấy đơn hàng');
        }
      } catch (err) {
        alert('Lỗi khi cập nhật trạng thái');
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
  display: flex;
  flex-direction: column;
}

hr {
  margin-top: auto !important;
}

.card:hover {
  transform: translateY(-5px);
  box-shadow: 0 5px 15px rgba(0,0,0,0.1);
}

.badge {
  font-size: 0.7rem;
  padding: 4px 8px;
  white-space: nowrap;
}

.product-name {
  font-size: 0.9rem;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}

.row {
  margin-right: -0.5rem;
  margin-left: -0.5rem;
}

.col {
  padding-right: 0.5rem;
  padding-left: 0.5rem;
}

.btn-sm {
  font-size: 0.8rem;
}
</style>