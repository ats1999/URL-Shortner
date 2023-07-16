package com.dsabyte.urlshortner.url;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document("url")
public class UrlModel {
    private String sortCode;
    private String url;
    private long createdAt;
    private long expiryDate;
}
