/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.wonwoo.dynamodb.autoconfigure;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import org.socialsignin.spring.data.dynamodb.core.DynamoDBTemplate;
import org.socialsignin.spring.data.dynamodb.mapping.DynamoDBMappingContext;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.domain.EntityScanner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

/**
 * @author wonwoo
 */
@Configuration
@ConditionalOnClass(DynamoDBTemplate.class)
@AutoConfigureAfter({DynamoAutoConfiguration.class, EmbeddedDynamoAutoConfiguration.class})
@EnableConfigurationProperties(DynamoProperties.class)
public class DynamoDataAutoConfiguration {

  private final ApplicationContext applicationContext;

  public DynamoDataAutoConfiguration(ApplicationContext applicationContext) {
    this.applicationContext = applicationContext;
  }

  @Bean
  @ConditionalOnMissingBean
  public DynamoDBMappingContext dynamoDBMappingContext() throws ClassNotFoundException {
    DynamoDBMappingContext context = new DynamoDBMappingContext();
    context.setInitialEntitySet(new EntityScanner(this.applicationContext)
        .scan(DynamoDBTable.class));
    return context;
  }

  @Bean
  @ConditionalOnMissingBean
  public DynamoDbMapping dynamoDbMapping(CreateTable createTable,
                                         DynamoDBMappingContext context) {
    return new DynamoDbMapping(createTable, context);
  }

  @Bean
  @ConditionalOnMissingBean
  public CreateTable createTable(AmazonDynamoDB amazonDynamoDB,
                                 DynamoProperties properties) {
    DynamoDbCreateTable createTable = new DynamoDbCreateTable(amazonDynamoDB);
    createTable.setReadCapacityUnits(properties.getReadCapacityUnits());
    createTable.setWriteCapacityUnits(properties.getWriteCapacityUnits());
    return createTable;
  }

  @Bean
  @ConditionalOnMissingBean
  public DynamoDBMapperConfig dynamoDBMapperConfig() {
    return DynamoDBMapperConfig.DEFAULT;
  }

  @Bean
  @ConditionalOnMissingBean
  public DynamoDBMapper dynamoDBMapper(AmazonDynamoDB amazonDynamoDB, DynamoDBMapperConfig dynamoDBMapperConfig) {
    return new DynamoDBMapper(amazonDynamoDB, dynamoDBMapperConfig);
  }

  @Bean
  @ConditionalOnMissingBean
  public DynamoDBTemplate dynamoDBTemplate(AmazonDynamoDB amazonDynamoDB, DynamoDBMapper dynamoDBMapper) {
    return new DynamoDBTemplate(amazonDynamoDB, dynamoDBMapper);
  }

  @ConditionalOnProperty(prefix = "spring.data.dynamodb.ddl", name = "enabled", havingValue = "true", matchIfMissing = false)
  @Configuration
  protected static class DynamoDbCreateTableAutoConfiguration {
    @Bean
    public DynamoDbCreateTableBeanPostProcessor dynamoDbCreateTableBeanPostProcessor() {
      return new DynamoDbCreateTableBeanPostProcessor();
    }
  }
}