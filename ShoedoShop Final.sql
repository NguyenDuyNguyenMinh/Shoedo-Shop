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
TrangThai NVARCHAR(50) CHECK (TrangThai IN (N'Đang xử lý', N'Đang giao', N'Hoàn tất', N'Đã từ chối', N'Báo lỗi')),
GhiChu NVARCHAR(MAX),
NgayMua DATETIME DEFAULT GETDATE(),
NgayDen DATETIME,
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
('admin', 'admin@shop.com', '$2a$10$FQi/T2Pcgc1UaMkS/8mf0uSLMzLUtjNn0Ja4YRsCj2aRDdxWBCf4K', 1),
('nv1', 'nhanvien1@gmail.com', '$2a$10$FQi/T2Pcgc1UaMkS/8mf0uSLMzLUtjNn0Ja4YRsCj2aRDdxWBCf4K', 1),
('nv2', 'nhanvien2@gmail.com', '$2a$10$FQi/T2Pcgc1UaMkS/8mf0uSLMzLUtjNn0Ja4YRsCj2aRDdxWBCf4K', 1),
('nv3', 'nhanvien3@gmail.com', '$2a$10$FQi/T2Pcgc1UaMkS/8mf0uSLMzLUtjNn0Ja4YRsCj2aRDdxWBCf4K', 1),
('user1', 'user1@gmail.com', '$2a$10$FQi/T2Pcgc1UaMkS/8mf0uSLMzLUtjNn0Ja4YRsCj2aRDdxWBCf4K', 1),
('user2', 'user2@gmail.com', '$2a$10$FQi/T2Pcgc1UaMkS/8mf0uSLMzLUtjNn0Ja4YRsCj2aRDdxWBCf4K', 1),
('user3', 'user3@gmail.com', '$2a$10$FQi/T2Pcgc1UaMkS/8mf0uSLMzLUtjNn0Ja4YRsCj2aRDdxWBCf4K', 1),
('QuanTesteremail', 'nguyenhoangminhquan786@gmail.com', '$2a$10$FQi/T2Pcgc1UaMkS/8mf0uSLMzLUtjNn0Ja4YRsCj2aRDdxWBCf4K', 1);

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
(N'Giày Da Tây Shoedo "Gentle Derby" - GD', 1, N'Sở hữu thiết kế Derby kinh điển, mẫu giày da Tây Gentle Derby từ Shoedo là biểu tượng của sự chuyên nghiệp và phong thái tự tin. Đây là sự lựa chọn không thể thay thế cho những buổi họp quan trọng, môi trường công sở hay các sự kiện lễ nghi, giúp phái mạnh khẳng định sự chỉn chu trong mọi tình huống.', 10, 2),
(N'Giày Mary Jane Shoedo "Sweet Pearl" - GD', 0, N'Mang hơi thở của phong cách vintage pha chút hiện đại, mẫu giày Sweet Pearl từ Shoedo là lựa chọn tuyệt vời cho những cô nàng yêu thích vẻ đẹp trong sáng, nữ tính. Với thiết kế mũi tròn đáng yêu và điểm nhấn ngọc trai tinh tế, đôi giày này sẽ biến mọi outfit của bạn trở nên dịu dàng như một nàng tiểu thư.', 10, 0),
(N'Giày Cao Gót Shoedo "Classic Muse" - GD', 0, N'Mang vẻ đẹp vượt thời gian với phong cách phối màu kinh điển (Two-tone), mẫu giày cao gót mũi nhọn Classic Muse từ Shoedo là món đồ không thể thiếu để hoàn thiện diện mạo sang trọng của phái đẹp. Sự giao thoa giữa tone kem nền nã và mũi giày đen quyền lực tạo nên sức hút khó cưỡng cho mọi quý cô công sở và những tín đồ thời trang cao cấp.', 30, 2),

--Giày sneaker
(N'Giày Sneaker Thể Thao Shoedo "Urban Pulse" - GSK', 1, N'Dòng Sneaker thể thao Urban Pulse từ Shoedo được thiết kế để bắt kịp nhịp sống sôi động của thế hệ trẻ. Đây không chỉ là một đôi giày tập luyện, mà còn là phụ kiện hoàn hảo cho những chuyến hành trình khám phá thành phố, mang lại sự cân bằng tuyệt vời giữa trọng lượng siêu nhẹ và khả năng nâng đỡ bàn chân tối ưu.', 0, 2),
(N'Giày Sneaker Shoedo "Heritage Canvas" - GSK', 1, N'Dòng Sneaker Heritage Canvas của Shoedo là sự tôn vinh dành cho những giá trị bền vững. Với thiết kế tối giản không bao giờ lỗi mốt, đây là đôi giày "quốc dân" có thể đồng hành cùng bạn từ giảng đường, văn phòng cho đến những buổi hẹn hò cuối tuần.', 0, 0),

