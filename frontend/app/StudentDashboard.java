package app;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.util.ArrayList; // Import retained to support BPDCClinicApp logic
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;

import core.ClinicUser;
import core.IClinicOperations;
import util.EmergencyLogWriter;

// Implements the IClinicOperations Interface
public class StudentDashboard extends JPanel implements IClinicOperations { 

    /**
     * NESTED CLASS: Student Model
     * Retains all fields to support data fetching logic in BPDCClinicApp.
     */
    public static class Student extends ClinicUser {
        
        private String allergies;
        private String chronicIllnesses;
        private String insuranceType;
        private String bloodType;
        private String mobileNo;
        private String studentTelegramId; 
        
        public Student(String bitsId, String fullName, String email, 
                       String allergies, String chronicIllnesses, String insuranceType, 
                       String bloodType, String mobileNo, String studentTelegramId) {
            super(bitsId, fullName, email, "Student");
            this.allergies = allergies;
            this.chronicIllnesses = chronicIllnesses;
            this.insuranceType = insuranceType;
            this.bloodType = bloodType;
            this.mobileNo = mobileNo;
            this.studentTelegramId = studentTelegramId;
        }

        @Override
        public String getDashboardTitle() {
            return "BPDC Clinic Student Portal";
        }
        
        public String getAlertsStatus() {
            if (this.allergies.equalsIgnoreCase("N/A") && this.chronicIllnesses.equalsIgnoreCase("N/A")) {
                return "No known critical alerts.";
            } else {
                return "Alert: Review medical history.";
            }
        }
        
        // Getters needed for dynamic card updates
        public String getInsuranceType() { return insuranceType; }
        public String getBloodType() { return bloodType; }
        public String getAllergies() { return allergies; }
        public String getChronicIllnesses() { return chronicIllnesses; }
        public String getStudentTelegramId() { return studentTelegramId; } 
    }
    // --- END STUDENT MODEL ---

    private final BPDCClinicApp app;
    private CardLayout cardLayout = new CardLayout();
    private JPanel mainContentPanel = new JPanel(cardLayout);
    
    // --- Color Palette (Consistent with other forms) ---
    private static final Color BACKGROUND_COLOR = new Color(240, 247, 245);
    private static final Color DARK_BLUE = new Color(25, 55, 109);
    private static final Color BRAND_BLUE = new Color(47, 103, 246); // For buttons/links
    private static final Color LIGHT_GRAY_BORDER = new Color(220, 220, 220);

    // --- Color Bar Colors ---
    private static final Color BAR_YELLOW = new Color(255, 193, 7);
    private static final Color BAR_BLUE = new Color(100, 149, 237);
    private static final Color BAR_RED = new Color(220, 53, 69); // Emergency Red

    // --- Fonts (Updated for prominence and appeal) ---
    private static final Font HEADER_TITLE_FONT = new Font("Arial", Font.BOLD, 28);
    private static final Font USER_INFO_NAME_FONT = new Font("Arial", Font.BOLD, 24); 
    private static final Font USER_INFO_ID_FONT = new Font("Arial", Font.BOLD, 20);  
    private static final Font CARD_TITLE_FONT = new Font("Arial", Font.BOLD, 18);
    private static final Font CARD_CONTENT_FONT = new Font("Arial", Font.BOLD, 22);
    private static final Font BODY_FONT_PLAIN = new Font("Arial", Font.PLAIN, 16);
    // private static final Font SMALL_TEXT_FONT = new Font("Arial", Font.PLAIN, 14); // Not strictly needed

    // --- Data Model ---
    private static class Clinic {
        String name;
        String location;
        String type;

        public Clinic(String name, String location, String type) {
            this.name = name;
            this.location = location;
            this.type = type;
        }
    }

    // Dynamic User Details (Retained JLabels for dynamic updates)
    private JLabel dashboardNameLabel;
    private JLabel studentIdLabel;
    private String currentBitsId = "N/A"; // ID used for logging
    private String currentFullName = "Student User"; 
    
    // Placeholder data for cards (Updated to match the new dashboard template)
    private String lastLogin = "N/A";
    private String insuranceProvider = "N/A"; 
    private String lastPrescriptionDate = "N/A";
    private String lastPrescriptionMedication = "N/A";

    private List<Clinic> nearbyClinics = new ArrayList<>();

