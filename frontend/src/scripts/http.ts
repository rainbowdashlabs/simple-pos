import {getSessionToken} from "./session.ts";

export async function postJson(url: string, body: Object) {
    let header = await headers()
    header.append("Content-Type", "application/json")
    let response = await fetch(constructUrl(url), {method: "POST", headers: header, body: JSON.stringify(body)})

    // todo error handling

    return response.json()
}

export async function getJson(url: string, params: Map<string, string | number> = new Map()) {
    let response = await fetch(constructUrl(url, params), {method: "GET", headers: await headers()})
    return response.json()
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
    console.log(backend)
    if (backend) {
        console.log("Found backend override")
        return backend
    }
    return "http://localhost:8888"
    return window.location.protocol + window.location.host
}
