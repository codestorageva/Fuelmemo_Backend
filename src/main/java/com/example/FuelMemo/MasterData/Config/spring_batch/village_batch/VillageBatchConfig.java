package com.example.FuelMemo.MasterData.Config.spring_batch.village_batch;


import com.example.FuelMemo.MasterData.Dto.VillageDto;
import com.example.FuelMemo.MasterData.Entity.Village;
import com.example.FuelMemo.MasterData.Repository.SubDistrictRepository;
import com.example.FuelMemo.MasterData.Service.Impl.VillageServiceImpl;
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
public class VillageBatchConfig {
    private final SubDistrictRepository subDistrictRepository;

    @Autowired
    public VillageBatchConfig(SubDistrictRepository subDistrictRepository) {
        this.subDistrictRepository = subDistrictRepository;
    }

    @Bean
    @Qualifier("villageReaderJob")
    public Job villageReaderJob(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager, FlatFileItemReader<VillageDto> reader) {
        return new JobBuilder("villageReaderJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(chunkStepVillage(jobRepository, platformTransactionManager, reader))
                .build();
    }

    @Bean
    public Step chunkStepVillage(JobRepository jobRepository, PlatformTransactionManager transactionManager, FlatFileItemReader<VillageDto> reader) {
        return new StepBuilder("villageReaderStep", jobRepository)
                .<VillageDto, Village>chunk(1000, transactionManager)
                .reader(reader)
                .processor(villageProcessor())
                .writer(villageWriter())
                .allowStartIfComplete(true)
//                .taskExecutor(villageTaskExecutor())
                .build();
    }

//    @Bean
//    public TaskExecutor villageTaskExecutor(){
//        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
//        taskExecutor.setCorePoolSize(20); // Set the number of concurrent threads
//        taskExecutor.setMaxPoolSize(40); // Set the maximum number of threads
//        taskExecutor.setQueueCapacity(100); // Set the queue capacity for pending tasks
//        return taskExecutor;
//    }

    @Bean
    @StepScope
    public ItemWriter<Village> villageWriter() {
        return new VillageWriter();
    }

    @Bean
    @StepScope
    public ItemProcessor<VillageDto, Village> villageProcessor() {
        return new CompositeItemProcessor<>(new VillageProcessor(subDistrictRepository, VillageServiceImpl.getUserName()));
    }

    @Bean
    @StepScope
    public FlatFileItemReader<VillageDto> villageReader(@Value("#{jobParameters[inputFileVillage]}") String pathToFile) {
        return new FlatFileItemReaderBuilder<VillageDto>()
                .name("villageReader")
                .resource(new FileSystemResource(new File(pathToFile)))
                .delimited()
                .names(new String[]{
                        "countryName", "stateName", "cityName", "subDistrictName", "villageName", "status"
                })
                .fieldSetMapper(new BeanWrapperFieldSetMapper<>() {{
                    setTargetType(VillageDto.class);
                }})
                .linesToSkip(1)
                .strict(false)
                .build();
    }
}