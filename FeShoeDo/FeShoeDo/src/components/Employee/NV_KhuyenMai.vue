<template>
  <!-- Toast -->
  <div
    class="toast-container position-fixed top-0 start-50 translate-middle-x p-3 mt-2"
    style="z-index: 1090"
  >
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
                'bi-info-circle-fill': toast.type === 'info',
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
<NV_Sidebar @toggle-collapse="handleSidebarCollapse" />
<main class="main-content" :class="{ 'expanded': isSidebarCollapsed }">
      <div class="page-container">
        <!-- Header -->
        <div class="d-flex align-items-center mb-4 pb-2 border-bottom">
          <h4 class="mb-0 text-dark">
            <i class="bi bi-tags-fill me-2"></i>Quản lý Khuyến Mãi (Flash Sale)
          </h4>
        </div>

        <!-- Tabs -->
        <ul class="nav nav-tabs mb-4" id="kmTabs">
          <li class="nav-item">
            <button
              class="nav-link fw-semibold"
              :class="{ active: activeTab === 'campaign' }"
              @click="activeTab = 'campaign'"
            >
              <i class="bi bi-megaphone-fill me-1 text-dark"></i> Chiến Dịch
              Khuyến Mãi
            </button>
          </li>
          <li class="nav-item">
            <button
              class="nav-link fw-semibold"
              :class="{ active: activeTab === 'history' }"
              @click="
                activeTab = 'history';
                fetchCampaigns();
              "
            >
              <i class="bi bi-clock-history me-1 text-secondary"></i> Lịch Sử
              Chiến Dịch
            </button>
          </li>
                    <li class="nav-item">
            <button
              class="nav-link fw-semibold"
              :class="{ active: activeTab === 'flashsale' }"
              @click="activeTab = 'flashsale'"
            >
              <i class="bi bi-lightning-fill me-1 text-warning"></i> Khuyến Mãi
            </button>
          </li>
        </ul>

        <!-- ===================== TAB FLASH SALE ===================== -->
        <div
          v-if="activeTab === 'flashsale'"
          class="bg-white p-4 rounded-3 shadow-sm border"
        >
          <!-- Bộ lọc -->
          <div class="row g-2 mb-4">
            <div class="col-md-3">
              <input
                type="text"
                v-model="filterKeyword"
                placeholder="Tìm theo tên sản phẩm..."
                class="form-control"
              />
            </div>
            <div class="col-md-2">
              <select v-model="filterCategory" class="form-select">
                <option value="">Tất cả danh mục</option>
                <option
                  v-for="cat in categories"
                  :key="cat.maDM"
                  :value="cat.maDM"
                >
                  {{ cat.tenDM }}
                </option>
              </select>
            </div>
            <div class="col-md-2">
              <select v-model="filterFlashSale" class="form-select">
                <option value="">Tất cả KM</option>
                <option value="DangSale">🔥 Đang Flash Sale (> 0%)</option>
                <option value="KhongSale">Không giảm giá (0%)</option>
              </select>
            </div>
            <div class="col-md-3">
              <select v-model="filterActive" class="form-select">
                <option value="">Tất cả trạng thái</option>
                <option value="true">Đang bán</option>
                <option value="false">Đã ẩn</option>
              </select>
            </div>
            <div class="col-md-2">
              <button @click="resetFilters" class="btn btn-secondary w-100">
                <i class="bi bi-arrow-clockwise me-1"></i>Đặt lại
              </button>
            </div>
          </div>

          <!-- Bulk actions -->
          <div
            class="row g-2 mb-3 align-items-center bg-light p-3 rounded border"
          >
            <div class="col-md-5">
              <div class="input-group">
                <span class="input-group-text bg-dark text-white fw-bold">
                  <i class="bi bi-percent me-1"></i> Mức KM chung
                </span>
                <input
                  type="number"
                  v-model="bulkDiscount"
                  min="0"
                  max="100"
                  class="form-control form-control-lg"
                  placeholder="Nhập % (0-100)"
                />
                <button @click="applyBulkDiscount" class="btn btn-dark fw-bold">
                  <i class="bi bi-check-lg me-2"></i>Áp dụng
                </button>
              </div>
            </div>
            <div class="col-md-7">
              <div class="d-flex justify-content-end align-items-center gap-2">
                <span class="text-muted me-3">
                  Đã chọn:
                  <strong class="text-danger">{{ selectedCount }}</strong> /
                  {{ filteredProducts.length }} sản phẩm
                </span>
                <button
                  @click="unselectAll"
                  class="btn btn-outline-secondary"
                  :disabled="selectedCount === 0"
                >
                  <i class="bi bi-x-circle me-1"></i>Bỏ chọn
                </button>
                <button
                  @click="handleBulkSave"
                  class="btn btn-danger fw-bold"
                  :disabled="selectedCount === 0"
                >
                  <i class="bi bi-lightning-fill me-1"></i> Lưu KM hàng loạt
                </button>
              </div>
            </div>
          </div>

          <!-- Table sản phẩm -->
          <div class="table-responsive">
            <table class="table table-bordered table-hover align-middle">
              <thead class="table-dark">
                <tr>
                  <th style="width: 40px" class="text-center">
                    <div class="form-check d-flex justify-content-center">
                      <input
                        class="form-check-input"
                        type="checkbox"
                        v-model="isAllSelected"
                      />
                    </div>
                  </th>
                  <th class="text-center" style="width: 80px">Hình</th>
                  <th class="text-center">Sản Phẩm</th>
                  <th class="text-center">Trạng Thái Flash Sale</th>
                  <th class="text-center">Giá Hiện Tại</th>
                  <th class="text-center" style="width: 200px">
                    Thiết Lập KM Mới (%)
                  </th>
                  <th class="text-center bg-dark">Preview Giá Mới</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="item in filteredProducts" :key="item.maSP">
                  <td class="text-center">
                    <div class="form-check d-flex justify-content-center">
                      <input
                        class="form-check-input"
                        type="checkbox"
                        v-model="item.selected"
                      />
                    </div>
                  </td>
                  <td>
                    <img
                      :src="
                        item.hinhAnh
                          ? `/images/${item.hinhAnh}`
                          : 'https://placehold.co/80x80'
                      "
                      class="img-thumbnail"
                      style="width: 60px; height: 60px; object-fit: cover"
                    />
                  </td>
                  <td>
                    <div>
                      <strong>{{ item.tenSP }}</strong
                      ><br />
                      <span
                        v-if="item.isActive === false || item.active === false"
                        class="badge bg-secondary"
                        style="font-size: 0.7rem"
                        ><i class="bi bi-eye-slash me-1"></i>Đã ẩn</span
                      >
                      <span
                        v-else
                        class="badge border border-success text-success"
                        style="font-size: 0.7rem"
                        >Đang bán</span
                      >
                    </div>
                    <div class="text-muted small">
                      Đã bán: {{ item.daBan }} | Mã SP: {{ item.maSP }}
                    </div>
                  </td>
                  <td class="text-center">
                    <span
                      v-if="item.khuyenMai > 0"
                      class="badge bg-danger px-3 py-2"
                      >🔥 Đang Sale ({{ item.khuyenMai }}%)</span
                    >
                    <span v-else class="badge bg-secondary px-3 py-2"
                      >Không Sale</span
                    >
                  </td>
                  <td class="text-end">
                    <div v-if="item.khuyenMai > 0">
                      <span
                        class="text-muted text-decoration-line-through small d-block"
                        >{{
                          displayPriceRange(item.donGiaMin, item.donGiaMax)
                        }}</span
                      >
                      <strong class="text-danger">{{
                        displayDiscountedPriceRange(
                          item.donGiaMin,
                          item.donGiaMax,
                          item.khuyenMai
                        )
                      }}</strong>
                    </div>
                    <div v-else>
                      <strong>{{
                        displayPriceRange(item.donGiaMin, item.donGiaMax)
                      }}</strong>
                    </div>
                  </td>
                  <td>
                    <div class="input-group">
                      <input
                        type="number"
                        v-model="item.khuyenMaiMoi"
                        class="form-control text-center fw-bold text-dark"
                        min="0"
                        max="100"
                        placeholder="%"
                        @input="item.khuyenMaiMoi > 0 ? item.selected = true : item.selected = false"
                      />
                      <button
                        @click="handleSingleSave(item)"
                        class="btn btn-outline-dark"
                      >
                        <i class="bi bi-save me-1"></i>Lưu
                      </button>
                    </div>
                  </td>
                  <td class="text-end bg-light">
                    <div v-if="item.khuyenMaiMoi > 0">
                      <span
                        class="text-muted text-decoration-line-through small d-block"
                        >{{
                          displayPriceRange(item.donGiaMin, item.donGiaMax)
                        }}</span
                      >
                      <span class="fs-5 fw-bold text-success">{{
                        displayDiscountedPriceRange(
                          item.donGiaMin,
                          item.donGiaMax,
                          item.khuyenMaiMoi
                        )
                      }}</span>
                    </div>
                    <div v-else class="fs-6 fw-bold text-dark mt-2">
                      {{ displayPriceRange(item.donGiaMin, item.donGiaMax) }}
                    </div>
                  </td>
                </tr>
                <tr v-if="filteredProducts.length === 0">
                  <td colspan="7" class="text-center py-5 text-muted">
                    <i class="bi bi-inbox fs-1 d-block mb-2"></i>Không tìm thấy
                    sản phẩm nào phù hợp với bộ lọc.
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <!-- ===================== TAB TẠO CHIẾN DỊCH ===================== -->
        <div
          v-if="activeTab === 'campaign'"
          class="bg-white p-4 rounded-3 shadow-sm border"
        >
          <!-- Thông tin chiến dịch -->
          <div class="row g-3 mb-4 p-3 bg-light rounded border">
            <div
              class="col-12 d-flex justify-content-between align-items-center mb-3"
            >
              <h6 class="fw-bold text-dark mb-3">
                <i class="bi bi-megaphone-fill me-2"></i>Thông Tin Chiến Dịch
              </h6>
              <button
                @click="handleStartCampaign"
                class="btn btn-dark btn-lg fw-bold px-5"
              >
                <i class="bi bi-play-fill me-2"></i>Khởi Tạo Chiến Dịch
              </button>
            </div>
            <div class="col-md-4">
              <label class="form-label fw-semibold"
                >Tên chiến dịch <span class="text-danger">*</span></label
              >
              <input
                type="text"
                v-model="campaign.tenChienDich"
                class="form-control"
                placeholder="Vd: Sale hè 2025..."
              />
            </div>
            <div class="col-md-4">
              <label class="form-label fw-semibold"
                >Thời gian bắt đầu <span class="text-danger">*</span></label
              >
              <input
                type="datetime-local"
                v-model="campaign.thoiGianBatDau"
                class="form-control"
              />
            </div>
            <div class="col-md-4">
              <label class="form-label fw-semibold"
                >Thời gian kết thúc <span class="text-danger">*</span></label
              >
              <input
                type="datetime-local"
                v-model="campaign.thoiGianKetThuc"
                class="form-control"
              />
            </div>
          </div>

          <!-- Bộ lọc sản phẩm chiến dịch -->
          <div class="row g-2 mb-3">
            <div class="col-md-4">
              <input
                type="text"
                v-model="campaignFilterKeyword"
                placeholder="Tìm theo tên sản phẩm..."
                class="form-control"
              />
            </div>
            <div class="col-md-3">
              <select v-model="campaignFilterCategory" class="form-select">
                <option value="">Tất cả danh mục</option>
                <option
                  v-for="cat in categories"
                  :key="cat.maDM"
                  :value="cat.maDM"
                >
                  {{ cat.tenDM }}
                </option>
              </select>
            </div>
            <div class="col-md-3">
              <select v-model="campaignFilterActive" class="form-select">
                <option value="">Tất cả trạng thái</option>
                <option value="true">Đang bán</option>
                <option value="false">Đã ẩn</option>
              </select>
            </div>
            <div class="col-md-2">
              <button
                @click="resetCampaignFilters"
                class="btn btn-secondary w-100"
              >
                <i class="bi bi-arrow-clockwise me-1"></i>Đặt lại
              </button>
            </div>
          </div>

          <!-- Áp dụng % chung cho chiến dịch -->
          <div
            class="row g-2 mb-3 align-items-center bg-light p-3 rounded border"
          >
            <div class="col-md-5">
              <div class="input-group">
                <span class="input-group-text bg-dark text-white fw-bold">
                  <i class="bi bi-percent me-1"></i> KM chung chiến dịch
                </span>
                <input
                  type="number"
                  v-model="campaignBulkDiscount"
                  min="0"
                  max="100"
                  class="form-control form-control-lg"
                  placeholder="Nhập % (0-100)"
                />
                <button
                  @click="applyCampaignBulkDiscount"
                  class="btn btn-dark fw-bold"
                >
                  <i class="bi bi-check-lg me-2"></i>Áp dụng
                </button>
              </div>
            </div>
            <div class="col-md-7">
              <div class="d-flex justify-content-end align-items-center gap-2">
                <span class="text-muted me-3">
                  Đã chọn:
                  <strong class="text-dark">{{
                    campaignSelectedCount
                  }}</strong>
                  / {{ filteredCampaignProducts.length }} sản phẩm
                </span>
                <button
                  @click="unselectAllCampaign"
                  class="btn btn-outline-secondary"
                  :disabled="campaignSelectedCount === 0"
                >
                  <i class="bi bi-x-circle me-1"></i>Bỏ chọn
                </button>
              </div>
            </div>
          </div>

          <!-- Table chọn sản phẩm cho chiến dịch -->
          <div class="table-responsive mb-4">
            <table class="table table-bordered table-hover align-middle">
              <thead class="table-dark">
                <tr>
                  <th style="width: 40px" class="text-center">
                    <div class="form-check d-flex justify-content-center">
                      <input
                        class="form-check-input"
                        type="checkbox"
                        v-model="isCampaignAllSelected"
                      />
                    </div>
                  </th>
                  <th class="text-center" style="width: 80px">Hình</th>
                  <th class="text-center">Sản Phẩm</th>
                  <th class="text-center">Trạng Thái Hiện Tại</th>
                  <th class="text-center">Giá Hiện Tại</th>
                  <th class="text-center" style="width: 200px">
                    % Giảm Giá Chiến Dịch
                  </th>
                  <th class="text-center">Preview Giá Mới</th>
                </tr>
              </thead>
              <tbody>
                <tr
                  v-for="item in filteredCampaignProducts"
                  :key="item.maSP"
                  :class="{ 'table-info': item.campaignSelected }"
                >
                  <td class="text-center">
                    <div class="form-check d-flex justify-content-center">
                      <input
                        class="form-check-input"
                        type="checkbox"
                        v-model="item.campaignSelected"
                      />
                    </div>
                  </td>
                  <td>
                    <img
                      :src="
                        item.hinhAnh
                          ? `/images/${item.hinhAnh}`
                          : 'https://placehold.co/80x80'
                      "
                      class="img-thumbnail"
                      style="width: 60px; height: 60px; object-fit: cover"
                    />
                  </td>
                  <td>
                    <strong>{{ item.tenSP }}</strong>
                    <div class="text-muted small">Mã SP: {{ item.maSP }}</div>
                  </td>
                  <td class="text-center">
                    <span
                      v-if="item.khuyenMai > 0"
                      class="badge bg-danger px-3 py-2"
                      >🔥 Đang Giảm ({{ item.khuyenMai }}%)</span
                    >
                    <span v-else class="badge bg-secondary px-3 py-2"
                      >Không Giảm</span
                    >
                  </td>
                  <td class="text-end">
                    <div v-if="item.khuyenMai > 0">
                      <span class="text-muted text-decoration-line-through small d-block">
                        {{ displayPriceRange(item.donGiaMin, item.donGiaMax) }}
                      </span>
                      <strong class="text-dark">
                        {{ displayDiscountedPriceRange(item.donGiaMin, item.donGiaMax, item.khuyenMai) }}
                      </strong>
                    </div>
                    <div v-else class="mt-2">
                      <strong class="text-dark">
                        {{ displayPriceRange(item.donGiaMin, item.donGiaMax) }}
                      </strong>
                    </div>
                  </td>
                  <td>
                    <div class="input-group">
                      <input
                        type="number"
                        v-model="item.campaignDiscount"
                        class="form-control text-center fw-bold text-dark"
                        min="0"
                        max="100"
                        placeholder="%"
                        @input="item.campaignDiscount > 0 ? item.campaignSelected = true : item.campaignSelected = false"
                      />
                      <span class="input-group-text">%</span>
                    </div>
                  </td>
                  <td class="text-end">
                    <div v-if="item.campaignDiscount > 0">
                      <span class="text-muted text-decoration-line-through small d-block">
                        {{ displayPriceRange(item.donGiaMin, item.donGiaMax) }}
                      </span>
                      <span class="fw-bold text-success">
                        {{ displayDiscountedPriceRange(item.donGiaMin, item.donGiaMax, item.campaignDiscount) }}
                      </span>
                    </div>
                    
                    <div v-else-if="item.khuyenMai > 0">
                      <span class="text-muted text-decoration-line-through small d-block">
                        {{ displayPriceRange(item.donGiaMin, item.donGiaMax) }}
                      </span>
                      <strong class="text-danger">
                        {{ displayDiscountedPriceRange(item.donGiaMin, item.donGiaMax, item.khuyenMai) }}
                      </strong>
                    </div>

                    <div v-else class="fs-6 fw-bold text-dark mt-2">
                      {{ displayPriceRange(item.donGiaMin, item.donGiaMax) }}
                    </div>
                  </td>
                </tr>
                <tr v-if="filteredCampaignProducts.length === 0">
                  <td colspan="6" class="text-center py-5 text-muted">
                    <i class="bi bi-inbox fs-1 d-block mb-2"></i>Không tìm thấy
                    sản phẩm nào.
                  </td>
                </tr>
              </tbody>
            </table>
          </div>

          <!-- Nút bắt đầu chiến dịch -->
          <div class="d-flex justify-content-end"></div>
        </div>

        <!-- ===================== TAB LỊCH SỬ CHIẾN DỊCH ===================== -->
        <div
          v-if="activeTab === 'history'"
          class="bg-white p-4 rounded-3 shadow-sm border"
        >
          <div class="d-flex justify-content-between align-items-center mb-3">
            <h6 class="fw-bold mb-0">
              <i class="bi bi-clock-history me-2"></i>Lịch Sử Chiến Dịch
            </h6>
          </div>

          <!-- Lọc trạng thái -->
          <div class="row g-2 mb-3">
            <div class="col-md-3">
