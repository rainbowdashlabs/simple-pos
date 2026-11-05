/**
 * Fails when a translation key is used in the code but missing in a locale, or present in one locale only. Keys built
 * at runtime (`$t(variable)`) cannot be checked and are skipped; the ones the backend's error codes and entity names
 * lead to are read from its sources and checked as well, whenever the backend is present (not in the frontend's Docker
 * build stage, which sees only the frontend); a backend whose codes or entity names cannot be found fails the check.
 */
import {existsSync, readFileSync, readdirSync, statSync} from "node:fs"
import {join} from "node:path"
import {fileURLToPath} from "node:url"

const root = fileURLToPath(new URL("../src/", import.meta.url))
const locales = Object.fromEntries(["en", "de"].map(l => [l, flatten(JSON.parse(readFileSync(join(root, "locales", `${l}.json`), "utf8")))]))

function flatten(object, prefix = "") {
    return Object.entries(object).flatMap(([key, value]) =>
        typeof value === "object" ? flatten(value, `${prefix}${key}.`) : [`${prefix}${key}`])
}

function files(dir, pattern = /\.(vue|ts)$/) {
    return readdirSync(dir).flatMap(name => {
        const path = join(dir, name)
        if (statSync(path).isDirectory()) return files(path, pattern)
        return pattern.test(name) ? [path] : []
    })
}

const usage = [
    /(?:\$t|\bt|translate)\(\s*['"]([\w.]+)['"]/g,
    /\bpush\(\s*null\s*,\s*['"]([\w.]+)['"]/g,
    /(?<![:\w-])value="([\w.]+)"[^>]*\btype="locale"/g,
    /\btype="locale"[^>]*(?<![:\w-])value="([\w.]+)"/g,
    /\btitleKey:\s*['"]([\w.]+)['"]/g,
]

const problems = []
for (const file of files(root)) {
    const text = readFileSync(file, "utf8")
    const used = [
        ...[...text.matchAll(/(?:\$t|\bt|translate)\(([^()]*)\)/g)].flatMap(call => [...call[1].matchAll(/['"]([\w.]+)['"]/g)]),
        ...usage.flatMap(pattern => [...text.matchAll(pattern)]),
    ].map(match => match[1]).filter(key => !key.endsWith("."))
    for (const key of used) {
        for (const [locale, keys] of Object.entries(locales)) {
            if (!keys.includes(key)) problems.push(`${file.replace(root, "")}: "${key}" is missing in ${locale}`)
        }
    }
}
const backendRoot = fileURLToPath(new URL("../../backend/", import.meta.url))
const backend = join(backendRoot, "src/main/java/dev/chojo/simplepos/")
if (existsSync(backendRoot)) {
    const errorCodes = readFileSync(join(backend, "exceptions", "ErrorCode.java"), "utf8")
    const codes = [...errorCodes.matchAll(/^\s{4}([A-Z][A-Z_]+)\(/gm)].map(match => match[1].toLowerCase())
    if (codes.length === 0) problems.push("No error codes found in ErrorCode.java")
    const entityNames = files(backend, /\.java$/)
        .flatMap(file => [...readFileSync(file, "utf8").matchAll(/(?:new NotFoundException\(|String entityName\(\) \{\s*return )"(\w+)"/g)])
        .map(match => match[1].toLowerCase())
    if (entityNames.length === 0) problems.push("No entity names found in the backend sources")
    for (const key of [...codes.map(code => `errors.code.${code}`), ...entityNames.map(name => `entities.${name}`)]) {
        for (const [locale, keys] of Object.entries(locales)) {
            if (!keys.includes(key)) problems.push(`backend: "${key}" is missing in ${locale}`)
        }
    }
}

const [en, de] = [new Set(locales.en), new Set(locales.de)]
for (const key of en) if (!de.has(key)) problems.push(`"${key}" is only in en`)
for (const key of de) if (!en.has(key)) problems.push(`"${key}" is only in de`)

if (problems.length) {
    console.error([...new Set(problems)].join("\n"))
    process.exit(1)
}
