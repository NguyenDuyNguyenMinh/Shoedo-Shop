<template>
  <div class="customer-layout">
    <KH_Navbar />
    
    <div class="container">
      <h2 class="mt-4 mb-3">Giỏ Hàng Của Bạn</h2>

      <div class="card d-flex flex-row p-3 mb-3 justify-content-between align-items-center fw-bold">
        <div class="col-sm-6 d-flex align-items-center">
          <div class="form-check me-2">
            <input class="form-check-input" type="checkbox" id="selectAllCheckbox" v-model="selectAll" @change="toggleSelectAll">
          </div>
          Sản Phẩm
        </div>
        <div class="col-sm-6 d-flex align-items-center">
          <div class="col-sm-3 text-center">Đơn Giá</div>
          <div class="col-sm-3 text-center">Số Lượng</div>
          <div class="col-sm-3 text-center">Số Tiền</div>
          <div class="col-sm-3 text-center">Thao Tác</div>
        </div>
      </div>

      <div v-if="loading" class="text-center p-5">
        <div class="spinner-border text-primary" role="status">
          <span class="visually-hidden">Loading...</span>
        </div>
      </div>

      <div v-else-if="cartItems.length === 0" class="card p-5 m-2 text-center">
        <h3>Giỏ hàng của bạn đang trống.</h3>
        <router-link to="/customer/index" class="btn btn-primary mt-3">Tiếp tục mua sắm</router-link>
      </div>

      <div v-else>
        <div v-for="item in cartItems" :key="item.maGH" class="card p-3 mb-3">
          <div class="row align-items-center">
            <div class="col-sm-6 d-flex align-items-center" style="height: 70px">
              <div class="form-check me-2">
                <input class="form-check-input" type="checkbox" v-model="selectedItems" :value="item.maGH">
              </div>
              <div class="d-flex align-items-center flex-grow-1" >
                <img :src="getImageUrl(item.sanPham.hinh)" 
                    :alt="item.sanPham.tenSP"
                    style="max-width: 70px;">
                <div class="ms-2 flex-grow-1">
                  <p class="mb-0"><strong>{{ item.sanPham.tenSP }}</strong></p>
                  <small class="text-muted" v-if="item.plSanPham && item.plSanPham.phanLoai">
                    Phân loại: <span class="">{{ item.plSanPham.phanLoai }}</span>
                  </small>
                </div>
              </div>
            </div>

            <div class="col-sm-6 d-flex align-items-center">
              <div class="col-sm-3 text-center">
                {{ formatPrice(item.plSanPham ? item.plSanPham.donGia : 0) }}
              </div>
              <div class="col-sm-3 text-center">
                <div class="input-group input-group-sm" style="width: 130px; margin: auto;">
                  <button class="btn btn-outline-secondary" type="button" 
                          @click="decreaseQuantity(item)" 
                          :disabled="item.soLuong <= 1">-</button>
                  <input class="form-control text-center" type="number" 
                         :value="item.soLuong" 
                         min="1" 
                         :max="item.plSanPham ? item.plSanPham.soLuong : 1"
                         @change="(e) => validateAndUpdateQuantity(item, e.target.value)" />
                  <button class="btn btn-outline-secondary" type="button" 
                          @click="increaseQuantity(item)"
                          :disabled="!canIncreaseQuantity(item)">+</button>
                </div>
               
              </div>
              <div class="col-sm-3 text-center">
                {{ formatPrice(calculateItemTotal(item)) }}
              </div>
              <div class="col-sm-3 text-center">
                <button class="btn btn-danger" @click="removeFromCart(item.maGH)">Bỏ Sản Phẩm</button>
              </div>
            </div>
          </div>
        </div>

        <div class="card d-flex flex-row p-3 justify-content-between align-items-center">
          <button type="button" class="btn btn-secondary" @click="deselectAll">Bỏ chọn tất cả</button>
          <div class="d-flex flex-row align-items-center">
            <div class="me-3">
              <span>Tổng Cộng (<span>{{ selectedItems.length }}</span> Sản Phẩm): </span>
              <span class="fw-bold">{{ formatPrice(selectedTotal) }}</span>
            </div>
            <button type="button" class="btn btn-success" @click="checkout" :disabled="selectedItems.length === 0">
              Đặt Hàng
            </button>
          </div>
        </div>
      </div>
    </div>
    <Footer />
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import api from '@/services/api';
import KH_Navbar from '@/components/shared/KH_Navbar.vue';
import Footer from '@/components/shared/Footer.vue';

