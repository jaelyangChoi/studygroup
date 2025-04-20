package com.studygroup.studygroup.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Getter
@Setter
@EqualsAndHashCode(of = "id")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Account {

    @Id
    @GeneratedValue
    private Long id;

    @Column(unique = true)
    private String email;

    @Column(unique = true)
    private String nickname;

    private String password;

    private boolean emailVerified;

    private String emailCheckToken; //이메일 검증 시 사용할 토큰 값

    private LocalDateTime joinedAt;

    private String bio;

    private String url;

    private String occupation;

    private String location; //varchar(255)

    @Lob
    @Basic(fetch = FetchType.EAGER) //@Lob 필드는 기본적으로 FetchType.LAZY
    private String profileImage; //String 이므로 CLOB

    /* 알림 */
    private boolean studyCreatedByEmail;

    private boolean studyCreatedByWeb = true; //builder 사용하면 기본값 적용 안된다.

    private boolean studyEnrollmentResultByEmail;

    private boolean studyEnrollmentResultByWeb = true;

    private boolean studyUpdatedByEmail;

    private boolean studyUpdatedByWeb = true;

    private LocalDateTime confirmEmailSendAt;

    @ManyToMany
    private Set<Tag> tags = new HashSet<>();

    @ManyToMany
    private Set<Zone> zones = new HashSet<>();

    public void generateEmailCheckToken() {
        this.emailCheckToken = UUID.randomUUID().toString();
    }

    public void completeSignUp() {
        emailVerified = true;
        joinedAt = LocalDateTime.now();
    }

    public boolean isValidToken(String token) {
        return this.emailCheckToken.equals(token);
    }

    public boolean canSendConfirmEmail() {
        return confirmEmailSendAt.isBefore(LocalDateTime.now().minusHours(1));
    }

    public void updateConfirmEmailLastSendTime() {
        confirmEmailSendAt = LocalDateTime.now();
    }
}
