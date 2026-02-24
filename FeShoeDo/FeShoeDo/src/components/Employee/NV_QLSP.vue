<template>
  <div class="employee-layout">
    <NV_Sidebar />

    <main class="main-content">
      <div class="page-container">
        <div class="alert alert-success alert-dismissible fade show" role="alert">
          <span>Đã lưu sản phẩm thành công.</span>
          <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>

        <div class="row g-3 mb-4">
          <div class="col-md-3">
            <div class="stats-mini bg-gradient-1">
              <h4>128</h4>
              <p>Tổng sản phẩm</p>
            </div>
          </div>
          <div class="col-md-3">
            <div class="stats-mini bg-gradient-2">
              <h4>96</h4>
              <p>Đang hoạt động</p>
            </div>
          </div>
          <div class="col-md-3">
            <div class="stats-mini bg-gradient-3">
              <h4>12</h4>
              <p>Hết hàng</p>
            </div>
          </div>
          <div class="col-md-3">
            <div class="stats-mini bg-gradient-4">
              <h4>8</h4>
              <p>Sắp hết hàng</p>
            </div>
          </div>
        </div>

        <div class="content-card">
          <div class="d-flex justify-content-between align-items-center mb-4">
            <div>
              <h5 class="page-title">Danh sách sản phẩm</h5>
              <p class="page-subtitle">Quản lý và chỉnh sửa thông tin sản phẩm</p>
            </div>
            <button class="btn btn-dark" data-bs-toggle="modal" data-bs-target="#addProductModal">
              <i class="bi bi-plus-circle me-2"></i>Thêm sản phẩm mới
            </button>
          </div>

          <div class="filter-section">
            <div class="row g-3">
              <div class="col-md-3">
                <div class="input-group">
                  <span class="input-group-text bg-light">
                    <i class="bi bi-search"></i>
                  </span>
                  <input type="text" class="form-control" placeholder="Tìm tên sản phẩm...">
                </div>
              </div>
              <div class="col-md-2">
                <select class="form-select">
                  <option>Tất cả danh mục</option>
                  <option>Giày sneaker</option>
                  <option>Giày thể thao</option>
                  <option>Giày công sở</option>
                </select>
              </div>
              <div class="col-md-2">
                <select class="form-select">
                  <option>Tất cả giới tính</option>
                  <option>Nam</option>
                  <option>Nữ</option>
                  <option>Unisex</option>
                </select>
              </div>
              <div class="col-md-2">
                <select class="form-select">
                  <option>Trạng thái</option>
                  <option>Còn hàng</option>
                  <option>Hết hàng</option>
                  <option>Sắp hết</option>
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

          <div class="products-list">
            <div class="row">
              <div class="col-12">
                <div class="product-card">
                  <div class="row align-items-center" style="height: 100px;">
                    <div class="col-md-1">
                      <img src="https://placehold.co/80x80" alt="Sneaker Classic" class="product-image rounded">
                    </div>
                    <div class="col-md-8 align-items-center">
                      <div class="d-flex">
                        <h6 class="mb-1 me-2">Sneaker Classic 01</h6>
                        <p class="text-muted small mb-1 d-flex align-items-center">
                          || Giay sneaker <i class="bi bi-tag me-1"></i>
                        </p>
                      </div>
                      <p class="text-muted small mb-1">
                        <i class="bi bi-box me-1"></i> 3 phân loại
                      </p>
                      <div class="mb-1">
                        <span class="text-primary fw-bold">1.290.000 VND</span>
                      </div>
                      <div class="small">
                        <span class="me-3">Tồn kho: <strong>120</strong></span>
                      </div>
                    </div>
                    <div class="col-md-1 align-items-center">
                      <div class="small">
                        <span class="badge bg-success">Còn hàng</span>
                      </div>
                    </div>
                    <div class="col-md-2 align-items-end">
                      <div class="action-buttons">
                        <button class="btn btn-outline-primary btn-sm me-2" data-bs-toggle="modal" data-bs-target="#editProductModal" title="Chỉnh sửa">
                          <i class="bi bi-pencil"></i>
                        </button>
                        <button class="btn btn-outline-secondary btn-sm me-2" data-bs-toggle="modal" data-bs-target="#variantsModal" title="Xem phân loại">
                          <i class="bi bi-list-check"></i>
                        </button>
                        <button class="btn btn-outline-danger btn-sm" title="Xóa">
                          <i class="bi bi-trash"></i>
                        </button>
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <div class="col-12">
                <div class="product-card">
                  <div class="row align-items-center" style="height: 100px;">
                    <div class="col-md-1">
                      <img src="https://placehold.co/80x80" alt="Runner Pro" class="product-image rounded">
                    </div>
                    <div class="col-md-8 align-items-center">
                      <div class="d-flex">
                        <h6 class="mb-1 me-2">Runner Pro 2.0</h6>
                        <p class="text-muted small mb-1 d-flex align-items-center">
                          || Giay the thao <i class="bi bi-tag me-1"></i>
                        </p>
                      </div>
                      <p class="text-muted small mb-1">
                        <i class="bi bi-box me-1"></i> 4 phân loại
                      </p>
                      <div class="mb-1">
                        <span class="text-primary fw-bold">1.990.000 VND</span>
                      </div>
                      <div class="small">
                        <span class="me-3">Tồn kho: <strong>12</strong></span>
                      </div>
                    </div>
                    <div class="col-md-1 align-items-center">
                      <div class="small">
                        <span class="badge bg-warning text-dark">Sắp hết</span>
                      </div>
                    </div>
                    <div class="col-md-2 align-items-end">
                      <div class="action-buttons">
                        <button class="btn btn-outline-primary btn-sm me-2" data-bs-toggle="modal" data-bs-target="#editProductModal" title="Chỉnh sửa">
                          <i class="bi bi-pencil"></i>
                        </button>
                        <button class="btn btn-outline-secondary btn-sm me-2" data-bs-toggle="modal" data-bs-target="#variantsModal" title="Xem phân loại">
                          <i class="bi bi-list-check"></i>
                        </button>
                        <button class="btn btn-outline-danger btn-sm" title="Xóa">
                          <i class="bi bi-trash"></i>
                        </button>
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <div class="col-12">
                <div class="product-card">
                  <div class="row align-items-center" style="height: 100px;">
                    <div class="col-md-1">
                      <img src="https://placehold.co/80x80" alt="Office Leather" class="product-image rounded">
                    </div>
                    <div class="col-md-8 align-items-center">
                      <div class="d-flex">
                        <h6 class="mb-1 me-2">Office Leather 88</h6>
                        <p class="text-muted small mb-1 d-flex align-items-center">
                          || Giay cong so <i class="bi bi-tag me-1"></i>
                        </p>
                      </div>
                      <p class="text-muted small mb-1">
                        <i class="bi bi-box me-1"></i> 2 phân loại
                      </p>
                      <div class="mb-1">
                        <span class="text-primary fw-bold">2.490.000 VND</span>
                      </div>
                      <div class="small">
                        <span class="me-3">Tồn kho: <strong>0</strong></span>
                      </div>
                    </div>
                    <div class="col-md-1 align-items-center">
                      <div class="small">
                        <span class="badge bg-danger">Hết hàng</span>
                      </div>
                    </div>
                    <div class="col-md-2 align-items-end">
                      <div class="action-buttons">
                        <button class="btn btn-outline-primary btn-sm me-2" data-bs-toggle="modal" data-bs-target="#editProductModal" title="Chỉnh sửa">
                          <i class="bi bi-pencil"></i>
                        </button>
                        <button class="btn btn-outline-secondary btn-sm me-2" data-bs-toggle="modal" data-bs-target="#variantsModal" title="Xem phân loại">
                          <i class="bi bi-list-check"></i>
                        </button>
                        <button class="btn btn-outline-danger btn-sm" title="Xóa">
                          <i class="bi bi-trash"></i>
                        </button>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <div class="mt-4">
              <nav>
                <ul class="pagination justify-content-center">
                  <li class="page-item disabled">
                    <button class="page-link">
                      <i class="bi bi-chevron-left"></i>
                    </button>
                  </li>
                  <li class="page-item active">
                    <button class="page-link">1</button>
                  </li>
                  <li class="page-item">
                    <button class="page-link">2</button>
                  </li>
                  <li class="page-item">
                    <button class="page-link">3</button>
                  </li>
                  <li class="page-item">
                    <button class="page-link">
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

    <div class="modal fade" id="addProductModal" tabindex="-1" aria-hidden="true">
      <div class="modal-dialog modal-lg">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">
              <i class="bi bi-plus-circle me-2"></i>Thêm sản phẩm mới
            </h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
          </div>
          <form>
            <div class="modal-body">
              <h6 class="mb-3"><i class="bi bi-info-circle me-2"></i>Thông tin cơ bản</h6>
              <div class="row mb-3">
                <div class="col-md-12">
                  <label class="form-label">Tên sản phẩm *</label>
                  <input type="text" class="form-control" value="Street Sneaker Lite">
                </div>
              </div>

              <div class="row mb-3 align-items-start">
                <div class="col-md-8">
                  <label class="form-label">Danh mục * <small class="text-muted">(Có thể chọn nhiều)</small></label>
                  <div class="d-flex flex-wrap gap-2 category-pills">
                    <input type="checkbox" class="btn-check" id="cat-snk-add" checked>
                    <label class="btn btn-outline-dark btn-sm rounded-pill px-3" for="cat-snk-add">Giày sneaker</label>

                    <input type="checkbox" class="btn-check" id="cat-sport-add">
                    <label class="btn btn-outline-dark btn-sm rounded-pill px-3" for="cat-sport-add">Giày thể thao</label>

                    <input type="checkbox" class="btn-check" id="cat-office-add">
                    <label class="btn btn-outline-dark btn-sm rounded-pill px-3" for="cat-office-add">Giày công sở</label>

                    <input type="checkbox" class="btn-check" id="cat-leather-add">
                    <label class="btn btn-outline-dark btn-sm rounded-pill px-3" for="cat-leather-add">Giày da</label>
                  </div>
                </div>
                <div class="col-md-4 mt-4">
                  <button type="button" class="btn btn-outline-dark w-100" data-bs-toggle="modal" data-bs-target="#addCategoryModal">
                    <i class="bi bi-plus-circle me-2"></i>Thêm danh mục
                  </button>
                </div>
              </div>

              <div class="mb-3">
                <label class="form-label">Giới tính *</label>
                <div class="btn-group w-100 gender-group" role="group">
                  <input type="radio" class="btn-check" name="gender-add" id="gender-male-add" value="male" checked>
                  <label class="btn btn-outline-dark" for="gender-male-add">
                    <i class="bi bi-gender-male me-1"></i> Nam
                  </label>
                  
                  <input type="radio" class="btn-check" name="gender-add" id="gender-female-add" value="female">
                  <label class="btn btn-outline-dark" for="gender-female-add">
                    <i class="bi bi-gender-female me-1"></i> Nữ
                  </label>
                  
                  <input type="radio" class="btn-check" name="gender-add" id="gender-unisex-add" value="unisex">
                  <label class="btn btn-outline-dark" for="gender-unisex-add">
                    <i class="bi bi-gender-ambiguous me-1"></i> Unisex
                  </label>
                </div>
              </div>

              <div class="mb-3">
                <label class="form-label">Mô tả sản phẩm</label>
                <textarea class="form-control" rows="5">Giay sneaker phong cach streetwear, phu hop di hoc va di choi.</textarea>
              </div>
              <hr>
              <h6 class="mb-3 mt-4">
                <i class="bi bi-layers me-2"></i>Phân loại sản phẩm
              </h6>
              
              <div class="mb-3">
                <div class="d-flex justify-content-between align-items-center mb-2">
                  <div class="fw-bold text-primary">#1</div>
                  <button type="button" class="btn btn-outline-danger btn-sm d-flex align-items-center">
                    <i class="bi bi-trash me-1"></i> Xóa
                  </button>
                </div>

                <div class="card">
                  <div class="card-body">
                    <div class="row g-3">
                      <div class="col-md-3">
                        <label class="form-label">Màu sắc *</label>
                        <input type="text" class="form-control" value="Trắng" placeholder="VD: Đen, Trắng">
                      </div>
                      <div class="col-md-3">
                        <label class="form-label">Size *</label>
                        <select class="form-select">
                          <option value="">Chọn size</option>
                          <option value="41">41</option>
                          <option value="42" selected>42</option>
                          <option value="43">43</option>
                        </select>
                      </div>
                      <div class="col-md-6">
                        <label class="form-label">Đơn giá (VND) *</label>
                        <input type="number" class="form-control" value="1290000">
                      </div>
                      <div class="col-md-4">
                        <label class="form-label">Số lượng *</label>
                        <input type="number" class="form-control" value="50">
                      </div>
                      <div class="col-md-4">
                        <label class="form-label">Trạng thái</label>
                        <select class="form-select">
                          <option selected>Còn hàng</option>
                          <option>Hết hàng</option>
                        </select>
                      </div>
                      <div class="col-md-4">
                        <label class="form-label">Hình ảnh</label>
                        <div class="input-group">
                          <input type="text" class="form-control" value="sneaker-white-42.jpg">
                          <button type="button" class="btn btn-outline-secondary" data-bs-toggle="modal" data-bs-target="#imagePickerModal">
                            <i class="bi bi-image"></i>
                          </button>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
                <hr>
              </div>

              <div class="mb-3">
                <div class="d-flex justify-content-between align-items-center mb-2">
                  <div class="fw-bold text-primary">#2</div>
                  <button type="button" class="btn btn-outline-danger btn-sm d-flex align-items-center">
                    <i class="bi bi-trash me-1"></i> Xóa
                  </button>
                </div>

                <div class="card">
                  <div class="card-body">
                    <div class="row g-3">
                      <div class="col-md-3">
                        <label class="form-label">Màu sắc *</label>
                        <input type="text" class="form-control" value="Den" placeholder="VD: Đen, Trắng">
                      </div>
                      <div class="col-md-3">
                        <label class="form-label">Size *</label>
                        <select class="form-select">
                          <option value="">Chọn size</option>
                          <option value="41" selected>41</option>
                          <option value="42">42</option>
                        </select>
                      </div>
                      <div class="col-md-6">
                        <label class="form-label">Đơn giá (VND) *</label>
                        <input type="number" class="form-control" value="1290000">
                      </div>
                      <div class="col-md-4">
                        <label class="form-label">Số lượng *</label>
                        <input type="number" class="form-control" value="36">
                      </div>
                      <div class="col-md-4">
                        <label class="form-label">Trạng thái</label>
                        <select class="form-select">
                          <option selected>Còn hàng</option>
                          <option>Hết hàng</option>
                        </select>
                      </div>
                      <div class="col-md-4">
                        <label class="form-label">Hình ảnh</label>
                        <div class="input-group">
                          <input type="text" class="form-control" value="sneaker-black-41.jpg">
                          <button type="button" class="btn btn-outline-secondary" data-bs-toggle="modal" data-bs-target="#imagePickerModal">
                            <i class="bi bi-image"></i>
                          </button>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
                <hr>
              </div>

              <div class="text-center mb-3">
                <button type="button" class="btn btn-outline-primary border-dashed w-100 py-2">
                  <i class="bi bi-plus-circle me-2"></i>Thêm phân loại
                </button>
              </div>
            </div>
            <div class="modal-footer">
              <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">
                <i class="bi bi-x-circle me-2"></i>Hủy
              </button>
              <button type="button" class="btn btn-dark">
                <i class="bi bi-check-circle me-2"></i>Lưu sản phẩm
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>

    <div class="modal fade" id="editProductModal" tabindex="-1" aria-hidden="true">
      <div class="modal-dialog modal-lg">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">
              <i class="bi bi-pencil me-2"></i>Chỉnh sửa sản phẩm
            </h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
          </div>
          <form>
            <div class="modal-body">
              <h6 class="mb-3"><i class="bi bi-info-circle me-2"></i>Thông tin cơ bản</h6>
              <div class="row mb-3">
                <div class="col-md-12">
                  <label class="form-label">Tên sản phẩm *</label>
                  <input type="text" class="form-control" value="Sneaker Classic 01">
                </div>
              </div>
              
              <div class="row mb-3 align-items-start">
                <div class="col-md-12">
                  <label class="form-label">Danh mục * <small class="text-muted">(Có thể chọn nhiều)</small></label>
                  <div class="d-flex flex-wrap gap-2 category-pills">
                    <input type="checkbox" class="btn-check" id="cat-snk-edit" checked>
                    <label class="btn btn-outline-dark btn-sm rounded-pill px-3" for="cat-snk-edit">Giày sneaker</label>

                    <input type="checkbox" class="btn-check" id="cat-sport-edit">
                    <label class="btn btn-outline-dark btn-sm rounded-pill px-3" for="cat-sport-edit">Giày thể thao</label>

                    <input type="checkbox" class="btn-check" id="cat-office-edit">
                    <label class="btn btn-outline-dark btn-sm rounded-pill px-3" for="cat-office-edit">Giày công sở</label>
                  </div>
                </div>
              </div>

              <div class="mb-3">
                <label class="form-label">Giới tính *</label>
                <div class="btn-group w-100 gender-group" role="group">
                  <input type="radio" class="btn-check" name="gender-edit" id="gender-male-edit" value="male" checked>
                  <label class="btn btn-outline-dark" for="gender-male-edit">
                    <i class="bi bi-gender-male me-1"></i> Nam
                  </label>
                  
                  <input type="radio" class="btn-check" name="gender-edit" id="gender-female-edit" value="female">
                  <label class="btn btn-outline-dark" for="gender-female-edit">
                    <i class="bi bi-gender-female me-1"></i> Nữ
                  </label>
                  
                  <input type="radio" class="btn-check" name="gender-edit" id="gender-unisex-edit" value="unisex">
                  <label class="btn btn-outline-dark" for="gender-unisex-edit">
                    <i class="bi bi-gender-ambiguous me-1"></i> Unisex
                  </label>
                </div>
              </div>

              <div class="mb-3">
                <label class="form-label">Mô tả sản phẩm</label>
                <textarea class="form-control" rows="5">Sneaker phong cach co dien, de day, de mang va ben.</textarea>
              </div>
              <hr>
              <h6 class="mb-3 mt-4"><i class="bi bi-layers me-2"></i>Phân loại sản phẩm</h6>
              <div class="mb-3">
                <div class="d-flex justify-content-between align-items-center mb-2">
                  <div class="fw-bold text-primary">#1</div>
                  <div class="fw-bold">
                    <button type="button" class="btn btn-outline-danger btn-sm align-items-center">
                      <i class="bi bi-trash me-1"></i>Xóa
                    </button>
                  </div>
                </div>
                <div class="card bg-light border-0">
                  <div class="card-body">
                    <div class="row g-3">
                      <div class="col-md-3">
                        <label class="form-label">Màu sắc *</label>
                        <input type="text" class="form-control" value="Trắng" placeholder="VD: Đen, Trắng">
                      </div>
                      <div class="col-md-3">
                        <label class="form-label">Size *</label>
                        <select class="form-select">
                          <option value="">Chọn size</option>
                          <option value="42" selected>42</option>
                        </select>
                      </div>
                      <div class="col-md-6">
                        <label class="form-label">Đơn giá (VND) *</label>
                        <input type="number" class="form-control" value="1290000">
                      </div>
                      <div class="col-md-4">
                        <label class="form-label">Số lượng *</label>
                        <input type="number" class="form-control" value="80">
                      </div>
                      <div class="col-md-4">
                        <label class="form-label">Trạng thái</label>
                        <select class="form-select">
                          <option selected>Còn hàng</option>
                          <option>Hết hàng</option>
                        </select>
                      </div>
                      <div class="col-md-4">
                        <label class="form-label">Hình ảnh</label>
                        <div class="input-group">
                          <input type="text" class="form-control" value="sneaker-white-42.jpg">
                          <button type="button" class="btn btn-outline-secondary" data-bs-toggle="modal" data-bs-target="#imagePickerModal">
                            <i class="bi bi-image"></i>
                          </button>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
                <hr>
              </div>

              <div class="text-center mb-3">
                <button type="button" class="btn btn-outline-primary border-dashed w-100 py-2">
                  <i class="bi bi-plus-circle me-2"></i>Thêm phân loại
                </button>
              </div>
            </div>
            <div class="modal-footer">
              <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">
                <i class="bi bi-x-circle me-2"></i>Hủy
              </button>
              <button type="button" class="btn btn-dark">
                <i class="bi bi-check-circle me-2"></i>Cập nhật
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>

    <div class="modal fade" id="variantsModal" tabindex="-1" aria-hidden="true">
      <div class="modal-dialog modal-lg">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">
              <i class="bi bi-list-check me-2"></i>Phân loại sản phẩm: Sneaker Classic 01
            </h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
          </div>
          <div class="modal-body">
            <div class="table-responsive">
              <table class="table table-hover">
                <thead>
                  <tr>
                    <th>#</th>
                    <th>Phân loại</th>
                    <th>Hình ảnh</th>
                    <th>Số lượng</th>
                    <th>Đơn giá</th>
                    <th>Trạng thái</th>
                  </tr>
                </thead>
                <tbody>
                  <tr>
                    <td>1</td>
                    <td>Trắng - Size 42</td>
                    <td>
                      <img src="https://placehold.co/56x56" alt="Trắng - Size 42" class="variant-image">
                    </td>
                    <td>80</td>
                    <td>1.290.000 VND</td>
                    <td>
                      <span class="badge bg-success">Còn hàng</span>
                    </td>
                  </tr>
                  <tr>
                    <td>2</td>
                    <td>Den - Size 41</td>
                    <td>
                      <img src="https://placehold.co/56x56" alt="Den - Size 41" class="variant-image">
                    </td>
                    <td>40</td>
                    <td>1.290.000 VND</td>
                    <td>
                      <span class="badge bg-warning text-dark">Sắp hết</span>
                    </td>
                  </tr>
                  <tr>
                    <td>3</td>
                    <td>Xanh - Size 43</td>
                    <td>
                      <img src="https://placehold.co/56x56" alt="Xanh - Size 43" class="variant-image">
                    </td>
                    <td>0</td>
                    <td>1.290.000 VND</td>
                    <td>
                      <span class="badge bg-danger">Hết hàng</span>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Đóng</button>
          </div>
        </div>
      </div>
    </div>

    <div class="modal fade" id="addCategoryModal" tabindex="-1" aria-hidden="true">
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">
              <i class="bi bi-plus-circle me-2"></i>Thêm danh mục mới
            </h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
          </div>
          <form>
            <div class="modal-body">
              <div class="mb-3">
                <label class="form-label">Tên danh mục *</label>
                <input type="text" class="form-control" value="Giay lifestyle">
              </div>
            </div>
            <div class="modal-footer">
              <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">
                <i class="bi bi-x-circle me-2"></i>Hủy
              </button>
              <button type="button" class="btn btn-primary">
                <i class="bi bi-check-circle me-2"></i>Thêm
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>

    <div class="modal fade" id="imagePickerModal" tabindex="-1" aria-hidden="true">
      <div class="modal-dialog modal-xl">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">
              <i class="bi bi-image me-2"></i>Chọn hình ảnh
            </h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
          </div>
          <div class="modal-body">
            <div class="mb-4">
              <div class="image-upload-area">
                <i class="bi bi-cloud-upload display-4 mb-3"></i>
                <p class="mb-2"><strong>Kéo thả hoặc nhấp để tải ảnh lên</strong></p>
                <p class="text-muted small mb-0">Hỗ trợ: JPG, PNG, JPEG, GIF, WEBP (Tối đa 5MB)</p>
                <input type="file" accept="image/*" hidden>
              </div>
            </div>

            <h6 class="mb-3">Hình ảnh có sẵn</h6>
            <div class="row g-3">
              <div class="col-3 col-md-2">
                <div class="image-thumbnail selected">
                  <img src="https://placehold.co/120x120" alt="sneaker-white-42.jpg">
                  <div class="image-name">sneaker-white-42.jpg</div>
                </div>
              </div>
              <div class="col-3 col-md-2">
                <div class="image-thumbnail">
                  <img src="https://placehold.co/120x120" alt="sneaker-black-41.jpg">
                  <div class="image-name">sneaker-black-41.jpg</div>
                </div>
              </div>
              <div class="col-3 col-md-2">
                <div class="image-thumbnail">
                  <img src="https://placehold.co/120x120" alt="runner-pro-blue.jpg">
                  <div class="image-name">runner-pro-blue.jpg</div>
                </div>
              </div>
              <div class="col-3 col-md-2">
                <div class="image-thumbnail">
                  <img src="https://placehold.co/120x120" alt="office-leather-brown.jpg">
                  <div class="image-name">office-leather-brown.jpg</div>
                </div>
              </div>
              <div class="col-3 col-md-2">
                <div class="image-thumbnail">
                  <img src="https://placehold.co/120x120" alt="street-lite-white.jpg">
                  <div class="image-name">street-lite-white.jpg</div>
                </div>
              </div>
            </div>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Hủy</button>
            <button type="button" class="btn btn-primary">Chọn ảnh</button>
          </div>
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

