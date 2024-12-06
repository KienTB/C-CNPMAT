package VNNet.VNNet.Request;

import lombok.Data;

@Data
public class GradeRequest {
    private Long studentId;
    private Long userId;
    private String subject;
    private float score;
    private String term;
}
