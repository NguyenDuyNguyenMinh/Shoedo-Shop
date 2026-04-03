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
    DiemTichLuy INT DEFAULT 0,
    MaGioiThieu VARCHAR(20) UNIQUE NOT NULL,
    MaNguoiGioiThieu VARCHAR(20),
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
    DaBan INT DEFAULT 0 CHECK (DaBan >= 0),
    IsActive BIT DEFAULT 1
);

-- 6. Bảng Sản phẩm - Danh mục
CREATE TABLE SanPham_DanhMuc (
    MaSP INT,
    MaDM INT,
    PRIMARY KEY (MaSP, MaDM),
    FOREIGN KEY (MaSP) REFERENCES SanPham(MaSP),
    FOREIGN KEY (MaDM) REFERENCES DanhMuc(MaDM)
);

-- 7. Bảng Size
CREATE TABLE Size (
    MaSize INT IDENTITY(1,1) PRIMARY KEY,
    CoGiay INT UNIQUE
);

-- 8. Bảng Chi tiết sản phẩm
CREATE TABLE SanPham_ChiTiet (
    MaSKU INT IDENTITY(1,1) PRIMARY KEY,
    MaSP INT NOT NULL,
    TenMau NVARCHAR(50),
    HinhAnh NVARCHAR(MAX),
    MaSize INT,
    TrangThai NVARCHAR(50),
    SoLuong INT DEFAULT 0 CHECK (SoLuong >= 0),
    DonGia DECIMAL(18,2) CHECK (DonGia > 0),
    CONSTRAINT UQ_SP_Mau_Size UNIQUE (MaSP, TenMau, MaSize),
    CONSTRAINT FK_ChiTiet_SanPham FOREIGN KEY (MaSP) REFERENCES SanPham(MaSP),
    CONSTRAINT FK_ChiTiet_Size FOREIGN KEY (MaSize) REFERENCES Size(MaSize)
);

-- 9. Bảng Phiếu Nhập
CREATE TABLE PhieuNhap (
    MaNK INT IDENTITY(1,1) PRIMARY KEY,
    MaSKU INT,
    SoLuong INT CHECK (SoLuong > 0),
    NgayNhap DATE DEFAULT GETDATE(),
    CONSTRAINT FK_PhieuNhap_SKU FOREIGN KEY (MaSKU) REFERENCES SanPham_ChiTiet(MaSKU)
);

-- 10. Bảng Địa Chỉ
CREATE TABLE DiaChi (
    MaDC INT IDENTITY(1,1) PRIMARY KEY,
    MaKH INT,
    MacDinh BIT DEFAULT 0,
    DiemGiao NVARCHAR(255),
    TenNN NVARCHAR(100),
    SDT VARCHAR(15),
    CONSTRAINT FK_DiaChi_KhachHang FOREIGN KEY (MaKH) REFERENCES KhachHang(MaKH)
);

-- 11. Bảng Giỏ Hàng
CREATE TABLE GioHang (
    MaGH INT IDENTITY(1,1) PRIMARY KEY,
    MaKH INT,
    MaSKU INT,
    SoLuong INT CHECK (SoLuong > 0),
    CONSTRAINT UQ_GioHang UNIQUE (MaKH, MaSKU),
    CONSTRAINT FK_GioHang_KhachHang FOREIGN KEY (MaKH) REFERENCES KhachHang(MaKH),
    CONSTRAINT FK_GioHang_SKU FOREIGN KEY (MaSKU) REFERENCES SanPham_ChiTiet(MaSKU)
);

-- 12. Bảng Voucher
CREATE TABLE Voucher (
    MaVoucher INT IDENTITY(1,1) PRIMARY KEY,
    TenVoucher NVARCHAR(100) NOT NULL,
    DiemCanDoi INT NOT NULL CHECK (DiemCanDoi > 0),
    GiaTriGiam DECIMAL(18,2),
    DonToiThieu DECIMAL(18,2) DEFAULT 0,
    SoLuong INT DEFAULT 0 CHECK (SoLuong >= 0),
    NgayBatDau DATETIME DEFAULT GETDATE(),
    NgayKetThuc DATETIME,
    IsActive BIT DEFAULT 1
);

-- 13. Bảng KhachHang_Voucher
CREATE TABLE KhachHang_Voucher (
    MaKH_VC INT IDENTITY(1,1) PRIMARY KEY,
    MaKH INT NOT NULL,
    MaVoucher INT NOT NULL,
    TrangThai NVARCHAR(50) DEFAULT N'Chưa sử dụng' CHECK (TrangThai IN (N'Chưa sử dụng', N'Đã sử dụng', N'Hết hạn')),
    NgayDoi DATETIME DEFAULT GETDATE(),
    HanSuDung DATETIME NULL,
    CONSTRAINT FK_KHVC_KhachHang FOREIGN KEY (MaKH) REFERENCES KhachHang(MaKH),
    CONSTRAINT FK_KHVC_Voucher FOREIGN KEY (MaVoucher) REFERENCES Voucher(MaVoucher)
);

-- 14. Bảng Hóa Đơn
CREATE TABLE HoaDon (
    MaHD INT IDENTITY(1,1) PRIMARY KEY,
    MaKH INT,
    MaQT INT,
    MaKH_VC INT,
    PhuongThucTT NVARCHAR(50),
    DiaChiJson NVARCHAR(MAX),
    TrangThai NVARCHAR(50) CHECK (TrangThai IN (N'Đang xử lý', N'Đang giao', N'Hoàn tất', N'Đã từ chối', N'Báo lỗi')),
    GhiChu NVARCHAR(MAX),
    NgayMua DATETIME DEFAULT GETDATE(),
    NgayDen DATETIME,
    CONSTRAINT FK_HoaDon_KhachHang FOREIGN KEY (MaKH) REFERENCES KhachHang(MaKH),
    CONSTRAINT FK_HoaDon_QuanTri FOREIGN KEY (MaQT) REFERENCES QuanTri(MaQT),
    CONSTRAINT FK_HoaDon_KhachHangVoucher FOREIGN KEY (MaKH_VC) REFERENCES KhachHang_Voucher(MaKH_VC)
);

-- 15. Bảng Hóa Đơn Chi Tiết
CREATE TABLE HoaDonCT (
    MaHDCT INT IDENTITY(1,1) PRIMARY KEY,
    MaHD INT,
    MaSKU INT,
    SoLuong INT CHECK (SoLuong > 0),
    DonGia DECIMAL(18,2) CHECK (DonGia > 0),
    MaNguoiChiaSe INT,
    CONSTRAINT FK_HoaDonCT_HoaDon FOREIGN KEY (MaHD) REFERENCES HoaDon(MaHD),
    CONSTRAINT FK_HoaDonCT_NguoiChiaSe FOREIGN KEY (MaNguoiChiaSe) REFERENCES KhachHang(MaKH),
    CONSTRAINT FK_HoaDonCT_SKU FOREIGN KEY (MaSKU) REFERENCES SanPham_ChiTiet(MaSKU)
);

-- 16. Bảng Đánh giá
CREATE TABLE DanhGia (
    MaDG INT IDENTITY(1,1) PRIMARY KEY,
    MaHDCT INT NOT NULL,
    Sao INT CHECK (Sao >= 1 AND Sao <= 5),
    DanhGiaCT NVARCHAR(MAX),
    NgayDG DATETIME DEFAULT GETDATE(),
    CONSTRAINT UQ_DanhGia_MotLan UNIQUE (MaHDCT),
    CONSTRAINT FK_DanhGia_HoaDonCT FOREIGN KEY (MaHDCT) REFERENCES HoaDonCT(MaHDCT)
);

-- 17. Bảng Tìm Kiếm
CREATE TABLE LSTimKiem (
    MaTK INT IDENTITY(1,1) PRIMARY KEY,
    MaKH INT,
    NoiDungTimKiem NVARCHAR(225) NOT NULL,
    ThoiGian DATETIME DEFAULT GETDATE(),
    CONSTRAINT FK_TimKiem_KhachHang FOREIGN KEY (MaKH) REFERENCES KhachHang(MaKH)
);

-- 18. Bảng Lịch Sử Tích Điểm
CREATE TABLE LichSuTichDiem (
    MaLS INT IDENTITY(1,1) PRIMARY KEY,
    MaKH INT NOT NULL,
    SoDiem INT,
    LoaiGiaoDich NVARCHAR(100) CHECK (LoaiGiaoDich IN (N'Mời bạn bè', N'Chia sẻ mua hàng', N'Nhập mã giới thiệu', N'Đổi voucher')),
    MaHDCT INT NULL,
    NgayGiaoDich DATETIME DEFAULT GETDATE(),
    CONSTRAINT FK_LichSuTichDiem_KhachHang FOREIGN KEY (MaKH) REFERENCES KhachHang(MaKH),
    CONSTRAINT FK_LichSuTichDiem_HoaDonCT FOREIGN KEY (MaHDCT) REFERENCES HoaDonCT(MaHDCT)
);

-- 19. Bảng Chiến Dịch
CREATE TABLE ChienDich (
    MaCD INT IDENTITY(1,1) PRIMARY KEY,
    TenChienDich NVARCHAR(255) NOT NULL,
    MaSP INT NOT NULL,
    KhuyenMaiCD INT NOT NULL,
    ThoiGianBatDau DATETIME NOT NULL,
    ThoiGianKetThuc DATETIME NOT NULL,
    TrangThai NVARCHAR(50) DEFAULT N'Đang chạy' CHECK (TrangThai IN (N'Chưa bắt đầu', N'Đang chạy', N'Đã dừng', N'Kết thúc')) NOT NULL,
    CONSTRAINT FK_ChienDich_SanPham FOREIGN KEY (MaSP) REFERENCES SanPham(MaSP)
);
GO

