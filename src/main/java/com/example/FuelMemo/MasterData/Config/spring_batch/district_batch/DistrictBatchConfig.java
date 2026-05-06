package com.example.FuelMemo.MasterData.Config.spring_batch.district_batch;


import com.example.FuelMemo.MasterData.Dto.DistrictDto;
import com.example.FuelMemo.MasterData.Entity.District;
import com.example.FuelMemo.MasterData.Repository.CountryRepository;
import com.example.FuelMemo.MasterData.Repository.StateRepository;
import com.example.FuelMemo.MasterData.Service.Impl.DistrictServiceImpl;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.support.CompositeItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

import java.io.File;

@Configuration
public class DistrictBatchConfig {
    private final StateRepository stateRepository;
    private final CountryRepository countryRepository;

    @Autowired
    public DistrictBatchConfig(StateRepository stateRepository, CountryRepository countryRepository) {
        this.stateRepository = stateRepository;
        this.countryRepository = countryRepository;
    }

    @Bean
    @Qualifier("districtReaderJob")
    public Job districtReaderJob(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager, FlatFileItemReader<DistrictDto> reader) {
        return new JobBuilder("districtReaderJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(chunkStepDistrict(jobRepository, platformTransactionManager, reader))
                .build();
    }

    @Bean
    public Step chunkStepDistrict(JobRepository jobRepository, PlatformTransactionManager transactionManager, FlatFileItemReader<DistrictDto> reader) {
        return new StepBuilder("districtReaderStep", jobRepository)
                .<DistrictDto, District>chunk(1000, transactionManager)
                .reader(reader)
                .processor(districtProcessor())
                .writer(districtWriter())
                .allowStartIfComplete(true)
//                .taskExecutor(districtTaskExecutor())
                .build();
    }

//    @Bean
//    public TaskExecutor districtTaskExecutor() {
//        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
//        taskExecutor.setCorePoolSize(20); // Set the number of concurrent threads
//        taskExecutor.setMaxPoolSize(40); // Set the maximum number of threads
//        taskExecutor.setQueueCapacity(100); // Set the queue capacity for pending tasks
//        return taskExecutor;
//    }

    @Bean
    @StepScope
    public ItemWriter<District> districtWriter() {
        return new DistrictWriter();
    }

    @Bean
    @StepScope
    public ItemProcessor<DistrictDto, District> districtProcessor() {
        return new CompositeItemProcessor<>(new DistrictProcessor(stateRepository, DistrictServiceImpl.getUserName()));
    }

    @Bean
    @StepScope
    public FlatFileItemReader<DistrictDto> districtReader(@Value("#{jobParameters[inputFileDistrict]}") String pathToFile) {
        return new FlatFileItemReaderBuilder<DistrictDto>()
                .name("districtReader")
                .resource(new FileSystemResource(new File(pathToFile)))
                .delimited()
                .delimiter(",")
//                .names(new String[]{
//                        "countryName",
//                        "stateName",
//                        "cityName",
//                        "status",
//                        "apiCityName"
//                })
                .names(new String[]{
                        "countryName",
                        "stateName",
                        "cityName",
                        "status"
                })
                .fieldSetMapper(new BeanWrapperFieldSetMapper<>() {{
                    setTargetType(DistrictDto.class);
                }})
                .linesToSkip(1)
                .strict(false)  // Set to non-strict mode
                .build();
    }
}