<template>
  <div class="employee-layout">
    <!-- Toast Component -->
    <Toast />
    
    <NV_Sidebar @toggle-collapse="handleSidebarCollapse" />
    <main class="main-content" :class="{ 'expanded': isSidebarCollapsed }">
      <div class="page-container">
        <!-- Thống kê nhanh -->
        <div class="row g-3 mb-4">
          <div class="col-md">
            <div class="text-white stats-mini bg-gradient-1 stats-card-animate" 
                 :style="{ '--delay': '0.1s' }"
                 @click="setActiveTab('pending')" style="cursor: pointer;">
              <h4 class="counter-animate">{{ orderCounts.pending }}</h4>
              <p>Chờ duyệt</p>
            </div>
          </div>
          <div class="col-md">
            <div class="text-white stats-mini bg-gradient-1 stats-card-animate" 
                 :style="{ '--delay': '0.2s' }"
                 @click="setActiveTab('delivering')" style="cursor: pointer;">
              <h4 class="counter-animate">{{ orderCounts.delivering }}</h4>
              <p>Đang giao</p>
            </div>
          </div>
          <div class="col-md">
            <div class="text-white stats-mini bg-gradient-1 stats-card-animate" 
                 :style="{ '--delay': '0.3s' }"
                 @click="setActiveTab('completed')" style="cursor: pointer;">
              <h4 class="counter-animate">{{ orderCounts.completed }}</h4>
              <p>Hoàn tất</p>
            </div>
          </div>
          <div class="col-md">
            <div class="text-white stats-mini bg-gradient-1 stats-card-animate" 
                 :style="{ '--delay': '0.4s' }"
                 @click="setActiveTab('rejected')" style="cursor: pointer;">
              <h4 class="counter-animate">{{ orderCounts.rejected }}</h4>
              <p>Từ chối</p>
            </div>
          </div>
          <div class="col-md">
            <div class="text-white stats-mini bg-gradient-1 stats-card-animate" 
                 :style="{ '--delay': '0.5s' }"
                 @click="setActiveTab('error')" style="cursor: pointer;">
              <h4 class="counter-animate">{{ orderCounts.error }}</h4>
              <p>Báo lỗi</p>
            </div>  
          </div>
        </div>

        <!-- Tabs Navigation -->
        <ul class="nav nav-tabs tab-nav-animate" id="ordersTab" role="tablist">
          <li class="nav-item tab-item-animate" role="presentation" :style="{ '--delay': '0.1s' }">
            <button class="nav-link tab-link-hover" :class="{ active: activeTab === 'pending' }" 
                    @click="setActiveTab('pending')">
              Chờ duyệt
              <span v-if="filteredStats.pending > 0" class="badge bg-warning ms-2 badge-pulse">
                {{ filteredStats.pending }}
              </span>
            </button>
          </li>
          <li class="nav-item tab-item-animate" role="presentation" :style="{ '--delay': '0.15s' }">
            <button class="nav-link tab-link-hover" :class="{ active: activeTab === 'delivering' }" 
                    @click="setActiveTab('delivering')">
              Đang giao
              <span v-if="filteredStats.delivering > 0" class="badge bg-primary ms-2 badge-pulse">
                {{ filteredStats.delivering }}
              </span>
            </button>
          </li>
          <li class="nav-item tab-item-animate" role="presentation" :style="{ '--delay': '0.2s' }">
            <button class="nav-link tab-link-hover" :class="{ active: activeTab === 'completed' }" 
                    @click="setActiveTab('completed')">
              Hoàn tất
              <span v-if="filteredStats.completed > 0" class="badge bg-success ms-2 badge-pulse">
                {{ filteredStats.completed }}
              </span>
            </button>
          </li>
          <li class="nav-item tab-item-animate" role="presentation" :style="{ '--delay': '0.25s' }">
            <button class="nav-link tab-link-hover" :class="{ active: activeTab === 'rejected' }" 
                    @click="setActiveTab('rejected')">
              Từ chối
              <span v-if="filteredStats.rejected > 0" class="badge bg-danger ms-2 badge-pulse">
                {{ filteredStats.rejected }}
              </span>
            </button>
          </li>
          <li class="nav-item tab-item-animate" role="presentation" :style="{ '--delay': '0.3s' }">
            <button class="nav-link tab-link-hover" :class="{ active: activeTab === 'error' }" 
                    @click="setActiveTab('error')">
              Báo lỗi
              <span v-if="filteredStats.error > 0" class="badge bg-info ms-2 badge-pulse">
                {{ filteredStats.error }}
              </span>
            </button>
          </li>
        </ul>

        <!-- Tab Content -->
        <div class="tab-content p-3 bg-white rounded-bottom shadow-sm content-animate">
          <!-- Search bar -->
          <div class="row g-2 mb-3">
            <div class="col-md-4 search-item-animate" style="--delay: 0.1s">
              <div class="input-group">
                <span class="input-group-text bg-light"><i class="bi bi-search"></i></span>
                <input type="text" v-model="searchKeyword" 
                       placeholder="Mã HD, tên KH, SĐT..." 
                       class="form-control input-focus-animate">
              </div>
            </div>
            <div class="col-md-2 search-item-animate" style="--delay: 0.15s">
              <select v-model="filterEmployee" class="form-select select-animate">
                <option value="">Tất cả nhân viên</option>
                <option v-for="emp in employees" :key="emp.maQT" :value="emp.maQT">
                  #QT{{ String(emp.maQT).padStart(4, '0') }} - {{ emp.tenQT }}
                </option>
              </select>
            </div>
            <div class="col-md-2 search-item-animate" style="--delay: 0.2s">
              <select v-model="sortOrder" class="form-select select-animate">
                <option value="desc">Mới nhất</option>
                <option value="asc">Cũ nhất</option>
              </select>
            </div>
            
            <div class="col-md-2 search-item-animate" style="--delay: 0.25s">
              <input type="date" v-model="filterDate" class="form-control input-focus-animate">
            </div>
            <div class="col-md-2 search-item-animate" style="--delay: 0.3s">
              <button @click="resetFilters" class="btn btn-outline-secondary w-100 btn-hover-scale">
                  <i class="bi bi-arrow-clockwise me-2"></i>Reset
                </button>
            </div>
          </div>
          
          <!-- Tab Chờ Duyệt -->
          <div v-show="activeTab === 'pending'" class="tab-pane-animate">
            <div v-if="filteredOrders.pending.length === 0" class="text-center p-5 text-muted empty-state-animate">
              <i class="bi bi-inbox display-4 icon-float"></i>
              <p class="mt-3">Không có đơn hàng chờ duyệt</p>
            </div>
            
            <div v-else class="row g-3">
              <div v-for="(order, index) in filteredOrders.pending" :key="order.maHD" class="col-12 col-md-6 col-lg-4">
                <div class="card h-100 border-dark order-card-animate" 
                     :style="{ '--delay': `${0.05 * index}s` }">
                  <div class="card-header bg-dark text-white d-flex justify-content-between align-items-center">
                    <strong>#HD{{ String(order.maHD).padStart(4, '0') }}</strong>
                    <span class="badge bg-warning text-white status-badge">Chờ duyệt</span>
                  </div>
                  <div class="card-body">
                    <div class="mb-2">
                      <small class="text-muted d-block">Ngày đặt:</small>
                      <strong>{{ formatDate(order.ngayMua) }}</strong>
                    </div>
                    
                    <div class="mb-2">
                      <small class="text-muted d-block">Người đặt:</small>
                      <strong>{{ order.tenKH }}</strong>
                    </div>
                    
                    <div class="mb-2">
                      <small class="text-muted d-block">SĐT người đặt:</small>
                      <strong>{{ order.sdtKH }}</strong>
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
                    
                    <div v-if="order.voucherGiam > 0" class="d-flex justify-content-between align-items-center mb-2">
                      <span class="text-muted">Voucher giảm:</span>
                      <strong class="text-danger">-{{ formatPrice(order.voucherGiam) }}</strong>
                    </div>
                    <div class="d-flex justify-content-between align-items-center mb-3">
                      <span class="text-muted fw-bold">Tổng tiền:</span>
                      <strong class="text-primary fs-5">{{ formatPrice(order.tongTienSauGiam || order.tongTien) }}</strong>
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
                      <button class="btn btn-danger btn-sm flex-fill btn-hover-scale" @click="showRejectModal(order)">
                        <i class="fas fa-times me-1"></i>Từ chối
                      </button>
                      <button class="btn btn-success btn-sm flex-fill btn-hover-scale" @click="confirmOrder(order.maHD)">
                        <i class="fas fa-check me-1"></i>Duyệt
                      </button>
                    </div>
                    <button class="btn btn-outline-dark btn-sm w-100 btn-hover-scale" @click="showOrderDetail(order)">
                      <i class="fas fa-eye me-1"></i>Xem chi tiết
                    </button>
                    <button class="btn btn-outline-primary btn-sm w-100 mt-2 btn-hover-scale" @click="printOrder(order)">
                      <i class="fas fa-print me-1"></i>Xuất hóa đơn
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Tab Đang Giao -->
          <div v-show="activeTab === 'delivering'" class="tab-pane-animate">
            <div v-if="filteredOrders.delivering.length === 0" class="text-center p-5 text-muted empty-state-animate">
              <i class="bi bi-truck display-4 icon-float"></i>
              <p class="mt-3">Không có đơn hàng đang giao</p>
            </div>
            
            <div v-else class="row g-3">
              <div v-for="(order, index) in filteredOrders.delivering" :key="order.maHD" class="col-12 col-md-6 col-lg-4">
                <div class="card h-100 border-dark order-card-animate" 
                     :style="{ '--delay': `${0.05 * index}s` }">
                  <div class="card-header bg-dark text-white d-flex justify-content-between align-items-center">
                    <strong>#HD{{ String(order.maHD).padStart(4, '0') }}</strong>
                    <span class="badge bg-primary text-white status-badge">Đang giao</span>
                  </div>
                  <div class="card-body">
                    <div class="mb-2">
                      <small class="text-muted d-block">Ngày đặt:</small>
                      <strong>{{ formatDate(order.ngayMua) }}</strong>
                    </div>
                    
                    <div class="mb-2">
                      <small class="text-muted d-block">Người đặt:</small>
                      <strong>{{ order.tenKH }}</strong>
                    </div>
                    
                    <div class="mb-2">
                      <small class="text-muted d-block">SĐT người đặt:</small>
                      <strong>{{order.sdtKH }}</strong>
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
                    
                    <div v-if="order.voucherGiam > 0" class="d-flex justify-content-between align-items-center mb-2">
                      <span class="text-muted">Voucher giảm:</span>
                      <strong class="text-danger">-{{ formatPrice(order.voucherGiam) }}</strong>
                    </div>
                    <div class="d-flex justify-content-between align-items-center mb-3">
                      <span class="text-muted fw-bold">Tổng tiền:</span>
                      <strong class="text-primary fs-5">{{ formatPrice(order.tongTienSauGiam || order.tongTien) }}</strong>
                    </div>
                    
                    <div class="mb-2">
                      <small class="text-muted d-block">NV duyệt:</small>
                      <small>#QT{{ String(order.maQT).padStart(4, '0') }} - {{ order.tenQT || order.emailQT }}</small>
                    </div>
                  </div>
                  <div class="card-footer bg-transparent">
                    <div class="d-flex gap-2 mb-2">
                      <button class="btn btn-warning btn-sm flex-fill btn-hover-scale" @click="showDeliveryFailedModal(order)">
                        <i class="fas fa-truck me-1"></i>Thất bại
                      </button>
                      <button class="btn btn-success btn-sm flex-fill btn-hover-scale" @click="deliverySuccess(order.maHD)">
                        <i class="fas fa-check-circle me-1"></i>Thành công
                      </button>
                    </div>
                    <button class="btn btn-outline-dark btn-sm w-100 btn-hover-scale" @click="showOrderDetail(order)">
                      <i class="fas fa-eye me-1"></i>Xem chi tiết
                    </button>
                    <button class="btn btn-outline-primary btn-sm w-100 mt-2 btn-hover-scale" @click="printOrder(order)">
                      <i class="fas fa-print me-1"></i>Xuất hóa đơn
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Tab Hoàn Tất -->
          <div v-show="activeTab === 'completed'" class="tab-pane-animate">
            <div v-if="filteredOrders.completed.length === 0" class="text-center p-5 text-muted empty-state-animate">
              <i class="bi bi-check-circle display-4 icon-float"></i>
              <p class="mt-3">Chưa có đơn hàng hoàn tất</p>
            </div>
            
            <div v-else class="row g-3">
              <div v-for="(order, index) in filteredOrders.completed" :key="order.maHD" class="col-12 col-md-6 col-lg-4">
                <div class="card h-100 border-dark order-card-animate" 
                     :style="{ '--delay': `${0.05 * index}s` }">
                  <div class="card-header bg-dark text-white d-flex justify-content-between align-items-center">
                    <strong>#HD{{ String(order.maHD).padStart(4, '0') }}</strong>
                    <span class="badge bg-success text-white status-badge">Hoàn tất</span>
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
                      <small class="text-muted d-block">Người đặt:</small>
                      <strong>{{ order.tenKH }}</strong>
                    </div>
                    
                    <div class="mb-2">
                      <small class="text-muted d-block">SĐT người đặt:</small>
                      <strong>{{ order.sdtKH }}</strong>
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
                    
                    <div v-if="order.voucherGiam > 0" class="d-flex justify-content-between align-items-center mb-2">
                      <span class="text-muted">Voucher giảm:</span>
                      <strong class="text-danger">-{{ formatPrice(order.voucherGiam) }}</strong>
                    </div>
                    <div class="d-flex justify-content-between align-items-center mb-3">
                      <span class="text-muted fw-bold">Tổng tiền:</span>
                      <strong class="text-primary fs-5">{{ formatPrice(order.tongTienSauGiam || order.tongTien) }}</strong>
                    </div>
                    
                    <div class="mb-2">
                      <small class="text-muted d-block">NV duyệt:</small>
                      <small>#QT{{ String(order.maQT).padStart(4, '0') }} - {{ order.tenQT || order.emailQT }}</small>
                    </div>
                  </div>
                  <div class="card-footer bg-transparent">
                    <button class="btn btn-outline-dark btn-sm w-100 btn-hover-scale" @click="showOrderDetail(order)">
                      <i class="fas fa-eye me-1"></i>Xem chi tiết
                    </button>
                    <button class="btn btn-outline-primary btn-sm w-100 mt-2 btn-hover-scale" @click="printOrder(order)">
                      <i class="fas fa-print me-1"></i>Xuất hóa đơn
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Tab Từ Chối -->
          <div v-show="activeTab === 'rejected'" class="tab-pane-animate">
            <div v-if="filteredOrders.rejected.length === 0" class="text-center p-5 text-muted empty-state-animate">
              <i class="bi bi-x-circle display-4 icon-float"></i>
              <p class="mt-3">Không có đơn hàng bị từ chối</p>
            </div>
            
            <div v-else class="row g-3">
              <div v-for="(order, index) in filteredOrders.rejected" :key="order.maHD" class="col-12 col-md-6 col-lg-4">
                <div class="card h-100 border-dark order-card-animate" 
                     :style="{ '--delay': `${0.05 * index}s` }">
                  <div class="card-header bg-dark text-white d-flex justify-content-between align-items-center">
                    <strong>#HD{{ String(order.maHD).padStart(4, '0') }}</strong>
                    <span class="badge bg-danger text-white status-badge">Từ chối</span>
                  </div>
                  <div class="card-body">
                    <div class="mb-2">
                      <small class="text-muted d-block">Ngày đặt:</small>
                      <strong>{{ formatDate(order.ngayMua) }}</strong>
                    </div>
                    
                    <div class="mb-2">
                      <small class="text-muted d-block">Người đặt:</small>
                      <strong>{{ order.tenKH }}</strong>
                    </div>
                    
                    <div class="mb-2">
                      <small class="text-muted d-block">SĐT người đặt:</small>
                      <strong>{{ order.sdtKH }}</strong>
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
                    
                    <div v-if="order.voucherGiam > 0" class="d-flex justify-content-between align-items-center mb-2">
                      <span class="text-muted">Voucher giảm:</span>
                      <strong class="text-danger">-{{ formatPrice(order.voucherGiam) }}</strong>
                    </div>
                    <div class="d-flex justify-content-between align-items-center mb-3">
                      <span class="text-muted fw-bold">Tổng tiền:</span>
                      <strong class="text-primary fs-5">{{ formatPrice(order.tongTienSauGiam || order.tongTien) }}</strong>
                    </div>
                  </div>
                  <div class="card-footer bg-transparent">
                    <button class="btn btn-outline-dark btn-sm w-100 btn-hover-scale" @click="showOrderDetail(order)">
                      <i class="fas fa-eye me-1"></i>Xem chi tiết
                    </button>
                    <button class="btn btn-outline-primary btn-sm w-100 mt-2 btn-hover-scale" @click="printOrder(order)">
                      <i class="fas fa-print me-1"></i>Xuất hóa đơn
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Tab Báo Lỗi -->
          <div v-show="activeTab === 'error'" class="tab-pane-animate">
            <div v-if="filteredOrders.error.length === 0" class="text-center p-5 text-muted empty-state-animate">
              <i class="bi bi-exclamation-triangle display-4 icon-float"></i>
              <p class="mt-3">Không có đơn hàng báo lỗi</p>
            </div>
            
            <div v-else class="row g-3">
              <div v-for="(order, index) in filteredOrders.error" :key="order.maHD" class="col-12 col-md-6 col-lg-4">
                <div class="card h-100 border-dark order-card-animate" 
                     :style="{ '--delay': `${0.05 * index}s` }">
                  <div class="card-header bg-dark text-white d-flex justify-content-between align-items-center">
                    <strong>#HD{{ String(order.maHD).padStart(4, '0') }}</strong>
                    <span class="badge bg-info text-white status-badge">Báo lỗi</span>
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
                      <small class="text-muted d-block">Người đặt:</small>
                      <strong>{{ order.tenKH }}</strong>
                    </div>
                    
                    <div class="mb-2">
                      <small class="text-muted d-block">SĐT người đặt:</small>
                      <strong>{{ order.sdtKH }}</strong>
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
                    
                    <div v-if="order.voucherGiam > 0" class="d-flex justify-content-between align-items-center mb-2">
                      <span class="text-muted">Voucher giảm:</span>
                      <strong class="text-danger">-{{ formatPrice(order.voucherGiam) }}</strong>
                    </div>
                    <div class="d-flex justify-content-between align-items-center mb-3">
                      <span class="text-muted fw-bold">Tổng tiền:</span>
                      <strong class="text-primary fs-5">{{ formatPrice(order.tongTienSauGiam || order.tongTien) }}</strong>
                    </div>
                  </div>
                  <div class="card-footer bg-transparent">
                    <button class="btn btn-info btn-sm w-100 mb-2 btn-hover-scale" @click="sendApologyEmail(order.maHD)">
                      <i class="fas fa-envelope me-1"></i>Gửi email xin lỗi
                    </button>
                    <button class="btn btn-outline-dark btn-sm w-100 btn-hover-scale" @click="showOrderDetail(order)">
                      <i class="fas fa-eye me-1"></i>Xem chi tiết
                    </button>
                    <button class="btn btn-outline-primary btn-sm w-100 mt-2 btn-hover-scale" @click="printOrder(order)">
                      <i class="fas fa-print me-1"></i>Xuất hóa đơn
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
    <div class="modal fade" id="orderDetailModal" tabindex="-1" aria-hidden="true" ref="orderDetailModal" data-bs-backdrop="static" data-bs-keyboard="false">
      <div class="modal-dialog modal-lg modal-dialog-centered modal-zoom-in">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">Chi tiết đơn hàng #HD{{ selectedOrder ? String(selectedOrder.maHD).padStart(4, '0') : '' }}</h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
          </div>
          <div class="modal-body">
            <div v-if="orderDetail" class="modal-content-animate">
              <div class="row mb-4">
                <div class="col-md-5 detail-item-animate" style="--delay: 0.05s">
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
                
                <div class="col-md-7 detail-item-animate" style="--delay: 0.1s">
                  <h6 class="fw-bold">Thông tin giao hàng</h6>
                  <div class="border p-3 rounded">
                    <div class="d-flex mb-2">
                      <div style="width: 100px;"><strong>Người nhận:</strong></div>
                      <div>{{ orderDetail.tenNN }}</div>
                    </div>
                    <div class="d-flex mb-2">
                      <div style="width: 100px;"><strong>SĐT người nhận:</strong></div>
                      <div>{{ orderDetail.sdt }}</div>
                    </div>
                    <div class="d-flex">
                      <div style="width: 100px;"><strong>Địa chỉ:</strong></div>
                      <div>{{ orderDetail.diemGiao }}</div>
                    </div>
                  </div>
                </div>
              </div>

              <div v-if="orderDetail.voucherApDung" class="row mb-4">
                <div class="col-md-12 detail-item-animate" style="--delay: 0.15s">
                  <h6 class="fw-bold">Voucher áp dụng</h6>
                  <div class="border p-3 rounded bg-light">
                    <div class="d-flex justify-content-between align-items-center">
                      <div>
                        <div class="fw-bold text-success">{{ orderDetail.voucherApDung.tenVoucher }}</div>
                        <div class="text-muted small">Đơn tối thiểu: {{ formatPrice(orderDetail.voucherApDung.donToiThieu) }}</div>
                      </div>
                      <div class="text-danger fw-bold">-{{ formatPrice(orderDetail.voucherApDung.giaTriGiam) }}</div>
                    </div>
                  </div>
                </div>
              </div>

              <div class="mb-4 detail-item-animate" style="--delay: 0.2s">
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
                      <tr v-for="(item, index) in orderDetail.chiTiet" :key="index" class="table-row-animate" :style="{ '--delay': `${0.05 * index}s` }">
                        <td>{{ index + 1 }}</td>
                        <td>{{ item.tenSP }}</td>
                        <td>{{ item.tenMau }} - Size {{ item.coGiay === 0 ? 'Free' : item.coGiay }}</td>
                        <td>{{ item.soLuong }}</td>
                        <td class="text-end">{{ formatPrice(item.donGia) }}</td>
                        <td class="fw-bold text-end">{{ formatPrice(item.thanhTien) }}</td>
                      </tr>
                    </tbody>
                    <tfoot>
                      <tr v-if="orderDetail.voucherGiam > 0">
                        <td colspan="5" class="text-end text-danger"><strong>Voucher giảm:</strong></td>
                        <td class="fw-bold text-danger text-end">-{{ formatPrice(orderDetail.voucherGiam) }}</td>
                      </tr>
                      <tr class="table-active">
                        <td colspan="5" class="text-end"><strong>Tổng tiền:</strong></td>
                        <td class="fw-bold text-primary text-end fs-5">{{ formatPrice(orderDetail.tongTienSauGiam || orderDetail.tongTien) }}</td>
                      </tr>
                    </tfoot>
                  </table>
                </div>
              </div>

              <div v-if="orderDetail.maQT" class="mb-3 detail-item-animate" style="--delay: 0.25s">
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

              <div v-if="orderDetail.ghiChu" class="mt-3 p-3 border rounded detail-item-animate" :class="getNoteClass(orderDetail.trangThai)" style="--delay: 0.3s">
                <strong class="d-block mb-2"> Ghi chú:</strong>
                <p class="mb-0">{{ orderDetail.ghiChu }}</p>
              </div>
            </div>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-primary btn-hover-scale" @click="printOrder(orderDetail)">
              <i class="fas fa-print me-2"></i>Xuất hóa đơn
            </button>
            <button type="button" class="btn btn-secondary btn-hover-scale" data-bs-dismiss="modal">Hủy</button>
          </div>
        </div>
      </div>
    </div>

    <!-- Reject Modal -->
    <div class="modal fade" id="rejectModal" tabindex="-1" aria-hidden="true" ref="rejectModal" data-bs-backdrop="static" data-bs-keyboard="false">
      <div class="modal-dialog modal-dialog-centered modal-zoom-in">
        <div class="modal-content modal-shake">
          <div class="modal-header">
            <h5 class="modal-title">Từ chối đơn hàng</h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
          </div>
          <div class="modal-body">
            <p>Từ chối đơn hàng <strong>#HD{{ orderToReject ? String(orderToReject.maHD).padStart(4, '0') : '' }}</strong>?</p>
            
            <div class="mb-3">
              <label class="form-label fw-bold">Lý do từ chối:</label>
              <textarea v-model="rejectReason" class="form-control input-focus-animate" rows="3" 
                        placeholder="Nhập lý do từ chối..."></textarea>
            </div>

            <div class="alert alert-warning alert-animate">
              <i class="fas fa-exclamation-triangle me-2 icon-pulse"></i>
              {{ getRejectWarning(orderToReject?.trangThai) }}
            </div>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary btn-hover-scale" data-bs-dismiss="modal">Hủy</button>
            <button type="button" class="btn btn-danger btn-hover-scale" @click="confirmRejectOrder" :disabled="processing">
              <span v-if="processing" class="spinner-border spinner-border-sm me-2"></span>
              Xác nhận từ chối
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Delivery Failed Modal -->
    <div class="modal fade" id="deliveryFailedModal" tabindex="-1" aria-hidden="true" ref="deliveryFailedModal" data-bs-backdrop="static" data-bs-keyboard="false">
      <div class="modal-dialog modal-dialog-centered modal-zoom-in">
        <div class="modal-content modal-shake">
          <div class="modal-header bg-warning text-dark">
            <h5 class="modal-title">Giao hàng thất bại</h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
          </div>
          <div class="modal-body">
            <p>Đánh dấu giao hàng thất bại cho đơn <strong>#HD{{ orderToFail ? String(orderToFail.maHD).padStart(4, '0') : '' }}</strong></p>
            
            <div class="mb-3">
              <label class="form-label fw-bold">Lý do thất bại:</label>
              <textarea v-model="failReason" class="form-control input-focus-animate" rows="3" 
                        placeholder="Nhập lý do giao hàng thất bại..."></textarea>
            </div>

            <div class="alert alert-info alert-animate">
              <i class="fas fa-info-circle me-2 icon-pulse"></i>
              Hệ thống sẽ hoàn trả số lượng sản phẩm về kho.
            </div>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary btn-hover-scale" data-bs-dismiss="modal">Hủy</button>
            <button type="button" class="btn btn-warning btn-hover-scale" @click="confirmDeliveryFailed" :disabled="processing">
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
import NV_Sidebar from '@/components/Shared/NV_Sidebar.vue';
import Toast from '@/components/Shared/Toast.vue';
import { Modal } from 'bootstrap';
import axios from 'axios';

