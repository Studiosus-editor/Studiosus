package lt.sus.Studiosus.service;

import java.time.LocalDateTime;
import lt.sus.Studiosus.model.Log;
import lt.sus.Studiosus.model.enums.LogLevel;
import lt.sus.Studiosus.repository.LogRepository;
import org.springframework.stereotype.Service;

@Service
public class LoggingService {
  private final LogRepository logRepository;

  public LoggingService(LogRepository logRepository) {
    this.logRepository = logRepository;
  }

  public void saveLogToDatabase(Log log) {
    try {
      logRepository.save(log);
    } catch (Exception e) {
      Log errorLog =
          new Log(
              "Failed to save log into database: %s".formatted(e.getMessage()),
              LogLevel.FATAL,
              LocalDateTime.now());

      logRepository.save(errorLog);
    }
  }
}
