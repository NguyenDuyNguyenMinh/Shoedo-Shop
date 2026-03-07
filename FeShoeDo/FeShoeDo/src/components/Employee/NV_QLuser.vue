<template>
  <div class="employee-layout">
    <NV_Sidebar />

    <main class="main-content">
      <div class="page-container">

        <!-- Stats Cards -->
        <div class="row g-3 mb-4">
          <div class="col-md-3">
            <div class="stats-mini bg-gradient-1">
              <div class="stats-icon"><i class="bi bi-people-fill"></i></div>
              <h4>{{ stats.totalUsers }}</h4>
              <p>Tổng User</p>
            </div>
          </div>
          <div class="col-md-3">
            <div class="stats-mini bg-gradient-2">
              <div class="stats-icon"><i class="bi bi-person-heart"></i></div>
              <h4>{{ stats.totalCustomers }}</h4>
              <p>Khách hàng</p>
            </div>
          </div>
          <div class="col-md-3">
            <div class="stats-mini bg-gradient-3">
              <div class="stats-icon"><i class="bi bi-person-gear"></i></div>
              <h4>{{ stats.totalEmployees + stats.totalAdmins }}</h4>
              <p>Quản trị viên</p>
            </div>
          </div>
          <div class="col-md-3">
            <div class="stats-mini bg-gradient-4">
              <div class="stats-icon"><i class="bi bi-person-x-fill"></i></div>
              <h4>{{ stats.totalInactive }}</h4>
              <p>Tài khoản bị khóa</p>
            </div>
          </div>
        </div>

        <!-- Content Card -->
        <div class="content-card">
          <div class="d-flex justify-content-between align-items-center mb-4">
            <div>
              <h5 class="page-title">Danh sách người dùng</h5>
              <p class="page-subtitle">Quản lý tài khoản và thông tin người dùng</p>
            </div>
            <button class="btn btn-dark" @click="openCreateModal">
              <i class="bi bi-plus-circle me-2"></i>Thêm người dùng
            </button>
          </div>

          <!-- Filter Section -->
          <div class="filter-section">
            <div class="row g-3">
              <div class="col-md-4">
                <div class="input-group">
                  <span class="input-group-text bg-light">
                    <i class="bi bi-search"></i>
                  </span>
                  <input type="text" class="form-control" v-model="filters.keyword"
                         placeholder="Tìm theo username, email, họ tên, SĐT..."
                         @keyup.enter="applyFilters">
                  <button v-if="filters.keyword" class="btn btn-outline-secondary" type="button" @click="clearKeyword">
                    <i class="bi bi-x"></i>
                  </button>
                </div>
              </div>
              <div class="col-md-2">
                <select class="form-select" v-model="filters.role" @change="applyFilters">
                  <option value="">Tất cả vai trò</option>
                  <option value="ADMIN">Admin</option>
                  <option value="EMPLOYEE">Nhân viên</option>
                  <option value="CUSTOMER">Khách hàng</option>
                </select>
              </div>
              <div class="col-md-2">
                <select class="form-select" v-model="filters.status" @change="applyFilters">
                  <option value="all">Tất cả trạng thái</option>
                  <option value="active">Đang hoạt động</option>
                  <option value="inactive">Bị khóa</option>
                </select>
              </div>
              <div class="col-md-2">
                <select class="form-select" v-model="filters.sortBy" @change="applyFilters">
                  <option value="maUser,desc">Mới nhất</option>
                  <option value="maUser,asc">Cũ nhất</option>
                  <option value="userName,asc">Username A-Z</option>
                  <option value="userName,desc">Username Z-A</option>
                  <option value="hoTen,asc">Tên A-Z</option>
                  <option value="hoTen,desc">Tên Z-A</option>
                </select>
              </div>
              <div class="col-md-2">
                <div class="d-flex gap-2">
                  <button class="btn btn-dark flex-fill" @click="applyFilters">
                    <i class="bi bi-search me-2"></i>Tìm kiếm
                  </button>
                  <button class="btn btn-outline-secondary" @click="resetFilters" title="Reset bộ lọc">
                    <i class="bi bi-arrow-clockwise"></i>
                  </button>
                </div>
              </div>
            </div>
          </div>

          <!-- Loading State -->
          <div v-if="loading" class="text-center py-5">
            <div class="spinner-border text-primary" role="status">
              <span class="visually-hidden">Loading...</span>
            </div>
          </div>

          <!-- User Table -->
          <div v-else class="table-responsive">
            <table class="table table-hover align-middle user-table">
              <thead>
              <tr>
                <th>#</th>
                <th>Người dùng</th>
                <th>Email</th>
                <th>Họ tên</th>
                <th>SĐT</th>
                <th>Vai trò</th>
                <th>Trạng thái</th>
                <th>Ngày tạo</th>
                <th class="text-center">Thao tác</th>
              </tr>
              </thead>
              <tbody>
              <tr v-for="(user, index) in users" :key="user.maUser">
                <td><span class="fw-semibold text-muted">{{ (pagination.currentPage - 1) * pagination.pageSize + index + 1 }}</span></td>
                <td>
                  <div class="d-flex align-items-center gap-2">
                    <div class="user-avatar" :class="getAvatarClass(user.role)">
                      <i :class="getAvatarIcon(user.role)"></i>
                    </div>
                    <span class="fw-semibold">{{ user.userName }}</span>
                  </div>
                </td>
                <td><span class="text-muted">{{ user.mail }}</span></td>
                <td>{{ user.hoTen || '—' }}</td>
                <td>{{ user.sdt || '—' }}</td>
                <td>
                    <span class="badge role-badge" :class="getRoleClass(user.role)">
                      <i :class="getRoleIcon(user.role)" class="me-1"></i>{{ getRoleText(user.role) }}
                    </span>
                </td>
                <td>
                    <span class="badge status-badge" :class="user.isActive ? 'badge-active' : 'badge-inactive'">
                      <i :class="user.isActive ? 'bi bi-check-circle' : 'bi bi-x-circle'" class="me-1"></i>
                      {{ user.isActive ? 'Hoạt động' : 'Bị khóa' }}
                    </span>
                </td>
                <td><span class="text-muted small">{{ formatDate(user.createAt) }}</span></td>
                <td class="text-center">
                  <div class="action-buttons justify-content-center">
                    <button class="btn btn-outline-primary btn-sm" @click="viewUserDetail(user.maUser)" title="Xem chi tiết">
                      <i class="bi bi-eye"></i>
                    </button>
                    <button class="btn btn-outline-secondary btn-sm" @click="openEditModal(user)" title="Chỉnh sửa">
                      <i class="bi bi-pencil"></i>
                    </button>
                    <button v-if="user.role !== 'ADMIN'" class="btn btn-outline-danger btn-sm" @click="toggleUserStatus(user)" :title="user.isActive ? 'Khóa tài khoản' : 'Mở khóa'">
                      <i :class="user.isActive ? 'bi bi-lock' : 'bi bi-unlock'"></i>
                    </button>
                  </div>
                </td>
              </tr>
              <tr v-if="users.length === 0">
                <td colspan="9" class="text-center py-4 text-muted">
                  Không tìm thấy người dùng nào
                </td>
              </tr>
              </tbody>
            </table>
          </div>

          <!-- Pagination -->
          <div class="mt-4">
            <div class="d-flex justify-content-between align-items-center">
              <span class="text-muted small">
                Hiển thị {{ startItem }} - {{ endItem }} trên tổng {{ pagination.totalItems }} người dùng
              </span>
              <nav>
                <ul class="pagination mb-0">
                  <li class="page-item" :class="{ disabled: pagination.currentPage === 1 }">
                    <button class="page-link" @click="changePage(pagination.currentPage - 1)">
                      <i class="bi bi-chevron-left"></i>
                    </button>
                  </li>
                  <li v-for="page in pagination.totalPages" :key="page" class="page-item" :class="{ active: pagination.currentPage === page }">
                    <button class="page-link" @click="changePage(page)">{{ page }}</button>
                  </li>
                  <li class="page-item" :class="{ disabled: pagination.currentPage === pagination.totalPages }">
                    <button class="page-link" @click="changePage(pagination.currentPage + 1)">
                      <i class="bi bi-chevron-right"></i>
                    </button>
                  </li>
                </ul>
              </nav>
            </div>
          </div>
        </div>
      </div>
    </main>

    <!-- Modal Xem Chi Tiết User -->
    <div class="modal fade" id="viewUserModal" tabindex="-1" aria-hidden="true" ref="viewUserModal">
      <div class="modal-dialog modal-lg">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">
              <i class="bi bi-person-lines-fill me-2"></i>Chi tiết người dùng
            </h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
          </div>
          <div class="modal-body" v-if="selectedUser">
            <!-- User Info Card -->
            <div class="user-detail-card mb-4">
              <div class="row">
                <div class="col-md-2 text-center">
                  <div class="user-avatar-lg" :class="getAvatarClass(selectedUser.role)">
                    <i :class="getAvatarIcon(selectedUser.role)"></i>
                  </div>
                </div>
                <div class="col-md-10">
                  <div class="row g-3">
                    <div class="col-md-6">
                      <div class="detail-item">
                        <label>Username</label>
                        <p>{{ selectedUser.userName || '—' }}</p>
                      </div>
                    </div>
                    <div class="col-md-6">
                      <div class="detail-item">
                        <label>Email</label>
                        <p>{{ selectedUser.mail || '—' }}</p>
                      </div>
                    </div>
                    <div class="col-md-6">
                      <div class="detail-item">
                        <label>Họ tên</label>
                        <p>{{ selectedUser.hoTen || '—' }}</p>
                      </div>
                    </div>
                    <div class="col-md-6">
                      <div class="detail-item">
                        <label>Số điện thoại</label>
                        <p>{{ selectedUser.sdt || '—' }}</p>
                      </div>
                    </div>
                    <div class="col-md-4">
                      <div class="detail-item">
                        <label>Vai trò</label>
                        <p>
                          <span class="badge role-badge" :class="getRoleClass(selectedUser.role)">
                            <i :class="getRoleIcon(selectedUser.role)" class="me-1"></i>{{ getRoleText(selectedUser.role) }}
                          </span>
                        </p>
                      </div>
                    </div>
                    <div class="col-md-4">
                      <div class="detail-item">
                        <label>Trạng thái</label>
                        <p>
                          <span class="badge status-badge" :class="selectedUser.isActive ? 'badge-active' : 'badge-inactive'">
                            <i :class="selectedUser.isActive ? 'bi bi-check-circle' : 'bi bi-x-circle'" class="me-1"></i>
                            {{ selectedUser.isActive ? 'Hoạt động' : 'Bị khóa' }}
                          </span>
                        </p>
                      </div>
                    </div>
                    <div class="col-md-4">
                      <div class="detail-item">
                        <label>Ngày tạo</label>
                        <p>{{ formatDate(selectedUser.createAt) }}</p>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <!-- Địa Chỉ -->
            <h6 class="section-title"><i class="bi bi-geo-alt me-2"></i>Danh sách địa chỉ</h6>
            <div class="address-list mb-4">
              <template v-if="selectedUser.diaChis && selectedUser.diaChis.length > 0">
                <div v-for="address in selectedUser.diaChis" :key="address.maDC" class="address-item">
                  <div class="d-flex justify-content-between align-items-start">
                    <div>
                      <div class="fw-semibold">{{ address.tenNN || '—' }} <span class="text-muted">| {{ address.sdt || '—' }}</span></div>
                      <div class="text-muted small mt-1">{{ address.diemGiao || '—' }}</div>
                    </div>
                    <span v-if="address.macDinh" class="badge bg-dark">Mặc định</span>
                  </div>
                </div>
              </template>
              <div v-else class="text-muted text-center py-3">
                <i class="bi bi-geo-alt me-2"></i>Không có địa chỉ nào
              </div>
            </div>

            <!-- Đơn hàng gần đây -->
            <h6 class="section-title"><i class="bi bi-bag-check me-2"></i>Đơn hàng gần đây</h6>
            <div class="table-responsive">
              <table class="table table-sm table-hover">
                <thead>
                <tr>
                  <th>Mã HĐ</th>
                  <th>Ngày mua</th>
                  <th>Phương thức TT</th>
                  <th>Trạng thái</th>
                  <th>Ghi chú</th>
                </tr>
                </thead>
                <tbody>
                <template v-if="selectedUser.hoaDons && selectedUser.hoaDons.length > 0">
                  <tr v-for="order in selectedUser.hoaDons" :key="order.maHD">
                    <td class="fw-semibold">#{{ formatOrderId(order.maHD) }}</td>
                    <td>{{ formatDate(order.ngayMua) }}</td>
                    <td>{{ order.phuongThucTT || '—' }}</td>
                    <td>
            <span class="badge" :class="getOrderStatusClass(order.trangThai)">
              {{ order.trangThai || '—' }}
            </span>
                    </td>
                    <td class="text-muted small">{{ order.ghiChu || '—' }}</td>
                  </tr>
                </template>
                <tr v-else>
                  <td colspan="5" class="text-center py-3 text-muted">
                    Không có đơn hàng nào
                  </td>
                </tr>
                </tbody>
              </table>
            </div>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" @click="closeViewModal">
              <i class="bi bi-x-circle me-2"></i>Đóng
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Modal Thêm/Sửa User -->
    <div class="modal fade" id="editUserModal" tabindex="-1" aria-hidden="true" ref="editUserModal">
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">
              <i :class="isEdit ? 'bi bi-pencil-square' : 'bi bi-plus-circle'" class="me-2"></i>
              {{ isEdit ? 'Chỉnh sửa người dùng' : 'Thêm người dùng mới' }}
            </h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
          </div>
          <form @submit.prevent="saveUser">
            <div class="modal-body">
              <div class="mb-3">
                <label class="form-label">Username <span class="text-danger">*</span></label>
                <input type="text" class="form-control" v-model="form.userName" :disabled="isEdit" required>
                <small class="text-muted" v-if="isEdit">Username không thể thay đổi</small>
              </div>
              <div class="mb-3">
                <label class="form-label">Email <span class="text-danger">*</span></label>
                <input type="email" class="form-control" v-model="form.mail" :disabled="isEdit" required>
                <small class="text-muted" v-if="isEdit">Email không thể thay đổi</small>
              </div>
              <div class="mb-3" v-if="!isEdit">
                <label class="form-label">Mật khẩu <span class="text-danger">*</span></label>
                <input type="password" class="form-control" v-model="form.password" required>
              </div>
              <div class="mb-3">
                <label class="form-label">Họ tên <span class="text-danger">*</span></label>
                <input type="text" class="form-control" v-model="form.hoTen" required>
              </div>
              <div class="mb-3">
                <label class="form-label">Số điện thoại</label>
                <input type="text" class="form-control" v-model="form.sdt">
              </div>
              <div class="mb-3">
                <label class="form-label">Vai trò <span class="text-danger">*</span></label>
                <select class="form-select" v-model="form.role"
                        :disabled="(isEdit && (form.role === 'ADMIN' || form.role === 'EMPLOYEE')) || (!isEdit && currentUserRole === 'EMPLOYEE')">
                  <option value="CUSTOMER">Khách hàng</option>
                  <option value="EMPLOYEE" :disabled="currentUserRole === 'EMPLOYEE'">Nhân viên</option>
                  <option value="ADMIN" :disabled="currentUserRole === 'EMPLOYEE'">Admin</option>
                </select>
                <small v-if="!isEdit && currentUserRole === 'EMPLOYEE'" class="text-warning d-block mt-1">
                  <i class="bi bi-info-circle"></i> Bạn chỉ có thể tạo tài khoản khách hàng
                </small>
              </div>
              <div class="mb-3">
                <label class="form-label">Trạng thái tài khoản</label>
                <div class="form-check form-switch">
                  <input class="form-check-input" type="checkbox" id="statusSwitch" v-model="form.isActive">
                  <label class="form-check-label" for="statusSwitch">
                    <span class="badge status-badge" :class="form.isActive ? 'badge-active' : 'badge-inactive'">
                      <i :class="form.isActive ? 'bi bi-check-circle' : 'bi bi-x-circle'" class="me-1"></i>
                      {{ form.isActive ? 'Đang hoạt động' : 'Bị khóa' }}
                    </span>
                  </label>
                </div>
              </div>
              <div class="mb-3" v-if="isEdit">
                <label class="form-label">Đặt lại mật khẩu</label>
                <button type="button" class="btn btn-outline-warning w-100" @click="resetPassword">
                  <i class="bi bi-key me-2"></i>Reset mật khẩu
                </button>
              </div>
            </div>
            <div class="modal-footer">
              <button type="button" class="btn btn-secondary" @click="closeEditModal">
                <i class="bi bi-x-circle me-2"></i>Hủy
              </button>
              <button type="submit" class="btn btn-dark" :disabled="submitting">
                <span v-if="submitting" class="spinner-border spinner-border-sm me-2"></span>
                <i class="bi bi-check-circle me-2"></i>{{ isEdit ? 'Cập nhật' : 'Thêm mới' }}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>

    <!-- Modal Xác nhận -->
    <div class="modal fade" id="confirmModal" tabindex="-1" ref="confirmModal">
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content">
          <div class="modal-header" :class="confirmConfig.type === 'danger' ? 'bg-danger' : 'bg-warning'">
            <h5 class="modal-title text-white">
              <i :class="confirmConfig.icon" class="me-2"></i>{{ confirmConfig.title }}
            </h5>
            <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
          </div>
          <div class="modal-body text-center py-4">
            <i :class="confirmConfig.icon" :style="{ fontSize: '4rem', color: confirmConfig.type === 'danger' ? '#dc3545' : '#ffc107' }"></i>
            <h5 class="mt-3">{{ confirmConfig.message }}</h5>
            <p class="text-muted" v-if="confirmConfig.detail">{{ confirmConfig.detail }}</p>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">
              <i class="bi bi-x-circle me-1"></i>Hủy
            </button>
            <button type="button" class="btn" :class="confirmConfig.type === 'danger' ? 'btn-danger' : 'btn-warning'" @click="confirmAction">
              <span v-if="confirmSubmitting" class="spinner-border spinner-border-sm me-2"></span>
              <i :class="confirmConfig.confirmIcon" class="me-1"></i>{{ confirmConfig.confirmText }}
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Modal thông báo thành công -->
    <div class="modal fade" id="successModal" tabindex="-1" ref="successModal">
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content">
          <div class="modal-header bg-success text-white">
            <h5 class="modal-title">
              <i class="bi bi-check-circle me-2"></i>Thành công
            </h5>
            <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
          </div>
          <div class="modal-body text-center py-4">
            <i class="bi bi-check-circle-fill text-success" style="font-size: 4rem;"></i>
            <h5 class="mt-3">{{ successMessage }}</h5>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-success" data-bs-dismiss="modal">
              <i class="bi bi-check me-1"></i>Đóng
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Modal thông báo lỗi -->
    <div class="modal fade" id="errorModal" tabindex="-1" ref="errorModal">
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content">
          <div class="modal-header bg-danger text-white">
            <h5 class="modal-title">
              <i class="bi bi-exclamation-triangle me-2"></i>Lỗi
            </h5>
            <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
          </div>
          <div class="modal-body text-center py-4">
            <i class="bi bi-x-circle-fill text-danger" style="font-size: 4rem;"></i>
            <h5 class="mt-3">{{ errorMessage }}</h5>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-danger" data-bs-dismiss="modal">
              <i class="bi bi-x me-1"></i>Đóng
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Modal thông báo thông tin -->
    <div class="modal fade" id="infoModal" tabindex="-1" ref="infoModal">
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content">
          <div class="modal-header bg-info text-white">
            <h5 class="modal-title">
              <i class="bi bi-info-circle me-2"></i>Thông tin
            </h5>
            <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
          </div>
          <div class="modal-body text-center py-4">
            <i class="bi bi-info-circle-fill text-info" style="font-size: 4rem;"></i>
            <h5 class="mt-3">{{ infoMessage }}</h5>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-info text-white" data-bs-dismiss="modal">
              <i class="bi bi-check me-1"></i>Đóng
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue';
import { Modal } from 'bootstrap';
import NV_Sidebar from '@/components/Shared/NV_Sidebar.vue';
import api from '@/services/api';

