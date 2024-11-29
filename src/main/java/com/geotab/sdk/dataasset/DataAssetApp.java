package com.geotab.sdk.dataasset;

import com.geotab.sdk.dataasset.util.CommandLineArguments;
import com.geotab.sdk.dataasset.worker.DataAssetWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a console example of obtaining the Assets from the server.
 *
 * <p>1) Process command line arguments: Server, Database, User, Password, File
 * Path.
 *
 * <p>2) Collect data and export it to csv file.
 *
 * <p>A complete Geotab API object and method reference is available at the Geotab
 * Developer page.
 */
public class DataAssetApp {

  private static final Logger log = LoggerFactory.getLogger(DataAssetApp.class);

  private static void addShutdownHook(DataAssetWorker dataAssetWorker) {
    final Thread mainThread = Thread.currentThread();
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      log.debug("Application is stopping…");
      if (dataAssetWorker.isProcessing()) {
        dataAssetWorker.shutdown();
      }
      try {
        mainThread.join(); // waits for main thread to finish
      } catch (InterruptedException e) {
        log.error("Can not join main thread");
      }
      log.debug("Application stopped");
    }));
  }

  public static void main(String[] args) throws Exception {
    CommandLineArguments commandLineArguments = new CommandLineArguments(args);

    DataAssetWorker dataAssetWorker = new DataAssetWorker(commandLineArguments);

    addShutdownHook(dataAssetWorker);

    dataAssetWorker.start();

    if (!commandLineArguments.isFeedContinuously()) {
      while (true) {
        if (dataAssetWorker.isProcessing()) {
          // shutdown only after it started processing
          dataAssetWorker.shutdown();
          break;
        }
      }
    }

    dataAssetWorker.join(); // main thread waits for it to finish
  }

}
