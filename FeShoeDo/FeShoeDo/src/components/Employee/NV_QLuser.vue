<template>
  <div class="employee-layout">
    <NV_Sidebar />
    
    <main class="main-content">
      <div class="page-container">
        <!-- Thông báo -->
        <div v-if="message" class="alert alert-success alert-dismissible fade show" role="alert">
          <span>{{ message }}</span>
          <button type="button" class="btn-close" @click="message = ''"></button>
        </div>

        <!-- Tabs -->
        <ul class="nav nav-tabs" role="tablist">
          <li class="nav-item" role="presentation">
            <a class="nav-link" :class="{ active: activeTab !== 'edit' }" 
                @click="setActiveTab('list')">
              <i class="bi bi-people-fill me-2"></i>Danh sách Users
            </a>
          </li>
          <li class="nav-item" role="presentation">
            <a class="nav-link" :class="{ active: activeTab === 'edit' }" 
                @click="setActiveTab('edit')">
              <i class="bi bi-pencil-square me-2"></i>Chỉnh sửa User
            </a>
          </li>
        </ul>

        <div class="tab-content p-3 border border-top-0">
          <!-- Tab Danh sách Users -->
          <div v-show="activeTab !== 'edit'" class="tab-pane show active">
            <!-- Filters -->
            <form @submit.prevent="fetchUsers" class="row g-2 mb-3">
              <div class="col-auto">
                <select class="form-select" v-model="filters.roleFilter" @change="fetchUsers">
                  <option value="">All</option>
                  <option value="NV">Nhân viên</option>
                  <option value="KH">Khách hàng</option>
                </select>
              </div>
              <div class="col-auto">
                <input type="text" v-model="filters.keyword" placeholder="Tìm theo email..." 
                        class="form-control">
              </div>
              <div class="col-auto">
                <button type="submit" class="btn btn-success">Tìm kiếm</button>
              </div>
            </form>

            <!-- Users Table -->
            <div v-if="loading" class="text-center p-5">
              <div class="spinner-border text-primary" role="status">
                <span class="visually-hidden">Loading...</span>
              </div>
            </div>

            <div v-else-if="error" class="alert alert-danger">{{ error }}</div>

            <div v-else>
              <div class="table-responsive">
                <table class="table table-bordered table-hover">
                  <thead class="table-dark">
                    <tr>
                      <th class="text-center">STT</th>
                      <th>Email</th>
                      <th>Tên</th>
                      <th>SĐT</th>
                      <th>Vai trò</th>
                      <th>Hành động</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="(user, index) in users" :key="user.userID">
                      <td class="text-center">{{ (currentPage * pageSize) + index + 1 }}</td>
                      <td>{{ user.mail }}</td>
                      <td>
                        <span v-if="user.nhanVien">{{ user.nhanVien.tenNV }}</span>
                        <span v-else-if="user.khachHang">{{ user.khachHang.tenKH }}</span>
                        <span v-else>N/A</span>
                      </td>
                      <td>
                        <span v-if="user.khachHang">{{ user.khachHang.sdt }}</span>
                        <span v-else>N/A</span>
                      </td>
                      <td>
                        <span v-if="user.nhanVien && user.nhanVien.vaiTro === 'Admin'" 
                              class="badge bg-danger">Admin</span>
                        <span v-else-if="user.nhanVien" class="badge bg-success">Nhân viên</span>
                        <span v-else-if="user.khachHang" class="badge bg-info">Khách hàng</span>
                      </td>
                      <td>
                        <!-- Chỉ hiển thị nút Edit nếu có quyền chỉnh sửa -->
                        <button class="btn btn-sm btn-outline-warning" 
                                @click="editUser(user)"
                                :disabled="!canEditUser(user)">
                          <i class="fas fa-edit"></i> Edit
                        </button>
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>

              <!-- Pagination -->
              <nav v-if="totalPages > 1" aria-label="Page navigation">
                <ul class="pagination justify-content-center">
                  <li class="page-item" :class="{ disabled: currentPage === 0 }">
                    <button class="page-link" @click="changePage(currentPage - 1)">Trước</button>
                  </li>
                  
                  <li v-for="page in visiblePages" :key="page" 
                      class="page-item" :class="{ active: page === currentPage }">
                    <button class="page-link" @click="changePage(page)">{{ page + 1 }}</button>
                  </li>
                  
                  <li class="page-item" :class="{ disabled: currentPage === totalPages - 1 }">
                    <button class="page-link" @click="changePage(currentPage + 1)">Sau</button>
                  </li>
                </ul>
              </nav>
            </div>
          </div>

          <!-- Tab Chỉnh sửa User -->
          <div v-show="activeTab === 'edit'" class="tab-pane show active">
            <form @submit.prevent="saveUser" class="form-section bg-light p-3 rounded">
              <input type="hidden" v-model="userForm.userID" />
              
              <div class="row mb-3">
                <div class="col-md-6">
                  <label class="form-label fw-bold">Email</label>
                  <input type="email" v-model="userForm.mail" class="form-control" 
                          placeholder="Nhập email..." required>
                </div>
                <div class="col-md-6">
                  <label class="form-label fw-bold">Password</label>
                  <input type="password" v-model="userForm.pass" class="form-control" 
                          :placeholder="userForm.userID ? 'Để trống nếu không đổi' : 'Nhập mật khẩu...'">
                </div>
              </div>
              
              <div class="row mb-3">
                <div class="col-md-6">
                  <label class="form-label fw-bold">Họ và tên</label>
                  <input type="text" v-model="userForm.fullname" class="form-control" 
                          placeholder="Nhập họ tên..." required>
                </div>
                <div class="col-md-6">
                  <label class="form-label fw-bold">Số điện thoại</label>
                  <input type="text" v-model="userForm.sdt" class="form-control" 
                          placeholder="Nhập SĐT...">
                </div>
              </div>
              
              <div class="mb-3">
                <label class="form-label fw-bold">Vai trò</label><br>
                <div class="form-check form-check-inline">
                  <input type="radio" id="role-nv" v-model="userForm.role" value="NV" 
                        class="form-check-input" 
                        :disabled="!!userForm.userID || !canCreateEmployee">
                  <label class="form-check-label" for="role-nv">Nhân viên</label>
                </div>
                <div class="form-check form-check-inline">
                  <input type="radio" id="role-kh" v-model="userForm.role" value="KH" 
                        class="form-check-input" 
                        :disabled="!!userForm.userID || !canCreateCustomer">
                  <label class="form-check-label" for="role-kh">Khách hàng</label>
                </div>
              </div>
              
              <div class="text-end">
                <button type="submit" class="btn btn-success me-2" v-if="!userForm.userID">
                  <i class="fas fa-plus"></i>Create
                </button>
                <button type="submit" class="btn btn-warning me-2" v-else>
                  <i class="fas fa-save"></i>Update
                </button>
                <button type="button" class="btn btn-danger me-2" @click="deleteUser" 
                        v-if="userForm.userID && canDeleteUser">
                  <i class="fas fa-trash"></i>Delete
                </button>
                <button type="button" class="btn btn-secondary" @click="resetForm">
                  <i class="fas fa-redo"></i>Reset
                </button>
              </div>
            </form>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script>
