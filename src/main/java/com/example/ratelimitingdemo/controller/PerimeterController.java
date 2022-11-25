package com.example.ratelimitingdemo.controller;

import com.example.ratelimitingdemo.request.Dimension;
import com.example.ratelimitingdemo.model.Perimeter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1")
public class PerimeterController {
    @PostMapping(value = "/perimeter/rectangle")
    public ResponseEntity<Perimeter> rectangle(@RequestBody Dimension dimensions) {
            return ResponseEntity.ok(new Perimeter("rectangle",
                    (double) 2 * (dimensions.getLength() + dimensions.getBreadth())));
    }
}
