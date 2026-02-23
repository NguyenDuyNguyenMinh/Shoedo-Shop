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

        <div v-if="error" class="alert alert-danger alert-dismissible fade show" role="alert">
          <span>{{ error }}</span>
          <button type="button" class="btn-close" @click="error = ''"></button>
        </div>

        <!-- Tabs Navigation -->
        <ul class="nav nav-tabs" id="ordersTab" role="tablist">
          <li class="nav-item" role="presentation">
            <button class="nav-link" :class="{ active: activeTab === 'pending' }" 
                    @click="setActiveTab('pending')">
              <i class="bi bi-clock-history me-2"></i>
              Chờ duyệt
              <span v-if="orderCounts.pending > 0" class="badge bg-warning ms-2">
                {{ orderCounts.pending }}
              </span>
            </button>
          </li>
          <li class="nav-item" role="presentation">
            <button class="nav-link" :class="{ active: activeTab === 'delivering' }" 
                    @click="setActiveTab('delivering')">
              <i class="bi bi-check-circle me-2"></i>
              Đã duyệt
              <span v-if="orderCounts.delivering > 0" class="badge bg-primary ms-2">
                {{ orderCounts.delivering }}
              </span>
            </button>
          </li>
          <li class="nav-item" role="presentation">
            <button class="nav-link" :class="{ active: activeTab === 'rejected' }" 
                    @click="setActiveTab('rejected')">
              <i class="bi bi-x-circle me-2"></i>
              Từ chối
              <span v-if="orderCounts.rejected > 0" class="badge bg-danger ms-2">
                {{ orderCounts.rejected }}
              </span>
            </button>
          </li>
          <li class="nav-item" role="presentation">
            <button class="nav-link" :class="{ active: activeTab === 'completed' }" 
                    @click="setActiveTab('completed')">
              <i class="bi bi-check-all me-2"></i>
              Hoàn tất
              <span v-if="orderCounts.completed > 0" class="badge bg-success ms-2">
                {{ orderCounts.completed }}
              </span>
            </button>
          </li>
        </ul>

        <!-- Tab Content -->
        <div class="tab-content p-3 bg-white rounded-bottom shadow-sm">
          <!-- Search bar for all tabs -->
          <div class="row g-2 mb-3">
            <div class="col-md-6">
              <div class="input-group">
                <span class="input-group-text bg-light fw-bold">
                  <i class="bi bi-search"></i>
                </span>
                <input type="text" v-model="searchKeyword" 
                       placeholder="Tìm theo mã HD (VD: HD0001) hoặc tên người nhận..." 
                       class="form-control" @input="handleSearch">
              </div>
            </div>
            <div class="col-md-2">
              <button class="btn btn-secondary" @click="resetSearch">
                <i class="bi bi-arrow-clockwise me-2"></i>Reset
              </button>
            </div>
          </div>
          
          <!-- Tab Chờ Duyệt -->
          <div class="tab-pane fade" :class="{ 'show active': activeTab === 'pending' }" v-show="activeTab === 'pending'">
            <div v-if="filteredOrders.pending.length === 0" class="text-muted text-center p-4">
              <i class="bi bi-inbox display-4 d-block mb-3"></i>
              <span v-if="searchKeyword">Không tìm thấy đơn hàng phù hợp</span>
              <span v-else>Không có đơn hàng chờ duyệt</span>
            </div>
            
            <div v-else class="row g-3">
              <div v-for="order in filteredOrders.pending" :key="order.maHD" class="col-12 col-md-6 col-lg-4">
                <div class="card h-100 border-dark">
                  <div class="card-header bg-dark text-white d-flex justify-content-between align-items-center">
                    <strong>{{ order.maHDStr }}</strong>
                    <span class="badge bg-warning">Chờ Duyệt</span>
                  </div>
                  <div class="card-body">
                    <div class="mb-2">
                      <small class="text-muted d-block">Ngày đặt:</small>
                      <strong>{{ formatDate(order.ngayMua) }}</strong>
                    </div>
                    
                    <div class="mb-2">
                      <small class="text-muted d-block">Người nhận:</small>
                      <strong>{{ order.tenNN }}</strong>
                    </div>
                    
                    <div class="mb-2">
                      <small class="text-muted d-block">SĐT:</small>
                      <strong>{{ order.sdt }}</strong>
                    </div>
                    
                    <div class="mb-3">
                      <small class="text-muted d-block">Địa chỉ:</small>
                      <small>{{ order.diemGiao }}</small>
                    </div>
                    
                    <hr>
                    
                    <div class="d-flex justify-content-between align-items-center mb-3">
                      <span class="text-muted">Tổng tiền:</span>
                      <strong class="text-primary fs-5">{{ formatPrice(order.tongTien) }}</strong>
                    </div>
                    
                    <div v-if="order.maNV" class="mb-2">
                      <small class="text-muted d-block">NV xử lý:</small>
                      <small>
                        {{ order.maNVStr }} - {{ order.emailNV }}
                      </small>
                    </div>
                    <div v-else class="mb-2">
                      <small class="text-muted">Chưa có NV xử lý</small>
                    </div>
                  </div>
                  <div class="card-footer bg-transparent">
                    <div class="d-flex justify-content-between gap-2">
                      <button class="btn btn-danger btn-sm flex-fill" 
                              @click="showRejectModal(order)"
                              :disabled="processing">
                        <i class="bi bi-x-lg me-1"></i> Từ chối
                      </button>
                      <button class="btn btn-success btn-sm flex-fill" 
                              @click="approveOrder(order.maHD)"
                              :disabled="processing">
                        <i class="bi bi-check-lg me-1"></i> Duyệt đơn
                      </button>
                    </div>
                    <button class="btn btn-outline-dark btn-sm w-100 mt-2" 
                            @click="showOrderDetail(order)">
                      <i class="bi bi-eye me-1"></i> Xem chi tiết
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Tab Đã Duyệt -->
          <div class="tab-pane fade" :class="{ 'show active': activeTab === 'delivering' }" v-show="activeTab === 'delivering'">
            <div v-if="filteredOrders.delivering.length === 0" class="text-muted text-center p-4">
              <i class="bi bi-truck display-4 d-block mb-3"></i>
              <span v-if="searchKeyword">Không tìm thấy đơn hàng phù hợp</span>
              <span v-else>Chưa có đơn hàng đang giao</span>
            </div>
            
            <div v-else class="row g-3">
              <div v-for="order in filteredOrders.delivering" :key="order.maHD" class="col-12 col-md-6 col-lg-4">
                <div class="card h-100 border-dark">
                  <div class="card-header bg-dark text-white d-flex justify-content-between align-items-center">
                    <strong>{{ order.maHDStr }}</strong>
                    <span class="badge bg-primary">Đang Giao</span>
                  </div>
                  <div class="card-body">
                    <div class="mb-2">
                      <small class="text-muted d-block">Ngày đặt:</small>
                      <strong>{{ formatDate(order.ngayMua) }}</strong>
                    </div>
                    
                    <div class="mb-2">
                      <small class="text-muted d-block">Người nhận:</small>
                      <strong>{{ order.tenNN }}</strong>
                    </div>
                    
                    <div class="mb-2">
                      <small class="text-muted d-block">SĐT:</small>
                      <strong>{{ order.sdt }}</strong>
                    </div>
                    
                    <div class="mb-3">
                      <small class="text-muted d-block">Địa chỉ:</small>
                      <small>{{ order.diemGiao }}</small>
                    </div>
                    
                    <hr>
                    
                    <div class="d-flex justify-content-between align-items-center mb-3">
                      <span class="text-muted">Tổng tiền:</span>
                      <strong class="text-primary fs-5">{{ formatPrice(order.tongTien) }}</strong>
                    </div>
                    
                    <div v-if="order.maNV" class="mb-2">
                      <small class="text-muted d-block">NV duyệt:</small>
                      <small>
                        {{ order.maNVStr }} - {{ order.emailNV }}
                      </small>
                    </div>
                  </div>
                  <div class="card-footer bg-transparent">
                    <button class="btn btn-outline-success btn-sm w-100" 
                            @click="markAsCompleted(order.maHD)">
                      <i class="bi bi-check-all me-1"></i> Đánh dấu hoàn tất
                    </button>
                    <button class="btn btn-outline-dark btn-sm w-100 mt-2" 
                            @click="showOrderDetail(order)">
                      <i class="bi bi-eye me-1"></i> Xem chi tiết
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Tab Đã Từ Chối -->
          <div class="tab-pane fade" :class="{ 'show active': activeTab === 'rejected' }" v-show="activeTab === 'rejected'">
            <div v-if="filteredOrders.rejected.length === 0" class="text-muted text-center p-4">
              <i class="bi bi-truck display-4 d-block mb-3"></i>
              <span v-if="searchKeyword">Không tìm thấy đơn hàng phù hợp</span>
              <span v-else>Không có đơn hàng bị từ chối</span>
            </div>
            
            <div v-else class="row g-3">
              <div v-for="order in filteredOrders.rejected" :key="order.maHD" class="col-12 col-md-6 col-lg-4">
                <div class="card h-100 border-dark">
                  <div class="card-header bg-dark text-white d-flex justify-content-between align-items-center">
                    <strong>{{ order.maHDStr }}</strong>
                    <span class="badge bg-danger">Đã Từ Chối</span>
                  </div>
                  <div class="card-body">
                    <div class="mb-2">
                      <small class="text-muted d-block">Ngày đặt:</small>
                      <strong>{{ formatDate(order.ngayMua) }}</strong>
                    </div>
                    
                    <div class="mb-2">
                      <small class="text-muted d-block">Người nhận:</small>
                      <strong>{{ order.tenNN }}</strong>
                    </div>
                    
                    <div class="mb-2">
                      <small class="text-muted d-block">SĐT:</small>
                      <strong>{{ order.sdt }}</strong>
                    </div>
                    
                    <div class="mb-3">
                      <small class="text-muted d-block">Địa chỉ:</small>
                      <small>{{ order.diemGiao }}</small>
                    </div>
                    
                    <hr>
                    
                    <div class="d-flex justify-content-between align-items-center mb-3">
                      <span class="text-muted">Tổng tiền:</span>
                      <strong class="text-primary fs-5">{{ formatPrice(order.tongTien) }}</strong>
                    </div>
                    
                    <div v-if="order.maNV" class="mb-2">
                      <small class="text-muted d-block">NV từ chối:</small>
                      <small>
                        {{ order.maNVStr }} - {{ order.emailNV }}
                      </small>
                    </div>
                  </div>
                  <div class="card-footer bg-transparent">
                    <button class="btn btn-outline-dark btn-sm w-100" 
                            @click="showOrderDetail(order)">
                      <i class="bi bi-eye me-1"></i> Xem chi tiết
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Tab Hoàn Tất -->
          <div class="tab-pane fade" :class="{ 'show active': activeTab === 'completed' }" v-show="activeTab === 'completed'">
            <div v-if="filteredOrders.completed.length === 0" class="text-muted text-center p-4">
              <i class="bi bi-check2-all display-4 d-block mb-3"></i>
              <span v-if="searchKeyword">Không tìm thấy đơn hàng phù hợp</span>
              <span v-else>Chưa có đơn hàng hoàn tất</span>
            </div>
            
            <div v-else>
              <div class="table-responsive">
                <table class="table table-bordered table-hover">
                  <thead class="table-dark">
                    <tr>
                      <th>Mã HD</th>
                      <th>Ngày mua</th>
                      <th>Người nhận</th>
                      <th>SĐT</th>
                      <th>Địa chỉ</th>
                      <th>Tổng tiền</th>
                      <th></th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="order in filteredOrders.completed" :key="order.maHD">
                      <td>
                        <strong>{{ order.maHDStr }}</strong>
                      </td>
                      <td>{{ formatDate(order.ngayMua) }}</td>
                      <td>{{ order.tenNN }}</td>
                      <td>{{ order.sdt }}</td>
                      <td>{{ truncateText(order.diemGiao, 30) }}</td>
                      <td class="fw-bold text-primary text-end">{{ formatPrice(order.tongTien) }}</td>
                      <td>
                        <button class="btn btn-sm btn-outline-dark" 
                                @click="showOrderDetail(order)">
                          <i class="bi bi-eye"></i>
                        </button>
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        </div>
      </div>
    </main>

    <!-- Order Detail Modal -->
    <div class="modal fade" id="orderDetailModal" tabindex="-1" aria-hidden="true">
      <div class="modal-dialog modal-lg">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">Chi tiết đơn hàng #{{ selectedOrder?.maHDStr }}</h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
          </div>
          <div class="modal-body">
            <div v-if="orderDetail">
              <!-- Order Info -->
              <div class="row mb-4">
                <div class="col-md-6">
                  <h6>Thông tin đơn hàng</h6>
                  <table class="table table-sm bg-dark">
                    <tbody>
                      <tr>
                        <td><strong>Mã HD:</strong></td>
                        <td>{{ orderDetail.maHDStr }}</td>
                      </tr>
                      <tr>
                        <td><strong>Ngày mua:</strong></td>
                        <td>{{ formatDate(orderDetail.ngayMua) }}</td>
                      </tr>
                      <tr>
                        <td><strong>Trạng thái:</strong></td>
                        <td>
                          <span :class="getStatusBadgeClass(orderDetail.trangThai)" 
                                class="badge">
                            {{ orderDetail.trangThai }}
                          </span>
                        </td>
                      </tr>
                      <tr>
                        <td><strong>Tổng tiền:</strong></td>
                        <td class="fw-bold text-primary">{{ formatPrice(orderDetail.tongTien) }}</td>
                      </tr>
                    </tbody>
                  </table>
                </div>
                <div class="col-md-6">
                  <h6>Thông tin giao hàng</h6>
                  <table class="table table-sm">
                    <tbody>
                      <tr>
                        <td><strong>Người nhận:</strong></td>
                        <td>{{ orderDetail.tenNN }}</td>
                      </tr>
                      <tr>
                        <td><strong>SĐT:</strong></td>
                        <td>{{ orderDetail.sdt }}</td>
                      </tr>
                      <tr>
                        <td><strong>Địa chỉ:</strong></td>
                        <td>{{ orderDetail.diemGiao }}</td>
                      </tr>
                    </tbody>
                  </table>
                </div>
              </div>
              
              <!-- Order Items -->
              <h6>Chi tiết sản phẩm</h6>
              <div class="table-responsive">
                <table class="table table-bordered table-hover">
                  <thead class="table-dark">
                    <tr>
                      <th>STT</th>
                      <th>Sản phẩm</th>
                      <th>Phân loại</th>
                      <th>SL</th>
                      <th>Đơn giá</th>
                      <th>Thành tiền</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="(item, index) in orderDetail.chiTiet" :key="item.maPLSP">
                      <td>{{ index + 1 }}</td>
                      <td>{{ item.tenSP }}</td>
                      <td>{{ item.phanLoai }}</td>
                      <td>{{ item.soLuong }}</td>
                      <td class="text-end">{{ formatPrice(item.donGia) }}</td>
                      <td class="fw-bold text-end">{{ formatPrice(item.thanhTien) }}</td>
                    </tr>
                  </tbody>
                  <tfoot>
                    <tr>
                      <td colspan="5" class="text-end"><strong>Tổng cộng:</strong></td>
                      <td class="fw-bold text-primary text-end">{{ formatPrice(orderDetail.tongTien) }}</td>
                    </tr>
                  </tfoot>
                </table>
              </div>
              
              <!-- Staff Info -->
              <div v-if="orderDetail.maNV" class="mt-4 p-3 bg-light rounded">
                <h6>Nhân viên xử lý</h6>
                <div class="row">
                  <div class="col-md-6">
                    <strong>Mã NV:</strong> {{ orderDetail.maNVStr }}<br>
                    <strong>Tên NV:</strong> {{ orderDetail.tenNV }}
                  </div>
                  <div class="col-md-6">
                    <strong>Email:</strong> {{ orderDetail.emailNV }}
                  </div>
                </div>
              </div>
            </div>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Đóng</button>
          </div>
        </div>
      </div>
    </div>

    <!-- Reject Modal -->
    <div class="modal fade" id="rejectModal" tabindex="-1" aria-hidden="true">
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">Từ chối đơn hàng</h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
          </div>
          <div class="modal-body">
            <p>Bạn có chắc muốn từ chối đơn hàng <strong>{{ orderToReject?.maHDStr }}</strong>?</p>
            <p class="text-danger">
              <i class="bi bi-exclamation-triangle me-1"></i>
              Hệ thống sẽ hoàn trả số lượng sản phẩm về kho.
            </p>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Hủy</button>
            <button type="button" class="btn btn-danger" 
                    @click="confirmRejectOrder" 
                    :disabled="processing">
              <span v-if="processing" class="spinner-border spinner-border-sm me-2"></span>
              Xác nhận từ chối
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, nextTick, computed } from 'vue';
import api from '@/services/api';
import NV_Sidebar from '@/components/shared/NV_Sidebar.vue';
import { Modal } from 'bootstrap';

