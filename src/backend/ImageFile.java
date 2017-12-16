package backend;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.nio.file.Path;

/** Collection of data about an image */
public class ImageFile {

    /** Path of imageFile */
    private Path filePath;

    /** StringProperty of image path */
    private StringProperty absPathProperty;

    /** Path of directory opened by user in program. */
    private Path baseDir;

    /**
     * Class constructor
     */
    public ImageFile(Path filePath, Path baseDir) {
        this.filePath = filePath;
        this.baseDir = baseDir;
        this.absPathProperty = new SimpleStringProperty("Path: " + filePath.toString());
    }

    /**
     * @return return filename of image
     */
    public String getFileName() {
        return this.filePath.getFileName().toString();
    }

    /**
     * @return return path of image
     */
    public Path getFilePath() {
        return filePath;
    }

    /**
     * @return return string representation of the image's path relative to baseDir
     */
    @Override
    public String toString() {
        return this.baseDir.relativize(this.filePath).toString();
    }

    /**
     * Set the filePath of backend.ImageFile
     * @param filePath the filePath to set.
     */
    public void setFilePath(Path filePath) {
        this.filePath = filePath;
        this.absPathProperty.set("Path: " + filePath.toString());
    }

    /**
     * @return return absPathProperty
     */
    public StringProperty getPathProperty() {
        return this.absPathProperty;
    }

}
