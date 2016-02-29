package io.pivotal.datagov.services;

import io.pivotal.datagov.model.Patent;

import org.springframework.data.repository.CrudRepository;

public interface PatentRepository extends CrudRepository<Patent, Long> {
}
