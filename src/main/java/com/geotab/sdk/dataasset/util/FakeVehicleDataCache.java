package com.geotab.sdk.dataasset.util;

import com.geotab.model.entity.NameEntityWithVersion;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
public class FakeVehicleDataCache extends NameEntityWithVersion {

  private Double latitude;
  private Double longitude;
  private Float speed;
  private String vehicleIdentificationNumber;

  public FakeVehicleDataCache(Double latitude, Double longitude, Float speed, String vehicleIdentificationNumber) {
    super();
    this.latitude = latitude;
    this.longitude = longitude;
    this.speed = speed;
    this.vehicleIdentificationNumber = vehicleIdentificationNumber;
  }

}
