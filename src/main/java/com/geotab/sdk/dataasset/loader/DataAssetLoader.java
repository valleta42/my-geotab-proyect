package com.geotab.sdk.dataasset.loader;

import static com.geotab.api.DataStore.LogRecordEntity;
import static com.geotab.api.DataStore.StatusDataEntity;
import static com.geotab.util.DateTimeUtil.nowUtcLocalDateTime;

import com.geotab.api.GeotabApi;
import com.geotab.http.exception.DbUnavailableException;
import com.geotab.http.exception.OverLimitException;
import com.geotab.http.invoker.ServerInvoker;
import com.geotab.http.request.param.GetFeedParameters;
import com.geotab.model.FeedResult;
import com.geotab.model.entity.Entity;
import com.geotab.model.entity.device.Device;
import com.geotab.model.entity.device.GoDevice;
import com.geotab.model.entity.logrecord.LogRecord;
import com.geotab.model.entity.statusdata.StatusData;
import com.geotab.model.login.Credentials;
import com.geotab.model.search.LogRecordSearch;
import com.geotab.model.search.StatusDataSearch;
import com.geotab.sdk.dataasset.util.FakeVehicleDataCache;
import com.geotab.sdk.dataasset.util.VehicleData;
import com.geotab.sdk.datafeed.cache.ControllerCache;
import com.geotab.sdk.datafeed.cache.DeviceCache;
import com.geotab.sdk.datafeed.cache.DiagnosticCache;
import com.geotab.sdk.datafeed.cache.DriverCache;
import com.geotab.sdk.datafeed.cache.FailureModeCache;
import com.geotab.sdk.datafeed.cache.UnitOfMeasureCache;
import com.geotab.sdk.datafeed.loader.DataFeedParameters;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Data feed loader which queries Geotab's servers for vehicle data.
 */
public class DataAssetLoader {

  private static final Logger log = LoggerFactory.getLogger(DataAssetLoader.class);

  private GeotabApi geotabApi;

  private DataFeedParameters dataFeedParameters;

  private ControllerCache controllerCache;

  private UnitOfMeasureCache unitOfMeasureCache;

  private DiagnosticCache diagnosticCache;

  private FailureModeCache failureModeCache;

  private DeviceCache deviceCache;

  private DriverCache driverCache;

  private LocalDateTime cacheReloadTime;

  private Map<String, FakeVehicleDataCache> mapFakeVehicleDataCache;

  public DataAssetLoader(String serverUrl, Credentials credentials, DataFeedParameters feedParameters) {
    this.geotabApi = new GeotabApi(credentials, serverUrl, ServerInvoker.DEFAULT_TIMEOUT);
    this.dataFeedParameters = feedParameters;
    this.cacheReloadTime = LocalDateTime.now().minusMinutes(1);
    this.controllerCache = new ControllerCache(geotabApi);
    this.unitOfMeasureCache = new UnitOfMeasureCache(geotabApi);
    this.diagnosticCache = new DiagnosticCache(geotabApi, controllerCache, unitOfMeasureCache);
    this.failureModeCache = new FailureModeCache(geotabApi);
    this.deviceCache = new DeviceCache(geotabApi);
    this.driverCache = new DriverCache(geotabApi);
    this.mapFakeVehicleDataCache = new HashMap<String, FakeVehicleDataCache>();
  }

  public DataAssetResult load(boolean firstStep) {

    log.info("Loading data Asset…");

    try {
      reloadCaches();

      DataAssetResult out = new DataAssetResult();

      if (firstStep) {
        // In the first step charge the initial charge
        log.info("Loading the first step");
        out.vehiclesData = loadFirstStatusData();
      }

      // After refresh the new vehicleDatas
      log.info("Loading the second step");
      out.vehiclesDataRefresh = loadStatusData();

      return out;
    } catch (DbUnavailableException dbUnavailableException) {
      log.error("Db unavailable - ", dbUnavailableException);
      try {
        Thread.sleep(5 * 60 * 1000);
      } catch (InterruptedException e) {
        log.warn("Can not sleep due to DbUnavailableException", e);
      }
    } catch (OverLimitException overLimitException) {
      log.error("OverLimitException ({}); sleeping for 1 minute…  ", overLimitException.getMessage());
      try {
        Thread.sleep(60 * 1000);
      } catch (InterruptedException e) {
        log.warn("Can not sleep due to OverLimitException", e);
      }
    } catch (Exception exception) {
      log.error("Can not load data feed", exception);
    }

    DataAssetResult out = new DataAssetResult();
    out.vehiclesData = new ArrayList<>();
    out.vehiclesDataRefresh = new ArrayList<>();
    return out;
  }

  public void stop() {
    geotabApi.disconnect();
  }

