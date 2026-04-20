package com.lemelson.visualizer.ui;

import com.lemelson.visualizer.core.benchmark.BenchmarkFunction;
import com.lemelson.visualizer.core.benchmark.BenchmarkFunctionDefinition;
import com.lemelson.visualizer.core.benchmark.BenchmarkLibrary;
import com.lemelson.visualizer.core.benchmark.ExpressionBenchmarkFunction;
import com.lemelson.visualizer.core.color.Palette;
import com.lemelson.visualizer.core.color.PaletteType;
import com.lemelson.visualizer.core.contour.MarchingSquares;
import com.lemelson.visualizer.core.grid.GridSpec;
import com.lemelson.visualizer.core.grid.SliceDefinition;
import com.lemelson.visualizer.core.grid.ViewWindow;
import com.lemelson.visualizer.core.grid.ViewWindowLimiter;
import com.lemelson.visualizer.core.optimization.AgentSnapshot;
import com.lemelson.visualizer.core.optimization.Constraint;
import com.lemelson.visualizer.core.optimization.DifferentialEvolutionOptimizer;
import com.lemelson.visualizer.core.optimization.ExpressionConstraintParser;
import com.lemelson.visualizer.core.optimization.IterationSnapshot;
import com.lemelson.visualizer.core.optimization.OptimizationHistoryDocument;
import com.lemelson.visualizer.core.optimization.OptimizationHistoryJsonWriter;
import com.lemelson.visualizer.core.optimization.OptimizationProblem;
import com.lemelson.visualizer.core.optimization.OptimizationRunResult;
import com.lemelson.visualizer.core.optimization.QuadraticPenaltyStrategy;
import com.lemelson.visualizer.render.HeatmapRenderResult;
import com.lemelson.visualizer.render.PngExporter;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.concurrent.CancellationException;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory.DoubleSpinnerValueFactory;
import javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import javafx.util.StringConverter;

public class MainController {

    private static final int ISOLINE_LEVELS = 10;
    private static final int COLOR_BAR_WIDTH = 24;
    private static final int COLOR_BAR_LABEL_COUNT = 6;

    private static final DecimalFormat NUMBER_FORMAT = new DecimalFormat("0.###");

    private final BorderPane root = new BorderPane();
    private final ToggleGroup functionModeGroup = new ToggleGroup();
    private final RadioButton builtInRadio = new RadioButton("Из библиотеки");
    private final RadioButton customRadio = new RadioButton("Своя функция");
    private final ChoiceBox<BenchmarkFunctionDefinition> functionChoice = new ChoiceBox<>();
    private final Spinner<Integer> dimensionSpinner = new Spinner<>();
    private final TextField expressionField = new TextField();
    private final Spinner<Integer> customDimensionSpinner = new Spinner<>();
    private final Spinner<Double> customLowerSpinner = new Spinner<>();
    private final Spinner<Double> customUpperSpinner = new Spinner<>();
    private final Button applyCustomButton = new Button("Применить");
    private final ChoiceBox<Integer> xAxisChoice = new ChoiceBox<>();
    private final ChoiceBox<Integer> yAxisChoice = new ChoiceBox<>();
    private final Spinner<Double> xminSpinner = new Spinner<>();
    private final Spinner<Double> xmaxSpinner = new Spinner<>();
    private final Spinner<Double> yminSpinner = new Spinner<>();
    private final Spinner<Double> ymaxSpinner = new Spinner<>();
    private final Spinner<Integer> nxSpinner = new Spinner<>();
    private final Spinner<Integer> nySpinner = new Spinner<>();
    private final ChoiceBox<PaletteType> paletteChoice = new ChoiceBox<>();
    private final Button recomputeButton = new Button("⟳ Пересчитать");
    private final Button runDeButton = new Button("▶ Запуск DE");
    private final Button applyConstraintsButton = new Button("Применить ограничения");
    private final Button exportButton = new Button("📷 Экспорт PNG");
    private final Button exportHistoryButton = new Button("🧾 Экспорт JSON");
    private final Button resetViewButton = new Button("⊡ Сброс вида");
    private final Spinner<Integer> dePopulationSpinner = new Spinner<>();
    private final Spinner<Integer> deIterationsSpinner = new Spinner<>();
    private final Spinner<Double> deCrSpinner = new Spinner<>();
    private final Spinner<Double> deFSpinner = new Spinner<>();
    private final Spinner<Double> equalityToleranceSpinner = new Spinner<>();
    private final Spinner<Double> constraintPenaltySpinner = new Spinner<>();
    private final Slider timelineSlider = new Slider(0, 0, 0);
    private final TextArea constraintsArea = new TextArea();
    private final Label statusLabel = new Label("Готово");
    private final Label functionInfoLabel = new Label("");
    private final Label optimizerInfoLabel = new Label("");
    private final Label constraintInfoLabel = new Label("");
    private final Label coordLabel = new Label("");
    private final Label timelineLabel = new Label("Таймлайн: запуск ещё не выполнен");
    private final ProgressIndicator progressIndicator = new ProgressIndicator();
    private final VBox dimensionValuesBox = new VBox(6);
    private final ScrollPane dimensionScroll = new ScrollPane(dimensionValuesBox);

    private final Canvas landscapeCanvas = new Canvas();
    private final Canvas agentOverlayCanvas = new Canvas();
    private final Canvas minimapCanvas = new Canvas(180, 130);

    private final Canvas colorBarCanvas = new Canvas(COLOR_BAR_WIDTH, 256);
    private final Label colorBarMaxLabel = new Label("");
    private final Label colorBarMinLabel = new Label("");

    private StackPane previewViewport;
    private StackPane imageLayer;
    private double dragStartX;
    private double dragStartY;
    private boolean dragging;

    private final ComputeService computeService = new ComputeService();
    private final PngExporter pngExporter = new PngExporter();
    private final MarchingSquares marchingSquares = new MarchingSquares();
    private final OptimizationHistoryJsonWriter historyJsonWriter = new OptimizationHistoryJsonWriter();
    private final ExpressionConstraintParser constraintParser = new ExpressionConstraintParser();
    private final Debouncer debouncer = new Debouncer(350);
    private final ExecutorService computeExecutor = Executors.newFixedThreadPool(
            Math.max(4, Runtime.getRuntime().availableProcessors()));

    private final BooleanProperty computing = new SimpleBooleanProperty(false);
    private final StringProperty lastError = new SimpleStringProperty();
    private final SimpleDoubleProperty zoom = new SimpleDoubleProperty(1.0);
    private final AtomicBoolean bootstrapDone = new AtomicBoolean(false);
    private final AtomicLong computeGeneration = new AtomicLong();
    private final AtomicLong minimapGeneration = new AtomicLong();

    private BenchmarkFunctionDefinition currentDefinition;
    private BenchmarkFunction currentFunction;
    private double[] dimensionValues = new double[0];
    private final List<Spinner<Double>> dimensionSpinners = new ArrayList<>();
    private boolean suppressRecompute;

    private Future<?> inFlightTask;
    private CompletableFuture<OptimizationRunResult> inFlightOptimizationTask;
    private double[][] lastGrid;
    private GridSpec lastSpec;
    private SliceDefinition lastSlice;
    private HeatmapRenderResult lastRender;
    private WritableImage lastConstraintMaskImage;
    private WritableImage lastIsolineImage;
    private OptimizationHistoryDocument lastOptimizationHistory;
    private List<Constraint> activeConstraints = List.of();
    private boolean constraintsValid = true;

    private HeatmapRenderResult minimapFullRender;
    private GridSpec minimapFullSpec;

    public MainController() {
        buildUi();
        wireEvents();
        initializeControls();
    }

    public Parent getRoot() {
        return root;
    }

    public void bootstrap() {
        if (bootstrapDone.compareAndSet(false, true)) {
            functionChoice.getSelectionModel().selectFirst();
        }
    }

    public void shutdown() {
        debouncer.close();
        computeExecutor.shutdownNow();
    }



    private void buildUi() {
        root.getStyleClass().add("main-root");
        root.setLeft(buildControls());
        root.setCenter(buildCenterArea());
        root.setBottom(buildStatusBar());
    }

