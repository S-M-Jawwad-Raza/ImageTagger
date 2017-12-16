package backend;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** The backend class used to tag images. */
public class ImageTagger {
    /**
     * An instance of backend.TagManager to keep create new tags and retrieve existing ones.
     */
    private TagManager tagManager;

    /**
     * Constructor for backend.ImageTagger. @throws IOException
     */
    public ImageTagger() throws IOException {
        this.tagManager = new TagManager();
    }

    /**
     * Tags an image file f with tag.
     *
     * @param f   the image file to tag.
     * @param tags the list of tags to add to the image.
     * @throws IOException if tagManager or rename fails.
     */
    public void tagImage(ImageFile f, List<String> tags) throws IOException {
        if(tags.size() == 0)
            return; // no tags to tag image with...

        ArrayList<String> parsedImageName = parseName(f.getFileName());
        for(String tag: tags) {
            if (!tagManager.contains(tag)) // tag manager does not know of this tag, so ignore it.
                continue;

            int idx = parsedImageName.indexOf(tag);
            if (idx > 0 && idx < parsedImageName.size() - 1)
                continue; // already tagged with tag.

            parsedImageName.add(parsedImageName.size() - 1, tag);
        }
        this.renameImage(f, parsedImageName);
    }

    /**
     * Removes tag from imagefile f if it exists.
     *
     * @param f   the file to remove the tag from.
     * @param tags the list of tags to remove.
     * @throws IOException if tagManager or rename fails.
     */
    public void unTagImage(ImageFile f, List<String> tags) throws IOException {
        if(tags.size() == 0)
            return; // no tags to remove from image...
        
        ArrayList<String> parsedImageName = parseName(f.getFileName());
        for(String tag: tags) {
            int idx = parsedImageName.indexOf(tag);
            if (idx <= 0 || idx >= parsedImageName.size() - 1)
                continue; // nothing to delete...

            parsedImageName.remove(idx);
        }
        this.renameImage(f, parsedImageName);
    }

    /**
     * Rename the imagefile f with the given new name.
     *
     * @param f       the file to rename.
     * @param newName the name to rename to.
     * @throws IOException if rename fails.
     */
    public void renameImage(ImageFile f, String newName) throws IOException {
        this.renameImage(f, parseName(newName));
    }

    /**
     * Returns a list of tags the file has used prior to this.
     *
     * @param f the file in question.
     * @return a list of old tagsets the file has used.
     * @throws IOException if tagManager fails retrieving.
     */
    public ArrayList<String> getOldNames(ImageFile f) throws IOException {
        String historyFileName = parseName(f.getFileName()).get(0) + ".log";
        Path historyFilePath = f.getFilePath().resolveSibling(historyFileName);
        File historyFile = historyFilePath.toFile();
        if (!historyFile.isFile())
            return new ArrayList<String>();
        return HistoryManager.getOldNames(historyFilePath);
    }

    /**
     * Adds a new tag to the tagManager.
     *
     * @param tag the tag to add.
     * @return true if a new tag was added, false otherwise.
     * @throws IOException if tagManager is unable to add new tag.
     */
    public boolean createTag(String tag) throws IOException {
        return tagManager.addTag(tag);
    }

    /**
     * Iterates through the provided list and adds any unrecognized tag to the tagManager.
     *
     * @param tags the list of tags to add if they do not already exist.
     * @return true if all tags were valid, false if an invalid tag was encountered.
     * @throws IOException due to tagManager.add performing i/o on tag file.
     */
    public boolean createTagsIfNotExist(List<String> tags) throws IOException {
        boolean malformedTagFound = false;
        for (String tag: tags)
        {
            if(!tagManager.contains(tag)) {
                try {
                    tagManager.addTag(tag);
                } catch (InvalidTagException e) {
                    malformedTagFound = true;
                }
            }
        }

        return !malformedTagFound;
    }

    /**
     * @return a list of tags the user has ever created.
     */
    public ArrayList<String> getTags() {
        return tagManager.getTags();
    }

    /**
     * Returns the list of tags the image uses.
     *
     * @param f the file to get the tags from.
     * @return an arraylist with all the tags the image uses.
     */
    public static ArrayList<String> parseTags(ImageFile f) {
        ArrayList<String> tags = parseName(f.getFileName());
        tags.remove(tags.size() - 1);
        tags.remove(0);

        // if there are new tags the tag manager does not recognize

        return tags;
    }

    /**
     * Moves the given file to the given destination.
     *
     * @param f          the file to move.
     * @param destFolder the destination path.
     * @throws IOException if backend.HistoryManager is unable to log and/or the moving fails.
     */
    public void moveFile(ImageFile f, Path destFolder) throws IOException {
        Path src = f.getFilePath();
        Path dest = destFolder.resolve(f.getFileName());
        Files.move(src, dest);
        f.setFilePath(dest);

        String historyFileName = parseName(f.getFileName()).get(0) + ".log";
        Path historyFilePath = src.resolveSibling(historyFileName);
        if (Files.exists(historyFilePath))
            Files.move(historyFilePath, destFolder.resolve(historyFileName));
    }

    /**
     * Helper method for the public renameImage. It takes in a tokenized image name list and the file to rename.
     *
     * @param f             the file to rename.
     * @param newParsedName the tokenized image name.
     * @throws IOException if backend.HistoryManager or rename fails.
     */
    private void renameImage(ImageFile f, List<String> newParsedName) throws IOException {
        Path src = f.getFilePath();
        Path dest = src.resolveSibling(parsedNameToStr(newParsedName));
        Path imgHistoryPath = src.resolveSibling(newParsedName.get(0) + ".log");
        Files.move(src, dest);
        f.setFilePath(dest);
        HistoryManager.logChange(imgHistoryPath, src, dest);
    }

    /**
     * Constructs a file name from a list of tokens.
     *
     * @param parsedName the list to stringify.
     * @return A string constructed from the tokenized name list.
     */
    private String parsedNameToStr(List<String> parsedName) {
        StringBuilder str = new StringBuilder(parsedName.get(0));

        for (int i = 1; i < parsedName.size() - 1; i++)
            str.append(" @").append(parsedName.get(i));

        str.append(parsedName.get(parsedName.size() - 1));

        return str.toString();
    }

    /**
     * Takes a file name and breaks it down into a list of tokens.
     * The elements of the list are as follows:
     * 0) the file name
     * 1) first tag if it exists, else the extension
     * ...) the remaining tags if any, else the extension
     *
     * @param name the file name to parse into a list
     * @return a tokenized list of the file name.
     */
    private static ArrayList<String> parseName(String name) {
        String[] fnameAndTags = name.split("(\\s@)|(\\..+)");
        String ext = name.substring(name.lastIndexOf("."), name.length());
        ArrayList<String> parsed = new ArrayList<>(Arrays.asList(fnameAndTags));
        parsed.add(ext);

        return parsed;
    }
}
