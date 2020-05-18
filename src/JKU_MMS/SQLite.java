package JKU_MMS;

import JKU_MMS.Model.Profile;

import java.nio.file.Path;
import java.rmi.UnexpectedException;
import java.sql.*;

public class SQLite {

    private final static String DB_PATH = "./data/profiles.db";
    private final static String DB_URL = "jdbc:sqlite:" + DB_PATH;

    /**
     * builds a Connection to the SQLite Database
     * @return a Connection object
     * @throws UnexpectedException if no Connection could be established
     */

    public static Connection getConnection() throws UnexpectedException {
        try {
            Class.forName("org.sqlite.JDBC");
            Connection conn = DriverManager.getConnection(DB_URL);
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

    public static void addSampleProfile() throws SQLException {
        //TODO query to see if profile with name has been added already
//        Profile profile = new Profile("Sample");
//        profile.setOutputPath(Path.of("C:\\Users\\Thomas\\IntellijProjects\\JKU_mms_project\\output"));
//        profile.setFormat("mp4");
//
//        savePremadeProfile(profile);
    }

    /**
     * Wrapper method for saving premade profiles
     * @param profile profile to save
     * @return true if saving was successful
     * @throws SQLException if Database query was unsuccessful
     */
    public static boolean saveCustomProfile(Profile profile) throws SQLException {
        return saveProfile(profile, true);
    }

    /**
     * Wrapper method for saving custom profiles
     * @param profile profile to save
     * @return true if saving was successful
     * @throws SQLException if Database query was unsuccessful
     */
    public static boolean savePremadeProfile(Profile profile) throws SQLException {
        return saveProfile(profile, false);
    }

    /**
     * method to save profiles to database
     * @param profile profile to save
     * @param custom  true if it is a custom profile
     * @return true if saving was successful
     * @throws SQLException if Database query was unsuccessful
     */
    private static boolean saveProfile(Profile profile, boolean custom) throws SQLException {

        Connection conn = Main.conn;

        String sql = "INSERT INTO profiles (Name,AudioCodec,AudioSampleRate,AudioBitRate,VideoCodec," +
                "VideoFrameRate,VideoWidth,VideoHeight,Format,OutputPath,RemoveSubtitles,RemoveAudio,Custom)" +
                " VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        PreparedStatement statement = conn.prepareStatement(sql);
        statement.setString(1, profile.getName());
        statement.setString(2, profile.getAudioCodec());
        statement.setInt(3, profile.getAudioSampleRate());
        statement.setInt(4, profile.getAudioBitRate());
        statement.setString(5, profile.getVideoCodec());
        statement.setDouble(6, profile.getVideoFrameRate());
        statement.setInt(7, profile.getVideoWidth());
        statement.setInt(8, profile.getVideoHeight());
        statement.setString(9, profile.getFormat());
        statement.setString(10, profile.getOutputPath().toString());
        statement.setInt(11, profile.removeSubtitles() ? 1 : 0);
        statement.setInt(12, profile.removeAudio() ? 1 : 0);
        statement.setInt(13, custom ? 1 : 0);

        int rowsInserted = statement.executeUpdate();
        if (rowsInserted > 0) {
            System.out.println("A new profile was inserted successfully!");
        }
        return true;
    }

    public String[] getProfileList() {
        //TODO implement query to look up all profile names;
        return null;
    }

    public Profile getProfile(String name) {
        //TODO implement query to return Profile from DB with specified name
        return null;
    }

    public Profile deleteProfile(String name) {
        //TODO implement query to delete Profile from DB with specified name
        return null;
    }
}