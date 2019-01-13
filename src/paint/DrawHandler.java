package paint;

import java.lang.reflect.Field;
import java.util.Optional;
import java.util.Stack;
import javafx.collections.ObservableList;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.TextInputDialog;
import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Line;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

public class DrawHandler {
    
    private final Canvas canvas;
    private final GraphicsContext gc;
    private final FileHandler file;
    private DrawOption opt;
    private Stack<PaintShape> drawStack;
    private PaintShape lastUndo;
    private String setText;

    /**
     * Creates a new DrawHandler object.
     * @param canvas the canvas to draw on
     * @param gc the graphics context that displays the drawings
     * @param file the FileHandler, used for updating changes made
     */
    public DrawHandler(Canvas canvas, GraphicsContext gc, FileHandler file) {
        this.canvas = canvas;
        this.gc = gc;
        this.file = file;
        opt = DrawOption.NONE;
        drawStack = new Stack();
        lastUndo = null;
        setText = "";
    }
    
    /**
     * Gets the current menu option.
     * @return the current draw option
     */
    public DrawOption getDrawOption() { 
        return opt; 
    }
    
    /**
     * Sets the current menu option.
     * @param opt the new menu option
     */
    public void setDrawOption(DrawOption opt) { 
        this.opt = opt; 
    }
    
    /**
     * Removes the last draw action.
     */
    public void undo() {
        if (!drawStack.empty()) {
            if (drawStack.peek().getName() == DrawOption.IMAGE_MOVE) {
                drawStack.pop(); // remove the move
                drawStack.pop(); // remove the erase
                lastUndo = null;
            } else {
                lastUndo = drawStack.pop();
            }
            file.setChangeMade(true);
            redraw();
        }
    }
    
    /**
     * Repaints the last undone draw action. 
     * Only works for the last undone action.
     */
    public void redo() {
        if (lastUndo != null) {
            drawStack.push(lastUndo);
            file.setChangeMade(true);
            redraw();
        }
    }
    
    /**
     * Add a shape to the stack of drawn shapes.
     * Also save current information about fill, colors, and stroke width from
     * the graphics context so it can be undone/redone.
     * @param ps the PaintShape to add
     */
    public void addShape(PaintShape ps) {
        ps.setStrokeColor(gc.getStroke());
        ps.setFillColor(gc.getFill());
        ps.setStrokeWidth(gc.getLineWidth());
        drawStack.push(ps);
    }
    
    /**
     * Pops the most recently added shape off the stack.
     * @return the most recently added shape, or null if the stack is empty
     */
    public PaintShape popShape() {
        if (!drawStack.empty()) {
            return drawStack.pop();
        } else {
            return null;
        }
    }
    
    /**
     * Peek at the most recently added shape.
     * @return the most recently added shape, or null if the stack is empty
     */
    public PaintShape peekShape() {
        if (!drawStack.empty()) {
            return drawStack.peek();
        } else {
            return null;
        }
    }
    
    /**
     * Redraws the canvas. Used for undo/redo.
     */
    public void redraw() {
        gc.save();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        drawStack.forEach((ps) -> {
            gc.setStroke(ps.getStrokeColor());
            gc.setFill(ps.getFillColor());
            gc.setLineWidth(ps.getStrokeWidth());
            draw(ps);
        });
        gc.restore();
        file.setChangeMade(true);
    }
    
    /**
     * Draws the given shape and adds the action to the list for undo/redo.
     * @param ps the PaintShape to draw
     */
    public void draw(PaintShape ps) {
        switch (ps.getName()) {
            case PENCIL: 
                drawPencil((Path) ps.getShape());
                break;
            case LINE:
                drawLine((Line) ps.getShape());
                break;
            case SQUARE: case RECTANGLE:
                drawRectangle((Rectangle) ps.getShape());
                break;
            case CIRCLE: case OVAL:
                drawOval((Ellipse) ps.getShape());
                break;
            case TRIANGLE:
                drawTriangle((Line) ps.getShape());
                break;
            case TEXT:
                drawText((Text) ps.getShape());
                break;
            case IMAGE_SELECTION:
                drawImageSelection((Rectangle) ps.getShape());
                break;
            case IMAGE_MOVE:
                drawImageMove((Rectangle) ps.getShape());
                break;
            case ERASER:
                erase((Path) ps.getShape());
            default:
                break;
        }
    }
    
    /**
     * Clears the canvas of all drawings.
     */
    public void clear() {
        drawStack = new Stack();
        lastUndo = null;
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }
    
    /**
     * Returns the text that was set by the user in the setText() prompt.
     * @return the user's chosen text
     */
    public String getText() {
        return setText;
    }
    
    /**
        Gets the text to draw from the user.
    */
    public void setText() {
        TextInputDialog inputText = new TextInputDialog("Input Text");
        inputText.setTitle("Enter Text");
        inputText.setHeaderText("Enter the text to draw:");
        inputText.setContentText("Text:");
        Optional<String> result = inputText.showAndWait();
        result.ifPresent((String text) -> {
            setText = text;
        });
    }
    
