package com.lunaris.ansenuza.domain.model.service;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import org.springframework.stereotype.Service;

@Service
public class OperationControlService {

    // 🕒 Estado Global del Interruptor de Jornada (true = abierto/acción humana, false = piloto automático 24/7)
    private final AtomicBoolean humanActionEnabled = new AtomicBoolean(true);

    // ⚖️ Lista estática de operadores actuales para el Load Balancer
    private final List<String> availableOperators = Arrays.asList("martin", "operador2");

    // 📊 Contador en memoria de chats asignados por operador para el cálculo rápido de carga mínima
    private final ConcurrentHashMap<String, Integer> operatorLoadMap = new ConcurrentHashMap<>();

    public OperationControlService() {
        // Inicializamos los operadores con carga cero al levantar el sistema
        for (String op : availableOperators) {
            operatorLoadMap.put(op, 0);
        }
    }

    // 🔄 MÉTODOS PARA EL INTERRUPTOR DE JORNADA LABORAL
    public boolean isHumanActionEnabled() {
        return humanActionEnabled.get();
    }

    public void setHumanActionEnabled(boolean enabled) {
        this.humanActionEnabled.set(enabled);
    }

    // ⚖️ LOGICA DEL LOAD BALANCER: Devuelve el operador con menos carga actual
    public String getOperatorWithLeastLoad() {
        String leastLoadedOperator = "martin"; // fallback por defecto
        int minLoad = Integer.MAX_VALUE;

        for (String operator : availableOperators) {
            int currentLoad = operatorLoadMap.getOrDefault(operator, 0);
            if (currentLoad < minLoad) {
                minLoad = currentLoad;
                leastLoadedOperator = operator;
            }
        }
        
        // Simulamos un incremento de carga al asignarlo
        operatorLoadMap.put(leastLoadedOperator, minLoad + 1);
        return leastLoadedOperator;
    }

    // Libera carga cuando un chat se cierra o se archiva
    public void releaseOperatorLoad(String operator) {
        if (operator != null && operatorLoadMap.containsKey(operator)) {
            operatorLoadMap.computeIfPresent(operator, (k, v) -> Math.max(0, v - 1));
        }
    }

    // ⏱️ VALIDACIÓN DE HORA DE CORTE (Deadline de las 19:00 Hs)
    public boolean isPastCutoffTime() {
        LocalTime ahora = LocalTime.now();
        LocalTime horaCorte = LocalTime.of(19, 0); // 19:00 Hs
        return ahora.isAfter(horaCorte);
    }
}
