package gov.samhsa.mhc.dss;

import gov.samhsa.mhc.brms.BrmsBasePackageMarkerInterface;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackageClasses = {DssApplication.class, BrmsBasePackageMarkerInterface.class})
@EnableDiscoveryClient
public class DssApplication {

    public static void main(String[] args) {
        SpringApplication.run(DssApplication.class, args);
    }
}
