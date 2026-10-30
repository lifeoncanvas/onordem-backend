package com.honouryourmentor.tribute.repository;

import com.honouryourmentor.tribute.entity.TributeMedia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TributeMediaRepository extends JpaRepository<TributeMedia, String> {
    List<TributeMedia> findByTributeIdOrderBySortOrderAsc(String tributeId);
}
