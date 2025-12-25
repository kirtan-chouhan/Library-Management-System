
import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class LoginGUI extends JFrame {

    JTextField username;
    JPasswordField password;

    public LoginGUI() {
        setTitle("Library Login");
        setSize(300,200);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new GridLayout(4,2));

        username = new JTextField();
        password = new JPasswordField();
        JButton login = new JButton("Login");

        login.addActionListener(e -> authenticate());

        add(new JLabel("Username"));
        add(username);
        add(new JLabel("Password"));
        add(password);
        add(new JLabel(""));
        add(login);
    }

    void authenticate() {
        try (Connection con = DBConnection.getConnection()) {
            PreparedStatement ps =
                con.prepareStatement("SELECT role FROM users WHERE username=? AND password=?");

            ps.setString(1, username.getText());
            ps.setString(2, new String(password.getPassword()));

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String role = rs.getString("role");
                dispose();
                new LibraryGUI(role).setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, "Invalid Login");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginGUI().setVisible(true));
    }
}
