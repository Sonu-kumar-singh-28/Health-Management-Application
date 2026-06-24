package com.manage.health.healthtrackerapplication.data.service

import android.content.Context
import android.os.Build
import android.os.Environment
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.layout.properties.UnitValue
import com.manage.health.healthtrackerapplication.data.model.HealthData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ExportServices(private val context: Context) {

    /**
     * Exports the user's health data into a CSV format.
     */
    suspend fun exportToCsv(healthData: List<HealthData>): String = withContext(Dispatchers.IO) {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "SmartHealthTracker_Data_$timestamp.csv"
        val file = getDownloadsFile(fileName)

        FileWriter(file).use { writer ->
            // Header Row
            writer.append("Date, Steps, Distance(m), calories Burned , Water Intake (mL), Sleep Hours , Heart Rate ,Heart Score , Created at , updated at \n")

            // Write data rows
            healthData.forEach { data ->
                writer.append("${data.date},")
                writer.append("${data.steps},")
                writer.append("${data.distance},")
                writer.append("${data.caloriesBurned},")
                writer.append("${data.waterIntake},")
                writer.append("${data.sleepHours},")
                writer.append("${data.heartRate},")
                writer.append("${data.healthScore},")
                writer.append("${data.createdAt},")
                writer.append("${data.updatedAt}\n")
            }
        }
        return@withContext getUserFriendlyPath(file)
    }

    /**
     * Generates a structural PDF summary report using iText7 library.
     */
    suspend fun exportToPdf(healthData: List<HealthData>): String = withContext(Dispatchers.IO) {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "SmartHealthTracker_Report_$timestamp.pdf"
        val file = getDownloadsFile(fileName)

        FileOutputStream(file).use { outputStream ->
            val pdfWriter = PdfWriter(outputStream)
            val pdfDocument = PdfDocument(pdfWriter)
            val document = Document(pdfDocument)

            // Document Title
            val title = Paragraph("Smart Health Tracker - Health Report")
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(18f)
            document.add(title)

            // --- Part 1: Report Info Block ---
            val reportInfo = Paragraph()
                .add("Generated on: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())}\n")
                .add("Report Period: ${if (healthData.isNotEmpty()) "${healthData.first().date} to ${healthData.last().date}" else "N/A"}\n")
                .add("Total Records: ${healthData.size}")
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(12f)
            document.add(reportInfo)

            document.add(Paragraph("\n"))

            if (healthData.isNotEmpty()) {
                // --- Part 2: Summary Statistics Calculations ---
                val totalSteps = healthData.sumOf { it.steps }
                val totalWater = healthData.sumOf { it.waterIntake }
                val totalDistance = healthData.sumOf { it.distance.toDouble() }
                val totalCalories = healthData.sumOf { it.caloriesBurned }
                val avgSleep = healthData.map { it.sleepHours }.average()
                val avgHealthScore = healthData.map { it.healthScore }.average()

                val avgHeartRate = if (healthData.any { it.heartRate > 0 }) {
                    healthData.filter { it.heartRate > 0 }.map { it.heartRate }.average()
                } else {
                    0.0
                }

                val summaryTitle = Paragraph("SUMMARY STATISTICS")
                    .setFontSize(14f)
//                    .setBold()
                document.add(summaryTitle)

                // Summary Table UI Definition
                val summaryTable = Table(UnitValue.createPercentArray(floatArrayOf(50f, 50f)))
                    .setWidth(UnitValue.createPercentValue(100f))

                summaryTable.addCell(Cell().add(Paragraph("Total Steps")))
                summaryTable.addCell(Cell().add(Paragraph(String.format(Locale.getDefault(), "%,d", totalSteps))))

                summaryTable.addCell(Cell().add(Paragraph("Total Distance")))
                summaryTable.addCell(Cell().add(Paragraph("${String.format(Locale.getDefault(), "%.2f", totalDistance / 1000)} km")))

                summaryTable.addCell(Cell().add(Paragraph("Total Calories Burned")))
                summaryTable.addCell(Cell().add(Paragraph(String.format(Locale.getDefault(), "%,d", totalCalories))))

                summaryTable.addCell(Cell().add(Paragraph("Total Water Intake")))
                summaryTable.addCell(Cell().add(Paragraph("${String.format(Locale.getDefault(), "%,d", totalWater)} ml")))

                summaryTable.addCell(Cell().add(Paragraph("Average Sleep")))
                summaryTable.addCell(Cell().add(Paragraph("${String.format(Locale.getDefault(), "%.1f", avgSleep)} hours")))

                summaryTable.addCell(Cell().add(Paragraph("Average Heart Rate")))
                summaryTable.addCell(Cell().add(Paragraph("${String.format(Locale.getDefault(), "%.0f", avgHeartRate)} bpm")))

                summaryTable.addCell(Cell().add(Paragraph("Average Health Score")))
                summaryTable.addCell(Cell().add(Paragraph("${String.format(Locale.getDefault(), "%.1f", avgHealthScore)}/100")))

                document.add(summaryTable)
                document.add(Paragraph("\n"))

                // --- Part 3: Health Insights Block ---
                val insightsTitle = Paragraph("HEALTH INSIGHTS")
                    .setFontSize(14f)
//                    .setBold()
                document.add(insightsTitle)

                val bestDay = healthData.maxByOrNull { it.healthScore }
                val worstDay = healthData.minByOrNull { it.healthScore }
                val mostActiveDay = healthData.maxByOrNull { it.steps }

                val insights = Paragraph()

                bestDay?.let {
                    insights.add("Best Health Day: ${it.date} (Score: ${it.healthScore}/100)\n")
                }
                worstDay?.let {
                    insights.add("Needs Improvement: ${it.date} (Score: ${it.healthScore}/100)\n")
                }
                mostActiveDay?.let {
                    insights.add("Most Active Day: ${it.date} (${String.format(Locale.getDefault(), "%,d", it.steps)} steps)\n")
                }

                document.add(insights)
                document.add(Paragraph("\n"))

                // --- Part 4: Detailed Data Table Block ---
                // References: Screenshot 2026-06-24 213351_2.jpg & Screenshot 2026-06-24 213405_2.jpg
                val dataTitle = Paragraph("DETAILED DAILY DATA")
                    .setFontSize(14f)
//                    .setBold()
                document.add(dataTitle)

                val dataTable = Table(UnitValue.createPercentArray(floatArrayOf(20f, 15f, 15f, 15f, 15f, 20f)))
                    .setWidth(UnitValue.createPercentValue(100f))

                // Table headers
                dataTable.addHeaderCell(Cell().add(Paragraph("Date")))
                dataTable.addHeaderCell(Cell().add(Paragraph("Steps")))
                dataTable.addHeaderCell(Cell().add(Paragraph("Water (ml)")))
                dataTable.addHeaderCell(Cell().add(Paragraph("Sleep (h)")))
                dataTable.addHeaderCell(Cell().add(Paragraph("Health Score")))
                dataTable.addHeaderCell(Cell().add(Paragraph("Created")))

                // Table data rows mapping
                healthData.sortedByDescending { it.date }.forEach { data ->
                    dataTable.addCell(Cell().add(Paragraph(data.date)))
                    dataTable.addCell(Cell().add(Paragraph(String.format(Locale.getDefault(), "%,d", data.steps))))
                    dataTable.addCell(Cell().add(Paragraph(String.format(Locale.getDefault(), "%,d", data.waterIntake))))
                    dataTable.addCell(Cell().add(Paragraph(String.format(Locale.getDefault(), "%.1f", data.sleepHours))))
                    dataTable.addCell(Cell().add(Paragraph(data.healthScore.toString())))
                    dataTable.addCell(Cell().add(Paragraph(data.createdAt.take(10)))) // Just the date part
                }

                // Reference: Screenshot 2026-06-24 213413_2.jpg
                document.add(dataTable)

            } else {
                // Reference: Screenshot 2026-06-24 213413_2.jpg
                val noDataMessage = Paragraph("No health data available for export.\nStart tracking your health")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(12f)
                document.add(noDataMessage)
            }

            // Reference: Screenshot 2026-06-24 213413_2.jpg
            document.close()
        }

        return@withContext getUserFriendlyPath(file)
    }

    /**
     * Delete an export file
     */
    fun deleteExport(file: File): Boolean {
        return try {
            file.delete()
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Get user-friendly file path for display
     * References: Screenshot 2026-06-24 213413_2.jpg & Screenshot 2026-06-24 213426_2.jpg
     */
    fun getUserFriendlyPath(file: File): String {
        val fileName = file.name
        return "Downloads/$fileName"
    }

    /**
     * Get a file in the Downloads directory with user-friendly access
     * References: Screenshot 2026-06-24 213426_2.jpg & Screenshot 2026-06-24 213435_2.jpg
     */
    private fun getDownloadsFile(fileName: String): File {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // For Android 10+ (API 29+), use scoped storage
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            File(downloadsDir, fileName)
        } else {
            // For older Android versions, use external storage
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            File(downloadsDir, fileName)
        }
    }

    // Extension function for string repetition
    private operator fun String.times(n: Int): String = this.repeat(n)
}