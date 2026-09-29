-- Desglose para nuevas liquidaciones. NULL identifica órdenes históricas sin snapshot.
ALTER TABLE interurban.payout_orders
    ADD COLUMN gross_amount NUMERIC(14,2),
    ADD COLUMN commission_amount NUMERIC(14,2),
    ADD COLUMN adjustment_amount NUMERIC(14,2),
    ADD CONSTRAINT interurban_payout_breakdown CHECK (
        (gross_amount IS NULL AND commission_amount IS NULL AND adjustment_amount IS NULL)
        OR (gross_amount IS NOT NULL AND commission_amount IS NOT NULL AND adjustment_amount IS NOT NULL
            AND gross_amount >= 0 AND commission_amount >= 0
            AND amount = gross_amount - commission_amount + adjustment_amount));
