package VNNet.VNNet.Repository;

import VNNet.VNNet.Model.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    List<Schedule> findByStudent_StudentId(Long studentId);
}
