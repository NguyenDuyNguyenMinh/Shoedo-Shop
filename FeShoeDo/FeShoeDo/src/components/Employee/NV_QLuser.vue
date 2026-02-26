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
              <h4>8</h4>
              <p>Tổng User</p>
            </div>
          </div>
          <div class="col-md-3">
            <div class="stats-mini bg-gradient-2">
              <div class="stats-icon"><i class="bi bi-person-heart"></i></div>
              <h4>4</h4>
              <p>Khách hàng</p>
            </div>
          </div>
          <div class="col-md-3">
            <div class="stats-mini bg-gradient-3">
              <div class="stats-icon"><i class="bi bi-person-gear"></i></div>
              <h4>4</h4>
              <p>Quản trị viên</p>
            </div>
          </div>
          <div class="col-md-3">
            <div class="stats-mini bg-gradient-4">
              <div class="stats-icon"><i class="bi bi-person-x-fill"></i></div>
              <h4>0</h4>
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
          </div>

          <!-- Filter Section -->
          <div class="filter-section">
            <div class="row g-3">
              <div class="col-md-3">
                <div class="input-group">
                  <span class="input-group-text bg-light">
                    <i class="bi bi-search"></i>
                  </span>
                  <input type="text" class="form-control" placeholder="Tìm username, email...">
                </div>
              </div>
              <div class="col-md-2">
                <select class="form-select">
                  <option>Tất cả vai trò</option>
                  <option>Admin</option>
                  <option>Nhân viên</option>
                  <option>Khách hàng</option>
                </select>
              </div>
              <div class="col-md-2">
                <select class="form-select">
                  <option>Tất cả trạng thái</option>
                  <option>Đang hoạt động</option>
                  <option>Bị khóa</option>
                </select>
              </div>
              <div class="col-md-2">
                <select class="form-select">
                  <option>Sắp xếp</option>
                  <option>Mới nhất</option>
                  <option>Cũ nhất</option>
                  <option>Username A-Z</option>
                </select>
              </div>
              <div class="col-md-3">
                <div class="d-flex gap-2">
                  <button class="btn btn-dark flex-fill">
                    <i class="bi bi-search me-2"></i>Tìm kiếm
                  </button>
                  <button class="btn btn-outline-secondary">
                    <i class="bi bi-arrow-clockwise"></i>
                  </button>
                </div>
              </div>
            </div>
          </div>

          <!-- User Table -->
          <div class="table-responsive">
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
                <!-- User 1 - Admin -->
                <tr>
                  <td><span class="fw-semibold text-muted">1</span></td>
                  <td>
                    <div class="d-flex align-items-center gap-2">
                      <div class="user-avatar bg-admin">
                        <i class="bi bi-star-fill"></i>
                      </div>
                      <span class="fw-semibold">admin</span>
                    </div>
                  </td>
                  <td><span class="text-muted">admin@shop.com</span></td>
                  <td>Quan</td>
                  <td>—</td>
                  <td><span class="badge role-badge badge-admin"><i class="bi bi-star-fill me-1"></i>Admin</span></td>
                  <td><span class="badge status-badge badge-active"><i class="bi bi-check-circle me-1"></i>Hoạt động</span></td>
                  <td><span class="text-muted small">25/01/2025</span></td>
                  <td class="text-center">
                    <div class="action-buttons justify-content-center">
                      <button class="btn btn-outline-primary btn-sm" data-bs-toggle="modal" data-bs-target="#viewUserModal" title="Xem chi tiết">
                        <i class="bi bi-eye"></i>
                      </button>
                      <button class="btn btn-outline-secondary btn-sm" data-bs-toggle="modal" data-bs-target="#editUserModal" title="Chỉnh sửa">
                        <i class="bi bi-pencil"></i>
                      </button>
                    </div>
                  </td>
                </tr>

                <!-- User 2 - Nhân viên -->
                <tr>
                  <td><span class="fw-semibold text-muted">2</span></td>
                  <td>
                    <div class="d-flex align-items-center gap-2">
                      <div class="user-avatar bg-employee">
                        <i class="bi bi-person-gear"></i>
                      </div>
                      <span class="fw-semibold">nv1</span>
                    </div>
                  </td>
                  <td><span class="text-muted">nhanvien1@gmail.com</span></td>
                  <td>Minh</td>
                  <td>—</td>
                  <td><span class="badge role-badge badge-employee"><i class="bi bi-person-gear me-1"></i>Nhân viên</span></td>
                  <td><span class="badge status-badge badge-active"><i class="bi bi-check-circle me-1"></i>Hoạt động</span></td>
                  <td><span class="text-muted small">25/01/2025</span></td>
                  <td class="text-center">
                    <div class="action-buttons justify-content-center">
                      <button class="btn btn-outline-primary btn-sm" data-bs-toggle="modal" data-bs-target="#viewUserModal" title="Xem chi tiết">
                        <i class="bi bi-eye"></i>
                      </button>
                      <button class="btn btn-outline-secondary btn-sm" data-bs-toggle="modal" data-bs-target="#editUserModal" title="Chỉnh sửa">
                        <i class="bi bi-pencil"></i>
                      </button>
                      <button class="btn btn-outline-danger btn-sm" title="Khóa tài khoản">
                        <i class="bi bi-lock"></i>
                      </button>
                    </div>
                  </td>
                </tr>

                <!-- User 3 - Nhân viên -->
                <tr>
                  <td><span class="fw-semibold text-muted">3</span></td>
                  <td>
                    <div class="d-flex align-items-center gap-2">
                      <div class="user-avatar bg-employee">
                        <i class="bi bi-person-gear"></i>
                      </div>
                      <span class="fw-semibold">nv2</span>
                    </div>
                  </td>
                  <td><span class="text-muted">nhanvien2@gmail.com</span></td>
                  <td>Minh3D</td>
                  <td>—</td>
                  <td><span class="badge role-badge badge-employee"><i class="bi bi-person-gear me-1"></i>Nhân viên</span></td>
                  <td><span class="badge status-badge badge-active"><i class="bi bi-check-circle me-1"></i>Hoạt động</span></td>
                  <td><span class="text-muted small">25/01/2025</span></td>
                  <td class="text-center">
                    <div class="action-buttons justify-content-center">
                      <button class="btn btn-outline-primary btn-sm" data-bs-toggle="modal" data-bs-target="#viewUserModal" title="Xem chi tiết">
                        <i class="bi bi-eye"></i>
                      </button>
                      <button class="btn btn-outline-secondary btn-sm" data-bs-toggle="modal" data-bs-target="#editUserModal" title="Chỉnh sửa">
                        <i class="bi bi-pencil"></i>
                      </button>
                      <button class="btn btn-outline-danger btn-sm" title="Khóa tài khoản">
                        <i class="bi bi-lock"></i>
                      </button>
                    </div>
                  </td>
                </tr>

                <!-- User 4 - Nhân viên -->
                <tr>
                  <td><span class="fw-semibold text-muted">4</span></td>
                  <td>
                    <div class="d-flex align-items-center gap-2">
                      <div class="user-avatar bg-employee">
                        <i class="bi bi-person-gear"></i>
                      </div>
                      <span class="fw-semibold">nv3</span>
                    </div>
                  </td>
                  <td><span class="text-muted">nhanvien3@gmail.com</span></td>
                  <td>Brynes</td>
                  <td>—</td>
                  <td><span class="badge role-badge badge-employee"><i class="bi bi-person-gear me-1"></i>Nhân viên</span></td>
                  <td><span class="badge status-badge badge-active"><i class="bi bi-check-circle me-1"></i>Hoạt động</span></td>
                  <td><span class="text-muted small">25/01/2025</span></td>
                  <td class="text-center">
                    <div class="action-buttons justify-content-center">
                      <button class="btn btn-outline-primary btn-sm" data-bs-toggle="modal" data-bs-target="#viewUserModal" title="Xem chi tiết">
                        <i class="bi bi-eye"></i>
                      </button>
                      <button class="btn btn-outline-secondary btn-sm" data-bs-toggle="modal" data-bs-target="#editUserModal" title="Chỉnh sửa">
                        <i class="bi bi-pencil"></i>
                      </button>
                      <button class="btn btn-outline-danger btn-sm" title="Khóa tài khoản">
                        <i class="bi bi-lock"></i>
                      </button>
                    </div>
                  </td>
                </tr>

                <!-- User 5 - Khách hàng -->
                <tr>
                  <td><span class="fw-semibold text-muted">5</span></td>
                  <td>
                    <div class="d-flex align-items-center gap-2">
                      <div class="user-avatar bg-customer">
                        <i class="bi bi-person"></i>
                      </div>
                      <span class="fw-semibold">user1</span>
                    </div>
                  </td>
                  <td><span class="text-muted">user1@gmail.com</span></td>
                  <td>Nguyễn Văn A</td>
                  <td>0901234567</td>
                  <td><span class="badge role-badge badge-customer"><i class="bi bi-person me-1"></i>Khách hàng</span></td>
                  <td><span class="badge status-badge badge-active"><i class="bi bi-check-circle me-1"></i>Hoạt động</span></td>
                  <td><span class="text-muted small">25/01/2025</span></td>
                  <td class="text-center">
                    <div class="action-buttons justify-content-center">
                      <button class="btn btn-outline-primary btn-sm" data-bs-toggle="modal" data-bs-target="#viewUserModal" title="Xem chi tiết">
                        <i class="bi bi-eye"></i>
                      </button>
                      <button class="btn btn-outline-secondary btn-sm" data-bs-toggle="modal" data-bs-target="#editUserModal" title="Chỉnh sửa">
                        <i class="bi bi-pencil"></i>
                      </button>
                      <button class="btn btn-outline-danger btn-sm" title="Khóa tài khoản">
                        <i class="bi bi-lock"></i>
                      </button>
                    </div>
                  </td>
                </tr>

                <!-- User 6 - Khách hàng -->
                <tr>
                  <td><span class="fw-semibold text-muted">6</span></td>
                  <td>
                    <div class="d-flex align-items-center gap-2">
                      <div class="user-avatar bg-customer">
                        <i class="bi bi-person"></i>
                      </div>
                      <span class="fw-semibold">user2</span>
                    </div>
                  </td>
                  <td><span class="text-muted">user2@gmail.com</span></td>
                  <td>Trần Thị Hi</td>
                  <td>0912345678</td>
                  <td><span class="badge role-badge badge-customer"><i class="bi bi-person me-1"></i>Khách hàng</span></td>
                  <td><span class="badge status-badge badge-active"><i class="bi bi-check-circle me-1"></i>Hoạt động</span></td>
                  <td><span class="text-muted small">25/01/2025</span></td>
                  <td class="text-center">
                    <div class="action-buttons justify-content-center">
                      <button class="btn btn-outline-primary btn-sm" data-bs-toggle="modal" data-bs-target="#viewUserModal" title="Xem chi tiết">
                        <i class="bi bi-eye"></i>
                      </button>
                      <button class="btn btn-outline-secondary btn-sm" data-bs-toggle="modal" data-bs-target="#editUserModal" title="Chỉnh sửa">
                        <i class="bi bi-pencil"></i>
                      </button>
                      <button class="btn btn-outline-danger btn-sm" title="Khóa tài khoản">
                        <i class="bi bi-lock"></i>
                      </button>
                    </div>
                  </td>
                </tr>

                <!-- User 7 - Khách hàng -->
                <tr>
                  <td><span class="fw-semibold text-muted">7</span></td>
                  <td>
                    <div class="d-flex align-items-center gap-2">
                      <div class="user-avatar bg-customer">
                        <i class="bi bi-person"></i>
                      </div>
                      <span class="fw-semibold">user3</span>
                    </div>
                  </td>
                  <td><span class="text-muted">user3@gmail.com</span></td>
                  <td>Lê Thị B</td>
                  <td>0987654321</td>
                  <td><span class="badge role-badge badge-customer"><i class="bi bi-person me-1"></i>Khách hàng</span></td>
                  <td><span class="badge status-badge badge-active"><i class="bi bi-check-circle me-1"></i>Hoạt động</span></td>
                  <td><span class="text-muted small">25/01/2025</span></td>
                  <td class="text-center">
                    <div class="action-buttons justify-content-center">
                      <button class="btn btn-outline-primary btn-sm" data-bs-toggle="modal" data-bs-target="#viewUserModal" title="Xem chi tiết">
                        <i class="bi bi-eye"></i>
                      </button>
                      <button class="btn btn-outline-secondary btn-sm" data-bs-toggle="modal" data-bs-target="#editUserModal" title="Chỉnh sửa">
                        <i class="bi bi-pencil"></i>
                      </button>
                      <button class="btn btn-outline-danger btn-sm" title="Khóa tài khoản">
                        <i class="bi bi-lock"></i>
                      </button>
                    </div>
                  </td>
                </tr>

                <!-- User 8 - Khách hàng -->
                <tr>
                  <td><span class="fw-semibold text-muted">8</span></td>
                  <td>
                    <div class="d-flex align-items-center gap-2">
                      <div class="user-avatar bg-customer">
                        <i class="bi bi-person"></i>
                      </div>
                      <span class="fw-semibold">QuanTesteremail</span>
                    </div>
                  </td>
                  <td><span class="text-muted">nguyenhoangminhquan786@gmail.com</span></td>
                  <td>Nguyễn Hoàng Minh Quân</td>
                  <td>010100101</td>
                  <td><span class="badge role-badge badge-customer"><i class="bi bi-person me-1"></i>Khách hàng</span></td>
                  <td><span class="badge status-badge badge-active"><i class="bi bi-check-circle me-1"></i>Hoạt động</span></td>
                  <td><span class="text-muted small">25/01/2025</span></td>
                  <td class="text-center">
                    <div class="action-buttons justify-content-center">
                      <button class="btn btn-outline-primary btn-sm" data-bs-toggle="modal" data-bs-target="#viewUserModal" title="Xem chi tiết">
                        <i class="bi bi-eye"></i>
                      </button>
                      <button class="btn btn-outline-secondary btn-sm" data-bs-toggle="modal" data-bs-target="#editUserModal" title="Chỉnh sửa">
                        <i class="bi bi-pencil"></i>
                      </button>
                      <button class="btn btn-outline-danger btn-sm" title="Khóa tài khoản">
                        <i class="bi bi-lock"></i>
                      </button>
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>

          <!-- Pagination -->
          <div class="mt-4">
            <div class="d-flex justify-content-between align-items-center">
              <span class="text-muted small">Hiển thị 1-8 trên tổng 8 người dùng</span>
              <nav>
                <ul class="pagination mb-0">
                  <li class="page-item disabled">
                    <button class="page-link"><i class="bi bi-chevron-left"></i></button>
                  </li>
                  <li class="page-item active">
                    <button class="page-link">1</button>
                  </li>
                  <li class="page-item disabled">
                    <button class="page-link"><i class="bi bi-chevron-right"></i></button>
                  </li>
                </ul>
              </nav>
            </div>
          </div>
        </div>
      </div>
    </main>

    <!-- Modal Xem Chi Tiết User -->
    <div class="modal fade" id="viewUserModal" tabindex="-1" aria-hidden="true">
      <div class="modal-dialog modal-lg">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">
              <i class="bi bi-person-lines-fill me-2"></i>Chi tiết người dùng
            </h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
          </div>
          <div class="modal-body">
            <!-- User Info Card -->
            <div class="user-detail-card mb-4">
              <div class="row">
                <div class="col-md-2 text-center">
                  <div class="user-avatar-lg bg-customer">
                    <i class="bi bi-person-fill"></i>
                  </div>
                </div>
                <div class="col-md-10">
                  <div class="row">
                    <div class="col-md-6">
                      <div class="detail-item">
                        <label>Username</label>
                        <p>user1</p>
                      </div>
                    </div>
                    <div class="col-md-6">
                      <div class="detail-item">
                        <label>Email</label>
                        <p>user1@gmail.com</p>
                      </div>
                    </div>
                    <div class="col-md-6">
                      <div class="detail-item">
                        <label>Họ tên</label>
                        <p>Nguyễn Văn A</p>
                      </div>
                    </div>
                    <div class="col-md-6">
                      <div class="detail-item">
                        <label>Số điện thoại</label>
                        <p>0901234567</p>
                      </div>
                    </div>
                    <div class="col-md-4">
                      <div class="detail-item">
                        <label>Vai trò</label>
                        <p><span class="badge role-badge badge-customer"><i class="bi bi-person me-1"></i>Khách hàng</span></p>
                      </div>
                    </div>
                    <div class="col-md-4">
                      <div class="detail-item">
                        <label>Trạng thái</label>
                        <p><span class="badge status-badge badge-active"><i class="bi bi-check-circle me-1"></i>Hoạt động</span></p>
                      </div>
                    </div>
                    <div class="col-md-4">
                      <div class="detail-item">
                        <label>Ngày tạo</label>
                        <p>25/01/2025</p>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <!-- Địa Chỉ -->
            <h6 class="section-title"><i class="bi bi-geo-alt me-2"></i>Danh sách địa chỉ</h6>
            <div class="address-list mb-4">
              <div class="address-item">
                <div class="d-flex justify-content-between align-items-start">
                  <div>
                    <div class="fw-semibold">Nguyễn Văn A <span class="text-muted">| 0901234567</span></div>
                    <div class="text-muted small mt-1">123 Nguyễn Huệ A, Quận 1, TP.HCM</div>
                  </div>
                  <span class="badge bg-dark">Mặc định</span>
                </div>
              </div>
              <div class="address-item">
                <div class="d-flex justify-content-between align-items-start">
                  <div>
                    <div class="fw-semibold">Nguyễn Văn A-B <span class="text-muted">| 0901234567</span></div>
                    <div class="text-muted small mt-1">123 Nguyễn Huệ B, Quận 1, TP.HCM</div>
                  </div>
                </div>
              </div>
              <div class="address-item">
                <div class="d-flex justify-content-between align-items-start">
                  <div>
                    <div class="fw-semibold">Nguyễn Văn A-C <span class="text-muted">| 0901234567</span></div>
                    <div class="text-muted small mt-1">123 Nguyễn Huệ C, Quận 1, TP.HCM</div>
                  </div>
                </div>
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
                  <tr>
                    <td class="fw-semibold">#HD001</td>
                    <td>10/01/2025</td>
                    <td>COD</td>
                    <td><span class="badge bg-danger">Đã từ chối</span></td>
                    <td class="text-muted small">Đơn hàng đặt số lượng quá lớn</td>
                  </tr>
                  <tr>
                    <td class="fw-semibold">#HD005</td>
                    <td>14/01/2025</td>
                    <td>COD</td>
                    <td><span class="badge bg-danger">Đã từ chối</span></td>
                    <td class="text-muted small">Khách hủy đơn</td>
                  </tr>
                  <tr>
                    <td class="fw-semibold">#HD009</td>
                    <td>18/01/2025</td>
                    <td>COD</td>
                    <td><span class="badge bg-warning text-dark">Đang xử lý</span></td>
                    <td class="text-muted small">—</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">
              <i class="bi bi-x-circle me-2"></i>Đóng
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Modal Chỉnh sửa User -->
    <div class="modal fade" id="editUserModal" tabindex="-1" aria-hidden="true">
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">
              <i class="bi bi-pencil-square me-2"></i>Chỉnh sửa người dùng
            </h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
          </div>
          <form>
            <div class="modal-body">
              <div class="mb-3">
                <label class="form-label">Username</label>
                <input type="text" class="form-control" value="user1" disabled>
                <small class="text-muted">Username không thể thay đổi</small>
              </div>
              <div class="mb-3">
                <label class="form-label">Email</label>
                <input type="email" class="form-control" value="user1@gmail.com" disabled>
              </div>
              <div class="mb-3">
                <label class="form-label">Họ tên</label>
                <input type="text" class="form-control" value="Nguyễn Văn A">
              </div>
              <div class="mb-3">
                <label class="form-label">Số điện thoại</label>
                <input type="text" class="form-control" value="0901234567">
              </div>
              <div class="mb-3">
                <label class="form-label">Trạng thái tài khoản</label>
                <div class="form-check form-switch">
                  <input class="form-check-input" type="checkbox" id="statusSwitch" checked>
                  <label class="form-check-label" for="statusSwitch">
                    <span class="badge status-badge badge-active"><i class="bi bi-check-circle me-1"></i>Đang hoạt động</span>
                  </label>
                </div>
              </div>
              <div class="mb-3">
                <label class="form-label">Đặt lại mật khẩu</label>
                <button type="button" class="btn btn-outline-warning w-100">
                  <i class="bi bi-key me-2"></i>Reset mật khẩu
                </button>
              </div>
            </div>
            <div class="modal-footer">
              <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">
                <i class="bi bi-x-circle me-2"></i>Hủy
              </button>
              <button type="button" class="btn btn-dark">
                <i class="bi bi-check-circle me-2"></i>Lưu thay đổi
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import NV_Sidebar from '@/components/Shared/NV_Sidebar.vue';
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
