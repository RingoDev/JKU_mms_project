package JKU_MMS.Database;

import JKU_MMS.Model.Profile;
import JKU_MMS.Model.Settings.Codec;
import JKU_MMS.Model.Settings.Format;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.*;

public class SQLite {

    private final static String DB_PATH = "./data/data.db";
    private final static String DB_URL = "jdbc:sqlite:" + DB_PATH;
    private static final Comparator<Codec> codecComparator = (c1, c2) -> c1.getImportance() == c2.getImportance() ? c1.getCodecName().compareTo(c2.getCodecName()) : c2.getImportance() - c1.getImportance();
    private static final Comparator<Format> formatComparator = (f1, f2) -> f1.getImportance() == f2.getImportance() ? f1.getFormatName().compareTo(f2.getFormatName()) : f2.getImportance() - f1.getImportance();
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
        profile.setVideoHeight(720);
        profile.setVideoWidth(1280);
        profile.setVideoFrameRate(30);
        profile.setAudioBitRate(196608);
        profile.setVideoCodec(getVideoCodecs().stream().filter(c -> c.getCodecName().equals("hvec")).findFirst().orElseGet(() -> new Codec("hvec")));
        profile.setRemoveAudio(false);
        profile.setRemoveSubtitles(false);
        profile.setAudioSampleRate(48000);
        profile.setAudioCodec(getAudioCodecs().stream().filter(c -> c.getCodecName().equals("mp3")).findFirst().orElseGet(() -> new Codec("mp3")));
        profile.setFormat(getFormats().stream().filter(f -> f.getFormatName().equals("mp4")).findFirst().orElseGet(() -> new Format("mp4")));

