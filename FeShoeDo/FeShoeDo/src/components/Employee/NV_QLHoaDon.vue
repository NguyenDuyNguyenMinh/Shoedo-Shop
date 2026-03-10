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
          <div class="col-md">
            <div class="card text-white bg-dark" @click="setActiveTab('pending')" style="cursor: pointer;">
              <div class="card-body text-center">
                <h5 class="card-title">Chờ duyệt</h5>
                <h2 class="mb-0">{{ orderCounts.pending }}</h2>
              </div>
            </div>
          </div>
          <div class="col-md">
            <div class="card text-white bg-dark" @click="setActiveTab('delivering')" style="cursor: pointer;">
              <div class="card-body text-center">
                <h5 class="card-title">Đang giao</h5>
                <h2 class="mb-0">{{ orderCounts.delivering }}</h2>
              </div>
            </div>
          </div>
          <div class="col-md">
            <div class="card text-white bg-dark" @click="setActiveTab('completed')" style="cursor: pointer;">
              <div class="card-body text-center">
                <h5 class="card-title">Hoàn tất</h5>
                <h2 class="mb-0">{{ orderCounts.completed }}</h2>
              </div>
            </div>
          </div>
          <div class="col-md">
            <div class="card text-white bg-dark" @click="setActiveTab('rejected')" style="cursor: pointer;">
              <div class="card-body text-center">
                <h5 class="card-title">Từ chối</h5>
                <h2 class="mb-0">{{ orderCounts.rejected }}</h2>
              </div>
            </div>
          </div>
          <div class="col-md">
            <div class="card text-white bg-dark" @click="setActiveTab('error')" style="cursor: pointer;">
              <div class="card-body text-center">
                <h5 class="card-title">Báo lỗi</h5>
                <h2 class="mb-0">{{ orderCounts.error }}</h2>
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
                      <strong>{{ order.tenNN || order.tenKH }}</strong>
                    </div>
                    
                    <div class="mb-2">
                      <small class="text-muted d-block">SĐT:</small>
                      <strong>{{ order.sdt || order.sdtKH }}</strong>
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
                    
                    <div v-if="order.maQT" class="mb-2">
                      <small class="text-muted d-block">NV xử lý:</small>
                      <small>#QT{{ String(order.maQT).padStart(4, '0') }} - {{ order.tenQT || order.emailQT }}</small>
                    </div>
                    <div v-else class="mb-2">
                      <small class="text-muted">Chưa có NV xử lý</small>
                    </div>
                  </div>
                  <div class="card-footer bg-transparent">
                    <div class="d-flex gap-2 mb-2">
                      <button class="btn btn-danger btn-sm flex-fill" @click="showRejectModal(order)">
                        <i class="fas fa-times me-1"></i>Từ chối
                      </button>
                      <button class="btn btn-success btn-sm flex-fill" @click="confirmOrder(order.maHD)">
                        <i class="fas fa-check me-1"></i>Duyệt
                      </button>
                    </div>
                    <button class="btn btn-outline-dark btn-sm w-100" @click="showOrderDetail(order)">
                      <i class="fas fa-eye me-1"></i>Xem chi tiết
                    </button>
                    <button class="btn btn-outline-primary btn-sm w-100 mt-2" @click="printOrder(order)">
                      <i class="fas fa-print me-1"></i>In hóa đơn
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
                      <strong>{{ order.tenNN || order.tenKH }}</strong>
                    </div>
                    
                    <div class="mb-2">
                      <small class="text-muted d-block">SĐT:</small>
                      <strong>{{ order.sdt || order.sdtKH }}</strong>
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
                      <small>#QT{{ String(order.maQT).padStart(4, '0') }} - {{ order.tenQT || order.emailQT }}</small>
                    </div>
                  </div>
                  <div class="card-footer bg-transparent">
                    <div class="d-flex gap-2 mb-2">
                      <button class="btn btn-warning btn-sm flex-fill" @click="showDeliveryFailedModal(order)">
                        <i class="fas fa-truck me-1"></i>Thất bại
                      </button>
                      <button class="btn btn-success btn-sm flex-fill" @click="deliverySuccess(order.maHD)">
                        <i class="fas fa-check-circle me-1"></i>Thành công
                      </button>
                    </div>
                    <button class="btn btn-outline-dark btn-sm w-100" @click="showOrderDetail(order)">
                      <i class="fas fa-eye me-1"></i>Xem chi tiết
                    </button>
                    <button class="btn btn-outline-primary btn-sm w-100 mt-2" @click="printOrder(order)">
                      <i class="fas fa-print me-1"></i>In hóa đơn
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
                      <small class="text-muted d-block">Ngày đến:</small>
                      <strong>{{ formatDate(order.ngayDen) || 'Chưa cập nhật' }}</strong>
                    </div>
                    
                    <div class="mb-2">
                      <small class="text-muted d-block">Người nhận:</small>
                      <strong>{{ order.tenNN || order.tenKH }}</strong>
                    </div>
                    
                    <div class="mb-2">
                      <small class="text-muted d-block">SĐT:</small>
                      <strong>{{ order.sdt || order.sdtKH }}</strong>
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
                      <small>#QT{{ String(order.maQT).padStart(4, '0') }} - {{ order.tenQT || order.emailQT }}</small>
                    </div>
                  </div>
                  <div class="card-footer bg-transparent">
                    <button class="btn btn-outline-dark  btn-sm w-100" @click="showOrderDetail(order)">
                      <i class="fas fa-eye me-1"></i>Xem chi tiết
                    </button>
                    <button class="btn btn-outline-primary btn-sm w-100 mt-2" @click="printOrder(order)">
                      <i class="fas fa-print me-1"></i>In hóa đơn
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
                  <div class="card-header bg-dark text-white d-flex justify-content-between align-items-center">
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
                      <strong>{{ order.tenNN || order.tenKH }}</strong>
                    </div>
                    
                    <div class="mb-2">
                      <small class="text-muted d-block">SĐT:</small>
                      <strong>{{ order.sdt || order.sdtKH }}</strong>
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
                    <button class="btn btn-outline-dark  btn-sm w-100" @click="showOrderDetail(order)">
                      <i class="fas fa-eye me-1"></i>Xem chi tiết
                    </button>
                    <button class="btn btn-outline-primary btn-sm w-100 mt-2" @click="printOrder(order)">
                      <i class="fas fa-print me-1"></i>In hóa đơn
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
                      <small class="text-muted d-block">Ngày đến:</small>
                      <strong>{{ formatDate(order.ngayDen) || 'Chưa cập nhật' }}</strong>
                    </div>

                    <div class="mb-2">
                      <small class="text-muted d-block">Người nhận:</small>
                      <strong>{{ order.tenNN || order.tenKH }}</strong>
                    </div>
                    
                    <div class="mb-2">
                      <small class="text-muted d-block">SĐT:</small>
                      <strong>{{ order.sdt || order.sdtKH }}</strong>
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
                    <button class="btn btn-info btn-sm w-100 mb-2" @click="sendApologyEmail(order.maHD)">
                      <i class="fas fa-envelope me-1"></i>Gửi email xin lỗi
                    </button>
                    <button class="btn btn-outline-dark  btn-sm w-100" @click="showOrderDetail(order)">
                      <i class="fas fa-eye me-1"></i>Xem chi tiết
                    </button>
                    <button class="btn btn-outline-primary btn-sm w-100 mt-2" @click="printOrder(order)">
                      <i class="fas fa-print me-1"></i>In hóa đơn
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Tab Hoàn Trả -->
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
              <div class="row mb-4">
                <div class="col-md-5">
                  <h6 class="fw-bold">Thông tin đơn hàng</h6>
                  <div class="border p-3 rounded">
                    <div class="d-flex mb-2">
                      <div style="width: 100px;"><strong>Mã HD:</strong></div>
                      <div>#HD{{ String(orderDetail.maHD).padStart(4, '0') }}</div>
                    </div>
                    <div class="d-flex mb-2">
                      <div style="width: 100px;"><strong>Ngày mua:</strong></div>
                      <div>{{ formatDate(orderDetail.ngayMua) }}</div>
                    </div>
                    <div class="d-flex mb-2" v-if="orderDetail.ngayDen">
                      <div style="width: 100px;"><strong>Ngày đến:</strong></div>
                      <div>{{ formatDate(orderDetail.ngayDen) }}</div>
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
                
                <div class="col-md-7">
                  <h6 class="fw-bold">Thông tin giao hàng</h6>
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

              <div class="mb-4">
                <h6 class="fw-bold mb-3">Chi tiết sản phẩm</h6>
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
                      <tr v-for="(item, index) in orderDetail.chiTiet" :key="index">
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

              <div v-if="orderDetail.maQT" class="mb-3">
                <h6 class="fw-bold mb-2">Nhân viên xử lý</h6>
                <div class="border p-3 rounded">
                  <div class="row">
                    <div class="col-md-4">
                      <span><strong>Mã NV:</strong> #QT{{ String(orderDetail.maQT).padStart(4, '0') }}</span>
                    </div>
                    <div class="col-md-4">
                      <span><strong>Tên NV:</strong> {{ orderDetail.tenQT }}</span>
                    </div>
                    <div class="col-md-4">
                      <span><strong>Email:</strong> {{ orderDetail.emailQT }}</span>
                    </div>
                  </div>
                </div>
              </div>

              <div v-if="orderDetail.ghiChu" class="mt-3 p-3 border rounded" :class="getNoteClass(orderDetail.trangThai)">
                <strong class="d-block mb-2"> Ghi chú:</strong>
                <p class="mb-0">{{ orderDetail.ghiChu }}</p>
              </div>
            </div>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-primary" @click="printOrder(orderDetail)">
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
          <div class="modal-header">
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
              {{ getRejectWarning(orderToReject?.trangThai) }}
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

    <!-- Delivery Failed Modal -->
    <div class="modal fade" id="deliveryFailedModal" tabindex="-1" aria-hidden="true" ref="deliveryFailedModal">
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header bg-warning text-dark">
            <h5 class="modal-title">Giao hàng thất bại</h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
          </div>
          <div class="modal-body">
            <p>Đánh dấu giao hàng thất bại cho đơn <strong>#HD{{ orderToFail ? String(orderToFail.maHD).padStart(4, '0') : '' }}</strong></p>
            
            <div class="mb-3">
              <label class="form-label fw-bold">Lý do thất bại:</label>
              <textarea v-model="failReason" class="form-control" rows="3" 
                        placeholder="Nhập lý do giao hàng thất bại..."></textarea>
            </div>

            <div class="alert alert-info">
              <i class="fas fa-info-circle me-2"></i>
              Hệ thống sẽ hoàn trả số lượng sản phẩm về kho.
            </div>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Hủy</button>
            <button type="button" class="btn btn-warning" @click="confirmDeliveryFailed" :disabled="processing">
              <span v-if="processing" class="spinner-border spinner-border-sm me-2"></span>
              Xác nhận thất bại
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
import axios from 'axios';

