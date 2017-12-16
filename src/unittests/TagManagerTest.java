package unittests;

import backend.InvalidTagException;
import backend.TagManager;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


import java.io.*;
import java.util.ArrayList;


public class TagManagerTest {

    @Test
    public void testInitialTags() throws IOException{


        StringWriter w = new StringWriter();
        w.append("tag1\n");
        w.append("tag2\n");
        w.append("tag3\n");

        ArrayList<String> expected = new ArrayList<String>();
        expected.add("tag1");
        expected.add("tag2");
        expected.add("tag3");

        BufferedReader r = new BufferedReader(new StringReader(w.toString()));
        TagManager t = new TagManager(r);

        assertEquals(expected,t.getTags());

    }

    @Test
    public void testAddSingleTag() throws IOException{


        StringWriter w = new StringWriter();
        BufferedReader r = new BufferedReader(new StringReader(w.toString()));
        TagManager t = new TagManager(r);


        assertTrue(t.addTag("t1", w));

        BufferedReader r2 = new BufferedReader(new StringReader(w.toString()));

        assertEquals("t1",r2.readLine());
        assertEquals(null, r2.readLine());
        r2.close();

    }

    @Test
    public void testAddMultipleTags() throws IOException{

        StringWriter w = new StringWriter();
        BufferedReader r = new BufferedReader(new StringReader(w.toString()));
        TagManager t = new TagManager(r);

        assertTrue(t.addTag("t1", w));
        assertTrue(t.addTag("t2", w));
        assertTrue(t.addTag("t3", w));

        ArrayList<String> tags = new ArrayList<String>();
        tags.add("t1");
        tags.add("t2");
        tags.add("t3");

        BufferedReader r2 = new BufferedReader(new StringReader(w.toString()));
        String line = r2.readLine();
        int tagNum = 0;
        while(line != null){
            assertEquals(tags.get(tagNum),line);
            line = r2.readLine();
            tagNum ++;
        }
        assertEquals(null, r2.readLine());
        r2.close();

    }

    @Test
    public void testAddDuplicateTag() throws IOException{

        StringWriter w = new StringWriter();
        BufferedReader r = new BufferedReader(new StringReader(w.toString()));
        TagManager t = new TagManager(r);


        assertTrue(t.addTag("t1",w));
        assertFalse(t.addTag("t1", w));

        BufferedReader r2 = new BufferedReader(new StringReader(w.toString()));
        assertEquals("t1",r2.readLine());
        assertEquals(null,r2.readLine());
        r2.close();

    }

    @Test
    public void testAddInvalidTag() throws IOException{

        StringWriter w = new StringWriter();
        BufferedReader r = new BufferedReader(new StringReader(w.toString()));
        TagManager t = new TagManager(r);

        assertThrows(InvalidTagException.class,()->{t.addTag("$$$", w);});

    }

    @Test
    public void testContains() throws IOException{

        StringWriter w = new StringWriter();
        BufferedReader r = new BufferedReader(new StringReader(w.toString()));
        TagManager t = new TagManager(r);

        assertTrue(t.addTag("t1", w));

        assertTrue(t.contains("t1"));

    }

    @Test
    public void testContainsFalse() throws IOException{

        StringWriter w = new StringWriter();
        BufferedReader r = new BufferedReader(new StringReader(w.toString()));
        TagManager t = new TagManager(r);

        assertTrue(t.addTag("t1", w));

        assertFalse(t.contains("t2"));

    }

    @Test
    public void testGetTags() throws IOException{

        StringWriter w = new StringWriter();
        BufferedReader r = new BufferedReader(new StringReader(w.toString()));
        TagManager t = new TagManager(r);

        assertTrue(t.addTag("t1", w));
        assertTrue(t.addTag("t2", w));
        assertTrue(t.addTag("t3", w));

        ArrayList<String> expected = new ArrayList<String>();
        expected.add("t1");
        expected.add("t2");
        expected.add("t3");

        assertEquals(expected,t.getTags());

    }
}
