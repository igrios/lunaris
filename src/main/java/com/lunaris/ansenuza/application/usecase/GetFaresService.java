package com.lunaris.ansenuza.application.usecase;

import com.lunaris.ansenuza.domain.port.in.FareLocalityView;
import com.lunaris.ansenuza.domain.port.in.GetFaresQuery;
import com.lunaris.ansenuza.domain.repository.FareRepository;
import com.lunaris.ansenuza.domain.repository.LocalityRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetFaresService implements GetFaresQuery {
    private final FareRepository fareRepository;
    private final LocalityRepository localityRepository;

    @Override
    public List<FareLocalityView> getAll() {
        return fareRepository.findAllByOrderByLocalityNameAsc().stream().map(fare -> {
            var locality = localityRepository.findFirstByNameIgnoreCase(fare.getLocalityName()).orElse(null);
            return new FareLocalityView(fare.getId(), locality == null ? null : locality.getId(),
                    fare.getLocalityName(), fare.getAmount(), locality == null ? null : locality.getKmsToCordoba(),
                    locality == null ? null : locality.getMinutesFromOrigin());
        }).toList();
    }
}