-- 1. Dữ liệu mẫu cho bảng [User]
INSERT INTO Users (UserName, Mail, PassWord, IsActive) VALUES
('admin', 'admin@shop.com', '$2a$10$FQi/T2Pcgc1UaMkS/8mf0uSLMzLUtjNn0Ja4YRsCj2aRDdxWBCf4K', 1),
('nv1', 'nhanvien1@gmail.com', '$2a$10$FQi/T2Pcgc1UaMkS/8mf0uSLMzLUtjNn0Ja4YRsCj2aRDdxWBCf4K', 1),
('nv2', 'nhanvien2@gmail.com', '$2a$10$FQi/T2Pcgc1UaMkS/8mf0uSLMzLUtjNn0Ja4YRsCj2aRDdxWBCf4K', 1),
('nv3', 'nhanvien3@gmail.com', '$2a$10$FQi/T2Pcgc1UaMkS/8mf0uSLMzLUtjNn0Ja4YRsCj2aRDdxWBCf4K', 1),
('user1', 'user1@gmail.com', '$2a$10$FQi/T2Pcgc1UaMkS/8mf0uSLMzLUtjNn0Ja4YRsCj2aRDdxWBCf4K', 1),
('user2', 'user2@gmail.com', '$2a$10$FQi/T2Pcgc1UaMkS/8mf0uSLMzLUtjNn0Ja4YRsCj2aRDdxWBCf4K', 1),
('user3', 'user3@gmail.com', '$2a$10$FQi/T2Pcgc1UaMkS/8mf0uSLMzLUtjNn0Ja4YRsCj2aRDdxWBCf4K', 1),
('QuanTesteremail', 'nguyenhoangminhquan786@gmail.com', '$2a$10$FQi/T2Pcgc1UaMkS/8mf0uSLMzLUtjNn0Ja4YRsCj2aRDdxWBCf4K', 1);

-- 2. Dữ liệu mẫu cho bảng Khách Hàng (MaUser 5, 6, 7, 8)
INSERT INTO KhachHang (TenKH, SDT, DiemTichLuy, MaGioiThieu, MaNguoiGioiThieu, MaUser) VALUES
(N'Nguyễn Văn A', '0901234567', 5, 'M91CAC', NULL, 5),
(N'Trần Thị Hi', '0912345678',  0, 'MVOZ9V', NULL, 6),
(N'Lê Thị B', '0987654321', 0, 'OVK32C', NULL, 7),
(N'Nguyễn Hoàng Minh Quân', '010100101', 0, 'AZHD13', 'M91CAC', 8);

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
(N'Giày Da Tây Shoedo "Gentle Derby" - GD', 1, N'Sở hữu thiết kế Derby kinh điển, mẫu giày da Tây Gentle Derby từ Shoedo là biểu tượng của sự chuyên nghiệp và phong thái tự tin. Đây là sự lựa chọn không thể thay thế cho những buổi họp quan trọng, môi trường công sở hay các sự kiện lễ nghi, giúp phái mạnh khẳng định sự chỉn chu trong mọi tình huống.', 10, 2),
(N'Giày Mary Jane Shoedo "Sweet Pearl" - GD', 0, N'Mang hơi thở của phong cách vintage pha chút hiện đại, mẫu giày Sweet Pearl từ Shoedo là lựa chọn tuyệt vời cho những cô nàng yêu thích vẻ đẹp trong sáng, nữ tính. Với thiết kế mũi tròn đáng yêu và điểm nhấn ngọc trai tinh tế, đôi giày này sẽ biến mọi outfit của bạn trở nên dịu dàng như một nàng tiểu thư.', 10, 0),
(N'Giày Cao Gót Shoedo "Classic Muse" - GD', 0, N'Mang vẻ đẹp vượt thời gian với phong cách phối màu kinh điển (Two-tone), mẫu giày cao gót mũi nhọn Classic Muse từ Shoedo là món đồ không thể thiếu để hoàn thiện diện mạo sang trọng của phái đẹp. Sự giao thoa giữa tone kem nền nã và mũi giày đen quyền lực tạo nên sức hút khó cưỡng cho mọi quý cô công sở và những tín đồ thời trang cao cấp.', 30, 2),

--Giày sneaker
(N'Giày Sneaker Thể Thao Shoedo "Urban Pulse" - GSK', NULL, N'Dòng Sneaker thể thao Urban Pulse từ Shoedo được thiết kế để bắt kịp nhịp sống sôi động của thế hệ trẻ. Đây không chỉ là một đôi giày tập luyện, mà còn là phụ kiện hoàn hảo cho những chuyến hành trình khám phá thành phố, mang lại sự cân bằng tuyệt vời giữa trọng lượng siêu nhẹ và khả năng nâng đỡ bàn chân tối ưu.', 0, 2),
(N'Giày Sneaker Shoedo "Heritage Canvas" - GSK', NULL, N'Dòng Sneaker Heritage Canvas của Shoedo là sự tôn vinh dành cho những giá trị bền vững. Với thiết kế tối giản không bao giờ lỗi mốt, đây là đôi giày "quốc dân" có thể đồng hành cùng bạn từ giảng đường, văn phòng cho đến những buổi hẹn hò cuối tuần.', 0, 0),

--Giày bóng đá
(N'Giày Đá Bóng Sân Cỏ Nhân Tạo Shoedo "Neon Strike" - GBD', 1, N'Sẵn sàng trở thành tâm điểm của mọi trận đấu với Shoedo Neon Strike. Được thiết kế cho những cầu thủ có lối chơi tốc độ và kĩ thuật, mẫu giày này không chỉ sở hữu vẻ ngoài cực kỳ cá tính mà còn tích hợp những công nghệ hỗ trợ tối đa cho việc kiểm soát bóng và bứt tốc phù hợp với sân cỏ nhân tạo.', 5, 3),

--Giày sandal
(N'Sandal Cao Gót Shoedo "Elegant Lift" - GSD', 0, N'Được thiết kế để tôn vinh nét đẹp nữ tính và hiện đại, mẫu Sandal cao gót từ Shoedo là sự lựa chọn hoàn hảo cho những quý cô yêu thích sự kết hợp giữa chiều cao ấn tượng và cảm giác thoải mái. Với tone màu trắng kem tinh khôi, đôi giày này sẵn sàng đồng hành cùng bạn từ môi trường công sở chuyên nghiệp đến những buổi tiệc tối sang trọng.', 30, 2),

--Giày boot
(N'Giày Chelsea Boot Shoedo "Soft Urban" - GBT', NULL, N'Định nghĩa lại phong cách thành thị với mẫu Boot vải cao cấp từ Shoedo. Không sử dụng dây buộc cầu kỳ, phiên bản "Soft Urban" tập trung vào những đường nét thanh thoát, mang lại sự tiện lợi tối đa cho người sử dụng mà vẫn giữ được vẻ ngoài thời thượng, tinh tế.', 0, 2),

--Giày boot & da
(N'Giày Combat Boot Nữ Shoedo "Rebel Charm" - GBT&GD', 0, N'Được thiết kế dành riêng cho những cô nàng hiện đại, yêu thích sự phá cách và không ngại thể hiện bản thân, mẫu Boot da từ Shoedo là sự kết hợp hoàn hảo giữa nét cổ điển của dòng Combat Boot và sự tinh tế trong từng đường nét đương đại. Đây chính là "vũ khí" bí mật giúp bạn nâng tầm mọi set đồ, từ dạo phố đến những buổi tiệc đêm sôi động.', 0, 1),

--Phụ kiện
(N'Vớ Cổ Cao Shoedo Daily Basic - PKv1', NULL, N'Dòng vớ Shoedo Daily Basic là món phụ kiện "phải có" trong tủ đồ của bất kỳ ai. Không cầu kỳ về họa tiết, sản phẩm tập trung hoàn toàn vào sự thoải mái và khả năng ứng dụng linh hoạt, giúp bạn dễ dàng kết hợp với mọi loại giày từ giày Tây, Sneaker cho đến giày lười.', 10, 5),
(N'Vớ Cổ Cao Shoedo "Helio Edition" - PKv2', NULL, N'Không chỉ là một món phụ kiện giữ ấm, mẫu vớ Shoedo "Helio Edition" là sự giao thoa giữa nghệ thuật đồ họa và thời trang hiện đại. Với họa tiết độc bản mang tinh thần tự do, đây là lựa chọn hàng đầu để bạn tạo điểm nhấn nổi bật khi diện cùng các mẫu Short, Sneaker hoặc quần xắn gấu.', 10, 1),
(N'Vớ Chạy Bộ Chuyên Dụng Shoedo Performance - PKv3', NULL, N'Được thiết kế dành riêng cho những vận động viên và người yêu thích chạy bộ, dòng vớ Shoedo Performance không chỉ là một phụ kiện mà là "người bạn đồng hành" giúp bảo vệ đôi chân tối đa. Với sự kết hợp giữa công nghệ dệt nén linh hoạt và khả năng kiểm soát độ ẩm, sản phẩm mang lại sự thoải mái tuyệt đối từ km đầu tiên đến khi về đích.', 10, 4),

(N'Dây Giày Tròn Shoedo Basic - PKgd1', NULL, N'Dòng dây giày tròn cơ bản từ Shoedo là giải pháp thay thế hoàn hảo cho mọi đôi giày khi dây cũ bị sờn rách hoặc bạn chỉ đơn giản muốn làm mới đôi giày của mình một cách nhanh chóng. Với thiết kế tối giản không cầu kỳ, sản phẩm chú trọng vào chất lượng thực tế và mức giá "mềm" nhất thị trường.', 10, 1),
(N'Dây Giày Dẹt Shoedo Camo - PKgd2', NULL, N'Dòng dây giày dẹt họa tiết Camo (rằn ri) từ Shoedo là phụ kiện lý tưởng dành cho những ai yêu thích phong cách Streetwear, Military hoặc muốn tạo nên một diện mạo phá cách cho đôi giày của mình. Không còn là những sợi dây đơn sắc nhàm chán, Shoedo Camo biến mỗi bước chân trở thành một tuyên ngôn về cá tính riêng biệt.', 10, 0),
(N'Dây Giày Tròn Phản Quang Shoedo - PKgd3', NULL, N'Dòng dây giày nằm trong bộ sưu tập phụ kiện cao cấp từ Shoedo, dòng dây giày tròn phản quang là sự kết hợp hoàn hảo giữa tính năng bảo vệ và yếu tố thời trang "vượt thời gian". Không chỉ đơn thuần là phụ kiện giữ form giày, đây chính là điểm nhấn giúp đôi giày của bạn tỏa sáng theo đúng nghĩa đen.', 10, 1),

