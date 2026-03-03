CREATE DATABASE ShoedoShop;
GO
USE ShoedoShop;
GO

-- 1. Bảng Users
CREATE TABLE Users (
    MaUser INT IDENTITY(1,1) PRIMARY KEY,
    UserName NVARCHAR(50) UNIQUE,
    Mail NVARCHAR(100) UNIQUE,
    PassWord NVARCHAR(255),
    IsActive BIT,
    CreateAt DATETIME DEFAULT GETDATE()
);

-- 2. Bảng Khách Hàng
CREATE TABLE KhachHang (
    MaKH INT IDENTITY(1,1) PRIMARY KEY,
    TenKH NVARCHAR(100),
    SDT VARCHAR(15),
    MaUser INT,
    CONSTRAINT FK_KhachHang_User FOREIGN KEY (MaUser) REFERENCES Users(MaUser)
);

-- 3. Bảng Quản Trị
CREATE TABLE QuanTri (
    MaQT INT IDENTITY(1,1) PRIMARY KEY,
    TenQT NVARCHAR(100),
    [Role] BIT DEFAULT 0,
    MaUser INT,
    CONSTRAINT FK_QuanTri_User FOREIGN KEY (MaUser) REFERENCES Users(MaUser)
);

-- 4. Bảng Danh Mục
CREATE TABLE DanhMuc (
    MaDM INT IDENTITY(1,1) PRIMARY KEY,
    TenDM NVARCHAR(100) NOT NULL
);

-- 5. Bảng Sản Phẩm
CREATE TABLE SanPham (
    MaSP INT IDENTITY(1,1) PRIMARY KEY,
    TenSP NVARCHAR(200) NOT NULL,
    GioiTinh BIT, 
    MoTa NVARCHAR(MAX),
    KhuyenMai INT DEFAULT 0 CHECK (KhuyenMai >= 0 AND KhuyenMai <= 100),
    DaBan INT DEFAULT 0 CHECK (DaBan >= 0), -- Đã thêm dấu phẩy ở đây
    IsActive BIT DEFAULT 1
);

-- 6. Bảng Sản phẩm - Danh mục (Many-to-Many)
CREATE TABLE SanPham_DanhMuc (
    MaSP INT,
    MaDM INT,
    PRIMARY KEY (MaSP, MaDM),
    FOREIGN KEY (MaSP) REFERENCES SanPham(MaSP),
    FOREIGN KEY (MaDM) REFERENCES DanhMuc(MaDM)
);

-- 7. Bảng Size
CREATE TABLE Size (
    MaSize INT IDENTITY (1,1) PRIMARY KEY, -- Bỏ dấu phẩy thừa
    CoGiay INT UNIQUE -- 39, 40, 41
);

-- 10. Bảng Chi tiết sản phẩm (SKU - Kho hàng - Giá)
CREATE TABLE SanPham_ChiTiet (
    MaSKU INT IDENTITY(1,1) PRIMARY KEY,
    MaSP INT NOT NULL,
    TenMau NVARCHAR(50),
    HinhAnh NVARCHAR(MAX),
    MaSize INT, 
    TrangThai NVARCHAR(50),
    SoLuong INT DEFAULT 0 CHECK (SoLuong >= 0),
    DonGia DECIMAL(18,2) CHECK (DonGia > 0),
    -- Ràng buộc: 1 sản phẩm + 1 màu + 1 size = 1 SKU duy nhất
    CONSTRAINT UQ_SP_Mau_Size UNIQUE (MaSP, TenMau, MaSize),
    CONSTRAINT FK_ChiTiet_SanPham FOREIGN KEY (MaSP) REFERENCES SanPham(MaSP),
    CONSTRAINT FK_ChiTiet_Size FOREIGN KEY (MaSize) REFERENCES Size(MaSize)
);

-- 11. Bảng Nhập Kho
CREATE TABLE NhapKho (
    MaNK INT IDENTITY(1,1) PRIMARY KEY,
    MaSKU INT,
    SoLuong INT CHECK (SoLuong > 0),
    NgayNhap DATE DEFAULT GETDATE(),
    CONSTRAINT FK_NhapKho_SKU FOREIGN KEY (MaSKU) REFERENCES SanPham_ChiTiet(MaSKU)
);

