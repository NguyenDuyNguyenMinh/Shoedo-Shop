<template>
  <div class="toast-container position-fixed top-0 start-50 translate-middle-x p-3 mt-2" style="z-index: 1090;">
  <transition name="toast-fade">
    <div
      v-if="toast.show"
      :key="toast.id"
      class="toast show align-items-center text-white border-0 shadow-lg overflow-hidden"
      :class="`bg-${toast.type}`"
      role="alert"
    >
      <div class="d-flex position-relative z-1">
        <div class="toast-body d-flex align-items-center fs-6">
          <i
            class="bi me-2 fs-5"
            :class="{
              'bi-check-circle-fill': toast.type === 'success',
              'bi-exclamation-triangle-fill': toast.type === 'warning',
              'bi-x-circle-fill': toast.type === 'danger',
              'bi-info-circle-fill': toast.type === 'info'
            }"
          ></i>
          {{ toast.message }}
        </div>
        <button
          type="button"
          class="btn-close btn-close-white me-2 m-auto"
          @click="toast.show = false"
        ></button>
      </div>
      <div class="toast-progress-bar"></div>
    </div>
  </transition>
</div>
  <div class="employee-layout">
    <NV_Sidebar />

    <main class="main-content">
      <div class="page-container">
        <div
          v-if="successMessage"
          class="alert alert-success alert-dismissible fade show shadow-sm"
          role="alert"
        >
          <i class="bi bi-check-circle-fill me-2"></i>
          <span>{{ successMessage }}</span>
          <button
            type="button"
            class="btn-close"
            @click="successMessage = ''"
          ></button>
        </div>

        <div class="row g-3 mb-4">
          <div class="col-md-3">
            <div class="stats-mini bg-gradient-1">
              <h4>{{ totalProducts }}</h4>
              <p>Tổng sản phẩm</p>
            </div>
          </div>
          <div class="col-md-3">
            <div class="stats-mini bg-gradient-2">
              <h4>{{ activeProducts }}</h4>
              <p>Đang hoạt động</p>
            </div>
          </div>
          <div class="col-md-3">
            <div class="stats-mini bg-gradient-4">
              <h4>{{ lowStock }}</h4>
              <p>Sắp hết hàng</p>
            </div>
          </div>
          <div class="col-md-3">
            <div class="stats-mini bg-gradient-3">
              <h4>{{ outOfStock }}</h4>
              <p>Hết hàng</p>
            </div>
          </div>
        </div>

        <div class="content-card">
          <div class="d-flex justify-content-between align-items-center mb-4">
            <div>
              <h5 class="page-title">Danh sách sản phẩm</h5>
              <p class="page-subtitle">
                Quản lý và chỉnh sửa thông tin sản phẩm
              </p>
            </div>
            <button
              class="btn btn-dark"
              data-bs-toggle="modal"
              data-bs-target="#addProductModal"
            >
              <i class="bi bi-plus-circle me-2"></i>Thêm sản phẩm mới
            </button>
          </div>

          <div class="filter-section">
            <div class="row g-3">
              <div class="col-md-4">
                <div class="input-group">
                  <span class="input-group-text bg-light">
                    <i class="bi bi-search"></i>
                  </span>
                  <input
                    type="text"
                    v-model="filterKeyword"
                    class="form-control"
                    placeholder="Tìm tên sản phẩm..."
                  />
                </div>
              </div>
              <div class="col-md-2">
                <select v-model="filterCategory" class="form-select">
                  <option value="">Tất cả danh mục</option>
                  <option
                    v-for="cat in categories"
                    :key="cat.maDM"
                    :value="cat.tenDM"
                  >
                    {{ cat.tenDM }}
                  </option>
                </select>
              </div>
              <div class="col-md-2">
                <select v-model="filterGender" class="form-select">
                  <option value="">Tất cả giới tính</option>
                  <option value="Nam">Nam</option>
                  <option value="Nữ">Nữ</option>
                  <option value="Unisex">Unisex</option>
                </select>
              </div>
              <div class="col-md-2">
                <select v-model="filterStatus" class="form-select">
                  <option value="">Tất cả trạng thái</option>

                  <optgroup label="Trạng thái hiển thị">
                    <option value="Đang hoạt động">
                      Đang hoạt động (Hiện)
                    </option>
                    <option value="Đã ẩn">Không hoạt động (Ẩn)</option>
                  </optgroup>

                  <optgroup label="Tình trạng tồn kho">
                    <option value="Còn hàng">Còn hàng</option>
                    <option value="Hết hàng">Hết hàng</option>
                    <option value="Sắp hết">Sắp hết</option>
                  </optgroup>
                </select>
              </div>
              <div class="col-md-2">
                <button
                  @click="resetFilters"
                  class="btn btn-outline-secondary w-100"
                >
                  <i class="bi bi-arrow-clockwise me-2"></i>Reset
                </button>
              </div>
            </div>
          </div>

          <div class="products-list">
            <div class="row">
              <div
                class="col-12"
                v-for="item in paginatedProducts"
                :key="item.maSP"
              >
                <div
                  class="product-card"
                  :class="{ 'product-hidden': !item.isActive }"
                >
                  <span
                    class="status-badge-corner"
                    :class="[
                      'badge',
                      item.trangThai === 'Còn hàng'
                        ? 'bg-success'
                        : item.trangThai === 'Sắp hết'
                        ? 'bg-warning text-dark'
                        : 'bg-danger',
                    ]"
                  >
                    {{ item.trangThai }}
                  </span>
                  <div class="row align-items-center" style="height: 100px">
                    <div class="col-md-1 me-3">
                      <img
                        :src="
                          item.hinhAnhDaiDien
                            ? `http://localhost:8080/images/${item.hinhAnhDaiDien}`
                            : 'https://placehold.co/80x80'
                        "
                        alt="Product"
                        class="product-image rounded"
                        style="width: 80px; height: 80px; object-fit: cover"
                      />
                    </div>
                    <div class="col-md-8 align-items-center">
                      <div class="d-flex">
                        <h6 class="mb-1 me-2">{{ item.tenSP }}</h6>
                        <p
                          class="text-muted small mb-1 d-flex align-items-center"
                        >
                          || {{ item.danhMucs || "Chưa phân loại" }}
                          <i class="bi bi-tag me-1"></i>
                        </p>
                      </div>
                      <p class="text-muted small mb-1">
                        <i class="bi bi-box me-1"></i>
                        {{ item.soPhanLoai }} phân loại
                      </p>
                      <div class="mb-1">
                        <template
                          v-if="!item.khuyenMai || item.khuyenMai === 0"
                        >
                          <span class="text-primary fw-bold">
                            <template v-if="item.giaMin === item.giaMax">
                              {{ item.giaMin?.toLocaleString("vi-VN") }} ₫
                            </template>
                            <template v-else>
                              {{ item.giaMin?.toLocaleString("vi-VN") }} ₫ -
                              {{ item.giaMax?.toLocaleString("vi-VN") }} ₫
                            </template>
                          </span>
                        </template>

                        <template v-else>
                          <div
                            class="d-flex align-items-center flex-wrap gap-2"
                          >
                            <span
                              class="text-muted text-decoration-line-through small"
                            >
                              <template v-if="item.giaMin === item.giaMax">
                                {{ item.giaMin?.toLocaleString("vi-VN") }} ₫
                              </template>
                              <template v-else>
                                {{ item.giaMin?.toLocaleString("vi-VN") }} ₫ -
                                {{ item.giaMax?.toLocaleString("vi-VN") }} ₫
                              </template>
                            </span>

                            <span class="text-danger fw-bold">
                              <template v-if="item.giaMin === item.giaMax">
                                {{
                                  (
                                    item.giaMin -
                                    (item.giaMin * item.khuyenMai) / 100
                                  )?.toLocaleString("vi-VN")
                                }}
                                ₫
                              </template>
                              <template v-else>
                                {{
                                  (
                                    item.giaMin -
                                    (item.giaMin * item.khuyenMai) / 100
                                  )?.toLocaleString("vi-VN")
                                }}
                                ₫ -
                                {{
                                  (
                                    item.giaMax -
                                    (item.giaMax * item.khuyenMai) / 100
                                  )?.toLocaleString("vi-VN")
                                }}
                                ₫
                              </template>
                            </span>

                            <span class="badge bg-danger"
                              >-{{ item.khuyenMai }}%</span
                            >
                          </div>
                        </template>
                      </div>
                      <div class="small">
                        <span class="me-3"
                          >Tồn kho tổng:
                          <strong>{{ item.tongTonKho }}</strong></span
                        >
                        <span v-if="!item.isActive" class="text-danger"
                          ><i class="bi bi-eye-slash"></i> Đã ẩn</span
                        >
                      </div>
                    </div>
                    <!-- <div class="col-md-1 align-items-center">
                      <div class="small">
                        <span
                          :class="[
                            'badge',
                            item.trangThai === 'Còn hàng'
                              ? 'bg-success'
                              : item.trangThai === 'Sắp hết'
                              ? 'bg-warning text-dark'
                              : 'bg-danger',
                          ]"
                        >
                          {{ item.trangThai }}
                        </span>
                      </div>
                    </div> -->
                    <div class="col-md-2 align-items-end">
                      <div class="action-buttons">
                        <button
                          type="button"
                          class="btn btn-outline-primary btn-sm me-2"
                          @click="openEditModal(item.maSP)"
                          data-bs-toggle="modal"
                          data-bs-target="#editProductModal"
                          title="Chỉnh sửa"
                        >
                          <i class="bi bi-pencil"></i>
                        </button>
                        <button
                          class="btn btn-outline-secondary btn-sm me-2"
                          data-bs-toggle="modal"
                          data-bs-target="#variantsModal"
                          title="Xem phân loại"
                          @click="viewVariants(item)"
                        >
                          <i class="bi bi-list-check"></i>
                        </button>
                        <button
                          type="button"
                          class="btn btn-sm"
                          :class="
                            item.isActive
                              ? 'btn-outline-danger'
                              : 'btn-outline-success'
                          "
                          :title="
                            item.isActive ? 'Ẩn sản phẩm' : 'Hiện sản phẩm'
                          "
                          @click="toggleProductStatus(item.maSP, item.isActive)"
                        >
                          <i
                            class="bi"
                            :class="item.isActive ? 'bi-trash' : 'bi-eye'"
                          ></i>
                        </button>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
              <div
                v-if="totalPages > 1"
                class="d-flex justify-content-center align-items-center mt-4 mb-2"
              >
                <nav aria-label="Page navigation">
                  <ul class="pagination pagination-sm mb-0">
                    <li
                      class="page-item"
                      :class="{ disabled: currentPage === 1 }"
                    >
                      <button
                        class="page-link text-dark"
                        @click="goToPage(currentPage - 1)"
                      >
                        <i class="bi bi-chevron-left"></i> Trước
                      </button>
                    </li>

                    <li
                      class="page-item"
                      v-for="page in totalPages"
                      :key="page"
                      :class="{ active: currentPage === page }"
                    >
                      <button
                        class="page-link"
                        :class="
                          currentPage === page
                            ? 'bg-dark border-dark text-white'
                            : 'text-dark'
                        "
                        @click="goToPage(page)"
                      >
                        {{ page }}
                      </button>
                    </li>

                    <li
                      class="page-item"
                      :class="{ disabled: currentPage === totalPages }"
                    >
                      <button
                        class="page-link text-dark"
                        @click="goToPage(currentPage + 1)"
                      >
                        Sau <i class="bi bi-chevron-right"></i>
                      </button>
                    </li>
                  </ul>
                </nav>
              </div>
              <div
                v-if="products.length === 0"
                class="text-center py-5 text-muted"
              >
                Đang tải dữ liệu hoặc chưa có sản phẩm nào...
              </div>
            </div>
          </div>
        </div>
      </div>
    </main>

    <div
      class="modal fade"
      id="addCategoryModal"
      tabindex="-1"
      aria-hidden="true"
    >
      <div class="modal-dialog modal-dialog-centered modal-sm">
        <div class="modal-content">
          <div class="modal-header">
            <h6 class="modal-title">
              <i class="bi bi-tags me-2"></i>Thêm danh mục mới
            </h6>
            <button
              type="button"
              class="btn-close"
              id="closeCategoryModalBtn"
              data-bs-toggle="modal"
              :data-bs-target="
                categoryMode === 'edit'
                  ? '#editProductModal'
                  : '#addProductModal'
              "
            ></button>
          </div>
          <div class="modal-body">
            <input
              type="text"
              class="form-control"
              v-model="newCategoryName"
              placeholder="Nhập tên danh mục..."
              @keyup.enter="saveNewCategory"
            />
          </div>
          <div class="modal-footer p-2">
            <button
              type="button"
              class="btn btn-sm btn-secondary"
              data-bs-toggle="modal"
              :data-bs-target="
                categoryMode === 'edit'
                  ? '#editProductModal'
                  : '#addProductModal'
              "
            >
              Hủy
            </button>
            <button
              type="button"
              class="btn btn-sm btn-dark"
              @click="saveNewCategory"
            >
              Lưu lại
            </button>
          </div>
        </div>
      </div>
    </div>

    <div
      class="modal fade"
      id="addProductModal"
      tabindex="-1"
      aria-hidden="true"
    >
      <div class="modal-dialog modal-lg">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">
              <i class="bi bi-plus-circle me-2"></i>Thêm sản phẩm mới
            </h5>
            <button
              type="button"
              class="btn-close"
              data-bs-dismiss="modal"
            ></button>
          </div>
          <form @submit.prevent="saveProduct">
            <div class="modal-body">
              <h6 class="mb-3">
                <i class="bi bi-info-circle me-2"></i>Thông tin cơ bản
              </h6>
              <div class="row mb-3">
                <div class="col-md-12">
                  <label class="form-label">Tên sản phẩm *</label>
                  <input
                    type="text"
                    class="form-control"
                    v-model="newProduct.tenSP"
                    required
                  />
                </div>
              </div>

              <div class="col-md-12">
                <label class="form-label mb-2"
                  >Danh mục *
                  <small class="text-muted">(Có thể chọn nhiều)</small>
                </label>

                <div
                  class="d-flex flex-wrap gap-2 category-pills align-items-center"
                >
                  <div v-for="cat in categories" :key="cat.maDM">
                    <input
                      type="checkbox"
                      class="btn-check"
                      :id="'cat-' + cat.maDM"
                      :value="cat.maDM"
                      v-model="newProduct.categoryIds"
                    />
                    <label
                      class="btn btn-outline-dark btn-sm rounded-pill px-3"
                      :for="'cat-' + cat.maDM"
                      >{{ cat.tenDM }}</label
                    >
                  </div>

                  <button
                    type="button"
                    class="btn btn-outline-dark btn-sm rounded-pill px-3 border-dashed"
                    data-bs-toggle="modal"
                    data-bs-target="#addCategoryModal"
                    @click="prepareAddCategory('add')"
                  >
                    <i class="bi bi-plus"></i> Thêm mới
                  </button>
                </div>
              </div>
              <br />
              <div class="mb-3">
                <label class="form-label">Giới tính *</label>
                <div class="btn-group w-100 gender-group" role="group">
                  <input
                    type="radio"
                    class="btn-check"
                    v-model="newProduct.gioiTinh"
                    :value="true"
                    id="g-male"
                    name="g"
                  />
                  <label class="btn btn-outline-dark" for="g-male">
                    <i class="bi bi-gender-male me-1"></i> Nam
                  </label>
                  <input
                    type="radio"
                    class="btn-check"
                    v-model="newProduct.gioiTinh"
                    :value="false"
                    id="g-female"
                    name="g"
                  />
                  <label class="btn btn-outline-dark" for="g-female">
                    <i class="bi bi-gender-female me-1"></i> Nữ
                  </label>
                  <input
                    type="radio"
                    class="btn-check"
                    v-model="newProduct.gioiTinh"
                    :value="null"
                    id="g-uni"
                    name="g"
                  />
                  <label class="btn btn-outline-dark" for="g-uni">
                    <i class="bi bi-gender-ambiguous me-1"></i> Unisex
                  </label>
                </div>
              </div>

              <div class="mb-3">
                <label class="form-label">Mô tả sản phẩm</label>
                <textarea
                  class="form-control"
                  rows="3"
                  v-model="newProduct.moTa"
                ></textarea>
              </div>

              <hr />
              <h6 class="mb-3 mt-4">
                <i class="bi bi-layers me-2"></i>Phân loại sản phẩm (Biến thể)
              </h6>

              <div
                v-for="(v, index) in newProduct.variants"
                :key="index"
                class="card mb-3 bg-light border-0 shadow-sm"
              >
                <div class="card-body">
                  <div
                    class="d-flex justify-content-between align-items-center mb-3"
                  >
                    <span class="badge bg-dark fs-6"
                      >Phân loại #{{ index + 1 }}</span
                    >
                    <button
                      type="button"
                      class="btn btn-outline-danger btn-sm d-flex align-items-center"
                      @click="removeVariant(index)"
                      v-if="newProduct.variants.length > 1"
                    >
                      <i class="bi bi-trash me-1"></i> Xóa
                    </button>
                  </div>

                  <div class="row g-3">
                    <div class="col-md-3">
                      <label class="form-label small">Màu sắc *</label>
                      <input
                        type="text"
                        class="form-control"
                        v-model="v.tenMau"
                        placeholder="VD: Đen, Trắng"
                        required
                      />
                    </div>
                    <div class="col-md-12 mb-3">
                      <label class="form-label small fw-bold text-dark">
                        Chọn các Size cho màu này
                      </label>
                      <div class="d-flex flex-wrap gap-2">
                        <div
                          v-for="s in sizes"
                          :key="'var-' + index + '-size-' + s.maSize"
                        >
                          <input
                            type="checkbox"
                            class="btn-check"
                            :id="'var-' + index + '-size-' + s.maSize"
                            :value="s.maSize"
                            v-model="v.selectedSizes"
                          />
                          <label
                            class="btn btn-outline-dark btn-sm rounded-pill px-3"
                            :for="'var-' + index + '-size-' + s.maSize"
                          >
                            {{ s.coGiay === 0 ? "Freesize" : s.coGiay }}
                          </label>
                        </div>
                      </div>
                    </div>
                    <div class="col-md-6">
                      <label class="form-label small">Đơn giá (VND) *</label>
                      <input
                        type="number"
                        class="form-control"
                        v-model="v.donGia"
                        required
                      />
                    </div>
                    <div class="col-md-4">
                      <label class="form-label small">Số lượng *</label>
                      <input
                        type="number"
                        class="form-control"
                        v-model="v.soLuong"
                        required
                      />
                    </div>
                    <div class="col-md-4">
                      <label class="form-label small">Trạng thái</label>
                      <select class="form-select" v-model="v.trangThai">
                        <option value="Còn hàng">Còn hàng</option>
                        <option value="Hết hàng">Hết hàng</option>
                        <option value="Sắp hết">Sắp hết</option>
                      </select>
                    </div>
                    <div class="col-md-4">
                      <label class="form-label small">Tên file ảnh</label>
                      <div class="input-group">
                        <input
                          type="text"
                          class="form-control"
                          v-model="v.hinhAnh"
                          placeholder="vd: giay1.jpg"
                        />
                        <button
                          type="button"
                          class="btn btn-outline-secondary"
                          @click="
                            openImagePicker(index, '#addProductModal', false)
                          "
                          data-bs-toggle="modal"
                          data-bs-target="#imagePickerModal"
                        >
                          <i class="bi bi-image"></i>
                        </button>
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <div class="text-center mb-3">
                <button
                  type="button"
                  class="btn btn-outline-dark border-dashed w-100 py-2"
                  @click="addVariant"
                >
                  <i class="bi bi-plus-circle me-2"></i>Thêm phân loại
                </button>
              </div>
            </div>

            <div class="modal-footer">
              <button
                type="button"
                class="btn btn-secondary"
                data-bs-dismiss="modal"
              >
                <i class="bi bi-x-circle me-2"></i>Hủy
              </button>
              <button type="submit" class="btn btn-dark">
                <i class="bi bi-check-circle me-2"></i>Lưu sản phẩm
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>

    <div
      class="modal fade"
      id="editProductModal"
      tabindex="-1"
      aria-hidden="true"
    >
      <div class="modal-dialog modal-lg">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">
              <i class="bi bi-pencil me-2"></i>Chỉnh sửa sản phẩm
            </h5>
            <button
              type="button"
              class="btn-close"
              data-bs-dismiss="modal"
            ></button>
          </div>
          <form @submit.prevent="saveEditProduct">
            <div class="modal-body">
              <div class="row mb-3">
                <div class="col-md-12">
                  <label class="form-label">Tên sản phẩm *</label>
                  <input
                    type="text"
                    class="form-control"
                    v-model="editProductData.tenSP"
                    required
                  />
                </div>
              </div>
              <div class="col-md-12">
                <label class="form-label mb-2">Danh mục *</label>

                <div
                  class="d-flex flex-wrap gap-2 category-pills align-items-center"
                >
                  <div v-for="cat in categories" :key="cat.maDM">
                    <input
                      type="checkbox"
                      class="btn-check"
                      :id="'edit-cat-' + cat.maDM"
                      :value="cat.maDM"
                      v-model="editProductData.categoryIds"
                    />
                    <label
                      class="btn btn-outline-dark btn-sm rounded-pill px-3"
                      :for="'edit-cat-' + cat.maDM"
                      >{{ cat.tenDM }}</label
                    >
                  </div>

                  <button
                    type="button"
                    class="btn btn-outline-dark btn-sm rounded-pill px-3 border-dashed"
                    data-bs-toggle="modal"
                    data-bs-target="#addCategoryModal"
                    @click="prepareAddCategory('edit')"
                  >
                    <i class="bi bi-plus"></i> Thêm mới
                  </button>
                </div>
              </div>
              <br />
              <div class="row mb-3">
                <div class="col-md-12">
                  <label class="form-label">Giới tính *</label>
                  <div class="btn-group w-100 gender-group">
                    <input
                      type="radio"
                      class="btn-check"
                      v-model="editProductData.gioiTinh"
                      :value="true"
                      id="edit-g-male"
                    />
                    <label class="btn btn-outline-dark" for="edit-g-male"
                      >Nam</label
                    >
                    <input
                      type="radio"
                      class="btn-check"
                      v-model="editProductData.gioiTinh"
                      :value="false"
                      id="edit-g-female"
                    />
                    <label class="btn btn-outline-dark" for="edit-g-female"
                      >Nữ</label
                    >
                    <input
                      type="radio"
                      class="btn-check"
                      v-model="editProductData.gioiTinh"
                      :value="null"
                      id="edit-g-uni"
                    />
                    <label class="btn btn-outline-dark" for="edit-g-uni"
                      >Unisex</label
                    >
                  </div>
                </div>
              </div>
              <div class="mb-3">
                <label class="form-label">Mô tả</label>
                <textarea
                  class="form-control"
                  rows="3"
                  v-model="editProductData.moTa"
                ></textarea>
              </div>
              <hr />
              <h6>Phân loại sản phẩm (Biến thể)</h6>
              <div
                v-for="(v, index) in editProductData.variants"
                :key="index"
                class="card mb-3 bg-light"
              >
                <div class="card-body">
                  <div class="d-flex justify-content-between align-items-center mb-3">
                    <span class="badge bg-dark fs-6"
                      >Phân loại #{{ index + 1 }}</span
                    >
                    <button
                      type="button"
                      class="btn btn-outline-danger btn-sm"
                      @click="removeEditVariant(index)"
                      v-if="editProductData.variants.length > 1"
                    ><i class="bi bi-trash me-1"></i>
                      Xóa
                    </button>
                  </div>
                  <div class="row g-2">
                    <div class="col-md-3">
                      <label class="form-label small">Màu *</label>
                      <input
                        type="text"
                        class="form-control"
                        v-model="v.tenMau"
                        required
                      />
                    </div>

                    <template v-if="!v.isNewGroup">
                      <div class="col-md-3">
                        <label class="form-label small">Size *</label>
                        <select class="form-select" v-model="v.maSize" required>
                          <option value="" disabled>Chọn Size</option>
                          <option
                            v-for="s in sizes"
                            :key="s.maSize"
                            :value="s.maSize"
                          >
                            {{
                              s.coGiay === 0 ? "Freesize (Phụ kiện)" : s.coGiay
                            }}
                          </option>
                        </select>
                      </div>
                      <div class="col-md-6">
                        <label class="form-label small">Đơn giá *</label>
                        <input
                          type="number"
                          class="form-control"
                          v-model="v.donGia"
                          required
                        />
                      </div>
                    </template>

                    <template v-else>
                      <div class="col-md-12 mb-3">
                        <label class="form-label small fw-bold text-dark">
                          Chọn các Size cho màu này
                        </label>
                        <div class="d-flex flex-wrap gap-2">
                          <div
                            v-for="s in sizes"
                            :key="'edit-var-' + index + '-size-' + s.maSize"
                          >
                            <input
                              type="checkbox"
                              class="btn-check"
                              :id="'edit-var-' + index + '-size-' + s.maSize"
                              :value="s.maSize"
                              v-model="v.selectedSizes"
                            />
                            <label
                              class="btn btn-outline-dark btn-sm rounded-pill px-3"
                              :for="'edit-var-' + index + '-size-' + s.maSize"
                            >
                              {{ s.coGiay === 0 ? "Freesize" : s.coGiay }}
                            </label>
                          </div>
                        </div>
                      </div>
                      <div class="col-md-6">
                        <label class="form-label small">Đơn giá *</label>
                        <input
                          type="number"
                          class="form-control"
                          v-model="v.donGia"
                          required
                        />
                      </div>
                    </template>

                    <div class="col-md-4">
                      <label class="form-label small">Số lượng *</label>
                      <input
                        type="number"
                        class="form-control"
                        v-model="v.soLuong"
                        @input="autoUpdateStatus(v)"
                        required
                      />
                    </div>
                    <div class="col-md-4">
                      <label class="form-label small">Trạng thái</label>
                      <select class="form-select" v-model="v.trangThai">
                        <option value="Còn hàng">Còn hàng</option>
                        <option value="Hết hàng">Hết hàng</option>
                        <option value="Sắp hết">Sắp hết</option>
                      </select>
                    </div>
                    <div class="col-md-4">
                      <label class="form-label small">Hình ảnh</label>
                      <div class="input-group">
                        <input
                          type="text"
                          class="form-control"
                          v-model="v.hinhAnh"
                        />
                        <button
                          type="button"
                          class="btn btn-outline-secondary"
                          @click="
                            openImagePicker(index, '#editProductModal', true)
                          "
                          data-bs-toggle="modal"
                          data-bs-target="#imagePickerModal"
                        >
                          <i class="bi bi-image"></i>
                        </button>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
              <button
                type="button"
                class="btn btn-outline-dark w-100 border-dashed"
                @click="addEditVariant"
              >
                + Thêm phân loại
              </button>
            </div>
            <div class="modal-footer">
              <button
                type="button"
                class="btn btn-secondary"
                data-bs-dismiss="modal"
              >
                Hủy
              </button>
              <button type="submit" class="btn btn-dark">Lưu thay đổi</button>
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
              <i class="bi bi-list-check me-2"></i>Phân loại:
              <span class="text-dark">{{ selectedProductName }}</span>
            </h5>
            <button
              type="button"
              class="btn-close"
              data-bs-dismiss="modal"
            ></button>
          </div>
          <div class="modal-body">
            <div class="table-responsive">
              <table class="table table-hover align-middle">
                <thead class="table-light">
                  <tr>
                    <th>#</th>
                    <th>Hình ảnh</th>
                    <th>Phân loại (Màu - Size)</th>
                    <th>Số lượng kho</th>
                    <th>Đơn giá</th>
                    <th>Trạng thái</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-if="selectedVariants.length === 0">
                    <td colspan="6" class="text-center py-4 text-muted">
                      <div
                        class="spinner-border spinner-border-sm me-2"
                        role="status"
                        v-if="selectedProductName"
                      ></div>
                      {{
                        selectedProductName
                          ? "Đang tải dữ liệu..."
                          : "Chưa có dữ liệu phân loại"
                      }}
                    </td>
                  </tr>

                  <tr
                    v-for="(v, index) in selectedVariants"
                    :key="index"
                    v-else
                  >
                    <td>{{ index + 1 }}</td>
                    <td>
                      <img
                        :src="
                          v.hinhAnh
                            ? `http://localhost:8080/images/${v.hinhAnh}`
                            : 'https://placehold.co/50x50'
                        "
                        alt="Variant Image"
                        class="rounded border"
                        style="width: 50px; height: 50px; object-fit: cover"
                      />
                    </td>
                    <td>
                      <div class="fw-bold">{{ v.tenMau }}</div>
                      <div class="small text-muted">
                        Size: {{ getSizeName(v.maSize) }}
                      </div>
                    </td>
                    <td>
                      <span :class="{ 'text-danger fw-bold': v.soLuong === 0 }">
                        {{ v.soLuong }}
                      </span>
                    </td>
                    <td class="fw-bold text-dark">
                      {{ v.donGia?.toLocaleString("vi-VN") }} ₫
                    </td>
                    <td>
                      <span
                        :class="[
                          'badge',
                          v.trangThai === 'Còn hàng'
                            ? 'bg-success'
                            : v.trangThai === 'Sắp hết'
                            ? 'bg-warning text-dark'
                            : 'bg-danger',
                        ]"
                      >
                        {{ v.trangThai }}
                      </span>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
          <div class="modal-footer text-end">
            <button
              type="button"
              class="btn btn-secondary"
              data-bs-dismiss="modal"
            >
              Đóng
            </button>
          </div>
        </div>
      </div>
    </div>

    <div
      class="modal fade"
      id="imagePickerModal"
      tabindex="-1"
      aria-hidden="true"
    >
      <div class="modal-dialog modal-xl">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">
              <i class="bi bi-image me-2"></i>Chọn hình ảnh
            </h5>
            <button
              type="button"
              class="btn-close"
              data-bs-dismiss="modal"
            ></button>
          </div>
          <div class="modal-body">
            <div class="mb-4">
              <label
                for="uploadFileInput"
                class="image-upload-area w-100 d-block"
              >
                <i class="bi bi-cloud-upload display-4 mb-3"></i>
                <p class="mb-2">
                  <strong>Nhấp để tải ảnh mới lên</strong>
                </p>
                <p class="text-muted small mb-0">
                  Hỗ trợ: JPG, PNG, WEBP (Tối đa 5MB)
                </p>
                <input
                  type="file"
                  id="uploadFileInput"
                  accept="image/*"
                  class="d-none"
                  @change="handleFileUpload"
                />
              </label>
            </div>

            <h6 class="mb-3">Hình ảnh có sẵn trên hệ thống</h6>
            <div class="row g-3" style="max-height: 400px; overflow-y: auto">
              <div
                class="col-6 col-sm-4 col-md-3 col-lg-2"
                v-for="img in availableImages"
                :key="img"
              >
                <div
                  class="image-thumbnail"
                  :class="{ selected: selectedImageName === img }"
                  @click="selectImage(img)"
                >
                  <img
                    :src="`http://localhost:8080/images/${img}`"
                    :alt="img"
                  />
                  <div class="image-name" :title="img">{{ img }}</div>
                </div>
              </div>
              <div
                v-if="availableImages.length === 0"
                class="text-center text-muted w-100 py-3"
              >
                Chưa có hình ảnh nào.
              </div>
            </div>
          </div>
          <div class="modal-footer">
            <button
              type="button"
              class="btn btn-secondary"
              data-bs-toggle="modal"
              :data-bs-target="parentModalSelector"
            >
              Hủy
            </button>
            <button
              type="button"
              class="btn btn-primary"
              @click="confirmImageSelection"
              data-bs-toggle="modal"
              :data-bs-target="parentModalSelector"
              :disabled="!selectedImageName"
            >
              Chọn ảnh này
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from "vue";
import axios from "axios";
import NV_Sidebar from "@/components/Shared/NV_Sidebar.vue";

