<template>
  <div class="customer-layout">
    <KH_Navbar />
    
    <main class="container">

      <div class="row mt-4 g-3">
        <div class="col-lg-8">
          <div id="mainCarousel" class="carousel slide" data-bs-ride="carousel">
            <div class="carousel-inner">
              <div class="carousel-item active">
                <img src="https://hoanghamobile.com/tin-tuc/wp-content/uploads/2024/02/black-friday.jpg" 
                     class="d-block w-100" 
                     alt="Banner 1"
                     style="height: 300px; object-fit: cover;">
              </div>
              <div class="carousel-item">
                <img src="https://img.freepik.com/premium-vector/special-sale-editable-text_404732-78.jpg" 
                     class="d-block w-100" 
                     alt="Banner 2"
                     style="height: 300px; object-fit: cover;">
              </div>
            </div>
            <button class="carousel-control-prev" type="button" data-bs-target="#mainCarousel" data-bs-slide="prev">
              <span class="carousel-control-prev-icon"></span>
            </button>
            <button class="carousel-control-next" type="button" data-bs-target="#mainCarousel" data-bs-slide="next">
              <span class="carousel-control-next-icon"></span>
            </button>
          </div>
        </div>
        <div class="col-lg-4">
          <div class="side-banner mb-3 text-center p-4">
            <h5>MIỄN PHÍ SHIP</h5>
          </div>
          <div class="side-banner text-center p-4">
            <h5>VOUCHER 100K</h5>
          </div>
        </div>
      </div>

      <div class="flash-sale mt-4">
        <h2 class="section-title">
          Tất Cả Sản Phẩm
          <small class="text-muted ms-2 fs-6">
            ({{ resultCount }} sản phẩm)
          </small>
        </h2>
        
        <div v-if="loading" class="text-center py-5">
          <div class="spinner-border text-dark" role="status">
            <span class="visually-hidden">Loading...</span>
          </div>
          <p class="mt-2">Đang tải sản phẩm...</p>
        </div>
        
        <div v-else class="row g-3">
          <div class="col-6 col-lg-2" v-for="sp in sanPhams" :key="sp.maSP">
            <div class="product-card">
              <div class="product-image" style="height:200px">
                <router-link :to="`/customer/detail-product/${sp.maSP}`">
                  <img :src="sp.hinh" 
                       :alt="sp.tenSP"
                       @error="handleImageError"
                       class="product-image" style="max-width: 150px;">
                </router-link>
                <span class="product-badge" v-if="sp.trangThai === 'Mới'">{{ sp.trangThai }}</span>
              </div>
              <div class="product-info">
                <h5 class="product-name">{{ sp.tenSP }}</h5>
                <div class="mb-2">
                  <span class="price-current">{{ formatPrice(sp.donGia) }}</span>
                </div>
                <div class="product-sold">
                  <i class="bi bi-box me-1"></i>
                  Kho: <span>{{ sp.soLuong }}</span>
                </div>
                <div class="product-category mt-2">
                  <i class="bi bi-tag me-1"></i>
                  <small>{{ getCategoryName(sp.danhMuc) }}</small>
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
import { ref, onMounted } from 'vue';
import KH_Navbar from '@/components/shared/KH_Navbar.vue';
import Footer from '@/components/shared/Footer.vue';

export default {
  name: 'CustomerIndex',
  components: {
    KH_Navbar,
    Footer
  },
  setup() {
    // Khởi tạo các biến chứa dữ liệu mẫu
    const sanPhams = ref([]);
    const loading = ref(true);
    const resultCount = ref(0);

    // Xử lý tiền tệ
    const formatPrice = (price) => {
      return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND'
      }).format(price);
    };

    // Lấy tên danh mục
    const getCategoryName = (danhMuc) => {
      return danhMuc?.tenDM || 'Không phân loại';
    };

    // Xử lý lỗi ảnh (Ảnh dự phòng)
    const handleImageError = (event) => {
      event.target.src = 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=400&h=400&fit=crop';
    };

    // Giả lập load dữ liệu khi trang vừa mở
    onMounted(() => {
      // Dùng setTimeout để tạo cảm giác đang load data (0.5 giây)
      setTimeout(() => {
        sanPhams.value = [
          {
            maSP: 1,
            tenSP: 'Nike Air Force 1 07',
            hinh: 'https://images.unsplash.com/photo-1595950653106-6c9ebd614d3a?w=400&h=400&fit=crop',
            donGia: 2900000,
            soLuong: 45,
            trangThai: 'Mới',
            danhMuc: { tenDM: 'Sneaker' }
          },
          {
            maSP: 2,
            tenSP: 'Adidas Ultraboost Light',
            hinh: 'https://images.unsplash.com/photo-1584735174965-48c48d4daf27?w=400&h=400&fit=crop',
            donGia: 4500000,
            soLuong: 12,
            trangThai: '',
            danhMuc: { tenDM: 'Giày Chạy Bộ' }
          },
          {
            maSP: 3,
            tenSP: 'Puma RS-X3 Puzzle',
            hinh: 'https://images.unsplash.com/photo-1608231387042-66d1773070a5?w=400&h=400&fit=crop',
            donGia: 2800000,
            soLuong: 8,
            trangThai: 'Mới',
            danhMuc: { tenDM: 'Giày Thể Thao' }
          },
          {
            maSP: 4,
            tenSP: 'New Balance 550 White',
            hinh: 'https://images.unsplash.com/photo-1539185441755-769473a23570?w=400&h=400&fit=crop',
            donGia: 3100000,
            soLuong: 20,
            trangThai: '',
            danhMuc: { tenDM: 'Sneaker' }
          },
          {
            maSP: 5,
            tenSP: 'Converse Chuck 70 Vintage',
            hinh: 'https://images.unsplash.com/photo-1607522370275-ba12051bed92?w=400&h=400&fit=crop',
            donGia: 1800000,
            soLuong: 55,
            trangThai: '',
            danhMuc: { tenDM: 'Classic' }
          },
          {
            maSP: 6,
            tenSP: 'Vans Old Skool Black',
            hinh: 'https://images.unsplash.com/photo-1525966222134-fcfa99b8ae77?w=400&h=400&fit=crop',
            donGia: 1650000,
            soLuong: 30,
            trangThai: 'Mới',
            danhMuc: { tenDM: 'Skate' }
          }
        ];
        resultCount.value = sanPhams.value.length;
        loading.value = false;
      }, 500); // Đợi 500ms rồi hiện data
    });

    return {
      sanPhams,
      loading,
      resultCount,
      handleImageError,
      formatPrice,
      getCategoryName
    };
  }
};
</script>

