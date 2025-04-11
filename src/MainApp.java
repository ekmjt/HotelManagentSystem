import java.awt.*;
import java.sql.*;
import java.util.Vector;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

/**
 * Hotel Management System – Single-File Edition
 * --------------------------------------------
 * Features:
 *  1. CRUD for reservations, rooms, inventory, bills, reports.
 *  2. Login or Register with a 'users' table in MySQL (username, pass, role).
 *  3. Enhanced UI styling with colors, borders, and spacing.
 *
 * How to compile & run:
 *  1. Place this file: HotelManagementSystem/src/MainApp.java
 *  2. Download mysql-connector-j-<version>.jar into: HotelManagementSystem/lib/
 *  3. In terminal:
 *      cd HotelManagementSystem
 *      javac -cp "lib/mysql-connector-j-9.2.0.jar" src/MainApp.java
 *      java  -cp "lib/mysql-connector-j-9.2.0.jar:src" MainApp
 *  4. The app should launch in Nimbus look-and-feel with a Login screen.
 *
 * Notes:
 *  - Adjust the DB credentials in the DB class below.
 *  - Create the 'users' table plus the five main tables: reservations, rooms,
 *    inventory, bills, reports. Then do inserts.
 */
public class MainApp extends JFrame {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
            } catch (Exception ignored) {}
            new MainApp().setVisible(true);
        });
    }

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cardPanel      = new JPanel(cardLayout);

    public MainApp() {
        super("Hotel Management System");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1200, 720);
        setLocationRelativeTo(null);

        setJMenuBar(buildMenuBar());

        // Add login panel by default
        cardPanel.add(new LoginPanel(this), "LOGIN");
        add(cardPanel, BorderLayout.CENTER);

        // Show login initially
        cardLayout.show(cardPanel, "LOGIN");
    }

    private JMenuBar buildMenuBar() {
        JMenuBar mb = new JMenuBar();
        mb.setBackground(new Color(230, 240, 250));

        JMenu fileMenu = new JMenu("File");
        JMenuItem logoutItem = new JMenuItem("Logout");
        logoutItem.addActionListener(e -> cardLayout.show(cardPanel, "LOGIN"));
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> System.exit(0));

        fileMenu.add(logoutItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);

        mb.add(fileMenu);
        return mb;
    }

    /**
     * Switches from login/register panel to the role-based dashboard.
     */
    public void showDashboard(String role) {
        JComponent dash = DashboardFactory.create(role);
        cardPanel.add(dash, "DASH");
        cardLayout.show(cardPanel, "DASH");
        setTitle("Hotel MS – " + role);
    }

    /**
     * Switch to the register panel.
     */
    public void showRegisterPanel() {
        cardPanel.add(new RegisterPanel(this), "REGISTER");
        cardLayout.show(cardPanel, "REGISTER");
    }

    /**
     * Panel: Login (pulls from 'users' table).
     */
    static class LoginPanel extends JPanel {
        private final MainApp parent;
        private final JTextField tfUser  = new JTextField(15);
        private final JPasswordField tfPass= new JPasswordField(15);

        LoginPanel(MainApp parent) {
            this.parent = parent;
            setLayout(new GridBagLayout());
            setBackground(new Color(220, 230, 245));
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(10,10,10,10);

            JLabel title = new JLabel("Hotel Management System", SwingConstants.CENTER);
            title.setFont(new Font("SansSerif", Font.BOLD, 28));
            title.setForeground(new Color(30, 50, 100));
            gbc.gridx=0; gbc.gridy=0; gbc.gridwidth=2; add(title, gbc);

            gbc.gridwidth=1; gbc.anchor=GridBagConstraints.EAST;
            gbc.gridy=1; gbc.gridx=0; add(new JLabel("Username: "), gbc);
            gbc.gridx=1; add(tfUser, gbc);

            gbc.gridy=2; gbc.gridx=0; add(new JLabel("Password: "), gbc);
            gbc.gridx=1; add(tfPass, gbc);

            gbc.gridy=3; gbc.gridx=0; gbc.gridwidth=2; gbc.anchor=GridBagConstraints.CENTER;
            JButton btnLogin = new JButton("Login");
            btnLogin.setForeground(Color.WHITE);
            btnLogin.setBackground(new Color(50, 100, 150));
            add(btnLogin, gbc);

            gbc.gridy=4;
            JButton btnRegister = new JButton("Register");
            add(btnRegister, gbc);

            btnLogin.addActionListener(e -> doLogin());
            btnRegister.addActionListener(e -> parent.showRegisterPanel());
        }

        private void doLogin() {
            String user = tfUser.getText().trim();
            String pass = new String(tfPass.getPassword()).trim();

            if (user.isEmpty() || pass.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Please fill username and password",
                        "Login Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            String sql = "SELECT role FROM users WHERE username=? AND pass=?";
            try (Connection c = DB.get();
                 PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, user);
                ps.setString(2, pass);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String role = rs.getString("role");
                        ((MainApp)SwingUtilities.getWindowAncestor(this)).showDashboard(role);
                    } else {
                        JOptionPane.showMessageDialog(this,
                                "Invalid credentials",
                                "Login Error",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Panel: Register new user (INSERT into 'users' table).
     */
    static class RegisterPanel extends JPanel {
        private final MainApp parent;
        private final JTextField tfUser = new JTextField(15);
        private final JPasswordField tfPass= new JPasswordField(15);
        private final JComboBox<String> cbRole = new JComboBox<>(new String[]{"Admin","Housekeeping","Reception"});

        RegisterPanel(MainApp parent) {
            this.parent = parent;
            setLayout(new GridBagLayout());
            setBackground(new Color(245, 230, 250));

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(10,10,10,10);

            JLabel title = new JLabel("Register New User", SwingConstants.CENTER);
            title.setFont(new Font("SansSerif", Font.BOLD, 24));
            title.setForeground(new Color(100, 50, 120));
            gbc.gridx=0; gbc.gridy=0; gbc.gridwidth=2; add(title, gbc);

            gbc.gridwidth=1; gbc.anchor=GridBagConstraints.EAST;
            gbc.gridy=1; gbc.gridx=0; add(new JLabel("Username: "), gbc);
            gbc.gridx=1; add(tfUser, gbc);

            gbc.gridy=2; gbc.gridx=0; add(new JLabel("Password: "), gbc);
            gbc.gridx=1; add(tfPass, gbc);

            gbc.gridy=3; gbc.gridx=0; add(new JLabel("Role: "), gbc);
            gbc.gridx=1; add(cbRole, gbc);

            gbc.gridy=4; gbc.gridx=0; gbc.gridwidth=2; gbc.anchor=GridBagConstraints.CENTER;
            JButton btnReg = new JButton("Register");
            add(btnReg, gbc);

            gbc.gridy=5;
            JButton btnBack = new JButton("Back to Login");
            add(btnBack, gbc);

            btnReg.addActionListener(e -> doRegister());
            btnBack.addActionListener(e -> parent.cardLayout.show(parent.cardPanel, "LOGIN"));
        }

        private void doRegister() {
            String user = tfUser.getText().trim();
            String pass = new String(tfPass.getPassword()).trim();
            String role = (String) cbRole.getSelectedItem();

            if (user.isEmpty() || pass.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Username and Password are required",
                        "Register Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            String sql = "INSERT INTO users(username, pass, role) VALUES(?,?,?)";
            try (Connection c = DB.get();
                 PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, user);
                ps.setString(2, pass);
                ps.setString(3, role);
                ps.executeUpdate();
                JOptionPane.showMessageDialog(this,
                        "Registration successful! You can now login.",
                        "Register",
                        JOptionPane.INFORMATION_MESSAGE);
                ((MainApp)SwingUtilities.getWindowAncestor(this)).cardLayout.show(parent.cardPanel, "LOGIN");
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this,
                        "Username already taken or DB error",
                        "Register Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Factory to produce different dashboards for each role.
     */
    static class DashboardFactory {
        static JComponent create(String role) {
            JTabbedPane tabs = new JTabbedPane();

            // Admin sees everything
            if (role.equalsIgnoreCase("Admin")) {
                tabs.addTab("Reservations", new ReservationsPanel());
                tabs.addTab("Rooms",       new RoomsPanel());
                tabs.addTab("Inventory",   new InventoryPanel());
                tabs.addTab("Bills",       new BillsPanel());
                tabs.addTab("Reports",     new ReportsPanel());
            }
            // Reception sees reservations & bills
            else if (role.equalsIgnoreCase("Reception")) {
                tabs.addTab("Reservations", new ReservationsPanel());
                tabs.addTab("Bills",        new BillsPanel());
            }
            // Housekeeping sees rooms & inventory
            else if (role.equalsIgnoreCase("Housekeeping")) {
                tabs.addTab("Rooms",     new RoomsPanel());
                tabs.addTab("Inventory", new InventoryPanel());
            }
            else {
                // fallback
                JPanel p = new JPanel();
                p.add(new JLabel("No specific dashboard for this role"));
                tabs.addTab("Home", p);
            }

            return tabs;
        }
    }

    /**
     * The DB Singleton connection.
     */
    static final class DB {
        private static Connection conn;
    
        static Connection get() {
            try {
                if (conn == null || conn.isClosed()) {
                    Class.forName("com.mysql.cj.jdbc.Driver");
                    conn = DriverManager.getConnection(
                        "jdbc:mysql://44.212.95.102:3306/testdb?serverTimezone=UTC",
                        "newuser",
                        "password"
                    );
                }
            } catch (Exception ex) {
                throw new RuntimeException("Cannot connect DB", ex);
            }
            return conn;
        }
    
        static void close(AutoCloseable ac) {
            if (ac != null) {
                try { ac.close(); } catch (Exception ignored) {}
            }
        }
    }
    


    /**
     * Utility: Convert a ResultSet to a TableModel
     */
    static TableModel rsToTableModel(ResultSet rs) throws SQLException {
        ResultSetMetaData md = rs.getMetaData();
        int colCount = md.getColumnCount();
        Vector<String> cols = new Vector<>();
        for (int i=1; i<=colCount; i++) {
            cols.add(md.getColumnName(i));
        }
        Vector<Vector<Object>> data = new Vector<>();
        while(rs.next()) {
            Vector<Object> row = new Vector<>();
            for (int c=1; c<=colCount; c++){
                row.add(rs.getObject(c));
            }
            data.add(row);
        }
        return new DefaultTableModel(data, cols){
            public boolean isCellEditable(int row, int col){ return false; }
        };
    }

    // ───────────────────────────────────────────────────────────────
    //                   RESERVATIONS PANEL
    // ───────────────────────────────────────────────────────────────
    static class ReservationsPanel extends JPanel {
        private final JTable table = new JTable();
        private final JTextField tfID = new JTextField(5);
        private final JTextField tfGuest = new JTextField(10);
        private final JTextField tfRoom  = new JTextField(5);
        private final JTextField tfCheckIn = new JTextField(10);
        private final JTextField tfCheckOut= new JTextField(10);

        ReservationsPanel() {
            setLayout(new BorderLayout(5,5));
            setBackground(new Color(240,245,250));

            JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JLabel title = new JLabel("Reservations");
            title.setFont(title.getFont().deriveFont(Font.BOLD,18));
            topPanel.setBackground(new Color(210,225,240));
            topPanel.add(title);

            add(topPanel, BorderLayout.NORTH);

            // Table in center
            add(new JScrollPane(table), BorderLayout.CENTER);

            // CRUD panel on the right
            add(buildCrudPanel(), BorderLayout.EAST);

            loadData();
        }

        private JPanel buildCrudPanel(){
            JPanel p = new JPanel(new GridLayout(0,1,5,5));
            p.setBorder(BorderFactory.createTitledBorder(
                    new EtchedBorder(), "Reservations CRUD"));
            p.setBackground(new Color(220,235,245));

            p.add(new JLabel("ID (Update/Delete)")); p.add(tfID);
            p.add(new JLabel("Guest Name")); p.add(tfGuest);
            p.add(new JLabel("Room #"));     p.add(tfRoom);
            p.add(new JLabel("CheckIn (YYYY-MM-DD)")); p.add(tfCheckIn);
            p.add(new JLabel("CheckOut")); p.add(tfCheckOut);

            JButton btnAdd = new JButton("Add");
            JButton btnUpd = new JButton("Update");
            JButton btnDel = new JButton("Delete");
            JButton btnRef = new JButton("Refresh");

            btnAdd.addActionListener(e -> addRow());
            btnUpd.addActionListener(e -> updateRow());
            btnDel.addActionListener(e -> deleteRow());
            btnRef.addActionListener(e -> loadData());

            p.add(btnAdd); p.add(btnUpd); p.add(btnDel); p.add(btnRef);

            return p;
        }

        private void loadData(){
            String sql = "SELECT * FROM reservations";
            try (Statement st = DB.get().createStatement();
                 ResultSet rs = st.executeQuery(sql)) {
                table.setModel(rsToTableModel(rs));
            } catch (SQLException ex){
                showError(ex);
            }
        }

        private void addRow(){
            String sql = "INSERT INTO reservations(guest,room,checkin,checkout) VALUES(?,?,?,?)";
            try (PreparedStatement ps = DB.get().prepareStatement(sql)) {
                ps.setString(1, tfGuest.getText().trim());
                ps.setInt(2, Integer.parseInt(tfRoom.getText().trim()));
                ps.setString(3, tfCheckIn.getText().trim());
                ps.setString(4, tfCheckOut.getText().trim());
                ps.executeUpdate();
                loadData();
            } catch (Exception ex){
                showError(ex);
            }
        }

        private void updateRow(){
            if (tfID.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "ID is required to update");
                return;
            }
            String sql = "UPDATE reservations SET guest=?, room=?, checkin=?, checkout=? WHERE id=?";
            try (PreparedStatement ps = DB.get().prepareStatement(sql)) {
                ps.setString(1, tfGuest.getText().trim());
                ps.setInt(2, Integer.parseInt(tfRoom.getText().trim()));
                ps.setString(3, tfCheckIn.getText().trim());
                ps.setString(4, tfCheckOut.getText().trim());
                ps.setInt(5, Integer.parseInt(tfID.getText().trim()));
                ps.executeUpdate();
                loadData();
            } catch (Exception ex){
                showError(ex);
            }
        }

        private void deleteRow(){
            if (tfID.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "ID is required to delete");
                return;
            }
            String sql = "DELETE FROM reservations WHERE id=?";
            try (PreparedStatement ps = DB.get().prepareStatement(sql)){
                ps.setInt(1, Integer.parseInt(tfID.getText().trim()));
                ps.executeUpdate();
                loadData();
            } catch (Exception ex){
                showError(ex);
            }
        }

        private void showError(Exception ex){
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, ex.getMessage(),"DB Error",JOptionPane.ERROR_MESSAGE);
        }
    }

    // ───────────────────────────────────────────────────────────────
    //                   ROOMS PANEL
    // ───────────────────────────────────────────────────────────────
    static class RoomsPanel extends JPanel {
        private final JTable table = new JTable();

        // textfields for CRUD
        private final JTextField tfID   = new JTextField(5);
        private final JTextField tfNum  = new JTextField(5);
        private final JTextField tfType = new JTextField(10);
        private final JTextField tfStat = new JTextField(10);

        RoomsPanel() {
            setLayout(new BorderLayout(5,5));
            setBackground(new Color(245,240,230));

            JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JLabel title = new JLabel("Rooms");
            title.setFont(title.getFont().deriveFont(Font.BOLD,18));
            topPanel.setBackground(new Color(235,220,210));
            topPanel.add(title);
            add(topPanel, BorderLayout.NORTH);

            add(new JScrollPane(table), BorderLayout.CENTER);
            add(buildCrudPanel(), BorderLayout.EAST);

            loadData();
        }

        private JPanel buildCrudPanel() {
            JPanel p = new JPanel(new GridLayout(0,1,5,5));
            p.setBorder(BorderFactory.createTitledBorder(
                    new EtchedBorder(), "Rooms CRUD"));
            p.setBackground(new Color(235,225,210));

            p.add(new JLabel("ID (Upd/Del)")); p.add(tfID);
            p.add(new JLabel("Number")); p.add(tfNum);
            p.add(new JLabel("Type"));   p.add(tfType);
            p.add(new JLabel("Status")); p.add(tfStat);

            JButton btnAdd = new JButton("Add");
            JButton btnUpd = new JButton("Update");
            JButton btnDel = new JButton("Delete");
            JButton btnRef = new JButton("Refresh");

            btnAdd.addActionListener(e->addRow());
            btnUpd.addActionListener(e->updateRow());
            btnDel.addActionListener(e->deleteRow());
            btnRef.addActionListener(e->loadData());

            p.add(btnAdd); p.add(btnUpd); p.add(btnDel); p.add(btnRef);
            return p;
        }

        private void loadData() {
            String sql = "SELECT * FROM rooms";
            try (Statement st = DB.get().createStatement();
                 ResultSet rs = st.executeQuery(sql)) {
                table.setModel(rsToTableModel(rs));
            } catch (SQLException ex){
                showError(ex);
            }
        }

        private void addRow(){
            String sql = "INSERT INTO rooms(number, type, status) VALUES(?,?,?)";
            try (PreparedStatement ps = DB.get().prepareStatement(sql)){
                ps.setInt(1, Integer.parseInt(tfNum.getText().trim()));
                ps.setString(2, tfType.getText().trim());
                ps.setString(3, tfStat.getText().trim());
                ps.executeUpdate();
                loadData();
            } catch(Exception ex){
                showError(ex);
            }
        }

        private void updateRow(){
            if (tfID.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "ID required to update");
                return;
            }
            String sql = "UPDATE rooms SET number=?, type=?, status=? WHERE id=?";
            try (PreparedStatement ps = DB.get().prepareStatement(sql)){
                ps.setInt(1, Integer.parseInt(tfNum.getText().trim()));
                ps.setString(2, tfType.getText().trim());
                ps.setString(3, tfStat.getText().trim());
                ps.setInt(4, Integer.parseInt(tfID.getText().trim()));
                ps.executeUpdate();
                loadData();
            } catch(Exception ex){
                showError(ex);
            }
        }

        private void deleteRow(){
            if (tfID.getText().trim().isEmpty()){
                JOptionPane.showMessageDialog(this, "ID required to delete");
                return;
            }
            String sql = "DELETE FROM rooms WHERE id=?";
            try (PreparedStatement ps = DB.get().prepareStatement(sql)){
                ps.setInt(1, Integer.parseInt(tfID.getText().trim()));
                ps.executeUpdate();
                loadData();
            } catch(Exception ex){
                showError(ex);
            }
        }

        private void showError(Exception ex){
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, ex.getMessage(),"DB Error",JOptionPane.ERROR_MESSAGE);
        }
    }

    // ───────────────────────────────────────────────────────────────
    //                   INVENTORY PANEL (Observer optional)
    // ───────────────────────────────────────────────────────────────
    static class InventoryPanel extends JPanel {
        private final JTable table = new JTable();

        // textfields for CRUD
        private final JTextField tfID    = new JTextField(5);
        private final JTextField tfItem  = new JTextField(15);
        private final JTextField tfQty   = new JTextField(5);

        InventoryPanel() {
            setLayout(new BorderLayout(5,5));
            setBackground(new Color(240,250,240));

            JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JLabel title = new JLabel("Inventory");
            title.setFont(title.getFont().deriveFont(Font.BOLD,18));
            topPanel.setBackground(new Color(220,240,220));
            topPanel.add(title);
            add(topPanel, BorderLayout.NORTH);

            add(new JScrollPane(table), BorderLayout.CENTER);
            add(buildCrudPanel(), BorderLayout.EAST);

            loadData();
        }

        private JPanel buildCrudPanel() {
            JPanel p = new JPanel(new GridLayout(0,1,5,5));
            p.setBorder(BorderFactory.createTitledBorder(
                    new EtchedBorder(), "Inventory CRUD"));
            p.setBackground(new Color(210,235,210));

            p.add(new JLabel("ID (Upd/Del)")); p.add(tfID);
            p.add(new JLabel("Item")); p.add(tfItem);
            p.add(new JLabel("Quantity")); p.add(tfQty);

            JButton btnAdd = new JButton("Add");
            JButton btnUpd = new JButton("Update");
            JButton btnDel = new JButton("Delete");
            JButton btnRef = new JButton("Refresh");

            btnAdd.addActionListener(e->addRow());
            btnUpd.addActionListener(e->updateRow());
            btnDel.addActionListener(e->deleteRow());
            btnRef.addActionListener(e->loadData());

            p.add(btnAdd); p.add(btnUpd); p.add(btnDel); p.add(btnRef);
            return p;
        }

        private void loadData(){
            String sql = "SELECT * FROM inventory";
            try (Statement st = DB.get().createStatement();
                 ResultSet rs = st.executeQuery(sql)){
                table.setModel(rsToTableModel(rs));
            } catch (SQLException ex){
                showErr(ex);
            }
        }

        private void addRow(){
            String sql = "INSERT INTO inventory(item,quantity) VALUES(?,?)";
            try (PreparedStatement ps = DB.get().prepareStatement(sql)){
                ps.setString(1, tfItem.getText().trim());
                ps.setInt(2, Integer.parseInt(tfQty.getText().trim()));
                ps.executeUpdate();
                loadData();
            } catch(Exception ex){
                showErr(ex);
            }
        }

        private void updateRow(){
            if (tfID.getText().trim().isEmpty()){
                JOptionPane.showMessageDialog(this, "ID needed for update");
                return;
            }
            String sql = "UPDATE inventory SET item=?, quantity=? WHERE id=?";
            try (PreparedStatement ps = DB.get().prepareStatement(sql)){
                ps.setString(1, tfItem.getText().trim());
                ps.setInt(2, Integer.parseInt(tfQty.getText().trim()));
                ps.setInt(3, Integer.parseInt(tfID.getText().trim()));
                ps.executeUpdate();
                loadData();
            } catch(Exception ex){
                showErr(ex);
            }
        }

        private void deleteRow(){
            if (tfID.getText().trim().isEmpty()){
                JOptionPane.showMessageDialog(this, "ID needed for delete");
                return;
            }
            String sql = "DELETE FROM inventory WHERE id=?";
            try (PreparedStatement ps = DB.get().prepareStatement(sql)){
                ps.setInt(1, Integer.parseInt(tfID.getText().trim()));
                ps.executeUpdate();
                loadData();
            } catch(Exception ex){
                showErr(ex);
            }
        }

        private void showErr(Exception ex){
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, ex.getMessage(),"DB Error",JOptionPane.ERROR_MESSAGE);
        }
    }

    // ───────────────────────────────────────────────────────────────
    //                   BILLS PANEL
    // ───────────────────────────────────────────────────────────────
    static class BillsPanel extends JPanel {
        private final JTable table = new JTable();

        private final JTextField tfID    = new JTextField(5);
        private final JTextField tfResID = new JTextField(5);
        private final JTextField tfAmt   = new JTextField(7);
        private final JCheckBox  chkPaid = new JCheckBox("Paid?");

        BillsPanel(){
            setLayout(new BorderLayout(5,5));
            setBackground(new Color(250,240,245));

            JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JLabel title = new JLabel("Bills");
            title.setFont(title.getFont().deriveFont(Font.BOLD,18));
            topPanel.setBackground(new Color(240,220,230));
            topPanel.add(title);
            add(topPanel, BorderLayout.NORTH);

            add(new JScrollPane(table), BorderLayout.CENTER);
            add(buildCrudPanel(), BorderLayout.EAST);

            loadData();
        }

        private JPanel buildCrudPanel(){
            JPanel p = new JPanel(new GridLayout(0,1,5,5));
            p.setBorder(BorderFactory.createTitledBorder(
                    new EtchedBorder(),"Bills CRUD"));
            p.setBackground(new Color(235,215,225));

            p.add(new JLabel("ID (Upd/Del)")); p.add(tfID);
            p.add(new JLabel("Reservation ID")); p.add(tfResID);
            p.add(new JLabel("Amount")); p.add(tfAmt);
            p.add(chkPaid);

            JButton btnAdd = new JButton("Add");
            JButton btnUpd = new JButton("Update");
            JButton btnDel = new JButton("Delete");
            JButton btnRef = new JButton("Refresh");

            btnAdd.addActionListener(e->addRow());
            btnUpd.addActionListener(e->updateRow());
            btnDel.addActionListener(e->deleteRow());
            btnRef.addActionListener(e->loadData());

            p.add(btnAdd); p.add(btnUpd); p.add(btnDel); p.add(btnRef);
            return p;
        }

        private void loadData(){
            String sql = "SELECT * FROM bills";
            try (Statement st = DB.get().createStatement();
                 ResultSet rs = st.executeQuery(sql)) {
                table.setModel(rsToTableModel(rs));
            } catch(SQLException ex){
                showErr(ex);
            }
        }

        private void addRow(){
            String sql = "INSERT INTO bills(reservation_id, amount, paid) VALUES(?,?,?)";
            try (PreparedStatement ps = DB.get().prepareStatement(sql)){
                ps.setInt(1,Integer.parseInt(tfResID.getText().trim()));
                ps.setBigDecimal(2, new java.math.BigDecimal(tfAmt.getText().trim()));
                ps.setBoolean(3, chkPaid.isSelected());
                ps.executeUpdate();
                loadData();
            } catch(Exception ex){
                showErr(ex);
            }
        }

        private void updateRow(){
            if(tfID.getText().trim().isEmpty()){
                JOptionPane.showMessageDialog(this, "ID required for update");
                return;
            }
            String sql = "UPDATE bills SET reservation_id=?, amount=?, paid=? WHERE id=?";
            try (PreparedStatement ps = DB.get().prepareStatement(sql)){
                ps.setInt(1,Integer.parseInt(tfResID.getText().trim()));
                ps.setBigDecimal(2,new java.math.BigDecimal(tfAmt.getText().trim()));
                ps.setBoolean(3, chkPaid.isSelected());
                ps.setInt(4, Integer.parseInt(tfID.getText().trim()));
                ps.executeUpdate();
                loadData();
            } catch(Exception ex){
                showErr(ex);
            }
        }

        private void deleteRow(){
            if(tfID.getText().trim().isEmpty()){
                JOptionPane.showMessageDialog(this, "ID required for delete");
                return;
            }
            String sql = "DELETE FROM bills WHERE id=?";
            try (PreparedStatement ps = DB.get().prepareStatement(sql)){
                ps.setInt(1, Integer.parseInt(tfID.getText().trim()));
                ps.executeUpdate();
                loadData();
            } catch(Exception ex){
                showErr(ex);
            }
        }

        private void showErr(Exception ex){
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, ex.getMessage(),"DB Error",JOptionPane.ERROR_MESSAGE);
        }
    }

    // ───────────────────────────────────────────────────────────────
    //                   REPORTS PANEL
    // ───────────────────────────────────────────────────────────────
    static class ReportsPanel extends JPanel {
        private final JTable table = new JTable();

        private final JTextField tfID  = new JTextField(5);
        private final JTextField tfInfo= new JTextField(20);

        ReportsPanel(){
            setLayout(new BorderLayout(5,5));
            setBackground(new Color(250,250,220));

            JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JLabel title = new JLabel("Reports");
            title.setFont(title.getFont().deriveFont(Font.BOLD,18));
            topPanel.setBackground(new Color(230,230,180));
            topPanel.add(title);
            add(topPanel, BorderLayout.NORTH);

            add(new JScrollPane(table), BorderLayout.CENTER);
            add(buildCrudPanel(), BorderLayout.EAST);

            loadData();
        }

        private JPanel buildCrudPanel() {
            JPanel p = new JPanel(new GridLayout(0,1,5,5));
            p.setBorder(BorderFactory.createTitledBorder(
                    new EtchedBorder(),"Reports CRUD"));
            p.setBackground(new Color(220,220,170));

            p.add(new JLabel("ID (Upd/Del)")); p.add(tfID);
            p.add(new JLabel("Info")); p.add(tfInfo);

            JButton btnAdd = new JButton("Add");
            JButton btnUpd = new JButton("Update");
            JButton btnDel = new JButton("Delete");
            JButton btnRef = new JButton("Refresh");
            JButton btnCustom = new JButton("Custom Query Example");

            btnAdd.addActionListener(e->addRow());
            btnUpd.addActionListener(e->updateRow());
            btnDel.addActionListener(e->deleteRow());
            btnRef.addActionListener(e->loadData());
            btnCustom.addActionListener(e->runCustom());

            p.add(btnAdd); p.add(btnUpd); p.add(btnDel); p.add(btnRef); p.add(btnCustom);
            return p;
        }

        private void loadData() {
            String sql = "SELECT * FROM reports";
            try (Statement st = DB.get().createStatement();
                 ResultSet rs = st.executeQuery(sql)) {
                table.setModel(rsToTableModel(rs));
            } catch (SQLException ex){
                showErr(ex);
            }
        }

        private void addRow() {
            String sql = "INSERT INTO reports(info) VALUES(?)";
            try (PreparedStatement ps = DB.get().prepareStatement(sql)){
                ps.setString(1, tfInfo.getText().trim());
                ps.executeUpdate();
                loadData();
            } catch (Exception ex){
                showErr(ex);
            }
        }

        private void updateRow() {
            if (tfID.getText().trim().isEmpty()){
                JOptionPane.showMessageDialog(this, "ID required to update");
                return;
            }
            String sql = "UPDATE reports SET info=? WHERE id=?";
            try (PreparedStatement ps = DB.get().prepareStatement(sql)){
                ps.setString(1, tfInfo.getText().trim());
                ps.setInt(2, Integer.parseInt(tfID.getText().trim()));
                ps.executeUpdate();
                loadData();
            } catch(Exception ex){
                showErr(ex);
            }
        }

        private void deleteRow() {
            if (tfID.getText().trim().isEmpty()){
                JOptionPane.showMessageDialog(this, "ID required to delete");
                return;
            }
            String sql = "DELETE FROM reports WHERE id=?";
            try (PreparedStatement ps = DB.get().prepareStatement(sql)){
                ps.setInt(1, Integer.parseInt(tfID.getText().trim()));
                ps.executeUpdate();
                loadData();
            } catch(Exception ex){
                showErr(ex);
            }
        }

        private void runCustom() {
            // Example custom query
            String sql = "SELECT room, COUNT(*) as stays FROM reservations GROUP BY room";
            try (Statement st = DB.get().createStatement();
                 ResultSet rs = st.executeQuery(sql)) {
                table.setModel(rsToTableModel(rs));
            } catch(Exception ex){
                showErr(ex);
            }
        }

        private void showErr(Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, ex.getMessage(),"DB Error",JOptionPane.ERROR_MESSAGE);
        }
    }
}
