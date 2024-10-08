package hyung.jin.seo.jae.model;

import org.hibernate.annotations.CreationTimestamp;
import org.springframework.format.annotation.DateTimeFormat;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ForeignKey;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="TestSchedule")
public class TestSchedule {
    
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // auto increment
    private Long id;
    
    @Column(length = 4, nullable = true)
    private Integer year;

    @Column(length = 2, nullable = true)
    private Integer week;

    @Column(length = 50, nullable = false)
    private String info;

    @Column
    private boolean active;

    @DateTimeFormat(pattern = "dd/MM/yyyy HH:mm")
    private LocalDateTime startDate;

    @DateTimeFormat(pattern = "dd/MM/yyyy HH:mm")
    private LocalDateTime endDate;
    
    @CreationTimestamp
    private LocalDate registerDate;

    @ManyToMany(cascade = CascadeType.ALL)
	@JoinTable(
		name = "TestScheculeLink",
		joinColumns = { @JoinColumn(name = "testScheduleId", foreignKey = @ForeignKey(name = "FK_TestSchedule_Test")) },
		inverseJoinColumns = { @JoinColumn(name = "testId", foreignKey = @ForeignKey(name = "FK_Test_TestSchedule")) }
	)
	private Set<Test> tests = new LinkedHashSet();

    public void addTest(Test test){
        tests.add(test);
    }

 }
