package editor;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class NeonSpriteEditor extends Application {

	private Canvas canvas;
	private float scale = 7;
	private float pointSize;
	private GraphicsContext g;
	private float gridSize = 5f;
	private float width = 30;
	private float height = 60;
	private float cursorX = 0;
	private float cursorY = 0;
	private float mouseX = 0;
	private float mouseY = 0;
	private float prevX = 0;
	private float prevY = 0;
	private float offsetX = 10;
	private float offsetY = 10;

	private boolean isCtrlDown = false;

	private ArrayList<Point> points;
	private ArrayList<Point> gridPoints;
	private Point movingPoint;
	
	/*
	 * TODO:
	 * - Load sprites
	 */

	public static void main(String[] args) {
		launch();
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		pointSize = gridSize / 2;
		points = new ArrayList<Point>();
		gridPoints = new ArrayList<Point>();
		setGridPoints();
		VBox root = new VBox();
		root.setPadding(new Insets(10f, 10f, 10f, 10f));
		root.setSpacing(10f);
		HBox menu = new HBox();
		menu.setSpacing(5);
		TextField widthTextField = new TextField();
		widthTextField.setText(String.valueOf(width));
		TextField heightTextField = new TextField();
		heightTextField.setText(String.valueOf(height));
		TextField gridTextField = new TextField();
		gridTextField.setText(String.valueOf(gridSize));
		Text widthText = new Text("Width: ");
		Text heightText = new Text("Height: ");
		Text gridText = new Text("Grid-size: ");
		Button newSpriteBtn = new Button("New Sprite");
		Button loadBtn = new Button("Load");
		Button saveBtn = new Button("Save");
		FileChooser fc = new FileChooser();
		menu.getChildren().addAll(widthText, widthTextField, heightText, heightTextField, gridText, gridTextField,
				newSpriteBtn, loadBtn, saveBtn);
		canvas = new Canvas();
		g = canvas.getGraphicsContext2D();
		canvas.setWidth(500);
		canvas.setHeight(500);
		root.getChildren().addAll(menu, canvas);
		draw();

		newSpriteBtn.setOnAction(e -> {
			width = Float.parseFloat(widthTextField.getText());
			height = Float.parseFloat(heightTextField.getText());
			gridSize = Float.parseFloat(gridTextField.getText());
			pointSize = gridSize / 2;
			setGridPoints();
			points.clear();
			draw();
		});
		
		loadBtn.setOnAction(e -> {
			File f = fc.showOpenDialog(primaryStage);
			if (f.getName().endsWith(".nspr")) {
				try {
					String str = new String(Files.readAllBytes(Paths.get(f.getAbsolutePath())));
					str = str.replaceAll("\r", "");
					ArrayList<Point> points = new ArrayList<Point>();
					String[] lines = str.split("\n");
					float width = Float.parseFloat(lines[0]);
					float height = Float.parseFloat(lines[1]);
					float gridSize = Float.parseFloat(lines[2]);
					for (int i = 3; i < lines.length; i++) {
						String[] parts = lines[i].split(",");
						float x = Float.parseFloat(parts[0]);
						float y = Float.parseFloat(parts[1]);
						Point p = new Point(x, y);
						points.add(p);
					}
					this.width = width;
					widthTextField.setText(String.valueOf(width));
					this.height = height;
					heightTextField.setText(String.valueOf(height));
					this.gridSize = gridSize;
					gridTextField.setText(String.valueOf(gridSize));
					this.pointSize = gridSize / 2;
					setGridPoints();
					this.points = points;
					draw();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		
		saveBtn.setOnAction(e -> {
			File f = fc.showSaveDialog(primaryStage);
			ArrayList<String> lines = new ArrayList<String>();
			lines.add(String.valueOf(width));
			lines.add(String.valueOf(height));
			lines.add(String.valueOf(gridSize));
			for (int i = 0; i < points.size(); i++) {
				lines.add(points.get(i).getX() + "," + points.get(i).getY());
			}
			try {
				Files.write(Paths.get(f.getAbsolutePath()), lines, StandardCharsets.UTF_8);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		});

		primaryStage.widthProperty().addListener((obs, oldVal, newVal) -> {
			canvas.setWidth(primaryStage.getWidth());
			draw();
		});

		primaryStage.heightProperty().addListener((obs, oldVal, newVal) -> {
			canvas.setHeight(primaryStage.getHeight());
			draw();
		});

		canvas.addEventFilter(ScrollEvent.ANY, new EventHandler<ScrollEvent>() {
			@Override
			public void handle(ScrollEvent event) {
				if (event.getDeltaY() > 0) {
					// Zoom in
					if (scale < 7) {
						scale += 1;
					}
				} else {
					// Zoom out
					if (scale > 1) {
						scale -= 1;
					}
				}
				draw();
				event.consume();
			}
		});

		canvas.setOnMouseMoved(e -> {
			prevX = mouseX;
			prevY = mouseY;
			mouseX = (float) e.getX();
			mouseY = (float) e.getY();
			if (isCtrlDown) {
				offsetX -= (prevX - mouseX) / scale;
				offsetY -= (prevY - mouseY) / scale;
			} else {
				Point p = new Point(mouseX, mouseY);
				Point closestPoint = new Point((gridPoints.get(0).getX() + offsetX) * scale,
						(gridPoints.get(0).getY() + offsetY) * scale);
				for (int i = 1; i < gridPoints.size(); i++) {
					Point to = new Point((gridPoints.get(i).getX() + offsetX) * scale,
							(gridPoints.get(i).getY() + offsetY) * scale);
					float distance = p.distaceTo(to);
					if (distance < p.distaceTo(closestPoint)) {
						closestPoint = to;
					}
				}
				cursorX = closestPoint.getX() / scale - offsetX;
				cursorY = closestPoint.getY() / scale - offsetY;
				if (movingPoint != null) {
					movingPoint.setX(cursorX);
					movingPoint.setY(cursorY);
				}
			}
			draw();
		});

		canvas.setOnMousePressed(e -> {
			Point p = new Point(cursorX, cursorY);
			if (e.getButton() == MouseButton.PRIMARY) {
				points.add(p);
			} else if (e.getButton() == MouseButton.SECONDARY) {
				for (int i = 0; i < points.size(); i++) {
					Point cursor = new Point(cursorX, cursorY);
					if (cursor.distaceTo(points.get(i)) < 0.01f) {
						points.remove(i);
						break;
					}
				}
			}
			draw();
		});

		Scene scene = new Scene(root);
		scene.setOnKeyPressed(e -> {
			if (e.getCode() == KeyCode.CONTROL) {
				isCtrlDown = true;
			}
			if (e.getCode() == KeyCode.ALT) {
				for (int i = 0; i < points.size(); i++) {
					Point cursor = new Point(cursorX, cursorY);
					if (cursor.distaceTo(points.get(i)) < 0.01f) {
						movingPoint = points.get(i);
						break;
					}
				}
			}
		});
		scene.setOnKeyReleased(e -> {
			if (e.getCode() == KeyCode.CONTROL) {
				isCtrlDown = false;
			}
			if (e.getCode() == KeyCode.ALT) {
				movingPoint = null;
			}
		});
		primaryStage.setScene(scene);
		primaryStage.show();
	}

	private void draw() {
		g.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
		g.scale(scale, scale);
		g.setLineWidth(2 / scale);

		g.setFill(Color.GRAY);
		int gridWidth = (int) ((float) width / (float) gridSize) + 1;
		int gridHeight = (int) ((float) height / (float) gridSize) + 1;
		for (int i = 0; i < gridWidth; i++) {
			g.fillRect(i * gridSize + offsetX, offsetY, 1 / scale, height);
		}
		for (int i = 0; i < gridHeight; i++) {
			g.fillRect(offsetX, i * gridSize + offsetY, width, 1 / scale);
		}
		g.setFill(Color.BLACK);

		if (points.size() > 0) {
			g.fillOval(points.get(0).getX() - pointSize / 2 + offsetX, points.get(0).getY() - pointSize / 2 + offsetY,
					pointSize, pointSize);
			for (int i = 1; i < points.size(); i++) {
				Point from = points.get(i - 1);
				Point to = points.get(i);
				g.strokeLine(from.getX() + offsetX, from.getY() + offsetY, to.getX() + offsetX, to.getY() + offsetY);
				g.fillOval(to.getX() - pointSize / 2 + offsetX, to.getY() - pointSize / 2 + offsetY, pointSize,
						pointSize);
			}
		}

		g.strokeOval(cursorX - pointSize / 2 + offsetX, cursorY - pointSize / 2 + offsetY, pointSize, pointSize);
		g.scale(1 / scale, 1 / scale);
	}

	private void setGridPoints() {
		gridPoints.clear();
		int gridWidth = (int) ((float) width / (float) gridSize) + 1;
		int gridHeight = (int) ((float) height / (float) gridSize) + 1;
		for (int i = 0; i < gridWidth; i++) {
			for (int j = 0; j < gridHeight; j++) {
				gridPoints.add(new Point(i * gridSize, j * gridSize));
			}
		}
	}
}
