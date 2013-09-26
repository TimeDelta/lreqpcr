/*
 * Copyright (C) 2013   Bob Rutledge
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 * and open the template in the editor.
 */
package org.lreqpcr.data_export_provider;

import java.awt.Desktop;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import javax.swing.JOptionPane;
import jxl.Workbook;
import jxl.format.Alignment;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
import jxl.format.Colour;
import jxl.write.*;
import jxl.write.Number;
import org.lreqpcr.core.data_objects.AverageSampleProfile;
import org.lreqpcr.core.utilities.IOUtilities;
import org.openide.windows.WindowManager;

/**
 *
 * @author Bob Rutledge
 */
public class ExcelAverageSampleProfileDataExport {

    /**
     *
     */
    @SuppressWarnings("unchecked")
    public static void exportProfiles(HashMap<String, List<AverageSampleProfile>> groupList) throws IOException, WriteException {
        //Setup the the workbook based on the file choosen by the user
        File selectedFile = IOUtilities.newExcelFile();
        if (selectedFile == null) {
            return;
        }
        WritableWorkbook workbook;
        try {
            workbook = Workbook.createWorkbook(selectedFile);
        } catch (Exception e) {
            Toolkit.getDefaultToolkit().beep();
            String msg = "The file '" + selectedFile.getName()
                    + "' could not be opened, possibly because it is already open.";
            JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(),
                    msg, "Unable to open " + selectedFile.getName(),
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        //Setup cell formatting
        WritableFont arialBold = new WritableFont(WritableFont.ARIAL, 10, WritableFont.BOLD);
        WritableFont arial = new WritableFont(WritableFont.ARIAL, 10);
        WritableCellFormat boldRight = new WritableCellFormat(arialBold);
        boldRight.setAlignment(Alignment.RIGHT);
        WritableCellFormat boldLeft = new WritableCellFormat(arialBold);
        boldLeft.setAlignment(Alignment.LEFT);
        WritableCellFormat boldCenter = new WritableCellFormat(arialBold);
        boldCenter.setAlignment(Alignment.CENTRE);
        WritableCellFormat center = new WritableCellFormat(arial);
        center.setAlignment(Alignment.CENTRE);
        WritableCellFormat leftYellow = new WritableCellFormat(arial);
        leftYellow.setAlignment(Alignment.LEFT);
        leftYellow.setBackground(Colour.YELLOW);
        WritableCellFormat centerBoldUnderline = new WritableCellFormat(arialBold);
        centerBoldUnderline.setAlignment(Alignment.CENTRE);
        centerBoldUnderline.setBorder(Border.BOTTOM, BorderLineStyle.DOUBLE, Colour.BLACK);

        WritableCellFormat integerFormat = new WritableCellFormat(NumberFormats.INTEGER);
        integerFormat.setAlignment(Alignment.CENTRE);
        WritableCellFormat floatFormat = new WritableCellFormat(NumberFormats.FLOAT);
        floatFormat.setAlignment(Alignment.CENTRE);
        WritableCellFormat percentFormat = new WritableCellFormat(NumberFormats.PERCENT_FLOAT);
        WritableCellFormat exponentialFormat = new WritableCellFormat(NumberFormats.EXPONENTIAL);
        NumberFormat nf = new NumberFormat("###,###");
        WritableCellFormat commaSep = new WritableCellFormat(nf);
        NumberFormat nfOCF = new NumberFormat("###,###.000");
        WritableCellFormat ocfFormat = new WritableCellFormat(nfOCF);

        int pageCounter = 0;
        //This is to sort the resulting worksheets
        List<String> nameArray = new ArrayList<String>(groupList.keySet());
        Collections.sort(nameArray);
        for (String pageName : nameArray) {
//Test whether the run name is >30 characters as this can create identical page names
            if (pageName.length() > 30) {
                Toolkit.getDefaultToolkit().beep();
                String msg = "The Parent name ''" + pageName + "'' is longer that 30 characters.\n"
                        + "The will cause the worksheet name to be truncated."
                        + "\nNote also that identical run names will generate an Excel error."
                        + "\nIf this occurs, select ''Yes'' in the resulting dialog box.";
                JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(),
                        msg, "Parent name is too long",
                        JOptionPane.WARNING_MESSAGE);
            }
            WritableSheet sheet = workbook.createSheet(pageName, pageCounter);

            Label label = new Label(1, 0, "Name:", boldRight);
            sheet.addCell(label);
            label = new Label(0, 2, "Run Date", centerBoldUnderline);
            sheet.addCell(label);
            label = new Label(1, 2, "Amplicon", centerBoldUnderline);
            sheet.addCell(label);
            label = new Label(2, 2, "Sample", centerBoldUnderline);
            sheet.addCell(label);
            label = new Label(3, 2, "No", centerBoldUnderline);
            sheet.addCell(label);
            label = new Label(4, 2, "Emax", centerBoldUnderline);
            sheet.addCell(label);
            label = new Label(5, 2, "LRE-Emax", centerBoldUnderline);
            sheet.addCell(label);
            label = new Label(6, 2, "C1/2", centerBoldUnderline);
            sheet.addCell(label);
            label = new Label(7, 2, "Fmax", centerBoldUnderline);
            sheet.addCell(label);
            label = new Label(8, 2, "Av. Amp Tm", centerBoldUnderline);
            sheet.addCell(label);
            label = new Label(9, 2, "Amp Size", centerBoldUnderline);
            sheet.addCell(label);
            label = new Label(10, 2, "OCF", centerBoldUnderline);
            sheet.addCell(label);
            label = new Label(11, 2, "Notes", centerBoldUnderline);
            sheet.addCell(label);
            int row = 3;
            label = new Label(2, 0, pageName);
            sheet.addCell(label);

            Number number;
            DateFormat customDateFormat = new DateFormat("ddMMMyy");

            List<AverageSampleProfile> profileList = groupList.get(pageName);
            Collections.sort(profileList);
            for (AverageSampleProfile avProfile : profileList) {
                if (avProfile.isTargetQuantityNormalizedToFmax()) {
                    label = new Label(2, 1, "Target quantities are normalized to the run's average Fmax", boldLeft);
                    sheet.addCell(label);
                }
                WritableCellFormat dateFormat = new WritableCellFormat(customDateFormat);
                dateFormat.setAlignment(Alignment.CENTRE);
                DateTime dateCell = new DateTime(0, row, avProfile.getRunDate(), dateFormat);
                sheet.addCell(dateCell);
                label = new Label(1, row, avProfile.getAmpliconName());
                sheet.addCell(label);
                label = new Label(2, row, avProfile.getSampleName());
                sheet.addCell(label);
                if (avProfile.isExcluded()) {
                    //All replicate profiles have been excluded
                    label = new Label(3, row, "nd", center);
                    sheet.addCell(label);
                    String s;
                    if (avProfile.getLongDescription() != null) {
                        s = "EXCLUDED... " + avProfile.getLongDescription();
                    } else {
                        s = "EXCLUDED ";
                    }
                    label = new Label(11, row, s, leftYellow);
                    sheet.addCell(label);
                    row++;
                    continue;
                } else {
                    if (avProfile.isTheReplicateAverageNoLessThan10Molecules()) {
                        //Target quantity == replicate average No
                        number = new Number(3, row, avProfile.getNo(), integerFormat);
                        sheet.addCell(number);
//Leave all other values empty but put a statement in the notes denoting that the No <10
                        label = new Label(4, row, "LRE-derived");
                        sheet.addCell(label);
                        label = new Label(5, row, "na <10N");
                        sheet.addCell(label);
                        if (avProfile.calculateAvAmpTm() != -1) {
                            number = new Number(8, row, avProfile.calculateAvAmpTm(), floatFormat);
                            sheet.addCell(number);
                        }
                        number = new Number(9, row, avProfile.getAmpliconSize(), integerFormat);
                        sheet.addCell(number);
                        number = new Number(10, row, avProfile.getOCF(), floatFormat);
                        sheet.addCell(number);
                        if (avProfile.getLongDescription() != null) {
                            label = new Label(11, row, avProfile.getLongDescription());
                            sheet.addCell(label);
                        }
                        //Move to the next average profile
                        row++;
                        continue;
                    } else {
                        number = new Number(3, row, avProfile.getNo(), integerFormat);
                        sheet.addCell(number);
                    }
                        label = new Label(4, row, "LRE-derived");
                        sheet.addCell(label);
                    if (avProfile.getEmax() != 0) {
                        number = new Number(5, row, avProfile.getEmax(), percentFormat);
                        sheet.addCell(number);
                    }
                    if (avProfile.getMidC() != 0) {
                        number = new Number(6, row, avProfile.getMidC(), floatFormat);
                        sheet.addCell(number);
                    }
                    if (avProfile.getEmax() != 0) {
                        double fmax = (avProfile.getEmax() / avProfile.getDeltaE()) * -1;
                        number = new Number(7, row, fmax, floatFormat);
                        sheet.addCell(number);
                    }
                    if (avProfile.calculateAvAmpTm() != -1) {
                        number = new Number(8, row, avProfile.calculateAvAmpTm(), floatFormat);
                        sheet.addCell(number);
                    }
                    number = new Number(9, row, avProfile.getAmpliconSize(), integerFormat);
                    sheet.addCell(number);
                    number = new Number(10, row, avProfile.getOCF(), floatFormat);
                    sheet.addCell(number);
                    if (avProfile.getLongDescription() != null) {
                        label = new Label(11, row, avProfile.getLongDescription());
                        sheet.addCell(label);
                    }
                    row++;
                }
            }
            pageCounter++;
        }
        workbook.write();
        workbook.close();
        Desktop desktop;
        if (Desktop.isDesktopSupported()) {
            desktop = Desktop.getDesktop();
            desktop.open(selectedFile);
        }
    }
}
