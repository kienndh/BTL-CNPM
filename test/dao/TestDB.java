package dao;

import dao.DAO;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public final class TestDB {

    private TestDB() {
    }

    public static Connection con() {
        new DAO();
        return DAO.con;
    }

    public static void reset() throws Exception {
        Connection con = con();
        if (!con.getAutoCommit()) {
            con.rollback();
            con.setAutoCommit(true);
        }
        String script = new String(Files.readAllBytes(Paths.get("db", "init.db")), StandardCharsets.UTF_8);
        StringBuilder sb = new StringBuilder();
        for (String line : script.split("\\R")) {
            if (!line.trim().startsWith("--")) {
                sb.append(line).append('\n');
            }
        }
        try (Statement st = con.createStatement()) {
            for (String sql : sb.toString().split(";")) {
                if (!sql.trim().isEmpty()) {
                    st.execute(sql.trim());
                }
            }
        }
    }

    public static void exec(String sql, Object... params) throws Exception {
        try (PreparedStatement ps = con().prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            ps.executeUpdate();
        }
    }

    public static int queryInt(String sql, Object... params) throws Exception {
        try (PreparedStatement ps = con().prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : -1;
            }
        }
    }
}