  private void reloadCaches() {
    if (LocalDateTime.now().isAfter(cacheReloadTime)) {
      log.debug("Reloading caches");

      controllerCache.reloadAll();
      unitOfMeasureCache.reloadAll();
      diagnosticCache.reloadAll();
      failureModeCache.reloadAll();
      deviceCache.reloadAll();
      driverCache.reloadAll();

      cacheReloadTime = LocalDateTime.now().plusHours(12);
    }
  }

  private List<VehicleData> loadFirstStatusData() {

    Optional<List<StatusData>> statusDataFeedResult = getLastStatusData();
    List<VehicleData> vehicleDatas = new ArrayList<>();
    if (statusDataFeedResult.isPresent()) {
      int i = 0;
      statusDataFeedResult.get().forEach(data -> {
        // Populate relevant VehicleData fields.
        data.setDevice(deviceCache.get(data.getDevice().getId().getId()));
        data.setDiagnostic(diagnosticCache.get(data.getDiagnostic().getId().getId()));
        data.setController(controllerCache.get(data.getController().getId().getId()));

        VehicleData vehicleData = transformStatusDataInVehicleData(data);

        if (vehicleData != null) {
          vehicleDatas.add(vehicleData);
        }
      });
    }

    return vehicleDatas;
  }

  private List<VehicleData> loadStatusData() {
    Optional<FeedResult<StatusData>> statusDataFeedResult = getFeed(StatusData.class,
        dataFeedParameters.lastStatusDataToken);

    List<VehicleData> vehicleDatas = new ArrayList<>();

    if (statusDataFeedResult.isPresent()) {
      dataFeedParameters.lastStatusDataToken = statusDataFeedResult.get().getToVersion();
      statusDataFeedResult.get().getData().forEach(data -> {
        // Populate relevant VehicleData fields.
        data.setDevice(deviceCache.get(data.getDevice().getId().getId()));
        data.setDiagnostic(diagnosticCache.get(data.getDiagnostic().getId().getId()));
        data.setController(controllerCache.get(data.getController().getId().getId()));

        VehicleData vehicleData = transformStatusDataInVehicleData(data);

        if (vehicleData != null) {
          vehicleDatas.add(vehicleData);
        }
      });
    }

    return vehicleDatas;
  }

  private VehicleData transformStatusDataInVehicleData(StatusData data) {

    VehicleData vehicleData = null;

    if (data.getDevice() instanceof GoDevice
        && !((GoDevice) data.getDevice()).getVehicleIdentificationNumber().isEmpty()) {

      vehicleData = new VehicleData();
      vehicleData.setStatusData(data);

      String vehicleIdentificationNumber = ((GoDevice) data.getDevice()).getVehicleIdentificationNumber();

      if (mapFakeVehicleDataCache.get(vehicleIdentificationNumber) == null) {

        Optional<List<LogRecord>> log = callGet(data.getDevice());

        if (log.isPresent()) {
          LogRecord r = log.get().get(log.get().size() - 1);
          vehicleData.setLatitude(r.getLatitude());
          vehicleData.setLongitude(r.getLongitude());
          vehicleData.setSpeed(r.getSpeed());
          // }
        }
      } else {

        FakeVehicleDataCache dataFakeCache = mapFakeVehicleDataCache.get(vehicleIdentificationNumber);
        vehicleData.setLatitude(dataFakeCache.getLatitude());
        vehicleData.setLongitude(dataFakeCache.getLongitude());
        vehicleData.setSpeed(dataFakeCache.getSpeed());
      }

      data.setDevice(deviceCache.get(data.getDevice().getId().getId()));

      mapFakeVehicleDataCache.put(vehicleIdentificationNumber, new FakeVehicleDataCache(vehicleData.getLatitude(),
          vehicleData.getLongitude(), vehicleData.getSpeed(), vehicleIdentificationNumber));

    }

    return vehicleData;
  }

  private <T extends Entity> Optional<FeedResult<T>> getFeed(Class<T> type, String fromVersion) {
    log.info("Get data feed for {} fromVersion {}", type.getSimpleName(), fromVersion);
    System.out.println("Get data feed for " + type.getSimpleName() + "  fromVersion " + fromVersion);
    return geotabApi.callGetFeed(
        GetFeedParameters.getFeedParamsBuilder().typeName(type.getSimpleName()).fromVersion(fromVersion).build(), type);
  }

  private Optional<List<StatusData>> getLastStatusData() {
    LocalDateTime toDate = nowUtcLocalDateTime();
    LocalDateTime fromDate = toDate.minusDays(7);
    return geotabApi.callGet(StatusDataEntity, StatusDataSearch.builder().fromDate(fromDate).toDate(toDate).build());
  }

  private Optional<List<LogRecord>> callGet(Device device) {
    LocalDateTime toDate = nowUtcLocalDateTime();
    LocalDateTime fromDate = toDate.minusDays(7);
    return geotabApi.callGet(LogRecordEntity,
        LogRecordSearch.builder().deviceSearch(device.getId()).fromDate(fromDate).toDate(toDate).build());
  }
}
