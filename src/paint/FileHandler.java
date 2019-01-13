package paint;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Optional;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javax.imageio.ImageIO;

public class FileHandler {
    
    private final Stage primaryStage;
    private final StackPane stack;
    private final ImageView background;
    private File openedFilePath;
    private File savedFilePath;
    private boolean changeMade;
    private String fileName;
    private String fileExtension;
    
    /**
     * Creates a new FileHandler object.
     * @param primaryStage the main stage
     * @param stack to capture the image to save
     * @param background to display a background image
     */
    public FileHandler(Stage primaryStage, StackPane stack, ImageView background) {
        this.primaryStage = primaryStage;
        this.stack = stack;
        this.background = background;
        openedFilePath = null;
        savedFilePath = null;
        changeMade = false;
        fileName = "untitled_image";
        fileExtension = ".PNG";
    }
    
    /**
     * Allows the user to pick an image to open in the background.
     */
    public void open() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif"));
        File selectedFile = fileChooser.showOpenDialog(primaryStage);
        try {
            BufferedImage bufferedImage = ImageIO.read(selectedFile);
            if (bufferedImage != null) {
                Image selectedImage = SwingFXUtils.toFXImage(bufferedImage, null);
                background.setImage(selectedImage);
                openedFilePath = selectedFile;
                setChangeMade(true);
            }
        } catch (IOException ex) {
            Alert alert = new Alert(Alert.AlertType.WARNING, 
                    "Error: " + ex.getMessage());
            alert.showAndWait();
            System.out.println(ex.getMessage());
        }
    }
    
    /**
     * Decides whether to use save or save as.
     */
    public void handleSave() {
        if (savedFilePath == null) {
            saveAs();
        } else {
            save();
        }
    }
    
    /**
     * Allows the user to save the current image/drawing to a location of their choice.
     */
    public void saveAs() {
        fileExtension = selectFileExtension();
        if (fileExtension.equals("")) { return; }
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Image As");
        fileChooser.setInitialFileName(fileName + fileExtension);
        savedFilePath = fileChooser.showSaveDialog(primaryStage);
        if (savedFilePath != null) {
            WritableImage image = stack.snapshot(new SnapshotParameters(), null);
            try {
                ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", savedFilePath);
                setChangeMade(false);
            } catch (IOException ex) {
                Alert alert = new Alert(Alert.AlertType.WARNING, 
                        "Error: " + ex.getMessage());
                alert.showAndWait();
                System.out.println(ex.getMessage());
            }
        }
    }
    
    /**
     * Saves the current image/drawing to the previously chosen location.
     */
    public void save() {
        if (savedFilePath != null) {
            WritableImage image = stack.snapshot(new SnapshotParameters(), null);
            try {
                ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", savedFilePath);
                setChangeMade(false);
            } catch (IOException ex) {
                Alert alert = new Alert(Alert.AlertType.WARNING, 
                        "Error: " + ex.getMessage());
                alert.showAndWait();
                System.out.println(ex.getMessage());
            }
        }
    }
    
    /**
     * Exits the app if save check returns true.
     * @param event the closing event
     */
    public void quit(WindowEvent event) {
        if (saveCheck(event)) {
            Platform.exit();
        }
    }
    
    /**
     * Gets whether or not a change has been made to the drawing.
     * @return true if change has been made
     */
    public boolean getChangeMade() {
        return changeMade;
    }
    
    /**
     * Sets whether or not a change has been made to the drawing.
     * Also updates the stage title to indicate if there are unsaved changes.
     * @param changeMade true if change has been made
     */
    public void setChangeMade(boolean changeMade) {
        this.changeMade = changeMade;
        if (changeMade) {
            primaryStage.setTitle("Paint! Unsaved Changes!");
        } else {
            primaryStage.setTitle("Paint!");
        }
    }
    
    /**
     * Asks the user what file extension they want to use.
     * @return the selected file extension
     */
    private String selectFileExtension() {
        String ex = "";
        ButtonType png = new ButtonType(".PNG");
        ButtonType jpg = new ButtonType(".JPG");
        ButtonType gif = new ButtonType(".GIF");
        ButtonType cancel = new ButtonType("Cancel");
        Alert alert = new Alert(Alert.AlertType.NONE, 
                "What file type do you want to save as?", png, jpg, gif, cancel);
        alert.setTitle("Choose File Extension");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == png) {
            ex = ".png";
        } else if (result.get() == jpg) {
            ex = ".jpg";
        } else if (result.get() == gif) {
            ex = ".gif";
        } else {
            ex = "";
        }
        return ex;
    }
    
    /**
     * Checks whether the user should be asked to save before quitting.
     * @param event the closing event
     * @return true if it is okay to quit
     */
    private boolean saveCheck(WindowEvent event) {
        if (changeMade) {
            ButtonType yes = new ButtonType("Yes");
            ButtonType no = new ButtonType("No");
            ButtonType cancel = new ButtonType("Cancel");
            Alert alert = new Alert(Alert.AlertType.WARNING, 
                    "Would you like to save before exiting?", yes, no, cancel);
            alert.setTitle("Save Warning");
            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == yes) {
                handleSave();
                return true;
            } else if (result.get() == no) {
                return true;
            } else /*if (result.get() == cancel)*/ {
                try {
                    event.consume();
                } catch (Exception ex) {
                    System.out.println(ex.getMessage());
                }
            }
        } else {
            return true;
        }
        return false;
    }
    
}
