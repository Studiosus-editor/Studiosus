package lt.sus.Studiosus.model;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatMessage {
  private String content;
  private String sender;
  private MessageType type;

  public enum MessageType {
    CHAT,
    LEAVE,
    JOIN
  }
}
