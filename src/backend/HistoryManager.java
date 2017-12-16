package backend;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.util.ArrayList;
import java.text.SimpleDateFormat;

/** Responsible for logging changes on file names to superLog and individual log files for images. */
public class HistoryManager {

    private static Path supPath = Paths.get("superlog.log");

    /**
     * Logs name change from oldName to newName in file located at Path as well as in the superlog file.
     *
     * @param filePath Path to log file
     * @param oldName Old name of Image file
     * @param newName New name of Image file
     * @throws IOException if file is not found or another IO exception occurs
     */

    static void logChange(Path filePath, Path oldName, Path newName) throws IOException {
        BufferedWriter imgLog = new BufferedWriter(new FileWriter( filePath.toFile() , true));
        //the above code was obtained from https://stackoverflow.com/questions/1237235/check-file-exists-java
        imgLogChange(imgLog, oldName.getFileName().toString(), newName.getFileName().toString());
        //The above logs the name change in the image log file
        BufferedWriter supLog = new BufferedWriter(new FileWriter( supPath.toFile() , true));
        supLogChange(supLog, oldName.toAbsolutePath().toString(), newName.toAbsolutePath().toString());
        //the above logs the name change in the super log file
    }

    /**
     * Writes name change entry to supWriter
     *
     * @param supWriter Writer for file to write to
     * @param abOldName Absolute old name of img (including absolute filepath)
     * @param abNewName Absolute new name of img (including absolute filepath)
     * @throws IOException if IOException occurs
     */
    public static void supLogChange(Writer supWriter , String abOldName, String abNewName) throws IOException {
        String timeStamp = new SimpleDateFormat("dd.MM.yyyy HH:mm").format(new java.util.Date());
        String supToWrite = timeStamp + " " + abOldName + " -> " + abNewName + "\n";
        supWriter.append(supToWrite);
        supWriter.close();
    }

    /**
     * Writes name change entry to logWriter
     *
     * @param imgWriter Writer for file to write to
     * @param oldName old name of img (including relative filepath)
     * @param newName new name of img (including relative filepath)
     * @throws IOException if IOException occurs
     */
    public static void imgLogChange(Writer imgWriter, String oldName, String newName) throws IOException {
        String timeStamp = new SimpleDateFormat("mm.HH.dd.MM.yyyy").format(new java.util.Date());
        String toWrite = timeStamp + "\u0000" + oldName + "\u0000"+ newName + "\n" ;
        imgWriter.append(toWrite);
        imgWriter.close();
    }

    /**
     * Returns an ArrayList of old names for image file associated with the given log file.
     *
     * @param filePath Path for log file with old names
     * @return ArrayList of old names
     */
    static ArrayList<String> getOldNames(Path filePath) throws IOException
    {
        try (BufferedReader reader = Files.newBufferedReader(filePath))
        {
            return getOldNames(reader);
        }
    }

    /**
     * Returns an ArrayList of old names found in reader
     *
     * @param reader reader to read from
     * @return ArrayList containing oldnames
     * @throws IOException if IOException occurs
     */
    public static ArrayList<String> getOldNames(BufferedReader reader) throws IOException
    {
        ArrayList<String> names = new ArrayList<>();
        String logLine = reader.readLine();

        while(logLine != null) {
            String[] oldName = logLine.split("\u0000");
            if (!names.contains(oldName[1]))
            {
                names.add(oldName[1]);
            }
            logLine = reader.readLine();
        }

        reader.close();
        return names;
    }

    /**
     * Return a String version of Superlog.txt
     *
     * @return SuperLog.txt in String format
     * @throws IOException if IOException occurs
     */
    public static String readSupLog() throws IOException{
        try (BufferedReader reader = Files.newBufferedReader(supPath))
        {
            return readSupLog(reader);
        }
    }

    /**
     * Returns a String representation of text in the reader
     *
     * @param reader reader to read from
     * @return String representation of all text in reader
     * @throws IOException if IOException occurs
     */
    public static String readSupLog(BufferedReader reader) throws IOException{
        StringBuilder longLog = new StringBuilder("");
        String line = reader.readLine();
        while(line != null){
            longLog.append(line);
            longLog.append("\n");
            line = reader.readLine();
        }
        reader.close();
        return longLog.toString();
    }
}