// State
const users = ref([]);
const loading = ref(false);
const submitting = ref(false);
const confirmSubmitting = ref(false);
const currentUserRole = ref('');

const stats = reactive({
  totalUsers: 0,
  totalCustomers: 0,
  totalEmployees: 0,
  totalAdmins: 0,
  totalInactive: 0
});

const pagination = reactive({
  currentPage: 1,
  totalPages: 1,
  totalItems: 0,
  pageSize: 10
});

const filters = reactive({
  keyword: '',
  role: '',
  status: 'all',
  sortBy: 'maUser,desc'
});

const selectedUser = ref(null);
const isEdit = ref(false);
const form = reactive({
  maUser: null,
  userName: '',
  mail: '',
  password: '',
  hoTen: '',
  sdt: '',
  role: 'CUSTOMER',
  isActive: true
});

// Modal refs
const viewUserModal = ref(null);
const editUserModal = ref(null);
const confirmModal = ref(null);
const successModal = ref(null);
const errorModal = ref(null);
const infoModal = ref(null);

let viewModalInstance = null;
let editModalInstance = null;
let confirmModalInstance = null;
let successModalInstance = null;
let errorModalInstance = null;
let infoModalInstance = null;

// Modal state
const confirmConfig = reactive({
  type: 'warning',
  title: '',
  message: '',
  detail: '',
  icon: 'bi bi-question-circle',
  confirmIcon: 'bi bi-check-circle',
  confirmText: 'Xác nhận',
  action: null,
  data: null
});