-- Sản phẩm thêm
-- Giày sandal
(N'Sandal Nam Shoedo "Urban Trek" - GSD', 1, N'Dòng Sandal Urban Trek của Shoedo là sự lựa chọn "quốc dân" dành cho phái mạnh nhờ thiết kế đơn giản nhưng cực kỳ chắc chắn. Với phong cách mạnh mẽ và linh hoạt, đây là người bạn đồng hành lý tưởng cho mọi hoạt động từ đi học, đi làm cho đến những chuyến dã ngoại cuối tuần.', 0, 0),
(N'Sandal Da Nam Shoedo "Leather Comfort"', 1, N'Dòng Sandal da Leather Comfort từ Shoedo là sự lựa chọn hoàn hảo cho những quý ông tìm kiếm sự cân bằng giữa phong cách lịch sự của giày da và sự thoải mái, thông thoáng của sandal. Với chất liệu da cao cấp và những điểm nhấn kim loại tinh tế, sản phẩm mang lại vẻ ngoài chỉn chu nhưng vẫn cực kỳ phóng khoáng.', 0, 0),
(N'Sandal Thể Thao Shoedo "Adventure Pro"', 1, N'Mẫu sandal Adventure Pro của Shoedo là sự kết hợp đột phá giữa sự thông thoáng của sandal và khả năng bảo vệ toàn diện của giày thể thao. Được thiết kế dành riêng cho những người ưa vận động, thích khám phá và các hoạt động dã ngoại ngoài trời, đây là người bạn đồng hành không thể thiếu cho những chuyến trekking nhẹ, đạp xe hay đi bộ đường dài.', 0, 0),
(N'Sandal Nữ Shoedo "Slim & Bold"', 0, N'Nằm trong bộ sưu tập Xuân-Hè mới nhất của Shoedo, mẫu sandal Slim & Bold là minh chứng cho việc sự đối lập có thể tạo nên vẻ đẹp hoàn hảo. Với sự kết hợp giữa phần quai mảnh mai thanh thoát và bộ đế dày cá tính, đôi giày này mang lại diện mạo vừa nữ tính, vừa hiện đại cho những cô nàng dẫn đầu xu hướng.', 0, 0),
(N'Sandal Cao Gót Shoedo "Urban Chic"', 0, N'Đơn giản nhưng đầy tinh tế, mẫu Sandal cao gót Urban Chic từ Shoedo là món phụ kiện "must-have" dành cho những cô nàng yêu thích phong cách tối giản (Minimalism). Với thiết kế quai ngang thanh mảnh kết hợp cùng gót vuông vững chãi, đôi giày này mang lại vẻ ngoài chuyên nghiệp nhưng không kém phần quyến rũ, sẵn sàng cùng bạn tỏa sáng từ văn phòng đến những buổi hẹn tối sang trọng.', 0, 0),

-- Giày da
(N'Giày Cao Gót Shoedo "Office Essential"', 0, N'Dòng giày Office Essential của Shoedo được thiết kế để trở thành món phụ kiện không thể thiếu trong tủ đồ của phụ nữ hiện đại. Với sự kết hợp giữa phom dáng bít mũi cổ điển và phần gót vuông vững chãi, đây là đôi giày mang lại vẻ ngoài chuyên nghiệp mà vẫn đảm bảo sự thoải mái tuyệt đối cho cả ngày dài làm việc.', 0, 0),
(N'Giày Da Nam Shoedo "Iron Step"', 1, N'Vượt ra khỏi những khuôn mẫu giày Tây truyền thống, Iron Step từ Shoedo là sự pha trộn hoàn hảo giữa vẻ lịch lãm của giày Oxford và sự hầm hố của dòng Work Boots. Đây là mẫu giày dành riêng cho những quý ông muốn tìm kiếm một phong cách khác biệt, mạnh mẽ và đầy sự phá cách.', 0, 0),
(N'Giày Lười Da Nam Shoedo "Easy Luxury"', 1, N'Được thiết kế hướng tới sự tối giản và tính ứng dụng cao, mẫu giày lười Easy Luxury của Shoedo là sự lựa chọn hoàn hảo cho những quý ông bận rộn. Không cần dây buộc cầu kỳ, đôi giày này vẫn giữ trọn vẹn vẻ lịch lãm của dòng giày Tây truyền thống, giúp bạn sẵn sàng cho mọi sự kiện chỉ trong vài giây.', 0, 0),
(N'Giày Oxford Nữ Shoedo "Dark Bold"', 0, N'Nếu bạn đang tìm kiếm một phụ kiện để khẳng định cái tôi mạnh mẽ và khác biệt, mẫu giày Dark Bold từ Shoedo chính là câu trả lời. Kết hợp giữa phom dáng Oxford cổ điển và bộ đế Chunky "khổng lồ", đôi giày này mang đến vẻ đẹp vừa nổi loạn, vừa thời thượng, giúp bạn chiếm trọn mọi ánh nhìn ngay khi xuất hiện.', 0, 0),
(N'Giày Loafer Nữ Shoedo "Luxe Urban"', 0, N'Sự kết hợp hoàn mỹ giữa nét cổ điển của dòng giày lười Loafer và sự phá cách của bộ đế Chunky hiện đại, Luxe Urban từ Shoedo chính là điểm nhấn hoàn hảo cho những cô nàng yêu thích phong cách dẫn đầu xu hướng. Với chi tiết khóa kim loại cách điệu, đôi giày mang lại vẻ ngoài sang trọng nhưng không kém phần mạnh mẽ.', 0, 0),

-- Giày boot
(N'Giày Boot Nữ Shoedo "Midnight Stiletto"', 0, N'Sự kết hợp hoàn mỹ giữa vẻ đẹp sắc sảo của giày cao gót stiletto và sự ôm sát tinh tế của dòng boot cổ tất, Midnight Stiletto từ Shoedo chính là biểu tượng của sự sang trọng và quyền lực. Thiết kế này sinh ra để dành cho những phụ nữ hiện đại, muốn khẳng định phong thái tự tin và gu thời trang đẳng cấp trong những bữa tiệc tối hay các sự kiện quan trọng.', 0, 0),
(N'Giày Boot Cao Su Shoedo "Storm Proof"', NULL, N'Đừng để những cơn mưa làm gián đoạn phong cách của bạn. Mẫu boot cao su Storm Proof từ Shoedo là sự kết hợp hoàn hảo giữa tính năng chống nước tuyệt đối và phom dáng Chelsea Boot cổ điển. Đây là món phụ kiện "cứu cánh" lý tưởng cho những ngày mưa ẩm ướt nhưng vẫn đảm bảo diện mạo thời thượng, năng động cho phái đẹp.', 0, 0),

-- Giày boot & da
(N'Giày Boot Nam Shoedo "Iron Heritage"', 1, N'Lấy cảm hứng từ những đôi giày bảo hộ lao động kinh điển, mẫu boot Iron Heritage của Shoedo là biểu tượng của tinh thần quật cường và phong thái nam tính bụi bặm. Đây là món đồ không thể thiếu cho những quý ông yêu thích phong cách Outdoor, Workwear hoặc đơn giản là muốn tìm kiếm một đôi giày "nồi đồng cối đá" cho những hành trình dài.', 0, 0),
(N'Giày Ankle Boot Shoedo "Urban Glam"', 0, N'Đại diện cho vẻ đẹp của phụ nữ thành thị hiện đại, mẫu boot Urban Glam từ Shoedo mang đến sự kết hợp hoàn hảo giữa thiết kế tối giản và những chi tiết điểm nhấn tinh tế. Với phom dáng ôm gọn cổ chân và phần gót vuông vững chãi, đây là phụ kiện lý tưởng để nâng tầm phong cách cho mọi quý cô trong mùa thu đông.', 0, 0),
(N'Giày Boot Nam Shoedo "Patina Soul"', 1, N'Đại diện cho sự giao thoa giữa nét thủ công tinh xảo và phong cách Urban mạnh mẽ, mẫu boot Patina Soul của Shoedo gây ấn tượng bởi kỹ thuật đánh màu loang (Patina) đầy nghệ thuật. Đây là đôi giày dành cho những người đàn ông trân trọng những giá trị cổ điển nhưng vẫn muốn khẳng định cái tôi thời thượng và đẳng cấp.', 0, 0),

-- Giày sneaker
(N'Giày Sneaker Da Shoedo "Monogram Luxe"', 1, N'Đưa định nghĩa về giày thể thao lên một tầm cao mới, mẫu sneaker Monogram Luxe từ Shoedo là sự kết hợp hoàn hảo giữa phom dáng năng động và chất liệu da cao cấp mang hơi hướng xa xỉ. Đây không chỉ là một đôi giày, mà là phụ kiện khẳng định gu thẩm mỹ tinh tế của người đàn ông hiện đại trong mọi hoàn cảnh.', 0, 0),

