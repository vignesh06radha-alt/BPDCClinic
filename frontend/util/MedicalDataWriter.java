package util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Writes student medical registration data to a CSV file.
 * Demonstrates file I/O and exception handling (try-with-resources).
 */
public class MedicalDataWriter {

    private static final String FILE_NAME = "medical_registrations.csv";
    // Header updated: Timestamp removed
    private static final String CSV_HEADER = 
        "FullName,BITS_ID,Gender,BITS_Email,MobileNo,TelegramNo,BloodType,Allergies,ChronicIllnesses,InsuranceType";

    /**
     * Appends a new user's medical and personal details to a CSV file.
     * @param data An array of strings containing the collected form data.
     * @return true if the write was successful, false otherwise.
     * Demonstrates Exception Handling.
     */
    public static boolean writeDataToCsv(String[] data) {
        File csvFile = new File(FILE_NAME);
        boolean isNewFile = !csvFile.exists();

        try (FileWriter fw = new FileWriter(csvFile, true);
             PrintWriter pw = new PrintWriter(fw)) {

            // Write header only if the file is new
            if (isNewFile) {
                pw.println(CSV_HEADER);
            }

            // Prepare the data line (Timestamp logic removed)
            // The data array is now written directly.
            StringBuilder sb = new StringBuilder();
            
            for (int i = 0; i < data.length; i++) {
                // Wrap in quotes and escape internal quotes
                sb.append("\"").append(data[i].replace("\"", "\"\"")).append("\"");
                // Add comma unless it's the last item
                if (i < data.length - 1) {
                    sb.append(",");
                }
            }
            
            pw.println(sb.toString());
            return true;

        } catch (IOException e) {
            System.err.println("Error writing data to CSV file: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