const successMessage = ref('');
const errorMessage = ref('');
const infoMessage = ref('');

// Computed
const startItem = computed(() => {
  if (users.value.length === 0) return 0;
  return (pagination.currentPage - 1) * pagination.pageSize + 1;
});

const endItem = computed(() => {
  if (users.value.length === 0) return 0;
  return Math.min(pagination.currentPage * pagination.pageSize, pagination.totalItems);
});

// Methods
const getCurrentUser = async () => {
  try {
    const response = await api.getCurrentUser();
    if (response.data.success) {
      currentUserRole.value = response.data.user?.role;
    }
  } catch (error) {
    console.error('Error getting current user:', error);
  }
};

const loadUsers = async () => {
  loading.value = true;
  try {
    // Xử lý sort
    let sortBy = 'maUser';
    let sortDir = 'desc';

    if (filters.sortBy) {
      const [field, dir] = filters.sortBy.split(',');
      sortBy = field;
      sortDir = dir;
    }

    const params = {
      keyword: filters.keyword,
      role: filters.role,
      status: filters.status,
      page: pagination.currentPage,
      size: pagination.pageSize,
      sortBy: sortBy,
      sortDir: sortDir
    };

    console.log('Loading users with params:', params);

    const response = await api.getUsers(params);
    console.log('API Response:', response.data);

    if (response.data.success) {
      users.value = response.data.data || [];

      if (response.data.stats) {
        Object.assign(stats, response.data.stats);
      }

      pagination.currentPage = response.data.currentPage || 1;
      pagination.totalPages = response.data.totalPages || 1;
      pagination.totalItems = response.data.totalItems || 0;

      console.log('Updated users:', users.value);
    } else {
      users.value = [];
      showErrorModal(response.data.message || 'Không thể tải danh sách người dùng');
    }
  } catch (error) {
    console.error('Error loading users:', error);
    users.value = [];
    showErrorModal(error.response?.data?.message || 'Không thể tải danh sách người dùng');
  } finally {
    loading.value = false;
  }
};