axios.defaults.withCredentials = true;

// Biến lưu nội dung thông báo thành công
const successMessage = ref("");

const products = ref([]);
const categories = ref([]);
const sizes = ref([]);

// Các biến state cho bộ lọc
const filterKeyword = ref("");
const filterCategory = ref("");
const filterGender = ref("");
const filterStatus = ref("");

// Thêm hàm này vào phần <script setup> của bạn
const autoUpdateStatus = (variant) => {
  // Ép kiểu về số nguyên để kiểm tra cho chắc chắn
  const quantity = parseInt(variant.soLuong) || 0;

  if (quantity <= 0) {
    variant.soLuong = 0; // Tránh trường hợp người dùng nhập số âm
    variant.trangThai = "Hết hàng";
  } else if (quantity > 0 && variant.trangThai === "Hết hàng") {
    // Nếu có nhập số lượng nhưng trạng thái đang kẹt ở 'Hết hàng' thì tự động đổi
    variant.trangThai = "Còn hàng";
  }
};

// --- LOGIC PHÂN TRANG ---
const currentPage = ref(1);
const itemsPerPage = 10;

// Tính tổng số trang dựa trên danh sách đã lọc
const totalPages = computed(() => {
  return Math.ceil(filteredProducts.value.length / itemsPerPage);
});

