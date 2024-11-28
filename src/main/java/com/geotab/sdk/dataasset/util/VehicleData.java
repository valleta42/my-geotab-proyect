package com.geotab.sdk.dataasset.util;

import com.geotab.model.entity.NameEntityWithVersion;
import com.geotab.model.entity.device.Device;
import com.geotab.model.entity.statusdata.StatusData;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * A record that represents an engine status record from the engine system of
 * the specific {@link Device}.
 */
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class VehicleData extends NameEntityWithVersion {

  private StatusData statusData;
  private Double latitude;
  private Double longitude;
  private Float speed;
  private String vehicleIdentificationNumber;

}
