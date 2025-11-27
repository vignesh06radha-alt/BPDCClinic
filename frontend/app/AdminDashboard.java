package app;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.File;
import java.io.RandomAccessFile;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;

import core.ClinicUser;
import core.IClinicOperations;
import util.EmergencyLogWriter;

/**
 * Implements the IClinicOperations Interface.
 */
public class AdminDashboard extends JPanel implements IClinicOperations {
    
    /**
     * NESTED CLASS: Staff Model
     * Demonstrates: Inheritance (extends ClinicUser) and Encapsulation (method for status).
     */
    public static class Staff extends ClinicUser {
        
        private String employeeId; 

        // Constructor chains to the base class
        public Staff(String bitsId, String fullName, String email, String employeeId) {
            super(bitsId, fullName, email, "Staff");
            this.employeeId = employeeId;
        }

        @Override
        public String getDashboardTitle() {
            return "BPDC Clinic Nurse Portal";
        }
        
        // Encapsulated method demonstrating business logic
        public String getPendingActionStatus() {
            // Mock check for pending items
            return "3 Pending appointments to review.";
        }
    }
    // --- END STAFF MODEL ---

    private final BPDCClinicApp app;
    
    // --- Color Palette ---
    private static final Color BACKGROUND_COLOR = new Color(240, 247, 245);
    private static final Color DARK_BLUE = new Color(25, 55, 109);
    private static final Color BRAND_BLUE = new Color(47, 103, 246); 
    private static final Color LIGHT_GRAY_BORDER = new Color(220, 220, 220);

    // --- Color Bar Colors ---
    private static final Color BAR_YELLOW = new Color(255, 193, 7);
    private static final Color BAR_BLUE = new Color(100, 149, 237);
    private static final Color BAR_RED = new Color(220, 53, 69); 

    // --- Fonts ---
    private static final Font HEADER_TITLE_FONT = new Font("Arial", Font.BOLD, 28);
    private static final Font USER_INFO_NAME_FONT = new Font("Arial", Font.BOLD, 24); 
    private static final Font USER_INFO_ID_FONT = new Font("Arial", Font.BOLD, 20);  
    private static final Font CARD_TITLE_FONT = new Font("Arial", Font.BOLD, 18);
    private static final Font BODY_FONT_PLAIN = new Font("Arial", Font.PLAIN, 16);

    // Fields to hold and display dynamic user data
    private JLabel dashboardNameLabel;
    private JLabel dashboardIdLabel;
    private JLabel emailLabel;
    private JLabel medicalAlertsLabel; 
    private String currentBitsId = "N/A"; 
    private String currentFullName = "Clinic Staff"; // NEW: Store the full name for the notifier
    
    // Placeholder data for card views 
    private String lastLogin = "N/A";
    
    // Data Model for Clinics 
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
    
    // --- NEW: Fields for Notification System ---
    private static final String MESSAGES_FILE = "C:\\Users\\vigne\\Documents\\BPDCCLinic\\oops\\oops\\messages.txt"; //
    private JButton notificationIcon;
    private int unreadMessagesCount = 0;
    private long lastMsgSize = 0; // To track file changes

    public AdminDashboard(BPDCClinicApp app) {
        this.app = app;
        
        setLayout(new BorderLayout());
        setBackground(BACKGROUND_COLOR);

        // 1. Top Color Bar
        JPanel topBar = createTopColorBar();
        add(topBar, BorderLayout.NORTH);

        // 2. Main Content Wrapper
        JPanel centerWrapper = new JPanel(new BorderLayout());
        centerWrapper.setBackground(BACKGROUND_COLOR);

        // 3. Header Panel 
        JPanel headerPanel = createHeaderPanel();
        centerWrapper.add(headerPanel, BorderLayout.NORTH);

        // 4. Dashboard Content (User Details and Cards)
        JPanel dashboardContent = createDashboardContent();
        centerWrapper.add(dashboardContent, BorderLayout.CENTER);

        add(centerWrapper, BorderLayout.CENTER);
        
        // Initialize placeholders with empty data
        updateUserDetails("Clinic Staff", "N/A", "N/A", "N/A");
        
        // NEW: Start the file watcher in a separate thread
        startMessageWatcher(); 
    }
    
