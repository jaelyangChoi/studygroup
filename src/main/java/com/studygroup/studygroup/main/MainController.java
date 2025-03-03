package com.studygroup.studygroup.main;

import com.studygroup.studygroup.account.CurrentUser;
import com.studygroup.studygroup.domain.Account;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {

    @GetMapping("/")
    public String home(@CurrentUser Account account, Model model) {
        if (account != null)
            model.addAttribute("account", account);

        return "index";
    }
}
