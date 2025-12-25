import java.sql.*;

public class MemberDAO {

    public void addMember(int id, String name, String email) throws Exception {
        String sql =
          "INSERT INTO members(member_id, name, email, issued_count) VALUES (?, ?, ?, 0)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.setString(2, name);
            ps.setString(3, email);
            ps.executeUpdate();

        } catch (SQLIntegrityConstraintViolationException e) {
            throw new Exception("Member ID or Email already exists");
        }
    }

    public ResultSet getAllMembers() throws Exception {
        return DBConnection.getConnection()
                .createStatement()
                .executeQuery("SELECT * FROM members");
    }

    public void updateMember(int id, String name, String email) throws Exception {
        PreparedStatement ps =
            DBConnection.getConnection()
                .prepareStatement(
                    "UPDATE members SET name=?, email=? WHERE member_id=?");
        ps.setString(1, name);
        ps.setString(2, email);
        ps.setInt(3, id);
        ps.executeUpdate();
    }

    public void deleteMember(int id) throws Exception {
        PreparedStatement ps =
            DBConnection.getConnection()
                .prepareStatement("DELETE FROM members WHERE member_id=?");
        ps.setInt(1, id);
        ps.executeUpdate();
    }

    public boolean memberExists(int memberId) throws Exception {
        PreparedStatement ps =
            DBConnection.getConnection()
                .prepareStatement("SELECT 1 FROM members WHERE member_id=?");
        ps.setInt(1, memberId);
        return ps.executeQuery().next();
    }
}