<select v-model="historyFilterStatus" class="form-select">
                <option value="">Tất cả trạng thái</option>
                <option value="Chưa bắt đầu">Chưa bắt đầu</option>
                <option value="Đang chạy">Đang chạy</option>
                <option value="Kết thúc">Kết thúc</option>
              </select>
            </div>
          </div>
          <div
            v-if="campaigns.length === 0"
            class="text-center py-5 text-muted"
          >
            <i class="bi bi-inbox fs-1 d-block mb-2"></i>Chưa có chiến dịch nào.
          </div>

          <!-- Accordion danh sách chiến dịch -->
          <div v-else class="accordion" id="campaignAccordion">
            <div
              v-for="(cd) in filteredCampaigns"
              :key="cd.maCD"
              class="accordion-item mb-3 border rounded shadow-sm position-relative"
            >
              <div 
                v-if="cd.trangThai === 'Đang chạy' || cd.trangThai === 'Chưa bắt đầu'" 
                class="position-absolute" 
                style="right: 60px; top: 11px; z-index: 10;"
              >
                <button
                  @click.stop="handleEndCampaignEarly(cd)"
                  class="btn btn-sm btn-danger fw-semibold shadow-sm"
                >
                  <i class="bi bi-stop-fill me-1"></i>Kết thúc sớm
                </button>
              </div>

              <h2 class="accordion-header">
                <button
                  class="accordion-button fw-semibold"
                  :class="{
                    'collapsed': !cd.isOpen,
                    'bg-info-subtle': cd.trangThai === 'Chưa bắt đầu',
                    'bg-success-subtle': cd.trangThai === 'Đang chạy',
                    'bg-warning-subtle': cd.trangThai === 'Đã dừng',
                    'bg-light': cd.trangThai === 'Kết thúc'
                  }"
                  type="button"
                  @click="handleToggleCampaign(cd)"
                >
                  <div class="d-flex align-items-center gap-3" style="width: calc(100% - 160px);">
                    <span
                      class="badge"
                      :class="{
                        'bg-info text-dark': cd.trangThai === 'Chưa bắt đầu',
                        'bg-success': cd.trangThai === 'Đang chạy',
                        'bg-warning text-dark': cd.trangThai === 'Đã dừng',
                        'bg-secondary': cd.trangThai === 'Kết thúc'
                      }"
                      style="min-width: 95px"
                    >
                      <i class="bi me-1" :class="{
                          'bi-clock-fill': cd.trangThai === 'Chưa bắt đầu',
                          'bi-play-circle-fill': cd.trangThai === 'Đang chạy',
                          'bi-pause-circle-fill': cd.trangThai === 'Đã dừng',
                          'bi-check-circle-fill': cd.trangThai === 'Kết thúc'
                        }"></i> {{ cd.trangThai }}
                    </span>

                    <div class="text-truncate">
                      <span class="fs-6 me-2">{{ cd.tenChienDich }}</span>
                      <span class="text-muted small">
                        <i class="bi bi-calendar3 me-1"></i>
                        {{ formatDateTime(cd.thoiGianBatDau) }} → {{ formatDateTime(cd.thoiGianKetThuc) }}
                      </span>
                    </div>
                  </div>

                  <div v-if="cd.loadingDetail" class="spinner-border spinner-border-sm text-secondary ms-auto" style="margin-right: 15px;"></div>
                </button>
              </h2>

              <button :id="`hidden-btn-${cd.maCD}`" class="d-none" data-bs-toggle="collapse" :data-bs-target="`#collapse-${cd.maCD}`"></button>

              <div 
                :id="`collapse-${cd.maCD}`" 
                class="accordion-collapse collapse" 
              >
                <div class="accordion-body p-0">
                  <div v-if="cd.details && cd.details.length > 0" class="table-responsive">
                    <table class="table table-sm table-hover mb-0 align-middle" style="table-layout: fixed;">
                      <thead class="table-light">
                        <tr>
                          <th class="text-center" style="width: 70px;">Hình</th>
                          <th>Sản Phẩm</th> 
                          <th class="text-center" style="width: 100px;">Giảm Giá</th>
                          <th class="text-end" style="width: 140px;">Giá Gốc</th>
                          <th class="text-end" style="width: 140px;">Giá Sau Giảm</th>
                          <th v-if="cd.trangThai === 'Đang chạy' || cd.trangThai === 'Chưa bắt đầu'" class="text-center" style="width: 100px;">Sửa %</th>
                        </tr>
                      </thead>
                      <tbody>
                        <tr v-for="sp in cd.details" :key="sp.maSP">
                          <td class="text-center">
                            <img :src="sp.hinhAnh ? `/images/${sp.hinhAnh}` : 'https://placehold.co/60x60'" class="img-thumbnail rounded" style="width:45px; height:45px; object-fit:cover;" />
                          </td>
                          <td class="text-truncate" :title="sp.tenSP">
                            <strong class="text-dark">{{ sp.tenSP }}</strong>
                          </td>
                          <td class="text-center">
                            <span class="badge bg-danger">🔥 {{ sp.khuyenMai }}%</span>
                          </td>
                          <td class="text-end text-muted text-decoration-line-through small">
                            {{ displayPriceRange(sp.donGiaMin, sp.donGiaMax) }}
                          </td>
                          <td class="text-end text-success fw-bold">
                            {{ displayDiscountedPriceRange(sp.donGiaMin, sp.donGiaMax, sp.khuyenMai) }}
                          </td>
                          <td v-if="cd.trangThai === 'Đang chạy' || cd.trangThai === 'Chưa bắt đầu'" class="text-center align-middle">
                            <div class="input-group input-group-sm mx-auto" style="min-width: 80px !important; width: 100%; flex-wrap: nowrap;">
                              <input type="number" v-model="sp.khuyenMaiEdit" class="form-control text-center fw-bold text-dark px-1" style="height: 32px;" min="0" max="100"/>
                              <button @click="handleUpdateCampaignDiscount(cd, sp)" class="btn btn-outline-primary px-2" style="height: 32px;" title="Lưu lại">
                                <i class="bi bi-save"></i>
                              </button>
                            </div>
                          </td>
                        </tr>
                      </tbody>
                    </table>
                  </div>

                  <div v-else-if="cd.details && cd.details.length === 0" class="d-flex justify-content-center align-items-center text-muted small py-4">
                    Không có dữ liệu sản phẩm trong chiến dịch này.
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
        <!-- end tabs -->
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, onUnmounted } from "vue";
import axios from "axios";
import NV_Sidebar from "@/components/Shared/NV_Sidebar.vue";