-- 12. Bảng Địa Chỉ
CREATE TABLE DiaChi (
    MaDC INT IDENTITY(1,1) PRIMARY KEY,
    MaKH INT,
    MacDinh BIT DEFAULT 0,
    DiemGiao NVARCHAR(255),
    TenNN NVARCHAR(100),
    SDT VARCHAR(15),
    CONSTRAINT FK_DiaChi_KhachHang FOREIGN KEY (MaKH) REFERENCES KhachHang(MaKH)
);

-- 13. Bảng Giỏ Hàng
CREATE TABLE GioHang (
    MaGH INT IDENTITY(1,1) PRIMARY KEY,
    MaKH INT,
    MaSKU INT,
    SoLuong INT CHECK (SoLuong > 0),
    -- Ràng buộc: 1 khách chỉ có 1 dòng cho 1 sản phẩm trong giỏ.
    CONSTRAINT UQ_GioHang UNIQUE (MaKH, MaSKU),

    CONSTRAINT FK_GioHang_KhachHang FOREIGN KEY (MaKH) REFERENCES KhachHang(MaKH),
    CONSTRAINT FK_GioHang_SKU FOREIGN KEY (MaSKU) REFERENCES SanPham_ChiTiet(MaSKU)
);

-- 14. Bảng Hóa Đơn
CREATE TABLE HoaDon (
    MaHD INT IDENTITY(1,1) PRIMARY KEY,
    MaKH INT,
    MaQT INT, -- Nhân viên duyệt đơn
    PhuongThucTT NVARCHAR(50),
    DiaChiJson NVARCHAR(MAX), 
    TrangThai NVARCHAR(50) CHECK (TrangThai IN (N'Đang xử lý', N'Đang giao', N'Hoàn tất', N'Đã từ chối', N'Báo lỗi', N'Hoàn hàng/trả hàng')),
    GhiChu NVARCHAR(MAX),
    NgayMua DATETIME DEFAULT GETDATE(),
    CONSTRAINT FK_HoaDon_KhachHang FOREIGN KEY (MaKH) REFERENCES KhachHang(MaKH),
    CONSTRAINT FK_HoaDon_QuanTri FOREIGN KEY (MaQT) REFERENCES QuanTri(MaQT)
);

-- 15. Bảng Hóa Đơn Chi Tiết
CREATE TABLE HoaDonCT (
    MaHDCT INT IDENTITY(1,1) PRIMARY KEY,
    MaHD INT,
    MaSKU INT,
    SoLuong INT CHECK (SoLuong > 0),
    DonGia DECIMAL(18,2) CHECK (DonGia > 0), -- Giá tại thời điểm mua
    CONSTRAINT FK_HoaDonCT_HoaDon FOREIGN KEY (MaHD) REFERENCES HoaDon(MaHD),
    CONSTRAINT FK_HoaDonCT_SKU FOREIGN KEY (MaSKU) REFERENCES SanPham_ChiTiet(MaSKU)
);

-- 16. Bảng Đánh giá
CREATE TABLE DanhGia (
    MaDG INT IDENTITY(1,1) PRIMARY KEY,
    MaHDCT INT NOT NULL,
    Sao INT CHECK (Sao >= 1 AND Sao <= 5),
    DanhGiaCT NVARCHAR(MAX),
    NgayDG DATETIME DEFAULT GETDATE(),
    -- Ràng buộc: Mua 1 lần -> đánh giá 1 lần (Unique trên MaKH và MaHDCT)
    CONSTRAINT UQ_DanhGia_MotLan UNIQUE (MaHDCT),

    CONSTRAINT FK_DanhGia_HoaDonCT FOREIGN KEY (MaHDCT) REFERENCES HoaDonCT(MaHDCT)
);

-- 17. Bảng Tìm Kiếm
CREATE TABLE TimKiem (
    MaTK INT IDENTITY(1,1) PRIMARY KEY, 
    MaKH INT,                        
    NoiDungTimKiem NVARCHAR(225) NOT NULL, 
    -- Ràng buộc: Một User không thể có 2 dòng chứa cùng 1 từ khóa (Unique trên MaKH và NoiDungTimKiem)
    CONSTRAINT UQ_User_Keyword UNIQUE (MaKH, NoiDungTimKiem),

    ThoiGian DATETIME DEFAULT GETDATE(), -- Bổ sung thời gian tìm
    CONSTRAINT FK_TimKiem_KhachHang FOREIGN KEY (MaKH) REFERENCES KhachHang(MaKH)
);
GO

