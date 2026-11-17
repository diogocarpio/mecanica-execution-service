package com.f1rsters.execution.bdd;

import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@CucumberContextConfiguration
@SpringBootTest(
        properties = {
                "spring.autoconfigure.exclude="
                        + "org.springframework.boot.mongodb.autoconfigure.MongoAutoConfiguration,"
                        + "org.springframework.boot.data.mongodb.autoconfigure.DataMongoAutoConfiguration,"
                        + "org.springframework.boot.data.mongodb.autoconfigure.DataMongoRepositoriesAutoConfiguration"
        })
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(BddTestConfiguration.class)
public class CucumberSpringConfiguration {
}
