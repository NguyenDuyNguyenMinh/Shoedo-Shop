package poly.edu.service;

import com.itextpdf.io.font.FontProgram;
import com.itextpdf.io.font.FontProgramFactory;
import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Div;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import poly.edu.dto.DiaChiJsonDTO;
import poly.edu.entity.HoaDon;
import poly.edu.entity.HoaDonCT;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;

@Service
public class PdfService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public byte[] generateInvoice(HoaDon hoaDon) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc, PageSize.A4);
        
        FontProgram fontProgram = FontProgramFactory.createFont("C:/Windows/Fonts/arial.ttf");
        PdfFont font = PdfFontFactory.createFont(fontProgram, PdfEncodings.IDENTITY_H);
        
        document.setFont(font);

        Paragraph title = new Paragraph("HÓA ĐƠN SHOEDO")
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(20)
                .setBold();
        
        document.add(title);
        document.add(new Paragraph("\n"));
        
        document.add(new Paragraph("THÔNG TIN ĐƠN HÀNG").setBold().setFontSize(14));
        
        document.add(new Paragraph("Mã hóa đơn: #HD" + String.format("%04d", hoaDon.getMaHD())).setBold());
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        document.add(new Paragraph("Ngày mua: " + sdf.format(hoaDon.getNgayMua())));
        if (hoaDon.getNgayDen() != null) {
        	 SimpleDateFormat sdf2 = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        	 document.add(new Paragraph("Ngày đến: " + sdf2.format(hoaDon.getNgayDen())));
        }
        document.add(new Paragraph("Phương thức thanh toán: " + hoaDon.getPhuongThucTT()));
        document.add(new Paragraph("\n"));

        document.add(new Paragraph("THÔNG TIN KHÁCH HÀNG").setBold().setFontSize(14));

        if (hoaDon.getDiaChiJson() != null && !hoaDon.getDiaChiJson().isEmpty()) {
            try {
                DiaChiJsonDTO diaChi = objectMapper.readValue(hoaDon.getDiaChiJson(), DiaChiJsonDTO.class);
                document.add(new Paragraph("Người nhận: " + diaChi.getTenNN()));
                document.add(new Paragraph("SĐT người nhận: " + diaChi.getSdt()));
                document.add(new Paragraph("Địa chỉ giao: " + diaChi.getDiemGiao()));
            } catch (Exception e) {
            	
            }
        }
        
        document.add(new Paragraph("\n"));

        document.add(new Paragraph("CHI TIẾT SẢN PHẨM").setBold().setFontSize(12));
        
        Table table = new Table(UnitValue.createPercentArray(new float[]{5, 30, 15, 5, 20, 25}));
        table.setWidth(UnitValue.createPercentValue(100));
        table.addHeaderCell(createCell("STT", true));
        table.addHeaderCell(createCell("Sản phẩm", true));
        table.addHeaderCell(createCell("Phân loại", true));
        table.addHeaderCell(createCell("SL", true));
        table.addHeaderCell(createCell("Đơn giá", true));
        table.addHeaderCell(createCell("Thành tiền", true));

        double tongTien = 0;
        int stt = 1;

        for (HoaDonCT ct : hoaDon.getHoaDonCTs()) {
            String tenSP = ct.getSanPhamChiTiet().getSanPham() != null ? 
                          ct.getSanPhamChiTiet().getSanPham().getTenSP() : "N/A";
            String phanLoai = ct.getSanPhamChiTiet().getTenMau() + 
                             (ct.getSanPhamChiTiet().getSize() != null ? 
                              " - Size " + ct.getSanPhamChiTiet().getSize().getCoGiay() : "");
            double thanhTien = ct.getSoLuong() * ct.getDonGia();
            tongTien += thanhTien;

            table.addCell(createCell(String.valueOf(stt++), false));
            table.addCell(createCell(tenSP, false));
            table.addCell(createCell(phanLoai, false));
            table.addCell(createCell(String.valueOf(ct.getSoLuong()), false, TextAlignment.CENTER));
            table.addCell(createCell(formatCurrency(ct.getDonGia()), false, TextAlignment.RIGHT));
            table.addCell(createCell(formatCurrency(thanhTien), false, TextAlignment.RIGHT));
        }

        document.add(table);

        Paragraph total = new Paragraph("Tổng cộng: " + formatCurrency(tongTien))
                .setTextAlignment(TextAlignment.RIGHT)
                .setBold()
                .setFontSize(14);
        document.add(total);

        document.add(new Paragraph("\n"));

        if (hoaDon.getGhiChu() != null && !hoaDon.getGhiChu().isEmpty()) {
            document.add(new Paragraph("Ghi chú:" + hoaDon.getGhiChu()));
        }
        
        document.close();
        return baos.toByteArray();
    }

    private Cell createCell(String content, boolean isHeader) {
        return createCell(content, isHeader, TextAlignment.LEFT);
    }

    private Cell createCell(String content, boolean isHeader, TextAlignment alignment) {
    	Paragraph p = new Paragraph(content);

        if (isHeader) {
            p.setFontColor(ColorConstants.WHITE);
        }
        Cell cell = new Cell().add(p);
        cell.setTextAlignment(alignment);
        if (isHeader) {
            cell.setBold();
            cell.setBackgroundColor(com.itextpdf.kernel.colors.ColorConstants.BLACK);
        }
        return cell;
    }

    private String formatCurrency(double amount) {
        return String.format("%,.0f VNĐ", amount);
    }
}