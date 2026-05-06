package com.example.FuelMemo.MasterData.Config.spring_batch.sub_district_batch;

import com.example.FuelMemo.MasterData.Dto.SubDistrictDto;
import com.example.FuelMemo.MasterData.Entity.SubDistrict;
import com.example.FuelMemo.MasterData.Repository.DistrictRepository;
import com.example.FuelMemo.MasterData.Service.Impl.SubDistrictServiceImpl;
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
public class SubDistrictBatchConfig {
    private final DistrictRepository districtRepository;

    @Autowired
    public SubDistrictBatchConfig(DistrictRepository districtRepository) {
        this.districtRepository = districtRepository;
    }

    @Bean
    @Qualifier("subDistrictReaderJob")
    public Job subDistrictReaderJob(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager, FlatFileItemReader<SubDistrictDto> reader) {
        return new JobBuilder("subDistrictReaderJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(chunkStepSubDistrict(jobRepository, platformTransactionManager, reader))
                .build();
    }

    @Bean
    public Step chunkStepSubDistrict(JobRepository jobRepository, PlatformTransactionManager transactionManager, FlatFileItemReader<SubDistrictDto> reader) {
        return new StepBuilder("subDistrictReaderStep", jobRepository)
                .<SubDistrictDto, SubDistrict>chunk(1000, transactionManager)
                .reader(reader)
                .processor(subDistrictProcessor())
                .writer(subDistrictWriter())
                .allowStartIfComplete(true)
//                .taskExecutor(subDistrictTaskExecutor())
                .build();
    }

    @Bean
    @StepScope
    public ItemWriter<SubDistrict> subDistrictWriter() {
        return new SubDistrictWriter();
    }

    @Bean
    @StepScope
    public ItemProcessor<SubDistrictDto, SubDistrict> subDistrictProcessor() {
        return new CompositeItemProcessor<>(new SubDistrictProcessor(districtRepository, SubDistrictServiceImpl.getUserName()));
    }

    @Bean
    @StepScope
    public FlatFileItemReader<SubDistrictDto> subDistrictReader(@Value("#{jobParameters[inputFileSubDistrict]}") String pathToFile) {
        return new FlatFileItemReaderBuilder<SubDistrictDto>()
                .name("subDistrictReader")
                .resource(new FileSystemResource(new File(pathToFile)))
                .delimited()
                .names(new String[]{
                        "countryName", "stateName", "cityName", "subDistrictName", "status"
                })
                .fieldSetMapper(new BeanWrapperFieldSetMapper<>() {{
                    setTargetType(SubDistrictDto.class);
                }})
                .linesToSkip(1)
                .strict(false)  // Set to non-strict mode
                .build();
    }
}