axios.defaults.withCredentials = true;

// =================== SHARED ===================
const products = ref([]);
const categories = ref([]);
const activeTab = ref("campaign");

// =================== FLASH SALE ===================
const bulkDiscount = ref(0);
const filterKeyword = ref("");
const filterCategory = ref("");
const filterFlashSale = ref("");
const filterActive = ref("");

const fetchCategories = async () => {
  try {
    const response = await axios.get(
      "http://localhost:8080/api/nhapkho/danhmuc"
    );
    categories.value = response.data;
  } catch (error) {
    console.error("Lỗi lấy danh mục:", error);
  }
};

const fetchProducts = async () => {
  try {
    const response = await axios.get(
      "http://localhost:8080/api/khuyenmai/sanpham"
    );
    products.value = response.data.map((item) => ({
      ...item,
      khuyenMaiMoi: item.khuyenMai || 0,
      selected: false,
      campaignSelected: false,
      campaignDiscount: item.khuyenMai || 0,
    }));
  } catch (error) {
    console.error("Lỗi lấy danh sách sản phẩm:", error);
  }
};

const filteredProducts = computed(() => {
  return products.value.filter((item) => {
    const matchKeyword =
      !filterKeyword.value ||
      item.tenSP?.toLowerCase().includes(filterKeyword.value.toLowerCase());
    let matchFlashSale = true;
    if (filterFlashSale.value === "DangSale")
      matchFlashSale = item.khuyenMai > 0;
    else if (filterFlashSale.value === "KhongSale")
      matchFlashSale = item.khuyenMai === 0;
    let matchCategory = true;
    if (filterCategory.value) {
      matchCategory = item.maDMs
        ? item.maDMs.split(",").includes(String(filterCategory.value))
        : false;
    }
    let matchActive = true;
    if (filterActive.value !== "") {
      const status = item.isActive !== undefined ? item.isActive : item.active;
      if (filterActive.value === "true")
        matchActive = status === true || status === 1 || status === "true";
      else if (filterActive.value === "false")
        matchActive = status === false || status === 0 || status === "false";
    }
    return matchKeyword && matchFlashSale && matchCategory && matchActive;
  });
});