// Lấy ra đúng 10 sản phẩm cho trang hiện tại
const paginatedProducts = computed(() => {
  const start = (currentPage.value - 1) * itemsPerPage;
  const end = start + itemsPerPage;
  return filteredProducts.value.slice(start, end);
});

// Hàm chuyển trang
const goToPage = (page) => {
  if (page >= 1 && page <= totalPages.value) {
    currentPage.value = page;
  }
};

// Tự động quay về trang 1 nếu người dùng thay đổi bộ lọc
watch([filterKeyword, filterCategory, filterGender, filterStatus], () => {
  currentPage.value = 1;
});

// 1. Gọi API Danh mục
const fetchCategories = async () => {
  try {
    const response = await axios.get(
      "http://localhost:8080/api/sanpham/danhmuc"
    );
    categories.value = response.data;
  } catch (error) {
    console.error("Lỗi lấy danh mục:", error);
  }
};
//Size
const fetchSizes = async () => {
  try {
    const response = await axios.get("http://localhost:8080/api/sanpham/sizes");
    sizes.value = response.data;
  } catch (error) {
    console.error("Lỗi lấy sizes:", error);
  }
};

// 2. Gọi API Sản phẩm
const fetchProducts = async () => {
  try {
    const response = await axios.get("http://localhost:8080/api/sanpham/list");
    products.value = response.data;
  } catch (error) {
    console.error("Lỗi lấy sản phẩm:", error);
  }
};

