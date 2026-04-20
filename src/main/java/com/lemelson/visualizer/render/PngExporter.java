package com.lemelson.visualizer.render;

import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.WritableImage;

public class PngExporter {

    public void export(WritableImage image, File target) throws IOException {
        if (image == null) {
            throw new IllegalArgumentException("Нет изображения для экспорта");
        }
        if (target == null) {
            throw new IllegalArgumentException("Не выбран файл назначения");
        }
        if (!ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", target)) {
            throw new IOException("PNG writer не найден в текущей среде выполнения");
        }
    }
}
