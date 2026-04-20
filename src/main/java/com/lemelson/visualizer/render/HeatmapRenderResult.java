package com.lemelson.visualizer.render;

import javafx.scene.image.WritableImage;

public record HeatmapRenderResult(WritableImage image, double minValue, double maxValue) {
}