--Giày bóng đá
(N'Giày Đá Bóng Shoedo "Neon Strike" - GBD', 1, N'Sẵn sàng trở thành tâm điểm của mọi trận đấu với Shoedo Neon Strike. Được thiết kế cho những cầu thủ có lối chơi tốc độ và kĩ thuật, mẫu giày này không chỉ sở hữu vẻ ngoài cực kỳ cá tính mà còn tích hợp những công nghệ hỗ trợ tối đa cho việc kiểm soát bóng và bứt tốc.', 5, 3),

--Giày sandal
(N'Sandal Cao Gót Shoedo "Elegant Lift" - GSD', 0, N'Được thiết kế để tôn vinh nét đẹp nữ tính và hiện đại, mẫu Sandal cao gót từ Shoedo là sự lựa chọn hoàn hảo cho những quý cô yêu thích sự kết hợp giữa chiều cao ấn tượng và cảm giác thoải mái. Với tone màu trắng kem tinh khôi, đôi giày này sẵn sàng đồng hành cùng bạn từ môi trường công sở chuyên nghiệp đến những buổi tiệc tối sang trọng.', 30, 2),

--Giày boot
(N'Giày Chelsea Boot Shoedo "Soft Urban" - GBT', NULL, N'Định nghĩa lại phong cách thành thị với mẫu Boot vải cao cấp từ Shoedo. Không sử dụng dây buộc cầu kỳ, phiên bản "Soft Urban" tập trung vào những đường nét thanh thoát, mang lại sự tiện lợi tối đa cho người sử dụng mà vẫn giữ được vẻ ngoài thời thượng, tinh tế.', 0, 2),

--Giày boot & da
(N'Giày Combat Boot Nữ Shoedo "Rebel Charm" - GBT&GD', 0, N'Được thiết kế dành riêng cho những cô nàng hiện đại, yêu thích sự phá cách và không ngại thể hiện bản thân, mẫu Boot da từ Shoedo là sự kết hợp hoàn hảo giữa nét cổ điển của dòng Combat Boot và sự tinh tế trong từng đường nét đương đại. Đây chính là "vũ khí" bí mật giúp bạn nâng tầm mọi set đồ, từ dạo phố đến những buổi tiệc đêm sôi động.', 0, 1),

--Phụ kiện
(N'Vớ Cổ Cao Shoedo Daily Basic - PKv1', NULL, N'Dòng vớ Shoedo Daily Basic là món phụ kiện "phải có" trong tủ đồ của bất kỳ ai. Không cầu kỳ về họa tiết, sản phẩm tập trung hoàn toàn vào sự thoải mái và khả năng ứng dụng linh hoạt, giúp bạn dễ dàng kết hợp với mọi loại giày từ giày Tây, Sneaker cho đến giày lười.', 10, 5),
(N'Vớ Cổ Cao Shoedo "Helio Edition" - PKv2', NULL, N'Không chỉ là một món phụ kiện giữ ấm, mẫu vớ Shoedo x Helio là sự giao thoa giữa nghệ thuật đồ họa và thời trang hiện đại. Với họa tiết độc bản mang tinh thần tự do, đây là lựa chọn hàng đầu để bạn tạo điểm nhấn nổi bật khi diện cùng các mẫu Short, Sneaker hoặc quần xắn gấu.', 10, 1),
(N'Vớ Chạy Bộ Chuyên Dụng Shoedo Performance - PKv3', NULL, N'Được thiết kế dành riêng cho những vận động viên và người yêu thích chạy bộ, dòng vớ Shoedo Performance không chỉ là một phụ kiện mà là "người bạn đồng hành" giúp bảo vệ đôi chân tối đa. Với sự kết hợp giữa công nghệ dệt nén linh hoạt và khả năng kiểm soát độ ẩm, sản phẩm mang lại sự thoải mái tuyệt đối từ km đầu tiên đến khi về đích.', 10, 4),