const resetFilters = () => {
  filterKeyword.value = "";
  filterCategory.value = "";
  filterFlashSale.value = "";
  filterActive.value = "";
};

const selectedCount = computed(
  () => filteredProducts.value.filter((p) => p.selected).length
);

const isAllSelected = computed({
  get: () =>
    filteredProducts.value.length > 0 &&
    filteredProducts.value.every((p) => p.selected),
  set: (val) => filteredProducts.value.forEach((p) => (p.selected = val)),
});

const unselectAll = () => products.value.forEach((p) => (p.selected = false));

const isValidDiscount = (value) =>
  value !== null && value !== "" && value >= 0 && value <= 100;

const applyBulkDiscount = () => {
  if (!isValidDiscount(bulkDiscount.value)) {
    showToast(
      "Phần trăm khuyến mãi phải nằm trong khoảng từ 0 đến 100!",
      "warning"
    );
    return;
  }
  filteredProducts.value.forEach((p) => {
    if (p.selected) p.khuyenMaiMoi = bulkDiscount.value;
  });
};

const handleBulkSave = async () => {
  const selectedItems = products.value.filter((p) => p.selected);
  if (!selectedItems.every((p) => isValidDiscount(p.khuyenMaiMoi))) {
    showToast(
      "Có sản phẩm chứa giá trị khuyến mãi không hợp lệ (Phải từ 0-100).",
      "warning"
    );
    return;
  }
  const payload = selectedItems.map((item) => ({
    maSP: item.maSP,
    khuyenMai: item.khuyenMaiMoi,
  }));
  try {
    await axios.post(
      "http://localhost:8080/api/khuyenmai/cap-nhat-hang-loat",
      payload
    );
    showToast(
      `Đã cập nhật khuyến mãi thành công cho ${selectedItems.length} sản phẩm!`
    );
    unselectAll();
    filterFlashSale.value = "DangSale";
    await fetchProducts();
  } catch (error) {
    showToast("Có lỗi xảy ra khi cập nhật khuyến mãi!", "danger");
    console.error(error);
  }
};

