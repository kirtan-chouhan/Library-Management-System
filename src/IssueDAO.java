import java.sql.*;

public class IssueDAO {

    BookDAO bookDAO = new BookDAO();
    MemberDAO memberDAO = new MemberDAO();

    // ================= ISSUE BOOK =================
    public void issueBook(int bookId, int memberId) throws Exception {

        if (!bookDAO.bookExists(bookId))
            throw new Exception("Invalid Book ID");

        if (!memberDAO.memberExists(memberId))
            throw new Exception("Invalid Member ID");

        try (Connection con = DBConnection.getConnection()) {

            // check quantity
            PreparedStatement check =
              con.prepareStatement("SELECT quantity FROM books WHERE book_id=?");
            check.setInt(1, bookId);
            ResultSet rs = check.executeQuery();

            if (!rs.next() || rs.getInt("quantity") <= 0)
                throw new Exception("Book not available");

            // insert issue record
            PreparedStatement issue =
              con.prepareStatement(
                "INSERT INTO issued(book_id, member_id, issue_date) VALUES (?, ?, CURDATE())");
            issue.setInt(1, bookId);
            issue.setInt(2, memberId);
            issue.executeUpdate();

            // reduce book quantity
            PreparedStatement updateBook =
              con.prepareStatement(
                "UPDATE books SET quantity = quantity - 1, available = (quantity - 1 > 0) WHERE book_id=?");
            updateBook.setInt(1, bookId);
            updateBook.executeUpdate();

            // increment member issued count
            PreparedStatement updateMember =
              con.prepareStatement(
                "UPDATE members SET issued_count = issued_count + 1 WHERE member_id=?");
            updateMember.setInt(1, memberId);
            updateMember.executeUpdate();
        }
    }

    // ================= RETURN BOOK =================
    public void returnBook(int bookId, int memberId) throws Exception {

        try (Connection con = DBConnection.getConnection()) {

            // find latest unreturned issue
            PreparedStatement find =
              con.prepareStatement(
                "SELECT issue_id FROM issued " +
                "WHERE book_id=? AND member_id=? AND return_date IS NULL " +
                "ORDER BY issue_date DESC LIMIT 1");
            find.setInt(1, bookId);
            find.setInt(2, memberId);

            ResultSet rs = find.executeQuery();
            if (!rs.next())
                throw new Exception("No active issue found for this book & member");

            int issueId = rs.getInt("issue_id");

            // mark returned
            PreparedStatement ret =
              con.prepareStatement(
                "UPDATE issued SET return_date=CURDATE() WHERE issue_id=?");
            ret.setInt(1, issueId);
            ret.executeUpdate();

            // increase book quantity
            PreparedStatement updateBook =
              con.prepareStatement(
                "UPDATE books SET quantity = quantity + 1, available = TRUE WHERE book_id=?");
            updateBook.setInt(1, bookId);
            updateBook.executeUpdate();

            // reset member issued count
            PreparedStatement updateMember =
              con.prepareStatement(
                "UPDATE members SET issued_count = 0 WHERE member_id=?");
            updateMember.setInt(1, memberId);
            updateMember.executeUpdate();
        }
    }
}
