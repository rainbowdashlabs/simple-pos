/**
 * Fails when a translation key is used in the code but missing in a locale, or present in one locale only. Keys built
 * at runtime (`$t(variable)`) cannot be checked and are skipped.
 */
import {readFileSync, readdirSync, statSync} from "node:fs"
import {join} from "node:path"
import {fileURLToPath} from "node:url"

const root = fileURLToPath(new URL("../src/", import.meta.url))
const locales = Object.fromEntries(["en", "de"].map(l => [l, flatten(JSON.parse(readFileSync(join(root, "locales", `${l}.json`), "utf8")))]))

function flatten(object, prefix = "") {
    return Object.entries(object).flatMap(([key, value]) =>
        typeof value === "object" ? flatten(value, `${prefix}${key}.`) : [`${prefix}${key}`])
}

function files(dir) {
    return readdirSync(dir).flatMap(name => {
        const path = join(dir, name)
        if (statSync(path).isDirectory()) return files(path)
        return /\.(vue|ts)$/.test(name) ? [path] : []
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
const [en, de] = [new Set(locales.en), new Set(locales.de)]
for (const key of en) if (!de.has(key)) problems.push(`"${key}" is only in en`)
for (const key of de) if (!en.has(key)) problems.push(`"${key}" is only in de`)

if (problems.length) {
    console.error([...new Set(problems)].join("\n"))
    process.exit(1)
}