// 3. Computed LỌC REAL-TIME TỔNG HỢP VÀ SẮP XẾP
const filteredProducts = computed(() => {
  // Bước 1: Lọc dữ liệu như bình thường
  let result = products.value.filter((item) => {
    const matchKeyword =
      !filterKeyword.value ||
      item.tenSP.toLowerCase().includes(filterKeyword.value.toLowerCase());

    const matchCategory =
      !filterCategory.value ||
      (item.danhMucs && item.danhMucs.includes(filterCategory.value));

    let matchGender = true;
    if (filterGender.value === "Nam") matchGender = item.gioiTinh === true;
    else if (filterGender.value === "Nữ") matchGender = item.gioiTinh === false;
    else if (filterGender.value === "Unisex")
      matchGender = item.gioiTinh === null;

    let matchStatus = true;
    if (filterStatus.value === "Còn hàng")
      matchStatus = item.trangThai === "Còn hàng";
    else if (filterStatus.value === "Hết hàng")
      matchStatus = item.trangThai === "Hết hàng";
    else if (filterStatus.value === "Sắp hết")
      matchStatus = item.trangThai === "Sắp hết";
    else if (filterStatus.value === "Đang hoạt động")
      matchStatus = item.isActive === true;
    else if (filterStatus.value === "Đã ẩn")
      matchStatus = item.isActive === false;

    return matchKeyword && matchCategory && matchGender && matchStatus;
  });

  // Bước 2: Sắp xếp ưu tiên sản phẩm isActive = true lên đầu
  return result.sort((a, b) => {
    // Nếu cả 2 sản phẩm cùng trạng thái (cùng hiện hoặc cùng ẩn) thì giữ nguyên thứ tự
    if (a.isActive === b.isActive) return 0;

    // Nếu sản phẩm 'a' đang hoạt động thì đưa lên trước (-1), ngược lại đẩy xuống sau (1)
    return a.isActive ? -1 : 1;
  });
});

