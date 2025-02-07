package pl.kamann.utility;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import pl.kamann.config.pagination.PaginatedResponseDto;
import pl.kamann.config.pagination.PaginationMetaData;

import java.util.List;
import java.util.function.Function;

@Component
public class PaginationUtil {

    public <T, R> PaginatedResponseDto<R> toPaginatedResponse(Page<T> page, Function<T, R> mapper) {
        List<R> content = page.getContent().stream().map(mapper).toList();

        PaginationMetaData metaData = new PaginationMetaData(
                page.getTotalPages(),
                page.getTotalElements()
        );

        return new PaginatedResponseDto<>(content, metaData);
    }
}