//package com.example.FuelMemo.ReportModule.Service.Impl;
//
//import com.example.FuelMemo.FuelMemoModule.Entity.FuelMemo;
//import com.example.FuelMemo.ReportModule.DTO.*;
//import com.example.FuelMemo.ReportModule.Repository.FuelReportRepository;
//import com.example.FuelMemo.ReportModule.Service.FuelReportService;
//import com.example.FuelMemo.Shared.Response.DataResponse;
//import com.example.FuelMemo.Shared.Response.HttpResponse;
//
//import jakarta.persistence.*;
//import jakarta.persistence.criteria.*;
//import jakarta.transaction.Transactional;
//
//import lombok.RequiredArgsConstructor;
//
//import org.apache.poi.ss.usermodel.*;
//import org.apache.poi.xssf.usermodel.XSSFWorkbook;
//
//import org.springframework.data.domain.PageRequest;
//import org.springframework.http.*;
//import org.springframework.stereotype.Service;
//
//import org.springframework.data.domain.Pageable;
//import java.io.ByteArrayOutputStream;
//import java.math.BigDecimal;
//import java.time.LocalDate;
//import java.util.*;
//import java.util.stream.Collectors;
//
//@Service
//
//public class FuelReportServiceImpl implements FuelReportService {
//
//    public FuelReportServiceImpl(FuelReportRepository fuelReportRepository) {
//        this.fuelReportRepository = fuelReportRepository;
//    }
//
//    private final FuelReportRepository fuelReportRepository;
//
//    @PersistenceContext
//    private EntityManager entityManager;
//
//    // =========================================================
//    // ✅ COMMON EXCEL GENERATOR
//    // =========================================================
//    private ResponseEntity<byte[]> generateExcel(
//            String sheetName,
//            String[] headers,
//            List<List<Object>> data
//    ) {
//        try (Workbook workbook = new XSSFWorkbook()) {
//
//            Sheet sheet = workbook.createSheet(sheetName);
//
//            // Header Style
//            CellStyle headerStyle = workbook.createCellStyle();
//            Font font = workbook.createFont();
//            font.setBold(true);
//            headerStyle.setFont(font);
//
//            // Header Row
//            Row headerRow = sheet.createRow(0);
//            for (int i = 0; i < headers.length; i++) {
//                Cell cell = headerRow.createCell(i);
//                cell.setCellValue(headers[i]);
//                cell.setCellStyle(headerStyle);
//            }
//
//            // Data Rows
//            int rowIdx = 1;
//            for (List<Object> rowData : data) {
//                Row row = sheet.createRow(rowIdx++);
//                for (int col = 0; col < rowData.size(); col++) {
//                    Cell cell = row.createCell(col);
//                    Object value = rowData.get(col);
//                    if (value != null) {
//                        cell.setCellValue(value.toString());
//                    }
//                }
//            }
//
//            // Auto size
//            for (int i = 0; i < headers.length; i++) {
//                sheet.autoSizeColumn(i);
//            }
//
//            ByteArrayOutputStream out = new ByteArrayOutputStream();
//            workbook.write(out);
//
//            return ResponseEntity.ok()
//                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + sheetName + ".xlsx")
//                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
//                    .body(out.toByteArray());
//
//        } catch (Exception e) {
//            throw new RuntimeException("Excel generation failed", e);
//        }
//    }
//
//    // =========================================================
//    // ✅ SITE WISE REPORT (EXCEL)
//    // =========================================================
//    @Override
//    public ResponseEntity<byte[]> getSiteWiseFuelReportExcel(SiteFuelReportFilterDto filter) {
//
//        List<FuelMemo> memos;
//
//        if (filter.getFromDate() != null && filter.getToDate() != null) {
//            memos = fuelReportRepository
//                    .findByDeletedFalseAndDateBetween(filter.getFromDate(), filter.getToDate());
//        } else {
//            memos = fuelReportRepository.findByDeletedFalse();
//        }
//
//        if (filter.getSiteId() != null) {
//            memos = memos.stream()
//                    .filter(m -> m.getSite().getSiteId().equals(filter.getSiteId()))
//                    .collect(Collectors.toList());
//        }
//
//        Map<Integer, List<FuelMemo>> grouped =
//                memos.stream().collect(Collectors.groupingBy(m -> m.getSite().getSiteId()));
//
//        List<List<Object>> excelData = new ArrayList<>();
//
//        for (Map.Entry<Integer, List<FuelMemo>> entry : grouped.entrySet()) {
//
//            List<FuelMemo> siteMemos = entry.getValue();
//
//            String siteName = siteMemos.get(0).getSite().getSiteName();
//
//            double totalFuel = siteMemos.stream()
//                    .mapToDouble(m -> m.getQuantity().doubleValue())
//                    .sum();
//
//            Integer totalMemo = siteMemos.size();
//
//            excelData.add(Arrays.asList(
//                    entry.getKey(),
//                    siteName,
//                    totalFuel,
//                    totalMemo
//            ));
//        }
//
//        String[] headers = {"Site ID", "Site Name", "Total Fuel", "Total Memo"};
//
//        return generateExcel("SiteFuelReport", headers, excelData);
//    }
//
//    // =========================================================
//    // ✅ DETAIL REPORT (EXCEL)
//    // =========================================================
//    @Override
//    public ResponseEntity<byte[]> getFuelMemoDetailReportExcel(Integer companyId) {
//
//        List<FuelMemo> memos = fuelReportRepository
//                .findByCompany_CompanyIdAndDeletedFalseAndActiveTrue(companyId);
//
//        List<List<Object>> data = memos.stream()
//                .map(m -> {
//                    List<Object> row = new ArrayList<>();
//                    row.add(m.getSrNo());
//                    row.add(m.getDate());
//                    row.add(m.getVehicle() != null ? m.getVehicle().getVehicleNumber() : "");
//                    row.add(m.getDriver() != null ? m.getDriver().getDriverName() : "");
//                    row.add(m.getSite() != null ? m.getSite().getSiteName() : "");
//                    row.add(m.getFuelStation() != null ? m.getFuelStation().getFuelStationName() : "");
//                    row.add(m.getRoyaltyPass() != null ? m.getRoyaltyPass().getRoyaltyNumber() : "");
//                    row.add(m.getQuantity());
//                    row.add(m.getUser() != null ? m.getUser().getUserName() : "");
//                    row.add(m.getEntryType() != null ? m.getEntryType().name() : "");
//                    return row;
//                })
//                .collect(Collectors.toList());
//
//        String[] headers = {
//                "SR No", "Date", "Vehicle", "Driver", "Site",
//                "Fuel Station", "Royalty Pass", "Quantity",
//                "Prepared By", "Entry Type"
//        };
//
//        return generateExcel("FuelMemoDetail", headers, data);
//    }
//
//    // =========================================================
//    // ✅ SR NO FILTER (EXCEL)
//    // =========================================================
//    @Override
//    public ResponseEntity<byte[]> getFuelMemoDetailExcel(
//            Integer companyId,
//            Integer srNo
//    ) {
//
//        List<FuelMemoDetailReportDto> list = getFuelMemoDetailData(companyId, srNo);
//
//        List<List<Object>> data = list.stream()
//                .map(d -> {
//                    List<Object> row = new ArrayList<>();
//                    row.add(d.getSrNo());
//                    row.add(d.getDate());
//                    row.add(d.getVehicleNo());
//                    row.add(d.getDriverName());
//                    row.add(d.getSiteName());
//                    row.add(d.getFuelStationName());
//                    row.add(d.getRoyaltyPassNo());
//                    row.add(d.getQuantity());
//                    row.add(d.getPreparedBy());
//                    row.add(d.getEntryType());
//                    return row;
//                })
//                .toList();
//
//        String[] headers = {
//                "SR No", "Date", "Vehicle", "Driver",
//                "Site", "Fuel Station", "Royalty Pass",
//                "Quantity", "Prepared By", "Entry Type"
//        };
//
//        return generateExcel("FuelMemoReport", headers, data);
//    }
//    @Override
//    public List<FuelMemoDetailReportDto> getFuelMemoDetailData(Integer companyId, Integer srNo) {
//
//        List<FuelMemo> memos =
//                fuelReportRepository.findByCompany_CompanyIdAndSrNoAndDeletedFalseAndActiveTrue(companyId, srNo);
//
//        return memos.stream().map(m ->
//                FuelMemoDetailReportDto.builder()
//                        .srNo(m.getSrNo())
//                        .date(m.getDate())
//                        .vehicleNo(m.getVehicle() != null ? m.getVehicle().getVehicleNumber() : "")
//                        .driverName(m.getDriver() != null ? m.getDriver().getDriverName() : "")
//                        .siteName(m.getSite() != null ? m.getSite().getSiteName() : "")
//                        .fuelStationName(m.getFuelStation() != null ? m.getFuelStation().getFuelStationName() : "")
//                        .royaltyPassNo(m.getRoyaltyPass() != null ? m.getRoyaltyPass().getRoyaltyNumber() : "")
//                        .quantity(m.getQuantity())
//                        .preparedBy(m.getUser() != null ? m.getUser().getUserName() : "")
//                        .entryType(m.getEntryType() != null ? m.getEntryType().name() : "")
//                        .build()
//        ).toList();
//    }
//    @Override
//    public HttpResponse getFuelMemoDetailReport(
//            Integer companyId,
//            Integer srNo,
//            Integer pageNumber,
//            Integer pageSize,
//            String sortBy,
//            String sortDirection
//    ) {
//
//        List<FuelMemoDetailReportDto> dtos = new ArrayList<>();
//
//        try {
//
//            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
//            CriteriaQuery<FuelMemo> cq = cb.createQuery(FuelMemo.class);
//            Root<FuelMemo> root = cq.from(FuelMemo.class);
//
//            // Sorting
//            Order order = sortDirection.equalsIgnoreCase("asc") ?
//                    cb.asc(root.get(sortBy)) :
//                    cb.desc(root.get(sortBy));
//
//            List<Predicate> predicates = new ArrayList<>();
//
//            predicates.add(cb.equal(root.get("deleted"), false));
//            predicates.add(cb.equal(root.get("active"), true));
//
//            if (companyId != null) {
//                predicates.add(cb.equal(root.get("company").get("companyId"), companyId));
//            }
//
//            if (srNo != null) {
//                predicates.add(cb.equal(root.get("srNo"), srNo));
//            }
//
//            cq.where(predicates.toArray(new Predicate[0]));
//            cq.orderBy(order);
//
//            Pageable pageable = PageRequest.of(pageNumber, pageSize);
//
//            List<FuelMemo> resultList = entityManager.createQuery(cq)
//                    .setFirstResult((int) pageable.getOffset())
//                    .setMaxResults(pageable.getPageSize())
//                    .getResultList();
//
//            // ✅ COUNT QUERY (PERFORMANCE FIX)
//            CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
//            Root<FuelMemo> countRoot = countQuery.from(FuelMemo.class);
//            countQuery.select(cb.count(countRoot));
//            countQuery.where(predicates.toArray(new Predicate[0]));
//
//            Long totalCount = entityManager.createQuery(countQuery).getSingleResult();
//
//            double totalQty = 0;
//
//            for (FuelMemo m : resultList) {
//
//                double qty = m.getQuantity() != null ? m.getQuantity().doubleValue() : 0;
//                totalQty += qty;
//
//                dtos.add(
//                        FuelMemoDetailReportDto.builder()
//                                .srNo(m.getSrNo())
//                                .date(m.getDate())
//                                .vehicleNo(m.getVehicle() != null ? m.getVehicle().getVehicleNumber() : "")
//                                .driverName(m.getDriver() != null ? m.getDriver().getDriverName() : "")
//                                .siteName(m.getSite() != null ? m.getSite().getSiteName() : "")
//                                .fuelStationName(m.getFuelStation() != null ? m.getFuelStation().getFuelStationName() : "")
//                                .royaltyPassNo(m.getRoyaltyPass() != null ? m.getRoyaltyPass().getRoyaltyNumber() : "")
//                                .quantity(m.getQuantity())
//                                .preparedBy(m.getUser() != null ? m.getUser().getUserName() : "")
//                                .entryType(m.getEntryType() != null ? m.getEntryType().name() : "")
//                                .build()
//                );
//            }
//
//            // ✅ TOTAL ROW
//            dtos.add(
//                    FuelMemoDetailReportDto.builder()
//                            .vehicleNo("TOTAL")
//                            .quantity(BigDecimal.valueOf(totalQty))
//                            .build()
//            );
//
//            return HttpResponse.builder()
//                    .success(true)
//                    .successCode(HttpStatus.OK)
//                    .data(dtos)
//                    .pageNumber(pageNumber)
//                    .pageSize(pageSize)
//                    .totalElements(totalCount)
//                    .totalPages((int) Math.ceil((double) totalCount / pageSize))
//                    .isLastPage(pageNumber >= (int) Math.ceil((double) totalCount / pageSize) - 1)
//                    .build();
//
//        } catch (Exception e) {
//            throw new RuntimeException("Error fetching Fuel Memo Report", e);
//        }
//    }
//
//    // =========================================================
//    // ✅ DATE WISE REPORT (CRITERIA + EXCEL)
//    // =========================================================
//    @Override
//    @Transactional
//    public ResponseEntity<byte[]> getDateWiseFuelMemoReportExcel(LocalDate fromDate, LocalDate toDate) {
//
//        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
//        CriteriaQuery<DateWiseFuelMemoReportDto> cq = cb.createQuery(DateWiseFuelMemoReportDto.class);
//
//        Root<FuelMemo> fm = cq.from(FuelMemo.class);
//
//        Join<Object, Object> vehicle = fm.join("vehicle");
//        Join<Object, Object> driver = fm.join("driver");
//        Join<Object, Object> site = fm.join("site");
//        Join<Object, Object> fuelStation = fm.join("fuelStation");
//
//        cq.select(cb.construct(
//                DateWiseFuelMemoReportDto.class,
//                fm.get("date"),
//                vehicle.get("vehicleNumber"),
//                driver.get("driverName"),
//                site.get("siteName"),
//                fuelStation.get("fuelStationName"),
//                fm.get("quantity")
//        ));
//
//        cq.where(cb.between(fm.get("date"), fromDate, toDate));
//
//        List<DateWiseFuelMemoReportDto> result =
//                entityManager.createQuery(cq).getResultList();
//
//        List<List<Object>> data = result.stream()
//                .map(r -> {
//                    List<Object> row = new ArrayList<>();
//                    row.add(r.getDate());
//                    row.add(r.getVehicleNumber()); // ✅ fixed
//                    row.add(r.getDriverName());
//                    row.add(r.getSiteName());
//                    row.add(r.getFuelStationName());
//                    row.add(r.getQuantity());
//                    return row;
//                })
//                .collect(Collectors.toList());
//
//        String[] headers = {
//                "Date", "Vehicle", "Driver",
//                "Site", "Fuel Station", "Quantity"
//        };
//
//        return generateExcel("DateWiseReport", headers, data);
//    }
//}
package com.example.FuelMemo.ReportModule.Service.Impl;