    /**
     * Implements IClinicOperations (Interface).
     * Public method to update the dashboard with user details from the object.
     */
    @Override
    public void updateUserDetails(String fullName, String bitsId, String email, String alerts) {
        this.currentBitsId = bitsId; 
        this.currentFullName = fullName; // Capture full name
        SwingUtilities.invokeLater(() -> {
            // Update Header labels
            dashboardNameLabel.setText("Welcome, " + fullName);
            dashboardIdLabel.setText("ID: " + bitsId);
            
            // Update Card labels
            if (emailLabel != null) emailLabel.setText(email);
            if (medicalAlertsLabel != null) medicalAlertsLabel.setText(alerts);

            // Highlight alerts if present
            if (alerts.contains("Alert:") || alerts.contains("Pending")) {
                 medicalAlertsLabel.setForeground(BAR_RED.darker());
            } else if (medicalAlertsLabel != null) {
                 medicalAlertsLabel.setForeground(DARK_BLUE);
            }
        });
    }
    
    /**
     * Creates the new styled header panel with user greeting, Emergency, and Logout buttons.
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

        JLabel titleLabel = new JLabel("BPDC Clinic Nurse Portal"); // Updated Title
        titleLabel.setFont(HEADER_TITLE_FONT);
        titleLabel.setForeground(DARK_BLUE);
        leftPanel.add(titleLabel, BorderLayout.NORTH);

        JPanel userInfo = new JPanel(new GridLayout(2, 1));
        userInfo.setOpaque(false);
        userInfo.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
        
        // --- Dynamic User Name Label ---
        dashboardNameLabel = new JLabel("Welcome, ");
        dashboardNameLabel.setFont(USER_INFO_NAME_FONT); 
        dashboardNameLabel.setForeground(DARK_BLUE.darker());
        userInfo.add(dashboardNameLabel);
        
        // --- Dynamic ID Label ---
        dashboardIdLabel = new JLabel("ID: ");
        dashboardIdLabel.setFont(USER_INFO_ID_FONT); 
        dashboardIdLabel.setForeground(Color.GRAY.darker());
        userInfo.add(dashboardIdLabel);

        leftPanel.add(userInfo, BorderLayout.CENTER);

        // Right side: Emergency and Logout Buttons
        JButton logoutBtn = new JButton("LOGOUT");
        logoutBtn.setBackground(BAR_BLUE.darker()); 
        logoutBtn.setForeground(Color.WHITE);
        logoutBtn.setFont(new Font("Arial", Font.BOLD, 20)); 
        logoutBtn.setPreferredSize(new Dimension(150, 60)); 
        logoutBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoutBtn.setFocusPainted(false);
        logoutBtn.setBorderPainted(false);
        logoutBtn.addActionListener(e -> app.showLogin());

        // NEW: Notification Icon/Button
        notificationIcon = new JButton("✉ (0)"); 
        notificationIcon.setBackground(BRAND_BLUE.brighter()); 
        notificationIcon.setForeground(Color.WHITE);
        notificationIcon.setFont(new Font("Arial", Font.BOLD, 16)); 
        notificationIcon.setPreferredSize(new Dimension(100, 60)); 
        notificationIcon.setCursor(new Cursor(Cursor.HAND_CURSOR));
        notificationIcon.setFocusPainted(false);
        notificationIcon.setBorderPainted(false);
        notificationIcon.setToolTipText("No unread messages.");
        notificationIcon.addActionListener(e -> {
            // Placeholder action: show all messages or redirect
            JOptionPane.showMessageDialog(this, 
                "You have " + unreadMessagesCount + " unread bot messages.", 
                "Bot Messages", 
                JOptionPane.INFORMATION_MESSAGE);
            unreadMessagesCount = 0;
            updateNotificationIcon();
        });


        // NEW: Emergency Button 
        JButton emergencyBtn = new JButton("EMERGENCY");
        emergencyBtn.setBackground(BAR_RED.darker()); 
        emergencyBtn.setForeground(Color.WHITE);
        emergencyBtn.setFont(new Font("Arial", Font.BOLD, 20)); 
        emergencyBtn.setPreferredSize(new Dimension(180, 60)); 
        emergencyBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        emergencyBtn.setFocusPainted(false);
        emergencyBtn.setBorderPainted(false);
        // Implements IClinicOperations
        emergencyBtn.addActionListener(e -> handleEmergencyCall()); 

        JPanel btnWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0)); 
        btnWrapper.setOpaque(false);
        btnWrapper.add(notificationIcon); // ADD THE ICON
        btnWrapper.add(emergencyBtn); 
        btnWrapper.add(logoutBtn); 

        panel.add(btnWrapper, BorderLayout.EAST);

        return panel;
    }

    /**
     * Implements IClinicOperations (Interface).
     * Handles the Emergency Call button click, logging the event and notifying via Telegram.
     */
    @Override
    public void handleEmergencyCall() {
        int result = JOptionPane.showConfirmDialog(this,
                "You are about to place an EMERGENCY ALERT.\nThis will notify clinic staff immediately and log the event.\nProceed?",
                "CONFIRM EMERGENCY ALERT",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (result == JOptionPane.YES_OPTION) {
            // UPDATED: Use the combined logAndNotifyEmergency utility
            if (EmergencyLogWriter.logAndNotifyEmergency(currentBitsId, currentFullName)) {
                JOptionPane.showMessageDialog(this,
                        "EMERGENCY ALERT SENT!\nClinic staff and wardens have been notified via Telegram for user: " + currentBitsId,
                        "ALERT CONFIRMED",
                        JOptionPane.ERROR_MESSAGE); // Use error icon for urgency
            } else {
                 // This is the Exception Handling feedback
                 JOptionPane.showMessageDialog(this,
                        "EMERGENCY ALERT FAILED TO LOG OR NOTIFY! Please check file permissions/network.",
                        "LOG/NOTIFY ERROR",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // --- NEW: Notification System Methods ---

    /**
     * Updates the notification icon text and color on the EDT.
     */
    private void updateNotificationIcon() {
        SwingUtilities.invokeLater(() -> {
            notificationIcon.setText("✉ (" + unreadMessagesCount + ")");
            if (unreadMessagesCount > 0) {
                notificationIcon.setBackground(BAR_RED); // Use red to signify urgency/unread
                notificationIcon.setToolTipText(unreadMessagesCount + " new bot messages received.");
            } else {
                notificationIcon.setBackground(BRAND_BLUE.brighter());
                notificationIcon.setToolTipText("No unread messages.");
            }
        });
    }

    /**
     * Displays a live pop-up notification.
     * @param message The content of the new log entry.
     */
    private void showLiveNotification(String message) {
        SwingUtilities.invokeLater(() -> {
            // Show Pop-up notification
            JOptionPane.showMessageDialog(
                this, 
                "New Bot Message Received:\n" + message, 
                "Live Notification", 
                JOptionPane.INFORMATION_MESSAGE
            );
            
            // Update Icon
            unreadMessagesCount++;
            updateNotificationIcon();
        });
    }

    /**
     * Initializes and runs the Java WatchService in a background thread
     * to monitor the messages.txt file for new logs.
     */
    private void startMessageWatcher() {
        new Thread(() -> {
            try {
                Path filePath = Paths.get(MESSAGES_FILE);
                Path dir = filePath.getParent();
                if (dir == null) {
                    System.err.println("Error: Parent directory for messages file not found.");
                    return;
                }
                
                WatchService watchService = FileSystems.getDefault().newWatchService();
                dir.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);
                
                // Initialize last size check
                File msgFile = filePath.toFile();
                if (msgFile.exists()) {
                     lastMsgSize = msgFile.length();
                } else {
                     System.out.println("Messages file not found, starting tracking from zero.");
                }

                while (true) {
                    WatchKey key;
                    try {
                        key = watchService.take();
                    } catch (InterruptedException ex) {
                        return;
                    }

                    for (WatchEvent<?> event : key.pollEvents()) {
                        if (event.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {
                            File changedFile = dir.resolve((Path) event.context()).toFile();
                            
                            // Check only for the target file
                            if (changedFile.getAbsolutePath().equals(msgFile.getAbsolutePath())) {
                                long newSize = changedFile.length();
                                if (newSize > lastMsgSize) {
                                    // Read new content from the last known position
                                    try (RandomAccessFile raf = new RandomAccessFile(changedFile, "r")) {
                                        raf.seek(lastMsgSize);
                                        String line;
                                        while ((line = raf.readLine()) != null) {
                                            // Show the raw log entry as the message content
                                            if (!line.trim().isEmpty()) {
                                                showLiveNotification(line.trim());
                                            }
                                        }
                                        lastMsgSize = newSize;
                                    } catch (Exception e) {
                                        System.err.println("Error reading new log entry: " + e.getMessage());
                                    }
                                }
                            }
                        }
                    }
                    key.reset();
                }
            } catch (Exception e) {
                System.err.println("Message Watcher Error: " + e.getMessage());
            }
        }, "MessageWatcherThread").start();
    }


    // --- Dashboard Content Creation Methods (Unchanged UI logic) ---
    
    private JPanel createDashboardContent() {
        JPanel dashboard = new JPanel(new BorderLayout(30, 30)); 
        dashboard.setOpaque(false);
        dashboard.setBorder(BorderFactory.createEmptyBorder(25, 50, 25, 50)); 

        JPanel cardGrid = new JPanel(new GridLayout(1, 3, 30, 0)); 
        cardGrid.setOpaque(false);

        cardGrid.add(createEmailCard());
        cardGrid.add(createMedicalAlertsCard());
        cardGrid.add(createDataCard("Last Login (Mock)", lastLogin, new Color(220, 237, 255)));

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
        contentLabel.setFont(BODY_FONT_PLAIN); 
        card.add(contentLabel, BorderLayout.CENTER);

        return card;
    }

    private JPanel createEmailCard() {
        JPanel card = createDataCard("BITS Email", "", new Color(200, 255, 200));

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setOpaque(false);
        
        emailLabel = new JLabel("N/A"); 
        emailLabel.setFont(BODY_FONT_PLAIN);
        emailLabel.setForeground(DARK_BLUE.darker());
        contentPanel.add(emailLabel, BorderLayout.CENTER);
        
        card.remove(card.getComponent(1)); 
        card.add(contentPanel, BorderLayout.CENTER);
        return card;
    }

    private JPanel createMedicalAlertsCard() {
        JPanel card = createDataCard("Medical Alerts", "", new Color(255, 240, 210));

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setOpaque(false);
        
        medicalAlertsLabel = new JLabel("N/A"); 
        medicalAlertsLabel.setFont(BODY_FONT_PLAIN);
        medicalAlertsLabel.setForeground(BAR_RED.darker());
        contentPanel.add(medicalAlertsLabel, BorderLayout.CENTER);
        
        card.remove(card.getComponent(1)); 
        card.add(contentPanel, BorderLayout.CENTER);
        return card;
    }
    
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
    
    private JPanel createClinicsCard() {
        List<Clinic> clinics = List.of(
            new Clinic("Fakeeh University Hospital", "Dubai Silicon Oasis", "Hospital"),
            new Clinic("Aster Clinic", "Dubai Silicon Oasis", "Clinic"),
            new Clinic("Health Connect Poly Clinic", "Academic City", "Clinic")
        );
        
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

        for (Clinic clinic : clinics) {
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
}