// 4. Hàm làm mới bộ lọc
const resetFilters = () => {
  filterKeyword.value = "";
  filterCategory.value = "";
  filterGender.value = "";
  filterStatus.value = "";
};

// 5. Thống kê số liệu (Tính trên danh sách TỔNG)
const totalProducts = computed(() => products.value.length);
const activeProducts = computed(
  () => products.value.filter((p) => p.isActive).length
);
const outOfStock = computed(
  () => products.value.filter((p) => p.trangThai === "Hết hàng").length
);
const lowStock = computed(
  () => products.value.filter((p) => p.trangThai === "Sắp hết").length
);

// Biến dùng cho Modal Thêm Danh Mục
const newCategoryName = ref("");
const categoryMode = ref(""); // Lưu trạng thái đang mở ở modal 'add' hay 'edit'

// Chuẩn bị mở modal
const prepareAddCategory = (mode) => {
  categoryMode.value = mode;
  newCategoryName.value = ""; // Xóa trắng ô text mỗi lần mở
};

// Hàm lưu danh mục khi bấm nút trong Modal
const saveNewCategory = async () => {
  if (!newCategoryName.value || newCategoryName.value.trim() === "") {
    showToast("Vui lòng nhập tên danh mục!","warning");
    return;
  }

  try {
    const response = await axios.post(
      "http://localhost:8080/api/sanpham/danhmuc/create",
      {
        tenDM: newCategoryName.value.trim(),
      }
    );

    if (response.data.success) {
      // 1. Load lại danh sách
      await fetchCategories();

      // 2. Tự động tick chọn danh mục vừa tạo
      const newCatId = response.data.data.maDM;
      if (categoryMode.value === "edit") {
        editProductData.value.categoryIds.push(newCatId);
      } else {
        newProduct.value.categoryIds.push(newCatId);
      }

      // 3. Tự động đóng modal Thêm danh mục bằng JS
      document.getElementById("closeCategoryModalBtn").click();
    }
  } catch (error) {
    console.error("Lỗi thêm danh mục:", error);
    showToast(error.response?.data?.message || "Có lỗi xảy ra khi thêm danh mục!");
  }
};

