<template>
  <div class="customer-layout">
    <KH_Navbar />
    
    <main class="container mt-4">
      <div v-if="message" class="alert alert-success alert-dismissible fade show">
        <i class="fas fa-check-circle me-2"></i>
        {{ message }}
        <button type="button" class="btn-close" @click="message = ''"></button>
      </div>
          
      <div v-if="error" class="alert alert-danger alert-dismissible fade show">
        <i class="fas fa-exclamation-circle me-2"></i>
        {{ error }}
        <button type="button" class="btn-close" @click="error = ''"></button>
      </div>

      <!-- Thanh ngang hiển thị điểm -->
      <div class="row mb-4">
        <div class="col-12">
          <div class="points-card">
            <div class="points-display">
              <i class="fas fa-star points-icon ms-2"></i>
              <span class="points-label text-white">Điểm tích lũy</span>
              <span class="points-value">{{ customer.diemTichLuy || 0 }}</span>
            </div>
            <div class="points-actions">
              <button class="btn-points" @click="showHistoryModal = true">
                <i class="fas fa-history me-2"></i>Lịch sử điểm
              </button>
              <button class="btn-points me-2" @click="showVoucherModal = true">
                <i class="fas fa-ticket-alt me-2"></i>Đổi điểm
              </button>
            </div>
          </div>
        </div>
      </div>

      <div class="row">
        <!-- Thông tin cá nhân -->
        <div class="col-md-7">
          <div class="card shadow-sm mb-4">
            <div class="card-header d-flex justify-content-between align-items-center">
              <span><i class="fa-solid fa-user me-2"></i> Thông tin cá nhân</span>
            </div>
            <div class="card-body">
              <form @submit.prevent="updateProfile">
                <div class="row">
                  <div class="col-md-6">
                    <div class="mb-3">
                      <label class="form-label fw-bold">Tên đăng nhập</label>
                      <input type="text" class="form-control" v-model="user.userName" required 
                             @input="user.userName = user.userName.trimStart()">
                    </div>
                    <div class="mb-3">
                      <label class="form-label fw-bold">Họ và tên</label>
                      <input type="text" class="form-control" v-model="customer.tenKH" required
                             @input="customer.tenKH = customer.tenKH.trimStart()">
                    </div>
                  </div>
                  <div class="col-md-6">
                    <div class="mb-3">
                      <label class="form-label fw-bold">Mã giới thiệu của bạn</label>
                      <div class="input-group">
                        <input type="text" class="form-control" :value="customer.maGioiThieu" disabled>
                        <button class="btn btn-outline-primary" type="button" @click="copyReferralCode">
                          <i class="fas fa-copy"></i> Sao chép
                        </button>
                      </div>
                    </div>
                    <div class="mb-3">
                      <label class="form-label fw-bold">Số điện thoại</label>
                      <input type="text" class="form-control" v-model="customer.sdt" required 
                             pattern="[0-9]{9,11}" placeholder="090xxxxxxx"
                             @input="customer.sdt = customer.sdt.replace(/[^0-9]/g, '')">
                    </div>
                  </div>
                </div>
                <div class="row">
                  <div class="col-md-12 mb-3">
                    <label class="form-label fw-bold">Email</label>
                    <input type="email" class="form-control" :value="user.mail" disabled>
                  </div>
                </div>
                
                <div class="text-end d-flex justify-content-end gap-2">
                  <button v-if="!customer.hasAppliedReferral" 
                          class="btn btn-outline-primary" 
                          @click="showReferralModal = true">
                    <i class="fas fa-gift me-1"></i> Nhập mã giới thiệu
                  </button>
                  <button type="submit" class="btn btn-success" :disabled="profileLoading">
                    <span v-if="profileLoading" class="spinner-border spinner-border-sm me-1"></span>
                    <i v-else class="fas fa-save me-1"></i> Lưu thay đổi
                  </button>
                </div>
              </form>
            </div>
          </div>
        </div>
              
        <!-- Đổi mật khẩu -->
        <div class="col-md-5">
          <div class="card shadow-sm mb-4">
            <div class="card-header">
              <i class="fas fa-key me-2"></i> Đổi mật khẩu
            </div>
            <div class="card-body">
              <form @submit.prevent="changePassword">
                <div class="mb-3">
                  <label class="form-label fw-bold">Mật khẩu hiện tại</label>
                  <input type="password" class="form-control" v-model="password.currentPassword" required>
                </div>
                <div class="mb-3">
                  <label class="form-label fw-bold">Mật khẩu mới</label>
                  <input type="password" class="form-control" v-model="password.newPassword" required>
                </div>
                <div class="mb-3">
                  <label class="form-label fw-bold">Xác nhận mật khẩu mới</label>
                  <input type="password" class="form-control" v-model="password.confirmPassword" required>
                </div>

                <div class="text-end">
                  <button type="submit" class="btn btn-primary" :disabled="passwordLoading">
                    <span v-if="passwordLoading" class="spinner-border spinner-border-sm me-1"></span>
                    <i v-else class="fas fa-key me-1"></i> Đổi mật khẩu
                  </button>
                </div>
              </form>
            </div>
          </div>
        </div>
      </div>

      <!-- Quản lý địa chỉ -->
      <div class="card shadow-sm">
        <div class="card-header d-flex justify-content-between align-items-center">
          <span><i class="fa-solid fa-location-dot me-2"></i>Quản lý địa chỉ nhận hàng</span>
          <button class="btn btn-light btn-sm" @click="showAddModal = true">
            <i class="fas fa-plus me-1"></i> Thêm địa chỉ mới
          </button>
        </div>
        <div class="card-body">
          <div v-for="address in addresses" :key="address.maDC" 
               class="address-card mb-3" :class="{ 'default': address.macDinh }">
            <div class="d-flex justify-content-between align-items-center">
              <div>
                <div class="address-header">
                  <span>{{ address.tenNN }}</span> 
                  <span class="text-muted"> | {{ address.sdt }}</span>
                </div>
                <div>{{ address.diemGiao }}</div>
              </div>
              <div class="text-end">
                <button v-if="address.macDinh" class="btn btn-sm btn-outline-danger me-2" disabled>
                  <i class="fas fa-check"></i> Mặc định
                </button>
                <button v-else class="btn btn-sm btn-outline-success me-2" 
                        @click="setDefaultAddress(address.maDC)">
                  <i class="fas fa-check"></i> Đặt mặc định
                </button>
                <button class="btn btn-sm btn-outline-primary me-2" 
                        @click="editAddress(address)">
                  <i class="fas fa-edit"></i> Cập nhật
                </button>
                <button class="btn btn-sm btn-outline-danger" 
                        @click="deleteAddress(address.maDC)"
                        :disabled="address.macDinh">
                  <i class="fas fa-trash"></i> Xóa
                </button>
              </div>
            </div>
          </div>

          <div v-if="addresses.length === 0" class="text-center text-muted p-4">
            <i class="fas fa-map-marked-alt fa-3x mb-3"></i>
            <p>Chưa có địa chỉ nào. Thêm địa chỉ mới để dễ dàng đặt hàng!</p>
          </div>
        </div>
      </div>
    </main>

    <!-- Modal Lịch sử điểm -->
    <div v-if="showHistoryModal" class="modal fade show d-block" style="background: rgba(0,0,0,0.5)">
      <div class="modal-dialog modal-lg">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">
              Lịch sử tích điểm
            </h5>
            <button type="button" class="btn-close" @click="showHistoryModal = false"></button>
          </div>
          <div class="modal-body">
            <div class="current-points mb-3 p-3 rounded">
              <strong>Tổng điểm hiện tại: </strong> 
              <span class="text-yellow fw-bold">{{ pointsHistory.currentPoints || 0 }}</span>
            </div>
            
            <div class="filter-bar mb-3">
              <select v-model="historySortBy" class="form-select form-select-sm" style="width: auto;">
                <option value="newest">Mới nhất</option>
                <option value="oldest">Cũ nhất</option>
                <option value="high-low">Điểm cao - thấp</option>
                <option value="low-high">Điểm thấp - cao</option>
              </select>
            </div>
            
            <div v-if="filteredHistory.length > 0" class="history-list">
              <div v-for="item in filteredHistory" :key="item.ngayGiaoDich" 
                   class="history-item d-flex justify-content-between align-items-center border-bottom py-2">
                <div>
                  <div class="fw-bold">{{ item.loaiGiaoDich }}</div>
                  <div class="small text-muted">{{ formatDate(item.ngayGiaoDich) }}</div>
                </div>
                <div :class="item.soDiem > 0 ? 'text-success fw-bold' : 'text-danger fw-bold'">
                  {{ item.soDiem > 0 ? '+' : '' }}{{ item.soDiem }}
                </div>
              </div>
            </div>
            <div v-else class="text-center text-muted py-4">
              <i class="fas fa-chart-line fa-3x mb-3"></i>
              <p>Chưa có lịch sử giao dịch điểm</p>
            </div>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" @click="showHistoryModal = false">Hủy</button>
          </div>
        </div>
      </div>
    </div>

    <!-- Modal Nhập mã giới thiệu -->
    <div v-if="showReferralModal" class="modal fade show d-block" style="background: rgba(0,0,0,0.5)">
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">
              Nhập mã giới thiệu
            </h5>
            <button type="button" class="btn-close" @click="closeReferralModal"></button>
          </div>
          <div class="modal-body">
            <div class="mb-3">
              <label class="form-label fw-bold">Mã giới thiệu</label>
              <input type="text" class="form-control" v-model="referralCodeInput" 
                     autocomplete="off"
                     @keyup.enter="submitReferralCode">
              <small class="text-muted">Nhập mã giới thiệu của người đã giới thiệu bạn đến với ShoeDo</small>
            </div>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-primary" @click="submitReferralCode" 
                    :disabled="referralLoading">
              <span v-if="referralLoading" class="spinner-border spinner-border-sm me-1"></span> Xác nhận
            </button>
            <button type="button" class="btn btn-secondary" @click="closeReferralModal">Hủy</button>
          </div>
        </div>
      </div>
    </div>

    <!-- Modal Voucher -->
    <div v-if="showVoucherModal" class="modal fade show d-block" style="background: rgba(0,0,0,0.5)">
      <div class="modal-dialog modal-lg">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">
              Quản lý voucher
            </h5>
            <button type="button" class="btn-close" @click="closeVoucherModal"></button>
          </div>
          <div class="modal-body">
            <ul class="nav nav-tabs mb-2">
              <li class="nav-item">
                <a class="nav-link" :class="{ active: activeTab === 'myVouchers' }" 
                   @click="activeTab = 'myVouchers'">
                  <i class="fas fa-gift me-1"></i> Voucher của tôi
                </a>
              </li>
              <li class="nav-item">
                <a class="nav-link" :class="{ active: activeTab === 'available' }" 
                   @click="activeTab = 'available'; loadAvailableVouchers()">
                  <i class="fas fa-store me-1"></i> Đổi điểm lấy voucher
                </a>
              </li>
            </ul>

            <!-- Tab 1: Voucher của tôi -->
            <div v-if="activeTab === 'myVouchers'">
              <div class="filter-bar mb-2 d-flex gap-2">
                <select v-model="myVoucherStatusFilter" class="form-select form-select-sm" style="width: auto;">
                  <option value="all">Tất cả trạng thái</option>
                  <option value="Chưa sử dụng">Chưa sử dụng</option>
                  <option value="Đã sử dụng">Đã sử dụng</option>
                  <option value="Hết hạn">Hết hạn</option>
                </select>
                <select v-model="myVoucherSortBy" class="form-select form-select-sm" style="width: auto;">
                  <option value="newest">Mới nhất</option>
                  <option value="oldest">Cũ nhất</option>
                  <option value="high-low">Giá trị cao - thấp</option>
                  <option value="low-high">Giá trị thấp - cao</option>
                </select>
              </div>
              
              <div v-if="filteredMyVouchers.length > 0" class="voucher-list">
                <div v-for="item in filteredMyVouchers" :key="item.maKHVC" 
                     class="voucher-card mb-2 p-3 border rounded">
                  <div class="row align-items-center">
                    <div class="col-md-8">
                      <div class="fw-bold fs-5">{{ item.voucher.tenVoucher }}</div>
                      <div class="text-muted small">Giảm {{ formatMoney(item.voucher.giaTriGiam) }}</div>
                      <div class="text-muted small">Đơn tối thiểu: {{ formatMoney(item.voucher.donToiThieu) }}</div>
                      <div class="text-muted small">Hạn sử dụng: {{ formatDate(item.hanSuDung) }}</div>
                    </div>
                    <div class="col-md-4 text-end">
                      <span class="badge" :class="getStatusBadgeClass(item.trangThai)">
                        {{ item.trangThai }}
                      </span>
                    </div>
                  </div>
                </div>
              </div>
              <div v-else class="text-center text-muted py-4">
                <i class="fas fa-ticket-alt fa-3x mb-3"></i>
                <p>Bạn chưa có voucher nào</p>
              </div>
            </div>

            <!-- Tab 2: Đổi điểm lấy voucher -->
            <div v-if="activeTab === 'available'">
              <div class="filter-bar mb-2 d-flex gap-2">
                <select v-model="availableVoucherFilter" class="form-select form-select-sm" style="width: auto;">
                  <option value="all">Tất cả voucher</option>
                  <option value="enough">Đủ điểm</option>
                  <option value="not-enough">Không đủ điểm</option>
                </select>
                <select v-model="availableVoucherSortBy" class="form-select form-select-sm" style="width: auto;">
                  <option value="newest">Mới nhất (HSD)</option>
                  <option value="oldest">Cũ nhất (HSD)</option>
                  <option value="discount-high-low">Giảm giá cao - thấp</option>
                  <option value="discount-low-high">Giảm giá thấp - cao</option>
                  <option value="points-high-low">Điểm cần cao - thấp</option>
                  <option value="points-low-high">Điểm cần thấp - cao</option>
                </select>
              </div>
              
              <div v-if="filteredAvailableVouchers.length > 0" class="voucherdoi-list">
                <div class="row">
                  <div v-for="voucher in filteredAvailableVouchers" :key="voucher.maVoucher" class="col-md-6 mb-2">
                    <div class="voucher-card p-3 border rounded h-100">
                      <div class="fw-bold fs-6">{{ voucher.tenVoucher }}</div>
                      <div class="text-danger fw-bold">-{{ formatMoney(voucher.giaTriGiam) }}</div>
                      <div class="small text-muted">Đơn tối thiểu: {{ formatMoney(voucher.donToiThieu) }}</div>
                      <div class="small text-muted">Cần: {{ voucher.diemCanDoi }} điểm</div>
                      <div class="small text-muted">Còn: {{ voucher.soLuong }} voucher</div>
                      <div class="small text-muted">
                        HSD: {{ formatDateShort(voucher.ngayBatDau) }} - {{ formatDateShort(voucher.ngayKetThuc) }}
                      </div>
                      <button class="btn btn-primary btn-sm mt-2 w-100" 
                              @click="redeemVoucher(voucher.maVoucher)"
                              :disabled="redeemLoading || customer.diemTichLuy < voucher.diemCanDoi || voucher.soLuong <= 0">
                        <span v-if="redeemLoading && selectedVoucher === voucher.maVoucher" class="spinner-border spinner-border-sm me-1"></span>
                        <i v-else class="fas fa-exchange-alt me-1"></i>
                        Đổi voucher
                      </button>
                    </div>
                  </div>
                </div>
              </div>
              <div v-if="filteredAvailableVouchers.length === 0" class="text-center text-muted py-4">
                <i class="fas fa-search fa-3x mb-3"></i>
                <p>Không có voucher nào phù hợp với bộ lọc</p>
              </div>
            </div>
          </div>
          <div class="modal-footer d-flex justify-content-between align-items-center">
            <div>
              <strong>Điểm hiện có: </strong> 
              <span class="text-yellow fw-bold">{{ customer.diemTichLuy || 0 }}</span>
            </div>
            <button type="button" class="btn btn-secondary" @click="closeVoucherModal">Hủy</button>
          </div>
        </div>
      </div>
    </div>

    <!-- Modal Thêm/Sửa Địa Chỉ -->
    <div v-if="showAddModal || editingAddress" class="modal fade show d-block" style="background: rgba(0,0,0,0.5)">
      <div class="modal-dialog">
        <div class="modal-content">
          <form @submit.prevent="saveAddress">
            <div class="modal-header">
              <h5 class="modal-title">{{ editingAddress ? 'Cập nhật địa chỉ' : 'Thêm địa chỉ mới' }}</h5>
              <button type="button" class="btn-close" @click="closeModal"></button>
            </div>
            <div class="modal-body">
              <div class="mb-3">
                <label class="form-label">Họ và tên người nhận</label>
                <input type="text" class="form-control" v-model="addressForm.tenNN" required
                       @input="addressForm.tenNN = addressForm.tenNN.trimStart()">
              </div>
              <div class="mb-3">
                <label class="form-label">Số điện thoại</label>
                <input type="text" class="form-control" v-model="addressForm.sdt" required pattern="[0-9]{9,11}"
                       @input="addressForm.sdt = addressForm.sdt.replace(/[^0-9]/g, '')">
                <small class="text-muted">9-11 số</small>
              </div>
              <div class="mb-3">
                <label class="form-label">Địa chỉ</label>
                <textarea class="form-control" v-model="addressForm.diemGiao" rows="3" required
                          @input="addressForm.diemGiao = addressForm.diemGiao.trimStart()"></textarea>
              </div>
              <div class="form-check">
                <input class="form-check-input" type="checkbox" v-model="addressForm.macDinh">
                <label class="form-check-label">
                  Đặt làm địa chỉ mặc định
                </label>
              </div>
            </div>
            <div class="modal-footer">
              <button type="submit" class="btn btn-primary" :disabled="addressLoading">
                <span v-if="addressLoading" class="spinner-border spinner-border-sm me-1"></span>
                {{ editingAddress ? 'Cập nhật' : 'Thêm địa chỉ' }}
              </button>
              <button type="button" class="btn btn-secondary" @click="closeModal">Hủy</button>
            </div>
          </form>
        </div>
      </div>
    </div>
    <Footer />
  </div>