    // MODIFIED: Constructor accepts BPDCClinicApp
    public StudentDashboard(BPDCClinicApp app) {
        this.app = app; 
        
        // Mock Data Initialization (Dubai locations) - Retained
        nearbyClinics.add(new Clinic("Fakeeh University Hospital", "Dubai Silicon Oasis", "Hospital"));
        nearbyClinics.add(new Clinic("Aster Clinic", "Dubai Silicon Oasis", "Clinic"));
        nearbyClinics.add(new Clinic("Health Connect Poly Clinic", "Academic City", "Clinic"));

        setLayout(new BorderLayout());
        setBackground(BACKGROUND_COLOR); 

        // 1. Top Color Bar
        JPanel topBar = createTopColorBar();
        add(topBar, BorderLayout.NORTH);

        // 2. Main Content Area Wrapper
        JPanel centerWrapper = new JPanel(new BorderLayout());
        centerWrapper.setBackground(BACKGROUND_COLOR);

        // Header Panel - Uses the new structure
        JPanel headerPanel = createHeaderPanel();
        centerWrapper.add(headerPanel, BorderLayout.NORTH);

        // Main Card Layout Panel
        mainContentPanel.setBorder(BorderFactory.createEmptyBorder(25, 50, 25, 50)); 
        mainContentPanel.setBackground(BACKGROUND_COLOR);
        mainContentPanel.add(createDashboardPanel(), "Dashboard");
        mainContentPanel.add(createPlaceholderPanel("Prescription History Content"), "Prescription History");
        mainContentPanel.add(createPlaceholderPanel("Appointments/Booking Content"), "Appointments");
        mainContentPanel.add(createPlaceholderPanel("Insurance Details Content"), "Insurance Details");

        centerWrapper.add(mainContentPanel, BorderLayout.CENTER);
        add(centerWrapper, BorderLayout.CENTER); 

        cardLayout.show(mainContentPanel, "Dashboard");
        
        // Initialize dynamic labels
        updateUserDetails("Student User", "N/A", "N/A", "N/A");
    }

    /**
     * Public method to update the dashboard with the full Student object details.
     * Overloaded method is used by BPDCClinicApp after successful login.
     */
    public void updateUserDetails(Student student) {
         this.currentBitsId = student.getBitsId(); 
         this.currentFullName = student.getFullName(); 
         
         SwingUtilities.invokeLater(() -> {
            // Update Header labels
            dashboardNameLabel.setText("Welcome, " + student.getFullName());
            studentIdLabel.setText("Student ID: " + student.getBitsId());
            
            // Update Dashboard Card data
            insuranceProvider = student.getInsuranceType() + " (" + (student.getInsuranceType().equals("N/A") ? "No Details" : "View Details") + ")";
            
            // Repaint the dashboard content to reflect card changes (e.g., Insurance Provider)
            mainContentPanel.revalidate();
            mainContentPanel.repaint();
         });
    }

    /**
     * Implements IClinicOperations (Interface).
     * Public method to update the dashboard with user details. (Fallback for Admin/generic usage)
     */
    @Override
    public void updateUserDetails(String fullName, String bitsId, String email, String alerts) {
         this.currentBitsId = bitsId; 
         this.currentFullName = fullName; 
         SwingUtilities.invokeLater(() -> {
            // Update Header labels
            if (dashboardNameLabel != null) dashboardNameLabel.setText("Welcome, " + fullName);
            if (studentIdLabel != null) studentIdLabel.setText("Student ID: " + bitsId);
         });
    }

    /**
     * Creates the top color bar (Yellow, Blue, Red).
     */
    private JPanel createTopColorBar() {
        JPanel barPanel = new JPanel();
        barPanel.setLayout(new GridLayout(1, 3, 0, 0));
        barPanel.setPreferredSize(new Dimension(0, 30)); 

        JPanel yellowZone = new JPanel();
        yellowZone.setBackground(BAR_YELLOW);

        JPanel blueZone = new JPanel();
        blueZone.setBackground(BAR_BLUE);

        JPanel redZone = new JPanel();
        redZone.setBackground(BAR_RED);

        barPanel.add(yellowZone);
        barPanel.add(blueZone);
        barPanel.add(redZone);

        return barPanel;
    }

