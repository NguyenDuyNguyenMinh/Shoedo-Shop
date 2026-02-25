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

        <!-- Thống kê nhanh -->
        <div class="row mb-4">
          <div class="col-md-2">
            <div class="card text-white" :class="getStatCardClass('pending')" @click="setActiveTab('pending')" style="cursor: pointer;">
              <div class="card-body text-center">
                <h5 class="card-title">Chờ duyệt</h5>
                <h2 class="mb-0">{{ orderCounts.pending }}</h2>
              </div>
            </div>
          </div>
          <div class="col-md-2">
            <div class="card text-white" :class="getStatCardClass('delivering')" @click="setActiveTab('delivering')" style="cursor: pointer;">
              <div class="card-body text-center">
                <h5 class="card-title">Đang giao</h5>
                <h2 class="mb-0">{{ orderCounts.delivering }}</h2>
              </div>
            </div>
          </div>
          <div class="col-md-2">
            <div class="card text-white" :class="getStatCardClass('completed')" @click="setActiveTab('completed')" style="cursor: pointer;">
              <div class="card-body text-center">
                <h5 class="card-title">Hoàn tất</h5>
                <h2 class="mb-0">{{ orderCounts.completed }}</h2>
              </div>
            </div>
          </div>
          <div class="col-md-2">
            <div class="card text-white" :class="getStatCardClass('rejected')" @click="setActiveTab('rejected')" style="cursor: pointer;">
              <div class="card-body text-center">
                <h5 class="card-title">Từ chối</h5>
                <h2 class="mb-0">{{ orderCounts.rejected }}</h2>
              </div>
            </div>
          </div>
          <div class="col-md-2">
            <div class="card text-white" :class="getStatCardClass('error')" @click="setActiveTab('error')" style="cursor: pointer;">
              <div class="card-body text-center">
                <h5 class="card-title">Báo lỗi</h5>
                <h2 class="mb-0">{{ orderCounts.error }}</h2>
              </div>
            </div>
          </div>
          <div class="col-md-2">
            <div class="card text-white" :class="getStatCardClass('return')" @click="setActiveTab('return')" style="cursor: pointer;">
              <div class="card-body text-center">
                <h5 class="card-title">Hoàn trả</h5>
                <h2 class="mb-0">{{ orderCounts.return }}</h2>
              </div>
            </div>
          </div>
        </div>

        <!-- Tabs Navigation -->
        <ul class="nav nav-tabs" id="ordersTab" role="tablist">
          <li class="nav-item" role="presentation">
            <button class="nav-link" :class="{ active: activeTab === 'pending' }" 
                    @click="setActiveTab('pending')">
              Chờ duyệt
              <span v-if="orderCounts.pending > 0" class="badge bg-warning ms-2">
                {{ orderCounts.pending }}
              </span>
            </button>
          </li>
          <li class="nav-item" role="presentation">
            <button class="nav-link" :class="{ active: activeTab === 'delivering' }" 
                    @click="setActiveTab('delivering')">
              Đang giao
              <span v-if="orderCounts.delivering > 0" class="badge bg-primary ms-2">
                {{ orderCounts.delivering }}
              </span>
            </button>
          </li>
          <li class="nav-item" role="presentation">
            <button class="nav-link" :class="{ active: activeTab === 'completed' }" 
                    @click="setActiveTab('completed')">
              Hoàn tất
              <span v-if="orderCounts.completed > 0" class="badge bg-success ms-2">
                {{ orderCounts.completed }}
              </span>
            </button>
          </li>
          <li class="nav-item" role="presentation">
            <button class="nav-link" :class="{ active: activeTab === 'rejected' }" 
                    @click="setActiveTab('rejected')">
              Từ chối
              <span v-if="orderCounts.rejected > 0" class="badge bg-danger ms-2">
                {{ orderCounts.rejected }}
              </span>
            </button>
          </li>
          <li class="nav-item" role="presentation">
            <button class="nav-link" :class="{ active: activeTab === 'error' }" 
                    @click="setActiveTab('error')">
              Báo lỗi
              <span v-if="orderCounts.error > 0" class="badge bg-info ms-2">
                {{ orderCounts.error }}
              </span>
            </button>
          </li>
          <li class="nav-item" role="presentation">
            <button class="nav-link" :class="{ active: activeTab === 'return' }" 
                    @click="setActiveTab('return')">
              Hoàn trả
              <span v-if="orderCounts.return > 0" class="badge bg-secondary ms-2">
                {{ orderCounts.return }}
              </span>
            </button>
          </li>
        </ul>

        <!-- Tab Content -->
        <div class="tab-content p-3 bg-white rounded-bottom shadow-sm">
          <!-- Search bar -->
          <div class="row g-2 mb-3">
            <div class="col-md-4">
              <div class="input-group">
                <span class="input-group-text bg-light">Tìm</span>
                <input type="text" v-model="searchKeyword" 
                       placeholder="Mã HD, tên KH, SĐT..." 
                       class="form-control">
              </div>
            </div>
            <div class="col-md-2">
              <select v-model="filterPayment" class="form-select">
                <option value="">Tất cả TT</option>
                <option value="COD">COD</option>
                <option value="Chuyển khoản">Chuyển khoản</option>
              </select>
            </div>
            <div class="col-md-2">
              <input type="date" v-model="filterDate" class="form-control">
            </div>
            <div class="col-md-2">
              <button class="btn btn-secondary" @click="resetFilters">Reset</button>
            </div>
          </div>
          
          <!-- Tab Chờ Duyệt -->
          <div v-show="activeTab === 'pending'">
            <div v-if="filteredOrders.pending.length === 0" class="text-center p-5 text-muted">
              Không có đơn hàng chờ duyệt
            </div>
            
            <div v-else class="row g-3">
              <div v-for="order in filteredOrders.pending" :key="order.maHD" class="col-12 col-md-6 col-lg-4">
                <div class="card h-100 border-dark">
                  <div class="card-header bg-dark text-white d-flex justify-content-between align-items-center">
                    <strong>#HD{{ String(order.maHD).padStart(4, '0') }}</strong>
                    <span class="badge bg-warning text-white">Chờ duyệt</span>
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
                    
                    <div class="mb-2">
                      <small class="text-muted d-block">Thanh toán:</small>
                      <span :class="order.phuongThucTT === 'COD' ? 'badge bg-info' : 'badge bg-success'">
                        {{ order.phuongThucTT }}
                      </span>
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
                      <small>#NV{{ String(order.maNV).padStart(2, '0') }} - {{ order.emailNV }}</small>
                    </div>
                    <div v-else class="mb-2">
                      <small class="text-muted">Chưa có NV xử lý</small>
                    </div>
                  </div>
                  <div class="card-footer bg-transparent">
                    <div class="d-flex gap-2">
                      <button class="btn btn-danger btn-sm flex-fill" @click="showRejectModal(order)">
                        Từ chối
                      </button>
                      <button class="btn btn-success btn-sm flex-fill" @click="approveOrder(order.maHD)">
                        Duyệt đơn
                      </button>
                    </div>
                    <button class="btn btn-outline-dark btn-sm w-100 mt-2" @click="showOrderDetail(order)">
                      Xem chi tiết
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Tab Đang Giao -->
          <div v-show="activeTab === 'delivering'">
            <div v-if="filteredOrders.delivering.length === 0" class="text-center p-5 text-muted">
              Không có đơn hàng đang giao
            </div>
            
            <div v-else class="row g-3">
              <div v-for="order in filteredOrders.delivering" :key="order.maHD" class="col-12 col-md-6 col-lg-4">
                <div class="card h-100 border-dark">
                  <div class="card-header bg-dark text-white d-flex justify-content-between align-items-center">
                    <strong>#HD{{ String(order.maHD).padStart(4, '0') }}</strong>
                    <span class="badge bg-primary text-white">Đang giao</span>
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
                    
                    <div class="mb-2">
                      <small class="text-muted d-block">Thanh toán:</small>
                      <span :class="order.phuongThucTT === 'COD' ? 'badge bg-info' : 'badge bg-success'">
                        {{ order.phuongThucTT }}
                      </span>
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
                    
                    <div class="mb-2">
                      <small class="text-muted d-block">NV duyệt:</small>
                      <small>#NV{{ String(order.maNV).padStart(2, '0') }} - {{ order.emailNV }}</small>
                    </div>
                  </div>
                  <div class="card-footer bg-transparent">
                    <button class="btn btn-success btn-sm w-100 mb-2" @click="markAsCompleted(order.maHD)">
                      Đánh dấu hoàn tất
                    </button>
                    <button class="btn btn-outline-primary btn-sm w-100" @click="showOrderDetail(order)">
                      Xem chi tiết
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Tab Hoàn Tất -->
          <div v-show="activeTab === 'completed'">
            <div v-if="filteredOrders.completed.length === 0" class="text-center p-5 text-muted">
              Chưa có đơn hàng hoàn tất
            </div>
            
            <div v-else class="row g-3">
              <div v-for="order in filteredOrders.completed" :key="order.maHD" class="col-12 col-md-6 col-lg-4">
                <div class="card h-100 border-dark">
                  <div class="card-header bg-dark text-white d-flex justify-content-between align-items-center">
                    <strong>#HD{{ String(order.maHD).padStart(4, '0') }}</strong>
                    <span class="badge bg-success text-white">Hoàn tất</span>
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
                    
                    <div class="mb-2">
                      <small class="text-muted d-block">Thanh toán:</small>
                      <span :class="order.phuongThucTT === 'COD' ? 'badge bg-info' : 'badge bg-success'">
                        {{ order.phuongThucTT }}
                      </span>
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
                    
                    <div class="mb-2">
                      <small class="text-muted d-block">NV duyệt:</small>
                      <small>#NV{{ String(order.maNV).padStart(2, '0') }} - {{ order.emailNV }}</small>
                    </div>
                  </div>
                  <div class="card-footer bg-transparent">
                    <button class="btn btn-outline-success btn-sm w-100" @click="showOrderDetail(order)">
                      Xem chi tiết
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Tab Từ Chối -->
          <div v-show="activeTab === 'rejected'">
            <div v-if="filteredOrders.rejected.length === 0" class="text-center p-5 text-muted">
              Không có đơn hàng bị từ chối
            </div>
            
            <div v-else class="row g-3">
              <div v-for="order in filteredOrders.rejected" :key="order.maHD" class="col-12 col-md-6 col-lg-4">
                <div class="card h-100 border-dark">
                  <div class="card-header bg-dark text-white text-white d-flex justify-content-between align-items-center">
                    <strong>#HD{{ String(order.maHD).padStart(4, '0') }}</strong>
                    <span class="badge bg-danger text-white">Từ chối</span>
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
                    
                    <div class="mb-2">
                      <small class="text-muted d-block">Thanh toán:</small>
                      <span :class="order.phuongThucTT === 'COD' ? 'badge bg-info' : 'badge bg-success'">
                        {{ order.phuongThucTT }}
                      </span>
                    </div>
                    
                    <div class="mb-3">
                      <small class="text-muted d-block">Địa chỉ:</small>
                      <small>{{ order.diemGiao }}</small>
                    </div>
                    
                    <div class="mb-3">
                      <small class="text-muted d-block">Ghi chú:</small>
                      <small class="text-danger fw-bold">{{ order.ghiChu }}</small>
                    </div>
                    
                    <hr>
                    
                    <div class="d-flex justify-content-between align-items-center mb-3">
                      <span class="text-muted">Tổng tiền:</span>
                      <strong class="text-primary fs-5">{{ formatPrice(order.tongTien) }}</strong>
                    </div>
                  </div>
                  <div class="card-footer bg-transparent">
                    <button class="btn btn-outline-danger btn-sm w-100" @click="showOrderDetail(order)">
                      Xem chi tiết
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Tab Báo Lỗi -->
          <div v-show="activeTab === 'error'">
            <div v-if="filteredOrders.error.length === 0" class="text-center p-5 text-muted">
              Không có đơn hàng báo lỗi
            </div>
            
            <div v-else class="row g-3">
              <div v-for="order in filteredOrders.error" :key="order.maHD" class="col-12 col-md-6 col-lg-4">
                <div class="card h-100 border-dark">
                  <div class="card-header bg-dark text-white d-flex justify-content-between align-items-center">
                    <strong>#HD{{ String(order.maHD).padStart(4, '0') }}</strong>
                    <span class="badge bg-info text-white">Báo lỗi</span>
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
                    
                    <div class="mb-2">
                      <small class="text-muted d-block">Thanh toán:</small>
                      <span :class="order.phuongThucTT === 'COD' ? 'badge bg-info' : 'badge bg-success'">
                        {{ order.phuongThucTT }}
                      </span>
                    </div>
                    
                    <div class="mb-3">
                      <small class="text-muted d-block">Địa chỉ:</small>
                      <small>{{ order.diemGiao }}</small>
                    </div>
                    
                    <div class="mb-3">
                      <small class="text-muted d-block">Ghi chú lỗi:</small>
                      <small class="text-info fw-bold">{{ order.ghiChu }}</small>
                    </div>
                    
                    <hr>
                    
                    <div class="d-flex justify-content-between align-items-center mb-3">
                      <span class="text-muted">Tổng tiền:</span>
                      <strong class="text-primary fs-5">{{ formatPrice(order.tongTien) }}</strong>
                    </div>
                  </div>
                  <div class="card-footer bg-transparent">
                    <button class="btn btn-outline-info btn-sm w-100" @click="showOrderDetail(order)">
                      Xem chi tiết
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Tab Hoàn Trả -->
          <div v-show="activeTab === 'return'">
            <div v-if="filteredOrders.return.length === 0" class="text-center p-5 text-muted">
              Không có đơn hàng hoàn trả
            </div>
            
            <div v-else class="row g-3">
              <div v-for="order in filteredOrders.return" :key="order.maHD" class="col-12 col-md-6 col-lg-4">
                <div class="card h-100 border-dark">
                  <div class="card-header bg-dark text-white d-flex justify-content-between align-items-center">
                    <strong>#HD{{ String(order.maHD).padStart(4, '0') }}</strong>
                    <span class="badge bg-secondary text-white">Hoàn trả</span>
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
                    
                    <div class="mb-2">
                      <small class="text-muted d-block">Thanh toán:</small>
                      <span :class="order.phuongThucTT === 'COD' ? 'badge bg-info' : 'badge bg-success'">
                        {{ order.phuongThucTT }}
                      </span>
                    </div>
                    
                    <div class="mb-3">
                      <small class="text-muted d-block">Địa chỉ:</small>
                      <small>{{ order.diemGiao }}</small>
                    </div>
                    
                    <div class="mb-3">
                      <small class="text-muted d-block">Ghi chú hoàn trả:</small>
                      <small class="text-secondary fw-bold">{{ order.ghiChu }}</small>
                    </div>
                    
                    <hr>
                    
                    <div class="d-flex justify-content-between align-items-center mb-3">
                      <span class="text-muted">Tổng tiền:</span>
                      <strong class="text-primary fs-5">{{ formatPrice(order.tongTien) }}</strong>
                    </div>
                  </div>
                  <div class="card-footer bg-transparent">
                    <button class="btn btn-outline-secondary btn-sm w-100" @click="showOrderDetail(order)">
                      Xem chi tiết
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </main>

    <!-- Order Detail Modal -->
    <div class="modal fade" id="orderDetailModal" tabindex="-1" aria-hidden="true" ref="orderDetailModal">
    <div class="modal-dialog modal-lg">
      <div class="modal-content">
        <div class="modal-header">
          <h5 class="modal-title">Chi tiết đơn hàng #HD{{ selectedOrder ? String(selectedOrder.maHD).padStart(4, '0') : '' }}</h5>
          <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
        </div>
        <div class="modal-body">
          <div v-if="orderDetail">
            <!-- Thông tin đơn hàng và giao hàng - Dạng div thay vì table để tránh lỗi -->
            <div class="row mb-4">
              <div class="col-md-5">
                <h6 class="fw-bold">📋 Thông tin đơn hàng</h6>
                <div class="border p-3 rounded">
                  <div class="d-flex mb-2">
                    <div style="width: 100px;"><strong>Mã HD:</strong></div>
                    <div>#HD{{ String(orderDetail.maHD).padStart(4, '0') }}</div>
                  </div>
                  <div class="d-flex mb-2">
                    <div style="width: 100px;"><strong>Ngày mua:</strong></div>
                    <div>{{ formatDate(orderDetail.ngayMua) }}</div>
                  </div>
                  <div class="d-flex mb-2">
                    <div style="width: 100px;"><strong>Trạng thái:</strong></div>
                    <div>
                      <span :class="'badge ' + getStatusBadgeClass(orderDetail.trangThai)">
                        {{ orderDetail.trangThai }}
                      </span>
                    </div>
                  </div>
                  <div class="d-flex mb-2">
                    <div style="width: 100px;"><strong>Thanh toán:</strong></div>
                    <div>
                      <span :class="orderDetail.phuongThucTT === 'COD' ? 'badge bg-info' : 'badge bg-success'">
                        {{ orderDetail.phuongThucTT }}
                      </span>
                    </div>
                  </div>
                  <div class="d-flex">
                    <div style="width: 100px;"><strong>Tổng tiền:</strong></div>
                    <div class="fw-bold text-primary">{{ formatPrice(orderDetail.tongTien) }}</div>
                  </div>
                </div>
              </div>
              
              <!-- Thông tin giao hàng -->
              <div class="col-md-7"> 
                <h6 class="fw-bold">🚚 Thông tin giao hàng</h6>
                <div class="border p-3 rounded">
                  <div class="d-flex mb-2">
                    <div style="width: 100px;"><strong>Người nhận:</strong></div>
                    <div>{{ orderDetail.tenNN }}</div>
                  </div>
                  <div class="d-flex mb-2">
                    <div style="width: 100px;"><strong>SĐT:</strong></div>
                    <div>{{ orderDetail.sdt }}</div>
                  </div>
                  <div class="d-flex">
                    <div style="width: 100px;"><strong>Địa chỉ:</strong></div>
                    <div>{{ orderDetail.diemGiao }}</div>
                  </div>
                </div>
              </div>
            </div>

            <!-- Chi tiết sản phẩm (giữ nguyên table) -->
            <div class="mb-4">
              <h6 class="fw-bold mb-3">🛒 Chi tiết sản phẩm</h6>
              <div class="table-responsive">
                <table class="table table-bordered">
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
                    <tr v-for="(item, index) in orderDetail.chiTiet" :key="item.maHDCT">
                      <td>{{ index + 1 }}</td>
                      <td>{{ item.tenSP }}</td>
                      <td>{{ item.tenMau }} - Size {{ item.coGiay === 0 ? 'Free' : item.coGiay }}</td>
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
            </div>

            <!-- Nhân viên xử lý -->
            <div v-if="orderDetail.maNV" class="mb-3">
              <h6 class="fw-bold mb-2">👤 Nhân viên xử lý</h6>
              <div class="border p-3 rounded">
                <div class="row">
                  <div class="col-md-4">
                    <span><strong>Mã NV:</strong> #NV{{ String(orderDetail.maNV).padStart(2, '0') }}</span>
                  </div>
                  <div class="col-md-4">
                    <span><strong>Tên NV:</strong> {{ orderDetail.tenNV }}</span>
                  </div>
                  <div class="col-md-4">
                    <span><strong>Email:</strong> {{ orderDetail.emailNV }}</span>
                  </div>
                </div>
              </div>
            </div>

            <!-- Ghi chú -->
            <div v-if="orderDetail.ghiChu" class="mt-3 p-3 border rounded" :class="{
              'bg-danger bg-opacity-10 border-danger': orderDetail.trangThai === 'Từ chối',
              'bg-info bg-opacity-10 border-info': orderDetail.trangThai === 'Báo lỗi',
              'bg-secondary bg-opacity-10 border-secondary': orderDetail.trangThai === 'Hoàn hàng/trả hàng',
              'bg-warning bg-opacity-10 border-warning': orderDetail.trangThai === 'Đang xử lý'
            }">
              <strong class="d-block mb-2">📝 Ghi chú:</strong>
              <p class="mb-0">{{ orderDetail.ghiChu }}</p>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button type="button" class="btn btn-primary" @click="printInvoice">
            <i class="fas fa-print me-2"></i>In hóa đơn
          </button>
          <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Đóng</button>
        </div>
      </div>
    </div>
  </div>

    <!-- Reject Modal -->
    <div class="modal fade" id="rejectModal" tabindex="-1" aria-hidden="true" ref="rejectModal">
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header bg-danger text-white">
            <h5 class="modal-title">Từ chối đơn hàng</h5>
            <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
          </div>
          <div class="modal-body">
            <p>Từ chối đơn hàng <strong>#HD{{ orderToReject ? String(orderToReject.maHD).padStart(4, '0') : '' }}</strong>?</p>
            
            <div class="mb-3">
              <label class="form-label fw-bold">Lý do từ chối:</label>
              <textarea v-model="rejectReason" class="form-control" rows="3" 
                        placeholder="Nhập lý do từ chối..."></textarea>
            </div>

            <div class="alert alert-warning">
              <i class="fas fa-exclamation-triangle me-2"></i>
              Hệ thống sẽ hoàn trả số lượng sản phẩm về kho.
            </div>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Hủy</button>
            <button type="button" class="btn btn-danger" @click="confirmRejectOrder" :disabled="processing">
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
import { ref, computed, onMounted } from 'vue';
import NV_Sidebar from '@/components/shared/NV_Sidebar.vue';
import { Modal } from 'bootstrap';
import jsPDF from 'jspdf';
import autoTable from 'jspdf-autotable';