</template>

<script>
import { ref, onMounted, computed } from 'vue'
import axios from 'axios'
import KH_Navbar from '@/components/shared/KH_Navbar.vue'
import Footer from '@/components/shared/Footer.vue'

export default {
  name: 'QLProfile',
  components: { KH_Navbar, Footer },
  setup() {
    const user = ref({ userName: '', mail: '', createAt: '' })
    const customer = ref({ tenKH: '', sdt: '', diemTichLuy: 0, maGioiThieu: '', hasAppliedReferral: false })
    const addresses = ref([])
    const password = ref({ currentPassword: '', newPassword: '', confirmPassword: '' })
    const referralCodeInput = ref('')
    
    const loading = ref({ profile: false, password: false, address: false, referral: false })
    const message = ref('')
    const error = ref('')
    const showAddModal = ref(false)
    const editingAddress = ref(null)
    const addressForm = ref({ maDC: null, tenNN: '', sdt: '', diemGiao: '', macDinh: false })
    
    // Modal states
    const showHistoryModal = ref(false)
    const showVoucherModal = ref(false)
    const showReferralModal = ref(false)
    const activeTab = ref('myVouchers')
    const pointsHistory = ref({ history: [], currentPoints: 0 })
    const myVouchers = ref([])
    const availableVouchers = ref([])
    const redeemLoading = ref(false)
    const selectedVoucher = ref(null)
    
    // Filter states
    const historySortBy = ref('newest')
    const myVoucherStatusFilter = ref('all')
    const myVoucherSortBy = ref('newest')
    const availableVoucherFilter = ref('all')
    const availableVoucherSortBy = ref('newest')

    const filteredHistory = computed(() => {
      let history = [...(pointsHistory.value.history || [])]

      switch (historySortBy.value) {
        case 'newest':
          history.sort((a, b) => new Date(b.ngayGiaoDich) - new Date(a.ngayGiaoDich))
          break
        case 'oldest':
          history.sort((a, b) => new Date(a.ngayGiaoDich) - new Date(b.ngayGiaoDich))
          break
        case 'high-low':
          history.sort((a, b) => b.soDiem - a.soDiem)
          break
        case 'low-high':
          history.sort((a, b) => a.soDiem - b.soDiem)
          break
      }
      
      return history
    })

    const filteredMyVouchers = computed(() => {
      let vouchers = [...(myVouchers.value || [])]

      if (myVoucherStatusFilter.value !== 'all') {
        vouchers = vouchers.filter(v => v.trangThai === myVoucherStatusFilter.value)
      }

      switch (myVoucherSortBy.value) {
        case 'newest':
          vouchers.sort((a, b) => new Date(b.ngayDoi) - new Date(a.ngayDoi))
          break
        case 'oldest':
          vouchers.sort((a, b) => new Date(a.ngayDoi) - new Date(b.ngayDoi))
          break
        case 'high-low':
          vouchers.sort((a, b) => (b.voucher?.giaTriGiam || 0) - (a.voucher?.giaTriGiam || 0))
          break
        case 'low-high':
          vouchers.sort((a, b) => (a.voucher?.giaTriGiam || 0) - (b.voucher?.giaTriGiam || 0))
          break
      }
      
      return vouchers
    })

    const filteredAvailableVouchers = computed(() => {
      let vouchers = [...(availableVouchers.value || [])]
      const currentPoints = customer.value.diemTichLuy || 0

      switch (availableVoucherFilter.value) {
        case 'enough':
          vouchers = vouchers.filter(v => v.diemCanDoi <= currentPoints)
          break
        case 'not-enough':
          vouchers = vouchers.filter(v => v.diemCanDoi > currentPoints)
          break
      }

      switch (availableVoucherSortBy.value) {
        case 'newest':
          vouchers.sort((a, b) => new Date(b.ngayBatDau) - new Date(a.ngayBatDau))
          break
        case 'oldest':
          vouchers.sort((a, b) => new Date(a.ngayBatDau) - new Date(b.ngayBatDau))
          break
        case 'discount-high-low':
          vouchers.sort((a, b) => (b.giaTriGiam || 0) - (a.giaTriGiam || 0))
          break
        case 'discount-low-high':
          vouchers.sort((a, b) => (a.giaTriGiam || 0) - (b.giaTriGiam || 0))
          break
        case 'points-high-low':
          vouchers.sort((a, b) => b.diemCanDoi - a.diemCanDoi)
          break
        case 'points-low-high':
          vouchers.sort((a, b) => a.diemCanDoi - b.diemCanDoi)
          break
      }
      
      return vouchers
    })

    const fetchProfile = async () => {
      try {
        const { data } = await axios.get('/api/customer/profile')
        console.log('Profile data:', data)
        if (data.success) {
          user.value = data.user
          customer.value = data.customer
          addresses.value = (data.addresses || []).sort((a, b) => 
            b.macDinh - a.macDinh
          )
        }
      } catch (err) {
        error.value = 'Không thể tải thông tin'
      }
    }

    const fetchPointsHistory = async () => {
      try {
        const { data } = await axios.get('/api/customer/points-history')
        if (data.success) {
          pointsHistory.value = data
        }
      } catch (err) {
        console.error('Lỗi tải lịch sử điểm:', err)
      }
    }

    const fetchMyVouchers = async () => {
      try {
        const { data } = await axios.get('/api/customer/vouchers/my-vouchers')
        if (data.success) {
          myVouchers.value = data.vouchers
        }
      } catch (err) {
        console.error('Lỗi tải voucher:', err)
      }
    }

    const loadAvailableVouchers = async () => {
      try {
        const { data } = await axios.get('/api/customer/vouchers/available')
        if (data.success) {
          availableVouchers.value = data.vouchers || []
          if (availableVouchers.value.length === 0) {
            error.value = ''
          }
        } else {
          console.error('Không thể tải voucher:', data.message)
          error.value = data.message
          availableVouchers.value = []
        }
      } catch (err) {
        error.value = err.response?.data?.message || 'Không thể tải danh sách voucher'
        availableVouchers.value = []
      }
    }

    const redeemVoucher = async (maVoucher) => {
      if (!confirm('Bạn có chắc muốn dùng điểm để đổi voucher này?')) return
      
      redeemLoading.value = true
      selectedVoucher.value = maVoucher
      
      try {
        const { data } = await axios.post('/api/customer/vouchers/redeem', { maVoucher })
        if (data.success) {
          message.value = data.message
          customer.value.diemTichLuy = data.remainingPoints
          await fetchMyVouchers()
          await loadAvailableVouchers()
          await fetchPointsHistory()
        } else {
          error.value = data.message
        }
      } catch (err) {
        error.value = err.response?.data?.message || 'Lỗi khi đổi voucher'
      } finally {
        redeemLoading.value = false
        selectedVoucher.value = null
      }
    }

    const submitReferralCode = async () => {
      if (!referralCodeInput.value.trim()) {
        error.value = 'Vui lòng nhập mã giới thiệu'
        return
      }
      
      loading.value.referral = true
      clearMessages()
      
      try {
        const { data } = await axios.post('/api/customer/apply-referral', {
          referralCode: referralCodeInput.value.trim()
        })
        
        if (data.success) {
          message.value = data.message
          customer.value.diemTichLuy = data.newPoints
          customer.value.hasAppliedReferral = true
          referralCodeInput.value = ''
          await fetchProfile()
          await fetchPointsHistory()
          closeReferralModal()
        } else {
          error.value = data.message
        }
      } catch (err) {
        error.value = err.response?.data?.message || 'Lỗi khi nhập mã giới thiệu'
      } finally {
        loading.value.referral = false
      }
    }

    const copyReferralCode = () => {
      const code = customer.value.maGioiThieu
      if (code) {
        navigator.clipboard.writeText(code)
        message.value = 'Đã sao chép mã giới thiệu: ' + code
        setTimeout(() => {
          if (message.value) message.value = ''
        }, 3000)
      } else {
        error.value = 'Không có mã giới thiệu'
      }
    }

    const updateProfile = async () => {
      if (!validateProfile()) return
      
      loading.value.profile = true
      clearMessages()
      
      try {
        const { data } = await axios.put('/api/customer/profile', {
          userName: user.value.userName.trim(),
          fullname: customer.value.tenKH.trim(),
          phone: customer.value.sdt.trim()
        })
        
        if (data.success) {
          message.value = data.message
          if (data.user) user.value.userName = data.user.userName
          if (data.customer) {
            customer.value.tenKH = data.customer.tenKH
            customer.value.sdt = data.customer.sdt
          }
        } else error.value = data.message
      } catch (err) {
        error.value = err.response?.data?.message || 'Lỗi kết nối'
      } finally {
        loading.value.profile = false
      }
    }

    const changePassword = async () => {
      if (!validatePassword()) return
      
      loading.value.password = true
      clearMessages()
      
      try {
        const { data } = await axios.put('/api/customer/change-password', password.value)
        
        if (data.success) {
          message.value = data.message
          password.value = { currentPassword: '', newPassword: '', confirmPassword: '' }
        } else error.value = data.message
      } catch (err) {
        error.value = err.response?.data?.message || 'Mật khẩu hiện tại không đúng'
      } finally {
        loading.value.password = false
      }
    }

    const saveAddress = async () => {
      if (!validateAddress()) return
      
      loading.value.address = true
      clearMessages()
      
      try {
        const url = editingAddress.value 
          ? `/api/customer/address/${addressForm.value.maDC}`
          : '/api/customer/address'
        
        const method = editingAddress.value ? 'put' : 'post'
        const { data } = await axios[method](url, addressForm.value)
        
        if (data.success) {
          message.value = data.message
          await fetchProfile()
          closeModal()
        } else error.value = data.message
      } catch (err) {
        error.value = err.response?.data?.message || 'Lỗi kết nối'
      } finally {
        loading.value.address = false
      }
    }

    const deleteAddress = async (maDC) => {
      if (!confirm('Bạn có chắc muốn xóa địa chỉ này?')) return
      
      try {
        const { data } = await axios.delete(`/api/customer/address/${maDC}`)
        if (data.success) {
          message.value = data.message
          await fetchProfile()
        } else error.value = data.message
      } catch (err) {
        error.value = err.response?.data?.message || 'Lỗi kết nối'
      }
    }

    const setDefaultAddress = async (maDC) => {
      try {
        const { data } = await axios.post(`/api/customer/address/${maDC}/set-default`)
        if (data.success) {
          message.value = data.message
          await fetchProfile()
        } else error.value = data.message
      } catch (err) {
        error.value = err.response?.data?.message || 'Lỗi kết nối'
      }
    }

    const closeVoucherModal = () => {
      showVoucherModal.value = false
      activeTab.value = 'myVouchers'
      // Reset filters
      myVoucherStatusFilter.value = 'all'
      myVoucherSortBy.value = 'newest'
      availableVoucherFilter.value = 'all'
      availableVoucherSortBy.value = 'newest'
    }

    const closeReferralModal = () => {
      showReferralModal.value = false
      referralCodeInput.value = ''
    }

    // Helper functions
    const validateProfile = () => {
      const { userName, tenKH, sdt } = { 
        userName: user.value.userName.trim(), 
        tenKH: customer.value.tenKH.trim(), 
        sdt: customer.value.sdt.trim() 
      }
      
      if (!/^\d{9,11}$/.test(sdt)) return setError('Số điện thoại không hợp lệ')
      if (userName.length < 3) return setError('Tên đăng nhập phải có ít nhất 3 ký tự')
      if (!tenKH) return setError('Họ và tên không được để trống')
      return true
    }

    const validatePassword = () => {
      if (password.value.newPassword !== password.value.confirmPassword) 
        return setError('Mật khẩu mới không khớp')
      if (password.value.currentPassword === password.value.newPassword) 
        return setError('Mật khẩu mới không được trùng với mật khẩu hiện tại')
      return true
    }

    const validateAddress = () => {
      const { tenNN, sdt } = addressForm.value
      if (!tenNN.trim()) return setError('Vui lòng nhập tên người nhận')
      if (!/^\d{9,11}$/.test(sdt.trim())) return setError('Số điện thoại không hợp lệ')
      return true
    }

    const formatMoney = (amount) => {
      if (!amount) return '0₫'
      return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(amount)
    }

    const formatDate = (dateString) => {
      if (!dateString) return ''
      const date = new Date(dateString)
      return date.toLocaleDateString('vi-VN')
    }

    const formatDateShort = (dateString) => {
      if (!dateString) return ''
      const date = new Date(dateString)
      return `${date.getDate()}/${date.getMonth() + 1}/${date.getFullYear()}`
    }

    const getStatusBadgeClass = (status) => {
      switch (status) {
        case 'Chưa sử dụng': return 'bg-success'
        case 'Đã sử dụng': return 'bg-secondary'
        case 'Hết hạn': return 'bg-danger'
        default: return 'bg-secondary'
      }
    }

    const setError = (msg) => { error.value = msg; return false }
    const clearMessages = () => { message.value = ''; error.value = '' }
    
    const editAddress = (address) => {
      editingAddress.value = address
      addressForm.value = { ...address }
      showAddModal.value = true
    }

    const closeModal = () => {
      showAddModal.value = false
      editingAddress.value = null
      addressForm.value = { maDC: null, tenNN: '', sdt: '', diemGiao: '', macDinh: false }
    }

    onMounted(async () => {
      await fetchProfile()
      await fetchPointsHistory()
      await fetchMyVouchers()
    })

    return {
      user, customer, addresses, password,
      loading, message, error, showAddModal, editingAddress, addressForm,
      showHistoryModal, showVoucherModal, showReferralModal, activeTab, pointsHistory, 
      myVouchers, availableVouchers, redeemLoading, selectedVoucher,
      referralCodeInput,
      // Filters
      historySortBy, myVoucherStatusFilter, myVoucherSortBy, 
      availableVoucherFilter, availableVoucherSortBy,
      filteredHistory, filteredMyVouchers, filteredAvailableVouchers,
      // Methods
      updateProfile, changePassword, saveAddress, deleteAddress,
      setDefaultAddress, editAddress, closeModal,
      fetchPointsHistory, fetchMyVouchers, loadAvailableVouchers, 
      redeemVoucher, submitReferralCode, copyReferralCode,
      formatMoney, formatDate, formatDateShort, getStatusBadgeClass,
      closeVoucherModal, closeReferralModal
    }
  }
}
</script>

