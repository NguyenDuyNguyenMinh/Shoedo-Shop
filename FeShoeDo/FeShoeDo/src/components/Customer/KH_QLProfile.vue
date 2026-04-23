<template>
  <div class="customer-layout">
    <Toast />
    <KH_Navbar />
    
    <main class="container mt-4">

      <!-- Thanh ngang hiển thị điểm -->
      <div class="row mb-4">
        <div class="col-12">
          <div class="points-card animate-slide-down">
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
          <div class="card shadow-sm mb-4 animate-fade-in" style="animation-delay: 0.1s">
            <div class="card-header d-flex justify-content-between align-items-center">
              <span><i class="fa-solid fa-user me-2"></i> Thông tin cá nhân</span>
            </div>
            <div class="card-body">
              <form @submit.prevent="updateProfile">
                <div class="row">
                  <div class="col-md-6">
                    <div class="mb-3 animate-slide-right" style="animation-delay: 0.15s">
                      <label class="form-label fw-bold">Tên đăng nhập</label>
                      <input type="text" class="form-control" v-model="user.userName" required 
                             @input="user.userName = user.userName.trimStart()">
                    </div>
                    <div class="mb-3 animate-slide-right" style="animation-delay: 0.2s">
                      <label class="form-label fw-bold">Họ và tên</label>
                      <input type="text" class="form-control" v-model="customer.tenKH" required
                             @input="customer.tenKH = customer.tenKH.trimStart()">
                    </div>
                  </div>
                  <div class="col-md-6">
                    <div class="mb-3 animate-slide-left" style="animation-delay: 0.15s">
                      <label class="form-label fw-bold">Mã giới thiệu của bạn</label>
                      <div class="input-group">
                        <input type="text" class="form-control" :value="customer.maGioiThieu" disabled>
                        <button class="btn btn-outline-primary" type="button" @click="copyReferralCode">
                          <i class="fas fa-copy"></i> Sao chép
                        </button>
                      </div>
                    </div>
                    <div class="mb-3 animate-slide-left" style="animation-delay: 0.2s">
                      <label class="form-label fw-bold">Số điện thoại</label>
                      <input type="text" class="form-control" v-model="customer.sdt" required 
                             pattern="[0-9]{9,11}" placeholder="090xxxxxxx"
                             @input="customer.sdt = customer.sdt.replace(/[^0-9]/g, '')">
                    </div>
                  </div>
                </div>
                <div class="row">
                  <div class="col-md-12 mb-3 animate-fade-in" style="animation-delay: 0.25s">
                    <label class="form-label fw-bold">Email</label>
                    <input type="email" class="form-control" :value="user.mail" disabled>
                  </div>
                </div>
                
                <div class="text-end d-flex justify-content-end gap-2 animate-fade-in" style="animation-delay: 0.3s">
                  <button v-if="!customer.hasAppliedReferral" 
                          type="button"
                          class="btn btn-outline-primary" 
                          @click.stop="showReferralModal = true">
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
          <div class="card shadow-sm mb-4 animate-fade-in" style="animation-delay: 0.15s">
            <div class="card-header">
              <i class="fas fa-key me-2"></i> Đổi mật khẩu
            </div>
            <div class="card-body">
              <form @submit.prevent="changePassword">
                <div class="mb-3 animate-slide-left" style="animation-delay: 0.2s">
                  <label class="form-label fw-bold">Mật khẩu hiện tại</label>
                  <input type="password" class="form-control" v-model="password.currentPassword" required>
                </div>
                <div class="mb-3 animate-slide-left" style="animation-delay: 0.25s">
                  <label class="form-label fw-bold">Mật khẩu mới</label>
                  <input type="password" class="form-control" v-model="password.newPassword" required>
                </div>
                <div class="mb-3 animate-slide-left" style="animation-delay: 0.3s">
                  <label class="form-label fw-bold">Xác nhận mật khẩu mới</label>
                  <input type="password" class="form-control" v-model="password.confirmPassword" required>
                </div>

                <div class="text-end animate-fade-in" style="animation-delay: 0.35s">
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
      <div class="card shadow-sm animate-fade-in" style="animation-delay: 0.4s">
        <div class="card-header d-flex justify-content-between align-items-center">
          <span><i class="fa-solid fa-location-dot me-2"></i>Quản lý địa chỉ nhận hàng</span>
          <button class="btn btn-light btn-sm" @click="showAddModal = true">
            <i class="fas fa-plus me-1"></i> Thêm địa chỉ mới
          </button>
        </div>
        <div class="card-body">
          <div v-for="(address, index) in addresses" :key="address.maDC" 
               class="address-card mb-3 animate-slide-up" 
               :class="{ 'default': address.macDinh }"
               :style="{ animationDelay: `${0.05 * index}s` }">
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

          <div v-if="addresses.length === 0" class="text-center text-muted p-4 animate-fade-in" style="animation-delay: 0.5s">
            <i class="fas fa-map-marked-alt fa-3x mb-3"></i>
            <p>Chưa có địa chỉ nào. Thêm địa chỉ mới để dễ dàng đặt hàng!</p>
          </div>
        </div>
      </div>
    </main>

    <!-- Modal Lịch sử điểm -->
    <div v-if="showHistoryModal" class="modal fade show d-block glass-backdrop" @click.self="showHistoryModal = false">
      <div class="modal-dialog modal-lg modal-dialog-centered modal-zoom-in">
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
              <div v-for="(item, index) in filteredHistory" :key="item.ngayGiaoDich" 
                   class="history-item d-flex justify-content-between align-items-center border-bottom py-2 modal-item-fade"
                   :style="{ animationDelay: `${0.03 * index}s` }">
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
            <button type="button" class="btn btn-secondary" @click="showHistoryModal = false">Đóng</button>
          </div>
        </div>
      </div>
    </div>

    <!-- Modal Nhập mã giới thiệu -->
    <div v-if="showReferralModal" class="modal fade show d-block glass-backdrop" @click.self="closeReferralModal">
      <div class="modal-dialog modal-dialog-centered modal-zoom-in">
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
    <div v-if="showVoucherModal" class="modal fade show d-block glass-backdrop" @click.self="closeVoucherModal">
      <div class="modal-dialog modal-lg modal-dialog-centered modal-zoom-in">
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
                <a class="nav-link" :class="{ active: activeTab === 'available' }" 
                    @click="activeTab = 'available'">
                  <i class="fas fa-store me-1"></i> Đổi điểm lấy voucher
                </a>
              </li>
              <li class="nav-item">
                <a class="nav-link" :class="{ active: activeTab === 'myVouchers' }" 
                    @click="activeTab = 'myVouchers'">
                  <i class="fas fa-gift me-1"></i> Voucher của tôi
                </a>
              </li>
            </ul>

            <!-- Tab 1: Đổi điểm lấy voucher -->
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
                  <div v-for="(voucher, index) in filteredAvailableVouchers" :key="voucher.maVoucher" class="col-md-6 mb-2">
                    <div class="voucher-card p-3 border rounded h-100 modal-item-fade"
                         :style="{ animationDelay: `${0.05 * index}s` }">
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

            <!-- Tab 2: Voucher của tôi -->
            <div v-if="activeTab === 'myVouchers'">
              <div class="toolbar mb-3 d-flex justify-content-between align-items-center flex-wrap gap-2">
                <div class="d-flex gap-2 align-items-center">
                  <div class="filter-bar d-flex gap-2">
                    <select v-model="myVoucherStatusFilter" class="form-select form-select-sm" style="width: auto;">
                      <option value="all">Tất cả trạng thái</option>
                      <option value="Chưa sử dụng">Chưa sử dụng</option>
                      <option value="Hết hạn">Hết hạn</option>
                    </select>
                    <select v-model="myVoucherSortBy" class="form-select form-select-sm" style="width: auto;">
                      <option value="newest">Mới nhất</option>
                      <option value="oldest">Cũ nhất</option>
                      <option value="high-low">Giá trị cao - thấp</option>
                      <option value="low-high">Giá trị thấp - cao</option>
                    </select>
                  </div>
                </div>
                
                <div class="d-flex gap-2 align-items-center" v-if="hasExpiredVouchers">
                  <div class="form-check">
                    <input class="form-check-input" type="checkbox" id="selectAllExpired" v-model="selectAllExpired">
                    <label class="form-check-label small" for="selectAllExpired">
                      Chọn tất cả hết hạn
                    </label>
                  </div>
                  <button class="btn btn-danger btn-sm" 
                          @click="deleteSelectedExpiredVouchers" 
                          :disabled="selectedExpiredIds.length === 0 || deleteBatchLoading">
                    <span v-if="deleteBatchLoading" class="spinner-border spinner-border-sm me-1"></span>
                    <i v-else class="fas fa-trash-alt me-1"></i>
                    Xóa đã chọn ({{ selectedExpiredIds.length }})
                  </button>
                </div>
              </div>
              
              <div v-if="filteredMyVouchers.length > 0" class="voucher-list">
                <div v-for="(item, index) in filteredMyVouchers" :key="item.maKHVC" 
                      class="voucher-card mb-2 p-3 border rounded position-relative modal-item-fade"
                      :class="{ 'expired-voucher': item.trangThai === 'Hết hạn' }"
                      :style="{ animationDelay: `${0.04 * index}s` }">
                  
                  <span class="status-badge-corner" :class="getStatusBadgeClass(item.trangThai)">
                    {{ item.trangThai }}
                  </span>
                  
                  <div class="row align-items-center">
                    <div class="col-sm-1" v-if="item.trangThai === 'Hết hạn'">
                      <div class="form-check">
                        <input class="form-check-input" type="checkbox" 
                                :value="item.maKHVC"
                                v-model="selectedExpiredIds">
                      </div>
                    </div>
                    
                    <div :class="item.trangThai === 'Hết hạn' ? 'col-md-10' : 'col-md-11'">
                      <div class="fw-bold fs-5">{{ item.voucher.tenVoucher }}</div>
                      <div class="text-muted small">Giảm {{ formatMoney(item.voucher.giaTriGiam) }}</div>
                      <div class="text-muted small">Đơn tối thiểu: {{ formatMoney(item.voucher.donToiThieu) }}</div>
                      <div class="text-muted small">Hạn sử dụng: {{ formatDate(item.hanSuDung) }}</div>
                      <div class="text-muted small">Ngày đổi: {{ formatDate(item.ngayDoi) }}</div>
                    </div>
                    
                    <div class="col-sm-1 text-center">
                      <button v-if="item.trangThai === 'Hết hạn'"   
                              class="btn btn-sm btn-outline-danger" 
                              @click="deleteSingleExpiredVoucher(item.maKHVC)"
                              :disabled="deleteSingleLoading === item.maKHVC"
                              style="width: 36px; height: 36px; border-radius: 50%; display: inline-flex; align-items: center; justify-content: center;">
                        <span v-if="deleteSingleLoading === item.maKHVC" class="spinner-border spinner-border-sm"></span>
                        <i v-else class="fas fa-trash"></i>
                      </button>
                    </div>
                  </div>
                </div>
              </div>
              <div v-else class="text-center text-muted py-4">
                <i class="fas fa-ticket-alt fa-3x mb-3"></i>
                <p>Bạn chưa có voucher nào</p>
              </div>
            </div>      
          </div>
          <div class="modal-footer d-flex justify-content-between align-items-center">
            <div>
              <strong>Điểm hiện có: </strong> 
              <span class="text-yellow fw-bold">{{ customer.diemTichLuy || 0 }}</span>
            </div>
            <button type="button" class="btn btn-secondary" @click="closeVoucherModal">Đóng</button>
          </div>
        </div>
      </div>
    </div>

    <!-- Modal Thêm/Sửa Địa Chỉ -->
    <div v-if="showAddModal || editingAddress" class="modal fade show d-block glass-backdrop" @click.self="closeModal">
      <div class="modal-dialog modal-dialog-centered modal-zoom-in">
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

