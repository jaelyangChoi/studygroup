package com.studygroup.studygroup.account;

import com.studygroup.studygroup.domain.Account;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@Controller
@RequiredArgsConstructor
public class AccountController {

    private final SignUpFormValidator signUpFormValidator;
    private final AccountService accountService;
    private final AccountRepository accountRepository;

    //바인딩 설정
    @InitBinder("signUpForm") //타입의 케멀케이스로 적용
    public void initBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(signUpFormValidator);
    }


    @GetMapping("/sign-up")
    public String signUpForm(Model model) {
//        model.addAttribute("signUpForm", new SignUpForm());
        model.addAttribute(new SignUpForm());
        return "account/sign-up";
    }

    @PostMapping("/sign-up")
    public String signUpSubmit(@Valid SignUpForm signUpForm, Errors errors) {
        if (errors.hasErrors())
            return "account/sign-up";

        Account account = accountService.processNewAccount(signUpForm);
        accountService.login(account);
        return "redirect:/";
    }

    @GetMapping("/check-email-token")
    public String checkEmailToken(@RequestParam("token") String token, @RequestParam("email") String email, Model model) {
        Account account = accountRepository.findByEmail(email);
        String view = "account/checked-email";
        if (account == null) {
            model.addAttribute("error", "wrong.email");
            return view;
        }
        if (!account.isValidToken(token)) {
            model.addAttribute("error", "wrong.token");
            return view;
        }
        accountService.completeSignUp(account);
        log.info("account.isEmailVerified={}", account.isEmailVerified());
        model.addAttribute("numberOfUser", accountRepository.count());
        model.addAttribute("nickname", account.getNickname());
        return view;
    }

    /**
     * 가입 확인 이메일을 전송한 이메일 주소 (== 가입할 때 입력한 이메일 주소)를 화면에 보여 줌
     * 재전송 버튼 노출
     */
    @GetMapping("/check-email")
    public String checkEmail(@CurrentUser Account account, Model model) {
        model.addAttribute("email", account.getEmail());
        return "account/check-email";
    }

    /**
     * 가입 이메일 재전송
     */
    @GetMapping("/resend-confirm-email")
    public String resendConfirmEmail(@CurrentUser Account account, Model model) {
        if (!account.canSendConfirmEmail()) {
            accountService.sendSignUpConfirmEmail(account);
            model.addAttribute("error", "이메일은 한 시간에 한 번만 보낼 수 있습니다.");
            model.addAttribute("email", account.getEmail());
            return "account/check-email";
        }
        return "redirect:/";
    }

    /**
     * 프로필 뷰
     * 본인 프로필만 수정 가능
     */
    @GetMapping("/profile/{nickname}")
    public String viewProfile(@PathVariable("nickname") String nickname, Model model, @CurrentUser Account account) {
        Account byNickname = accountRepository.findByNickname(nickname);
        if (byNickname == null)
            throw new IllegalArgumentException(nickname + "에 해당하는 사용자가 없습니다.");

        model.addAttribute(byNickname); //객체 이름을 생략하면 기본값으로 타입이 들어감 "account"
        model.addAttribute("isOwner", byNickname.equals(account));
        return "account/profile";
    }

    /**
     * 이메일 로그인 링크 발송
     */
    @GetMapping("/email-login")
    public String emailLoginForm() {
        return "account/email-login";
    }

    @PostMapping("/email-login")
    public String sendEmailLoginLink(@RequestParam("email") String email, Model model, RedirectAttributes redirectAttributes) {
        Account account = accountRepository.findByEmail(email);
        if (account == null) {
            model.addAttribute("error", "유효한 이메일 주소가 아닙니다.");
            return "account/email-login";
        }

        if (!account.canSendConfirmEmail()) {
            model.addAttribute("error", "이메일 로그인은 1시간 뒤에 이용할 수 있습니다.");
            return "account/email-login";
        }

        accountService.sendLoginLink(account);
        redirectAttributes.addFlashAttribute("message", "이메일 인증 메일을 발송했습니다.");
        return "redirect:/email-login";
    }

    /**
     * 이메일 로그인
     */
    @GetMapping("/login-by-email")
    public String loginByEmail(@RequestParam("token") String token, @RequestParam("email") String email, Model model) {
        Account account = accountRepository.findByEmail(email);
        String view = "account/logged-in-by-email";
        if (account == null || !account.isValidToken(token)) {
            model.addAttribute("error", "로그인할 수 없습니다.");
            return view;
        }

        accountService.login(account);
        return view;
    }
}
