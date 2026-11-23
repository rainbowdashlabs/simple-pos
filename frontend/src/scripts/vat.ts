import {getJson, patchJson, postJson, putJson} from "./http.ts";

/**
 * A VAT rate in percent. Exactly one active rate is the default, used wherever no other rate is chosen.
 */
export interface VatRate {
    id: number
    name: string
    rate: number
    isDefault: boolean
    active: boolean
}

export interface VatRateRequest {
    name: string
    rate: number
}

/**
 * The active rates, highest first.
 */
export function activeVatRates(): Promise<VatRate[]> {
    return getJson("api/vat-rates")
}

/**
 * Every rate, active ones first. Administrators only.
 */
export function allVatRates(): Promise<VatRate[]> {
    return getJson("api/admin/vat-rates")
}

export function createVatRate(request: VatRateRequest): Promise<VatRate> {
    return postJson("api/admin/vat-rates", request)
}

export function updateVatRate(id: number, request: VatRateRequest): Promise<VatRate> {
    return patchJson(`api/admin/vat-rates/${id}`, request)
}

export function makeDefaultVatRate(id: number): Promise<VatRate> {
    return putJson(`api/admin/vat-rates/${id}/default`, {})
}

export function toggleVatRateActive(id: number): Promise<VatRate> {
    return patchJson(`api/admin/vat-rates/${id}/active`, {})
}
