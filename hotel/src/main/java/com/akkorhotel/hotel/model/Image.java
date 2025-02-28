package com.akkorhotel.hotel.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;

@Getter
@Setter
@Builder
@EqualsAndHashCode
public class Image {

    @Id
    private String id;

    private String name;
    private String url;
    private String uploaderId;

    private ImageCategory category;
    private ImageExtension extension;

}