const handleSingleSave = async (item) => {
  if (!isValidDiscount(item.khuyenMaiMoi)) {
    showToast(
      "Phần trăm khuyến mãi phải nằm trong khoảng từ 0 đến 100!",
      "warning"
    );
    return;
  }
  try {
    await axios.post("http://localhost:8080/api/khuyenmai/cap-nhat", {
      maSP: item.maSP,
      khuyenMai: item.khuyenMaiMoi,
    });
    showToast("Cập nhật khuyến mãi thành công!");
    filterFlashSale.value = "DangSale";
    await fetchProducts();
  } catch (error) {
    showToast("Có lỗi xảy ra!", "danger");
    console.error(error);
  }
};

// =================== CAMPAIGN TAB ===================
const campaign = ref({
  tenChienDich: "",
  thoiGianBatDau: "",
  thoiGianKetThuc: "",
});
const campaignBulkDiscount = ref(0);
const campaignFilterKeyword = ref("");
const campaignFilterCategory = ref("");
const campaignFilterActive = ref("");

const filteredCampaignProducts = computed(() => {
  return products.value.filter((item) => {
    const matchKeyword =
      !campaignFilterKeyword.value ||
      item.tenSP
        ?.toLowerCase()
        .includes(campaignFilterKeyword.value.toLowerCase());
    let matchCategory = true;
    if (campaignFilterCategory.value) {
      matchCategory = item.maDMs
        ? item.maDMs.split(",").includes(String(campaignFilterCategory.value))
        : false;
    }
    let matchActive = true;
    if (campaignFilterActive.value !== "") {
      const status = item.isActive !== undefined ? item.isActive : item.active;
      if (campaignFilterActive.value === "true")
        matchActive = status === true || status === 1 || status === "true";
      else if (campaignFilterActive.value === "false")
        matchActive = status === false || status === 0 || status === "false";
    }
    return matchKeyword && matchCategory && matchActive;
  });
});