(N'Dây Giày Tròn Shoedo Basic - PKgd1', NULL, N'Dòng dây giày tròn cơ bản từ Shoedo là giải pháp thay thế hoàn hảo cho mọi đôi giày khi dây cũ bị sờn rách hoặc bạn chỉ đơn giản muốn làm mới đôi giày của mình một cách nhanh chóng. Với thiết kế tối giản không cầu kỳ, sản phẩm chú trọng vào chất lượng thực tế và mức giá "mềm" nhất thị trường.', 10, 1),
(N'Dây Giày Dẹt Shoedo Camo - PKgd2', NULL, N'Dòng dây giày dẹt họa tiết Camo (rằn ri) từ Shoedo là phụ kiện lý tưởng dành cho những ai yêu thích phong cách Streetwear, Military hoặc muốn tạo nên một diện mạo phá cách cho đôi giày của mình. Không còn là những sợi dây đơn sắc nhàm chán, Shoedo Camo biến mỗi bước chân trở thành một tuyên ngôn về cá tính riêng biệt.', 10, 0),
(N'Dây Giày Tròn Phản Quang Shoedo - PKgd3', NULL, N'Dòng dây giày nằm trong bộ sưu tập phụ kiện cao cấp từ Shoedo, dòng dây giày tròn phản quang là sự kết hợp hoàn hảo giữa tính năng bảo vệ và yếu tố thời trang "vượt thời gian". Không chỉ đơn thuần là phụ kiện giữ form giày, đây chính là điểm nhấn giúp đôi giày của bạn tỏa sáng theo đúng nghĩa đen.', 10, 1);

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

(13, 6),
(14, 6),
(15, 6);

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
(1, N'Đen', 'sp1_gd1_black.jpg', 5, N'Còn hàng', 10, 1000000), -- Size 40 (ID 5)
(1, N'Đen', 'sp1_gd1_black.jpg', 6, N'Còn hàng', 10, 1200000), -- Size 41 (ID 6)

-- SP 2: ShoeDo - SP2 - GD (Nữ)
(2, N'Trắng', 'sp2_gd2_white.jpg', 2, N'Còn hàng', 15, 1000000), -- Size 37 (ID 2)
(2, N'Trắng', 'sp2_gd2_white.jpg', 3, N'Còn hàng', 20, 1200000), -- Size 38 (ID 3)
(2, N'Trắng', 'sp2_gd2_white.jpg', 4, N'Còn hàng', 20, 1500000), -- Size 39 (ID 4)

-- SP 3: ShoeDo - SP3 - GD (Cao gót)
(3, N'Trắng', 'sp3_gd3_white.jpg', 2, N'Còn hàng', 10, 1000000), -- Size 37 (ID 2)
(3, N'Trắng', 'sp3_gd3_white.jpg', 3, N'Còn hàng', 15, 1200000), -- Size 38 (ID 3)
(3, N'Trắng', 'sp3_gd3_white.jpg', 4, N'Còn hàng', 10, 1300000), -- Size 39 (ID 4)

-- =======================
-- Giày sneaker (MaSP 4,5)
-- =======================
-- SP 4: ShoeDo - SP1 - GSK
(4, N'Đen', 'sp4_snk1_black.jpg', 5, N'Còn hàng', 10, 1500000),
(4, N'Đen', 'sp4_snk1_black.jpg', 6, N'Còn hàng', 10, 1500000), 
(4, N'Đen', 'sp4_snk1_black.jpg', 7, N'Còn hàng', 10, 1500000), 
(4, N'Trắng', 'sp4_snk1_white.jpg', 5, N'Còn hàng', 10, 1300000), 
(4, N'Trắng', 'sp4_snk1_white.jpg', 6, N'Còn hàng', 10, 1300000), 
(4, N'Trắng', 'sp4_snk1_white.jpg', 7, N'Còn hàng', 10, 1300000), 
(4, N'Nâu', 'sp4_snk1_brown.jpg', 5, N'Còn hàng', 20, 1800000),
(4, N'Nâu', 'sp4_snk1_brown.jpg', 6, N'Còn hàng', 20, 1800000), 
(4, N'Nâu', 'sp4_snk1_brown.jpg', 7, N'Còn hàng', 20, 1800000), 