.bg-gradient-1, .bg-gradient-2, .bg-gradient-3, .bg-gradient-4 {
  background: linear-gradient(135deg, #212529, #000000);
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

/* Product Card */
.product-card {
  background: white;
  border: 1px solid #e0e0e0;
  border-radius: 12px;
  padding: 20px;
  transition: all 0.3s;
  margin-bottom: 15px;
}

.product-card:hover {
  box-shadow: 0 6px 15px rgba(0, 0, 0, 0.08);
  transform: translateY(-2px);
  border-color: #d0d0d0;
}

.product-image {
  max-width: 80px;
}

.variant-image {
  width: 50px;
  height: 50px;
  object-fit: cover;
  border-radius: 6px;
}

.action-buttons {
  display: flex;
  gap: 8px;
}

/* Image Upload Area */
.image-upload-area {
  border: 2px dashed #ddd;
  border-radius: 8px;
  padding: 40px 20px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
  background: #fafafa;
}

.image-upload-area:hover {
  border-color: #667eea;
  background: #f8f9ff;
}

/* Image Thumbnail */
.image-thumbnail {
  position: relative;
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s;
}

.image-thumbnail:hover {
  border-color: #667eea;
  transform: scale(1.05);
}

.image-thumbnail.selected {
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.3);
}

.image-thumbnail img {
  width: 100%;
  height: 100px;
  object-fit: cover;
}

.image-name {
  padding: 5px;
  font-size: 11px;
  text-align: center;
  background: #f8f9fa;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* Badge */
.badge {
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
}

/* UI mới cho Nút và Tag */
.category-pills .btn-check:checked + .btn {
  background-color: #212529;
  color: white;
  border-color: #212529;
  box-shadow: 0 2px 5px rgba(0,0,0,0.2);
}

.category-pills .btn {
  transition: all 0.2s;
}

.gender-group .btn-check:checked + .btn {
  background-color: #212529;
  color: white;
  border-color: #212529;
}

.border-dashed {
  border-style: dashed !important;
  border-width: 2px !important;
}

/* Responsive */
@media (max-width: 768px) {
  .main-content {
    margin-left: 0;
  }
  
  .page-container {
    padding: 15px;
  }
  
  .product-card .row {
    flex-direction: column;
  }
  
  .action-buttons {
    justify-content: center;
    margin-top: 15px;
  }
}
</style>