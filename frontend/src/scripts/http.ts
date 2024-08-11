import {getSessionToken} from "./session.ts";

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
    let response
    if (body) {
        console.log(`${method} ${requrl}: ${JSON.stringify(body)}`)
        response = await fetch(requrl, {method: method, headers: header, body: JSON.stringify(body)})
    } else {
        console.log(`${method} ${requrl}`)
        response = await fetch(requrl, {method: method, headers: header})
    }

    if (!response.ok) {
        console.error(response.text())
        // todo error handling
        return null
    }

    let resBody = await response.text()
    if (resBody) {
        return JSON.parse(resBody)
    }

    return null

}

async function headers() {
    let header: Headers = new Headers()
    header.append("Authorization", `Bearer ${(await getSessionToken())!}`)
    return header
}

function constructUrl(url: string, params: Map<string, string | number> = new Map()) {
    let req = new URL(url, getHost())
    Object.keys(params)
        .forEach(key => req.searchParams.append(key, String(params.get(key)!)))

    return req
}

export function urlEncode(data: Object) {
    return Object.keys(data)
        // @ts-expect-error
        .map((key: string) => encodeURIComponent(key) + '=' + encodeURIComponent(data[key]))
        .join('&');
}

export function getHost() {
    let backend = import.meta.env.VITE_BACKEND_HOST
    console.log(`Backend: ${backend}`)
    if (backend) {
        console.log("Found backend override")
        return backend
    }
    return "http://localhost:8888"
    return window.location.protocol + window.location.host
}
