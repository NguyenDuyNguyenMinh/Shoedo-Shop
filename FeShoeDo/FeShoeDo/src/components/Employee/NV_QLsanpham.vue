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

        <!-- Stats Cards -->
        <div class="row g-3 mb-4">
          <div class="col-md-3">
            <div class="stats-mini bg-gradient-1">
              <h4>{{ stats.totalProducts || 0 }}</h4>
              <p>Tổng sản phẩm</p>
            </div>
          </div>
          <div class="col-md-3">
            <div class="stats-mini bg-gradient-2">
              <h4>{{ stats.activeProducts || 0 }}</h4>
              <p>Đang hoạt động</p>
            </div>
          </div>
          <div class="col-md-3">
            <div class="stats-mini bg-gradient-3">
              <h4>{{ stats.outOfStock || 0 }}</h4>
              <p>Hết hàng</p>
            </div>
          </div>
          <div class="col-md-3">
            <div class="stats-mini bg-gradient-4">
              <h4>{{ stats.lowStock || 0 }}</h4>
              <p>Sắp hết hàng</p>
            </div>
          </div>
        </div>

        <!-- Main Card -->
        <div class="content-card">
          <!-- Header -->
          <div class="d-flex justify-content-between align-items-center mb-4">
            <div>
              <h5 class="page-title">Danh sách sản phẩm</h5>
              <p class="page-subtitle">Quản lý và chỉnh sửa thông tin sản phẩm</p>
            </div>
            <button class="btn btn-dark" @click="showAddModal">
              <i class="bi bi-plus-circle me-2"></i>Thêm sản phẩm mới
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
                  <input type="text" v-model="searchKeyword" 
                         class="form-control" 
                         placeholder="Tìm kiếm theo tên sản phẩm..."
                         @input="handleSearch">
                </div>
              </div>
              <div class="col-md-3">
                <select class="form-select" v-model="selectedCategory" @change="fetchProducts">
                  <option value="">Tất cả danh mục</option>
                  <option v-for="category in categories" :key="category.maDM" :value="category.maDM">
                    {{ category.tenDM }}
                  </option>
                </select>
              </div>
              <div class="col-md-2">
                <select class="form-select" v-model="selectedStatus" @change="fetchProducts">
                  <option value="">Trạng thái</option>
                  <option value="Còn hàng">Còn hàng</option>
                  <option value="Hết hàng">Hết hàng</option>
                  <option value="Sắp hết">Sắp hết</option>
                </select>
              </div>
              <div class="col-md-3">
                <div class="d-flex gap-2">
                  <button class="btn btn-dark flex-fill" @click="fetchProducts">
                    <i class="bi bi-search me-2"></i>Tìm kiếm
                  </button>
                  <button class="btn btn-outline-secondary" @click="resetFilters">
                    <i class="bi bi-arrow-clockwise"></i>
                  </button>
                </div>
              </div>
            </div>
          </div>

          <!-- Products List -->
          <div class="products-list">
            <div v-if="loading" class="text-center py-5">
              <div class="spinner-border text-primary" role="status">
                <span class="visually-hidden">Loading...</span>
              </div>
            </div>

            <div v-else-if="products.length === 0" class="text-center py-5">
              <i class="bi bi-inbox display-4 d-block mb-3 text-muted"></i>
              <p class="text-muted" v-if="searchKeyword || selectedCategory || selectedStatus">
                Không tìm thấy sản phẩm nào phù hợp
              </p>
              <p class="text-muted" v-else>
                Chưa có sản phẩm nào
              </p>
            </div>

            <div v-else class="row">
              <div v-for="product in products" :key="product.maSP" class="col-12">
                <div class="product-card">
                  <div class="row align-items-center" style="height: 100px;">
                    <!-- Product Image -->
                    <div class="col-md-1">
                      <img :src="getImageUrl(product.hinh)" 
                           :alt="product.tenSP"
                           class="product-image rounded"
                           @error="handleImageError" >
                    </div>
                    
                    <!-- Product Info -->
                    <div class="col-md-8 align-items-center">
                      <div class="d-flex">
                        <h6 class="mb-1 me-2">{{ product.tenSP }} </h6>
                        <p class="text-muted small mb-1 d-flex align-items-center">
                          ||  {{ product.danhMuc?.tenDM }}<i class="bi bi-tag me-1"></i>
                        </p>
                      </div>

                      <p class="text-muted small mb-1">
                        <i class="bi bi-box me-1"></i> {{ product.totalVariants }} phân loại
                      </p>
                      <div class="mb-1">
                        <span class="text-primary fw-bold">{{ formatPrice(product.donGia) }}</span>
                      </div>
                      <div class="small">
                        <span class="me-3">Tồn kho: <strong>{{ product.soLuong }}</strong></span>
                      </div>
                    </div>
        
                    <!-- Price & Stock -->
                    <div class="col-md-1 align-items-center">
                      <div class="small">
                        <span :class="getStatusBadgeClass(product.trangThai)" class="badge">
                          {{ product.trangThai }}
                        </span>
                      </div>
                    </div>
                    
                    <!-- Actions -->
                    <div class="col-md-2 align-items-end">
                      <div class="action-buttons">
                        <button class="btn btn-outline-primary btn-sm me-2" 
                                @click="showEditModal(product)"
                                title="Chỉnh sửa">
                          <i class="bi bi-pencil"></i>
                        </button>
                        <button class="btn btn-outline-secondary btn-sm me-2" 
                                @click="showVariantsModal(product)"
                                title="Xem phân loại">
                          <i class="bi bi-list-check"></i>
                        </button>
                        <button class="btn btn-outline-danger btn-sm" 
                                @click="confirmDeleteProduct(product)"
                                title="Xóa">
                          <i class="bi bi-trash"></i>
                        </button>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <!-- Pagination -->
            <div v-if="totalPages > 1" class="mt-4">
              <nav>
                <ul class="pagination justify-content-center">
                  <li class="page-item" :class="{ disabled: currentPage === 0 }">
                    <button class="page-link" @click="changePage(currentPage - 1)">
                      <i class="bi bi-chevron-left"></i>
                    </button>
                  </li>
                  
                  <li v-for="page in visiblePages" :key="page" 
                      class="page-item" 
                      :class="{ active: page === currentPage + 1 }">
                    <button class="page-link" @click="changePage(page - 1)">
                      {{ page }}
                    </button>
                  </li>
                  
                  <li class="page-item" :class="{ disabled: currentPage === totalPages - 1 }">
                    <button class="page-link" @click="changePage(currentPage + 1)">
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

    <!-- Add Product Modal -->
    <div class="modal fade" id="addProductModal" tabindex="-1" aria-hidden="true">
      <div class="modal-dialog modal-lg">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">
              <i class="bi bi-plus-circle me-2"></i>Thêm sản phẩm mới
            </h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
          </div>
          <form @submit.prevent="submitAddProduct">
            <div class="modal-body">
              <h6 class="mb-3"><i class="bi bi-info-circle me-2"></i>Thông tin cơ bản</h6>
              <div class="row mb-3">
                <div class="col-md-12">
                  <label class="form-label">Tên sản phẩm *</label>
                  <input type="text" v-model="newProduct.tenSP" class="form-control" required>
                </div>
              </div>
              
              <div class="row mb-3">
                <div class="col-md-8">
                  <label class="form-label">Danh mục *</label>
                  <select v-model="newProduct.maDM" class="form-select" required>
                    <option value="">Chọn danh mục</option>
                    <option v-for="category in categories" :key="category.maDM" :value="category.maDM">
                      {{ category.tenDM }}
                    </option>
                  </select>
                </div>
                <div class="col-md-4">
                  <label class="form-label">&nbsp;</label>
                  <button type="button" class="btn btn-outline-primary w-100" @click="showAddCategoryModal">
                    <i class="bi bi-plus-circle me-2"></i>Thêm danh mục
                  </button>
                </div>
              </div>
              
              <div class="mb-3">
                <label class="form-label">Mô tả sản phẩm</label>
                <textarea v-model="newProduct.moTa" class="form-control" rows="5"></textarea>
              </div>
              <hr>
              <h6 class="mb-3 mt-4">
                <i class="bi bi-layers me-2"></i>Phân loại sản phẩm
              </h6>
              <div v-for="(variant, index) in newProduct.variants" :key="index" class="mb-3">
                <div v-if="newProduct.variants.length > 1" class="d-flex justify-content-between align-items-center mb-2">
                  <div class="fw-bold text-primary">
                    #{{ index + 1 }}
                  </div>
                  <button type="button"
                          class="btn btn-outline-danger btn-sm d-flex align-items-center"
                          @click="removeVariant(index)">
                    <i class="bi bi-trash me-1"></i> Xóa
                  </button>
                </div>

                <div class="card">
                  <div class="card-body">
                    <div class="row g-3">
                      <div class="col-md-6">
                        <label class="form-label">Tên phân loại *</label>
                        <input type="text" v-model="variant.phanLoai" class="form-control" required>
                      </div>
                      <div class="col-md-6">
                        <label class="form-label">Đơn giá (VND) *</label>
                        <input type="number" v-model="variant.donGia" class="form-control" min="0" required>
                      </div>
                      <div class="col-md-4">
                        <label class="form-label">Số lượng *</label>
                        <input type="number" v-model="variant.soLuong" class="form-control" min="0" required>
                      </div>
                      <div class="col-md-4">
                        <label class="form-label">Trạng thái</label>
                        <select v-model="variant.trangThai" class="form-select">
                          <option value="Còn hàng">Còn hàng</option>
                          <option value="Hết hàng">Hết hàng</option>

                        </select>
                      </div>
                      <div class="col-md-4">
                        <label class="form-label">Hình ảnh</label>
                        <div class="input-group">
                          <input type="text" v-model="variant.hinh" class="form-control" placeholder="Tên file ảnh">
                          <button type="button" class="btn btn-outline-secondary" @click="showImagePicker(index)">
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
                <button type="button" class="btn btn-outline-primary" @click="addVariant">
                  <i class="bi bi-plus-circle me-2"></i>Thêm phân loại
                </button>
              </div>
            </div>
            <div class="modal-footer">
              <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">
                <i class="bi bi-x-circle me-2"></i>Hủy
              </button>
              <button type="submit" class="btn btn-primary" :disabled="processing">
                <span v-if="processing" class="spinner-border spinner-border-sm me-2"></span>
                <i class="bi bi-check-circle me-2"></i>Lưu sản phẩm
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>

    <!-- Edit Product Modal -->
    <div class="modal fade" id="editProductModal" tabindex="-1" aria-hidden="true">
      <div class="modal-dialog modal-lg">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">
              <i class="bi bi-pencil me-2"></i>Chỉnh sửa sản phẩm
            </h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
          </div>
          <form @submit.prevent="submitEditProduct">
            <div class="modal-body">
              <h6 class="mb-3"><i class="bi bi-info-circle me-2"></i>Thông tin cơ bản</h6>
              <div class="row mb-3">
                <div class="col-md-12">
                  <label class="form-label">Tên sản phẩm *</label>
                  <input type="text" v-model="editingProduct.tenSP" class="form-control" required>
                </div>
              </div>
              <div class="row mb-3">
                <div class="col-md-12">
                  <label class="form-label">Danh mục *</label>
                  <select v-model="editingProduct.maDM" class="form-select" required>
                    <option value="">Chọn danh mục</option>
                    <option v-for="category in categories" :key="category.maDM" :value="category.maDM">
                      {{ category.tenDM }}
                    </option>
                  </select>
                </div>
              </div>
              
              <div class="mb-3">
                <label class="form-label">Mô tả sản phẩm</label>
                <textarea v-model="editingProduct.moTa" class="form-control" rows="5"></textarea>
              </div>
              <hr>
              <h6 class="mb-3 mt-4"><i class="bi bi-layers me-2"></i>Phân loại sản phẩm</h6>
              <div v-for="(variant, index) in editingProduct.variants" :key="variant.maPLSP || index" class="mb-3">
                <div v-if="editingProduct.variants.length > 1" class="d-flex justify-content-between align-items-center mb-2">
                  <div class="fw-bold text-primary">
                    #{{ index + 1 }}
                  </div>
                  <div class="fw-bold">
                    <button type="button"
                          class="btn btn-outline-danger align-items-center"
                          @click="removeEditVariant(index)"
                          :disabled="variant.maPLSP && editingProduct.variants.filter(v => v.maPLSP).length <= 1">
                      <i class="bi bi-trash me-1"></i>Xóa
                    </button>
                  </div>    
                </div>
                <div class="card">
                  <div class="card-body">
                    <input type="hidden" v-model="variant.maPLSP">
                    <div class="row g-3">
                      <div class="col-md-6">
                        <label class="form-label">Tên phân loại *</label>
                        <input type="text" v-model="variant.phanLoai" class="form-control" required>
                      </div>
                      <div class="col-md-6">
                        <label class="form-label">Đơn giá (VND) *</label>
                        <input type="number" v-model="variant.donGia" class="form-control" min="0" required>
                      </div>
                      <div class="col-md-4">
                        <label class="form-label">Số lượng *</label>
                        <input type="number" v-model="variant.soLuong" class="form-control" min="0" required>
                      </div>
                      <div class="col-md-4">
                        <label class="form-label">Trạng thái</label>
                        <select v-model="variant.trangThai" class="form-select">
                          <option value="Còn hàng">Còn hàng</option>
                          <option value="Hết hàng">Hết hàng</option>
                          
                        </select>
                      </div>
                      <div class="col-md-4">
                        <label class="form-label">Hình ảnh</label>
                        <div class="input-group">
                          <input type="text" v-model="variant.hinh" class="form-control" placeholder="Tên file ảnh">
                          <button type="button" class="btn btn-outline-secondary" @click="showImagePickerForEdit(index)">
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
                <button type="button" class="btn btn-outline-primary" @click="addEditVariant">
                  <i class="bi bi-plus-circle me-2"></i>Thêm phân loại
                </button>
              </div>
            </div>
            <div class="modal-footer">
              <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">
                <i class="bi bi-x-circle me-2"></i>Hủy
              </button>
              <button type="submit" class="btn btn-primary" :disabled="processing">
                <span v-if="processing" class="spinner-border spinner-border-sm me-2"></span>
                <i class="bi bi-check-circle me-2"></i>Cập nhật
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>

    <!-- Variants Modal -->
    <div class="modal fade" id="variantsModal" tabindex="-1" aria-hidden="true">
      <div class="modal-dialog modal-lg">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">
              <i class="bi bi-list-check me-2"></i>Phân loại sản phẩm: {{ selectedProduct?.tenSP }}
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
                  <tr v-for="(variant, index) in productVariants" :key="variant.maPLSP">
                    <td>{{ index + 1 }}</td>
                    <td>{{ variant.phanLoai }}</td>
                    <td>
                      <img :src="getImageUrl(variant.hinh)" 
                           :alt="variant.phanLoai"
                           class="variant-image"
                           @error="handleImageError">
                    </td>
                    <td>{{ variant.soLuong }}</td>
                    <td>{{ formatPrice(variant.donGia) }}</td>
                    <td>
                      <span :class="getStatusBadgeClass(variant.trangThai)" class="badge">
                        {{ variant.trangThai }}
                      </span>
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

    <!-- Add Category Modal -->
    <div class="modal fade" id="addCategoryModal" tabindex="-1" aria-hidden="true">
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">
              <i class="bi bi-plus-circle me-2"></i>Thêm danh mục mới
            </h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
          </div>
          <form @submit.prevent="submitAddCategory">
            <div class="modal-body">
              <div class="mb-3">
                <label class="form-label">Tên danh mục *</label>
                <input type="text" v-model="newCategory.tenDM" class="form-control" required>
              </div>
            </div>
            <div class="modal-footer">
              <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">
                <i class="bi bi-x-circle me-2"></i>Hủy
              </button>
              <button type="submit" class="btn btn-primary" :disabled="processing">
                <span v-if="processing" class="spinner-border spinner-border-sm me-2"></span>
                <i class="bi bi-check-circle me-2"></i>Thêm
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>

    <!-- Image Picker Modal -->
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
            <!-- Upload Section -->
            <div class="mb-4">
              <div class="image-upload-area" @click="triggerFileInput" 
                   @dragover.prevent="dragover = true"
                   @dragleave="dragover = false"
                   @drop.prevent="handleDrop"
                   :class="{ 'dragover': dragover }">
                <i class="bi bi-cloud-upload display-4 mb-3"></i>
                <p class="mb-2"><strong>Kéo thả hoặc nhấp để tải ảnh lên</strong></p>
                <p class="text-muted small mb-0">Hỗ trợ: JPG, PNG, JPEG, GIF, WEBP (Tối đa 5MB)</p>
                <input type="file" ref="fileInput" @change="handleFileSelect" accept="image/*" hidden>
              </div>
              <div v-if="uploadPreview" class="mt-3 text-center">
                <img :src="uploadPreview" class="img-thumbnail mb-2" style="max-height: 150px;">
                <p class="small">{{ uploadFileName }}</p>
              </div>
            </div>
            
            <!-- Available Images -->
            <div v-if="availableImages.length > 0">
              <h6 class="mb-3">Hình ảnh có sẵn</h6>
              <div class="row g-3">
                <div v-for="image in availableImages" :key="image" class="col-3 col-md-2">
                  <div class="image-thumbnail" :class="{ 'selected': selectedImage === image }" 
                       @click="selectImage(image)">
                    <img :src="getImageUrl(image)" :alt="image" @error="handleImageError">
                    <div class="image-name">{{ image }}</div>
                  </div>
                </div>
              </div>
            </div>
            <div v-else class="text-center py-4">
              <p class="text-muted">Chưa có hình ảnh nào</p>
            </div>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Hủy</button>
            <button type="button" class="btn btn-primary" @click="applySelectedImage" :disabled="!selectedImage">
              Chọn ảnh
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, computed } from 'vue';
import { Modal } from 'bootstrap';
import NV_Sidebar from '@/components/shared/NV_Sidebar.vue';
import api from '@/services/api';