<style scoped>
.card-header {
  font-weight: bold;
  background-color: #000000;
  color: #ffffff;
  border-bottom: 2px solid #000000;
}

.address-card {
  border: 2px solid #000000;
  border-radius: 10px;
  padding: 15px;
  background-color: #ffffff;
  transition: 0.2s;
}

.address-card:hover {
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
}

.address-card.default {
  border: 2px solid #dc3545;
  background-color: #f8f9fa;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
}

.address-header {
  font-weight: bold;
  font-size: 1.1rem;
}

.btn-primary, .btn-success {
  background-color: #000000;
  border-color: #000000;
}

.btn-primary:hover, .btn-success:hover {
  background-color: #333333;
  border-color: #333333;
}

.btn-secondary {
  background-color: #ffffff;
  border-color: #000000;
  color: #000000;
}
.btn-secondary:hover {
  background-color: #f0f0f0;
  border-color: #000000;
  color: #000000;
}

.btn-outline-primary {
  border-color: #000000;
  color: #000000;
}

.btn-outline-primary:hover {
  background-color: #000000;
  border-color: #000000;
  color: #ffffff;
}

.btn-outline-success {
  border-color: #28a745;
  color: #28a745;
}

.btn-outline-success:hover {
  background-color: #28a745;
  border-color: #28a745;
  color: #ffffff;
}

