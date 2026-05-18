import io.WorldLoader;
import simulation.World;
import simulation.Controller;
import simulation.WorldSnapshot;
import simulation.HexSnapshot;
import simulation.CritterSnapshot;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.animation.AnimationTimer;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.CheckBox;
import javafx.stage.FileChooser;
import javafx.scene.control.Tooltip;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.Objects;

public class GuiMain extends Application {

    private World world;
    private Controller controller;
    private String worldName = "Default Empty World";

    // Simulation threading
    private Thread simThread;
    private volatile boolean running = false;
    private long stepCount = 0;
    private static final int MS_PER_STEP = 50;
    private volatile int stepsPerSec = 20;
    private final AtomicReference<WorldSnapshot> latestSnapshot = new AtomicReference<>();
    private final ConcurrentLinkedQueue<Long> stepTimestamps = new ConcurrentLinkedQueue<>();

    // UI Components
    private Canvas canvas;
    private Label stepLabel;
    private Label critterLabel;
    private Label fpsLabel;
    private Label spsLabel;
    private Label selectedLabel;
    private VBox rightPanel;
    private VBox inspectorContent;
    private Button toggleBtn;
    private Button stepBtn;
    private int selectedX = -1;
    private int selectedY = -1;

    // Manual placement state
    private simulation.Critter pendingManualCritter = null;
    private boolean manualPlacementActive = false;
    
    // Rendering configs: HEX_RADIUS is circumradius R
    private static final double HEX_RADIUS = 12.0;
    private static final double SQRT_3 = Math.sqrt(3.0);
    // Pan and zoom
    private double panX = 0;
    private double panY = 0;
    private double scale = 1.0;
    private double dragAnchorX, dragAnchorY;
    private double viewAnchorPanX, viewAnchorPanY;
    private long lastFrameTime = -1;
    // FPS smoothing buffer
    private final long[] frameDeltas = new long[30];
    private int frameDeltaIndex = 0;
    private int frameDeltaCount = 0;
    private long frameDeltasSum = 0L;

    @Override
    public void init() throws Exception {
        // You can load arguments here similar to Main.java
        // For simplicity in this mockup, we'll initialize a default world
        var parameters = getParameters().getRaw();
        if (parameters.size() > 0) {
            WorldLoader.WorldAndController wac = WorldLoader.loadFromFile(Path.of(parameters.get(0)));
            world = wac.world;
            controller = wac.controller;
            worldName = wac.name;
        } else {
            world = new World(50, 50);
            controller = new Controller(world, new ArrayList<>());
            worldName = "Default Empty World";
        }
    }

    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #1e1e1e;");
        // Left control panel
        VBox leftPanel = new VBox(8);
        leftPanel.setPadding(new Insets(10));
        leftPanel.setPrefWidth(260);
        leftPanel.setStyle("-fx-background-color: #252526; -fx-border-color: #333; -fx-border-width: 0 1 0 0;");

        // Canvas for the world
        canvas = new Canvas(800, 600);
        StackPane centerPane = new StackPane(canvas);
        canvas.widthProperty().bind(centerPane.widthProperty());
        canvas.heightProperty().bind(centerPane.heightProperty());
        root.setCenter(centerPane);

        // Right side panel
        rightPanel = new VBox(20);
        rightPanel.setPadding(new Insets(20));
        rightPanel.setStyle("-fx-background-color: #252526; -fx-border-color: #333; -fx-border-width: 0 0 0 1;");
        rightPanel.setPrefWidth(280);

        Label titleLabel = new Label("Toadally Awesome\nSimulator");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        titleLabel.setTextFill(Color.web("#e0e0e0"));

        stepLabel = new Label("Steps: 0");
        stepLabel.setTextFill(Color.web("#a0a0a0"));
        stepLabel.setFont(Font.font("Segoe UI", 14));

        critterLabel = new Label("Critters: " + controller.getTurnOrder().size());
        critterLabel.setTextFill(Color.web("#a0a0a0"));
        critterLabel.setFont(Font.font("Segoe UI", 14));

        fpsLabel = new Label("FPS: 0");
        fpsLabel.setTextFill(Color.web("#E0E0E0"));
        fpsLabel.setFont(Font.font("Monospaced", 13));
        spsLabel = new Label("SPS: 0");
        spsLabel.setTextFill(Color.web("#E0E0E0"));
        spsLabel.setFont(Font.font("Monospaced", 13));

