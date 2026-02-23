<template>
  <div class="customer-layout">
    <KH_Navbar />
    
    <main class="container">
      <!-- Hiển thị lỗi nếu có -->
      <div v-if="errorMessage" class="alert alert-danger mt-3">
        <i class="bi bi-exclamation-triangle me-2"></i>
        {{ errorMessage }}
      </div>

      <!-- Hiển thị kết quả tìm kiếm -->
      <div v-if="searchPerformed && resultCount !== null" class="search-result-info mt-4 text-center">
        <span class="badge bg-success-subtle text-success py-2 px-4">
          <i class="bi bi-check-circle me-2"></i>
          Tìm thấy <strong>{{ resultCount }}</strong> sản phẩm
          <span v-if="keyword"> với từ khóa: "{{ keyword }}"</span>
          <span v-if="selectedCategoryName"> trong danh mục: {{ selectedCategoryName }}</span>
        </span>
      </div>

      <!-- Banner Carousel -->
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

      <!-- Products from API -->
      <div class="flash-sale mt-4">
        <h2 class="section-title">
          <span v-if="searchPerformed">Kết Quả Tìm Kiếm</span>
          <span v-else>Tất Cả Sản Phẩm</span>
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
        
        <div v-else>
          <div v-if="sanPhams.length === 0" class="text-center py-5">
            <div class="alert alert-info">
              <i class="bi bi-info-circle me-2"></i>
              <span v-if="searchPerformed">Không tìm thấy sản phẩm phù hợp</span>
              <span v-else>Không có sản phẩm nào trong hệ thống</span>
            </div>
          </div>
          
          <div v-else class="row g-3">
            <div class="col-6 col-lg-2" v-for="sp in sanPhams" :key="sp.maSP">
              <div class="product-card">
                <div class="product-image" style="height:200px">
                  <router-link :to="`/customer/detail-product/${sp.maSP}`">
                    <img :src="getImageUrl(sp.hinh)" 
                         :alt="sp.tenSP"
                         @error="handleImageError"
                         class="product-image" style="max-width: 150px;">
                  </router-link>
                  <span class="product-badge" v-if="sp.trangThai && sp.trangThai === 'Mới'">{{ sp.trangThai }}</span>
                </div>
                <div class="product-info">
                  <h5 class="product-name">{{ sp.tenSP }}</h5>
                  <div class="mb-2">
                    <span class="price-current">{{ formatPrice(sp.donGia) }}</span>
                  </div>
                  <div class="product-sold">
                    <i class="bi bi-box me-1"></i>
                    Kho: <span>{{ sp.soLuong || 0 }}</span>
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
      </div>
    </main>
    
    <Footer />
  </div>
</template>

<!-- Trong script -->
<script>
import { ref, onMounted, watch } from 'vue';
import { useRoute } from 'vue-router';
import api from '@/services/api';
import KH_Navbar from '@/components/shared/KH_Navbar.vue';
import Footer from '@/components/shared/Footer.vue';

export default {
  name: 'CustomerIndex',
  components: {
    KH_Navbar,
    Footer
  },
  setup() {
    const route = useRoute();
    const sanPhams = ref([]);
    const loading = ref(false);
    const resultCount = ref(null);
    const keyword = ref('');
    const categoryId = ref(null);
    const selectedCategoryName = ref('');
    const searchPerformed = ref(false);
    const danhMucs = ref([]);
    const errorMessage = ref('');

    const getImageUrl = (imageName) => {
      if (!imageName) return 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=400&h=400&fit=crop';
      return `http://localhost:8080/images/${imageName}`;
    };

    const handleImageError = (event) => {
      event.target.src = 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=400&h=400&fit=crop';
    };

    const formatPrice = (price) => {
      if (!price) return '0₫';
      return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND'
      }).format(price);
    };

    const getCategoryName = (danhMuc) => {
      return danhMuc?.tenDM || 'Không phân loại';
    };

    const fetchCategories = async () => {
      try {
        console.log('Fetching categories...');
        const response = await api.getCategories();
        if (response.data.success) {
          danhMucs.value = response.data.data || [];
        }
      } catch (error) {
        console.error('Error fetching categories:', error);
      }
    };

    const fetchAllProducts = async () => {
      loading.value = true;
      errorMessage.value = '';
      searchPerformed.value = false;
      keyword.value = '';
      categoryId.value = null;
      selectedCategoryName.value = '';
      
      try {
        const response = await api.getPublicProducts();

        if (response.data.success) {
          sanPhams.value = response.data.data || [];
          resultCount.value = response.data.count || sanPhams.value.length;
        } else {
          sanPhams.value = [];
          resultCount.value = 0;
          errorMessage.value = response.data.message || 'Không thể tải sản phẩm';
        }
      } catch (error) {
        console.error('Error fetching all products:', error);
        sanPhams.value = [];
        resultCount.value = 0;
        errorMessage.value = 'Lỗi kết nối server: ' + error.message;
      } finally {
        loading.value = false;
      }
    };

    const searchProducts = async (searchKeyword = keyword.value, searchCategoryId = categoryId.value) => {
      loading.value = true;
      errorMessage.value = '';
      searchPerformed.value = true;
      
      try {
        const params = {};
        if (searchKeyword && searchKeyword.trim()) {
          params.keyword = searchKeyword.trim();
        }
        if (searchCategoryId) {
          params.categoryId = searchCategoryId;
        }

        const response = await api.searchPublicProducts(params); 
        if (response.data.success) {
          sanPhams.value = response.data.data || [];
          resultCount.value = response.data.count || 0;
          
          if (searchCategoryId) {
            const category = danhMucs.value.find(dm => dm.maDM == searchCategoryId);
            selectedCategoryName.value = category ? category.tenDM : '';
          } else {
            selectedCategoryName.value = '';
          }
          
        } else {
          sanPhams.value = [];
          resultCount.value = 0;
          errorMessage.value = response.data.message || 'Tìm kiếm thất bại';
        }
      } catch (error) {
        sanPhams.value = [];
        resultCount.value = 0;
        errorMessage.value = 'Lỗi tìm kiếm: ' + error.message;
      } finally {
        loading.value = false;
      }
    };

    watch(() => route.query, (newQuery) => {
      const newKeyword = newQuery.keyword || '';
      const newCategoryId = newQuery.categoryId || null;
      
      keyword.value = newKeyword;
      categoryId.value = newCategoryId;
      
      if (newKeyword || newCategoryId) {
        searchProducts(newKeyword, newCategoryId);
      } else {
        searchPerformed.value = false;
        selectedCategoryName.value = '';
        fetchAllProducts();
      }
    }, { immediate: true });

    onMounted(async () => {
      console.log('Page mounted');
      await fetchCategories();
      
      keyword.value = route.query.keyword || '';
      categoryId.value = route.query.categoryId || null;
      
      if (keyword.value || categoryId.value) {
        searchProducts(keyword.value, categoryId.value);
      } else {
        fetchAllProducts();
      }
    });

    return {
      sanPhams,
      loading,
      resultCount,
      keyword,
      categoryId,
      selectedCategoryName,
      searchPerformed,
      danhMucs,
      errorMessage,
      getImageUrl,
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