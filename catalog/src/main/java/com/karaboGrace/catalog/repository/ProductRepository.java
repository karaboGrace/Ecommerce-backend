package com.karaboGrace.catalog.repository;

import com.karaboGrace.catalog.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}