// Apply filters
const applyFilters = () => {
  pagination.currentPage = 1;
  loadUsers();
};

// Clear keyword
const clearKeyword = () => {
  filters.keyword = '';
  applyFilters();
};

// Reset filters
const resetFilters = () => {
  filters.keyword = '';
  filters.role = '';
  filters.status = 'all';
  filters.sortBy = 'maUser,desc';
  pagination.currentPage = 1;
  loadUsers();
};

const viewUserDetail = async (id) => {
  try {
    const response = await api.getUserDetails(id);
    if (response.data.success) {
      selectedUser.value = response.data.data;
      if (!viewModalInstance) {
        viewModalInstance = new Modal(viewUserModal.value);
      }
      viewModalInstance.show();
    }
  } catch (error) {
    console.error('Error loading user detail:', error);
    showErrorModal('Không thể tải chi tiết người dùng');
  }
};

const closeViewModal = () => {
  if (viewModalInstance) {
    viewModalInstance.hide();
  }
};

const openCreateModal = () => {
  isEdit.value = false;
  Object.assign(form, {
    maUser: null,
    userName: '',
    mail: '',
    password: '',
    hoTen: '',
    sdt: '',
    role: 'CUSTOMER',
    isActive: true
  });

  if (!editModalInstance) {
    editModalInstance = new Modal(editUserModal.value);
  }
  editModalInstance.show();
};

