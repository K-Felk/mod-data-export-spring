package org.folio.des.config;

import java.util.HashMap;
import java.util.Map;

import org.folio.des.builder.job.*;
import org.folio.des.builder.scheduling.EdifactScheduledTaskBuilder;
import org.folio.des.builder.scheduling.ScheduledTaskBuilder;
import org.folio.des.client.ConfigurationClient;
import org.folio.des.converter.DefaultExportConfigToModelConfigConverter;
import org.folio.des.converter.DefaultModelConfigToExportConfigConverter;
import org.folio.des.converter.ExportConfigConverterResolver;
import org.folio.des.converter.aqcuisition.EdifactExportConfigToModelConfigConverter;
import org.folio.des.converter.aqcuisition.EdifactOrdersExportConfigToTaskTriggerConverter;
import org.folio.des.domain.dto.*;
import org.folio.des.repository.FileSourceProperties;
import org.folio.des.scheduling.acquisition.AcqSchedulingProperties;
import org.folio.des.scheduling.acquisition.EdifactOrdersExportJobScheduler;
import org.folio.des.scheduling.acquisition.EdifactScheduledJobInitializer;
import org.folio.des.service.DownloadFileService;
import org.folio.des.service.DownloadFileServiceResolver;
import org.folio.des.service.DownloadManager;
import org.folio.des.service.JobService;
import org.folio.des.service.config.ExportConfigService;
import org.folio.des.service.config.acquisition.EdifactOrdersExportService;
import org.folio.des.service.config.impl.BaseExportConfigService;
import org.folio.des.service.config.impl.BurSarFeesFinesExportConfigService;
import org.folio.des.service.config.impl.ExportConfigServiceResolver;
import org.folio.des.service.config.impl.ExportTypeBasedConfigManager;
import org.folio.des.service.impl.aqcuisition.SFTPDownloadFileService;
import org.folio.des.validator.BurSarFeesFinesExportParametersValidator;
import org.folio.des.validator.ExportConfigValidatorResolver;
import org.folio.des.validator.acquisition.EdifactOrdersExportParametersValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.validation.Validator;

@Configuration
@ComponentScan("org.folio.des")
public class ServiceConfiguration {
  @Bean
  ExportConfigConverterResolver exportConfigConverterResolver(DefaultExportConfigToModelConfigConverter defaultExportConfigToModelConfigConverter,
                      EdifactExportConfigToModelConfigConverter edifactExportConfigToModelConfigConverter) {
    Map<ExportType, Converter<ExportConfig, ModelConfiguration>> converters = new HashMap<>();
    converters.put(ExportType.BURSAR_FEES_FINES, defaultExportConfigToModelConfigConverter);
    converters.put(ExportType.EDIFACT_ORDERS_EXPORT, edifactExportConfigToModelConfigConverter);
    return new ExportConfigConverterResolver(converters, defaultExportConfigToModelConfigConverter);
  }

  @Bean
  ExportConfigValidatorResolver exportConfigValidatorResolver(BurSarFeesFinesExportParametersValidator burSarFeesFinesExportParametersValidator,
                      EdifactOrdersExportParametersValidator edifactOrdersExportParametersValidator) {
    Map<String, Validator> validators = new HashMap<>();
    validators.put(ExportConfigValidatorResolver.buildKey(ExportType.BURSAR_FEES_FINES, ExportTypeSpecificParameters.class),
      burSarFeesFinesExportParametersValidator);
    validators.put(ExportConfigValidatorResolver.buildKey(ExportType.EDIFACT_ORDERS_EXPORT, ExportTypeSpecificParameters.class),
      edifactOrdersExportParametersValidator);
    return new ExportConfigValidatorResolver(validators);
  }

  @Bean
  DownloadFileServiceResolver downloadFileServiceResolver(SFTPDownloadFileService sftpDownloadFileService) {
    Map<DownloadSources, DownloadFileService> downloadService = new HashMap<>();
    downloadService.put(DownloadSources.SFTP, sftpDownloadFileService);
    return new DownloadFileServiceResolver(downloadService);
  }

  @Bean
  ExportTypeBasedConfigManager exportTypeBasedConfigManager(ConfigurationClient client,
                      ExportConfigServiceResolver exportConfigServiceResolver,
                      BaseExportConfigService baseExportConfigService,
                      DefaultModelConfigToExportConfigConverter defaultModelConfigToExportConfigConverter) {
    return new ExportTypeBasedConfigManager(client, exportConfigServiceResolver,
                      baseExportConfigService, defaultModelConfigToExportConfigConverter);
  }

  @Bean
  DownloadManager downloadManager(DownloadFileServiceResolver downloadFileServiceResolver, FileSourceProperties fileSourceProperties) {
    return new DownloadManager(downloadFileServiceResolver, fileSourceProperties);
  }