export default {
  name: 'QLDonHang',
  components: {
    NV_Sidebar
  },
  setup() {
    const activeTab = ref('pending');
    const orders = ref({
      pending: [],     
      delivering: [],   
      rejected: [],    
      completed: []     
    });
    const orderCounts = ref({
      pending: 0,
      delivering: 0,
      rejected: 0,
      completed: 0
    });
    const loading = ref(false);
    const processing = ref(false);
    const searchKeyword = ref('');
    const selectedOrder = ref(null);
    const orderDetail = ref(null);
    const orderToReject = ref(null);
    const rejectReason = ref('');
    
    let orderDetailModal = null;
    let rejectModal = null;

    const filteredOrders = computed(() => {
      if (!searchKeyword.value.trim()) {
        return orders.value;
      }
      
      const keyword = searchKeyword.value.trim().toLowerCase();
      
      const filterByKeyword = (orderList) => {
        return orderList.filter(order => {
          if (order.maHDStr && order.maHDStr.toLowerCase().includes(keyword)) {
            return true;
          }

          if (order.maHD && order.maHD.toString().includes(keyword)) {
            return true;
          }
          
          if (order.tenNN && order.tenNN.toLowerCase().includes(keyword)) {
            return true;
          }
          
          if (order.sdt && order.sdt.includes(keyword)) {
            return true;
          }

          if (order.diemGiao && order.diemGiao.toLowerCase().includes(keyword)) {
            return true;
          }
          
          return false;
        });
      };
      
      return {
        pending: filterByKeyword(orders.value.pending),
        delivering: filterByKeyword(orders.value.delivering),
        rejected: filterByKeyword(orders.value.rejected),
        completed: filterByKeyword(orders.value.completed)
      };
    });

    const setActiveTab = async (tab) => {
      activeTab.value = tab;
      await fetchOrders();
    };

    const handleSearch = () => {
      clearTimeout(searchTimeout);
      searchTimeout = setTimeout(() => {
      }, 300);
    };

    let searchTimeout = null;

    const resetSearch = () => {
      searchKeyword.value = '';
    };

    const fetchOrders = async () => {
      loading.value = true;
      try {
        const response = await api.getAllOrders();      
        if (response.data.success) {
          const data = response.data.data;
          
          orders.value = {
            pending: data.choduyet || [],
            delivering: data.danggiao || [],
            rejected: data.datuchoi || [],
            completed: data.giaothanhcong || []
          };
          
          orderCounts.value = {
            pending: data.choduyet?.length || 0,
            delivering: data.danggiao?.length || 0,
            rejected: data.datuchoi?.length || 0,
            completed: data.giaothanhcong?.length || 0
          };
        } else {
          console.error('API error:', response.data.message);
          alert('Không thể tải danh sách đơn hàng: ' + response.data.message);
        }
      } catch (error) {
        console.error('Error fetching orders:', error);
        alert('Lỗi khi tải danh sách đơn hàng: ' + error.message);
      } finally {
        loading.value = false;
      }
    };

    const approveOrder = async (orderId) => {
      if (!confirm('Bạn có chắc muốn duyệt đơn hàng này?')) return;
      
      processing.value = true;
      try {
        const response = await api.approveOrder(orderId);
        
        if (response.data.success) {
          alert('Đã duyệt đơn hàng thành công!');
          await fetchOrders();
        } else {
          alert(response.data.message || 'Duyệt đơn hàng thất bại');
        }
      } catch (error) {
        console.error('Error approving order:', error);
        alert('Lỗi khi duyệt đơn hàng');
      } finally {
        processing.value = false;
      }
    };

    const showRejectModal = (order) => {
      orderToReject.value = order;
      rejectReason.value = '';
      rejectModal.show();
    };

    const confirmRejectOrder = async () => {
      if (!orderToReject.value) return;
      
      processing.value = true;
      try {
        const response = await api.rejectOrder(orderToReject.value.maHD);
        
        if (response.data.success) {
          alert('Đã từ chối đơn hàng và hoàn trả số lượng sản phẩm!');
          rejectModal.hide();
          await fetchOrders();
        } else {
          alert(response.data.message || 'Từ chối đơn hàng thất bại');
        }
      } catch (error) {
        console.error('Error rejecting order:', error);
        alert('Lỗi khi từ chối đơn hàng');
      } finally {
        processing.value = false;
      }
    };

    const markAsCompleted = async (orderId) => {
      if (!confirm('Đánh dấu đơn hàng này là đã giao thành công?')) return;
      
      processing.value = true;
      try {
        const response = await api.updateOrderStatus(orderId, 'Giao thành công');
        
        if (response.data.success) {
          alert('Đã cập nhật trạng thái đơn hàng!');
          await fetchOrders();
        } else {
          alert(response.data.message || 'Cập nhật trạng thái thất bại');
        }
      } catch (error) {
        console.error('Error updating order status:', error);
        alert('Lỗi khi cập nhật trạng thái');
      } finally {
        processing.value = false;
      }
    };

    const showOrderDetail = async (order) => {
      selectedOrder.value = order;
      
      try {
        const response = await api.getEmployeeOrderDetail(order.maHD);
        
        if (response.data.success) {
          orderDetail.value = response.data.order;
          orderDetailModal.show();
        } else {
          alert('Không thể tải chi tiết đơn hàng');
        }
      } catch (error) {
        console.error('Error fetching order detail:', error);
        alert('Lỗi khi tải chi tiết đơn hàng');
      }
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
        year: 'numeric'
      });
    };

    const truncateText = (text, length) => {
      if (!text) return '';
      return text.length > length ? text.substring(0, length) + '...' : text;
    };

    const getStatusBadgeClass = (status) => {
      switch(status) {
        case 'Chờ duyệt': return 'bg-warning';
        case 'Đang giao': return 'bg-primary';
        case 'Đã từ chối': return 'bg-danger';
        case 'Giao thành công': return 'bg-success';
        default: return 'bg-secondary';
      }
    };

    onMounted(async () => {
      await fetchOrders();
      
      nextTick(() => {
        orderDetailModal = new Modal(document.getElementById('orderDetailModal'));
        rejectModal = new Modal(document.getElementById('rejectModal'));
      });
    });

    return {
      activeTab,
      orders,
      orderCounts,
      loading,
      processing,
      selectedOrder,
      orderDetail,
      orderToReject,
      rejectReason,
      searchKeyword,
      filteredOrders,
      
      setActiveTab,
      approveOrder,
      showRejectModal,
      confirmRejectOrder,
      markAsCompleted,
      showOrderDetail,
      handleSearch,
      resetSearch,
      formatPrice,
      formatDate,
      truncateText,
      getStatusBadgeClass
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
  margin-bottom: 0;
}

.nav-tabs .nav-link {
  color: #666;
  font-weight: 500;
  border: none;
  border-bottom: 3px solid transparent;
  padding: 12px 24px;
  transition: all 0.3s;
  background: none;
  border-radius: 0;
}

.nav-tabs .nav-link:hover {
  color: #ee4d2d;
  border-color: transparent;
}

.nav-tabs .nav-link.active {
  color: #ee4d2d;
  background: transparent;
  border-color: #ee4d2d;
}

.tab-content {
  border: 1px solid #dee2e6;
  border-top: none;
  border-radius: 0 0 8px 8px;
}

.card {
  transition: transform 0.2s, box-shadow 0.2s;
  border-radius: 10px;
  overflow: hidden;
}

.card:hover {
  transform: translateY(-5px);
  box-shadow: 0 5px 20px rgba(0,0,0,0.1);
}

.card-header {
  font-size: 0.9rem;
}

.badge {
  font-size: 0.75em;
  padding: 0.35em 0.65em;
}

.input-group-text {
  background-color: #f8f9fa;
  border-color: #dee2e6;
}

.text-muted.text-center {
  padding: 60px 20px;
}
</style>