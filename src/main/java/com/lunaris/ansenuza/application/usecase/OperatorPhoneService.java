package com.lunaris.ansenuza.application.usecase;

import com.lunaris.ansenuza.domain.model.OperatorNotificationPhone;
import com.lunaris.ansenuza.domain.repository.OperatorNotificationPhoneRepository;
import com.lunaris.ansenuza.domain.exception.DomainValidationException;
import com.lunaris.ansenuza.shared.ArgentinaTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;

@Service @RequiredArgsConstructor @Transactional
public class OperatorPhoneService {
    private final OperatorNotificationPhoneRepository phones;

    @Transactional(readOnly = true)
    public List<OperatorNotificationPhone> list() { return phones.findAllByOrderByCreatedAtAsc(); }

    public void add(String name, String phone) {
        String normalized = phone == null ? "" : phone.replaceAll("[ +()\\-]", "");
        if (name == null || name.isBlank() || name.trim().length() > 255
                || !normalized.matches("[1-9][0-9]{7,14}")) {
            throw new DomainValidationException("Ingresá un nombre y un teléfono internacional válido (ej. 5493512282251).");
        }
        if (phones.existsByPhone(normalized)) {
            throw new DomainValidationException("El teléfono ya está registrado.");
        }
        var operator = new OperatorNotificationPhone();
        // Igual que Inquiry: Hibernate genera el UUID al persistir una entidad nueva.
        operator.setName(name.trim());
        operator.setPhone(normalized);
        operator.setCreatedAt(ArgentinaTime.now());
        phones.save(operator);
    }

    public void setActive(UUID id, boolean active) {
        var phone = find(id);
        phone.setActive(active);
        phones.save(phone);
    }

    public void delete(UUID id) { phones.delete(find(id)); }

    private OperatorNotificationPhone find(UUID id) {
        return phones.findById(id).orElseThrow(() -> new DomainValidationException("No se encontró el operador."));
    }
}