export default {
  name: 'GioHang',
  components: {
    KH_Navbar,
    Footer 
  },
  setup() {
    const router = useRouter();
    const cartItems = ref([]);
    const selectedItems = ref([]);
    const selectAll = ref(false);
    const loading = ref(false);

    const getImageUrl = (imageName) => {
      return `http://localhost:8080/images/${imageName || 'default.jpg'}`;
    };

    const formatPrice = (price) => {
      return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND'
      }).format(price || 0);
    };

    const fetchCart = async () => {
      loading.value = true;
      try {
        const response = await api.getCart();
        if (response.data.success) {
          cartItems.value = response.data.cartItems || [];
        }
      } catch (error) {
        console.error('Error fetching cart:', error);
      } finally {
        loading.value = false;
      }
    };

    const canIncreaseQuantity = (item) => {
      if (!item.plSanPham) return false;
      return item.soLuong < item.plSanPham.soLuong;
    };

    const showStockWarning = (item) => {
      if (!item.plSanPham) return false;
      return item.soLuong >= item.plSanPham.soLuong;
    };

    const calculateItemTotal = (item) => {
      if (!item.plSanPham) return 0;
      return item.plSanPham.donGia * item.soLuong;
    };

    const decreaseQuantity = async (item) => {
      if (item.soLuong <= 1) return;
      await updateQuantity(item.maGH, item.soLuong - 1);
    };


    const increaseQuantity = async (item) => {
      if (!canIncreaseQuantity(item)) return;
      await updateQuantity(item.maGH, item.soLuong + 1);
    };

    const validateAndUpdateQuantity = async (item, newQuantity) => {
      newQuantity = parseInt(newQuantity);
      
      if (isNaN(newQuantity) || newQuantity < 1) {
        newQuantity = 1;
      }
      
      if (item.plSanPham && newQuantity > item.plSanPham.soLuong) {
        alert(`Số lượng vượt quá tồn kho. Chỉ còn ${item.plSanPham.soLuong} sản phẩm.`);
        newQuantity = item.plSanPham.soLuong;
      }
      
      await updateQuantity(item.maGH, newQuantity);
    };

    const updateQuantity = async (maGH, newQuantity) => {
      if (newQuantity < 1) return;
      
      try {
        const response = await api.updateCartItem(maGH, newQuantity);
        
        if (response.data.success) {
          const itemIndex = cartItems.value.findIndex(item => item.maGH === maGH);
          if (itemIndex !== -1) {
            cartItems.value[itemIndex].soLuong = newQuantity;
          }
        } else {
          alert(response.data.message);
          await fetchCart();
        }
      } catch (error) {
        console.error('Error updating quantity:', error);
        if (error.response && error.response.data) {
          alert(error.response.data.message || 'Có lỗi xảy ra khi cập nhật số lượng');
        }
      }
    };

    const removeFromCart = async (maGH) => {
      if (!confirm('Bạn có chắc muốn xóa sản phẩm này?')) return;
      
      try {
        await api.removeFromCart(maGH);
        cartItems.value = cartItems.value.filter(item => item.maGH !== maGH);
        selectedItems.value = selectedItems.value.filter(id => id !== maGH);
      } catch (error) {
        console.error('Error removing from cart:', error);
      }
    };

    const toggleSelectAll = () => {
      if (selectAll.value) {
        selectedItems.value = cartItems.value.map(item => item.maGH);
      } else {
        selectedItems.value = [];
      }
    };

    const deselectAll = () => {
      selectedItems.value = [];
      selectAll.value = false;
    };

    const selectedTotal = computed(() => {
      return selectedItems.value.reduce((total, maGH) => {
        const item = cartItems.value.find(i => i.maGH === maGH);
        return total + calculateItemTotal(item);
      }, 0);
    });


    const checkout = () => {
      if (selectedItems.value.length === 0) {
        alert('Vui lòng chọn ít nhất một sản phẩm để đặt hàng');
        return;
      }

      const selectedCartItems = cartItems.value
        .filter(item => selectedItems.value.includes(item.maGH))
        .map(item => ({
          maGH: item.maGH,
          sanPham: item.sanPham,
          plSanPham: item.plSanPham,
          soLuong: item.soLuong,
          donGia: item.plSanPham?.donGia || 0
        }));
      
      localStorage.setItem('selectedCartItems', JSON.stringify(selectedCartItems));
      router.push('/customer/checkout');
    };

    onMounted(() => {
      fetchCart();
    });

    return {
      cartItems,
      selectedItems,
      selectAll,
      loading,
      getImageUrl,
      formatPrice,
      updateQuantity,
      removeFromCart,
      toggleSelectAll,
      deselectAll,
      selectedTotal,
      checkout,
      decreaseQuantity,
      increaseQuantity,
      canIncreaseQuantity,
      showStockWarning,
      calculateItemTotal,
      validateAndUpdateQuantity
    };
  }
};
</script>

<style scoped>
.card { 
  border: 2px solid #000000; 
  border-radius: 10px; 
}

</style>