package com.vodafone.ecommerce.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.Serializable;

@NoRepositoryBean
interface BaseRepository<T, U extends Serializable> extends JpaRepository<T, U> {
}
