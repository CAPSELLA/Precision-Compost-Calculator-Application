package gr.uoa.di.madgik;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;


import gr.uoa.di.madgik.service.GeoService;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
@EnableSwagger2
public class PrecisionCompostCalculatorApplication extends SpringBootServletInitializer {
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(PrecisionCompostCalculatorApplication.class);
    }    
	

	public static void main(String[] args) {
		SpringApplication.run(PrecisionCompostCalculatorApplication.class, args);
	}
}