// Khởi tạo Object cho sản phẩm mới
const newProduct = ref({
  tenSP: "",
  moTa: "",
  gioiTinh: null, // Mặc định Unisex
  categoryIds: [],
  variants: [
    {
      tenMau: "",
      selectedSizes: [],
      donGia: 0,
      soLuong: 0,
      hinhAnh: "",
      trangThai: "Còn hàng",
    },
  ],
});

// Thêm một dòng phân loại mới
const addVariant = () => {
  newProduct.value.variants.push({
    tenMau: "",
    selectedSizes: [],
    donGia: 0,
    soLuong: 0,
    hinhAnh: "",
    trangThai: "Còn hàng",
  });
};

// Xóa một dòng phân loại
const removeVariant = (index) => {
  if (newProduct.value.variants.length > 1) {
    newProduct.value.variants.splice(index, 1);
  }
};

// Trạng thái của thông báo (Toast)
const toast = ref({
  id: 0, // Thêm ID để ép reset thanh tiến trình
  show: false,
  message: "",
  type: "success",
});

let toastTimeout = null;

// Hàm gọi thông báo dùng chung
const showToast = (message, type = "success") => {
  // Gán Date.now() làm ID giúp mỗi lần bật là một animation mới hoàn toàn
  toast.value = { id: Date.now(), show: true, message, type };
  
  if (toastTimeout) clearTimeout(toastTimeout);
  
  // Tự động tắt sau đúng 3 giây
  toastTimeout = setTimeout(() => {
    toast.value.show = false;
  }, 5000);
};