const resetCampaignFilters = () => {
  campaignFilterKeyword.value = "";
  campaignFilterCategory.value = "";
  campaignFilterActive.value = "";
};

const campaignSelectedCount = computed(
  () => filteredCampaignProducts.value.filter((p) => p.campaignSelected).length
);

// =================== AUTO POLLING (CHẠY NGẦM) ===================
let campaignPollingInterval = null;

onMounted(() => {
  fetchProducts();
  fetchCategories();
  
  // Khởi tạo lấy danh sách chiến dịch ngay khi vừa vào trang
  fetchCampaigns();

  // Cài đặt vòng lặp: Cứ 5000ms (5 giây) sẽ chạy lại hàm 1 lần
  campaignPollingInterval = setInterval(() => {
    // Mẹo tối ưu: Chỉ gọi API chọc xuống Database nếu người dùng ĐANG MỞ tab Lịch sử
    // Nếu họ đang ở tab Flash Sale thì không gọi để giảm tải cho Spring Boot
    if (activeTab.value === 'history') {
      fetchCampaigns();
    }
  }, 5000);
});

// RẤT QUAN TRỌNG: Hàm này sẽ tự động chạy khi bạn chuyển sang component khác (Vd: qua trang QL Sản Phẩm)
// Nó giúp "tiêu diệt" vòng lặp 5s, tránh việc gọi API ảo gây giật lag và tốn RAM trình duyệt
onUnmounted(() => {
  if (campaignPollingInterval) {
    clearInterval(campaignPollingInterval);
  }
});

const isCampaignAllSelected = computed({
  get: () =>
    filteredCampaignProducts.value.length > 0 &&
    filteredCampaignProducts.value.every((p) => p.campaignSelected),
  set: (val) =>
    filteredCampaignProducts.value.forEach((p) => (p.campaignSelected = val)),
});

const unselectAllCampaign = () =>
  products.value.forEach((p) => (p.campaignSelected = false));

const applyCampaignBulkDiscount = () => {
  if (!isValidDiscount(campaignBulkDiscount.value)) {
    showToast("Phần trăm giảm giá phải từ 0 đến 100!", "warning");
    return;
  }
  filteredCampaignProducts.value.forEach((p) => {
    if (p.campaignSelected) p.campaignDiscount = campaignBulkDiscount.value;
  });
};