export default {
  name: 'QLDonHang',
  components: {
    NV_Sidebar,
    Toast
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
    const sortOrder = ref('desc'); 
    const filterEmployee = ref('');
    const employees = ref([]);
    const activeTab = ref('pending');
    const searchKeyword = ref('');
    const filterDate = ref('');
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

    const isOrderInDate = (order, date) => {
      if (!date) return true;
      const orderDate = new Date(order.ngayMua).toISOString().split('T')[0];
      return orderDate === date;
    };

    const filteredStats = computed(() => {
        const date = filterDate.value;
        
        const filterByDate = (orderList) => {
          if (!date) return orderList;
          return orderList.filter(order => isOrderInDate(order, date));
        };

        return {
        pending: filterByDate(orders.value.pending).length,
        delivering: filterByDate(orders.value.delivering).length,
        completed: filterByDate(orders.value.completed).length,
        rejected: filterByDate(orders.value.rejected).length,
        error: filterByDate(orders.value.error).length
      };
    });

    const filteredOrders = computed(() => {
      const keyword = searchKeyword.value.toLowerCase().trim();
      const date = filterDate.value;
      const employeeId = filterEmployee.value;
      const sort = sortOrder.value; 

      const filterFn = (order) => {
        
        if (keyword) {
          if (date && !isOrderInDate(order, date)) return false;
          const maHDStr = `HD${String(order.maHD).padStart(4, '0')}`.toLowerCase();
          const matchesKeyword = 
            maHDStr.includes(keyword) ||
            (order.tenKH && order.tenKH.toLowerCase().includes(keyword)) ||
            (order.tenNN && order.tenNN.toLowerCase().includes(keyword)) ||
            (order.sdtKH && order.sdtKH.includes(keyword)) ||
            (order.sdt && order.sdt.includes(keyword)) ||
            (order.diemGiao && order.diemGiao.toLowerCase().includes(keyword));
          
          if (!matchesKeyword) return false;
        }

        if (date) {
          const orderDate = new Date(order.ngayMua).toISOString().split('T')[0];
          if (orderDate !== date) return false;
        }
        if (employeeId) {
          const empId = parseInt(employeeId);
          if (!order.maQT || order.maQT !== empId) return false;
        }

        return true;
        
      };

      const sortByDate = (orders) => {
        return [...orders].sort((a, b) => {
          const dateA = new Date(a.ngayMua);
          const dateB = new Date(b.ngayMua);
          return sort === 'desc' ? dateB - dateA : dateA - dateB;
        });
      };

      const filteredPending = orders.value.pending.filter(filterFn);
      const filteredDelivering = orders.value.delivering.filter(filterFn);
      const filteredCompleted = orders.value.completed.filter(filterFn);
      const filteredRejected = orders.value.rejected.filter(filterFn);
      const filteredError = orders.value.error.filter(filterFn);

      return {
        pending: sortByDate(filteredPending),
        delivering: sortByDate(filteredDelivering),
        completed: sortByDate(filteredCompleted),
        rejected: sortByDate(filteredRejected),
        error: sortByDate(filteredError)
      };
    });

    // Methods
    const loadOrders = async () => {
      try {
        const response = await axios.get('/api/employee/orders/all');
        if (response.data.success) {
          orders.value = response.data.data;
        } else {
          window.showToast(response.data.message || 'Lỗi khi tải danh sách đơn hàng', 'danger');
        }
      } catch (err) {
        window.showToast('Lỗi khi tải danh sách đơn hàng', 'danger');
      }
    };

    const loadEmployees = async () => {
      try {
        const response = await axios.get('/api/employee/orders/listnv');
        if (response.data.success) {
          employees.value = response.data.data;
        }
      } catch (err) {
        console.error('Lỗi khi tải danh sách nhân viên:', err);
      }
    };

    const showOrderDetail = async (order) => {
      try {
        const response = await axios.get(`/api/employee/orders/${order.maHD}`);
        if (response.data.success) {
          orderDetail.value = response.data.order;
          selectedOrder.value = order;
          orderDetailModalInstance?.show();
        } else {
          window.showToast(response.data.message || 'Lỗi khi tải chi tiết đơn hàng', 'danger');
        }
      } catch (err) {
        window.showToast('Lỗi khi tải chi tiết đơn hàng', 'danger');
      }
    };

    const confirmOrder = async (orderId) => {
      if (!confirm('Xác nhận đơn hàng này? Số lượng sản phẩm sẽ bị trừ khỏi kho. (VNPAY được ưu tiên)')) return;
      processing.value = true;
      try {
        const response = await axios.post(`/api/employee/orders/${orderId}/confirm`);
        if (response.data.success) {
          window.showToast(response.data.message, 'success');
          await loadOrders();
        } else {
          window.showToast(response.data.message || 'Lỗi khi xác nhận đơn hàng', 'danger');
        }
      } catch (err) {
        window.showToast(err.response?.data?.message || 'Lỗi khi xác nhận đơn hàng', 'danger');
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
        window.showToast('Vui lòng nhập lý do từ chối', 'warning');
        return;
      }
      processing.value = true;
      try {
        const response = await axios.post(`/api/employee/orders/${orderToReject.value.maHD}/failed`, {
          lyDo: rejectReason.value
        });
        if (response.data.success) {
          window.showToast(response.data.message, 'success');
          await loadOrders();
          rejectModalInstance?.hide();
        } else {
          window.showToast(response.data.message || 'Lỗi khi từ chối đơn hàng', 'danger');
        }
      } catch (err) {
        window.showToast(err.response?.data?.message || 'Lỗi khi từ chối đơn hàng', 'danger');
      } finally {
        processing.value = false;
      }
    };

    const deliverySuccess = async (orderId) => {
      if (!confirm('Đánh dấu giao thành công? KH có 1 tháng báo lỗi/bảo hành')) return;
      processing.value = true;
      try {
        const response = await axios.post(`/api/employee/orders/${orderId}/delivery-success`);
        if (response.data.success) {
          window.showToast(response.data.message, 'success');
          await loadOrders();
        } else {
          window.showToast(response.data.message || 'Lỗi khi cập nhật giao hàng thành công', 'danger');
        }
      } catch (err) {
        window.showToast(err.response?.data?.message || 'Lỗi khi cập nhật giao hàng thành công', 'danger');
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
        window.showToast('Vui lòng nhập lý do giao hàng thất bại', 'warning');
        return;
      }
      processing.value = true;
      try {
        const response = await axios.post(`/api/employee/orders/${orderToFail.value.maHD}/delivery-failed`, {
          lyDo: failReason.value
        });
        if (response.data.success) {
          window.showToast(response.data.message, 'success');
          await loadOrders();
          deliveryFailedModalInstance?.hide();
        } else {
          window.showToast(response.data.message || 'Lỗi khi cập nhật giao hàng thất bại', 'danger');
        }
      } catch (err) {
        window.showToast(err.response?.data?.message || 'Lỗi khi cập nhật giao hàng thất bại', 'danger');
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
          window.showToast(response.data.message, 'success');
          await loadOrders();
        } else {
          window.showToast(response.data.message || 'Lỗi khi gửi email', 'danger');
        }
      } catch (err) {
        window.showToast(err.response?.data?.message || 'Lỗi khi gửi email', 'danger');
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
        window.showToast('Đã tải xuống hóa đơn thành công!', 'success');

      } catch (err) {
        console.error('Lỗi xuất hóa đơn:', err);
        window.showToast('Lỗi khi xuất hóa đơn', 'danger');
      } finally {
        processing.value = false;
      }
    };

    const setActiveTab = (tab) => {
      activeTab.value = tab;
    };

    const resetFilters = () => {
      searchKeyword.value = '';
      filterDate.value = '';
      filterEmployee.value = ''; 
      sortOrder.value = 'desc'; 
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
      return 'Đơn hàng sẽ bị từ chối mà với lý do trên';
    };

    const isSidebarCollapsed = ref(false);

    const handleSidebarCollapse = (collapsedState) => {
      isSidebarCollapsed.value = collapsedState;
    };

    onMounted(() => {
      loadOrders();
      loadEmployees();
      
      orderDetailModalInstance = new Modal(document.getElementById('orderDetailModal'));
      rejectModalInstance = new Modal(document.getElementById('rejectModal'));
      deliveryFailedModalInstance = new Modal(document.getElementById('deliveryFailedModal'));
    });

    return {
      orders,
      activeTab,
      searchKeyword,
      filterDate,
      processing,
      selectedOrder,
      orderDetail,
      orderToReject,
      rejectReason,
      orderToFail,
      failReason,
      isSidebarCollapsed,
      orderCounts,
      filteredOrders,
      employees,
      filterEmployee,
      sortOrder,
      filteredStats,
      loadEmployees,
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
      printOrder,
      handleSidebarCollapse
    };
  }
};
</script>

