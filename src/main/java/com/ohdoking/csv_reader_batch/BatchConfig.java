package com.ohdoking.csv_reader_batch;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.PlatformTransactionManager;


@Configuration
public class BatchConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final EntityManagerFactory entityManagerFactory;
    private final JpaProductWriter jpaProductWriter;

    public BatchConfig(JobRepository jobRepository, PlatformTransactionManager transactionManager, EntityManagerFactory entityManagerFactory, JpaProductWriter jpaProductWriter) {
        this.jobRepository = jobRepository;
        this.transactionManager = transactionManager;
        this.entityManagerFactory = entityManagerFactory;
        this.jpaProductWriter = jpaProductWriter;
    }

    @Bean
    public FlatFileItemReader<Product> reader() {
        return new FlatFileItemReaderBuilder<Product>()
                .name("productItemReader")
                .resource(new ClassPathResource("product.csv")) // resources 폴더의 product.csv 파일을 읽음
                .linesToSkip(1) // 헤더(첫 번째 줄) 건너뛰기
                .delimited()
                .names(new String[]{"id", "name", "price"}) // CSV 컬럼 이름 지정
                .fieldSetMapper(new BeanWrapperFieldSetMapper<Product>() {{
                    setTargetType(Product.class); // Product 객체로 매핑
                }})
                .build();
    }

    @Bean
    public ItemProcessor<Product, Product> processor() {
        // 데이터를 가공하는 로직. 여기서는 추가 가공 없이 그대로 반환
        return item -> item;
    }

//    @Bean
//    public ItemWriter<Product> writer() {
//        JpaItemWriter<Product> writer = new JpaItemWriter<>();
//        writer.setEntityManagerFactory(entityManagerFactory);
//        return writer;
//    }

    @Bean
    public Step importProductStep() {
        return new StepBuilder("importProductStep", jobRepository)
                .<Product, Product>chunk(100, transactionManager) // 10개씩 묶어서 처리
                .reader(reader())
                .processor(processor())
                .writer(jpaProductWriter) // Use your custom ItemWriter
                .build();
    }

    @Bean
    public Job importProductsJob() {
        return new JobBuilder("importProductsJob", jobRepository)
                .start(importProductStep())
                .build();
    }
}