const handleStartCampaign = async () => {
  if (!campaign.value.tenChienDich.trim()) {
    showToast("Vui lòng nhập tên chiến dịch!", "warning");
    return;
  }
  if (!campaign.value.thoiGianBatDau || !campaign.value.thoiGianKetThuc) {
    showToast("Vui lòng chọn thời gian bắt đầu và kết thúc!", "warning");
    return;
  }
  if (
    new Date(campaign.value.thoiGianBatDau) >=
    new Date(campaign.value.thoiGianKetThuc)
  ) {
    showToast("Thời gian kết thúc phải sau thời gian bắt đầu!", "warning");
    return;
  }

  const selectedItems = products.value.filter((p) => p.campaignSelected);
  if (selectedItems.length === 0) {
    showToast("Vui lòng chọn ít nhất một sản phẩm cho chiến dịch!", "warning");
    return;
  }

  const invalidItems = selectedItems.filter(
    (p) => !isValidDiscount(p.campaignDiscount) || p.campaignDiscount <= 0
  );
  if (invalidItems.length > 0) {
    showToast(
      "Vui lòng nhập % giảm giá hợp lệ (1-100) cho tất cả sản phẩm đã chọn!",
      "warning"
    );
    return;
  }

  const payload = {
    tenChienDich: campaign.value.tenChienDich,
    thoiGianBatDau: campaign.value.thoiGianBatDau,
    thoiGianKetThuc: campaign.value.thoiGianKetThuc,
    sanPhams: selectedItems.map((item) => ({
      maSP: item.maSP,
      khuyenMai: item.campaignDiscount,
    })),
  };

  try {
    await axios.post("http://localhost:8080/api/chiendich/tao", payload);
    showToast(
      `Chiến dịch "${campaign.value.tenChienDich}" đã được tạo thành công!`
    );
    // Reset form
    campaign.value = {
      tenChienDich: "",
      thoiGianBatDau: "",
      thoiGianKetThuc: "",
    };
    products.value.forEach((p) => {
      p.campaignSelected = false;
      p.campaignDiscount = 0;
    });
    campaignBulkDiscount.value = 0;
    // Chuyển sang tab lịch sử
    activeTab.value = "history";
    await fetchCampaigns();
  } catch (error) {
    showToast("Có lỗi xảy ra khi tạo chiến dịch!", "danger");
    console.error(error);
  }
};

// =================== HISTORY TAB ===================
const campaigns = ref([]);
const historyFilterStatus = ref("");

const filteredCampaigns = computed(() => {
  if (!historyFilterStatus.value) return campaigns.value;
  return campaigns.value.filter(
    (cd) => cd.trangThai === historyFilterStatus.value
  );
});

const fetchCampaigns = async () => {
  try {
    const response = await axios.get(
      "http://localhost:8080/api/chiendich/danh-sach"
    );
    
    // Lưu tạm lại danh sách chiến dịch cũ đang hiển thị trên màn hình
    const oldCampaigns = campaigns.value;

    campaigns.value = response.data.map((cd) => {
      // Tìm xem chiến dịch này lúc trước đã được mở ra xem (có details) chưa
      const existingCd = oldCampaigns.find(old => old.maCD === cd.maCD);
      
      return {
        ...cd,
        // Nếu đã mở rồi thì giữ nguyên mảng details cũ, nếu chưa thì để null
        details: existingCd ? existingCd.details : null,
        loadingDetail: existingCd ? existingCd.loadingDetail : false,
        isOpen: existingCd ? existingCd.isOpen : false,
      };
    });
  } catch (error) {
    showToast("Lỗi lấy danh sách chiến dịch!", "danger");
    console.error(error);
  }
};


const handleToggleCampaign = async (cd) => {
  // Tránh việc user nhấp đúp nhiều lần khi đang xoay loading
  if (cd.loadingDetail) return; 

  // 1. TÌNH HUỐNG: Đang mở -> Bấm để ĐÓNG LẠI
  if (cd.isOpen) {
    cd.isOpen = false;
    document.getElementById(`hidden-btn-${cd.maCD}`).click(); // Kích hoạt đóng
    return;
  }

  // 2. TÌNH HUỐNG: Đang đóng nhưng đã lấy data rồi -> MỞ RA LUÔN (Không load lại)
  if (cd.details !== null) {
    cd.isOpen = true;
    document.getElementById(`hidden-btn-${cd.maCD}`).click(); // Kích hoạt mở
    return;
  }

  // 3. TÌNH HUỐNG: Lần đầu tiên bấm -> PHẢI LOAD DATA TRƯỚC RỒI MỚI XỔ
  cd.loadingDetail = true;
  try {
    const response = await axios.get(
      `http://localhost:8080/api/chiendich/${cd.maCD}/chi-tiet`
    );
    cd.details = response.data.map((sp) => ({
      ...sp,
      khuyenMaiEdit: sp.khuyenMai || 0,
    }));

    // Fake delay 0.3s cho mượt theo đúng ý tưởng của bạn
    await new Promise(resolve => setTimeout(resolve, 90));

    // Đã load xong, đổi trạng thái thành Mở
    cd.isOpen = true;

    // QUAN TRỌNG: Chờ 50ms cho Vue render xong cái table ẩn ở dưới
    // Rồi mới ra lệnh cho Bootstrap trượt xuống (Lúc này đã tính toán đúng 100% chiều cao)
    setTimeout(() => {
      const hiddenBtn = document.getElementById(`hidden-btn-${cd.maCD}`);
      if (hiddenBtn) hiddenBtn.click();
    }, 50);

  } catch (error) {
    showToast("Lỗi lấy chi tiết chiến dịch!", "danger");
    cd.details = [];
  } finally {
    cd.loadingDetail = false;
  }
};

