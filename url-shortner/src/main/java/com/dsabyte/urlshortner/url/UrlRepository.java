package com.dsabyte.urlshortner.url;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UrlRepository extends MongoRepository<UrlModel,String> {
    UrlModel findBySortCode(String sortCode);
    UrlModel findByUrl(String url);
}
