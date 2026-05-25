package Release;
import ast.Mutator;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import io.CritterLoader;
import io.WorldLoader;
import simulation.Controller;
import simulation.Critter;
import simulation.CritterSnapshot;
import simulation.HexSnapshot;
import simulation.World;
import simulation.WorldSnapshot;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicReference;

public class CritterWorldController {
    @FXML private AnchorPane root;
    @FXML private Canvas worldCanvas;
    @FXML private VBox detailsPanel;
    @FXML private VBox statsPanel;
    @FXML private VBox controlsPanel;
    @FXML private Label worldNameLabel;
    @FXML private Label stepLabel;
    @FXML private Label critterLabel;
    @FXML private Label fpsLabel;
    @FXML private Label spsLabel;
    @FXML private Label selectedLabel;
    @FXML private VBox inspectorContent;
    @FXML private Button toggleBtn;
    @FXML private Button stepBtn;
    @FXML private Button loadWorldBtn;
    @FXML private Button loadCritterBtn;
    @FXML private Button helpBtn;
    @FXML private RadioButton continuousRb;
    @FXML private RadioButton stepRb;
    @FXML private Spinner<Integer> speedSpinner;
    @FXML private CheckBox forcedMutationCb;
    @FXML private CheckBox mannaCb;

    private final List<String> launchArguments = new ArrayList<>();
    private final AtomicReference<WorldSnapshot> latestSnapshot = new AtomicReference<>();
    private final ConcurrentLinkedQueue<Long> stepTimestamps = new ConcurrentLinkedQueue<>();

    private World world;
    private Controller simulationController;
    private String worldName = "Default Empty World";

    private Thread simThread;
    private volatile boolean running;
    private long stepCount;
    private volatile int stepsPerSec = 20;

    private static final double HEX_RADIUS = 12.0;
    private static final double SQRT_3 = Math.sqrt(3.0);

    private double panX;
    private double panY;
    private double scale = 1.0;
    private double dragAnchorX;
    private double dragAnchorY;
    private double viewAnchorPanX;
    private double viewAnchorPanY;
    private long lastFrameTime = -1;
    private final long[] frameDeltas = new long[30];
    private int frameDeltaIndex;
    private int frameDeltaCount;
    private long frameDeltasSum;

    private int selectedX = -1;
    private int selectedY = -1;
    private Critter pendingManualCritter;
    private boolean manualPlacementActive;

    private AnimationTimer renderTimer;

    public void setLaunchArguments(List<String> args) {
        launchArguments.clear();
        if (args != null) {
            launchArguments.addAll(args);
        }
    }

    public void attachScene(Scene scene) {
        scene.addEventFilter(KeyEvent.KEY_PRESSED, this::handleKeyPressed);
    }

