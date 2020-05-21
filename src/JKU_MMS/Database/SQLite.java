package JKU_MMS.Database;

import JKU_MMS.Model.Profile;
import JKU_MMS.Settings.Codec;
import JKU_MMS.Settings.Format;
import net.bramp.ffmpeg.probe.FFmpegStream;
import org.apache.tools.ant.types.resources.Sort;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.text.Normalizer;
import java.util.*;

public class SQLite {

    private final static String DB_PATH = "./data/data.db";
    private final static String DB_URL = "jdbc:sqlite:" + DB_PATH;
    private static Connection conn;

    /**
     * builds a Connection to the SQLite Database
     *
     * @throws ConnectionFailedException if no Connection could be established
     */

    public static void openConnection() throws ConnectionFailedException {
        try {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection(DB_URL);
            if (conn != null) {
                System.out.println("Connected to the database");
                DatabaseMetaData dm = conn.getMetaData();
                System.out.println("Driver name: " + dm.getDriverName());
                System.out.println("Driver version: " + dm.getDriverVersion());
                System.out.println("Product name: " + dm.getDatabaseProductName());
                System.out.println("Product version: " + dm.getDatabaseProductVersion());
                return;
            }
        } catch (ClassNotFoundException | SQLException ex) {
            ex.printStackTrace();
        }
        throw new ConnectionFailedException("Couldn't get a Connection to the Database");
    }

    /**
     * closes the connection to the DB
     *
     * @throws SQLException if a DB access error occurs
     */
    public static void closeConnection() throws SQLException {
        conn.close();
    }

    public static boolean deleteSampleProfile() throws SQLException {
        return deleteProfile("Sample");
    }


    /**
     * adds a sample profile to the DB
     *
     * @throws SQLException if a Database error occurs or if the connection is closed
     */
    public static boolean addSampleProfile() throws SQLException {

        Profile profile = new Profile("Sample");
        profile.setOutputPath(Path.of("./output"));
        profile.setFormat("mp4");

        return savePremadeProfile(profile);
    }

    /**
     * Wrapper method for saving custom profiles
     *
     * @param profile profile to save
     * @return true if saving was successful
     * @throws SQLException if a Database error occurs or if the connection is closed
     */
    public static boolean saveCustomProfile(Profile profile) throws SQLException {
        return saveProfile(profile, true);
    }

    /**
     * Wrapper method for saving premade profiles
     *
     * @param profile profile to save
     * @return true if saving was successful
     * @throws SQLException if a Database error occurs or if the connection is closed
     */
    public static boolean savePremadeProfile(Profile profile) throws SQLException {
        return saveProfile(profile, false);
    }