(N'Giày Sneaker Shoedo "Daily Essential"', 1, N'Nằm trong dòng sản phẩm chủ đạo của Shoedo, mẫu sneaker Daily Essential là định nghĩa hoàn hảo cho sự bền bỉ và tính ứng dụng. Với phom dáng classic không bao giờ lỗi mốt, đây chính là đôi giày "phải có" trong tủ đồ của bất kỳ ai, sẵn sàng cùng bạn đi qua mọi cung đường từ sáng đến tối.', 0, 0),
(N'Giày Sneaker Shoedo "Vortex Runner"', NULL, N'Đón đầu xu hướng Dad Shoes và phong cách tương lai (Futuristic), mẫu sneaker Vortex Runner của Shoedo là sự tuyên ngôn về cá tính mạnh mẽ. Với thiết kế đế đồ sộ nhưng thanh thoát cùng những đường nét đồ họa táo bạo, đây là lựa chọn số 1 cho những ai muốn sở hữu diện mạo thời thượng và đẳng cấp.', 0, 0),
(N'Giày Sneaker Shoedo "Hybrid Classic"', 0, N'Nằm trong phân khúc cao cấp của dòng giày dạo phố, Hybrid Classic từ Shoedo gây ấn tượng bởi khả năng kết hợp bậc thầy giữa các chất liệu khác nhau trên cùng một phom dáng tối giản. Đây là đôi giày dành cho những người yêu thích sự chi tiết, chiều sâu trong thiết kế và muốn tìm kiếm một vẻ ngoài vừa cổ điển vừa hiện đại.', 0, 0),
(N'Giày Sneaker Shoedo "Scarlet Retro"', 1, N'Sẵn sàng để trở thành tâm điểm của mọi ánh nhìn với Scarlet Retro. Thuộc dòng sản phẩm phá cách dựa trên nền tảng tối giản, mẫu sneaker này từ Shoedo là sự tuyên ngôn mạnh mẽ về thời trang dành cho những tâm hồn không ngại khác biệt. Với gam màu nổi bật cùng các chi tiết kem vintage, đôi giày mang lại vẻ ngoài vừa nổi loạn, vừa thanh lịch một cách lạ kỳ.', 0, 0),
(N'Giày Sneaker Shoedo "Aero Strike"', 1, N'Mang ngôn ngữ thiết kế của tương lai, Aero Strike từ Shoedo là sự kết hợp hoàn hảo giữa thời trang ứng dụng và công nghệ hỗ trợ vận động. Điểm nhấn ấn tượng nhất chính là bộ đế rỗng cách điệu, không chỉ tạo nên vẻ ngoài đậm chất cơ khí mà còn mang lại trải nghiệm êm ái chưa từng có cho đôi chân của bạn.', 0, 0),
(N'Giày Sneaker Shoedo "Airy Blue"', NULL, N'Đúng như tên gọi, mẫu sneaker Airy Blue từ Shoedo mang đến một cảm giác tươi mới, nhẹ nhàng và thanh thoát. Với thiết kế hướng đến sự tối giản nhưng không đơn điệu, đây là người bạn đồng hành lý tưởng cho những người yêu thích sự sạch sẽ của tông trắng kết hợp với điểm nhấn màu sắc đầy năng lượng.', 0, 0),

-- Giày bóng đá
(N'Giày Đá Bóng Sân Cỏ Tự Nhiên Shoedo "Galaxy Strike"', 1, N'Lấy cảm hứng từ những tia chớp rực rỡ và năng lượng của dải ngân hà, Shoedo Galaxy Strike là "vũ khí" tối thượng dành cho những tiền đạo và tiền vệ cánh khao khát tốc độ. Với thiết kế cổ cao ôm sát và hệ thống đinh chuyên dụng, đôi giày này sẽ giúp bạn bứt tốc kinh hoàng và thực hiện những pha dứt điểm đầy uy lực trên mặt sân cỏ tự nhiên.', 0, 0),
(N'Giày Đá Bóng Sân Cỏ Nhân Tạo Shoedo "Silver Bolt"', 1, N'Hãy sẵn sàng để bứt tốc như một tia chớp bạc trên sân cỏ nhân tạo với Shoedo Silver Bolt. Được thiết kế cho những cầu thủ ưa thích lối chơi tốc độ và dứt điểm quyết đoán, mẫu giày này kết hợp hoàn hảo độ thoải mái và cảm giác từ lòng bàn chân. Mang lại sự tự tin, chắc chắn trong mỗi trận đấu.', 0, 0),
(N'Giày Đá Bóng Sân Cỏ Nhân Tạo Shoedo "Crimson Grid"', 1, N'Crimson Grid là dòng giày đá bóng được Shoedo tối ưu hóa cho những cầu thủ ưa thích lối chơi kiểm soát và ưu tiên sự chắc chắn. Với sự kết hợp giữa cấu trúc bề mặt tổ ong độc đáo và phối màu xám đen hiện đại xen lẫn sắc đỏ rực rỡ, đôi giày này không chỉ hỗ trợ tối đa kỹ năng trên sân mà còn mang đến vẻ ngoài vô cùng chuyên nghiệp.', 0, 0);

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
(7, 1),

-- Giày boot
(8, 5),

-- Giày boot & da (1 sản phẩm - 2 danh mục)
(9, 5),
(9, 1),

-- Phụ kiện
(10, 6),
(11, 6),
(12, 6),

(13, 6),
(14, 6),
(15, 6),

-- Sản phẩm thêm
-- Giày sandal
(16, 4),

(17, 4),
(17, 1),

(18, 4),

(19, 4),

(20, 4),
(20, 1),

-- Giày da
(21, 1),
(22, 1),
(23, 1),
(24, 1),
(25, 1),

-- Giày boot
(26, 5),
(27, 5),

-- Giày boot & da
(28, 5),
(28, 1),

(29, 5),
(29, 1),

(30, 5),
(30, 1),

-- Giày sneaker
(31, 1),
(31, 2),

(32, 2),
(33, 2),
(34, 2),
(35, 2),
(36, 2),
(37, 2),

-- Giày bóng đá
(38, 3),
(39, 3),
(40, 3);

-- 6. Dữ liệu Size
INSERT INTO Size (CoGiay) VALUES
(0),  -- ID 1: Size 0 (Freesize - Dùng cho phụ kiện)
(32), -- ID 2: Size 32
(33), -- ID 3: Size 33
(34), -- ID 4: Size 34
(35), -- ID 5: Size 35
(36), -- ID 6: Size 36
(37), -- ID 7: Size 37
(38), -- ID 8: Size 38
(39), -- ID 9: Size 39
(40), -- ID 10: Size 40
(41), -- ID 11: Size 41
(42), -- ID 12: Size 42
(43), -- ID 13: Size 43
(44), -- ID 14: Size 44
(45); -- ID 15: Size 45

-- 9.Note: Cột MaSize bây giờ điền ID của bảng Size ở trên (Ví dụ: ID 5 là size 40, ID 6 là size 41)
-- 8. Dữ liệu Chi tiết sản phẩm (đã gộp màu sắc)
INSERT INTO SanPham_ChiTiet (MaSP, TenMau, HinhAnh, MaSize, TrangThai, SoLuong, DonGia) VALUES
-- =======================
-- Giày da (MaSP 1,2,3)
-- =======================
-- SP 1: ShoeDo - SP1 - GD
(1, N'Đen', 'sp1_gd1_black.jpg', 10, N'Còn hàng', 10, 1000000), -- Size 40 (ID 10)
(1, N'Đen', 'sp1_gd1_black.jpg', 11, N'Còn hàng', 10, 1200000), -- Size 41 (ID 11)

-- SP 2: ShoeDo - SP2 - GD (Nữ)
(2, N'Trắng', 'sp2_gd2_white.jpg', 7, N'Còn hàng', 15, 1000000), -- Size 37 (ID 7)
(2, N'Trắng', 'sp2_gd2_white.jpg', 8, N'Còn hàng', 20, 1200000), -- Size 38 (ID 8)
(2, N'Trắng', 'sp2_gd2_white.jpg', 9, N'Còn hàng', 20, 1500000), -- Size 39 (ID 9)

-- SP 3: ShoeDo - SP3 - GD (Cao gót)
(3, N'Trắng', 'sp3_gd3_white.jpg', 7, N'Còn hàng', 10, 1000000), -- Size 37 (ID 7)
(3, N'Trắng', 'sp3_gd3_white.jpg', 8, N'Còn hàng', 15, 1200000), -- Size 38 (ID 8)
(3, N'Trắng', 'sp3_gd3_white.jpg', 9, N'Còn hàng', 10, 1300000), -- Size 39 (ID 9)

-- =======================
-- Giày sneaker (MaSP 4,5)
-- =======================
-- SP 4: ShoeDo - SP1 - GSK
(4, N'Đen', 'sp4_snk1_black.jpg', 10, N'Còn hàng', 10, 1500000), -- Size 40 (ID 10)
(4, N'Đen', 'sp4_snk1_black.jpg', 11, N'Còn hàng', 10, 1500000), -- Size 41 (ID 11)
(4, N'Đen', 'sp4_snk1_black.jpg', 12, N'Còn hàng', 10, 1500000), -- Size 42 (ID 12)
(4, N'Trắng', 'sp4_snk1_white.jpg', 10, N'Còn hàng', 10, 1300000), -- Size 40 (ID 10)
(4, N'Nâu', 'sp4_snk1_brown.jpg', 10, N'Còn hàng', 20, 1800000), -- Size 40 (ID 10)
(4, N'Nâu', 'sp4_snk1_brown.jpg', 11, N'Còn hàng', 20, 1800000), -- Size 41 (ID 11)
(4, N'Nâu', 'sp4_snk1_brown.jpg', 12, N'Còn hàng', 20, 1800000), -- Size 42 (ID 12)

-- SP 5: ShoeDo - SP2 - GSK
(5, N'Xanh lá', 'sp5_snk2_green.jpg', 10, N'Còn hàng', 20, 1500000), -- Size 40 (ID 10)
(5, N'Xanh lá', 'sp5_snk2_green.jpg', 11, N'Còn hàng', 20, 1500000), -- Size 41 (ID 11)
(5, N'Xanh lá', 'sp5_snk2_green.jpg', 12, N'Còn hàng', 20, 1500000), -- Size 42 (ID 12)
(5, N'Trắng be', 'sp5_snk2_white.jpg', 10, N'Còn hàng', 10, 1200000), -- Size 40 (ID 10)
(5, N'Trắng be', 'sp5_snk2_white.jpg', 12, N'Còn hàng', 10, 1200000), -- Size 42 (ID 12)