    private Parent buildControls() {
        VBox container = new VBox(10);
        container.getStyleClass().add("control-panel");

        ScrollPane controlScroll = new ScrollPane(container);
        controlScroll.setFitToWidth(true);
        controlScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        controlScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        controlScroll.getStyleClass().add("control-panel");
        controlScroll.setMinWidth(390);
        controlScroll.setMaxWidth(420);

        builtInRadio.setToggleGroup(functionModeGroup);
        customRadio.setToggleGroup(functionModeGroup);
        builtInRadio.setSelected(true);

        HBox modeBox = new HBox(12, builtInRadio, customRadio);
        modeBox.setAlignment(Pos.CENTER_LEFT);

        functionChoice.setPrefWidth(280);
        functionChoice.setMaxWidth(Double.MAX_VALUE);
        Tooltip.install(functionChoice, createTooltip("Выберите тестовую функцию из библиотеки GWO (F1–F23)"));

        functionInfoLabel.setWrapText(true);
        functionInfoLabel.getStyleClass().add("function-info-label");
        functionInfoLabel.setMaxWidth(280);

        VBox functionBox = new VBox(6,
                functionChoice,
                buildDimensionRow(),
                functionInfoLabel);
        functionBox.visibleProperty().bind(builtInRadio.selectedProperty());
        functionBox.managedProperty().bind(builtInRadio.selectedProperty());

        VBox customBox = buildCustomFunctionControls();
        customBox.visibleProperty().bind(customRadio.selectedProperty());
        customBox.managedProperty().bind(customRadio.selectedProperty());

        VBox sourceSection = createSection("📐  ФУНКЦИЯ", modeBox, functionBox, customBox);

        dimensionScroll.setPrefHeight(180);
        dimensionScroll.setFitToWidth(true);
        dimensionScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        dimensionScroll.getStyleClass().add("dimension-scroll");

        VBox axesSection = createSection("📊  ОСИ И ДИАПАЗОНЫ",
                buildAxisGrid(),
                new Label("Фиксированные измерения"),
                dimensionScroll);

        paletteChoice.getItems().setAll(PaletteType.values());
        paletteChoice.getSelectionModel().selectFirst();
        paletteChoice.setMaxWidth(Double.MAX_VALUE);
        Tooltip.install(paletteChoice, createTooltip("Цветовая палитра для тепловой карты"));

        setupIntegerSpinner(nxSpinner, 32, 2048, 400, 32);
        setupIntegerSpinner(nySpinner, 32, 2048, 300, 32);
        Tooltip.install(nxSpinner, createTooltip("Разрешение сетки по оси X (больше = качественнее)"));
        Tooltip.install(nySpinner, createTooltip("Разрешение сетки по оси Y (больше = качественнее)"));

        HBox resolutionRow = new HBox(10,
                new Label("Nx"), nxSpinner,
                new Label("Ny"), nySpinner);
        resolutionRow.setAlignment(Pos.CENTER_LEFT);

        VBox vizSection = createSection("🎨  ВИЗУАЛИЗАЦИЯ",
                new HBox(10, new Label("Палитра"), paletteChoice),
                resolutionRow);

        constraintsArea.setPromptText("""
                x1^2 + x2^2 <= 4
                x1 - x2 = 0
                """);
        constraintsArea.setPrefRowCount(4);
        constraintsArea.getStyleClass().add("constraint-area");
        setupDoubleSpinner(equalityToleranceSpinner, 0.0, 1.0, 1e-4, 1e-4);
        setupDoubleSpinner(constraintPenaltySpinner, 1.0, 1_000_000_000.0, 1_000_000.0, 1000.0);
        applyConstraintsButton.getStyleClass().add("button-primary");
        applyConstraintsButton.setMaxWidth(Double.MAX_VALUE);
        constraintInfoLabel.getStyleClass().add("function-info-label");
        constraintInfoLabel.setWrapText(true);

        GridPane constraintGrid = new GridPane();
        constraintGrid.setHgap(10);
        constraintGrid.setVgap(6);
        constraintGrid.add(new Label("epsilon"), 0, 0);
        constraintGrid.add(equalityToleranceSpinner, 1, 0);
        constraintGrid.add(new Label("penalty"), 0, 1);
        constraintGrid.add(constraintPenaltySpinner, 1, 1);

        VBox constraintSection = createSection("⛳  CONSTRAINTS",
                new Label("Каждое ограничение на новой строке: <=, >=, ="),
                constraintsArea,
                constraintGrid,
                applyConstraintsButton,
                constraintInfoLabel);

        setupIntegerSpinner(dePopulationSpinner, 4, 500, 30, 1);
        setupIntegerSpinner(deIterationsSpinner, 1, 5000, 100, 10);
        setupDoubleSpinner(deCrSpinner, 0.0, 1.0, 0.9, 0.05);
        setupDoubleSpinner(deFSpinner, 0.0, 2.0, 0.7, 0.05);

        optimizerInfoLabel.setWrapText(true);
        optimizerInfoLabel.getStyleClass().add("function-info-label");

        runDeButton.getStyleClass().add("button-primary");
        runDeButton.setMaxWidth(Double.MAX_VALUE);
        Tooltip.install(runDeButton, createTooltip("Запустить Differential Evolution и сохранить всю историю итераций"));
        exportHistoryButton.setMaxWidth(Double.MAX_VALUE);
        Tooltip.install(exportHistoryButton, createTooltip("Сохранить последнюю history в JSON"));

        GridPane optimizerGrid = new GridPane();
        optimizerGrid.setHgap(10);
        optimizerGrid.setVgap(6);
        optimizerGrid.add(new Label("Популяция"), 0, 0);
        optimizerGrid.add(dePopulationSpinner, 1, 0);
        optimizerGrid.add(new Label("Итерации"), 0, 1);
        optimizerGrid.add(deIterationsSpinner, 1, 1);
        optimizerGrid.add(new Label("CR"), 0, 2);
        optimizerGrid.add(deCrSpinner, 1, 2);
        optimizerGrid.add(new Label("F"), 0, 3);
        optimizerGrid.add(deFSpinner, 1, 3);

        VBox optimizerSection = createSection("🧠  ОПТИМИЗАТОР",
                optimizerGrid,
                runDeButton,
                exportHistoryButton,
                optimizerInfoLabel);

        recomputeButton.getStyleClass().add("button-primary");
        recomputeButton.setMaxWidth(Double.MAX_VALUE);
        Tooltip.install(recomputeButton, createTooltip("Пересчитать тепловую карту (Enter)"));

        exportButton.getStyleClass().add("button-success");
        exportButton.setMaxWidth(Double.MAX_VALUE);
        Tooltip.install(exportButton, createTooltip("Сохранить текущее изображение в PNG-файл"));

        resetViewButton.setMaxWidth(Double.MAX_VALUE);
        Tooltip.install(resetViewButton, createTooltip("Вернуть масштаб 1:1 и сбросить сдвиг"));

        VBox buttons = new VBox(8, recomputeButton, new HBox(8, exportButton, resetViewButton));
        ((HBox) buttons.getChildren().get(1)).getChildren().forEach(b -> HBox.setHgrow(b, Priority.ALWAYS));

        container.getChildren().addAll(sourceSection, axesSection, vizSection, constraintSection, optimizerSection, buttons);
        return controlScroll;
    }

    private VBox createSection(String title, javafx.scene.Node... children) {
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("section-title");

        VBox section = new VBox(8);
        section.getStyleClass().add("section-card");
        section.getChildren().add(titleLabel);
        section.getChildren().addAll(children);
        return section;
    }