export default {
  name: 'QLDonHang',
  components: {
    NV_Sidebar
  },
  setup() {
    // Dữ liệu mẫu từ database ShoedoShop
    const sampleOrders = ref({
      pending: [
        {
          maHD: 2,
          maKH: 2,
          tenKH: 'Trần Thị Hi',
          tenNN: 'Trần Thị Hi A',
          sdt: '0912345678',
          diemGiao: '789 Cách Mạng Tháng 8Z, Tân Bình',
          phuongThucTT: 'Chuyển khoản',
          ngayMua: '2025-01-11T00:00:00',
          tongTien: 5000000,
          trangThai: 'Đang xử lý',
          chiTiet: [
            { maHDCT: 2, tenSP: 'Giày da nữ đen', tenMau: 'Đen', coGiay: 38, soLuong: 1, donGia: 3200000, thanhTien: 3200000 },
            { maHDCT: 3, tenSP: 'Sandal đen', tenMau: 'Đen', coGiay: 41, soLuong: 1, donGia: 1800000, thanhTien: 1800000 }
          ]
        },
        {
          maHD: 9,
          maKH: 1,
          tenKH: 'Nguyễn Văn A',
          tenNN: 'Nguyễn Văn A-C',
          sdt: '0901234567',
          diemGiao: '123 Nguyễn Huệ C, Quận 1, TP.HCM',
          phuongThucTT: 'COD',
          ngayMua: '2025-01-18T00:00:00',
          tongTien: 4300000,
          trangThai: 'Đang xử lý',
          chiTiet: [
            { maHDCT: 16, tenSP: 'Giày da đen', tenMau: 'Đen', coGiay: 41, soLuong: 1, donGia: 2500000, thanhTien: 2500000 },
            { maHDCT: 17, tenSP: 'Sandal nâu', tenMau: 'Nâu', coGiay: 40, soLuong: 1, donGia: 1800000, thanhTien: 1800000 }
          ]
        }
      ],
      
      delivering: [
        {
          maHD: 3,
          maKH: 3,
          tenKH: 'Lê Thị B',
          tenNN: 'Nguyễn Văn A',
          sdt: '0987654321',
          diemGiao: '456 Lê Lợi A, Quận 1, TP.HCM',
          phuongThucTT: 'COD',
          ngayMua: '2025-01-12T00:00:00',
          tongTien: 4570000,
          trangThai: 'Đang giao',
          maNV: 1,
          tenNV: 'Quan',
          emailNV: 'admin@shop.com',
          chiTiet: [
            { maHDCT: 4, tenSP: 'Giày bóng đá xanh lá', tenMau: 'Xanh Lá', coGiay: 40, soLuong: 2, donGia: 2200000, thanhTien: 4400000 },
            { maHDCT: 5, tenSP: 'Vớ cổ cao', tenMau: 'Đen', coGiay: 0, soLuong: 1, donGia: 170000, thanhTien: 170000 }
          ]
        },
        {
          maHD: 8,
          maKH: 4,
          tenKH: 'Nguyễn Hoàng Minh Quân',
          tenNN: 'Nguyễn Văn B',
          sdt: '010100101',
          diemGiao: 'Quận Cam B, Quận 1, TP.HCM',
          phuongThucTT: 'COD',
          ngayMua: '2025-01-17T00:00:00',
          tongTien: 3340000,
          trangThai: 'Đang giao',
          maNV: 1,
          tenNV: 'Quan',
          emailNV: 'admin@shop.com',
          chiTiet: [
            { maHDCT: 14, tenSP: 'Sneaker đen', tenMau: 'Đen', coGiay: 42, soLuong: 1, donGia: 3000000, thanhTien: 3000000 },
            { maHDCT: 15, tenSP: 'Vớ cổ cao', tenMau: 'Đen', coGiay: 0, soLuong: 2, donGia: 170000, thanhTien: 340000 }
          ]
        }
      ],
      
      completed: [
        {
          maHD: 4,
          maKH: 4,
          tenKH: 'Nguyễn Hoàng Minh Quân',
          tenNN: 'Nguyễn Văn A',
          sdt: '010100101',
          diemGiao: 'Quận Cam A, Quận 1, TP.HCM',
          phuongThucTT: 'COD',
          ngayMua: '2025-01-13T00:00:00',
          tongTien: 3620000,
          trangThai: 'Hoàn tất',
          maNV: 1,
          tenNV: 'Quan',
          emailNV: 'admin@shop.com',
          chiTiet: [
            { maHDCT: 6, tenSP: 'Boot đen', tenMau: 'Đen', coGiay: 41, soLuong: 1, donGia: 3500000, thanhTien: 3500000 },
            { maHDCT: 7, tenSP: 'Dây giày', tenMau: 'Đa màu', coGiay: 0, soLuong: 1, donGia: 120000, thanhTien: 120000 }
          ]
        },
        {
          maHD: 10,
          maKH: 2,
          tenKH: 'Trần Thị Hi',
          tenNN: 'Trần Thị Hi A',
          sdt: '0912345678',
          diemGiao: '789 Cách Mạng Tháng 8Z, Tân Bình',
          phuongThucTT: 'Chuyển khoản',
          ngayMua: '2025-01-19T00:00:00',
          tongTien: 3820000,
          trangThai: 'Hoàn tất',
          maNV: 1,
          tenNV: 'Quan',
          emailNV: 'admin@shop.com',
          chiTiet: [
            { maHDCT: 18, tenSP: 'Boot nâu', tenMau: 'Nâu', coGiay: 42, soLuong: 1, donGia: 3500000, thanhTien: 3500000 },
            { maHDCT: 19, tenSP: 'Vớ trung bình', tenMau: 'Xám', coGiay: 0, soLuong: 2, donGia: 160000, thanhTien: 320000 }
          ]
        }
      ],
      
      rejected: [
        {
          maHD: 1,
          maKH: 1,
          tenKH: 'Nguyễn Văn A',
          tenNN: 'Nguyễn Văn A',
          sdt: '0901234567',
          diemGiao: '123 Nguyễn Huệ A, Quận 1, TP.HCM',
          phuongThucTT: 'COD',
          ngayMua: '2025-01-10T00:00:00',
          tongTien: 5600000,
          trangThai: 'Từ chối',
          maNV: 1,
          tenNV: 'Quan',
          emailNV: 'admin@shop.com',
          ghiChu: 'Đơn hàng đặt số lượng quá lớn, nhân viên từ chối',
          chiTiet: [
            { maHDCT: 1, tenSP: 'Giày da trắng', tenMau: 'Trắng', coGiay: 40, soLuong: 1, donGia: 2500000, thanhTien: 2500000 },
            { maHDCT: 2, tenSP: 'Sneaker trắng', tenMau: 'Trắng', coGiay: 41, soLuong: 1, donGia: 2800000, thanhTien: 2800000 },
            { maHDCT: 3, tenSP: 'Vớ thể thao', tenMau: 'Trắng', coGiay: 0, soLuong: 2, donGia: 150000, thanhTien: 300000 }
          ]
        },
        {
          maHD: 5,
          maKH: 1,
          tenKH: 'Nguyễn Văn A',
          tenNN: 'Nguyễn Văn A-B',
          sdt: '0901234567',
          diemGiao: '123 Nguyễn Huệ B, Quận 1, TP.HCM',
          phuongThucTT: 'COD',
          ngayMua: '2025-01-14T00:00:00',
          tongTien: 4820000,
          trangThai: 'Từ chối',
          maNV: 1,
          tenNV: 'Quan',
          emailNV: 'admin@shop.com',
          ghiChu: 'Khách hủy đơn',
          chiTiet: [
            { maHDCT: 8, tenSP: 'Cao gót đỏ', tenMau: 'Đỏ', coGiay: 37, soLuong: 1, donGia: 4500000, thanhTien: 4500000 },
            { maHDCT: 9, tenSP: 'Vớ trung bình', tenMau: 'Xám', coGiay: 0, soLuong: 2, donGia: 160000, thanhTien: 320000 }
          ]
        }
      ],

      error: [
        {
          maHD: 6,
          maKH: 2,
          tenKH: 'Trần Thị Hi',
          tenNN: 'Trần Thị Hi B',
          sdt: '0912345678',
          diemGiao: '789 Cách Mạng Tháng 8Y, Tân Bình',
          phuongThucTT: 'Chuyển khoản',
          ngayMua: '2025-01-15T00:00:00',
          tongTien: 3250000,
          trangThai: 'Báo lỗi',
          maNV: 1,
          tenNV: 'Quan',
          emailNV: 'admin@shop.com',
          ghiChu: 'Khách hàng không nhận được hàng',
          chiTiet: [
            { maHDCT: 10, tenSP: 'Sneaker xám', tenMau: 'Xám', coGiay: 42, soLuong: 1, donGia: 2800000, thanhTien: 2800000 },
            { maHDCT: 11, tenSP: 'Vớ thể thao', tenMau: 'Trắng', coGiay: 0, soLuong: 3, donGia: 150000, thanhTien: 450000 }
          ]
        }
      ],

      return: [
        {
          maHD: 7,
          maKH: 3,
          tenKH: 'Lê Thị B',
          tenNN: 'Nguyễn Văn A-B',
          sdt: '0987654321',
          diemGiao: '456 Lê Lợi B, Quận 1, TP.HCM',
          phuongThucTT: 'COD',
          ngayMua: '2025-01-16T00:00:00',
          tongTien: 4320000,
          trangThai: 'Hoàn hàng/trả hàng',
          maNV: 1,
          tenNV: 'Quan',
          emailNV: 'admin@shop.com',
          ghiChu: 'Sản phẩm không đúng mô tả',
          chiTiet: [
            { maHDCT: 12, tenSP: 'Boot & da đen bóng', tenMau: 'Đen bóng', coGiay: 41, soLuong: 1, donGia: 4200000, thanhTien: 4200000 },
            { maHDCT: 13, tenSP: 'Dây giày', tenMau: 'Đa màu', coGiay: 0, soLuong: 1, donGia: 120000, thanhTien: 120000 }
          ]
        }
      ]
    });

    const activeTab = ref('pending');
    const searchKeyword = ref('');
    const filterPayment = ref('');
    const filterDate = ref('');
    const message = ref('');
    const error = ref('');
    const processing = ref(false);
    const selectedOrder = ref(null);
    const orderDetail = ref(null);
    const orderToReject = ref(null);
    const rejectReason = ref('');

    const orderDetailModal = ref(null);
    const rejectModal = ref(null);
    let orderDetailModalInstance = null;
    let rejectModalInstance = null;

    const orderCounts = computed(() => ({
      pending: sampleOrders.value.pending.length,
      delivering: sampleOrders.value.delivering.length,
      completed: sampleOrders.value.completed.length,
      rejected: sampleOrders.value.rejected.length,
      error: sampleOrders.value.error.length,
      return: sampleOrders.value.return.length
    }));

    const filteredOrders = computed(() => {
      const keyword = searchKeyword.value.toLowerCase().trim();
      const payment = filterPayment.value;
      const date = filterDate.value;

      const filterFn = (order) => {
        if (keyword) {
          const maHDStr = `HD${String(order.maHD).padStart(4, '0')}`.toLowerCase();
          const matchesKeyword = 
            maHDStr.includes(keyword) ||
            order.tenKH.toLowerCase().includes(keyword) ||
            order.sdt.includes(keyword) ||
            (order.diemGiao && order.diemGiao.toLowerCase().includes(keyword));
          
          if (!matchesKeyword) return false;
        }

        if (payment && order.phuongThucTT !== payment) return false;

        if (date) {
          const orderDate = new Date(order.ngayMua).toISOString().split('T')[0];
          if (orderDate !== date) return false;
        }

        return true;
      };

      return {
        pending: sampleOrders.value.pending.filter(filterFn),
        delivering: sampleOrders.value.delivering.filter(filterFn),
        completed: sampleOrders.value.completed.filter(filterFn),
        rejected: sampleOrders.value.rejected.filter(filterFn),
        error: sampleOrders.value.error.filter(filterFn),
        return: sampleOrders.value.return.filter(filterFn)
      };
    });

    const getStatCardClass = (tab) => {
      const classes = {
        pending: 'bg-warning',
        delivering: 'bg-primary',
        completed: 'bg-success',
        rejected: 'bg-danger',
        error: 'bg-info',
        return: 'bg-secondary'
      };
      return classes[tab] || 'bg-dark';
    };

    const setActiveTab = (tab) => {
      activeTab.value = tab;
    };

    const resetFilters = () => {
      searchKeyword.value = '';
      filterPayment.value = '';
      filterDate.value = '';
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

    const getStatusBadgeClass = (status) => {
      switch(status) {
        case 'Đang xử lý': return 'bg-warning';
        case 'Đang giao': return 'bg-primary';
        case 'Hoàn tất': return 'bg-success';
        case 'Từ chối': return 'bg-danger';
        case 'Báo lỗi': return 'bg-info';
        case 'Hoàn hàng/trả hàng': return 'bg-secondary';
        default: return 'bg-secondary';
      }
    };

    const showOrderDetail = (order) => {
      selectedOrder.value = order;
      orderDetail.value = order;
      orderDetailModalInstance?.show();
    };

    const approveOrder = (orderId) => {
      if (!confirm('Duyệt đơn hàng này?')) return;
      
      processing.value = true;
      setTimeout(() => {
        const orderIndex = sampleOrders.value.pending.findIndex(o => o.maHD === orderId);
        if (orderIndex !== -1) {
          const order = sampleOrders.value.pending[orderIndex];
          order.maNV = 1;
          order.tenNV = 'Quan';
          order.emailNV = 'admin@shop.com';
          order.trangThai = 'Đang giao';
          sampleOrders.value.delivering.push(order);
          sampleOrders.value.pending.splice(orderIndex, 1);
          message.value = 'Duyệt đơn thành công!';
        }
        processing.value = false;
      }, 500);
    };

    const showRejectModal = (order) => {
      orderToReject.value = order;
      rejectReason.value = '';
      rejectModalInstance?.show();
    };

    const confirmRejectOrder = () => {
      if (!orderToReject.value) return;
      if (!rejectReason.value.trim()) {
        error.value = 'Nhập lý do từ chối';
        return;
      }

      processing.value = true;
      setTimeout(() => {
        const orderIndex = sampleOrders.value.pending.findIndex(o => o.maHD === orderToReject.value.maHD);
        if (orderIndex !== -1) {
          const order = sampleOrders.value.pending[orderIndex];
          order.maNV = 1;
          order.tenNV = 'Quan';
          order.emailNV = 'admin@shop.com';
          order.ghiChu = rejectReason.value;
          order.trangThai = 'Từ chối';
          sampleOrders.value.rejected.push(order);
          sampleOrders.value.pending.splice(orderIndex, 1);
          message.value = 'Từ chối đơn hàng!';
          rejectModalInstance?.hide();
        }
        processing.value = false;
      }, 500);
    };

    const markAsCompleted = (orderId) => {
      if (!confirm('Đánh dấu đã giao thành công?')) return;
      
      processing.value = true;
      setTimeout(() => {
        const orderIndex = sampleOrders.value.delivering.findIndex(o => o.maHD === orderId);
        if (orderIndex !== -1) {
          const order = sampleOrders.value.delivering[orderIndex];
          order.trangThai = 'Hoàn tất';
          sampleOrders.value.completed.push(order);
          sampleOrders.value.delivering.splice(orderIndex, 1);
          message.value = 'Đã hoàn tất đơn hàng!';
        }
        processing.value = false;
      }, 500);
    };

    const printInvoice = () => {
      if (!orderDetail.value) return;
      
      const doc = new jsPDF();
      const order = orderDetail.value;

      doc.setFontSize(18);
      doc.text('HÓA ĐƠN BÁN HÀNG', 105, 20, { align: 'center' });

      doc.setFontSize(12);
      doc.text('ShoeDo Shop', 20, 35);
      doc.setFontSize(10);
      doc.text('Địa chỉ: 123 Nguyễn Huệ, Quận 1, TP.HCM', 20, 42);
      doc.text('SĐT: 1900 1234', 20, 49);

      doc.setFontSize(12);
      doc.text(`Mã HD: #HD${String(order.maHD).padStart(4, '0')}`, 20, 60);
      doc.text(`Ngày mua: ${formatDate(order.ngayMua)}`, 20, 67);
      doc.text(`Trạng thái: ${order.trangThai}`, 20, 74);

      doc.text('Thông tin khách hàng:', 20, 85);
      doc.setFontSize(10);
      doc.text(`Người nhận: ${order.tenNN}`, 20, 92);
      doc.text(`SĐT: ${order.sdt}`, 20, 99);
      doc.text(`Địa chỉ: ${order.diemGiao}`, 20, 106);
      
      const tableColumn = ['STT', 'Sản phẩm', 'Phân loại', 'SL', 'Đơn giá', 'Thành tiền'];
      const tableRows = [];
      
      order.chiTiet.forEach((item, index) => {
        const row = [
          index + 1,
          item.tenSP,
          `${item.tenMau} - Size ${item.coGiay === 0 ? 'Free' : item.coGiay}`,
          item.soLuong,
          formatPrice(item.donGia),
          formatPrice(item.thanhTien)
        ];
        tableRows.push(row);
      });
      
      autoTable(doc, {
        head: [tableColumn],
        body: tableRows,
        startY: 115,
        theme: 'grid',
        styles: { fontSize: 8 },
        headStyles: { fillColor: [0, 0, 0] }
      });

      const finalY = doc.lastAutoTable.finalY + 10;
      doc.setFontSize(12);
      doc.text(`Tổng cộng: ${formatPrice(order.tongTien)}`, 150, finalY);
      
      doc.setFontSize(10);
      doc.text(`Phương thức thanh toán: ${order.phuongThucTT}`, 20, finalY + 10);
      
      if (order.ghiChu) {
        doc.text(`Ghi chú: ${order.ghiChu}`, 20, finalY + 20);
      }
      
      if (order.maNV) {
        doc.text(`Nhân viên xử lý: ${order.tenNV} (${order.emailNV})`, 20, finalY + 30);
      }
      
      doc.save(`HD${String(order.maHD).padStart(4, '0')}.pdf`);
    };

    onMounted(() => {
      orderDetailModalInstance = new Modal(document.getElementById('orderDetailModal'));
      rejectModalInstance = new Modal(document.getElementById('rejectModal'));
    });

    return {
      activeTab,
      searchKeyword,
      filterPayment,
      filterDate,
      message,
      error,
      processing,
      selectedOrder,
      orderDetail,
      orderToReject,
      rejectReason,
      orderCounts,
      filteredOrders,
      getStatCardClass,
      setActiveTab,
      resetFilters,
      formatPrice,
      formatDate,
      getStatusBadgeClass,
      showOrderDetail,
      approveOrder,
      showRejectModal,
      confirmRejectOrder,
      markAsCompleted,
      printInvoice
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
  background: #f4f6f9;
  min-height: 100vh;
}

.nav-tabs {
  border-bottom: 2px solid #dee2e6;
}

.nav-tabs .nav-link {
  color: #495057;
  font-weight: 500;
  border: none;
  border-bottom: 3px solid transparent;
  padding: 12px 24px;
  background: none;
}

.nav-tabs .nav-link.active {
  color: #000000;
  border-color: #000000;
}

.tab-content {
  border: 1px solid #dee2e6;
  border-top: none;
  border-radius: 0 0 8px 8px;
}

.card {
  transition: transform 0.2s;
}

.card:hover {
  transform: translateY(-5px);
  box-shadow: 0 5px 15px rgba(0,0,0,0.1);
}

.card-header {
  font-weight: 600;
}

.badge {
  font-size: 0.75rem;
  padding: 0.35em 0.65em;
}

.table td {
  padding: 0.5rem;
}

.text-primary {
  color: #007bff !important;
}

.modal-header.bg-dark {
  background-color: #343a40 !important;
}

.modal-header .btn-close-white {
  filter: invert(1);
}

.btn-success {
  background-color: #28a745;
  border-color: #28a745;
}

.btn-danger {
  background-color: #dc3545;
  border-color: #dc3545;
}

.btn-outline-dark {
  border-color: #343a40;
  color: #343a40;
}

.btn-outline-dark:hover {
  background-color: #343a40;
  color: #fff;
}

.btn-outline-primary {
  border-color: #007bff;
  color: #007bff;
}

.btn-outline-primary:hover {
  background-color: #007bff;
  color: #fff;
}

.btn-outline-success {
  border-color: #28a745;
  color: #28a745;
}

.btn-outline-success:hover {
  background-color: #28a745;
  color: #fff;
}

.btn-outline-danger {
  border-color: #dc3545;
  color: #dc3545;
}

.btn-outline-danger:hover {
  background-color: #dc3545;
  color: #fff;
}

.btn-outline-info {
  border-color: #17a2b8;
  color: #17a2b8;
}

.btn-outline-info:hover {
  background-color: #17a2b8;
  color: #fff;
}

.btn-outline-secondary {
  border-color: #6c757d;
  color: #6c757d;
}

.btn-outline-secondary:hover {
  background-color: #6c757d;
  color: #fff;
}
</style>