-- 1. Dữ liệu mẫu cho bảng [User]
INSERT INTO Users (UserName, Mail, PassWord, IsActive) VALUES 
('admin', 'admin@shop.com', 'PasswordEncoder', 1),
('nv1', 'nhanvien1@gmail.com', 'PasswordEncoder', 1),
('nv2', 'nhanvien2@gmail.com', 'PasswordEncoder', 1),
('nv3', 'nhanvien3@gmail.com', 'PasswordEncoder', 1),
('user1', 'user1@gmail.com', 'PasswordEncoder', 1),
('user2', 'user2@gmail.com', 'PasswordEncoder', 1),
('user3', 'user3@gmail.com', 'PasswordEncoder', 1),
('QuanTesteremail', 'nguyenhoangminhquan786@gmail.com', 'PasswordEncoder', 1);

-- 2. Dữ liệu mẫu cho bảng Khách Hàng (MaUser 5, 6, 7, 8)
INSERT INTO KhachHang (TenKH, SDT, MaUser) VALUES 
(N'Nguyễn Văn A', '0901234567', 5),
(N'Trần Thị Hi', '0912345678', 6),
(N'Lê Thị B', '0987654321', 7),
(N'Nguyễn Hoàng Minh Quân', '010100101', 8);

-- 3. Dữ liệu mẫu cho bảng Quản Trị (MaUser 1, 2, 3, 4)
INSERT INTO QuanTri (TenQT, [Role], MaUser) VALUES 
(N'Quan', 1, 1),
(N'Minh', 0, 2),
(N'Minh3D', 0, 3),
(N'Brynes', 0, 4);

-- 4. Dữ liệu mẫu cho bảng Danh Mục
INSERT INTO DanhMuc (TenDM) VALUES 
(N'Giày da'),
(N'Giày sneaker'),
(N'Giày bóng đá'),
(N'Giày sandal'),
(N'Giày boot'),
(N'Phụ Kiện');

-- 5. Dữ liệu mẫu cho bảng Sản Phẩm
INSERT INTO SanPham (TenSP, GioiTinh, MoTa, KhuyenMai, DaBan) VALUES 
--Giày da
(N'SP001-ShoeDo - SP1 - GD', 1, N'Giày tây mỏ dẹt cao cấp', 10, 5),
(N'SP002-ShoeDo - SP2 - GD', 0, N'Giày tây nữ thanh thoát cao cấp', 10, 2),
(N'SP003-ShoeDo - SP3 - GD', 0, N'Giày cao gót nữ', 30, 0),

--Giày sneaker
(N'SP004-ShoeDo - SP1 - GSK', 1, N'Êm ái trên từng bước chân', 0, 10),
(N'SP005-ShoeDo - SP2 - GSK', 1, N'Giày có khả năng chạy bộ tốt', 0, 0),

--Giày bóng đá
(N'SP006-ShoeDo - SP1 - GBD', 1, N'Giày có gai bám cỏ tốt', 5, 0),

--Giày sandal
(N'SP007-ShoeDo - SP1 - GSD', 1, N'Giày có độ thoáng mát cao', 30, 0),

--Giày boot
(N'SP008-ShoeDo - SP1 - GBT', 1, N'Giày có độ ma sát tốt chống bám bẩn cao', 0, 0),

--Giày boot & da
(N'SP009-ShoeDo - SP1 - GBT&GD', 1, N'Giày có độ ma sát tốt giữ form cao', 0, 0),

