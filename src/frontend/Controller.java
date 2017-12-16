package frontend;

import backend.HistoryManager;
import backend.ImageFile;
import backend.ImageTagger;
import backend.InvalidTagException;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.DirectoryChooser;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.*;
import java.util.*;
import java.util.List;
import java.util.stream.Stream;

/** The class used to control view and model */
public class Controller implements Initializable {

    @FXML
    private Button addTag;

    @FXML
    private Button removeTag;

    @FXML
    private Button createTag;

    @FXML
    private Button chooseDirectory;

    @FXML
    private Button revertTags;

    @FXML
    private Button moveFile;

    @FXML
    private Button showDir;

    @FXML
    private Button showLog;

    /** UI list of all imageFiles in directory */
    @FXML
    private ListView<ImageFile> imagesInDirectory;

    /** ObservableList of all ImageFiles in directory */
    private ObservableList<ImageFile> imageList = FXCollections.observableArrayList();

    /**
     * This list is bounded to imageList, thus it mimics the imageList's changes.
     * A filter may added to filter off certain image files.
     */
    private FilteredList<ImageFile> filteredImages = new FilteredList<>(imageList, s -> true);

    /** UI list of available tags */
    @FXML
    private ListView<String> unusedTagViewList;

    /** UI list of tags in use by an image */
    @FXML
    private ListView<String> imageTagViewList;

    /** UI Image view */
    @FXML
    private ImageView imageView;

    @FXML
    private BorderPane borderPane;

    /** UI label representing the path of the selected image */
    @FXML
    private Label path;

    @FXML
    private AnchorPane pathPane;

    /** UI input, allows user to input what to search for images in imagesInDirectory*/
    @FXML
    private TextField searchInput;

    @FXML
    private Button exitSearch;

    /** Currently selected imageFile */
    private ImageFile selectedImage = null;

    /** instance of backend.ImageTagger object (the model of the program) */
    private ImageTagger imageTagger;

    /** Manages the observable lists of unusedTagViewList and imageTagViewList */
    private TagListsHandler tagListsHandler;

    /** Directory user has opened. */
    private Path currentDir;

    /**
     * Opens a directory chooser.
     * Populates all images in and under selected directory to ListView.\
     *
     * @param event this is ignored (necessary for SceneBuilder callback)
     */
    public void chooseDirectoryAction(ActionEvent event) {
        PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:**.{jpg,jpeg,png,gif,bmp}");
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File selectedDirectory = directoryChooser.showDialog(null);
        if (selectedDirectory != null && selectedDirectory.isDirectory()) {
            currentDir = selectedDirectory.toPath();
            imageList.clear(); // Clear the image list.
            searchInput.setText("");
            // This triggers the imageList's changeListener which in turn, clears other elements.

            Path directoryPath = Paths.get(selectedDirectory.getAbsolutePath());
            try (Stream<Path> files = Files.walk(directoryPath)) {
                files.forEach(path -> {
                    if (matcher.matches(path)) {
                        imageList.add(new ImageFile(path, currentDir));
                    }
                });
            } catch (IOException e) {
                PopUpGenerator.generateErrorMessage("Unable to open Directory or subDirectory");
                return;
            }
            updateTags();
        }
    }

    /**
     * Look through each image in imageList and add any pre-existing tags.
     */
    private void updateTags() {
        boolean invalidTagFound = false;
        for (ImageFile image : imageList) {
            ArrayList<String> imageTags = ImageTagger.parseTags(image);
            try {
                if(!imageTagger.createTagsIfNotExist(imageTags))
                    invalidTagFound = true;
            } catch (IOException e) {
                PopUpGenerator.generateErrorMessage(e.getMessage());
                return;
            }
        }
        if(invalidTagFound)
            PopUpGenerator.generateErrorMessage("Ignoring invalid tag(s) that were detected");
    }


