import {getSessionToken, handleUnauthorized} from "./session.ts";
import {ApiError} from "./errors.ts";

export async function postJson(url: string, body: Object) {
    return requestJson(url, "POST", new Map(), body)
}

export async function putJson(url: string, body: Object) {
    return requestJson(url, "PUT", new Map(), body)
}

export async function patchJson(url: string, body: Object) {
    return requestJson(url, "PATCH", new Map(), body)
}

export async function getJson(url: string, params: Map<string, string | number> = new Map()) {
    return requestJson(url, "GET", params, null)
}
export async function deleteJson(url: string, params: Map<string, string | number> = new Map()) {
    return requestJson(url, "DELETE", params, null)
}

export async function requestJson(url: string, method: string, params: Map<string, string | number> = new Map(), body: Object | null = null) {
    let header = await headers()
    header.append("Content-Type", "application/json")
    let requrl = constructUrl(url, params);
    let response: Response
    console.log(body ? `${method} ${requrl}: ${JSON.stringify(body)}` : `${method} ${requrl}`)
    try {
        response = await fetch(requrl, {method: method, headers: header, body: body ? JSON.stringify(body) : undefined})
    } catch (e) {
        throw ApiError.network(method, requrl, e)
    }

    if (!response.ok) {
        const error = await ApiError.fromResponse(method, requrl, response)
        if (error.status === 401) handleUnauthorized()
        throw error
    }

    let resBody = await response.text()
    if (resBody) {
        return JSON.parse(resBody)
    }

    return null

}

async function headers() {
    let header: Headers = new Headers()
    const token = await getSessionToken()
    if (token) header.append("Authorization", `Bearer ${token}`)
    return header
}

function constructUrl(url: string, params: Map<string, string | number> = new Map()) {
    let req = new URL(url, getHost())
    params.forEach((v,k) => req.searchParams.append(k,String(v)))
    return req.toString()
}

export function urlEncode(data: Object) {
    return Object.keys(data)
        // @ts-expect-error
        .map((key: string) => encodeURIComponent(key) + '=' + encodeURIComponent(data[key]))
        .join('&');
}

export function getHost() {
    //return "http://localhost:8888"
    return window.location.protocol + "//" + window.location.host
}
