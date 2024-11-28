package com.geotab.sdk.dataasset.exporter;

import com.geotab.sdk.dataasset.loader.DataAssetResult;
import com.geotab.sdk.dataasset.util.CommandLineArguments;
import java.util.function.Function;

public interface Exporter {

  Function<CommandLineArguments, Exporter> FACTORY = commandLineArguments -> {
    return new CsvExporterVehicles(commandLineArguments.getOutputPath());
  };

  void export(DataAssetResult dataAssetResult) throws Exception;
}
