package hyung.jin.seo.jae.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import hyung.jin.seo.jae.dto.TestScheduleDTO;
import hyung.jin.seo.jae.model.TestSchedule;

public interface TestScheduleRepository extends JpaRepository<TestSchedule, Long>{  
	
	List<TestSchedule> findAll();

	// filter TestScheduleDTO by time & testGroup
	@Query("SELECT new hyung.jin.seo.jae.dto.TestScheduleDTO(t.id, t.fromDatetime, t.toDatetime, t.grade, t.testGroup, t.week, t.info, t.active, t.registerDate, t.resultDate, t.explanationFromDatetime, t.explanationToDatetime) " +
	"FROM TestSchedule t " +
	"WHERE t.fromDatetime BETWEEN :from AND :to " +
	"AND (:testGroup = '0' OR t.testGroup = :testGroup OR t.testGroup LIKE CONCAT('%,', :testGroup, ',%') OR t.testGroup LIKE CONCAT(:testGroup, ',%') OR t.testGroup LIKE CONCAT('%,', :testGroup))")
	List<TestScheduleDTO> filterTestScheduleByTimeNGroup(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to, @Param("testGroup") String testGroup);

	@Query("SELECT new hyung.jin.seo.jae.dto.TestScheduleDTO(t.id, t.fromDatetime, t.toDatetime, t.grade, t.testGroup, t.week, t.info, t.active, t.registerDate, t.explanationFromDatetime, t.explanationToDatetime) " +
	"FROM TestSchedule t WHERE " +
	"(t.testGroup = '0' OR t.testGroup LIKE CONCAT('%,', :testGroup, ',%') OR t.testGroup LIKE CONCAT(:testGroup, ',%') OR t.testGroup LIKE CONCAT('%,', :testGroup) OR t.testGroup = :testGroup) " +
	"AND (t.grade = '0' OR t.grade LIKE CONCAT('%,', :grade, ',%') OR t.grade LIKE CONCAT(:grade, ',%') OR t.grade LIKE CONCAT('%,', :grade) OR t.grade = :grade) " +
	"AND (:now BETWEEN t.fromDatetime AND t.toDatetime) AND (t.active = true)")
	List<TestScheduleDTO> getTestScheduleByGroupAndGrade(@Param("testGroup") String testGroup, @Param("grade") String grade, @Param("now") LocalDateTime now);

	@Query("SELECT new hyung.jin.seo.jae.dto.TestScheduleDTO(t.id, t.fromDatetime, t.toDatetime, t.grade, t.testGroup, t.week, t.info, t.active, t.registerDate, t.explanationFromDatetime, t.explanationToDatetime) " +
	"FROM TestSchedule t WHERE " +
	"(t.testGroup = '0' OR t.testGroup LIKE CONCAT('%,', :testGroup, ',%') OR t.testGroup LIKE CONCAT(:testGroup, ',%') OR t.testGroup LIKE CONCAT('%,', :testGroup) OR t.testGroup = :testGroup) " +
	"AND (t.grade = '0' OR t.grade LIKE CONCAT('%,', :grade, ',%') OR t.grade LIKE CONCAT(:grade, ',%') OR t.grade LIKE CONCAT('%,', :grade) OR t.grade = :grade) " +
	"AND (:now BETWEEN t.explanationFromDatetime AND t.explanationToDatetime) AND (t.active = true)")
	List<TestScheduleDTO> getTestSchedule4Explanation(@Param("testGroup") String testGroup, @Param("grade") String grade, @Param("now") LocalDateTime now);
}

