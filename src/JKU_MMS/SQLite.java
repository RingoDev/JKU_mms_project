package JKU_MMS;

import JKU_MMS.Model.Profile;


import java.nio.file.Path;
import java.rmi.UnexpectedException;
import java.sql.*;

public class SQLite {

    public static Connection getConnection() throws UnexpectedException {
        try {
            Class.forName("org.sqlite.JDBC");
            String dbURL = "jdbc:sqlite:profiles.db";
            Connection conn = DriverManager.getConnection(dbURL);
            if (conn != null) {
                System.out.println("Connected to the database");
                DatabaseMetaData dm = conn.getMetaData();
                System.out.println("Driver name: " + dm.getDriverName());
                System.out.println("Driver version: " + dm.getDriverVersion());
                System.out.println("Product name: " + dm.getDatabaseProductName());
                System.out.println("Product version: " + dm.getDatabaseProductVersion());
                return conn;
            }
        } catch (ClassNotFoundException | SQLException ex) {
            ex.printStackTrace();
        }
        throw new UnexpectedException("Couldn't get a Connection to the Database");
    }

    public void saveProfile(Profile profile) throws SQLException, UnexpectedException {

        Connection conn = getConnection();


        String sql = "INSERT INTO profiles (Name,AudioCodec,AudioSampleRate,AudioBitRate,VideoCodec," +
                "VideoFrameRate,VideoWidth,VideoHeight,Format,OutputPath,RemoveSubtitles,RemoveAudio)" +
                " VALUES (?,?,?,?,?,?,?,?,?, ?, ?, ?)";

        PreparedStatement statement = conn.prepareStatement(sql);
        statement.setString(1, profile.getName());
        statement.setString(2, profile.getAudioCodec());
        statement.setInt(3, profile.getAudioSampleRate());
        statement.setInt(4, profile.getAudioBitRate());
        statement.setString(4, profile.getVideoCodec());
        statement.setDouble(4, profile.getVideoFrameRate());
        statement.setInt(4, profile.getVideoWidth());
        statement.setInt(4, profile.getVideoHeight());
        statement.setString(4, profile.getFormat());
        statement.setString(4, profile.getOutputPath().toString());
        statement.setInt(4, profile.removeSubtitles()?1:0);
        statement.setInt(4, profile.removeAudio()?1:0);


        int rowsInserted = statement.executeUpdate();
        if (rowsInserted > 0) {
            System.out.println("A new profile was inserted successfully!");
        }
    }
}