-- =======================
-- Giày bóng đá (MaSP 6)
-- =======================
-- SP 6: ShoeDo - SP1 - GBD
(6, N'Hồng', 'sp6_gbd1_pink.jpg', 10, N'Còn hàng', 12, 1200000), -- Size 40 (ID 10)
(6, N'Hồng', 'sp6_gbd1_pink.jpg', 11, N'Còn hàng', 20, 1200000), -- Size 41 (ID 11)
(6, N'Hồng', 'sp6_gbd1_pink.jpg', 12, N'Còn hàng', 10, 1500000), -- Size 42 (ID 12)
(6, N'Hồng', 'sp6_gbd1_pink.jpg', 13, N'Còn hàng', 10, 1500000), -- Size 43 (ID 13)
(6, N'Vàng', 'sp6_gbd1_yellow.jpg', 10, N'Còn hàng', 12, 1200000), -- Size 40 (ID 10)
(6, N'Vàng', 'sp6_gbd1_yellow.jpg', 11, N'Còn hàng', 20, 1200000), -- Size 41 (ID 11)

-- =======================
-- Giày sandal (MaSP 7)
-- =======================
-- SP 7: ShoeDo - SP1 - GSD
(7, N'Đen', 'sp7_gsd1_black.jpg', 7, N'Còn hàng', 18, 1800000), -- Size 37 (ID 7)
(7, N'Đen', 'sp7_gsd1_black.jpg', 8, N'Còn hàng', 18, 1800000), -- Size 38 (ID 8)
(7, N'Đen', 'sp7_gsd1_black.jpg', 9, N'Còn hàng', 18, 1800000), -- Size 39 (ID 9)
(7, N'Trắng', 'sp7_gsd1_white.jpg', 7, N'Còn hàng', 18, 1800000), -- Size 37 (ID 7)
(7, N'Trắng', 'sp7_gsd1_white.jpg', 8, N'Còn hàng', 18, 1800000), -- Size 38 (ID 8)
(7, N'Trắng', 'sp7_gsd1_white.jpg', 9, N'Còn hàng', 18, 1800000), -- Size 39 (ID 9)

-- =======================
-- Giày boot (MaSP 8)
-- =======================
-- SP 8: ShoeDo - SP1 - GBT
(8, N'Đen', 'sp8_b1_black.jpg', 8, N'Còn hàng', 15, 1900000), -- Size 38 (ID 8)
(8, N'Đen', 'sp8_b1_black.jpg', 10, N'Còn hàng', 15, 2000000), -- Size 40 (ID 10)
(8, N'Đen', 'sp8_b1_black.jpg', 12, N'Còn hàng', 15, 2100000), -- Size 42 (ID 12)
(8, N'Nâu', 'sp8_b1_brown.jpg', 8, N'Còn hàng', 15, 1900000), -- Size 38 (ID 8)
(8, N'Nâu', 'sp8_b1_brown.jpg', 10, N'Còn hàng', 15, 2000000), -- Size 40 (ID 10)
(8, N'Nâu', 'sp8_b1_brown.jpg', 12, N'Còn hàng', 15, 2100000), -- Size 42 (ID 12)

-- =======================
-- Giày boot + da
-- =======================
-- SP 9: ShoeDo - SP9 - GBT&GD
(9, N'Đen', 'sp9_bd1_black.jpg', 7, N'Còn hàng', 15, 1500000), -- Size 37 (ID 7)
(9, N'Đen', 'sp9_bd1_black.jpg', 8, N'Còn hàng', 15, 1500000), -- Size 38 (ID 8)
(9, N'Đen', 'sp9_bd1_black.jpg', 9, N'Còn hàng', 15, 1500000), -- Size 39 (ID 9)
(9, N'Đen', 'sp9_bd1_black.jpg', 10, N'Còn hàng', 15, 1500000), -- Size 40 (ID 10)

-- =======================
-- Phụ kiện (MaSP 10 → 15)
-- =======================
-- SP 10 - 12: Vớ (Dùng ID 1 = FreeSize)
(10, N'Đen', 'sp10_v1_black.jpg', 1, N'Còn hàng', 100, 70000), -- Free size (ID 1) 
(10, N'Trắng', 'sp10_v1_white.jpg', 1, N'Còn hàng', 100, 70000), -- Free size (ID 1)

(11, N'Đen', 'sp11_v2_black.jpg', 1, N'Còn hàng', 80, 150000), -- Free size (ID 1)
(11, N'Trắng', 'sp11_v2_white.jpg', 1, N'Còn hàng', 80, 150000), 

(12, N'Xám đen quân đội', 'sp12_v3_vip.jpg', 1, N'Còn hàng', 50, 180000), -- Free size (ID 1)

-- SP 13 - 15: Dây giày
(13, N'Đen', 'sp13_dg1_black.png', 1, N'Còn hàng', 100, 80000), -- Free size (ID 1)
(13, N'Trắng', 'sp13_dg1_white.png', 1, N'Còn hàng', 100, 80000), -- Free size (ID 1)

(14, N'Xanh biển', 'sp14_dg2_blue.png', 1, N'Còn hàng', 50, 150000), -- Free size (ID 1)
(14, N'Hồng', 'sp14_dg2_pink.png', 1, N'Còn hàng', 50, 150000), -- Free size (ID 1)
(14, N'Xanh lá', 'sp14_dg2_green.png', 1, N'Còn hàng', 50, 150000), -- Free size (ID 1)

(15, N'Đen', 'sp15_dg3_black.png', 1, N'Còn hàng', 80, 120000), -- Free size (ID 1)
(15, N'Trắng', 'sp15_dg3_white.png', 1, N'Còn hàng', 80, 120000), -- Free size (ID 1)
(15, N'Vàng', 'sp15_dg3_yellow.png', 1, N'Còn hàng', 80, 120000), -- Free size (ID 1)
(15, N'Đỏ', 'sp15_dg3_red.png', 1, N'Còn hàng', 80, 120000), -- Free size (ID 1)
(15, N'Xanh lá', 'sp15_dg3_green.png', 1, N'Còn hàng', 80, 120000), -- Free size (ID 1)

-- Sản phẩm thêm
-- Giày sandal
(16, N'Đen', 'sp16_gsd2_black.jpg', 10, N'Còn hàng', 10, 800000), -- Size 40 (ID 10)
(16, N'Đen', 'sp16_gsd2_black.jpg', 11, N'Còn hàng', 10, 850000), -- Size 41 (ID 11)
(16, N'Đen', 'sp16_gsd2_black.jpg', 12, N'Còn hàng', 10, 900000), -- Size 42 (ID 12)
(16, N'Đen', 'sp16_gsd2_black.jpg', 13, N'Còn hàng', 10, 1000000), -- Size 43 (ID 13)
(16, N'Xanh quân đội', 'sp16_gsd2_green.jpg', 10, N'Còn hàng', 10, 800000), -- Size 40 (ID 10)
(16, N'Xanh quân đội', 'sp16_gsd2_green.jpg', 11, N'Còn hàng', 10, 850000), -- Size 41 (ID 11)
(16, N'Xanh quân đội', 'sp16_gsd2_green.jpg', 12, N'Còn hàng', 10, 900000), -- Size 42 (ID 12)
(16, N'Xanh quân đội', 'sp16_gsd2_green.jpg', 13, N'Còn hàng', 10, 1000000), -- Size 43 (ID 13)

(17, N'Đen', 'sp17_gsd3_black.jpg', 10, N'Còn hàng', 10, 1000000), -- Size 40 (ID 10)
(17, N'Đen', 'sp17_gsd3_black.jpg', 11, N'Còn hàng', 10, 1100000), -- Size 41 (ID 11)
(17, N'Nâu', 'sp17_gsd3_brown.jpg', 10, N'Còn hàng', 10, 1000000), -- Size 40 (ID 10)
(17, N'Nâu', 'sp17_gsd3_brown.jpg', 11, N'Còn hàng', 10, 1100000), -- Size 41 (ID 11)
(17, N'Nâu', 'sp17_gsd3_brown.jpg', 12, N'Còn hàng', 10, 1200000), -- Size 42 (ID 12)
(17, N'Nâu', 'sp17_gsd3_brown.jpg', 13, N'Còn hàng', 10, 1300000), -- Size 43 (ID 13)

(18, N'Đen', 'sp18_gsd4_black.jpg', 10, N'Còn hàng', 10, 1500000), -- Size 40 (ID 10)
(18, N'Đen', 'sp18_gsd4_black.jpg', 11, N'Còn hàng', 10, 1800000), -- Size 41 (ID 11)
(18, N'Đen', 'sp18_gsd4_black.jpg', 12, N'Còn hàng', 10, 2000000), -- Size 42 (ID 12)
(18, N'Xanh quân đội', 'sp18_gsd4_green.jpg', 10, N'Còn hàng', 10, 1500000), -- Size 40 (ID 10)
(18, N'Xanh quân đội', 'sp18_gsd4_green.jpg', 11, N'Còn hàng', 10, 1800000), -- Size 41 (ID 11)
(18, N'Xanh quân đội', 'sp18_gsd4_green.jpg', 12, N'Còn hàng', 10, 2000000), -- Size 42 (ID 12)
(18, N'Trắng be', 'sp18_gsd4_white.jpg', 10, N'Còn hàng', 10, 1500000), -- Size 40 (ID 10)
(18, N'Trắng be', 'sp18_gsd4_white.jpg', 11, N'Còn hàng', 10, 1800000), -- Size 41 (ID 11)
(18, N'Trắng be', 'sp18_gsd4_white.jpg', 12, N'Còn hàng', 10, 2000000), -- Size 42 (ID 12)

(19, N'Đen', 'sp19_gsd5_black.jpg', 7, N'Còn hàng', 10, 1000000), -- Size 37 (ID 7)
(19, N'Đen', 'sp19_gsd5_black.jpg', 8, N'Còn hàng', 10, 1200000), -- Size 38 (ID 8)
(19, N'Đen', 'sp19_gsd5_black.jpg', 9, N'Còn hàng', 10, 1500000), -- Size 39 (ID 9)
(19, N'Trắng be', 'sp19_gsd5_white.jpg', 7, N'Còn hàng', 10, 1000000), -- Size 37 (ID 7)
(19, N'Trắng be', 'sp19_gsd5_white.jpg', 8, N'Còn hàng', 10, 1200000), -- Size 38 (ID 8)
(19, N'Trắng be', 'sp19_gsd5_white.jpg', 9, N'Còn hàng', 10, 1500000), -- Size 39 (ID 9)

