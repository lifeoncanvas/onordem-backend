package com.honouryourmentor.admin.repository;

import com.honouryourmentor.admin.entity.ContentReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContentReportRepository extends JpaRepository<ContentReport, String> {
    List<ContentReport> findByStatusOrderByCreatedAtDesc(String status);
}
