package com.dsabyte.urlshortner.url;

import jakarta.validation.constraints.NotEmpty;

public record UrlSortPayloadDTO(@NotEmpty String url, Integer expiryDays) {

}
