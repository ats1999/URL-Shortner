package com.dsabyte.urlshortner.url;

import com.dsabyte.urlshortner.services.SortCodeService;
import com.dsabyte.urlshortner.services.ZooCurator;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
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

    public String sortUrl(UrlSortPayloadDTO urlSortPayloadDTO) throws Exception {
        UrlModel existingUrl = getUrl(urlSortPayloadDTO.url());

        if (existingUrl != null) {
            return existingUrl.getSortCode();
        }

        boolean encodePathToBase64 = true;
        InterProcessMutex lock = ZooCurator
                .getLock(
                        urlSortPayloadDTO.url(),
                        encodePathToBase64
                );

        try {
            lock.acquire();

            // it's possible that url might have been stored by some other client
            // we need to double-check in-order to ensure that no duplicate urls present in DB
            UrlModel url = getUrl(urlSortPayloadDTO.url());

            return Objects
                    .requireNonNullElseGet(url, () -> {
                        try {
                            return saveUrl(urlSortPayloadDTO);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .getSortCode();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        } finally {
            lock.release();
        }
    }

    public String getLongUrl(String sortCode) {
        String longUrl = stringRedisTemplate
                .opsForValue()
                .get(sortCode);

        if (longUrl != null) {
            return longUrl;
        }

        UrlModel urlModel = urlRepository.findBySortCode(sortCode);

        if (urlModel == null) {
            return null;
        }

        stringRedisTemplate
                .opsForValue()
                .set(sortCode, urlModel.getUrl());

        return urlModel.getUrl();
    }

    public List<UrlModel> getAllUrls() {
        return urlRepository.findAll();
    }

    private UrlModel getUrl(String longUrl) {
        return urlRepository
                .findByUrl(longUrl);
    }

    private UrlModel saveUrl(UrlSortPayloadDTO urlSortPayloadDTO) throws Exception {
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
        return urlModel;
    }
}
