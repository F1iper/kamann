package pl.kamann.config.codes;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PaginationCodes {
    INVALID_PAGE_INDEX("INVALID_PAGE_INDEX"),
    INVALID_PAGE_SIZE("INVALID_PAGE_SIZE"),
    PAGE_INDEX_CANNOT_BE_0("Page index must not be less than zero"),
    PAGE_SIZE_LESS_THAN_0("Page size must not be less than one");

    private final String code;
}
