package com.alttd.util;

import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.utils.FileUpload;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ExcelWriter {

    private final String fileName = "suggestions.xlsx";
    private final String sheetName = "Sheet1";
    private final Workbook workbook;
    private final Sheet sheet;
    private int currentRow = 0;
    private boolean done = false;

    public ExcelWriter() {
        this.workbook = new XSSFWorkbook();

        // Create a new sheet in the workbook
        this.sheet = workbook.createSheet(sheetName);
    }

    public synchronized void addRow(String... data) {
        if (done) {
            Logger.altitudeLogs.warning("Tried to write to finished excel file");
            return;
        }
        Row row = sheet.createRow(currentRow++);
        int curCel = 0;
        for (String entry : data) {
            Cell cell = row.createCell(curCel++);
            cell.setCellValue(entry);
        }
    }

    public synchronized void saveAndSend(InteractionHook reply) {
        done = true;
        try {
            File file = new File(fileName);
            FileOutputStream outputStream = new FileOutputStream(file);
            workbook.write(outputStream);
            workbook.close();
            reply.sendFiles(FileUpload.fromData(file)).queue(done -> {
                //noinspection ResultOfMethodCallIgnored
                file.delete();
            });
        } catch (IOException e) {
            reply.sendMessageEmbeds(Util.genericErrorEmbed("Error", "Error while uploading excel file")).queue();
        }
    }
}
