package com.maybank.demo.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import com.maybank.demo.dto.PostResponse;
import com.maybank.demo.exception.ExternalServiceException;
import com.maybank.demo.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostService {

    private static final String EXTERNAL_API_URL = "https://jsonplaceholder.typicode.com/posts";
    private static final String SERVICE_NAME = "JSONPlaceholder";

    private final RestTemplate restTemplate;

    public List<PostResponse> getAllPosts() {
        log.info("Fetching all posts from external API: {}", EXTERNAL_API_URL);

        try {
            PostResponse[] posts = restTemplate.getForObject(EXTERNAL_API_URL, PostResponse[].class);

            if (posts == null) {
                log.warn("Received null response from external API");
                return List.of();
            }

            log.info("Successfully fetched {} posts from external API", posts.length);
            return Arrays.asList(posts);
        } catch (HttpClientErrorException e) {
            log.error("Client error fetching posts from external API - Status: {}, Message: {}",
                    e.getStatusCode(), e.getMessage());
            throw new ExternalServiceException(SERVICE_NAME,
                    "External service returned client error: " + e.getStatusCode(),
                    e.getStatusCode().value());
        } catch (HttpServerErrorException e) {
            log.error("Server error fetching posts from external API - Status: {}, Message: {}",
                    e.getStatusCode(), e.getMessage());
            throw new ExternalServiceException(SERVICE_NAME,
                    "External service returned server error: " + e.getStatusCode(),
                    e.getStatusCode().value());
        } catch (ResourceAccessException e) {
            log.error("Network error fetching posts from external API", e);
            throw new ExternalServiceException(SERVICE_NAME,
                    "Unable to connect to external service: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error fetching posts from external API", e);
            throw new ExternalServiceException(SERVICE_NAME,
                    "Unexpected error communicating with external service", e);
        }
    }

    public PostResponse getPostById(Long id) {
        log.info("Fetching post with id: {} from external API", id);

        try {
            String url = EXTERNAL_API_URL + "/" + id;
            PostResponse post = restTemplate.getForObject(url, PostResponse.class);

            if (post == null) {
                log.warn("Post not found with id: {} from external API", id);
                throw new ResourceNotFoundException("Post not found with id: " + id);
            }

            log.info("Successfully fetched post with id: {} from external API", id);
            return post;
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("Post not found with id: {} - Status: {}", id, e.getStatusCode());
            throw new ResourceNotFoundException("Post not found with id: " + id);
        } catch (HttpClientErrorException e) {
            log.error("Client error fetching post with id: {} - Status: {}, Message: {}",
                    id, e.getStatusCode(), e.getMessage());
            throw new ExternalServiceException(SERVICE_NAME,
                    "External service returned client error: " + e.getStatusCode(),
                    e.getStatusCode().value());
        } catch (HttpServerErrorException e) {
            log.error("Server error fetching post with id: {} - Status: {}, Message: {}",
                    id, e.getStatusCode(), e.getMessage());
            throw new ExternalServiceException(SERVICE_NAME,
                    "External service returned server error: " + e.getStatusCode(),
                    e.getStatusCode().value());
        } catch (ResourceAccessException e) {
            log.error("Network error fetching post with id: {} from external API", id, e);
            throw new ExternalServiceException(SERVICE_NAME,
                    "Unable to connect to external service: " + e.getMessage(), e);
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error fetching post with id: {} from external API", id, e);
            throw new ExternalServiceException(SERVICE_NAME,
                    "Unexpected error communicating with external service", e);
        }
    }
}
