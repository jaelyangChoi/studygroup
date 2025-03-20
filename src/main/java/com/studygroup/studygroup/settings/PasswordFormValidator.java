package com.studygroup.studygroup.settings;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

// 여러 곳에서 사용하지 않으므로 빈으로 등록할 필요없다.
public class PasswordFormValidator implements Validator {
    @Override
    public boolean supports(Class<?> clazz) {
        return PasswordForm.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        PasswordForm passwordForm = (PasswordForm) target;
        if(!passwordForm.getNewPassword().equals(passwordForm.getNewPasswordConfirm()))
            errors.rejectValue("newPassword","wrong.value", "입력한 새 패스워드가 일치하지 않습니다.");
    }
}
