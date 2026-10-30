package com.honouryourmentor.feed.service;

import com.honouryourmentor.common.PagedResponse;
import com.honouryourmentor.feed.dto.FeedFilterCriteria;
import com.honouryourmentor.tribute.dto.TributeDto;
import com.honouryourmentor.tribute.entity.Tribute;
import com.honouryourmentor.tribute.repository.TributeRepository;
import com.honouryourmentor.tribute.service.TributeService;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FeedService {

    private final TributeRepository tributeRepository;
    private final TributeService tributeService;

    @Transactional(readOnly = true)
    public PagedResponse<TributeDto> getFilteredFeed(FeedFilterCriteria criteria, String currentUserId) {
        Specification<Tribute> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Always only public tributes in feed
            predicates.add(cb.equal(root.get("privacy"), "PUBLIC"));

            if (criteria.getKind() != null && !criteria.getKind().isBlank() && !"all".equalsIgnoreCase(criteria.getKind())) {
                predicates.add(cb.equal(cb.lower(root.get("kind")), criteria.getKind().toLowerCase()));
            }

            if (criteria.getRelationship() != null && !criteria.getRelationship().isBlank() && !"all".equalsIgnoreCase(criteria.getRelationship())) {
                predicates.add(cb.equal(cb.lower(root.get("relationship")), criteria.getRelationship().toLowerCase()));
            }

            if (criteria.getSearch() != null && !criteria.getSearch().isBlank()) {
                String term = "%" + criteria.getSearch().trim().toLowerCase() + "%";
                Predicate searchTitle = cb.like(cb.lower(root.get("title")), term);
                Predicate searchMentor = cb.like(cb.lower(root.get("mentorName")), term);
                Predicate searchQuote = cb.like(cb.lower(root.get("quote")), term);
                Predicate searchStory = cb.like(cb.lower(root.get("story")), term);
                predicates.add(cb.or(searchTitle, searchMentor, searchQuote, searchStory));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        PageRequest pageRequest = PageRequest.of(
                criteria.getPage(),
                criteria.getSize(),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<Tribute> page = tributeRepository.findAll(spec, pageRequest);
        List<TributeDto> content = page.getContent().stream()
                .map(t -> tributeService.mapToDto(t, currentUserId))
                .toList();

        return PagedResponse.<TributeDto>builder()
                .content(content)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }
}