export default {
  name: 'QLDonHang',
  components: {
    NV_Sidebar
  },
  setup() {
    // State
    const orders = ref({
      pending: [],
      delivering: [],
      completed: [],
      rejected: [],
      error: []
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
    const orderToFail = ref(null);
    const failReason = ref('');

    // Modal refs
    const orderDetailModal = ref(null);
    const rejectModal = ref(null);
    const deliveryFailedModal = ref(null);
    
    let orderDetailModalInstance = null;
    let rejectModalInstance = null;
    let deliveryFailedModalInstance = null;

    // Computed
    const orderCounts = computed(() => ({
      pending: orders.value.pending.length,
      delivering: orders.value.delivering.length,
      completed: orders.value.completed.length,
      rejected: orders.value.rejected.length,
      error: orders.value.error.length
    }));

    const getSortedOrders = (ordersList, tabType) => {
      if (tabType === 'completed' || tabType === 'rejected') {
        return [...ordersList].sort((a, b) => new Date(b.ngayMua) - new Date(a.ngayMua));
      } else {
        return [...ordersList].sort((a, b) => new Date(a.ngayMua) - new Date(b.ngayMua));
      }
    };

    const filteredOrders = computed(() => {
      const keyword = searchKeyword.value.toLowerCase().trim();
      const payment = filterPayment.value;
      const date = filterDate.value;

      const filterFn = (order) => {
        if (keyword) {
          const maHDStr = `HD${String(order.maHD).padStart(4, '0')}`.toLowerCase();
          const matchesKeyword = 
            maHDStr.includes(keyword) ||
            (order.tenKH && order.tenNN.toLowerCase().includes(keyword)) ||
            (order.sdt && order.sdt.includes(keyword)) ||
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

      const filteredPending = orders.value.pending.filter(filterFn);
      const filteredDelivering = orders.value.delivering.filter(filterFn);
      const filteredCompleted = orders.value.completed.filter(filterFn);
      const filteredRejected = orders.value.rejected.filter(filterFn);
      const filteredError = orders.value.error.filter(filterFn);

      return {
        pending: getSortedOrders(filteredPending, 'pending'),
        delivering: getSortedOrders(filteredDelivering, 'delivering'),
        completed: getSortedOrders(filteredCompleted, 'completed'),
        rejected: getSortedOrders(filteredRejected, 'rejected'),
        error: getSortedOrders(filteredError, 'error')
      };
    });

    // Methods
    const loadOrders = async () => {
      try {
        const response = await axios.get('/api/employee/orders/all');
        if (response.data.success) {
          orders.value = response.data.data;
        }
      } catch (err) {
        error.value = 'Lỗi khi tải danh sách đơn hàng';
      }
    };

    const showOrderDetail = async (order) => {
      try {
        const response = await axios.get(`/api/employee/orders/${order.maHD}`);
        if (response.data.success) {
          orderDetail.value = response.data.order;
          selectedOrder.value = order;
          orderDetailModalInstance?.show();
        }
      } catch (err) {
        error.value = 'Lỗi khi tải chi tiết đơn hàng';
      }
    };

    const confirmOrder = async (orderId) => {
      if (!confirm('Xác nhận đơn hàng này? Số lượng sản phẩm sẽ bị trừ khỏi kho.')) return;
      processing.value = true;
      try {
        const response = await axios.post(`/api/employee/orders/${orderId}/confirm`);
        if (response.data.success) {
          message.value = response.data.message;
          await loadOrders();
        }
      } catch (err) {
        error.value = err.response?.data?.message || 'Lỗi khi xác nhận đơn hàng';
      } finally {
        processing.value = false;
      }
    };

    const showRejectModal = (order) => {
      orderToReject.value = order;
      rejectReason.value = '';
      rejectModalInstance?.show();
    };

    const confirmRejectOrder = async () => {
      if (!orderToReject.value) return;
      if (!rejectReason.value.trim()) {
        error.value = 'Vui lòng nhập lý do từ chối';
        return;
      }
      processing.value = true;
      try {
        const response = await axios.post(`/api/employee/orders/${orderToReject.value.maHD}/failed`, {
          lyDo: rejectReason.value
        });
        if (response.data.success) {
          message.value = response.data.message;
          await loadOrders();
          rejectModalInstance?.hide();
        }
      } catch (err) {
        error.value = err.response?.data?.message || 'Lỗi khi từ chối đơn hàng';
      } finally {
        processing.value = false;
      }
    };

    const deliverySuccess = async (orderId) => {
      if (!confirm('Đánh dấu giao thành công? KH có 3 ngày báo lỗi/trả hàng')) return;
      processing.value = true;
      try {
        const response = await axios.post(`/api/employee/orders/${orderId}/delivery-success`);
        if (response.data.success) {
          message.value = response.data.message;
          await loadOrders();
        }
      } catch (err) {
        error.value = err.response?.data?.message || 'Lỗi khi cập nhật giao hàng thành công';
      } finally {
        processing.value = false;
      }
    };

    const showDeliveryFailedModal = (order) => {
      orderToFail.value = order;
      failReason.value = '';
      deliveryFailedModalInstance?.show();
    };

    const confirmDeliveryFailed = async () => {
      if (!orderToFail.value) return;
      if (!failReason.value.trim()) {
        error.value = 'Vui lòng nhập lý do giao hàng thất bại';
        return;
      }
      processing.value = true;
      try {
        const response = await axios.post(`/api/employee/orders/${orderToFail.value.maHD}/delivery-failed`, {
          lyDo: failReason.value
        });
        if (response.data.success) {
          message.value = response.data.message;
          await loadOrders();
          deliveryFailedModalInstance?.hide();
        }
      } catch (err) {
        error.value = err.response?.data?.message || 'Lỗi khi cập nhật giao hàng thất bại';
      } finally {
        processing.value = false;
      }
    };

    const sendApologyEmail = async (orderId) => {
      if (!confirm('Đã xử lý xong thông tin báo lỗi? Gửi email xin lỗi kèm PDF.')) return;
      processing.value = true;
      try {
        const response = await axios.post(`/api/employee/orders/${orderId}/send-apology-email`);
        if (response.data.success) {
          message.value = response.data.message;
          await loadOrders();
        }
      } catch (err) {
        error.value = err.response?.data?.message || 'Lỗi khi gửi email';
      } finally {
        processing.value = false;
      }
    };

    const printOrder = async (order) => {
      try {
        processing.value = true;
        const response = await axios({
          url: `/api/employee/orders/${order.maHD}/print`,
          method: 'GET',
          responseType: 'blob',
        });

        const blob = new Blob([response.data], { type: 'application/pdf' });
        const link = document.createElement('a');
        link.href = window.URL.createObjectURL(blob);
        link.download = `HD${String(order.maHD).padStart(4, '0')}.pdf`;
        link.click();
        window.URL.revokeObjectURL(link.href);
        message.value = 'Đã tải xuống hóa đơn thành công!';

      } catch (err) {
        console.error('Lỗi in hóa đơn:', err);
        error.value = 'Lỗi khi in hóa đơn';
      } finally {
        processing.value = false;
      }
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
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
      });
    };

    const getStatusBadgeClass = (status) => {
      const map = {
        'Đang xử lý': 'bg-warning',
        'Đang giao': 'bg-primary',
        'Hoàn tất': 'bg-success',
        'Đã từ chối': 'bg-danger',
        'Báo lỗi': 'bg-info',
        'Hoàn hàng/trả hàng': 'bg-secondary'
      };
      return map[status] || 'bg-secondary';
    };

    const getNoteClass = (status) => {
      const map = {
        'Đã từ chối': 'bg-danger bg-opacity-10 border-danger',
        'Báo lỗi': 'bg-info bg-opacity-10 border-info',
        'Hoàn tất': 'bg-success bg-opacity-10 border-success'
      };
      return map[status] || '';
    };

    const getRejectWarning = (status) => {
      if (status === 'Đang giao') {
        return 'Hệ thống sẽ hoàn trả số lượng sản phẩm về kho.';
      }
      return 'Đơn hàng sẽ bị từ chối mà không ảnh hưởng đến kho.';
    };

    onMounted(() => {
      loadOrders();
      
      orderDetailModalInstance = new Modal(document.getElementById('orderDetailModal'));
      rejectModalInstance = new Modal(document.getElementById('rejectModal'));
      deliveryFailedModalInstance = new Modal(document.getElementById('deliveryFailedModal'));
    });

    return {
      // State
      orders,
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
      orderToFail,
      failReason,
      
      // Computed
      orderCounts,
      filteredOrders,
      
      // Methods
      setActiveTab,
      resetFilters,
      formatPrice,
      formatDate,
      getStatusBadgeClass,
      getNoteClass,
      getRejectWarning,
      showOrderDetail,
      confirmOrder,
      showRejectModal,
      confirmRejectOrder,
      deliverySuccess,
      showDeliveryFailedModal,
      confirmDeliveryFailed,
      sendApologyEmail,
      printOrder
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

.btn-warning {
  background-color: #ffc107;
  border-color: #ffc107;
  color: #212529;
}

.btn-info {
  background-color: #17a2b8;
  border-color: #17a2b8;
  color: #fff;
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

</style>