export default {
  name: 'NV_QLSanPham',
  components: {
    NV_Sidebar
  },
  setup() {
    const products = ref([]);
    const categories = ref([]);
    const loading = ref(false);
    const processing = ref(false);
    const message = ref('');
    const error = ref('');
    
    const searchKeyword = ref('');
    const selectedCategory = ref('');
    const selectedStatus = ref('');
    
    const currentPage = ref(0);
    const pageSize = ref(20);
    const totalElements = ref(0);
    const totalPages = computed(() => Math.ceil(totalElements.value / pageSize.value));
    
    const stats = ref({
      totalProducts: 0,
      activeProducts: 0,
      outOfStock: 0,
      lowStock: 0
    });
    

    let addProductModal = null;
    let editProductModal = null;
    let variantsModal = null;
    let addCategoryModal = null;
    let imagePickerModal = null;
    
    const newProduct = ref({
      tenSP: '',
      maDM: '',
      moTa: '',
      variants: [
        {
          phanLoai: '',
          donGia: 0,
          soLuong: 0,
          trangThai: 'Còn hàng',
          hinh: ''
        }
      ]
    });
    
    const editingProduct = ref({
      maSP: null,
      tenSP: '',
      maDM: '',
      moTa: '',
      variants: []
    });
    
    const selectedProduct = ref(null);
    const productVariants = ref([]);
    const newCategory = ref({ tenDM: '' });
    const availableImages = ref([]);
    const selectedImage = ref('');
    const uploadPreview = ref('');
    const uploadFileName = ref('');
    const dragover = ref(false);
    const fileInput = ref(null);
    const currentImagePickerIndex = ref(null);
    const currentImagePickerMode = ref('add'); // 'add' or 'edit'
    
    const visiblePages = computed(() => {
      const pages = [];
      const start = Math.max(0, currentPage.value - 2);
      const end = Math.min(totalPages.value, start + 5);
      
      for (let i = start; i < end; i++) {
        pages.push(i + 1);
      }
      return pages;
    });
    
    const fetchProducts = async () => {
      loading.value = true;
      try {
        const params = {
          page: currentPage.value,
          size: pageSize.value,
          keyword: searchKeyword.value,
          categoryId: selectedCategory.value || undefined,
          status: selectedStatus.value || undefined
        };
        
        const response = await api.getEmployeeProducts(params);
        
        if (response.data.success) {
          products.value = response.data.products;
          stats.value = response.data.stats;
          totalElements.value = response.data.totalElements;
        } else {
          error.value = response.data.message || 'Lỗi khi tải danh sách sản phẩm';
        }
      } catch (err) {
        console.error('Error fetching products:', err);
        error.value = 'Lỗi kết nối đến server';
      } finally {
        loading.value = false;
      }
    };
    
    const fetchCategories = async () => {
      try {
        const response = await api.getEmployeeCategories();
        if (response.data.success) {
          categories.value = response.data.categories;
        }
      } catch (err) {
        console.error('Error fetching categories:', err);
      }
    };
    
    const fetchAvailableImages = async () => {
      try {
        const response = await api.getAvailableImages();
        if (response.data.success) {
          availableImages.value = response.data.images;
        }
      } catch (err) {
        console.error('Error fetching images:', err);
      }
    };
    
    const fetchProductDetail = async (maSP) => {
      try {
        const response = await api.getEmployeeProductDetail(maSP);
        if (response.data.success) {
          return response.data.data;
        }
        return null;
      } catch (err) {
        console.error('Error fetching product detail:', err);
        return null;
      }
    };
    
    const fetchProductVariants = async (maSP) => {
      try {
        const response = await api.getProductVariants(maSP);
        if (response.data.success) {
          return response.data.variants;
        }
        return [];
      } catch (err) {
        console.error('Error fetching variants:', err);
        return [];
      }
    };
    
    const handleSearch = () => {
      currentPage.value = 0;
      fetchProducts();
    };
    
    const resetFilters = () => {
      searchKeyword.value = '';
      selectedCategory.value = '';
      selectedStatus.value = '';
      currentPage.value = 0;
      fetchProducts();
    };
    
    const changePage = (page) => {
      if (page >= 0 && page < totalPages.value) {
        currentPage.value = page;
        fetchProducts();
      }
    };
    
    const showAddModal = () => {
      resetNewProduct();
      addProductModal.show();
    };
    
    const showEditModal = async (product) => {
      loading.value = true;
      try {
        const productDetail = await fetchProductDetail(product.maSP);
        if (productDetail) {
          editingProduct.value = {
            maSP: productDetail.maSP,
            tenSP: productDetail.tenSP,
            maDM: productDetail.danhMuc?.maDM || '',
            moTa: productDetail.moTa || '',
            variants: productDetail.phanLoaiSanPhams || []
          };
          editProductModal.show();
        } else {
          error.value = 'Không thể tải thông tin sản phẩm';
        }
      } catch (err) {
        console.error('Error loading product for edit:', err);
        error.value = 'Lỗi khi tải thông tin sản phẩm';
      } finally {
        loading.value = false;
      }
    };
    
    const showVariantsModal = async (product) => {
      selectedProduct.value = product;
      productVariants.value = await fetchProductVariants(product.maSP);
      variantsModal.show();
    };
    
    const showAddCategoryModal = () => {
      newCategory.value.tenDM = '';
      addCategoryModal.show();
    };
    
    const showImagePicker = (index) => {
      currentImagePickerIndex.value = index;
      currentImagePickerMode.value = 'add';
      fetchAvailableImages();
      imagePickerModal.show();
    };
    
    const showImagePickerForEdit = (index) => {
      currentImagePickerIndex.value = index;
      currentImagePickerMode.value = 'edit';
      fetchAvailableImages();
      imagePickerModal.show();
    };
    
    const addVariant = () => {
      newProduct.value.variants.push({
        phanLoai: '',
        donGia: 0,
        soLuong: 0,
        trangThai: 'Còn hàng',
        hinh: ''
      });
    };
    
    const removeVariant = (index) => {
      if (newProduct.value.variants.length > 1) {
        newProduct.value.variants.splice(index, 1);
      }
    };
    
    const addEditVariant = () => {
      editingProduct.value.variants.push({
        phanLoai: '',
        donGia: 0,
        soLuong: 0,
        trangThai: 'Còn hàng',
        hinh: ''
      });
    };
    
    const removeEditVariant = (index) => {
      if (editingProduct.value.variants.length > 1) {
        editingProduct.value.variants.splice(index, 1);
      }
    };
    
    const resetNewProduct = () => {
      newProduct.value = {
        tenSP: '',
        maDM: '',
        moTa: '',
        variants: [
          {
            phanLoai: '',
            donGia: 0,
            soLuong: 0,
            trangThai: 'Còn hàng',
            hinh: ''
          }
        ]
      };
    };
    
    const submitAddProduct = async () => {
      processing.value = true;
      try {
        if (!newProduct.value.tenSP.trim()) {
          throw new Error('Vui lòng nhập tên sản phẩm');
        }
        
        if (!newProduct.value.maDM) {
          throw new Error('Vui lòng chọn danh mục');
        }
        
        const validVariants = newProduct.value.variants.filter(v => 
          v.phanLoai.trim() && v.donGia > 0
        );
        
        if (validVariants.length === 0) {
          throw new Error('Vui lòng thêm ít nhất một phân loại hợp lệ');
        }
        
        const response = await api.createEmployeeProduct(newProduct.value);
        
        if (response.data.success) {
          message.value = 'Thêm sản phẩm thành công!';
          addProductModal.hide();
          resetNewProduct();
          fetchProducts();
          fetchCategories();
        } else {
          throw new Error(response.data.message || 'Thêm sản phẩm thất bại');
        }
      } catch (err) {
        error.value = err.message;
      } finally {
        processing.value = false;
      }
    };
    
    const submitEditProduct = async () => {
      processing.value = true;
      try {
        if (!editingProduct.value.tenSP.trim()) {
          throw new Error('Vui lòng nhập tên sản phẩm');
        }
        
        if (!editingProduct.value.maDM) {
          throw new Error('Vui lòng chọn danh mục');
        }
        
        const response = await api.updateEmployeeProduct(
          editingProduct.value.maSP, 
          editingProduct.value
        );
        
        if (response.data.success) {
          message.value = 'Cập nhật sản phẩm thành công!';
          editProductModal.hide();
          fetchProducts();
        } else {
          throw new Error(response.data.message || 'Cập nhật sản phẩm thất bại');
        }
      } catch (err) {
        error.value = err.message;
      } finally {
        processing.value = false;
      }
    };
    
    const confirmDeleteProduct = (product) => {
      if (confirm(`Bạn có chắc muốn xóa sản phẩm "${product.tenSP}"?`)) {
        deleteProduct(product.maSP);
      }
    };
    
    const deleteProduct = async (maSP) => {
      processing.value = true;
      try {
        const response = await api.deleteEmployeeProduct(maSP);
        
        if (response.data.success) {
          message.value = 'Sản phẩm đã ngừng kinh doanh!';
          fetchProducts();
        } else {
          throw new Error(response.data.message || 'Xóa sản phẩm thất bại');
        }
      } catch (err) {
        error.value = err.message;
      } finally {
        processing.value = false;
      }
    };
    
    const submitAddCategory = async () => {
      processing.value = true;
      try {
        if (!newCategory.value.tenDM.trim()) {
          throw new Error('Vui lòng nhập tên danh mục');
        }
        
        const response = await api.createEmployeeCategory(newCategory.value);
        
        if (response.data.success) {
          message.value = 'Thêm danh mục thành công!';
          addCategoryModal.hide();
          fetchCategories();
        } else {
          throw new Error(response.data.message || 'Danh mục đã tồn tại');
        }
      } catch (err) {
        error.value = err.message;
      } finally {
        processing.value = false;
      }
    };
    
    const triggerFileInput = () => {
      fileInput.value.click();
    };
    
    const handleFileSelect = (event) => {
      const file = event.target.files[0];
      if (file) {
        handleUploadFile(file);
      }
    };
    
    const handleDrop = (event) => {
      dragover.value = false;
      const file = event.dataTransfer.files[0];
      if (file) {
        handleUploadFile(file);
      }
    };
    
    const handleUploadFile = async (file) => {
      if (!file.type.startsWith('image/')) {
        error.value = 'Vui lòng chọn file ảnh';
        return;
      }
      
      if (file.size > 5 * 1024 * 1024) {
        error.value = 'Kích thước ảnh không được vượt quá 5MB';
        return;
      }
      
      const reader = new FileReader();
      reader.onload = (e) => {
        uploadPreview.value = e.target.result;
        uploadFileName.value = file.name;
      };
      reader.readAsDataURL(file);
      
      processing.value = true;
      try {
        const formData = new FormData();
        formData.append('file', file);
        
        const response = await api.uploadProductImage(formData);
        
        if (response.data.success) {
          selectedImage.value = response.data.filename;
          availableImages.value.unshift(response.data.filename);
          message.value = 'Upload ảnh thành công!';
        } else {
          throw new Error(response.data.message || 'Upload ảnh thất bại');
        }
      } catch (err) {
        error.value = err.message;
      } finally {
        processing.value = false;
        fileInput.value.value = '';
      }
    };
    
    const selectImage = (image) => {
      selectedImage.value = image;
    };
    
    const applySelectedImage = () => {
      if (selectedImage.value) {
        if (currentImagePickerMode.value === 'add') {
          newProduct.value.variants[currentImagePickerIndex.value].hinh = selectedImage.value;
        } else {
          editingProduct.value.variants[currentImagePickerIndex.value].hinh = selectedImage.value;
        }
        imagePickerModal.hide();
        selectedImage.value = '';
        uploadPreview.value = '';
        uploadFileName.value = '';
      }
    };

    const getImageUrl = (imageName) => {
      if (!imageName) return '/images/default.jpg';
      return `/images/${imageName}`;
    };
    
    const handleImageError = (event) => {
      event.target.src = '/images/default.jpg';
    };
    
    const formatPrice = (price) => {
      return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND'
      }).format(price);
    };
    
    const getStatusBadgeClass = (status) => {
      switch(status) {
        case 'Còn hàng': return 'bg-success';
        case 'Hết hàng': return 'bg-danger';
        case 'Sắp hết': return 'bg-warning';
        default: return 'bg-secondary';
      }
    };
    
    onMounted(async () => {
      await Promise.all([
        fetchProducts(),
        fetchCategories()
      ]);
      
      addProductModal = new Modal(document.getElementById('addProductModal'));
      editProductModal = new Modal(document.getElementById('editProductModal'));
      variantsModal = new Modal(document.getElementById('variantsModal'));
      addCategoryModal = new Modal(document.getElementById('addCategoryModal'));
      imagePickerModal = new Modal(document.getElementById('imagePickerModal'));
    });
    
    return {
      products,
      categories,
      loading,
      processing,
      message,
      error,
      searchKeyword,
      selectedCategory,
      selectedStatus,
      currentPage,
      totalPages,
      totalElements,
      stats,
      newProduct,
      editingProduct,
      selectedProduct,
      productVariants,
      newCategory,
      availableImages,
      selectedImage,
      uploadPreview,
      uploadFileName,
      dragover,
      fileInput,
      visiblePages,
      
      fetchProducts,
      handleSearch,
      resetFilters,
      changePage,
      showAddModal,
      showEditModal,
      showVariantsModal,
      showAddCategoryModal,
      showImagePicker,
      showImagePickerForEdit,
      addVariant,
      removeVariant,
      addEditVariant,
      removeEditVariant,
      submitAddProduct,
      submitEditProduct,
      confirmDeleteProduct,
      submitAddCategory,
      triggerFileInput,
      handleFileSelect,
      handleDrop,
      selectImage,
      applySelectedImage,
      getImageUrl,
      handleImageError,
      formatPrice,
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
  background: #ffffff;
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

.bg-gradient-1 {
  background: linear-gradient(135deg, #000000, #000000);
}

.bg-gradient-2 {
  background: linear-gradient(135deg, #000000, #000000);
}

.bg-gradient-3 {
  background: linear-gradient(135deg, #000000, #000000);
}

.bg-gradient-4 {
  background: linear-gradient(135deg, #000000, #000000);
}

/* Content Card */
.content-card {
  background: white;
  border-radius: 16px;
  padding: 30px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
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
  box-shadow: 0 6px 10px rgba(0, 0, 0, 0.12);
  transform: translateY(-3px);
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

.image-upload-area.dragover {
  border-color: #667eea;
  background: #f0f3ff;
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