import com.example.FuelMemo.FuelMemoModule.Entity.FuelMemo;
import com.example.FuelMemo.ReportModule.DTO.*;
import com.example.FuelMemo.ReportModule.Repository.FuelReportRepository;
import com.example.FuelMemo.ReportModule.Service.FuelReportService;
import com.example.FuelMemo.Shared.Response.HttpResponse;

import jakarta.persistence.*;
import jakarta.persistence.criteria.*;

import jakarta.transaction.Transactional;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FuelReportServiceImpl implements FuelReportService {

    private final FuelReportRepository fuelReportRepository;

    public FuelReportServiceImpl(FuelReportRepository fuelReportRepository) {
        this.fuelReportRepository = fuelReportRepository;
    }

    @PersistenceContext
    private EntityManager entityManager;

    // =========================================================
    // ✅ COMMON EXCEL GENERATOR
    // =========================================================
    private ResponseEntity<byte[]> generateExcel(
            String sheetName,
            String[] headers,
            List<List<Object>> data
    ) {
        try (Workbook workbook = new XSSFWorkbook()) {

            Sheet sheet = workbook.createSheet(sheetName);

            // ================= COMMON BORDER STYLE =================
            CellStyle baseStyle = workbook.createCellStyle();
            baseStyle.setBorderBottom(BorderStyle.THIN);
            baseStyle.setBorderTop(BorderStyle.THIN);
            baseStyle.setBorderLeft(BorderStyle.THIN);
            baseStyle.setBorderRight(BorderStyle.THIN);

            // ================= HEADER STYLE =================
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.cloneStyleFrom(baseStyle);
            headerStyle.setFont(headerFont);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            // ================= DATE STYLE =================
            CellStyle dateStyle = workbook.createCellStyle();
            CreationHelper createHelper = workbook.getCreationHelper();
            dateStyle.cloneStyleFrom(baseStyle);
            dateStyle.setDataFormat(
                    createHelper.createDataFormat().getFormat("dd-MM-yyyy")
            );

            // ================= QUANTITY STYLE =================
            CellStyle qtyStyle = workbook.createCellStyle();
            DataFormat format = workbook.createDataFormat();
            qtyStyle.cloneStyleFrom(baseStyle);
            qtyStyle.setDataFormat(format.getFormat("#,##0.00"));
            Font qtyFont = workbook.createFont();
            qtyFont.setBold(true);
            qtyStyle.setFont(qtyFont);
            qtyStyle.setAlignment(HorizontalAlignment.RIGHT);

            // ================= TOTAL LABEL STYLE =================
            CellStyle totalLabelStyle = workbook.createCellStyle();
            Font totalFont = workbook.createFont();
            totalFont.setBold(true);
            totalLabelStyle.cloneStyleFrom(baseStyle);
            totalLabelStyle.setFont(totalFont);
            totalLabelStyle.setAlignment(HorizontalAlignment.RIGHT);

            // ================= HEADER =================
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            sheet.createFreezePane(0, 1);

            // ================= DATA =================
            int rowIdx = 1;
            double totalQty = 0;

            for (List<Object> rowData : data) {
                Row row = sheet.createRow(rowIdx++);

                for (int col = 0; col < rowData.size(); col++) {

                    Cell cell = row.createCell(col);
                    Object value = rowData.get(col);

                    if (value == null) {
                        cell.setCellStyle(baseStyle);
                        continue;
                    }

                    if (value instanceof LocalDate) {
                        cell.setCellValue(java.sql.Date.valueOf((LocalDate) value));
                        cell.setCellStyle(dateStyle);
                    }

                    else if (col == 7 && value instanceof Number) {
                        double qty = ((Number) value).doubleValue();
                        cell.setCellValue(qty);
                        totalQty += qty;
                        cell.setCellStyle(qtyStyle);
                    }

                    else {
                        cell.setCellValue(value.toString());
                        cell.setCellStyle(baseStyle);
                    }
                }
            }

            // ================= TOTAL ROW =================
            Row totalRow = sheet.createRow(rowIdx);

            for (int i = 0; i < headers.length; i++) {
                Cell cell = totalRow.createCell(i);

                if (i == 6) {
                    cell.setCellValue("TOTAL");
                    cell.setCellStyle(totalLabelStyle);
                } else if (i == 7) {
                    cell.setCellValue(totalQty);
                    cell.setCellStyle(qtyStyle);
                } else {
                    cell.setCellStyle(totalLabelStyle);
                }
            }

            sheet.addMergedRegion(new CellRangeAddress(
                    totalRow.getRowNum(),
                    totalRow.getRowNum(),
                    0,
                    6
            ));

            // ================= AUTO SIZE =================
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // ================= OUTPUT =================
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=" + sheetName + ".xlsx")
                    .contentType(MediaType.parseMediaType(
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                    ))
                    .body(out.toByteArray());

        } catch (Exception e) {
            throw new RuntimeException("Excel generation failed", e);
        }
    }

    // =========================================================
    // ✅ JSON DATA (REUSABLE)
    // =========================================================
    @Override
    public List<FuelMemoDetailReportDto> getFuelMemoDetailData(Integer companyId, Integer srNo) {

        List<FuelMemo> memos;

        if (srNo != null) {
            memos = fuelReportRepository
                    .findByCompany_CompanyIdAndSrNoAndDeletedFalseAndActiveTrue(companyId, srNo);
        } else {
            memos = fuelReportRepository
                    .findByCompany_CompanyIdAndDeletedFalseAndActiveTrue(companyId);
        }
        return memos.stream().map(m ->
                FuelMemoDetailReportDto.builder()
                        .srNo(m.getSrNo())
                        .date(m.getDate())
                        .vehicleNo(m.getVehicle() != null ? m.getVehicle().getVehicleNumber() : "")
                        .driverName(m.getDriver() != null ? m.getDriver().getDriverName() : "")
                        .siteName(m.getSite() != null ? m.getSite().getSiteName() : "")
                        .fuelStationName(m.getFuelStation() != null ? m.getFuelStation().getFuelStationName() : "")
                        .royaltyPassNo(m.getRoyaltyPass() != null ? m.getRoyaltyPass().getRoyaltyNumber() : "")
                        .quantity(m.getQuantity())
                        .preparedBy(m.getUser() != null ? m.getUser().getUserName() : "")
                        .entryType(m.getEntryType() != null ? m.getEntryType().name() : "")
                        .build()
        ).toList();
    }

    // =========================================================
    // ✅ EXCEL REPORT
    // =========================================================
    @Override
    public ResponseEntity<byte[]> getFuelMemoDetailExcel(Integer companyId, Integer srNo) {

        List<FuelMemoDetailReportDto> list = getFuelMemoDetailData(companyId, srNo);

        List<List<Object>> data = new ArrayList<>();

        for (FuelMemoDetailReportDto d : list) {

            List<Object> row = new ArrayList<>();

            row.add(d.getSrNo());
            row.add(d.getDate());
            row.add(d.getVehicleNo());
            row.add(d.getDriverName());
            row.add(d.getSiteName());
            row.add(d.getFuelStationName());
            row.add(d.getRoyaltyPassNo());
            row.add(d.getQuantity());
            row.add(d.getPreparedBy());
            row.add(d.getEntryType());

            data.add(row);
        }

        // ✅ Remove manual TOTAL row addition here
        // generateExcel will calculate total automatically

        String[] headers = {
                "SR No", "Date", "Vehicle", "Driver",
                "Site", "Fuel Station", "Royalty Pass",
                "Quantity", "Prepared By", "Entry Type"
        };

        return generateExcel("FuelMemoReport", headers, data);
    }

    // =========================================================
    // ✅ JSON REPORT WITH PAGINATION + TOTAL
    // =========================================================
    @Override
    @Transactional
    public HttpResponse getFuelMemoDetailReport(
            Integer companyId,
            Integer srNo,
            Integer pageNumber,
            Integer pageSize,
            String sortBy,
            String sortDirection
    ) {

        List<FuelMemoDetailReportDto> dtos = new ArrayList<>();

        try {

            CriteriaBuilder cb = entityManager.getCriteriaBuilder();

            // ================= MAIN QUERY =================
            CriteriaQuery<FuelMemo> cq = cb.createQuery(FuelMemo.class);
            Root<FuelMemo> root = cq.from(FuelMemo.class);

            // ✅ FETCH JOIN (Fix Lazy Loading Issue)
            root.fetch("vehicle", JoinType.LEFT);
            root.fetch("driver", JoinType.LEFT);
            root.fetch("site", JoinType.LEFT);
            root.fetch("fuelStation", JoinType.LEFT);
            root.fetch("royaltyPass", JoinType.LEFT);
            root.fetch("user", JoinType.LEFT);

            cq.distinct(true); // important with fetch join

            // ================= SORT =================
            List<String> allowedSortFields = List.of("date", "srNo");

            if (!allowedSortFields.contains(sortBy)) {
                sortBy = "date";
            }

            Order order = "asc".equalsIgnoreCase(sortDirection)
                    ? cb.asc(root.get(sortBy))
                    : cb.desc(root.get(sortBy));

            // ================= FILTER =================
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.isFalse(root.get("deleted")));
            predicates.add(cb.isTrue(root.get("active")));

            if (companyId != null) {
                predicates.add(cb.equal(root.get("company").get("companyId"), companyId));
            }

            if (srNo != null) {
                predicates.add(cb.equal(root.get("srNo"), srNo));
            }

            cq.where(predicates.toArray(new Predicate[0]));
            cq.orderBy(order);

            Pageable pageable = PageRequest.of(pageNumber, pageSize);

            List<FuelMemo> resultList = entityManager.createQuery(cq)
                    .setFirstResult((int) pageable.getOffset())
                    .setMaxResults(pageable.getPageSize())
                    .getResultList();

            // ================= COUNT QUERY (FIXED) =================
            CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
            Root<FuelMemo> countRoot = countQuery.from(FuelMemo.class);

            List<Predicate> countPredicates = new ArrayList<>();

            countPredicates.add(cb.isFalse(countRoot.get("deleted")));
            countPredicates.add(cb.isTrue(countRoot.get("active")));

            if (companyId != null) {
                countPredicates.add(cb.equal(countRoot.get("company").get("companyId"), companyId));
            }

            if (srNo != null) {
                countPredicates.add(cb.equal(countRoot.get("srNo"), srNo));
            }

            countQuery.select(cb.countDistinct(countRoot));
            countQuery.where(countPredicates.toArray(new Predicate[0]));

            Long totalCount = entityManager.createQuery(countQuery).getSingleResult();

            // ================= DTO MAPPING =================
            double totalQty = 0;

            for (FuelMemo m : resultList) {

                double qty = m.getQuantity() != null ? m.getQuantity().doubleValue() : 0;
                totalQty += qty;

                dtos.add(
                        FuelMemoDetailReportDto.builder()
                                .srNo(m.getSrNo())
                                .date(m.getDate())
                                .vehicleNo(m.getVehicle() != null ? m.getVehicle().getVehicleNumber() : "")
                                .driverName(m.getDriver() != null ? m.getDriver().getDriverName() : "")
                                .siteName(m.getSite() != null ? m.getSite().getSiteName() : "")
                                .fuelStationName(m.getFuelStation() != null ? m.getFuelStation().getFuelStationName() : "")
                                .royaltyPassNo(m.getRoyaltyPass() != null ? m.getRoyaltyPass().getRoyaltyNumber() : "")
                                .quantity(m.getQuantity())
                                .preparedBy(m.getUser() != null ? m.getUser().getUserName() : "")
                                .entryType(m.getEntryType() != null ? m.getEntryType().name() : "")
                                .build()
                );
            }

            int totalPages = (int) Math.ceil((double) totalCount / pageSize);

            // ================= RESPONSE =================
            Map<String, Object> response = new HashMap<>();
            response.put("rows", dtos);
            response.put("totalQty", totalQty);

            return HttpResponse.builder()
                    .success(true)
                    .successCode(HttpStatus.OK)
                    .data(response)
                    .pageNumber(pageNumber)
                    .pageSize(pageSize)
                    .totalElements(totalCount)
                    .totalPages(totalPages)
                    .isLastPage(pageNumber >= totalPages - 1)
                    .build();

        } catch (Exception e) {
            e.printStackTrace(); // 🔥 debug
            throw new RuntimeException("Error fetching Fuel Memo Report: " + e.getMessage(), e);
        }
    }

    @Override
    public ResponseEntity<byte[]> getSiteWiseFuelReportExcel(SiteFuelReportFilterDto filter) {

        HttpResponse<List<SiteFuelReportDto>> response = getSiteWiseFuelReportData(filter);

        List<SiteFuelReportDto> result = response.getData();

        List<List<Object>> excelData = result.stream()
                .map(r -> {
                    List<Object> row = new ArrayList<>();
                    row.add(r.getSiteId());
                    row.add(r.getSiteName());
                    row.add(r.getTotalFuel());
                    row.add(r.getTotalMemo());
                    return row;
                })
                .collect(Collectors.toList());

        String[] headers = {"Site ID", "Site Name", "Total Fuel", "Total Memo"};

        return generateExcel("SiteFuelReport", headers, excelData);
    }
    @Override
    public HttpResponse<List<SiteFuelReportDto>> getSiteWiseFuelReportData(SiteFuelReportFilterDto filter) {

        List<FuelMemo> memos;

        if (filter.getFromDate() != null && filter.getToDate() != null) {
            memos = fuelReportRepository
                    .findByDeletedFalseAndDateBetween(filter.getFromDate(), filter.getToDate());
        } else {
            memos = fuelReportRepository.findByDeletedFalse();
        }

        if (filter.getSiteId() != null) {
            memos = memos.stream()
                    .filter(m -> m.getSite() != null &&
                            m.getSite().getSiteId().equals(filter.getSiteId()))
                    .collect(Collectors.toList());
        }

        Map<Integer, List<FuelMemo>> grouped =
                memos.stream()
                        .filter(m -> m.getSite() != null)
                        .collect(Collectors.groupingBy(m -> m.getSite().getSiteId()));

        List<SiteFuelReportDto> result = new ArrayList<>();

        for (Map.Entry<Integer, List<FuelMemo>> entry : grouped.entrySet()) {

            List<FuelMemo> siteMemos = entry.getValue();
            if (siteMemos.isEmpty()) continue;

            FuelMemo first = siteMemos.get(0);

            String siteName = first.getSite() != null
                    ? first.getSite().getSiteName()
                    : "Unknown";

            double totalFuel = siteMemos.stream()
                    .mapToDouble(m -> m.getQuantity() != null ? m.getQuantity().doubleValue() : 0)
                    .sum();

            Integer totalMemo = siteMemos.size();

            result.add(new SiteFuelReportDto(
                    entry.getKey(),
                    siteName,
                    totalFuel,
                    totalMemo
            ));
        }

        // ✅ FINAL RESPONSE
        return HttpResponse.<List<SiteFuelReportDto>>builder()
                .success(true)
                .successCode(HttpStatus.OK)
                .message("Site wise fuel report fetched successfully")
                .data(result)
                .build();
    }

    @Override
    @Transactional
    public ResponseEntity<byte[]> getDateWiseFuelMemoReportExcel(
            Integer companyId,
            LocalDate fromDate,
            LocalDate toDate) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<DateWiseFuelMemoReportDto> cq = cb.createQuery(DateWiseFuelMemoReportDto.class);

        Root<FuelMemo> fm = cq.from(FuelMemo.class);

        Join<Object, Object> vehicle = fm.join("vehicle");
        Join<Object, Object> driver = fm.join("driver");
        Join<Object, Object> site = fm.join("site");
        Join<Object, Object> fuelStation = fm.join("fuelStation");

        cq.select(cb.construct(
                DateWiseFuelMemoReportDto.class,
                fm.get("date"),
                vehicle.get("vehicleNumber"),
                driver.get("driverName"),
                site.get("siteName"),
                fuelStation.get("fuelStationName"),
                fm.get("quantity")
        ));

        List<Predicate> predicates = new ArrayList<>();

        predicates.add(cb.between(fm.get("date"), fromDate, toDate));

        if (companyId != null) {
            predicates.add(cb.equal(fm.get("company").get("companyId"), companyId));
        }

        cq.where(predicates.toArray(new Predicate[0]));
        List<DateWiseFuelMemoReportDto> result =
                entityManager.createQuery(cq).getResultList();

        List<List<Object>> data = result.stream()
                .map(r -> {
                    List<Object> row = new ArrayList<>();
                    row.add(r.getDate());
                    row.add(r.getVehicleNumber()); // ✅ fixed
                    row.add(r.getDriverName());
                    row.add(r.getSiteName());
                    row.add(r.getFuelStationName());
                    row.add(r.getQuantity());
                    return row;
                })
                .collect(Collectors.toList());

        String[] headers = {
                "Date", "Vehicle", "Driver",
                "Site", "Fuel Station", "Quantity"
        };

        return generateExcel("DateWiseReport", headers, data);
    }


}