<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import axios from 'axios'
import KH_Navbar from '@/components/Shared/KH_Navbar.vue'
import Footer from '@/components/Shared/Footer.vue'
import Toast from '@/components/Shared/Toast.vue'

const user = ref({ userName: '', mail: '', createAt: '' })
    const customer = ref({ tenKH: '', sdt: '', diemTichLuy: 0, maGioiThieu: '', hasAppliedReferral: false })
    const addresses = ref([])
    const password = ref({ currentPassword: '', newPassword: '', confirmPassword: '' })
    const referralCodeInput = ref('')
    
    const loading = ref({ profile: false, password: false, address: false, referral: false })
    const showAddModal = ref(false)
    const editingAddress = ref(null)
    const addressForm = ref({ maDC: null, tenNN: '', sdt: '', diemGiao: '', macDinh: false })
    
    // Modal states
    const showHistoryModal = ref(false)
    const showVoucherModal = ref(false)
    const showReferralModal = ref(false)
    const activeTab = ref('available')
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
    
    const selectedExpiredIds = ref([])
    const selectAllExpired = ref(false)
    const deleteSingleLoading = ref(null)
    const deleteBatchLoading = ref(false)

    const expiredVouchers = computed(() => {
      return myVouchers.value.filter(v => v.trangThai === 'Hết hạn')
    })

    const hasExpiredVouchers = computed(() => {
      return expiredVouchers.value.length > 0
    })

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
      vouchers = vouchers.filter(v => v.trangThai !== 'Đã sử dụng')

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

      const redeemedVoucherIds = myVouchers.value.map(v => v.voucher?.maVoucher)

      vouchers = vouchers.filter(v => !redeemedVoucherIds.includes(v.maVoucher))

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

    const deleteSingleExpiredVoucher = async (maKHVC) => {
      if (!confirm('Bạn có chắc muốn xóa voucher này?')) return
      
      deleteSingleLoading.value = maKHVC
      
      try {
        const { data } = await axios.delete(`/api/customer/vouchers/${maKHVC}`)
        if (data.success) {
          window.showToast(data.message, 'success')
          await fetchMyVouchers()
          selectedExpiredIds.value = []
          selectAllExpired.value = false
        } else {
          window.showToast(data.message, 'danger')
        }
      } catch (err) {
        window.showToast(err.response?.data?.message || 'Lỗi khi xóa voucher', 'danger')
      } finally {
        deleteSingleLoading.value = null
      }
    }

    const deleteSelectedExpiredVouchers = async () => {
      if (selectedExpiredIds.value.length === 0) {
        window.showToast('Vui lòng chọn voucher cần xóa', 'warning')
        return
      }
      
      if (!confirm(`Bạn có chắc muốn xóa ${selectedExpiredIds.value.length} voucher đã hết hạn?`)) return
      
      deleteBatchLoading.value = true
      
      try {
        const { data } = await axios.delete('/api/customer/vouchers/batch', {
          data: { ids: selectedExpiredIds.value }
        })
        
        if (data.success) {
          window.showToast(data.message, 'success')
          await fetchMyVouchers()
          selectedExpiredIds.value = []
          selectAllExpired.value = false
        } else {
          window.showToast(data.message, 'danger')
        }
      } catch (err) {
        window.showToast(err.response?.data?.message || 'Lỗi khi xóa voucher', 'danger')
      } finally {
        deleteBatchLoading.value = false
      }
    }

    watch(selectAllExpired, (newVal) => {
      if (newVal) {
        selectedExpiredIds.value = expiredVouchers.value.map(v => v.maKHVC)
      } else {
        selectedExpiredIds.value = []
      }
    })

    watch(expiredVouchers, () => {
      selectedExpiredIds.value = []
      selectAllExpired.value = false
    })

    const fetchProfile = async () => {
      try {
        const { data } = await axios.get('/api/customer/profile')
        if (data.success) {
          user.value = data.user
          customer.value = data.customer
          addresses.value = (data.addresses || []).sort((a, b) => b.macDinh - a.macDinh)
        }
      } catch (err) {
        window.showToast('Không thể tải thông tin', 'danger')
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
        } else {
          console.error('Không thể tải voucher:', data.message)
          availableVouchers.value = []
        }
      } catch (err) {
        window.showToast(err.response?.data?.message || 'Không thể tải danh sách voucher', 'danger')
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
          window.showToast(data.message, 'success')
          customer.value.diemTichLuy = data.remainingPoints
          await fetchMyVouchers()
          await loadAvailableVouchers()
          await fetchPointsHistory()
        } else {
          window.showToast(data.message, 'danger')
        }
      } catch (err) {
        window.showToast(err.response?.data?.message || 'Lỗi khi đổi voucher', 'danger')
      } finally {
        redeemLoading.value = false
        selectedVoucher.value = null
      }
    }

    const submitReferralCode = async () => {
      if (!referralCodeInput.value.trim()) {
        window.showToast('Vui lòng nhập mã giới thiệu', 'warning')
        return
      }
      
      loading.value.referral = true
      
      try {
        const { data } = await axios.post('/api/customer/apply-referral', {
          referralCode: referralCodeInput.value.trim()
        })
        
        if (data.success) {
          window.showToast(data.message, 'success')
          customer.value.diemTichLuy = data.newPoints
          customer.value.hasAppliedReferral = true
          referralCodeInput.value = ''
          await fetchProfile()
          await fetchPointsHistory()
          closeReferralModal()
        } else {
          window.showToast(data.message, 'danger')
        }
      } catch (err) {
        window.showToast(err.response?.data?.message || 'Lỗi khi nhập mã giới thiệu', 'danger')
      } finally {
        loading.value.referral = false
      }
    }

    const copyReferralCode = () => {
      const code = customer.value.maGioiThieu
      if (code) {
        navigator.clipboard.writeText(code)
        window.showToast('Đã sao chép mã giới thiệu: ' + code, 'success')
      } else {
        window.showToast('Không có mã giới thiệu', 'warning')
      }
    }

    const updateProfile = async () => {
      if (!validateProfile()) return
      
      loading.value.profile = true
      
      try {
        const { data } = await axios.put('/api/customer/profile', {
          userName: user.value.userName.trim(),
          fullname: customer.value.tenKH.trim(),
          phone: customer.value.sdt.trim()
        })
        
        if (data.success) {
          window.showToast(data.message, 'success')
          if (data.user) user.value.userName = data.user.userName
          if (data.customer) {
            customer.value.tenKH = data.customer.tenKH
            customer.value.sdt = data.customer.sdt
          }
        } else {
          window.showToast(data.message, 'danger')
        }
      } catch (err) {
        window.showToast(err.response?.data?.message || 'Lỗi kết nối', 'danger')
      } finally {
        loading.value.profile = false
      }
    }

    const changePassword = async () => {
      if (!validatePassword()) return
      
      loading.value.password = true
      
      try {
        const { data } = await axios.put('/api/customer/change-password', password.value)
        
        if (data.success) {
          window.showToast(data.message, 'success')
          password.value = { currentPassword: '', newPassword: '', confirmPassword: '' }
        } else {
          window.showToast(data.message, 'danger')
        }
      } catch (err) {
        window.showToast(err.response?.data?.message || 'Mật khẩu hiện tại không đúng', 'danger')
      } finally {
        loading.value.password = false
      }
    }

    const saveAddress = async () => {
      if (!validateAddress()) return
      
      loading.value.address = true
      
      try {
        const url = editingAddress.value 
          ? `/api/customer/address/${addressForm.value.maDC}`
          : '/api/customer/address'
        
        const method = editingAddress.value ? 'put' : 'post'
        const { data } = await axios[method](url, addressForm.value)
        
        if (data.success) {
          window.showToast(data.message, 'success')
          await fetchProfile()
          closeModal()
        } else {
          window.showToast(data.message, 'danger')
        }
      } catch (err) {
        window.showToast(err.response?.data?.message || 'Lỗi kết nối', 'danger')
      } finally {
        loading.value.address = false
      }
    }

    const deleteAddress = async (maDC) => {
      if (!confirm('Bạn có chắc muốn xóa địa chỉ này?')) return
      
      try {
        const { data } = await axios.delete(`/api/customer/address/${maDC}`)
        if (data.success) {
          window.showToast(data.message, 'success')
          await fetchProfile()
        } else {
          window.showToast(data.message, 'danger')
        }
      } catch (err) {
        window.showToast(err.response?.data?.message || 'Lỗi kết nối', 'danger')
      }
    }

    const setDefaultAddress = async (maDC) => {
      try {
        const { data } = await axios.post(`/api/customer/address/${maDC}/set-default`)
        if (data.success) {
          window.showToast(data.message, 'success')
          await fetchProfile()
        } else {
          window.showToast(data.message, 'danger')
        }
      } catch (err) {
        window.showToast(err.response?.data?.message || 'Lỗi kết nối', 'danger')
      }
    }

    const closeVoucherModal = () => {
      showVoucherModal.value = false
      activeTab.value = 'myVouchers'
      myVoucherStatusFilter.value = 'all'
      myVoucherSortBy.value = 'newest'
      availableVoucherFilter.value = 'all'
      availableVoucherSortBy.value = 'newest'
      selectedExpiredIds.value = []
      selectAllExpired.value = false
    }

    const closeReferralModal = () => {
      showReferralModal.value = false
      referralCodeInput.value = ''
    }

    const validateProfile = () => {
      const { userName, tenKH, sdt } = { 
        userName: user.value.userName.trim(), 
        tenKH: customer.value.tenKH.trim(), 
        sdt: customer.value.sdt.trim() 
      }
      
      if (!/^\d{9,11}$/.test(sdt)) {
        window.showToast('Số điện thoại không hợp lệ', 'warning')
        return false
      }
      if (userName.length < 3) {
        window.showToast('Tên đăng nhập phải có ít nhất 3 ký tự', 'warning')
        return false
      }
      if (!tenKH) {
        window.showToast('Họ và tên không được để trống', 'warning')
        return false
      }
      return true
    }

    const validatePassword = () => {
      if (password.value.newPassword !== password.value.confirmPassword) {
        window.showToast('Mật khẩu mới không khớp', 'warning')
        return false
      }
      if (password.value.currentPassword === password.value.newPassword) {
        window.showToast('Mật khẩu mới không được trùng với mật khẩu hiện tại', 'warning')
        return false
      }
      return true
    }

    const validateAddress = () => {
      const { tenNN, sdt } = addressForm.value
      if (!tenNN.trim()) {
        window.showToast('Vui lòng nhập tên người nhận', 'warning')
        return false
      }
      if (!/^\d{9,11}$/.test(sdt.trim())) {
        window.showToast('Số điện thoại không hợp lệ', 'warning')
        return false
      }
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
        case 'Hết hạn': return 'bg-danger'
        default: return 'bg-secondary'
      }
    }
    
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
      await loadAvailableVouchers()
    })
