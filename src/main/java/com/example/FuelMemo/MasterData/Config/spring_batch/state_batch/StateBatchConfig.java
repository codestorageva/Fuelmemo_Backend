

package com.example.FuelMemo.MasterData.Config.spring_batch.state_batch;

import com.example.FuelMemo.MasterData.Dto.StateDto;
import com.example.FuelMemo.MasterData.Entity.State;
import com.example.FuelMemo.MasterData.Repository.CountryRepository;
import com.example.FuelMemo.MasterData.Service.Impl.StateServiceImpl;
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
public class StateBatchConfig {

    private final CountryRepository countryRepository;

    @Autowired
    public StateBatchConfig(CountryRepository countryRepository) {
        this.countryRepository = countryRepository;
    }

    @Bean
    @Qualifier("stateReaderJob")
    public Job stateReaderJob(JobRepository jobRepository,
                              PlatformTransactionManager transactionManager,
                              Step chunkStepState) {
        return new JobBuilder("stateReaderJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(chunkStepState)
                .build();
    }

    @Bean
    public Step chunkStepState(JobRepository jobRepository,
                               PlatformTransactionManager transactionManager,
                               FlatFileItemReader<StateDto> reader,
                               ItemProcessor<StateDto, State> processor,
                               ItemWriter<State> writer) {
        return new StepBuilder("stateReaderStep", jobRepository)
                .<StateDto, State>chunk(1000, transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .allowStartIfComplete(true)
                .build();
    }

    @Bean
    @StepScope
    public ItemWriter<State> stateWriter() { return new StateWriter(); }

    @Bean
    @StepScope
    public ItemProcessor<StateDto, State> stateProcessor(StateServiceImpl stateService) {
        return new CompositeItemProcessor<>(new StateProcessor(countryRepository, stateService.getUserName()));
    }

    @Bean
    @StepScope
    public FlatFileItemReader<StateDto> stateReader(@Value("#{jobParameters[inputFileState]}") String pathToFile) {
        return new FlatFileItemReaderBuilder<StateDto>()
                .name("stateReader")
                .resource(new FileSystemResource(new File(pathToFile)))
                .delimited()
                .names("countryName", "stateName", "status")
                .fieldSetMapper(new BeanWrapperFieldSetMapper<>() {{ setTargetType(StateDto.class); }})
                .linesToSkip(1)
                .strict(false)
                .build();
    }
}