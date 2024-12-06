package VNNet.VNNet.Request;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ScheduleRequest {
    private Long scheduleId;
    private Long studentId;
    private Long userId;
    private String activity;
    private LocalDate scheduleDate;
    private String status;
}
