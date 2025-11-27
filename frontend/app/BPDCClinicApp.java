package app;

import java.awt.CardLayout;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class BPDCClinicApp extends JFrame {

    // Removed the duplicate nested definitions of IClinicOperations and ClinicUser.

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel mainPanel = new JPanel(cardLayout);
    
    // Reference to the dashboard components
    private final AdminDashboard adminDashboardPanel; 
    private final StudentDashboard studentDashboardPanel; 
    private final BPDCLoginFullScreen loginPanel;

    public BPDCClinicApp() {
        setTitle("BPDC Clinic System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH); // Full Screen
        setMinimumSize(new Dimension(800, 600));
        
        // 1. Initialize Panels
        // Note: Dashboards need to be created before MedicalInfoForm and Login 
        // because those panels need to access the nested Student/Staff classes.
        adminDashboardPanel = new AdminDashboard(this); 
        studentDashboardPanel = new StudentDashboard(this); 
        loginPanel = new BPDCLoginFullScreen(this);
        JPanel medicalFormPanel = new MedicalInfoForm(this);
        

        // 2. Add panels to the CardLayout
        mainPanel.add(loginPanel, "Login");
        mainPanel.add(medicalFormPanel, "MedicalForm");
        mainPanel.add(adminDashboardPanel, "AdminDashboard"); 
        mainPanel.add(studentDashboardPanel, "StudentDashboard"); 
        
        add(mainPanel);
        setVisible(true);

        // Start with the login screen
        showLogin();
    }

    public void showLogin() {
        cardLayout.show(mainPanel, "Login");
        setTitle("BPDC Clinic System - Login");
    }

    public void showMedicalForm() {
        cardLayout.show(mainPanel, "MedicalForm");
        setTitle("BPDC Clinic System - Medical Registration");
    }

    /**
     * Accepts Staff object.
     * Demonstrates: Polymorphism (method signature accepts concrete Staff type).
     * @param staff The Staff object (Nurse/Admin).
     */
    public void showAdminDashboard(AdminDashboard.Staff staff) {
        String alerts = staff.getPendingActionStatus();
        adminDashboardPanel.updateUserDetails(staff.getFullName(), staff.getBitsId(), staff.getEmail(), alerts);
        cardLayout.show(mainPanel, "AdminDashboard");
        setTitle(staff.getDashboardTitle());
    }
    
    /**
     * Accepts Student object.
     * FIX: Signature is changed back to accept Student object to resolve compilation errors.
     * @param student The Student object.
     */
    public void showStudentDashboard(StudentDashboard.Student student) {
        
        // Use the object's Encapsulated logic to generate the alert message
        String alerts = student.getAlertsStatus(); 
        
        // Pass encapsulated user data to the dashboard
        // Note: The StudentDashboard.updateUserDetails(String...) method is still present but redundant.
        studentDashboardPanel.updateUserDetails(student); 
        cardLayout.show(mainPanel, "StudentDashboard");
        setTitle(student.getDashboardTitle());
    }

    public static void main(String[] args) {
        // Run on the Event Dispatch Thread
        SwingUtilities.invokeLater(BPDCClinicApp::new);
    }
}