--Phụ kiện
(N'SP0010-ShoeDo - Vớ Thể Thao ShoeDo - PKvtt1', NULL, N'Thiết kế tất ngắn, Co giản tốt', 10, 100),
(N'SP0011-ShoeDo - Vớ Thể Thao ShoeDo - PKvtt2', NULL, N'Thiết kế tất cổ cao, Mịn màng cho da', 10, 20),
(N'SP0012-ShoeDo - Vớ Thể Thao ShoeDo - PKvtt3', NULL, N'Thiết kế tất trung bình, Thấm hút mồ hôi tốt', 10, 10),
(N'SP0013-ShoeDo - Dây giày - PKgd1', NULL, N'Thiết kế đan dây tinh tế nhiều màu sắt để lựa chọn', 10, 50);

INSERT INTO SanPham_DanhMuc (MaSP, MaDM) VALUES
-- Giày da
(1, 1), 
(2, 1), 
(3, 1),

-- Giày sneaker
(4, 2), 
(5, 2),

-- Giày bóng đá
(6, 3),

-- Giày sandal
(7, 4),

-- Giày boot
(8, 5),

-- Giày boot & da (1 sản phẩm - 2 danh mục)
(9, 5), 
(9, 1),

-- Phụ kiện
(10, 6), 
(11, 6), 
(12, 6), 
(13, 6);

-- 6. Dữ liệu Size
INSERT INTO Size (CoGiay) VALUES 
(0),  -- ID 1: Size 0 (Freesize - Dùng cho phụ kiện)
(37), -- ID 2: Size 37
(38), -- ID 3: Size 38
(39), -- ID 4: Size 39
(40), -- ID 5: Size 40
(41), -- ID 6: Size 41
(42), -- ID 7: Size 42
(43); -- ID 8: Size 43

-- 9.Note: Cột MaSize bây giờ điền ID của bảng Size ở trên (Ví dụ: ID 5 là size 40, ID 6 là size 41)
-- 8. Dữ liệu Chi tiết sản phẩm (đã gộp màu sắc)
INSERT INTO SanPham_ChiTiet (MaSP, TenMau, HinhAnh, MaSize, TrangThai, SoLuong, DonGia) VALUES
-- =======================
-- Giày da (MaSP 1,2,3)
-- =======================
-- SP 1: ShoeDo - SP1 - GD
(1, N'Trắng', 'sp1_trang_40.jpg', 5, N'Còn hàng', 50, 2500000), -- Size 40 (ID 5) 
(1, N'Đen', 'sp1_den_41.jpg', 6, N'Còn hàng', 30, 2500000), -- Size 41 (ID 6)

-- SP 2: ShoeDo - SP2 - GD (Nữ) 
(2, N'Đen', 'sp2_den_38.jpg', 3, N'Còn hàng', 20, 3200000), -- Size 38 (ID 3)
(2, N'Xám', 'sp2_xam_39.jpg', 4, N'Còn hàng', 15, 3200000), -- Size 39 (ID 4)

-- SP 3: ShoeDo - SP3 - GD (Cao gót)
(3, N'Đỏ', 'sp3_do_37.jpg', 2, N'Còn hàng', 10, 4500000), -- Size 37 (ID 2)
(3, N'Đen', 'sp3_den_38.jpg', 3, N'Còn hàng', 12, 4500000), -- Size 38 (ID 3)

-- =======================
-- Giày sneaker (MaSP 4,5)
-- =======================
-- SP 4: ShoeDo - SP1 - GSK
(4, N'Trắng', 'sp4_trang_41.jpg', 6, N'Còn hàng', 40, 2800000), -- Size 41 (ID 6)
(4, N'Xám', 'sp4_xam_42.jpg', 7, N'Còn hàng', 35, 2800000), -- Size 42 (ID 7)

-- SP 5: ShoeDo - SP2 - GSK
(5, N'Đen', 'sp5_den_42.jpg', 7, N'Còn hàng', 25, 3000000), -- Size 42 (ID 7)
(5, N'Xanh biển', 'sp5_xanhbien_43.jpg', 8, N'Còn hàng', 20, 3000000), -- Size 43 (ID 8)

-- =======================
-- Giày bóng đá (MaSP 6)
-- =======================
-- SP 6: ShoeDo - SP1 - GBD
(6, N'Xanh Lá', 'sp6_xanh_40.jpg', 5, N'Còn hàng', 30, 2200000), -- Size 40 (ID 5)
(6, N'Cam', 'sp6_cam_41.jpg', 6, N'Còn hàng', 25, 2200000), -- Size 41 (ID 6)

