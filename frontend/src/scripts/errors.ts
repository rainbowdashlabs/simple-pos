/**
 * The error body the backend sends for every failed request (RFC 9457).
 */
export interface ProblemDetail {
    type?: string
    title?: string
    status?: number
    detail?: string
    instance?: string

    [property: string]: unknown
}

/**
 * A failed request to the backend. `status` is 0 when the backend could not be reached at all.
 */
export class ApiError extends Error {
    readonly status: number
    readonly problem: ProblemDetail | null

    constructor(status: number, message: string, problem: ProblemDetail | null = null) {
        super(message)
        this.name = "ApiError"
        this.status = status
        this.problem = problem
    }

    get isNetworkError(): boolean {
        return this.status === 0
    }

    /**
     * True for the 403 the backend sends when the user's account is disabled.
     */
    get isAccountDisabled(): boolean {
        return this.status === 403 && this.problem?.accountDisabled === true
    }

    /**
     * Reads the ProblemDetail body of a failed response, falling back to the status text when the body is not one.
     */
    static async fromResponse(method: string, url: string, response: Response): Promise<ApiError> {
        const problem = await readProblem(response)
        const message = problem?.detail ?? problem?.title ?? (response.statusText || `HTTP ${response.status}`)
        console.error(`${method} ${url} failed with ${response.status}: ${message}`, problem)
        return new ApiError(response.status, message, problem)
    }

    static network(method: string, url: string, cause: unknown): ApiError {
        console.error(`${method} ${url} could not reach the server`, cause)
        return new ApiError(0, String(cause))
    }
}

async function readProblem(response: Response): Promise<ProblemDetail | null> {
    try {
        const text = await response.text()
        return text ? JSON.parse(text) as ProblemDetail : null
    } catch {
        return null
    }
}
