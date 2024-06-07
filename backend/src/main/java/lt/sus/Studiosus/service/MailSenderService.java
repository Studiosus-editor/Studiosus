package lt.sus.Studiosus.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class MailSenderService {
  private final JavaMailSender mailSender;

  public MailSenderService(JavaMailSender mailSender) {
    this.mailSender = mailSender;
  }

  private static String inviteLinkStyles() {
    return """
            <head>
                            <meta charset="UTF-8">
                            <meta name="viewport" content="width=device-width, initial-scale=1.0">
                            <meta name="color-scheme" content="light">
                            <meta name="supported-color-schemes" content="light">
                            <title>Project Invitation</title>
                            <style>
                              body {
                                font-family: Arial, sans-serif;
                                display: flex;
                                flex-direction: column;
                                justify-content: center;
                                align-items: center;
                                height: 100vh;
                                margin: 0;
                              }
                              .background {
                                background: linear-gradient(5deg, #143755 50%,   #1b4895 50%);
                                padding: 20px 0 20px 0;
                              }
                              .card {
                                margin: 50px auto;
                                width: 330px;
                                height: 350px;
                                background: linear-gradient(5deg, #fff 50%,   #fff 50%);
                                border-radius: 10px;
                                box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
                                padding: 20px;
                                box-sizing: border-box;
                              }
                              .header {
                                text-align: center;
                                color: #333;
                                font-size: 24px;
                                font-weight: bold;
                                margin-bottom: 0px;
                              }
                              .content {
                                text-align: center;
                                font-size: 16px;
                                color: #666;
                                margin-bottom: 25px;
                              }
                              .button {
                                text-decoration: none;
                                background-color: #b8d2fc;
                                font-weight: bold;
                                color: #f7faff;
                                border: none;
                                padding: 12.5px 30px;
                                font-size: 16px;
                                border-radius: 10px;
                                margin-left: 32.5%;
                                cursor: pointer;
                              }
                              .button:hover {
                                background-color: #9dc1fc;
                              }
                              hr {
                                width: 180px;
                                margin-bottom: 20px;
                              }
                              .project {
                                text-align: center;
                                font-size: 16px;
                                color: #666;
                                margin-bottom: 50px;
                              }
                            </style>
                            </head>
            """;
  }

  public static String inviteLinkHtml(String projectName, String inviteLink) {
    return """
                            <html>
                            %s
                            <body>
                            <div class="background">
                              <div class="card">
                                <div class="header">Studiosus</div>
                                <hr>
                                <div class="content">You have received an invite to project</div>
                                <div class="project">
                                <b>%s</b>
                                </div>
                                <a href="%s" class="button">Accept</a>
                              </div>
                            </div>
                            </body>
                            </html>
                    """
        .formatted(inviteLinkStyles(), projectName, inviteLink);
  }

  public static String resetPasswordHtml(String userName, String resetLink) {
    return """
                            <html>
                            %s
                            <body>
                            <div class="background">
                              <div class="card">
                                <div class="header">Studiosus</div>
                                <hr>
                                <div class="content">Request to reset password for user:</div>
                                <div class="project">
                                <b>%s</b>
                                </div>
                                <a href="%s" class="button">Reset</a>
                              </div>
                            </div>
                            </body>
                            </html>
                    """
        .formatted(inviteLinkStyles(), userName, resetLink);
  }

  public void sendNewMail(String to, String subject, String body) {
    MimeMessage message = mailSender.createMimeMessage();
    MimeMessageHelper helper = new MimeMessageHelper(message);

    try {
      helper.setTo(to);
      helper.setSubject(subject);
      helper.setText(body, true);
    } catch (MessagingException e) {
      throw new RuntimeException("Failed to set email attributes", e);
    }
    mailSender.send(message);
  }
}
