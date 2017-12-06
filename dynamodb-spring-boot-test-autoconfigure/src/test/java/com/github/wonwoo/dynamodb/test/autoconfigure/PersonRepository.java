package com.github.wonwoo.dynamodb.test.autoconfigure;

import org.socialsignin.spring.data.dynamodb.repository.EnableScan;

import com.github.wonwoo.dynamodb.repository.DynamoDBRepository;

@EnableScan
public interface PersonRepository extends DynamoDBRepository<Person, String> {
}
