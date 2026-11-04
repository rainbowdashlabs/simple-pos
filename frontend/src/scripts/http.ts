import {getSessionToken, handleUnauthorized, logout} from "./session.ts";
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

/**
 * Sends a request with the session's login token and returns the parsed JSON body, or null for an empty body.
 * A 401 refreshes the session and retries the request once.
 *
 * @throws ApiError for every failed request: status 0 if the server is unreachable, otherwise the response status
 */
export async function requestJson(url: string, method: string, params: Map<string, string | number> = new Map(), body: Object | null = null) {
    return send(constructUrl(url, params), method, body, true)
}

async function send(requrl: string, method: string, body: Object | null, retryUnauthorized: boolean): Promise<any> {
    const token = await getSessionToken()
    if (!token) {
        logout()
        throw new ApiError(401, "Not logged in")
    }
    let header = new Headers()
    header.append("Authorization", `Bearer ${token}`)
    header.append("Content-Type", "application/json")
    console.log(body ? `${method} ${requrl}: ${JSON.stringify(body)}` : `${method} ${requrl}`)
    let response: Response
    try {
        response = await fetch(requrl, {method: method, headers: header, body: body ? JSON.stringify(body) : undefined})
    } catch (e) {
        throw ApiError.network(method, requrl, e)
    }

    if (!response.ok) {
        const error = await ApiError.fromResponse(method, requrl, response)
        if (error.isAccountDisabled) logout()
        else if (error.status === 401) {
            if (!retryUnauthorized) logout()
            else if (await handleUnauthorized(token)) return send(requrl, method, body, false)
        }
        throw error
    }

    let resBody = await response.text()
    if (resBody) {
        return JSON.parse(resBody)
    }

    return null

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
