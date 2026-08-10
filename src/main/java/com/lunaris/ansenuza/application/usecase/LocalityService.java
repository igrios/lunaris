package com.lunaris.ansenuza.application.usecase;

import com.lunaris.ansenuza.domain.model.Locality;
import com.lunaris.ansenuza.domain.repository.LocalityRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LocalityService {

    private final LocalityRepository localityRepository;

    @Transactional(readOnly = true)
    public List<Locality> findAllWithActiveFare() {
        return localityRepository.findAllWithActiveFare();
    }
}
