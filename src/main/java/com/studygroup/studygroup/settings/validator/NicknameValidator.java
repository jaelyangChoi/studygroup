package com.studygroup.studygroup.settings.validator;

import com.studygroup.studygroup.account.AccountRepository;
import com.studygroup.studygroup.domain.Account;
import com.studygroup.studygroup.settings.form.NicknameForm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
public class NicknameValidator implements Validator {

    private final AccountRepository accountRepository;

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz.isAssignableFrom(NicknameForm.class);
    }

    @Override
    public void validate(Object target, Errors errors) {
        NicknameForm nicknameForm = (NicknameForm) target;
        Account account = accountRepository.findByNickname(nicknameForm.getNickname());
        if (account != null) {
            errors.rejectValue("nickname", "wrong.value", "입력하신 닉네임은 이미 사용 중 입니다.");
        }
    }
}
