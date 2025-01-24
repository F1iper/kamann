package pl.kamann.config.codes;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PaginationCodes {
    PAGE_INDEX_CANNOT_BE_0("PAGE_INDEX_CANNOT_BE_0", "Page index must not be less than zero"),
    PAGE_SIZE_LESS_THAN_0("PAGE_SIZE_LESS_THAN_0", "Page size must not be less than one");

    private final String code;
    private final String message;

}