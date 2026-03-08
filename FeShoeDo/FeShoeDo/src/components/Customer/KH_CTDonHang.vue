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
                  <span :class="getStatusClass(order.trangThai)" class="badge fw-bold">
                    {{ order.trangThai }}
                  </span>
                </p>
                <p><strong>Phương thức thanh toán:</strong> {{ order.phuongThucTT || 'Chưa cập nhật' }}</p>
                <p v-if="order.ghiChu"><strong>Ghi chú:</strong> {{ order.ghiChu }}</p>
              </div>
              <div class="col-md-6">
                <h6><strong>Địa chỉ giao hàng:</strong></h6>
                <div v-if="diaChi" class="p-3 bg-light rounded border">
                  <p class="mb-1"><strong>Địa chỉ:</strong> {{ diaChi.diaChi }}</p>
                  <p class="mb-1"><strong>Phường/Xã:</strong> {{ diaChi.phuongXa }}</p>
                  <p class="mb-1"><strong>Quận/Huyện:</strong> {{ diaChi.quanHuyen }}</p>
                  <p class="mb-0"><strong>Tỉnh/Thành:</strong> {{ diaChi.tinhThanh }}</p>
                </div>
                <div v-if="diaChiError" class="alert alert-warning mt-2">{{ diaChiError }}</div>
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
                         style="max-width: 70px; max-height: 70px; object-fit: cover;">
                    <div>
                      <p class="mb-0 fw-bold">{{ item.plSanPham?.sanPham?.tenSP || item.plSanPham?.tenSP }}</p>
                      <small class="text-muted" v-if="item.plSanPham?.phanLoai">
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

    const getImageUrl = (imageName) => {
      if (!imageName) return 'https://via.placeholder.com/70';
      // Using placeholder images since we don't have actual images
      return `https://via.placeholder.com/70/000000/ffffff?text=SP`;
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
        hour: '2-digit',
        minute: '2-digit'
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

    const fetchOrderDetail = async () => {
      loading.value = true;
      error.value = '';
      diaChiError.value = '';

      try {
        // Simulate network delay
        await new Promise(resolve => setTimeout(resolve, 800));

        const orderId = parseInt(route.params.id);
        const allOrders = getPlaceholderOrders();
        const foundOrder = allOrders.find(o => o.maHD === orderId);

        if (foundOrder) {
          order.value = foundOrder;

          // Parse diaChiJson if it exists
          if (foundOrder.diaChiJson) {
            try {
              diaChi.value = JSON.parse(foundOrder.diaChiJson);
            } catch (e) {
              diaChiError.value = 'Lỗi parse địa chỉ';
            }
          } else {
            diaChiError.value = 'Không có thông tin địa chỉ';
          }
        } else {
          error.value = 'Không tìm thấy đơn hàng';
        }
      } catch (err) {
        error.value = 'Lỗi khi tải chi tiết đơn hàng';
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

.img-thumbnail {
  object-fit: cover;
}
</style>