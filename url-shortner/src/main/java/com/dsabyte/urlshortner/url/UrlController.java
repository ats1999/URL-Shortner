package com.dsabyte.urlshortner.url;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.net.MalformedURLException;
import java.util.List;

@RestController
public class UrlController {

    @Autowired
    Environment environment;
    UrlService urlService;

    UrlController(UrlService urlService) {
        this.urlService = urlService;
    }

    @PostMapping("/url/sort")
    public SortCodeDTO sortUrl(@RequestBody @Valid UrlSortPayloadDTO urlSortPayloadDTO) throws Exception {
        String sortCode = urlService.sortUrl(urlSortPayloadDTO);
        return new SortCodeDTO(urlSortPayloadDTO.url(), sortCode);
    }

    @GetMapping("/{sortCode:^(?!.*index.).*$}")
    public void redirectToLongUrl(@PathVariable @Valid @NotEmpty String sortCode, HttpServletResponse httpServletResponse) throws MalformedURLException {
        String longUrl = urlService.getLongUrl(sortCode);

        String urlToRedirect = longUrl == null
                ? environment.getProperty("app.host")
                : longUrl;

        httpServletResponse.setHeader(HttpHeaders.LOCATION, urlToRedirect);
        httpServletResponse.setStatus(HttpStatus.TEMPORARY_REDIRECT.value());
    }

    @GetMapping("/urls")
    public List<UrlModel> getAllUrls() {
        return urlService.getAllUrls();
    }

}