<style scoped>
/* ===== ANIMATION KEYFRAMES ===== */
@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
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

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-20px);
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

@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-5px); }
}

@keyframes pulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.05); }
}

@keyframes shake {
  0%, 100% { transform: translateX(0); }
  10%, 30%, 50%, 70%, 90% { transform: translateX(-5px); }
  20%, 40%, 60%, 80% { transform: translateX(5px); }
}

/* ===== ANIMATION CLASSES ===== */
.stats-card-animate {
  animation: slideUp 0.5s ease-out forwards;
  opacity: 0;
  animation-delay: var(--delay, 0s);
  transition: all 0.3s ease;
}

.stats-card-animate:hover {
  transform: translateY(-5px) scale(1.02);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}

.counter-animate {
  animation: pulse 0.5s ease-out;
}

.tab-nav-animate {
  animation: slideDown 0.5s ease-out forwards;
}

.tab-item-animate {
  animation: fadeIn 0.4s ease-out forwards;
  opacity: 0;
  animation-delay: var(--delay, 0s);
}

.tab-link-hover {
  transition: all 0.3s ease;
  position: relative;
}

.tab-link-hover::after {
  content: '';
  position: absolute;
  bottom: -2px;
  left: 50%;
  width: 0;
  height: 3px;
  background: #000000;
  transition: all 0.3s ease;
  transform: translateX(-50%);
}

