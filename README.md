# URL-Shortner

## Functional Requirements 
- User should be able to get a short url against a long url 
- User should be able to redirected to original url after clicking on sort url
- URLs should expire after certain time

## Non-Function Requirements
- System should be scalable 
- System should work with low latency

## Traffic
- R/W - assuming 200:1 ration of read/write
- Number of unique urls shortened per month = 10M
- Number of unique urls shortened per second = 10^8/(30\*24\*60\*60) = 3.85 URL shortened per second
- Number of redirection per second - 3.85\*200 (R/W = 200) = 770

## Storage Requirements
- URL expiry time - 10 years 
- Size of each record (sort url, long url, created date, etc) = 500 bytes
- Total number of urls in 10 year = 10\*12\*10^8 = ~10^10
- Total size = (10^10\*500)/(1000\*1024\*1024\*1024) = 5TB

## Memory Requirements 
> This is estimation for the cache memory requirements 
> 

- Redirection req per second = 770
- Redirection req per day = 770\*24\*60\*60 = 66.528M
- Total size =  66.528 × 10^6\*500/(1000\*1024*1024) = ~31GB
- Assuming that we will cache only 20% of the request, so total memory requirements = 6.1GB

