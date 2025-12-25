import java.sql.*;

public class BookDAO {

    // CREATE
    public void addBook(int id, String title, String author, int quantity) throws Exception {
        String sql =
          "INSERT INTO books(book_id, title, author, quantity, available) VALUES (?, ?, ?, ?, ?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.setString(2, title);
            ps.setString(3, author);
            ps.setInt(4, quantity);
            ps.setBoolean(5, quantity > 0);
            ps.executeUpdate();

        } catch (SQLIntegrityConstraintViolationException e) {
            throw new Exception("Book ID or Book already exists");
        }
    }

    // READ
    public ResultSet getAllBooks() throws Exception {
        return DBConnection.getConnection()
                .createStatement()
                .executeQuery("SELECT * FROM books");
    }

    // SEARCH
    public ResultSet searchBook(String keyword) throws Exception {
        PreparedStatement ps =
            DBConnection.getConnection()
                .prepareStatement("SELECT * FROM books WHERE title LIKE ?");
        ps.setString(1, "%" + keyword + "%");
        return ps.executeQuery();
    }

    // UPDATE
    public void updateBook(int id, String title, String author, int quantity) throws Exception {
        String sql =
          "UPDATE books SET title=?, author=?, quantity=?, available=? WHERE book_id=?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, title);
            ps.setString(2, author);
            ps.setInt(3, quantity);
            ps.setBoolean(4, quantity > 0);
            ps.setInt(5, id);
            ps.executeUpdate();
        }
    }

    // DELETE
    public void deleteBook(int id) throws Exception {
        PreparedStatement ps =
            DBConnection.getConnection()
                .prepareStatement("DELETE FROM books WHERE book_id=?");
        ps.setInt(1, id);
        ps.executeUpdate();
    }

    // VALIDATION
    public boolean bookExists(int bookId) throws Exception {
        PreparedStatement ps =
            DBConnection.getConnection()
                .prepareStatement("SELECT 1 FROM books WHERE book_id=?");
        ps.setInt(1, bookId);
        return ps.executeQuery().next();
    }
}
