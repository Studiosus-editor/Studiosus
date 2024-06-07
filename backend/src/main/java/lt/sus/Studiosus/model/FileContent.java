package lt.sus.Studiosus.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FileContent {
  private String content;

  public FileContent() {}

  public FileContent(String content) {
    this.content = content;
  }
}
