package com.studygroup.studygroup.account;

import com.studygroup.studygroup.domain.Account;
import com.studygroup.studygroup.domain.Tag;
import com.studygroup.studygroup.domain.Zone;
import com.studygroup.studygroup.settings.form.Notifications;
import com.studygroup.studygroup.settings.form.Profile;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AccountService implements UserDetailsService {

    private final AccountRepository accountRepository;
    private final JavaMailSender javaMailSender;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;

    public Account processNewAccount(SignUpForm signUpForm) {
        Account newAccount = saveNewAccount(signUpForm);
        sendSignUpConfirmEmail(newAccount);
        return newAccount;
    }

    private Account saveNewAccount(SignUpForm signUpForm) {
        signUpForm.setPassword(passwordEncoder.encode(signUpForm.getPassword()));
        Account account = modelMapper.map(signUpForm, Account.class);
        account.generateEmailCheckToken();
        return accountRepository.save(account);
    }

    public void sendSignUpConfirmEmail(Account newAccount) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(newAccount.getEmail());
        mailMessage.setSubject("스터디올래, 회원 가입 인증");
        mailMessage.setText("/check-email-token?token=" + newAccount.getEmailCheckToken() + "&email=" + newAccount.getEmail());
        javaMailSender.send(mailMessage);
        newAccount.updateConfirmEmailLastSendTime();
    }

    public void completeSignUp(Account account) {
        account.completeSignUp();
        login(account);
    }

    public void login(Account account) {
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                new UserAccount(account),
                account.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);

        // HttpSession에 SecurityContext를 저장하여 이후 요청에서도 인증 상태가 유지되도록 함
        HttpSession session = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest()
                .getSession();
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, SecurityContextHolder.getContext());
    }

    @Transactional(readOnly = true)
    @Override
    public UserDetails loadUserByUsername(String emailOrNickname) throws UsernameNotFoundException {

        Account account = accountRepository.findByEmail(emailOrNickname);
        if (account == null)
            account = accountRepository.findByNickname(emailOrNickname);

        if (account == null)
            throw new UsernameNotFoundException(emailOrNickname);

        return new UserAccount(account);
    }

    public void updateProfile(Account account, Profile profile) {
        Account accountPS = accountRepository.findByEmail(account.getEmail()); //dirty check가 안전
        modelMapper.map(profile, accountPS);
//        accountRepository.save(account); //id가 있는 detached 상태면 merge 시킨다. /merge -> DB에서 엔티티 조회 후 detached entity의 필드 값 복사

        //Authentication 과 세션에 변경 사항을 재반영
        login(accountPS);
    }

    public void updatePassword(Account account, String newPassword) {
        Account accountPS = accountRepository.findByEmail(account.getEmail()); //dirty check가 안전
        accountPS.setPassword(passwordEncoder.encode(newPassword));

        //Authentication 과 세션에 변경 사항을 재반영
        login(accountPS);
    }

    public void updateNotifications(Account account, Notifications notifications) {
        Account accountPS = accountRepository.findByEmail(account.getEmail());
        modelMapper.map(notifications, accountPS);

        //Authentication 과 세션에 변경 사항을 재반영
        login(accountPS);
    }

    public void updateNickname(Account account, String nickname) {
        Account accountPS = accountRepository.findByEmail(account.getEmail());
        accountPS.setNickname(nickname);
        login(accountPS);
    }

    public void sendLoginLink(Account account) {
        account.generateEmailCheckToken();
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(account.getEmail());
        mailMessage.setSubject("스터디올래, 로그인 링크");
        mailMessage.setText("/login-by-email?token=" + account.getEmailCheckToken() + "&email=" + account.getEmail());
        javaMailSender.send(mailMessage);
    }

    public void addTag(Account account, Tag tag) {
        Optional<Account> byId = accountRepository.findById(account.getId());
        byId.ifPresent(ac -> ac.getTags().add(tag));
    }

    public void removeTag(Account account, Tag tag) {
        Optional<Account> byId = accountRepository.findById(account.getId());
        byId.ifPresent(ac -> ac.getTags().remove(tag));
    }

    public Set<Tag> getTags(Account account) {
        Optional<Account> byId = accountRepository.findById(account.getId());
        return byId.orElseThrow().getTags();
    }

    public Set<Zone> getZones(Account account) {
        return accountRepository.findById(account.getId()).orElseThrow().getZones();
    }

    public void addZone(Account account, Zone zone) {
        accountRepository.findById(account.getId()).ifPresent(ac -> ac.getZones().add(zone));
    }

    public void removeZone(Account account, Zone zone) {
        accountRepository.findById(account.getId()).ifPresent(ac -> ac.getZones().remove(zone));
    }
}