    /**
     * create an instance of imageTagger.
     * set up UI features.
     *
     * @param location creates relative paths for root object.
     * @param resources used to find root object.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {

        try {
            imageTagger = new ImageTagger();
        } catch (IOException e) {
            PopUpGenerator.generateErrorMessage("We have encountered a fatal error. Exiting Program....");
            Platform.exit(); // exit program if imageTagger encounters an error on initialization.
            System.exit(0);
        }

        unusedTagViewList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE); //tag list  multi-selection
        imageTagViewList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        tagListsHandler = new TagListsHandler(imageTagger);
        tagListsHandler.bindToListViews(unusedTagViewList, imageTagViewList); // Bind tag ListViews to the 2 tag lists.
        imagesInDirectory.setItems(filteredImages); // Bind images ListView to the list of imageFiles filteredImages.
        setupSearchListener(); //Set filteredImages to listen to changes in search TextField.

        path.prefWidthProperty().bind(pathPane.widthProperty());
        imageView.fitWidthProperty().bind(borderPane.widthProperty());
        imageView.fitHeightProperty().bind(borderPane.heightProperty());

        // reset UI elements when a new image is selected from imagedInDirectory or selectedImage becomes null.
        imagesInDirectory.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<ImageFile>() {
            @Override
            public void changed(ObservableValue<? extends ImageFile> observable, ImageFile oldValue, ImageFile newValue) {
                selectedImage = newValue;
                if (selectedImage == null) {
                    imageView.setImage(null);
                    tagListsHandler.clearTagLists();
                    path.textProperty().unbind(); //unbind path label from selectedImage
                    path.setText("");
                    return;
                }
                path.textProperty().bind(selectedImage.getPathProperty()); //bind path label to new selected item
                Image image = new Image(selectedImage.getFilePath().toUri().toString());
                imageView.setImage(image);
                tagListsHandler.setUpTagLists(selectedImage); //set up and unusedTagViewList and imageTagViewList
            }
        });
    }

    /**
     * Open dialog box and allow user to input a new tag name.
     *
     * @param event this is ignored (necessary for SceneBuilder callback)
     */
    public void createTagAction(ActionEvent event) {
        Optional<String> result = PopUpGenerator.generateTextInputDialog("Create new tag",
                "Enter a new tag (only alphanumeric and space)", "Enter your new tag name");
        if (!result.isPresent()) return;
        String tag = result.get().trim();

        try {
            if (imageTagger.createTag(tag) && selectedImage != null) //check if tag can be created.
                tagListsHandler.addTagToAvailableTags(tag);
        } catch (IOException | InvalidTagException e) {
            PopUpGenerator.generateErrorMessage(e.getMessage());
        }
    }

    /**
     * Adds tag(s) to selectedImage.
     *
     * @param event this is ignored (necessary for SceneBuilder callback)
     */
    public void addTagsAction(ActionEvent event) {
        ObservableList<String> selectedTags = unusedTagViewList.getSelectionModel().getSelectedItems();
        if (selectedTags == null || selectedImage == null) return;
        try {
            imageTagger.tagImage(selectedImage, selectedTags);
        } catch (IOException e) {
            PopUpGenerator.generateErrorMessage("Unable to tag image. Reason: " + e.getLocalizedMessage());
            return;
        }

        //Update all 3 lists in UI to show changes.
        tagListsHandler.addTagsToImage(selectedTags);
        imagesInDirectory.refresh();
    }

    /**
     * Remove tag(s) from selectedImage.
     *
     * @param event this is ignored (necessary for SceneBuilder callback)
     */
    public void removeTagsAction(ActionEvent event) {
        ObservableList<String> selectedTags = imageTagViewList.getSelectionModel().getSelectedItems();
        if (selectedTags == null || selectedImage == null) return;
        try {
            imageTagger.unTagImage(selectedImage, selectedTags);
        } catch (IOException e) {
            PopUpGenerator.generateErrorMessage("Unable to remove tag from image. Reason: " + e.getLocalizedMessage());
            return;
        }

        //Update all 3 lists in UI to show changes.
        tagListsHandler.removeTagsFromImage(selectedTags);
        imagesInDirectory.refresh();
    }