        toggleBtn = new Button("Run");
        setRunButtonPaused();
        toggleBtn.setPrefWidth(Double.MAX_VALUE);
        
        stepBtn = new Button("Step");
        stepBtn.setStyle("-fx-background-color: #3e3e42; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 16; -fx-background-radius: 4;");
        stepBtn.setPrefWidth(Double.MAX_VALUE);
        stepBtn.setOnAction(e -> {
            stopSimulation();
            setRunButtonPaused();
            stepOnce();
        });

        toggleBtn.setOnAction(e -> {
            if (running) {
                stopSimulation();
                setRunButtonPaused();
            } else {
                startSimulation();
                setRunButtonRunning();
            }
        });

        // Execution mode radio buttons
        Label modeLabel = new Label("Execution Mode:");
        modeLabel.setTextFill(Color.web("#cfcfcf"));
        RadioButton continuousRb = new RadioButton("Continuous");
        RadioButton stepRb = new RadioButton("One-by-One");
        ToggleGroup modeGroup = new ToggleGroup();
        continuousRb.setToggleGroup(modeGroup);
        stepRb.setToggleGroup(modeGroup);
        continuousRb.setSelected(true);
        // Radio button text contrast fix
        continuousRb.setTextFill(Color.web("#E0E0E0"));
        stepRb.setTextFill(Color.web("#E0E0E0"));

