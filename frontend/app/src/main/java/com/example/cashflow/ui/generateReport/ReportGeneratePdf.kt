import android.content.Context
import android.os.Environment
import com.example.cashflow.model.Transaction
import com.example.cashflow.ui.booksEntry.formatDate
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream

fun generatePdf(context: Context, transactions: List<Transaction>) {
    try {
        val file = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            "report_${System.currentTimeMillis()}.pdf"
        )
        println("PDF path: ${file.absolutePath}")
        println("Dir exists: ${file.parentFile?.exists()}")
        println("Dir writable: ${file.parentFile?.canWrite()}")

        val writer = PdfWriter(file)
        val pdf = PdfDocument(writer)
        val document = Document(pdf)

        document.add(Paragraph("Transaction Report").setBold().setFontSize(18f))
        document.add(Paragraph(" "))

        val table = Table(floatArrayOf(3f, 2f, 2f, 2f))
        table.addHeaderCell("Title")
        table.addHeaderCell("Category")
        table.addHeaderCell("Amount")
        table.addHeaderCell("Date")

        transactions.forEach {
            table.addCell(it.title)
            table.addCell(it.category)
            table.addCell(it.amount.toString())
            table.addCell(formatDate(it.date))
        }

        document.add(table)
        document.close()
        println("PDF generated successfully at: ${file.absolutePath}")

    } catch (e: Exception) {
        println("PDF ERROR: ${e.message}")
        e.printStackTrace()
    }
}

fun generateExcel(context: Context, transactions: List<Transaction>) {
    try {
        val file = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            "report_${System.currentTimeMillis()}.xlsx"
        )
        println("Excel path: ${file.absolutePath}")
        println("Dir exists: ${file.parentFile?.exists()}")
        println("Dir writable: ${file.parentFile?.canWrite()}")

        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("Transactions")

        val header = sheet.createRow(0)
        header.createCell(0).setCellValue("Title")
        header.createCell(1).setCellValue("Category")
        header.createCell(2).setCellValue("Amount")
        header.createCell(3).setCellValue("Date")

        transactions.forEachIndexed { index, transaction ->
            val row = sheet.createRow(index + 1)
            row.createCell(0).setCellValue(transaction.title)
            row.createCell(1).setCellValue(transaction.category)
            row.createCell(2).setCellValue(transaction.amount)
            row.createCell(3).setCellValue(formatDate(transaction.date))
        }

        FileOutputStream(file).use { workbook.write(it) }
        workbook.close()
        println("Excel generated successfully at: ${file.absolutePath}")

    } catch (e: Exception) {
        println("Excel ERROR: ${e.message}")
        e.printStackTrace()
    }
}