-- SP 5: ShoeDo - SP2 - GSK
-- Size 40 (ID 5)
-- Size 41 (ID 6)
-- Size 42 (ID 7)
(5, N'Xanh lá', 'sp5_snk2_green.jpg', 5, N'Còn hàng', 20, 1500000),
(5, N'Xanh lá', 'sp5_snk2_green.jpg', 6, N'Còn hàng', 20, 1500000), 
(5, N'Xanh lá', 'sp5_snk2_green.jpg', 7, N'Còn hàng', 20, 1500000), 
(5, N'Trắng', 'sp5_snk2_white.jpg', 5, N'Còn hàng', 10, 1200000), 
(5, N'Trắng', 'sp5_snk2_white.jpg', 6, N'Còn hàng', 10, 1200000), 
(5, N'Trắng', 'sp5_snk2_white.jpg', 7, N'Còn hàng', 10, 1200000), 

-- =======================
-- Giày bóng đá (MaSP 6)
-- =======================
-- SP 6: ShoeDo - SP1 - GBD
(6, N'Hồng', 'sp6_gbd1_pink.jpg', 8, N'Còn hàng', 12, 1500000), -- Size 43 (ID 8)
(6, N'Hồng', 'sp6_gbd1_pink.jpg', 7, N'Còn hàng', 20, 1500000), -- Size 42 (ID 7)
(6, N'Hồng', 'sp6_gbd1_pink.jpg', 6, N'Còn hàng', 10, 1200000), -- Size 41 (ID 6)
(6, N'Hồng', 'sp6_gbd1_pink.jpg', 5, N'Còn hàng', 10, 1200000), -- Size 40 (ID 5)
(6, N'Vàng', 'sp6_gbd1_yellow.jpg', 8, N'Còn hàng', 12, 1500000), -- Size 43 (ID 8)
(6, N'Vàng', 'sp6_gbd1_yellow.jpg', 7, N'Còn hàng', 20, 1500000), -- Size 42 (ID 7)
(6, N'Vàng', 'sp6_gbd1_yellow.jpg', 6, N'Còn hàng', 10, 1200000), -- Size 41 (ID 6)
(6, N'Vàng', 'sp6_gbd1_yellow.jpg', 5, N'Còn hàng', 10, 1200000), -- Size 40 (ID 5)

-- =======================
-- Giày sandal (MaSP 7)
-- =======================
-- SP 7: ShoeDo - SP1 - GSD
-- Size 37 (ID 2)
-- Size 38 (ID 3)
-- Size 39 (ID 4)
(7, N'Đen', 'sp7_gsd1_black.jpg', 2, N'Còn hàng', 18, 1800000),
(7, N'Đen', 'sp7_gsd1_black.jpg', 3, N'Còn hàng', 18, 1800000),
(7, N'Đen', 'sp7_gsd1_black.jpg', 4, N'Còn hàng', 18, 1800000),
(7, N'Trắng', 'sp7_gsd1_white.jpg', 2, N'Còn hàng', 18, 1800000),
(7, N'Trắng', 'sp7_gsd1_white.jpg', 3, N'Còn hàng', 18, 1800000), 
(7, N'Trắng', 'sp7_gsd1_white.jpg', 4, N'Còn hàng', 18, 1800000), 

-- =======================
-- Giày boot (MaSP 8)
-- =======================
-- SP 8: ShoeDo - SP1 - GBT
(8, N'Đen', 'sp8_b1_black.jpg', 3, N'Còn hàng', 15, 1900000), -- Size 38 (ID 3)
(8, N'Đen', 'sp8_b1_black.jpg', 5, N'Còn hàng', 15, 2000000), -- Size 40 (ID 5)
(8, N'Đen', 'sp8_b1_black.jpg', 7, N'Còn hàng', 15, 2100000), -- Size 42 (ID 7)
(8, N'Nâu', 'sp8_b1_brown.jpg', 3, N'Còn hàng', 15, 1900000), -- Size 38 (ID 3)
(8, N'Nâu', 'sp8_b1_brown.jpg', 5, N'Còn hàng', 15, 2000000), -- Size 40 (ID 5)
(8, N'Nâu', 'sp8_b1_brown.jpg', 7, N'Còn hàng', 15, 2100000), -- Size 42 (ID 7)

-- =======================
-- Giày boot + da
-- =======================
-- SP 9: ShoeDo - SP9 - GBT&GD
-- Size 37 (ID 2)
-- Size 38 (ID 3)
-- Size 39 (ID 4)
-- Size 40 (ID 5)
(9, N'Đen', 'sp9_bd1_black.jpg', 2, N'Còn hàng', 15, 1500000),
(9, N'Đen', 'sp9_bd1_black.jpg', 3, N'Còn hàng', 15, 1500000), 
(9, N'Đen', 'sp9_bd1_black.jpg', 4, N'Còn hàng', 15, 1500000), 
(9, N'Đen', 'sp9_bd1_black.jpg', 5, N'Còn hàng', 15, 1500000), 

