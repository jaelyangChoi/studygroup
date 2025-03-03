package com.studygroup.studygroup.account;

import com.studygroup.studygroup.domain.Account;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.List;

//시큐리티가 갖고 있는 유저 정보와 내가 만든 Account 사이의 어댑터 -> Principle 로 사용한다
@Getter
@Setter
public class UserAccount extends User {

    private Account account;

    public UserAccount(Account account) {
        super(account.getNickname(), account.getPassword(), List.of(new SimpleGrantedAuthority("ROLE_USER")));
        this.account = account;
    }
}