const openEditModal = (user) => {
  isEdit.value = true;

  // Kiểm tra quyền
  if (currentUserRole.value === 'EMPLOYEE' && (user.role === 'ADMIN' || user.role === 'EMPLOYEE')) {
    showErrorModal('Bạn không có quyền sửa tài khoản này');
    return;
  }

  Object.assign(form, {
    maUser: user.maUser,
    userName: user.userName,
    mail: user.mail,
    password: '',
    hoTen: user.hoTen,
    sdt: user.sdt,
    role: user.role,
    isActive: user.isActive
  });

  if (!editModalInstance) {
    editModalInstance = new Modal(editUserModal.value);
  }
  editModalInstance.show();
};

const closeEditModal = () => {
  if (editModalInstance) {
    editModalInstance.hide();
  }
};

const saveUser = async () => {
  submitting.value = true;
  try {
    let response;
    if (isEdit.value) {
      response = await api.updateUser(form.maUser, form);
    } else {
      response = await api.createUser(form);
    }

    if (response.data.success) {
      editModalInstance.hide();
      showSuccessModal(response.data.message);
      await loadUsers();
    }
  } catch (error) {
    console.error('Error saving user:', error);
    showErrorModal(error.response?.data?.message || 'Có lỗi xảy ra');
  } finally {
    submitting.value = false;
  }
};