-- =======================
-- Phụ kiện (MaSP 10 → 15)
-- =======================
-- SP 10 - 12: Vớ (Dùng ID 1 = FreeSize)
(10, N'Đen', 'sp10_v1_bw.jpg', 1, N'Còn hàng', 100, 70000), -- Free size (ID 1) 
(10, N'Trắng', 'sp10_v1_bw.jpg', 1, N'Còn hàng', 100, 70000), -- Free size (ID 1)

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
(15, N'Xanh lá', 'sp15_dg3_green.png', 1, N'Còn hàng', 80, 120000); -- Free size (ID 1)

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
INSERT INTO HoaDon (MaKH, MaQT, PhuongThucTT, DiaChiJson, TrangThai, GhiChu, NgayMua, NgayDen) VALUES
-- HD 1
(1, 1, N'COD',
 N'{"DiemGiao":"123 Nguyễn Huệ A, Q1","TenNN":"Nguyễn Văn A","SDT":"0901234567"}',
 N'Đã từ chối', N'Đơn hàng đặt số lượng quá lớn nhân viên miễn cưỡng từ chối vì gọi không ghe máy', '2025-01-10', NULL),

-- HD 2
(2, NULL, N'Chuyển khoản',
 N'{"DiemGiao":"789 Cách Mạng Tháng 8Z, Tân Bình","TenNN":"Trần Thị Hi A","SDT":"0912345678"}',
 N'Đang xử lý', NULL, '2026-01-11', NULL),

-- HD 3
(3, 1, N'COD',
 N'{"DiemGiao":"456 Lê Lợi A, Q1","TenNN":"Nguyễn Văn A","SDT":"0901234567"}',
 N'Đang giao', NULL, '2026-01-12', NULL),

-- HD 4
(4, 1, N'COD',
 N'{"DiemGiao":"Quận Cam A","TenNN":"Nguyễn Văn A","SDT":"0901234567"}',
 N'Hoàn tất', NULL, '2026-01-13', '2026-03-25'),

-- HD 5
(1, 1, N'COD',
 N'{"DiemGiao":"123 Nguyễn Huệ B","TenNN":"Nguyễn Văn A-B","SDT":"0901234567"}',
 N'Đã từ chối', N'Khách hủy đơn', '2026-01-14', NULL),

-- HD 6
(2, 1, N'Chuyển khoản',
 N'{"DiemGiao":"789 Cách Mạng Tháng 8Y","TenNN":"Trần Thị Hi B","SDT":"0912345678"}',
 N'Báo lỗi', N'Khách hàng không nhận được hàng', '2026-01-15', '2026-01-19'),

-- HD 7
(3, 1, N'COD',
 N'{"DiemGiao":"456 Lê Lợi B","TenNN":"Nguyễn Văn A-B","SDT":"0901234567"}',
 N'Báo lỗi', N'Mũi giày bị móp', '2026-01-16', '2026-01-20'),

-- HD 8
(4, 1, N'COD',
 N'{"DiemGiao":"Quận Cam B","TenNN":"Nguyễn Văn B","SDT":"0901234567"}',
 N'Đang giao', NULL, '2026-01-17', NULL),

-- HD 9
(1, NULL, N'COD',
 N'{"DiemGiao":"123 Nguyễn Huệ C","TenNN":"Nguyễn Văn A-C","SDT":"0901234567"}',
 N'Đang xử lý', NULL, '2026-01-18', NULL),

-- HD 10
(2, 1, N'Chuyển khoản',
 N'{"DiemGiao":"789 Cách Mạng Tháng 8Z","TenNN":"Trần Thị Hi A","SDT":"0912345678"}',
 N'Hoàn tất', NULL, '2026-01-19', '2026-01-24');

-- 14. Dữ liệu mẫu cho bảng Hóa Đơn Chi Tiết 
INSERT INTO HoaDonCT (MaHD, MaSKU, SoLuong, DonGia) VALUES
-- ===== HD 1 =====
-- Giày da đen 40 (MaSP 1, Size 40 - MaSKU 1): Giá gốc 1,000,000 - KM 10% = 900,000
(1, 1, 1, 900000),
-- Giày bóng đá hồng 41 (MaSP 6, Size 41 - MaSKU 25): Giá gốc 1,200,000 - KM 5% = 1,140,000
(1, 25, 1, 1140000),
-- Vớ đen (MaSP 10, FreeSize - MaSKU 40): Giá gốc 70,000 - KM 10% = 63,000
(1, 40, 2, 63000),

