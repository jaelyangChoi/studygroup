package com.studygroup.studygroup.settings;

import com.studygroup.studygroup.domain.Account;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor // @ModelAttribute 바인딩 시 빈 객체를 만들고 setter 로 주입하므로 필요
public class Profile {
    private String bio;
    private String url;
    private String occupation;
    private String location;

    public Profile(Account account) {
        this.bio = account.getBio();
        this.url = account.getUrl();
        this.occupation = account.getOccupation();
        this.location = account.getLocation();
    }
}