-- =======================
-- Giày sandal (MaSP 7)
-- =======================
-- SP 7: ShoeDo - SP1 - GSD
(7, N'Nâu', 'sp7_nau_40.jpg', 5, N'Còn hàng', 20, 1800000), -- Size 40 (ID 5)
(7, N'Đen', 'sp7_den_41.jpg', 6, N'Còn hàng', 15, 1800000), -- Size 41 (ID 6)

-- =======================
-- Giày boot (MaSP 8)
-- =======================
-- SP 8: ShoeDo - SP1 - GBT
(8, N'Đen', 'sp8_den_41.jpg', 6, N'Còn hàng', 18, 3500000), -- Size 41 (ID 6)
(8, N'Nâu', 'sp8_nau_42.jpg', 7, N'Còn hàng', 12, 3500000), -- Size 42 (ID 7)

-- =======================
-- Giày boot + da (MaSP 9)
-- =======================
-- SP 9: ShoeDo - SP1 - GBT&GD
(9, N'Đen bóng', 'sp9_den_41.jpg', 6, N'Còn hàng', 10, 4200000), -- Size 41 (ID 6)
(9, N'Nâu đậm', 'sp9_nau_42.jpg', 7, N'Còn hàng', 8, 4200000), -- Size 42 (ID 7)

-- =======================
-- Phụ kiện (MaSP 10 → 13)
-- =======================
-- SP 10: Vớ thể thao (Dùng ID 1 = FreeSize)
(10, N'Trắng', 'pk_vottt1.jpg', 1, N'Còn hàng', 100, 150000), -- Free size (ID 1)

-- SP 11: Vớ cổ cao
(11, N'Đen', 'pk_vottt2.jpg', 1, N'Còn hàng', 80, 170000), -- Free size (ID 1)

-- SP 12: Vớ trung bình
(12, N'Xám', 'pk_vottt3.jpg', 1, N'Còn hàng', 90, 160000), -- Free size (ID 1)

-- SP 13: Dây giày
(13, N'Đa màu', 'pk_daygiay.jpg', 1, N'Còn hàng', 200, 120000); -- Free size (ID 1)

-- 10. Dữ liệu mẫu cho bảng Nhập Kho
INSERT INTO NhapKho (MaSKU, SoLuong, NgayNhap) VALUES 
-- Giày da
(1, 30, GETDATE()), 
(2, 20, GETDATE()), 
(3, 15, GETDATE()),
(4, 15, GETDATE()), 
(5, 10, GETDATE()), 
(6, 12, GETDATE()),

-- Sneaker
(7, 25, GETDATE()), 
(8, 15, GETDATE()), 
(9, 15, GETDATE()), 
(10, 10, GETDATE()),

-- Bóng đá
(11, 20, GETDATE()), 
(12, 10, GETDATE()),

-- Sandal
(13, 12, GETDATE()), 
(14, 8, GETDATE()),

-- Boot
(15, 10, GETDATE()), 
(16, 8, GETDATE()),

-- Boot + da
(17, 6, GETDATE()), 
(18, 4, GETDATE());

-- 11. Dữ liệu mẫu cho bảng Địa Chỉ
INSERT INTO DiaChi (MaKH, MacDinh, DiemGiao, TenNN, SDT) VALUES 
(1, 1, N'123 Nguyễn Huệ A, Quận 1, TP.HCM', N'Nguyễn Văn A', '0901234567'),
(1, 0, N'123 Nguyễn Huệ B, Quận 1, TP.HCM', N'Nguyễn Văn A-B', '0901234567'),
(1, 0, N'123 Nguyễn Huệ C, Quận 1, TP.HCM', N'Nguyễn Văn A-C', '0901234567'),

(2, 1, N'789 Cách Mạng Tháng 8Z, Tân Bình', N'Trần Thị Hi A', '0912345678'),
(2, 0, N'789 Cách Mạng Tháng 8Y, Tân Bình', N'Trần Thị Hi B', '0912345678'),

(3, 1, N'456 Lê Lợi A, Quận 1, TP.HCM', N'Nguyễn Văn A', '0901234567'),
(3, 0, N'456 Lê Lợi B, Quận 1, TP.HCM', N'Nguyễn Văn A-B', '0901234567'),

