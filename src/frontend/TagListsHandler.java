package frontend;

import backend.ImageFile;
import backend.ImageTagger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ListView;

import java.util.ArrayList;
import java.util.List;

/** Manages the observable lists of unusedTagViewList and imageTagViewList */
class TagListsHandler {

    /** Tags currently not in use by an image... they are available to the user. */
    private ObservableList<String> availableTags = FXCollections.observableArrayList();

    /** Tags currently in use by an image. */
    private ObservableList<String> usedTags = FXCollections.observableArrayList();

    /** Instance of backend imageTagger class */
    private ImageTagger imageTagger;

    /** Class constructor */
    TagListsHandler(ImageTagger imageTagger) {
        this.imageTagger = imageTagger;
    }

    /**
     * Populates availableTags and usedTags according to tags of a selected image.
     *
     * @param selectedImage an imageFile selected by user.
     */
    void setUpTagLists(ImageFile selectedImage) {
        availableTags.clear();
        usedTags.clear();
        if (selectedImage == null) return;
        setUpAvailableTags(selectedImage);
        setUpUsedTags(selectedImage);
    }

    /**
     * Adds tag to availableTags.
     *
     * @param tag a tag provided by user.
     */
    void addTagToAvailableTags(String tag) {
        availableTags.add(tag);
    }

    /**
     * Updates tag lists when a user tags an image.
     *
     * @param selectedTags tag(s) from availableTags selected by user.
     */
    void addTagsToImage(List<String> selectedTags) {
        usedTags.addAll(selectedTags);
        availableTags.removeAll(selectedTags);
    }

    /**
     * Updates tag lists when a user removes tag(s) from an image.
     *
     * @param selectedTags tag(s) from usedTags selected by user.
     */
    void removeTagsFromImage(List<String> selectedTags) {
            availableTags.addAll(selectedTags);
            usedTags.removeAll(selectedTags);
    }

    /**
     * Bind appropriate UI ListViews to availableTags and usedTags.
     *
     * @param availableTags UI ListView that displays available tags.
     */
    void bindToListViews(ListView<String> availableTags, ListView<String> usedTags) {
        availableTags.setItems(this.availableTags);
        usedTags.setItems(this.usedTags);
    }

    /** Clear lists. */
    void clearTagLists() {
        availableTags.clear();
        usedTags.clear();
    }

    /**
     * Populates availableTags according to tags of a selected image.
     *
     * @param selectedImage an imageFile selected by user.
     */
    private void setUpAvailableTags(ImageFile selectedImage) {
        ArrayList<String> allTags = imageTagger.getTags();
        ArrayList<String> imageTags = ImageTagger.parseTags(selectedImage);
        for (String tag : allTags) {
            if (!imageTags.contains(tag)) {
                availableTags.add(tag);
            }
        }

    }

    /**
     * Populates usedTags according to tags of a selected image.
     *
     * @param selectedImage an imageFile selected by user.
     */
    private void setUpUsedTags(ImageFile selectedImage) {
        usedTags.setAll(ImageTagger.parseTags(selectedImage));
    }
}