  @Bean
  BurSarFeesFinesExportConfigService burSarExportConfigService(ConfigurationClient client, ExportConfigValidatorResolver exportConfigValidatorResolver,
            DefaultModelConfigToExportConfigConverter defaultModelConfigToExportConfigConverter,
            ExportConfigConverterResolver  exportConfigConverterResolver) {
    return new BurSarFeesFinesExportConfigService(client, defaultModelConfigToExportConfigConverter,
            exportConfigConverterResolver, exportConfigValidatorResolver);
  }

  @Bean
  EdifactOrdersExportService edifactOrdersExportService(ConfigurationClient client, ExportConfigValidatorResolver exportConfigValidatorResolver,
           DefaultModelConfigToExportConfigConverter defaultModelConfigToExportConfigConverter,
           ExportConfigConverterResolver  exportConfigConverterResolver,
           EdifactOrdersExportJobScheduler exportJobScheduler) {
    return new EdifactOrdersExportService(client, defaultModelConfigToExportConfigConverter,
      exportConfigConverterResolver, exportConfigValidatorResolver, exportJobScheduler);
  }

  @Bean
  BaseExportConfigService baseExportConfigService(ConfigurationClient client, ExportConfigValidatorResolver exportConfigValidatorResolver,
                        DefaultModelConfigToExportConfigConverter defaultModelConfigToExportConfigConverter,
                        ExportConfigConverterResolver exportConfigConverterResolver) {
    return new BaseExportConfigService(client, defaultModelConfigToExportConfigConverter,
                        exportConfigConverterResolver, exportConfigValidatorResolver);
  }


  @Bean
  ExportConfigServiceResolver exportConfigServiceResolver(BurSarFeesFinesExportConfigService burSarFeesFinesExportConfigService,
                          EdifactOrdersExportService edifactOrdersExportService) {
    Map<ExportType, ExportConfigService> exportConfigServiceMap = new HashMap<>();
    exportConfigServiceMap.put(ExportType.BURSAR_FEES_FINES, burSarFeesFinesExportConfigService);
    exportConfigServiceMap.put(ExportType.EDIFACT_ORDERS_EXPORT, edifactOrdersExportService);
    return new ExportConfigServiceResolver(exportConfigServiceMap);
  }

  @Bean JobCommandBuilderResolver jobCommandBuilderResolver(BulkEditQueryJobCommandBuilder bulkEditQueryJobCommandBuilder,
                          BurSarFeeFinesJobCommandBuilder burSarFeeFinesJobCommandBuilder,
                          CirculationLogJobCommandBuilder circulationLogJobCommandBuilder,
                          EdifactOrdersJobCommandBuilder edifactOrdersJobCommandBuilder) {
    Map<ExportType, JobCommandBuilder> converters = new HashMap<>();
    converters.put(ExportType.BULK_EDIT_QUERY, bulkEditQueryJobCommandBuilder);
    converters.put(ExportType.BURSAR_FEES_FINES, burSarFeeFinesJobCommandBuilder);
    converters.put(ExportType.CIRCULATION_LOG, circulationLogJobCommandBuilder);
    converters.put(ExportType.EDIFACT_ORDERS_EXPORT, edifactOrdersJobCommandBuilder);
    return new JobCommandBuilderResolver(converters);
  }

  @Bean ScheduledTaskBuilder edifactScheduledTaskBuilder(JobService jobService, FolioExecutionContextHelper contextHelper,
    AcqSchedulingProperties acqSchedulingProperties) {
    return new EdifactScheduledTaskBuilder(jobService, contextHelper, acqSchedulingProperties);
  }

  @Bean EdifactOrdersExportJobScheduler edifactOrdersExportJobScheduler(ScheduledTaskBuilder edifactScheduledTaskBuilder,
                    EdifactOrdersExportConfigToTaskTriggerConverter triggerConverter,
                    EdifactScheduledJobInitializer edifactScheduledJobInitializer,
                    @Value("${folio.schedule.acquisition.poolSize:10}") int poolSize) {
    return new EdifactOrdersExportJobScheduler(new ThreadPoolTaskScheduler(), triggerConverter,
      edifactScheduledTaskBuilder, poolSize, edifactScheduledJobInitializer);
  }

  @Bean
  AcqSchedulingProperties acqSchedulingProperties(
                    @Value("${folio.schedule.acquisition.runOnlyIfModuleRegistered:true}") String runOnlyIfModuleRegistered) {
    return new AcqSchedulingProperties(runOnlyIfModuleRegistered);
  }

  @Bean
  EdifactScheduledJobInitializer edifactScheduledJobInitializer(BaseExportConfigService baseExportConfigService,
                    FolioExecutionContextHelper contextHelper, AcqSchedulingProperties acqSchedulingProperties) {
    return new EdifactScheduledJobInitializer(baseExportConfigService, contextHelper, acqSchedulingProperties);
  }
}