(4, 1, N'Quận Cam A, Quận 1, TP.HCM', N'Nguyễn Văn A', '0901234567'),
(4, 0, N'Quận Cam B, Quận 1, TP.HCM', N'Nguyễn Văn B', '0901234567'),
(4, 0, N'Quận Cam C, Quận 1, TP.HCM', N'Nguyễn Văn C', '0901234567');

-- 12. Dữ liệu mẫu cho bảng Giỏ Hàng
INSERT INTO GioHang (MaKH, MaSKU, SoLuong) VALUES 
-- =======================
-- Khách hàng 1 (mua nhiều loại)
-- =======================
(1, 1, 1),   -- Giày da trắng 40
(1, 5, 2),   -- Giày da đỏ 37
(1, 15, 1),  -- Giày boot đen 41

-- =======================
-- Khách hàng 2 (mua ít, chọn nữ)
-- =======================
(2, 4, 1),   -- Giày da nude 39
(2, 13, 1),  -- Sandal nâu 40

-- =======================
-- Khách hàng 3 (thể thao)
-- =======================
(3, 7, 1),   -- Sneaker trắng 41
(3, 11, 2),  -- Giày bóng đá xanh lá 40
(3, 10, 1),  -- Sneaker xanh 43

-- =======================
-- Khách hàng 4 (boot + phụ kiện)
-- =======================
(4, 17, 1),  -- Boot & da đen bóng 41
(4, 18, 1),  -- Boot & da nâu đậm 42
(4, 10, 2);  -- Sneaker xanh 43

-- 13. Dữ liệu mẫu cho bảng Hóa Đơn
INSERT INTO HoaDon (MaKH, MaQT, PhuongThucTT, DiaChiJson, TrangThai, GhiChu, NgayMua) VALUES
-- HD 1
(1, 1, N'COD', 
N'{"DiemGiao":"123 Nguyễn Huệ A, Q1","TenNN":"Nguyễn Văn A","SDT":"0901234567"}', 
N'Đã từ chối', N'Đơn hàng đặt số lượng quá lớn nhân viên miễn cưỡng từ chối vì gọi không ghe máy', '2025-01-10'),

-- HD 2
(2, NULL, N'Chuyển khoản', 
N'{"DiemGiao":"789 Cách Mạng Tháng 8Z, Tân Bình","TenNN":"Trần Thị Hi A","SDT":"0912345678"}', 
N'Đang xử lý', NULL, '2025-01-11'),

-- HD 3
(3, 1, N'COD', 
N'{"DiemGiao":"456 Lê Lợi A, Q1","TenNN":"Nguyễn Văn A","SDT":"0901234567"}', 
N'Đang giao', NULL, '2025-01-12'),

-- HD 4
(4, 1, N'COD', 
N'{"DiemGiao":"Quận Cam A","TenNN":"Nguyễn Văn A","SDT":"0901234567"}', 
N'Hoàn tất', NULL, '2025-01-13'),

-- HD 5
(1, 1, N'COD', 
N'{"DiemGiao":"123 Nguyễn Huệ B","TenNN":"Nguyễn Văn A-B","SDT":"0901234567"}', 
N'Đã từ chối', N'Khách hủy đơn', '2025-01-14'),

-- HD 6
(2, 1, N'Chuyển khoản', 
N'{"DiemGiao":"789 Cách Mạng Tháng 8Y","TenNN":"Trần Thị Hi B","SDT":"0912345678"}', 
N'Báo lỗi', N'Khách hàng không nhận được hàng', '2025-01-15'),

-- HD 7
(3, 1, N'COD', 
N'{"DiemGiao":"456 Lê Lợi B","TenNN":"Nguyễn Văn A-B","SDT":"0901234567"}', 
N'Hoàn hàng/trả hàng', N'Sản phẩm không đúng mô tả', '2025-01-16'),

-- HD 8
(4, 1, N'COD', 
N'{"DiemGiao":"Quận Cam B","TenNN":"Nguyễn Văn B","SDT":"0901234567"}', 
N'Đang giao', NULL, '2025-01-17'),

