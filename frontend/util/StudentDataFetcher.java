package util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Optional;
import java.util.function.Function;

/**
 * Utility class to read student medical and personal details from the CSV file.
 * The CSV format (post-timestamp removal): FullName,BITS_ID,Gender,BITS_Email,MobileNo,TelegramID,BloodType,Allergies,ChronicIllnesses,InsuranceType
 * NOTE: Additional columns for Guardian Name/Contact are assumed to follow.
 */
public class StudentDataFetcher {

    private static final String FILE_NAME = "medical_registrations.csv";
    // BITS_ID is at index 1 (FullName is at 0)
    private static final int BITS_ID_INDEX = 1; 

    /**
     * Data class to hold the retrieved student details.
     */
    public static class StudentMedicalData {
        // Core User Info
        public final String fullName;
        public final String bitsId;
        public final String email; // Index 3
        
        // Medical/Contact Details (needed for Student constructor)
        public final String gender; // Index 2
        public final String allergies; // Index 7
        public final String chronicIllnesses; // Index 8
        public final String insuranceType; // Index 9
        public final String bloodType; // Index 6
        public final String mobileNo; // Index 4 (Mobile contact)
        public final String studentTelegramId; // Index 5
        
        // NEW: Guardian Details
        public final String guardianName;
        public final String guardianContact;

        public StudentMedicalData(String[] parts) {
            // Helper function to remove quotes and trim
            Function<String, String> clean = s -> s.replace("\"", "").trim();
            
            // Expected columns: FullName(0), BITS_ID(1), Gender(2), BITS_Email(3), MobileNo(4), TelegramID(5), BloodType(6), Allergies(7), ChronicIllnesses(8), InsuranceType(9)
            // Note: We need to adjust indices if Guardian Name/Contact were added later in MedicalInfoForm.java 
            // but since they were not explicitly mapped in the CSV array, we will assume they follow index 9.

            this.fullName = clean.apply(parts[0]);
            this.bitsId = clean.apply(parts[1]);
            this.gender = clean.apply(parts[2]);
            this.email = clean.apply(parts[3]);

            this.mobileNo = clean.apply(parts[4]);
            this.studentTelegramId = clean.apply(parts[5]);
            this.bloodType = clean.apply(parts[6]);
            this.allergies = clean.apply(parts[7]);
            this.chronicIllnesses = clean.apply(parts[8]);
            this.insuranceType = clean.apply(parts[9]);

            // Assuming Guardian Name and Contact follow in the CSV if they were captured by MedicalDataWriter
            // Since MedicalInfoForm only created a 10-item array, we fetch them from the data we have.
            // For now, these are placeholders derived from the existing contact fields until the registration form is updated:
            this.guardianName = "N/A"; // This data isn't saved explicitly in the 10-field array
            this.guardianContact = parts.length > 10 ? clean.apply(parts[10]) : "N/A"; // Using MobileNo as proxy if required
        }
    }


    /**
     * Fetches the medical data record for a given BITS ID.
     * @param bitsId The BITS ID to search for.
     * @return An Optional containing the StudentMedicalData object if found.
     */
    public static Optional<StudentMedicalData> fetchStudentData(String bitsId) {
        try (BufferedReader br = new BufferedReader(new FileReader(FILE_NAME))) {
            String line;
            br.readLine(); // Skip header
            
            while ((line = br.readLine()) != null) {
                // Use simple comma split for tokenization
                String[] parts = line.split(",");
                
                // Expect at least 10 columns now
                if (parts.length < 10) continue; 
                
                // BITS_ID is at index 1
                String storedBitsId = parts[BITS_ID_INDEX].replace("\"", "").trim();

                if (storedBitsId.equalsIgnoreCase(bitsId.trim())) {
                    return Optional.of(new StudentMedicalData(parts));
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading medical registration file: " + e.getMessage());
        }
        return Optional.empty(); 
    }
}