.tab-link-hover:hover::after {
  width: 100%;
}

.tab-link-hover.active::after {
  width: 100%;
}

.badge-pulse {
  animation: pulse 1.5s ease-in-out infinite;
}

.content-animate {
  animation: fadeIn 0.6s ease-out forwards;
}

.tab-pane-animate {
  animation: fadeIn 0.4s ease-out forwards;
}

.search-item-animate {
  animation: slideLeft 0.4s ease-out forwards;
  opacity: 0;
  animation-delay: var(--delay, 0s);
}

.input-focus-animate {
  border: 1px solid #dee2e6;
}

.input-focus-animate:focus {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  border-color: #000000;
}

.select-animate {
  transition: all 0.3s ease;
  cursor: pointer;
}

.select-animate:hover {
  border-color: #000000;
}

.btn-hover-scale {
  transition: all 0.3s ease;
}

.btn-hover-scale:hover {
  transform: scale(1.05) translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.btn-hover-scale:active {
  transform: scale(0.98);
}

.order-card-animate {
  animation: slideUp 0.5s ease-out forwards;
  opacity: 0;
  animation-delay: var(--delay, 0s);
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
}

.order-card-animate:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.12);
}

.status-badge {
  animation: pulse 2s ease-in-out infinite;
}

.empty-state-animate {
  animation: fadeIn 0.6s ease-out forwards;
}

