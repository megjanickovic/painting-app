package paint;

import java.text.DecimalFormat;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.geometry.Orientation;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.MenuItemBuilder;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToolBar;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Line;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class Paint extends Application {
    
    @Override
    public void start(Stage primaryStage) {
        
        // set up --------------------------------------------------------------
        BorderPane root = new BorderPane();
        Scene scene = new Scene(root);
        primaryStage.setTitle("Paint!");
        primaryStage.setScene(scene);
        
        // make the app fill the screen
        Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
        primaryStage.setX(primaryScreenBounds.getMinX());
        primaryStage.setY(primaryScreenBounds.getMinY());
        primaryStage.setWidth(primaryScreenBounds.getWidth());
        primaryStage.setHeight(primaryScreenBounds.getHeight());
        
        ImageView background = new ImageView();
        background.setFitWidth(primaryStage.getWidth());
        background.setFitHeight(primaryStage.getHeight());
        background.setPreserveRatio(true);
        background.setSmooth(true);
        background.setCache(true);
        
        Canvas canvas = new Canvas(primaryStage.getWidth(), primaryStage.getHeight());
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setStroke(Color.BLACK);
        gc.setFill(Color.TRANSPARENT);
        gc.setLineWidth(1.0);
        
        StackPane stack = new StackPane();
        stack.getChildren().addAll(background, canvas);
        root.setCenter(stack);
        
        // helper objects ------------------------------------------------------
        FileHandler file = new FileHandler(primaryStage, stack, background);
        DrawHandler draw = new DrawHandler(canvas, gc, file);
        
        // tool bar ------------------------------------------------------------
        ToolBar toolBar = new ToolBar();
        
        ColorPicker lineColorPicker = new ColorPicker(Color.BLACK);
        Label lineColorPickerLabel = new Label("Line: " + draw.colorLookup(lineColorPicker.getValue()));
        ColorPicker fillColorPicker = new ColorPicker(Color.BLACK);
        Label fillColorPickerLabel = new Label("Fill: " + draw.colorLookup(lineColorPicker.getValue()));
        
        ToggleButton fillToggle = new ToggleButton("Fill");
        ToggleButton dropperToggle = new ToggleButton("Color Dropper");
        ToggleButton eraserToggle = new ToggleButton("Eraser");
        ToggleButton selectToggle = new ToggleButton("Select");

        Slider lineWidthSlider = new Slider(1, 10, 1);
        lineWidthSlider.setBlockIncrement(.5);
        DecimalFormat df = new DecimalFormat("#0.0"); 
        Label lineWidthSliderLabel = new Label("Width: " + df.format(lineWidthSlider.getValue()));
        
        toolBar.getItems().addAll(lineColorPickerLabel, lineColorPicker, 
                fillColorPickerLabel, fillColorPicker, fillToggle, 
                dropperToggle, eraserToggle, selectToggle,
                lineWidthSliderLabel, lineWidthSlider);
        toolBar.setOrientation(Orientation.VERTICAL);
        root.setLeft(toolBar);

        // menu bar ------------------------------------------------------------
        MenuBar menuBar = new MenuBar();

        Menu menuFile = new Menu("File");
        MenuItem menuItemOpen = MenuItemBuilder.create()
                .text("Open")
                .accelerator(new KeyCodeCombination(KeyCode.O, KeyCombination.META_DOWN))
                .onAction((ActionEvent event) -> {
                    file.open();
                })
                .build();
        MenuItem menuItemSave = MenuItemBuilder.create()
                .text("Save")
                .accelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.META_DOWN))
                .onAction((ActionEvent event) -> {
                    file.handleSave();
                })
                .build();
        MenuItem menuItemSaveAs = MenuItemBuilder.create()
                .text("Save As")
                .accelerator(new KeyCodeCombination(KeyCode.A, KeyCombination.META_DOWN))
                .onAction((ActionEvent event) -> {
                    file.saveAs();
                })
                .build();
        MenuItem menuItemQuit = MenuItemBuilder.create()
                .text("Quit")
                .onAction((ActionEvent event) -> {
                    file.quit(null);
                })
                .build();
        menuFile.getItems().addAll(menuItemOpen, menuItemSave, menuItemSaveAs, menuItemQuit);
        
        Menu menuEdit = new Menu("Edit");
        MenuItem menuItemUndo = MenuItemBuilder.create()
                .text("Undo")
                .accelerator(new KeyCodeCombination(KeyCode.Z, KeyCombination.META_DOWN))
                .onAction((ActionEvent event) -> {
                    draw.undo();
                })
                .build();
        MenuItem menuItemRedo = MenuItemBuilder.create()
                .text("Redo")
                .accelerator(new KeyCodeCombination(KeyCode.Y, KeyCombination.META_DOWN))
                .onAction((ActionEvent event) -> {
                    draw.redo();
                })
                .build();
        MenuItem menuItemClear = MenuItemBuilder.create()
                .text("Clear")
                .accelerator(new KeyCodeCombination(KeyCode.C, KeyCombination.META_DOWN))
                .onAction((ActionEvent event) -> {
                    draw.clear();
                })
                .build();
        menuEdit.getItems().addAll(menuItemUndo, menuItemRedo, menuItemClear);
        
        Menu menuLines = new Menu("Lines");
        MenuItem menuItemPencil = MenuItemBuilder.create()
                .text("Pencil")
                .accelerator(new KeyCodeCombination(KeyCode.P, KeyCombination.ALT_DOWN))
                .onAction((ActionEvent event) -> {
                    draw.setDrawOption(DrawOption.PENCIL);
                    dropperToggle.setSelected(false);
                    eraserToggle.setSelected(false);
                    selectToggle.setSelected(false);
                })
                .build();
        MenuItem menuItemLine = MenuItemBuilder.create()
                .text("Line")
                .accelerator(new KeyCodeCombination(KeyCode.L, KeyCombination.ALT_DOWN))
                .onAction((ActionEvent event) -> {
                    draw.setDrawOption(DrawOption.LINE);
                    dropperToggle.setSelected(false);
                    eraserToggle.setSelected(false);
                    selectToggle.setSelected(false);
                })
                .build();
        menuLines.getItems().addAll(menuItemPencil, menuItemLine);
        
        Menu menuShapes = new Menu("Shapes");
        MenuItem menuItemSquare = MenuItemBuilder.create()
                .text("Square")
                .accelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.ALT_DOWN))
                .onAction((ActionEvent event) -> {
                    draw.setDrawOption(DrawOption.SQUARE);
                    dropperToggle.setSelected(false);
                    eraserToggle.setSelected(false);
                    selectToggle.setSelected(false);
                })
                .build();
        MenuItem menuItemRectangle = MenuItemBuilder.create()
                .text("Rectangle")
                .accelerator(new KeyCodeCombination(KeyCode.R, KeyCombination.ALT_DOWN))
                .onAction((ActionEvent event) -> {
                    draw.setDrawOption(DrawOption.RECTANGLE);
                    dropperToggle.setSelected(false);
                    eraserToggle.setSelected(false);
                    selectToggle.setSelected(false);
                })
                .build();
        MenuItem menuItemCircle = MenuItemBuilder.create()
                .text("Circle")
                .accelerator(new KeyCodeCombination(KeyCode.C, KeyCombination.ALT_DOWN))
                .onAction((ActionEvent event) -> {
                    draw.setDrawOption(DrawOption.CIRCLE);
                    dropperToggle.setSelected(false);
                    eraserToggle.setSelected(false);
                    selectToggle.setSelected(false);
                })
                .build();
        MenuItem menuItemOval = MenuItemBuilder.create()
                .text("Oval")
                .accelerator(new KeyCodeCombination(KeyCode.O, KeyCombination.ALT_DOWN))
                .onAction((ActionEvent event) -> {
                    draw.setDrawOption(DrawOption.OVAL);
                    dropperToggle.setSelected(false);
                    eraserToggle.setSelected(false);
                    selectToggle.setSelected(false);
                })
                .build();
        MenuItem menuItemTriangle = MenuItemBuilder.create()
                .text("Triangle")
                .accelerator(new KeyCodeCombination(KeyCode.T, KeyCombination.ALT_DOWN))
                .onAction((ActionEvent event) -> {
                    draw.setDrawOption(DrawOption.TRIANGLE);
                    dropperToggle.setSelected(false);
                    eraserToggle.setSelected(false);
                    selectToggle.setSelected(false);
                })
                .build();
        menuShapes.getItems().addAll(menuItemSquare, menuItemRectangle, menuItemCircle, menuItemOval, menuItemTriangle);
        
        Menu menuText = new Menu("Text");
        MenuItem menuItemSetText = MenuItemBuilder.create()
                .text("Set Text")
                .accelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN))
                .onAction((ActionEvent event) -> {
                    draw.setText();
                })
                .build();
        MenuItem menuItemPlaceText = MenuItemBuilder.create()
                .text("Place Text")
                .accelerator(new KeyCodeCombination(KeyCode.P, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN))
                .onAction((ActionEvent event) -> {
                    draw.setDrawOption(DrawOption.TEXT);
                    dropperToggle.setSelected(false);
                    eraserToggle.setSelected(false);
                    selectToggle.setSelected(false);
                })
                .build();
        menuText.getItems().addAll(menuItemSetText, menuItemPlaceText);
        
        menuBar.getMenus().addAll(menuFile, menuEdit, menuLines, menuShapes, menuText);
        root.setTop(menuBar);
        
        // actions -------------------------------------------------------------
        primaryStage.setOnCloseRequest((WindowEvent event) -> {
            file.quit(event);
        });
        lineColorPicker.setOnAction((ActionEvent e) -> {
            gc.setStroke(lineColorPicker.getValue());
            lineColorPickerLabel.setText("Line: " + draw.colorLookup(lineColorPicker.getValue()));
        });
        fillColorPicker.setOnAction((ActionEvent e) -> {
            gc.setFill(fillColorPicker.getValue());
            fillColorPickerLabel.setText("Fill: " + draw.colorLookup(fillColorPicker.getValue()));
        });
        fillToggle.setOnAction((ActionEvent e) -> {
            if (fillToggle.isSelected()) {
                gc.setFill(fillColorPicker.getValue());
            } else {
                gc.setFill(Color.TRANSPARENT);
            }
        });
        dropperToggle.setOnAction((ActionEvent e) -> {
            if (dropperToggle.isSelected()) {
                draw.setDrawOption(DrawOption.DROPPER);
                eraserToggle.setSelected(false);
                selectToggle.setSelected(false);
            } else {
                draw.setDrawOption(DrawOption.NONE);
            }
        });
        eraserToggle.setOnAction((ActionEvent e) -> {
            if (eraserToggle.isSelected()) {
                draw.setDrawOption(DrawOption.ERASER);
                dropperToggle.setSelected(false);
                selectToggle.setSelected(false);
            } else {
                draw.setDrawOption(DrawOption.NONE);
            }
        });
        selectToggle.setOnAction((ActionEvent e) -> {
            if (selectToggle.isSelected()) {
                draw.setDrawOption(DrawOption.IMAGE_SELECTION);
                eraserToggle.setSelected(false);
                dropperToggle.setSelected(false);
            } else {
                draw.setDrawOption(DrawOption.NONE);
            }
        });
        lineWidthSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            gc.setLineWidth(lineWidthSlider.getValue());
            lineWidthSliderLabel.setText("Width: " + df.format(lineWidthSlider.getValue()));
        });
        
        canvas.addEventHandler(MouseEvent.MOUSE_PRESSED, (MouseEvent m) -> {
            switch (draw.getDrawOption()) {
                case PENCIL:
                    Path p = new Path();
                    MoveTo moveTo = new MoveTo();
                    moveTo.setX(m.getX());
                    moveTo.setY(m.getY());
                    p.getElements().add(moveTo);
                    draw.addShape(new PaintShape (DrawOption.PENCIL, p));
                    draw.redraw();
                    break;
                case LINE:
                    Line l = new Line(m.getX(), m.getY(), m.getX(), m.getY());
                    draw.addShape(new PaintShape(DrawOption.LINE, l));
                    draw.redraw();
                    break;
                case SQUARE:
                    Rectangle s = new Rectangle(m.getX(), m.getY(), 0, 0);
                    draw.addShape(new PaintShape(DrawOption.SQUARE, s));
                    draw.redraw();
                    break;
                case RECTANGLE:
                    Rectangle r = new Rectangle(m.getX(), m.getY(), 0, 0);
                    draw.addShape(new PaintShape(DrawOption.RECTANGLE, r));
                    draw.redraw();
                    break;
                case CIRCLE:
                    Ellipse c = new Ellipse(m.getX(), m.getY(), 0, 0);
                    draw.addShape(new PaintShape(DrawOption.CIRCLE, c));
                    draw.redraw();
                    break;
                case OVAL:
                    Ellipse o = new Ellipse(m.getX(), m.getY(), 0, 0);
                    draw.addShape(new PaintShape(DrawOption.OVAL, o));
                    draw.redraw();
                    break;
                case TRIANGLE:
                    Line t = new Line(m.getX(), m.getY(), m.getX(), m.getY());
                    draw.addShape(new PaintShape(DrawOption.TRIANGLE, t));
                    draw.redraw();
                    break;
                case TEXT:
                    Text txt = new Text(m.getX(), m.getY(), draw.getText());
                    draw.addShape(new PaintShape (DrawOption.TEXT, txt));
                    draw.redraw();
                    break;
                case IMAGE_SELECTION:
                    Rectangle select = new Rectangle(m.getX(), m.getY(), 0, 0);
                    draw.addShape(new PaintShape(DrawOption.IMAGE_SELECTION, select));
                    draw.redraw();
                    break;
                case IMAGE_MOVE:
                    if (draw.peekShape().getName() == DrawOption.IMAGE_SELECTION) {
                        Rectangle move = (Rectangle) draw.popShape().getShape();
                        Rectangle clear = new Rectangle(move.getX(), move.getY(), move.getWidth(), move.getHeight());
                        clear.setFill(Color.WHITE);
                        draw.addShape(new PaintShape(DrawOption.IMAGE_SELECTION, clear));
                        move.setX(m.getX());
                        move.setY(m.getY());
                        draw.addShape(new PaintShape(DrawOption.IMAGE_SELECTION, move));
                        draw.redraw();
                    }
                    break;
                case DROPPER:
                    WritableImage image = canvas.snapshot(new SnapshotParameters(), null);
                    PixelReader pixelReader = image.getPixelReader();
                    Color color = pixelReader.getColor((int)m.getX(), (int)m.getY());
                    lineColorPicker.setValue(color);
                    gc.setStroke(lineColorPicker.getValue());
                    lineColorPickerLabel.setText("Line: " + draw.colorLookup(lineColorPicker.getValue()));
                    fillColorPicker.setValue(color);
                    if (fillToggle.isSelected()) { gc.setFill(fillColorPicker.getValue()); }
                    fillColorPickerLabel.setText("Fill: " + draw.colorLookup(fillColorPicker.getValue()));
                    break;
                case ERASER:
                    Path e = new Path();
                    MoveTo moveToE = new MoveTo();
                    moveToE.setX(m.getX());
                    moveToE.setY(m.getY());
                    e.getElements().add(moveToE);
                    draw.addShape(new PaintShape (DrawOption.ERASER, e));
                    draw.redraw();
                    break;
                default:
                    break;
            }
        });
        canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED, (MouseEvent m) -> { 
            // this is to prevent the type of shape from being changed mid-drawing
            DrawOption drawOption = DrawOption.NONE;
            if (draw.getDrawOption() != DrawOption.DROPPER && draw.peekShape() != null) {
                PaintShape shape = draw.peekShape();
                if (draw.getDrawOption() == shape.getName()) {
                    drawOption = shape.getName();
                } else if (draw.getDrawOption() == DrawOption.IMAGE_MOVE 
                        && shape.getName() == DrawOption.IMAGE_SELECTION) {
                    drawOption = DrawOption.IMAGE_MOVE;
                }
            }
            switch (drawOption) {
                case PENCIL:
                    Path p = (Path) draw.peekShape().getShape();
                    LineTo lineTo = new LineTo();
                    lineTo.setX(m.getX());
                    lineTo.setY(m.getY());
                    p.getElements().add(lineTo);
                    draw.addShape(new PaintShape(DrawOption.PENCIL, p));
                    draw.redraw();
                    break;
                case LINE:
                    Line l = (Line) draw.popShape().getShape();
                    l.setEndX(m.getX());
                    l.setEndY(m.getY());
                    draw.addShape(new PaintShape(DrawOption.LINE, l));
                    draw.redraw();
                    break;
                case SQUARE: 
                    Rectangle s = (Rectangle) draw.popShape().getShape();
                    s.setWidth(m.getX() - s.getX());
                    s.setHeight(m.getX() - s.getX());
                    draw.addShape(new PaintShape(DrawOption.SQUARE, s));
                    draw.redraw();
                    break;
                case RECTANGLE:
                    Rectangle r = (Rectangle) draw.popShape().getShape();
                    r.setWidth(Math.abs(m.getX() - r.getX()));
                    r.setHeight(Math.abs(r.getY() - m.getY()));
                    draw.addShape(new PaintShape(DrawOption.RECTANGLE, r));
                    draw.redraw();
                    break;
                case CIRCLE:
                    Ellipse c = (Ellipse) draw.popShape().getShape();
                    c.setRadiusX(Math.abs(m.getX() - c.getCenterX()));
                    c.setRadiusY(Math.abs(m.getX() - c.getCenterX()));
                    draw.addShape(new PaintShape(DrawOption.CIRCLE, c));
                    draw.redraw();
                    break;
                case OVAL:
                    Ellipse o = (Ellipse) draw.popShape().getShape();
                    o.setRadiusX(Math.abs(m.getX() - o.getCenterX()));
                    o.setRadiusY(Math.abs(o.getCenterY() - m.getY()));
                    draw.addShape(new PaintShape(DrawOption.OVAL, o));
                    draw.redraw();
                    break;
                case TRIANGLE:
                    Line t = (Line) draw.popShape().getShape();
                    t.setEndX(m.getX());
                    t.setEndY(m.getY());
                    draw.addShape(new PaintShape(DrawOption.TRIANGLE, t));
                    draw.redraw();
                    break;
                case IMAGE_SELECTION:
                    Rectangle select = (Rectangle) draw.popShape().getShape();
                    select.setWidth(Math.abs(m.getX() - select.getX()));
                    select.setHeight(Math.abs(select.getY() - m.getY()));
                    select.setFill(Color.TRANSPARENT);
                    draw.addShape(new PaintShape(DrawOption.IMAGE_SELECTION, select));
                    draw.redraw();
                    break;
                case IMAGE_MOVE:
                    if (draw.peekShape().getName() == DrawOption.IMAGE_SELECTION) {
                        Rectangle move = (Rectangle) draw.popShape().getShape();
                        move.setX(m.getX());
                        move.setY(m.getY());
                        draw.addShape(new PaintShape(DrawOption.IMAGE_SELECTION, move));
                        draw.redraw();
                    }
                    break;
                case ERASER:
                    Path e = (Path) draw.popShape().getShape();
                    LineTo lineToE = new LineTo();
                    lineToE.setX(m.getX());
                    lineToE.setY(m.getY());
                    e.getElements().add(lineToE);
                    draw.addShape(new PaintShape(DrawOption.ERASER, e));
                    draw.redraw();
                    break;
                default:
                    break;
            }
        });
        canvas.addEventHandler(MouseEvent.MOUSE_RELEASED, (MouseEvent m) -> {
            DrawOption drawOption = DrawOption.NONE;
            if (draw.getDrawOption() != DrawOption.DROPPER && draw.peekShape() != null) {
                PaintShape shape = draw.peekShape();
                if (draw.getDrawOption() == shape.getName()) {
                    drawOption = shape.getName();
                } else if (draw.getDrawOption() == DrawOption.IMAGE_MOVE 
                        && shape.getName() == DrawOption.IMAGE_SELECTION) {
                    drawOption = DrawOption.IMAGE_MOVE;
                }
            }
            switch (drawOption) { 
                case IMAGE_SELECTION:
                    Rectangle select = (Rectangle) draw.popShape().getShape();
                    WritableImage img = stack.snapshot(new SnapshotParameters(), null);
                    PixelReader pixelReader = img.getPixelReader();
                    try {
                        WritableImage cropped = new WritableImage(pixelReader, 
                            (int) select.getX(), (int) select.getY(), 
                            (int) select.getWidth(), (int) select.getHeight());
                        ImagePattern imgPattern = new ImagePattern(cropped);
                        select.setFill(imgPattern);
                        draw.addShape(new PaintShape(DrawOption.IMAGE_SELECTION, select));
                        draw.redraw();
                        draw.setDrawOption(DrawOption.IMAGE_MOVE);
                    } catch (Exception ex) {
                        selectToggle.setSelected(false);
                        draw.setDrawOption(DrawOption.NONE);
                    }
                    break;
                case IMAGE_MOVE:
                    if (draw.peekShape().getName() == DrawOption.IMAGE_SELECTION) {
                        Rectangle move = (Rectangle) draw.popShape().getShape();
                        move.setX(m.getX());
                        move.setY(m.getY());
                        draw.addShape(new PaintShape(DrawOption.IMAGE_MOVE, move));
                        draw.redraw();
                        draw.setDrawOption(DrawOption.NONE);
                        selectToggle.setSelected(false);
                    }
                    break;
                default:
                    break;
            }
        });
        
        primaryStage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
}
