package com.studygroup.studygroup.account;

import com.studygroup.studygroup.domain.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true) //기본 생성된 메서드는 트랜잭션 처리가 되어있지만, 우리가 만든 메소드에도 트랜잭션 처리를 하려면 붙여야 한다.
public interface AccountRepository extends JpaRepository<Account, Long> {

    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);

    Account findByEmail(String email);

    Account findByNickname(String nickname);
}
