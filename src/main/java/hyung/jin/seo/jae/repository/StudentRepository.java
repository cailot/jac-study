package hyung.jin.seo.jae.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import hyung.jin.seo.jae.dto.StudentDTO;
import hyung.jin.seo.jae.model.Student;

public interface StudentRepository extends JpaRepository<Student, Long>, JpaSpecificationExecutor<Student>{  
	
	List<Student> findAllByEndDateIsNull();
	
	List<Student> findAllByEndDateIsNotNull();
	
	Student findByIdAndEndDateIsNull(Long id);
	
	long countByEndDateIsNull();
	
	@Modifying
	@Query("UPDATE Student s SET s.endDate = null WHERE s.id = ?1")
	void setEndDateToNull(Long id);

	@Query(value = "SELECT DISTINCT c.year FROM jae.Cycle c WHERE c.id IN (SELECT l.cycleId FROM Class l WHERE l.id IN (SELECT e.clazzId FROM Enrolment e WHERE e.studentId = ?1))", nativeQuery = true)
        List<Integer> findYearsByStudentId(Long id);	

	// retrieve active student by state, branch & grade called from studentList.jsp
	@Query(value = "SELECT new hyung.jin.seo.jae.dto.StudentDTO(s.id, s.firstName, s.lastName, s.grade, s.gender, s.contactNo1, s.contactNo2, s.email1, s.email2, s.state, s.branch, s.registerDate, s.endDate, s.password, s.active) FROM Student s WHERE s.state LIKE ?1 AND s.branch LIKE ?2 AND s.grade LIKE ?3 AND s.active = 1")
	List<StudentDTO> listActiveStudent(String state, String branch, String grade);

	// retrieve inactive student by state, branch & grade called from studentList.jsp
	@Query(value = "SELECT new hyung.jin.seo.jae.dto.StudentDTO(s.id, s.firstName, s.lastName, s.grade, s.gender, s.contactNo1, s.contactNo2, s.email1, s.email2, s.state, s.branch, s.registerDate, s.endDate, s.password, s.active) FROM Student s WHERE s.state LIKE ?1 AND s.branch LIKE ?2 AND s.grade LIKE ?3 AND s.active = 0")
	List<StudentDTO> listInactiveStudent(String state, String branch, String grade);

        @Query("SELECT new hyung.jin.seo.jae.dto.StudentDTO" +
        "(s.id, s.firstName, s.lastName, s.grade, s.gender, s.contactNo1, s.contactNo2, s.email1, s.email2, s.state, s.branch, s.registerDate, s.endDate, s.password, s.active, e.startWeek, e.endWeek) " +
        "FROM Student s " +
        "JOIN Enrolment e ON s.id = e.student.id " +
        "WHERE s.endDate IS NULL " +
        "AND s.state LIKE :state " +
        "AND s.branch LIKE :branch " +
        "AND s.grade LIKE :grade " +
        "AND e.discount != '" + "100%" + "' " +
        "AND e.clazz.id IN (" +
        "SELECT cla.id FROM Clazz cla WHERE cla.course.cycle.id IN (" +
        "SELECT cyc.id FROM Cycle cyc WHERE cyc.year = :year))")
	List<StudentDTO> listActiveStudent(@Param("state") String state, @Param("branch") String branch, @Param("grade") String grade, @Param("year") int year);

	@Query("SELECT new hyung.jin.seo.jae.dto.StudentDTO" +
        "(s.id, s.firstName, s.lastName, s.grade, s.gender, s.contactNo1, s.contactNo2, s.email1, s.email2, s.state, s.branch, s.registerDate, s.endDate, s.password, s.active, e.startWeek, e.endWeek) " +
        "FROM Student s " +
        "JOIN Enrolment e ON s.id = e.student.id " +
        "WHERE s.endDate IS NOT NULL " +
        "AND s.state LIKE :state " +
        "AND s.branch LIKE :branch " +
        "AND s.grade LIKE :grade " +
        "AND e.discount != '" + "100%" + "' " +
        "AND e.clazz.id IN (" +
        "SELECT cla.id FROM Clazz cla WHERE cla.course.cycle.id IN (" +
        "SELECT cyc.id FROM Cycle cyc WHERE cyc.year = :year))")
	List<StudentDTO> listInactiveStudent(@Param("state") String state, @Param("branch") String branch, @Param("grade") String grade, @Param("year") int year);        
        
        @Modifying
        @Query(value = "UPDATE Student s SET s.password = ?2 WHERE s.id = ?1 AND ACTIVE = 0", nativeQuery = true)
        void updatePassword(Long id, String password);    

        @Query(value = "SELECT s.id, s.password, s.firstName, s.lastName, s.active, s.grade FROM Student s WHERE s.id =?1 AND ACTIVE = 1", nativeQuery = true)   
	Object[] checkStudentAccount(Long id);


        @Modifying
        @Query(value = "UPDATE Student s SET s.grade = ?2 WHERE s.id = ?1", nativeQuery = true)
        void updateGrade(Long id, String grade);    

}
