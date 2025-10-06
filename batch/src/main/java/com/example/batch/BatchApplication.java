package com.example.batch;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.nio.charset.Charset;

@SpringBootApplication
public class BatchApplication {

    public static void main(String[] args) {
        SpringApplication.run(BatchApplication.class, args);
    }

    @Bean
    ApplicationRunner runner(Job job, JobLauncher jobLauncher) {
        return _ -> jobLauncher.run(job, new JobParametersBuilder().toJobParameters());
    }


    @Bean
    FlatFileItemReader<Dog> dogFlatFileItemReader(@Value("file://${HOME}/Desktop/2025-10-06-devoxx-bootiful-spring-boot-deep-dive/dogs.csv") Resource csv)
            throws Exception {
        var contents = csv.getContentAsString(Charset.defaultCharset());
        IO.println(contents);
        return new FlatFileItemReaderBuilder<Dog>()
                .resource(csv)
                .name("dogFlatFileItemReader")
                .delimited().delimiter(",")
                .names("id,name,description,dob,owner,gender,image".split(","))
                .fieldSetMapper(fieldSet -> new Dog(fieldSet.readInt("id"),
                        fieldSet.readString("name"), fieldSet.readString("description")))
                .linesToSkip(1)
                .build();
    }

    @Bean
    Step step1(JobRepository jobRepository, PlatformTransactionManager transactionManager,
               FlatFileItemReader<Dog> dogFlatFileItemReader, ItemWriter<Dog> dogItemWriter
    ) {
        return new StepBuilder("step1", jobRepository)
                .<Dog, Dog>chunk(10, transactionManager)
                .reader(dogFlatFileItemReader)
//                .processor()
                .writer(dogItemWriter)
                .build();
    }


    @Bean
    JdbcBatchItemWriter<Dog> dogJdbcBatchItemWriter(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<Dog>()
                .dataSource(dataSource)
                .sql("INSERT INTO DOG (ID, NAME, DESCRIPTION) VALUES  (?,?,?)")
                .itemPreparedStatementSetter((item, ps) -> {
                    ps.setInt(1, item.id());
                    ps.setString(2, item.name());
                    ps.setString(3, item.description());
                })
                .build();
    }
//    @Bean
//    ItemWriter<Dog> dogItemWriter() {
//        return chunk -> chunk.forEach(dog -> IO.println(dog));
//    }

    @Bean
    Job job(JobRepository repository, Step step) {
        return new JobBuilder("dogsEtlJob", repository)
                .start(step)
                .incrementer(new RunIdIncrementer())
                .build();
    }
}

record Dog(int id, String name, String description) {
}