import { ref, onMounted, computed } from 'vue';
import api from '@/services/api';
import NV_Sidebar from '@/components/shared/NV_Sidebar.vue';
import { useAuthStore } from '@/stores/auth'; 

export default {
  name: 'QLUser',
  components: {
    NV_Sidebar
  },
  setup() {
    const users = ref([]);
    const loading = ref(false);
    const saving = ref(false);
    const error = ref('');
    const message = ref('');
    const activeTab = ref('list');
    const authStore = useAuthStore();

    const filters = ref({
      roleFilter: '',
      keyword: ''
    });
    const currentPage = ref(0);
    const pageSize = ref(15);
    const totalPages = ref(0);
    const totalElements = ref(0);
    
    const userForm = ref({
      userID: null,
      mail: '',
      pass: '',
      fullname: '',
      sdt: '',
      role: 'KH'
    });

    const visiblePages = computed(() => {
      const pages = [];
      const start = Math.max(0, currentPage.value - 2);
      const end = Math.min(totalPages.value - 1, currentPage.value + 2);
      
      for (let i = start; i <= end; i++) {
        pages.push(i);
      }
      return pages;
    });

    const canEditUser = computed(() => {
      return (user) => {
        const userRole = user.nhanVien ? 'NV' : 'KH';

        if (authStore.isAdmin) {
          return true;
        }

        if (authStore.isEmployee) {
          return userRole === 'KH';
        }
        
        return false;
      };
    });

    const canDeleteUser = computed(() => {
      if (authStore.isAdmin) {
        return true;
      }

      if (authStore.isEmployee) {
        return userForm.value.role === 'KH';
      }
      
      return false;
    });

    const canCreateEmployee = computed(() => {
      return authStore.isAdmin;
    });

    const canCreateCustomer = computed(() => {
      return authStore.isAdmin || authStore.isEmployee;
    });

    const setActiveTab = (tab) => {
      activeTab.value = tab;
      if (tab === 'list') {
        resetForm();
      }
    };

    const fetchUsers = async () => {
      loading.value = true;
      error.value = '';
      
      try {
        const params = {
          ...filters.value,
          page: currentPage.value,
          size: pageSize.value
        };
        
        const response = await api.getUsers(params);
        
        if (response.data.success) {
          users.value = response.data.users || [];
          totalPages.value = response.data.totalPages || 0;
          totalElements.value = response.data.totalElements || 0;
        } else {
          error.value = response.data.message || 'Không thể tải danh sách users';
        }
      } catch (err) {
        console.error('Error fetching users:', err);
        error.value = 'Lỗi kết nối máy chủ';
      } finally {
        loading.value = false;
      }
    };

    const changePage = (page) => {
      if (page >= 0 && page < totalPages.value) {
        currentPage.value = page;
        fetchUsers();
      }
    };

    const editUser = (user) => {
      const userRole = user.nhanVien ? 'NV' : 'KH';
      if (!canEditUser.value(user)) {
        showMessage('Bạn không có quyền chỉnh sửa user này', 'danger');
        return;
      }
      
      userForm.value = {
        userID: user.userID,
        mail: user.mail,
        pass: '',
        fullname: user.nhanVien?.tenNV || user.khachHang?.tenKH || '',
        sdt: user.khachHang?.sdt || '',
        role: userRole
      };
      activeTab.value = 'edit';
    };

    const saveUser = async () => {
      if (!userForm.value.mail || !userForm.value.fullname) {
        showMessage('Vui lòng điền đầy đủ thông tin', 'danger');
        return;
      }

      if (!userForm.value.userID) {
        if (userForm.value.role === 'NV' && !authStore.isAdmin) {
          showMessage('Chỉ admin mới có thể tạo tài khoản nhân viên', 'danger');
          return;
        }
        if (userForm.value.role === 'KH' && !(authStore.isAdmin || authStore.isEmployee)) {
          showMessage('Bạn không có quyền tạo khách hàng', 'danger');
          return;
        }
      }
      
      saving.value = true;
      try {
        let response;
        
        if (userForm.value.userID) {
          response = await api.updateUser(userForm.value.userID, userForm.value);
        } else {
          if (!userForm.value.pass) {
            showMessage('Vui lòng nhập mật khẩu cho user mới', 'danger');
            saving.value = false;
            return;
          }
          response = await api.createUser(userForm.value);
        }
        
        if (response.data.success) {
          showMessage(response.data.message || 'Lưu user thành công!', 'success');
          await fetchUsers();
          resetForm();
          activeTab.value = 'list';
        } else {
          showMessage(response.data.message || 'Lưu user thất bại', 'danger');
        }
      } catch (err) {
        console.error('Error saving user:', err);
        showMessage('Lỗi kết nối máy chủ', 'danger');
      } finally {
        saving.value = false;
      }
    };

    const deleteUser = async () => {
      if (!canDeleteUser.value) {
        showMessage('Bạn không có quyền xóa user này', 'danger');
        return;
      }
      
      if (!confirm('Bạn có chắc muốn xóa user này?')) return;
      
      try {
        const response = await api.deleteUser(userForm.value.userID);
        
        if (response.data.success) {
          showMessage(response.data.message || 'Xóa user thành công!', 'success');
          await fetchUsers();
          resetForm();
          activeTab.value = 'list';
        } else {
          showMessage(response.data.message || 'Xóa user thất bại', 'danger');
        }
      } catch (err) {
        console.error('Error deleting user:', err);
        showMessage('Lỗi kết nối máy chủ', 'danger');
      }
    };

    const resetForm = () => {
      userForm.value = {
        userID: null,
        mail: '',
        pass: '',
        fullname: '',
        sdt: '',
        role: 'KH'
      };
    };

    const resetFilters = () => {
      filters.value = {
        roleFilter: '',
        keyword: ''
      };
      currentPage.value = 0;
      fetchUsers();
    };

    const showMessage = (msg, type = 'success') => {
      message.value = msg;
      
      setTimeout(() => {
        message.value = '';
      }, 3000);
    };

    onMounted(() => {
      fetchUsers();
    });

    return {
      users,
      loading,
      saving,
      error,
      message,
      activeTab,
      filters,
      currentPage,
      pageSize,
      totalPages,
      visiblePages,
      userForm,
      canEditUser,
      canDeleteUser,
      canCreateEmployee,
      canCreateCustomer,
      setActiveTab,
      fetchUsers,
      changePage,
      editUser,
      saveUser,
      deleteUser,
      resetForm,
      resetFilters,
      showMessage
    };
  }
};
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

.nav-tabs {
  border-bottom: 2px solid #dee2e6;
}

.nav-tabs .nav-link {
  color: #666;
  font-weight: 500;
  border: none;
  border-bottom: 3px solid transparent;
  padding: 12px 24px;
  transition: all 0.3s;
  cursor: pointer;
}

.nav-tabs .nav-link:hover {
  color: #4f46e5;
  border-color: transparent;
}

.nav-tabs .nav-link.active {
  color: #4f46e5;
  background: transparent;
  border-color: #4f46e5;
}

.tab-content {
  background: white;
  padding: 25px;
  border-radius: 0 0 12px 12px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.08);
}

/* Style cho nút bị disabled */
.btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
</style>