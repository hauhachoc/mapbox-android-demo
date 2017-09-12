package com.mapbox.mapboxandroiddemo.examples.plugins;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import com.getbase.floatingactionbutton.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.afollestad.materialdialogs.folderselector.FileChooserDialog;
import com.google.gson.JsonObject;
import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.plugins.geojson.GeoJsonPlugin;
import com.mapbox.mapboxsdk.plugins.geojson.GeoJsonPluginBuilder;
import com.mapbox.mapboxsdk.plugins.geojson.listener.OnLoadingGeoJsonListener;
import com.mapbox.mapboxsdk.plugins.geojson.listener.OnMarkerEventListener;

import java.io.File;

import timber.log.Timber;

public class GeoJSONPluginActivity extends AppCompatActivity implements OnMapReadyCallback,
  OnLoadingGeoJsonListener, OnMarkerEventListener, FileChooserDialog.FileCallback {

  private MapView mapView;
  private MapboxMap mapboxMap;
  private GeoJsonPlugin geoJsonPlugin;
  private ProgressBar progressBar;
  private FloatingActionButton urlFab;
  private FloatingActionButton assetsFab;
  private FloatingActionButton pathFab;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_geojson_plugin);
    setUpFabButtons();
    progressBar = (ProgressBar) findViewById(R.id.progress_bar);

    mapView = (MapView) findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(this);
  }

  @Override
  public void onMapReady(MapboxMap mapboxMap) {
    GeoJSONPluginActivity.this.mapboxMap = mapboxMap;
    geoJsonPlugin = new GeoJsonPluginBuilder()
      .withContext(this)
      .withMap(mapboxMap)
      .withOnLoadingURL(this)
      .withOnLoadingFileAssets(this)
      .withOnLoadingFilePath(this)
      .withMarkerClickListener(this)
      .withRandomFillColor()
      .build();
    mapboxMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(32.6546, 51.6680), 7));
  }

  private void setUpFabButtons() {
    urlFab = (FloatingActionButton) findViewById(R.id.fabURL);
    assetsFab = (FloatingActionButton) findViewById(R.id.fabAssets);
    pathFab = (FloatingActionButton) findViewById(R.id.fabPath);
    onUrlFabClick();
    onAssetsFabClick();
    onPathFabClick();
  }

  private void onUrlFabClick() {
    urlFab.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (mapboxMap != null && geoJsonPlugin != null) {
          mapboxMap.clear();
          geoJsonPlugin.setUrl("https://data.illinois.gov/dataset/101823db-237b-4005-8737-f58" +
            "79803dd59/resource/4eb5b1e7-4e5d-44bc-a478-277d5c5d0909/download/data.geojson");
          Toast.makeText(GeoJSONPluginActivity.this, R.string.building_footprints, Toast.LENGTH_SHORT).show();
        }
      }
    });
  }

  private void onAssetsFabClick() {
    assetsFab.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (mapboxMap != null && geoJsonPlugin != null) {
          mapboxMap.clear();
          geoJsonPlugin.setAssetsName("west_africa_power_plants.geojson");
        }
      }
    });
  }

  private void onPathFabClick() {
    pathFab.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (Build.VERSION.SDK_INT >= 23) {
          if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            == PackageManager.PERMISSION_GRANTED) {
            Timber.v("Permission is granted");
            showFileChooserDialog();
            Toast.makeText(GeoJSONPluginActivity.this, R.string.find_file_instruction_toast,
              Toast.LENGTH_SHORT).show();
          } else {
            Timber.v("Permission is revoked");
            ActivityCompat.requestPermissions(GeoJSONPluginActivity.this,
              new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
          }
        } else { //permission is automatically granted on sdk<23 upon installation
          Timber.v("Permission is granted");
          showFileChooserDialog();
        }
      }
    });
  }

  /**
   * Draws GeoJSON file from a specific path. Please add and locate a GeoJSON file in your device to test it.
   *
   * @param file selected file from external storage
   */
  private void drawFromPath(File file) {
    String path = file.getAbsolutePath();
    if (mapboxMap != null && geoJsonPlugin != null) {
      mapboxMap.clear();
      geoJsonPlugin.setFilePath(path);
    }
  }

  // Add the mapView lifecycle to the activity's lifecycle methods
  @Override
  public void onResume() {
    super.onResume();
    mapView.onResume();
  }

  @Override
  protected void onStart() {
    super.onStart();
    mapView.onStart();
  }

  @Override
  protected void onStop() {
    super.onStop();
    mapView.onStop();
  }

  @Override
  public void onPause() {
    super.onPause();
    mapView.onPause();
  }

  @Override
  public void onLowMemory() {
    super.onLowMemory();
    mapView.onLowMemory();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    mapView.onDestroy();
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    mapView.onSaveInstanceState(outState);
  }

  private void showFileChooserDialog() {
    new FileChooserDialog.Builder(this)
      .extensionsFilter(".geojson", ".json", ".js", ".txt")
      .goUpLabel("Up")
      .show();
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                         @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
      Timber.v("Permission: " + permissions[0] + "was " + grantResults[0]);
      showFileChooserDialog();
    }
  }

  @Override
  public void onFileSelection(@NonNull FileChooserDialog dialog, @NonNull File file) {
    drawFromPath(file);
  }

  @Override
  public void onFileChooserDismissed(@NonNull FileChooserDialog dialog) {

  }

  @Override
  public void onPreLoading() {
//    progressBar.setVisibility(View.VISIBLE);
  }

  @Override
  public void onLoaded() {
    Toast.makeText(this, "GeoJson data loaded", Toast.LENGTH_LONG).show();
//    progressBar.setVisibility(View.INVISIBLE);
  }

  @Override
  public void onLoadFailed(Exception e) {
//    progressBar.setVisibility(View.INVISIBLE);
    Toast.makeText(this, "Error occur during load GeoJson data. see logcat", Toast.LENGTH_LONG).show();
    e.printStackTrace();
  }

  @Override
  public void onMarkerClickListener(Marker marker, JsonObject properties) {
    Toast.makeText(this, properties.get("title").getAsString(), Toast.LENGTH_SHORT).show();
  }
}