</script>

<style scoped>
/* ===== ANIMATION KEYFRAMES ===== */
@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes slideLeft {
  from {
    opacity: 0;
    transform: translateX(30px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

@keyframes slideRight {
  from {
    opacity: 0;
    transform: translateX(-30px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

@keyframes zoomIn {
  from {
    opacity: 0;
    transform: scale(0.9);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

@keyframes modalItemFade {
  from {
    opacity: 0;
    transform: translateY(15px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* ===== ANIMATION CLASSES ===== */
.animate-fade-in {
  animation: fadeIn 0.5s ease-out forwards;
  opacity: 0;
}

.animate-slide-down {
  animation: slideDown 0.6s ease-out forwards;
  opacity: 0;
}

.animate-slide-up {
  animation: slideUp 0.5s ease-out forwards;
  opacity: 0;
}

.animate-slide-left {
  animation: slideLeft 0.5s ease-out forwards;
  opacity: 0;
}

.animate-slide-right {
  animation: slideRight 0.5s ease-out forwards;
  opacity: 0;
}

.modal-zoom-in {
  animation: zoomIn 0.3s ease-out forwards;
}

.modal-item-fade {
  animation: modalItemFade 0.4s ease-out forwards;
  opacity: 0;
}

/* ===== EXISTING STYLES ===== */
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
  transform: translateY(-2px);
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
  transition: all 0.3s ease;
}

.btn-primary:hover, .btn-success:hover {
  background-color: #333333;
  border-color: #333333;
  transform: translateY(-2px);
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.2);
}

.btn-secondary {
  background-color: #ffffff;
  border-color: #000000;
  color: #000000;
  transition: all 0.3s ease;
}

.btn-secondary:hover {
  background-color: #f0f0f0;
  border-color: #000000;
  color: #000000;
  transform: translateY(-2px);
}

.btn-outline-primary {
  border-color: #000000;
  color: #000000;
  transition: all 0.3s ease;
}

.btn-outline-primary:hover {
  background-color: #000000;
  border-color: #000000;
  color: #ffffff;
  transform: translateY(-2px);
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.2);
}

.btn-outline-success {
  border-color: #28a745;
  color: #28a745;
  transition: all 0.3s ease;
}

.btn-outline-success:hover {
  background-color: #28a745;
  border-color: #28a745;
  color: #ffffff;
  transform: translateY(-2px);
}

.btn-outline-danger {
  border-color: #dc3545;
  color: #dc3545;
  transition: all 0.3s ease;
}

.btn-outline-danger:hover:not(:disabled) {
  background-color: #dc3545;
  border-color: #dc3545;
  color: #ffffff;
  transform: translateY(-2px);
}

.btn-outline-danger:disabled {
  border-color: #6c757d;
  color: #6c757d;
  cursor: not-allowed;
  opacity: 0.5;
}

.form-control {
  border: 1px solid #000000;
  transition: all 0.3s ease;
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
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.points-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.2);
}

.points-display {
  display: flex;
  align-items: center;
  gap: 15px;
}

.points-icon {
  font-size: 2rem;
  color: #ffc107;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.1);
  }
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
  transition: all 0.2s ease;
  padding: 10px 20px;
}

.history-item:hover {
  background: #f8f9fa;
  transform: translateX(5px);
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
  transition: all 0.3s ease;
}

.nav-tabs .nav-link.active {
  color: #000000;
  font-weight: bold;
  border-bottom: 2px solid #000000;
}

.nav-tabs .nav-link:hover:not(.active) {
  border-color: #dee2e6 #dee2e6 #ddd;
  background-color: #f8f9fa;
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

.status-badge-corner {
  position: absolute;
  top: 10px;
  right: 10px;
  font-size: 0.75rem;
  color:#ffffff;
  padding: 4px 8px;
  border-radius: 4px;
  z-index: 1;
}

.voucher-card {
  position: relative;
  transition: transform 0.2s, box-shadow 0.2s;
}

.voucher-card .row {
  padding-right: 80px; 
}

body.modal-open {
  overflow: hidden;
}

@media (max-width: 768px) {
  .voucher-card .row {
    padding-right: 70px;
  }
  
  .status-badge-corner {
    top: 5px;
    right: 5px;
    font-size: 0.7rem;
    padding: 3px 6px;
  }
}
</style>

<style>
.glass-backdrop {
  background-color: rgba(0, 0, 0, 0.25) !important;
  backdrop-filter: blur(5px);
  -webkit-backdrop-filter: blur(8px);
  animation: fadeIn 0.3s ease-out;
}

.glass-backdrop.show {
  opacity: 1 !important;
}
</style>