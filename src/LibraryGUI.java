import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.ResultSet;

public class LibraryGUI extends JFrame {

    String role;

    BookDAO bookDAO = new BookDAO();
    MemberDAO memberDAO = new MemberDAO();
    IssueDAO issueDAO = new IssueDAO();

    JTable bookTable, memberTable;

    JTextField bookIdField, titleField, authorField, quantityField, searchBookField;
    JTextField memberIdField, memberNameField, memberEmailField;

    public LibraryGUI(String role) {
        this.role = role;

        setTitle("Library Management System - " + role.toUpperCase());
        setSize(1000, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // ---------- TOP BAR ----------
        JPanel topBar = new JPanel(new BorderLayout());
        JLabel roleLabel = new JLabel("Logged in as: " + role.toUpperCase());
        JButton logoutBtn = new JButton("Logout");

        logoutBtn.addActionListener(e -> logout());

        topBar.add(roleLabel, BorderLayout.WEST);
        topBar.add(logoutBtn, BorderLayout.EAST);

        // ---------- TABS ----------
        JTabbedPane tabs = new JTabbedPane();

        tabs.add("View Books", viewBooksPanel());
        tabs.add("Issue Book", issueBookPanel());
        tabs.add("Return Book", returnBookPanel());

        if (role.equals("admin")) {
            tabs.add("Add Book", addBookPanel());
            tabs.add("Members", membersPanel());
        }

        add(topBar, BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);
    }

    // ================= LOGOUT =================
    void logout() {
        if (JOptionPane.showConfirmDialog(
                this,
                "Do you want to logout?",
                "Confirm Logout",
                JOptionPane.YES_NO_OPTION
        ) == JOptionPane.YES_OPTION) {
            dispose();
            new LoginGUI().setVisible(true);
        }
    }

    // ================= ADD BOOK =================
    JPanel addBookPanel() {
        JPanel p = new JPanel(new GridLayout(5, 2, 10, 10));

        bookIdField = new JTextField();
        titleField = new JTextField();
        authorField = new JTextField();
        quantityField = new JTextField();
        JButton add = new JButton("Add Book");

        add.addActionListener(e -> {
            try {
                bookDAO.addBook(
                        Integer.parseInt(bookIdField.getText()),
                        titleField.getText(),
                        authorField.getText(),
                        Integer.parseInt(quantityField.getText())
                );
                JOptionPane.showMessageDialog(this, "Book Added");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage());
            }
        });

        p.add(new JLabel("Book ID"));  p.add(bookIdField);
        p.add(new JLabel("Title"));    p.add(titleField);
        p.add(new JLabel("Author"));   p.add(authorField);
        p.add(new JLabel("Quantity")); p.add(quantityField);
        p.add(new JLabel(""));         p.add(add);

        return p;
    }

    // ================= VIEW BOOKS =================
    JPanel viewBooksPanel() {
        JPanel p = new JPanel(new BorderLayout());

        bookTable = new JTable();
        searchBookField = new JTextField();

        JButton load = new JButton("Load");
        JButton search = new JButton("Search");
        JButton update = new JButton("Update");
        JButton delete = new JButton("Delete");

        load.addActionListener(e -> loadBooks());
        search.addActionListener(e -> searchBooks());
        update.addActionListener(e -> updateBook());
        delete.addActionListener(e -> deleteBook());

        JPanel top = new JPanel(new GridLayout(1, 5, 5, 5));
        top.add(searchBookField);
        top.add(search);
        top.add(load);

        if (role.equals("admin")) {
            top.add(update);
            top.add(delete);
        }

        p.add(top, BorderLayout.NORTH);
        p.add(new JScrollPane(bookTable), BorderLayout.CENTER);

        return p;
    }

