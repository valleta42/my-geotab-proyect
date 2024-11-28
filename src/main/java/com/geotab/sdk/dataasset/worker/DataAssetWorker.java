package com.geotab.sdk.dataasset.worker;

import static com.geotab.util.DateTimeUtil.nowUtcLocalDateTime;

import com.geotab.sdk.dataasset.exporter.Exporter;
import com.geotab.sdk.dataasset.loader.DataAssetLoader;
import com.geotab.sdk.dataasset.loader.DataAssetResult;
import com.geotab.sdk.dataasset.util.CommandLineArguments;
import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataAssetWorker extends Thread {

  private static final Logger log = LoggerFactory.getLogger(DataAssetWorker.class);
  private static final int SECOND_TO_WAIT = 60;
  private AtomicBoolean isAlive = new AtomicBoolean(true);
  private AtomicBoolean isProccessing = new AtomicBoolean(false);

  private DataAssetLoader loader;
  private Exporter exporter;
  private CommandLineArguments commandLineWorker;
  // private Object commandLineArguments;

  public DataAssetWorker(CommandLineArguments commandLineArguments) {
    this.loader = new DataAssetLoader(
        commandLineArguments.getServer(),
        commandLineArguments.getCredentials(),
        commandLineArguments.getDataFeedParameters());
    this.exporter = Exporter.FACTORY.apply(commandLineArguments);
    this.commandLineWorker = commandLineArguments;
  }

  @Override
  public void run() {
    log.info("Running…");

    Boolean firstCharge = true;

    try {

      renameOldFiles();

      isProccessing.set(true);

      while (isAlive.get()) {
        try {
          DataAssetResult dataAssetResult = loader.load(firstCharge);
          exporter.export(dataAssetResult);
          treatSleepTime();
          firstCharge = false;
        } catch (Exception exception) {
          log.error("Worker exception while processing", exception);
        }
      }

    } catch (Exception e) {
      log.error("Error deleting files : {}", e.getMessage());
    } finally {
      loader.stop();
      isProccessing.set(false);
      log.debug("Processing stopped.");
    }
  }

  public void shutdown() {
    log.debug("Signal to stop processing…");
    isAlive.set(false);
  }

  public boolean isProcessing() {
    return isProccessing.get();
  }

  private void treatSleepTime() throws InterruptedException {
    Thread.sleep(SECOND_TO_WAIT * 1000);
  }

  private void renameOldFiles() throws Exception {

    if ("csv".equalsIgnoreCase(this.commandLineWorker.getExportType()) && this.commandLineWorker.getOutputPath() != null
        && !this.commandLineWorker.getOutputPath().isEmpty()) {
      String outputPath = this.commandLineWorker.getOutputPath();

      File path = new File(outputPath);

      if (!path.isDirectory()) {
        throw new Exception();
      }

      String[] listado = path.list();
      if (listado == null || listado.length == 0) {
        log.info("Not old element found");
        return;
      } else {
        for (String file : listado) {
          // create a new path for for transfer the old files
          if (new File(file).isFile()) {
            createHistoricalFiles(outputPath, file);
          }
        }
      }
    }
  }

  private void createHistoricalFiles(String outputPath, String fileName) {

    File historicPath = new File(outputPath + "/historic");

    if (!historicPath.exists()) {
      historicPath.mkdir();
    }

    File oldfile = new File(outputPath + "/" + fileName);
    File newfile = new File(historicPath + "/" + getNameWithoutExtension(fileName) + "-"
        + nowUtcLocalDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss")) + ".csv");
    if (oldfile.renameTo(newfile)) {
      System.out.println("archivo renombrado");
    } else {
      System.out.println("error");
    }
  }

  public String getNameWithoutExtension(String fileName) {

    int dotIndex = fileName.lastIndexOf('.');
    return (dotIndex == -1) ? fileName : fileName.substring(0, dotIndex);
  }
}
