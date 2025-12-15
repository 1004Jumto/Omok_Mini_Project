package team.omok.omok_mini_project.repository;

import team.omok.omok_mini_project.domain.UserVO;
import team.omok.omok_mini_project.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class UserDAO {
    public UserVO findById(String id) {

        String sql = "SELECT id, password, nickname FROM users WHERE id=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                UserVO vo = new UserVO();
                vo.setId(rs.getString("id"));
                vo.setPassword(rs.getString("password"));
                vo.setNickname(rs.getString("nickname"));

                System.out.println("[INFO]login-" + vo);
                return vo;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