    private HBox buildDimensionRow() {
        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER_LEFT);
        dimensionSpinner.setEditable(true);
        Tooltip.install(dimensionSpinner, createTooltip("Количество измерений функции"));
        box.getChildren().addAll(new Label("Размерность n"), dimensionSpinner);
        return box;
    }

    private VBox buildCustomFunctionControls() {
        VBox box = new VBox(6);

        expressionField.setPromptText("sin(x1) * cos(x2)");
        Tooltip.install(expressionField, createTooltip(
                "Формула с переменными x1, x2, ...\nПоддерживаются: +, -, *, /, ^, sin, cos, tan, exp, log, sqrt, abs"));

        setupIntegerSpinner(customDimensionSpinner, 2, 50, 2, 1);
        setupCustomBoundSpinner(customLowerSpinner, -10.0);
        setupCustomBoundSpinner(customUpperSpinner, 10.0);

        HBox dimensionRow = new HBox(10, new Label("Размерность n"), customDimensionSpinner);
        dimensionRow.setAlignment(Pos.CENTER_LEFT);
        HBox lowerRow = new HBox(10, new Label("Нижняя граница"), customLowerSpinner);
        lowerRow.setAlignment(Pos.CENTER_LEFT);
        HBox upperRow = new HBox(10, new Label("Верхняя граница"), customUpperSpinner);
        upperRow.setAlignment(Pos.CENTER_LEFT);

        applyCustomButton.getStyleClass().add("button-primary");
        applyCustomButton.setMaxWidth(Double.MAX_VALUE);

        box.getChildren().addAll(
                new Label("Формула f(x₁,...,xₙ) ="),
                expressionField,
                dimensionRow,
                lowerRow,
                upperRow,
                applyCustomButton);
        return box;
    }

    private GridPane buildAxisGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(6);
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(40);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(60);
        grid.getColumnConstraints().setAll(col1, col2);

        Tooltip.install(xAxisChoice, createTooltip("Измерение, отображаемое по горизонтали"));
        Tooltip.install(yAxisChoice, createTooltip("Измерение, отображаемое по вертикали"));

        grid.add(new Label("Ось X"), 0, 0);
        grid.add(xAxisChoice, 1, 0);
        grid.add(rangeBox("X min", xminSpinner), 0, 1, 2, 1);
        grid.add(rangeBox("X max", xmaxSpinner), 0, 2, 2, 1);

        grid.add(new Label("Ось Y"), 0, 3);
        grid.add(yAxisChoice, 1, 3);
        grid.add(rangeBox("Y min", yminSpinner), 0, 4, 2, 1);
        grid.add(rangeBox("Y max", ymaxSpinner), 0, 5, 2, 1);

        return grid;
    }

    private HBox rangeBox(String label, Spinner<?> spinner) {
        HBox box = new HBox(8);
        box.setAlignment(Pos.CENTER_LEFT);
        spinner.setPrefWidth(140);
        box.getChildren().addAll(new Label(label), spinner);
        return box;
    }

    private Parent buildCenterArea() {

        imageLayer = new StackPane(landscapeCanvas, agentOverlayCanvas);
        imageLayer.setStyle("-fx-background-color: transparent;");
        landscapeCanvas.setMouseTransparent(true);
        agentOverlayCanvas.setMouseTransparent(true);

        coordLabel.getStyleClass().add("coord-tracker");
        coordLabel.setVisible(false);
        coordLabel.setMouseTransparent(true);

        minimapCanvas.getStyleClass().add("minimap-canvas");
        VBox minimapBox = new VBox(4, new Label("Minimap"), minimapCanvas);
        minimapBox.getStyleClass().add("minimap-box");
        minimapBox.setMouseTransparent(true);
        minimapBox.setMaxWidth(Region.USE_PREF_SIZE);
        minimapBox.setMaxHeight(Region.USE_PREF_SIZE);

        previewViewport = new StackPane(imageLayer, coordLabel, minimapBox);
        previewViewport.getStyleClass().add("preview-area");
        installClip(previewViewport);

        StackPane.setAlignment(coordLabel, Pos.TOP_LEFT);
        StackPane.setMargin(coordLabel, new Insets(10, 0, 0, 10));
        StackPane.setAlignment(minimapBox, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(minimapBox, new Insets(0, 16, 16, 0));

        previewViewport.layoutBoundsProperty().addListener((obs, oldBounds, newBounds) -> {
            double viewportWidth = newBounds.getWidth();
            double viewportHeight = newBounds.getHeight();
            fitImagesToViewport(viewportWidth, viewportHeight);
        });

        wirePreviewInteractions();

        VBox colorBar = buildColorBar();

        HBox center = new HBox(0, previewViewport, colorBar);
        HBox.setHgrow(previewViewport, Priority.ALWAYS);
        installClip(center);

        timelineSlider.setDisable(true);
        timelineSlider.setBlockIncrement(1);
        timelineSlider.setMajorTickUnit(1);
        timelineSlider.setMinorTickCount(0);
        timelineSlider.setSnapToTicks(true);
        timelineSlider.getStyleClass().add("timeline-slider");
        timelineLabel.getStyleClass().add("timeline-label");

        VBox timelineBox = new VBox(6, timelineLabel, timelineSlider);
        timelineBox.getStyleClass().add("timeline-box");

        VBox wrapper = new VBox(10, center, timelineBox);
        VBox.setVgrow(center, Priority.ALWAYS);
        installClip(wrapper);
        return wrapper;
    }

    private VBox buildColorBar() {
        colorBarMaxLabel.getStyleClass().add("color-bar-label");
        colorBarMinLabel.getStyleClass().add("color-bar-label");
        colorBarMaxLabel.setText("");
        colorBarMinLabel.setText("");

        VBox bar = new VBox(4);
        bar.getStyleClass().add("color-bar-container");
        bar.setAlignment(Pos.CENTER);
        bar.getChildren().addAll(colorBarMaxLabel, colorBarCanvas, colorBarMinLabel);

        for (int i = 0; i < COLOR_BAR_LABEL_COUNT; i++) {

        }

        return bar;
    }

    private void updateColorBar() {
        PaletteType pt = paletteChoice.getValue();
        if (pt == null || lastRender == null) {
            return;
        }
        Palette palette = pt.get();
        double min = lastRender.minValue();
        double max = lastRender.maxValue();
        if (!Double.isFinite(min) || !Double.isFinite(max)) {
            return;
        }

        int h = (int) colorBarCanvas.getHeight();
        int w = (int) colorBarCanvas.getWidth();
        GraphicsContext gc = colorBarCanvas.getGraphicsContext2D();

        for (int y = 0; y < h; y++) {
            double t = 1.0 - (double) y / (h - 1);
            int argb = palette.argb(t);
            int r = (argb >> 16) & 0xFF;
            int g = (argb >> 8) & 0xFF;
            int b = argb & 0xFF;
            gc.setFill(Color.rgb(r, g, b));
            gc.fillRect(0, y, w, 1);
        }

        colorBarMaxLabel.setText(formatNumber(max));
        colorBarMinLabel.setText(formatNumber(min));
    }

    private void fitImagesToViewport(double viewportWidth, double viewportHeight) {
        if (lastRender == null || lastRender.image() == null) {
            return;
        }
        if (viewportWidth <= 0 || viewportHeight <= 0) {
            return;
        }

        double fittedWidth = viewportWidth;
        double fittedHeight = viewportHeight;
        double contentWidth = viewportWidth;
        double contentHeight = viewportHeight;

        landscapeCanvas.setWidth(fittedWidth);
        landscapeCanvas.setHeight(fittedHeight);
        agentOverlayCanvas.setWidth(contentWidth);
        agentOverlayCanvas.setHeight(contentHeight);
        imageLayer.setMinSize(contentWidth, contentHeight);
        imageLayer.setPrefSize(contentWidth, contentHeight);
        imageLayer.setMaxSize(contentWidth, contentHeight);
        renderLandscapeCanvas();
        renderOptimizationOverlay();
        renderMinimap();
    }

    private Parent buildStatusBar() {
        HBox statusBar = new HBox(12);
        statusBar.getStyleClass().add("status-bar");
        statusBar.setAlignment(Pos.CENTER_LEFT);
        progressIndicator.setVisible(false);
        progressIndicator.setPrefSize(16, 16);
        HBox.setHgrow(statusLabel, Priority.ALWAYS);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        statusBar.getChildren().addAll(progressIndicator, statusLabel, spacer);
        return statusBar;
    }

    private void renderLandscapeCanvas() {
        GraphicsContext gc = landscapeCanvas.getGraphicsContext2D();
        double width = landscapeCanvas.getWidth();
        double height = landscapeCanvas.getHeight();
        gc.clearRect(0, 0, width, height);
        if (width <= 0.0 || height <= 0.0 || lastRender == null || lastRender.image() == null) {
            return;
        }

        gc.drawImage(lastRender.image(), 0, 0, width, height);
        if (lastConstraintMaskImage != null) {
            gc.drawImage(lastConstraintMaskImage, 0, 0, width, height);
        }
        if (lastIsolineImage != null) {
            gc.drawImage(lastIsolineImage, 0, 0, width, height);
        }
    }



    private void initializeControls() {
        functionChoice.setConverter(new StringConverter<>() {
            @Override
            public String toString(BenchmarkFunctionDefinition object) {
                if (object == null) {
                    return "";
                }
                return object.id() + " · " + object.displayName();
            }

            @Override
            public BenchmarkFunctionDefinition fromString(String string) {
                return null;
            }
        });
        List<BenchmarkFunctionDefinition> definitions = new ArrayList<>(BenchmarkLibrary.definitions());
        definitions.sort(Comparator.comparing(BenchmarkFunctionDefinition::id));
        functionChoice.setItems(FXCollections.observableArrayList(definitions));

        functionChoice.disableProperty().bind(customRadio.selectedProperty());
        dimensionSpinner.disableProperty().bind(customRadio.selectedProperty());
        expressionField.disableProperty().bind(builtInRadio.selectedProperty());
        customDimensionSpinner.disableProperty().bind(builtInRadio.selectedProperty());
        customLowerSpinner.disableProperty().bind(builtInRadio.selectedProperty());
        customUpperSpinner.disableProperty().bind(builtInRadio.selectedProperty());
        applyCustomButton.disableProperty().bind(builtInRadio.selectedProperty());

        xAxisChoice.setConverter(dimensionLabelConverter());
        yAxisChoice.setConverter(dimensionLabelConverter());

        progressIndicator.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
        optimizerInfoLabel.setText("DE готов к запуску после построения среза.");
        constraintInfoLabel.setText("Ограничения не заданы.");
        exportHistoryButton.setDisable(true);
        setInteractiveControlsEnabled(false);
        statusLabel.setText("Выберите функцию из библиотеки или задайте свою.");
    }

    private void wireEvents() {
        functionChoice.getSelectionModel().selectedItemProperty().addListener((obs, oldDef, newDef) -> {
            if (!builtInRadio.isSelected()) {
                return;
            }
            if (newDef != null) {
                onFunctionDefinitionSelected(newDef);
            }
        });

        dimensionSpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (!builtInRadio.isSelected()) {
                return;
            }
            if (newVal != null && currentDefinition != null) {
                onDimensionChanged(newVal);
            }
        });

        builtInRadio.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
            if (isSelected) {
                if (functionChoice.getValue() == null && !functionChoice.getItems().isEmpty()) {
                    functionChoice.getSelectionModel().selectFirst();
                } else if (functionChoice.getValue() != null) {
                    onFunctionDefinitionSelected(functionChoice.getValue());
                }
            }
        });

        customRadio.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
            if (isSelected) {
                clearCurrentFunction();
                statusLabel.setText("Введите формулу и нажмите «Применить».");
            }
        });

        applyCustomButton.setOnAction(e -> applyCustomFunction());

        xAxisChoice.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                ensureDistinctAxes();
                updateAxisConfigurations();
            }
        });
        yAxisChoice.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                ensureDistinctAxes();
                updateAxisConfigurations();
            }
        });

        addRangeListener(xminSpinner);
        addRangeListener(xmaxSpinner);
        addRangeListener(yminSpinner);
        addRangeListener(ymaxSpinner);
        nxSpinner.valueProperty().addListener((obs, oldVal, newVal) -> triggerRecompute());
        nySpinner.valueProperty().addListener((obs, oldVal, newVal) -> triggerRecompute());
        paletteChoice.getSelectionModel().selectedItemProperty().addListener((obs, old, neu) -> {
            if (lastGrid != null && lastRender != null) {
                applyPaletteOnly();
                computeMinimapFullDomain();
            }
        });

        recomputeButton.setOnAction(e -> triggerRecomputeImmediate());
        runDeButton.setOnAction(e -> runDifferentialEvolution());
        applyConstraintsButton.setOnAction(e -> rebuildConstraintsFromEditor(true));
        exportButton.setOnAction(e -> exportCurrentFrame());
        exportHistoryButton.setOnAction(e -> exportOptimizationHistory());
        resetViewButton.setOnAction(e -> resetView());
        timelineSlider.valueProperty().addListener((obs, oldVal, newVal) -> renderOptimizationOverlay());

        computing.addListener((obs, wasComputing, isComputing) -> progressIndicator.setVisible(isComputing));
        lastError.addListener((obs, oldErr, newErr) -> {
            if (newErr != null && !newErr.isBlank()) {
                statusLabel.setText(newErr);
            }
        });
    }

    private void addRangeListener(Spinner<Double> spinner) {
        spinner.valueProperty().addListener((obs, oldVal, newVal) -> triggerRecompute());
        spinner.focusedProperty().addListener((obs, oldVal, focused) -> {
            if (!focused) {
                commitEditorText(spinner);
            }
        });
    }

    private void commitEditorText(Spinner<Double> spinner) {
        if (!spinner.isEditable()) {
            return;
        }
        String text = spinner.getEditor().getText();
        Double value = ((DoubleSpinnerValueFactory) spinner.getValueFactory()).getConverter().fromString(text);
        spinner.getValueFactory().setValue(value);
    }



    private void applyCustomFunction() {
        if (!customRadio.isSelected()) {
            return;
        }
        String expression = expressionField.getText() != null ? expressionField.getText().trim() : "";
        if (expression.isEmpty()) {
            statusLabel.setText("Введите формулу.");
            return;
        }
        int dimension = customDimensionSpinner.getValue();
        double lower = customLowerSpinner.getValue();
        double upper = customUpperSpinner.getValue();
        try {
            BenchmarkFunction function = new ExpressionBenchmarkFunction(expression, dimension, lower, upper);
            currentDefinition = null;
            setFunction(function);
            statusLabel.setText("Пользовательская функция применена.");
        } catch (IllegalArgumentException ex) {
            statusLabel.setText("Ошибка формулы: " + ex.getMessage());
        }
    }

    private void setFunction(BenchmarkFunction function) {
        if (function == null) {
            clearCurrentFunction();
            return;
        }
        currentFunction = function;
        dimensionValues = new double[function.dimension()];
        double[] startPoint = function.globalMinimumPoint()
                .map(this::clampToBounds)
                .orElseGet(() -> defaultCenterPoint(function));
        System.arraycopy(startPoint, 0, dimensionValues, 0, dimensionValues.length);

        withSuppressedRecompute(() -> {
            rebuildDimensionControls();
            updateAxisChoices();
            updateAxisConfigurations();
        });

        updateFunctionInfo(function);
        rebuildConstraintsFromEditor(false);
        clearOptimizationHistory();
        computeMinimapFullDomain();

        setInteractiveControlsEnabled(true);
        triggerRecomputeImmediate();
    }

    private void updateFunctionInfo(BenchmarkFunction function) {
        if (function == null) {
            functionInfoLabel.setText("");
            return;
        }

        StringBuilder info = new StringBuilder();
        info.append("Глобальный минимум: ").append(formatNumber(function.globalMinimumValue()));

        function.globalMinimumPoint().ifPresent(point -> {
            info.append("\nТочка минимума: (");
            for (int i = 0; i < Math.min(point.length, 4); i++) {
                if (i > 0)
                    info.append(", ");
                info.append(formatNumber(point[i]));
            }
            if (point.length > 4) {
                info.append(", ...");
            }
            info.append(")");
        });

        functionInfoLabel.setText(info.toString());
    }

    private void clearCurrentFunction() {
        computeGeneration.incrementAndGet();
        minimapGeneration.incrementAndGet();
        cancelInFlightComputation();
        currentFunction = null;
        dimensionValues = new double[0];
        dimensionValuesBox.getChildren().clear();
        dimensionSpinners.clear();
        xAxisChoice.getItems().clear();
        yAxisChoice.getItems().clear();
        lastGrid = null;
        lastSpec = null;
        lastSlice = null;
        lastRender = null;
        lastConstraintMaskImage = null;
        lastIsolineImage = null;
        minimapFullRender = null;
        minimapFullSpec = null;
        landscapeCanvas.getGraphicsContext2D().clearRect(0, 0, landscapeCanvas.getWidth(), landscapeCanvas.getHeight());
        clearOptimizationHistory();
        functionInfoLabel.setText("");
        coordLabel.setVisible(false);
        activeConstraints = List.of();
        constraintsValid = true;
        setInteractiveControlsEnabled(false);
        statusLabel.setText("Функция не выбрана.");
    }

    private void setInteractiveControlsEnabled(boolean enabled) {
        xAxisChoice.setDisable(!enabled);
        yAxisChoice.setDisable(!enabled);
        xminSpinner.setDisable(!enabled);
        xmaxSpinner.setDisable(!enabled);
        yminSpinner.setDisable(!enabled);
        ymaxSpinner.setDisable(!enabled);
        nxSpinner.setDisable(!enabled);
        nySpinner.setDisable(!enabled);
        paletteChoice.setDisable(!enabled);
        dimensionValuesBox.setDisable(!enabled);
        recomputeButton.setDisable(!enabled);
        runDeButton.setDisable(!enabled);
        applyConstraintsButton.setDisable(!enabled);
        resetViewButton.setDisable(!enabled);
        dePopulationSpinner.setDisable(!enabled);
        deIterationsSpinner.setDisable(!enabled);
        deCrSpinner.setDisable(!enabled);
        deFSpinner.setDisable(!enabled);
        constraintsArea.setDisable(!enabled);
        equalityToleranceSpinner.setDisable(!enabled);
        constraintPenaltySpinner.setDisable(!enabled);
    }

    private void withSuppressedRecompute(Runnable action) {
        boolean previous = suppressRecompute;
        suppressRecompute = true;
        try {
            action.run();
        } finally {
            suppressRecompute = previous;
        }
    }

    private void onFunctionDefinitionSelected(BenchmarkFunctionDefinition definition) {
        currentDefinition = definition;
        int minDimension = Math.max(definition.minDimension(), 2);
        int maxDimension = Math.max(definition.maxDimension(), minDimension);
        int defaultDimension = Math.max(definition.defaultDimension(), minDimension);
        setupIntegerSpinner(dimensionSpinner, minDimension, maxDimension, defaultDimension, 1);
        if (builtInRadio.isSelected()) {
            applyDimension(defaultDimension);
        }
    }

    private void onDimensionChanged(int newDimension) {
        if (!builtInRadio.isSelected() || currentDefinition == null) {
            return;
        }
        if (currentFunction != null && currentFunction.dimension() == newDimension) {
            return;
        }
        applyDimension(newDimension);
    }

    private void applyDimension(int dimension) {
        BenchmarkFunction function = currentDefinition.create(dimension);
        setFunction(function);
    }



    private void rebuildDimensionControls() {
        dimensionValuesBox.getChildren().clear();
        dimensionSpinners.clear();
        if (currentFunction == null) {
            return;
        }
        for (int i = 0; i < currentFunction.dimension(); i++) {
            HBox row = buildDimensionValueRow(i);
            dimensionValuesBox.getChildren().add(row);
        }
    }

    private HBox buildDimensionValueRow(int index) {
        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);
        double min = currentFunction.lowerBound(index);
        double max = currentFunction.upperBound(index);
        double initial = clamp(dimensionValues[index], min, max);
        dimensionValues[index] = initial;

        Spinner<Double> spinner = new Spinner<>();
        DoubleSpinnerValueFactory factory = new DoubleSpinnerValueFactory(min, max, initial, stepForRange(min, max));
        factory.setConverter(new NumberStringConverter());
        spinner.setValueFactory(factory);
        spinner.setEditable(true);
        int idx = index;
        spinner.valueProperty().addListener((obs, oldVal, newVal) -> {
            dimensionValues[idx] = newVal;
            if (!isAxisDimension(idx)) {
                computeMinimapFullDomain();
                triggerRecompute();
            }
        });
        spinner.focusedProperty().addListener((obs, oldVal, focused) -> {
            if (!focused) {
                commitEditorText(spinner);
            }
        });
        dimensionSpinners.add(spinner);

        Label label = new Label(dimensionLabel(index));
        label.getStyleClass().add("field-label");
        row.getChildren().addAll(label, spinner);
        return row;
    }

    private void updateAxisChoices() {
        if (currentFunction == null) {
            xAxisChoice.getItems().clear();
            yAxisChoice.getItems().clear();
            return;
        }
        List<Integer> dims = new ArrayList<>();
        for (int i = 0; i < currentFunction.dimension(); i++) {
            dims.add(i);
        }
        xAxisChoice.setItems(FXCollections.observableArrayList(dims));
        yAxisChoice.setItems(FXCollections.observableArrayList(dims));
        if (currentFunction.dimension() >= 2) {
            xAxisChoice.getSelectionModel().select(0);
            yAxisChoice.getSelectionModel().select(1);
        } else {
            xAxisChoice.getSelectionModel().select(0);
            yAxisChoice.getSelectionModel().select(0);
        }
    }

    private void ensureDistinctAxes() {
        if (currentFunction == null) {
            return;
        }
        Integer x = xAxisChoice.getValue();
        Integer y = yAxisChoice.getValue();
        if (x == null || y == null) {
            return;
        }
        if (Objects.equals(x, y)) {
            int replacement = (y + 1) % currentFunction.dimension();
            if (replacement == x) {
                replacement = (replacement + 1) % currentFunction.dimension();
            }
            yAxisChoice.getSelectionModel().select(replacement);
        }
    }

    private void updateAxisConfigurations() {
        if (currentFunction == null) {
            return;
        }
        updateAxisRange(xAxisChoice, xminSpinner, xmaxSpinner);
        updateAxisRange(yAxisChoice, yminSpinner, ymaxSpinner);
        updateDimensionSpinnerState();
        clearOptimizationHistory();
        computeMinimapFullDomain();
        triggerRecompute();
    }

    private void updateAxisRange(ChoiceBox<Integer> axisChoice, Spinner<Double> minSpinner,
            Spinner<Double> maxSpinner) {
        Integer dim = axisChoice.getValue();
        if (currentFunction == null || dim == null) {
            return;
        }
        double lower = currentFunction.lowerBound(dim);
        double upper = currentFunction.upperBound(dim);

        updateRangeSpinner(minSpinner,
                ViewWindowLimiter.allowedLowerBound(currentFunction, dim),
                ViewWindowLimiter.allowedUpperBound(currentFunction, dim),
                lower);
        updateRangeSpinner(maxSpinner,
                ViewWindowLimiter.allowedLowerBound(currentFunction, dim),
                ViewWindowLimiter.allowedUpperBound(currentFunction, dim),
                upper);

        if (minSpinner.getValue() >= maxSpinner.getValue()) {
            minSpinner.getValueFactory().setValue(lower);
            maxSpinner.getValueFactory().setValue(upper);
        }

        dimensionValues[dim] = clamp(dimensionValues[dim], lower, upper);
        if (dim < dimensionSpinners.size()) {
            dimensionSpinners.get(dim).getValueFactory().setValue(dimensionValues[dim]);
        }
    }

    private void updateDimensionSpinnerState() {
        if (currentFunction == null) {
            return;
        }
        for (int i = 0; i < dimensionSpinners.size(); i++) {
            dimensionSpinners.get(i).setDisable(isAxisDimension(i));
        }
    }

    private boolean isAxisDimension(int index) {
        if (currentFunction == null) {
            return false;
        }
        Integer x = xAxisChoice.getValue();
        Integer y = yAxisChoice.getValue();
        return (x != null && index == x) || (y != null && index == y);
    }

    private void updateRangeSpinner(Spinner<Double> spinner, double min, double max, double defaultValue) {
        double step = stepForRange(min, max);
        DoubleSpinnerValueFactory factory = new DoubleSpinnerValueFactory(min, max,
                clamp(spinner.getValue() != null ? spinner.getValue() : defaultValue, min, max), step);
        factory.setConverter(new NumberStringConverter());
        spinner.setValueFactory(factory);
        spinner.setEditable(true);
    }



    private void triggerRecompute() {
        if (suppressRecompute || currentFunction == null) {
            return;
        }
        debouncer.submit(this::startComputation);
    }

    private void triggerRecomputeImmediate() {
        if (suppressRecompute || currentFunction == null) {
            return;
        }
        debouncer.flushAndRun(this::startComputation);
    }

    private void startComputation() {
        BenchmarkFunction function = currentFunction;
        if (function == null) {
            return;
        }
        Integer xDim = xAxisChoice.getValue();
        Integer yDim = yAxisChoice.getValue();
        if (xDim == null || yDim == null || Objects.equals(xDim, yDim)) {
            return;
        }
        GridSpec spec;
        try {
            spec = new GridSpec(
                    xminSpinner.getValue(),
                    xmaxSpinner.getValue(),
                    yminSpinner.getValue(),
                    ymaxSpinner.getValue(),
                    nxSpinner.getValue(),
                    nySpinner.getValue());
        } catch (IllegalArgumentException ex) {
            Platform.runLater(() -> statusLabel.setText("Ошибка диапазона: " + ex.getMessage()));
            return;
        }

        double[] basePoint = dimensionValues.clone();
        SliceDefinition slice = new SliceDefinition(function, xDim, yDim, basePoint);

        long generation = computeGeneration.incrementAndGet();
        cancelInFlightComputation();
        computing.set(true);
        statusLabel.setText("Вычисление...");

        Future<?> task = computeExecutor.submit(() -> {
            try {
                ComputeService.ComputeResult result = computeSlice(slice, spec);
                Platform.runLater(() -> {
                    if (computeGeneration.get() != generation) {
                        return;
                    }
                    inFlightTask = null;
                    computing.set(false);
                    applyResult(result);
                });
            } catch (CancellationException ignored) {
                Platform.runLater(() -> {
                    if (computeGeneration.get() != generation) {
                        return;
                    }
                    inFlightTask = null;
                    computing.set(false);
                    statusLabel.setText("Вычисление отменено.");
                });
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    if (computeGeneration.get() != generation) {
                        return;
                    }
                    inFlightTask = null;
                    computing.set(false);
                    statusLabel.setText("Ошибка: " + ex.getMessage());
                });
            }
        });
        inFlightTask = task;
    }

    private ComputeService.ComputeResult computeSlice(SliceDefinition slice, GridSpec spec) {
        PaletteType palette = paletteChoice.getValue();
        if (palette == null) {
            palette = PaletteType.VIRIDIS;
        }
        return computeService.computeAndRender(slice, spec, palette.get(), computeExecutor);
    }

    private WritableImage createIsolineImage(double[][] grid, GridSpec spec, HeatmapRenderResult render) {
        double width = render.image().getWidth();
        double height = render.image().getHeight();
        double min = render.minValue();
        double max = render.maxValue();
        if (!Double.isFinite(min) || !Double.isFinite(max) || min == max) {
            return null;
        }
        Canvas canvas = new Canvas(width, height);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(1.0);
        for (int levelIndex = 1; levelIndex <= ISOLINE_LEVELS; levelIndex++) {
            double alpha = (double) levelIndex / (ISOLINE_LEVELS + 1);
            double level = min + alpha * (max - min);
            List<List<MarchingSquares.Point>> segments = marchingSquares.compute(grid, level, spec);
            gc.setGlobalAlpha(0.6);
            for (List<MarchingSquares.Point> segment : segments) {
                if (segment.size() < 2) {
                    continue;
                }
                MarchingSquares.Point p1 = segment.get(0);
                MarchingSquares.Point p2 = segment.get(1);
                double x1 = mapToPixel(p1.x(), spec.xmin(), spec.xmax(), width);
                double y1 = mapToPixel(p1.y(), spec.ymin(), spec.ymax(), height);
                double x2 = mapToPixel(p2.x(), spec.xmin(), spec.xmax(), width);
                double y2 = mapToPixel(p2.y(), spec.ymin(), spec.ymax(), height);
                gc.strokeLine(x1, height - y1, x2, height - y2);
            }
        }
        WritableImage img = new WritableImage((int) Math.round(width), (int) Math.round(height));
        SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.TRANSPARENT);
        canvas.snapshot(params, img);
        return img;
    }

    private double mapToPixel(double value, double min, double max, double size) {
        return (value - min) / (max - min) * (size - 1);
    }

    private void applyResult(ComputeService.ComputeResult result) {
        this.lastSlice = result.slice();
        this.lastSpec = result.spec();
        this.lastGrid = result.grid();
        this.lastRender = result.render();
        this.lastIsolineImage = createIsolineImage(result.grid(), result.spec(), result.render());

        Bounds viewport = previewViewport.getLayoutBounds();
        if (viewport != null && viewport.getWidth() > 0.0 && viewport.getHeight() > 0.0) {
            fitImagesToViewport(viewport.getWidth(), viewport.getHeight());
        } else {
            renderLandscapeCanvas();
        }

        updateColorBar();
        renderConstraintMask();
        renderOptimizationOverlay();

        statusLabel.setText(String.format("Сетка %d×%d • %d мс • Диапазон [%s, %s]",
                result.spec().nx(), result.spec().ny(), result.elapsedMs(),
                formatNumber(result.render().minValue()), formatNumber(result.render().maxValue())));
    }

    private void applyPaletteOnly() {
        if (lastGrid == null || lastSpec == null || lastSlice == null) {
            return;
        }
        PaletteType palette = paletteChoice.getValue();
        if (palette == null) {
            palette = PaletteType.VIRIDIS;
        }
        HeatmapRenderResult rerender = computeService.rerenderWithPalette(lastGrid, palette.get());
        lastRender = rerender;
        renderLandscapeCanvas();
        updateColorBar();
        renderConstraintMask();
        renderOptimizationOverlay();
        statusLabel.setText(String.format("Диапазон значений [%s, %s]",
                formatNumber(rerender.minValue()), formatNumber(rerender.maxValue())));
    }



    private void runDifferentialEvolution() {
        if (currentFunction == null || lastSpec == null || lastSlice == null) {
            statusLabel.setText("Сначала постройте срез функции.");
            return;
        }
        if (computing.get()) {
            statusLabel.setText("Дождитесь завершения текущего вычисления.");
            return;
        }
        if (!constraintsValid) {
            statusLabel.setText("Исправьте ошибки в ограничениях перед запуском.");
            return;
        }

        OptimizationProblem problem = createOptimizationProblem();
        DifferentialEvolutionOptimizer optimizer = new DifferentialEvolutionOptimizer(
                dePopulationSpinner.getValue(),
                deIterationsSpinner.getValue(),
                deCrSpinner.getValue(),
                deFSpinner.getValue(),
                42L);

        computing.set(true);
        statusLabel.setText("Запуск Differential Evolution...");
        optimizerInfoLabel.setText("DE выполняется, история собирается по каждой итерации.");

        CompletableFuture<OptimizationRunResult> task = CompletableFuture.supplyAsync(
                () -> optimizer.optimize(problem), computeExecutor);
        inFlightOptimizationTask = task;
        task.whenComplete((result, error) -> Platform.runLater(() -> {
            if (inFlightOptimizationTask != task) {
                return;
            }
            computing.set(false);
            if (error != null) {
                statusLabel.setText("Ошибка оптимизации: " + error.getMessage());
                optimizerInfoLabel.setText("DE завершился с ошибкой.");
            } else {
                applyOptimizationResult(result);
            }
        }));
    }

    private void applyOptimizationResult(OptimizationRunResult result) {
        lastOptimizationHistory = result.history();
        int maxIteration = Math.max(0, result.history().iterations().size() - 1);
        timelineSlider.setDisable(false);
        timelineSlider.setMin(0);
        timelineSlider.setMax(maxIteration);
        timelineSlider.setValue(maxIteration);
        timelineSlider.setMajorTickUnit(Math.max(1, maxIteration / 10.0));
        exportHistoryButton.setDisable(false);

        AgentSnapshot bestAgent = result.bestAgent();
        optimizerInfoLabel.setText(String.format(
                "DE: %d снимков • best f=%s • penalized=%s • feasible=%s",
                result.history().iterations().size(),
                formatNumber(bestAgent.objectiveValue()),
                formatNumber(bestAgent.penalizedFitness()),
                bestAgent.feasible() ? "yes" : "no"));
        renderOptimizationOverlay();
        statusLabel.setText(String.format(
                "DE завершён • %d итераций • best=%s",
                maxIteration,
                formatNumber(bestAgent.penalizedFitness())));
    }

    private void clearOptimizationHistory() {
        lastOptimizationHistory = null;
        timelineSlider.setDisable(true);
        timelineSlider.setMin(0);
        timelineSlider.setMax(0);
        timelineSlider.setValue(0);
        exportHistoryButton.setDisable(true);
        timelineLabel.setText("Таймлайн: запуск ещё не выполнен");
        GraphicsContext gc = agentOverlayCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, agentOverlayCanvas.getWidth(), agentOverlayCanvas.getHeight());
        optimizerInfoLabel.setText("DE готов к запуску после построения среза.");
        renderMinimap();
    }

    private void renderOptimizationOverlay() {
        GraphicsContext gc = agentOverlayCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, agentOverlayCanvas.getWidth(), agentOverlayCanvas.getHeight());

        if (lastOptimizationHistory == null || lastOptimizationHistory.iterations().isEmpty()
                || lastSlice == null || lastSpec == null || lastRender == null || lastRender.image() == null) {
            return;
        }

        int iterationIndex = clamp((int) Math.round(timelineSlider.getValue()), 0,
                lastOptimizationHistory.iterations().size() - 1);
        IterationSnapshot iteration = lastOptimizationHistory.iterations().get(iterationIndex);
        double imgW = landscapeCanvas.getWidth();
        double imgH = landscapeCanvas.getHeight();
        if (imgW <= 0.0 || imgH <= 0.0) {
            return;
        }

        double imgLeft = 0.0;
        double imgTop = 0.0;
        drawAgentTrails(gc, imgLeft, imgTop, imgW, imgH, iterationIndex);
        drawAgents(gc, iteration, imgLeft, imgTop, imgW, imgH);

        timelineLabel.setText(String.format(
                "Таймлайн: итерация %d / %d • best=%s • feasible best=%s",
                iteration.iteration(),
                lastOptimizationHistory.iterations().size() - 1,
                formatNumber(iteration.bestPenalizedFitness()),
                iteration.agents().get(iteration.bestAgentIndex()).feasible() ? "yes" : "no"));
        renderMinimap();
    }

    private void drawAgentTrails(GraphicsContext gc, double imgLeft, double imgTop, double imgW, double imgH,
            int iterationIndex) {
        if (iterationIndex < 1) {
            return;
        }
        int agentCount = lastOptimizationHistory.iterations().get(0).agents().size();
        gc.setLineWidth(1.2);
        for (int agentIndex = 0; agentIndex < agentCount; agentIndex++) {
            gc.setStroke(Color.rgb(137, 180, 250, 0.18));
            for (int step = 1; step <= iterationIndex; step++) {
                AgentSnapshot previous = lastOptimizationHistory.iterations().get(step - 1).agents().get(agentIndex);
                AgentSnapshot current = lastOptimizationHistory.iterations().get(step).agents().get(agentIndex);
                double x1 = toOverlayX(previous.position()[lastSlice.xIndex()], imgLeft, imgW);
                double y1 = toOverlayY(previous.position()[lastSlice.yIndex()], imgTop, imgH);
                double x2 = toOverlayX(current.position()[lastSlice.xIndex()], imgLeft, imgW);
                double y2 = toOverlayY(current.position()[lastSlice.yIndex()], imgTop, imgH);
                gc.strokeLine(x1, y1, x2, y2);
            }
        }
    }

    private void drawAgents(GraphicsContext gc, IterationSnapshot iteration, double imgLeft, double imgTop,
            double imgW, double imgH) {
        for (int i = 0; i < iteration.agents().size(); i++) {
            AgentSnapshot agent = iteration.agents().get(i);
            double x = toOverlayX(agent.position()[lastSlice.xIndex()], imgLeft, imgW);
            double y = toOverlayY(agent.position()[lastSlice.yIndex()], imgTop, imgH);

            gc.setFill(agent.feasible() ? Color.rgb(166, 227, 161, 0.95) : Color.rgb(243, 139, 168, 0.95));
            gc.fillOval(x - 4.0, y - 4.0, 8.0, 8.0);

            gc.setStroke(Color.rgb(24, 24, 37, 0.9));
            gc.setLineWidth(1.0);
            gc.strokeOval(x - 4.0, y - 4.0, 8.0, 8.0);

            if (i == iteration.bestAgentIndex()) {
                gc.setStroke(Color.rgb(249, 226, 175, 0.95));
                gc.setLineWidth(2.0);
                gc.strokeOval(x - 7.0, y - 7.0, 14.0, 14.0);
            }
        }
    }

    private double toOverlayX(double xValue, double imgLeft, double imgW) {
        double relX = (xValue - lastSpec.xmin()) / (lastSpec.xmax() - lastSpec.xmin());
        return imgLeft + relX * imgW;
    }

    private double toOverlayY(double yValue, double imgTop, double imgH) {
        double relY = (lastSpec.ymax() - yValue) / (lastSpec.ymax() - lastSpec.ymin());
        return imgTop + relY * imgH;
    }

    private void rebuildConstraintsFromEditor(boolean showSuccessStatus) {
        if (currentFunction == null) {
            activeConstraints = List.of();
            constraintsValid = true;
            lastConstraintMaskImage = null;
            constraintInfoLabel.setText("Ограничения не заданы.");
            return;
        }
        try {
            activeConstraints = constraintParser.parse(
                    constraintsArea.getText(),
                    currentFunction.dimension(),
                    equalityToleranceSpinner.getValue());
            constraintsValid = true;
            constraintInfoLabel.setText(activeConstraints.isEmpty()
                    ? "Ограничения не заданы."
                    : "Активно ограничений: " + activeConstraints.size());
            if (showSuccessStatus) {
                statusLabel.setText(activeConstraints.isEmpty()
                        ? "Ограничения очищены."
                        : "Ограничения успешно применены.");
            }
        } catch (IllegalArgumentException ex) {
            activeConstraints = List.of();
            constraintsValid = false;
            constraintInfoLabel.setText("Ошибка ограничения: " + ex.getMessage());
            statusLabel.setText("Ошибка ограничения: " + ex.getMessage());
        }
        clearOptimizationHistory();
        renderConstraintMask();
    }

    private OptimizationProblem createOptimizationProblem() {
        return new OptimizationProblem(
                currentFunction,
                activeConstraints,
                new QuadraticPenaltyStrategy(constraintPenaltySpinner.getValue()));
    }

    private void renderConstraintMask() {
        if (lastSpec == null || lastSlice == null || lastRender == null || activeConstraints.isEmpty()) {
            lastConstraintMaskImage = null;
            renderLandscapeCanvas();
            renderMinimap();
            return;
        }
        lastConstraintMaskImage = createConstraintMaskImage(lastSpec, lastSlice);
        renderLandscapeCanvas();
        renderMinimap();
    }

    private WritableImage createConstraintMaskImage(GridSpec spec, SliceDefinition slice) {
        WritableImage mask = new WritableImage(spec.nx(), spec.ny());
        PixelWriter writer = mask.getPixelWriter();
        int argb = ((int) Math.round(0.72 * 255) << 24) | (245 << 16) | (245 << 8) | 250;
        double[] point = slice.createPointTemplate();
        for (int row = 0; row < spec.ny(); row++) {
            point[slice.yIndex()] = spec.yAt(row);
            for (int col = 0; col < spec.nx(); col++) {
                point[slice.xIndex()] = spec.xAt(col);
                writer.setArgb(col, spec.ny() - 1 - row, violatesConstraints(point) ? argb : 0x00000000);
            }
        }
        return mask;
    }

    private boolean violatesConstraints(double[] point) {
        for (Constraint constraint : activeConstraints) {
            if (!constraint.isSatisfied(point)) {
                return true;
            }
        }
        return false;
    }

    private void computeMinimapFullDomain() {
        BenchmarkFunction func = currentFunction;
        long generation = minimapGeneration.incrementAndGet();
        if (func == null) {
            minimapFullRender = null;
            minimapFullSpec = null;
            return;
        }
        Integer xDim = xAxisChoice.getValue();
        Integer yDim = yAxisChoice.getValue();
        if (xDim == null || yDim == null) {
            return;
        }
        double xLower = ViewWindowLimiter.allowedLowerBound(func, xDim);
        double xUpper = ViewWindowLimiter.allowedUpperBound(func, xDim);
        double yLower = ViewWindowLimiter.allowedLowerBound(func, yDim);
        double yUpper = ViewWindowLimiter.allowedUpperBound(func, yDim);
        try {
            GridSpec fullSpec = new GridSpec(xLower, xUpper, yLower, yUpper, 120, 90);
            double[] basePoint = dimensionValues.clone();
            SliceDefinition fullSlice = new SliceDefinition(func, xDim, yDim, basePoint);
            PaletteType pt = paletteChoice.getValue();
            Palette palette = (pt != null ? pt : PaletteType.VIRIDIS).get();
            CompletableFuture.supplyAsync(
                    () -> computeService.computeAndRender(fullSlice, fullSpec, palette, computeExecutor),
                    computeExecutor
            ).thenAccept(result -> Platform.runLater(() -> {
                if (minimapGeneration.get() != generation) {
                    return;
                }
                minimapFullRender = result.render();
                minimapFullSpec = result.spec();
                renderMinimap();
            }));
        } catch (IllegalArgumentException ignored) {

        }
    }

    private void renderMinimap() {
        GraphicsContext gc = minimapCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, minimapCanvas.getWidth(), minimapCanvas.getHeight());

        HeatmapRenderResult mapRender = minimapFullRender != null ? minimapFullRender : lastRender;
        GridSpec mapSpec = minimapFullSpec != null ? minimapFullSpec : lastSpec;

        if (mapRender == null || mapRender.image() == null) {
            return;
        }

        double canvasW = minimapCanvas.getWidth();
        double canvasH = minimapCanvas.getHeight();
        double sourceW = mapRender.image().getWidth();
        double sourceH = mapRender.image().getHeight();
        double scale = Math.min(canvasW / sourceW, canvasH / sourceH);
        double drawW = sourceW * scale;
        double drawH = sourceH * scale;
        double drawX = (canvasW - drawW) / 2.0;
        double drawY = (canvasH - drawH) / 2.0;

        gc.setFill(Color.rgb(17, 17, 27, 0.95));
        gc.fillRoundRect(0, 0, canvasW, canvasH, 10, 10);
        gc.drawImage(mapRender.image(), drawX, drawY, drawW, drawH);

        renderMinimapAgents(gc, mapSpec, drawX, drawY, drawW, drawH);
        renderMinimapViewport(gc, mapSpec, drawX, drawY, drawW, drawH);
    }

    private void renderMinimapAgents(GraphicsContext gc, GridSpec mapSpec,
            double drawX, double drawY, double drawW, double drawH) {
        if (lastOptimizationHistory == null || lastOptimizationHistory.iterations().isEmpty() || lastSlice == null
                || mapSpec == null) {
            return;
        }
        int iterationIndex = clamp((int) Math.round(timelineSlider.getValue()), 0,
                lastOptimizationHistory.iterations().size() - 1);
        IterationSnapshot iteration = lastOptimizationHistory.iterations().get(iterationIndex);
        for (int i = 0; i < iteration.agents().size(); i++) {
            AgentSnapshot agent = iteration.agents().get(i);
            double x = drawX + (agent.position()[lastSlice.xIndex()] - mapSpec.xmin()) / (mapSpec.xmax() - mapSpec.xmin()) * drawW;
            double y = drawY + (mapSpec.ymax() - agent.position()[lastSlice.yIndex()]) / (mapSpec.ymax() - mapSpec.ymin()) * drawH;
            gc.setFill(agent.feasible() ? Color.rgb(166, 227, 161, 0.85) : Color.rgb(243, 139, 168, 0.85));
            gc.fillOval(x - 2.0, y - 2.0, 4.0, 4.0);
        }
    }

    private void renderMinimapViewport(GraphicsContext gc, GridSpec mapSpec,
            double drawX, double drawY, double drawW, double drawH) {
        if (lastSpec == null || mapSpec == null || currentFunction == null) {
            return;
        }
        double mapXmin = mapSpec.xmin();
        double mapXmax = mapSpec.xmax();
        double mapYmin = mapSpec.ymin();
        double mapYmax = mapSpec.ymax();
        double mapXSpan = mapXmax - mapXmin;
        double mapYSpan = mapYmax - mapYmin;
        if (mapXSpan <= 0.0 || mapYSpan <= 0.0) {
            return;
        }

        double relLeft = clamp((lastSpec.xmin() - mapXmin) / mapXSpan, 0.0, 1.0);
        double relTop = clamp((mapYmax - lastSpec.ymax()) / mapYSpan, 0.0, 1.0);
        double relRight = clamp((lastSpec.xmax() - mapXmin) / mapXSpan, 0.0, 1.0);
        double relBottom = clamp((mapYmax - lastSpec.ymin()) / mapYSpan, 0.0, 1.0);

        double rawRectX = drawX + relLeft * drawW;
        double rawRectY = drawY + relTop * drawH;
        double rawRectW = Math.max((relRight - relLeft) * drawW, 0.0);
        double rawRectH = Math.max((relBottom - relTop) * drawH, 0.0);
        double rectW = Math.max(rawRectW, 2.0);
        double rectH = Math.max(rawRectH, 2.0);
        double rectCenterX = rawRectX + rawRectW / 2.0;
        double rectCenterY = rawRectY + rawRectH / 2.0;
        double rectX = clamp(rectCenterX - rectW / 2.0, drawX, drawX + drawW - rectW);
        double rectY = clamp(rectCenterY - rectH / 2.0, drawY, drawY + drawH - rectH);

        gc.setStroke(Color.rgb(249, 226, 175, 0.95));
        gc.setLineWidth(1.8);
        gc.strokeRect(rectX, rectY, rectW, rectH);
    }



    private void exportCurrentFrame() {
        if (lastRender == null) {
            showAlert("Нет данных для экспорта", "Сначала выполните расчёт.");
            return;
        }
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Сохранение PNG");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG", "*.png"));
        File file = chooser.showSaveDialog(root.getScene().getWindow());
        if (file == null) {
            return;
        }
        WritableImage snapshot = takeSnapshot();
        try {
            pngExporter.export(snapshot, file);
            statusLabel.setText("Изображение сохранено: " + file.getName());
        } catch (IOException ex) {
            showAlert("Ошибка экспорта", ex.getMessage());
        }
    }

    private void exportOptimizationHistory() {
        if (lastOptimizationHistory == null) {
            showAlert("Нет истории", "Сначала выполните запуск оптимизатора.");
            return;
        }
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Сохранение JSON history");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON", "*.json"));
        File file = chooser.showSaveDialog(root.getScene().getWindow());
        if (file == null) {
            return;
        }
        try {
            historyJsonWriter.write(lastOptimizationHistory, file.toPath());
            statusLabel.setText("History сохранена: " + file.getName());
        } catch (IOException ex) {
            showAlert("Ошибка экспорта JSON", ex.getMessage());
        }
    }

    private WritableImage takeSnapshot() {
        Bounds bounds = landscapeCanvas.getLayoutBounds();
        WritableImage image = new WritableImage((int) Math.ceil(bounds.getWidth()),
                (int) Math.ceil(bounds.getHeight()));
        SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.TRANSPARENT);
        imageLayer.snapshot(params, image);
        return image;
    }

    private void resetView() {
        Integer xDim = xAxisChoice.getValue();
        Integer yDim = yAxisChoice.getValue();
        if (currentFunction != null && xDim != null && yDim != null) {
            withSuppressedRecompute(() -> {
                xminSpinner.getValueFactory().setValue(currentFunction.lowerBound(xDim));
                xmaxSpinner.getValueFactory().setValue(currentFunction.upperBound(xDim));
                yminSpinner.getValueFactory().setValue(currentFunction.lowerBound(yDim));
                ymaxSpinner.getValueFactory().setValue(currentFunction.upperBound(yDim));
            });
            triggerRecomputeImmediate();
        }
        renderOptimizationOverlay();
    }



    private void wirePreviewInteractions() {
        previewViewport.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
            if (event.getButton() != MouseButton.PRIMARY || lastRender == null || lastRender.image() == null) {
                return;
            }
            Point2D localPoint = toImageLayerPoint(event);
            dragging = true;
            dragStartX = localPoint.getX();
            dragStartY = localPoint.getY();
            previewViewport.setCursor(Cursor.CLOSED_HAND);
        });
        previewViewport.addEventFilter(MouseEvent.MOUSE_RELEASED, event -> {
            if (event.getButton() != MouseButton.PRIMARY) {
                return;
            }
            dragging = false;
            previewViewport.setCursor(Cursor.CROSSHAIR);
        });
        previewViewport.addEventFilter(MouseEvent.MOUSE_DRAGGED, event -> {
            if (!dragging) {
                return;
            }
            Point2D localPoint = toImageLayerPoint(event);
            applyPanFromDrag(localPoint);
        });

        previewViewport.addEventFilter(MouseEvent.MOUSE_MOVED, event -> {
            if (lastSpec == null || lastGrid == null || lastRender == null || lastRender.image() == null) {
                coordLabel.setVisible(false);
                return;
            }
            Point2D localPoint = toImageLayerPoint(event);

            double imgW = landscapeCanvas.getWidth();
            double imgH = landscapeCanvas.getHeight();
            if (imgW <= 0 || imgH <= 0)
                return;
            double relX = localPoint.getX() / imgW;
            double relY = localPoint.getY() / imgH;

            if (relX < 0 || relX > 1 || relY < 0 || relY > 1) {
                coordLabel.setVisible(false);
                return;
            }

            double dataX = lastSpec.xmin() + relX * (lastSpec.xmax() - lastSpec.xmin());
            double dataY = lastSpec.ymax() - relY * (lastSpec.ymax() - lastSpec.ymin());

            int gridCol = (int) Math.round(relX * (lastSpec.nx() - 1));
            int gridRow = (int) Math.round((1.0 - relY) * (lastSpec.ny() - 1));
            gridCol = Math.max(0, Math.min(lastSpec.nx() - 1, gridCol));
            gridRow = Math.max(0, Math.min(lastSpec.ny() - 1, gridRow));
            double value = lastGrid[gridRow][gridCol];

            String xLabel = lastSlice != null ? dimensionLabel(lastSlice.xIndex()) : "x";
            String yLabel = lastSlice != null ? dimensionLabel(lastSlice.yIndex()) : "y";

            coordLabel.setText(String.format("%s=%.4f  %s=%.4f  →  f=%.6g", xLabel, dataX, yLabel, dataY, value));
            coordLabel.setVisible(true);
        });

        previewViewport.addEventFilter(MouseEvent.MOUSE_EXITED, event -> coordLabel.setVisible(false));

        previewViewport.addEventFilter(ScrollEvent.SCROLL, this::handleZoom);
        previewViewport.setCursor(Cursor.CROSSHAIR);
    }

    private void handleZoom(ScrollEvent event) {
        if (lastRender == null || lastRender.image() == null) {
            return;
        }
        if (lastSpec == null) {
            return;
        }

        double delta = event.getDeltaY();
        if (delta == 0.0) {
            event.consume();
            return;
        }
        double zoomFactor = delta > 0 ? 0.9 : 1.1;

        Point2D localPoint = toImageLayerPoint(event);
        double relX = clamp(localPoint.getX() / Math.max(1.0, imageLayer.getWidth()), 0.0, 1.0);
        double relY = clamp(localPoint.getY() / Math.max(1.0, imageLayer.getHeight()), 0.0, 1.0);

        double xmin = xminSpinner.getValue();
        double xmax = xmaxSpinner.getValue();
        double ymin = yminSpinner.getValue();
        double ymax = ymaxSpinner.getValue();
        double xSpan = xmax - xmin;
        double ySpan = ymax - ymin;
        double anchorX = xmin + relX * xSpan;
        double anchorY = ymax - relY * ySpan;

        double newXSpan = xSpan * zoomFactor;
        double newYSpan = ySpan * zoomFactor;
        double newXMin = anchorX - relX * newXSpan;
        double newXMax = newXMin + newXSpan;
        double newYMax = anchorY + relY * newYSpan;
        double newYMin = newYMax - newYSpan;

        updateViewRanges(newXMin, newXMax, newYMin, newYMax, true);
        event.consume();
    }

    private Point2D toImageLayerPoint(MouseEvent event) {
        return imageLayer.sceneToLocal(event.getSceneX(), event.getSceneY());
    }

    private Point2D toImageLayerPoint(ScrollEvent event) {
        return imageLayer.sceneToLocal(event.getSceneX(), event.getSceneY());
    }

    private void applyPanFromDrag(Point2D localPoint) {
        if (lastSpec == null) {
            return;
        }
        double width = Math.max(1.0, imageLayer.getWidth());
        double height = Math.max(1.0, imageLayer.getHeight());
        double deltaX = localPoint.getX() - dragStartX;
        double deltaY = localPoint.getY() - dragStartY;

        double xSpan = xmaxSpinner.getValue() - xminSpinner.getValue();
        double ySpan = ymaxSpinner.getValue() - yminSpinner.getValue();
        double shiftX = -(deltaX / width) * xSpan;
        double shiftY = (deltaY / height) * ySpan;

        dragStartX = localPoint.getX();
        dragStartY = localPoint.getY();
        updateViewRanges(
                xminSpinner.getValue() + shiftX,
                xmaxSpinner.getValue() + shiftX,
                yminSpinner.getValue() + shiftY,
                ymaxSpinner.getValue() + shiftY,
                false);
    }

    private void updateViewRanges(double newXMin, double newXMax, double newYMin, double newYMax, boolean immediate) {
        Integer xDim = xAxisChoice.getValue();
        Integer yDim = yAxisChoice.getValue();
        if (currentFunction == null || xDim == null || yDim == null) {
            return;
        }

        if (newXMax <= newXMin || newYMax <= newYMin) {
            return;
        }

        ViewWindow limited = ViewWindowLimiter.limit(currentFunction, xDim, yDim,
                newXMin, newXMax, newYMin, newYMax);
        if (rangesEqual(limited.xmin(), xminSpinner.getValue())
                && rangesEqual(limited.xmax(), xmaxSpinner.getValue())
                && rangesEqual(limited.ymin(), yminSpinner.getValue())
                && rangesEqual(limited.ymax(), ymaxSpinner.getValue())) {
            return;
        }

        withSuppressedRecompute(() -> {
            xminSpinner.getValueFactory().setValue(limited.xmin());
            xmaxSpinner.getValueFactory().setValue(limited.xmax());
            yminSpinner.getValueFactory().setValue(limited.ymin());
            ymaxSpinner.getValueFactory().setValue(limited.ymax());
        });
        if (immediate) {
            triggerRecomputeImmediate();
        } else {
            triggerRecompute();
        }
    }



    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private double[] defaultCenterPoint(BenchmarkFunction function) {
        double[] point = new double[function.dimension()];
        for (int i = 0; i < point.length; i++) {
            double lower = function.lowerBound(i);
            double upper = function.upperBound(i);
            point[i] = clamp(0.0, lower, upper);
        }
        return point;
    }

    private double[] clampToBounds(double[] point) {
        double[] result = point.clone();
        for (int i = 0; i < result.length; i++) {
            double lower = currentFunction.lowerBound(i);
            double upper = currentFunction.upperBound(i);
            result[i] = clamp(result[i], lower, upper);
        }
        return result;
    }

    private double clamp(double value, double min, double max) {
        if (value < min) {
            return min;
        }
        if (value > max) {
            return max;
        }
        return value;
    }

    private boolean rangesEqual(double left, double right) {
        double scale = Math.max(1.0, Math.max(Math.abs(left), Math.abs(right)));
        return Math.abs(left - right) <= scale * 1e-9;
    }

    private double stepForRange(double min, double max) {
        double span = Math.abs(max - min);
        if (span == 0.0) {
            return 0.1;
        }
        double step = span / 200.0;
        return Math.max(step, 0.0001);
    }

    private void setupIntegerSpinner(Spinner<Integer> spinner, int min, int max, int initial, int step) {
        spinner.setValueFactory(new IntegerSpinnerValueFactory(min, max, clamp(initial, min, max), step));
        spinner.setEditable(true);
    }

    private void setupCustomBoundSpinner(Spinner<Double> spinner, double initialValue) {
        DoubleSpinnerValueFactory factory = new DoubleSpinnerValueFactory(-1000.0, 1000.0, initialValue, 1.0);
        factory.setConverter(new NumberStringConverter());
        spinner.setValueFactory(factory);
        spinner.setEditable(true);
        spinner.focusedProperty().addListener((obs, wasFocused, focused) -> {
            if (!focused) {
                commitEditorText(spinner);
            }
        });
    }

    private void setupDoubleSpinner(Spinner<Double> spinner, double min, double max, double initial, double step) {
        DoubleSpinnerValueFactory factory = new DoubleSpinnerValueFactory(min, max, initial, step);
        factory.setConverter(new NumberStringConverter());
        spinner.setValueFactory(factory);
        spinner.setEditable(true);
        spinner.focusedProperty().addListener((obs, oldVal, focused) -> {
            if (!focused) {
                commitEditorText(spinner);
            }
        });
    }

    private void installClip(Region region) {
        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(region.widthProperty());
        clip.heightProperty().bind(region.heightProperty());
        region.setClip(clip);
    }

    private void cancelInFlightComputation() {
        if (inFlightTask != null) {
            inFlightTask.cancel(true);
            inFlightTask = null;
        }
    }

    private int clamp(int value, int min, int max) {
        if (value < min) {
            return min;
        }
        if (value > max) {
            return max;
        }
        return value;
    }

    private StringConverter<Integer> dimensionLabelConverter() {
        return new StringConverter<>() {
            @Override
            public String toString(Integer object) {
                if (object == null) {
                    return "";
                }
                return dimensionLabel(object);
            }

            @Override
            public Integer fromString(String string) {
                return null;
            }
        };
    }

    private String dimensionLabel(int index) {
        return "x" + (index + 1);
    }

    private String formatNumber(double value) {
        if (!Double.isFinite(value)) {
            return "NaN";
        }
        return NUMBER_FORMAT.format(value);
    }

    private Tooltip createTooltip(String text) {
        Tooltip tooltip = new Tooltip(text);
        tooltip.setShowDelay(Duration.millis(400));
        tooltip.setHideDelay(Duration.millis(200));

        return tooltip;
    }

    private static final class NumberStringConverter extends StringConverter<Double> {
        private final DecimalFormat format = new DecimalFormat("0.#####");

        @Override
        public String toString(Double object) {
            if (object == null) {
                return "";
            }
            return format.format(object);
        }

        @Override
        public Double fromString(String string) {
            if (string == null || string.isBlank()) {
                return 0.0;
            }
            try {
                return Double.parseDouble(string.replace(',', '.'));
            } catch (NumberFormatException ex) {
                return 0.0;
            }
        }
    }

}
