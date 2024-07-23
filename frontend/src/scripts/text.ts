// Media Queries: sm md lg xl 2xl
// sized
export class SizeGroup {
    static readonly sm: string = "text-sm xl:text-md 2xl:text-lg"
    static readonly md: string = "text-md xl:text-lg 2xl:text-xl"
    static readonly lg: string = "text-lg xl:text-xl 2xl:text-2xl"
    static readonly xl: string = "text-xl xl:text-2xl 2xl:text-3xl"
    static readonly xl2: string = "text-2xl xl:text-3xl 2xl:text-4xl"
    static readonly xl3: string = "text-3xl xl:text-4xl 2xl:text-5xl"
    static readonly xl4: string = "text-4xl xl:text-5xl 2xl:text-6xl"
    static readonly xl5: string = "text-5xl xl:text-6xl 2xl:text-7xl"
}