(20, N'Đen', 'sp20_gsd6_black.jpg', 7, N'Còn hàng', 10, 1000000), -- Size 37 (ID 7)
(20, N'Đen', 'sp20_gsd6_black.jpg', 8, N'Còn hàng', 10, 1200000), -- Size 38 (ID 8)
(20, N'Đen', 'sp20_gsd6_black.jpg', 9, N'Còn hàng', 10, 1500000), -- Size 39 (ID 9)
(20, N'Trắng', 'sp20_gsd6_white.jpg', 7, N'Còn hàng', 10, 1000000), -- Size 37 (ID 7)
(20, N'Trắng', 'sp20_gsd6_white.jpg', 8, N'Còn hàng', 10, 1200000), -- Size 38 (ID 8)
(20, N'Trắng', 'sp20_gsd6_white.jpg', 9, N'Còn hàng', 10, 1500000), -- Size 39 (ID 9)

-- Giày da
(21, N'Đen', 'sp21_gd4_black.jpg', 7, N'Còn hàng', 10, 1000000), -- Size 37 (ID 7)
(21, N'Đen', 'sp21_gd4_black.jpg', 8, N'Còn hàng', 10, 1200000), -- Size 38 (ID 8)
(21, N'Đen', 'sp21_gd4_black.jpg', 9, N'Còn hàng', 10, 1500000), -- Size 39 (ID 9)
(21, N'Trắng be', 'sp21_gd4_white.jpg', 7, N'Còn hàng', 10, 1000000), -- Size 37 (ID 7)
(21, N'Trắng be', 'sp21_gd4_white.jpg', 8, N'Còn hàng', 10, 1200000), -- Size 38 (ID 8)
(21, N'Trắng be', 'sp21_gd4_white.jpg', 9, N'Còn hàng', 10, 1500000), -- Size 39 (ID 9)

(22, N'Đen', 'sp22_gd5_black.jpg', 11, N'Còn hàng', 10, 1500000), -- Size 41 (ID 11)
(22, N'Đen', 'sp22_gd5_black.jpg', 12, N'Còn hàng', 10, 1580000), -- Size 42 (ID 12)
(22, N'Nâu', 'sp22_gd5_brown.jpg', 11, N'Còn hàng', 10, 1500000), -- Size 41 (ID 11)
(22, N'Nâu', 'sp22_gd5_brown.jpg', 12, N'Còn hàng', 10, 1580000), -- Size 42 (ID 12)

(23, N'Đen', 'sp23_gd6_black.jpg', 11, N'Còn hàng', 10, 1000000), -- Size 41 (ID 11)
(23, N'Đen', 'sp23_gd6_black.jpg', 12, N'Còn hàng', 10, 1100000), -- Size 42 (ID 12)
(23, N'Nâu', 'sp23_gd6_brown.jpg', 10, N'Còn hàng', 10, 900000), -- Size 40 (ID 10)
(23, N'Nâu', 'sp23_gd6_brown.jpg', 11, N'Còn hàng', 10, 1000000), -- Size 41 (ID 11)
(23, N'Nâu', 'sp23_gd6_brown.jpg', 12, N'Còn hàng', 10, 1100000), -- Size 42 (ID 12)

(24, N'Đen', 'sp24_gd7_black.jpg', 8, N'Còn hàng', 10, 1500000), -- Size 38 (ID 8)
(24, N'Đen', 'sp24_gd7_black.jpg', 9, N'Còn hàng', 10, 1700000), -- Size 39 (ID 9)
(24, N'Đen', 'sp24_gd7_black.jpg', 10, N'Còn hàng', 10, 1800000), -- Size 40 (ID 10)

(25, N'Đen', 'sp25_gd8_black.jpg', 7, N'Còn hàng', 10, 1200000), -- Size 37 (ID 7)
(25, N'Đen', 'sp25_gd8_black.jpg', 8, N'Còn hàng', 10, 1400000), -- Size 38 (ID 8)
(25, N'Đen', 'sp25_gd8_black.jpg', 9, N'Còn hàng', 10, 1600000), -- Size 39 (ID 9)
(25, N'Trắng be', 'sp25_gd8_white.jpg', 7, N'Còn hàng', 10, 1200000), -- Size 37 (ID 7)
(25, N'Trắng be', 'sp25_gd8_white.jpg', 8, N'Còn hàng', 10, 1400000), -- Size 38 (ID 8)
(25, N'Trắng be', 'sp25_gd8_white.jpg', 9, N'Còn hàng', 10, 1600000), -- Size 39 (ID 9)

-- Giày boot
(26, N'Xám', 'sp26_b2_black.jpg', 8, N'Còn hàng', 5, 1800000), -- Size 38 (ID 8)
(26, N'Xám', 'sp26_b2_black.jpg', 9, N'Còn hàng', 5, 2000000), -- Size 39 (ID 9)
(26, N'Xám', 'sp26_b2_black.jpg', 10, N'Còn hàng', 5, 2200000), -- Size 40 (ID 10)

(27, N'Trắng be', 'sp27_b3_white.jpg', 8, N'Còn hàng', 10, 500000), -- Size 38 (ID 8)
(27, N'Trắng be', 'sp27_b3_white.jpg', 9, N'Còn hàng', 10, 600000), -- Size 39 (ID 9)
(27, N'Trắng be', 'sp27_b3_white.jpg', 10, N'Còn hàng', 10, 700000), -- Size 40 (ID 10)
(27, N'Trắng be', 'sp27_b3_white.jpg', 11, N'Còn hàng', 10, 800000), -- Size 41 (ID 11)
(27, N'Trắng be', 'sp27_b3_white.jpg', 12, N'Còn hàng', 10, 900000), -- Size 42 (ID 12)

-- Giày boot & da
(28, N'Nâu', 'sp28_bd2_brown.jpg', 10, N'Còn hàng', 5, 1500000), -- Size 40 (ID 10)
(28, N'Nâu', 'sp28_bd2_brown.jpg', 11, N'Còn hàng', 5, 1800000), -- Size 41 (ID 11)
(28, N'Nâu', 'sp28_bd2_brown.jpg', 12, N'Còn hàng', 5, 2000000), -- Size 42 (ID 12)

(29, N'Đen', 'sp29_bd3_black.jpg', 9, N'Còn hàng', 10, 2100000), -- Size 39 (ID 9)
(29, N'Nâu', 'sp29_bd3_brown.jpg', 7, N'Còn hàng', 10, 1700000), -- Size 37 (ID 7)
(29, N'Nâu', 'sp29_bd3_brown.jpg', 8, N'Còn hàng', 10, 1900000), -- Size 38 (ID 8)
(29, N'Nâu', 'sp29_bd3_brown.jpg', 9, N'Còn hàng', 10, 2100000), -- Size 39 (ID 9)

(30, N'Nâu đậm', 'sp30_bd4_brown.jpg', 10, N'Còn hàng', 10, 1500000), -- Size 40 (ID 10)
(30, N'Nâu đậm', 'sp30_bd4_brown.jpg', 11, N'Còn hàng', 10, 1650000), -- Size 41 (ID 11)
(30, N'Nâu đậm', 'sp30_bd4_brown.jpg', 12, N'Còn hàng', 10, 1780000), -- Size 42 (ID 12)

-- Giày sneaker
(31, N'Đen', 'sp31_snk2_black.jpg', 10, N'Còn hàng', 5, 1000000), -- Size 40 (ID 10)
(31, N'Đen', 'sp31_snk2_black.jpg', 11, N'Còn hàng', 5, 1250000), -- Size 41 (ID 11)
(31, N'Đen', 'sp31_snk2_black.jpg', 12, N'Còn hàng', 5, 1500000), -- Size 42 (ID 12)

(32, N'Đen', 'sp32_snk3_black.jpg', 10, N'Còn hàng', 10, 1000000), -- Size 40 (ID 10)
(32, N'Đen', 'sp32_snk3_black.jpg', 11, N'Còn hàng', 10, 1200000), -- Size 41 (ID 11)
(32, N'Đen', 'sp32_snk3_black.jpg', 12, N'Còn hàng', 10, 1400000), -- Size 42 (ID 12)
(32, N'Trắng be nhạt', 'sp32_snk3_white.jpg', 10, N'Còn hàng', 10, 1000000), -- Size 40 (ID 10)
(32, N'Trắng be nhạt', 'sp32_snk3_white.jpg', 11, N'Còn hàng', 10, 1200000), -- Size 41 (ID 11)
(32, N'Trắng be nhạt', 'sp32_snk3_white.jpg', 12, N'Còn hàng', 10, 1400000), -- Size 42 (ID 12)

(33, N'Trắng', 'sp33_snk4_be.jpg', 10, N'Còn hàng', 10, 1000000), -- Size 40 (ID 10)
(33, N'Trắng', 'sp33_snk4_be.jpg', 11, N'Còn hàng', 10, 1200000), -- Size 41 (ID 11)
(33, N'Trắng', 'sp33_snk4_be.jpg', 12, N'Còn hàng', 10, 1400000), -- Size 42 (ID 12)
(33, N'Trắng be', 'sp33_snk4_white.jpg', 10, N'Còn hàng', 10, 1000000), -- Size 40 (ID 10)
(33, N'Trắng be', 'sp33_snk4_white.jpg', 11, N'Còn hàng', 10, 1200000), -- Size 41 (ID 11)
(33, N'Trắng be', 'sp33_snk4_white.jpg', 12, N'Còn hàng', 10, 1400000), -- Size 42 (ID 12)

(34, N'Đen', 'sp34_snk5_black.jpg', 10, N'Còn hàng', 10, 1200000), -- Size 40 (ID 10)
(34, N'Đen', 'sp34_snk5_black.jpg', 11, N'Còn hàng', 10, 1500000), -- Size 41 (ID 11)
(34, N'Trắng be', 'sp34_snk5_white.jpg', 10, N'Còn hàng', 10, 1200000), -- Size 40 (ID 10)
(34, N'Trắng be', 'sp34_snk5_white.jpg', 11, N'Còn hàng', 10, 1500000), -- Size 41 (ID 11)