    /**
     * Method to save profiles to database
     *
     * @param profile profile to save
     * @param custom  true if it is a custom profile
     * @return true if saving was successful
     * @throws SQLException if a Database error occurs or if the connection is closed
     */
    private static boolean saveProfile(Profile profile, boolean custom) throws SQLException {
        if (profileIsSaved(profile.getName())) return false;

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

    /**
     * Turns a single query result into a Profile.
     *
     * @param result the query result
     * @return the created Profile
     * @throws SQLException if a Database error occurs or if the connection is closed
     */
    private static Profile extractProfile(ResultSet result) throws SQLException {
        Profile profile = new Profile(result.getString(1));

        profile.setAudioCodec(result.getString(2));
        profile.setAudioSampleRate(result.getInt(3));
        profile.setAudioBitRate(result.getInt(4));
        profile.setVideoCodec(result.getString(5));
        profile.setVideoFrameRate(result.getDouble(6));
        profile.setVideoWidth(result.getInt(7));
        profile.setVideoHeight(result.getInt(8));
        profile.setFormat(result.getString(9));
        profile.setOutputPath(Path.of(result.getString(10)));
        profile.setRemoveSubtitles(result.getInt(11) == 1);
        profile.setRemoveAudio(result.getInt(12) == 1);
        profile.setCustom(result.getInt(13) == 1);
        return profile;
    }

    /**
     * Returns all Profiles in the Database.
     *
     * @return a Hashmap containing all Profiles in the Database sorted into custom and premade and then sorted by name
     * @throws SQLException if a Database error occurs or if the connection is closed
     */
    public static Map<String, Profile> getAllProfiles() throws SQLException {
        //sorts by Type(custom/premade) and then by Name
        //TODO test comparator
        Map<String, Profile> map = new HashMap<>();

        String sql = "SELECT * FROM Profiles";

        Statement statement = conn.createStatement();
        ResultSet result = statement.executeQuery(sql);

        while (result.next()) {

            Profile profile = extractProfile(result);

            map.put(profile.getName(), profile);
        }
        return map;
    }

    /**
     * Looks up a Profile by name and returns it if it exists.
     *
     * @param name the name of the Profile
     * @return the Profile if it exists
     * @throws SQLException         if a Database error occurs or if the connection is closed
     * @throws NoSuchFieldException if the Profile doesn't exist
     */
    public static Profile getProfile(String name) throws SQLException, NoSuchFieldException {
        //TODO testing

        String sql = "SELECT * FROM Profiles WHERE Name=?";

        PreparedStatement statement = conn.prepareStatement(sql);
        statement.setString(1, name);
        ResultSet result = statement.executeQuery(sql);
        if (result.next()) return extractProfile(result);
        else throw new NoSuchFieldException("There is no Profile with the name: " + name);

    }

    /**
     * Deletes a Profile.
     *
     * @param name the name of the Profile
     * @return true if 1 or more Profiles were deleted
     * @throws SQLException if a Database error occurs or if the connection is closed
     */
    public static boolean deleteProfile(String name) throws SQLException {

        String sql = "DELETE FROM Profiles WHERE Name=?";

        PreparedStatement statement = conn.prepareStatement(sql);
        statement.setString(1, name);

        int rowsDeleted = statement.executeUpdate();
        if (rowsDeleted > 0) {
            System.out.println(rowsDeleted + " profile" + (rowsDeleted != 1 ? "s were " : " was ") + "deleted successfully!");
            return true;
        }
        return false;
    }

    /**
     * Looks up the DB to see if Profile exists.
     *
     * @param name the Name of the Profile to look up.
     * @return true if profile with this name exists in Database
     * @throws SQLException if a Database error occurs or if the connection is closed
     */
    public static boolean profileIsSaved(String name) throws SQLException {
        String sql = "SELECT * FROM Profiles WHERE Name=?";
        PreparedStatement statement = conn.prepareStatement(sql);
        statement.setString(1, name);
        ResultSet result = statement.executeQuery();
        return result.next();
    }

    public static void test() throws SQLException {
        System.out.println("Starting Database testrun");
        if (addSampleProfile()) System.out.println("added Sample Profile");
        else System.out.println("Didnt add Sample Profile ... maybe it already exists?");
        if (deleteSampleProfile()) System.out.println("deleted Sample Profile");
        else System.out.println("Didnt delete Sample Profile ... something went wrong here!");
    }

    /**
     * Looks up all the Profiles in the Database and returns all the Profile names in a SortedSet.
     *
     * @return a TreeSet with the Profile names in alphabetical order.
     * @throws SQLException if a Database error occurs or if the connection is closed
     */
    public static SortedSet<String> getProfileNames() throws SQLException {

        SortedSet<String> set = new TreeSet<>();
        String sql = "SELECT Name FROM Profiles";

        Statement statement = conn.createStatement();
        ResultSet result = statement.executeQuery(sql);

        while (result.next()) {
            set.add(result.getString("Name"));
        }
        return set;
    }

    /**
     * Queries the Database for all VideoCodecs and returns the VideoCodecs alphabetically sorted.
     *
     * @return a TreeSet with the Available VideoCodecs
     * @throws SQLException if a Database error occurs or if the connection is closed
     */
    public static SortedSet<Codec> getVideoCodecs() throws SQLException {
        SortedSet<Codec> set = new TreeSet<>(Comparator.comparing(Codec::getCodecName));
        String sql = "SELECT * FROM AvailableCodecs WHERE CodecType=0";
        Statement statement = conn.createStatement();
        ResultSet result = statement.executeQuery(sql);
        while (result.next()) {

            Codec codec = convert(result);
            set.add(codec);
        }
        return set;
    }

    /**
     * Queries the Database for all AudioCodecs and returns the AudioCodecs alphabetically sorted.
     *
     * @return a TreeSet with the Available AudioCodecs
     * @throws SQLException if a Database error occurs or if the connection is closed
     */
    public static SortedSet<Codec> getAudioCodecs() throws SQLException {
        SortedSet<Codec> set = new TreeSet<>(Comparator.comparing(Codec::getCodecName));
        String sql = "SELECT * FROM AvailableCodecs WHERE CodecType=1";
        Statement statement = conn.createStatement();
        ResultSet result = statement.executeQuery(sql);
        while (result.next()) {

            Codec codec = convert(result);
            set.add(codec);
        }
        return set;
    }

    /**
     * Queries the Database for all Formats and returns the Formats alphabetically sorted.
     *
     * @return a TreeSet with the Available Formats Descriptions
     * @throws SQLException if a Database error occurs or if the connection is closed
     */
    public static SortedSet<Format> getFormats() throws SQLException {
        SortedSet<Format> set = new TreeSet<>(Comparator.comparing(Format::getFormatName));
        String sql = "SELECT * FROM AvailableFormats";
        Statement statement = conn.createStatement();
        ResultSet result = statement.executeQuery(sql);
        while (result.next()) {
            Format format = new Format(result.getString("Format"));
            format.setDescription(result.getString("Description"));
            format.setDemuxing(result.getInt("Demuxing") != 0);
            format.setMuxing(result.getInt("Muxing") != 0);
            set.add(format);
        }
        return set;
    }

    /**
     * deletes all codecs in Database
     *
     * @throws SQLException if a Database error occurs or if the connection is closed
     */
    public static void deleteCodecs() throws SQLException {
        String sql = "DELETE FROM AvailableCodecs";

        Statement statement = conn.createStatement();

        int rowsDeleted = statement.executeUpdate(sql);
        if (rowsDeleted > 0) {
            System.out.println(rowsDeleted + " codec" + (rowsDeleted != 1 ? "s were " : " was ") + "deleted successfully!");
        }
    }

    /**
     * was used to inser codec into Database from String
     *
     * @param codec
     * @throws SQLException if a Database error occurs or if the connection is closed
     */
    public static void add(String codec) throws SQLException {

        String sql = "INSERT INTO AvailableFormats (Format,Description,Demuxing,Muxing)" +
                " VALUES ( ?, ?, ?, ?)";

        String flags = codec.substring(1, 3);
        codec = codec.substring(4);
        String codecName = codec.replaceAll("(\\s\\s.+?$)", "");
        String description = codec.replaceAll("(^.*?\\s\\s+)", "");

        PreparedStatement statement = conn.prepareStatement(sql);
        statement.setString(1, codecName);
        statement.setString(2, description);
        statement.setInt(3, codec.charAt(1) == ' ' ? 0 : 1);
        statement.setInt(4, codec.charAt(2) == ' ' ? 0 : 1);

        int rowsInserted = statement.executeUpdate();
        if (rowsInserted > 0) {
            System.out.println("A new format was inserted successfully!");
        }
    }


    /**
     * was used to read in codecs from file
     *
     * @throws IOException
     * @throws SQLException if a Database error occurs or if the connection is closed
     */
    public static void readFile() throws IOException, SQLException {
        BufferedReader reader = new BufferedReader(new FileReader(
                Paths.get("data/formats.txt").toFile()));
        String line = reader.readLine();
        while (line != null) {
            add(line);
            line = reader.readLine();
        }
        reader.close();
    }

    public static Codec convert(ResultSet result) throws SQLException {
        Codec codec = new Codec(result.getString("CodecName"));
        codec.setDescription(result.getString("Description"));
        codec.setDecoding(result.getInt("Decoding") != 0);
        codec.setEncoding(result.getInt("Encoding") != 0);
        codec.setCodecType(result.getInt("CodecType") == 0 ? Codec.CodecType.VIDEO : Codec.CodecType.AUDIO);
        codec.setIntraCodec(result.getInt("IntraCodec") != 0);
        codec.setLossyCompression(result.getInt("LossyCompression") != 0);
        codec.setLosslessCompression(result.getInt("LosslessCompression") != 0);
        return codec;
    }
}