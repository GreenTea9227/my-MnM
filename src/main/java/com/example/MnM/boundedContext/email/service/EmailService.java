package com.example.MnM.boundedContext.email.service;


import com.example.MnM.base.appConfig.AppConfig;
import com.example.MnM.base.rsData.RsData;
import com.example.MnM.boundedContext.email.entity.SendEmailLog;
import com.example.MnM.boundedContext.email.repository.SendEmailLogRepository;
import com.example.MnM.boundedContext.emailSender.service.EmailSenderService;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.MailException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmailService {
    private final SendEmailLogRepository emailLogRepository;
    private final EmailSenderService emailSenderService;

    @Transactional
    public RsData sendEmail(String email, String subject, String body) {
        SendEmailLog sendEmailLog = SendEmailLog
                .builder()
                .email(email)
                .subject(subject)
                .body(body)
                .build();

        emailLogRepository.save(sendEmailLog);

        RsData trySendRs = trySend(email, subject, body);

        setCompleted(sendEmailLog, trySendRs.getResultCode(), trySendRs.getMsg());

        return RsData.of("S-1", "메일이 발송되었습니다.", sendEmailLog.getId());
    }

    private RsData trySend(String email, String title, String body) {

        try {
            emailSenderService.send(email, "no-reply@no-reply.com", title, body);

            return RsData.of("S-1", "메일이 발송되었습니다.");
        } catch (MailException e) {
            return RsData.of("F-1", e.getMessage());
        }
    }

    @Transactional
    public void setCompleted(SendEmailLog sendEmailLog, String resultCode, String message) {

        if (resultCode.startsWith("S-")) {
            sendEmailLog.setSuccessSendEmailLog(resultCode, message, LocalDateTime.now());
        } else {
            sendEmailLog.setFailSendEmailLog(resultCode, message, LocalDateTime.now());
        }

        emailLogRepository.save(sendEmailLog);
    }
}