.icon-float {
  animation: float 3s ease-in-out infinite;
}

.icon-pulse {
  animation: pulse 1.5s ease-in-out infinite;
}

/* ===== MODAL ANIMATIONS ===== */
.modal-zoom-in {
  animation: zoomIn 0.3s ease-out forwards;
}

.modal-shake {
  animation: shake 0.5s ease-in-out;
}

.modal-content-animate {
  animation: fadeIn 0.4s ease-out forwards;
}

.detail-item-animate {
  animation: slideUp 0.4s ease-out forwards;
  opacity: 0;
  animation-delay: var(--delay, 0s);
}

.table-row-animate {
  animation: fadeIn 0.3s ease-out forwards;
  opacity: 0;
  animation-delay: var(--delay, 0s);
}

.alert-animate {
  animation: slideUp 0.3s ease-out;
}

/* ===== EXISTING STYLES ===== */
.main-content {
  margin-left: 260px;
  min-height: 100vh;
  transition: margin-left 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
}

.main-content.expanded {
  margin-left: 80px;
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

.card-header {
  font-weight: 600;
}

.badge {
  font-size: 0.75rem;
  padding: 0.35em 0.65em;
}

.text-primary {
  color: #007bff !important;
}

.stats-mini {
  padding: 25px 20px;
  border-radius: 12px;
  text-align: center;
  color: rgb(255, 255, 255);
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
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

.modal-content {
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
  border: none;
  border-radius: 16px;
}

.modal-header {
  border-bottom: 2px solid #e9ecef;
  padding: 1rem 1.5rem;
}

.modal-footer {
  border-top: 2px solid #e9ecef;
  padding: 1rem 1.5rem;
}

body.modal-open {
  overflow: hidden;
  padding-right: 0 !important;
}

@media (max-width: 768px) {
  .main-content {
    margin-left: 0;
  }
  
  .page-container {
    padding: 15px;
  }
}
</style>

<style>
.modal-backdrop {
  background-color: rgba(0, 0, 0, 0.25) !important;
  backdrop-filter: blur(5px);
  -webkit-backdrop-filter: blur(12px) brightness(0.9);
  animation: fadeIn 0.3s ease-out;
}

.modal-backdrop.show {
  opacity: 1 !important;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}
</style>