        // Speed control
        Label speedLabel = new Label("Speed (steps/sec):");
        speedLabel.setTextFill(Color.web("#cfcfcf"));
        Spinner<Integer> speedSpinner = new Spinner<>();
        speedSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 1000, 20));
        stepsPerSec = speedSpinner.getValue();
        // Update simulation speed immediately when spinner changes
        speedSpinner.valueProperty().addListener((obs, oldv, newv) -> {
            if (newv != null) {
                stepsPerSec = newv;
            }
        });

        // Modifiers
        CheckBox forcedMutationCb = new CheckBox("Forced Mutation");
        forcedMutationCb.setTextFill(Color.web("#cfcfcf"));
        CheckBox mannaCb = new CheckBox("Manna");
        mannaCb.setTextFill(Color.web("#cfcfcf"));
        // Initialize manna checkbox from controller state and wire changes back to controller
        /*
        mannaCb.setSelected(controller != null && controller.isMannaEnabled());
        mannaCb.selectedProperty().addListener((obs, oldv, newv) -> {
            if (controller != null) {
                controller.setMannaEnabled(newv);
            }
        });*/

        // Load buttons
        Button loadWorldBtn = new Button("Load World");
        Button loadCritterBtn = new Button("Load Critter");
        loadWorldBtn.setPrefWidth(Double.MAX_VALUE);
        loadCritterBtn.setPrefWidth(Double.MAX_VALUE);
        loadWorldBtn.setOnAction(ev -> {
            FileChooser fc = new FileChooser();
            fc.setTitle("Open World File");
            Path p = null;
            var f = fc.showOpenDialog(primaryStage);
            if (f != null) {
                try {
                    stopSimulation();
                    setRunButtonPaused();
                    WorldLoader.WorldAndController wac = WorldLoader.loadFromFile(f.toPath());
                    this.world = wac.world;
                    this.controller = wac.controller;
                    this.worldName = wac.name;
                    this.stepCount = 0;
                    latestSnapshot.set(WorldSnapshot.from(world));
                    // Sync the manna checkbox with the newly loaded controller
                    // mannaCb.setSelected(controller != null && controller.isMannaEnabled());
                    clearInspector();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        loadCritterBtn.setOnAction(ev -> {
            FileChooser fc = new FileChooser();
            fc.setTitle("Open Critter File");
            var f = fc.showOpenDialog(primaryStage);
            if (f == null) return;
            try {
                simulation.Critter loaded = io.CritterLoader.loadFromFile(f.toPath());
                // Show placement options: random N or manual click
                Stage dialog = new Stage();
                dialog.initOwner(primaryStage);
                dialog.setTitle("Place Critter");
                VBox box = new VBox(8);
                box.setPadding(new Insets(10));
                Label info = new Label("Choose placement mode for the loaded critter:");
                Spinner<Integer> copiesSpinner = new Spinner<>(1, 500, 1);
                copiesSpinner.setPrefWidth(120);
                Button placeRandomBtn = new Button("Place N Random Copies");
                Button manualBtn = new Button("Manual: Click to place one");
                Button cancelBtn = new Button("Cancel");
                placeRandomBtn.setOnAction(ae -> {
                    int n = copiesSpinner.getValue();
                    int placed = 0;
                    int attempts = 0;
                    while (placed < n && attempts++ < n * 1000) {
                        int rx = (int) (Math.random() * world.getWidth());
                        int ry = (int) (Math.random() * world.getHeight());
                        if (!world.isValidCoordinate(rx, ry)) continue;
                        if (world.getHex(rx, ry).isEmpty()) {
                            // create copy for each placement
                            simulation.Critter copy = new simulation.Critter(loaded.getEnergy(), loaded.getDirection(), loaded.getCritterInterpreter());
                            copy.setMemorySize(loaded.getMemorySize());
                            copy.setOffense(loaded.getOffense());
                            copy.setDefense(loaded.getDefense());
                            copy.setPosture(loaded.getPosture());
                            copy.setSpecies(loaded.getSpecies());
                            controller.addCritter(copy, rx, ry);
                            placed++;
                        }
                    }
                    latestSnapshot.set(WorldSnapshot.from(world));
                    dialog.close();
                });
                manualBtn.setOnAction(ae -> {
                    // enable manual placement mode
                    pendingManualCritter = loaded;
                    manualPlacementActive = true;
                    dialog.close();
                });
                cancelBtn.setOnAction(ae -> dialog.close());
                box.getChildren().addAll(info, copiesSpinner, placeRandomBtn, manualBtn, cancelBtn);
                Scene ds = new Scene(box);
                dialog.setScene(ds);
                dialog.showAndWait();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        // Canvas interactions: pan/drag/scroll and click-to-select
        canvas.setOnMousePressed(e -> {
            dragAnchorX = e.getX();
            dragAnchorY = e.getY();
            viewAnchorPanX = panX;
            viewAnchorPanY = panY;
        });
        canvas.setOnMouseDragged(e -> {
            panX = viewAnchorPanX + (e.getX() - dragAnchorX);
            panY = viewAnchorPanY + (e.getY() - dragAnchorY);
        });
        canvas.setOnScroll(e -> {
            if (e.isShiftDown()) {
                // scroll-wheel pan horizontally when shift is down
                panX += e.getDeltaY();
            } else if (e.isControlDown()) {
                // ctrl+scroll = zoom
                scale = Math.max(0.2, Math.min(4.0, scale + e.getDeltaY() * 0.001));
            } else {
                panY += e.getDeltaY();
            }
        });
        canvas.setOnMouseClicked(e -> {
            double px = e.getX();
            double py = e.getY();
            int[] found = findHexAt(px, py);
            if (found != null) {
                int hx = found[0], hy = found[1];
                // Manual placement mode
                if (manualPlacementActive && pendingManualCritter != null) {
                    if (world.isValidCoordinate(hx, hy) && world.getHex(hx, hy).isEmpty()) {
                        controller.addCritter(pendingManualCritter, hx, hy);
                        latestSnapshot.set(WorldSnapshot.from(world));
                        // clear manual mode
                        pendingManualCritter = null;
                        manualPlacementActive = false;
                    }
                    return;
                }

                var snap = latestSnapshot.get();
                if (snap != null) {
                    HexSnapshot hs = snap.getHex(hx, hy);
                    if (hs != null) {
                        // update right panel selected label and inspector
                        selectedX = hx;
                        selectedY = hy;
                        selectedLabel.setText(String.format("Selected: (%d,%d)", hx, hy));
                        showInspector(hs, hx, hy);
                    } else {
                        clearInspector();
                    }
                }
            }
        });

        selectedLabel = new Label("Selected: none");
        selectedLabel.setTextFill(Color.web("#a0a0a0"));
        inspectorContent = new VBox(8);
        inspectorContent.setPadding(new Insets(10, 0, 0, 0));
        rightPanel.getChildren().addAll(titleLabel, stepLabel, critterLabel, selectedLabel, inspectorContent);
        root.setRight(rightPanel);
        Button helpBtn = new Button("Help?");
        helpBtn.setOnAction(ae -> showHelp(primaryStage));
        Tooltip.install(loadWorldBtn, new Tooltip("Load a saved world file"));
        Tooltip.install(loadCritterBtn, new Tooltip("Load a critter file and choose placement options"));
        Tooltip.install(continuousRb, new Tooltip("Continuous playback executes steps automatically"));
        Tooltip.install(stepRb, new Tooltip("One-by-one mode lets you step manually"));
        Tooltip.install(speedSpinner, new Tooltip("Choose steps per second for continuous mode."));
        Tooltip.install(forcedMutationCb, new Tooltip("Force mutation events during budding"));
        Tooltip.install(mannaCb, new Tooltip("Enable manna drops in the world"));
        Tooltip.install(toggleBtn, new Tooltip("Run or pause the simulation"));

        // Help button styling to match dark theme
        helpBtn.setStyle("-fx-background-color: #3A3A3A; -fx-text-fill: #CCCCCC; -fx-border-color: #555555; -fx-border-radius: 4; -fx-background-radius: 4;");
        helpBtn.setOnMouseEntered(ev -> helpBtn.setStyle("-fx-background-color: #4A4A4A; -fx-text-fill: #CCCCCC; -fx-border-color: #555555; -fx-border-radius: 4; -fx-background-radius: 4;"));
        helpBtn.setOnMouseExited(ev -> helpBtn.setStyle("-fx-background-color: #3A3A3A; -fx-text-fill: #CCCCCC; -fx-border-color: #555555; -fx-border-radius: 4; -fx-background-radius: 4;"));

        leftPanel.getChildren().addAll(helpBtn, fpsLabel, spsLabel, toggleBtn, stepBtn, loadWorldBtn, loadCritterBtn, modeLabel, continuousRb, stepRb, speedLabel, speedSpinner, forcedMutationCb, mannaCb);
        root.setLeft(leftPanel);

        // Rendering loop with FPS smoothing (rolling average over last 30 frames)
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                renderWorld();
                // frame delta accounting
                if (lastFrameTime > 0) {
                    long delta = now - lastFrameTime;
                    // replace oldest in circular buffer
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
                critterLabel.setText("Critters: " + controller.getTurnOrder().size());
            }
        };
        timer.start();

        Scene scene = new Scene(root, 1000, 600);
        primaryStage.setTitle("Toadally Awesome World Simulator");
        primaryStage.setScene(scene);
        // Keyboard shortcuts
        scene.setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case SPACE -> {
                    if (continuousRb.isSelected()) {
                        // toggle run/pause
                        if (running) {
                            stopSimulation();
                            setRunButtonPaused();
                        } else {
                            startSimulation();
                            setRunButtonRunning();
                        }
                    } else {
                        // one-by-one step
                        stepOnce();
                    }
                }
                case PLUS, EQUALS -> scale = Math.min(4.0, scale * 1.1);
                case MINUS -> scale = Math.max(0.2, scale / 1.1);
                case ESCAPE -> clearInspector();
                default -> {}
            }
        });
        primaryStage.show();

        // Initial draw
        renderWorld();
    }

    private void renderWorld() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        // world dimensions used via snap.width / snap.height below
        
        WorldSnapshot snap = latestSnapshot.get();
        if (snap == null) {
            // fallback to live world snapshot to avoid NPE
            snap = WorldSnapshot.from(world);
            latestSnapshot.set(snap);
        }

        // Doubled-coordinate flat-top spacing:
        // pixelX = x * 1.5R, pixelY = y * (sqrt(3) / 2)R.
        double R = HEX_RADIUS * scale;
        double colSpacing = columnSpacing(R);
        double rowSpacing = rowSpacing(R);

        // center the grid
        double canvasCenterX = canvas.getWidth() / 2.0 + panX;
        double canvasCenterY = canvas.getHeight() / 2.0 + panY;
        double lastCol = snap.width - 1;
        double lastRow = snap.height - 1;
        // Total width and height for grid centered on canvas
        double totalWidth = lastCol * colSpacing;
        double totalHeight = lastRow * rowSpacing;

        double offsetX = canvasCenterX - totalWidth / 2.0;
        double offsetY = canvasCenterY - totalHeight / 2.0;

        for (int y = 0; y < snap.height; y++) {
            for (int x = 0; x < snap.width; x++) {
                if (!world.isValidCoordinate(x, y)) continue;
                HexSnapshot hex = snap.getHex(x, y);

                // For doubled coordinates: valid cells already form checkerboard
                // No row-based offset needed; x and y directly map to visual positions
                double cx = offsetX + x * colSpacing;
                double cy = offsetY + y * rowSpacing;

                Color fillColor = Color.web("#2d2d30");
                Color strokeColor = Color.web("#3e3e42");

                if (hex.rock) {
                    fillColor = Color.web("#424242");
                    strokeColor = Color.web("#616161");
                } else if (hex.critter != null) {
                    // species-stable color
                    fillColor = colorForKey(hex.critter.speciesKey, 0.25, 0.6);
                    strokeColor = colorForKey(hex.critter.speciesKey, 0.35, 0.75);
                } else if (hex.foodAmount > 0) {
                    fillColor = Color.web("#d0eaff");
                    strokeColor = Color.web("#b3d9ff");
                }

                drawHexagon(gc, cx, cy, R, fillColor, strokeColor);

                if (hex.critter != null) {
                    // draw critter as circle scaled by size (radius proportional to critter Size)
                    // draw critter as circle scaled by size
                    double radius = Math.max(3.0, hex.critter.size * R * 0.18);
                    gc.setFill(colorForKey(hex.critter.speciesKey, 0.9, 0.9));
                    gc.fillOval(cx - radius / 2.0, cy - radius / 2.0, radius, radius);
                    var direction = simulation.HexDirection.fromIndex(hex.critter.direction);
                    double vx = direction.dx() * colSpacing;
                    double vy = direction.dy() * rowSpacing;
                    double length = Math.max(1e-9, Math.hypot(vx, vy));
                    double ax = cx + vx / length * (R * 0.55);
                    double ay = cy + vy / length * (R * 0.55);
                    gc.setStroke(Color.BLACK);
                    gc.setLineWidth(1.5);
                    gc.strokeLine(cx, cy, ax, ay);
                } else if (hex.foodAmount > 0) {
                    double fr = Math.max(3.0, R * 0.3);
                    gc.setFill(Color.web("#D9A200"));
                    gc.fillOval(cx - fr / 2.0, cy - fr / 2.0, fr, fr);
                }
            }
        }


        // Compute SPS by trimming timestamps older than 1s
        long cutoff = System.nanoTime() - 1_000_000_000L;
        while (true) {
            Long t = stepTimestamps.peek();
            if (t == null || t >= cutoff) break;
            stepTimestamps.poll();
        }
        spsLabel.setText("SPS: " + stepTimestamps.size());
    }

    private void drawHexagon(GraphicsContext gc, double cx, double cy, double R, Color fill, Color stroke) {
        double[] xPoints = new double[6];
        double[] yPoints = new double[6];

        for (int i = 0; i < 6; i++) {
            double angle = Math.toRadians(60.0 * i);
            xPoints[i] = cx + R * Math.cos(angle);
            yPoints[i] = cy + R * Math.sin(angle);
        }

        gc.setFill(fill);
        gc.fillPolygon(xPoints, yPoints, 6);
        gc.setStroke(stroke);
        gc.setLineWidth(1.0);
        gc.strokePolygon(xPoints, yPoints, 6);
    }

    private int[] findHexAt(double px, double py) {
        WorldSnapshot snap = latestSnapshot.get();
        if (snap == null) return null;
        double R = HEX_RADIUS * scale;
        double colSpacing = columnSpacing(R);
        double rowSpacing = rowSpacing(R);
        double canvasCenterX = canvas.getWidth() / 2.0 + panX;
        double canvasCenterY = canvas.getHeight() / 2.0 + panY;
        double lastCol = snap.width - 1;
        double lastRow = snap.height - 1;
        double totalWidth = lastCol * colSpacing;
        double totalHeight = lastRow * rowSpacing;
        double offsetX = canvasCenterX - totalWidth / 2.0;
        double offsetY = canvasCenterY - totalHeight / 2.0;

        double bestDist = Double.MAX_VALUE;
        int bestX = -1, bestY = -1;
        for (int y = 0; y < snap.height; y++) {
            for (int x = 0; x < snap.width; x++) {
                if (!world.isValidCoordinate(x, y)) continue;
                double cx = offsetX + x * colSpacing;
                double cy = offsetY + y * rowSpacing;
                double dx = px - cx;
                double dy = py - cy;
                double d2 = dx * dx + dy * dy;
                if (d2 < bestDist && isInsideFlatTopHex(dx, dy, R)) {
                    bestDist = d2;
                    bestX = x;
                    bestY = y;
                }
            }
        }
        if (bestX >= 0) {
            return new int[]{bestX, bestY};
        }
        return null;
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
        return ax <= radius
            && ay <= rowSpacing(radius)
            && SQRT_3 * ax + ay <= SQRT_3 * radius;
    }

    private Color colorForKey(String key, double saturation, double brightness) {
        int h = Math.abs(Objects.requireNonNull(key).hashCode());
        float hue = (h % 360);
        return Color.hsb(hue, saturation, brightness);
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
        stopSimulation();
        setRunButtonPaused();
        controller.step();
        stepCount++;
        latestSnapshot.set(WorldSnapshot.from(world));
        stepTimestamps.add(System.nanoTime());
        refreshSelectedHex();
    }

    private void refreshSelectedHex() {
        if (selectedX < 0 || selectedY < 0) return;
        WorldSnapshot snap = latestSnapshot.get();
        if (snap == null) return;
        HexSnapshot hs = snap.getHex(selectedX, selectedY);
        if (hs == null || !world.isValidCoordinate(selectedX, selectedY)) {
            clearInspector();
            return;
        }
        showInspector(hs, selectedX, selectedY);
    }

    private void showHelp(Stage owner) {
        Stage d = new Stage();
        d.initOwner(owner);
        d.setTitle("Help — Controls");
        VBox box = new VBox(8);
        box.setPadding(new Insets(12));
        String[] lines = new String[]{
            "Load World: open a saved world file.",
            "Load Critter: choose random/manual placement.",
            "Run/Pause: toggle simulation playback.",
            "Execution Mode: Continuous or One-by-One (Step).",
            "Speed: steps per second (applies only in Continuous mode).",
            "Modifiers: Forced Mutation and Manna toggles.",
            "Pan: drag mouse. Scroll: pan; Ctrl+Scroll: zoom.",
            "Click hex: select critter / place manual critter.",
            "Keyboard: Space = step or toggle Run/Pause; +/- = zoom; Escape = close inspector."
        };
        for (String l : lines) box.getChildren().add(new Label(l));
        Scene s = new Scene(box);
        d.setScene(s);
        d.show();
    }

    private void showInspector(HexSnapshot hs, int hx, int hy) {
        Platform.runLater(() -> {
            if (inspectorContent == null) return;
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

                    // Memory display
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

                    javafx.scene.control.TextArea programArea = new javafx.scene.control.TextArea(cs.programText == null ? "" : cs.programText);
                    programArea.setEditable(false);
                    programArea.setPrefRowCount(8);
                    programArea.setWrapText(true);

                    Button popOut = new Button("Pop-out Program");
                    popOut.setOnAction(e -> {
                        Stage ps = new Stage();
                        ps.initOwner(rightPanel.getScene().getWindow());
                        javafx.scene.control.TextArea ta = new javafx.scene.control.TextArea(cs.programText == null ? "" : cs.programText);
                        ta.setEditable(false);
                        ta.setWrapText(true);
                        Scene s = new Scene(new VBox(ta), 600, 400);
                        ps.setScene(s);
                        ps.setTitle("Program — " + cs.speciesKey);
                        ps.show();
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
            close.setOnAction(e -> clearInspector());

            inspectorContent.getChildren().addAll(header, posLabel, infoBox, close);
        });
    }

    private void clearInspector() {
        Platform.runLater(() -> {
            selectedX = -1;
            selectedY = -1;
            selectedLabel.setText("Selected: none");
            if (inspectorContent != null) {
                inspectorContent.getChildren().clear();
            }
        });
    }

    private void startSimulation() {
        running = true;
        simThread = new Thread(() -> {
            while (running) {
                controller.step();
                stepCount++;
                // snapshot after step and push timestamp
                try {
                    latestSnapshot.set(WorldSnapshot.from(world));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                stepTimestamps.add(System.nanoTime());
                try {
                    long sleepMs = Math.max(1, 1000 / Math.max(1, stepsPerSec));
                    Thread.sleep(sleepMs);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        simThread.setDaemon(true);
        simThread.start();
    }

    private void stopSimulation() {
        running = false;
        if (simThread != null) {
            simThread.interrupt();
        }
    }

    @Override
    public void stop() {
        stopSimulation();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
