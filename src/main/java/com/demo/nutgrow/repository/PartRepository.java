package com.demo.nutgrow.repository;

import com.demo.nutgrow.model.Part;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
@SpringBootApplication
public interface PartRepository extends JpaRepository<Part, Integer> {

}