    void loadBooks() {
        try {
            DefaultTableModel model =
                new DefaultTableModel(
                    new String[]{"ID","Title","Author","Quantity","Status"},0);

            ResultSet rs = bookDAO.getAllBooks();
            while (rs.next()) {
                int q = rs.getInt("quantity");
                model.addRow(new Object[]{
                        rs.getInt("book_id"),
                        rs.getString("title"),
                        rs.getString("author"),
                        q,
                        q > 0 ? "Available" : "Unavailable"
                });
            }
            bookTable.setModel(model);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    void searchBooks() {
        try {
            DefaultTableModel model =
                new DefaultTableModel(
                    new String[]{"ID","Title","Author","Quantity","Status"},0);

            ResultSet rs = bookDAO.searchBook(searchBookField.getText());
            while (rs.next()) {
                int q = rs.getInt("quantity");
                model.addRow(new Object[]{
                        rs.getInt("book_id"),
                        rs.getString("title"),
                        rs.getString("author"),
                        q,
                        q > 0 ? "Available" : "Unavailable"
                });
            }
            bookTable.setModel(model);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    void updateBook() {
        int row = bookTable.getSelectedRow();
        if (row == -1) return;

        int id = (int) bookTable.getValueAt(row, 0);

        JTextField t = new JTextField();
        JTextField a = new JTextField();
        JTextField q = new JTextField();

        Object[] fields = {"Title", t, "Author", a, "Quantity", q};

        if (JOptionPane.showConfirmDialog(
                this, fields, "Update Book",
                JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {

            try {
                bookDAO.updateBook(id, t.getText(), a.getText(),
                        Integer.parseInt(q.getText()));
                loadBooks();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, e.getMessage());
            }
        }
    }

    void deleteBook() {
        int row = bookTable.getSelectedRow();
        if (row == -1) return;

        int id = (int) bookTable.getValueAt(row, 0);

        if (JOptionPane.showConfirmDialog(
                this, "Delete book?",
                "Confirm", JOptionPane.YES_NO_OPTION)
                == JOptionPane.YES_OPTION) {
            try {
                bookDAO.deleteBook(id);
                loadBooks();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, e.getMessage());
            }
        }
    }

    // ================= MEMBERS (ADMIN ONLY) =================
    JPanel membersPanel() {
        JPanel p = new JPanel(new BorderLayout());
        memberTable = new JTable();

        JButton load = new JButton("Load Members");
        load.addActionListener(e -> loadMembers());

        p.add(load, BorderLayout.NORTH);
        p.add(new JScrollPane(memberTable), BorderLayout.CENTER);

        return p;
    }

    void loadMembers() {
        if (!role.equals("admin")) return;

        try {
            DefaultTableModel model =
                new DefaultTableModel(
                    new String[]{"ID","Name","Email","Issued Books"},0);

            ResultSet rs = memberDAO.getAllMembers();
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("member_id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getInt("issued_count")
                });
            }
            memberTable.setModel(model);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    // ================= ISSUE BOOK =================
    JPanel issueBookPanel() {
        JPanel p = new JPanel(new GridLayout(3, 2, 10, 10));

        JTextField bookId = new JTextField();
        JTextField memberId = new JTextField();
        JButton issue = new JButton("Issue Book");

        issue.addActionListener(e -> {
            try {
                issueDAO.issueBook(
                        Integer.parseInt(bookId.getText()),
                        Integer.parseInt(memberId.getText())
                );
                JOptionPane.showMessageDialog(this, "Book Issued");
                loadBooks();

                if (role.equals("admin")) {
                    loadMembers();
                }

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage());
            }
        });

        p.add(new JLabel("Book ID"));   p.add(bookId);
        p.add(new JLabel("Member ID")); p.add(memberId);
        p.add(new JLabel(""));          p.add(issue);

        return p;
    }

    // ================= RETURN BOOK =================
    JPanel returnBookPanel() {
        JPanel p = new JPanel(new GridLayout(3, 2, 10, 10));

        JTextField bookId = new JTextField();
        JTextField memberId = new JTextField();
        JButton ret = new JButton("Return Book");

        ret.addActionListener(e -> {
            try {
                issueDAO.returnBook(
                        Integer.parseInt(bookId.getText()),
                        Integer.parseInt(memberId.getText())
                );
                JOptionPane.showMessageDialog(this, "Book Returned");
                loadBooks();

                if (role.equals("admin")) {
                    loadMembers();
                }

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage());
            }
        });

        p.add(new JLabel("Book ID"));   p.add(bookId);
        p.add(new JLabel("Member ID")); p.add(memberId);
        p.add(new JLabel(""));          p.add(ret);

        return p;
    }
}
