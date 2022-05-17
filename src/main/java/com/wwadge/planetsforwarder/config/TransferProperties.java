package com.wwadge.planetsforwarder.config;

import java.math.BigDecimal;
import java.util.List;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "planets")
public class TransferProperties {
  List<AccountDetail> from;
  String to;
  String schedule;

  @Data
  public static class AccountDetail {
    String name;
    String seed;
    BigDecimal percentageToTransfer = BigDecimal.ONE;

  }
}
