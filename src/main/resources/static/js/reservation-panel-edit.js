const returnAction = document.getElementById('returnAction');
const returnMode = document.getElementById('returnMode');
const pricingMode = document.getElementById('pricingMode');

function showField(id, visible, required = false) {
    const field = document.getElementById(id);
    field.hidden = !visible;
    const input = field.querySelector('input, select');
    input.disabled = !visible;
    input.required = visible && required;
}

function updateReturnFields() {
    const plansReturn = ['ADD', 'UPDATE'].includes(returnAction.value);
    const plan = document.getElementById('returnPlanFields');
    plan.hidden = plan.disabled = !plansReturn;
    showField('returnTargetFields', ['UPDATE', 'REMOVE'].includes(returnAction.value), true);
    const scheduled = returnMode.value === 'SCHEDULED';
    const flexible = returnMode.value === 'FLEXIBLE';
    showField('returnDateField', plansReturn && (scheduled || flexible), scheduled);
    showField('returnTimeField', plansReturn && scheduled, true);
    showField('returnPreferenceField', plansReturn && flexible);
}

if (returnAction) {
    returnAction.addEventListener('change', updateReturnFields);
    returnMode.addEventListener('change', updateReturnFields);
    const updatePricing = () => showField('agreedAmountField', pricingMode.value === 'AGREED_TOTAL', true);
    pricingMode.addEventListener('change', updatePricing);
    updateReturnFields();
    updatePricing();
}
