package com.dsabyte.urlshortner.url;

import com.dsabyte.urlshortner.services.SortCodeService;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Service
public class UrlService {
    UrlRepository urlRepository;
    StringRedisTemplate stringRedisTemplate;
    Environment environment;

    UrlService(
            UrlRepository urlRepository,
            Environment environment,
            StringRedisTemplate stringRedisTemplate
    ) {
        this.urlRepository = urlRepository;
        this.environment = environment;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public String sortUrl(UrlSortPayloadDTO urlSortPayloadDTO) {
        // TODO: make sure that duplicates URLs does not exists in DB
        UrlModel urlModel = new UrlModel();
        urlModel.setUrl(urlSortPayloadDTO.url());
        urlModel.setSortCode(SortCodeService.getSortCode());
        urlModel.setCreatedAt(new Date().getTime());

        Calendar calendar = Calendar.getInstance();

        int expiryDays = urlSortPayloadDTO.expiryDays() != null
                ? urlSortPayloadDTO.expiryDays()
                : Integer.parseInt(
                Objects
                        .requireNonNull(environment.getProperty("default.url.expiry_days"))
        );

        calendar.add(Calendar.DATE, expiryDays);
        urlModel.setExpiryDate(calendar.getTime().getTime());

        urlRepository.insert(urlModel);
        return urlModel.getSortCode();
    }

    public String getLongUrl(String sortCode) {
        String longUrl = stringRedisTemplate
                .opsForValue()
                .get(sortCode);

        if(longUrl != null){
            return longUrl;
        }

        UrlModel urlModel = urlRepository.findBySortCode(sortCode);

        if(urlModel == null){
            return null;
        }

        stringRedisTemplate
                .opsForValue()
                .set(sortCode,urlModel.getUrl());

        return urlModel.getUrl();
    }

    public List<UrlModel> getAllUrls() {
        return urlRepository.findAll();
    }
}
