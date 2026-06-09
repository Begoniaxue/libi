package com.library.service;

import com.library.entity.Book;
import com.library.entity.BorrowRecord;
import com.library.entity.FeeRecord;
import com.library.entity.Reader;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ExportService {

    @Autowired
    private StatisticsService statisticsService;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public byte[] exportBooksToExcel() {
        List<Book> books = statisticsService.getBooksForExport();

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("图书数据");

            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);

            String[] headers = {"ID", "ISBN", "书名", "作者", "出版社", "出版日期",
                    "分类", "馆藏位置", "总数量", "可借数量", "状态", "创建时间"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowNum = 1;
            for (Book book : books) {
                Row row = sheet.createRow(rowNum++);
                createCell(row, 0, book.getId(), dataStyle);
                createCell(row, 1, book.getIsbn(), dataStyle);
                createCell(row, 2, book.getName(), dataStyle);
                createCell(row, 3, book.getAuthor(), dataStyle);
                createCell(row, 4, book.getPublisher(), dataStyle);
                createCell(row, 5, book.getPublishDate() != null ?
                        book.getPublishDate().format(DATE_FORMATTER) : "", dataStyle);
                createCell(row, 6, book.getCategory(), dataStyle);
                createCell(row, 7, book.getLocation(), dataStyle);
                createCell(row, 8, book.getTotalQuantity(), dataStyle);
                createCell(row, 9, book.getAvailableQuantity(), dataStyle);
                createCell(row, 10, book.getStatus() == 1 ? "上架" : "下架", dataStyle);
                createCell(row, 11, book.getCreateTime() != null ?
                        book.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "", dataStyle);
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("导出Excel失败", e);
        }
    }

    public byte[] exportBorrowRecordsToExcel(LocalDate startDate, LocalDate endDate) {
        List<BorrowRecord> records = statisticsService.getBorrowRecordsForExport(startDate, endDate);

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("借阅记录");

            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);

            String[] headers = {"ID", "读者姓名", "图书名称", "借书日期", "应还日期",
                    "实际还书日期", "续借次数", "逾期天数", "罚款金额", "赔偿金额", "状态"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowNum = 1;
            for (BorrowRecord record : records) {
                Row row = sheet.createRow(rowNum++);
                createCell(row, 0, record.getId(), dataStyle);
                createCell(row, 1, record.getReader() != null ? record.getReader().getName() : "", dataStyle);
                createCell(row, 2, record.getBook() != null ? record.getBook().getName() : "", dataStyle);
                createCell(row, 3, record.getBorrowDate() != null ?
                        record.getBorrowDate().format(DATE_FORMATTER) : "", dataStyle);
                createCell(row, 4, record.getDueDate() != null ?
                        record.getDueDate().format(DATE_FORMATTER) : "", dataStyle);
                createCell(row, 5, record.getReturnDate() != null ?
                        record.getReturnDate().format(DATE_FORMATTER) : "", dataStyle);
                createCell(row, 6, record.getRenewCount(), dataStyle);
                createCell(row, 7, record.getOverdueDays(), dataStyle);
                createCell(row, 8, record.getFineAmount() != null ?
                        record.getFineAmount().doubleValue() : 0, dataStyle);
                createCell(row, 9, record.getCompensationAmount() != null ?
                        record.getCompensationAmount().doubleValue() : 0, dataStyle);
                createCell(row, 10, getStatusText(record.getStatus()), dataStyle);
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("导出Excel失败", e);
        }
    }

    public byte[] exportReadersToExcel() {
        List<Reader> readers = statisticsService.getReadersForExport();

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("读者数据");

            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);

            String[] headers = {"ID", "借书证号", "姓名", "性别", "手机号", "邮箱",
                    "出生日期", "身份类型", "信用分", "违规次数", "状态"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowNum = 1;
            for (Reader reader : readers) {
                Row row = sheet.createRow(rowNum++);
                createCell(row, 0, reader.getId(), dataStyle);
                createCell(row, 1, reader.getCardNo(), dataStyle);
                createCell(row, 2, reader.getName(), dataStyle);
                createCell(row, 3, reader.getGender() != null ?
                        (reader.getGender() == 1 ? "男" : "女") : "", dataStyle);
                createCell(row, 4, reader.getPhone(), dataStyle);
                createCell(row, 5, reader.getEmail(), dataStyle);
                createCell(row, 6, reader.getBirthday() != null ?
                        reader.getBirthday().format(DATE_FORMATTER) : "", dataStyle);
                createCell(row, 7, reader.getIdentityType(), dataStyle);
                createCell(row, 8, reader.getCreditScore(), dataStyle);
                createCell(row, 9, reader.getViolationCount(), dataStyle);
                createCell(row, 10, reader.getStatus() == 1 ? "正常" : "注销", dataStyle);
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("导出Excel失败", e);
        }
    }

    public byte[] exportFeeRecordsToExcel(LocalDate startDate, LocalDate endDate) {
        List<FeeRecord> records = statisticsService.getFeeRecordsForExport(startDate, endDate);

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("费用记录");

            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);

            String[] headers = {"ID", "读者姓名", "费用类型", "金额", "已缴金额", "是否已缴", "备注", "创建时间"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowNum = 1;
            for (FeeRecord record : records) {
                Row row = sheet.createRow(rowNum++);
                createCell(row, 0, record.getId(), dataStyle);
                createCell(row, 1, record.getReader() != null ? record.getReader().getName() : "", dataStyle);
                createCell(row, 2, record.getFeeType(), dataStyle);
                createCell(row, 3, record.getAmount() != null ?
                        record.getAmount().doubleValue() : 0, dataStyle);
                createCell(row, 4, record.getPaidAmount() != null ?
                        record.getPaidAmount().doubleValue() : 0, dataStyle);
                createCell(row, 5, record.getIsPaid() == 1 ? "是" : "否", dataStyle);
                createCell(row, 6, record.getRemark(), dataStyle);
                createCell(row, 7, record.getCreateTime() != null ?
                        record.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "", dataStyle);
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("导出Excel失败", e);
        }
    }

    public byte[] exportStatisticsToPdf(String reportType, LocalDate startDate, LocalDate endDate) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 36, 36, 36, 36);
            PdfWriter.getInstance(document, out);
            document.open();

            com.itextpdf.text.Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, BaseColor.BLACK);
            com.itextpdf.text.Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.BLACK);
            com.itextpdf.text.Font contentFont = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.BLACK);

            String title = getReportTitle(reportType);
            Paragraph titlePara = new Paragraph(title, titleFont);
            titlePara.setAlignment(Element.ALIGN_CENTER);
            titlePara.setSpacingAfter(20);
            document.add(titlePara);

            String dateRange = "统计时间范围: " +
                    (startDate != null ? startDate.format(DATE_FORMATTER) : "全部") +
                    " 至 " +
                    (endDate != null ? endDate.format(DATE_FORMATTER) : "至今");
            Paragraph datePara = new Paragraph(dateRange, contentFont);
            datePara.setSpacingAfter(15);
            document.add(datePara);

            switch (reportType) {
                case "collection":
                    addCollectionStatistics(document, startDate, endDate, headerFont, contentFont);
                    break;
                case "borrow":
                    addBorrowStatistics(document, startDate, endDate, headerFont, contentFont);
                    break;
                case "reader":
                    addReaderStatistics(document, startDate, endDate, headerFont, contentFont);
                    break;
                case "hot":
                    addHotResourcesStatistics(document, startDate, endDate, headerFont, contentFont);
                    break;
                case "fee":
                    addFeeStatistics(document, startDate, endDate, headerFont, contentFont);
                    break;
            }

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("导出PDF失败", e);
        }
    }

    private String getReportTitle(String reportType) {
        switch (reportType) {
            case "collection": return "馆藏数据统计报表";
            case "borrow": return "借阅数据统计报表";
            case "reader": return "读者数据统计报表";
            case "hot": return "热门资源统计报表";
            case "fee": return "费用数据统计报表";
            default: return "数据统计报表";
        }
    }

    private void addCollectionStatistics(Document document, LocalDate startDate, LocalDate endDate,
                                          com.itextpdf.text.Font headerFont, com.itextpdf.text.Font contentFont) throws DocumentException {
        var stats = statisticsService.getCollectionStatistics(startDate, endDate);

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setSpacingAfter(15);

        addTableCell(table, "馆藏总量", headerFont, true);
        addTableCell(table, String.valueOf(stats.get("totalBooks")), contentFont, false);

        addTableCell(table, "可借图书数", headerFont, true);
        addTableCell(table, String.valueOf(stats.get("availableBooks")), contentFont, false);

        addTableCell(table, "馆藏总册数", headerFont, true);
        addTableCell(table, String.valueOf(stats.get("totalQuantity")), contentFont, false);

        addTableCell(table, "可借册数", headerFont, true);
        addTableCell(table, String.valueOf(stats.get("availableQuantity")), contentFont, false);

        addTableCell(table, "新增图书数", headerFont, true);
        addTableCell(table, String.valueOf(stats.get("newBooksCount")), contentFont, false);

        addTableCell(table, "呆滞图书数", headerFont, true);
        addTableCell(table, String.valueOf(stats.get("inactiveBooksCount")), contentFont, false);

        document.add(table);
    }

    private void addBorrowStatistics(Document document, LocalDate startDate, LocalDate endDate,
                                     com.itextpdf.text.Font headerFont, com.itextpdf.text.Font contentFont) throws DocumentException {
        var stats = statisticsService.getBorrowStatistics(startDate, endDate);

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setSpacingAfter(15);

        addTableCell(table, "总借阅量", headerFont, true);
        addTableCell(table, String.valueOf(stats.get("totalBorrows")), contentFont, false);

        addTableCell(table, "总归还量", headerFont, true);
        addTableCell(table, String.valueOf(stats.get("totalReturns")), contentFont, false);

        addTableCell(table, "归还率", headerFont, true);
        addTableCell(table, stats.get("returnRate") + "%", contentFont, false);

        addTableCell(table, "逾期数", headerFont, true);
        addTableCell(table, String.valueOf(stats.get("overdueCount")), contentFont, false);

        addTableCell(table, "逾期率", headerFont, true);
        addTableCell(table, stats.get("overdueRate") + "%", contentFont, false);

        addTableCell(table, "续借数", headerFont, true);
        addTableCell(table, String.valueOf(stats.get("renewCount")), contentFont, false);

        addTableCell(table, "续借率", headerFont, true);
        addTableCell(table, stats.get("renewRate") + "%", contentFont, false);

        document.add(table);
    }

    private void addReaderStatistics(Document document, LocalDate startDate, LocalDate endDate,
                                     com.itextpdf.text.Font headerFont, com.itextpdf.text.Font contentFont) throws DocumentException {
        var stats = statisticsService.getReaderStatistics(startDate, endDate);

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setSpacingAfter(15);

        addTableCell(table, "读者总数", headerFont, true);
        addTableCell(table, String.valueOf(stats.get("totalReaders")), contentFont, false);

        addTableCell(table, "活跃读者数", headerFont, true);
        addTableCell(table, String.valueOf(stats.get("activeReaders")), contentFont, false);

        addTableCell(table, "新增读者数", headerFont, true);
        addTableCell(table, String.valueOf(stats.get("newReadersCount")), contentFont, false);

        addTableCell(table, "有违规记录读者数", headerFont, true);
        addTableCell(table, String.valueOf(stats.get("readersWithViolations")), contentFont, false);

        document.add(table);
    }

    private void addHotResourcesStatistics(Document document, LocalDate startDate, LocalDate endDate,
                                           com.itextpdf.text.Font headerFont, com.itextpdf.text.Font contentFont) throws DocumentException {
        var stats = statisticsService.getHotResourcesStatistics(startDate, endDate, 10);

        Paragraph subTitle = new Paragraph("热门图书TOP10", headerFont);
        subTitle.setSpacingAfter(10);
        document.add(subTitle);

        List<java.util.Map<String, Object>> hotBooks = (List<java.util.Map<String, Object>>) stats.get("hotBooks");
        PdfPTable table = new PdfPTable(3);
        table.setWidthPercentage(100);
        table.setSpacingAfter(15);

        addTableCell(table, "排名", headerFont, true);
        addTableCell(table, "图书名称", headerFont, true);
        addTableCell(table, "借阅次数", headerFont, true);

        for (var book : hotBooks) {
            addTableCell(table, String.valueOf(book.get("rank")), contentFont, false);
            addTableCell(table, String.valueOf(book.get("bookName")), contentFont, false);
            addTableCell(table, String.valueOf(book.get("borrowCount")), contentFont, false);
        }

        document.add(table);
    }

    private void addFeeStatistics(Document document, LocalDate startDate, LocalDate endDate,
                                  com.itextpdf.text.Font headerFont, com.itextpdf.text.Font contentFont) throws DocumentException {
        var stats = statisticsService.getFeeStatistics(startDate, endDate);

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setSpacingAfter(15);

        addTableCell(table, "逾期罚款总额", headerFont, true);
        addTableCell(table, "¥" + stats.get("totalFine"), contentFont, false);

        addTableCell(table, "赔偿费用总额", headerFont, true);
        addTableCell(table, "¥" + stats.get("totalCompensation"), contentFont, false);

        addTableCell(table, "总收入", headerFont, true);
        addTableCell(table, "¥" + stats.get("totalIncome"), contentFont, false);

        addTableCell(table, "欠费未结清金额", headerFont, true);
        addTableCell(table, "¥" + stats.get("unpaidAmount"), contentFont, false);

        addTableCell(table, "欠费未结清单数", headerFont, true);
        addTableCell(table, String.valueOf(stats.get("unpaidCount")), contentFont, false);

        document.add(table);
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        org.apache.poi.ss.usermodel.Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.LEFT);
        return style;
    }

    private void createCell(Row row, int column, Object value, CellStyle style) {
        Cell cell = row.createCell(column);
        if (value == null) {
            cell.setCellValue("");
        } else if (value instanceof Number) {
            cell.setCellValue(((Number) value).doubleValue());
        } else {
            cell.setCellValue(String.valueOf(value));
        }
        cell.setCellStyle(style);
    }

    private String getStatusText(Integer status) {
        if (status == null) return "";
        switch (status) {
            case 1: return "借阅中";
            case 2: return "已归还";
            case 3: return "已逾期";
            case 4: return "已赔偿";
            default: return "未知";
        }
    }

    private void addTableCell(PdfPTable table, String text, com.itextpdf.text.Font font, boolean isHeader) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(8);
        if (isHeader) {
            cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
            cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        }
        table.addCell(cell);
    }
}
