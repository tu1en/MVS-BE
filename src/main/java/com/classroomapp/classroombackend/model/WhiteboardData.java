package com.classroomapp.classroombackend.model;

import java.time.LocalDateTime;

import com.classroomapp.classroombackend.model.usermanagement.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity để lưu trữ dữ liệu whiteboard trong live sessions
 */
@Entity
@Table(name = "whiteboard_data")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WhiteboardData {
    
    public enum DrawingAction {
        DRAW,
        ERASE,
        CLEAR,
        ADD_TEXT,
        ADD_SHAPE,
        MOVE,
        RESIZE,
        DELETE
    }
    
    public enum DrawingTool {
        PEN,
        PENCIL,
        MARKER,
        ERASER,
        LINE,
        RECTANGLE,
        CIRCLE,
        ARROW,
        TEXT
    }
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "live_stream_id", nullable = false)
    private LiveStream liveStream;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false)
    private DrawingAction actionType;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "tool_type")
    private DrawingTool toolType;
    
    @Lob
    @Column(name = "drawing_data", columnDefinition = "NTEXT")
    private String drawingData; // JSON data cho drawing operations
    
    @Column(name = "x_coordinate")
    private Double xCoordinate;
    
    @Column(name = "y_coordinate")
    private Double yCoordinate;
    
    @Column(name = "width")
    private Double width;
    
    @Column(name = "height")
    private Double height;
    
    @Column(name = "color", length = 20)
    private String color;
    
    @Column(name = "stroke_width")
    private Integer strokeWidth;
    
    @Column(name = "opacity")
    private Double opacity;
    
    @Column(name = "rotation")
    private Double rotation;
    
    @Column(name = "layer_index")
    private Integer layerIndex;
    
    @Column(name = "element_id", length = 100)
    private String elementId; // Unique ID cho mỗi drawing element
    
    @Column(name = "text_content", length = 1000)
    private String textContent; // Nội dung text nếu là text element
    
    @Column(name = "font_family", length = 50)
    private String fontFamily;
    
    @Column(name = "font_size")
    private Integer fontSize;
    
    @Column(name = "is_bold")
    @Builder.Default
    private Boolean isBold = false;
    
    @Column(name = "is_italic")
    @Builder.Default
    private Boolean isItalic = false;
    
    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
    
    @Column(name = "sequence_number")
    private Long sequenceNumber; // Để maintain order của các actions
    
    /**
     * Convert drawing data to JSON format for client
     */
    public String toJson() {
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"id\":\"").append(elementId).append("\",");
        json.append("\"action\":\"").append(actionType).append("\",");
        json.append("\"tool\":\"").append(toolType).append("\",");
        json.append("\"x\":").append(xCoordinate).append(",");
        json.append("\"y\":").append(yCoordinate).append(",");
        json.append("\"width\":").append(width).append(",");
        json.append("\"height\":").append(height).append(",");
        json.append("\"color\":\"").append(color).append("\",");
        json.append("\"strokeWidth\":").append(strokeWidth).append(",");
        json.append("\"opacity\":").append(opacity).append(",");
        json.append("\"rotation\":").append(rotation).append(",");
        json.append("\"layer\":").append(layerIndex).append(",");
        
        if (textContent != null) {
            json.append("\"text\":\"").append(textContent.replace("\"", "\\\"")).append("\",");
            json.append("\"fontFamily\":\"").append(fontFamily).append("\",");
            json.append("\"fontSize\":").append(fontSize).append(",");
            json.append("\"bold\":").append(isBold).append(",");
            json.append("\"italic\":").append(isItalic).append(",");
        }
        
        if (drawingData != null) {
            json.append("\"data\":").append(drawingData).append(",");
        }
        
        json.append("\"userId\":").append(user.getId()).append(",");
        json.append("\"userName\":\"").append(user.getFullName()).append("\",");
        json.append("\"timestamp\":\"").append(createdAt).append("\",");
        json.append("\"sequence\":").append(sequenceNumber);
        json.append("}");
        
        return json.toString();
    }
    
    /**
     * Create drawing data for simple shapes
     */
    public static WhiteboardData createShape(LiveStream liveStream, User user, DrawingTool tool,
                                          double x, double y, double width, double height,
                                          String color, int strokeWidth, String elementId) {
        return WhiteboardData.builder()
                .liveStream(liveStream)
                .user(user)
                .actionType(DrawingAction.ADD_SHAPE)
                .toolType(tool)
                .xCoordinate(x)
                .yCoordinate(y)
                .width(width)
                .height(height)
                .color(color)
                .strokeWidth(strokeWidth)
                .opacity(1.0)
                .rotation(0.0)
                .layerIndex(0)
                .elementId(elementId)
                .isActive(true)
                .build();
    }
    
    /**
     * Create drawing data for text elements
     */
    public static WhiteboardData createText(LiveStream liveStream, User user, double x, double y,
                                         String text, String fontFamily, int fontSize,
                                         String color, boolean bold, boolean italic, String elementId) {
        return WhiteboardData.builder()
                .liveStream(liveStream)
                .user(user)
                .actionType(DrawingAction.ADD_TEXT)
                .toolType(DrawingTool.TEXT)
                .xCoordinate(x)
                .yCoordinate(y)
                .textContent(text)
                .fontFamily(fontFamily)
                .fontSize(fontSize)
                .color(color)
                .isBold(bold)
                .isItalic(italic)
                .opacity(1.0)
                .layerIndex(0)
                .elementId(elementId)
                .isActive(true)
                .build();
    }
}