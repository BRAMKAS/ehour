/*
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package net.rrm.ehour.ui.timesheet.export.excel.part;

import net.rrm.ehour.report.reports.ReportData;
import net.rrm.ehour.report.reports.element.FlatReportElement;
import net.rrm.ehour.report.reports.element.ReportElement;
import net.rrm.ehour.ui.common.report.Report;
import net.rrm.ehour.ui.common.report.excel.CellFactory;
import net.rrm.ehour.ui.common.report.excel.CellStyle;
import net.rrm.ehour.ui.common.report.excel.ExcelWorkbook;
import net.rrm.ehour.util.DateUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.ss.util.CellRangeAddress;

import java.util.*;

/**
 * Created on Mar 25, 2009, 6:35:04 AM
 *
 * @author Thies Edeling (thies@te-con.nl)
 */
public class ExportReportBody extends AbstractExportReportPart {
    public ExportReportBody(int cellMargin, HSSFSheet sheet, Report report, ExcelWorkbook workbook) {
        super(cellMargin, sheet, report, workbook);
    }

    @Override
    public int createPart(int rowNumber) {
        Map<Date, List<FlatReportElement>> dateMap = getElementsAsDateMap(getReport());
        List<Date> dateSequence = DateUtil.createDateSequence(getReport().getReportRange(), getConfig());

        rowNumber = createRowForDateSequence(rowNumber, dateMap, dateSequence);

        return rowNumber;
    }

    private int createRowForDateSequence(int rowNumber, Map<Date, List<FlatReportElement>> dateMap, List<Date> dateSequence) {
        for (Date date : dateSequence) {
            List<FlatReportElement> flatList = dateMap.get(date);

            boolean borderCells = isFirstDayOfWeek(date);

            rowNumber = !CollectionUtils.isEmpty(flatList) ? addColumnsToRow(date, flatList, rowNumber, borderCells) : addEmptyRow(rowNumber, date, borderCells);
        }
        return rowNumber;
    }

    private boolean isFirstDayOfWeek(Date date) {
        Calendar cal = GregorianCalendar.getInstance();
        cal.setTime(date);

        return cal.get(Calendar.DAY_OF_WEEK) == getConfig().getFirstDayOfWeek();
    }

    private int addEmptyRow(int rowNumber, Date date, boolean isBorder) {
        HSSFRow row = getSheet().createRow(rowNumber++);
        createDateCell(date, row, isBorder);

        if (isBorder) {
            CellStyle border = CellStyle.BORDER_NORTH_THIN;

            createEmptyCells(row, border);

            CellFactory.createCell(row, getCellMargin() + ExportReportColumn.CUSTOMER_CODE.getColumn(), getWorkbook(), border);
            CellFactory.createCell(row, getCellMargin() + ExportReportColumn.PROJECT.getColumn(), getWorkbook(), border);
            CellFactory.createCell(row, getCellMargin() + ExportReportColumn.PROJECT_CODE.getColumn(), getWorkbook(), border);
            CellFactory.createCell(row, getCellMargin() + ExportReportColumn.HOURS.getColumn(), getWorkbook(), border);
        }

        return rowNumber;
    }

    private int addColumnsToRow(Date date, List<FlatReportElement> elements, int rowNumber, boolean isBorder) {
        boolean addedForDate = false;

        for (FlatReportElement flatReportElement : elements) {
            HSSFRow row = getSheet().createRow(rowNumber);

            if (flatReportElement.getTotalHours() != null && flatReportElement.getTotalHours().doubleValue() >= 0.0) {
                createDateCell(date, row, isBorder);
                createActivityCell(flatReportElement.getProjectName(), row, isBorder);
                createProjectCodeCell(flatReportElement.getProjectCode(), row, isBorder);
                createHoursCell(flatReportElement.getTotalHours(), row, isBorder);
                createCustomerCodeCell(flatReportElement.getCustomerCode(), row, isBorder);

                if (isBorder) {
                    createEmptyCells(row, CellStyle.BORDER_NORTH_THIN);

                    getSheet().addMergedRegion(new CellRangeAddress(rowNumber, rowNumber, getCellMargin() + 3, getCellMargin() + 5));
                }

                rowNumber++;
                addedForDate = true;
            }
        }

        if (!addedForDate) {
            HSSFRow row = getSheet().createRow(rowNumber++);
            createDateCell(date, row, isBorder);
        }

        return rowNumber;

    }

    private HSSFCell createHoursCell(Number hours, HSSFRow row, boolean isBorder) {
        return CellFactory.createCell(row, getCellMargin() + ExportReportColumn.HOURS.getColumn(), hours, getWorkbook(), (isBorder) ? CellStyle.DIGIT_BORDER_NORTH_THIN : CellStyle.DIGIT);
    }

    private HSSFCell createActivityCell(String project, HSSFRow row, boolean isBorder) {
        return CellFactory.createCell(row, getCellMargin() + ExportReportColumn.PROJECT.getColumn(), project, getWorkbook(), (isBorder) ? CellStyle.BORDER_NORTH_THIN : CellStyle.NORMAL_FONT);
    }

    private HSSFCell createProjectCodeCell(String project, HSSFRow row, boolean isBorder) {
        return CellFactory.createCell(row, getCellMargin() + ExportReportColumn.PROJECT_CODE.getColumn(), project, getWorkbook(), (isBorder) ? CellStyle.BORDER_NORTH_THIN : CellStyle.NORMAL_FONT);
    }

    private HSSFCell createCustomerCodeCell(String customerCode, HSSFRow row, boolean isBorder) {
        return CellFactory.createCell(row, getCellMargin() + ExportReportColumn.CUSTOMER_CODE.getColumn(), customerCode, getWorkbook(), (isBorder) ? CellStyle.BORDER_NORTH_THIN : CellStyle.NORMAL_FONT);
    }

    private HSSFCell createDateCell(Date date, HSSFRow row, boolean isBorder) {
        return CellFactory.createCell(row, getCellMargin() + ExportReportColumn.DATE.getColumn(), getFormatter().format(date), getWorkbook(), (isBorder) ? CellStyle.DATE_BORDER_NORTH_THIN : CellStyle.DATE);
    }

    /**
     * Return a map with the key being the report's date and a list of a report elements for that date as the value
     */
    private Map<Date, List<FlatReportElement>> getElementsAsDateMap(Report report) {
        Map<Date, List<FlatReportElement>> flatMap = new TreeMap<Date, List<FlatReportElement>>();

        ReportData reportData = report.getReportData();

        for (ReportElement reportElement : reportData.getReportElements()) {
            FlatReportElement flat = (FlatReportElement) reportElement;

            Date date = DateUtil.nullifyTime(flat.getDayDate());

            List<FlatReportElement> dateElements;

            if (flatMap.containsKey(date)) {
                dateElements = flatMap.get(date);
            } else {
                dateElements = new ArrayList<FlatReportElement>();
            }

            dateElements.add(flat);

            flatMap.put(date, dateElements);
        }

        return flatMap;
    }
}
