package com.studygroup.studygroup.settings;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studygroup.studygroup.WithAccount;
import com.studygroup.studygroup.account.AccountRepository;
import com.studygroup.studygroup.account.AccountService;
import com.studygroup.studygroup.domain.Account;
import com.studygroup.studygroup.domain.Tag;
import com.studygroup.studygroup.domain.Zone;
import com.studygroup.studygroup.settings.form.TagForm;
import com.studygroup.studygroup.settings.form.ZoneForm;
import com.studygroup.studygroup.tag.TagRepository;
import com.studygroup.studygroup.zone.ZoneRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static com.studygroup.studygroup.settings.SettingsController.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
class SettingsControllerTestV2 {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    AccountRepository accountRepository;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    TagRepository tagRepository;
    @Autowired
    AccountService accountService;
    @Autowired
    ZoneRepository zoneRepository;

    private Zone testZone = Zone.builder().city("test").localNameOfCity("테스트시").province("테스트주").build();

    @BeforeEach
    void beforeEach() {
        zoneRepository.save(testZone);
    }

    @AfterEach
    void afterEach() {
        accountRepository.deleteAll(); //@WithAccount 에서 저장하므로 매번 지워줘야 한다.
        zoneRepository.deleteAll();
    }

    @WithAccount("cjl0701")
    @DisplayName("계정의 지역 정보 수정 폼")
    @Test
    void updateZonesForm() throws Exception {
        mockMvc.perform(get(ROOT + SETTINGS + ZONES))
                .andExpect(status().isOk())
                .andExpect(view().name(SETTINGS + ZONES))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("whitelist"))
                .andExpect(model().attributeExists("zones"));
    }

    @Transactional //lazy 로딩이 적용된다.
    @WithAccount("cjl0701")
    @DisplayName("계정의 지역 정보 추가")
    @Test
    void addZone() throws Exception {
        ZoneForm zoneForm = new ZoneForm();
        zoneForm.setZoneName(testZone.toString());

        mockMvc.perform(post(ROOT + SETTINGS + ZONES + "/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(zoneForm))
                        .with(csrf()))
                .andExpect(status().isOk());

        //then
        Account account = accountRepository.findByNickname("cjl0701");
        Zone zone = zoneRepository.findByCityAndProvince(testZone.getCity(), testZone.getProvince());
        assertTrue(account.getZones().contains(zone));
    }

    @Transactional
    @WithAccount("cjl0701")
    @DisplayName("계정의 지역 정보 삭제")
    @Test
    void removeZone() throws Exception {
        // given
        Account account = accountRepository.findByNickname("cjl0701");
        Zone zone = zoneRepository.findByCityAndProvince(testZone.getCity(), testZone.getProvince());
        accountService.addZone(account, zone);

        ZoneForm zoneForm = new ZoneForm();
        zoneForm.setZoneName(testZone.toString());

        mockMvc.perform(post(ROOT + SETTINGS + ZONES + "/remove")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(zoneForm))
                        .with(csrf()))
                .andExpect(status().isOk());

        assertFalse(account.getZones().contains(zone)); //트랜잭션 유지
    }

    @WithAccount("cjl0701")
    @DisplayName("태그 수정 폼")
    @Test
    void updateTagsForm() throws Exception {
        mockMvc.perform(get(ROOT + SETTINGS + TAGS))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("tags"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("whitelist"))
                .andExpect(view().name(SETTINGS + TAGS));
    }

    @Transactional //lazy 로딩이 적용된다.
    @WithAccount("cjl0701")
    @DisplayName("계정에 태그 추가")
    @Test
    void addTag() throws Exception {
        TagForm tagForm = new TagForm();
        tagForm.setTagTitle("newTag");

        mockMvc.perform(post(ROOT + SETTINGS + TAGS + "/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tagForm))
                        .with(csrf()))
                .andExpect(status().isOk());

        Tag newTag = tagRepository.findByTitle(tagForm.getTagTitle()).orElseThrow();
        assertNotNull(newTag);
        assertTrue(accountRepository.findByNickname("cjl0701").getTags().contains(newTag)); //LazyInitializationException 발생
    }

    @Transactional //lazy 로딩이 적용된다.
    @WithAccount("cjl0701")
    @DisplayName("계정에서 태그 삭제")
    @Test
    void removeTag() throws Exception {
        //given
        Account account = accountRepository.findByNickname("cjl0701");
        Tag newTag = tagRepository.save(Tag.builder().title("newTag").build());
        accountService.addTag(account, newTag);
        assertTrue(account.getTags().contains(newTag));

        //when
        TagForm tagForm = new TagForm();
        tagForm.setTagTitle("newTag");

        mockMvc.perform(post(ROOT + SETTINGS + TAGS + "/remove")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tagForm))
                        .with(csrf()))
                .andExpect(status().isOk());

        //then
        assertFalse(account.getTags().contains(newTag));
    }

    @WithAccount("cjl0701")
    @DisplayName("닉네임 수정 폼")
    @Test
    void updateAccountForm() throws Exception {
        mockMvc.perform(get(ROOT + SETTINGS + ACCOUNT))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("nicknameForm"));
    }

    @WithAccount("cjl0701")
    @DisplayName("닉네임 수정 - 입력값 정상")
    @Test
    void updateAccount_success() throws Exception {
        String newNickname = "cjl2076";
        mockMvc.perform(post(ROOT + SETTINGS + ACCOUNT)
                        .param("nickname", newNickname)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(ROOT + SETTINGS + ACCOUNT))
                .andExpect(flash().attributeExists("message"));

        assertNotNull(accountRepository.findByNickname(newNickname));
    }

    @WithAccount("cjl0701")
    @DisplayName("닉네임 수정 - 입력값 에러")
    @Test
    void updateAccount_failure() throws Exception {
        String newNickname = "A!ADF@#DDSF";
        mockMvc.perform(post(ROOT + SETTINGS + ACCOUNT)
                        .param("nickname", newNickname)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name(SETTINGS + ACCOUNT))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("nicknameForm"));
    }

    @WithAccount("cjl0701")
    @DisplayName("프로필 수정 폼")
    @Test
    void updateProfileForm() throws Exception {
        mockMvc.perform(get(ROOT + SETTINGS + PROFILE))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("profile"));
    }

    @WithAccount("cjl0701")
    @DisplayName("프로필 수정하기 - 입력값 정상")
    @Test
    void updateProfile() throws Exception {
        String bio = "짧은 소개를 수정하는 경우";
        mockMvc.perform(post(ROOT + SETTINGS + PROFILE)
                        .param("bio", bio)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(ROOT + SETTINGS + PROFILE))
                .andExpect(flash().attributeExists("message"));

        Account account = accountRepository.findByNickname("cjl0701");
        assertEquals(bio, account.getBio());
    }

    @WithAccount("cjl0701")
    @DisplayName("프로필 수정하기 - 입력값 에러")
    @Test
    void updateProfile_error() throws Exception {
        String bio = "길게 소개를 수정하는 경우 길게 소개를 수정하는 경우길게 소개를 수정하는 경우길게 소개를 수정하는 경우길게 소개를 수정하는 경우";
        mockMvc.perform(post(ROOT + SETTINGS + PROFILE)
                        .param("bio", bio)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name(SETTINGS + PROFILE))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("profile"))
                .andExpect(model().hasErrors());

        Account account = accountRepository.findByNickname("cjl0701");
        assertNull(account.getBio());
    }


    @WithAccount("cjl0701")
    @DisplayName("패스워드 수정 폼")
    @Test
    void updatePassword_form() throws Exception {
        mockMvc.perform(get(ROOT + SETTINGS + PASSWORD))
                .andExpect(status().isOk())
                .andExpect(view().name(SETTINGS + PASSWORD))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("passwordForm"));
    }

    @WithAccount("cjl0701")
    @DisplayName("패스워드 수정 - 입력값 정상")
    @Test
    void updatePassword_success() throws Exception {
        String newPassword = "12345678";

        mockMvc.perform(post(ROOT + SETTINGS + PASSWORD)
                        .param("newPassword", newPassword)
                        .param("newPasswordConfirm", newPassword)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(ROOT + SETTINGS + PASSWORD))
                .andExpect(flash().attributeExists("message"));

        Account account = accountRepository.findByNickname("cjl0701");
//        assertEquals(passwordEncoder.encode(newPassword), account.getPassword()); 동일하게 암호화하지 않는다.
        assertTrue(passwordEncoder.matches(newPassword, account.getPassword()));
    }

    @WithAccount("cjl0701")
    @DisplayName("패스워드 수정 - 입력값 에러 - 패스워드 불일치")
    @Test
    void updatePassword_fail() throws Exception {
        String newPassword = "12345678";
        String newPasswordConfirm = "11111111";

        mockMvc.perform(post(ROOT + SETTINGS + PASSWORD)
                        .param("newPassword", newPassword)
                        .param("newPasswordConfirm", newPasswordConfirm)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name(SETTINGS + PASSWORD))
                .andExpect(model().attributeExists("passwordForm"))
                .andExpect(model().attributeExists("account"))
//                .andExpect(model().attributeExists("error")) //model에 error로 안 담긴다.
                .andExpect(model().hasErrors())
                .andExpect(model().attributeHasErrors("passwordForm"));
    }
}