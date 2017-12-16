package backend;

import java.io.*;
import java.util.ArrayList;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TagManager {

    private ArrayList<String> tags;
    private static final String FILE_NAME = "tags.txt";
    private static final Path PATH = Paths.get(FILE_NAME);
    private static final Pattern invalidPattern = Pattern.compile("[^a-zA-Z0-9\\s]");

    public TagManager() throws IOException {
        this(getTagsFileBufferedReader());
    }

    public TagManager(BufferedReader reader) throws IOException {
        tags = new ArrayList<String>();

        String line = reader.readLine();
        while(line != null) {
            tags.add(line);
            line = reader.readLine();
        }

        reader.close();
    }

    /**
     * If tag does not exist, add it to the list, update the file, and return true
     * Otherwise, do nothing and return false.
     *
     * @param  tag the tag to be added
     * @return     true iff tag is not already in the list
     *
     */
    public boolean addTag(String tag) throws IOException {
        FileWriter writer = new FileWriter(PATH.toFile(),true);
        boolean result = this.addTag(tag, writer);
        writer.close();
        return result;
    }

    /**
     * If tag does not exist, add it to the list, update the file, and return true
     * Otherwise, do nothing and return false.
     *
     * @param  tag    the tag to be added
     * @param  writer the writer to write the tag with
     * @return        true iff tag is not already in the list
     *
     */
    public boolean addTag(String tag, Writer writer) throws IOException {

        if(tag == null || tags.contains(tag)){
            return false;
        }

        Matcher matcher = invalidPattern.matcher(tag);
        if (matcher.find() || tag.equals("")) {
            throw new InvalidTagException("Invalid tag format");
        }
        tags.add(tag);
        writer.append(tag).append("\n");
        return true;
    }

    /**
     * Return whether or not tag is in the list
     *
     * @param  tag the tag to search for
     * @return     true iff tag is in tags list
     */
    public boolean contains(String tag){
        return tags.contains(tag);
    }

    /**
     * Return a copy of the tags list.
     *
     * @return list of tags
     */
    public ArrayList<String> getTags(){
        return (ArrayList<String>)tags.clone();
    }


    /**
     * Returns a buffered reader of the tags file.
     *
     * @return a buffered reader of the tags file
     * @throws IOException if we are unable read the tags file
     */
    private static BufferedReader getTagsFileBufferedReader() throws IOException {
        File tagFile = PATH.toFile();
        if(!tagFile.isFile() && !tagFile.createNewFile())
            throw new IOException("Could not create: " + tagFile.getAbsolutePath());

        return Files.newBufferedReader(PATH);
    }

}