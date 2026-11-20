package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DAO {
    protected static Connection con;

    private static final String DB_URL = "jdbc:mysql://localhost:3306/choptiters"
            + "?useSSL=false&allowPublicKeyRetrieval=true"
            + "&serverTimezone=Asia/Ho_Chi_Minh&characterEncoding=UTF-8";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "123456";

    public DAO() {
        if (con == null) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                con = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
            } catch (ClassNotFoundException | SQLException e) {
                throw new RuntimeException("Không thể kết nối CSDL: " + e.getMessage(), e);
            }
        }
    }
}