// --- LOGIC CHO MODAL CHỌN ẢNH VÀ EDIT ---
const availableImages = ref([]);
const currentVariantIndex = ref(-1);
const selectedImageName = ref("");
const parentModalSelector = ref("#addProductModal"); // Ghi nhớ modal gốc
const isEditMode = ref(false); // Kiểm tra đang ở form Thêm hay Sửa

const fetchImages = async () => {
  try {
    const res = await axios.get("http://localhost:8080/api/sanpham/images");
    availableImages.value = res.data;
  } catch (error) {
    console.error(error);
  }
};

// Cập nhật hàm mở Modal ảnh
const openImagePicker = (index, parentModal, isEdit) => {
  currentVariantIndex.value = index;
  parentModalSelector.value = parentModal;
  isEditMode.value = isEdit;

  let img = isEdit
    ? editProductData.value.variants[index].hinhAnh
    : newProduct.value.variants[index].hinhAnh;

  selectedImageName.value = img || "";
  fetchImages();
};
const selectImage = (imgName) => {
  selectedImageName.value = imgName;
};

const confirmImageSelection = () => {
  if (currentVariantIndex.value >= 0) {
    if (isEditMode.value) {
      editProductData.value.variants[currentVariantIndex.value].hinhAnh =
        selectedImageName.value;
    } else {
      newProduct.value.variants[currentVariantIndex.value].hinhAnh =
        selectedImageName.value;
    }
  }
};

// Hàm upload ảnh mới
const handleFileUpload = async (event) => {
  const file = event.target.files[0];
  if (!file) return;

  const formData = new FormData();
  formData.append("file", file);

  try {
    const res = await axios.post(
      "http://localhost:8080/api/sanpham/upload",
      formData,
      {
        headers: { "Content-Type": "multipart/form-data" },
      }
    );
    if (res.data.success) {
      showToast("Tải ảnh lên thành công!");
      await fetchImages(); // Tải lại danh sách ảnh
      selectedImageName.value = res.data.fileName; // Chọn luôn ảnh vừa tải
    }
  } catch (error) {
    showToast("Lỗi tải ảnh lên!","danger");
    console.error(error);
  }
};

// Khởi tạo Object cho sản phẩm Đang Sửa
const editProductData = ref({
  maSP: null,
  tenSP: "",
  moTa: "",
  gioiTinh: null,
  khuyenMai: 0,
  categoryIds: [],
  variants: [],
});

// Khi ấn nút Cây Bút, load data vào Modal
const openEditModal = async (maSP) => {
  try {
    const res = await axios.get(`http://localhost:8080/api/sanpham/${maSP}`);
    editProductData.value = res.data;
  } catch (error) {
    showToast("Lỗi tải thông tin sản phẩm!","danger");
  }
};

// Lưu thông tin chỉnh sửa
const saveEditProduct = async () => {
  try {
    const payload = { ...editProductData.value };
    const formattedVariants = [];

    // Duyệt qua danh sách phân loại
    for (const v of payload.variants) {
      if (v.isNewGroup) {
        // Tách cụm size mới thêm thành các phân loại lẻ
        if (!v.selectedSizes || v.selectedSizes.length === 0) {
          showToast(
            `Vui lòng chọn ít nhất 1 size cho màu "${
              v.tenMau || "chưa nhập tên"
            }"`,"warning"
          );
          return;
        }
        v.selectedSizes.forEach((sizeId) => {
          formattedVariants.push({
            tenMau: v.tenMau,
            maSize: sizeId,
            donGia: v.donGia,
            soLuong: v.soLuong,
            hinhAnh: v.hinhAnh,
            trangThai: v.soLuong <= 0 ? "Hết hàng" : "Còn hàng",
          });
        });
      } else {
        // Giữ nguyên các phân loại cũ đã có sẵn
        formattedVariants.push(v);
      }
    }

    payload.variants = formattedVariants;

    await axios.put("http://localhost:8080/api/sanpham/update", payload);
    showToast("Cập nhật sản phẩm thành công!");
    location.reload();
  } catch (error) {
    console.error(error);
    showToast("Lỗi cập nhật sản phẩm!","danger");
  }
};

const addEditVariant = () => {
  editProductData.value.variants.push({
    isNewGroup: true,
    tenMau: "",
    selectedSizes: [],
    donGia: 0,
    soLuong: 0,
    hinhAnh: "",
    trangThai: "Còn hàng",
  });
};
const removeEditVariant = (index) => {
  if (editProductData.value.variants.length > 1) {
    const variant = editProductData.value.variants[index];
    
    if (variant.isNewGroup) {
      // Nếu là dòng mới bấm "+ Thêm phân loại" (chưa có trong DB) -> Xóa hẳn cho đỡ rác form
      editProductData.value.variants.splice(index, 1);
    } else {
      // Nếu là phân loại cũ từ DB -> Giữ nguyên trên UI, ép số lượng về 0 và Hết hàng
      variant.soLuong = 0;
      variant.trangThai = "Hết hàng";
      
      // Thêm một thông báo nhỏ để nhân viên biết họ vừa thao tác gì
      showToast(`Đã chuyển phân loại "${variant.tenMau || 'này'}" về 0 - Hết hàng!`);
    }
  } else {
    showToast("Sản phẩm phải có ít nhất 1 phân loại!","warning");
  }
};

