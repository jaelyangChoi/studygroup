package com.studygroup.studygroup.zone;

import com.studygroup.studygroup.domain.Zone;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ZoneService {

    private final ZoneRepository zoneRepository;

    @PostConstruct //빈 생성 후 실행됨
    public void initZoneData() throws IOException {
        if(zoneRepository.count() == 0){
            Resource resource = new ClassPathResource("zones_kr.csv");
            List<Zone> zoneList = Files.readAllLines(resource.getFile().toPath(), StandardCharsets.UTF_8).stream()
                    .map(line -> {
                        String[] split = line.split(",");
                        return Zone.builder()
                                .city(split[0])
                                .localNameOfCity(split[1])
                                .province(split[2])
                                .build();
                    })
                    .toList();
            zoneRepository.saveAll(zoneList);
        }
    }
}