-- ===== HD 2 =====
-- Giày cao gót trắng 38 (MaSP 3, Size 38 - MaSKU 6): Giá gốc 1,200,000 - KM 30% = 840,000
(2, 6, 1, 840000),
-- Sandal đen 39 (MaSP 7, Size 39 - MaSKU 32): Giá gốc 1,800,000 - KM 30% = 1,260,000
(2, 32, 1, 1260000),

-- ===== HD 3 =====
-- Giày bóng đá vàng 42 (MaSP 6, Size 42 - MaSKU 29): Giá gốc 1,500,000 - KM 5% = 1,425,000
(3, 29, 2, 1425000),
-- Vớ cổ cao Helio trắng (MaSP 11, FreeSize - MaSKU 42): Giá gốc 150,000 - KM 10% = 135,000
(3, 42, 1, 135000),

-- ===== HD 4 =====
-- Boot đen 40 (MaSP 8, Size 40 - MaSKU 36): Giá gốc 2,000,000 - KM 0% = 2,000,000
(4, 36, 1, 2000000),
-- Dây giày tròn phản quang đen (MaSP 15, FreeSize - MaSKU 50): Giá gốc 120,000 - KM 10% = 108,000
(4, 50, 1, 108000),

-- ===== HD 5 =====
-- Giày cao gót trắng 37 (MaSP 3, Size 37 - MaSKU 5): Giá gốc 1,000,000 - KM 30% = 700,000
(5, 5, 1, 700000),
-- Vớ chạy bộ Performance (MaSP 12, FreeSize - MaSKU 43): Giá gốc 180,000 - KM 10% = 162,000
(5, 43, 2, 162000),

-- ===== HD 6 =====
-- Sneaker đen 42 (MaSP 4, Size 42 - MaSKU 10): Giá gốc 1,500,000 - KM 0% = 1,500,000
(6, 10, 1, 1500000),
-- Vớ trắng (MaSP 10, FreeSize - MaSKU 41): Giá gốc 70,000 - KM 10% = 63,000
(6, 41, 3, 63000),

-- ===== HD 7 =====
-- Boot combat đen 38 (MaSP 9, Size 38 - MaSKU 38): Giá gốc 1,500,000 - KM 0% = 1,500,000
(7, 38, 1, 1500000),
-- Dây giày tròn basic đen (MaSP 13, FreeSize - MaSKU 44): Giá gốc 80,000 - KM 10% = 72,000
(7, 44, 1, 72000),

-- ===== HD 8  =====
-- Sneaker trắng 41 (MaSP 4, Size 41 - MaSKU 12): Giá gốc 1,300,000 - KM 0% = 1,300,000
(8, 12, 1, 1300000),
-- Vớ cổ cao Helio đen (MaSP 11, FreeSize - MaSKU 41): Giá gốc 150,000 - KM 10% = 135,000
(8, 41, 2, 135000),

-- ===== HD 9 =====
-- Giày da đen 41 (MaSP 1, Size 41 - MaSKU 2): Giá gốc 1,200,000 - KM 10% = 1,080,000
(9, 2, 1, 1080000),
-- Sandal trắng 38 (MaSP 7, Size 38 - MaSKU 33): Giá gốc 1,800,000 - KM 30% = 1,260,000
(9, 33, 1, 1260000),

-- ===== HD 10 =====
-- Boot nâu 42 (MaSP 8, Size 42 - MaSKU 37): Giá gốc 2,100,000 - KM 0% = 2,100,000
(10, 37, 1, 2100000),
-- Vớ chạy bộ Performance (MaSP 12, FreeSize - MaSKU 43): Giá gốc 180,000 - KM 10% = 162,000
(10, 43, 2, 162000);

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
INSERT INTO TimKiem (MaKH, NoiDungTimKiem) VALUES
(1, N'Giày da nam'),
(1, N'Giày tây công sở'),
(2, N'Giày sneaker nữ trắng'),
(3, N'Giày đá banh sân cỏ nhân tạo'),
(4, N'Vớ thể thao cổ ngắn');
GO