(35, N'Đen', 'sp35_snk6_black.jpg', 9, N'Còn hàng', 10, 1200000), -- Size 39 (ID 9)
(35, N'Đen', 'sp35_snk6_black.jpg', 10, N'Còn hàng', 10, 1400000), -- Size 40 (ID 10)
(35, N'Đen', 'sp35_snk6_black.jpg', 11, N'Còn hàng', 10, 1500000), -- Size 41 (ID 11)
(35, N'Đỏ', 'sp35_snk6_red.jpg', 9, N'Còn hàng', 10, 1200000), -- Size 39 (ID 9)
(35, N'Đỏ', 'sp35_snk6_red.jpg', 11, N'Còn hàng', 10, 1200000), -- Size 41 (ID 11)

(36, N'Nâu', 'sp36_snk7_brown.jpg', 10, N'Còn hàng', 10, 1700000), -- Size 40 (ID 10)
(36, N'Nâu', 'sp36_snk7_brown.jpg', 11, N'Còn hàng', 10, 1900000), -- Size 41 (ID 11)
(36, N'Nâu', 'sp36_snk7_brown.jpg', 12, N'Còn hàng', 10, 2100000), -- Size 42 (ID 12)

(37, N'Trắng xanh', 'sp37_snk8_white.jpg', 8, N'Còn hàng', 10, 1300000), -- Size 38 (ID 8)
(37, N'Trắng xanh', 'sp37_snk8_white.jpg', 9, N'Còn hàng', 10, 1500000), -- Size 39 (ID 9)
(37, N'Trắng xanh', 'sp37_snk8_white.jpg', 10, N'Còn hàng', 10, 1700000), -- Size 40 (ID 10)
(37, N'Trắng xanh', 'sp37_snk8_white.jpg', 11, N'Còn hàng', 10, 1900000), -- Size 41 (ID 11)
(37, N'Trắng xanh', 'sp37_snk8_white.jpg', 12, N'Còn hàng', 10, 2100000), -- Size 42 (ID 12)

-- Giày bóng đá
(38, N'Đen đỏ', 'sp38_gbd2_black.jpg', 10, N'Còn hàng', 10, 1500000), -- Size 40 (ID 10)
(38, N'Đen đỏ', 'sp38_gbd2_black.jpg', 11, N'Còn hàng', 10, 1600000), -- Size 41 (ID 11)
(38, N'Đen đỏ', 'sp38_gbd2_black.jpg', 12, N'Còn hàng', 10, 1700000), -- Size 42 (ID 12)
(38, N'Đen đỏ', 'sp38_gbd2_black.jpg', 13, N'Còn hàng', 10, 1800000), -- Size 43 (ID 13)
(38, N'Trắng xanh', 'sp38_gbd2_white.jpg', 10, N'Còn hàng', 10, 1500000), -- Size 40 (ID 10)
(38, N'Trắng xanh', 'sp38_gbd2_white.jpg', 11, N'Còn hàng', 10, 1600000), -- Size 41 (ID 11)

(39, N'Đen bạc', 'sp39_gbd3_black.jpg', 12, N'Còn hàng', 10, 1400000), -- Size 42 (ID 12)
(39, N'Đen bạc', 'sp39_gbd3_black.jpg', 13, N'Còn hàng', 10, 1500000), -- Size 43 (ID 13)
(39, N'Xanh nhạt', 'sp39_gbd3_blue.jpg', 10, N'Còn hàng', 10, 1200000), -- Size 40 (ID 10)
(39, N'Xanh nhạt', 'sp39_gbd3_blue.jpg', 13, N'Còn hàng', 10, 1500000), -- Size 43 (ID 13)
(39, N'Xanh navy', 'sp39_gbd3_bluenavy.jpg', 10, N'Còn hàng', 10, 1200000), -- Size 40 (ID 10)
(39, N'Xanh navy', 'sp39_gbd3_bluenavy.jpg', 11, N'Còn hàng', 10, 1300000), -- Size 41 (ID 11)
(39, N'Xanh navy', 'sp39_gbd3_bluenavy.jpg', 12, N'Còn hàng', 10, 1400000), -- Size 42 (ID 12)
(39, N'Xanh navy', 'sp39_gbd3_bluenavy.jpg', 13, N'Còn hàng', 10, 1500000), -- Size 43 (ID 13)
(39, N'Xanh lá chuối', 'sp39_gbd3_green.jpg', 10, N'Còn hàng', 10, 1200000), -- Size 40 (ID 10)
(39, N'Xanh lá chuối', 'sp39_gbd3_green.jpg', 11, N'Còn hàng', 10, 1300000), -- Size 41 (ID 11)
(39, N'Xanh lá chuối', 'sp39_gbd3_green.jpg', 12, N'Còn hàng', 10, 1400000), -- Size 42 (ID 12)
(39, N'Xanh lá chuối', 'sp39_gbd3_green.jpg', 13, N'Còn hàng', 10, 1500000), -- Size 43 (ID 13)
(39, N'Cam đen', 'sp39_gbd3_orange.jpg', 10, N'Còn hàng', 10, 1200000), -- Size 40 (ID 10)
(39, N'Cam đen', 'sp39_gbd3_orange.jpg', 12, N'Còn hàng', 10, 1400000), -- Size 42 (ID 12)
(39, N'Cam đen', 'sp39_gbd3_orange.jpg', 13, N'Còn hàng', 10, 1500000), -- Size 43 (ID 13)

(40, N'Đen xám', 'sp40_gbd4_black.jpg', 10, N'Còn hàng', 10, 1200000), -- Size 40 (ID 10)
(40, N'Đen xám', 'sp40_gbd4_black.jpg', 13, N'Còn hàng', 10, 1500000), -- Size 43 (ID 13)
(40, N'Xanh nhạt', 'sp40_gbd4_blue.jpg', 10, N'Còn hàng', 10, 1200000), -- Size 40 (ID 10)
(40, N'Xanh nhạt', 'sp40_gbd4_blue.jpg', 11, N'Còn hàng', 10, 1300000), -- Size 41 (ID 11)
(40, N'Xanh nhạt', 'sp40_gbd4_blue.jpg', 12, N'Còn hàng', 10, 1400000), -- Size 42 (ID 12)
(40, N'Xanh nhạt', 'sp40_gbd4_blue.jpg', 13, N'Còn hàng', 10, 1500000), -- Size 43 (ID 13)
(40, N'Xanh navy', 'sp40_gbd4_bluenavy.jpg', 12, N'Còn hàng', 10, 1400000), -- Size 42 (ID 12)
(40, N'Xanh navy', 'sp40_gbd4_bluenavy.jpg', 13, N'Còn hàng', 10, 1500000), -- Size 43 (ID 13)
(40, N'Cam bạc', 'sp40_gbd4_silver.jpg', 10, N'Còn hàng', 10, 1200000), -- Size 40 (ID 10)
(40, N'Cam bạc', 'sp40_gbd4_silver.jpg', 11, N'Còn hàng', 10, 1300000), -- Size 41 (ID 11)
(40, N'Cam bạc', 'sp40_gbd4_silver.jpg', 13, N'Còn hàng', 10, 1500000), -- Size 43 (ID 13)
(40, N'Trắng xanh', 'sp40_gbd4_white.jpg', 11, N'Còn hàng', 10, 1300000), -- Size 41 (ID 11)
(40, N'Trắng xanh', 'sp40_gbd4_white.jpg', 12, N'Còn hàng', 10, 1400000), -- Size 42 (ID 12)
(40, N'Trắng xanh', 'sp40_gbd4_white.jpg', 13, N'Còn hàng', 10, 1500000); -- Size 43 (ID 13)

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

INSERT INTO Voucher (TenVoucher, DiemCanDoi, GiaTriGiam, DonToiThieu, SoLuong, NgayBatDau, NgayKetThuc, IsActive) VALUES
-- Voucher 10,000đ
(N'Giảm 10K cho đơn từ 200K', 10, 10000, 200000, 100, '2026-01-01', '2026-12-31', 1),

-- Voucher 20,000đ
(N'Giảm 20K cho đơn từ 300K', 20, 20000, 300000, 80, '2026-01-01', '2026-12-31', 1),

-- Voucher 50,000đ
(N'Giảm 50K cho đơn từ 500K', 50, 50000, 500000, 50, '2026-01-01', '2026-12-31', 1),

-- Voucher 100,000đ
(N'Giảm 100K cho đơn từ 1,000,000đ', 100, 100000, 1000000, 30, '2026-01-01', '2026-12-31', 1),

-- Thêm một số voucher đặc biệt
(N'Giảm 15K cho đơn từ 250K', 12, 15000, 250000, 90, '2026-02-01', '2026-12-31', 1),
(N'Giảm 30K cho đơn từ 400K', 30, 30000, 400000, 60, '2026-02-01', '2026-12-31', 1),
(N'Giảm 70K cho đơn từ 800K', 75, 70000, 800000, 35, '2026-02-01', '2026-12-31', 1),
(N'Giảm 150K cho đơn từ 2,000,000đ', 150, 150000, 2000000, 15, '2026-01-01', '2026-12-31', 1),

-- Voucher theo mùa (có thời hạn ngắn)
(N'FLASH SALE - Giảm 20K', 15, 20000, 200000, 100, '2026-03-01', '2026-06-30', 1),
(N'FLASH SALE - Giảm 50K', 40, 50000, 500000, 50, '2026-03-01', '2026-06-30', 1),
(N'BLACK FRIDAY - Giảm 100K', 80, 100000, 800000, 100, '2026-03-01', '2026-6-30', 1);

-- 13. Dữ liệu mẫu cho bảng Hóa Đơn
INSERT INTO HoaDon (MaKH, MaQT, MaKH_VC, PhuongThucTT, DiaChiJson, TrangThai, GhiChu, NgayMua, NgayDen) VALUES
-- HD 1
(1, 1, NULL, N'COD',
 N'{"DiemGiao":"123 Nguyễn Huệ A, Q1","TenNN":"Nguyễn Văn A","SDT":"0901234567"}',
 N'Đã từ chối', N'Đơn hàng đặt số lượng quá lớn nhân viên miễn cưỡng từ chối vì gọi không ghe máy', '2026-01-10', NULL),