const toggleUserStatus = (user) => {
  confirmConfig.type = user.isActive ? 'danger' : 'warning';
  confirmConfig.title = user.isActive ? 'Khóa tài khoản' : 'Mở khóa tài khoản';
  confirmConfig.message = user.isActive
      ? 'Bạn có chắc muốn khóa tài khoản này?'
      : 'Bạn có chắc muốn mở khóa tài khoản này?';
  confirmConfig.detail = `Tài khoản: ${user.userName} (${user.mail})`;
  confirmConfig.icon = user.isActive ? 'bi bi-lock-fill' : 'bi bi-unlock-fill';
  confirmConfig.confirmIcon = user.isActive ? 'bi bi-lock' : 'bi bi-unlock';
  confirmConfig.confirmText = user.isActive ? 'Khóa' : 'Mở khóa';
  confirmConfig.action = async () => {
    await processToggleStatus(user.maUser);
  };
  confirmConfig.data = user;

  if (!confirmModalInstance) {
    confirmModalInstance = new Modal(confirmModal.value);
  }
  confirmModalInstance.show();
};

const processToggleStatus = async (id) => {
  confirmSubmitting.value = true;
  try {
    const response = await api.toggleUserStatus(id);

    if (response.data.success) {
      confirmModalInstance.hide();
      showSuccessModal(response.data.message);
      await loadUsers();
    }
  } catch (error) {
    console.error('Error toggling user status:', error);
    showErrorModal(error.response?.data?.message || 'Có lỗi xảy ra');
  } finally {
    confirmSubmitting.value = false;
  }
};

