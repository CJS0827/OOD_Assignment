package ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.Font;
import java.awt.Rectangle;
import java.io.*;

// iText 5 imports — make sure itextpdf-5.5.13.3.jar is in your Eclipse build path
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

/**
 * ReceiptWindow shows a formatted receipt after payment is collected.
 * Counter staff can download the receipt as a professional PDF file.
 *
 * OOP Concepts:
 * - Encapsulation: all receipt fields are private; generatePDF() is private
 *                  — PDF generation logic is hidden from outside classes
 * - Inheritance: extends JFrame
 * - Abstraction: CollectPayment just calls new ReceiptWindow(...) without
 *                knowing anything about how the PDF is created
 */
public class ReceiptWindow extends JFrame {

    // Private fields — Encapsulation: stored for use in generatePDF()
    private String paymentID, appointmentID, customerName, technicianName;
    private String serviceType, carPlate, vehicleModel, date, time, paymentMethod;
    private double amount;

    public ReceiptWindow(
            String paymentID,
            String appointmentID,
            String customerName,
            String technicianName,
            String serviceType,
            String carPlate,
            String vehicleModel,
            String date,
            String time,
            String paymentMethod,
            double amount) {

        // Store fields for PDF generation
        this.paymentID      = paymentID;
        this.appointmentID  = appointmentID;
        this.customerName   = customerName;
        this.technicianName = technicianName;
        this.serviceType    = serviceType;
        this.carPlate       = carPlate;
        this.vehicleModel   = vehicleModel;
        this.date           = date;
        this.time           = time;
        this.paymentMethod  = paymentMethod;
        this.amount         = amount;

        setTitle("Receipt - " + paymentID);
        setSize(420, 620);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(false);
        setLocationRelativeTo(null);

        // ── Main panel ──
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(new EmptyBorder(20, 30, 20, 30));

        // ── Header ──
        JLabel shopName = new JLabel("APU Automotive Service Centre");
        shopName.setFont(new Font("Arial", Font.BOLD, 15));
        shopName.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(shopName);

        JLabel shopSub = new JLabel("APU – ASC");
        shopSub.setFont(new Font("Arial", Font.PLAIN, 11));
        shopSub.setForeground(Color.GRAY);
        shopSub.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(shopSub);

        mainPanel.add(Box.createVerticalStrut(5));
        mainPanel.add(makeDivider());
        mainPanel.add(Box.createVerticalStrut(5));

        JLabel receiptTitle = new JLabel("OFFICIAL RECEIPT");
        receiptTitle.setFont(new Font("Arial", Font.BOLD, 13));
        receiptTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(receiptTitle);

        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(makeDivider());
        mainPanel.add(Box.createVerticalStrut(8));

        // ── Details ──
        mainPanel.add(makeRow("Payment ID",     paymentID));
        mainPanel.add(makeRow("Appointment ID", appointmentID));
        mainPanel.add(Box.createVerticalStrut(6));
        mainPanel.add(makeDivider());
        mainPanel.add(Box.createVerticalStrut(6));

        mainPanel.add(makeRow("Customer",  customerName));
        mainPanel.add(makeRow("Car Plate", carPlate));
        mainPanel.add(makeRow("Vehicle",   vehicleModel));
        mainPanel.add(Box.createVerticalStrut(6));
        mainPanel.add(makeDivider());
        mainPanel.add(Box.createVerticalStrut(6));

        mainPanel.add(makeRow("Service Type", serviceType));
        mainPanel.add(makeRow("Technician",   technicianName));
        mainPanel.add(makeRow("Date",         date));
        mainPanel.add(makeRow("Time",         time));
        mainPanel.add(Box.createVerticalStrut(6));
        mainPanel.add(makeDivider());
        mainPanel.add(Box.createVerticalStrut(6));

        mainPanel.add(makeRow("Payment Method", paymentMethod));

        // ── Amount ──
        mainPanel.add(Box.createVerticalStrut(6));
        JPanel amountPanel = new JPanel(new BorderLayout());
        amountPanel.setBackground(new Color(230, 245, 255));
        amountPanel.setBorder(BorderFactory.createLineBorder(new Color(100, 180, 255)));
        amountPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        JLabel lblAmountTitle = new JLabel("  TOTAL AMOUNT");
        lblAmountTitle.setFont(new Font("Arial", Font.BOLD, 13));
        amountPanel.add(lblAmountTitle, BorderLayout.WEST);

        JLabel lblAmount = new JLabel(String.format("RM %.2f  ", amount));
        lblAmount.setFont(new Font("Arial", Font.BOLD, 15));
        lblAmount.setForeground(new Color(0, 100, 0));
        amountPanel.add(lblAmount, BorderLayout.EAST);

        mainPanel.add(amountPanel);
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(makeDivider());
        mainPanel.add(Box.createVerticalStrut(8));

        // ── Thank you ──
        JLabel thanks = new JLabel("Thank you for choosing APU – ASC!");
        thanks.setFont(new Font("Arial", Font.ITALIC, 11));
        thanks.setForeground(Color.GRAY);
        thanks.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(thanks);

        mainPanel.add(Box.createVerticalStrut(15));

        // ── Buttons: Download PDF + Close ──
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        btnPanel.setBackground(Color.WHITE);
        btnPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        JButton btnDownload = new JButton("⬇  Download Receipt (PDF)");
        btnDownload.setFont(new Font("Arial", Font.BOLD, 12));
        btnDownload.setBackground(new Color(180, 30, 30));  // red — PDF colour
        btnDownload.setForeground(Color.WHITE);
        btnDownload.setFocusPainted(false);
        btnDownload.setPreferredSize(new Dimension(220, 34));

        JButton btnClose = new JButton("Close");
        btnClose.setFont(new Font("Arial", Font.PLAIN, 12));
        btnClose.setBackground(new Color(70, 130, 180));
        btnClose.setForeground(Color.WHITE);
        btnClose.setFocusPainted(false);
        btnClose.setPreferredSize(new Dimension(90, 34));

        btnPanel.add(btnDownload);
        btnPanel.add(btnClose);
        mainPanel.add(btnPanel);

        // ── Actions ──
        btnDownload.addActionListener(e -> downloadAsPDF());
        btnClose.addActionListener(e -> dispose());

        JScrollPane scroll = new JScrollPane(mainPanel);
        scroll.setBorder(null);
        add(scroll);

        setVisible(true);
    }

