package com.ohdoking.csv_reader_batch;

import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class JpaProductWriter implements ItemWriter<Product> {

    @Autowired
    private ProductRepository productRepository;

    @Override
    public void write(Chunk<? extends Product> chunk) throws Exception {
        productRepository.saveAll(chunk);
    }

}