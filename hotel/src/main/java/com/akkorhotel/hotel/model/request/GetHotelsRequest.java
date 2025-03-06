package com.akkorhotel.hotel.model.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetHotelsRequest {
    private int page;
    private int pageSize;
    private String filter;

    @Builder.Default
    private GetHotelsFilters filters = new GetHotelsFilters();
}