// Biến lưu trữ dữ liệu cho Modal Xem phân loại
const selectedVariants = ref([]);
const selectedProductName = ref("");

// Hàm lấy tên Size từ maSize (Để hiển thị "Size 40" thay vì "ID 5")
const getSizeName = (maSize) => {
  if (!maSize) return "Chưa cập nhật";
  const sizeObj = sizes.value.find((s) => s.maSize === maSize);
  if (!sizeObj) return maSize;
  return sizeObj.coGiay === 0 ? "Freesize (Phụ kiện)" : sizeObj.coGiay;
};

// Hàm gọi API lấy phân loại khi bấm nút "Xem phân loại"
const viewVariants = async (item) => {
  selectedProductName.value = item.tenSP;
  selectedVariants.value = []; // Reset dữ liệu cũ trong lúc chờ tải

  try {
    // Tận dụng API getProductDetail đã viết sẵn cho chức năng Edit
    const res = await axios.get(
      `http://localhost:8080/api/sanpham/${item.maSP}`
    );
    selectedVariants.value = res.data.variants;
  } catch (error) {
    console.error("Lỗi lấy phân loại:", error);
    showToast("Không thể tải danh sách phân loại!");
  }
};

// 6. Hàm đổi trạng thái Ẩn/Hiện sản phẩm (Xóa mềm)
const toggleProductStatus = async (maSP, currentStatus) => {
  const actionText = currentStatus ? "ẩn" : "hiện";

  // Hỏi xác nhận trước khi làm
  if (!confirm(`Bạn có chắc chắn muốn ${actionText} sản phẩm này không?`)) {
    return;
  }

  try {
    const res = await axios.put(
      `http://localhost:8080/api/sanpham/toggle-status/${maSP}`
    );

    if (res.data.success) {
      // Tìm và cập nhật lại trạng thái ngay trên mảng products để UI tự render lại
      // Không cần reload cả trang web (location.reload())
      const productIndex = products.value.findIndex((p) => p.maSP === maSP);
      if (productIndex !== -1) {
        products.value[productIndex].isActive = res.data.isActive;
      }
    }
  } catch (error) {
    console.error(error);
    showToast(`Lỗi khi ${actionText} sản phẩm!`);
  }
};

// Hàm lưu sản phẩm thêm mới
const saveProduct = async () => {
  try {
    // 1. Tạo bản sao của dữ liệu form
    const payload = { ...newProduct.value };
    const formattedVariants = [];

    // 2. Duyệt qua từng nhóm Màu và tách ra thành các phân loại lẻ
    for (const group of payload.variants) {
      // Bắt lỗi nếu người dùng quên chọn size
      if (!group.selectedSizes || group.selectedSizes.length === 0) {
        showToast(
          `Vui lòng chọn ít nhất 1 size cho màu "${
            group.tenMau || "chưa nhập tên"
          }"`,"warning"
        );
        return;
      }

      // Tách mỗi size được chọn thành một object variant riêng
      group.selectedSizes.forEach((sizeId) => {
        formattedVariants.push({
          tenMau: group.tenMau,
          maSize: sizeId,
          donGia: group.donGia,
          soLuong: group.soLuong,
          hinhAnh: group.hinhAnh,
          // Tích hợp luôn logic tự động set trạng thái Hết hàng ở đây
          trangThai: group.soLuong <= 0 ? "Hết hàng" : "Còn hàng",
        });
      });
    }

    // 3. Ghi đè mảng variants đã được làm phẳng vào payload
    payload.variants = formattedVariants;

    // 4. Gửi payload đã xử lý xuống Backend
    const res = await axios.post(
      "http://localhost:8080/api/sanpham/create",
      payload
    );

    showToast("Thêm sản phẩm thành công!");
    location.reload();
  } catch (error) {
    console.error(error);
    showToast("Lỗi khi lưu sản phẩm! Vui lòng kiểm tra lại console.");
  }
};

onMounted(() => {
  fetchCategories();
  fetchProducts();
  fetchSizes();
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

.bg-gradient-1,
.bg-gradient-2,
.bg-gradient-3,
.bg-gradient-4 {
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
  position: relative;
}

.status-badge-corner {
  position: absolute;
  top: 15px;
  right: 15px;
  font-size: 12px;
  padding: 6px 12px;
  border-radius: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  z-index: 2;
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
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.2);
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

/* Style cho sản phẩm bị ẩn */
.product-card.product-hidden {
  background-color: #f8f9fa; /* Thêm chút nền xám để dễ phân biệt hơn */
  border-color: #eeeeee;
}

/* Chỉ làm mờ hình ảnh (col-md-1), thông tin (col-md-8) và badge trạng thái */
.product-card.product-hidden .col-md-1,
.product-card.product-hidden .col-md-8,
.product-card.product-hidden .status-badge-corner {
  opacity: 1;
  pointer-events: none; /* Tùy chọn: chặn không cho click bôi đen text ở phần đã mờ */
}

/* --- TOAST & PROGRESS BAR CUSTOME CSS --- */
.toast {
  position: relative;
}

/* Thanh tiến trình chạy ở dưới đáy Toast */
.toast-progress-bar {
  height: 4px;
  background-color: rgba(255, 255, 255, 0.7);
  width: 100%;
  position: absolute;
  bottom: 0;
  left: 0;
  border-bottom-left-radius: var(--bs-toast-border-radius);
  /* Trùng khớp với 3s ở setTimeout */
  animation: shrinkProgress 3s linear forwards; 
}

@keyframes shrinkProgress {
  from { width: 100%; }
  to { width: 0%; }
}

/* Hiệu ứng trượt từ trên xuống cho vị trí Center-Top */
.toast-fade-enter-active,
.toast-fade-leave-active {
  transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}
.toast-fade-enter-from {
  opacity: 0;
  transform: translateY(-50px);
}
.toast-fade-leave-to {
  opacity: 0;
  transform: translateY(-50px);
}

/* Phần cột chứa cụm nút bấm (.action-buttons) hoàn toàn không bị CSS này đụng tới 
   nên nó sẽ giữ nguyên độ rõ nét và có thể click bình thường */

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