    /**
     * Adopts the layout of BPDCClinicDashboard.java, but uses member JLabels.
     */
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout(50, 0)); 
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(LIGHT_GRAY_BORDER, 1),
            BorderFactory.createEmptyBorder(20, 50, 20, 50) 
        ));

        // Left side: User Info and Title
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("BPDC Clinic Student Portal");
        titleLabel.setFont(HEADER_TITLE_FONT);
        titleLabel.setForeground(DARK_BLUE);
        leftPanel.add(titleLabel, BorderLayout.NORTH);

        JPanel userInfo = new JPanel(new GridLayout(2, 1));
        userInfo.setOpaque(false);
        userInfo.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
        
        // --- Dynamic User Name Label (ASSIGNED) ---
        dashboardNameLabel = new JLabel("Welcome, "); // Initialized as member field
        dashboardNameLabel.setFont(USER_INFO_NAME_FONT); 
        dashboardNameLabel.setForeground(DARK_BLUE.darker());
        userInfo.add(dashboardNameLabel);
        
        // --- Dynamic Student ID Label (ASSIGNED) ---
        studentIdLabel = new JLabel("Student ID: "); // Initialized as member field
        studentIdLabel.setFont(USER_INFO_ID_FONT); 
        studentIdLabel.setForeground(Color.GRAY.darker());
        userInfo.add(studentIdLabel);

        leftPanel.add(userInfo, BorderLayout.CENTER);

        panel.add(leftPanel, BorderLayout.WEST);

        // Right side: Emergency Button
        JButton emergencyBtn = new JButton("EMERGENCY CALL");
        emergencyBtn.setBackground(BAR_RED); 
        emergencyBtn.setForeground(Color.WHITE);
        emergencyBtn.setFont(new Font("Arial", Font.BOLD, 20)); 
        emergencyBtn.setPreferredSize(new Dimension(280, 60)); 
        emergencyBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        emergencyBtn.setFocusPainted(false);
        emergencyBtn.setBorderPainted(false);
        // Implements IClinicOperations
        emergencyBtn.addActionListener(e -> handleEmergencyCall()); 

        JPanel btnWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        btnWrapper.setOpaque(false);
        btnWrapper.add(emergencyBtn);

        panel.add(btnWrapper, BorderLayout.EAST);

        return panel;
    }

    /**
     * Implements IClinicOperations (Interface).
     * Retains the critical logging and notification functionality.
     */
    @Override
    public void handleEmergencyCall() {
        int result = JOptionPane.showConfirmDialog(this,
                "You are about to place an EMERGENCY ALERT.\nThis will instantly notify clinic staff and log the event.\nProceed?",
                "CONFIRM EMERGENCY ALERT",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (result == JOptionPane.YES_OPTION) {
             // Retain the crucial logging/notification logic from the original file
             if (EmergencyLogWriter.logAndNotifyEmergency(currentBitsId, currentFullName)) {
                JOptionPane.showMessageDialog(this,
                        "EMERGENCY ALERT SENT!\nClinic staff and wardens have been notified via Telegram for user: " + currentBitsId,
                        "ALERT CONFIRMED",
                        JOptionPane.ERROR_MESSAGE);
            } else {
                // Exception Handling feedback
                JOptionPane.showMessageDialog(this,
                        "EMERGENCY ALERT FAILED TO LOG OR NOTIFY! Please check file permissions/network.",
                        "LOG/NOTIFY ERROR",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    /**
     * Adopts the simple 3-card + Clinics layout from BPDCClinicDashboard.java.
     */
    private JPanel createDashboardPanel() {
        JPanel dashboard = new JPanel(new BorderLayout(30, 30)); 
        dashboard.setOpaque(false);

        // 1 row, 3 columns for key data cards
        JPanel cardGrid = new JPanel(new GridLayout(1, 3, 30, 0)); 
        cardGrid.setOpaque(false);

        cardGrid.add(createDataCard("Last Login", lastLogin, new Color(220, 237, 255)));
        // Insurance Provider will be updated dynamically via updateUserDetails
        cardGrid.add(createDataCard("Insurance Provider", insuranceProvider, new Color(200, 255, 200)));
        cardGrid.add(createPrescriptionCard());

        JPanel clinicsPanel = createClinicsCard();

        dashboard.add(cardGrid, BorderLayout.NORTH);
        dashboard.add(clinicsPanel, BorderLayout.CENTER);

        return dashboard;
    }

    private JPanel createDataCard(String title, String content, Color bgColor) {
        JPanel card = new JPanel(new BorderLayout(5, 5));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LIGHT_GRAY_BORDER, 1),
                BorderFactory.createEmptyBorder(25, 25, 25, 25) 
        ));
        card.setBackground(bgColor);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(CARD_TITLE_FONT);
        titleLabel.setForeground(DARK_BLUE);
        card.add(titleLabel, BorderLayout.NORTH);

        JLabel contentLabel = new JLabel("<html><p style='width: 150px;'>" + content + "</p></html>");
        contentLabel.setFont(CARD_CONTENT_FONT); 
        card.add(contentLabel, BorderLayout.CENTER);

        return card;
    }

    private JPanel createPrescriptionCard() {
        JPanel card = createDataCard("Last Prescription", "", new Color(255, 240, 210));

        JPanel contentPanel = new JPanel(new GridLayout(3, 1));
        contentPanel.setOpaque(false);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));

        JLabel dateLabel = new JLabel(lastPrescriptionDate);
        dateLabel.setFont(CARD_CONTENT_FONT);
        dateLabel.setForeground(DARK_BLUE.darker());
        contentPanel.add(dateLabel);

        JLabel medLabel = new JLabel(lastPrescriptionMedication);
        medLabel.setFont(BODY_FONT_PLAIN);
        medLabel.setForeground(Color.GRAY.darker());
        contentPanel.add(medLabel);

        JButton historyBtn = new JButton("View Full History →");
        historyBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        historyBtn.setForeground(BRAND_BLUE);
        historyBtn.setBorderPainted(false);
        historyBtn.setContentAreaFilled(false);
        historyBtn.setFocusPainted(false);
        historyBtn.setHorizontalAlignment(SwingConstants.LEFT);
        historyBtn.setFont(BODY_FONT_PLAIN);
        // Action listener retained to show the right card
        historyBtn.addActionListener(e -> cardLayout.show(mainContentPanel, "Prescription History"));

        contentPanel.add(historyBtn);

        card.remove(card.getComponent(1));
        card.add(contentPanel, BorderLayout.CENTER);

        return card;
    }

    private JPanel createClinicsCard() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                    BorderFactory.createLineBorder(DARK_BLUE, 1), 
                    "Nearby Medical Facilities", 
                    0, 
                    0, 
                    CARD_TITLE_FONT, 
                    DARK_BLUE),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        card.setBackground(Color.WHITE);

        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setOpaque(false);

        for (Clinic clinic : nearbyClinics) {
            JPanel item = new JPanel(new BorderLayout(15, 0)); 
            Border bottomLine = BorderFactory.createMatteBorder(0, 0, 1, 0, LIGHT_GRAY_BORDER); 
            Border itemPadding = BorderFactory.createEmptyBorder(10, 0, 10, 0); 
            item.setBorder(new CompoundBorder(bottomLine, itemPadding));
            item.setOpaque(false);
            item.setAlignmentX(Component.LEFT_ALIGNMENT);

            JPanel textPanel = new JPanel(new GridLayout(2, 1));
            textPanel.setOpaque(false);

            JLabel nameLabel = new JLabel(clinic.name);
            nameLabel.setFont(CARD_TITLE_FONT);
            textPanel.add(nameLabel);

            JLabel locationLabel = new JLabel(clinic.location + " (" + clinic.type + ")");
            locationLabel.setFont(BODY_FONT_PLAIN);
            locationLabel.setForeground(Color.GRAY.darker());
            textPanel.add(locationLabel);

            item.add(textPanel, BorderLayout.CENTER);

            JLabel icon = new JLabel("📍"); 
            icon.setFont(new Font("Arial", Font.PLAIN, 24)); 
            icon.setForeground(BRAND_BLUE);
            item.add(icon, BorderLayout.EAST);

            listPanel.add(item);
        }

        JScrollPane scrollPane = new JScrollPane(listPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        card.add(scrollPane, BorderLayout.CENTER);
        return card;
    }

    private JPanel createPlaceholderPanel(String contentText) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.add(new JLabel("Placeholder: " + contentText, SwingConstants.CENTER));
        panel.setBackground(new Color(255, 255, 240));
        return panel;
    }
}