    /**
     * Opens a JFileChooser and saves the receipt as a formatted PDF.
     * Uses iText 5 library.
     * Encapsulation: PDF logic is private — no other class needs to know how this works.
     */
    private void downloadAsPDF() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new java.io.File("Receipt_" + paymentID + ".pdf"));
        fileChooser.setDialogTitle("Save Receipt as PDF");
        fileChooser.setFileFilter(
            new javax.swing.filechooser.FileNameExtensionFilter("PDF Files (*.pdf)", "pdf")
        );

        int result = fileChooser.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) return;

        java.io.File file = fileChooser.getSelectedFile();
        // Auto-append .pdf extension if missing
        if (!file.getName().toLowerCase().endsWith(".pdf")) {
            file = new java.io.File(file.getAbsolutePath() + ".pdf");
        }

        try {
            generatePDF(file.getAbsolutePath());
            JOptionPane.showMessageDialog(this,
                "PDF receipt saved successfully!\n" + file.getAbsolutePath(),
                "Download Complete",
                JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Failed to generate PDF:\n" + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    /**
     * Generates a professional PDF receipt using iText 5.
     * Private — Encapsulation: PDF structure and styling are hidden from all other classes.
     *
     * @param filePath absolute path where the PDF will be saved
     */
    private void generatePDF(String filePath) throws Exception {

        // ── iText colours ──
        BaseColor darkBlue  = new BaseColor(30,  80,  160);
        BaseColor lightBlue = new BaseColor(220, 235, 255);
        BaseColor darkGreen = new BaseColor(0,   120,  0);
        BaseColor lineGray  = new BaseColor(180, 180, 180);
        BaseColor headerBg  = new BaseColor(15,  55,  120);

        // ── iText fonts ──
        com.itextpdf.text.Font titleFont   = new com.itextpdf.text.Font(
                com.itextpdf.text.Font.FontFamily.HELVETICA, 18, com.itextpdf.text.Font.BOLD,
                BaseColor.WHITE);
        com.itextpdf.text.Font subFont     = new com.itextpdf.text.Font(
                com.itextpdf.text.Font.FontFamily.HELVETICA, 10, com.itextpdf.text.Font.NORMAL,
                BaseColor.WHITE);
        com.itextpdf.text.Font sectionFont = new com.itextpdf.text.Font(
                com.itextpdf.text.Font.FontFamily.HELVETICA, 9, com.itextpdf.text.Font.BOLD,
                lineGray);
        com.itextpdf.text.Font labelFont   = new com.itextpdf.text.Font(
                com.itextpdf.text.Font.FontFamily.HELVETICA, 10, com.itextpdf.text.Font.NORMAL,
                BaseColor.DARK_GRAY);
        com.itextpdf.text.Font valueFont   = new com.itextpdf.text.Font(
                com.itextpdf.text.Font.FontFamily.HELVETICA, 10, com.itextpdf.text.Font.BOLD,
                BaseColor.BLACK);
        com.itextpdf.text.Font amountFont  = new com.itextpdf.text.Font(
                com.itextpdf.text.Font.FontFamily.HELVETICA, 14, com.itextpdf.text.Font.BOLD,
                darkGreen);
        com.itextpdf.text.Font footerFont  = new com.itextpdf.text.Font(
                com.itextpdf.text.Font.FontFamily.HELVETICA, 9, com.itextpdf.text.Font.ITALIC,
                BaseColor.GRAY);

        // ── Document setup (A5 size — good for a receipt) ──
        Document doc = new Document(PageSize.A5, 40, 40, 30, 30);
        PdfWriter.getInstance(doc, new FileOutputStream(filePath));
        doc.open();

        // ══════════════════════════════════
        //  HEADER: dark blue banner
        // ══════════════════════════════════
        PdfPTable headerTable = new PdfPTable(1);
        headerTable.setWidthPercentage(100);

        PdfPCell headerCell = new PdfPCell();
        headerCell.setBackgroundColor(headerBg);
        headerCell.setPadding(14);
        headerCell.setBorder(PdfPCell.NO_BORDER);
        headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);

        Paragraph shopNamePara = new Paragraph("APU Automotive Service Centre", titleFont);
        shopNamePara.setAlignment(Element.ALIGN_CENTER);
        headerCell.addElement(shopNamePara);

        Paragraph shopSubPara = new Paragraph("APU – ASC  |  Official Receipt", subFont);
        shopSubPara.setAlignment(Element.ALIGN_CENTER);
        headerCell.addElement(shopSubPara);

        headerTable.addCell(headerCell);
        doc.add(headerTable);
        doc.add(Chunk.NEWLINE);

        // ══════════════════════════════════
        //  SECTION: Payment Info
        // ══════════════════════════════════
        doc.add(makeSectionLabel("PAYMENT INFORMATION", sectionFont, lineGray));
        doc.add(makeDetailTable(
            new String[]{"Payment ID", "Appointment ID"},
            new String[]{paymentID,    appointmentID},
            labelFont, valueFont, lightBlue
        ));
        doc.add(Chunk.NEWLINE);

        // ══════════════════════════════════
        //  SECTION: Customer & Vehicle
        // ══════════════════════════════════
        doc.add(makeSectionLabel("CUSTOMER & VEHICLE", sectionFont, lineGray));
        doc.add(makeDetailTable(
            new String[]{"Customer Name", "Car Plate No.", "Vehicle Model"},
            new String[]{customerName,    carPlate,        vehicleModel},
            labelFont, valueFont, lightBlue
        ));
        doc.add(Chunk.NEWLINE);

        // ══════════════════════════════════
        //  SECTION: Service Details
        // ══════════════════════════════════
        doc.add(makeSectionLabel("SERVICE DETAILS", sectionFont, lineGray));
        doc.add(makeDetailTable(
            new String[]{"Service Type", "Technician",   "Date", "Time"},
            new String[]{serviceType,    technicianName, date,   time},
            labelFont, valueFont, lightBlue
        ));
        doc.add(Chunk.NEWLINE);

        // ══════════════════════════════════
        //  SECTION: Payment Summary
        // ══════════════════════════════════
        doc.add(makeSectionLabel("PAYMENT SUMMARY", sectionFont, lineGray));

        PdfPTable summaryTable = new PdfPTable(2);
        summaryTable.setWidthPercentage(100);
        summaryTable.setWidths(new float[]{1.5f, 1f});

        // Payment method row
        PdfPCell pmLabel = new PdfPCell(new Phrase("Payment Method", labelFont));
        pmLabel.setBorder(PdfPCell.BOTTOM);
        pmLabel.setBorderColor(lineGray);
        pmLabel.setPadding(6);
        pmLabel.setBackgroundColor(lightBlue);
        summaryTable.addCell(pmLabel);

        PdfPCell pmValue = new PdfPCell(new Phrase(paymentMethod, valueFont));
        pmValue.setBorder(PdfPCell.BOTTOM);
        pmValue.setBorderColor(lineGray);
        pmValue.setPadding(6);
        pmValue.setBackgroundColor(lightBlue);
        summaryTable.addCell(pmValue);

        // Total amount row — highlighted green background
        BaseColor totalBg = new BaseColor(210, 240, 210);
        PdfPCell totalLabel = new PdfPCell(new Phrase("TOTAL AMOUNT", valueFont));
        totalLabel.setBackgroundColor(totalBg);
        totalLabel.setBorder(PdfPCell.BOX);
        totalLabel.setBorderColor(darkGreen);
        totalLabel.setPadding(8);
        summaryTable.addCell(totalLabel);

        PdfPCell totalValue = new PdfPCell(
            new Phrase(String.format("RM %.2f", amount), amountFont));
        totalValue.setBackgroundColor(totalBg);
        totalValue.setBorder(PdfPCell.BOX);
        totalValue.setBorderColor(darkGreen);
        totalValue.setPadding(8);
        summaryTable.addCell(totalValue);

        doc.add(summaryTable);
        doc.add(Chunk.NEWLINE);

        // ══════════════════════════════════
        //  FOOTER
        // ══════════════════════════════════
        com.itextpdf.text.Font dividerFont = new com.itextpdf.text.Font(
        	    com.itextpdf.text.Font.FontFamily.HELVETICA, 8,
        	    com.itextpdf.text.Font.NORMAL, lineGray);
        	Paragraph divider = new Paragraph("------------------------------------------------", dividerFont);
        	divider.setAlignment(Element.ALIGN_CENTER);
        	doc.add(divider);

        Paragraph footer = new Paragraph(
            "Thank you for choosing APU – ASC! We look forward to serving you again.", footerFont);
        footer.setAlignment(Element.ALIGN_CENTER);
        doc.add(footer);

        // Generated timestamp
        String timestamp = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
                               .format(new java.util.Date());
        Paragraph generated = new Paragraph("Generated on: " + timestamp, footerFont);
        generated.setAlignment(Element.ALIGN_CENTER);
        doc.add(generated);

        doc.close();
    }

    /**
     * Helper: creates a bold section label with a bottom line (e.g. "PAYMENT INFORMATION")
     */
    private Paragraph makeSectionLabel(String text,
                                       com.itextpdf.text.Font font,
                                       BaseColor lineColor) {
        Paragraph p = new Paragraph(text, font);
        p.setSpacingAfter(3);
        return p;
    }

    /**
     * Helper: creates a two-column label/value table for receipt details.
     * Uses alternating light-blue background for readability.
     */
    private PdfPTable makeDetailTable(String[] labels, String[] values,
                                      com.itextpdf.text.Font labelFont,
                                      com.itextpdf.text.Font valueFont,
                                      BaseColor bgColor) throws Exception {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1.5f, 2f});

        BaseColor lineGray = new BaseColor(200, 200, 200);

        for (int i = 0; i < labels.length; i++) {
            PdfPCell labelCell = new PdfPCell(new Phrase(labels[i], labelFont));
            labelCell.setBackgroundColor(bgColor);
            labelCell.setBorder(PdfPCell.BOTTOM);
            labelCell.setBorderColor(lineGray);
            labelCell.setPadding(6);
            table.addCell(labelCell);

            PdfPCell valueCell = new PdfPCell(new Phrase(values[i], valueFont));
            valueCell.setBackgroundColor(bgColor);
            valueCell.setBorder(PdfPCell.BOTTOM);
            valueCell.setBorderColor(lineGray);
            valueCell.setPadding(6);
            table.addCell(valueCell);
        }
        return table;
    }

    /** Creates a label-value row (for the Swing UI display) */
    private JPanel makeRow(String label, String value) {
        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(Color.WHITE);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));

        JLabel lbl = new JLabel(label + ":");
        lbl.setFont(new Font("Arial", Font.PLAIN, 12));
        lbl.setForeground(Color.DARK_GRAY);

        JLabel val = new JLabel(value);
        val.setFont(new Font("Arial", Font.BOLD, 12));

        row.add(lbl, BorderLayout.WEST);
        row.add(val, BorderLayout.EAST);
        return row;
    }

    /** Creates a horizontal divider for the Swing UI */
    private JSeparator makeDivider() {
        JSeparator sep = new JSeparator();
        sep.setForeground(Color.LIGHT_GRAY);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        return sep;
    }
}
