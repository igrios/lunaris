const { test } = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const vm = require('node:vm');

const resources = path.join(__dirname, '../../src/main/resources');
const script = fs.readFileSync(path.join(resources, 'static/js/reservation-panel-edit.js'), 'utf8');

function element(value = '') {
    const control = { disabled: false, required: false };
    return {
        value, hidden: false, disabled: false, control,
        querySelector: () => control,
        addEventListener(event, handler) { this[event] = handler; },
    };
}

test('Reservas ofrece editar por UUID y el formulario guarda en el panel', () => {
    const grid = fs.readFileSync(path.join(resources, 'templates/reservations-grid.html'), 'utf8');
    const form = fs.readFileSync(path.join(resources, 'templates/reservation-panel-edit.html'), 'utf8');
    assert.match(grid, /th:href="@\{\/reservas-panel\/\{id\}\/editar\(id=\$\{r\.id\}\)\}"/);
    assert.match(form, /<form method="post" th:action="@\{\/reservas-panel\/\{id\}\/editar/);
    assert.match(form, /type="date" name="travelDate"/);
    assert.match(form, /type="time" name="departureSchedule"/);
    assert.match(form, /th:unless="\$\{state\.operationalOnly\}"/);
    assert.doesNotMatch(form, /admin\/chat|wa\.me|whatsapp\.com/);
});

test('el regreso y el monto acordado envían solo los campos de su modalidad', () => {
    const fields = {
        returnAction: element('KEEP'), returnMode: element('SCHEDULED'), pricingMode: element(),
        returnPlanFields: element(), returnTargetFields: element(), returnDateField: element(),
        returnTimeField: element(), returnPreferenceField: element(), agreedAmountField: element(),
    };
    vm.runInNewContext(script, { document: { getElementById: id => fields[id] } });
    assert.equal(fields.returnPlanFields.disabled, true);
    assert.equal(fields.agreedAmountField.control.disabled, true);

    fields.returnAction.value = 'UPDATE';
    fields.returnAction.change();
    assert.equal(fields.returnTargetFields.control.required, true);
    assert.equal(fields.returnDateField.control.required, true);
    assert.equal(fields.returnTimeField.control.required, true);

    fields.returnMode.value = 'FLEXIBLE';
    fields.returnMode.change();
    assert.equal(fields.returnDateField.control.disabled, false);
    assert.equal(fields.returnDateField.control.required, false);
    assert.equal(fields.returnTimeField.control.disabled, true);
    assert.equal(fields.returnPreferenceField.control.disabled, false);

    fields.returnAction.value = 'REMOVE';
    fields.returnAction.change();
    assert.equal(fields.returnPlanFields.disabled, true);
    assert.equal(fields.returnTargetFields.control.disabled, false);

    fields.pricingMode.value = 'AGREED_TOTAL';
    fields.pricingMode.change();
    assert.equal(fields.agreedAmountField.control.required, true);
});

test('el editor de una reserva cerrada funciona sin controles comerciales', () => {
    assert.doesNotThrow(() => vm.runInNewContext(script, { document: { getElementById: () => null } }));
});
