package com.ohdoking.csv_reader_batch;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ImportProductsJobBDDTest {

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private Job importProductsJob;

    @Autowired
    private ProductRepository productRepository;

    @BeforeEach
    void setup() {
        // Given: the database is clean before each test
        productRepository.deleteAll();
    }


    @Test
    @DisplayName("Given a product CSV file, when the batch job runs, then the products are inserted into the database")
    void given_a_product_csv_file_when_the_batch_job_runs_then_the_products_are_inserted_into_the_database() throws Exception {

        // Given: a product.csv file with 3 valid records exists
        // (This is a precondition handled by the test setup and file placement in src/main/resources)

        // When: the batch job is executed
        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();
        JobExecution jobExecution = jobLauncher.run(importProductsJob, jobParameters);

        // Then: the job should complete successfully
        assertThat(jobExecution.getExitStatus().getExitCode()).isEqualTo(ExitStatus.COMPLETED.getExitCode());

        // And: the database should contain the expected number of records
        assertThat(productRepository.count()).isEqualTo(3);

        // And: a specific product should exist in the database with the correct data
        List<Product> laptopList = productRepository.findAllByName("Laptop");
        assertThat(laptopList).isNotNull();
        assertThat(laptopList.getFirst().getPrice()).isEqualTo(1200.00);
    }
}