<style scoped>
:root {
  --pine-primary: #333333;
  --pine-secondary: #666666;
  --pine-green: #000000;
  --pine-dark: #111111;
  --pine-light: #f8f9fa;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  background: #ffffff;
  color: #000000;
  min-height: 100vh;
}

/* Categories */
.categories {
  background: #ffffff;
  padding: 35px 25px 40px 25px;
  border-radius: 16px;
  margin-top: 30px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.13);
  border: 2px solid #000000;
}

.category-item {
  text-align: center;
  cursor: pointer;
  padding: 18px 0 0 0;
  transition: transform 0.25s, box-shadow 0.25s;
  border-radius: 12px;
  background: transparent;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.07);
  position: relative;
}

.category-item:hover {
  transform: translateY(-8px) scale(1.10);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.2);
  background: #f8f9fa;
  z-index: 2;
}

.category-icon {
  width: 64px;
  height: 64px;
  margin: 0 auto 12px;
  background: #000000;
  border-radius: 20%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
  color: #ffffff;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.2);
  border: 2px solid #000000;
  transition: background 0.2s, box-shadow 0.2s, color 0.2s;
}

.category-item:hover .category-icon {
  background: #333333;
  color: #ffffff;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
  border: 2px solid #333333;
}

.category-name {
  font-size: 15px;
  color: #000000;
  font-weight: 600;
  letter-spacing: 1px;
  margin-top: 2px;
}

/* Carousel */
.carousel-item img {
  height: 300px;
  object-fit: cover;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.12);
}

.carousel-control-prev-icon,
.carousel-control-next-icon {
  background-color: #000000;
  border-radius: 50%;
  padding: 20px;
}

/* Side Banners */
.side-banner {
  background: #000000;
  height: 145px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ffffff;
  font-size: 18px;
  font-weight: bold;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.12);
}

/* Flash Sale */
.flash-sale {
  background: #ffffff;
  padding: 25px;
  border-radius: 8px;
  margin-top: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.09);
  border: 2px solid #000000;
}

.flash-title {
  font-size: 18px;
  color: #000000;
  font-weight: bold;
}

.flash-timer {
  background: #000000;
  color: white;
  padding: 8px 15px;
  border-radius: 8px;
  font-size: 14px;
}

/* Product Card */
.product-card {
  background: #ffffff;
  border: 2px solid #000000;
  border-radius: 8px;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
  margin-bottom: 15px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.product-card:hover {
  transform: translateY(-2px) scale(1.03);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.13);
}

.product-image {
  width: 100%;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
}

.product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s
}


.product-card:hover .product-image img {
  transform: scale(1.07);
}

.product-badge {
  position: absolute;
  top: 5px;
  left: 5px;
  background: #000000;
  color: #ffffff;
  padding: 3px 8px;
  font-size: 12px;
  border-radius: 4px;
  font-weight: bold;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.product-info {
  padding: 12px;
}

.product-name {
  font-size: 15px;
  color: #000000;
  height: 40px;
  overflow: hidden;
  margin-bottom: 8px;
  font-weight: 500;
}

.price-current {
  color: #000000;
  font-size: 16px;
  font-weight: 600;
}

.price-discount {
  background: #000000;
  color: #ffffff;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 12px;
  margin-left: 5px;
  font-weight: bold;
}

.product-sold {
  font-size: 12px;
  color: #666666;
  margin-top: 8px;
}

/* Section Title */
.section-title {
  font-size: 26px;
  color: #000000;
  font-weight: bold;
  text-align: center;
  margin-bottom: 32px;
  text-transform: uppercase;
  letter-spacing: 2px;
  border-bottom: 2px solid #000000;
  display: inline-block;
  padding-bottom: 6px;
}

/* Footer */
.footer {
  background: #ffffff;
  margin-top: 40px;
  padding: 40px 0 20px;
  border-top: 4px solid #000000;
  border-radius: 8px 8px 0 0;
}

.footer h5 {
  font-size: 14px;
  color: #000000;
  margin-bottom: 15px;
  text-transform: uppercase;
}

.footer ul {
  list-style: none;
  padding: 0;
}

.footer li {
  margin-bottom: 10px;
}

.footer a {
  color: #000000;
  text-decoration: none;
  font-size: 13px;
}

.footer a:hover {
  color: #333333;
}

.footer-bottom {
  text-align: center;
  padding-top: 20px;
  border-top: 1px solid #000000;
  color: #000000;
  font-size: 12px;
}

.btn-primary {
  background-color: #000000;
  border-color: #000000;
}

.btn-primary:hover {
  background-color: #333333;
  border-color: #333333;
}
</style>