-- HD 2
(2, NULL, NULL, N'VNPAY',
 N'{"DiemGiao":"789 Cách Mạng Tháng 8Z, Tân Bình","TenNN":"Trần Thị Hi A","SDT":"0912345678"}',
 N'Đang xử lý', NULL, '2026-01-11', NULL),

-- HD 3
(3, 1, NULL, N'COD',
 N'{"DiemGiao":"456 Lê Lợi A, Q1","TenNN":"Nguyễn Văn A","SDT":"0901234567"}',
 N'Đang giao', NULL, '2026-01-12', NULL),

-- HD 4
(4, 1, NULL, N'COD',
 N'{"DiemGiao":"Quận Cam A","TenNN":"Nguyễn Văn A","SDT":"0901234567"}',
 N'Hoàn tất', NULL, '2026-01-13', '2026-03-25'),

-- HD 5
(1, 1, NULL, N'COD',
 N'{"DiemGiao":"123 Nguyễn Huệ B","TenNN":"Nguyễn Văn A-B","SDT":"0901234567"}',
 N'Đã từ chối', N'Khách hủy đơn', '2026-01-14', NULL),

-- HD 6
(2, 1, NULL, N'VNPAY',
 N'{"DiemGiao":"789 Cách Mạng Tháng 8Y","TenNN":"Trần Thị Hi B","SDT":"0912345678"}',
 N'Báo lỗi', N'Khách hàng không nhận được hàng', '2026-01-15', '2026-01-19'),

-- HD 7
(3, 1, NULL, N'COD',
 N'{"DiemGiao":"456 Lê Lợi B","TenNN":"Nguyễn Văn A-B","SDT":"0901234567"}',
 N'Báo lỗi', N'Mũi giày bị móp', '2026-01-16', '2026-01-20'),

-- HD 8
(4, 1, NULL, N'COD',
 N'{"DiemGiao":"Quận Cam B","TenNN":"Nguyễn Văn B","SDT":"0901234567"}',
 N'Đang giao', NULL, '2026-01-17', NULL),

-- HD 9
(1, NULL, NULL, N'COD',
 N'{"DiemGiao":"123 Nguyễn Huệ C","TenNN":"Nguyễn Văn A-C","SDT":"0901234567"}',
 N'Đang xử lý', NULL, '2026-01-18', NULL),

-- HD 10
(2, 1, NULL, N'VNPAY',
 N'{"DiemGiao":"789 Cách Mạng Tháng 8Z","TenNN":"Trần Thị Hi A","SDT":"0912345678"}',
 N'Hoàn tất', NULL, '2026-01-19', '2026-01-24');

-- 14. Dữ liệu mẫu cho bảng Hóa Đơn Chi Tiết 
INSERT INTO HoaDonCT (MaHD, MaSKU, SoLuong, DonGia, MaNguoiChiaSe) VALUES
-- ===== HD 1 =====
-- Giày da đen 40: Giá gốc 1,000,000 - KM 10% = 900,000
(1, 1, 1, 900000, NULL),
-- Giày bóng đá hồng 41: Giá gốc 1,200,000 - KM 5% = 1,140,000
(1, 25, 1, 1140000, NULL),
-- Vớ đen: Giá gốc 70,000 - KM 10% = 63,000
(1, 48, 2, 63000, NULL),

-- ===== HD 2 =====
-- Giày cao gót trắng 38: Giá gốc 1,200,000 - KM 30% = 840,000
(2, 7, 1, 840000, NULL),
-- Sandal đen 37: Giá gốc 1,800,000 - KM 30% = 1,260,000
(2, 32, 1, 1260000, NULL),

-- ===== HD 3 =====
-- Giày bóng đá vàng 42: Giá gốc 1,500,000 - KM 5% = 1,425,000
(3, 30, 2, 1425000, NULL),
-- Vớ cổ cao Helio trắng: Giá gốc 150,000 - KM 10% = 135,000
(3, 51, 1, 135000, NULL),

-- ===== HD 4 =====
-- Boot đen 40: Giá gốc 2,000,000 - KM 0% = 2,000,000
(4, 39, 1, 2000000, NULL),
-- Dây giày tròn phản quang đen: Giá gốc 120,000 - KM 10% = 108,000
(4, 58, 1, 108000, NULL),

-- ===== HD 5 =====
-- Giày cao gót trắng 37: Giá gốc 1,000,000 - KM 30% = 700,000
(5, 3, 1, 700000, NULL),
-- Vớ chạy bộ Performance: Giá gốc 180,000 - KM 10% = 162,000
(5, 52, 2, 162000, NULL),

-- ===== HD 6 =====
-- Sneaker đen 42: Giá gốc 1,500,000 - KM 0% = 1,500,000
(6, 11, 1, 1500000, NULL),
-- Vớ trắng: Giá gốc 70,000 - KM 10% = 63,000
(6, 49, 3, 63000, NULL),

-- ===== HD 7 =====
-- Boot combat đen 38: Giá gốc 1,500,000 - KM 0% = 1,500,000
(7, 45, 1, 1500000, NULL),
-- Dây giày tròn basic đen: Giá gốc 80,000 - KM 10% = 72,000
(7, 53, 1, 72000, NULL),

-- ===== HD 8  =====
-- Sneaker trắng 40: Giá gốc 1,300,000 - KM 0% = 1,300,000
(8, 12, 1, 1300000, NULL),
-- Vớ cổ cao Helio đen: Giá gốc 150,000 - KM 10% = 135,000
(8, 50, 2, 135000, NULL),

-- ===== HD 9 =====
-- Giày da đen 41: Giá gốc 1,200,000 - KM 10% = 1,080,000
(9, 2, 1, 1080000, NULL),
-- Sandal trắng 38: Giá gốc 1,800,000 - KM 30% = 1,260,000
(9, 36, 1, 1260000, NULL),

-- ===== HD 10 =====
-- Boot nâu 42: Giá gốc 2,100,000 - KM 0% = 2,100,000
(10, 43, 1, 2100000, NULL),       
-- Vớ chạy bộ Performance: Giá gốc 180,000 - KM 10% = 162,000
(10, 52, 2, 162000, NULL);

-- 12. Dữ liệu mẫu cho bảng Đánh Giá
INSERT INTO DanhGia (MaHDCT, Sao, DanhGiaCT) VALUES

-- ===== ĐÁNH GIÁ CỦA KHÁCH HÀNG 4 (MaKH = 4) - Hóa đơn HD4 =====
-- HD4 có 2 dòng:
-- Dòng 1: Boot đen 40 (MaSKU 36) - MaHDCT = 7 (vì insert từ trên xuống)
-- Dòng 2: Dây giày phản quang đen (MaSKU 50) - MaHDCT = 8
(8, 5, N'Giày boot đẹp, da mềm, đi rất êm chân. Giao hàng nhanh, đóng gói cẩn thận!'),
(9, 4, N'Dây giày phản quang đẹp, nhưng hơi ngắn so với mong đợi. Chất lượng ổn.'),

-- ===== ĐÁNH GIÁ CỦA KHÁCH HÀNG 2 (MaKH = 2) - Hóa đơn HD2 và HD10 =====
-- HD2 có 2 dòng:
-- Dòng 1: Giày cao gót trắng 38 (MaSKU 6) - MaHDCT = 4
-- Dòng 2: Sandal đen 39 (MaSKU 32) - MaHDCT = 5
(4, 5, N'Giày cao gót rất đẹp, đúng size, đi tiệc thoải mái. Sẽ ủng hộ shop thêm.'),
(5, 4, N'Sandal đen thời trang, chất liệu tốt. Trừ 1 sao vì giao hơi chậm.'),

-- HD10 có 2 dòng:
-- Dòng 1: Boot nâu 42 (MaSKU 37) - MaHDCT = 19
-- Dòng 2: Vớ chạy bộ Performance (MaSKU 43) - MaHDCT = 20
(19, 5, N'Mua tặng chồng, chồng rất thích. Boot nâu đẹp, da mềm, đi êm.'),
(20, 4, N'Vớ chạy bộ chất tốt, thấm hút mồ hôi. Giá hơi cao so với mặt bằng chung.'),

-- ===== ĐÁNH GIÁ CỦA KHÁCH HÀNG 3 (MaKH = 3) - Hóa đơn HD3 và HD7 =====
-- HD3 có 2 dòng:
-- Dòng 1: Giày bóng đá vàng 42 (MaSKU 29) - MaHDCT = 6
-- Dòng 2: Vớ Helio trắng (MaSKU 42) - MaHDCT = 7 (đã có đánh giá ở trên? Không, đây là HD3)
(6, 5, N'Giày bóng đá chất lượng tốt, đế bám sân, đá bóng êm chân. Đáng tiền!'),
(7, 3, N'Vớ Helio màu đẹp nhưng hơi mỏng, đá bóng 1 trận đã thấy sờn gót.'),

-- HD7 có 2 dòng:
-- Dòng 1: Boot combat đen 38 (MaSKU 38) - MaHDCT = 13
-- Dòng 2: Dây giày basic đen (MaSKU 44) - MaHDCT = 14
(13, 4, N'Boot combat đen phong cách, đi chơi rất ngầu. Trừ 1 sao vì hơi nặng.'),
(14, 5, N'Dây giày basic đen chất lượng tốt, giá rẻ, thay cho dây cũ hỏng.');
GO

-- 15. Dữ liệu mẫu cho bảng TimKiem
INSERT INTO LSTimKiem (MaKH, NoiDungTimKiem) VALUES
(1, N'Giày da nam'),
(1, N'Giày tây công sở'),
(2, N'Giày sneaker nữ trắng'),
(3, N'Giày đá banh sân cỏ nhân tạo'),
(4, N'Vớ thể thao cổ ngắn');
GO
