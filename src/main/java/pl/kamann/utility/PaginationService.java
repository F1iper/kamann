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
        int pageNumber = pageable.getPageNumber();
        int pageSize = pageable.getPageSize();

        pageSize = (pageSize > 0) ? Math.min(pageSize, MAX_PAGE_SIZE) : DEFAULT_PAGE_SIZE;

        pageNumber = (pageNumber <= 0) ? 0 : pageNumber - 1;

        Sort sort = (pageable.getSort() != null && pageable.getSort().isSorted()) ? pageable.getSort() : Sort.unsorted();

        return PageRequest.of(pageNumber, pageSize, sort);
    }
}