.btn-outline-danger {
  border-color: #dc3545;
  color: #dc3545;
}

.btn-outline-danger:hover:not(:disabled) {
  background-color: #dc3545;
  border-color: #dc3545;
  color: #ffffff;
}

.btn-outline-danger:disabled {
  border-color: #6c757d;
  color: #6c757d;
  cursor: not-allowed;
  opacity: 0.5;
}

.form-control {
  border: 1px solid #000000;
}

.form-control:focus {
  border-color: #000000;
  box-shadow: 0 0 0 0.2rem rgba(0, 0, 0, 0.25);
}

.form-control:disabled {
  background-color: #f8f9fa;
  cursor: not-allowed;
}

.modal-content {
  border: 2px solid #000000;
  border-radius: 10px;
}

.modal-header {
  background-color: #000000;
  color: #ffffff;
  border-bottom: 2px solid #000000;
}

.modal-header .btn-close {
  filter: invert(1) grayscale(100%) brightness(200%);
}

.modal-footer {
  border-top: 1px solid #000000;  
}

.points-card {
  background: black;
  border-radius: 15px;
  padding: 12px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
}

.points-display {
  display: flex;
  align-items: center;
  gap: 15px;
}

.points-icon {
  font-size: 2rem;
  color: #ffc107;
}

