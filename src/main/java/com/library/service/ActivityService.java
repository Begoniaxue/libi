package com.library.service;

import com.library.entity.Activity;
import com.library.entity.ActivityRegistration;
import com.library.entity.Reader;
import com.library.repository.ActivityRegistrationRepository;
import com.library.repository.ActivityRepository;
import com.library.repository.ReaderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ActivityService {

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private ActivityRegistrationRepository registrationRepository;

    @Autowired
    private ReaderRepository readerRepository;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public Map<String, Object> getActivityPage(int page, int size, String keyword) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<Activity> activityPage;
        if (keyword != null && !keyword.trim().isEmpty()) {
            activityPage = activityRepository.findByNameContaining(keyword, pageable);
        } else {
            activityPage = activityRepository.findAll(pageable);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("list", activityPage.getContent());
        result.put("total", activityPage.getTotalElements());
        result.put("pages", activityPage.getTotalPages());
        result.put("current", page);
        result.put("size", size);
        return result;
    }

    public Activity getActivityById(Long id) {
        return activityRepository.findById(id).orElse(null);
    }

    @Transactional
    public Activity addActivity(Activity activity) {
        activity.setRegisteredCount(0);
        if (activity.getStatus() == null) {
            activity.setStatus(1);
        }
        return activityRepository.save(activity);
    }

    @Transactional
    public Activity updateActivity(Activity activity) {
        Activity exist = activityRepository.findById(activity.getId()).orElse(null);
        if (exist == null) {
            throw new RuntimeException("活动不存在");
        }
        activity.setRegisteredCount(exist.getRegisteredCount());
        return activityRepository.save(activity);
    }

    @Transactional
    public void deleteActivity(Long id) {
        Activity activity = activityRepository.findById(id).orElse(null);
        if (activity == null) {
            throw new RuntimeException("活动不存在");
        }
        activityRepository.deleteById(id);
    }

    public Map<String, Object> getRegistrationPage(Long activityId, int page, int size, String keyword) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<ActivityRegistration> registrationPage;
        if (keyword != null && !keyword.trim().isEmpty()) {
            registrationPage = registrationRepository.findByActivityIdAndNameContaining(activityId, keyword, pageable);
        } else {
            registrationPage = registrationRepository.findByActivityId(activityId, pageable);
        }

        List<ActivityRegistration> registrations = registrationPage.getContent();
        for (ActivityRegistration reg : registrations) {
            if (reg.getReaderId() != null) {
                Reader reader = readerRepository.findById(reg.getReaderId()).orElse(null);
                if (reader != null) {
                    reg.setReaderName(reader.getName());
                }
            }
            Activity activity = activityRepository.findById(activityId).orElse(null);
            if (activity != null) {
                reg.setActivityName(activity.getName());
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("list", registrations);
        result.put("total", registrationPage.getTotalElements());
        result.put("pages", registrationPage.getTotalPages());
        result.put("current", page);
        result.put("size", size);
        return result;
    }

    @Transactional
    public ActivityRegistration register(ActivityRegistration registration) {
        Activity activity = activityRepository.findById(registration.getActivityId()).orElse(null);
        if (activity == null) {
            throw new RuntimeException("活动不存在");
        }
        if (activity.getStatus() != 1) {
            throw new RuntimeException("活动未发布");
        }
        if (activity.getRegisteredCount() >= activity.getQuota()) {
            throw new RuntimeException("活动名额已满");
        }
        if (LocalDateTime.now().isAfter(activity.getStartTime())) {
            throw new RuntimeException("活动已开始，无法报名");
        }

        if (registration.getPhone() != null && !registration.getPhone().isEmpty()) {
            if (registrationRepository.findByActivityIdAndPhone(registration.getActivityId(), registration.getPhone()).isPresent()) {
                throw new RuntimeException("该手机号已报名");
            }
        }

        registration.setStatus(1);
        ActivityRegistration saved = registrationRepository.save(registration);

        activity.setRegisteredCount(activity.getRegisteredCount() + 1);
        activityRepository.save(activity);

        return saved;
    }

    @Transactional
    public void cancelRegistration(Long id) {
        ActivityRegistration registration = registrationRepository.findById(id).orElse(null);
        if (registration == null) {
            throw new RuntimeException("报名记录不存在");
        }
        if (registration.getStatus() != 1) {
            throw new RuntimeException("报名已取消");
        }

        registration.setStatus(0);
        registrationRepository.save(registration);

        Activity activity = activityRepository.findById(registration.getActivityId()).orElse(null);
        if (activity != null && activity.getRegisteredCount() > 0) {
            activity.setRegisteredCount(activity.getRegisteredCount() - 1);
            activityRepository.save(activity);
        }
    }

    public byte[] exportRegistrationsToCsv(Long activityId) {
        List<ActivityRegistration> registrations = registrationRepository.findByActivityId(activityId);
        Activity activity = activityRepository.findById(activityId).orElse(null);
        String activityName = activity != null ? activity.getName() : "";

        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             OutputStreamWriter writer = new OutputStreamWriter(out, StandardCharsets.UTF_8)) {

            writer.write('\uFEFF');

            writer.write("活动名称：" + activityName + "\n\n");

            String[] headers = {"ID", "姓名", "手机号", "邮箱", "备注", "报名时间", "状态"};
            writer.write(String.join(",", headers) + "\n");

            for (ActivityRegistration reg : registrations) {
                String[] row = {
                    String.valueOf(reg.getId()),
                    reg.getName() != null ? reg.getName() : "",
                    reg.getPhone() != null ? reg.getPhone() : "",
                    reg.getEmail() != null ? reg.getEmail() : "",
                    reg.getRemark() != null ? reg.getRemark().replace(",", "，").replace("\n", " ") : "",
                    reg.getCreateTime() != null ? reg.getCreateTime().format(DATE_TIME_FORMATTER) : "",
                    reg.getStatus() == 1 ? "已报名" : "已取消"
                };
                writer.write(String.join(",", row) + "\n");
            }

            writer.flush();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("导出CSV失败", e);
        }
    }

    public Map<String, Object> getActivityStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalActivities", activityRepository.count());
        stats.put("publishedActivities", activityRepository.countPublishedActivities());
        stats.put("upcomingActivities", activityRepository.countUpcomingActivities(LocalDateTime.now()));
        return stats;
    }

    public List<Activity> getUpcomingActivities(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return activityRepository.findUpcomingActivities(LocalDateTime.now(), pageable);
    }
}
