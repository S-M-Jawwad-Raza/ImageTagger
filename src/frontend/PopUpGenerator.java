package frontend;

import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

import java.util.List;
import java.util.Optional;

/** Class that contains methods that generate pop-up windows */
public class PopUpGenerator {

    //The following 4 functions were made with code provided from an outside source:
    // ***************************************************************************************/
    //        Title: JavaFX Dialogs (official)
    //        Author: Marco Jakob
    //        Date: Oct 28, 2014
    //        Code version: 1.0
    //        Availability: http://code.makery.ch/blog/javafx-dialogs-official/
    // ***************************************************************************************

    /**
     * @param message error message
     * Open an alert window with message message.
     */
    static void generateErrorMessage(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message); //citation needed
        alert.showAndWait();
    }

    /**
     * @param title title
     * @param header header
     * @param content content
     * @return
     * Open a dialog window that allows user to input a string.
     * Returns input when window is closed.
     */
    static Optional<String> generateTextInputDialog(String title, String header, String content) {
        TextInputDialog dialog = new TextInputDialog(""); //citation needed
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        dialog.setContentText(content);
        return dialog.showAndWait();
    }

    /**
     * @param title title
     * @param content content
     * @param choices the options user must select from.
     * @return
     * Open a dialog window that allows user to select an option.
     * Return selected option as string.
     */
    static Optional<String> generateChoiceDialog(String title, String content, List<String> choices) {
        ChoiceDialog<String> dialog = new ChoiceDialog<>(choices.get(0), choices);
        dialog.setTitle(title);
        dialog.setContentText(content);
        return dialog.showAndWait();
    }

    /**
     * @param title title
     * @param header header
     * @param contentText content of dialogBox
     * @param labelText dialog label
     *
     * Open a dialog window with text.
     */
    static void generateTextAreaDialog(String title, String header, String contentText, String labelText) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);

        TextArea textArea = new TextArea(contentText);
        textArea.setEditable(false);

        Label label = new Label(labelText);

        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);

        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(label, 0, 0);
        expContent.add(textArea, 0, 1);

        alert.setResizable(true);
        alert.getDialogPane().setContent(expContent);

        alert.showAndWait();
    }
}
