package Server1;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class Database {

    // ✅ Correct driver
    String drivername = "org.postgresql.Driver";

    // ✅ Convert to JDBC format
    String connectionURL = "jdbc:postgresql://dpg-d6u0mpf5r7bs73fobo30-a.oregon-postgres.render.com:5432/parking_db_zhr6";

    String username = "parking_db_zhr6_user";
    String password = "etzXsOLuxD0CVYdUjND6jJRJspQOZamQ";

    Statement stmt = null;
    ResultSet rs = null;
    Connection conn;

    public Database() {
        try {
            Class.forName(drivername);
            conn = DriverManager.getConnection(connectionURL, username, password);
            stmt = conn.createStatement();
            System.out.println("Connected to PostgreSQL successfully!");
        } catch (Exception ex) {
            System.out.println("Connection Error: " + ex.getMessage());
        }
    }

    public void insertData(String vitri, String bienso, String loai, String mau, String gio) {
        String sSQL = "INSERT INTO server1 VALUES ('" + vitri + "','" + bienso + "','" + loai + "','" + mau + "','" + gio + "')";
        try {
            stmt.executeUpdate(sSQL);
        } catch (Exception e) {
            System.out.println("Insert Error: " + e.getMessage());
        }
    }

    public void delData(String id) {
        try {
            String sSQL = "DELETE FROM server1 WHERE vitri='" + id + "'";
            stmt.executeUpdate(sSQL);
        } catch (Exception e) {
            System.out.println("Delete Error: " + e.getMessage());
        }
    }

    public String getData() {
        String pos, num, type, clr, time, st = "";
        try {
            String sSQL = "SELECT * FROM server1";
            rs = stmt.executeQuery(sSQL);
            while (rs.next()) {
                pos = rs.getString("vitri");
                num = rs.getString("bienso");
                type = rs.getString("hieu");
                clr = rs.getString("mau");
                time = rs.getString("gio");
                st = st + pos + "|" + num + "|" + type + "|" + clr + "|" + time + "|";
            }
        } catch (Exception e) {
            System.out.println("Select Error: " + e.getMessage());
        }
        return st;
    }

    public boolean isEmpty(String id) {
        boolean check = true;
        try {
            String sSQL = "SELECT vitri FROM server1 WHERE vitri='" + id + "'";
            rs = stmt.executeQuery(sSQL);
            if (rs.next()) {
                check = false;
            }
        } catch (Exception e) {
            System.out.println("Check Error: " + e.getMessage());
        }
        return check;
    }

    public boolean querySQL(String vitri, String bienso, String hieu, String mau) {
        boolean check = true;
        try {
            String sSQL = "SELECT * FROM server1 WHERE vitri='" + vitri + "' "
                    + "AND bienso='" + bienso + "' "
                    + "AND hieu='" + hieu + "' "
                    + "AND mau='" + mau + "'";
            rs = stmt.executeQuery(sSQL);

            if (rs != null && rs.next()) {
                check = false;
            }
        } catch (Exception e) {
            System.out.println("Query Error: " + e.getMessage());
        }
        return check;
    }
}