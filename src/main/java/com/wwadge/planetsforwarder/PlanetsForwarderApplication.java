package com.wwadge.planetsforwarder;

import com.wwadge.planetsforwarder.service.TransferService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PlanetsForwarderApplication implements ApplicationRunner {

  final TransferService transferService;

  @Value("${planets.schedule}")
  private String schedule;

  public PlanetsForwarderApplication(TransferService transferService) {
    this.transferService = transferService;
  }

  public static void main(String[] args) {
    SpringApplication.run(PlanetsForwarderApplication.class, args);
  }


  @Override
  public void run(ApplicationArguments args) throws Exception {
    if ("-".equals(schedule)) {
      transferService.start();
    }
  }
}
