package lt.sus.Studiosus.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lt.sus.Studiosus.model.enums.LogLevel;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
public class Log {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "log_id")
  private Long id;

  @Size(max = 5000)
  @Column(name = "message", columnDefinition = "TEXT")
  private String message;

  @Column(name = "level")
  private LogLevel level;

  @Column(name = "timestamp")
  private LocalDateTime timestamp;

  public Log(String message, LogLevel level, LocalDateTime timestamp) {
    this.message = message;
    this.level = level;
    this.timestamp = timestamp;
  }
}
