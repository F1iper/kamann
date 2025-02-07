package pl.kamann.utility;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PaginationServiceTest {

    private PaginationService paginationService;

    @BeforeEach
    void setUp() {
        paginationService = new PaginationService();
    }

    @Test
    void shouldUseDefaultPageSizeWhenPageSizeIsZeroOrNegative() {
        Pageable pageable = mock(Pageable.class);
        when(pageable.getPageSize()).thenReturn(0);
        when(pageable.getPageNumber()).thenReturn(1);
        when(pageable.getSort()).thenReturn(null);

        Pageable result = paginationService.validatePageable(pageable);

        assertEquals(20, result.getPageSize());
        assertEquals(0, result.getPageNumber());
        assertEquals(Sort.unsorted(), result.getSort());
    }

    @Test
    void shouldCapPageSizeToMaximum() {
        Pageable pageable = PageRequest.of(2, 200);
        Pageable result = paginationService.validatePageable(pageable);

        assertEquals(100, result.getPageSize());
        assertEquals(1, result.getPageNumber());
    }

    @Test
    void shouldAdjustPageNumberForOneBasedIndexing() {
        Pageable pageable = PageRequest.of(1, 10);
        Pageable result = paginationService.validatePageable(pageable);

        assertEquals(0, result.getPageNumber());
        assertEquals(10, result.getPageSize());
    }

    @Test
    void shouldReturnFirstPageForNegativePageNumber() {
        Pageable pageable = mock(Pageable.class);
        when(pageable.getPageSize()).thenReturn(10);
        when(pageable.getPageNumber()).thenReturn(-5);
        when(pageable.getSort()).thenReturn(Sort.unsorted());

        Pageable result = paginationService.validatePageable(pageable);

        assertEquals(0, result.getPageNumber());
        assertEquals(10, result.getPageSize());
        assertEquals(Sort.unsorted(), result.getSort());
    }

    @Test
    void shouldPreserveValidPageNumberAndSize() {
        Pageable pageable = PageRequest.of(2, 15);
        Pageable result = paginationService.validatePageable(pageable);

        assertEquals(1, result.getPageNumber());
        assertEquals(15, result.getPageSize());
    }
}
