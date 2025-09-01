package com.example.budgetbuddy;

import android.content.Context;
import android.os.Environment;
import android.widget.Toast;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ExportUtils {

    // Helper: Convert timestamp to readable date
    private static String formatDate(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    // Export expenses to CSV
    public static void exportToCSV(Context context, List<Expense> expenses) {
        String fileName = "BudgetBuddy_Expenses.csv";
        File exportDir = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "BudgetBuddy");
        if (!exportDir.exists()) {
            exportDir.mkdirs();
        }
        File file = new File(exportDir, fileName);

        try {
            FileWriter writer = new FileWriter(file);
            writer.append("Amount,Note,Date\n");
            for (Expense expense : expenses) {
                writer.append(expense.getAmount() + ",");
                writer.append(expense.getNote() + ",");
                writer.append(formatDate(expense.getTimestamp()) + "\n");
            }
            writer.flush();
            writer.close();
            Toast.makeText(context, "CSV exported: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Toast.makeText(context, "Error exporting CSV: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    // Export expenses to PDF
    // Export expenses to PDF
    public static void exportToPDF(Context context, List<Expense> expenses) {
        String fileName = "BudgetBuddy_Expenses.pdf";
        File exportDir = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "BudgetBuddy");
        if (!exportDir.exists()) {
            exportDir.mkdirs();
        }
        File file = new File(exportDir, fileName);

        Document document = new Document();
        try {
            PdfWriter.getInstance(document, new FileOutputStream(file));
            document.open();

            Paragraph title = new Paragraph("BudgetBuddy Expense Report\n\n");
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            PdfPTable table = new PdfPTable(3); // 3 columns: Amount, Note, Date
            table.addCell(new PdfPCell(new Phrase("Amount")));
            table.addCell(new PdfPCell(new Phrase("Note")));
            table.addCell(new PdfPCell(new Phrase("Date")));

            for (Expense expense : expenses) {
                table.addCell(new PdfPCell(new Phrase(String.valueOf(expense.getAmount()))));
                table.addCell(new PdfPCell(new Phrase(expense.getNote())));
                table.addCell(new PdfPCell(new Phrase(formatDate(expense.getTimestamp()))));
            }

            document.add(table);
            document.close();
            Toast.makeText(context, "PDF exported: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (IOException | DocumentException e) {
            Toast.makeText(context, "Error exporting PDF: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

}
