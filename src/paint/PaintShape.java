package paint;

import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Shape;

public class PaintShape {
    
    private DrawOption name;
    private Shape shape;
    private Paint strokeColor;
    private Paint fillColor;
    private double strokeWidth;
    
    /**
     * Constructs a new PaintShape object. 
     * @param name the type of shape
     * @param shape the shape object
     */
    public PaintShape(DrawOption name, Shape shape) {
        this.name = name;
        this.shape = shape;
        strokeColor = Color.BLACK;
        fillColor = Color.BLACK;
        strokeWidth = 1.0;
    }
    
    /**
     * Gets the type of shape.
     * @return the DrawOption that names the shape
     */
    public DrawOption getName() { 
        return name; 
    }
    
    /**
     * Gets the Shape object.
     * @return the Shape object
     */
    public Shape getShape() { 
        return shape; 
    }
    
    /**
     * Gets the color of the stroke.
     * @return the Paint for the stroke color
     */
    public Paint getStrokeColor() { 
        return strokeColor; 
    }
    
    /**
     * Gets the color of the fill.
     * @return the Paint for the fill color
     */
    public Paint getFillColor() { 
        return fillColor; 
    }
    
    /**
     * Gets the width of the stroke.
     * @return the width of the stroke 
     */
    public double getStrokeWidth() { 
        return strokeWidth; 
    }
    
    /**
     * Sets the type of shape.
     * @param name the DrawOption that names the shape
     */
    public void setName(DrawOption name) { 
        this.name = name; 
    }
    
    /**
     * Sets the Shape object.
     * @param shape the Shape object
     */
    public void setShape(Shape shape) { 
        this.shape = shape; 
    }
    
    /**
     * Sets the color of the stroke.
     * @param strokeColor the Paint for the stroke color
     */
    public void setStrokeColor(Paint strokeColor) { 
        this.strokeColor = strokeColor; 
    }
    
    /**
     * Sets the color of the fill.
     * @param fillColor the Paint for the fill color
     */
    public void setFillColor(Paint fillColor) { 
        this.fillColor = fillColor; 
    }
    
    /**
     * Sets the width of the stroke.
     * @param strokeWidth the width of the stroke
     */
    public void setStrokeWidth(double strokeWidth) { 
        this.strokeWidth = strokeWidth; 
    }
    
}
