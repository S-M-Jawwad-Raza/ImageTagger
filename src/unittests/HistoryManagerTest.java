package unittests;

import backend.HistoryManager;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class HistoryManagerTest {

    @Test
    void testImgLogChange() throws IOException{

        String old = "img.jpg";
        String present = "img @Aunt Samantha.jpg";
        String timeStamp = new SimpleDateFormat("mm.HH.dd.MM.yyyy").format(new java.util.Date());
        String log = timeStamp + "\u0000" + old + "\u0000"+ present+ "\n" ;
        StringWriter imgLogger = new StringWriter();
        HistoryManager.imgLogChange(imgLogger, old, present);
        assertEquals(log, imgLogger.toString());
    }

    @Test
    void testSupLogChange() throws IOException{
        String old = "img.jpg";
        String present = "img @Aunt Samantha.jpg";
        String timeStamp = new SimpleDateFormat("dd.MM.yyyy HH:mm").format(new java.util.Date());
        String log = timeStamp + " " + old + " -> " + present+ "\n";
        StringWriter supLogger = new StringWriter();
        HistoryManager.supLogChange(supLogger, old, present);

        assertEquals(log, supLogger.toString());

    }

    @Test
    void testImgLogMultChanges() throws IOException {
        String old = "img.jpg";
        String interim1 = "img @Jane.jpg";
        String interim2 = "img @ Aunt Samantha @Jane.jpg";
        String present = "img @Aunt Samantha.jpg";

        String timeStamp = new SimpleDateFormat("mm.HH.dd.MM.yyyy").format(new java.util.Date());
        String log1 = timeStamp + "\u0000" + old + "\u0000"+ interim1+ "\n" ;
        StringWriter imgLogger = new StringWriter();
        HistoryManager.imgLogChange(imgLogger, old, interim1);
        assertEquals(log1, imgLogger.toString());

        timeStamp = new SimpleDateFormat("mm.HH.dd.MM.yyyy").format(new java.util.Date());
        String log2 = timeStamp + "\u0000" + interim1 + "\u0000"+ interim2 + "\n" ;
        HistoryManager.imgLogChange(imgLogger, interim1, interim2);
        assertEquals((log1 + log2), imgLogger.toString());

        timeStamp = new SimpleDateFormat("mm.HH.dd.MM.yyyy").format(new java.util.Date());
        String log3 = timeStamp + "\u0000" + interim2 + "\u0000"+ present + "\n" ;
        HistoryManager.imgLogChange(imgLogger, interim2, present);
        assertEquals((log1 + log2 + log3), imgLogger.toString());
        imgLogger.close();
    }

    @Test
    void testSupLogMultChanges() throws IOException {
        String old = "img.jpg";
        String interim1 = "img @Jane.jpg";
        String interim2 = "img @ Aunt Samantha @Jane.jpg";
        String present = "img @Aunt Samantha.jpg";

        String timeStamp = new SimpleDateFormat("dd.MM.yyyy HH:mm").format(new java.util.Date());
        String log1 = timeStamp + " " + old + " -> " + interim1 + "\n";
        StringWriter supLogger = new StringWriter();
        HistoryManager.supLogChange(supLogger, old, interim1);
        assertEquals(log1, supLogger.toString());

        timeStamp = new SimpleDateFormat("dd.MM.yyyy HH:mm").format(new java.util.Date());
        String log2 = timeStamp + " " + interim1 + " -> " + interim2 + "\n";
        HistoryManager.supLogChange(supLogger, interim1, interim2);
        assertEquals((log1 + log2), supLogger.toString());

        timeStamp = new SimpleDateFormat("dd.MM.yyyy HH:mm").format(new java.util.Date());
        String log3 = timeStamp + " " + interim2 + " -> " + present + "\n";
        HistoryManager.supLogChange(supLogger, interim2, present);
        assertEquals((log1 + log2 + log3), supLogger.toString());
        supLogger.close();
    }
    @Test
    void testGetOldNamesSingleChange() throws IOException{
        String old = "img.jpg";
        String present = "img @Aunt Samantha.jpg";
        String timeStamp = new SimpleDateFormat("mm.HH.dd.MM.yyyy").format(new java.util.Date());
        String source = timeStamp + "\u0000" + old + "\u0000"+ present+ "\n" ;

        ArrayList<String> toCheck = new ArrayList<>();
        toCheck.add("img.jpg");

        BufferedReader reader = new BufferedReader(new StringReader(source));
        ArrayList<String> x = HistoryManager.getOldNames(reader);
        assertEquals(toCheck, x);
    }

    @Test
    void testGetOldNamesMultChanges() throws IOException{
        String old = "img.jpg";
        String interim1 = "img @Jane.jpg";
        String interim2 = "img @ Aunt Samantha @Jane.jpg";
        String present = "img @Aunt Samantha.jpg";

        String timeStamp = new SimpleDateFormat("mm.HH.dd.MM.yyyy").format(new java.util.Date());
        String source = timeStamp + "\u0000" + old + "\u0000"+ interim1+ "\n" ;

        timeStamp = new SimpleDateFormat("mm.HH.dd.MM.yyyy").format(new java.util.Date());
        source = source + timeStamp + "\u0000" + interim1 + "\u0000"+ interim2+ "\n" ;

        timeStamp = new SimpleDateFormat("mm.HH.dd.MM.yyyy").format(new java.util.Date());
        source = source + timeStamp + "\u0000" + interim2 + "\u0000"+ present + "\n" ;

        ArrayList<String> toCheck = new ArrayList<>();
        toCheck.add("img.jpg");
        toCheck.add("img @Jane.jpg");
        toCheck.add("img @ Aunt Samantha @Jane.jpg");

        BufferedReader reader = new BufferedReader(new StringReader(source));
        ArrayList<String> actual = HistoryManager.getOldNames(reader);
        assertEquals(toCheck, actual);
        reader.close();
    }

    @Test
    void testReadSupLogSingleLine() throws IOException{
        String old = "img.jpg";
        String present = "img @Aunt Samantha.jpg";
        String timeStamp = new SimpleDateFormat("dd.MM.yyyy HH:mm").format(new java.util.Date());
        String source = timeStamp + " " + old + " -> " + present + "\n";
        String toCheck = timeStamp + " " + old + " -> " + present + "\n";

        BufferedReader reader = new BufferedReader(new StringReader(source));

        String actual = HistoryManager.readSupLog(reader);
        assertEquals(toCheck, actual);
    }

    @Test
    void testReadSupLogMultipleLines() throws IOException{
        String old = "img.jpg";
        String interim1 = "img @Jane.jpg";
        String interim2 = "img @ Aunt Samantha @Jane.jpg";
        String present = "img @Aunt Samantha.jpg";

        String timeStamp = new SimpleDateFormat("dd.MM.yyyy HH:mm").format(new java.util.Date());
        String source = timeStamp + " " + old + " -> " + interim1 + "\n";

        timeStamp = new SimpleDateFormat("dd.MM.yyyy HH:mm").format(new java.util.Date());
        source = source + timeStamp + " " + interim1 + " -> " + interim2 + "\n";

        timeStamp = new SimpleDateFormat("dd.MM.yyyy HH:mm").format(new java.util.Date());
        source = source + timeStamp + " " + interim2 + " -> " + present + "\n";

        BufferedReader reader = new BufferedReader(new StringReader(source));

        String actual = HistoryManager.readSupLog(reader);
        assertEquals(source, actual);

    }



}