const resetPassword = async () => {
  if (!form.maUser) return;

  confirmConfig.type = 'warning';
  confirmConfig.title = 'Reset mật khẩu';
  confirmConfig.message = 'Bạn có chắc muốn reset mật khẩu?';
  confirmConfig.detail = `Mật khẩu mới sẽ được tạo cho tài khoản ${form.userName}`;
  confirmConfig.icon = 'bi bi-key-fill';
  confirmConfig.confirmIcon = 'bi bi-key';
  confirmConfig.confirmText = 'Reset';
  confirmConfig.action = async () => {
    await processResetPassword(form.maUser);
  };

  if (!confirmModalInstance) {
    confirmModalInstance = new Modal(confirmModal.value);
  }
  confirmModalInstance.show();
};

const processResetPassword = async (id) => {
  confirmSubmitting.value = true;
  try {
    const response = await api.resetPassword(id);

    if (response.data.success) {
      confirmModalInstance.hide();

      infoMessage.value = `Mật khẩu mới: ${response.data.newPassword}`;
      if (!infoModalInstance) {
        infoModalInstance = new Modal(infoModal.value);
      }
      infoModalInstance.show();
    }
  } catch (error) {
    console.error('Error resetting password:', error);
    showErrorModal(error.response?.data?.message || 'Có lỗi xảy ra');
  } finally {
    confirmSubmitting.value = false;
  }
};

const confirmAction = () => {
  if (confirmConfig.action) {
    confirmConfig.action();
  }
};

const showSuccessModal = (message) => {
  successMessage.value = message;
  if (!successModalInstance) {
    successModalInstance = new Modal(successModal.value);
  }
  successModalInstance.show();
};

const showErrorModal = (message) => {
  errorMessage.value = message;
  if (!errorModalInstance) {
    errorModalInstance = new Modal(errorModal.value);
  }
  errorModalInstance.show();
};

const changePage = (page) => {
  if (page >= 1 && page <= pagination.totalPages) {
    pagination.currentPage = page;
    loadUsers();
  }
};

// Helper methods
const getAvatarClass = (role) => {
  switch (role) {
    case 'ADMIN': return 'bg-admin';
    case 'EMPLOYEE': return 'bg-employee';
    default: return 'bg-customer';
  }
};

const getAvatarIcon = (role) => {
  switch (role) {
    case 'ADMIN': return 'bi bi-star-fill';
    case 'EMPLOYEE': return 'bi bi-person-gear';
    default: return 'bi bi-person';
  }
};

const getRoleClass = (role) => {
  switch (role) {
    case 'ADMIN': return 'badge-admin';
    case 'EMPLOYEE': return 'badge-employee';
    default: return 'badge-customer';
  }
};

const getRoleIcon = (role) => {
  switch (role) {
    case 'ADMIN': return 'bi bi-star-fill';
    case 'EMPLOYEE': return 'bi bi-person-gear';
    default: return 'bi bi-person';
  }
};

const getRoleText = (role) => {
  switch (role) {
    case 'ADMIN': return 'Admin';
    case 'EMPLOYEE': return 'Nhân viên';
    default: return 'Khách hàng';
  }
};

const formatDate = (dateString) => {
  if (!dateString) return '';
  const date = new Date(dateString);
  return date.toLocaleDateString('vi-VN', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric'
  });
};

const formatOrderId = (id) => {
  return `HD${String(id).padStart(4, '0')}`;
};

const getOrderStatusClass = (status) => {
  switch (status) {
    case 'Hoàn tất': return 'bg-success';
    case 'Đang giao': return 'bg-primary';
    case 'Đang xử lý': return 'bg-warning text-dark';
    case 'Đã từ chối': return 'bg-danger';
    case 'Hoàn hàng/trả hàng': return 'bg-secondary';
    case 'Báo lỗi': return 'bg-dark';
    default: return 'bg-secondary';
  }
};

// Lifecycle
onMounted(() => {
  getCurrentUser();
  loadUsers();
});
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