-- HD 9
(1, NULL, N'COD', 
N'{"DiemGiao":"123 Nguyễn Huệ C","TenNN":"Nguyễn Văn A-C","SDT":"0901234567"}', 
N'Đang xử lý', NULL, '2025-01-18'),

-- HD 10
(2, 1, N'Chuyển khoản', 
N'{"DiemGiao":"789 Cách Mạng Tháng 8Z","TenNN":"Trần Thị Hi A","SDT":"0912345678"}', 
N'Hoàn tất', NULL, '2025-01-19');

-- 14. Dữ liệu mẫu cho bảng Hóa Đơn Chi Tiết
INSERT INTO HoaDonCT (MaHD, MaSKU, SoLuong, DonGia) VALUES
-- ===== HD 1 =====
(1, 1, 1, 2500000),   -- Giày da trắng 40
(1, 7, 1, 2800000),   -- Sneaker trắng 41
(1, 19, 2, 150000),   -- Vớ thể thao

-- ===== HD 2 =====
(2, 3, 1, 3200000),   -- Giày da nữ đen 38
(2, 14, 1, 1800000),  -- Sandal đen 41

-- ===== HD 3 =====
(3, 11, 2, 2200000),  -- Giày bóng đá xanh lá
(3, 20, 1, 170000),   -- Vớ cổ cao

-- ===== HD 4 =====
(4, 15, 1, 3500000),  -- Boot đen 41
(4, 22, 1, 120000),   -- Dây giày

-- ===== HD 5 =====
(5, 5, 1, 4500000),   -- Cao gót đỏ 37
(5, 21, 2, 160000),   -- Vớ trung bình

-- ===== HD 6 =====
(6, 8, 1, 2800000),   -- Sneaker xám 42
(6, 19, 3, 150000),   -- Vớ thể thao

-- ===== HD 7 =====
(7, 17, 1, 4200000),  -- Boot & da đen bóng
(7, 22, 1, 120000),   -- Dây giày

-- ===== HD 8 =====
(8, 9, 1, 3000000),   -- Sneaker đen 42
(8, 20, 2, 170000),   -- Vớ cổ cao

-- ===== HD 9 =====
(9, 2, 1, 2500000),   -- Giày da đen 41
(9, 13, 1, 1800000),  -- Sandal nâu 40

-- ===== HD 10 =====
(10, 16, 1, 3500000), -- Boot nâu 42
(10, 21, 2, 160000);  -- Vớ trung bình

-- 12. Dữ liệu mẫu cho bảng Đánh Giá
INSERT INTO DanhGia (MaHDCT, Sao, DanhGiaCT) VALUES 
-- Đánh giá 1: Khách 4 đánh giá đôi Boot đen size 41 thuộc HD 4
-- Giả sử MaHDCT của đôi Boot trong HD4 là 8 (dựa trên thứ tự insert bảng HoaDonCT)
(8, 5, N'Giày đẹp, da xịn, đi rất êm chân. Giao hàng nhanh!'),
-- Đánh giá 2: Khách 4 đánh giá Dây giày thuộc HD 4
-- Giả sử MaHDCT của dây giày trong HD4 là 9
(9, 4, N'Dây giày màu đẹp nhưng hơi ngắn so với mong đợi.'),

-- Đánh giá 3: Khách 2 đánh giá đôi Boot nâu size 42 thuộc HD 10
-- Giả sử MaHDCT của đôi Boot trong HD10 là 18
(18, 5, N'Mua tặng chồng, chồng rất thích. Shop đóng gói cẩn thận.'),

-- Đánh giá 4: Khách 2 đánh giá Vớ trung bình thuộc HD 10
-- Giả sử MaHDCT của Vớ trong HD10 là 19
(19, 3, N'Vớ hơi mỏng, dùng tạm được.');

-- 15. Dữ liệu mẫu cho bảng TimKiem
INSERT INTO TimKiem (MaKH, NoiDungTimKiem) VALUES 
(1, N'Giày da nam'),
(1, N'Giày tây công sở'),
(2, N'Giày sneaker nữ trắng'),
(3, N'Giày đá banh sân cỏ nhân tạo'),
(4, N'Vớ thể thao cổ ngắn');
GO 