package com.library.controller;

import com.library.common.Result;
import com.library.entity.Activity;
import com.library.entity.ActivityRegistration;
import com.library.service.ActivityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@RestController
@RequestMapping("/activities")
public class ActivityController {

    @Autowired
    private ActivityService activityService;

    @GetMapping
    public Result<Map<String, Object>> getActivityPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        return Result.success(activityService.getActivityPage(page, size, keyword));
    }

    @GetMapping("/{id}")
    public Result<Activity> getActivityById(@PathVariable Long id) {
        return Result.success(activityService.getActivityById(id));
    }

    @PostMapping
    public Result<Activity> addActivity(@RequestBody Activity activity) {
        return Result.success(activityService.addActivity(activity));
    }

    @PutMapping
    public Result<Activity> updateActivity(@RequestBody Activity activity) {
        return Result.success(activityService.updateActivity(activity));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteActivity(@PathVariable Long id) {
        activityService.deleteActivity(id);
        return Result.success();
    }

    @GetMapping("/{id}/registrations")
    public Result<Map<String, Object>> getRegistrations(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        return Result.success(activityService.getRegistrationPage(id, page, size, keyword));
    }

    @PostMapping("/register")
    public Result<ActivityRegistration> register(@RequestBody ActivityRegistration registration) {
        return Result.success(activityService.register(registration));
    }

    @PostMapping("/registrations/{id}/cancel")
    public Result<Void> cancelRegistration(@PathVariable Long id) {
        activityService.cancelRegistration(id);
        return Result.success();
    }

    @GetMapping("/{id}/registrations/export")
    public ResponseEntity<byte[]> exportRegistrations(@PathVariable Long id) {
        byte[] data = activityService.exportRegistrationsToCsv(id);
        Activity activity = activityService.getActivityById(id);
        String fileName = (activity != null ? activity.getName() : "活动报名") + "_" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv";
        fileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replaceAll("\\+", "%20");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv; charset=UTF-8"));
        headers.setContentDispositionFormData("attachment", fileName);

        return ResponseEntity.ok()
                .headers(headers)
                .body(data);
    }

    @GetMapping("/statistics/summary")
    public Result<Map<String, Object>> getActivityStatistics() {
        return Result.success(activityService.getActivityStatistics());
    }
}