        return addPremadeProfile(profile);
    }

    /**
     * Wrapper method for saving custom profiles
     *
     * @param profile profile to save
     * @return true if saving was successful
     * @throws SQLException if a Database error occurs or if the connection is closed
     */
    public static boolean addProfile(Profile profile) throws SQLException {
        return saveProfile(profile, true);
    }

    /**
     * Wrapper method for saving premade profiles
     *
     * @param profile profile to save
     * @return true if saving was successful
     * @throws SQLException if a Database error occurs or if the connection is closed
     */
    public static boolean addPremadeProfile(Profile profile) throws SQLException {
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
        if (profileExists(profile.getName())) return false;

        String sql = "INSERT INTO profiles (Name,AudioCodec,AudioSampleRate,AudioBitRate,VideoCodec," +
                "VideoFrameRate,VideoWidth,VideoHeight,Format,OutputPath,RemoveSubtitles,RemoveAudio,Custom)" +
                " VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        PreparedStatement statement = conn.prepareStatement(sql);
        statement.setString(1, profile.getName());
        statement.setString(2, profile.getAudioCodec().getCodecName());
        statement.setInt(3, profile.getAudioSampleRate());
        statement.setInt(4, profile.getAudioBitRate());
        statement.setString(5, profile.getVideoCodec().getCodecName());
        statement.setDouble(6, profile.getVideoFrameRate());
        statement.setInt(7, profile.getVideoWidth());
        statement.setInt(8, profile.getVideoHeight());
        statement.setString(9, profile.getFormat().getFormatName());
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

    private static Codec extractCodec(ResultSet result) throws SQLException {
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

    private static Format extractFormat(ResultSet result) throws SQLException {
        Format format = new Format(result.getString(1));
        format.setDescription(result.getString(2));
        format.setMuxing(result.getInt(3) != 0);
        format.setDemuxing(result.getInt(4) != 0);
        return format;
    }

    /**
     * Turns a single query result into a Profile.
     *
     * @param result the query result
     * @return the created Profile
     * @throws SQLException if a Database error occurs or if the connection is closed
     */
    private static Profile extractProfile(ResultSet result) throws SQLException, NoSuchFieldException {

        Profile profile = new Profile(result.getString(1));

        profile.setAudioCodec(getCodec(result.getString(2)));
        profile.setAudioSampleRate(result.getInt(3));
        profile.setAudioBitRate(result.getInt(4));
        profile.setVideoCodec(getCodec(result.getString(5)));
        profile.setVideoFrameRate(result.getDouble(6));
        profile.setVideoWidth(result.getInt(7));
        profile.setVideoHeight(result.getInt(8));
        profile.setFormat(getFormat(result.getString(9)));
        profile.setOutputPath(Path.of(result.getString(10)));
        profile.setRemoveSubtitles(result.getInt(11) == 1);
        profile.setRemoveAudio(result.getInt(12) == 1);
        profile.setCustom(result.getInt(13) == 1);
        return profile;
    }

    private static Format getFormat(String name) throws SQLException, NoSuchFieldException {
        if (name.equals("copy")) return new Format("copy");
        if (name.equals("auto")) return new Format("auto");

        String sql = "SELECT * FROM AvailableFormats WHERE Format=?";

        PreparedStatement statement = conn.prepareStatement(sql);
        statement.setString(1, name);
        ResultSet result = statement.executeQuery();
        if (result.next()) return extractFormat(result);
        else throw new NoSuchFieldException("There is no Format with the name: " + name);

    }

    private static Codec getCodec(String name) throws SQLException, NoSuchFieldException {
        if (name.equals("copy")) return new Codec("copy");
        if (name.equals("auto")) return new Codec("auto");

        String sql = "SELECT * FROM AvailableCodecs WHERE CodecName=?";

        PreparedStatement statement = conn.prepareStatement(sql);
        statement.setString(1, name);
        ResultSet result = statement.executeQuery();
        if (result.next()) return extractCodec(result);
        else throw new NoSuchFieldException("There is no Codec with the name: " + name);

    }

    /**
     * Returns all Profiles in the Database.
     *
     * @return a Hashmap containing all Profiles in the Database sorted into custom and premade and then sorted by name
     * @throws SQLException if a Database error occurs or if the connection is closed
     */
    public static Map<String, Profile> getAllProfiles() throws SQLException, NoSuchFieldException {
        //sorts by Type(custom/premade) and then by Name
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

        String sql = "SELECT * FROM Profiles WHERE Name=?";

        PreparedStatement statement = conn.prepareStatement(sql);
        statement.setString(1, name);
        ResultSet result = statement.executeQuery();
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
    public static boolean profileExists(String name) throws SQLException {
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
     * Queries the Database for all VideoCodecs and returns the VideoCodecs alphabetically sorted.
     * The DummyCodecs "copy" and "auto" are also included.
     *
     * @return a {@link TreeSet} with the Available VideoCodecs
     * @throws SQLException if a Database error occurs or if the connection is closed
     */
    public static SortedSet<Codec> getVideoCodecs() throws SQLException {
        SortedSet<Codec> set = new TreeSet<>(codecComparator);
        String sql = "SELECT * FROM AvailableCodecs WHERE CodecType=0";
        Statement statement = conn.createStatement();
        ResultSet result = statement.executeQuery(sql);
        while (result.next()) {

            Codec codec = extractCodec(result);
            set.add(codec);
        }
        set.add(new Codec("copy", 9));
        set.add(new Codec("auto", 10));
        return set;
    }

    /**
     * Queries the Database for all AudioCodecs and returns the AudioCodecs alphabetically sorted.
     *
     * @return a {@link TreeSet} with the Available AudioCodecs
     * @throws SQLException if a Database error occurs or if the connection is closed
     */
    public static SortedSet<Codec> getAudioCodecs() throws SQLException {
        SortedSet<Codec> set = new TreeSet<>(codecComparator);
        String sql = "SELECT * FROM AvailableCodecs WHERE CodecType=1";
        Statement statement = conn.createStatement();
        ResultSet result = statement.executeQuery(sql);
        while (result.next()) {

            Codec codec = extractCodec(result);
            set.add(codec);
        }
        set.add(new Codec("copy", 9));
        set.add(new Codec("auto", 10));
        return set;
    }

    /**
     * Queries the Database for all Formats and returns the Formats alphabetically sorted.
     *
     * @return a {@link TreeSet} with the Available Formats Descriptions
     * @throws SQLException if a Database error occurs or if the connection is closed
     */
    public static SortedSet<Format> getFormats() throws SQLException {
        SortedSet<Format> set = new TreeSet<>(formatComparator);
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
        set.add(new Format("copy", 9));
        set.add(new Format("auto", 10));
        return set;
    }

    /**
     * deletes all codecs in Database
     *
     * @throws SQLException if a Database error occurs or if the connection is closed
     */
    private static void deleteCodecs() throws SQLException {
        String sql = "DELETE FROM AvailableCodecs";

        Statement statement = conn.createStatement();

        int rowsDeleted = statement.executeUpdate(sql);
        if (rowsDeleted > 0) {
            System.out.println(rowsDeleted + " codec" + (rowsDeleted != 1 ? "s were " : " was ") + "deleted successfully!");
        }
    }

    /**
     * was used to insert codec into Database from String
     *
     * @param codec the codec to insert
     * @throws SQLException if a Database error occurs or if the connection is closed
     */
    private static void add(String codec) throws SQLException {

        String sql = "INSERT INTO AvailableCodecs (CodecName,Description,Decoding,Encoding," +
                "CodecType,IntraCodec,LossyCompression,LosslessCompression)" +
                " VALUES ( ?, ?, ?, ?,?,?,?,?)";


        String flags = codec.substring(1, 7);
        System.out.println(flags);
        codec = codec.substring(8);
        String codecName = codec.replaceAll("(\\s\\s.+?$)", "");
        System.out.println(codecName);
        String description = codec.replaceAll("(^.*?\\s\\s+)", "");
        System.out.println(description);

        PreparedStatement statement = conn.prepareStatement(sql);
        statement.setString(1, codecName);
        statement.setString(2, description);
        statement.setInt(3, flags.charAt(0) == '.' ? 0 : 1);
        statement.setInt(4, flags.charAt(1) == '.' ? 0 : 1);
        statement.setInt(5, flags.charAt(2) == 'V' ? 0 : flags.charAt(2) == 'A' ? 1 : 2);
        statement.setInt(6, flags.charAt(3) == '.' ? 0 : 1);
        statement.setInt(7, flags.charAt(4) == '.' ? 0 : 1);
        statement.setInt(8, flags.charAt(5) == '.' ? 0 : 1);

        int rowsInserted = statement.executeUpdate();
        if (rowsInserted > 0) {
            System.out.println("A new format was inserted successfully!");
        }
    }


    /**
     * was used to read in codecs from file
     *
     * @throws IOException If the file cannot be read
     * @throws SQLException if a Database error occurs or if the connection is closed
     */
    public static void readFile() throws IOException, SQLException {
        BufferedReader reader = new BufferedReader(new FileReader(
                Paths.get("data/codecs.txt").toFile()));
        String line = reader.readLine();
        while (line != null) {
            add(line);
            line = reader.readLine();
        }
        reader.close();
    }
}