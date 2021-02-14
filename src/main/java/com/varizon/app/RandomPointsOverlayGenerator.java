package com.varizon.app;

import java.awt.Color;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.SpatialReference;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol;
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol.Style;
import com.esri.arcgisruntime.symbology.UniqueValueRenderer;
import com.esri.arcgisruntime.symbology.UniqueValueRenderer.UniqueValue;

public class RandomPointsOverlayGenerator {

    public static final double MAX_LAT = 48.466000;
    public static final double MIN_LAT = 48.434000;
    public static final double MAX_LON = 35.060000;
    public static final double MIN_LON = 34.976000;

    private final Random random = new Random();

    final Logger logger = Logger.getLogger(this.getClass().getSimpleName());

    public GraphicsOverlay getOverlayWithRandomPoints() {
        logger.info("Start generating GraphicOverlay");

        GraphicsOverlay randomPointsOverlay = new GraphicsOverlay();
        randomPointsOverlay.getGraphics().addAll(this.generateRandomPoints());

        randomPointsOverlay.setRenderer(this.prepareUniqueValueRenderer());
        return randomPointsOverlay;
    }

    private List<Graphic> generateRandomPoints() {
        logger.info("Start generating 100 Points");
        final SpatialReference spatialReference = SpatialReferences.getWgs84();

        final List<Graphic> graphics = Stream.generate(() -> new Graphic(
            new Point(((Math.random() * (MAX_LON - MIN_LON)) + MIN_LON),
                ((Math.random() * (MAX_LAT - MIN_LAT)) + MIN_LAT), spatialReference)))
            .limit(100)
            .collect(Collectors.toList());

        graphics.forEach(
            graphic -> graphic.getAttributes().put("TYPE", PoiTypeEnum.findById(random.nextInt(7) + 1).name()));
        return graphics;
    }

    private UniqueValueRenderer prepareUniqueValueRenderer() {
        logger.info("Start preparing UniqueValueRenderer");
        final UniqueValueRenderer uniqueValueRenderer = new UniqueValueRenderer();
        uniqueValueRenderer.getFieldNames().add("TYPE");

        final List<UniqueValue> uniqueValues = IntStream.range(1, 7)
            .mapToObj(i -> new UniqueValue("Type: " + PoiTypeEnum.findById(i).name(),
                PoiTypeEnum.findById(i).name(),
                new SimpleMarkerSymbol(Style.values().clone()[i % Style.values().length],  this.getRandomColor(), 15.0f),
                Collections.singletonList(PoiTypeEnum.findById(i).name())))
            .collect(Collectors.toList());

        uniqueValueRenderer.getUniqueValues().addAll(uniqueValues);

        return uniqueValueRenderer;
     }

    private int getRandomColor() {
        int redValue = random.nextInt(255);
        int greenValue = random.nextInt(255);
        int blueValue = random.nextInt(255);

        return new Color(redValue, greenValue, blueValue).getRGB();
    }


}