    /**
     * Allow user to select a previous names of selectedImage from a list previous names.
     * Revert name of selectedImage.
     *
     * @param event this is ignored (necessary for SceneBuilder callback)
     */
    public void revertImageTags(ActionEvent event) {
        if (selectedImage == null) return;
        List<String> choices;
        try {
            choices = imageTagger.getOldNames(selectedImage);
        } catch (IOException e) {
            PopUpGenerator.generateErrorMessage("Unable to tag image. Reason: " + e.getLocalizedMessage());
            return;
        }
        if (choices.size() == 0) {
            PopUpGenerator.generateErrorMessage("There are no previous tags.");
            return;
        }
        Optional<String> result = PopUpGenerator.generateChoiceDialog("Revert Tags",
                "Choose a previous tag set to revert to:", choices);
        if (result.isPresent()) {
            try {
                imageTagger.renameImage(selectedImage, result.get());
            } catch (IOException e) {
                PopUpGenerator.generateErrorMessage("Unable to revert to old tag set");
                return;
            }

            // update all 3 UI lists.
            imagesInDirectory.refresh();
            tagListsHandler.setUpTagLists(selectedImage);
        }
    }

    /**
     * Opens a directory chooser, and allows user to choose which directory to move the selected image.
     *
     * @param event this is ignored (necessary for SceneBuilder callback)
     */
    public void moveImageFile(ActionEvent event) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        if (selectedImage == null) return;
        File selectedDirectory = directoryChooser.showDialog(null);
        if (selectedDirectory == null || !selectedDirectory.isDirectory()) return;// check if user selected valid dir
        try {
            imageTagger.moveFile(selectedImage, selectedDirectory.toPath());
        } catch (FileAlreadyExistsException e) { // check if file with same name already exists in dir.
            PopUpGenerator.generateErrorMessage("File with same name exists in destination.");
            return;
        } catch (IOException e) {
            PopUpGenerator.generateErrorMessage("Unable to move file to destination.");
            return;
        }
        if (!selectedDirectory.toPath().startsWith(currentDir.toAbsolutePath()))
            imageList.remove(selectedImage); // remove image from imageList if it is moved outside of currentDir.
        else
            imagesInDirectory.refresh(); //refresh to show changes in UI.
    }

    /**
     * Opens the parent directory of the selected image.
     *
     * @param event this is ignored (necessary for SceneBuilder callback)
     */
    public void showDirAction(ActionEvent event) {
        if (selectedImage == null) return;

        File file = selectedImage.getFilePath().toFile().getParentFile();
        Desktop desktop = Desktop.getDesktop();
        if(!Desktop.isDesktopSupported())
        {
            // Possible reason: http://bugs.java.com/view_bug.do?bug_id=6486393)
            PopUpGenerator.generateErrorMessage("Sorry, this feature is not supported for your machine.");
            return;
        }
        // thread is launched to avoid crash.
        // see: https://stackoverflow.com/questions/23176624/javafx-freeze-on-desktop-openfile-desktop-browseuri
        new Thread(() -> {
            try {
                desktop.open(file);
            } catch (IOException e) {
                PopUpGenerator.generateErrorMessage("Unable to open this folder.");
            }
        }).start();
    }

    //The function below was made with code provided from an outside source:
    // ***************************************************************************************/
    //        Title: How to use JavaFX FilteredList in a ListView? [closed]
    //        Author: eckig
    //        Date: Feb 11, 2017
    //        Code version: 1.0
    //        Availability: https://stackoverflow.com/questions/28448851/how-to-use-javafx-filteredlist-in-a-listview
    // ***************************************************************************************

    /**
     * Sets filter of filteredImage to listen to searchInput.
     * Thus, as user types into searchInput, filteredImage filters imagesInDirectory.
     */
    private void setupSearchListener() {
        searchInput.textProperty().addListener(observable -> {
            String filter = searchInput.getText();
            if (filter == null || filter.length() == 0) {
                filteredImages.setPredicate(s -> true);
            } else {
                filteredImages.setPredicate(s -> s.toString().contains(filter));
            }
        });
    }

    /**
     * Clears searchInput.
     *
     * @param event this is ignored (necessary for SceneBuilder callback)
     */
    public void clearSearchAction(ActionEvent event) {
        searchInput.setText("");
    }

    /**
     * Generates a text dialog with a visual representation of all renaming done with the program.
     *
     * @param event this is ignored (necessary for SceneBuilder callback)
     */
    public void showLogAction(ActionEvent event) {
        String content;
        try {
            content = HistoryManager.readSupLog();
        } catch (IOException e) {
            PopUpGenerator.generateErrorMessage("Unable to show log history file.");
            return;
        }
        PopUpGenerator.generateTextAreaDialog("Log History", "History of all renames...",
                content,"Format: DD.MM.YY HH:mm OldName -> NewName");
    }
}