.points-label {
  font-size: 1.1rem;
  font-weight: 600;
  color: #ffffff;
}

.points-value {
  font-size: 1.8rem;
  font-weight: bold;
  color: #ffc107;
  background: #ffffff;
  padding: 3px 15px;
  border-radius: 25px;
  text-shadow: 2px 2px 3px rgba(0,0,0,0.4);
}

.points-actions {
  display: flex;
  gap: 10px;
}

.btn-points {
  background: white;
  border: none;
  padding: 10px 20px;
  border-radius: 25px;
  font-weight: 500;
  transition: all 0.3s ease;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
}

.btn-points:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.15);
  background: #f8f9fa;
}

.history-list {
  max-height: 380px;
  overflow-y: auto;
}

.history-item {
  transition: background 0.2s;
  padding: 10px 20px;
}

.history-item:hover {
  background: #f8f9fa;
  transform: translateY(-2px);
}

.voucher-list {
  max-height: 418px;
  overflow-y: auto;
}

.voucherdoi-list {
  max-height: 418px;
  overflow-y: auto;
  overflow-x: hidden;
}

.voucher-card {
  transition: transform 0.2s, box-shadow 0.2s;
}

.voucher-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.current-points {
  background: black;
  color: white;
  border-radius: 10px;
}

.current-points strong {
  color: white;
}

.nav-tabs .nav-link {
  color: #333;
  cursor: pointer;
}

.nav-tabs .nav-link.active {
  color: #000000;
  font-weight: bold;
  border-bottom: 2px solid #000000;
}

.nav-tabs .nav-link:hover:not(.active) {
  border-color: #dee2e6 #dee2e6 #ddd;
}

.text-yellow {
  color: #ffc107;
}

.filter-bar {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.filter-bar .form-select {
  min-width: 140px;
}
</style>