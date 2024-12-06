package pl.kamann.utility;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class PaginationService {
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;

    public Pageable validatePageable(Pageable pageable) {
        int pageSize = pageable.getPageSize() > 0 ? pageable.getPageSize() : DEFAULT_PAGE_SIZE;
        pageSize = Math.min(pageSize, MAX_PAGE_SIZE);

        int pageNumber = Math.max(0, pageable.getPageNumber());

        Sort sort = pageable.getSort() == null ? Sort.unsorted() : pageable.getSort();

        return PageRequest.of(pageNumber, pageSize, sort);
    }
}