/* Stats Cards */
.stats-mini {
  padding: 25px 20px;
  border-radius: 12px;
  text-align: center;
  color: rgb(255, 255, 255);
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
  position: relative;
  overflow: hidden;
}

.stats-mini .stats-icon {
  font-size: 20px;
  opacity: 0.6;
  margin-bottom: 8px;
}

.stats-mini h4 {
  font-size: 32px;
  font-weight: bold;
  margin: 0;
}

.stats-mini p {
  margin: 8px 0 0;
  font-size: 14px;
  opacity: 0.9;
}

.bg-gradient-1 {
  background: linear-gradient(135deg, #212529, #000000);
}
.bg-gradient-2 {
  background: linear-gradient(135deg, #10b981, #059669);
}
.bg-gradient-3 {
  background: linear-gradient(135deg, #48cae4, #0096c7);
}
.bg-gradient-4 {
  background: linear-gradient(135deg, #ef4444, #dc2626);
}

/* Content Card */
.content-card {
  background: white;
  border-radius: 16px;
  padding: 30px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
}

.page-title {
  font-size: 24px;
  font-weight: bold;
  color: #333;
  margin-bottom: 5px;
}

.page-subtitle {
  color: #666;
  font-size: 14px;
  margin-bottom: 0;
}

.filter-section {
  background: #f8f9fa;
  padding: 20px;
  border-radius: 12px;
  margin-bottom: 25px;
  border: 1px solid #eee;
}

/* User Avatar */
.user-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 14px;
  flex-shrink: 0;
}

.user-avatar-lg {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 32px;
  margin: 0 auto;
}

.bg-admin {
  background: linear-gradient(135deg, #fbbf24, #ee5a24);
}
.bg-employee {
  background: linear-gradient(135deg, #48cae4, #0096c7);
}
.bg-customer {
  background: linear-gradient(135deg, #10b981, #059669);
}

/* Table */
.user-table thead th {
  background: #f8f9fa;
  border-bottom: 2px solid #dee2e6;
  font-size: 13px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  color: #6c757d;
  padding: 12px 16px;
  white-space: nowrap;
}

.user-table tbody td {
  padding: 14px 16px;
  vertical-align: middle;
  border-bottom: 1px solid #f0f0f0;
}

.user-table tbody tr {
  transition: all 0.2s;
}

.user-table tbody tr:hover {
  background: #f8f9ff;
}

/* Badges */
.role-badge {
  padding: 5px 12px;
  border-radius: 20px;
  font-size: 11px;
  font-weight: 600;
  letter-spacing: 0.3px;
}

.badge-admin {
  background: linear-gradient(135deg, #fbbf24, #ee5a24);
  color: white;
}

.badge-employee {
  background: linear-gradient(135deg, #48cae4, #0096c7);
  color: white;
}

.badge-customer {
  background: linear-gradient(135deg, #10b981, #059669);
  color: white;
}

.status-badge {
  padding: 5px 12px;
  border-radius: 20px;
  font-size: 11px;
  font-weight: 600;
}

.badge-active {
  background: rgba(16, 185, 129, 0.1);
  color: #059669;
}

.badge-inactive {
  background: rgba(239, 68, 68, 0.1);
  color: #dc2626;
}

/* Action Buttons */
.action-buttons {
  display: flex;
  gap: 6px;
}

/* Detail Modal */
.user-detail-card {
  background: #f8f9fa;
  border-radius: 12px;
  padding: 24px;
  border: 1px solid #eee;
}

.detail-item {
  margin-bottom: 12px;
}

.detail-item label {
  font-size: 11px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  color: #6c757d;
  font-weight: 600;
  margin-bottom: 4px;
}

.detail-item p {
  margin: 0;
  font-size: 14px;
  color: #333;
  font-weight: 500;
}

/* Section Title */
.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  padding-bottom: 10px;
  border-bottom: 2px solid #f0f0f0;
  margin-bottom: 16px;
}

/* Address List */
.address-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.address-item {
  background: #fff;
  border: 1px solid #e0e0e0;
  border-radius: 10px;
  padding: 14px 18px;
  transition: all 0.2s;
}

.address-item:hover {
  border-color: #c0c0c0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

/* Pagination */
.page-link {
  color: #333;
  border: 1px solid #dee2e6;
}

.page-item.active .page-link {
  background-color: #212529;
  border-color: #212529;
  color: white;
}

/* Form Switch */
.form-check-input:checked {
  background-color: #10b981;
  border-color: #10b981;
}

/* Responsive */
@media (max-width: 768px) {
  .main-content {
    margin-left: 0;
  }

  .page-container {
    padding: 15px;
  }

  .action-buttons {
    flex-direction: column;
  }
}
</style>