const loadCampaignDetail = async (cd) => {
  // Đã load rồi thì thôi
  if (cd.details !== null) return;
  cd.loadingDetail = true;
  try {
    const response = await axios.get(
      `http://localhost:8080/api/chiendich/${cd.maCD}/chi-tiet`
    );
    // Gán trực tiếp dữ liệu chuẩn từ Backend trả về
    cd.details = response.data.map((sp) => ({
      ...sp,
      khuyenMaiEdit: sp.khuyenMai || 0,
    }));
  } catch (error) {
    showToast("Lỗi lấy chi tiết chiến dịch!", "danger");
    cd.details = [];
    console.error(error);
  } finally {
    cd.loadingDetail = false;
  }
};

const handleEndCampaignEarly = async (cd) => {
  if (
    !confirm(`Bạn có chắc muốn kết thúc sớm chiến dịch "${cd.tenChienDich}"?`)
  )
    return;
  try {
    await axios.put(
      `http://localhost:8080/api/chiendich/${cd.maCD}/ket-thuc-som`
    );
    showToast(`Đã kết thúc sớm chiến dịch "${cd.tenChienDich}"!`);
    await fetchCampaigns();
  } catch (error) {
    showToast("Có lỗi xảy ra khi kết thúc chiến dịch!", "danger");
    console.error(error);
  }
};

const handleUpdateCampaignDiscount = async (cd, sp) => {
  if (!isValidDiscount(sp.khuyenMaiEdit) || sp.khuyenMaiEdit <= 0) {
    showToast("Phần trăm giảm giá phải từ 1 đến 100!", "warning");
    return;
  }
  try {
    await axios.put(
      `http://localhost:8080/api/chiendich/${cd.maCD}/cap-nhat-km`,
      {
        maSP: sp.maSP,
        khuyenMai: sp.khuyenMaiEdit,
      }
    );
    sp.khuyenMai = sp.khuyenMaiEdit;
    showToast(`Đã cập nhật giảm giá ${sp.khuyenMaiEdit}% cho "${sp.tenSP}"!`);
  } catch (error) {
    showToast("Có lỗi xảy ra khi cập nhật!", "danger");
    console.error(error);
  }
};

// =================== UTILITIES ===================
const formatCurrency = (value) => {
  if (!value) return "0 ₫";
  return new Intl.NumberFormat("vi-VN", {
    style: "currency",
    currency: "VND",
  }).format(value);
};

const formatDateTime = (dt) => {
  if (!dt) return "";
  return new Date(dt).toLocaleString("vi-VN", {
    day: "2-digit",
    month: "2-digit",
    year: "numeric",
    hour: "2-digit",
    minute: "2-digit",
  });
};

const displayPriceRange = (min, max) => {
  if (min == null || max == null) return "0 ₫";
  if (min === max) return formatCurrency(min);
  return `${formatCurrency(min)} - ${formatCurrency(max)}`;
};

const displayDiscountedPriceRange = (min, max, discountPercent) => {
  if (min == null || max == null) return "0 ₫";
  const ratio = 1 - discountPercent / 100;
  const discountedMin = min * ratio;
  const discountedMax = max * ratio;
  if (min === max) return formatCurrency(discountedMin);
  return `${formatCurrency(discountedMin)} - ${formatCurrency(discountedMax)}`;
};

// =================== TOAST ===================
const toast = ref({ id: 0, show: false, message: "", type: "success" });
let toastTimeout = null;
const showToast = (message, type = "success") => {
  toast.value = { id: Date.now(), show: true, message, type };
  if (toastTimeout) clearTimeout(toastTimeout);
  toastTimeout = setTimeout(() => {
    toast.value.show = false;
  }, 5000);
};

const isSidebarCollapsed = ref(false);

const handleSidebarCollapse = (collapsedState) => {
  isSidebarCollapsed.value = collapsedState;
};

onMounted(() => {
  fetchProducts();
  fetchCategories();
});
</script>

<style scoped>
.main-content {
  margin-left: 260px;
  min-height: 100vh;
  background: #f8f9fa;
}
.page-container {
  padding: 30px;
}
.input-group .form-control-lg {
  font-size: 16px;
}
td .input-group {
  min-width: 150px;
}
td .form-control {
  height: 38px;
  padding: 8px 12px;
  font-size: 15px;
  font-weight: bold;
}

/* Toast */
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

/* Accordion campaign */
.accordion-button:not(.collapsed) {
  box-shadow: none;
}
.accordion-button:focus {
  box-shadow: none;
}
</style>
<style>
/* CSS Global để fix lỗi giật layout do Scrollbar xuất hiện đột ngột */
html {
  /* Cách 1: Luôn luôn hiện rãnh (track) của thanh cuộn dọc (Được hỗ trợ trên 100% trình duyệt) */
  overflow-y: scroll; 
  
  /* Cách 2 (Hiện đại): Yêu cầu trình duyệt tự động chừa sẵn một khoảng trống tương đương thanh cuộn */
  scrollbar-gutter: stable;
}

.main-content {
  margin-left: 260px; /* Trạng thái Sidebar mặc định */
  min-height: 100vh;
  background: #f8f9fa;
  transition: margin-left 0.3s cubic-bezier(0.25, 0.8, 0.25, 1); /* Thêm dòng này để mượt */
}

/* Khi Sidebar thu nhỏ thì nới rộng nội dung chính ra */
.main-content.expanded {
  margin-left: 80px; 
}
</style>