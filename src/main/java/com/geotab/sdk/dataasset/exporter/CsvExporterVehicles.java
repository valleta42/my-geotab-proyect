package com.geotab.sdk.dataasset.exporter;

import static com.geotab.util.DateTimeUtil.localDateTimeToString;

import com.geotab.model.entity.device.GoDevice;
import com.geotab.sdk.dataasset.loader.DataAssetResult;
import com.geotab.sdk.dataasset.util.VehicleData;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CsvExporterVehicles implements Exporter {

  private static final String FILE_NAME = "_Data";

  private static final String[] DATA_HEADER = new String[] { "Id", "Date", "Vehicle Name", "Vehicle Serial Number",
      "VIN", "Longitude", "Latitude", "Odometer" };

  private static final Logger log = LoggerFactory.getLogger(CsvExporterVehicles.class);

  private String outputPath;

  public CsvExporterVehicles(String outputPath) {
    this.outputPath = outputPath != null && !outputPath.isEmpty() ? outputPath : ".";

    if (!Files.exists(Paths.get(this.outputPath))) {
      try {
        Files.createDirectories(Paths.get(outputPath));
      } catch (IOException e) {
        throw new RuntimeException("Failed to initialize for output path " + outputPath, e);
      }
    }
  }

  public void export(DataAssetResult dataAssetResult) throws Exception {
    log.info("Exporting ......");
    exportVehicleData(dataAssetResult.vehiclesData);
    exportVehicleData(dataAssetResult.vehiclesDataRefresh);

  }

  private void exportVehicleData(List<VehicleData> vehicleData) throws Exception {

    log.info("Exporting VehicleData to csv…");

    if (vehicleData != null && vehicleData.size() > 0) {

      log.info("statusData exported to {}", vehicleData.size());
      vehicleData.forEach(data -> {

        String vin = data.getStatusData().getDevice() instanceof GoDevice
            ? ((GoDevice) data.getStatusData().getDevice()).getVehicleIdentificationNumber().replace(",", " ")
            : "";

        if (!vin.isEmpty()) {
          String[] line = new String[] {

              data.getStatusData(). getId().getId(),
              localDateTimeToString(data.getStatusData().getDateTime()),
              data.getStatusData().getDevice().getName().replace(",", " "),
              data.getStatusData().getDevice().getSerialNumber(),
              data.getStatusData().getDevice() instanceof GoDevice
                  ? ((GoDevice) data.getStatusData().getDevice()).getVehicleIdentificationNumber().replace(",", " ")
                  : "",
              data.getLatitude() != null ? data.getLatitude().toString().replace(",", " ") : "NO_DATA",
              data.getLongitude() != null ? data.getLongitude().toString().replace(",", " ") : "NO_DATA",
              data.getStatusData().getData().toString().replace(",", " "), };

          try {
            generateCsv(vin + FILE_NAME, DATA_HEADER, line);
          } catch (Exception e) {
            log.error("Error accediendo al archivo para añadir la linea");
          }
        }

      });
    } else {
      log.info("The list hasn't results ");
    }

  }

  private String generateCsv(String fileNamePrefix, String[] headers, String[] csvRow) throws Exception {

    String reportFileName = fileNamePrefix + ".csv";
    Path reportFilePath = Paths.get(outputPath + File.separator + reportFileName);

    boolean addHeaderRow = !Files.exists(reportFilePath);

    try (Writer writer = new FileWriter(reportFilePath.toString(), true)) {
      if (addHeaderRow) {
        writer.append(String.join(",", headers));
      }

      writer.append(System.getProperty("line.separator")).append(String.join(",", csvRow));
    }

    return reportFilePath.toString();
  }

  public static String escapeCsv(final String input) {
    final String Q = String.valueOf('"');
    return !input.contains(Q) ? input : Q + input.replaceAll(Q, Q + Q) + Q;
  }

}