    @FXML
    public void initialize() {
        configureWorldState();

        worldCanvas.widthProperty().bind(((AnchorPane) worldCanvas.getParent()).widthProperty());
        worldCanvas.heightProperty().bind(((AnchorPane) worldCanvas.getParent()).heightProperty());

        speedSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 1000, 20));
        stepsPerSec = speedSpinner.getValue();
        speedSpinner.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null) {
                stepsPerSec = newValue;
            }
        });

        ToggleGroup modeGroup = new ToggleGroup();
        continuousRb.setToggleGroup(modeGroup);
        stepRb.setToggleGroup(modeGroup);
        continuousRb.setSelected(true);

        worldNameLabel.setText(worldName);
        stepLabel.setText("Steps: 0");
        critterLabel.setText("Critters: " + simulationController.getTurnOrder().size());
        fpsLabel.setText("FPS: 0");
        spsLabel.setText("SPS: 0");
        selectedLabel.setText("Selected: none");
        latestSnapshot.set(WorldSnapshot.from(world));

        tooltip(loadWorldBtn, "Load a saved world file");
        tooltip(loadCritterBtn, "Load a critter file and choose placement options");
        tooltip(continuousRb, "Continuous playback executes steps automatically");
        tooltip(stepRb, "One-by-one mode lets you step manually");
        tooltip(speedSpinner, "Choose steps per second for continuous mode");
        tooltip(forcedMutationCb, "Force offspring mutation when critters bud");
        tooltip(mannaCb, "Toggle random manna food drops during simulation");
        tooltip(toggleBtn, "Run or pause the simulation");

        simulationController.setForceMutationEnabled(forcedMutationCb.isSelected());
        forcedMutationCb.selectedProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null) {
                simulationController.setForceMutationEnabled(newValue);
            }
        });

        mannaCb.setSelected(simulationController.isMannaEnabled());
        mannaCb.selectedProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null) {
                simulationController.setMannaEnabled(newValue);
            }
        });

        renderTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                renderWorld();
                if (lastFrameTime > 0) {
                    long delta = now - lastFrameTime;
                    if (frameDeltaCount < frameDeltas.length) {
                        frameDeltas[frameDeltaIndex] = delta;
                        frameDeltasSum += delta;
                        frameDeltaCount++;
                    } else {
                        frameDeltasSum -= frameDeltas[frameDeltaIndex];
                        frameDeltas[frameDeltaIndex] = delta;
                        frameDeltasSum += delta;
                    }
                    frameDeltaIndex = (frameDeltaIndex + 1) % frameDeltas.length;
                    if (frameDeltaCount == frameDeltas.length && frameDeltaIndex == 0) {
                        double totalSec = frameDeltasSum / 1_000_000_000.0;
                        double avgFps = frameDeltas.length / Math.max(1e-9, totalSec);
                        fpsLabel.setText(String.format("FPS: %.0f", avgFps));
                    }
                }
                lastFrameTime = now;
                stepLabel.setText("Steps: " + stepCount);
                critterLabel.setText("Critters: " + simulationController.getTurnOrder().size());
            }
        };
        renderTimer.start();

        setRunButtonPaused();
        root.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.addEventFilter(KeyEvent.KEY_PRESSED, this::handleKeyPressed);
            }
        });

        renderWorld();
    }

    private void configureWorldState() {
        try {
            if (!launchArguments.isEmpty()) {
                WorldLoader.WorldAndController wac = WorldLoader.loadFromFile(Path.of(launchArguments.get(0)));
                world = wac.world;
                simulationController = wac.controller;
                simulationController.setForceMutationEnabled(forcedMutationCb == null || forcedMutationCb.isSelected());
                simulationController.setMannaEnabled(mannaCb == null || mannaCb.isSelected());
                worldName = wac.name;
            } else {
                world = new World(50, 50);
                simulationController = new Controller(world, new ArrayList<>());
                simulationController.setForceMutationEnabled(forcedMutationCb == null || forcedMutationCb.isSelected());
                simulationController.setMannaEnabled(mannaCb == null || mannaCb.isSelected());
                worldName = "Default Empty World";
            }
        } catch (Exception ex) {
            world = new World(50, 50);
            simulationController = new Controller(world, new ArrayList<>());
            simulationController.setForceMutationEnabled(forcedMutationCb == null || forcedMutationCb.isSelected());
            simulationController.setMannaEnabled(mannaCb == null || mannaCb.isSelected());
            worldName = "Default Empty World";
            ex.printStackTrace();
        }
    }

    @FXML private void handleRunToggle() {
        if (running) {
            stopSimulation();
            setRunButtonPaused();
        } else {
            startSimulation();
            setRunButtonRunning();
        }
    }

    @FXML private void handleStep() {
        stopSimulation();
        setRunButtonPaused();
        stepOnce();
    }

    @FXML private void handleLoadWorld() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Open World File");
        Stage stage = (Stage) root.getScene().getWindow();
        var file = chooser.showOpenDialog(stage);
        if (file == null) {
            return;
        }
        try {
            stopSimulation();
            setRunButtonPaused();
            WorldLoader.WorldAndController wac = WorldLoader.loadFromFile(file.toPath());
            world = wac.world;
            simulationController = wac.controller;
            simulationController.setForceMutationEnabled(forcedMutationCb.isSelected());
            simulationController.setMannaEnabled(mannaCb.isSelected());
            worldName = wac.name;
            stepCount = 0;
            latestSnapshot.set(WorldSnapshot.from(world));
            worldNameLabel.setText(worldName);
            clearInspector();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @FXML private void handleLoadCritter() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Open Critter File");
        Stage stage = (Stage) root.getScene().getWindow();
        var file = chooser.showOpenDialog(stage);
        if (file == null) {
            return;
        }
        try {
            Critter loaded = CritterLoader.loadFromFile(file.toPath());
            Stage dialog = new Stage();
            dialog.initOwner(stage);
            dialog.setTitle("Place Critter");

            VBox box = new VBox(8);
            box.setPadding(new Insets(12));
            Label info = new Label("Choose placement mode for the loaded critter:");
            Spinner<Integer> copiesSpinner = new Spinner<>(1, 500, 1);
            copiesSpinner.setPrefWidth(120);

            Button placeRandomBtn = new Button("Place N Random Copies");
            Button manualBtn = new Button("Manual: Click to place one");
            Button cancelBtn = new Button("Cancel");

            placeRandomBtn.setOnAction(event -> {
                int n = copiesSpinner.getValue();
                int placed = 0;
                int attempts = 0;
                while (placed < n && attempts++ < n * 1000) {
                    int rx = (int) (Math.random() * world.getWidth());
                    int ry = (int) (Math.random() * world.getHeight());
                    if (!world.isValidCoordinate(rx, ry)) {
                        continue;
                    }
                    if (world.getHex(rx, ry).isEmpty()) {
                        simulationController.addCritter(copyCritter(loaded), rx, ry);
                        placed++;
                    }
                }
                latestSnapshot.set(WorldSnapshot.from(world));
                dialog.close();
            });

            manualBtn.setOnAction(event -> {
                pendingManualCritter = loaded;
                manualPlacementActive = true;
                dialog.close();
            });

            cancelBtn.setOnAction(event -> dialog.close());

            box.getChildren().addAll(info, copiesSpinner, placeRandomBtn, manualBtn, cancelBtn);
            dialog.setScene(new Scene(box, 320, 220));
            dialog.showAndWait();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @FXML private void handleHelp() {
        Stage dialog = new Stage();
        dialog.initOwner(root.getScene().getWindow());
        dialog.setTitle("Help");
        VBox box = new VBox(8);
        box.setPadding(new Insets(12));
        String[] lines = new String[] {
            "Load World: open a saved world file.",
            "Load Critter: choose random/manual placement.",
            "Run/Pause: toggle simulation playback.",
            "Execution Mode: Continuous or One-by-One (Step).",
            "Speed: steps per second (applies only in Continuous mode).",
            "Pan: drag mouse. Scroll: pan; Ctrl+Scroll: zoom.",
            "Click hex: select critter / place manual critter.",
            "Space: step or toggle Run/Pause, +/- zoom, Esc closes the inspector."
        };
        for (String line : lines) {
            box.getChildren().add(new Label(line));
        }
        dialog.setScene(new Scene(box, 560, 240));
        dialog.show();
    }

    @FXML private void handlePanelMousePressed(MouseEvent event) {
        Node panel = (Node) event.getSource();
        dragAnchorX = event.getSceneX() - panel.getLayoutX();
        dragAnchorY = event.getSceneY() - panel.getLayoutY();
        panel.toFront();
    }

    @FXML private void handlePanelMouseDragged(MouseEvent event) {
        Node panel = (Node) event.getSource();
        panel.setLayoutX(event.getSceneX() - dragAnchorX);
        panel.setLayoutY(event.getSceneY() - dragAnchorY);
    }

    @FXML private void handleCanvasMousePressed(MouseEvent event) {
        dragAnchorX = event.getX();
        dragAnchorY = event.getY();
        viewAnchorPanX = panX;
        viewAnchorPanY = panY;
    }

    @FXML private void handleCanvasMouseDragged(MouseEvent event) {
        panX = viewAnchorPanX + (event.getX() - dragAnchorX);
        panY = viewAnchorPanY + (event.getY() - dragAnchorY);
    }

    @FXML private void handleCanvasScroll(ScrollEvent event) {
        if (event.isShiftDown()) {
            panX += event.getDeltaY();
        } else if (event.isControlDown()) {
            scale = Math.max(0.2, Math.min(4.0, scale + event.getDeltaY() * 0.001));
        } else {
            panY += event.getDeltaY();
        }
    }

    @FXML private void handleCanvasClicked(MouseEvent event) {
        int[] found = findHexAt(event.getX(), event.getY());
        if (found == null) {
            return;
        }

        int hx = found[0];
        int hy = found[1];
        if (manualPlacementActive && pendingManualCritter != null) {
            if (world.isValidCoordinate(hx, hy) && world.getHex(hx, hy).isEmpty()) {
                simulationController.addCritter(pendingManualCritter, hx, hy);
                latestSnapshot.set(WorldSnapshot.from(world));
                pendingManualCritter = null;
                manualPlacementActive = false;
            }
            return;
        }

        WorldSnapshot snap = latestSnapshot.get();
        if (snap == null) {
            return;
        }
        HexSnapshot hs = snap.getHex(hx, hy);
        if (hs != null) {
            selectedX = hx;
            selectedY = hy;
            selectedLabel.setText(String.format("Selected: (%d,%d)", hx, hy));
            showInspector(hs, hx, hy);
        } else {
            clearInspector();
        }
    }

    private void handleKeyPressed(KeyEvent event) {
        switch (event.getCode()) {
            case SPACE -> {
                if (continuousRb.isSelected()) {
                    handleRunToggle();
                } else {
                    stepOnce();
                }
            }
            case PLUS, EQUALS -> scale = Math.min(4.0, scale * 1.1);
            case MINUS -> scale = Math.max(0.2, scale / 1.1);
            case ESCAPE -> clearInspector();
            default -> {
            }
        }
    }

    private void renderWorld() {
        GraphicsContext gc = worldCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, worldCanvas.getWidth(), worldCanvas.getHeight());

        WorldSnapshot snap = latestSnapshot.get();
        if (snap == null) {
            snap = WorldSnapshot.from(world);
            latestSnapshot.set(snap);
        }

        double radius = HEX_RADIUS * scale;
        double colSpacing = columnSpacing(radius);
        double rowSpacing = rowSpacing(radius);

        double centerX = worldCanvas.getWidth() / 2.0 + panX;
        double centerY = worldCanvas.getHeight() / 2.0 + panY;
        double totalWidth = (snap.width - 1) * colSpacing;
        double totalHeight = (snap.height - 1) * rowSpacing;
        double offsetX = centerX - totalWidth / 2.0;
        double offsetY = centerY - totalHeight / 2.0;

        for (int y = 0; y < snap.height; y++) {
            for (int x = 0; x < snap.width; x++) {
                if (!world.isValidCoordinate(x, y)) {
                    continue;
                }
                HexSnapshot hex = snap.getHex(x, y);
                if (hex == null) {
                    continue;
                }

                double cx = offsetX + x * colSpacing;
                double cy = offsetY + y * rowSpacing;

                Color fillColor = Color.web("#2d2d30");
                Color strokeColor = Color.web("#3e3e42");

                if (hex.rock) {
                    fillColor = Color.web("#424242");
                    strokeColor = Color.web("#616161");
                } else if (hex.critter != null) {
                    fillColor = colorForKey(hex.critter.speciesKey, 0.25, 0.6);
                    strokeColor = colorForKey(hex.critter.speciesKey, 0.35, 0.75);
                } else if (hex.foodAmount > 0) {
                    fillColor = Color.web("#d0eaff");
                    strokeColor = Color.web("#b3d9ff");
                }

                drawHexagon(gc, cx, cy, radius, fillColor, strokeColor);

                if (hex.critter != null) {
                    double critterRadius = Math.max(3.0, hex.critter.size * radius * 0.18);
                    gc.setFill(colorForKey(hex.critter.speciesKey, 0.9, 0.9));
                    gc.fillOval(cx - critterRadius / 2.0, cy - critterRadius / 2.0, critterRadius, critterRadius);

                    var direction = simulation.HexDirection.fromIndex(hex.critter.direction);
                    double vx = direction.dx() * colSpacing;
                    double vy = direction.dy() * rowSpacing;
                    double length = Math.max(1e-9, Math.hypot(vx, vy));
                    double ax = cx + vx / length * (radius * 0.55);
                    double ay = cy + vy / length * (radius * 0.55);
                    gc.setStroke(Color.BLACK);
                    gc.setLineWidth(1.5);
                    gc.strokeLine(cx, cy, ax, ay);
                } else if (hex.foodAmount > 0) {
                    double foodRadius = Math.max(3.0, radius * 0.3);
                    gc.setFill(Color.web("#D9A200"));
                    gc.fillOval(cx - foodRadius / 2.0, cy - foodRadius / 2.0, foodRadius, foodRadius);
                }
            }
        }

        long cutoff = System.nanoTime() - 1_000_000_000L;
        while (true) {
            Long timestamp = stepTimestamps.peek();
            if (timestamp == null || timestamp >= cutoff) {
                break;
            }
            stepTimestamps.poll();
        }
        spsLabel.setText("SPS: " + stepTimestamps.size());
    }

    private void drawHexagon(GraphicsContext gc, double cx, double cy, double radius, Color fill, Color stroke) {
        double[] xPoints = new double[6];
        double[] yPoints = new double[6];
        for (int i = 0; i < 6; i++) {
            double angle = Math.toRadians(60.0 * i);
            xPoints[i] = cx + radius * Math.cos(angle);
            yPoints[i] = cy + radius * Math.sin(angle);
        }

        gc.setFill(fill);
        gc.fillPolygon(xPoints, yPoints, 6);
        gc.setStroke(stroke);
        gc.setLineWidth(1.0);
        gc.strokePolygon(xPoints, yPoints, 6);
    }

    private int[] findHexAt(double px, double py) {
        WorldSnapshot snap = latestSnapshot.get();
        if (snap == null) {
            return null;
        }

        double radius = HEX_RADIUS * scale;
        double colSpacing = columnSpacing(radius);
        double rowSpacing = rowSpacing(radius);
        double centerX = worldCanvas.getWidth() / 2.0 + panX;
        double centerY = worldCanvas.getHeight() / 2.0 + panY;
        double totalWidth = (snap.width - 1) * colSpacing;
        double totalHeight = (snap.height - 1) * rowSpacing;
        double offsetX = centerX - totalWidth / 2.0;
        double offsetY = centerY - totalHeight / 2.0;

        double bestDist = Double.MAX_VALUE;
        int bestX = -1;
        int bestY = -1;
        for (int y = 0; y < snap.height; y++) {
            for (int x = 0; x < snap.width; x++) {
                if (!world.isValidCoordinate(x, y)) {
                    continue;
                }
                double cx = offsetX + x * colSpacing;
                double cy = offsetY + y * rowSpacing;
                double dx = px - cx;
                double dy = py - cy;
                double distanceSquared = dx * dx + dy * dy;
                if (distanceSquared < bestDist && isInsideFlatTopHex(dx, dy, radius)) {
                    bestDist = distanceSquared;
                    bestX = x;
                    bestY = y;
                }
            }
        }

        return bestX >= 0 ? new int[] { bestX, bestY } : null;
    }

    private double columnSpacing(double radius) {
        return 1.5 * radius;
    }

    private double rowSpacing(double radius) {
        return (SQRT_3 / 2.0) * radius;
    }

    private boolean isInsideFlatTopHex(double dx, double dy, double radius) {
        double ax = Math.abs(dx);
        double ay = Math.abs(dy);
        return ax <= radius && ay <= rowSpacing(radius) && SQRT_3 * ax + ay <= SQRT_3 * radius;
    }

    private Color colorForKey(String key, double saturation, double brightness) {
        int hash = Math.abs(Objects.requireNonNull(key).hashCode());
        return Color.hsb(hash % 360, saturation, brightness);
    }

    private void setRunButtonPaused() {
        toggleBtn.setText("Run");
        toggleBtn.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 16; -fx-background-radius: 4;");
    }

    private void setRunButtonRunning() {
        toggleBtn.setText("Pause");
        toggleBtn.setStyle("-fx-background-color: #ffb300; -fx-text-fill: black; -fx-font-weight: bold; -fx-padding: 8 16; -fx-background-radius: 4;");
    }

    private void stepOnce() {
        simulationController.step();
        stepCount++;
        latestSnapshot.set(WorldSnapshot.from(world));
        stepTimestamps.add(System.nanoTime());
        refreshSelectedHex();
    }

    private void refreshSelectedHex() {
        if (selectedX < 0 || selectedY < 0) {
            return;
        }
        WorldSnapshot snap = latestSnapshot.get();
        if (snap == null) {
            return;
        }
        HexSnapshot hs = snap.getHex(selectedX, selectedY);
        if (hs == null || !world.isValidCoordinate(selectedX, selectedY)) {
            clearInspector();
            return;
        }
        showInspector(hs, selectedX, selectedY);
    }

    private void showInspector(HexSnapshot hs, int hx, int hy) {
        Platform.runLater(() -> {
            inspectorContent.getChildren().clear();

            Label header = new Label("Hex Inspector");
            header.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
            header.setTextFill(Color.web("#e0e0e0"));

            Label posLabel = new Label(String.format("Location: (%d, %d)", hx, hy));
            posLabel.setTextFill(Color.web("#a0a0a0"));
            posLabel.setFont(Font.font("Segoe UI", 14));

            VBox infoBox = new VBox(5);
            infoBox.setPadding(new Insets(10, 0, 10, 0));

            if (hs.rock) {
                Label rockLabel = new Label("Type: ROCK");
                rockLabel.setTextFill(Color.web("#9e9e9e"));
                rockLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
                infoBox.getChildren().add(rockLabel);
            } else {
                Label foodLabel = new Label("Food: " + hs.foodAmount);
                foodLabel.setTextFill(Color.web("#d0eaff"));
                infoBox.getChildren().add(foodLabel);

                if (hs.critter != null) {
                    CritterSnapshot cs = hs.critter;
                    Label critterHeader = new Label("Critter: " + cs.speciesKey);
                    critterHeader.setTextFill(colorForKey(cs.speciesKey, 0.8, 0.9));
                    critterHeader.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));

                    Label attrs = new Label(String.format("Energy: %d\nSize: %d\nDir: %d\nPosture: %d\nOff: %d Def: %d",
                        cs.energy, cs.size, cs.direction, cs.posture, cs.offense, cs.defense));
                    attrs.setTextFill(Color.web("#cfcfcf"));
                    attrs.setWrapText(true);

                    StringBuilder memSb = new StringBuilder("Memory:");
                    for (int i = 0; i < cs.memory.length; i++) {
                        memSb.append(i == 0 ? " " : ", ");
                        memSb.append(i).append("=").append(cs.memory[i]);
                    }
                    Label memLabel = new Label(memSb.toString());
                    memLabel.setWrapText(true);
                    memLabel.setTextFill(Color.web("#cfcfcf"));

                    Label lastRule = new Label("Last Rule: " + (cs.lastRule == null ? "none" : cs.lastRule));
                    lastRule.setWrapText(true);
                    lastRule.setTextFill(Color.web("#f0c0c0"));

                    TextArea programArea = new TextArea(cs.programText == null ? "" : cs.programText);
                    programArea.setEditable(false);
                    programArea.setPrefRowCount(8);
                    programArea.setWrapText(true);

                    Button popOut = new Button("Pop-out Program");
                    popOut.setOnAction(event -> {
                        Stage popOutStage = new Stage();
                        popOutStage.initOwner(root.getScene().getWindow());
                        TextArea ta = new TextArea(cs.programText == null ? "" : cs.programText);
                        ta.setEditable(false);
                        ta.setWrapText(true);
                        popOutStage.setScene(new Scene(new VBox(ta), 600, 400));
                        popOutStage.setTitle("Program - " + cs.speciesKey);
                        popOutStage.show();
                    });

                    infoBox.getChildren().addAll(critterHeader, attrs, memLabel, lastRule, programArea, popOut);
                } else {
                    Label emptyLabel = new Label("Type: EMPTY");
                    emptyLabel.setTextFill(Color.web("#a0a0a0"));
                    infoBox.getChildren().add(emptyLabel);
                }
            }

            Button close = new Button("Close Inspector");
            close.setStyle("-fx-background-color: #3e3e42; -fx-text-fill: white; -fx-padding: 5 10;");
            close.setOnAction(event -> clearInspector());

            inspectorContent.getChildren().addAll(header, posLabel, infoBox, close);
        });
    }

    private void clearInspector() {
        Platform.runLater(() -> {
            selectedX = -1;
            selectedY = -1;
            selectedLabel.setText("Selected: none");
            inspectorContent.getChildren().clear();
        });
    }

    private void startSimulation() {
        if (running) {
            return;
        }
        running = true;
        simThread = new Thread(() -> {
            while (running) {
                try {
                    simulationController.step();
                    stepCount++;
                    latestSnapshot.set(WorldSnapshot.from(world));
                    stepTimestamps.add(System.nanoTime());
                } catch (Exception ex) {
                    // Catch silent crashes — show error in UI so it's visible in the .exe
                    ex.printStackTrace();
                    running = false;
                    Platform.runLater(() -> {
                        setRunButtonPaused();
                        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                            javafx.scene.control.Alert.AlertType.ERROR);
                        alert.setTitle("Simulation Error");
                        alert.setHeaderText("The simulation stopped due to an error:");
                        alert.setContentText(ex.getClass().getSimpleName() + ": " + ex.getMessage());
                        alert.showAndWait();
                    });
                    break;
                }
                try {
                    long sleepMs = Math.max(1, 1000 / Math.max(1, stepsPerSec));
                    Thread.sleep(sleepMs);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }, "critterworld-simulation");
        simThread.setDaemon(true);
        simThread.start();
    }

    private void stopSimulation() {
        running = false;
        if (simThread != null) {
            simThread.interrupt();
        }
    }

    private Critter copyCritter(Critter loaded) {
        Critter copy = new Critter(loaded.getEnergy(), loaded.getDirection(), loaded.getCritterInterpreter());
        copy.setMemorySize(loaded.getMemorySize());
        copy.setOffense(loaded.getOffense());
        copy.setDefense(loaded.getDefense());
        copy.setPosture(loaded.getPosture());
        copy.setSpecies(loaded.getSpecies());
        return copy;
    }

    private void tooltip(Node node, String text) {
        Tooltip.install(node, new Tooltip(text));
    }
}