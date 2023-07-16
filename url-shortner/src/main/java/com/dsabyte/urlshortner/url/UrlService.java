package com.dsabyte.urlshortner.url;

import com.dsabyte.urlshortner.services.SortCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Service
public class UrlService {
    @Autowired
    Environment environment;
    UrlRepository urlRepository;

    UrlService(UrlRepository urlRepository) {
        this.urlRepository = urlRepository;
    }

    public String sortUrl(UrlSortPayloadDTO urlSortPayloadDTO) {
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
        UrlModel urlModel = urlRepository.findBySortCode(sortCode);

        return urlModel != null
                ? urlModel.getUrl()
                : null;
    }

    public List<UrlModel> getAllUrls(){
        return urlRepository.findAll();
    }
}
