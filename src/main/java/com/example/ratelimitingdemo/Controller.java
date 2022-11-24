package com.example.ratelimitingdemo;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.Refill;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

@RestController
public class Controller {
    private final Bucket bucket;

    public Controller() {
        Bandwidth limit = Bandwidth.classic(5, Refill.greedy(5, Duration.ofMinutes(1)));
        this.bucket = Bucket4j.builder()
                .addLimit(limit)
                .build();
    }

    @PostMapping(value = "/api/v1/perimeter/rectangle")
    public ResponseEntity<Perimeter> rectangle(@RequestBody Dimension dimensions) {

        if (bucket.tryConsume(1)) {
            return ResponseEntity.ok(new Perimeter("rectangle",
                    (double) 2 * (dimensions.getLength() + dimensions.getBreadth())));
        }

        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
    }
}
