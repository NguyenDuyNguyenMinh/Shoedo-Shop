<script setup>
// Không cần script phức tạp, chỉ thuần template và css
</script>

<template>
  <nav class="navbar navbar-expand-lg bg-black py-3 sticky-top" data-bs-theme="dark">
    <div class="container-fluid px-4 px-lg-5 d-flex align-items-center justify-content-between">
      
      <a class="navbar-brand logo-box d-flex align-items-center justify-content-center text-white" href="#">
        Logo
      </a>

      <div class="collapse navbar-collapse flex-grow-0 mx-auto d-none d-lg-block" id="navbarNav">
        <ul class="navbar-nav gap-5"> <li class="nav-item">
            <a class="nav-link text-uppercase text-white fw-light" href="#">Trang Chủ</a>
          </li>
          <li class="nav-item">
            <a class="nav-link text-uppercase text-white fw-light" href="#">Sản Phẩm</a>
          </li>
          <li class="nav-item">
            <a class="nav-link text-uppercase text-white fw-light" href="#">Chính Sách</a>
          </li>
        </ul>
      </div>

      <div class="d-flex align-items-center gap-3 right-actions">
        
        <div class="search-wrapper d-none d-md-block">
          <div class="input-group">
            <span class="input-group-text bg-black border-0 ps-3">
              <span class="text-secondary fw-light" style="font-size: 0.9rem;">Tìm Kiếm</span>
            </span>
            <input type="text" class="form-control bg-black border-0 shadow-none text-white" aria-label="Search">
            <button class="btn btn-outline-light border-0 bg-black" type="button">
              <i class="bi bi-search text-white"></i>
            </button>
          </div>
        </div>

        <a href="#" class="btn btn-icon position-relative text-white">
          <i class="bi bi-cart3 fs-5"></i>
        </a>

        <div class="user-box d-flex align-items-center gap-2 px-3 py-2 rounded-0 cursor-pointer text-white">
          <i class="bi bi-person fs-5"></i>
          <span class="fw-light">Minh</span>
          <i class="bi bi-chevron-down" style="font-size: 0.7rem;"></i>
        </div>

        <button class="navbar-toggler border-white" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav">
          <span class="navbar-toggler-icon"></span>
        </button>
      </div>

    </div>
  </nav>
</template>

<style scoped>
/* --- DARK LUXURY THEME CSS --- */

/* Nền đen tuyền */
.bg-black {
  background-color: #000000 !important;
}

/* 1. Logo Box: Viền trắng, chữ trắng */
.logo-box {
  width: 120px;
  height: 48px;
  border: 1px solid #ffffff; 
  font-weight: 500;
  letter-spacing: 2px;
  font-size: 1rem;
  transition: all 0.3s ease;
}

.logo-box:hover {
  background-color: #fff;
  color: #000 !important; /* Đảo màu khi hover */
}

/* 2. Navigation Links */
.nav-link {
  font-size: 0.9rem;
  letter-spacing: 1.5px; /* Giãn chữ tạo cảm giác cao cấp */
  position: relative;
  opacity: 0.9;
  transition: opacity 0.3s;
}

.nav-link:hover {
  opacity: 1;
}

/* Gạch chân trắng mảnh khi hover */
.nav-link::after {
  content: '';
  position: absolute;
  width: 0;
  height: 1px;
  bottom: 2px;
  left: 0;
  background-color: #fff;
  transition: width 0.4s cubic-bezier(0.25, 0.8, 0.25, 1);
}

.nav-link:hover::after {
  width: 100%;
}

/* 3. Search Bar Container */
.search-wrapper {
  width: 320px;
}

.search-wrapper .input-group {
  border: 1px solid #ffffff; /* Viền bao quanh màu trắng */
  border-radius: 8px; /* Bo góc cho ô tìm kiếm */
  overflow: hidden; /* Giữ bo góc cho phần tử con */
  height: 44px; /* Chiều cao tiêu chuẩn cho tất cả controls */
}

/* Tùy chỉnh placeholder cho input */
input::placeholder {
  color: #666 !important;
}

/* 4. Cart Icon Box */
.btn-icon {
  padding: 0 14px;
  border: 1px solid #ffffff;
  border-radius: 10px; /* Bo góc cho nút cart */
  transition: transform 0.18s ease, box-shadow 0.18s ease, background-color 0.18s ease;
  height: 48px; /* Cao hơn một chút để có cảm giác nổi */
  display: inline-flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 8px 18px rgba(0,0,0,0.45), 0 2px 6px rgba(0,0,0,0.35);
  background: linear-gradient(180deg, rgba(255,255,255,0.02), rgba(0,0,0,0.06));
}

.btn-icon:hover {
  transform: none;
  background-color: #fff;
  color: #000 !important;
}

/* Press (lift) effect */
.btn-icon:active,
.btn-icon.is-pressed {
  transform: translateY(-3px);
  box-shadow: 0 12px 24px rgba(0,0,0,0.55), 0 4px 8px rgba(0,0,0,0.4);
}

.btn-icon:focus {
  outline: none;
  box-shadow: 0 8px 18px rgba(0,0,0,0.45);
}

/* 5. User Box */
.user-box {
  border: 1px solid #ffffff;
  border-radius: 8px !important; /* Bo góc cho user box */
  transition: all 0.3s;
  height: 44px; /* Chiều cao bằng ô tìm kiếm */
  align-items: center;
}

.user-box:hover {
  background-color: #fff;
  color: #000 !important;
}

/* Make nav links and small labels bold */
.nav-link {
  font-weight: 700 !important;
}

.user-box span {
  font-weight: 700 !important;
}

.search-wrapper .input-group .input-group-text .text-secondary,
.search-wrapper .input-group .input-group-text {
  font-weight: 700 !important;
}

/* Ensure inner input and buttons fill the container height and don't round separately */
.search-wrapper .input-group > .input-group-text,
.search-wrapper .input-group > input.form-control,
.search-wrapper .input-group > .btn {
  height: 100%;
  border-radius: 0;
}

/* Responsive */
@media (max-width: 991px) {
  .search-wrapper {
    display: none;
  }
}
</style>