    /**
     * Used to see if a color has a "name". Only works for the defaults found in Color class.
     * For example: #DC143C returns CRIMSON
     * @param color the color to look up
     * @return the name of the color if found, "Custom" if not
     */
    public String colorLookup(Color color) {
        String colorString = "Custom";
        for (Field field : Color.class.getFields()) {
            if (field.getType() == Color.class) {
                try {
                    Color c = (Color) field.get(null);
                    if (color.equals(c)) {
                        colorString = field.getName();
                    }
                } catch (IllegalAccessException | IllegalArgumentException ex) {
                    Alert alert = new Alert(Alert.AlertType.WARNING, 
                            "Error: " + ex.getMessage());
                    alert.showAndWait();
                    System.out.println(ex.getMessage());
                }
            }
        }
        return colorString;
    }
    
    /**
     * Stroke the given path on the graphics context.
     * @param p the path to draw
     */
    private void drawPencil(Path p) {
            // http://java-buddy.blogspot.com/2014/06/draw-arraylist-of-path-on-canvas.html
            ObservableList<PathElement> l = p.getElements();
            gc.beginPath();
            for (PathElement pe : l) {
                if (pe.getClass() == MoveTo.class) {
                    gc.moveTo(((MoveTo)pe).getX(), ((MoveTo)pe).getY());
                } else if (pe.getClass() == LineTo.class) {
                    gc.lineTo(((LineTo)pe).getX(), ((LineTo)pe).getY());
                }
            }
            gc.stroke();
            gc.closePath();
    }
    
    /**
     * Stroke the given line on the graphics context.
     * @param l the line to draw
     */
    private void drawLine(Line l) {
        gc.strokeLine(l.getStartX(), l.getStartY(), l.getEndX(), l.getEndY());
    }
    
    /**
     * Stroke the given rectangle on the graphics context.
     * @param r the rectangle to draw
     */
    private void drawRectangle(Rectangle r) {
        gc.strokeRect(r.getX(), r.getY(), r.getWidth(), r.getHeight());
        gc.fillRect(r.getX(), r.getY(), r.getWidth(), r.getHeight());
    }
    
    /**
     * Stroke the given ellipse on the graphics context.
     * @param e the ellipse to draw
     */
    private void drawOval(Ellipse e) {
        gc.strokeOval(e.getCenterX(), e.getCenterY(), e.getRadiusX(), e.getRadiusY());
        gc.fillOval(e.getCenterX(), e.getCenterY(), e.getRadiusX(), e.getRadiusY());
    }
    
    /**
     * Stroke an equilateral triangle on the graphics context.
     * @param l one side of the triangle
     */
    private void drawTriangle(Line l) {
        // https://stackoverflow.com/questions/23239703/how-to-draw-an-equilateral-triangle-when-two-points-are-already-given#23241331
        double dx = l.getEndX() - l.getStartX();
        double dy = l.getEndY() - l.getStartY();
        double length = Math.sqrt(dx*dx+dy*dy);
        double dirX = dx / length;
        double dirY = dy / length;
        double height = Math.sqrt(3)/2 * length;
        double cx = l.getStartX() + dx * 0.5;
        double cy = l.getStartY() + dy * 0.5;
        double pDirX = -dirY;
        double pDirY = dirX;
        double x3 = cx - height * pDirX;
        double y3 = cy - height * pDirY;      
        double xs[] = {l.getStartX(), l.getEndX(), x3};
        double ys[] = {l.getStartY(), l.getEndY(), y3};
        gc.strokePolygon(xs, ys, 3);
        gc.fillPolygon(xs, ys, 3);
    }
    
    /**
     * Strokes the given text on the graphics context.
     * @param txt the text to draw.
     */
    private void drawText(Text txt) {
        gc.save();
        gc.setLineWidth(1.0);
        gc.strokeText(txt.getText(), txt.getX(), txt.getY());
        gc.restore();
    }
    
    /**
     * Draws a rectangle around the image selection to be moved. 
     * @param select the dimensions of the image selection
     */
    private void drawImageSelection(Rectangle select) {
        gc.save();
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(0.5);
        gc.setLineDashes(2);
        gc.setFill(select.getFill());
        gc.strokeRect(select.getX(), select.getY(), select.getWidth(), select.getHeight());
        gc.fillRect(select.getX(), select.getY(), select.getWidth(), select.getHeight());
        gc.restore();
    }
    
    /**
     * For drawing the moved image, removes the selection rectangle.
     * @param select the rectangle with the ImagePattern
     */
    private void drawImageMove(Rectangle select) {
        gc.save();
        gc.setFill(select.getFill());
        gc.fillRect(select.getX(), select.getY(), select.getWidth(), select.getHeight());
        gc.restore();
    }
    
    /**
     * Erases the graphics context along the given path.
     * @param e the path to erase
     */
    private void erase(Path e) {
        ObservableList<PathElement> l = e.getElements();
        for (PathElement pe : l) {
            if (pe.getClass() == MoveTo.class) {
                gc.clearRect(((MoveTo)pe).getX(), ((MoveTo)pe).getY(), gc.getLineWidth(), gc.getLineWidth());
            } else if (pe.getClass() == LineTo.class) {
                gc.clearRect(((LineTo)pe).getX(), ((LineTo)pe).getY(), gc.getLineWidth(), gc.getLineWidth());
            }
        }
    }
    
}
