package com.varizon.app;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.Callout;
import com.esri.arcgisruntime.mapping.view.Callout.LeaderPosition;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol;
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol.Style;
import com.esri.arcgisruntime.tasks.geocode.GeocodeResult;
import com.esri.arcgisruntime.tasks.geocode.LocatorTask;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import static com.varizon.app.RandomPointsOverlayGenerator.MAX_LAT;
import static com.varizon.app.RandomPointsOverlayGenerator.MAX_LON;
import static com.varizon.app.RandomPointsOverlayGenerator.MIN_LAT;
import static com.varizon.app.RandomPointsOverlayGenerator.MIN_LON;

public class App extends Application {

    private MapView mapView;

    final Logger logger = Logger.getLogger(this.getClass().getSimpleName());

    @Override
    public void start(Stage stage) throws Exception {

        try {
            stage.setTitle("Varizon Testing Task");

            mapView = new MapView();

            ComboBox<String> searchBox = new ComboBox<>();
            searchBox.setPromptText("Search");
            searchBox.setEditable(true);
            searchBox.setMaxWidth(260.0);

            Button zoomInButton = new Button("+");
            zoomInButton.setOnAction(event -> mapView.setViewpointScaleAsync(mapView.getMapScale() * 0.5));
            Button zoomOutButton = new Button("-");
            zoomOutButton.setOnAction(event -> mapView.setViewpointScaleAsync(mapView.getMapScale() * 1.5));

            ArcGISMap map = new ArcGISMap(Basemap.createOpenStreetMap());
            mapView.setMap(map);

            mapView.setViewpoint(
                new Viewpoint(((MIN_LAT + MAX_LAT) / 2), ((MIN_LON + MAX_LON) / 2), 60000));

            GraphicsOverlay graphicsOverlay = new GraphicsOverlay();

            mapView.getGraphicsOverlays().add(graphicsOverlay);
            mapView.getGraphicsOverlays().add(new RandomPointsOverlayGenerator().getOverlayWithRandomPoints());

            Callout callout = mapView.getCallout();
            callout.setLeaderPosition(LeaderPosition.BOTTOM);
            LocatorTask locatorTask = new LocatorTask(
                "https://geocode.arcgis.com/arcgis/rest/services/World/GeocodeServer");

            searchBox.setOnAction((ActionEvent evt) -> {
                String query = searchBox.getEditor().getText();
                logger.info("Start geocoding user query: " + query);

                if (!query.equals("")) {
                    mapView.getCallout().dismiss();
                    ListenableFuture<List<GeocodeResult>> results = locatorTask.geocodeAsync(query);
                    results.addDoneListener(() -> {
                        try {
                            List<GeocodeResult> geocodes = results.get();
                            if (!geocodes.isEmpty()) {
                                GeocodeResult geocode = geocodes.get(0);
                                Graphic marker = new Graphic(geocode.getDisplayLocation(), geocode.getAttributes(),
                                    new SimpleMarkerSymbol(Style.CROSS, 0xFFFF0000, 20.0f));

                                Point location = geocodes.get(0).getDisplayLocation();
                                mapView.setViewpointCenterAsync(location, 10000);

                                Platform.runLater(() -> {
                                    graphicsOverlay.getGraphics().clear();
                                    searchBox.hide();

                                    graphicsOverlay.getGraphics().add(marker);
                                });
                            }
                        } catch (InterruptedException | ExecutionException e) {
                            e.printStackTrace();
                        }
                    });
                }
            });

            StackPane stackPane = new StackPane();
            stackPane.getChildren().addAll(mapView, searchBox, zoomInButton, zoomOutButton);
            StackPane.setAlignment(zoomInButton, Pos.BOTTOM_RIGHT);
            StackPane.setAlignment(zoomOutButton, Pos.BOTTOM_RIGHT);
            StackPane.setAlignment(searchBox, Pos.TOP_RIGHT);
            StackPane.setMargin(searchBox, new Insets(10, 10, 0, 0));
            StackPane.setMargin(zoomInButton, new Insets(0, 10, 50, 0));
            StackPane.setMargin(zoomOutButton, new Insets(0, 10, 20, 0));

            Scene scene = new Scene(stackPane, 1000, 700);
            stage.setScene(scene);

            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() throws Exception {

        if (mapView != null) {
            mapView.dispose();
        }
    }


}
